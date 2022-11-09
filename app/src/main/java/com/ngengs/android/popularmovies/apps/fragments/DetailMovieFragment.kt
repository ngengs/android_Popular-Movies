package com.ngengs.android.popularmovies.apps.fragments

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.text.format.DateFormat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.ngengs.android.popularmovies.apps.R
import com.ngengs.android.popularmovies.apps.adapters.ReviewListAdapter
import com.ngengs.android.popularmovies.apps.adapters.VideoListAdapter
import com.ngengs.android.popularmovies.apps.data.MoviesDetail
import com.ngengs.android.popularmovies.apps.data.ReviewDetail
import com.ngengs.android.popularmovies.apps.data.ReviewList
import com.ngengs.android.popularmovies.apps.data.VideosDetail
import com.ngengs.android.popularmovies.apps.data.VideosList
import com.ngengs.android.popularmovies.apps.data.local.MoviesProviderHelper
import com.ngengs.android.popularmovies.apps.databinding.FragmentDetailMovieBinding
import com.ngengs.android.popularmovies.apps.fragments.DetailMovieFragment.OnFragmentInteractionListener
import com.ngengs.android.popularmovies.apps.globals.Values
import com.ngengs.android.popularmovies.apps.utils.MoviesAPI
import com.ngengs.android.popularmovies.apps.utils.NetworkHelpers.provideAPI
import com.ngengs.android.popularmovies.apps.utils.ResourceHelpers.getColor
import com.ngengs.android.popularmovies.apps.utils.ResourceHelpers.getDrawable
import com.squareup.picasso.Picasso
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.text.NumberFormat

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [DetailMovieFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class DetailMovieFragment : Fragment() {

    private var _binding: FragmentDetailMovieBinding? = null
    private val binding: FragmentDetailMovieBinding get() = _binding!!
    private var snackbar: Snackbar? = null

    private var data: MoviesDetail? = null
    private lateinit var moviesAPI: MoviesAPI
    private val disposable = CompositeDisposable()
    private var loadFromServer = false
    private var loadVideoFromServer = false
    private var loadReviewFromServer = false
    private var favoriteMovies = false

    private var mListener: OnFragmentInteractionListener? = null
    private lateinit var videoListAdapter: VideoListAdapter
    private lateinit var reviewListAdapter: ReviewListAdapter

    private lateinit var moviesProviderHelper: MoviesProviderHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let { data = it.getParcelable(ARG_DATA) }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentDetailMovieBinding.inflate(inflater, container, false)
        val view: View = binding.root
        moviesProviderHelper = MoviesProviderHelper(requireContext())
        if (data != null) {
            Log.d(TAG, "onCreateView: data status: not null")
            createLayout(savedInstanceState)
        } else {
            Log.d(TAG, "onCreateView: data status: null")
            binding.detailView.visibility = View.GONE
            binding.taglineView.visibility = View.GONE
            loadFromServer = false
            loadVideoFromServer = false
            loadReviewFromServer = false
            favoriteMovies = false
        }
        return view
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
        if (!disposable.isDisposed) disposable.dispose()
        _binding = null
    }

    override fun onDetach() {
        super.onDetach()
        mListener = null
        if (!disposable.isDisposed) disposable.dispose()
    }

    private fun getRatingColor(score: Double): Int = if (score >= Values.RATING_SCORE_PERFECT)
        getColor(requireContext(), R.color.colorRatingPerfect)
    else if (score < Values.RATING_SCORE_PERFECT && score >= Values.RATING_SCORE_GOOD)
        getColor(requireContext(), R.color.colorRatingGood)
    else if (score < Values.RATING_SCORE_GOOD && score >= Values.RATING_SCORE_NORMAL)
        getColor(requireContext(), R.color.colorRatingNormal)
    else getColor(requireContext(), R.color.colorRatingBad)

    private fun bindOldData() {
        data?.let { movie ->
            val backdropPath = movie.getBackdropPath(Values.TYPE_DEFAULT_IMAGE_THUMB).orEmpty()
            if (backdropPath.isNotEmpty()) {
                mListener?.onFragmentChangeHeaderImage(backdropPath)
            }
            if (movie.getPosterPath(3) != null) {
                Picasso.get()
                    .load(movie.getPosterPath(3))
                    .placeholder(getDrawable(requireContext(), R.drawable.ic_collections_white))
                    .resize(
                        resources.getDimensionPixelSize(R.dimen.image_description_thumbnail_width),
                        0
                    )
                    .into(binding.imageDetailThumb)
            }
            bindUpdatedData()
        }
    }

    private fun bindUpdatedData() {
        data?.let { movie ->
            if (movie.title != null) mListener?.onFragmentChangeTitle(movie.title)

            binding.textRating.text = resources.getString(R.string.rating_number, movie.voteAverage)
            binding.textRating.setTextColor(getRatingColor(movie.voteAverage))
            binding.textMovieOriginalTitle.text = movie.originalTitle.orEmpty()
            movie.releaseDate.let { releaseDate ->
                val dateFormat = DateFormat.getLongDateFormat(context)
                val stringDate = dateFormat.format(releaseDate)
                binding.textMovieReleaseDate.text =
                    resources.getString(R.string.release_date, stringDate)
            }
            binding.textMovieSynopsis.text = movie.overview.orEmpty()
        }
    }

    private fun bindData() {
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
            }
            bindUpdatedData()
        }
    }

    private fun createLayout(savedInstanceState: Bundle?) {
        binding.detailView.visibility = View.GONE
        binding.taglineView.visibility = View.GONE
        loadFromServer = false
        loadVideoFromServer = false
        loadReviewFromServer = false
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
        moviesAPI = provideAPI()
        Log.d(TAG, "createLayout: savedInstanceState: " + (savedInstanceState == null))
        if (savedInstanceState != null) {
            data = savedInstanceState.getParcelable("DATA")
            loadFromServer = savedInstanceState.getBoolean("ALREADY_CONNECT", false)
            loadVideoFromServer = savedInstanceState.getBoolean("ALREADY_VIDEO_CONNECT", false)
            loadReviewFromServer = savedInstanceState.getBoolean("ALREADY_REVIEW_CONNECT", false)
            favoriteMovies = savedInstanceState.getBoolean("FAVORITED_MOVIES", false)
            val tempVideo: List<VideosDetail> =
                savedInstanceState.getParcelableArrayList("DATA_VIDEO") ?: emptyList()
            val tempReview: List<ReviewDetail> =
                savedInstanceState.getParcelableArrayList("DATA_REVIEW") ?: emptyList()
            Log.d(TAG, "createLayout: loadFromServer: $loadFromServer")
            bindOldData()
            if (loadFromServer && loadVideoFromServer && loadReviewFromServer) {
                bindData()
                bindVideo(tempVideo)
                bindReview(tempReview)
            } else getDetailMovie()
        } else {
            data?.let {
                favoriteMovies = moviesProviderHelper.isFavorite(it.id)
                bindOldData()
                getDetailMovie()
            }
        }
        mListener?.onFragmentChangeFavorite(data, favoriteMovies, false)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable("DATA", data)
        outState.putParcelableArrayList("DATA_VIDEO", ArrayList<Parcelable>(videoListAdapter.get()))
        outState.putParcelableArrayList(
            "DATA_REVIEW",
            ArrayList<Parcelable>(reviewListAdapter.get())
        )
        outState.putBoolean("ALREADY_CONNECT", loadFromServer)
        outState.putBoolean("ALREADY_VIDEO_CONNECT", loadVideoFromServer)
        outState.putBoolean("ALREADY_REVIEW_CONNECT", loadReviewFromServer)
        outState.putBoolean("FAVORITED_MOVIES", favoriteMovies)
    }

    private fun getDetailMovie() {
        data?.let { movie ->
            binding.rootProgressBar.visibility = View.VISIBLE
            binding.taglineView.visibility = View.GONE
            binding.detailView.visibility = View.GONE
            disposable.addAll(
                moviesAPI.detail(movie.id)
                    .subscribeOn(Schedulers.io())
                    .doOnNext { moviesDetail: MoviesDetail ->
                        Log.d(TAG, "accept: doOnNext: " + moviesDetail.id)
                        moviesProviderHelper.saveMovies(moviesDetail)
                    }
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                        { response: MoviesDetail -> this.onResponse(response) },
                        { t: Throwable -> onFailure(t) }
                    ),
                moviesAPI.videos(movie.id)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                        { response: VideosList -> this.onResponseVideo(response) },
                        { t: Throwable -> Log.e(TAG, "accept: Error Get Videos", t) }
                    ),
                moviesAPI.reviews(movie.id)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                        { response: ReviewList -> this.onResponseReview(response) },
                        { t: Throwable? -> Log.e(TAG, "accept: Error Get Reviews", t) }
                    )
            )
        }
    }

    private fun onResponse(response: MoviesDetail) {
        Log.d(TAG, "onResponse: $response")
        if (binding.rootProgressBar.visibility == View.VISIBLE) binding.rootProgressBar.visibility =
            View.GONE
        if (binding.detailView.visibility == View.GONE) binding.detailView.visibility = View.VISIBLE
        snackbar?.dismiss()
        snackbar = null
        data = response
        Log.d(TAG, "onResponse: " + data!!.homepage)
        loadFromServer = true
        bindData()
    }

    fun onFailure(t: Throwable) {
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
        snackbar?.setAction(R.string.retry) {
            bindOldData()
            getDetailMovie()
        }
        snackbar?.show()
        Log.e(TAG, "onFailure: ", t)
    }

    private fun onResponseVideo(response: VideosList) {
        Log.d(TAG, "onResponseVideo: $response")
        response.videos?.let { bindVideo(it) }
    }

    private fun onResponseReview(response: ReviewList) {
        Log.d(TAG, "onResponseReview: " + response.review?.size)
        response.review?.let { bindReview(it) }
    }

    private fun bindVideo(video: List<VideosDetail>) {
        loadVideoFromServer = true
        videoListAdapter.clear()
        if (video.isNotEmpty()) {
            mListener?.onFragmentShowShare()
            binding.videoLayout.cardVideo.visibility = View.VISIBLE
            videoListAdapter.add(video)
        }
    }

    private fun bindReview(review: List<ReviewDetail>) {
        loadReviewFromServer = true
        if (review.isNotEmpty()) binding.reviewLayout.cardReview.visibility = View.VISIBLE
        reviewListAdapter.clear()
        reviewListAdapter.add(review)
    }

    fun setData(data: MoviesDetail?) {
        this.data = data
        createLayout(null)
    }

    fun getMoviesId(): Int = data?.id ?: -1

    fun changeFavorite() {
        data?.let { movie ->
            if (favoriteMovies) moviesProviderHelper.removeFromFavorites(movie.id)
            else moviesProviderHelper.addToFavorites(movie.id)
            favoriteMovies = !favoriteMovies
            mListener?.onFragmentChangeFavorite(movie, favoriteMovies, true)
        }
    }

    fun getStatusLoadedFromServer(): Boolean {
        return loadFromServer
    }

    fun getShareContent(): String? {
        var shareUrl: String? = ""
        if (videoListAdapter.itemCount > 0) shareUrl = videoListAdapter.get(0)!!.youtubeVideo
        return resources.getString(R.string.share_content, data!!.title, shareUrl)
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
        fun onFragmentChangeHeaderImage(imageUri: String?)
    }

    companion object {
        private const val TAG = "DetailMovieFragment"
        private const val ARG_DATA = "DATA"

        fun newInstance(moviesDetail: MoviesDetail) = DetailMovieFragment().apply {
            val args = Bundle().apply { putParcelable(ARG_DATA, moviesDetail) }
            arguments = args
        }
    }
}