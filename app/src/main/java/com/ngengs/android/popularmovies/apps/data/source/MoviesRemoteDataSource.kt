package com.ngengs.android.popularmovies.apps.data.source

import com.ngengs.android.popularmovies.apps.data.remote.MoviesDetail
import com.ngengs.android.popularmovies.apps.data.remote.ReviewList
import com.ngengs.android.popularmovies.apps.data.remote.VideosList

/**
 * Created by rizky.kharisma on 11/11/22.
 * @ngengs
 */
interface MoviesRemoteDataSource: MoviesBaseDataSource {
    suspend fun getDetailMovies(id: Int): MoviesDetail
    suspend fun getMoviesVideos(id: Int): VideosList
    suspend fun getMoviesReviews(id: Int): ReviewList
}