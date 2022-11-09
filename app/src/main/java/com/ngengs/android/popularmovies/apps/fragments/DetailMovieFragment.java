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
import android.support.v7.widget.LinearLayoutManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
import com.ngengs.android.popularmovies.apps.databinding.FragmentDetailMovieBinding;
import com.ngengs.android.popularmovies.apps.globals.Values;
import com.ngengs.android.popularmovies.apps.utils.MoviesAPI;
import com.ngengs.android.popularmovies.apps.utils.NetworkHelpers;
import com.ngengs.android.popularmovies.apps.utils.ResourceHelpers;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

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

    private FragmentDetailMovieBinding binding;
    private Snackbar snackbar;

    private MoviesDetail data;
    private MoviesAPI moviesAPI;
    private CompositeDisposable disposable = new CompositeDisposable();
    private boolean loadFromServer;
    private boolean loadVideoFromServer;
    private boolean loadReviewFromServer;
    private boolean favoriteMovies;
    private Context context;

    private OnFragmentInteractionListener mListener;
    private VideoListAdapter videoListAdapter;
    private ReviewListAdapter reviewListAdapter;

    private MoviesProviderHelper moviesProviderHelper;

    public DetailMovieFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param params MovieDetail data.
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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            data = getArguments().getParcelable("DATA");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentDetailMovieBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        context = view.getContext();
        moviesProviderHelper = new MoviesProviderHelper(context);
        if (data != null) {
            Log.d(TAG, "onCreateView: data status: not null");
            createLayout(savedInstanceState);
        } else {
            Log.d(TAG, "onCreateView: data status: null");
            binding.detailView.setVisibility(View.GONE);
            binding.taglineView.setVisibility(View.GONE);
            loadFromServer = false;
            loadVideoFromServer = false;
            loadReviewFromServer = false;
            favoriteMovies = false;
        }
        return view;
    }

    @SuppressWarnings("EmptyMethod")
    @Override
    public void onPause() {
        super.onPause();
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
    public void onDestroyView() {
        super.onDestroyView();
        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
        }
        binding = null;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
        }
    }

    private int getRatingColor(double score) {
        if (score >= Values.RATING_SCORE_PERFECT)
            return ResourceHelpers.getColor(context, R.color.colorRatingPerfect);
        else if (score < Values.RATING_SCORE_PERFECT && score >= Values.RATING_SCORE_GOOD)
            return ResourceHelpers.getColor(context, R.color.colorRatingGood);
        else if (score < Values.RATING_SCORE_GOOD && score >= Values.RATING_SCORE_NORMAL)
            return ResourceHelpers.getColor(context, R.color.colorRatingNormal);
        else
            return ResourceHelpers.getColor(context, R.color.colorRatingBad);
    }

    public void changeFavorite() {
        if (favoriteMovies) moviesProviderHelper.removeFromFavorites(data.getId());
        else moviesProviderHelper.addToFavorites(data.getId());

        favoriteMovies = !favoriteMovies;
        if (mListener != null) mListener.onFragmentChangeFavorite(data, favoriteMovies, true);
    }

    private void bindOldData() {
        if (data.getBackdropPath() != null && mListener != null) {
            mListener.onFragmentChangeHeaderImage(data.getBackdropPath());
        }
        if (data.getPosterPath(3) != null) {
            Picasso.with(context)
                    .load(data.getPosterPath(3))
                    .placeholder(ResourceHelpers.getDrawable(context, R.drawable.ic_collections_white))
                    .resize(getResources().getDimensionPixelSize(R.dimen.image_description_thumbnail_width), 0)
                    .into(binding.imageDetailThumb);
        }
        bindUpdatedData();
    }

    private void bindUpdatedData() {
        if (data.getTitle() != null && mListener != null) {
            mListener.onFragmentChangeTitle(data.getTitle());
        }
        binding.textRating.setText(getResources().getString(R.string.rating_number, data.getVoteAverage()));
        binding.textRating.setTextColor(getRatingColor(data.getVoteAverage()));
        if (data.getOriginalTitle() != null) binding.textMovieOriginalTitle.setText(data.getOriginalTitle());
        if (data.getReleaseDate() != null) {
            DateFormat dateFormat = android.text.format.DateFormat.getLongDateFormat(context);
            String stringDate = dateFormat.format(data.getReleaseDate());

            binding.textMovieReleaseDate.setText(getResources().getString(R.string.release_date, stringDate));
        }

        if (data.getOverview() != null) binding.textMovieSynopsis.setText(data.getOverview());
    }

    public int getMoviesId() {
        if (data != null) return data.getId();
        else return -1;
    }

    private void bindData() {
        if (binding.detailView.getVisibility() == View.GONE) binding.detailView.setVisibility(View.VISIBLE);
        if (binding.rootProgressBar.getVisibility() == View.VISIBLE) binding.rootProgressBar.setVisibility(View.GONE);

        if (data.getGenres() != null && data.getGenres().size() > 0) {
            List<String> genre = new ArrayList<>();
            for (ObjectName item : data.getGenres()) {
                genre.add(item.getName());
            }
            binding.textMovieGenre.setText(TextUtils.join(", ", genre));
        }
        if (data.getBudget() >= 0) {
            String budgetCurrencyString = NumberFormat.getCurrencyInstance().format(data.getRevenue());
            budgetCurrencyString = budgetCurrencyString.replaceAll("\\.00", "");
            binding.textMovieBudget.setText(budgetCurrencyString);
        }
        if (data.getRevenue() >= 0) {
            String revenueCurrencyString = NumberFormat.getCurrencyInstance().format(data.getRevenue());
            revenueCurrencyString = revenueCurrencyString.replaceAll("\\.00", "");
            binding.textMovieRevenue.setText(revenueCurrencyString);
        }
        if (data.getProductionCompanies() != null && data.getProductionCompanies().size() > 0) {
            List<String> companies = new ArrayList<>();
            for (ObjectName item : data.getProductionCompanies()) {
                companies.add(item.getName());
            }
            binding.textMovieCompany.setText(TextUtils.join(", ", companies));
        }
        if (data.getProductionCountries() != null && data.getProductionCountries().size() > 0) {
            List<String> country = new ArrayList<>();
            for (ObjectName item : data.getProductionCountries()) {
                country.add(item.getName());
            }
            binding.textMovieCountry.setText(TextUtils.join(", ", country));
        }
        if (data.getSpokenLanguages() != null && data.getSpokenLanguages().size() > 0) {
            List<String> spokenLanguage = new ArrayList<>();
            for (ObjectName item : data.getSpokenLanguages()) {
                spokenLanguage.add(item.getName());
            }
            binding.textMovieLanguage.setText(TextUtils.join(", ", spokenLanguage));
        }
        if (data.getStatus() != null) binding.textMovieStatus.setText(data.getStatus());
        if (!TextUtils.isEmpty(data.getTagline())) {
            binding.textMovieTagline.setText(data.getTagline());
            binding.textMovieTagline.setVisibility(View.VISIBLE);
        }
        bindUpdatedData();
    }

    @SuppressWarnings("unused")
    public void setData(MoviesDetail data) {
        this.data = data;
        createLayout(null);
    }

    private void createLayout(Bundle savedInstanceState) {
        binding.detailView.setVisibility(View.GONE);
        binding.taglineView.setVisibility(View.GONE);
        loadFromServer = false;
        loadVideoFromServer = false;
        loadReviewFromServer = false;

        binding.videoLayout.recyclerVideo.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
        binding.videoLayout.recyclerVideo.setHasFixedSize(true);
        videoListAdapter = new VideoListAdapter(context, position -> {
            Log.d(TAG, "onClickListener: " + position);
            VideosDetail video = videoListAdapter.get(position);
            if (video != null && video.isYoutubeVideo()) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(video.getYoutubeVideo()));
                startActivity(intent);
            }
        });
        binding.videoLayout.recyclerVideo.setAdapter(videoListAdapter);

        binding.reviewLayout.recyclerReview.setLayoutManager(new LinearLayoutManager(context));
        binding.reviewLayout.recyclerReview.setHasFixedSize(true);
        reviewListAdapter = new ReviewListAdapter(context, position -> {
            Log.d(TAG, "onClickListener: " + position);
            ReviewDetail review = reviewListAdapter.get(position);
            if (review != null && !TextUtils.isEmpty(review.getUrl())) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(review.getUrl()));
                startActivity(intent);
            }
        });
        binding.reviewLayout.recyclerReview.setAdapter(reviewListAdapter);

        moviesAPI = NetworkHelpers.provideAPI();

        Log.d(TAG, "createLayout: savedInstanceState: " + (savedInstanceState == null));

        if (savedInstanceState != null) {
            data = savedInstanceState.getParcelable("DATA");
            loadFromServer = savedInstanceState.getBoolean("ALREADY_CONNECT", false);
            loadVideoFromServer = savedInstanceState.getBoolean("ALREADY_VIDEO_CONNECT", false);
            loadReviewFromServer = savedInstanceState.getBoolean("ALREADY_REVIEW_CONNECT", false);
            favoriteMovies = savedInstanceState.getBoolean("FAVORITED_MOVIES", false);
            List<VideosDetail> tempVideo = savedInstanceState.getParcelableArrayList("DATA_VIDEO");
            List<ReviewDetail> tempReview = savedInstanceState.getParcelableArrayList("DATA_REVIEW");
            Log.d(TAG, "createLayout: loadFromServer: " + loadFromServer);
            bindOldData();
            if (loadFromServer && loadVideoFromServer && loadReviewFromServer) {
                bindData();
                bindVideo(tempVideo);
                bindReview(tempReview);
            } else getDetailMovie();
        } else {
            favoriteMovies = moviesProviderHelper.isFavorite(data.getId());
            if (data != null) bindOldData();
            getDetailMovie();
        }
        if (mListener != null) mListener.onFragmentChangeFavorite(data, favoriteMovies, false);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("DATA", data);
        outState.putParcelableArrayList("DATA_VIDEO", new ArrayList<Parcelable>(videoListAdapter.get()));
        outState.putParcelableArrayList("DATA_REVIEW", new ArrayList<Parcelable>(reviewListAdapter.get()));
        outState.putBoolean("ALREADY_CONNECT", loadFromServer);
        outState.putBoolean("ALREADY_VIDEO_CONNECT", loadVideoFromServer);
        outState.putBoolean("ALREADY_REVIEW_CONNECT", loadReviewFromServer);
        outState.putBoolean("FAVORITED_MOVIES", favoriteMovies);
    }

    private void getDetailMovie() {
        if (moviesAPI != null) {
            binding.rootProgressBar.setVisibility(View.VISIBLE);
            binding.taglineView.setVisibility(View.GONE);
            binding.detailView.setVisibility(View.GONE);
            disposable.addAll(
                    moviesAPI.detail(data.getId())
                            .subscribeOn(Schedulers.io())
                            .doOnNext(new Consumer<MoviesDetail>() {
                                @Override
                                public void accept(@io.reactivex.annotations.NonNull MoviesDetail moviesDetail) throws Exception {
                                    Log.d(TAG, "accept: doOnNext: " + moviesDetail.getId());
                                    moviesProviderHelper.saveMovies(moviesDetail);
                                }
                            })
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new Consumer<MoviesDetail>() {
                                @Override
                                public void accept(@io.reactivex.annotations.NonNull MoviesDetail moviesDetail) throws Exception {
                                    onResponse(moviesDetail);
                                }
                            }, new Consumer<Throwable>() {
                                @Override
                                public void accept(@io.reactivex.annotations.NonNull Throwable throwable) throws Exception {
                                    onFailure(throwable);
                                }
                            }),
                    moviesAPI.videos(data.getId())
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new Consumer<VideosList>() {
                                @Override
                                public void accept(@io.reactivex.annotations.NonNull VideosList videosList) throws Exception {
                                    onResponseVideo(videosList);
                                }
                            }, new Consumer<Throwable>() {
                                @Override
                                public void accept(@io.reactivex.annotations.NonNull Throwable throwable) throws Exception {
                                    Log.e(TAG, "accept: Error Get Videos", throwable);
                                }
                            }),
                    moviesAPI.reviews(data.getId())
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new Consumer<ReviewList>() {
                                @Override
                                public void accept(@io.reactivex.annotations.NonNull ReviewList reviewList) throws Exception {
                                    onResponseReview(reviewList);
                                }
                            }, new Consumer<Throwable>() {
                                @Override
                                public void accept(@io.reactivex.annotations.NonNull Throwable throwable) throws Exception {
                                    Log.e(TAG, "accept: Error Get Reviews", throwable);
                                }
                            })
            );
        }
    }

    public void onResponse(@NonNull MoviesDetail response) {
        Log.d(TAG, "onResponse: " + response);
        if (binding.rootProgressBar.getVisibility() == View.VISIBLE) binding.rootProgressBar.setVisibility(View.GONE);
        if (binding.detailView.getVisibility() == View.GONE) binding.detailView.setVisibility(View.VISIBLE);
        if (snackbar != null) {
            snackbar.dismiss();
            snackbar = null;
        }
        data = response;

        Log.d(TAG, "onResponse: " + data.getHomepage());
        loadFromServer = true;
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
        loadVideoFromServer = true;
        videoListAdapter.clear();
        if (video.size() > 0) {
            if (mListener != null) mListener.onFragmentShowShare();
            binding.videoLayout.cardVideo.setVisibility(View.VISIBLE);
            videoListAdapter.add(video);
        }
    }

    private void bindReview(List<ReviewDetail> review) {
        loadReviewFromServer = true;
        if (review.size() > 0) binding.reviewLayout.cardReview.setVisibility(View.VISIBLE);
        reviewListAdapter.clear();
        reviewListAdapter.add(review);
    }

    public boolean getStatusLoadedFromServer() {
        return loadFromServer;
    }

    public String getShareContent() {
        String shareUrl = "";
        if (videoListAdapter.getItemCount() > 0)
            shareUrl = videoListAdapter.get(0).getYoutubeVideo();
        return getResources().getString(R.string.share_content, data.getTitle(), shareUrl);
    }

    public void onFailure(@NonNull Throwable t) {
        if (binding.rootProgressBar.getVisibility() == View.VISIBLE) binding.rootProgressBar.setVisibility(View.GONE);
        if (binding.detailView.getVisibility() == View.VISIBLE) binding.detailView.setVisibility(View.GONE);
        if (binding.taglineView.getVisibility() == View.VISIBLE) binding.taglineView.setVisibility(View.GONE);
        if (snackbar != null) snackbar.dismiss();
        snackbar = Snackbar.make(binding.rootProgressBar, R.string.error_cant_get_data_check_connection, Snackbar.LENGTH_INDEFINITE);
        snackbar.setAction(R.string.retry, v -> {
            bindOldData();
            getDetailMovie();
        });
        snackbar.show();
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
