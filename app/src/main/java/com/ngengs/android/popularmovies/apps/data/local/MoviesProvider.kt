package com.ngengs.android.popularmovies.apps.data.local

import android.content.ContentProvider
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteQueryBuilder
import android.net.Uri
import android.provider.BaseColumns
import android.util.Log
import com.ngengs.android.popularmovies.apps.globals.LocalDatabase
import com.ngengs.android.popularmovies.apps.utils.DatabaseHelper
import kotlin.collections.HashSet

/**
 * Created by rizky.kharisma on 10/11/22.
 * @ngengs
 */
open class MoviesProvider : ContentProvider() {
    private lateinit var databaseHelper: DatabaseHelper

    override fun onCreate(): Boolean {
        databaseHelper = DatabaseHelper(context!!)
        return false
    }

    override fun query(
        uri: Uri,
        projection: Array<String?>?,
        selection: String?,
        selectionArgs: Array<String?>?,
        sortOrder: String?
    ): Cursor? {
        val match = URI_MATCHER.match(uri)
        checkColumns(match, projection)
        val cursor: Cursor? = when (match) {
            LocalDatabase.CODE_MOVIES -> databaseHelper.readableDatabase.query(
                MoviesEntry.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
            )
            LocalDatabase.CODE_MOVIE_BY_ID -> getMovieById(uri, projection, sortOrder)
            LocalDatabase.CODE_POPULAR_MOVIES -> getMoviesFromReferenceTable(
                MoviesPopular.TABLE_NAME,
                projection, selection, selectionArgs, sortOrder
            )
            LocalDatabase.CODE_TOP_RATED_MOVIES -> getMoviesFromReferenceTable(
                MoviesTopRated.TABLE_NAME,
                projection, selection, selectionArgs, sortOrder
            )
            LocalDatabase.CODE_FAVORITES -> getMoviesFromReferenceTable(
                MoviesFavorites.TABLE_NAME,
                projection, selection, selectionArgs, sortOrder
            )
            else -> return null
        }
        cursor?.setNotificationUri(context!!.contentResolver, uri)
        return cursor
    }

