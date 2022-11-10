package com.ngengs.android.popularmovies.apps.data.remote

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.ngengs.android.popularmovies.apps.globals.Values
import kotlinx.parcelize.Parcelize

/**
 * Created by rizky.kharisma on 10/11/22.
 * @ngengs
 */
@Parcelize
data class VideosDetail(
    val id: String = "",
    @SerializedName("iso_639_1")
    val iso639_1: String = "",
    @SerializedName("iso_3166_1")
    val iso3166_1: String = "",
    @SerializedName("key")
    val key: String = "",
    @SerializedName("site")
    val site: String = "",
    @SerializedName("size")
    val size: Int = 0,
    @SerializedName("type")
    val type: String= "",

    // Error Data
    @SerializedName("status_code")
    val statusCode: Int? = null,
    @SerializedName("status_message")
    val statusMessage: String? = null,
) : Parcelable