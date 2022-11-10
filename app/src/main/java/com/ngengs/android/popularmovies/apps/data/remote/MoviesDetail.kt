package com.ngengs.android.popularmovies.apps.data.remote

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.ngengs.android.popularmovies.apps.globals.Values
import kotlinx.parcelize.Parcelize
import java.util.Date

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
) : Parcelable