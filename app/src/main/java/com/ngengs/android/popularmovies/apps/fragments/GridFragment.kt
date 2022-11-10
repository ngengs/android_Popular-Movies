package com.ngengs.android.popularmovies.apps.fragments

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.ngengs.android.popularmovies.apps.R
import com.ngengs.android.popularmovies.apps.adapters.MovieListAdapter
import com.ngengs.android.popularmovies.apps.data.local.MoviesDatabaseHelper
import com.ngengs.android.popularmovies.apps.data.remote.MoviesDetail
import com.ngengs.android.popularmovies.apps.data.remote.MoviesList
import com.ngengs.android.popularmovies.apps.databinding.FragmentGridBinding
import com.ngengs.android.popularmovies.apps.fragments.GridFragment.OnFragmentInteractionListener
import com.ngengs.android.popularmovies.apps.globals.Values
import com.ngengs.android.popularmovies.apps.utils.GridSpacesItemDecoration
import com.ngengs.android.popularmovies.apps.utils.ResourceHelpers.getDrawable
import com.ngengs.android.popularmovies.apps.utils.networks.MoviesAPI
import com.ngengs.android.popularmovies.apps.utils.networks.NetworkHelpers
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.Action
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers


/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [GridFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class GridFragment : Fragment() {
    private var _binding: FragmentGridBinding? = null
    private val binding: FragmentGridBinding get() = _binding!!
    private lateinit var layoutManager: GridLayoutManager
    private lateinit var layoutDecoration: GridSpacesItemDecoration
    private var snackbar: Snackbar? = null
    private lateinit var moviesAPI: MoviesAPI
    private lateinit var adapter: MovieListAdapter
    private var disposable: CompositeDisposable = CompositeDisposable()
    private var sortType = 0
    private var pageTotal = 1
    private var pageNow = 0
    private var forceRefresh = false
    private var loading = false
    private var fromPagination = false
    private var changeData = false
    private var processLoadData = true
    private val actionComplete = Action { this.onComplete() }
    private val moviesListConsumer =
        Consumer { moviesList: MoviesList -> this.onResponse(moviesList) }
    private lateinit var moviesDB: MoviesDatabaseHelper
    private val errorConsumer = Consumer { t: Throwable -> onFailure(t) }
    private var mListener: OnFragmentInteractionListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let { sortType = it.getInt(ARGS_SORT_TYPE, Values.TYPE_POPULAR) }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentGridBinding.inflate(inflater, container, false)
        val view: View = binding.root
        moviesDB = MoviesDatabaseHelper(requireContext())
        createLayout(savedInstanceState)
        mListener?.onAttachHandler()
        initializeViewAction()
        return view
    }

    private fun initializeViewAction() {
        binding.imageTools.setOnClickListener { imageToolsClick() }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) {
            mListener = context
        } else {
            throw RuntimeException("$context must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        mListener = null
        if (!disposable.isDisposed) disposable.dispose()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (!disposable.isDisposed) disposable.dispose()
        _binding = null
    }

    fun getSortType(): Int = sortType

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (adapter.itemCount > 0) {
            val data = adapter.get()
            outState.putParcelableArrayList("DATA", ArrayList(data))
            outState.putInt("PAGE_NOW", pageNow)
            outState.putInt("PAGE_TOTAL", pageTotal)
            outState.putInt("SORT_TYPE", sortType)
            outState.putBoolean("PROCESS_LOAD_DATA", processLoadData)
        }
    }

    private fun doMoviePressed(position: Int) {
        Log.d(TAG, "doMoviePressed: $position")
        mListener?.let { listener ->
            if (!disposable.isDisposed) disposable.dispose()
            onComplete()
            val dataPressed = adapter.get(position)
                ?: throw IllegalAccessException("Can't press unsupported movie")
            listener.onFragmentClickMovies(position, dataPressed)

        } ?: throw UnsupportedOperationException()
    }

    private fun doChangeTitle() {
        Log.d(TAG, "doChangeTitle: $sortType")
        mListener?.onFragmentChangeTitle(sortType) ?: throw UnsupportedOperationException()
    }

    private fun createLayout(savedInstanceState: Bundle?) {
        Log.d(TAG, "createLayout: now")
        loading = false
        fromPagination = false
        changeData = true

        // Make sure all view not visible
        binding.recyclerView.visibility = View.GONE
        binding.progressBar.visibility = View.GONE
        adapter = MovieListAdapter(
            requireContext(),
            object : MovieListAdapter.ClickListener {
                override fun onClickListener(position: Int) {
                    doMoviePressed(position)
                }
            }
        )
        val gridSpan: Int =
            if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) 4 else 2
        layoutManager = GridLayoutManager(context, gridSpan)
        layoutDecoration = GridSpacesItemDecoration(
            gridSpan,
            resources.getDimensionPixelSize(R.dimen.grid_spacing)
        )
        binding.recyclerView.layoutManager = layoutManager
        binding.recyclerView.addItemDecoration(layoutDecoration)
        binding.recyclerView.adapter = adapter
        binding.recyclerView.setHasFixedSize(true)
        binding.recyclerView.isNestedScrollingEnabled = false
        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount
                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
                val endHasBeenReached =
                    firstVisibleItemPosition + visibleItemCount + 5 >= totalItemCount
                if (!loading && pageNow < pageTotal && endHasBeenReached && !processLoadData) {
                    Log.d(TAG, "onScrolled: CatchData")
                    forceRefresh = false
                    fromPagination = true
                    if (sortType == Values.TYPE_POPULAR) getPopularMovies() else if (sortType == Values.TYPE_HIGH_RATED) getTopRatedMovies()
                }
            }
        })
        forceRefresh = false
        binding.swipeRefresh.setOnRefreshListener {
            forceRefresh = true
            pageNow = 0
            pageTotal = 1
            fromPagination = false
            if (sortType == Values.TYPE_POPULAR) getPopularMovies() else if (sortType == Values.TYPE_HIGH_RATED) getTopRatedMovies() else getFavoriteMovies()
        }
        moviesAPI = NetworkHelpers.provideAPI(requireActivity())
        if (savedInstanceState != null) {
            sortType = savedInstanceState.getInt("SORT_TYPE", Values.TYPE_POPULAR)
            pageNow = savedInstanceState.getInt("PAGE_NOW", 0)
            pageTotal = savedInstanceState.getInt("PAGE_TOTAL", 1)
            val temp: List<MoviesDetail>? = savedInstanceState.getParcelableArrayList("DATA")
            if (temp != null) {
                adapter.clear()
                adapter.add(temp)
                binding.recyclerView.visibility = View.VISIBLE
                binding.progressBar.visibility = View.GONE
                binding.tools.visibility = View.GONE
                doChangeTitle()
            }
            processLoadData = savedInstanceState.getBoolean("PROCESS_LOAD_DATA", false)
            Log.d(
                TAG,
                "createLayout: load savedInstanceState: isProcessLoadData value $processLoadData"
            )
            Log.d(
                TAG,
                "createLayout: load savedInstanceState: sortType value " + (sortType == Values.TYPE_POPULAR)
            )
            if (processLoadData) {
                if (pageNow == 0) adapter.clear()
                if (sortType == Values.TYPE_POPULAR) getPopularMovies() else if (sortType == Values.TYPE_HIGH_RATED) getTopRatedMovies() else getFavoriteMovies()
            }
        } else {
            when (sortType) {
                Values.TYPE_POPULAR -> {
                    bindOldData()
                    Log.d(TAG, "createLayout: catch new data")
                    getPopularMovies()
                }
                Values.TYPE_HIGH_RATED -> {
                    bindOldData()
                    Log.d(TAG, "createLayout: catch new data")
                    getPopularMovies()
                }
                else -> getFavoriteMovies()
            }
            doChangeTitle()
        }
    }

    fun updateSpanColumn(span: Int) {
        layoutManager.spanCount = span
        binding.recyclerView.layoutManager = layoutManager
        binding.recyclerView.removeItemDecoration(layoutDecoration)
        layoutDecoration =
            GridSpacesItemDecoration(span, resources.getDimensionPixelSize(R.dimen.grid_spacing))
        binding.recyclerView.addItemDecoration(layoutDecoration)
    }

    fun scrollToPosition(position: Int) {
        layoutManager.scrollToPosition(position)
    }

    private fun onResponse(moviesList: MoviesList) {
        if (binding.recyclerView.visibility == View.GONE) {
            binding.recyclerView.visibility = View.VISIBLE
        }
        if (forceRefresh) adapter.clear()
        snackbar?.dismiss()
        snackbar = null
        if (binding.tools.visibility == View.VISIBLE) binding.tools.visibility = View.GONE
        fromPagination = false
        pageTotal = moviesList.totalPage
        pageNow = moviesList.page
        val movies = moviesList.movies
        if (moviesList.statusMessage == null && movies.isNotEmpty()) {
            adapter.add(movies)
        }
        if (movies.isEmpty()) {
            binding.tools.visibility = View.VISIBLE
            binding.imageTools.setImageDrawable(
                getDrawable(
                    requireContext(),
                    R.drawable.ic_movie_white
                )
            )
            binding.textTools.setText(R.string.data_empty)
        }
        Log.d(TAG, "onResponse: pageNow: $pageNow pageTotal: $pageTotal")
        Log.d(TAG, "onResponse: finish Response")
    }

    private fun onComplete() {
        if (binding.progressBar.visibility == View.VISIBLE) binding.progressBar.visibility =
            View.GONE
        if (binding.swipeRefresh.isRefreshing) binding.swipeRefresh.isRefreshing = false
        if (forceRefresh) forceRefresh = false
        processLoadData = false
        changeData = false
        loading = false
        Log.d(TAG, "onComplete: finish complete")
    }

    private fun onFailure(t: Throwable) {
        Log.e(TAG, "onFailure: ", t)
        if (binding.progressBar.visibility == View.VISIBLE) binding.progressBar.visibility =
            View.GONE
        if (binding.recyclerView.visibility == View.VISIBLE && adapter.itemCount == 0) binding.recyclerView.visibility =
            View.GONE
        if (fromPagination) {
            binding.imageTools.setImageDrawable(
                getDrawable(
                    requireContext(),
                    R.drawable.ic_refresh_white
                )
            )
            binding.textTools.setText(R.string.error_next_page)
            binding.tools.visibility = View.VISIBLE
        } else if (adapter.itemCount == 0 || pageNow == 0) {
            adapter.clear()
            binding.imageTools.setImageDrawable(
                getDrawable(requireContext(), R.drawable.ic_cloud_off_white)
            )
            binding.textTools.setText(R.string.error_no_connection)
            binding.tools.visibility = View.VISIBLE
        }
        if (snackbar != null) snackbar?.dismiss()
        snackbar = Snackbar.make(
            binding.textTools,
            R.string.error_cant_get_data_check_connection,
            BaseTransientBottomBar.LENGTH_INDEFINITE
        )
        snackbar?.setAction(R.string.retry) { if (sortType == Values.TYPE_POPULAR) getPopularMovies() else if (sortType == Values.TYPE_HIGH_RATED) getTopRatedMovies() else getFavoriteMovies() }
        snackbar?.show()
    }

    private fun imageToolsClick() {
        if (sortType == Values.TYPE_POPULAR) getPopularMovies() else if (sortType == Values.TYPE_HIGH_RATED) getTopRatedMovies() else getFavoriteMovies()
    }

    private fun getPopularMovies() {
        Log.d(TAG, "getPopularMovies: pageNow: $pageNow pageTotal: $pageTotal")
        fetchServerData(
            moviesAPI.listMoviesPopular(pageNow + 1)
        ) { listOf(moviesDB.deletePopular(), moviesDB.savePopular(it.movies)) }
    }

    private fun getTopRatedMovies() {
        Log.d(TAG, "getTopRatedMovies: pageNow: $pageNow pageTotal: $pageTotal")
        fetchServerData(
            moviesAPI.listMoviesTopRated(pageNow + 1)
        ) { listOf(moviesDB.deleteTopRated(), moviesDB.saveTopRated(it.movies)) }
    }

    private fun fetchServerData(moviesApiTarget: Observable<MoviesList>, saveCategoryJob: (MoviesList) -> Collection<Completable>) {
        if (pageNow < pageTotal) {
            Log.d(TAG, "fetchServerData: now. page: $pageNow")
            loading = true
            processLoadData = true
            if (!forceRefresh || changeData) binding.progressBar.visibility = View.VISIBLE
            binding.tools.visibility = View.GONE
            Log.d(TAG, "fetchServerData: page: $pageNow")
            disposable.add(moviesApiTarget.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext {
                    Log.d(TAG, "fetchServerData: save data page: $pageNow")
                    val saveJob = mutableListOf(moviesDB.saveMovies(it.movies))
                    if (pageNow == 0) {
                        saveJob.addAll(saveCategoryJob(it))
                    }
                    disposable.add(Completable.merge(saveJob)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({
                            Log.d(TAG, "fetchServerData: success")
                        }) { throwable -> Log.d(TAG, "fetchServerData: error", throwable) }
                    )
                }
                .subscribe(moviesListConsumer, errorConsumer, actionComplete)
            )
        }
    }

    private fun getFavoriteMovies() {
        Log.d(TAG, "getFavoriteMovies: now")
        loading = true
        processLoadData = true
        if (changeData) binding.progressBar.visibility = View.VISIBLE
        binding.tools.visibility = View.GONE
        disposable.add(
            moviesDB.getFavorites()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doFinally(actionComplete)
                .subscribe(moviesListConsumer, errorConsumer)
        )
    }

    fun changeType(sortType: Int) {
        Log.d(TAG, "changeType")
        this.sortType = sortType
        forceRefresh = true
        pageNow = 0
        pageTotal = 1
        loading = false
        changeData = true
        doChangeTitle()
//        if (!disposable.isDisposed) disposable.dispose()
        when (this.sortType) {
            Values.TYPE_POPULAR -> {
                bindOldData()
                Log.d(TAG, "changeType: catch new data")
                getPopularMovies()
            }
            Values.TYPE_HIGH_RATED -> {
                bindOldData()
                Log.d(TAG, "changeType: catch new data")
                getTopRatedMovies()
            }
            else -> getFavoriteMovies()
        }
    }

    private fun bindOldData() {
        // Catch Offline data
        Log.d(TAG, "bindOldData: catch old data")
        val temp = when (sortType) {
            Values.TYPE_POPULAR -> moviesDB.getPopular()
            Values.TYPE_HIGH_RATED -> moviesDB.getTopRated()
            else -> null
        }
        temp?.let { tempJob ->
            disposable.add(tempJob.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    Log.d(TAG, "bindOldData: finishOld Data")
                    it?.let { onResponse(it) }
                }) { Log.d(TAG, "bindOldData: error", it) }
            )
        }
    }

    fun addMovies(item: MoviesDetail) {
        adapter.add(item)
    }

    fun removeMovies(item: MoviesDetail) {
        adapter.deleteById(item.id)
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     *
     *
     * See the Android Training lesson [Communicating with Other Fragments](http://developer.android.com/training/basics/fragments/communicating.html) for more information.
     */
    interface OnFragmentInteractionListener {
        fun onFragmentClickMovies(position: Int, data: MoviesDetail)
        fun onFragmentChangeTitle(sortType: Int)
        fun onAttachHandler()
    }

    companion object {
        private const val TAG = "GridFragment"
        private const val ARGS_SORT_TYPE = "ARGS_SORT_TYPE"

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param sort Type of sort ([Values.TYPE_POPULAR], [Values.TYPE_HIGH_RATED], [Values.TYPE_FAVORITE]])
         * @return A new instance of fragment GridFragment.
         */
        fun newInstance(sort: Int): GridFragment = GridFragment().apply {
            val args = Bundle().apply { putInt(ARGS_SORT_TYPE, sort) }
            arguments = args
        }
    }
}