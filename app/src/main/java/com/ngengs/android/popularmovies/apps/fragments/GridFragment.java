package com.ngengs.android.popularmovies.apps.fragments;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import androidx.annotation.NonNull;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ngengs.android.popularmovies.apps.R;
import com.ngengs.android.popularmovies.apps.adapters.MovieListAdapter;
import com.ngengs.android.popularmovies.apps.data.MoviesDetail;
import com.ngengs.android.popularmovies.apps.data.MoviesList;
import com.ngengs.android.popularmovies.apps.data.local.MoviesProviderHelper;
import com.ngengs.android.popularmovies.apps.databinding.FragmentGridBinding;
import com.ngengs.android.popularmovies.apps.globals.Values;
import com.ngengs.android.popularmovies.apps.utils.GridSpacesItemDecoration;
import com.ngengs.android.popularmovies.apps.utils.MoviesAPI;
import com.ngengs.android.popularmovies.apps.utils.NetworkHelpers;
import com.ngengs.android.popularmovies.apps.utils.ResourceHelpers;

import org.reactivestreams.Subscriber;

import java.util.ArrayList;
import java.util.List;

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
    FragmentGridBinding binding;
    private GridLayoutManager layoutManager;
    private GridSpacesItemDecoration layoutDecoration;
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
    private final Action actionComplete = this::onComplete;
    private Context context;
    private final Consumer<MoviesList> moviesListConsumer = this::onResponse;
    private MoviesProviderHelper moviesProviderHelper;
    private final Consumer<Throwable> errorConsumer = this::onFailure;
    private OnFragmentInteractionListener mListener;

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
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentGridBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        context = view.getContext();
        moviesProviderHelper = new MoviesProviderHelper(context);
        createLayout(savedInstanceState);
        if (mListener != null) mListener.onAttachHandler();
        initializeViewAction();
        return view;
    }

    private void initializeViewAction() {
        binding.imageTools.setOnClickListener(view -> imageToolsClick());
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context + " must implement OnFragmentInteractionListener");
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
        binding = null;
    }

    public int getSortType() {
        return sortType;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
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
        binding.recyclerView.setVisibility(View.GONE);
        binding.progressBar.setVisibility(View.GONE);

        adapter = new MovieListAdapter(context, this::doMoviePressed);


        int gridSpan;
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
            gridSpan = 4;
        else gridSpan = 2;
        layoutManager = new GridLayoutManager(context, gridSpan);
        layoutDecoration = new GridSpacesItemDecoration(gridSpan, getResources().getDimensionPixelSize(R.dimen.grid_spacing));

        binding.recyclerView.setLayoutManager(layoutManager);
        binding.recyclerView.addItemDecoration(layoutDecoration);
        binding.recyclerView.setAdapter(adapter);
        binding.recyclerView.setHasFixedSize(true);
        binding.recyclerView.setNestedScrollingEnabled(false);
        binding.recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @SuppressWarnings("EmptyMethod")
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
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
                }
            }
        });

        forceRefresh = false;
        binding.swipeRefresh.setOnRefreshListener(() -> {
            forceRefresh = true;
            pageNow = 0;
            pageTotal = 1;
            fromPagination = false;
            if (sortType == Values.TYPE_POPULAR) getPopularMovies();
            else if (sortType == Values.TYPE_HIGH_RATED) getTopRatedMovies();
            else getFavoriteMovies();
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
                binding.recyclerView.setVisibility(View.VISIBLE);
                binding.progressBar.setVisibility(View.GONE);
                binding.tools.setVisibility(View.GONE);
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
        binding.recyclerView.setLayoutManager(layoutManager);
        binding.recyclerView.removeItemDecoration(layoutDecoration);
        layoutDecoration = new GridSpacesItemDecoration(span, getResources().getDimensionPixelSize(R.dimen.grid_spacing));
        binding.recyclerView.addItemDecoration(layoutDecoration);
    }

    public void scrollToPosition(int position) {
        layoutManager.scrollToPosition(position);
    }

    private void onResponse(@NonNull MoviesList moviesList) {
        if (binding.recyclerView.getVisibility() == View.GONE) binding.recyclerView.setVisibility(View.VISIBLE);

        if (forceRefresh) adapter.clear();


        if (snackbar != null) {
            snackbar.dismiss();
            snackbar = null;
        }
        if (binding.tools.getVisibility() == View.VISIBLE) binding.tools.setVisibility(View.GONE);
        fromPagination = false;
        pageTotal = moviesList.getTotalPage();
        pageNow = moviesList.getPage();
        List<MoviesDetail> movies = moviesList.getMovies();
        if (moviesList.getStatusMessage() == null && movies.size() > 0) {
            adapter.add(movies);
        }
        if (movies.size() == 0) {
            binding.tools.setVisibility(View.VISIBLE);
            binding.imageTools.setImageDrawable(ResourceHelpers.getDrawable(context, R.drawable.ic_movie_white));
            binding.textTools.setText(R.string.data_empty);
        }
        Log.d(TAG, "onResponse: pageNow: " + pageNow + " pageTotal: " + pageTotal);
        Log.d(TAG, "onResponse: finish Response");
    }

    private void onComplete() {
        if (binding.progressBar.getVisibility() == View.VISIBLE) binding.progressBar.setVisibility(View.GONE);
        if (binding.swipeRefresh.isRefreshing()) binding.swipeRefresh.setRefreshing(false);
        if (forceRefresh) forceRefresh = false;

        processLoadData = false;
        changeData = false;
        loading = false;
        Log.d(TAG, "onComplete: finish complete");
    }

    private void onFailure(@NonNull Throwable t) {
        if (binding.progressBar.getVisibility() == View.VISIBLE) binding.progressBar.setVisibility(View.GONE);
        if (binding.recyclerView.getVisibility() == View.VISIBLE && adapter.getItemCount() == 0)
            binding.recyclerView.setVisibility(View.GONE);

        if (fromPagination) {
            binding.imageTools.setImageDrawable(ResourceHelpers.getDrawable(context, R.drawable.ic_refresh_white));
            binding.textTools.setText(R.string.error_next_page);
            binding.tools.setVisibility(View.VISIBLE);
        } else if (adapter.getItemCount() == 0) {
            binding.imageTools.setImageDrawable(ResourceHelpers.getDrawable(context, R.drawable.ic_cloud_off_white));
            binding.textTools.setText(R.string.error_no_connection);
            binding.tools.setVisibility(View.VISIBLE);
        }

        if (snackbar != null) snackbar.dismiss();
        snackbar = Snackbar.make(binding.textTools, R.string.error_cant_get_data_check_connection, BaseTransientBottomBar.LENGTH_INDEFINITE);
        snackbar.setAction(R.string.retry, v -> {
            if (sortType == Values.TYPE_POPULAR) getPopularMovies();
            else if (sortType == Values.TYPE_HIGH_RATED) getTopRatedMovies();
            else getFavoriteMovies();
        });
        snackbar.show();
        Log.e(TAG, "onFailure: ", t);
    }

    @SuppressWarnings("EmptyMethod")
    @Override
    public void onPause() {
        super.onPause();
    }

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
            if (!forceRefresh || changeData) binding.progressBar.setVisibility(View.VISIBLE);
            binding.tools.setVisibility(View.GONE);

            Log.d(TAG, "getPopularMovies: page: " + pageNow);
            disposable = moviesAPI.listMoviesPopular(pageNow + 1)
                    .subscribeOn(Schedulers.io())
                    .doOnNext(moviesList -> {
                        Log.d(TAG, "getPopularMovies: page: " + pageNow);
                        moviesProviderHelper.saveMovies(moviesList.getMovies());
                        if (pageNow == 0) {
                            moviesProviderHelper.deletePopular();
                            moviesProviderHelper.savePopular(moviesList.getMovies());
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
            if (!forceRefresh || changeData) binding.progressBar.setVisibility(View.VISIBLE);
            binding.tools.setVisibility(View.GONE);

            disposable = moviesAPI.listMoviesTopRated(pageNow + 1)
                    .subscribeOn(Schedulers.io())
                    .doOnNext(moviesList -> {
                        Log.d(TAG, "getTopRatedMovies: page: " + pageNow);
                        moviesProviderHelper.saveMovies(moviesList.getMovies());
                        if (pageNow == 0) {
                            moviesProviderHelper.deleteTopRated();
                            moviesProviderHelper.saveTopRated(moviesList.getMovies());
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
        if (changeData) binding.progressBar.setVisibility(View.VISIBLE);
        binding.tools.setVisibility(View.GONE);

        disposable = Observable.fromPublisher(new Flowable<MoviesList>() {
            @Override
            protected void subscribeActual(Subscriber<? super MoviesList> s) {
                MoviesList movieList = moviesProviderHelper.getFavorites();
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
        if (sortType == Values.TYPE_POPULAR) temp = moviesProviderHelper.getPopular();
        else if (sortType == Values.TYPE_HIGH_RATED) temp = moviesProviderHelper.getTopRated();

        if (temp != null) onResponse(temp);
    }

    public void addMovies(MoviesDetail item) {
        adapter.add(item);
    }

    public void removeMovies(MoviesDetail item) {
        adapter.deleteById(item.getId());
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
