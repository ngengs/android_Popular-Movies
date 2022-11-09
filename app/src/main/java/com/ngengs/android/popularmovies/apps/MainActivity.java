package com.ngengs.android.popularmovies.apps;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.ngengs.android.popularmovies.apps.data.MoviesDetail;
import com.ngengs.android.popularmovies.apps.databinding.ActivityMainBinding;
import com.ngengs.android.popularmovies.apps.fragments.DetailMovieFragment;
import com.ngengs.android.popularmovies.apps.fragments.GridFragment;
import com.ngengs.android.popularmovies.apps.globals.Values;
import com.ngengs.android.popularmovies.apps.utils.ResourceHelpers;
import com.squareup.picasso.Picasso;


public class MainActivity extends AppCompatActivity implements GridFragment.OnFragmentInteractionListener, DetailMovieFragment.OnFragmentInteractionListener {
    private static final String TAG = "MainActivity";
    private static final int RESULT_DETAIL = 10;
    private ActivityMainBinding binding;
    private ActionBar actionBar;
    private Menu menuDetail;
    private GridFragment gridFragment;
    private DetailMovieFragment detailMovieFragment;
    private FragmentManager fragmentManager;
    private SharedPreferences sharedPref;
    private boolean openDetail = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
//        ButterKnife.bind(this);

        setSupportActionBar(binding.toolbar);
        actionBar = getSupportActionBar();
        Log.d(TAG, "onCreate: now");

        sharedPref = getPreferences(Context.MODE_PRIVATE);
        int sortType = sharedPref.getInt("SORT_TYPE_NOW", Values.TYPE_POPULAR);
        switch (sortType) {
            case Values.TYPE_POPULAR:
            case Values.TYPE_HIGH_RATED:
            case Values.TYPE_FAVORITE:
                break;
            default:
                sortType = Values.TYPE_POPULAR;
        }

        if (fragmentManager == null) fragmentManager = getSupportFragmentManager();

