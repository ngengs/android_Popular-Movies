package com.ngengs.android.popularmovies.apps.utils.networks

/**
 * Created by rizky.kharisma on 15/11/22.
 * @ngengs
 */

fun <T> Resource<T>.getAnyData() = when (this) {
    is Resource.Loading -> this.tempData
    is Resource.Failure -> this.oldData
    is Resource.Success -> this.data
}