package com.ngengs.android.popularmovies.apps;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.FrameLayout;

import com.ngengs.android.popularmovies.apps.data.MoviesDetail;
import com.ngengs.android.popularmovies.apps.fragments.GridFragment;
import com.ngengs.android.popularmovies.apps.globals.Values;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements GridFragment.OnFragmentInteractionListener {
    private static final String TAG = "MainActivity";

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.fragmentGrid)
    FrameLayout gridFragmentLayout;
    private ActionBar actionBar;

    private GridFragment gridFragment;
    private FragmentManager fragmentManager;

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
        Intent intent = new Intent(MainActivity.this, DetailMovieActivity.class);
        intent.putExtra("DATA", data);
        startActivity(intent);
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
}
