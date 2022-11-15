package com.ngengs.android.popularmovies.apps.screen.moviedetail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ngengs.android.popularmovies.apps.data.remote.MoviesDetail
import com.ngengs.android.popularmovies.apps.data.remote.ReviewDetail
import com.ngengs.android.popularmovies.apps.data.remote.VideosDetail
import com.ngengs.android.popularmovies.apps.data.source.MoviesRepository
import com.ngengs.android.popularmovies.apps.utils.networks.Resource
import com.ngengs.android.popularmovies.apps.utils.networks.getAnyData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch

/**
 * Created by rizky.kharisma on 15/11/22.
 * @ngengs
 */
class MovieDetailViewModel(
    private val moviesRepository: MoviesRepository
) : ViewModel() {
    private val _movieDetail = MutableLiveData<Resource<MoviesDetail>>()
    val movieDetail: LiveData<Resource<MoviesDetail>> = _movieDetail
    private val _movieVideo = MutableLiveData<Resource<List<VideosDetail>>>()
    val movieVideo: LiveData<Resource<List<VideosDetail>>> = _movieVideo
    private val _movieReview = MutableLiveData<Resource<List<ReviewDetail>>>()
    val movieReview: LiveData<Resource<List<ReviewDetail>>> = _movieReview
    private val _favoriteMovie = MutableLiveData<Triple<MoviesDetail, Boolean, Boolean>>()
    val favoriteMovie: LiveData<Triple<MoviesDetail, Boolean, Boolean>> = _favoriteMovie

    fun setTempMovieDetail(movie: MoviesDetail) {
        _movieDetail.value = Resource.Loading(false, movie)
    }

    fun fetchMovieData() {
        val movieDetail = movieDetail.value?.getAnyData()
        movieDetail?.let { movie ->
            _movieDetail.value = Resource.Loading(true, movie)
            _movieVideo.value = Resource.Loading(true, movieVideo.value?.getAnyData().orEmpty())
            _movieReview.value = Resource.Loading(true, movieReview.value?.getAnyData().orEmpty())
            viewModelScope.launch {
                listOf(
                    initializeFavorite(movie),
                    fetchMovieDetail(movie.id),
                    fetchMovieVideo(movie.id),
                    fetchMovieReview(movie.id),
                ).joinAll()
            }
        }
    }

    private fun fetchMovieDetail(movieId: Int) = viewModelScope.launch(Dispatchers.IO) {
        moviesRepository.getDetailMovies(movieId).collect {
            launch(Dispatchers.Main) {
                _movieDetail.value = it
            }
        }
    }

    private fun fetchMovieVideo(movieId: Int) = viewModelScope.launch(Dispatchers.IO) {
        val result = moviesRepository.getMoviesVideos(movieId)
        if (result is Resource.Success) {
            launch(Dispatchers.Main) {
                _movieVideo.value = Resource.Success(result.data.videos)
            }
        }
    }

    private fun fetchMovieReview(movieId: Int) = viewModelScope.launch(Dispatchers.IO) {
        val result = moviesRepository.getMoviesReviews(movieId)
        if (result is Resource.Success) {
            launch(Dispatchers.Main) {
                _movieReview.value = Resource.Success(result.data.review)
            }
        }
    }

    private fun initializeFavorite(movie: MoviesDetail? = null) = viewModelScope.launch(Dispatchers.IO) {
        val movieDetail = movie ?: movieDetail.value?.getAnyData()
        movieDetail?.let { movie ->
            val result = moviesRepository.isFavoriteMovie(movie.id)
            viewModelScope.launch(Dispatchers.Main) {
                _favoriteMovie.value = Triple(movie, result, false)
            }
        }
    }

    fun changeFavorite() {
        val movieDetail = movieDetail.value?.getAnyData()
        val isFavorite = favoriteMovie.value?.second ?: false
        movieDetail?.let { movie ->
            viewModelScope.launch {
                if (isFavorite) moviesRepository.removeFavoriteMovies(movie)
                else moviesRepository.saveFavoriteMovies(movie)
                _favoriteMovie.postValue(Triple(movie, !isFavorite, true))
            }
        }
    }

    fun currentMovieId(): Int = movieDetail.value?.getAnyData()?.id ?: -1
    fun currentMovieTitle(): String = movieDetail.value?.getAnyData()?.title.orEmpty()
}