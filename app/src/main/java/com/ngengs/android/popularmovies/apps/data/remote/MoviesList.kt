package com.ngengs.android.popularmovies.apps.data.remote

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

/**
 * Created by rizky.kharisma on 10/11/22.
 * @ngengs
 */
@Parcelize
data class MoviesList(
    @SerializedName("page")
    val page: Int = 0,
    @SerializedName("total_result")
    val totalResult: Int = 0,
    @SerializedName("total_pages")
    val totalPage: Int = 0,
    @SerializedName("results")
    val movies: List<MoviesDetail> = mutableListOf(),

    // Error Data
    @SerializedName("status_code")
    val statusCode: Int? = null,
    @SerializedName("status_message")
    val statusMessage: String? = null
) : Parcelable