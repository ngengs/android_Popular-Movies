package com.ngengs.android.popularmovies.apps.data.remote

import com.ngengs.android.popularmovies.apps.globals.Values

/**
 * Created by rizky.kharisma on 11/11/22.
 * @ngengs
 */

fun MoviesDetail.getPosterPath(sizeType: Int): String? {
    return if (posterPath?.isNotEmpty() == true) {
        var size = Values.IMAGE_SIZE_PATH[0]
        if (sizeType >= 0 && sizeType < Values.IMAGE_SIZE_PATH.size) size = Values.IMAGE_SIZE_PATH[sizeType]
        Values.URL_IMAGE + size + posterPath
    } else null
}


fun MoviesDetail.getBackdropPath(sizeType: Int): String? {
    return if (backdropPath?.isNotEmpty() == true) {
        var size = Values.IMAGE_SIZE_BACKDROP[0]
        if (sizeType >= 0 && sizeType < Values.IMAGE_SIZE_BACKDROP.size) size =
            Values.IMAGE_SIZE_BACKDROP[sizeType]
        Values.URL_IMAGE + size + backdropPath
    } else null
}

val VideosDetail.isYoutubeVideo: Boolean get() = site.equals("youtube", ignoreCase = true)

val VideosDetail.youtubeVideo: String? get() =
    if (key.isNotEmpty() && isYoutubeVideo) Values.URL_VIDEO_YOUTUBE + key else null

val VideosDetail.youtubeThumbnail: String? get() = if (key.isNotEmpty() && isYoutubeVideo) {
    String.format(Values.URL_VIDEO_YOUTUBE_THUMB, key)
} else null