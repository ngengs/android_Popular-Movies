package com.ngengs.android.popularmovies.apps.globals;

/**
 * Created by ngengs on 6/15/2017.
 */

@SuppressWarnings({"unused", "DefaultFileTemplate"})
public final class Values {

    /**
     * Server API
     */
    public static final String URL_BASE = "https://api.themoviedb.org/3/";
    public static final String URL_PATH_DETAIL = "movie/{id}";
    public static final String URL_PATH_DETAIL_PARAM = "id";
    public static final String URL_PATH_POPULAR = "movie/popular";
    public static final String URL_PATH_TOP_RATED = "movie/top_rated";
    public static final String URL_PATH_VIDEO = "movie/{id}/videos";
    public static final String URL_PATH_VIDEO_PARAM = "id";
    public static final String URL_PATH_REVIEW = "movie/{id}/reviews";
    public static final String URL_PATH_REVIEW_PARAM = "id";
    public static final String URL_FRAGMENT_KEY_API = "api_key";
    public static final String URL_FRAGMENT_KEY_PAGE = "page";
    public static final String URL_IMAGE = "https://image.tmdb.org/t/p/";
    public static final String URL_VIDEO_YOUTUBE = "https://www.youtube.com/watch?v=";
    public static final String URL_VIDEO_YOUTUBE_THUMB = "https://img.youtube.com/vi/%s/hqdefault.jpg";
    public static final String[] IMAGE_SIZE_PATH = {"original", "w92", "w154", "w185", "w342", "w500", "w780"};
    public static final String[] IMAGE_SIZE_BACKDROP = {"original", "w300", "w780", "w1280"};

    /**
     * Other URL
     */
    public static final String URL_IMDB_BASE = "https://www.imdb.com/";
    public static final String URL_IMDB_PATH_TITLE = "title/";


    /**
     * Integer / Decimal Value
     */
    public static final int TYPE_POPULAR = 0;
    public static final int TYPE_HIGH_RATED = 1;
    public static final int TYPE_FAVORITE = 2;
    public static final int TYPE_DEFAULT_IMAGE_THUMB = 6;
    public static final double RATING_SCORE_PERFECT = 9.0;
    public static final double RATING_SCORE_GOOD = 7.0;
    public static final double RATING_SCORE_NORMAL = 5.0;

}
