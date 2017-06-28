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
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.ngengs.android.popularmovies.apps.data.MoviesDetail;
import com.ngengs.android.popularmovies.apps.fragments.DetailMovieFragment;
import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class DetailMovieActivity extends AppCompatActivity implements DetailMovieFragment.OnFragmentInteractionListener {
    private static final String TAG = "DetailMovieActivity";

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.detailHeaderImage)
    ImageView imageHeader;
    @BindView(R.id.fabShare)
    FloatingActionButton fab;
    @BindView(R.id.fragmentDetail)
    FrameLayout detailFragmentLayout;

    private MoviesDetail data;
    private FragmentManager fragmentManager;
    private DetailMovieFragment detailMovieFragment;
    private ActionBar actionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_movie);
        ButterKnife.bind(this);

        fab.hide();

        setSupportActionBar(toolbar);

        data = getIntent().getParcelableExtra("DATA");
        if (data == null) {
            Toast.makeText(this, "Something wrong with detail data", Toast.LENGTH_SHORT).show();
            finish();
        }

        actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
//            if (data != null) getSupportActionBar().setTitle(data.getTitle());
        }

        fragmentManager = getSupportFragmentManager();

        if (detailFragmentLayout != null) {
            if (savedInstanceState == null) {
                Log.d(TAG, "onCreate: attach fragment");
                detailMovieFragment = DetailMovieFragment.newInstance(data);
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.add(detailFragmentLayout.getId(), detailMovieFragment);
                fragmentTransaction.commit();
            } else {
                detailMovieFragment = (DetailMovieFragment) fragmentManager.findFragmentById(detailFragmentLayout.getId());
            }
        }

//        if (data != null) {
//            bindOldData();
//        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
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

    @OnClick(R.id.fabShare)
    void fabShare() {
        if (detailMovieFragment.getStatusLoadedFromServer()) {
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            Log.d(TAG, "onClick: " + detailMovieFragment.getShareContent());
            sendIntent.putExtra(Intent.EXTRA_TEXT, detailMovieFragment.getShareContent());
            sendIntent.setType("text/plain");
            startActivity(Intent.createChooser(sendIntent, getResources().getText(R.string.send_to)));
        }
    }

    @Override
    public void onFragmentShowShare() {
        fab.show();
    }

    @Override
    public void onFragmentChangeTitle(@NonNull String title) {
        if (actionBar != null) actionBar.setTitle(title);
    }

    @Override
    public void onFragmentChangeHeaderImage(@Nullable String imageUri) {
        if (!TextUtils.isEmpty(imageUri)) {
            Picasso.with(this)
                    .load(imageUri)
                    .centerCrop()
                    .resize(Resources.getSystem().getDisplayMetrics().widthPixels, getResources().getDimensionPixelSize(R.dimen.image_description_header))
                    .into(imageHeader);
        }
    }
}
