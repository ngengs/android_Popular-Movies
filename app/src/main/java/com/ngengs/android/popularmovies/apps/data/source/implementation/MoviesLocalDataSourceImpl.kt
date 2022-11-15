package com.ngengs.android.popularmovies.apps.data.source.implementation

import com.ngengs.android.popularmovies.apps.data.local.MoviesDao
import com.ngengs.android.popularmovies.apps.data.local.MoviesFavorites
import com.ngengs.android.popularmovies.apps.data.local.MoviesPopular
import com.ngengs.android.popularmovies.apps.data.local.MoviesTopRated
import com.ngengs.android.popularmovies.apps.data.local.toMovieList
import com.ngengs.android.popularmovies.apps.data.local.toMovies
import com.ngengs.android.popularmovies.apps.data.local.toMoviesDetail
import com.ngengs.android.popularmovies.apps.data.remote.MoviesDetail
import com.ngengs.android.popularmovies.apps.data.remote.MoviesList
import com.ngengs.android.popularmovies.apps.data.source.MoviesLocalDataSource
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Created by rizky.kharisma on 12/11/22.
 * @ngengs
 */
class MoviesLocalDataSourceImpl(
    private val moviesDao: MoviesDao,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : MoviesLocalDataSource {
    override suspend fun getFavoriteMovies(page: Int): MoviesList = withContext(dispatcher) {
        moviesDao.getFavorites().mapNotNull { it.movies?.toMoviesDetail() }.toMovieList() ?: throw Exception("Data not found")
    }

    override suspend fun getDetailMovies(movieId: Int): MoviesDetail = withContext(dispatcher) {
        moviesDao.getDetailMovie(movieId).toMoviesDetail()
    }

    override suspend fun saveMovies(movie: MoviesDetail) = withContext(dispatcher){
        moviesDao.saveMovies(movie.toMovies())
    }

    override suspend fun savePopularMovies(movies: List<MoviesDetail>) = withContext(dispatcher){
        moviesDao.saveMovies(movies.map { it.toMovies() })
        moviesDao.deletePopular()
        moviesDao.savePopularOnly(movies.map { MoviesPopular(movieId = it.id) })
    }

    override suspend fun saveTopRatedMovies(movies: List<MoviesDetail>) = withContext(dispatcher){
        moviesDao.saveMovies(movies.map { it.toMovies() })
        moviesDao.deleteTopRated()
        moviesDao.saveTopRatedOnly(movies.map { MoviesTopRated(movieId = it.id) })
    }

    override suspend fun saveFavoriteMovies(movies: MoviesDetail) = withContext(dispatcher) {
        moviesDao.addToFavorites(MoviesFavorites(movieId = movies.id))
    }

    override suspend fun removeFavoriteMovies(movies: MoviesDetail) = withContext(dispatcher){
        moviesDao.getFavorite(movies.id)?.let { moviesDao.removeFromFavorites(it) }
        Unit
    }

    override suspend fun getFavoriteMovie(movieId: Int): MoviesDetail  = withContext(dispatcher) {
        moviesDao.getFavorite(movieId)?.let { MoviesDetail(id = it.movieId) } ?: throw Exception("Data not found")
    }

    override suspend fun getPopularMovies(page: Int): MoviesList  = withContext(dispatcher) {
        moviesDao.getPopular().mapNotNull { it.movies?.toMoviesDetail() }.toMovieList() ?: throw Exception("Data not found")
    }

    override suspend fun getTopRatedMovies(page: Int): MoviesList  = withContext(dispatcher) {
        moviesDao.getTopRated().mapNotNull { it.movies?.toMoviesDetail() }.toMovieList() ?: throw Exception("Data not found")
    }
}