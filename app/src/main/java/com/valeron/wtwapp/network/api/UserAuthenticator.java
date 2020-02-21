package com.valeron.wtwapp.network.api;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.valeron.wtwapp.CheckAuthActivity;
import com.valeron.wtwapp.network.HttpRequestSender;

import org.json.JSONException;
import org.json.JSONObject;

public class UserAuthenticator{
    public static final String API_HOST = "https://api.themoviedb.org/3";
    public static final String API_KEY = "api_key=80238c20c7cf130b707b5e596c35aac2";

    private Context mContext;
    private HttpRequestSender requestSender;
    private Handler responseHandler;
    private String requestToken = "none";
    private String sessionId;
    private static UserAuthenticator instance;

    public static UserAuthenticator getInstance(Context context, HttpRequestSender sender){
        if(instance == null){
            instance = new UserAuthenticator(context, sender);
        }
        return instance;
    }

    private UserAuthenticator(Context context, HttpRequestSender sender){
        mContext = context;
        responseHandler = new Handler();
        requestSender = sender;
    }

    public void sendAuthRequestAsync(){
        String url = API_HOST + "/authentication/token/new?" + API_KEY;

        requestSender.sendRequest("GET",url).setOnRequestReadyEvent(new HttpRequestSender.OnRequestReadyEvent() {
            @Override
            public void ready(String response, Exception e) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    requestToken = jsonObject.getString("request_token");
                    askAuthPermission(requestToken);
                } catch (JSONException ex) {
                    //TODO: handling exception
                    ex.printStackTrace();
                }
            }
        });
    }

    private void askAuthPermission(String requestToken){
        String url = "https://www.themoviedb.org/authenticate/" + requestToken + "?redirect_to=https://se.ifmo.ru/~s264444/checkauth.com";
        Intent checkAuthIntent = new Intent(mContext, CheckAuthActivity.class);
        mContext.startActivity(checkAuthIntent);
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        mContext.startActivity(browserIntent);
    }

    public boolean checkAuthAsync(){
        String url = API_HOST + "/authentication/session/new?" + API_KEY;
        final boolean[] isLoggedIn = {false};
        requestSender.sendRequest("POST", url, "request_token=" + this.requestToken)
            .setOnRequestReadyEvent(new HttpRequestSender.OnRequestReadyEvent() {
                @Override
                public void ready(String response, Exception e) {
                    try {
                        Log.d("HTTP_REQUEST", response);
                        JSONObject jsonObject = new JSONObject(response);
                        if(response.contains("success")){
                            Toast.makeText(mContext, "LoggedIn", Toast.LENGTH_SHORT).show();
                            UserAuthenticator.this.sessionId = jsonObject.getString("session_id");
                            //TODO: sending intent to MainActivity
                        }else{
                            Toast.makeText(mContext, "unLoggedIn", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException ex) {
                        //TODO: handling exception
                        ex.printStackTrace();
                    }
                    isLoggedIn[0] = true;
                }
            });
        return true;
    }



}
