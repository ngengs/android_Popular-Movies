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
import com.ngengs.android.popularmovies.apps.data.local.MoviesProviderHelper;
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
    RecyclerView mMoviesRecycler;
    @BindView(R.id.progressBar)
    ProgressBar mProgressBar;
    @BindView(R.id.textTools)
    TextView mTextMessage;
    @BindView(R.id.imageTools)
    ImageView mImageTools;
    @BindView(R.id.tools)
    View mTools;
    @BindView(R.id.swipeRefresh)
    SwipeRefreshLayout mSwipeRefreshLayout;
    private GridLayoutManager mLayoutManager;
    private GridSpacesItemDecoration mLayoutDecoration;
    private Snackbar mSnackbar;
    private MoviesAPI mMoviesAPI;
    private MovieListAdapter mAdapter;
    private Disposable mDisposable;
    private int mSortType;
    private int mPageTotal = 1;
    private int mPageNow = 0;
    private boolean mForceRefresh;
    private boolean mLoading;
    private boolean mFromPagination;
    private boolean mChangeData;
    private boolean mProcessLoadData = true;
    private final Action mActionComplete = new Action() {
        @Override
        public void run() throws Exception {
            onComplete();
        }
    };
    private Context mContext;
    private final Consumer<MoviesList> mMoviesListConsumer = new Consumer<MoviesList>() {
        @Override
        public void accept(@io.reactivex.annotations.NonNull MoviesList moviesList) throws
                Exception {
            onResponse(moviesList);
        }
    };
    private MoviesProviderHelper mMoviesProviderHelper;
    private final Consumer<Throwable> mErrorConsumer = new Consumer<Throwable>() {
        @Override
        public void accept(@io.reactivex.annotations.NonNull Throwable throwable) throws Exception {
            onFailure(throwable);
        }
    };
    private OnFragmentInteractionListener mListener;

    private Unbinder mUnbinder;

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

    @SuppressWarnings("EmptyMethod")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mSortType = getArguments().getInt("ARGS_SORT_TYPE", Values.TYPE_POPULAR);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_grid, container, false);
        mContext = view.getContext();
        mUnbinder = ButterKnife.bind(this, view);
        mMoviesProviderHelper = new MoviesProviderHelper(mContext);
        createLayout(savedInstanceState);
        if (mListener != null) mListener.onAttachHandler();
        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mAdapter.getItemCount() > 0) {
            List<MoviesDetail> data = mAdapter.get();
            outState.putParcelableArrayList("DATA", new ArrayList<>(data));
            outState.putInt("PAGE_NOW", mPageNow);
            outState.putInt("PAGE_TOTAL", mPageTotal);
            outState.putInt("SORT_TYPE", mSortType);
            outState.putBoolean("PROCESS_LOAD_DATA", mProcessLoadData);
        }
    }

    @SuppressWarnings("EmptyMethod")
    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mDisposable != null && !mDisposable.isDisposed()) mDisposable.dispose();
        if (mUnbinder != null) mUnbinder.unbind();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
        if (mDisposable != null && !mDisposable.isDisposed()) mDisposable.dispose();
    }

    public int getSortType() {
        return mSortType;
    }

    private void doMoviePressed(int position) {
        Log.d(TAG, "doMoviePressed: " + position);
        if (mListener != null) {
            if (mDisposable != null && !mDisposable.isDisposed()) {
                mDisposable.dispose();
                mDisposable = null;
                onComplete();
            }

            mListener.onFragmentClickMovies(position, mAdapter.get(position));
        } else throw new UnsupportedOperationException();
    }

    private void doChangeTitle() {
        Log.d(TAG, "doChangeTitle: " + mSortType);
        if (mListener != null) mListener.onFragmentChangeTitle(mSortType);
        else throw new UnsupportedOperationException();
    }

    private void createLayout(Bundle savedInstanceState) {
        Log.d(TAG, "createLayout: now");
        mLoading = false;
        mFromPagination = false;
        mChangeData = true;

        // Make sure all view not visible
        mMoviesRecycler.setVisibility(View.GONE);
        mProgressBar.setVisibility(View.GONE);

        mAdapter = new MovieListAdapter(mContext, new MovieListAdapter.ClickListener() {
            @Override
            public void OnClickListener(int position) {
                doMoviePressed(position);
            }
        });


        int gridSpan;
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            gridSpan = 4;
        } else gridSpan = 2;
        mLayoutManager = new GridLayoutManager(mContext, gridSpan);
        mLayoutDecoration = new GridSpacesItemDecoration(gridSpan,
                                                         getResources().getDimensionPixelSize(
                                                                 R.dimen.grid_spacing));

        mMoviesRecycler.setLayoutManager(mLayoutManager);
        mMoviesRecycler.addItemDecoration(mLayoutDecoration);
        mMoviesRecycler.setAdapter(mAdapter);
        mMoviesRecycler.setHasFixedSize(true);
        mMoviesRecycler.setNestedScrollingEnabled(false);
        mMoviesRecycler.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @SuppressWarnings("EmptyMethod")
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int visibleItemCount = mLayoutManager.getChildCount();
                int totalItemCount = mLayoutManager.getItemCount();
                int firstVisibleItemPosition = mLayoutManager.findFirstVisibleItemPosition();
                boolean endHasBeenReached =
                        (firstVisibleItemPosition + visibleItemCount + 5) >= totalItemCount;

                if (!mLoading && mPageNow < mPageTotal && endHasBeenReached && !mProcessLoadData) {
                    Log.d(TAG, "onScrolled: CatchData");
                    mForceRefresh = false;
                    mFromPagination = true;
                    if (mSortType == Values.TYPE_POPULAR) getPopularMovies();
                    else if (mSortType == Values.TYPE_TOP_RATED) getTopRatedMovies();
                }
            }
        });

        mForceRefresh = false;
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mForceRefresh = true;
                mPageNow = 0;
                mPageTotal = 1;
                mFromPagination = false;
                if (mSortType == Values.TYPE_POPULAR) getPopularMovies();
                else if (mSortType == Values.TYPE_TOP_RATED) getTopRatedMovies();
                else getFavoriteMovies();
            }
        });

        mMoviesAPI = NetworkHelpers.provideAPI();

        if (savedInstanceState != null) {
            mSortType = savedInstanceState.getInt("SORT_TYPE", Values.TYPE_POPULAR);
            mPageNow = savedInstanceState.getInt("PAGE_NOW", 0);
            mPageTotal = savedInstanceState.getInt("PAGE_TOTAL", 1);
            List<MoviesDetail> temp = savedInstanceState.getParcelableArrayList("DATA");
            if (temp != null) {
                mAdapter.clear();
                mAdapter.add(temp);
                mMoviesRecycler.setVisibility(View.VISIBLE);
                mProgressBar.setVisibility(View.GONE);
                mTools.setVisibility(View.GONE);
                doChangeTitle();
            }
            mProcessLoadData = savedInstanceState.getBoolean("PROCESS_LOAD_DATA", false);
            Log.d(TAG, "createLayout: load savedInstanceState: isProcessLoadData value " +
                    mProcessLoadData + ", mSortType is popular: " +
                    (mSortType == Values.TYPE_POPULAR));
            if (mProcessLoadData) {
                if (mPageNow == 0) mAdapter.clear();
                if (mSortType == Values.TYPE_POPULAR) getPopularMovies();
                else if (mSortType == Values.TYPE_TOP_RATED) getTopRatedMovies();
                else getFavoriteMovies();
            }
        } else {
            if (mSortType == Values.TYPE_POPULAR) {
                bindOldData();
                Log.d(TAG, "createLayout: catch new data");
                getPopularMovies();
            } else if (mSortType == Values.TYPE_TOP_RATED) {
                bindOldData();
                Log.d(TAG, "createLayout: catch new data");
                getPopularMovies();
            } else getFavoriteMovies();
            doChangeTitle();
        }
    }

    public void updateSpanColumn(int span) {
        if (mLayoutManager != null) mLayoutManager.setSpanCount(span);
        else mLayoutManager = new GridLayoutManager(mContext, span);
        mMoviesRecycler.setLayoutManager(mLayoutManager);
        mMoviesRecycler.removeItemDecoration(mLayoutDecoration);
        mLayoutDecoration = new GridSpacesItemDecoration(span, getResources().getDimensionPixelSize(
                R.dimen.grid_spacing));
        mMoviesRecycler.addItemDecoration(mLayoutDecoration);
    }

    public void scrollToPosition(int position) {
        mLayoutManager.scrollToPosition(position);
    }

    private void onResponse(@NonNull MoviesList moviesList) {
        if (mMoviesRecycler.getVisibility() == View.GONE) {
            mMoviesRecycler.setVisibility(View.VISIBLE);
        }

        if (mForceRefresh) mAdapter.clear();


        if (mSnackbar != null) {
            mSnackbar.dismiss();
            mSnackbar = null;
        }
        if (mTools.getVisibility() == View.VISIBLE) mTools.setVisibility(View.GONE);
        mFromPagination = false;
        mPageTotal = moviesList.getTotalPage();
        mPageNow = moviesList.getPage();
        List<MoviesDetail> movies = moviesList.getMovies();
        if (moviesList.getStatusMessage() == null && movies.size() > 0) {
            mAdapter.add(movies);
        }
        if (movies.size() == 0) {
            mTools.setVisibility(View.VISIBLE);
            mImageTools.setImageDrawable(
                    ResourceHelpers.getDrawable(mContext, R.drawable.ic_movie_white));
            mTextMessage.setText(R.string.data_empty);
        }
        Log.d(TAG, "onResponse: mPageNow: " + mPageNow + " mPageTotal: " + mPageTotal);
    }

    private void onComplete() {
        if (mProgressBar.getVisibility() == View.VISIBLE) mProgressBar.setVisibility(View.GONE);
        if (mSwipeRefreshLayout.isRefreshing()) mSwipeRefreshLayout.setRefreshing(false);
        if (mForceRefresh) mForceRefresh = false;

        mProcessLoadData = false;
        mChangeData = false;
        mLoading = false;
        Log.d(TAG, "onComplete: finish complete");
    }

    private void onFailure(@NonNull Throwable t) {
        if (mProgressBar.getVisibility() == View.VISIBLE) mProgressBar.setVisibility(View.GONE);
        if (mMoviesRecycler.getVisibility() == View.VISIBLE && mAdapter.getItemCount() == 0) {
            mMoviesRecycler.setVisibility(View.GONE);
        }

        if (mFromPagination) {
            mImageTools.setImageDrawable(
                    ResourceHelpers.getDrawable(mContext, R.drawable.ic_refresh_white));
            mTextMessage.setText(R.string.error_next_page);
            mTools.setVisibility(View.VISIBLE);
        } else if (mAdapter.getItemCount() == 0) {
            mImageTools.setImageDrawable(
                    ResourceHelpers.getDrawable(mContext, R.drawable.ic_cloud_off_white));
            mTextMessage.setText(R.string.error_no_connection);
            mTools.setVisibility(View.VISIBLE);
        }

        if (mSnackbar != null) mSnackbar.dismiss();

        mSnackbar = Snackbar.make(mTextMessage, R.string.error_cant_get_data_check_connection,
                                  BaseTransientBottomBar.LENGTH_INDEFINITE);
        mSnackbar.setAction(R.string.retry, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSortType == Values.TYPE_POPULAR) getPopularMovies();
                else if (mSortType == Values.TYPE_TOP_RATED) getTopRatedMovies();
                else getFavoriteMovies();
            }
        });
        mSnackbar.show();
        Log.e(TAG, "onFailure: ", t);
    }

    @OnClick(R.id.imageTools)
    void imageToolsClick() {
        if (mSortType == Values.TYPE_POPULAR) getPopularMovies();
        else if (mSortType == Values.TYPE_TOP_RATED) getTopRatedMovies();
        else getFavoriteMovies();
    }

    private void getPopularMovies() {
        Log.d(TAG, "getPopularMovies: mPageNow: " + mPageNow + " mPageTotal: " + mPageTotal);
        if (mMoviesAPI != null && mPageNow < mPageTotal) {
            mLoading = true;
            mProcessLoadData = true;

            if (!mForceRefresh || mChangeData) mProgressBar.setVisibility(View.VISIBLE);

            mTools.setVisibility(View.GONE);

            mDisposable = mMoviesAPI.listMoviesPopular(mPageNow + 1)
                    .subscribeOn(Schedulers.io())
                    .doOnNext(new Consumer<MoviesList>() {
                        @Override
                        public void accept(
                                @io.reactivex.annotations.NonNull MoviesList moviesList) throws
                                Exception {
                            mMoviesProviderHelper.saveMovies(moviesList.getMovies());
                            if (mPageNow == 0) {
                                mMoviesProviderHelper.deletePopular();
                                mMoviesProviderHelper.savePopular(moviesList.getMovies());
                            }
                        }
                    })
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(mMoviesListConsumer, mErrorConsumer, mActionComplete);
        }
    }

    private void getTopRatedMovies() {
        Log.d(TAG, "getTopRatedMovies: now. page: " + mPageNow);
        if (mMoviesAPI != null && mPageNow < mPageTotal) {
            mLoading = true;
            mProcessLoadData = true;

            if (!mForceRefresh || mChangeData) mProgressBar.setVisibility(View.VISIBLE);

            mTools.setVisibility(View.GONE);

            mDisposable = mMoviesAPI.listMoviesTopRated(mPageNow + 1)
                    .subscribeOn(Schedulers.io())
                    .doOnNext(new Consumer<MoviesList>() {
                        @Override
                        public void accept(
                                @io.reactivex.annotations.NonNull MoviesList moviesList) throws
                                Exception {
                            mMoviesProviderHelper.saveMovies(moviesList.getMovies());
                            if (mPageNow == 0) {
                                mMoviesProviderHelper.deleteTopRated();
                                mMoviesProviderHelper.saveTopRated(moviesList.getMovies());
                            }
                        }
                    })
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(mMoviesListConsumer, mErrorConsumer, mActionComplete);
        }
    }

    private void getFavoriteMovies() {
        Log.d(TAG, "getFavoriteMovies: now");
        mLoading = true;
        mProcessLoadData = true;

        if (mChangeData) mProgressBar.setVisibility(View.VISIBLE);

        mTools.setVisibility(View.GONE);

        mDisposable = Observable.fromPublisher(new Flowable<MoviesList>() {
            @Override
            protected void subscribeActual(Subscriber<? super MoviesList> s) {
                MoviesList movieList = mMoviesProviderHelper.getFavorites();
                if (movieList != null) s.onNext(movieList);
                else s.onError(null);
                s.onComplete();
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(mMoviesListConsumer, mErrorConsumer, mActionComplete);
    }

    public void changeType(int sortType) {
        this.mSortType = sortType;
        mForceRefresh = true;
        mPageNow = 0;
        mPageTotal = 1;
        mLoading = false;
        mChangeData = true;
        doChangeTitle();
        if (mDisposable != null && !mDisposable.isDisposed()) mDisposable.dispose();
        if (this.mSortType == Values.TYPE_POPULAR) {
            bindOldData();
            Log.d(TAG, "createLayout: catch new data");
            getPopularMovies();
        } else if (this.mSortType == Values.TYPE_TOP_RATED) {
            bindOldData();
            Log.d(TAG, "createLayout: catch new data");
            getTopRatedMovies();
        } else getFavoriteMovies();
    }

    private void bindOldData() {
        MoviesList temp = null;
        // Catch Offline data
        Log.d(TAG, "createLayout: catch old data");
        if (mSortType == Values.TYPE_POPULAR) temp = mMoviesProviderHelper.getPopular();
        else if (mSortType == Values.TYPE_TOP_RATED) temp = mMoviesProviderHelper.getTopRated();

        if (temp != null) onResponse(temp);
    }

    public void addMovies(MoviesDetail item) {
        mAdapter.add(item);
    }

    public void removeMovies(MoviesDetail item) {
        mAdapter.deleteById(item.getId());
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
