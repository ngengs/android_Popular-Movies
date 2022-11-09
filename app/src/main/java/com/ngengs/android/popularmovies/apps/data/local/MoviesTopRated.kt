package com.ngengs.android.popularmovies.apps.data.local

import android.content.ContentResolver
import android.net.Uri
import android.provider.BaseColumns
import com.ngengs.android.popularmovies.apps.globals.LocalDatabase

/**
 * Created by rizky.kharisma on 10/11/22.
 * @ngengs
 */
object MoviesTopRated : BaseColumns {
    val CONTENT_URI: Uri = MoviesEntry.CONTENT_URI.buildUpon()
        .appendPath(LocalDatabase.PATH_TOP_RATED)
        .build()
    const val CONTENT_DIR_TYPE =
        (ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + LocalDatabase.CONTENT_AUTHORITY + "/" + LocalDatabase.PATH_MOVIES
                + "/" + LocalDatabase.PATH_TOP_RATED)

    const val TABLE_NAME = "top_rated_movies"

    const val SQL_CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " (" +
            BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            LocalDatabase.COLUMN_MOVIE_ID_KEY + " INTEGER NOT NULL, " +
            " FOREIGN KEY (" + LocalDatabase.COLUMN_MOVIE_ID_KEY + ") REFERENCES " +
            MoviesEntry.TABLE_NAME + " (" + BaseColumns._ID + ") " +
            " );"

    private val COLUMNS = arrayOf(BaseColumns._ID, LocalDatabase.COLUMN_MOVIE_ID_KEY)

    fun getColumns(): Array<String> = COLUMNS.clone()
}