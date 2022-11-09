package com.ngengs.android.popularmovies.apps.utils.networks

import android.content.Context
import com.ngengs.android.popularmovies.apps.BuildConfig
import com.ngengs.android.popularmovies.apps.globals.Values
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit


/**
 * Created by rizky.kharisma on 09/11/22.
 * @ngengs
 */
object NetworkHelpers {
    private const val CACHE_SIZE = 10 * 1024 * 1024L  // 10 MB
    private const val CONNECT_TIMEOUT = 15
    private const val WRITE_TIMEOUT = 60
    private const val TIMEOUT = 60

    private fun provideOkHttp(context: Context): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor()
        if (BuildConfig.DEBUG) loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY)

        val cache = Cache(context.cacheDir, CACHE_SIZE)

        return OkHttpClient.Builder()
            .connectTimeout(CONNECT_TIMEOUT.toLong(), TimeUnit.SECONDS)
            .writeTimeout(WRITE_TIMEOUT.toLong(), TimeUnit.SECONDS)
            .readTimeout(TIMEOUT.toLong(), TimeUnit.SECONDS)
            .addInterceptor(loggingInterceptor)
            .cache(cache)
            .build()
    }

    private fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit = Retrofit.Builder()
        .baseUrl(Values.URL_BASE)
        .addConverterFactory(GsonConverterFactory.create(GsonConfig.gson))
        .addCallAdapterFactory(RxJava2CallAdapterFactory.createAsync())
        .client(okHttpClient)
        .build()

    fun provideAPI(context: Context): MoviesAPI {
        return provideRetrofit(provideOkHttp(context)).create(MoviesAPI::class.java)
    }
}