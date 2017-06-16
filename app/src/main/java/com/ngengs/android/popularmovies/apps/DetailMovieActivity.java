package com.ngengs.android.popularmovies.apps;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ngengs.android.popularmovies.apps.data.MoviesDetail;
import com.ngengs.android.popularmovies.apps.data.ObjectName;
import com.ngengs.android.popularmovies.apps.globals.Values;
import com.ngengs.android.popularmovies.apps.utils.MoviesDBService;
import com.ngengs.android.popularmovies.apps.utils.ResourceHelpers;
import com.squareup.picasso.Picasso;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

@SuppressWarnings("FieldCanBeLocal")
public class DetailMovieActivity extends AppCompatActivity implements Callback<MoviesDetail> {
    private static final String TAG = "DetailMovieActivity";

    private Toolbar toolbar;
    private ImageView imageHeader;
    private ImageView imageThumbnail;
    private TextView textRating;
    private TextView textOriginalTitle;
    private TextView textReleaseDate;
    private TextView textGenre;
    private TextView textBudget;
    private TextView textRevenue;
    private TextView textCompany;
    private TextView textCountry;
    private TextView textLanguage;
    private TextView textStatus;
    private TextView textTagline;
    private TextView textSynopsis;
    private View rootData;
    private View rootProgress;
    private Snackbar snackbar;
    private FloatingActionButton fab;

