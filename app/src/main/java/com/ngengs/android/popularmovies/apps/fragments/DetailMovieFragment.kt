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
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import com.ngengs.android.popularmovies.apps.R
import com.ngengs.android.popularmovies.apps.adapters.ReviewListAdapter
import com.ngengs.android.popularmovies.apps.adapters.VideoListAdapter
import com.ngengs.android.popularmovies.apps.data.local.MoviesDatabaseHelper
import com.ngengs.android.popularmovies.apps.data.remote.MoviesDetail
import com.ngengs.android.popularmovies.apps.data.remote.ReviewDetail
import com.ngengs.android.popularmovies.apps.data.remote.ReviewList
import com.ngengs.android.popularmovies.apps.data.remote.VideosDetail
import com.ngengs.android.popularmovies.apps.data.remote.VideosList
import com.ngengs.android.popularmovies.apps.data.remote.getBackdropPath
import com.ngengs.android.popularmovies.apps.data.remote.getPosterPath
import com.ngengs.android.popularmovies.apps.data.remote.isYoutubeVideo
import com.ngengs.android.popularmovies.apps.data.remote.youtubeVideo
import com.ngengs.android.popularmovies.apps.databinding.FragmentDetailMovieBinding
import com.ngengs.android.popularmovies.apps.fragments.DetailMovieFragment.OnFragmentInteractionListener
import com.ngengs.android.popularmovies.apps.globals.Values
import com.ngengs.android.popularmovies.apps.utils.ResourceHelpers.getColor
import com.ngengs.android.popularmovies.apps.utils.ResourceHelpers.getDrawable
import com.ngengs.android.popularmovies.apps.utils.networks.MoviesAPI
import com.ngengs.android.popularmovies.apps.utils.networks.NetworkHelpers
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

    private lateinit var moviesDB: MoviesDatabaseHelper

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
        moviesDB = MoviesDatabaseHelper(requireContext())
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
                mListener?.onFragmentChangeHeaderImage(backdropPath, movie.getBackdropPath(1))
            }
            if (movie.getPosterPath(3) != null) {
                Glide.with(this)
                    .load(movie.getPosterPath(3))
                    .placeholder(getDrawable(requireContext(), R.drawable.ic_collections_white))
//                    .resize(
//                        resources.getDimensionPixelSize(R.dimen.image_description_thumbnail_width),
//                        0
//                    )
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
            movie.releaseDate?.let { releaseDate ->
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
        moviesAPI = NetworkHelpers.provideAPI(requireContext())
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

            mListener?.onFragmentChangeFavorite(data, favoriteMovies, false)
        } else {
            Log.d(TAG, "isFavorite start, check data")
            data?.let {
                Log.d(TAG, "isFavorite start, data exist")
                disposable.add(
                    moviesDB.isFavorite(it.id)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .doFinally {
                            Log.d(TAG, "isFavorite finish")
                            bindOldData()
                            getDetailMovie()
                            mListener?.onFragmentChangeFavorite(data, favoriteMovies, false)
                        }
                        .subscribe({ favorite ->
                            Log.d(TAG, "isFavorite local=${favorite?.id}")
                            favoriteMovies = favorite != null
                        }, { throwable ->
                            Log.d(TAG, "isFavorite failed", throwable)
                            favoriteMovies = false
                        })
                )
            }
        }
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
                    .doOnNext {
                        Log.d(TAG, "accept: doOnNext: " + it.id)
                        disposable.add(
                            moviesDB.saveMovies(it)
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe()
                        )
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

    private fun onFailure(t: Throwable) {
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
        bindVideo(response.videos)
    }

    private fun onResponseReview(response: ReviewList) {
        Log.d(TAG, "onResponseReview: " + response.review.size)
        bindReview(response.review)
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
        Log.d(TAG, "changeFavorite")
        data?.let { movie ->
            val favoriteJob = if (favoriteMovies) {
                moviesDB.removeFromFavorites(movie.id)
            } else moviesDB.addToFavorites(movie.id)
            disposable.add(
                favoriteJob.subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        Log.d(TAG, "changeFavorite finish")
                        favoriteMovies = !favoriteMovies
                        mListener?.onFragmentChangeFavorite(movie, favoriteMovies, true)
                    }) { Log.d(TAG, "changeFavorite error", it) }
            )
        }
    }

    fun getStatusLoadedFromServer(): Boolean {
        return loadFromServer
    }

    fun getShareContent(): String {
        val shareUrl =
            if (videoListAdapter.itemCount > 0) videoListAdapter.get(0)?.youtubeVideo.orEmpty()
            else ""
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
        fun onFragmentChangeHeaderImage(imageUri: String?, thumbnailUri: String?)
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