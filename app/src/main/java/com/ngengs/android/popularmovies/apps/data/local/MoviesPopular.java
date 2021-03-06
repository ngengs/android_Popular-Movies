package com.ngengs.android.popularmovies.apps.data.local;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

import com.ngengs.android.popularmovies.apps.globals.LocalDatabase;

/**
 * Created by ngengs on 7/3/2017.
 */

@SuppressWarnings({"WeakerAccess", "unused", "DefaultFileTemplate"})
public final class MoviesPopular implements BaseColumns {
    public static final Uri CONTENT_URI = MoviesEntry.CONTENT_URI.buildUpon()
            .appendPath(LocalDatabase.PATH_POPULAR)
            .build();
    public static final String CONTENT_DIR_TYPE =
            ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + LocalDatabase.CONTENT_AUTHORITY + "/" + LocalDatabase.PATH_MOVIES
                    + "/" + LocalDatabase.PATH_POPULAR;

    public static final String TABLE_NAME = "popular_movies";

    public static final String SQL_CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    LocalDatabase.COLUMN_MOVIE_ID_KEY + " INTEGER NOT NULL, " +

                    " FOREIGN KEY (" + LocalDatabase.COLUMN_MOVIE_ID_KEY + ") REFERENCES " +
                    MoviesEntry.TABLE_NAME + " (" + MoviesEntry._ID + ") " +

                    " );";

    private static final String[] COLUMNS = {_ID, LocalDatabase.COLUMN_MOVIE_ID_KEY};

    public static String[] getColumns() {
        return COLUMNS.clone();
    }
}
