package com.valeron.wtwapp.network.api;

import android.content.Context;
import android.widget.Toast;

import com.valeron.wtwapp.network.HttpRequestSender;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class MovieBank {

    private Context mContext;
    private ArrayList<Movie> mTheaterMovies;
    private HttpRequestSender mRequestSender;
    private onTheaterMoviesLoaded mOnTheaterMoviesLoaded;

    private boolean isTheaterMoviesLoaded = false;

    private static MovieBank instance;

     public MovieBank(Context context, HttpRequestSender requestSender){
         mContext = context;
         mTheaterMovies = new ArrayList<>();
         mRequestSender = requestSender;
         loadTheaterMoviesAsync();
     }

    public static MovieBank getInstance(Context context, HttpRequestSender requestSender) {
        if(instance == null){
            instance = new MovieBank(context, requestSender);
        }
        return instance;
    }

    private void loadTheaterMoviesAsync(){
         String url = ApiConst.API_HOST + "/movie/now_playing?" + ApiConst.API_KEY + "&language=" + ApiConst.APP_LANG;
        mRequestSender.sendRequest("GET", url).setOnRequestReadyEvent(new HttpRequestSender.OnRequestReadyEvent() {
            @Override
            public void ready(byte[] response, Exception e) {
                try (ByteArrayInputStream stream =new ByteArrayInputStream(response)){
                    JSONObject jsonObject = new JSONObject(IOUtils.toString(stream));
                    JSONArray array = jsonObject.getJSONArray("results");
                    for (int i = 0; i < array.length(); i++) {
                        mTheaterMovies.add(new Movie(array.getJSONObject(i)));
                    }
                    isTheaterMoviesLoaded = true;
                    if(mOnTheaterMoviesLoaded != null){
                        mOnTheaterMoviesLoaded.loaded();
                    }
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

    public ArrayList<Movie> getTheaterMovies() {
        return mTheaterMovies;
    }

    public boolean isTheaterMoviesLoaded() {
        return isTheaterMoviesLoaded;
    }

    public void setOnTheaterMoviesLoaded(onTheaterMoviesLoaded onTheaterMoviesLoaded) {
        mOnTheaterMoviesLoaded = onTheaterMoviesLoaded;
    }

    public interface onTheaterMoviesLoaded{
         void loaded();
    }
}
