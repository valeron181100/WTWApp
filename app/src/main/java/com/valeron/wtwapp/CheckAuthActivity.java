package com.valeron.wtwapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;

import com.valeron.wtwapp.network.HttpRequestSender;
import com.valeron.wtwapp.network.api.MovieBank;
import com.valeron.wtwapp.network.api.UserAuthenticator;

public class CheckAuthActivity extends AppCompatActivity {

    Button mCheckButton;
    HttpRequestSender mRequestSender;
    Handler mResponseHandler;
    UserAuthenticator mAuthenticator;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_auth);

        mResponseHandler = new Handler();
        mRequestSender = new HttpRequestSender(this, mResponseHandler);
        mRequestSender.start();

        mAuthenticator = UserAuthenticator.getInstance(this, mRequestSender);

        mCheckButton = findViewById(R.id.checkAuthButton);
        mCheckButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuthenticator.checkAuthAsync().setOnLoggedListener(new UserAuthenticator.OnLogged() {
                    @Override
                    public void logged() {
                        MovieBank.getInstance(CheckAuthActivity.this, CheckAuthActivity.this.mRequestSender).setOnTheaterMoviesLoaded(new MovieBank.onTheaterMoviesLoaded() {
                            @Override
                            public void loaded() {

                                Intent intent = new Intent(CheckAuthActivity.this, MainActivity.class);
                                startActivity(intent);
                            }
                        });
                    }
                });

            }
        });
    }
}
