package com.ngengs.android.popularmovies.apps.data.local

import android.content.Context
import android.util.Log
import androidx.room.Room
import com.ngengs.android.popularmovies.apps.data.remote.MoviesDetail
import com.ngengs.android.popularmovies.apps.data.remote.MoviesList
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

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

    fun getFavorites(): Maybe<MoviesList?> =
        dao.getFavorites()
            .map { data -> data.mapNotNull { it.movies?.toMoviesDetail() }.toMovieList() }

    fun getPopular(): Maybe<MoviesList?> =
        dao.getPopular()
            .map { data -> data.mapNotNull { it.movies?.toMoviesDetail() }.toMovieList() }

    fun deletePopular(): Completable = dao.deletePopular()

    fun savePopular(movies: List<MoviesDetail>): Completable =
        dao.savePopularOnly(movies.map { MoviesPopular(movieId = it.id) })

    fun getTopRated(): Maybe<MoviesList?> =
        dao.getTopRated()
            .map { data -> data.mapNotNull { it.movies?.toMoviesDetail() }.toMovieList() }

    fun deleteTopRated(): Completable {
        return dao.deleteTopRated()
    }

    fun saveTopRated(movies: List<MoviesDetail>): Completable {
        return dao.saveTopRatedOnly(movies.map { MoviesTopRated(movieId = it.id) })
    }

    fun saveMovies(movies: List<MoviesDetail>): Completable =
        dao.saveMovies(movies.map { it.toMovies() })

    fun saveMovies(movies: MoviesDetail): Completable {
        return dao.saveMovies(movies.toMovies())
    }

    fun addToFavorites(movieId: Int): Completable {
        return dao.addToFavorites(MoviesFavorites(movieId = movieId))
    }

    fun removeFromFavorites(movieId: Int): Completable =
        isFavorite(movieId).flatMapCompletable { dao.removeFromFavorites(it) }

    fun isFavorite(movieId: Int): Maybe<MoviesFavorites?> = dao.getFavorite(movieId)

    companion object {
        private const val DATABASE_NAME = "popularMovies.db"
        const val DATABASE_VERSION = 1
    }
}