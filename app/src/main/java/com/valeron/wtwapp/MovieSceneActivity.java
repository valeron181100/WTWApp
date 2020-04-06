package com.valeron.wtwapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import jp.wasabeef.picasso.transformations.BlurTransformation;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.valeron.wtwapp.network.HttpRequestSender;
import com.valeron.wtwapp.network.api.ApiConst;
import com.valeron.wtwapp.network.api.Movie;
import com.valeron.wtwapp.utils.AndroidUtils;
import com.valeron.wtwapp.views.TextProgressBar;

import java.io.IOException;

public class MovieSceneActivity extends AppCompatActivity {

    public static final String MOVIE_SCENE_INTENT_KEY = "com.valeron.wtwapp.MovieSceneIntentTag";

    HttpRequestSender mRequestSender;
    Handler mResponseHandler;

    private ImageView mMovieCoverIV;
    private ConstraintLayout mMovieMainRL;
    private ImageView mMovieBackDropIV;
    private Movie mMovie;
    private TextProgressBar mMovieRateBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_scene);

        Intent intent = getIntent();
        if(intent == null){
            Toast.makeText(this, "Упс, произошло что-то ужасное!", Toast.LENGTH_SHORT).show();
            finish();
        }

        mResponseHandler = new Handler();
        mRequestSender = new HttpRequestSender(this, mResponseHandler);
        mRequestSender.start();

        mMovie = (Movie) intent.getSerializableExtra(MOVIE_SCENE_INTENT_KEY);

        mMovieCoverIV = findViewById(R.id.movieCoverIV);

        mMovieMainRL = findViewById(R.id.movieMainRL);

        mMovieBackDropIV = findViewById(R.id.movieBackdropIV);

        mMovieRateBar = findViewById(R.id.movieRateBar);

        Picasso.with(this).load(ApiConst.API_IMAGE_HOST + mMovie.getBackdropPath())
                .transform(new BlurTransformation(this, 10, 1)).into(mMovieBackDropIV);

        mRequestSender.sendRequest("GET", ApiConst.API_IMAGE_HOST + mMovie.getPosterPath())
                .setOnRequestReadyEvent(new HttpRequestSender.OnRequestReadyEvent() {
            @Override
            public void ready(byte[] stream, Exception e) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(stream, 0, stream.length);
                mMovieCoverIV.setImageBitmap(AndroidUtils.getRoundedBitMap(bitmap, 10));
            }
        });

        int voteAverage = (int)(mMovie.getVoteAverage() * 10);
        Toast.makeText(this, String.valueOf(voteAverage), Toast.LENGTH_SHORT).show();
        mMovieRateBar.setProgress(voteAverage);
        mMovieRateBar.setText(voteAverage + "%");

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        mRequestSender.clearQueue();
        mRequestSender.quit();
    }
}
