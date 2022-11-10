package com.ngengs.android.popularmovies.apps.data.local

import android.provider.BaseColumns
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ngengs.android.popularmovies.apps.globals.LocalDatabase
import java.util.Date

/**
 * Created by rizky.kharisma on 10/11/22.
 * @ngengs
 */

@Entity(tableName = LocalDatabase.PATH_MOVIES)
data class Movies(
    @PrimaryKey
    @ColumnInfo(name = BaseColumns._ID)
    val id: Int,
    @ColumnInfo(name = "original_title")
    val originalTitle: String?,
    @ColumnInfo(name = "overview")
    val overview: String?,
    @ColumnInfo(name = "release_date")
    val releaseDate: Date?,
    @ColumnInfo(name = "poster_path")
    val posterPath: String?,
    @ColumnInfo(name = "popularity")
    val popularity: Double,
    @ColumnInfo(name = "title")
    val title: String?,
    @ColumnInfo(name = "vote_average")
    val voteAverage: Double,
    @ColumnInfo(name = "vote_count")
    val voteCount: Long,
    @ColumnInfo(name = "backdrop_path")
    val backdropPath: String?,
)