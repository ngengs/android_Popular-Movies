package com.ngengs.android.popularmovies.apps.data

import android.content.ContentValues
import android.database.Cursor
import android.os.Parcelable
import android.provider.BaseColumns
import android.util.Log
import com.google.gson.annotations.SerializedName
import com.ngengs.android.popularmovies.apps.data.local.MoviesEntry
import com.ngengs.android.popularmovies.apps.globals.Values
import com.ngengs.android.popularmovies.apps.globals.Values.IMAGE_SIZE_BACKDROP
import com.ngengs.android.popularmovies.apps.globals.Values.IMAGE_SIZE_PATH
import kotlinx.parcelize.Parcelize
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by rizky.kharisma on 10/11/22.
 * @ngengs
 */
@Parcelize
data class MoviesDetail(
    var id: Int = 0,
    @SerializedName("adult")
    val adult: Boolean = false,
    @SerializedName("backdrop_path")
    val backdropPath: String? = null,
    @SerializedName("budget")
    val budget: Float = 0F,
    @SerializedName("genres")
    val genres: List<ObjectName> = emptyList(),
    @SerializedName("homepage")
    val homepage: String? = null,
    @SerializedName("imdb_id")
    val imdbId: String? = null,
    @SerializedName("original_language")
    val originalLanguage: String? = null,
    @SerializedName("original_title")
    val originalTitle: String? = null,
    @SerializedName("overview")
    val overview: String? = null,
    @SerializedName("popularity")
    val popularity: Double = 0.0,
    @SerializedName("poster_path")
    val posterPath: String? = null,
    @SerializedName("production_companies")
    val productionCompanies: List<ObjectName> = emptyList(),
    @SerializedName("production_countries")
    val productionCountries: List<ObjectName> = emptyList(),
    @SerializedName("release_date")
    val releaseDate: Date? = null,
    @SerializedName("revenue")
    val revenue: Float = 0F,
    @SerializedName("runtime")
    val runtime: Int = 0,
    @SerializedName("spoken_languages")
    val spokenLanguages: List<ObjectName> = emptyList(),
    @SerializedName("status")
    val status: String? = null,
    @SerializedName("tagline")
    val tagline: String? = null,
    @SerializedName("title")
    val title: String? = null,
    @SerializedName("video")
    val video: Boolean = false,
    @SerializedName("vote_average")
    val voteAverage: Double = 0.0,
    @SerializedName("vote_count")
    val voteCount: Long = 0L,

    // Error Data
    @SerializedName("status_code")
    val statusCode: Int? = null,
    @SerializedName("status_message")
    val statusMessage: String? = null
) : Parcelable {
    fun getPosterPath(sizeType: Int): String? {
        return if (posterPath?.isNotEmpty() == true) {
            var size = IMAGE_SIZE_PATH[0]
            if (sizeType >= 0 && sizeType < IMAGE_SIZE_PATH.size) size = IMAGE_SIZE_PATH[sizeType]
            Values.URL_IMAGE + size + posterPath
        } else null
    }

    fun getBackdropPath(sizeType: Int): String? {
        return if (backdropPath?.isNotEmpty() == true) {
            var size = IMAGE_SIZE_BACKDROP[0]
            if (sizeType >= 0 && sizeType < IMAGE_SIZE_BACKDROP.size) size =
                IMAGE_SIZE_BACKDROP[sizeType]
            Values.URL_IMAGE + size + backdropPath
        } else null
    }

    fun toContentValues(): ContentValues {
        val values = ContentValues()
        values.put(BaseColumns._ID, id)
        values.put(MoviesEntry.COLUMN_ORIGINAL_TITLE, originalTitle)
        values.put(MoviesEntry.COLUMN_OVERVIEW, overview)
        values.put(MoviesEntry.COLUMN_RELEASE_DATE, releaseDate.toString())
        values.put(MoviesEntry.COLUMN_POSTER_PATH, posterPath)
        values.put(MoviesEntry.COLUMN_POPULARITY, popularity)
        values.put(MoviesEntry.COLUMN_TITLE, title)
        values.put(MoviesEntry.COLUMN_AVERAGE_VOTE, voteAverage)
        values.put(MoviesEntry.COLUMN_VOTE_COUNT, voteCount)
        values.put(MoviesEntry.COLUMN_BACKDROP_PATH, backdropPath)
        return values
    }

    companion object {
        @JvmStatic
        fun fromCursor(cursor: Cursor): MoviesDetail {
            val movieId = cursor.getInt(cursor.getColumnIndex(BaseColumns._ID))
            val movieTitle = cursor.getString(cursor.getColumnIndex(MoviesEntry.COLUMN_TITLE))
            val movieOriginalTitle =
                cursor.getString(cursor.getColumnIndex(MoviesEntry.COLUMN_ORIGINAL_TITLE))
            val movieOverview = cursor.getString(cursor.getColumnIndex(MoviesEntry.COLUMN_OVERVIEW))
            val releaseString =
                cursor.getString(cursor.getColumnIndex(MoviesEntry.COLUMN_RELEASE_DATE))
            val movieDate: Date? = if (releaseString != null && releaseString != "") {
                val formatter = SimpleDateFormat("EEE MMM dd kk:mm:ss zzz yyyy")
                try {
                    formatter.parse(releaseString)
                } catch (e: ParseException) {
                    Log.e("MoviesDetail", "fromCursor: ", e)
                    null
                }
            } else null
            val moviePosterPath =
                cursor.getString(cursor.getColumnIndex(MoviesEntry.COLUMN_POSTER_PATH))
            val moviePopularity =
                cursor.getDouble(cursor.getColumnIndex(MoviesEntry.COLUMN_POPULARITY))
            val movieVoteAverage =
                cursor.getDouble(cursor.getColumnIndex(MoviesEntry.COLUMN_AVERAGE_VOTE))
            val movieVoteCount = cursor.getInt(cursor.getColumnIndex(MoviesEntry.COLUMN_VOTE_COUNT))
            val movieBackdropPath =
                cursor.getString(cursor.getColumnIndex(MoviesEntry.COLUMN_BACKDROP_PATH))

            return MoviesDetail(
                id = movieId,
                title = movieTitle,
                originalTitle = movieOriginalTitle,
                overview = movieOverview,
                releaseDate = movieDate,
                posterPath = moviePosterPath,
                popularity = moviePopularity,
                voteAverage = movieVoteAverage,
                voteCount = movieVoteCount.toLong(),
                backdropPath = movieBackdropPath,
            )
        }
    }
}
