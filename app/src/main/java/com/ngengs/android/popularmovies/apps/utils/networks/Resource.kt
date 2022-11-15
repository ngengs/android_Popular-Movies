package com.ngengs.android.popularmovies.apps.utils.networks

/**
 * Created by rizky.kharisma on 11/11/22.
 * @ngengs
 */
sealed class Resource<out T> {
    data class Loading<out T>(val state: Boolean, val tempData: T? = null) : Resource<T>()
    data class Success<out T>(val data: T) : Resource<T>()
    data class Failure<out T>(
        val throwable: Throwable,
        val type: FailureType = FailureType.UNKNOWN,
        val oldData: T? = null
    ) : Resource<T>()
}