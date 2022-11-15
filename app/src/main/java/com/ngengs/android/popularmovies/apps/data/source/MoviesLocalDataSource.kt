package com.ngengs.android.popularmovies.apps.data.source

import com.ngengs.android.popularmovies.apps.data.remote.MoviesDetail
import com.ngengs.android.popularmovies.apps.data.remote.MoviesList

/**
 * Created by rizky.kharisma on 11/11/22.
 * @ngengs
 */
interface MoviesLocalDataSource: MoviesBaseDataSource {
    suspend fun getFavoriteMovies(page: Int): MoviesList
    suspend fun getDetailMovies(movieId: Int): MoviesDetail
    suspend fun saveMovies(movie: MoviesDetail)
    suspend fun savePopularMovies(movies: List<MoviesDetail>)
    suspend fun saveTopRatedMovies(movies: List<MoviesDetail>)
    suspend fun saveFavoriteMovies(movies: MoviesDetail)
    suspend fun removeFavoriteMovies(movies: MoviesDetail)
    suspend fun getFavoriteMovie(movieId: Int): MoviesDetail?
}