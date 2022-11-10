package com.ngengs.android.popularmovies.apps.data.local

import android.provider.BaseColumns
import androidx.room.AutoMigration
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.Transaction
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.ngengs.android.popularmovies.apps.globals.LocalDatabase
import com.ngengs.android.popularmovies.apps.utils.db.DateConverter
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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

