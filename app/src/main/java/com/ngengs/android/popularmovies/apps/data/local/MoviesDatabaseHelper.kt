package com.ngengs.android.popularmovies.apps.data.local

import android.content.Context
import androidx.room.Room
import com.ngengs.android.popularmovies.apps.data.remote.MoviesDetail
import com.ngengs.android.popularmovies.apps.data.remote.MoviesList

/**
 * Created by rizky.kharisma on 10/11/22.
 * @ngengs
 */
class MoviesDatabaseHelper(context: Context) {
    private val db by lazy {
        Room.databaseBuilder(context, MoviesDatabase::class.java, DATABASE_NAME)
            .build()
    }

    private val dao get() = db.moviesDao()

    suspend fun getFavorites(): MoviesList? = try {
        dao.getFavorites().mapNotNull { it.movies?.toMoviesDetail() }.toMovieList()
    } catch (e: Exception) { null }

    suspend fun getPopular(): MoviesList? = try {
        dao.getPopular().mapNotNull { it.movies?.toMoviesDetail() }.toMovieList()
    } catch (e: Exception) { null }

    suspend fun savePopular(movies: List<MoviesDetail>) {
        dao.deletePopular()
        dao.savePopularOnly(movies.map { MoviesPopular(movieId = it.id) })
    }

    suspend fun getTopRated(): MoviesList? = try {
        dao.getTopRated().mapNotNull { it.movies?.toMoviesDetail() }.toMovieList()
    } catch (e: Exception) { null }

    suspend fun saveTopRated(movies: List<MoviesDetail>) {
        dao.deleteTopRated()
        dao.saveTopRatedOnly(movies.map { MoviesTopRated(movieId = it.id) })
    }

    suspend fun saveMovies(movies: List<MoviesDetail>) =
        dao.saveMovies(movies.map { it.toMovies() })

    suspend fun saveMovies(movies: MoviesDetail) = dao.saveMovies(movies.toMovies())

    suspend fun addToFavorites(movieId: Int) =
        dao.addToFavorites(MoviesFavorites(movieId = movieId))

    suspend fun removeFromFavorites(movieId: Int) {
        try {
            val result = dao.getFavorite(movieId)
            if (result != null) dao.removeFromFavorites(result)
        } catch (_: Exception) {
        }
    }

    suspend fun isFavorite(movieId: Int): Boolean = try {
        val result = dao.getFavorite(movieId)
        result != null
    } catch (e: Exception) {
        false
    }

    companion object {
        private const val DATABASE_NAME = "popularMovies.db"
        const val DATABASE_VERSION = 1
    }
}