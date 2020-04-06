package com.valeron.wtwapp;

import android.opengl.Visibility;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.valeron.wtwapp.network.HttpRequestSender;
import com.valeron.wtwapp.network.api.MovieBank;
import com.valeron.wtwapp.views.HomeMoviesAdapter;
import com.valeron.wtwapp.views.ItemOffsetDecoration;

public class HomeFragment extends Fragment {


    private HttpRequestSender mRequsetSender;

    private Handler mResponseHandler;
    private RecyclerView mTheaterMoviesRV;
    private RecyclerView.LayoutManager mTheaterMoviesLM;
    private HomeMoviesAdapter mTheaterMoviesAdapter;
    private MovieBank mMovieBank;

    private ProgressBar mTheaterMoviesPB;

    private View v;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mResponseHandler = new Handler();
        mRequsetSender = new HttpRequestSender(this.getContext(), mResponseHandler);
        mRequsetSender.start();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        v =  inflater.inflate(R.layout.fragment_home, container, false);

        mTheaterMoviesPB = v.findViewById(R.id.theaterMoviesProgressBar);

        mMovieBank = MovieBank.getInstance(this.getContext(), mRequsetSender);

        if(!mMovieBank.isTheaterMoviesLoaded()){
            mTheaterMoviesPB.setVisibility(View.VISIBLE);
            mMovieBank.setOnTheaterMoviesLoaded(new MovieBank.onTheaterMoviesLoaded() {
                @Override
                public void loaded() {
                    mTheaterMoviesPB.setVisibility(View.GONE);
                    mTheaterMoviesRV = v.findViewById(R.id.homeTheaterRV);
                    mTheaterMoviesRV.setHasFixedSize(true);
                    mTheaterMoviesAdapter = new HomeMoviesAdapter(v.getContext(), mMovieBank, mRequsetSender);
                    mTheaterMoviesLM = new LinearLayoutManager(v.getContext(), LinearLayoutManager.HORIZONTAL, false);
                    mTheaterMoviesRV.setLayoutManager(mTheaterMoviesLM);
                    mTheaterMoviesRV.setAdapter(mTheaterMoviesAdapter);
                    mTheaterMoviesRV.addItemDecoration(new ItemOffsetDecoration(10));
                }
            });
        }

        mTheaterMoviesRV = v.findViewById(R.id.homeTheaterRV);
        mTheaterMoviesRV.setHasFixedSize(true);
        mTheaterMoviesAdapter = new HomeMoviesAdapter(this.getContext(), mMovieBank, mRequsetSender);
        mTheaterMoviesLM = new LinearLayoutManager(this.getContext(), LinearLayoutManager.HORIZONTAL, false);
        mTheaterMoviesRV.setLayoutManager(mTheaterMoviesLM);
        mTheaterMoviesRV.setAdapter(mTheaterMoviesAdapter);
        mTheaterMoviesRV.addItemDecoration(new ItemOffsetDecoration(10));
        return v;
    }
}
