package com.ngengs.android.popularmovies.apps

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.CreationExtras
import com.ngengs.android.popularmovies.apps.screen.moviedetail.MovieDetailViewModel
import com.ngengs.android.popularmovies.apps.screen.movielist.MovieListViewModel

/**
 * Created by rizky.kharisma on 11/11/22.
 * @ngengs
 */

@Suppress("UNCHECKED_CAST")
val MoviesViewModelFactory = object : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        return with(modelClass) {
            val application = checkNotNull(extras[APPLICATION_KEY]) as MoviesApplication
            val moviesRepository = application.moviesRepository
            when {
                isAssignableFrom(MovieListViewModel::class.java) ->
                    MovieListViewModel(moviesRepository)
                isAssignableFrom(MovieDetailViewModel::class.java) ->
                    MovieDetailViewModel(moviesRepository)
                else ->
                    throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
            }
        } as T
    }
}