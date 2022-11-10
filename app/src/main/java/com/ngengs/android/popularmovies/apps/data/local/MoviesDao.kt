package com.ngengs.android.popularmovies.apps.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ngengs.android.popularmovies.apps.globals.LocalDatabase

/**
 * Created by rizky.kharisma on 11/11/22.
 * @ngengs
 */
@Dao
abstract class MoviesDao {
    @Query("SELECT * from ${LocalDatabase.PATH_FAVORITES}")
    abstract suspend fun getFavorites(): List<MoviesFavoritesAndDetail>

    @Query("SELECT * from ${LocalDatabase.PATH_POPULAR}")
    abstract suspend fun getPopular(): List<MoviesPopularAndDetail>

    @Query("DELETE from ${LocalDatabase.PATH_POPULAR}")
    abstract suspend fun deletePopular()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun savePopularOnly(populars: List<MoviesPopular>)

    @Query("SELECT * from ${LocalDatabase.PATH_TOP_RATED}")
    abstract suspend fun getTopRated(): List<MoviesTopRatedAndDetail>

    @Query("DELETE from ${LocalDatabase.PATH_TOP_RATED}")
    abstract suspend fun deleteTopRated()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun saveTopRatedOnly(topRated: List<MoviesTopRated>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun saveMovies(movies: List<Movies>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun saveMovies(movies: Movies)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun addToFavorites(moviesFavorites: MoviesFavorites)

    @Delete
    abstract fun removeFromFavorites(moviesFavorites: MoviesFavorites)

    @Query("SELECT * from ${LocalDatabase.PATH_FAVORITES} WHERE ${LocalDatabase.COLUMN_MOVIE_ID_KEY}=:moviesId")
    abstract suspend fun getFavorite(moviesId: Int): MoviesFavorites?
}