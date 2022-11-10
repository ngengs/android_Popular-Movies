package com.ngengs.android.popularmovies.apps.data.local

import android.provider.BaseColumns
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.ngengs.android.popularmovies.apps.globals.LocalDatabase

/**
 * Created by rizky.kharisma on 10/11/22.
 * @ngengs
 */
@Entity(
    tableName = LocalDatabase.PATH_TOP_RATED,
    foreignKeys = [
        ForeignKey(
            entity = Movies::class,
            parentColumns = arrayOf(BaseColumns._ID),
            childColumns = arrayOf(LocalDatabase.COLUMN_MOVIE_ID_KEY),
        )
    ]
)
data class MoviesTopRated(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = BaseColumns._ID)
    val id: Int? = null,
    @ColumnInfo(name = LocalDatabase.COLUMN_MOVIE_ID_KEY, index = true)
    val movieId: Int,
)
