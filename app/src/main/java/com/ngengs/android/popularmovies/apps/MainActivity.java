package com.ngengs.android.popularmovies.apps;

import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
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
    CollapsingToolbarLayout toolbarDetail;
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
    @BindView(R.id.fabShare)
    FloatingActionButton fab;
    private ActionBar actionBar;

    private GridFragment gridFragment;
    private DetailMovieFragment detailMovieFragment;
    private FragmentManager fragmentManager;
    private boolean openDetail = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        Log.d(TAG, "onCreate: now");

        fragmentManager = getSupportFragmentManager();

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
    public void onFragmentClickMovies(MoviesDetail data) {
        if (!isMultiLayout()) {
            Intent intent = new Intent(MainActivity.this, DetailMovieActivity.class);
            intent.putExtra("DATA", data);
            startActivity(intent);
        } else {
            showMultiLayout(true);
            if (isMultiLayout() && detailFragmentLayout != null) {
                detailMovieFragment = DetailMovieFragment.newInstance(data);
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(detailFragmentLayout.getId(), detailMovieFragment);
                fragmentTransaction.commit();
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
        if (isMultiLayout() && fab != null) {
            fab.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onFragmentChangeTitle(@NonNull String title) {
        Log.d(TAG, "onFragmentChangeTitle: start");
        if (isMultiLayout() && toolbarDetail != null) {
            Log.d(TAG, "onFragmentChangeTitle: change to: " + title);
            toolbarDetail.clearFocus();
            toolbarDetail.destroyDrawingCache();
            toolbarDetail.setTitle(title);
            Log.d(TAG, "onFragmentChangeTitle: changed to: " + toolbarDetail.getTitle());
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

    @SuppressWarnings({"ConstantConditions", "unused"})
    @Optional
    @OnClick(R.id.detailToolbarButtonClose)
    void onCloseMultiLayout() {
        if (isMultiLayout()) {
            showMultiLayout(false);
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.remove(detailMovieFragment);
            fragmentTransaction.commit();
            detailHeaderImage.setImageResource(0);
            fab.setVisibility(View.GONE);
        }
    }
}
