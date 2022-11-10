package com.ngengs.android.popularmovies.apps.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.ngengs.android.popularmovies.apps.utils.db.DateConverter

/**
 * Created by rizky.kharisma on 10/11/22.
 * @ngengs
 */
@Database(
    entities = [Movies::class, MoviesFavorites::class, MoviesPopular::class, MoviesTopRated::class],
    version = MoviesDatabaseHelper.DATABASE_VERSION,
)
@TypeConverters(DateConverter::class)
abstract class MoviesDatabase : RoomDatabase() {
    abstract fun moviesDao(): MoviesDao
}

