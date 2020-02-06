package com.valeron.wtwapp.network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;

public class HttpRequestSender extends HandlerThread {
    private static final String NAME = "HttpRequestSender";
    private static final int MESSAGE_DOWNLOAD = 101;

    private Handler mResponseHandler;
    private Handler mRequestHandler;
    private Context mContext;

    List<OnRequestReadyEvent> mEventList = new ArrayList<>();

    public HttpRequestSender(Context context, Handler responseHandler) {
        super(NAME);
        mResponseHandler = responseHandler;
        mContext = context;
        this.getLooper();
    }


    @Override
    protected void onLooperPrepared() {
        mRequestHandler = new HttpRequestHandler(this);
    }

    public void sendRequest(String url){
        mRequestHandler.obtainMessage(MESSAGE_DOWNLOAD, url).sendToTarget();
    }

    public void handleMessage(String url) throws IOException {
        if(!isNetworkAvailable()){
            mResponseHandler.post(new Runnable() {
                @Override
                public void run() {
                    for(OnRequestReadyEvent event : mEventList){
                        event.ready(null, new IOException("Network is unavailable"));
                    }
                }
            });
            return;
        }
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
            throw new IOException(connection.getResponseMessage());
        }
        else{
            try(ByteArrayOutputStream out = new ByteArrayOutputStream(); InputStream inputStream = new URL(url).openStream()) {
                int bytesRead = 0;
                byte[] buffer = new byte[1024];
                while ((bytesRead = inputStream.read(buffer)) > 0) {
                    out.write(buffer, 0, bytesRead);
                }
                mResponseHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        for(OnRequestReadyEvent event : mEventList){
                            event.ready(new String(out.toByteArray()), null);
                        }
                    }
                });
            }
        }

    }

    public void addOnRequestReadyEvent(OnRequestReadyEvent event){
        mEventList.add(event);
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
            if(msg.what == MESSAGE_DOWNLOAD){
                try {
                    mHttpRequestSender.handleMessage(msg.obj.toString());
                } catch (IOException e) {
                    //TODO: handling exception
                    e.printStackTrace();
                }
            }
        }
    }
}
