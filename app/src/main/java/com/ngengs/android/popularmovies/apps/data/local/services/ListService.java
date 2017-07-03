package com.ngengs.android.popularmovies.apps.data.local.services;

import android.content.Context;
import android.database.Cursor;

import com.ngengs.android.popularmovies.apps.data.MoviesDetail;
import com.ngengs.android.popularmovies.apps.data.MoviesList;
import com.ngengs.android.popularmovies.apps.data.local.MoviesEntry;
import com.ngengs.android.popularmovies.apps.data.local.MoviesFavorites;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ngengs on 7/4/2017.
 */

@SuppressWarnings("DefaultFileTemplate")
public class ListService {
    private final Context context;

    public ListService(Context context) {
        this.context = context;
    }

    public MoviesList getFavorites() {
        Cursor cursor = context.getContentResolver().query(MoviesFavorites.CONTENT_URI, MoviesEntry.getColumnsWithTable(), null, null, null);
        List<MoviesDetail> data = new ArrayList<>();
        if (cursor != null) {
            while (cursor.moveToNext()) {
                data.add(MoviesDetail.fromCursor(cursor));
            }
        }
        MoviesList moviesList = new MoviesList(data);
        moviesList.setPage(1);
        moviesList.setTotalPage(1);
        moviesList.setTotalResult(data.size());
        return moviesList;
    }
}
