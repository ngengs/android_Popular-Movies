package com.ngengs.android.popularmovies.apps.globals

/**
 * Created by rizky.kharisma on 09/11/22.
 * @ngengs
 */
object Values {

    /**
     * Server API
     */
    const val URL_BASE = "https://api.themoviedb.org/3/"
    const val URL_PATH_DETAIL = "movie/{id}"
    const val URL_PATH_DETAIL_PARAM = "id"
    const val URL_PATH_POPULAR = "movie/popular"
    const val URL_PATH_TOP_RATED = "movie/top_rated"
    const val URL_PATH_VIDEO = "movie/{id}/videos"
    const val URL_PATH_VIDEO_PARAM = "id"
    const val URL_PATH_REVIEW = "movie/{id}/reviews"
    const val URL_PATH_REVIEW_PARAM = "id"
    const val URL_FRAGMENT_KEY_API = "api_key"
    const val URL_FRAGMENT_KEY_PAGE = "page"
    const val URL_IMAGE = "https://image.tmdb.org/t/p/"
    const val URL_VIDEO_YOUTUBE = "https://www.youtube.com/watch?v="
    const val URL_VIDEO_YOUTUBE_THUMB = "https://img.youtube.com/vi/%s/hqdefault.jpg"
    const val URL_VIDEO_YOUTUBE_SMALL_THUMB = "https://img.youtube.com/vi/%s/default.jpg"
    val IMAGE_SIZE_PATH = arrayOf("original", "w92", "w154", "w185", "w342", "w500", "w780")
    val IMAGE_SIZE_BACKDROP = arrayOf("original", "w300", "w780", "w1280")

    /**
     * Other URL
     */
    const val URL_IMDB_BASE = "https://www.imdb.com/"
    const val URL_IMDB_PATH_TITLE = "title/"


    /**
     * Integer / Decimal Value
     */
    const val TYPE_POPULAR = 0
    const val TYPE_HIGH_RATED = 1
    const val TYPE_FAVORITE = 2
    const val TYPE_DEFAULT_IMAGE_THUMB = 6
    const val RATING_SCORE_PERFECT = 9.0
    const val RATING_SCORE_GOOD = 7.0
    const val RATING_SCORE_NORMAL = 5.0
}