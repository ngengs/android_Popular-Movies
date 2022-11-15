package com.ngengs.android.popularmovies.apps.data.local

import android.content.Context
import androidx.room.Room
import com.ngengs.android.popularmovies.apps.data.remote.MoviesDetail
import com.ngengs.android.popularmovies.apps.data.remote.MoviesList

/**
 * Created by rizky.kharisma on 10/11/22.
 * @ngengs
 */
object MoviesDatabaseHelper {
    private const val DATABASE_NAME = "popularMovies.db"
    const val DATABASE_VERSION = 1

    fun createDatabase(context: Context): MoviesDatabase =
        Room.databaseBuilder(context, MoviesDatabase::class.java, DATABASE_NAME)
            .build()
}