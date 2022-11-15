package com.ngengs.android.popularmovies.apps.data.source.implementation

import com.ngengs.android.popularmovies.apps.data.remote.MoviesDetail
import com.ngengs.android.popularmovies.apps.data.remote.MoviesList
import com.ngengs.android.popularmovies.apps.data.remote.ReviewList
import com.ngengs.android.popularmovies.apps.data.remote.VideosList
import com.ngengs.android.popularmovies.apps.data.source.MoviesLocalDataSource
import com.ngengs.android.popularmovies.apps.data.source.MoviesRemoteDataSource
import com.ngengs.android.popularmovies.apps.data.source.MoviesRepository
import com.ngengs.android.popularmovies.apps.utils.debugTrySuspend
import com.ngengs.android.popularmovies.apps.utils.networks.FailureType
import com.ngengs.android.popularmovies.apps.utils.networks.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Created by rizky.kharisma on 11/11/22.
 * @ngengs
 */
class MoviesRepositoryImpl(
    private val remoteDataSource: MoviesRemoteDataSource,
    private val localDataSource: MoviesLocalDataSource,
) : MoviesRepository {
    override fun getPopularMovies(page: Int): Flow<Resource<MoviesList>> = flow {
        emit(Resource.Loading(true))
        if (page == 0) {
            val localData = debugTrySuspend(TAG) {
                localDataSource.getPopularMovies(page)
            }
            localData?.let { emit(Resource.Loading(true, it)) }
        }
        val result = try {
            val data = remoteDataSource.getPopularMovies(page + 1)
            if (data.statusCode != null) {
                Resource.Failure(Exception("Something Wrong"), FailureType.SERVER)
            } else {
                if (page == 0) debugTrySuspend(TAG) { localDataSource.savePopularMovies(data.movies) }
                Resource.Success(data)
            }
        } catch (e: Exception) {
            Resource.Failure(e, FailureType.NETWORK)
        }
        emit(result)
    }

    override fun getTopRatedMovies(page: Int): Flow<Resource<MoviesList>> = flow {
        emit(Resource.Loading(true))
        if (page == 0) {
            val localData = debugTrySuspend(TAG) {
                localDataSource.getTopRatedMovies(page)
            }
            localData?.let { emit(Resource.Loading(true, it)) }
        }
        val result = try {
            val data = remoteDataSource.getTopRatedMovies(page + 1)
            if (data.statusCode != null) {
                Resource.Failure(Exception("Something Wrong"), FailureType.SERVER)
            } else {
                if (page == 0) debugTrySuspend(TAG) { localDataSource.saveTopRatedMovies(data.movies) }
                Resource.Success(data)
            }
        } catch (e: Exception) {
            Resource.Failure(e, FailureType.NETWORK)
        }
        emit(result)
    }

    override suspend fun getFavoriteMovies(page: Int): Resource<MoviesList> = try {
        val data = localDataSource.getFavoriteMovies(page)
        Resource.Success(data)
    } catch (e: Exception) {
        Resource.Failure(e, FailureType.EMPTY)
    }

    override fun getDetailMovies(id: Int): Flow<Resource<MoviesDetail>> = flow {
        emit(Resource.Loading(true))
        val localData = debugTrySuspend(TAG) { localDataSource.getDetailMovies(id) }
        localData?.let { emit(Resource.Loading(true, it)) }
        val result = try {
            val data = remoteDataSource.getDetailMovies(id)
            if (data.statusCode != null) {
                Resource.Failure(Exception("Something Wrong"), FailureType.SERVER)
            } else {
                localDataSource.saveMovies(data)
                Resource.Success(data)
            }
        } catch (e: Exception) {
            Resource.Failure(e, FailureType.NETWORK)
        }
        emit(result)
    }

    override suspend fun getMoviesVideos(id: Int): Resource<VideosList> = try {
        val data = remoteDataSource.getMoviesVideos(id)
        if (data.statusCode != null) {
            Resource.Failure(Exception("Something Wrong"), FailureType.SERVER)
        } else {
            Resource.Success(data)
        }
    } catch (e: Exception) {
        Resource.Failure(e, FailureType.NETWORK)
    }

    override suspend fun getMoviesReviews(id: Int): Resource<ReviewList> = try {
        val data = remoteDataSource.getMoviesReviews(id)
        if (data.statusCode != null) {
            Resource.Failure(Exception("Something Wrong"), FailureType.SERVER)
        } else {
            Resource.Success(data)
        }
    } catch (e: Exception) {
        Resource.Failure(e, FailureType.NETWORK)
    }


    override suspend fun saveFavoriteMovies(movies: MoviesDetail) {
        debugTrySuspend { localDataSource.saveFavoriteMovies(movies) }
    }

    override suspend fun removeFavoriteMovies(movies: MoviesDetail) {
        debugTrySuspend { localDataSource.removeFavoriteMovies(movies) }
    }

    override suspend fun isFavoriteMovie(movieId: Int): Boolean {
        val data = debugTrySuspend { localDataSource.getFavoriteMovie(movieId) }
        return data != null
    }

    companion object {
        private const val TAG = "MoviesRepositoryImpl"
    }
}