package com.ngengs.android.popularmovies.apps

import android.content.Context
import androidx.annotation.VisibleForTesting
import com.ngengs.android.popularmovies.apps.data.local.MoviesDatabase
import com.ngengs.android.popularmovies.apps.data.local.MoviesDatabaseHelper
import com.ngengs.android.popularmovies.apps.data.source.MoviesLocalDataSource
import com.ngengs.android.popularmovies.apps.data.source.MoviesRemoteDataSource
import com.ngengs.android.popularmovies.apps.data.source.MoviesRepository
import com.ngengs.android.popularmovies.apps.data.source.implementation.MoviesLocalDataSourceImpl
import com.ngengs.android.popularmovies.apps.data.source.implementation.MoviesRemoteDataSourceImpl
import com.ngengs.android.popularmovies.apps.data.source.implementation.MoviesRepositoryImpl
import com.ngengs.android.popularmovies.apps.utils.networks.MoviesAPI
import com.ngengs.android.popularmovies.apps.utils.networks.NetworkHelpers
import com.ngengs.android.popularmovies.apps.utils.pref.MenuPref

/**
 * Created by rizky.kharisma on 11/11/22.
 * @ngengs
 */
object ServiceLocator {
    @Volatile
    lateinit var _networkHelpers: NetworkHelpers
        @VisibleForTesting set

    internal val networkHelpers: NetworkHelpers
        get() {
            if (::_networkHelpers.isInitialized.not()) _networkHelpers = NetworkHelpers
            return _networkHelpers
        }

    private var moviesDatabase: MoviesDatabase? = null
    private var moviesAPI: MoviesAPI? = null

    @Volatile
    lateinit var _moviesRepository: MoviesRepository
        @VisibleForTesting set

    fun provideMoviesRepository(context: Context): MoviesRepository {
        synchronized(this) {
            if (::_moviesRepository.isInitialized.not()) {
                _moviesRepository = createMoviesRepository(context)
            }
            return _moviesRepository
        }
    }

    private fun createMoviesRepository(context: Context): MoviesRepository {
        return MoviesRepositoryImpl(createRemoteDataSource(context), createLocalDataSource(context))
    }

    private fun createRemoteDataSource(context: Context): MoviesRemoteDataSource {
        val api = moviesAPI ?: createMoviesAPI(context)
        return MoviesRemoteDataSourceImpl(api)
    }

    private fun createLocalDataSource(context: Context): MoviesLocalDataSource {
        val database = moviesDatabase ?: createDatabase(context)
        return MoviesLocalDataSourceImpl(database.moviesDao())
    }

    private fun createMoviesAPI(context: Context): MoviesAPI {
        val result = NetworkHelpers.moviesAPI(context)
        moviesAPI = result
        return result
    }

    private fun createDatabase(context: Context): MoviesDatabase {
        val result = MoviesDatabaseHelper.createDatabase(context)
        moviesDatabase = result
        return result
    }
}