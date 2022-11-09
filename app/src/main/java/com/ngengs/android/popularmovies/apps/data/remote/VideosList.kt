package com.ngengs.android.popularmovies.apps.data.remote

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

/**
 * Created by rizky.kharisma on 10/11/22.
 * @ngengs
 */
@Parcelize
data class VideosList(
    val id: Int = 0,
    @SerializedName("results")
    val videos: List<VideosDetail> = emptyList(),

    //Error Data
    @SerializedName("status_code")
    var statusCode: Int? = null,
    @SerializedName("status_message")
    val statusMessage: String? = null,
) : Parcelable