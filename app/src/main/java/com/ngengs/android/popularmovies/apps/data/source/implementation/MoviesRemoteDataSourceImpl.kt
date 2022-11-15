package com.ngengs.android.popularmovies.apps.data.source.implementation

import com.ngengs.android.popularmovies.apps.data.remote.MoviesDetail
import com.ngengs.android.popularmovies.apps.data.remote.MoviesList
import com.ngengs.android.popularmovies.apps.data.remote.ReviewList
import com.ngengs.android.popularmovies.apps.data.remote.VideosList
import com.ngengs.android.popularmovies.apps.data.source.MoviesRemoteDataSource
import com.ngengs.android.popularmovies.apps.utils.networks.MoviesAPI
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Created by rizky.kharisma on 11/11/22.
 * @ngengs
 */
class MoviesRemoteDataSourceImpl(
    private val moviesAPI: MoviesAPI,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : MoviesRemoteDataSource {
    override suspend fun getDetailMovies(id: Int): MoviesDetail = withContext(dispatcher){
        moviesAPI.detail(id)
    }

    override suspend fun getMoviesVideos(id: Int): VideosList  = withContext(dispatcher){
        moviesAPI.videos(id)
    }

    override suspend fun getMoviesReviews(id: Int): ReviewList  = withContext(dispatcher){
        moviesAPI.reviews(id)
    }

    override suspend fun getPopularMovies(page: Int): MoviesList = withContext(dispatcher){
        moviesAPI.listMoviesPopular(page)
    }

    override suspend fun getTopRatedMovies(page: Int): MoviesList = withContext(dispatcher){
        moviesAPI.listMoviesTopRated(page)
    }
}