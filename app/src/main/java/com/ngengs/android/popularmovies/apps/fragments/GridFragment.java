package com.ngengs.android.popularmovies.apps.fragments;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BaseTransientBottomBar;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ngengs.android.popularmovies.apps.R;
import com.ngengs.android.popularmovies.apps.adapters.MovieListAdapter;
import com.ngengs.android.popularmovies.apps.data.MoviesDetail;
import com.ngengs.android.popularmovies.apps.data.MoviesList;
import com.ngengs.android.popularmovies.apps.data.local.MoviesService;
import com.ngengs.android.popularmovies.apps.globals.Values;
import com.ngengs.android.popularmovies.apps.utils.GridSpacesItemDecoration;
import com.ngengs.android.popularmovies.apps.utils.MoviesAPI;
import com.ngengs.android.popularmovies.apps.utils.NetworkHelpers;
import com.ngengs.android.popularmovies.apps.utils.ResourceHelpers;

import org.reactivestreams.Subscriber;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link GridFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link GridFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class GridFragment extends Fragment {
    private static final String TAG = "GridFragment";
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
    private GridLayoutManager layoutManager;
    private Snackbar snackbar;
    private MoviesAPI moviesAPI;
    private MovieListAdapter adapter;
    private Disposable disposable;
    private int sortType;
    private int pageTotal = 1;
    private int pageNow = 0;
    private boolean forceRefresh;
    private boolean loading;
    private boolean fromPagination;
    private boolean changeData;
    private boolean processLoadData = true;
    private final Action actionComplete = new Action() {
        @Override
        public void run() throws Exception {
            onComplete();
        }
    };
    private Context context;
    private final Consumer<MoviesList> moviesListConsumer = new Consumer<MoviesList>() {
        @Override
        public void accept(@io.reactivex.annotations.NonNull MoviesList moviesList) throws Exception {
            onResponse(moviesList);
        }
    };
    private MoviesService moviesService;
    private final Consumer<Throwable> errorConsumer = new Consumer<Throwable>() {
        @Override
        public void accept(@io.reactivex.annotations.NonNull Throwable throwable) throws Exception {
            onFailure(throwable);
        }
    };
    private OnFragmentInteractionListener mListener;

    private Unbinder unbinder;

    public GridFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment GridFragment.
     */
    public static GridFragment newInstance(int sort) {
        GridFragment fragment = new GridFragment();
        Bundle args = new Bundle();
        args.putInt("ARGS_SORT_TYPE", sort);
        fragment.setArguments(args);
        return fragment;
    }

    @SuppressWarnings("EmptyMethod")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            sortType = getArguments().getInt("ARGS_SORT_TYPE", Values.TYPE_POPULAR);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_grid, container, false);
        context = view.getContext();
        unbinder = ButterKnife.bind(this, view);
        moviesService = new MoviesService(context);
        createLayout(savedInstanceState);
        if (mListener != null) mListener.onAttachHandler();
        return view;
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
    public void onDetach() {
        super.onDetach();
        mListener = null;
        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
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

    public int getSortType() {
        return sortType;
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

    private void doMoviePressed(int position) {
        Log.d(TAG, "doMoviePressed: " + position);
        if (mListener != null) {
            if (disposable != null && !disposable.isDisposed()) {
                disposable.dispose();
                disposable = null;
                onComplete();
            }

            mListener.onFragmentClickMovies(position, adapter.get(position));
        } else throw new UnsupportedOperationException();

    }

    private void doChangeTitle() {
        Log.d(TAG, "doChangeTitle: " + sortType);
        if (mListener != null) {
            mListener.onFragmentChangeTitle(sortType);
        } else throw new UnsupportedOperationException();
    }

    private void createLayout(Bundle savedInstanceState) {
        Log.d(TAG, "createLayout: now");
        loading = false;
        fromPagination = false;
        changeData = true;

        // Make sure all view not visible
        rv.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);

        adapter = new MovieListAdapter(context, new MovieListAdapter.ClickListener() {
            @Override
            public void OnClickListener(int position) {
                doMoviePressed(position);
            }
        });


        int gridSpan;
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
            gridSpan = 4;
        else gridSpan = 2;
        layoutManager = new GridLayoutManager(context, gridSpan);

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
                    else if (sortType == Values.TYPE_HIGH_RATED) getTopRatedMovies();
                    else getFavoriteMovies();
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
                else if (sortType == Values.TYPE_HIGH_RATED) getTopRatedMovies();
                else getFavoriteMovies();
            }
        });

        moviesAPI = NetworkHelpers.provideAPI();

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
                doChangeTitle();
            }
            processLoadData = savedInstanceState.getBoolean("PROCESS_LOAD_DATA", false);
            Log.d(TAG, "createLayout: load savedInstanceState: isProcessLoadData value " + processLoadData);
            Log.d(TAG, "createLayout: load savedInstanceState: sortType value " + (sortType == Values.TYPE_POPULAR));
            if (processLoadData) {
                if (pageNow == 0) adapter.clear();
                if (sortType == Values.TYPE_POPULAR) getPopularMovies();
                else if (sortType == Values.TYPE_HIGH_RATED) getTopRatedMovies();
                else getFavoriteMovies();
            }
        } else {
            if (sortType == Values.TYPE_POPULAR) {
                bindOldData();
                Log.d(TAG, "createLayout: catch new data");
                getPopularMovies();
            } else if (sortType == Values.TYPE_HIGH_RATED) {
                bindOldData();
                Log.d(TAG, "createLayout: catch new data");
                getPopularMovies();
            } else getFavoriteMovies();
            doChangeTitle();
        }
    }

    public void updateSpanColumn(int span) {
        if (layoutManager != null) layoutManager.setSpanCount(span);
        else layoutManager = new GridLayoutManager(context, span);
        rv.setLayoutManager(layoutManager);
    }

    public void scrollToPosition(int position) {
        layoutManager.scrollToPosition(position);
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
        if (movies.size() == 0) {
            tools.setVisibility(View.VISIBLE);
            imageTools.setImageDrawable(ResourceHelpers.getDrawable(context, R.drawable.ic_refresh_white));
            textMessage.setText(R.string.data_empty);
        }
        Log.d(TAG, "onResponse: pageNow: " + pageNow + " pageTotal: " + pageTotal);
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
            imageTools.setImageDrawable(ResourceHelpers.getDrawable(context, R.drawable.ic_refresh_white));
            textMessage.setText(R.string.error_next_page);
            tools.setVisibility(View.VISIBLE);
        } else if (adapter.getItemCount() == 0) {
            imageTools.setImageDrawable(ResourceHelpers.getDrawable(context, R.drawable.ic_cloud_off_white));
            textMessage.setText(R.string.error_no_connection);
            tools.setVisibility(View.VISIBLE);
        }

        if (snackbar != null) snackbar.dismiss();
        snackbar = Snackbar.make(textMessage, R.string.error_cant_get_data_check_connection, BaseTransientBottomBar.LENGTH_INDEFINITE);
        snackbar.setAction(R.string.retry, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refresh();
            }
        });
        snackbar.show();
        Log.e(TAG, "onFailure: ", t);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @OnClick(R.id.imageTools)
    void imageToolsClick() {
        if (sortType == Values.TYPE_POPULAR) getPopularMovies();
        else if (sortType == Values.TYPE_HIGH_RATED) getTopRatedMovies();
        else getFavoriteMovies();
    }

    private void getPopularMovies() {
        Log.d(TAG, "getPopularMovies: pageNow: " + pageNow + " pageTotal: " + pageTotal);
        if (moviesAPI != null && pageNow < pageTotal) {
            Log.d(TAG, "getPopularMovies: now. page: " + pageNow);
            loading = true;
            processLoadData = true;
            if (!forceRefresh || changeData) progressBar.setVisibility(View.VISIBLE);
            tools.setVisibility(View.GONE);

            Log.d(TAG, "getPopularMovies: page: " + pageNow);
            disposable = moviesAPI.listMoviesPopular(pageNow + 1)
                    .subscribeOn(Schedulers.io())
                    .doOnNext(new Consumer<MoviesList>() {
                        @Override
                        public void accept(@io.reactivex.annotations.NonNull MoviesList moviesList) throws Exception {
                            Log.d(TAG, "getPopularMovies: page: " + pageNow);
                            moviesService.saveMovies(moviesList.getMovies());
                            if (pageNow == 0) {
                                moviesService.deletePopular();
                                moviesService.savePopular(moviesList.getMovies());
                            }
                        }
                    })
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(moviesListConsumer, errorConsumer, actionComplete);
        }
    }

    private void getTopRatedMovies() {
        if (moviesAPI != null && pageNow < pageTotal) {
            Log.d(TAG, "getTopRatedMovies: now. page: " + pageNow);
            loading = true;
            processLoadData = true;
            if (!forceRefresh || changeData) progressBar.setVisibility(View.VISIBLE);
            tools.setVisibility(View.GONE);

            disposable = moviesAPI.listMoviesTopRated(pageNow + 1)
                    .subscribeOn(Schedulers.io())
                    .doOnNext(new Consumer<MoviesList>() {
                        @Override
                        public void accept(@io.reactivex.annotations.NonNull MoviesList moviesList) throws Exception {
                            Log.d(TAG, "getTopRatedMovies: page: " + pageNow);
                            moviesService.saveMovies(moviesList.getMovies());
                            if (pageNow == 0) {
                                moviesService.deleteTopRated();
                                moviesService.saveTopRated(moviesList.getMovies());
                            }
                        }
                    })
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(moviesListConsumer, errorConsumer, actionComplete);
        }
    }

    private void getFavoriteMovies() {
        Log.d(TAG, "getFavoriteMovies: now");
        loading = true;
        processLoadData = true;
        if (changeData) progressBar.setVisibility(View.VISIBLE);
        tools.setVisibility(View.GONE);

        disposable = Observable.fromPublisher(new Flowable<MoviesList>() {
            @Override
            protected void subscribeActual(Subscriber<? super MoviesList> s) {
                MoviesList movieList = moviesService.getFavorites();
                if (movieList != null) s.onNext(movieList);
                else s.onError(null);
                s.onComplete();
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(moviesListConsumer, errorConsumer, actionComplete);
    }

    public void changeType(int sortType) {
        this.sortType = sortType;
        forceRefresh = true;
        pageNow = 0;
        pageTotal = 1;
        loading = false;
        changeData = true;
        doChangeTitle();
        if (disposable != null && !disposable.isDisposed()) disposable.dispose();
        if (this.sortType == Values.TYPE_POPULAR) {
            bindOldData();
            Log.d(TAG, "createLayout: catch new data");
            getPopularMovies();
        } else if (this.sortType == Values.TYPE_HIGH_RATED) {
            bindOldData();
            Log.d(TAG, "createLayout: catch new data");
            getTopRatedMovies();
        } else getFavoriteMovies();
    }

    private void bindOldData() {
        MoviesList temp = null;
        // Catch Offline data
        Log.d(TAG, "createLayout: catch old data");
        if (sortType == Values.TYPE_POPULAR) temp = moviesService.getPopular();
        else if (sortType == Values.TYPE_HIGH_RATED) temp = moviesService.getTopRated();

        if (temp != null) onResponse(temp);
    }

    public void addMovies(MoviesDetail item) {
        adapter.add(item);
    }

    public void removeMovies(MoviesDetail item) {
        adapter.deleteById(item.getId());
    }

    public void refresh() {
        if (sortType == Values.TYPE_POPULAR) getPopularMovies();
        else if (sortType == Values.TYPE_HIGH_RATED) getTopRatedMovies();
        else getFavoriteMovies();
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
        void onFragmentClickMovies(int position, MoviesDetail data);

        void onFragmentChangeTitle(int sortType);

        void onAttachHandler();
    }

}
