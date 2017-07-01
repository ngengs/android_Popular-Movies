package com.ngengs.android.popularmovies.apps;

import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.ngengs.android.popularmovies.apps.data.MoviesDetail;
import com.ngengs.android.popularmovies.apps.fragments.DetailMovieFragment;
import com.ngengs.android.popularmovies.apps.fragments.GridFragment;
import com.ngengs.android.popularmovies.apps.globals.Values;
import com.ngengs.android.popularmovies.apps.utils.ResourceHelpers;
import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Optional;

public class MainActivity extends AppCompatActivity implements GridFragment.OnFragmentInteractionListener, DetailMovieFragment.OnFragmentInteractionListener {
    private static final String TAG = "MainActivity";

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @Nullable
    @BindView(R.id.collapsingToolbar)
    CollapsingToolbarLayout toolbarDetailCollapsing;
    @Nullable
    @BindView(R.id.appbarDetail)
    AppBarLayout toolbarDetailAppBar;
    @Nullable
    @BindView(R.id.toolbarDetail)
    Toolbar toolbarDetail;
    @BindView(R.id.fragmentGrid)
    FrameLayout gridFragmentLayout;
    @Nullable
    @BindView(R.id.fragmentDetail)
    FrameLayout detailFragmentLayout;
    @Nullable
    @BindView(R.id.rootConstrain)
    View constrainRoot;
    @Nullable
    @BindView(R.id.rootDetailView)
    View detailRoot;
    @Nullable
    @BindView(R.id.guideline)
    View constrainGuideline;
    @Nullable
    @BindView(R.id.detailHeaderImage)
    ImageView detailHeaderImage;
    @Nullable
    @BindView(R.id.fabFavorite)
    FloatingActionButton fab;
    @Nullable
    @BindView(R.id.scrollDetail)
    NestedScrollView scrollDetail;
    private ActionBar actionBar;
    private Menu menuDetail;

    private GridFragment gridFragment;
    private DetailMovieFragment detailMovieFragment;
    private FragmentManager fragmentManager;
    private boolean openDetail = false;
    // TODO: remove temporary favorite detector
    private boolean moviesFavorite = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        Log.d(TAG, "onCreate: now");

        if (fragmentManager == null) fragmentManager = getSupportFragmentManager();

