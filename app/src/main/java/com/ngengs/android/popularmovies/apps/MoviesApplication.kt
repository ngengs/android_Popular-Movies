package com.ngengs.android.popularmovies.apps

import android.app.Application
import android.util.Log
import com.ngengs.android.popularmovies.apps.data.source.MoviesRepository

/**
 * Created by rizky.kharisma on 11/11/22.
 * @ngengs
 */
class MoviesApplication: Application() {
    val moviesRepository: MoviesRepository
        get() = ServiceLocator.provideMoviesRepository(this)

    override fun onCreate() {
        super.onCreate()
        Log.d("MoviesApplication", "onCreate")
    }
}