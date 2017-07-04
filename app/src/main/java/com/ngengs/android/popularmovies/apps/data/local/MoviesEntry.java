package com.ngengs.android.popularmovies.apps.data.local;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

import com.ngengs.android.popularmovies.apps.globals.LocalDatabase;

/**
 * Created by ngengs on 7/3/2017.
 */

@SuppressWarnings({"WeakerAccess", "unused", "DefaultFileTemplate"})
public final class MoviesEntry implements BaseColumns {

    public static final Uri CONTENT_URI = LocalDatabase.BASE_CONTENT_URI.buildUpon()
            .appendPath(LocalDatabase.PATH_MOVIES)
            .build();

    public static final String CONTENT_DIR_TYPE =
            ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + LocalDatabase.CONTENT_AUTHORITY + "/" +
                    LocalDatabase.PATH_MOVIES;
    public static final String CONTENT_ITEM_TYPE =
            ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + LocalDatabase.CONTENT_AUTHORITY + "/" +
                    LocalDatabase.PATH_MOVIES;

    public static final String TABLE_NAME = "movies";

    public static final String COLUMN_ORIGINAL_TITLE = "original_title";
    public static final String COLUMN_OVERVIEW = "overview";
    public static final String COLUMN_RELEASE_DATE = "release_date";
    public static final String COLUMN_POSTER_PATH = "poster_path";
    public static final String COLUMN_POPULARITY = "popularity";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_AVERAGE_VOTE = "vote_average";
    public static final String COLUMN_VOTE_COUNT = "vote_count";
    public static final String COLUMN_BACKDROP_PATH = "backdrop_path";

    public static final String SQL_CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    _ID + " INTEGER PRIMARY KEY, " +
                    COLUMN_ORIGINAL_TITLE + " TEXT, " +
                    COLUMN_OVERVIEW + " TEXT, " +
                    COLUMN_RELEASE_DATE + " TEXT, " +
                    COLUMN_POSTER_PATH + " TEXT, " +
                    COLUMN_POPULARITY + " REAL, " +
                    COLUMN_TITLE + " TEXT, " +
                    COLUMN_AVERAGE_VOTE + " REAL, " +
                    COLUMN_VOTE_COUNT + " INTEGER," +
                    COLUMN_BACKDROP_PATH + " TEXT " +
                    " );";

    private static final String[] COLUMNS = {_ID, COLUMN_ORIGINAL_TITLE, COLUMN_OVERVIEW,
            COLUMN_RELEASE_DATE, COLUMN_POSTER_PATH, COLUMN_POPULARITY, COLUMN_TITLE,
            COLUMN_AVERAGE_VOTE, COLUMN_VOTE_COUNT, COLUMN_BACKDROP_PATH};

    public static Uri buildMovieUri(long id) {
        return ContentUris.withAppendedId(CONTENT_URI, id);
    }

    public static long getIdFromUri(Uri uri) {
        return ContentUris.parseId(uri);
    }

    public static String[] getColumns() {
        return COLUMNS.clone();
    }

    public static String[] getColumnsWithTable() {
        String[] tmp = COLUMNS.clone();
        tmp[0] = TABLE_NAME + "." + _ID;
        return tmp;
    }
}
