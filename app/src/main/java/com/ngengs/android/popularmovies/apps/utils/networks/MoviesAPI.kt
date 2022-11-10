package com.ngengs.android.popularmovies.apps.utils.networks

import com.ngengs.android.popularmovies.apps.BuildConfig
import com.ngengs.android.popularmovies.apps.data.remote.MoviesDetail
import com.ngengs.android.popularmovies.apps.data.remote.MoviesList
import com.ngengs.android.popularmovies.apps.data.remote.ReviewList
import com.ngengs.android.popularmovies.apps.data.remote.VideosList
import com.ngengs.android.popularmovies.apps.globals.Values
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Created by rizky.kharisma on 09/11/22.
 * @ngengs
 */
interface MoviesAPI {
    @GET(Values.URL_PATH_POPULAR + "?" + Values.URL_FRAGMENT_KEY_API + "=" + BuildConfig.API_KEY)
    suspend fun listMoviesPopular(@Query(Values.URL_FRAGMENT_KEY_PAGE) page: Int): MoviesList

    @GET(Values.URL_PATH_TOP_RATED + "?" + Values.URL_FRAGMENT_KEY_API + "=" + BuildConfig.API_KEY)
    suspend fun listMoviesTopRated(@Query(Values.URL_FRAGMENT_KEY_PAGE) page: Int): MoviesList

    @GET(Values.URL_PATH_DETAIL + "?" + Values.URL_FRAGMENT_KEY_API + "=" + BuildConfig.API_KEY)
    suspend fun detail(@Path(Values.URL_PATH_DETAIL_PARAM) id: Int): MoviesDetail

    @GET(Values.URL_PATH_VIDEO + "?" + Values.URL_FRAGMENT_KEY_API + "=" + BuildConfig.API_KEY)
    suspend fun videos(@Path(Values.URL_PATH_VIDEO_PARAM) id: Int): VideosList

    @GET(Values.URL_PATH_REVIEW + "?" + Values.URL_FRAGMENT_KEY_API + "=" + BuildConfig.API_KEY)
    suspend fun reviews(@Path(Values.URL_PATH_REVIEW_PARAM) id: Int): ReviewList
}