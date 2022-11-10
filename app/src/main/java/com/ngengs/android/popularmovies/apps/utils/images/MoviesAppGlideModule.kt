package com.ngengs.android.popularmovies.apps.utils.images

import android.content.Context
import com.bumptech.glide.Glide
import com.bumptech.glide.Registry
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.module.AppGlideModule
import com.ngengs.android.popularmovies.apps.utils.networks.NetworkHelpers
import java.io.InputStream

/**
 * Created by rizky.kharisma on 11/11/22.
 * @ngengs
 */
@Suppress("unused")
@GlideModule
class MoviesAppGlideModule : AppGlideModule() {
    override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
        registry.replace(
            GlideUrl::class.java,
            InputStream::class.java,
            OkHttpUrlLoader.Factory(NetworkHelpers.provideOkHttp(context))
        )
    }
}