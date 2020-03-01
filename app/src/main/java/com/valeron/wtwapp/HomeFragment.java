package com.valeron.wtwapp;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.valeron.wtwapp.network.HttpRequestSender;
import com.valeron.wtwapp.network.api.MovieBank;
import com.valeron.wtwapp.views.HomeMoviesAdapter;
import com.valeron.wtwapp.views.ItemOffsetDecoration;

public class HomeFragment extends Fragment {

    private static final String REQUEST_SENDER_CONST = "HTTP_REQUEST_SENDER_FRAGMENT";

    private HttpRequestSender mRequsetSender;
    private RecyclerView mTheaterMoviesRV;
    private RecyclerView.LayoutManager mTheaterMoviesLM;
    private HomeMoviesAdapter mTheaterMoviesAdapter;
    private MovieBank mMovieBank;

    private HomeFragment(){}

    public static HomeFragment newInstance(HttpRequestSender requestSender) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putSerializable(REQUEST_SENDER_CONST, requestSender);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mRequsetSender = (HttpRequestSender)getArguments().getSerializable(REQUEST_SENDER_CONST);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v =  inflater.inflate(R.layout.fragment_home, container, false);

        mMovieBank = MovieBank.getInstance(this.getContext(), mRequsetSender);

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
