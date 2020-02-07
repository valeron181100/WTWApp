package com.valeron.wtwapp.network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;
import android.util.Pair;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import androidx.annotation.NonNull;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class HttpRequestSender extends HandlerThread {
    private static final String NAME = "HttpRequestSender";
    private static final int MESSAGE_DOWNLOAD_GET = 101;
    private static final int MESSAGE_DOWNLOAD_POST = 102;
    public static final MediaType JSON
            = MediaType.get("application/json; charset=utf-8");

    private Handler mResponseHandler;
    private Handler mRequestHandler;
    private Context mContext;
    private OkHttpClient client;

    private CopyOnWriteArrayList<HttpRequest> mEventList = new CopyOnWriteArrayList<>();

    public HttpRequestSender(Context context, Handler responseHandler) {
        super(NAME);
        mResponseHandler = responseHandler;
        mContext = context;
        client = new OkHttpClient();
        this.getLooper();
    }


    @Override
    protected void onLooperPrepared() {
        mRequestHandler = new HttpRequestHandler(this);
    }

    public HttpRequest sendRequest(String method, String url){
        mRequestHandler.obtainMessage(
                (method.toUpperCase().equals("POST")? MESSAGE_DOWNLOAD_POST : MESSAGE_DOWNLOAD_GET)
                , url).sendToTarget();
        return new HttpRequest(url);
    }

    public HttpRequest sendRequest(String method, String url, String ... args){
        mRequestHandler.obtainMessage(
                (method.toUpperCase().equals("POST")? MESSAGE_DOWNLOAD_POST : MESSAGE_DOWNLOAD_GET)
                , new Pair<>(url, args)).sendToTarget();
        return new HttpRequest(url);
    }

    /**
     * Method for handling get request
     * @param url request url
     * @throws IOException
     */
    public void handleMessage(final String url) throws IOException {
        if(!isNetworkAvailable()){
            mResponseHandler.post(new Runnable() {
                @Override
                public void run() {
                    for(HttpRequest request : mEventList){
                        if(request.url.equals(url))
                            request.event.ready(null, new IOException("Network is unavailable"));
                    }
                }
            });
            return;
        }
        Request request = new Request.Builder()
                .url(url)
                .build();

        try (Response response = client.newCall(request).execute()) {
            final String responseStr = response.body().string();
            mResponseHandler.post(new Runnable() {
                @Override
                public void run() {
                    for(HttpRequest request : mEventList){
                        if(request.url.equals(url))
                            request.event.ready(responseStr, null);
                    }
                }
            });
        }
    }

    /**
     * Method for handling post request
     * @param url request url
     * @param args post request body params. Must be like ["param1=val", "param2=val"]
     * @throws IOException
     */
    public void handleMessage(final String url, String[] args) throws IOException {
        if(!isNetworkAvailable()){
            mResponseHandler.post(new Runnable() {
                @Override
                public void run() {
                    for(HttpRequest request : mEventList){
                        if(request.url.equals(url))
                            request.event.ready(null, new IOException("Network is unavailable"));
                    }
                }
            });
            return;
        }
        JSONObject jsonObject = new JSONObject();

        for(int i = 0; i < args.length; i++){
            String[] pair = args[i].split("=");
            try {
                jsonObject.put(pair[0], pair[1]);
            } catch (JSONException e) {
                //TODO: handling exception
                e.printStackTrace();
            }
        }
        String params = jsonObject.toString();
        RequestBody body = RequestBody.create(params, JSON);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        try (Response response = client.newCall(request).execute()) {
            final String responseStr = response.body().string();
            mResponseHandler.post(new Runnable() {
                @Override
                public void run() {
                    for(HttpRequest request : mEventList){
                        if(request.url.equals(url))
                            request.event.ready(responseStr, null);
                    }
                }
            });
        }

    }

    private boolean isNetworkAvailable() {
        ConnectivityManager cm =
                (ConnectivityManager)mContext.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
        return isConnected;

    }

    public interface OnRequestReadyEvent{
        void ready(String response, Exception e);
    }

    private static class HttpRequestHandler extends Handler{

        private HttpRequestSender mHttpRequestSender;

        public HttpRequestHandler(HttpRequestSender httpRequestSender){
            mHttpRequestSender = httpRequestSender;
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            if(msg.what == MESSAGE_DOWNLOAD_GET){
                try {
                    mHttpRequestSender.handleMessage(msg.obj.toString());
                } catch (IOException e) {
                    //TODO: handling exception
                    e.printStackTrace();
                }
            }else if(msg.what == MESSAGE_DOWNLOAD_POST){
                try {
                    @SuppressWarnings("unchecked")
                    Pair<String, String[]> pair = (Pair<String, String[]>)msg.obj;
                    mHttpRequestSender.handleMessage(pair.first, pair.second);
                } catch (IOException e) {
                    //TODO: handling exception
                    e.printStackTrace();
                }
            }
        }
    }

    public class HttpRequest{
        private String url;
        private OnRequestReadyEvent event;

        public HttpRequest(String url){
            this.url = url;
        }

        public void setOnRequestReadyEvent(OnRequestReadyEvent event){
            this.event = event;
            HttpRequestSender.this.mEventList.add(this);
        }
    }
}
