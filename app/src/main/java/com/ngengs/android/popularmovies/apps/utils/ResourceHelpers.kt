package com.ngengs.android.popularmovies.apps.utils

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import androidx.core.content.res.ResourcesCompat

/**
 * Created by rizky.kharisma on 09/11/22.
 * @ngengs
 */
object ResourceHelpers {
    fun getColor(context: Context, colorId: Int): Int =
        ResourcesCompat.getColor(context.resources, colorId, context.theme)


    fun getDrawable(context: Context, drawableId: Int): Drawable =
        ResourcesCompat.getDrawable(context.resources, drawableId, context.theme)!!
}