        if (gridFragmentLayout != null) {
            if (savedInstanceState == null) {
                Log.d(TAG, "onCreate: attach fragment");
                gridFragment = GridFragment.newInstance();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.add(gridFragmentLayout.getId(), gridFragment);
                fragmentTransaction.commit();
            } else {
                gridFragment = (GridFragment) fragmentManager.findFragmentById(gridFragmentLayout.getId());
            }
        }
        if (savedInstanceState != null) {
            openDetail = savedInstanceState.getBoolean("OPEN_DETAIL", false);
        }
    }

    private boolean isMultiLayout() {
        return constrainRoot != null && detailRoot != null && constrainGuideline != null;
    }

    @SuppressWarnings("ConstantConditions")
    private void createMultiLayout() {
        if (isMultiLayout()) {
            Log.d(TAG, "createMultiLayout: success");
            Log.d(TAG, "createMultiLayout: gridfragment status: " + (gridFragment != null));
            toolbarDetail.inflateMenu(R.menu.menu_detail);
            menuDetail = toolbarDetail.getMenu();
            menuDetail.findItem(R.id.menu_detail_close).setVisible(true);
            menuDetail.findItem(R.id.menu_detail_share).setVisible(false);
            toolbarDetail.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.menu_detail_close:
                            onCloseMultiLayout();
                            return true;
                        case R.id.menu_detail_share:
                            Log.d(TAG, "onMenuItemClick: Share");
                            return true;
                        default:
                            return false;
                    }
                }
            });
            if (openDetail) {
                showMultiLayout(true);
            } else {
                showMultiLayout(false);
            }
        }
    }

    @SuppressWarnings("ConstantConditions")
    private void showMultiLayout(boolean show) {
        if (isMultiLayout()) {
            ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) constrainGuideline.getLayoutParams();
            if (!show) {
                openDetail = false;
                detailRoot.setVisibility(View.GONE);
                params.guidePercent = 1f;
                if (gridFragment != null) gridFragment.updateSpanColumn(4);
            } else {
                openDetail = true;
                detailRoot.setVisibility(View.VISIBLE);
                if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
                    params.guidePercent = 0.35f;
                else
                    params.guidePercent = 0.5f;
                if (gridFragment != null) gridFragment.updateSpanColumn(2);
            }
            constrainGuideline.setLayoutParams(params);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("OPEN_DETAIL", openDetail);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_popular, menu);
        if (gridFragment.getSortType() == Values.TYPE_POPULAR)
            menu.findItem(R.id.menu_sort_by_popular).setChecked(true);
        else
            menu.findItem(R.id.menu_sort_by_top_rated).setChecked(true);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_sort_by_popular:
                gridFragment.changeType(Values.TYPE_POPULAR);
                break;
            case R.id.menu_sort_by_top_rated:
                gridFragment.changeType(Values.TYPE_HIGH_RATED);
                break;
        }
        item.setChecked(true);
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onFragmentClickMovies(int position, MoviesDetail data) {
        if (!isMultiLayout()) {
            Intent intent = new Intent(MainActivity.this, DetailMovieActivity.class);
            intent.putExtra("DATA", data);
            startActivity(intent);
        } else {
            showMultiLayout(true);
            if (isMultiLayout() && detailFragmentLayout != null) {
                boolean changeFragment = true;
                if (detailMovieFragment != null) {
                    // Check is fragment same as the clicked data
                    DetailMovieFragment temp = (DetailMovieFragment) fragmentManager.findFragmentById(detailFragmentLayout.getId());
                    Log.d(TAG, "onFragmentClickMovies: old id: " + temp.getMoviesId());
                    Log.d(TAG, "onFragmentClickMovies: new id: " + data.getId());
                    if (data.getId() == temp.getMoviesId()) changeFragment = false;
                }
                Log.d(TAG, "onFragmentClickMovies: can change: " + changeFragment);
                if (changeFragment) {
                    detailMovieFragment = DetailMovieFragment.newInstance(data);
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.replace(detailFragmentLayout.getId(), detailMovieFragment);
                    fragmentTransaction.commit();
                    if (toolbarDetailAppBar != null) toolbarDetailAppBar.setExpanded(true);
                    if (scrollDetail != null) scrollDetail.scrollTo(0, 0);
                    gridFragment.scrollToPosition(position);
                    moviesFavorite = false;
                }
            }
        }
    }

    @Override
    public void onFragmentChangeTitle(int sortType) {
        if (actionBar != null) {
            if (sortType == Values.TYPE_POPULAR)
                actionBar.setTitle(getResources().getString(R.string.title_popular));
            else
                actionBar.setTitle(getResources().getString(R.string.title_top_rated));
        }
    }

    @Override
    public void onAttachHandler() {
        createMultiLayout();
    }

    @Override
    public void onFragmentShowShare() {
        if (isMultiLayout()) {
            menuDetail.findItem(R.id.menu_detail_share).setVisible(true);
        }
    }

    @SuppressWarnings("ConstantConditions")
    @Optional
    @OnClick(R.id.fabFavorite)
    void onFavoriteClick() {
        if (isMultiLayout()) {
            moviesFavorite = !moviesFavorite;
            if (moviesFavorite)
                fab.setImageDrawable(ResourceHelpers.getDrawable(this, R.drawable.ic_favorite_white));
            else
                fab.setImageDrawable(ResourceHelpers.getDrawable(this, R.drawable.ic_favorite_border_white));
        }
    }

    @Override
    public void onFragmentChangeTitle(@NonNull String title) {
        Log.d(TAG, "onFragmentChangeTitle: start");
        if (isMultiLayout() && toolbarDetailCollapsing != null) {
            Log.d(TAG, "onFragmentChangeTitle: change to: " + title);
            toolbarDetailCollapsing.setTitle(title);
            Log.d(TAG, "onFragmentChangeTitle: changed to: " + toolbarDetailCollapsing.getTitle());
        }
    }

    @Override
    public void onFragmentChangeHeaderImage(@Nullable String imageUri) {
        if (isMultiLayout()) {
            Picasso.with(this)
                    .load(imageUri)
                    .centerCrop()
                    .resize(Resources.getSystem().getDisplayMetrics().widthPixels, getResources().getDimensionPixelSize(R.dimen.image_description_header))
                    .into(detailHeaderImage);
        }
    }

    @SuppressWarnings("ConstantConditions")
    void onCloseMultiLayout() {
        if (isMultiLayout()) {
            if (detailMovieFragment == null)
                detailMovieFragment = (DetailMovieFragment) fragmentManager.findFragmentById(R.id.fragmentDetail);

            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.remove(detailMovieFragment);
            fragmentTransaction.commit();
            detailHeaderImage.setImageResource(0);
            detailMovieFragment = null;
            showMultiLayout(false);
        }
    }
}
