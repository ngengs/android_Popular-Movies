package com.ngengs.android.popularmovies.apps;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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

public class MainActivity extends AppCompatActivity
        implements GridFragment.OnFragmentInteractionListener, DetailMovieFragment.OnFragmentInteractionListener {
    private static final String TAG = "MainActivity";
    private static final int RESULT_DETAIL = 10;
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @Nullable
    @BindView(R.id.collapsingToolbar)
    CollapsingToolbarLayout mToolbarDetailCollapsing;
    @Nullable
    @BindView(R.id.appbarDetail)
    AppBarLayout mToolbarDetailAppBar;
    @Nullable
    @BindView(R.id.toolbarDetail)
    Toolbar mToolbarDetail;
    @BindView(R.id.fragmentGrid)
    FrameLayout mGridFragmentLayout;
    @Nullable
    @BindView(R.id.fragmentDetail)
    FrameLayout mDetailFragmentLayout;
    @Nullable
    @BindView(R.id.rootConstrain)
    View mConstrainRoot;
    @Nullable
    @BindView(R.id.rootDetailView)
    View mDetailRoot;
    @Nullable
    @BindView(R.id.guideline)
    View mConstrainGuideline;
    @Nullable
    @BindView(R.id.detailHeaderImage)
    ImageView mDetailHeaderImage;
    @Nullable
    @BindView(R.id.fabFavorite)
    FloatingActionButton mFabFavorite;
    @Nullable
    @BindView(R.id.scrollDetail)
    NestedScrollView mScrollDetail;
    private ActionBar mActionBar;
    private Menu mMenuDetail;
    private GridFragment mGridFragment;
    private DetailMovieFragment mDetailMovieFragment;
    private FragmentManager mFragmentManager;
    private SharedPreferences mSharedPref;
    private boolean mOpenDetail = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);
        mActionBar = getSupportActionBar();
        Log.d(TAG, "onCreate: now");

        mSharedPref = getPreferences(Context.MODE_PRIVATE);
        int sortType = mSharedPref.getInt("SORT_TYPE_NOW", Values.TYPE_POPULAR);
        switch (sortType) {
            case Values.TYPE_POPULAR:
                break;
            case Values.TYPE_TOP_RATED:
                break;
            case Values.TYPE_FAVORITE:
                break;
            default:
                sortType = Values.TYPE_POPULAR;
        }

        if (mFragmentManager == null) mFragmentManager = getSupportFragmentManager();

        if (mGridFragmentLayout != null) {
            if (savedInstanceState == null) {
                Log.d(TAG, "onCreate: attach fragment");
                mGridFragment = GridFragment.newInstance(sortType);
                FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
                fragmentTransaction.add(mGridFragmentLayout.getId(), mGridFragment);
                fragmentTransaction.commit();
            } else {
                mGridFragment = (GridFragment) mFragmentManager.findFragmentById(
                        mGridFragmentLayout.getId());
                if (mDetailFragmentLayout != null) {
                    if (mFragmentManager.findFragmentById(mDetailFragmentLayout.getId()) != null) {
                        mDetailMovieFragment =
                                (DetailMovieFragment) mFragmentManager.findFragmentById(
                                        mDetailFragmentLayout.getId());
                    }
                }
            }
        }
        if (savedInstanceState != null) {
            mOpenDetail = savedInstanceState.getBoolean("OPEN_DETAIL", false);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("OPEN_DETAIL", mOpenDetail);
    }

    private boolean isMultiLayout() {
        return mConstrainRoot != null && mDetailRoot != null && mConstrainGuideline != null;
    }

    @SuppressWarnings("ConstantConditions")
    private void createMultiLayout() {
        if (isMultiLayout()) {
            Log.d(TAG, "createMultiLayout: success");
            mToolbarDetail.inflateMenu(R.menu.menu_detail);
            mMenuDetail = mToolbarDetail.getMenu();
            mMenuDetail.findItem(R.id.menu_detail_close).setVisible(true);
            mMenuDetail.findItem(R.id.menu_detail_share).setVisible(false);
            mToolbarDetail.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.menu_detail_close:
                            onCloseMultiLayout();
                            return true;
                        case R.id.menu_detail_share:
                            Log.d(TAG, "onMenuItemClick: Share");
                            Intent sendIntent = new Intent();
                            sendIntent.setAction(Intent.ACTION_SEND);
                            sendIntent.putExtra(Intent.EXTRA_TEXT,
                                                mDetailMovieFragment.getShareContent());
                            sendIntent.setType("text/plain");
                            startActivity(Intent.createChooser(sendIntent, getResources().getText(
                                    R.string.send_to)));
                            return true;
                        default:
                            return false;
                    }
                }
            });

            if (mOpenDetail) showMultiLayout(true);
            else showMultiLayout(false);

        }
    }

    @SuppressWarnings("ConstantConditions")
    private void showMultiLayout(boolean show) {
        if (isMultiLayout()) {
            ConstraintLayout.LayoutParams params =
                    (ConstraintLayout.LayoutParams) mConstrainGuideline
                            .getLayoutParams();
            if (!show) {
                mOpenDetail = false;
                mDetailRoot.setVisibility(View.GONE);
                params.guidePercent = 1f;
                if (mGridFragment != null) mGridFragment.updateSpanColumn(4);
            } else {
                mOpenDetail = true;
                mDetailRoot.setVisibility(View.VISIBLE);
                if (getResources().getConfiguration().orientation ==
                        Configuration.ORIENTATION_LANDSCAPE) {
                    params.guidePercent = 0.35f;
                } else {
                    params.guidePercent = 0.5f;
                }
                if (mGridFragment != null) mGridFragment.updateSpanColumn(2);
            }
            mConstrainGuideline.setLayoutParams(params);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_popular, menu);
        if (mGridFragment.getSortType() == Values.TYPE_POPULAR) {
            menu.findItem(R.id.menu_sort_by_popular).setChecked(true);
        } else if (mGridFragment.getSortType() == Values.TYPE_TOP_RATED) {
            menu.findItem(R.id.menu_sort_by_top_rated).setChecked(true);
        } else menu.findItem(R.id.menu_sort_by_favorite).setChecked(true);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int sortType = -1;
        switch (item.getItemId()) {
            case R.id.menu_sort_by_popular:
                sortType = Values.TYPE_POPULAR;
                break;
            case R.id.menu_sort_by_top_rated:
                sortType = Values.TYPE_TOP_RATED;
                break;
            case R.id.menu_sort_by_favorite:
                sortType = Values.TYPE_FAVORITE;
                break;
        }
        if (sortType > -1) {
            mGridFragment.changeType(sortType);
            item.setChecked(true);
            SharedPreferences.Editor shEditor = mSharedPref.edit();
            shEditor.putInt("SORT_TYPE_NOW", sortType);
            shEditor.apply();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onFragmentClickMovies(int position, MoviesDetail data) {
        if (!isMultiLayout()) {
            Intent intent = new Intent(MainActivity.this, DetailMovieActivity.class);
            intent.putExtra("DATA", data);
            startActivityForResult(intent, RESULT_DETAIL);
        } else {
            showMultiLayout(true);
            if (isMultiLayout() && mDetailFragmentLayout != null) {
                boolean changeFragment = true;
                if (mDetailMovieFragment != null) {
                    // Check is fragment same as the clicked data
                    DetailMovieFragment temp =
                            (DetailMovieFragment) mFragmentManager.findFragmentById(
                                    mDetailFragmentLayout.getId());
                    Log.d(TAG,
                          "onFragmentClickMovies: old id: " + temp.getMoviesId() + " new id: " +
                                  data.getId());
                    if (data.getId() == temp.getMoviesId()) changeFragment = false;
                }
                if (changeFragment) {
                    // Clear button favorite
                    onFragmentChangeFavorite(null, false, false);
                    mDetailMovieFragment = DetailMovieFragment.newInstance(data);
                    FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
                    fragmentTransaction.replace(mDetailFragmentLayout.getId(),
                                                mDetailMovieFragment);
                    fragmentTransaction.commit();
                    if (mToolbarDetailAppBar != null) mToolbarDetailAppBar.setExpanded(true);
                    if (mScrollDetail != null) mScrollDetail.scrollTo(0, 0);
                    mGridFragment.scrollToPosition(position);
                }
            }
        }
    }

    @Override
    public void onFragmentChangeTitle(int sortType) {
        if (mActionBar != null) {
            if (sortType == Values.TYPE_POPULAR) {
                mActionBar.setTitle(getResources().getString(R.string.title_popular));
            } else if (sortType == Values.TYPE_TOP_RATED) {
                mActionBar.setTitle(getResources().getString(R.string.title_top_rated));
            } else mActionBar.setTitle(R.string.title_favorite);
        }
    }

    @Override
    public void onAttachHandler() {
        createMultiLayout();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_DETAIL) {
            if (mGridFragment.getSortType() == Values.TYPE_FAVORITE) {
                Log.d(TAG, "onActivityResult: Refresh the favorite");
                mGridFragment.changeType(Values.TYPE_FAVORITE);
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (isMultiLayout() && mFragmentManager != null) onCloseMultiLayout();
        else super.onBackPressed();
    }

    @SuppressWarnings("EmptyMethod")
    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onFragmentShowShare() {
        if (isMultiLayout()) {
            mMenuDetail.findItem(R.id.menu_detail_share).setVisible(true);
        }
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onFragmentChangeFavorite(MoviesDetail data, boolean isFavorite, boolean isRefresh) {
        if (isMultiLayout()) {
            Log.d(TAG, "onFragmentChangeFavorite: now");
            if (isFavorite)
                mFabFavorite.setImageDrawable(
                        ResourceHelpers.getDrawable(this, R.drawable.ic_favorite_white));
            else
                mFabFavorite.setImageDrawable(
                        ResourceHelpers.getDrawable(this, R.drawable.ic_favorite_border_white));
            if (mGridFragment.getSortType() == Values.TYPE_FAVORITE && isRefresh) {
                if (data != null) {
                    if (isFavorite) mGridFragment.addMovies(data);
                    else mGridFragment.removeMovies(data);
                }
            }
        }
    }

    @Override
    public void onFragmentChangeTitle(@NonNull String title) {
        if (isMultiLayout() && mToolbarDetailCollapsing != null) {
            Log.d(TAG, "onFragmentChangeTitle: change to: " + title);
            mToolbarDetailCollapsing.setTitle(title);
        }
    }

    @Override
    public void onFragmentChangeHeaderImage(@Nullable String imageUri) {
        if (isMultiLayout()) {
            Picasso.with(this)
                    .load(imageUri)
                    .centerCrop()
                    .resize(Resources.getSystem().getDisplayMetrics().widthPixels,
                            getResources().getDimensionPixelSize(R.dimen.image_description_header))
                    .into(mDetailHeaderImage);
        }
    }

    @SuppressWarnings("ConstantConditions")
    @Optional
    @OnClick(R.id.fabFavorite)
    void onFavoriteClick() {
        if (isMultiLayout()) {
            Log.d(TAG, "onFavoriteClick: now");
            if (mDetailMovieFragment != null) mDetailMovieFragment.changeFavorite();
        }
    }

    @SuppressWarnings("ConstantConditions")
    private void onCloseMultiLayout() {
        if (isMultiLayout()) {
            if (mDetailMovieFragment == null)
                mDetailMovieFragment = (DetailMovieFragment) mFragmentManager.findFragmentById(
                        R.id.fragmentDetail);

            FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
            fragmentTransaction.remove(mDetailMovieFragment);
            fragmentTransaction.commit();
            mDetailHeaderImage.setImageResource(0);
            mDetailMovieFragment = null;
            showMultiLayout(false);
        }
    }
}
