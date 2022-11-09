package com.ngengs.android.popularmovies.apps.utils

import com.ngengs.android.popularmovies.apps.globals.Values
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Created by rizky.kharisma on 09/11/22.
 * @ngengs
 */
object NetworkHelpers {
    private fun provideRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(Values.URL_BASE)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.createAsync())
            .build()
    }

    fun provideAPI(): MoviesAPI {
        return provideRetrofit().create(MoviesAPI::class.java)
    }
}