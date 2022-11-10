package com.ngengs.android.popularmovies.apps.data.local

import com.ngengs.android.popularmovies.apps.data.remote.MoviesDetail
import com.ngengs.android.popularmovies.apps.data.remote.MoviesList

/**
 * Created by rizky.kharisma on 10/11/22.
 * @ngengs
 */

fun MoviesDetail.toMovies(): Movies {
    return Movies(
        id = id,
        originalTitle = originalTitle,
        overview = overview,
        releaseDate = releaseDate,
        posterPath = posterPath,
        popularity = popularity,
        title = title,
        voteAverage = voteAverage,
        voteCount = voteCount,
        backdropPath = backdropPath,
    )
}

fun Movies.toMoviesDetail(): MoviesDetail {
    return MoviesDetail(
        id = id,
        originalTitle = originalTitle,
        overview = overview,
        releaseDate = releaseDate,
        posterPath = posterPath,
        popularity = popularity,
        title = title,
        voteAverage = voteAverage,
        voteCount = voteCount,
        backdropPath = backdropPath
    )
}

fun List<MoviesDetail>.toMovieList(): MoviesList? {
    return if (isEmpty()) null
    else MoviesList(1, this.size, 1, this)
}