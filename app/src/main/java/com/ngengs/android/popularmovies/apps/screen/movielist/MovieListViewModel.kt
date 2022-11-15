package com.ngengs.android.popularmovies.apps.screen.movielist

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ngengs.android.popularmovies.apps.data.remote.MoviesDetail
import com.ngengs.android.popularmovies.apps.data.remote.MoviesList
import com.ngengs.android.popularmovies.apps.data.source.MoviesRepository
import com.ngengs.android.popularmovies.apps.globals.Values
import com.ngengs.android.popularmovies.apps.utils.networks.Resource
import com.ngengs.android.popularmovies.apps.utils.networks.getAnyData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

/**
 * Created by rizky.kharisma on 11/11/22.
 * @ngengs
 */
class MovieListViewModel(
    private val moviesRepository: MoviesRepository
) : ViewModel() {
    private val _pageTotal = MutableLiveData<Int>()
    private val pageTotal: LiveData<Int> = _pageTotal
    private val _pageNow = MutableLiveData<Int>()
    private val pageNow: LiveData<Int> = _pageNow
    private val _sortType = MutableLiveData<Int>()
    val sortType: LiveData<Int> = _sortType

    private var oldSortType: Int = sortType.value ?: Values.TYPE_POPULAR

    private val _movies = MutableLiveData<Resource<MutableList<MoviesList>>>()
    val movies: LiveData<Resource<MutableList<MoviesList>>> = _movies

    fun refreshData() {
        _pageNow.value = 0
        _pageTotal.value = 1
        fetchBySortType()
    }

    fun fetchBySortType() {
        viewModelScope.launch(Dispatchers.IO) {
            when (_sortType.value) {
                Values.TYPE_POPULAR -> fetchPopular()
                Values.TYPE_HIGH_RATED -> fetchTopRated()
                Values.TYPE_FAVORITE -> fetchFavorite()
            }
        }
    }

    fun fetchNext() {
        val now = pageNow.value ?: 0
        val total = pageTotal.value ?: 1
        if (movies.value !is Resource.Loading<*> && now < total) {
            fetchBySortType()
        }
    }

    fun changeSortType(sortType: Int, skipRefresh: Boolean = false) {
        _sortType.value = sortType
        if (!skipRefresh) refreshData()
    }

    fun addOrRemoveFavoriteMovie(movie: MoviesDetail, isAdd: Boolean) {
        if (isFavoriteType()) {
            val oldMoviesList = getOldMovieList()
            val firstSize = oldMoviesList.size
            val tempMovies = oldMoviesList.lastOrNull() ?: MoviesList()
            val oldList = oldMoviesList.flatMap { it.movies }.toMutableList()
            if (isAdd) {
                if (oldList.find { it.id == movie.id } == null) oldList.add(movie)
            } else {
                val index = oldList.indexOfFirst { it.id == movie.id }
                if (index >= 0) oldList.removeAt(index)
            }
            if (firstSize != oldList.size) {
                val result = MoviesList(
                    page = tempMovies.page,
                    totalResult = oldList.size,
                    totalPage = tempMovies.totalPage,
                    movies = oldList
                )
                _movies.value = Resource.Success(mutableListOf(result))
            }
        }
    }

    fun isPopularType() = sortType.value == Values.TYPE_POPULAR
    fun isTopRatedType() = sortType.value == Values.TYPE_HIGH_RATED
    fun isFavoriteType() = sortType.value == Values.TYPE_FAVORITE

    private suspend fun fetchPopular() {
        fetchNetworkData { moviesRepository.getPopularMovies(it) }
    }

    private suspend fun fetchTopRated() {
        fetchNetworkData { moviesRepository.getTopRatedMovies(it) }
    }

    private suspend fun fetchFavorite() {
        val now = pageNow.value ?: 0
        if (now == 0) viewModelScope.launch(Dispatchers.Main) {
            _movies.value = Resource.Loading(true)
        }
        when (val response = moviesRepository.getFavoriteMovies(now)) {
            is Resource.Failure -> onFailure(response)
            is Resource.Loading -> onLoading(response)
            is Resource.Success -> onSuccess(response)
        }
    }

    private fun onLoading(response: Resource.Loading<MoviesList>) =
        viewModelScope.launch(Dispatchers.Main) {
            val now = pageNow.value ?: 0
            if (now == 0 && response.tempData != null) {
                _movies.value = Resource.Loading(response.state, mutableListOf(response.tempData))
            } else {
                val targetMovies =
                    if (sortType.value == oldSortType) getOldMovieList().toMutableList() else null
                _movies.value = Resource.Loading(response.state, targetMovies)
            }
        }

    private fun onFailure(response: Resource.Failure<MoviesList>) =
        viewModelScope.launch(Dispatchers.Main) {
            val oldData = if (sortType.value == oldSortType) {
                getOldMovieList().toMutableList()
            } else mutableListOf()
            response.oldData?.let { oldData.add(it) }
            _movies.value = Resource.Failure(response.throwable, response.type, oldData)
        }

    private fun onSuccess(response: Resource.Success<MoviesList>) =
        viewModelScope.launch(Dispatchers.Main) {
            val now = pageNow.value ?: 0
            _pageTotal.value = response.data.totalPage
            _pageNow.value = response.data.page
            oldSortType = sortType.value ?: oldSortType
            if (now == 0) _movies.value = Resource.Success(mutableListOf(response.data))
            else {
                val tempData = getOldMovieList().toMutableList()
                val newData = response.data
                tempData.add(newData)
                _movies.value = Resource.Success(tempData)
            }
        }

    private suspend fun fetchNetworkData(moviesDataTarget: suspend (page: Int) -> Flow<Resource<MoviesList>>) {
        val now = pageNow.value ?: 0
        val total = pageTotal.value ?: 1
        if (now < total) {
            moviesDataTarget(now).collect {
                when (it) {
                    is Resource.Loading -> onLoading(it)
                    is Resource.Failure -> onFailure(it)
                    is Resource.Success -> onSuccess(it)
                }
            }
        }
    }

    private fun getOldMovieList(): List<MoviesList> = movies.value?.getAnyData().orEmpty()
}