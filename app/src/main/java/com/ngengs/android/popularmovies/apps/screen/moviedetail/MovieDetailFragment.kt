package com.ngengs.android.popularmovies.apps.screen.moviedetail

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.format.DateFormat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import com.ngengs.android.popularmovies.apps.MoviesViewModelFactory
import com.ngengs.android.popularmovies.apps.R
import com.ngengs.android.popularmovies.apps.data.remote.MoviesDetail
import com.ngengs.android.popularmovies.apps.data.remote.ReviewDetail
import com.ngengs.android.popularmovies.apps.data.remote.VideosDetail
import com.ngengs.android.popularmovies.apps.data.remote.getBackdropPath
import com.ngengs.android.popularmovies.apps.data.remote.getPosterPath
import com.ngengs.android.popularmovies.apps.data.remote.isYoutubeVideo
import com.ngengs.android.popularmovies.apps.data.remote.youtubeVideo
import com.ngengs.android.popularmovies.apps.databinding.FragmentDetailMovieBinding
import com.ngengs.android.popularmovies.apps.globals.Values
import com.ngengs.android.popularmovies.apps.screen.moviedetail.MovieDetailFragment.OnFragmentInteractionListener
import com.ngengs.android.popularmovies.apps.utils.ResourceHelpers.getColor
import com.ngengs.android.popularmovies.apps.utils.ResourceHelpers.getDrawable
import com.ngengs.android.popularmovies.apps.utils.networks.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.NumberFormat

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [MovieDetailFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MovieDetailFragment : Fragment() {

    private var _binding: FragmentDetailMovieBinding? = null
    private val binding: FragmentDetailMovieBinding get() = _binding!!
    private var snackbar: Snackbar? = null

    private var mListener: OnFragmentInteractionListener? = null
    private lateinit var videoListAdapter: VideoListAdapter
    private lateinit var reviewListAdapter: ReviewListAdapter

    private val viewModel by viewModels<MovieDetailViewModel> { MoviesViewModelFactory }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentDetailMovieBinding.inflate(inflater, container, false)
        createLayout()
        arguments?.let {  arg ->
            val movieDetail = arg.getParcelable(ARG_DATA) as? MoviesDetail
            movieDetail?.let { viewModel.setTempMovieDetail(it) }
        }
        viewModel.fetchMovieData()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeData()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mListener = if (context is OnFragmentInteractionListener) {
            context
        } else {
            throw RuntimeException("$context must implement OnFragmentInteractionListener")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onDetach() {
        super.onDetach()
        mListener = null
    }

    private fun observeData() {
        viewModel.movieDetail.observe(viewLifecycleOwner) {
            when(it) {
                is Resource.Failure -> onFailure(it.throwable)
                is Resource.Loading -> {
                    if (it.state) {
                        binding.rootProgressBar.visibility = View.VISIBLE
                        binding.taglineView.visibility = View.GONE
                        binding.detailView.visibility = View.GONE
                    }
                    bindOldData(it.tempData)
                }
                is Resource.Success -> onResponse(it.data)
            }
        }
        viewModel.favoriteMovie.observe(viewLifecycleOwner) {
            mListener?.onFragmentChangeFavorite(it.first, it.second, it.third)
        }
        viewModel.movieVideo.observe(viewLifecycleOwner) {
            if (it is Resource.Success) bindVideo(it.data)
        }
        viewModel.movieReview.observe(viewLifecycleOwner) {
            if (it is Resource.Success) bindReview(it.data)
        }
    }

    private fun getRatingColor(score: Double): Int = if (score >= Values.RATING_SCORE_PERFECT)
        getColor(requireContext(), R.color.colorRatingPerfect)
    else if (score < Values.RATING_SCORE_PERFECT && score >= Values.RATING_SCORE_GOOD)
        getColor(requireContext(), R.color.colorRatingGood)
    else if (score < Values.RATING_SCORE_GOOD && score >= Values.RATING_SCORE_NORMAL)
        getColor(requireContext(), R.color.colorRatingNormal)
    else getColor(requireContext(), R.color.colorRatingBad)

    private fun bindOldData(data: MoviesDetail?) {
        data?.let { movie ->
            val backdropPath = movie.getBackdropPath(Values.TYPE_DEFAULT_IMAGE_THUMB).orEmpty()
            if (backdropPath.isNotEmpty()) {
                mListener?.onFragmentChangeHeaderImage(backdropPath, movie.getBackdropPath(1))
            }
            if (movie.getPosterPath(3) != null) {
                Glide.with(this)
                    .load(movie.getPosterPath(3))
                    .placeholder(getDrawable(requireContext(), R.drawable.ic_collections_white))
                    .into(binding.imageDetailThumb)
            }
            bindUpdatedData(data)
        }
    }

    private fun bindUpdatedData(data: MoviesDetail?) {
        data?.let { movie ->
            if (movie.title != null) mListener?.onFragmentChangeTitle(movie.title)

            binding.textRating.text = resources.getString(R.string.rating_number, movie.voteAverage)
            binding.textRating.setTextColor(getRatingColor(movie.voteAverage))
            binding.textMovieOriginalTitle.text = movie.originalTitle.orEmpty()
            movie.releaseDate?.let { releaseDate ->
                val dateFormat = DateFormat.getLongDateFormat(context)
                val stringDate = dateFormat.format(releaseDate)
                binding.textMovieReleaseDate.text =
                    resources.getString(R.string.release_date, stringDate)
            }
            binding.textMovieSynopsis.text = movie.overview.orEmpty()
        }
    }

    private fun bindData(data: MoviesDetail?) {
        if (binding.detailView.visibility == View.GONE) binding.detailView.visibility = View.VISIBLE
        if (binding.rootProgressBar.visibility == View.VISIBLE) binding.rootProgressBar.visibility =
            View.GONE
        data?.let { movie ->
            if (movie.genres.isNotEmpty()) {
                binding.textMovieGenre.text = movie.genres.joinToString(", ") { it.name }
            }
            if (movie.budget >= 0) {
                var budgetCurrencyString =
                    NumberFormat.getCurrencyInstance().format(movie.revenue.toDouble())
                budgetCurrencyString = budgetCurrencyString.replace("\\.00".toRegex(), "")
                binding.textMovieBudget.text = budgetCurrencyString
            }
            if (movie.revenue >= 0) {
                var revenueCurrencyString =
                    NumberFormat.getCurrencyInstance().format(movie.revenue.toDouble())
                revenueCurrencyString = revenueCurrencyString.replace("\\.00".toRegex(), "")
                binding.textMovieRevenue.text = revenueCurrencyString
            }
            if (movie.productionCompanies.isNotEmpty()) {
                binding.textMovieCompany.text =
                    movie.productionCompanies.joinToString(", ") { it.name }
            }
            if (movie.productionCountries.isNotEmpty()) {
                binding.textMovieCountry.text =
                    movie.productionCountries.joinToString(", ") { it.name }
            }
            if (movie.spokenLanguages.isNotEmpty()) {
                binding.textMovieLanguage.text =
                    movie.spokenLanguages.joinToString(", ") { it.name }
            }
            if (movie.status?.isNotEmpty() == true) binding.textMovieStatus.text = movie.status
            if (movie.tagline?.isNotEmpty() == true) {
                binding.textMovieTagline.text = movie.tagline
                binding.textMovieTagline.visibility = View.VISIBLE
                binding.taglineView.visibility = View.VISIBLE
            }
            bindUpdatedData(data)
        }
    }

    private fun createLayout() {
        binding.detailView.visibility = View.GONE
        binding.taglineView.visibility = View.GONE
        binding.videoLayout.recyclerVideo.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        binding.videoLayout.recyclerVideo.setHasFixedSize(true)
        videoListAdapter =
            VideoListAdapter(requireContext(), object : VideoListAdapter.ClickListener {
                override fun onClickListener(position: Int) {
                    Log.d(TAG, "onClickListener: $position")
                    val video = videoListAdapter.get(position)
                    if (video?.isYoutubeVideo == true) {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(video.youtubeVideo))
                        startActivity(intent)
                    }
                }

            })
        binding.videoLayout.recyclerVideo.adapter = videoListAdapter
        binding.reviewLayout.recyclerReview.layoutManager = LinearLayoutManager(context)
        binding.reviewLayout.recyclerReview.setHasFixedSize(true)
        reviewListAdapter =
            ReviewListAdapter(requireContext(), object : ReviewListAdapter.ClickListener {
                override fun onClickListener(position: Int) {
                    Log.d(TAG, "onClickListener: $position")
                    val review = reviewListAdapter.get(position)
                    if (review?.url?.isNotEmpty() == true) {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(review.url))
                        startActivity(intent)
                    }
                }
            })
        binding.reviewLayout.recyclerReview.adapter = reviewListAdapter
    }

    private fun onResponse(
        response: MoviesDetail
    ) = lifecycleScope.launch(Dispatchers.Main) {
        Log.d(TAG, "onResponse: $response")
        if (binding.rootProgressBar.visibility == View.VISIBLE) binding.rootProgressBar.visibility =
            View.GONE
        if (binding.detailView.visibility == View.GONE) binding.detailView.visibility = View.VISIBLE
        snackbar?.dismiss()
        snackbar = null
        Log.d(TAG, "onResponse: " + response.homepage)
        bindData(response)
    }

    private fun onFailure(t: Throwable) = lifecycleScope.launch(Dispatchers.Main) {
        if (binding.rootProgressBar.visibility == View.VISIBLE) binding.rootProgressBar.visibility =
            View.GONE
        if (binding.detailView.visibility == View.VISIBLE) binding.detailView.visibility = View.GONE
        if (binding.taglineView.visibility == View.VISIBLE) binding.taglineView.visibility =
            View.GONE
        if (snackbar != null) snackbar!!.dismiss()
        snackbar = Snackbar.make(
            binding.rootProgressBar,
            R.string.error_cant_get_data_check_connection,
            Snackbar.LENGTH_INDEFINITE
        )
        snackbar?.setAction(R.string.retry) { viewModel.fetchMovieData() }
        snackbar?.show()
        Log.e(TAG, "onFailure: ", t)
    }

    private fun bindVideo(video: List<VideosDetail>) {
        videoListAdapter.clear()
        if (video.isNotEmpty()) {
            mListener?.onFragmentShowShare()
            binding.videoLayout.cardVideo.visibility = View.VISIBLE
            videoListAdapter.add(video)
        }
    }

    private fun bindReview(review: List<ReviewDetail>) {
        if (review.isNotEmpty()) binding.reviewLayout.cardReview.visibility = View.VISIBLE
        reviewListAdapter.clear()
        reviewListAdapter.add(review)
    }

    fun getMoviesId(): Int = viewModel.currentMovieId()

    fun changeFavorite() = lifecycleScope.launch(Dispatchers.IO) {
        Log.d(TAG, "changeFavorite")
        viewModel.changeFavorite()
    }

    fun getStatusLoadedFromServer(): Boolean {
        return viewModel.currentMovieId() != -1
    }

    fun getShareContent(): String {
        val shareUrl =
            if (videoListAdapter.itemCount > 0) videoListAdapter.get(0)?.youtubeVideo.orEmpty()
            else ""
        val title = viewModel.currentMovieTitle()
        return resources.getString(R.string.share_content, title, shareUrl)
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
        fun onFragmentShowShare()
        fun onFragmentChangeFavorite(data: MoviesDetail?, isFavorite: Boolean, isRefresh: Boolean)
        fun onFragmentChangeTitle(title: String)
        fun onFragmentChangeHeaderImage(imageUri: String?, thumbnailUri: String?)
    }

    companion object {
        private const val TAG = "DetailMovieFragment"
        private const val ARG_DATA = "DATA"

        fun newInstance(moviesDetail: MoviesDetail) = MovieDetailFragment().apply {
            val args = Bundle().apply { putParcelable(ARG_DATA, moviesDetail) }
            arguments = args
        }
    }
}