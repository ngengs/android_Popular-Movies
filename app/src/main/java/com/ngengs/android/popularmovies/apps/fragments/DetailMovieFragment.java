package com.ngengs.android.popularmovies.apps.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ngengs.android.popularmovies.apps.R;
import com.ngengs.android.popularmovies.apps.data.MoviesDetail;
import com.ngengs.android.popularmovies.apps.data.ObjectName;
import com.ngengs.android.popularmovies.apps.globals.Values;
import com.ngengs.android.popularmovies.apps.utils.MoviesDBService;
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
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link DetailMovieFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link DetailMovieFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DetailMovieFragment extends Fragment {
    private static final String TAG = "DetailMovieFragment";

    @BindView(R.id.imageDetailThumb)
    ImageView imageThumbnail;
    @BindView(R.id.textRating)
    TextView textRating;
    @BindView(R.id.textMovieOriginalTitle)
    TextView textOriginalTitle;
    @BindView(R.id.textMovieReleaseDate)
    TextView textReleaseDate;
    @BindView(R.id.textMovieGenre)
    TextView textGenre;
    @BindView(R.id.textMovieBudget)
    TextView textBudget;
    @BindView(R.id.textMovieRevenue)
    TextView textRevenue;
    @BindView(R.id.textMovieCompany)
    TextView textCompany;
    @BindView(R.id.textMovieCountry)
    TextView textCountry;
    @BindView(R.id.textMovieLanguage)
    TextView textLanguage;
    @BindView(R.id.textMovieStatus)
    TextView textStatus;
    @BindView(R.id.textMovieTagline)
    TextView textTagline;
    @BindView(R.id.textMovieSynopsis)
    TextView textSynopsis;
    @BindView(R.id.taglineView)
    View taglineView;
    @BindView(R.id.detailView)
    View detailView;
    @BindView(R.id.rootProgressBar)
    View rootProgress;
    private Snackbar snackbar;

    private MoviesDetail data;
    private MoviesDBService moviesDBService;
    private CompositeDisposable disposable = new CompositeDisposable();
    private boolean loadFromServer;
    private Context context;

    private Unbinder unbinder;

    private OnFragmentInteractionListener mListener;

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
        View view = inflater.inflate(R.layout.fragment_detail_movie, container, false);
        context = view.getContext();
        unbinder = ButterKnife.bind(this, view);
        if (data != null) {
            Log.d(TAG, "onCreateView: data status: not null");
            createLayout(savedInstanceState);
        } else {
            Log.d(TAG, "onCreateView: data status: null");
            detailView.setVisibility(View.GONE);
            taglineView.setVisibility(View.GONE);
            loadFromServer = false;
        }
        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
        if (disposable != null && !disposable.isDisposed()) {
            disposable.clear();
        }
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
        if (unbinder != null) unbinder.unbind();
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

    private void bindOldData() {
        if (data.getTitle() != null && mListener != null) {
            mListener.onFragmentChangeTitle(data.getTitle());
        }
        if (data.getBackdropPath() != null && mListener != null) {
            mListener.onFragmentChangeHeaderImage(data.getBackdropPath());
        }
        if (data.getPosterPath(3) != null) {
            Picasso.with(context)
                    .load(data.getPosterPath(3))
                    .placeholder(ResourceHelpers.getDrawable(context, R.drawable.ic_collections_white))
                    .resize(getResources().getDimensionPixelSize(R.dimen.image_description_thumbnail_width), 0)
                    .into(imageThumbnail);
        }
        textRating.setText(getResources().getString(R.string.rating_number, data.getVoteAverage()));
        textRating.setTextColor(getRatingColor(data.getVoteAverage()));
        if (data.getOriginalTitle() != null) textOriginalTitle.setText(data.getOriginalTitle());
        if (data.getReleaseDate() != null) {
            DateFormat dateFormat = android.text.format.DateFormat.getLongDateFormat(context);
            String stringDate = dateFormat.format(data.getReleaseDate());

            textReleaseDate.setText(getResources().getString(R.string.release_date, stringDate));
        }

        if (data.getOverview() != null) textSynopsis.setText(data.getOverview());
    }

    private void bindData() {
        if (mListener != null) {
            mListener.onFragmentShowShare();
        }
        if (detailView.getVisibility() == View.GONE) detailView.setVisibility(View.VISIBLE);
        if (rootProgress.getVisibility() == View.VISIBLE) rootProgress.setVisibility(View.GONE);

        Log.d(TAG, "bindData: https://www.imdb.com/title/" + data.getImdbId());

        if (data.getGenres() != null && data.getGenres().size() > 0) {
            List<String> genre = new ArrayList<>();
            for (ObjectName item : data.getGenres()) {
                genre.add(item.getName());
            }
            textGenre.setText(TextUtils.join(", ", genre));
        }
        if (data.getBudget() >= 0) {
            String budgetCurrencyString = NumberFormat.getCurrencyInstance().format(data.getRevenue());
            budgetCurrencyString = budgetCurrencyString.replaceAll("\\.00", "");
            textBudget.setText(budgetCurrencyString);
        }
        if (data.getRevenue() >= 0) {
            String revenueCurrencyString = NumberFormat.getCurrencyInstance().format(data.getRevenue());
            revenueCurrencyString = revenueCurrencyString.replaceAll("\\.00", "");
            textRevenue.setText(revenueCurrencyString);
        }
        if (data.getProductionCompanies() != null && data.getProductionCompanies().size() > 0) {
            List<String> companies = new ArrayList<>();
            for (ObjectName item : data.getProductionCompanies()) {
                companies.add(item.getName());
            }
            textCompany.setText(TextUtils.join(", ", companies));
        }
        if (data.getProductionCountries() != null && data.getProductionCountries().size() > 0) {
            List<String> country = new ArrayList<>();
            for (ObjectName item : data.getProductionCountries()) {
                country.add(item.getName());
            }
            textCountry.setText(TextUtils.join(", ", country));
        }
        if (data.getSpokenLanguages() != null && data.getSpokenLanguages().size() > 0) {
            List<String> spokenLanguage = new ArrayList<>();
            for (ObjectName item : data.getSpokenLanguages()) {
                spokenLanguage.add(item.getName());
            }
            textLanguage.setText(TextUtils.join(", ", spokenLanguage));
        }
        if (data.getStatus() != null) textStatus.setText(data.getStatus());
        if (!TextUtils.isEmpty(data.getTagline())) {
            textTagline.setText(data.getTagline());
            taglineView.setVisibility(View.VISIBLE);
        }
    }

    public void setData(MoviesDetail data) {
        this.data = data;
        createLayout(null);
    }

    private void createLayout(Bundle savedInstanceState) {
        detailView.setVisibility(View.GONE);
        taglineView.setVisibility(View.GONE);
        loadFromServer = false;


        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Values.URL_BASE)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.createAsync())
                .build();
        moviesDBService = retrofit.create(MoviesDBService.class);

        Log.d(TAG, "createLayout: savedInstanceState: " + (savedInstanceState == null));

        if (savedInstanceState != null) {
            data = savedInstanceState.getParcelable("DATA");
            loadFromServer = savedInstanceState.getBoolean("ALREADY_CONNECT", false);
            Log.d(TAG, "createLayout: loadFromServer: " + loadFromServer);
            bindOldData();
            if (loadFromServer) bindData();
            else getDetailMovie();
        } else {
            if (data != null) bindOldData();
            getDetailMovie();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("DATA", data);
        outState.putBoolean("ALREADY_CONNECT", loadFromServer);
    }

    private void getDetailMovie() {
        if (moviesDBService != null) {
            rootProgress.setVisibility(View.VISIBLE);
            taglineView.setVisibility(View.GONE);
            detailView.setVisibility(View.GONE);
            disposable.add(
                    moviesDBService.detail(data.getId())
                            .subscribeOn(Schedulers.io())
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
                            }));
        }
    }

    public void onResponse(@NonNull MoviesDetail response) {
        Log.d(TAG, "onResponse: " + response.toString());
        if (rootProgress.getVisibility() == View.VISIBLE) rootProgress.setVisibility(View.GONE);
        if (detailView.getVisibility() == View.GONE) detailView.setVisibility(View.VISIBLE);
        if (snackbar != null) {
            snackbar.dismiss();
            snackbar = null;
        }
        data = response;

        Log.d(TAG, "onResponse: " + data.getHomepage());
        loadFromServer = true;
        bindData();

    }

    public boolean getStatusLoadedFromServer() {
        return loadFromServer;
    }

    public String getShareContent() {
        return getResources().getString(R.string.share_content, data.getTitle(), Values.URL_IMDB_BASE + Values.URL_IMDB_PATH_TITLE + data.getImdbId());
    }

    public void onFailure(@NonNull Throwable t) {
        if (rootProgress.getVisibility() == View.VISIBLE) rootProgress.setVisibility(View.GONE);
        if (detailView.getVisibility() == View.VISIBLE) detailView.setVisibility(View.GONE);
        if (taglineView.getVisibility() == View.VISIBLE) taglineView.setVisibility(View.GONE);
        if (snackbar != null) snackbar.dismiss();
        snackbar = Snackbar.make(rootProgress, R.string.error_cant_get_data_check_connection, Snackbar.LENGTH_INDEFINITE);
        snackbar.setAction(R.string.retry, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDetailMovie();
            }
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

        void onFragmentChangeTitle(@NonNull String title);

        void onFragmentChangeHeaderImage(@Nullable String imageUri);
    }
}
