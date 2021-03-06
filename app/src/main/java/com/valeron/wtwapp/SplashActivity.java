package com.valeron.wtwapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.method.PasswordTransformationMethod;
import android.transition.ChangeBounds;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.view.ViewGroup;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.valeron.wtwapp.network.HttpRequestSender;
import com.valeron.wtwapp.network.api.UserAuthenticator;
import com.valeron.wtwapp.views.SwipeButton;

import java.util.Timer;
import java.util.TimerTask;

public class SplashActivity extends AppCompatActivity {
    public static final String TAG = SplashActivity.class.getSimpleName();
    ImageView logoIV;
    ConstraintLayout mainCL;
    SwipeButton mLoginButton;
    SwipeButton mRegButton;
    Handler mResponseHandler;
    HttpRequestSender mRequestSender;
    UserAuthenticator mAuthenticator;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        mResponseHandler = new Handler();
        mRequestSender = new HttpRequestSender(SplashActivity.this, mResponseHandler);
        mRequestSender.start();
        mAuthenticator = UserAuthenticator.getInstance(this, mRequestSender);
        mLoginButton = findViewById(R.id.loginSwipeButton);
        mRegButton = findViewById(R.id.regSwipeButton);
        logoIV = findViewById(R.id.logoIV);
        mainCL = findViewById(R.id.splashConstraintLayout);
        Timer timer = new Timer();
        if(!mAuthenticator.isLoggedIn()) {
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    mainCL.post(new Runnable() {
                        @Override
                        public void run() {
                            ConstraintSet constraintSet = new ConstraintSet();
                            constraintSet.clone(SplashActivity.this, R.layout.activity_splash_anim_1);
                            constraintSet.applyTo(mainCL);
                            ChangeBounds transition = new ChangeBounds();
                            transition.setInterpolator(new AnticipateInterpolator(1.0f));
                            transition.setDuration(300);
                            transition.addListener(new Transition.TransitionListener() {
                                @Override
                                public void onTransitionStart(Transition transition) {

                                }

                                @Override
                                public void onTransitionEnd(Transition transition) {
                                    ConstraintSet constraintSet = new ConstraintSet();
                                    constraintSet.clone(SplashActivity.this, R.layout.activity_splash_anim_2);
                                    constraintSet.applyTo(mainCL);
                                    transition = new ChangeBounds();
                                    transition.setInterpolator(new AnticipateInterpolator(1.0f));
                                    transition.setDuration(300);
                                    TransitionManager.beginDelayedTransition(mainCL, transition);
                                }

                                @Override
                                public void onTransitionCancel(Transition transition) {

                                }

                                @Override
                                public void onTransitionPause(Transition transition) {

                                }

                                @Override
                                public void onTransitionResume(Transition transition) {

                                }
                            });
                            TransitionManager.beginDelayedTransition(mainCL, transition);
                        }
                    });
                }
            }, 500);

            mLoginButton.addOnSwipedListener(new SwipeButton.OnSwipedListener() {
                @Override
                public void swiped() {
                    mAuthenticator.sendAuthRequestAsync();
                }
            });
        }
        else{
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                    startActivity(intent);
                }
            }, 500);
        }

    }
}
