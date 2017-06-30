package com.ngengs.android.popularmovies.apps.utils;

import com.ngengs.android.popularmovies.apps.BuildConfig;
import com.ngengs.android.popularmovies.apps.data.MoviesDetail;
import com.ngengs.android.popularmovies.apps.data.MoviesList;
import com.ngengs.android.popularmovies.apps.data.VideosList;
import com.ngengs.android.popularmovies.apps.globals.Values;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by ngengs on 6/15/2017.
 */

@SuppressWarnings("DefaultFileTemplate")
public interface MoviesDBService {

    @GET(Values.URL_PATH_POPULAR + "?" + Values.URL_FRAGMENT_KEY_API + "=" + BuildConfig.API_KEY)
    Observable<MoviesList> listMoviesPopular(@Query(Values.URL_FRAGMENT_KEY_PAGE) int page);

    @GET(Values.URL_PATH_TOP_RATED + "?" + Values.URL_FRAGMENT_KEY_API + "=" + BuildConfig.API_KEY)
    Observable<MoviesList> listMoviesTopRated(@Query(Values.URL_FRAGMENT_KEY_PAGE) int page);

    @GET(Values.URL_PATH_DETAIL + "?" + Values.URL_FRAGMENT_KEY_API + "=" + BuildConfig.API_KEY)
    Observable<MoviesDetail> detail(@Path(Values.URL_PATH_DETAIL_PARAM) int id);

    @GET(Values.URL_PATH_VIDEO + "?" + Values.URL_FRAGMENT_KEY_API + "=" + BuildConfig.API_KEY)
    Observable<VideosList> videos(@Path(Values.URL_PATH_VIDEO_PARAM) int id);

}
