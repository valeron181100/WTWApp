package com.valeron.wtwapp.models;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;

import com.valeron.wtwapp.network.api.ApiConst;
import com.valeron.wtwapp.network.api.Movie;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.ConcurrentHashMap;

import androidx.annotation.NonNull;

public class PosterDownloader<T> extends HandlerThread {

    private static final String TAG = "ThumbnailDownloader";
    private static final int MESSAGE_DOWNLOAD = 0;
    private Handler mRequestHandler;
    private ConcurrentHashMap<T, Movie> mRequestMap;
    private OnPosterDownloadedListener<T> mDownloadedListener;
    private Handler mResponseHandler;
    private boolean mHasQuit = false;
    private Movie mMovie;

    @Override
    public boolean quit(){
        mHasQuit = true;
        return super.quit();
    }

    public void clearQueue(){
        mRequestHandler.removeMessages(MESSAGE_DOWNLOAD);
        mRequestMap.clear();
    }

    public void setOnPosterDownloadedListener(OnPosterDownloadedListener<T> downloadedListener) {
        mDownloadedListener = downloadedListener;
    }

    public PosterDownloader(Handler responseHandler) {
        super(TAG);
        mRequestMap = new ConcurrentHashMap<>();
        mResponseHandler = responseHandler;
    }

    @SuppressLint("HandlerLeak")
    @Override
    protected void onLooperPrepared() {
        mRequestHandler = new Handler(){
            @Override
            public void handleMessage(@NonNull Message msg) {
                if(msg.what == MESSAGE_DOWNLOAD){
                    T holder = (T) msg.obj;
                    handleRequest(holder);
                }
            }
        };
    }

    public void queueMessage(T holder, Movie movie){
        mMovie = movie;
        if(mMovie.getPosterPath() == null){
            mRequestMap.remove(holder);
        }else{
            mRequestMap.put(holder, movie);
            mRequestHandler.obtainMessage(MESSAGE_DOWNLOAD, holder).sendToTarget();
        }
    }

    public void handleRequest(final T holder){
        final Movie movie = mRequestMap.get(holder);
        try {
            if (movie == null) {
                return;
            }

            final String url = ApiConst.API_IMAGE_HOST + movie.getPosterPath();

            URLConnection connection = new URL(url).openConnection();

            InputStream stream = connection.getInputStream();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buff = new byte[1024];

            while (stream.read(buff) != -1) {
                baos.write(buff);
            }
            final Bitmap bitmap = BitmapFactory.decodeStream(stream);

            mResponseHandler.post(new Runnable() {
                @Override
                public void run() {
                    Movie movie1 = mRequestMap.get(holder);
                    if(movie1 != null)
                        if(!(ApiConst.API_IMAGE_HOST + movie1.getPosterPath()).equals(url) || mHasQuit){
                            return;
                        }

                    mRequestMap.remove(holder);
                    if(mDownloadedListener != null){
                        mDownloadedListener.downloaded(holder, bitmap, movie);
                    }
                }
            });
        }catch (IOException e){
            e.printStackTrace();
        }

    }

    public interface OnPosterDownloadedListener<T>{
        void downloaded(T holder, Bitmap cover, Movie realMovie);
    }

}