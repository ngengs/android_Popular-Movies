package com.ngengs.android.popularmovies.apps.data

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

/**
 * Created by rizky.kharisma on 10/11/22.
 * @ngengs
 */
@Parcelize
data class ObjectName(
    var id: Int = 0,
    val name: String = "",
    @SerializedName("iso_639_1")
    val iso: String = "",
) : Parcelable
