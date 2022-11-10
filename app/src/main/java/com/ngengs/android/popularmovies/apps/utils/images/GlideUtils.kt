package com.ngengs.android.popularmovies.apps.utils.images

import android.content.Context
import com.bumptech.glide.Glide

/**
 * Created by rizky.kharisma on 11/11/22.
 * @ngengs
 */
object GlideUtils {
    fun thumbnailBuilder(context: Context, url: String?) = Glide.with(context).load(url).sizeMultiplier(0.05F)
}