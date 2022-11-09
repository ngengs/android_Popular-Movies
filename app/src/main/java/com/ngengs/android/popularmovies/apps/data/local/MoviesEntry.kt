package com.ngengs.android.popularmovies.apps.data.local

import android.content.ContentResolver
import android.content.ContentUris
import android.net.Uri
import android.provider.BaseColumns
import com.ngengs.android.popularmovies.apps.globals.LocalDatabase
import com.ngengs.android.popularmovies.apps.globals.LocalDatabase.BASE_CONTENT_URI

/**
 * Created by rizky.kharisma on 10/11/22.
 * @ngengs
 */
object MoviesEntry {
    @JvmStatic
    val CONTENT_URI: Uri = BASE_CONTENT_URI.buildUpon().appendPath(LocalDatabase.PATH_MOVIES).build()

    const val CONTENT_DIR_TYPE =
        ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + LocalDatabase.CONTENT_AUTHORITY + "/" + LocalDatabase.PATH_MOVIES
    const val CONTENT_ITEM_TYPE =
        ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + LocalDatabase.CONTENT_AUTHORITY + "/" + LocalDatabase.PATH_MOVIES

    const val TABLE_NAME = "movies"

    const val COLUMN_ORIGINAL_TITLE = "original_title"
    const val COLUMN_OVERVIEW = "overview"
    const val COLUMN_RELEASE_DATE = "release_date"
    const val COLUMN_POSTER_PATH = "poster_path"
    const val COLUMN_POPULARITY = "popularity"
    const val COLUMN_TITLE = "title"
    const val COLUMN_AVERAGE_VOTE = "vote_average"
    const val COLUMN_VOTE_COUNT = "vote_count"
    const val COLUMN_BACKDROP_PATH = "backdrop_path"

    const val SQL_CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " (" +
            BaseColumns._ID + " INTEGER PRIMARY KEY, " +
            COLUMN_ORIGINAL_TITLE + " TEXT, " +
            COLUMN_OVERVIEW + " TEXT, " +
            COLUMN_RELEASE_DATE + " TEXT, " +
            COLUMN_POSTER_PATH + " TEXT, " +
            COLUMN_POPULARITY + " REAL, " +
            COLUMN_TITLE + " TEXT, " +
            COLUMN_AVERAGE_VOTE + " REAL, " +
            COLUMN_VOTE_COUNT + " INTEGER," +
            COLUMN_BACKDROP_PATH + " TEXT " +
            " );"

    private val COLUMNS = arrayOf(
        BaseColumns._ID, COLUMN_ORIGINAL_TITLE, COLUMN_OVERVIEW,
        COLUMN_RELEASE_DATE, COLUMN_POSTER_PATH, COLUMN_POPULARITY, COLUMN_TITLE,
        COLUMN_AVERAGE_VOTE, COLUMN_VOTE_COUNT, COLUMN_BACKDROP_PATH
    )

    @JvmStatic
    fun buildMovieUri(id: Long): Uri = ContentUris.withAppendedId(CONTENT_URI, id)

    @JvmStatic
    fun getIdFromUri(uri: Uri?): Long = ContentUris.parseId(uri!!)

    @JvmStatic
    fun getColumns(): Array<String> = COLUMNS.clone()

    @JvmStatic
    fun getColumnsWithTable(): Array<String> {
        val tmp = COLUMNS.clone()
        tmp[0] = TABLE_NAME + "." + BaseColumns._ID
        return tmp
    }
}