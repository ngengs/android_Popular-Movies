package com.ngengs.android.popularmovies.apps.fragments;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ngengs.android.popularmovies.apps.R;
import com.ngengs.android.popularmovies.apps.adapters.ReviewListAdapter;
import com.ngengs.android.popularmovies.apps.adapters.VideoListAdapter;
import com.ngengs.android.popularmovies.apps.data.MoviesDetail;
import com.ngengs.android.popularmovies.apps.data.ObjectName;
import com.ngengs.android.popularmovies.apps.data.ReviewDetail;
import com.ngengs.android.popularmovies.apps.data.ReviewList;
import com.ngengs.android.popularmovies.apps.data.VideosDetail;
import com.ngengs.android.popularmovies.apps.data.VideosList;
import com.ngengs.android.popularmovies.apps.data.local.MoviesProviderHelper;
import com.ngengs.android.popularmovies.apps.globals.Values;
import com.ngengs.android.popularmovies.apps.utils.MoviesAPI;
import com.ngengs.android.popularmovies.apps.utils.NetworkHelpers;
import com.ngengs.android.popularmovies.apps.utils.ResourceHelpers;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link DetailMovieFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DetailMovieFragment extends Fragment {
    private static final String TAG = "DetailMovieFragment";

    @BindView(R.id.imageDetailThumb)
    ImageView mImageThumbnail;
    @BindView(R.id.textRating)
    TextView mTextRating;
    @BindView(R.id.textMovieOriginalTitle)
    TextView mTextOriginalTitle;
    @BindView(R.id.textMovieReleaseDate)
    TextView mTextReleaseDate;
    @BindView(R.id.textMovieGenre)
    TextView mTextGenre;
    @BindView(R.id.textMovieBudget)
    TextView mTextBudget;
    @BindView(R.id.textMovieRevenue)
    TextView mTextRevenue;
    @BindView(R.id.textMovieCompany)
    TextView mTextCompany;
    @BindView(R.id.textMovieCountry)
    TextView mTextCountry;
    @BindView(R.id.textMovieLanguage)
    TextView mTextLanguage;
    @BindView(R.id.textMovieStatus)
    TextView mTextStatus;
    @BindView(R.id.textMovieTagline)
    TextView mTextTagline;
    @BindView(R.id.textMovieSynopsis)
    TextView mTextSynopsis;
    @BindView(R.id.taglineView)
    View mTaglineView;
    @BindView(R.id.detailView)
    View mDetailView;
    @BindView(R.id.rootProgressBar)
    View mRootProgress;
    @BindView(R.id.recyclerVideo)
    RecyclerView mRecyclerVideo;
    @BindView(R.id.cardVideo)
    CardView mCardVideo;
    @BindView(R.id.recyclerReview)
    RecyclerView mRecyclerReview;
    @BindView(R.id.cardReview)
    CardView mCardReview;
    private Snackbar mSnackbar;

    private MoviesDetail mData;
    private MoviesAPI mMoviesAPI;
    private CompositeDisposable mDisposable = new CompositeDisposable();
    private boolean mLoadFromServer;
    private boolean mLoadVideoFromServer;
    private boolean mLoadReviewFromServer;
    private boolean mFavoritedMovies;
    private Context mContext;

    private Unbinder mUnbinder;

    private OnFragmentInteractionListener mListener;
    private VideoListAdapter mVideoListAdapter;
    private ReviewListAdapter mReviewListAdapter;

    private MoviesProviderHelper mMoviesProviderHelper;

    public DetailMovieFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param params MovieDetail mData.
     * @return A new instance of fragment DetailMovieFragment.
     */
    public static DetailMovieFragment newInstance(MoviesDetail params) {
        DetailMovieFragment fragment = new DetailMovieFragment();
        Bundle args = new Bundle();
        args.putParcelable("DATA", params);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                                               + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) mData = getArguments().getParcelable("DATA");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_detail_movie, container, false);
        mContext = view.getContext();
        mUnbinder = ButterKnife.bind(this, view);
        mMoviesProviderHelper = new MoviesProviderHelper(mContext);
        if (mData != null) {
            Log.d(TAG, "onCreateView: mData status: not null");
            createLayout(savedInstanceState);
        } else {
            Log.d(TAG, "onCreateView: mData status: null");
            mDetailView.setVisibility(View.GONE);
            mTaglineView.setVisibility(View.GONE);
            mLoadFromServer = false;
            mLoadVideoFromServer = false;
            mLoadReviewFromServer = false;
            mFavoritedMovies = false;
        }
        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("DATA", mData);
        outState.putParcelableArrayList("DATA_VIDEO",
                                        new ArrayList<Parcelable>(mVideoListAdapter.get()));
        outState.putParcelableArrayList("DATA_REVIEW",
                                        new ArrayList<Parcelable>(mReviewListAdapter.get()));
        outState.putBoolean("ALREADY_CONNECT", mLoadFromServer);
        outState.putBoolean("ALREADY_VIDEO_CONNECT", mLoadVideoFromServer);
        outState.putBoolean("ALREADY_REVIEW_CONNECT", mLoadReviewFromServer);
        outState.putBoolean("FAVORITED_MOVIES", mFavoritedMovies);
    }

    @SuppressWarnings("EmptyMethod")
    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mDisposable != null && !mDisposable.isDisposed()) {
            mDisposable.dispose();
        }
        if (mUnbinder != null) mUnbinder.unbind();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
        if (mDisposable != null && !mDisposable.isDisposed()) {
            mDisposable.dispose();
        }
    }

    private int getRatingColor(double score) {
        if (score >= Values.RATING_SCORE_PERFECT)
            return ResourceHelpers.getColor(mContext, R.color.colorRatingPerfect);
        else if (score < Values.RATING_SCORE_PERFECT && score >= Values.RATING_SCORE_GOOD)
            return ResourceHelpers.getColor(mContext, R.color.colorRatingGood);
        else if (score < Values.RATING_SCORE_GOOD && score >= Values.RATING_SCORE_NORMAL)
            return ResourceHelpers.getColor(mContext, R.color.colorRatingNormal);
        else
            return ResourceHelpers.getColor(mContext, R.color.colorRatingBad);
    }

    public void changeFavorite() {
        if (mFavoritedMovies) mMoviesProviderHelper.removeFromFavorites(mData.getId());
        else mMoviesProviderHelper.addToFavorites(mData.getId());

        mFavoritedMovies = !mFavoritedMovies;
        if (mListener != null) mListener.onFragmentChangeFavorite(mData, mFavoritedMovies, true);
    }

    private void bindOldData() {
        if (mData.getBackdropPath() != null && mListener != null) {
            mListener.onFragmentChangeHeaderImage(mData.getBackdropPath());
        }
        if (mData.getPosterPath(3) != null) {
            Picasso.with(mContext)
                    .load(mData.getPosterPath(3))
                    .placeholder(
                            ResourceHelpers.getDrawable(mContext, R.drawable.ic_collections_white))
                    .resize(getResources().getDimensionPixelSize(
                            R.dimen.image_description_thumbnail_width), 0)
                    .into(mImageThumbnail);
        }
        bindUpdatedData();
    }

    private void bindUpdatedData() {
        if (mData.getTitle() != null && mListener != null) {
            mListener.onFragmentChangeTitle(mData.getTitle());
        }
        mTextRating.setText(
                getResources().getString(R.string.rating_number, mData.getVoteAverage()));
        mTextRating.setTextColor(getRatingColor(mData.getVoteAverage()));
        if (mData.getOriginalTitle() != null) mTextOriginalTitle.setText(mData.getOriginalTitle());
        if (mData.getReleaseDate() != null) {
            DateFormat dateFormat = android.text.format.DateFormat.getLongDateFormat(mContext);
            String stringDate = dateFormat.format(mData.getReleaseDate());

            mTextReleaseDate.setText(getResources().getString(R.string.release_date, stringDate));
        }

        if (mData.getOverview() != null) mTextSynopsis.setText(mData.getOverview());
    }

    public int getMoviesId() {
        if (mData != null) return mData.getId();
        else return -1;
    }

    private void bindData() {
        if (mDetailView.getVisibility() == View.GONE) mDetailView.setVisibility(View.VISIBLE);
        if (mRootProgress.getVisibility() == View.VISIBLE) mRootProgress.setVisibility(View.GONE);

        if (mData.getGenres() != null && mData.getGenres().size() > 0) {
            List<String> genre = new ArrayList<>();
            for (ObjectName item : mData.getGenres()) {
                genre.add(item.getName());
            }
            mTextGenre.setText(TextUtils.join(", ", genre));
        }
        if (mData.getBudget() >= 0) {
            String budgetCurrencyString =
                    NumberFormat.getCurrencyInstance().format(mData.getRevenue());
            budgetCurrencyString = budgetCurrencyString.replaceAll("\\.00", "");
            mTextBudget.setText(budgetCurrencyString);
        }
        if (mData.getRevenue() >= 0) {
            String revenueCurrencyString =
                    NumberFormat.getCurrencyInstance().format(mData.getRevenue());
            revenueCurrencyString = revenueCurrencyString.replaceAll("\\.00", "");
            mTextRevenue.setText(revenueCurrencyString);
        }
        if (mData.getProductionCompanies() != null && mData.getProductionCompanies().size() > 0) {
            List<String> companies = new ArrayList<>();
            for (ObjectName item : mData.getProductionCompanies()) {
                companies.add(item.getName());
            }
            mTextCompany.setText(TextUtils.join(", ", companies));
        }
        if (mData.getProductionCountries() != null && mData.getProductionCountries().size() > 0) {
            List<String> country = new ArrayList<>();
            for (ObjectName item : mData.getProductionCountries()) {
                country.add(item.getName());
            }
            mTextCountry.setText(TextUtils.join(", ", country));
        }
        if (mData.getSpokenLanguages() != null && mData.getSpokenLanguages().size() > 0) {
            List<String> spokenLanguage = new ArrayList<>();
            for (ObjectName item : mData.getSpokenLanguages()) {
                spokenLanguage.add(item.getName());
            }
            mTextLanguage.setText(TextUtils.join(", ", spokenLanguage));
        }
        if (mData.getStatus() != null) mTextStatus.setText(mData.getStatus());
        if (!TextUtils.isEmpty(mData.getTagline())) {
            mTextTagline.setText(mData.getTagline());
            mTaglineView.setVisibility(View.VISIBLE);
        }
        bindUpdatedData();
    }

    @SuppressWarnings("unused")
    public void setmData(MoviesDetail mData) {
        this.mData = mData;
        createLayout(null);
    }

    private void createLayout(Bundle savedInstanceState) {
        mDetailView.setVisibility(View.GONE);
        mTaglineView.setVisibility(View.GONE);
        mLoadFromServer = false;
        mLoadVideoFromServer = false;
        mLoadReviewFromServer = false;

        mRecyclerVideo.setLayoutManager(
                new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false));
        mRecyclerVideo.setHasFixedSize(true);
        mVideoListAdapter = new VideoListAdapter(mContext, new VideoListAdapter.ClickListener() {
            @Override
            public void onClickListener(int position) {
                Log.d(TAG, "onClickListener: Video: " + position);
                VideosDetail video = mVideoListAdapter.get(position);
                if (video != null && video.isYoutubeVideo()) {
                    Intent intent =
                            new Intent(Intent.ACTION_VIEW, Uri.parse(video.getYoutubeVideo()));
                    startActivity(intent);
                }
            }
        });
        mRecyclerVideo.setAdapter(mVideoListAdapter);

        mRecyclerReview.setLayoutManager(new LinearLayoutManager(mContext));
        mRecyclerReview.setHasFixedSize(true);
        mReviewListAdapter = new ReviewListAdapter(mContext, new ReviewListAdapter.ClickListener() {
            @Override
            public void onClickListener(int position) {
                Log.d(TAG, "onClickListener: Review: " + position);
                ReviewDetail review = mReviewListAdapter.get(position);
                if (review != null && !TextUtils.isEmpty(review.getUrl())) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(review.getUrl()));
                    startActivity(intent);
                }
            }
        });
        mRecyclerReview.setAdapter(mReviewListAdapter);

        mMoviesAPI = NetworkHelpers.provideAPI();

        Log.d(TAG, "createLayout: savedInstanceState: " + (savedInstanceState == null));

        if (savedInstanceState != null) {
            mData = savedInstanceState.getParcelable("DATA");
            mLoadFromServer = savedInstanceState.getBoolean("ALREADY_CONNECT", false);
            mLoadVideoFromServer = savedInstanceState.getBoolean("ALREADY_VIDEO_CONNECT", false);
            mLoadReviewFromServer = savedInstanceState.getBoolean("ALREADY_REVIEW_CONNECT", false);
            mFavoritedMovies = savedInstanceState.getBoolean("FAVORITED_MOVIES", false);
            List<VideosDetail> tempVideo = savedInstanceState.getParcelableArrayList("DATA_VIDEO");
            List<ReviewDetail> tempReview =
                    savedInstanceState.getParcelableArrayList("DATA_REVIEW");
            Log.d(TAG, "createLayout: mLoadFromServer: " + mLoadFromServer);
            bindOldData();
            if (mLoadFromServer && mLoadVideoFromServer && mLoadReviewFromServer) {
                bindData();
                bindVideo(tempVideo);
                bindReview(tempReview);
            } else getDetailMovie();
        } else {
            mFavoritedMovies = mMoviesProviderHelper.isFavorite(mData.getId());
            if (mData != null) bindOldData();
            getDetailMovie();
        }
        if (mListener != null) mListener.onFragmentChangeFavorite(mData, mFavoritedMovies, false);
    }

    private void getDetailMovie() {
        if (mMoviesAPI != null) {
            mRootProgress.setVisibility(View.VISIBLE);
            mTaglineView.setVisibility(View.GONE);
            mDetailView.setVisibility(View.GONE);
            mDisposable.addAll(
                    mMoviesAPI.detail(mData.getId())
                            .subscribeOn(Schedulers.io())
                            .doOnNext(new Consumer<MoviesDetail>() {
                                @Override
                                public void accept(
                                        @io.reactivex.annotations.NonNull MoviesDetail moviesDetail) throws
                                        Exception {
                                    Log.d(TAG, "accept: doOnNext: " + moviesDetail.getId());
                                    mMoviesProviderHelper.saveMovies(moviesDetail);
                                }
                            })
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new Consumer<MoviesDetail>() {
                                @Override
                                public void accept(
                                        @io.reactivex.annotations.NonNull MoviesDetail moviesDetail) throws
                                        Exception {
                                    onResponse(moviesDetail);
                                }
                            }, new Consumer<Throwable>() {
                                @Override
                                public void accept(
                                        @io.reactivex.annotations.NonNull Throwable throwable) throws
                                        Exception {
                                    onFailure(throwable);
                                }
                            }),
                    mMoviesAPI.videos(mData.getId())
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new Consumer<VideosList>() {
                                @Override
                                public void accept(
                                        @io.reactivex.annotations.NonNull VideosList videosList) throws
                                        Exception {
                                    onResponseVideo(videosList);
                                }
                            }, new Consumer<Throwable>() {
                                @Override
                                public void accept(
                                        @io.reactivex.annotations.NonNull Throwable throwable) throws
                                        Exception {
                                    Log.e(TAG, "accept: Error Get Videos", throwable);
                                }
                            }),
                    mMoviesAPI.reviews(mData.getId())
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new Consumer<ReviewList>() {
                                @Override
                                public void accept(
                                        @io.reactivex.annotations.NonNull ReviewList reviewList) throws
                                        Exception {
                                    onResponseReview(reviewList);
                                }
                            }, new Consumer<Throwable>() {
                                @Override
                                public void accept(
                                        @io.reactivex.annotations.NonNull Throwable throwable) throws
                                        Exception {
                                    Log.e(TAG, "accept: Error Get Reviews", throwable);
                                }
                            })
            );
        }
    }

    public void onResponse(@NonNull MoviesDetail response) {
        Log.d(TAG, "onResponse: " + response.toString());
        if (mRootProgress.getVisibility() == View.VISIBLE) mRootProgress.setVisibility(View.GONE);
        if (mDetailView.getVisibility() == View.GONE) mDetailView.setVisibility(View.VISIBLE);
        if (mSnackbar != null) {
            mSnackbar.dismiss();
            mSnackbar = null;
        }
        mData = response;

        mLoadFromServer = true;
        bindData();
    }

    public void onResponseVideo(@NonNull VideosList response) {
        Log.d(TAG, "onResponseVideo: " + response.toString());
        if (response.getVideos() != null) {
            bindVideo(response.getVideos());
        }
    }

    public void onResponseReview(@NonNull ReviewList response) {
        Log.d(TAG, "onResponseReview: " + response.getReview().size());
        if (response.getReview() != null) {
            bindReview(response.getReview());
        }
    }

    private void bindVideo(List<VideosDetail> video) {
        mLoadVideoFromServer = true;
        mVideoListAdapter.clear();
        if (video.size() > 0) {
            if (mListener != null) mListener.onFragmentShowShare();
            mCardVideo.setVisibility(View.VISIBLE);
            mVideoListAdapter.add(video);
        }
    }

    private void bindReview(List<ReviewDetail> review) {
        mLoadReviewFromServer = true;
        if (review.size() > 0) mCardReview.setVisibility(View.VISIBLE);
        mReviewListAdapter.clear();
        mReviewListAdapter.add(review);
    }

    public boolean getStatusLoadedFromServer() {
        return mLoadFromServer;
    }

    public String getShareContent() {
        String shareUrl = "";
        if (mVideoListAdapter.getItemCount() > 0)
            shareUrl = mVideoListAdapter.get(0).getYoutubeVideo();
        return getResources().getString(R.string.share_content, mData.getTitle(), shareUrl);
    }

    public void onFailure(@NonNull Throwable t) {
        if (mRootProgress.getVisibility() == View.VISIBLE) mRootProgress.setVisibility(View.GONE);
        if (mDetailView.getVisibility() == View.VISIBLE) mDetailView.setVisibility(View.GONE);
        if (mTaglineView.getVisibility() == View.VISIBLE) mTaglineView.setVisibility(View.GONE);
        if (mSnackbar != null) mSnackbar.dismiss();
        mSnackbar = Snackbar.make(mRootProgress, R.string.error_cant_get_data_check_connection,
                                  Snackbar.LENGTH_INDEFINITE);
        mSnackbar.setAction(R.string.retry, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bindOldData();
                getDetailMovie();
            }
        });
        mSnackbar.show();
        Log.e(TAG, "onFailure: ", t);
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        void onFragmentShowShare();

        void onFragmentChangeFavorite(MoviesDetail data, boolean isFavorite, boolean isRefresh);

        void onFragmentChangeTitle(@NonNull String title);

        void onFragmentChangeHeaderImage(@Nullable String imageUri);
    }
}
