package com.ngengs.android.popularmovies.apps.utils;

import com.ngengs.android.popularmovies.apps.globals.Values;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by ngengs on 7/4/2017.
 */

@SuppressWarnings({"WeakerAccess"})
public class NetworkHelpers {
    public static Retrofit provideRetrofit() {
        return new Retrofit.Builder()
                .baseUrl(Values.URL_BASE)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.createAsync())
                .build();
    }

    public static MoviesAPI provideAPI() {
        return provideRetrofit().create(MoviesAPI.class);
    }
}
