package com.valeron.wtwapp.views;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.valeron.wtwapp.MovieSceneActivity;
import com.valeron.wtwapp.R;
import com.valeron.wtwapp.models.ImageCache;
import com.valeron.wtwapp.network.HttpRequestSender;
import com.valeron.wtwapp.network.api.ApiConst;
import com.valeron.wtwapp.network.api.Movie;
import com.valeron.wtwapp.network.api.MovieBank;
import com.valeron.wtwapp.utils.AndroidUtils;

import java.io.IOException;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class HomeMoviesAdapter extends RecyclerView.Adapter<HomeMoviesAdapter.HomeMoviesViewHolder> {

    private Context mContext;

    private MovieBank mMovieBank;

    private HttpRequestSender mRequestSender;

    public HomeMoviesAdapter(Context context, MovieBank movieBank, HttpRequestSender requestSender){
        mContext = context;
        mMovieBank = movieBank;
        mRequestSender = requestSender;
    }

    @NonNull
    @Override
    public HomeMoviesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.template_movie, parent, false);
        return new HomeMoviesViewHolder(mContext, v, mRequestSender);
    }

    @Override
    public void onBindViewHolder(@NonNull HomeMoviesViewHolder holder, int position) {
        holder.bind(mMovieBank.getTheaterMovies().get(position));
    }

    @Override
    public int getItemCount() {
        return mMovieBank.getTheaterMovies().size();
    }

    public static class HomeMoviesViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        private ImageView mMovieIV;
        private Movie mMovie;
        private Context mContext;
        private HttpRequestSender mRequestSender;

        public HomeMoviesViewHolder(Context context, @NonNull View itemView, HttpRequestSender requestSender) {
            super(itemView);
            itemView.setOnClickListener(this);
            mMovieIV = itemView.findViewById(R.id.movieIV);
            mContext = context;
            mRequestSender = requestSender;
        }

        public void bind(Movie movie){
            mMovie = movie;
            mMovieIV.setImageResource(R.drawable.ic_logo_img);
            final Bitmap poster = ImageCache.getInstance().get(mMovie.getId());
            String url = ApiConst.API_IMAGE_HOST + mMovie.getPosterPath();
            if(poster == null){
                Log.d("CACHE", "Downloaded");
                mRequestSender.sendRequest("GET", url).setOnRequestReadyEvent(new HttpRequestSender.OnRequestReadyEvent() {
                    @Override
                    public void ready(byte[] stream, Exception e) {
                        Bitmap bitmap = BitmapFactory.decodeByteArray(stream, 0, stream.length);
                        Bitmap bitmapRounded = AndroidUtils.getRoundedBitMap(bitmap, 10);
                        ImageCache.getInstance().put(mMovie.getId(), bitmapRounded);
                        mMovieIV.setImageBitmap(bitmapRounded);
                    }
                });
            }
            else{
                Log.d("CACHE", "Gotted");
                mMovieIV.setImageBitmap(poster);
            }

        }

        @Override
        public void onClick(View v) {
            if(mMovie != null){
                Toast.makeText(v.getContext(), String.valueOf(mMovie.getTitle()), Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(mContext, MovieSceneActivity.class);
                intent.putExtra(MovieSceneActivity.MOVIE_SCENE_INTENT_KEY, mMovie);
                mContext.startActivity(intent);
            }
        }
    }
}
