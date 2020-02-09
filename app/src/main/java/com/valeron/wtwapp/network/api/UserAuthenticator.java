package com.valeron.wtwapp.network.api;

import android.content.Context;
import android.os.Handler;

import com.valeron.wtwapp.network.HttpRequestSender;

import org.json.JSONException;
import org.json.JSONObject;

public class UserAuthenticator{
    public static final String API_HOST = "https://api.themoviedb.org/3";
    public static final String API_KEY = "api_key=80238c20c7cf130b707b5e596c35aac2";

    private Context mContext;
    private HttpRequestSender requestSender;
    private Handler responseHandler;
    private String requestToken;

    public UserAuthenticator(Context context, HttpRequestSender sender){
        mContext = context;
        responseHandler = new Handler();
        requestSender = sender;
    }

    private void getRequestTokenAsync(){
        String url = API_HOST + "/authentication/token/new?" + API_KEY;

        requestSender.sendRequest("GET",url).setOnRequestReadyEvent(new HttpRequestSender.OnRequestReadyEvent() {
            @Override
            public void ready(String response, Exception e) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    requestToken = jsonObject.getString("request_token");
                    //TODO: next step
                } catch (JSONException ex) {
                    //TODO: handling exception
                    ex.printStackTrace();
                }

            }
        });
    }

    private void askAuthPermissionAsync(String requestToken){
        String url = "https://www.themoviedb.org/authenticate/" + requestToken;
        requestSender.sendRequest("GET",url).setOnRequestReadyEvent(new HttpRequestSender.OnRequestReadyEvent() {
            @Override
            public void ready(String response, Exception e) {

            }
        });
    }



}
