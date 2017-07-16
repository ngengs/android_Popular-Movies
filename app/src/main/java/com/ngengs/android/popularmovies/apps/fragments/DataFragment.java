package com.ngengs.android.popularmovies.apps.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import com.ngengs.android.popularmovies.apps.data.remote.MoviesDetail;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ngengs on 7/16/2017.
 */

@SuppressWarnings("DefaultFileTemplate")
public class DataFragment extends Fragment {
    private List<MoviesDetail> moviesDetailList;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // retain this fragment
        setRetainInstance(true);
    }

    public List<MoviesDetail> getMoviesDetailList() {
        return moviesDetailList;
    }

    public void setMoviesDetailList(List<MoviesDetail> moviesDetailList) {
        this.moviesDetailList = new ArrayList<>();
        this.moviesDetailList.addAll(moviesDetailList);
    }
}