    private MoviesDetail data;
    private MoviesDBService moviesDBService;
    private Call<MoviesDetail> callService;
    private boolean loadFromServer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_movie);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        imageHeader = (ImageView) findViewById(R.id.detailHeaderImage);
        imageThumbnail = (ImageView) findViewById(R.id.imageDetailThumb);
        textRating = (TextView) findViewById(R.id.textRating);
        textOriginalTitle = (TextView) findViewById(R.id.textMovieOriginalTitle);
        textReleaseDate = (TextView) findViewById(R.id.textMovieReleaseDate);
        textGenre = (TextView) findViewById(R.id.textMovieGenre);
        textBudget = (TextView) findViewById(R.id.textMovieBudget);
        textRevenue = (TextView) findViewById(R.id.textMovieRevenue);
        textCompany = (TextView) findViewById(R.id.textMovieCompany);
        textCountry = (TextView) findViewById(R.id.textMovieCountry);
        textLanguage = (TextView) findViewById(R.id.textMovieLanguage);
        textStatus = (TextView) findViewById(R.id.textMovieStatus);
        textTagline = (TextView) findViewById(R.id.textMovieTagline);
        textSynopsis = (TextView) findViewById(R.id.textMovieSynopsis);
        rootData = findViewById(R.id.rootData);
        rootProgress = findViewById(R.id.rootProgressBar);
        fab = (FloatingActionButton) findViewById(R.id.fabShare);

        rootData.setVisibility(View.GONE);
        fab.hide();
        loadFromServer = false;

        setSupportActionBar(toolbar);

        data = (MoviesDetail) getIntent().getSerializableExtra("DATA");

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            if (data != null) getSupportActionBar().setTitle(data.getTitle());
        } else {
            Toast.makeText(this, "Something wrong with detail data", Toast.LENGTH_SHORT).show();
            finish();
        }

        if (data != null) {
            bindOldData();
        }

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Values.URL_BASE)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        moviesDBService = retrofit.create(MoviesDBService.class);

        if (savedInstanceState != null) {
            data = (MoviesDetail) savedInstanceState.getSerializable("DATA");
            loadFromServer = savedInstanceState.getBoolean("ALREADY_CONNECT", false);
            if (loadFromServer) bindData();
            else getDetailMovie();
        } else {
            getDetailMovie();
        }


        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (loadFromServer) {
                    Intent sendIntent = new Intent();
                    sendIntent.setAction(Intent.ACTION_SEND);
                    Log.d(TAG, "onClick: " + getResources().getString(R.string.share_content, data.getTitle(), Values.URL_IMDB_BASE + Values.URL_IMDB_PATH_TITLE + data.getImdbId()));
                    sendIntent.putExtra(Intent.EXTRA_TEXT, getResources().getString(R.string.share_content, data.getTitle(), Values.URL_IMDB_BASE + Values.URL_IMDB_PATH_TITLE + data.getImdbId()));
                    sendIntent.setType("text/plain");
                    startActivity(Intent.createChooser(sendIntent, getResources().getText(R.string.send_to)));
                }
            }
        });
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putSerializable("DATA", data);
        outState.putBoolean("ALREADY_CONNECT", loadFromServer);
    }

    private void getDetailMovie() {
        if (moviesDBService != null) {
            rootProgress.setVisibility(View.VISIBLE);
            rootData.setVisibility(View.GONE);
            if (callService != null) callService.cancel();
            callService = moviesDBService.detail(data.getId());
            callService.enqueue(this);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResponse(@NonNull Call<MoviesDetail> call, @NonNull Response<MoviesDetail> response) {
        Log.d(TAG, "onResponse: " + response.toString());
        if (rootProgress.getVisibility() == View.VISIBLE) rootProgress.setVisibility(View.GONE);
        if (rootData.getVisibility() == View.GONE) rootData.setVisibility(View.VISIBLE);
        if (snackbar != null) {
            snackbar.dismiss();
            snackbar = null;
        }
        MoviesDetail moviesDetail = response.body();
        if (moviesDetail != null) {
            data = moviesDetail;

            Log.d(TAG, "onResponse: " + data.getHomepage());
            loadFromServer = true;
            bindData();
        }
    }

    @Override
    public void onFailure(@NonNull Call<MoviesDetail> call, @NonNull Throwable t) {
        Log.e(TAG, "onFailure: ", t);
        if (rootProgress.getVisibility() == View.VISIBLE) rootProgress.setVisibility(View.GONE);
        if (rootData.getVisibility() == View.VISIBLE) rootData.setVisibility(View.GONE);
        if (snackbar != null) snackbar.dismiss();
        snackbar = Snackbar.make(toolbar, R.string.error_cant_get_data_check_connection, Snackbar.LENGTH_INDEFINITE);
        snackbar.setAction(R.string.retry, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDetailMovie();
            }
        });
        snackbar.show();
    }

    private int getRatingColor(double score) {
        if (score >= Values.RATING_SCORE_PERFECT)
            return ResourceHelpers.getColor(this, R.color.colorRatingPerfect);
        else if (score < Values.RATING_SCORE_PERFECT && score >= Values.RATING_SCORE_GOOD)
            return ResourceHelpers.getColor(this, R.color.colorRatingGood);
        else if (score < Values.RATING_SCORE_GOOD && score >= Values.RATING_SCORE_NORMAL)
            return ResourceHelpers.getColor(this, R.color.colorRatingNormal);
        else
            return ResourceHelpers.getColor(this, R.color.colorRatingBad);
    }

    private void bindOldData() {
        if (data.getBackdropPath() != null) {
            Picasso.with(this)
                    .load(data.getBackdropPath(3))
                    .centerCrop()
                    .resize(Resources.getSystem().getDisplayMetrics().widthPixels, getResources().getDimensionPixelSize(R.dimen.image_description_header))
                    .into(imageHeader);
        }
        if (data.getPosterPath(3) != null) {
            Picasso.with(this)
                    .load(data.getPosterPath(3))
                    .placeholder(ResourceHelpers.getDrawable(this, R.drawable.ic_collections_white))
                    .resize(getResources().getDimensionPixelSize(R.dimen.image_description_thumbnail_width), 0)
                    .into(imageThumbnail);
        }
        textRating.setText(getResources().getString(R.string.rating_number, data.getVoteAverage()));
        textRating.setTextColor(getRatingColor(data.getVoteAverage()));
        if (data.getOriginalTitle() != null) textOriginalTitle.setText(data.getOriginalTitle());
        if (data.getReleaseDate() != null) {
            java.text.DateFormat dateFormat = android.text.format.DateFormat.getLongDateFormat(this);
            String stringDate = dateFormat.format(data.getReleaseDate());

            textReleaseDate.setText(getResources().getString(R.string.release_date, stringDate));
        }

        if (data.getOverview() != null) textSynopsis.setText(data.getOverview());
    }

    private void bindData() {
        fab.show();
        if (rootData.getVisibility() == View.GONE) rootData.setVisibility(View.VISIBLE);
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
        if (data.getTagline() != null) textTagline.setText(data.getTagline());
    }
}
