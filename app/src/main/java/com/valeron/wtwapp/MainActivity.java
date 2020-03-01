package com.valeron.wtwapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.os.Bundle;
import android.os.Handler;

import com.valeron.wtwapp.network.HttpRequestSender;
import com.valeron.wtwapp.network.api.UserAuthenticator;

public class MainActivity extends AppCompatActivity {

    HttpRequestSender mRequestSender;
    Handler mResponseHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mResponseHandler = new Handler();
        mRequestSender = new HttpRequestSender(this, mResponseHandler);
        mRequestSender.start();

        FragmentManager fm = this.getSupportFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.mainFrame);
        if(fragment == null){
            fragment = HomeFragment.newInstance(mRequestSender);
        }
        fm.beginTransaction().replace(R.id.mainFrame, fragment).commit();
    }
}