    override fun getType(uri: Uri): String? {
        return when (URI_MATCHER.match(uri)) {
            LocalDatabase.CODE_MOVIES -> MoviesEntry.CONTENT_DIR_TYPE
            LocalDatabase.CODE_MOVIE_BY_ID -> MoviesEntry.CONTENT_ITEM_TYPE
            LocalDatabase.CODE_POPULAR_MOVIES -> MoviesPopular.CONTENT_DIR_TYPE
            LocalDatabase.CODE_TOP_RATED_MOVIES -> MoviesTopRated.CONTENT_DIR_TYPE
            LocalDatabase.CODE_FAVORITES -> MoviesFavorites.CONTENT_DIR_TYPE
            else -> null
        }
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        val db = databaseHelper.writableDatabase
        val match = URI_MATCHER.match(uri)
        val id: Long
        val returnUri: Uri = when (match) {
            LocalDatabase.CODE_MOVIES -> {
                id = db.insertWithOnConflict(
                    MoviesEntry.TABLE_NAME, null,
                    values, SQLiteDatabase.CONFLICT_REPLACE
                )
                if (id > 0) {
                    MoviesEntry.buildMovieUri(id)
                } else {
                    throw SQLException(FAILED_TO_INSERT_ROW_INTO + uri)
                }
            }
            LocalDatabase.CODE_POPULAR_MOVIES -> {
                id = db.insert(MoviesPopular.TABLE_NAME, null, values)
                if (id > 0) {
                    MoviesPopular.CONTENT_URI
                } else {
                    throw SQLException(FAILED_TO_INSERT_ROW_INTO + uri)
                }
            }
            LocalDatabase.CODE_TOP_RATED_MOVIES -> {
                id = db.insert(MoviesTopRated.TABLE_NAME, null, values)
                if (id > 0) {
                    MoviesTopRated.CONTENT_URI
                } else {
                    throw SQLException(FAILED_TO_INSERT_ROW_INTO + uri)
                }
            }
            LocalDatabase.CODE_FAVORITES -> {
                id = db.insert(MoviesFavorites.TABLE_NAME, null, values)
                if (id > 0) {
                    MoviesFavorites.CONTENT_URI
                } else {
                    throw SQLException(FAILED_TO_INSERT_ROW_INTO + uri)
                }
            }
            else -> throw UnsupportedOperationException("Unknown uri: $uri")
        }
        context!!.contentResolver.notifyChange(uri, null)
        return returnUri
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int {
        val db = databaseHelper.writableDatabase
        val match = URI_MATCHER.match(uri)
        val rowsDeleted: Int = when (match) {
            LocalDatabase.CODE_MOVIES -> db.delete(MoviesEntry.TABLE_NAME, selection, selectionArgs)
            LocalDatabase.CODE_MOVIE_BY_ID -> {
                val id = MoviesEntry.getIdFromUri(uri)
                db.delete(
                    MoviesEntry.TABLE_NAME,
                    MOVIE_ID_SELECTION,
                    arrayOf(java.lang.Long.toString(id))
                )
            }
            LocalDatabase.CODE_POPULAR_MOVIES -> db.delete(
                MoviesPopular.TABLE_NAME,
                selection,
                selectionArgs
            )
            LocalDatabase.CODE_TOP_RATED_MOVIES -> db.delete(
                MoviesTopRated.TABLE_NAME,
                selection,
                selectionArgs
            )
            LocalDatabase.CODE_FAVORITES -> db.delete(
                MoviesFavorites.TABLE_NAME,
                selection,
                selectionArgs
            )
            else -> throw java.lang.UnsupportedOperationException("Unknown uri: $uri")
        }
        if (rowsDeleted != 0) {
            context!!.contentResolver.notifyChange(uri, null)
        }
        return rowsDeleted
    }

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?
    ): Int {
        val db = databaseHelper.writableDatabase
        val match = URI_MATCHER.match(uri)
        val rowsUpdated: Int = when (match) {
            LocalDatabase.CODE_MOVIES ->
                db.update(MoviesEntry.TABLE_NAME, values, selection, selectionArgs)
            else -> throw java.lang.UnsupportedOperationException("Unknown uri: $uri")
        }
        if (rowsUpdated != 0) {
            context!!.contentResolver.notifyChange(uri, null)
        }
        return rowsUpdated
    }

    override fun shutdown() {
        databaseHelper.close()
        super.shutdown()
    }


    override fun bulkInsert(uri: Uri, values: Array<ContentValues?>): Int {
        val db = databaseHelper.writableDatabase
        val match = URI_MATCHER.match(uri)
        var returnCount = 0
        return when (match) {
            LocalDatabase.CODE_MOVIES -> {
                db.beginTransaction()
                try {
                    for (value in values) {
                        val id = db.insertWithOnConflict(
                            MoviesEntry.TABLE_NAME,
                            null,
                            value,
                            SQLiteDatabase.CONFLICT_REPLACE
                        )
                        if (id != -1L) {
                            returnCount++
                        }
                    }
                    db.setTransactionSuccessful()
                } finally {
                    db.endTransaction()
                }
                context!!.contentResolver.notifyChange(uri, null)
                returnCount
            }
            LocalDatabase.CODE_POPULAR_MOVIES -> {
                db.beginTransaction()
                try {
                    db.setTransactionSuccessful()
                    for (value in values) {
                        val id = db.insertWithOnConflict(
                            MoviesPopular.TABLE_NAME,
                            null,
                            value,
                            SQLiteDatabase.CONFLICT_REPLACE
                        )
                        if (id != -1L) {
                            returnCount++
                        }
                    }
                } finally {
                    db.endTransaction()
                }
                context!!.contentResolver.notifyChange(uri, null)
                returnCount
            }
            LocalDatabase.CODE_TOP_RATED_MOVIES -> {
                db.beginTransaction()
                try {
                    db.setTransactionSuccessful()
                    for (value in values) {
                        val id = db.insertWithOnConflict(
                            MoviesTopRated.TABLE_NAME,
                            null,
                            value,
                            SQLiteDatabase.CONFLICT_REPLACE
                        )
                        if (id != -1L) {
                            returnCount++
                        }
                    }
                } finally {
                    db.endTransaction()
                }
                context!!.contentResolver.notifyChange(uri, null)
                returnCount
            }
            else -> super.bulkInsert(uri, values)
        }
    }

