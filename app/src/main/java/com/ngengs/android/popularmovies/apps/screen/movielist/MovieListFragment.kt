package com.ngengs.android.popularmovies.apps.screen.movielist

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.ngengs.android.popularmovies.apps.MoviesViewModelFactory
import com.ngengs.android.popularmovies.apps.R
import com.ngengs.android.popularmovies.apps.data.remote.MoviesDetail
import com.ngengs.android.popularmovies.apps.data.remote.MoviesList
import com.ngengs.android.popularmovies.apps.databinding.FragmentGridBinding
import com.ngengs.android.popularmovies.apps.globals.Values
import com.ngengs.android.popularmovies.apps.screen.movielist.MovieListFragment.OnFragmentInteractionListener
import com.ngengs.android.popularmovies.apps.utils.GridSpacesItemDecoration
import com.ngengs.android.popularmovies.apps.utils.ResourceHelpers.getDrawable
import com.ngengs.android.popularmovies.apps.utils.networks.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [MovieListFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MovieListFragment : Fragment() {
    private var _binding: FragmentGridBinding? = null
    private val binding: FragmentGridBinding get() = _binding!!
    private lateinit var layoutManager: GridLayoutManager
    private lateinit var layoutDecoration: GridSpacesItemDecoration
    private var snackbar: Snackbar? = null
    private lateinit var adapter: MovieListAdapter
    private var mListener: OnFragmentInteractionListener? = null

    private val viewModel: MovieListViewModel by activityViewModels { MoviesViewModelFactory }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentGridBinding.inflate(inflater, container, false)
        val view: View = binding.root
        createLayout()
        viewModel.refreshData()
        mListener?.onAttachHandler()
        initializeViewAction()
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeData()
    }

    private fun observeData() {
        viewModel.movies.observe(viewLifecycleOwner) { movies ->
            when (movies) {
                is Resource.Loading -> {
                    onLoading((movies.tempData?.size ?: 1) >= 1, movies.tempData)
                }
                is Resource.Failure -> {
                    onFailure(movies.throwable, movies.oldData?.size ?: 0)
                    onComplete()
                }
                is Resource.Success -> {
                    onResponse(movies.data)
                    onComplete()
                }
            }
        }
        viewModel.sortType.observe(viewLifecycleOwner) {
            doChangeTitle(it)
        }
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
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun doMoviePressed(position: Int) {
        Log.d(TAG, "doMoviePressed: $position")
        mListener?.let { listener ->
            onComplete()
            val dataPressed = adapter.get(position)
                ?: throw IllegalAccessException("Can't press unsupported movie")
            listener.onFragmentClickMovies(position, dataPressed)

        } ?: throw UnsupportedOperationException()
    }

    private fun doChangeTitle(sortType: Int) {
        Log.d(TAG, "doChangeTitle: $sortType")
        mListener?.onFragmentChangeTitle(sortType) ?: throw UnsupportedOperationException()
    }

    private fun createLayout() {
        Log.d(TAG, "createLayout: now")

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
                if (endHasBeenReached) viewModel.fetchNext()
            }
        })
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.refreshData()
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

    private fun onResponse(moviesList: MutableList<MoviesList>) = lifecycleScope.launch(Dispatchers.Main) {
        if (binding.recyclerView.visibility == View.GONE) {
            binding.recyclerView.visibility = View.VISIBLE
        }
        if (moviesList.size == 1) adapter.clear()
        snackbar?.dismiss()
        snackbar = null
        if (binding.tools.visibility == View.VISIBLE) binding.tools.visibility = View.GONE

        val movies = if (adapter.itemCount == 0 && moviesList.size > 1) {
            moviesList.flatMap { it.movies }
        } else moviesList.last().movies
        if (movies.isNotEmpty()) {
            adapter.add(movies)
        }
        if (moviesList.isEmpty()) {
            binding.tools.visibility = View.VISIBLE
            binding.imageTools.setImageDrawable(
                getDrawable(requireContext(), R.drawable.ic_movie_white)
            )
            binding.textTools.setText(R.string.data_empty)
        }
    }

    private fun onLoading(isNextPage: Boolean, tempMovies: MutableList<MoviesList>?) {
        if (isNextPage) binding.progressBar.visibility = View.VISIBLE
        else if (tempMovies?.isNotEmpty() == true) {
            binding.recyclerView.visibility = View.VISIBLE
            adapter.add(tempMovies.last().movies)
        }
        binding.tools.visibility = View.GONE
    }

    private fun onComplete() = lifecycleScope.launch(Dispatchers.Main) {
        if (binding.progressBar.visibility == View.VISIBLE) {
            binding.progressBar.visibility = View.GONE
        }
        if (binding.swipeRefresh.isRefreshing) binding.swipeRefresh.isRefreshing = false
        Log.d(TAG, "onComplete: finish complete")
    }

    private fun onFailure(t: Throwable, oldDataSize: Int) = lifecycleScope.launch(Dispatchers.Main) {
        Log.e(TAG, "onFailure: ", t)
        if (binding.progressBar.visibility == View.VISIBLE) binding.progressBar.visibility =
            View.GONE
        if (binding.recyclerView.visibility == View.VISIBLE && adapter.itemCount == 0) binding.recyclerView.visibility =
            View.GONE
        if (oldDataSize >= 1) {
            binding.imageTools.setImageDrawable(
                getDrawable(
                    requireContext(),
                    R.drawable.ic_refresh_white
                )
            )
            binding.textTools.setText(R.string.error_next_page)
            binding.tools.visibility = View.VISIBLE
        } else if (adapter.itemCount == 0 || oldDataSize == 0) {
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
        snackbar?.setAction(R.string.retry) {
            viewModel.fetchBySortType()
        }
        snackbar?.show()
    }

    private fun imageToolsClick() {
        viewModel.fetchBySortType()
    }

    fun changeType(sortType: Int) {
        Log.d(TAG, "changeType")
        viewModel.changeSortType(sortType)
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
        fun newInstance(sort: Int): MovieListFragment = MovieListFragment().apply {
            val args = Bundle().apply { putInt(ARGS_SORT_TYPE, sort) }
            arguments = args
        }
    }
}