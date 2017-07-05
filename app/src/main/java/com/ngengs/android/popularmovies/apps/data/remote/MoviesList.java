package com.ngengs.android.popularmovies.apps.data.remote;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by ngengs on 6/15/2017.
 */

@SuppressWarnings({"unused", "DefaultFileTemplate"})
public class MoviesList {
    @SerializedName("page")
    private int page;
    @SerializedName("total_result")
    private int totalResult;
    @SerializedName("total_pages")
    private int totalPage;
    @SerializedName("results")
    private List<MoviesDetail> movies;

    //Error Data
    @SerializedName("status_code")
    private int statusCode;
    @SerializedName("status_message")
    private String statusMessage;

    public MoviesList(List<MoviesDetail> movies) {
        this.movies = movies;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getTotalResult() {
        return totalResult;
    }

    public void setTotalResult(int totalResult) {
        this.totalResult = totalResult;
    }

    public int getTotalPage() {
        return totalPage;
    }

    public void setTotalPage(int totalPage) {
        this.totalPage = totalPage;
    }

    public List<MoviesDetail> getMovies() {
        return movies;
    }

    public void setMovies(List<MoviesDetail> movies) {
        this.movies = movies;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }
}
