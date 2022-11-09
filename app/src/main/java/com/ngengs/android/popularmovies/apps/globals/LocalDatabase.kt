package com.ngengs.android.popularmovies.apps.globals

import android.net.Uri

/**
 * Created by rizky.kharisma on 09/11/22.
 * @ngengs
 */
object LocalDatabase {
    const val CONTENT_AUTHORITY = "com.ngengs.android.popularmovies.apps"
    val BASE_CONTENT_URI: Uri = Uri.parse("content://$CONTENT_AUTHORITY")

    const val PATH_MOVIES = "movies"
    const val PATH_POPULAR = "popular"
    const val PATH_TOP_RATED = "top_rated"
    const val PATH_FAVORITES = "favorites"

    const val COLUMN_MOVIE_ID_KEY = "movie_id"

    const val CODE_MOVIES = 100
    const val CODE_MOVIE_BY_ID = 101
    const val CODE_POPULAR_MOVIES = 201
    const val CODE_TOP_RATED_MOVIES = 202
    const val CODE_FAVORITES = 300
}