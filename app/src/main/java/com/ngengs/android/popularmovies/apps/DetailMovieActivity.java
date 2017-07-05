package com.ngengs.android.popularmovies.apps;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.ngengs.android.popularmovies.apps.data.remote.MoviesDetail;
import com.ngengs.android.popularmovies.apps.fragments.DetailMovieFragment;
import com.ngengs.android.popularmovies.apps.utils.ResourceHelpers;
import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

@SuppressWarnings("FieldCanBeLocal")
public class DetailMovieActivity extends AppCompatActivity
        implements DetailMovieFragment.OnFragmentInteractionListener {
    private static final String TAG = "DetailMovieActivity";

    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.detailHeaderImage)
    ImageView mImageHeader;
    @BindView(R.id.fabFavorite)
    FloatingActionButton mFabFavorite;
    @BindView(R.id.fragmentDetail)
    FrameLayout mDetailFragmentLayout;

    private MoviesDetail mData;
    private FragmentManager mFragmentManager;
    private DetailMovieFragment mDetailMovieFragment;
    private ActionBar mActionBar;
    private boolean mMoviesShare = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_movie);
        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);

        mData = getIntent().getParcelableExtra("DATA");
        if (mData == null) {
            Toast.makeText(this, "Something wrong with detail mData", Toast.LENGTH_SHORT).show();
            finish();
        }

        mActionBar = getSupportActionBar();
        if (mActionBar != null) {
            mActionBar.setDisplayHomeAsUpEnabled(true);
            mActionBar.setDisplayShowHomeEnabled(true);
        }

        mFragmentManager = getSupportFragmentManager();

        if (mDetailFragmentLayout != null) {
            if (savedInstanceState == null) {
                Log.d(TAG, "onCreate: attach fragment");
                mDetailMovieFragment = DetailMovieFragment.newInstance(mData);
                FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
                fragmentTransaction.add(mDetailFragmentLayout.getId(), mDetailMovieFragment);
                fragmentTransaction.commit();
            } else {
                mDetailMovieFragment = (DetailMovieFragment) mFragmentManager.findFragmentById(
                        mDetailFragmentLayout.getId());
            }
        }

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @SuppressWarnings("EmptyMethod")
    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d(TAG, "onCreateOptionsMenu: create menu");
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_detail, menu);
        menu.findItem(R.id.menu_detail_close).setVisible(false);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.menu_detail_share).setVisible(mMoviesShare);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.menu_detail_share:
                shareItem();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @OnClick(R.id.fabFavorite)
    void onFavoriteClick() {
        mDetailMovieFragment.changeFavorite();
    }

    private void shareItem() {
        if (mDetailMovieFragment.getStatusLoadedFromServer()) {
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            Log.d(TAG, "onClick: " + mDetailMovieFragment.getShareContent());
            sendIntent.putExtra(Intent.EXTRA_TEXT, mDetailMovieFragment.getShareContent());
            sendIntent.setType("text/plain");
            startActivity(
                    Intent.createChooser(sendIntent, getResources().getText(R.string.send_to)));
        }
    }

    @Override
    public void onFragmentShowShare() {
        Log.d(TAG, "onFragmentShowShare: changed");
        mMoviesShare = true;
        invalidateOptionsMenu();
    }

    @Override
    public void onFragmentChangeFavorite(MoviesDetail data, boolean isFavorite, boolean isRefresh) {
        if (isFavorite) {
            mFabFavorite.setImageDrawable(
                    ResourceHelpers.getDrawable(this, R.drawable.ic_favorite_white));
        } else {
            mFabFavorite.setImageDrawable(
                    ResourceHelpers.getDrawable(this, R.drawable.ic_favorite_border_white));
        }
    }

    @Override
    public void onFragmentChangeTitle(@NonNull String title) {
        if (mActionBar != null) mActionBar.setTitle(title);
    }

    @Override
    public void onFragmentChangeHeaderImage(@Nullable String imageUri) {
        if (!TextUtils.isEmpty(imageUri)) {
            Picasso.with(this)
                    .load(imageUri)
                    .centerCrop()
                    .resize(Resources.getSystem().getDisplayMetrics().widthPixels,
                            getResources().getDimensionPixelSize(R.dimen.image_description_header))
                    .into(mImageHeader);
        }
    }
}
