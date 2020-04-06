package com.valeron.wtwapp.network.api;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.valeron.wtwapp.CheckAuthActivity;
import com.valeron.wtwapp.MainActivity;
import com.valeron.wtwapp.network.HttpRequestSender;

import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import static com.valeron.wtwapp.network.api.ApiConst.*;

public class UserAuthenticator{
    private Context mContext;
    private HttpRequestSender requestSender;
    private Handler responseHandler;
    private String requestToken = "none";
    private String sessionId;
    private OnLogged mOnLogged;
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
        SharedPreferences preferences = mContext.getSharedPreferences(APP_PREFFERENCES_NAME, Context.MODE_PRIVATE);
        sessionId = preferences.getString(API_SESSION_ID_KEY, null);
    }

    public void sendAuthRequestAsync(){
        String url = API_HOST + "/authentication/token/new?" + API_KEY;

        requestSender.sendRequest("GET",url).setOnRequestReadyEvent(new HttpRequestSender.OnRequestReadyEvent() {
            @Override
            public void ready(byte[] response, Exception e) {
                try (ByteArrayInputStream stream =new ByteArrayInputStream(response)){

                    JSONObject jsonObject = new JSONObject(IOUtils.toString(stream));
                    requestToken = jsonObject.getString("request_token");
                    askAuthPermission(requestToken);
                } catch (JSONException ex) {
                    //TODO: handling exception
                    ex.printStackTrace();
                } catch (IOException ex) {
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

    public UserAuthenticator checkAuthAsync(){
        String url = API_HOST + "/authentication/session/new?" + API_KEY;
        final boolean[] isLoggedIn = {false};
        requestSender.sendRequest("POST", url, "request_token=" + this.requestToken)
            .setOnRequestReadyEvent(new HttpRequestSender.OnRequestReadyEvent() {
                @Override
                public void ready(byte[] response, Exception e) {
                    try(ByteArrayInputStream stream =new ByteArrayInputStream(response)) {
                        String responseStr = IOUtils.toString(stream);
                        Log.d("HTTP_REQUEST", responseStr);
                        JSONObject jsonObject = new JSONObject(responseStr);
                        if(responseStr.contains("success")){
                            MovieBank.getInstance(mContext, requestSender);
                            Toast.makeText(mContext, "LoggedIn", Toast.LENGTH_SHORT).show();
                            UserAuthenticator.this.sessionId = jsonObject.getString("session_id");

                            //Saving sessionId in memory

                            mContext.getSharedPreferences(APP_PREFFERENCES_NAME, Context.MODE_PRIVATE).edit()
                                    .putString(API_SESSION_ID_KEY, UserAuthenticator.this.sessionId).apply();

                            if(mOnLogged != null)
                                mOnLogged.logged();
                            //sending intent to MainActivity
                        }else{
                            Toast.makeText(mContext, "unLoggedIn", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException ex) {
                        //TODO: handling exception
                        ex.printStackTrace();
                    } catch (IOException ex) {
                        //TODO: handling exception
                        ex.printStackTrace();
                    }
                    isLoggedIn[0] = true;
                }
            });
        return this;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public boolean isLoggedIn(){
        return sessionId != null;
    }

    public void setOnLoggedListener(OnLogged onLogged) {
        mOnLogged = onLogged;
    }

    public interface OnLogged{
        void logged();
    }

}
