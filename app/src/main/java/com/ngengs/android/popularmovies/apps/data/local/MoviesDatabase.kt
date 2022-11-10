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

@Dao
abstract class MoviesDao {
    @Query("SELECT * from ${LocalDatabase.PATH_FAVORITES}")
    abstract fun getFavorites(): Maybe<List<MoviesFavoritesAndDetail>>

    @Query("SELECT * from ${LocalDatabase.PATH_POPULAR}")
    abstract fun getPopular(): Maybe<List<MoviesPopularAndDetail>>

    @Query("DELETE from ${LocalDatabase.PATH_POPULAR}")
    abstract fun deletePopular(): Completable

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun savePopularOnly(populars: List<MoviesPopular>): Completable

    @Query("SELECT * from ${LocalDatabase.PATH_TOP_RATED}")
    abstract fun getTopRated(): Maybe<List<MoviesTopRatedAndDetail>>

    @Query("DELETE from ${LocalDatabase.PATH_TOP_RATED}")
    abstract fun deleteTopRated(): Completable

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun saveTopRatedOnly(topRated: List<MoviesTopRated>): Completable

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun saveMovies(movies: List<Movies>): Completable

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun saveMovies(movies: Movies): Completable

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun addToFavorites(moviesFavorites: MoviesFavorites): Completable

    @Delete
    abstract fun removeFromFavorites(moviesFavorites: MoviesFavorites): Completable

    @Query("SELECT * from ${LocalDatabase.PATH_FAVORITES} WHERE ${LocalDatabase.COLUMN_MOVIE_ID_KEY}=:moviesId")
    abstract fun getFavorite(moviesId: Int): Maybe<MoviesFavorites?>
}

object DateConverter {
    @TypeConverter
    fun fromDate(date: Date?): String {
        if (date == null) return ""
        val formatter = SimpleDateFormat("EEE MMM dd kk:mm:ss zzz yyyy", Locale.ROOT)
        return try {
            formatter.format(date)
        } catch (e: Exception) {
            ""
        }
    }

    @TypeConverter
    fun toDate(dateString: String): Date? {
        if (dateString.isEmpty()) return null
        val formatter = SimpleDateFormat("EEE MMM dd kk:mm:ss zzz yyyy", Locale.ROOT)
        return try {
            formatter.parse(dateString)
        } catch (e: Exception) {
            null
        }
    }
}