        if (savedInstanceState == null) {
            Log.d(TAG, "onCreate: attach fragment");
            gridFragment = GridFragment.newInstance(sortType);
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.add(binding.fragmentGrid.getId(), gridFragment);
            fragmentTransaction.commit();
        } else {
            gridFragment = (GridFragment) fragmentManager.findFragmentById(binding.fragmentGrid.getId());
            assert binding.fragmentDetail != null;
            if (binding.fragmentDetail.getVisibility() == View.VISIBLE) {
                if (fragmentManager.findFragmentById(binding.fragmentDetail.getId()) != null) {
                    detailMovieFragment = (DetailMovieFragment) fragmentManager.findFragmentById(binding.fragmentDetail.getId());
                }
            }
        }
        if (savedInstanceState != null) {
            openDetail = savedInstanceState.getBoolean("OPEN_DETAIL", false);
        }
        initializeViewAction();
    }

    private void initializeViewAction() {
        if (binding.fabFavorite != null) {
            binding.fabFavorite.setOnClickListener(view -> onFavoriteClick());
        }
    }

    private boolean isMultiLayout() {
        return binding.rootDetailView != null && binding.guideline != null;
    }

    @SuppressWarnings("ConstantConditions")
    private void createMultiLayout() {
        if (isMultiLayout()) {
            Log.d(TAG, "createMultiLayout: success");
            Log.d(TAG, "createMultiLayout: gridfragment status: " + (gridFragment != null));
            binding.toolbarDetail.inflateMenu(R.menu.menu_detail);
            menuDetail = binding.toolbarDetail.getMenu();
            menuDetail.findItem(R.id.menu_detail_close).setVisible(true);
            menuDetail.findItem(R.id.menu_detail_share).setVisible(false);
            binding.toolbarDetail.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.menu_detail_close) {
                    onCloseMultiLayout();
                    return true;
                } else if (item.getItemId() == R.id.menu_detail_share) {
                    Log.d(TAG, "onMenuItemClick: Share");
                    Intent sendIntent = new Intent();
                    sendIntent.setAction(Intent.ACTION_SEND);
                    Log.d(TAG, "onClick: " + detailMovieFragment.getShareContent());
                    sendIntent.putExtra(Intent.EXTRA_TEXT, detailMovieFragment.getShareContent());
                    sendIntent.setType("text/plain");
                    startActivity(Intent.createChooser(sendIntent, getResources().getText(R.string.send_to)));
                    return true;
                } else {
                    return false;
                }
            });
            showMultiLayout(openDetail);
        }
    }

    private void showMultiLayout(boolean show) {
        if (isMultiLayout()) {
            ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) binding.guideline.getLayoutParams();
            if (!show) {
                openDetail = false;
                binding.rootDetailView.setVisibility(View.GONE);
                params.guidePercent = 1f;
                if (gridFragment != null) gridFragment.updateSpanColumn(4);
            } else {
                openDetail = true;
                binding.rootDetailView.setVisibility(View.VISIBLE);
                if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
                    params.guidePercent = 0.35f;
                else
                    params.guidePercent = 0.5f;
                if (gridFragment != null) gridFragment.updateSpanColumn(2);
            }
            binding.guideline.setLayoutParams(params);
        }
    }

    @SuppressWarnings("EmptyMethod")
    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("OPEN_DETAIL", openDetail);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_popular, menu);
        if (gridFragment.getSortType() == Values.TYPE_POPULAR)
            menu.findItem(R.id.menu_sort_by_popular).setChecked(true);
        else if (gridFragment.getSortType() == Values.TYPE_HIGH_RATED)
            menu.findItem(R.id.menu_sort_by_top_rated).setChecked(true);
        else
            menu.findItem(R.id.menu_sort_by_favorite).setChecked(true);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int sortType = -1;
        if (item.getItemId() == R.id.menu_sort_by_popular) {
            sortType = Values.TYPE_POPULAR;
        } else if (item.getItemId() == R.id.menu_sort_by_top_rated) {
            sortType = Values.TYPE_HIGH_RATED;
        } else if (item.getItemId() == R.id.menu_sort_by_favorite) {
            sortType = Values.TYPE_FAVORITE;
        }
        if (sortType > -1) {
            gridFragment.changeType(sortType);
            item.setChecked(true);
            SharedPreferences.Editor shEditor = sharedPref.edit();
            shEditor.putInt("SORT_TYPE_NOW", sortType);
            shEditor.apply();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (isMultiLayout() && fragmentManager != null) onCloseMultiLayout();
        else super.onBackPressed();
    }

    @Override
    public void onFragmentClickMovies(int position, MoviesDetail data) {
        if (!isMultiLayout()) {
            Intent intent = new Intent(MainActivity.this, DetailMovieActivity.class);
            intent.putExtra("DATA", data);
            startActivityForResult(intent, RESULT_DETAIL);
        } else {
            showMultiLayout(true);
            if (isMultiLayout() && binding.fragmentDetail != null) {
                boolean changeFragment = true;
                if (detailMovieFragment != null) {
                    // Check is fragment same as the clicked data
                    DetailMovieFragment temp = (DetailMovieFragment) fragmentManager.findFragmentById(binding.fragmentDetail.getId());
                    int tempMoviesId = temp != null ? temp.getMoviesId() : -1;
                    Log.d(TAG, "onFragmentClickMovies: old id: " + tempMoviesId);
                    Log.d(TAG, "onFragmentClickMovies: new id: " + data.getId());
                    if (data.getId() == tempMoviesId) changeFragment = false;
                }
                Log.d(TAG, "onFragmentClickMovies: can change: " + changeFragment);
                if (changeFragment) {
                    // Clear button favorite
                    onFragmentChangeFavorite(null, false, false);
                    detailMovieFragment = DetailMovieFragment.newInstance(data);
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.replace(binding.fragmentDetail.getId(), detailMovieFragment);
                    fragmentTransaction.commit();
                    if (binding.appbarDetail != null) binding.appbarDetail.setExpanded(true);
                    if (binding.scrollDetail != null) binding.scrollDetail.scrollTo(0, 0);
                    gridFragment.scrollToPosition(position);
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_DETAIL) {
            if (gridFragment.getSortType() == Values.TYPE_FAVORITE) {
                Log.d(TAG, "onActivityResult: Refresh the favorite");
                gridFragment.changeType(Values.TYPE_FAVORITE);
            }
        }
    }

    @Override
    public void onFragmentChangeTitle(int sortType) {
        if (actionBar != null) {
            if (sortType == Values.TYPE_POPULAR)
                actionBar.setTitle(getResources().getString(R.string.title_popular));
            else if (sortType == Values.TYPE_HIGH_RATED)
                actionBar.setTitle(getResources().getString(R.string.title_top_rated));
            else
                actionBar.setTitle(R.string.title_favorite);
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
    @Override
    public void onFragmentChangeFavorite(MoviesDetail data, boolean isFavorite, boolean isRefresh) {
        if (isMultiLayout()) {
            Log.d(TAG, "onFragmentChangeFavorite: now");
            if (isFavorite)
                binding.fabFavorite.setImageDrawable(ResourceHelpers.getDrawable(this, R.drawable.ic_favorite_white));
            else
                binding.fabFavorite.setImageDrawable(ResourceHelpers.getDrawable(this, R.drawable.ic_favorite_border_white));
            if (gridFragment.getSortType() == Values.TYPE_FAVORITE && isRefresh) {
                if (data != null) {
                    if (isFavorite) gridFragment.addMovies(data);
                    else gridFragment.removeMovies(data);
                }
            }
        }
    }

    void onFavoriteClick() {
        if (isMultiLayout()) {
            Log.d(TAG, "onFavoriteClick: now");
            if (detailMovieFragment != null) detailMovieFragment.changeFavorite();
        }
    }

    @Override
    public void onFragmentChangeTitle(@NonNull String title) {
        Log.d(TAG, "onFragmentChangeTitle: start");
        if (isMultiLayout() && binding.collapsingToolbar != null) {
            Log.d(TAG, "onFragmentChangeTitle: change to: " + title);
            binding.collapsingToolbar.setTitle(title);
            Log.d(TAG, "onFragmentChangeTitle: changed to: " + binding.collapsingToolbar.getTitle());
        }
    }

    @Override
    public void onFragmentChangeHeaderImage(@Nullable String imageUri) {
        if (isMultiLayout()) {
            Picasso.get()
                    .load(imageUri)
                    .centerCrop()
                    .resize(Resources.getSystem().getDisplayMetrics().widthPixels, getResources().getDimensionPixelSize(R.dimen.image_description_header))
                    .into(binding.detailHeaderImage);
        }
    }

    @SuppressWarnings("ConstantConditions")
    private void onCloseMultiLayout() {
        if (isMultiLayout()) {
            if (detailMovieFragment == null)
                detailMovieFragment = (DetailMovieFragment) fragmentManager.findFragmentById(R.id.fragmentDetail);

            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.remove(detailMovieFragment);
            fragmentTransaction.commit();
            binding.detailHeaderImage.setImageResource(0);
            detailMovieFragment = null;
            showMultiLayout(false);
        }
    }
}
