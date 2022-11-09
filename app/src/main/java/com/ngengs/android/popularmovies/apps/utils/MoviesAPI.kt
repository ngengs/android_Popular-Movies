package com.ngengs.android.popularmovies.apps.utils

import com.ngengs.android.popularmovies.apps.BuildConfig
import com.ngengs.android.popularmovies.apps.data.MoviesDetail
import com.ngengs.android.popularmovies.apps.data.MoviesList
import com.ngengs.android.popularmovies.apps.data.ReviewList
import com.ngengs.android.popularmovies.apps.data.VideosList
import com.ngengs.android.popularmovies.apps.globals.Values
import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Created by rizky.kharisma on 09/11/22.
 * @ngengs
 */
interface MoviesAPI {
    @GET(Values.URL_PATH_POPULAR + "?" + Values.URL_FRAGMENT_KEY_API + "=" + BuildConfig.API_KEY)
    fun listMoviesPopular(@Query(Values.URL_FRAGMENT_KEY_PAGE) page: Int): Observable<MoviesList>

    @GET(Values.URL_PATH_TOP_RATED + "?" + Values.URL_FRAGMENT_KEY_API + "=" + BuildConfig.API_KEY)
    fun listMoviesTopRated(@Query(Values.URL_FRAGMENT_KEY_PAGE) page: Int): Observable<MoviesList>

    @GET(Values.URL_PATH_DETAIL + "?" + Values.URL_FRAGMENT_KEY_API + "=" + BuildConfig.API_KEY)
    fun detail(@Path(Values.URL_PATH_DETAIL_PARAM) id: Int): Observable<MoviesDetail>

    @GET(Values.URL_PATH_VIDEO + "?" + Values.URL_FRAGMENT_KEY_API + "=" + BuildConfig.API_KEY)
    fun videos(@Path(Values.URL_PATH_VIDEO_PARAM) id: Int): Observable<VideosList>

    @GET(Values.URL_PATH_REVIEW + "?" + Values.URL_FRAGMENT_KEY_API + "=" + BuildConfig.API_KEY)
    fun reviews(@Path(Values.URL_PATH_REVIEW_PARAM) id: Int): Observable<ReviewList>
}