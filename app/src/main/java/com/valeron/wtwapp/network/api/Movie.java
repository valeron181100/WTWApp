package com.valeron.wtwapp.network.api;

import android.annotation.SuppressLint;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class Movie {
    private int id;
    private String posterPath;
    private String backdropPath;
    private String title;
    private String originTitle;
    private String originLang;
    private ArrayList<Integer> genreIds;
    private double voteAverage;
    private String overview;
    private Date releaseDate;
    private boolean isForAdult;

    @SuppressLint("SimpleDateFormat")
    public Movie(JSONObject jsonObject){
        try {
            id = jsonObject.getInt("id");
            posterPath = jsonObject.getString("poster_path");
            backdropPath = jsonObject.getString("backdrop_path");
            title = jsonObject.getString("title");
            originTitle = jsonObject.getString("original_title");
            originLang = jsonObject.getString("original_language");
            voteAverage = jsonObject.getDouble("vote_average");
            overview = jsonObject.getString("overview");
            isForAdult = jsonObject.getBoolean("adult");
            releaseDate = new SimpleDateFormat("yyyy-MM-dd").parse(jsonObject.getString("release_date"));
            JSONArray array = jsonObject.getJSONArray("genre_ids");
            genreIds = new ArrayList<>();
            for (int i = 0; i < array.length(); i++) {
                genreIds.add(array.getInt(i));
            }
        } catch (JSONException e) {
            //TODO: handling exception
            e.printStackTrace();
        } catch (ParseException e) {
            //TODO: handling exception
            e.printStackTrace();
        }
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPosterPath() {
        return posterPath;
    }

    public void setPosterPath(String posterPath) {
        this.posterPath = posterPath;
    }

    public String getBackdropPath() {
        return backdropPath;
    }

    public void setBackdropPath(String backdropPath) {
        this.backdropPath = backdropPath;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getOriginTitle() {
        return originTitle;
    }

    public void setOriginTitle(String originTitle) {
        this.originTitle = originTitle;
    }

    public String getOriginLang() {
        return originLang;
    }

    public void setOriginLang(String originLang) {
        this.originLang = originLang;
    }

    public ArrayList<Integer> getGenreIds() {
        return genreIds;
    }

    public void setGenreIds(ArrayList<Integer> genreIds) {
        this.genreIds = genreIds;
    }

    public double getVoteAverage() {
        return voteAverage;
    }

    public void setVoteAverage(double voteAverage) {
        this.voteAverage = voteAverage;
    }

    public String getOverview() {
        return overview;
    }

    public void setOverview(String overview) {
        this.overview = overview;
    }

    public Date getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(Date releaseDate) {
        this.releaseDate = releaseDate;
    }

    public boolean isForAdult() {
        return isForAdult;
    }

    public void setForAdult(boolean forAdult) {
        isForAdult = forAdult;
    }
}
