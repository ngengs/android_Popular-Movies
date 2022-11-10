package com.ngengs.android.popularmovies.apps.data.local

import android.provider.BaseColumns
import androidx.room.Embedded
import androidx.room.Relation
import com.ngengs.android.popularmovies.apps.globals.LocalDatabase

/**
 * Created by rizky.kharisma on 10/11/22.
 * @ngengs
 */

data class MoviesFavoritesAndDetail(
    @Relation(
        parentColumn = LocalDatabase.COLUMN_MOVIE_ID_KEY,
        entityColumn = BaseColumns._ID,
    )
    val movies: Movies?,
    @Embedded
    val favorite: MoviesFavorites?,
)
