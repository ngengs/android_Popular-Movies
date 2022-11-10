package com.ngengs.android.popularmovies.apps.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ngengs.android.popularmovies.apps.globals.LocalDatabase
import io.reactivex.Completable
import io.reactivex.Maybe

/**
 * Created by rizky.kharisma on 11/11/22.
 * @ngengs
 */
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