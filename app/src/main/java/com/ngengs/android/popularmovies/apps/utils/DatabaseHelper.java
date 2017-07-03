package com.ngengs.android.popularmovies.apps.utils;

import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.ngengs.android.popularmovies.apps.data.local.MoviesEntry;
import com.ngengs.android.popularmovies.apps.data.local.MoviesFavorites;
import com.ngengs.android.popularmovies.apps.data.local.MoviesPopular;
import com.ngengs.android.popularmovies.apps.data.local.MoviesTopRated;

/**
 * Created by ngengs on 7/3/2017.
 */

@SuppressWarnings({"WeakerAccess", "unused", "DefaultFileTemplate"})
public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "popularMovies.db";
    private static final int DATABASE_VERSION = 1;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public DatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    public DatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version, DatabaseErrorHandler errorHandler) {
        super(context, name, factory, version, errorHandler);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(MoviesEntry.SQL_CREATE_TABLE);
        db.execSQL(MoviesPopular.SQL_CREATE_TABLE);
        db.execSQL(MoviesTopRated.SQL_CREATE_TABLE);
        db.execSQL(MoviesFavorites.SQL_CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // No need for now
    }
}
