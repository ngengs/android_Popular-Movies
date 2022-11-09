package com.ngengs.android.popularmovies.apps.data.local

import android.content.ContentValues
import android.content.Context
import com.ngengs.android.popularmovies.apps.data.remote.MoviesDetail
import com.ngengs.android.popularmovies.apps.data.remote.MoviesList
import com.ngengs.android.popularmovies.apps.globals.LocalDatabase

/**
 * Created by rizky.kharisma on 10/11/22.
 * @ngengs
 */
class MoviesProviderHelper(private val context: Context) {
    fun getFavorites(): MoviesList? {
        val cursor = context.contentResolver.query(
            MoviesFavorites.CONTENT_URI,
            MoviesEntry.getColumnsWithTable(),
            null,
            null,
            null
        )
        val data: MutableList<MoviesDetail> = mutableListOf()
        return if (cursor != null) {
            while (cursor.moveToNext()) {
                data.add(MoviesDetail.fromCursor(cursor))
            }
            MoviesList(1, data.size, 1, data)
        } else null
    }

    fun getPopular(): MoviesList? {
        val cursor = context.contentResolver.query(
            MoviesPopular.CONTENT_URI,
            MoviesEntry.getColumnsWithTable(),
            null,
            null,
            null
        )
        val data: MutableList<MoviesDetail> = mutableListOf()
        return if (cursor != null) {
            while (cursor.moveToNext()) {
                data.add(MoviesDetail.fromCursor(cursor))
            }
            MoviesList(0, data.size, 1, data)
        } else null
    }

    fun deletePopular() {
        context.contentResolver.delete(MoviesPopular.CONTENT_URI, null, null)
    }

    fun savePopular(movies: List<MoviesDetail>) {
        if (movies.isNotEmpty()) {
            val contentValues = arrayOfNulls<ContentValues>(movies.size)
            for (i in movies.indices) {
                val contentValue = ContentValues()
                contentValue.put(LocalDatabase.COLUMN_MOVIE_ID_KEY, movies[i].id)
                contentValues[i] = contentValue
            }
            context.contentResolver.bulkInsert(MoviesPopular.CONTENT_URI, contentValues)
        }
    }

    fun getTopRated(): MoviesList? {
        val cursor = context.contentResolver.query(
            MoviesTopRated.CONTENT_URI,
            MoviesEntry.getColumnsWithTable(),
            null,
            null,
            null
        )
        val data: MutableList<MoviesDetail> = mutableListOf()
        return if (cursor != null) {
            while (cursor.moveToNext()) {
                data.add(MoviesDetail.fromCursor(cursor))
            }
            MoviesList(0, data.size, 1, data)
        } else null
    }

    fun deleteTopRated() {
        context.contentResolver.delete(MoviesTopRated.CONTENT_URI, null, null)
    }

    fun saveTopRated(movies: List<MoviesDetail>) {
        if (movies.isNotEmpty()) {
            val contentValues = arrayOfNulls<ContentValues>(movies.size)
            for (i in movies.indices) {
                val contentValue = ContentValues()
                contentValue.put(LocalDatabase.COLUMN_MOVIE_ID_KEY, movies[i].id)
                contentValues[i] = contentValue
            }
            context.contentResolver.bulkInsert(MoviesTopRated.CONTENT_URI, contentValues)
        }
    }

    fun saveMovies(movies: List<MoviesDetail>) {
        if (movies.isNotEmpty()) {
            val contentValues = arrayOfNulls<ContentValues>(movies.size)
            for (i in movies.indices) {
                contentValues[i] = movies[i].toContentValues()
            }
            context.contentResolver.bulkInsert(MoviesEntry.CONTENT_URI, contentValues)
        }
    }

    fun saveMovies(movie: MoviesDetail) {
        context.contentResolver.insert(MoviesEntry.CONTENT_URI, movie.toContentValues())
    }

    fun addToFavorites(movieId: Int) {
        val contentValues = ContentValues()
        contentValues.put(LocalDatabase.COLUMN_MOVIE_ID_KEY, movieId)
        context.contentResolver.insert(MoviesFavorites.CONTENT_URI, contentValues)
    }

    fun removeFromFavorites(movieId: Int) {
        context.contentResolver.delete(
            MoviesFavorites.CONTENT_URI,
            LocalDatabase.COLUMN_MOVIE_ID_KEY + " = " + movieId,
            null
        )
    }

    fun isFavorite(movieId: Int): Boolean {
        var favorite = false
        val cursor = context.contentResolver.query(
            MoviesFavorites.CONTENT_URI,
            null,
            LocalDatabase.COLUMN_MOVIE_ID_KEY + " = " + movieId,
            null,
            null
        )
        if (cursor != null) {
            favorite = cursor.count != 0
            cursor.close()
        }
        return favorite
    }
}