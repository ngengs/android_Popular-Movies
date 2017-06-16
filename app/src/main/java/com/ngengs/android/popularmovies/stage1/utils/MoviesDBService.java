package com.ngengs.android.popularmovies.stage1.utils;

import com.ngengs.android.popularmovies.stage1.BuildConfig;
import com.ngengs.android.popularmovies.stage1.data.MoviesDetail;
import com.ngengs.android.popularmovies.stage1.data.MoviesList;
import com.ngengs.android.popularmovies.stage1.globals.Values;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by ngengs on 6/15/2017.
 */

@SuppressWarnings("DefaultFileTemplate")
public interface MoviesDBService {

    @GET(Values.URL_PATH_POPULAR + "?" + Values.URL_FRAGMENT_KEY_API + "=" + BuildConfig.API_KEY)
    Call<MoviesList> listMoviesPopular(@Query(Values.URL_FRAGMENT_KEY_PAGE) int page);

    @GET(Values.URL_PATH_TOP_RATED + "?" + Values.URL_FRAGMENT_KEY_API + "=" + BuildConfig.API_KEY)
    Call<MoviesList> listMoviesTopRated(@Query(Values.URL_FRAGMENT_KEY_PAGE) int page);

    @GET(Values.URL_PATH_DETAIL + "?" + Values.URL_FRAGMENT_KEY_API + "=" + BuildConfig.API_KEY)
    Call<MoviesDetail> detail(@Path(Values.URL_PATH_DETAIL_PARAM) int id);
}
