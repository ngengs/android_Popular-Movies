package com.ngengs.android.popularmovies.apps.utils

import android.util.Log
import com.ngengs.android.popularmovies.apps.BuildConfig

/**
 * Created by rizky.kharisma on 11/11/22.
 * @ngengs
 */

fun <T> debugTry(debugTag: String? = null, block: () -> T?): T? {
    return try {
        block()
    } catch (e: Exception) {
        if (BuildConfig.DEBUG) Log.d(debugTag ?: "DEBUG_TRY", e.toString())
        null
    }
}

suspend fun <T> debugTrySuspend(debugTag: String? = null, block: suspend () -> T?): T? {
    return try {
        block()
    } catch (e: Exception) {
        if (BuildConfig.DEBUG) Log.d(debugTag ?: "DEBUG_TRY", e.toString())
        null
    }
}