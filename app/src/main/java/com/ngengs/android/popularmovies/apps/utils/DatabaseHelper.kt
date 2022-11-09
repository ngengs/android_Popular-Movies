package com.ngengs.android.popularmovies.apps.utils

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.ngengs.android.popularmovies.apps.data.local.MoviesEntry
import com.ngengs.android.popularmovies.apps.data.local.MoviesFavorites
import com.ngengs.android.popularmovies.apps.data.local.MoviesPopular
import com.ngengs.android.popularmovies.apps.data.local.MoviesTopRated

/**
 * Created by rizky.kharisma on 09/11/22.
 * @ngengs
 */
class DatabaseHelper constructor(
    val context: Context,
) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(MoviesEntry.SQL_CREATE_TABLE)
        db.execSQL(MoviesPopular.SQL_CREATE_TABLE)
        db.execSQL(MoviesTopRated.SQL_CREATE_TABLE)
        db.execSQL(MoviesFavorites.SQL_CREATE_TABLE)
    }

    override fun onUpgrade(p0: SQLiteDatabase?, p1: Int, p2: Int) {
        // No need for now
    }

    companion object {
        private const val DATABASE_NAME = "popularMovies.db"
        private const val DATABASE_VERSION = 1
    }
}