    private fun getMovieById(uri: Uri, projection: Array<String?>?, sortOrder: String?): Cursor? {
        val id = MoviesEntry.getIdFromUri(uri)
        return databaseHelper.readableDatabase.query(
            MoviesEntry.TABLE_NAME,
            projection,
            MOVIE_ID_SELECTION, arrayOf(id.toString()),
            null,
            null,
            sortOrder
        )
    }


    private fun getMoviesFromReferenceTable(
        tableName: String, projection: Array<String?>?, selection: String?,
        selectionArgs: Array<String?>?, sortOrder: String?
    ): Cursor? {
        val sqLiteQueryBuilder = SQLiteQueryBuilder()

        // tableName INNER JOIN movies ON tableName.movie_id = movies._id
        sqLiteQueryBuilder.tables = tableName + " INNER JOIN " + MoviesEntry.TABLE_NAME +
                " ON " + tableName + "." + LocalDatabase.COLUMN_MOVIE_ID_KEY +
                " = " + MoviesEntry.TABLE_NAME + "." + BaseColumns._ID
        Log.d(TAG, "getMoviesFromReferenceTable: $sqLiteQueryBuilder")
        return sqLiteQueryBuilder.query(
            databaseHelper.readableDatabase,
            projection,
            selection,
            selectionArgs,
            null,
            null,
            sortOrder
        )
    }

    private fun checkColumns(match: Int, projection: Array<String?>?) {
        if (projection != null) {
            val availableColumns = HashSet(listOf(MoviesEntry.getColumnsWithTable()))
            val requestedColumns = HashSet(listOf(projection.mapNotNull { it }.toTypedArray()))
//            require(availableColumns.containsAll(requestedColumns)) { "Unknown columns in projection." }
        }
    }

    companion object {
        const val TAG = "MoviesProvider"


        private val URI_MATCHER = buildUriMatcher()

        private const val FAILED_TO_INSERT_ROW_INTO = "Failed to insert row into "

        // movies._id = ?
        private const val MOVIE_ID_SELECTION =
            MoviesEntry.TABLE_NAME + "." + BaseColumns._ID + " = ? "


        private fun buildUriMatcher(): UriMatcher {
            val uriMatcher = UriMatcher(UriMatcher.NO_MATCH)
            val authority = LocalDatabase.CONTENT_AUTHORITY
            uriMatcher.addURI(authority, LocalDatabase.PATH_MOVIES, LocalDatabase.CODE_MOVIES)
            uriMatcher.addURI(
                authority,
                LocalDatabase.PATH_MOVIES + "/#",
                LocalDatabase.CODE_MOVIE_BY_ID
            )
            uriMatcher.addURI(
                authority,
                LocalDatabase.PATH_MOVIES + "/" + LocalDatabase.PATH_POPULAR,
                LocalDatabase.CODE_POPULAR_MOVIES
            )
            uriMatcher.addURI(
                authority,
                LocalDatabase.PATH_MOVIES + "/" + LocalDatabase.PATH_TOP_RATED,
                LocalDatabase.CODE_TOP_RATED_MOVIES
            )
            uriMatcher.addURI(
                authority,
                LocalDatabase.PATH_MOVIES + "/" + LocalDatabase.PATH_FAVORITES,
                LocalDatabase.CODE_FAVORITES
            )
            return uriMatcher
        }
    }
}