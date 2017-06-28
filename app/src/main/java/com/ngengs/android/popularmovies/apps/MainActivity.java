package com.ngengs.android.popularmovies.apps;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BaseTransientBottomBar;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ngengs.android.popularmovies.apps.adapters.MovieListAdapter;
import com.ngengs.android.popularmovies.apps.data.MoviesDetail;
import com.ngengs.android.popularmovies.apps.data.MoviesList;
import com.ngengs.android.popularmovies.apps.globals.Values;
import com.ngengs.android.popularmovies.apps.utils.GridSpacesItemDecoration;
import com.ngengs.android.popularmovies.apps.utils.MoviesDBService;
import com.ngengs.android.popularmovies.apps.utils.ResourceHelpers;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    @BindView(R.id.recyclerView)
    RecyclerView rv;
    @BindView(R.id.progressBar)
    ProgressBar progressBar;
    @BindView(R.id.textTools)
    TextView textMessage;
    @BindView(R.id.imageTools)
    ImageView imageTools;
    @BindView(R.id.tools)
    View tools;
    @BindView(R.id.swipeRefresh)
    SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.rootMainView)
    CoordinatorLayout rootMainView;

    private GridLayoutManager layoutManager;
    private Snackbar snackbar;

    private MoviesDBService moviesDBService;
    private MovieListAdapter adapter;
    private CompositeDisposable disposable = new CompositeDisposable();
    private int sortType;
    private int pageTotal = 1;
    private int pageNow = 0;
    private boolean forceRefresh;
    private boolean loading;
    private boolean fromPagination;
    private boolean changeData;
    private boolean processLoadData = true;
    private Consumer<MoviesList> moviesListConsumer = new Consumer<MoviesList>() {
        @Override
        public void accept(@io.reactivex.annotations.NonNull MoviesList moviesList) throws Exception {
            onResponse(moviesList);
        }
    };
    private Action actionComplete = new Action() {
        @Override
        public void run() throws Exception {
            onComplete();
        }
    };
    private Consumer<Throwable> errorConsumer = new Consumer<Throwable>() {
        @Override
        public void accept(@io.reactivex.annotations.NonNull Throwable throwable) throws Exception {
            onFailure(throwable);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        loading = false;
        fromPagination = false;
        changeData = true;

        // Make sure all view not visible
        rv.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);

        adapter = new MovieListAdapter(this, new MovieListAdapter.ClickListener() {
            @Override
            public void OnClickListener(int position) {
                Intent intent = new Intent(MainActivity.this, DetailMovieActivity.class);
                intent.putExtra("DATA", adapter.get(position));
                startActivity(intent);
            }
        });

        int gridSpan;
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
            gridSpan = 3;
        else gridSpan = 2;
        layoutManager = new GridLayoutManager(this, gridSpan);

        rv.setLayoutManager(layoutManager);
        rv.addItemDecoration(new GridSpacesItemDecoration(gridSpan, getResources().getDimensionPixelSize(R.dimen.grid_spacing)));
        rv.setAdapter(adapter);
        rv.setHasFixedSize(true);
        rv.setNestedScrollingEnabled(false);
        rv.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @SuppressWarnings("EmptyMethod")
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int visibleItemCount = layoutManager.getChildCount();
                int totalItemCount = layoutManager.getItemCount();
                int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();
                boolean endHasBeenReached = (firstVisibleItemPosition + visibleItemCount + 5) >= totalItemCount;

                if (!loading && pageNow < pageTotal && endHasBeenReached && !processLoadData) {
                    Log.d(TAG, "onScrolled: CatchData");
                    forceRefresh = false;
                    fromPagination = true;
                    if (sortType == Values.TYPE_POPULAR) getPopularMovies();
                    else getTopRatedMovies();
                }
            }
        });

        forceRefresh = false;
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                forceRefresh = true;
                pageNow = 0;
                pageTotal = 1;
                fromPagination = false;
                if (sortType == Values.TYPE_POPULAR) getPopularMovies();
                else getTopRatedMovies();
            }
        });

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Values.URL_BASE)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.createAsync())
                .build();
        moviesDBService = retrofit.create(MoviesDBService.class);
        Log.d(TAG, "onCreate: disposable isDiposed: " + disposable.isDisposed());

        sortType = Values.TYPE_POPULAR;
        if (savedInstanceState != null) {
            sortType = savedInstanceState.getInt("SORT_TYPE", Values.TYPE_POPULAR);
            pageNow = savedInstanceState.getInt("PAGE_NOW", 0);
            pageTotal = savedInstanceState.getInt("PAGE_TOTAL", 1);
            List<MoviesDetail> temp = savedInstanceState.getParcelableArrayList("DATA");
            if (temp != null) {
                adapter.clear();
                adapter.add(temp);
                rv.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
                tools.setVisibility(View.GONE);
                changeTitle();
            }
            processLoadData = savedInstanceState.getBoolean("PROCESS_LOAD_DATA", false);
            Log.d(TAG, "onCreate: load savedInstanceState: isProcessLoadData value " + processLoadData);
            Log.d(TAG, "onCreate: load savedInstanceState: sortType value " + (sortType == Values.TYPE_POPULAR));
            if (processLoadData) {
                if (pageNow == 0) adapter.clear();
                if (sortType == Values.TYPE_POPULAR) getPopularMovies();
                else getTopRatedMovies();
            }
        } else {
            getPopularMovies();
            changeTitle();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (disposable != null && !disposable.isDisposed()) {
            disposable.clear();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (adapter.getItemCount() > 0) {
            List<MoviesDetail> data = adapter.get();
            outState.putParcelableArrayList("DATA", new ArrayList<>(data));
            outState.putInt("PAGE_NOW", pageNow);
            outState.putInt("PAGE_TOTAL", pageTotal);
            outState.putInt("SORT_TYPE", sortType);
            outState.putBoolean("PROCESS_LOAD_DATA", processLoadData);
        }
    }

    private void changeTitle() {
        if (sortType == Values.TYPE_POPULAR)
            setTitle(getResources().getString(R.string.title_popular));
        else
            setTitle(getResources().getString(R.string.title_top_rated));
    }

    private void getPopularMovies() {
        if (moviesDBService != null && pageNow < pageTotal) {
            Log.d(TAG, "getPopularMovies: now. page: " + pageNow);
            loading = true;
            processLoadData = true;
            if (!forceRefresh || changeData) progressBar.setVisibility(View.VISIBLE);
            tools.setVisibility(View.GONE);

            disposable.add(
                    moviesDBService.listMoviesPopular(pageNow + 1)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribeOn(Schedulers.io())
                            .subscribe(moviesListConsumer, errorConsumer, actionComplete)
            );
        }
    }

    private void getTopRatedMovies() {
        if (moviesDBService != null && pageNow < pageTotal) {
            Log.d(TAG, "getTopRatedMovies: now. page: " + pageNow);
            loading = true;
            processLoadData = true;
            if (!forceRefresh || changeData) progressBar.setVisibility(View.VISIBLE);
            tools.setVisibility(View.GONE);

            disposable.add(
                    moviesDBService.listMoviesTopRated(pageNow + 1)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribeOn(Schedulers.io())
                            .subscribe(moviesListConsumer, errorConsumer, actionComplete)
            );
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_popular, menu);
        if (sortType == Values.TYPE_POPULAR)
            menu.findItem(R.id.menu_sort_by_popular).setChecked(true);
        else
            menu.findItem(R.id.menu_sort_by_top_rated).setChecked(true);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_sort_by_popular:
                forceRefresh = true;
                pageNow = 0;
                pageTotal = 1;
                sortType = Values.TYPE_POPULAR;
                loading = false;
                changeData = true;
                if (disposable != null && !disposable.isDisposed()) disposable.clear();
                if (!item.isChecked()) {
                    changeTitle();
                    getPopularMovies();
                }
                break;
            case R.id.menu_sort_by_top_rated:
                forceRefresh = true;
                pageNow = 0;
                pageTotal = 1;
                sortType = Values.TYPE_HIGH_RATED;
                loading = false;
                changeData = true;
                if (disposable != null && !disposable.isDisposed()) disposable.clear();
                if (!item.isChecked()) {
                    changeTitle();
                    getTopRatedMovies();
                }
                break;
        }
        item.setChecked(true);
        return super.onOptionsItemSelected(item);
    }

    private void onResponse(@NonNull MoviesList moviesList) {
        if (rv.getVisibility() == View.GONE) rv.setVisibility(View.VISIBLE);

        if (forceRefresh) adapter.clear();


        if (snackbar != null) {
            snackbar.dismiss();
            snackbar = null;
        }
        if (tools.getVisibility() == View.VISIBLE) tools.setVisibility(View.GONE);
        fromPagination = false;
        pageTotal = moviesList.getTotalPage();
        pageNow = moviesList.getPage();
        List<MoviesDetail> movies = moviesList.getMovies();
        if (moviesList.getStatusMessage() == null && movies.size() > 0) {
            adapter.add(movies);
        }
        Log.d(TAG, "onResponse: finish Response");
    }

    private void onComplete() {
        if (progressBar.getVisibility() == View.VISIBLE) progressBar.setVisibility(View.GONE);
        if (swipeRefreshLayout.isRefreshing()) swipeRefreshLayout.setRefreshing(false);
        if (forceRefresh) forceRefresh = false;

        processLoadData = false;
        changeData = false;
        loading = false;
        Log.d(TAG, "onComplete: finish complete");
    }

    private void onFailure(@NonNull Throwable t) {
        if (progressBar.getVisibility() == View.VISIBLE) progressBar.setVisibility(View.GONE);
        if (rv.getVisibility() == View.VISIBLE && adapter.getItemCount() == 0)
            rv.setVisibility(View.GONE);

        if (fromPagination) {
            imageTools.setImageDrawable(ResourceHelpers.getDrawable(this, R.drawable.ic_refresh_white));
            textMessage.setText(R.string.error_next_page);
        } else {
            imageTools.setImageDrawable(ResourceHelpers.getDrawable(this, R.drawable.ic_cloud_off_white));
            textMessage.setText(R.string.error_no_connection);
        }
        tools.setVisibility(View.VISIBLE);

        if (snackbar != null) snackbar.dismiss();
        snackbar = Snackbar.make(textMessage, R.string.error_cant_get_data_check_connection, BaseTransientBottomBar.LENGTH_INDEFINITE);
        snackbar.setAction(R.string.retry, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (sortType == Values.TYPE_POPULAR) getPopularMovies();
                else getTopRatedMovies();
            }
        });
        snackbar.show();
        Log.e(TAG, "onFailure: ", t);
    }

    @OnClick(R.id.imageTools)
    void imageToolsClick() {
        if (sortType == Values.TYPE_POPULAR) getPopularMovies();
        else getTopRatedMovies();
    }

}
