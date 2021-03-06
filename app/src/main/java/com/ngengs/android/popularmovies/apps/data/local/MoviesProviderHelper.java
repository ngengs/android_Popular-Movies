package com.ngengs.android.popularmovies.apps.data.local;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.ngengs.android.popularmovies.apps.data.MoviesDetail;
import com.ngengs.android.popularmovies.apps.data.MoviesList;
import com.ngengs.android.popularmovies.apps.globals.LocalDatabase;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ngengs on 7/4/2017.
 */

@SuppressWarnings("DefaultFileTemplate")
public class MoviesProviderHelper {
    private final Context context;

    public MoviesProviderHelper(Context context) {
        this.context = context;
    }

    public MoviesList getFavorites() {
        Cursor cursor = context.getContentResolver().query(MoviesFavorites.CONTENT_URI, MoviesEntry.getColumnsWithTable(), null, null, null);
        List<MoviesDetail> data = new ArrayList<>();
        if (cursor != null) {
            while (cursor.moveToNext()) {
                data.add(MoviesDetail.fromCursor(cursor));
            }
            MoviesList moviesList = new MoviesList(data);
            moviesList.setPage(1);
            moviesList.setTotalPage(1);
            moviesList.setTotalResult(data.size());
            return moviesList;
        } else return null;
    }

    public MoviesList getPopular() {
        Cursor cursor = context.getContentResolver().query(MoviesPopular.CONTENT_URI, MoviesEntry.getColumnsWithTable(), null, null, null);
        List<MoviesDetail> data = new ArrayList<>();
        if (cursor != null) {
            while (cursor.moveToNext()) {
                data.add(MoviesDetail.fromCursor(cursor));
            }
            MoviesList moviesList = new MoviesList(data);
            moviesList.setPage(0);
            moviesList.setTotalPage(1);
            moviesList.setTotalResult(data.size());
            return moviesList;
        } else return null;
    }

    public void deletePopular() {
        context.getContentResolver().delete(MoviesPopular.CONTENT_URI, null, null);
    }

    public void savePopular(List<MoviesDetail> movies) {
        if (!movies.isEmpty()) {
            ContentValues[] contentValues = new ContentValues[movies.size()];
            for (int i = 0; i < movies.size(); i++) {
                ContentValues contentValue = new ContentValues();
                contentValue.put(LocalDatabase.COLUMN_MOVIE_ID_KEY, movies.get(i).getId());
                contentValues[i] = contentValue;
            }
            context.getContentResolver().bulkInsert(MoviesPopular.CONTENT_URI, contentValues);
        }
    }

    public MoviesList getTopRated() {
        Cursor cursor = context.getContentResolver().query(MoviesTopRated.CONTENT_URI, MoviesEntry.getColumnsWithTable(), null, null, null);
        List<MoviesDetail> data = new ArrayList<>();
        if (cursor != null) {
            while (cursor.moveToNext()) {
                data.add(MoviesDetail.fromCursor(cursor));
            }
            MoviesList moviesList = new MoviesList(data);
            moviesList.setPage(0);
            moviesList.setTotalPage(1);
            moviesList.setTotalResult(data.size());
            return moviesList;
        } else return null;
    }

    public void deleteTopRated() {
        context.getContentResolver().delete(MoviesTopRated.CONTENT_URI, null, null);
    }

    public void saveTopRated(List<MoviesDetail> movies) {
        if (!movies.isEmpty()) {
            ContentValues[] contentValues = new ContentValues[movies.size()];
            for (int i = 0; i < movies.size(); i++) {
                ContentValues contentValue = new ContentValues();
                contentValue.put(LocalDatabase.COLUMN_MOVIE_ID_KEY, movies.get(i).getId());
                contentValues[i] = contentValue;
            }
            context.getContentResolver().bulkInsert(MoviesTopRated.CONTENT_URI, contentValues);
        }
    }

    public void saveMovies(List<MoviesDetail> movies) {
        if (!movies.isEmpty()) {
            ContentValues[] contentValues = new ContentValues[movies.size()];
            for (int i = 0; i < movies.size(); i++) {
                contentValues[i] = movies.get(i).toContentValues();
            }
            context.getContentResolver().bulkInsert(MoviesEntry.CONTENT_URI, contentValues);
        }
    }

    public void saveMovies(MoviesDetail movie) {
        context.getContentResolver().insert(MoviesEntry.CONTENT_URI, movie.toContentValues());
    }

    public void addToFavorites(int movieId) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(LocalDatabase.COLUMN_MOVIE_ID_KEY, movieId);
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
