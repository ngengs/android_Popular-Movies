package com.ngengs.android.popularmovies.apps.data.local.services;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.ngengs.android.popularmovies.apps.data.MoviesDetail;
import com.ngengs.android.popularmovies.apps.data.local.MoviesEntry;
import com.ngengs.android.popularmovies.apps.data.local.MoviesFavorites;
import com.ngengs.android.popularmovies.apps.globals.LocalDatabase;

/**
 * Created by ngengs on 7/4/2017.
 */

@SuppressWarnings("DefaultFileTemplate")
public class FavoriteService {

    private final Context context;

    public FavoriteService(Context context) {
        this.context = context.getApplicationContext();
    }

    public void addToFavorites(MoviesDetail movie) {
        context.getContentResolver().insert(MoviesEntry.CONTENT_URI, movie.toContentValues());
        ContentValues contentValues = new ContentValues();
        contentValues.put(LocalDatabase.COLUMN_MOVIE_ID_KEY, movie.getId());
        context.getContentResolver().insert(MoviesFavorites.CONTENT_URI, contentValues);
    }

    public void removeFromFavorites(int movieId) {
        context.getContentResolver().delete(
                MoviesFavorites.CONTENT_URI,
                LocalDatabase.COLUMN_MOVIE_ID_KEY + " = " + movieId,
                null
        );
    }

    public boolean isFavorite(int movieId) {
        boolean favorite = false;
        Cursor cursor = context.getContentResolver().query(
                MoviesFavorites.CONTENT_URI,
                null,
                LocalDatabase.COLUMN_MOVIE_ID_KEY + " = " + movieId,
                null,
                null
        );
        if (cursor != null) {
            favorite = cursor.getCount() != 0;
            cursor.close();
        }
        return favorite;
    }
}
