package com.ngengs.android.popularmovies.apps.data.source

import com.ngengs.android.popularmovies.apps.data.remote.MoviesList

/**
 * Created by rizky.kharisma on 11/11/22.
 * @ngengs
 */
interface MoviesBaseDataSource {
    suspend fun getPopularMovies(page: Int): MoviesList
    suspend fun getTopRatedMovies(page: Int): MoviesList
}