package com.ngengs.android.popularmovies.apps.globals;

import android.net.Uri;

/**
 * Created by ngengs on 7/3/2017.
 */

@SuppressWarnings({"WeakerAccess", "unused"})
public final class LocalDatabase {

    public static final String CONTENT_AUTHORITY = "com.ngengs.android.popularmovies.apps";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_MOVIES = "movies";
    public static final String PATH_POPULAR = "popular";
    public static final String PATH_TOP_RATED = "top_rated";
    public static final String PATH_FAVORITES = "favorites";

    public static final String COLUMN_MOVIE_ID_KEY = "movie_id";

    public static final int CODE_MOVIES = 100;
    public static final int CODE_MOVIE_BY_ID = 101;
    public static final int CODE_POPULAR_MOVIES = 201;
    public static final int CODE_TOP_RATED_MOVIES = 202;
    public static final int CODE_FAVORITES = 300;


}
