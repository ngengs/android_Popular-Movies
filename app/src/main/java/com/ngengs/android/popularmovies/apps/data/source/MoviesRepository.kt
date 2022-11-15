package com.ngengs.android.popularmovies.apps.data.source

import com.ngengs.android.popularmovies.apps.data.remote.MoviesDetail
import com.ngengs.android.popularmovies.apps.data.remote.MoviesList
import com.ngengs.android.popularmovies.apps.data.remote.ReviewList
import com.ngengs.android.popularmovies.apps.data.remote.VideosList
import com.ngengs.android.popularmovies.apps.utils.networks.Resource
import kotlinx.coroutines.flow.Flow

/**
 * Created by rizky.kharisma on 11/11/22.
 * @ngengs
 */
interface MoviesRepository {
    fun getPopularMovies(page: Int): Flow<Resource<MoviesList>>
    fun getTopRatedMovies(page: Int): Flow<Resource<MoviesList>>
    suspend fun getFavoriteMovies(page: Int): Resource<MoviesList>
    fun getDetailMovies(id: Int): Flow<Resource<MoviesDetail>>
    suspend fun getMoviesVideos(id: Int): Resource<VideosList>
    suspend fun getMoviesReviews(id: Int): Resource<ReviewList>
    suspend fun saveFavoriteMovies(movies: MoviesDetail)
    suspend fun removeFavoriteMovies(movies: MoviesDetail)
    suspend fun isFavoriteMovie(movieId: Int): Boolean
}