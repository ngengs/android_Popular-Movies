package com.ngengs.android.popularmovies.apps.data.remote

import com.ngengs.android.popularmovies.apps.globals.Values

/**
 * Created by rizky.kharisma on 11/11/22.
 * @ngengs
 */

fun MoviesDetail.getPosterPath(sizeType: Int): String? {
    return if (posterPath?.isNotEmpty() == true) {
        val size = Values.IMAGE_SIZE_PATH.getOrNull(sizeType) ?: Values.IMAGE_SIZE_PATH.first()
        Values.URL_IMAGE + size + posterPath
    } else null
}


fun MoviesDetail.getBackdropPath(sizeType: Int): String? {
    return if (backdropPath?.isNotEmpty() == true) {
        val size = Values.IMAGE_SIZE_BACKDROP.getOrNull(sizeType) ?: Values.IMAGE_SIZE_BACKDROP.first()
        Values.URL_IMAGE + size + backdropPath
    } else null
}

val VideosDetail.isYoutubeVideo: Boolean get() = site.equals("youtube", ignoreCase = true)

val VideosDetail.youtubeVideo: String? get() =
    if (key.isNotEmpty() && isYoutubeVideo) Values.URL_VIDEO_YOUTUBE + key else null

val VideosDetail.youtubeThumbnail: String? get() = if (key.isNotEmpty() && isYoutubeVideo) {
    String.format(Values.URL_VIDEO_YOUTUBE_THUMB, key)
} else null

val VideosDetail.youtubeSmallThumbnail: String? get() = if (key.isNotEmpty() && isYoutubeVideo) {
    String.format(Values.URL_VIDEO_YOUTUBE_SMALL_THUMB, key)
} else null