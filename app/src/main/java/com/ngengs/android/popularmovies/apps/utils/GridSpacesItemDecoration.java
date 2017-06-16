package com.ngengs.android.popularmovies.apps.utils;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by ngengs on 6/16/2017.
 */

@SuppressWarnings("DefaultFileTemplate")
public class GridSpacesItemDecoration extends RecyclerView.ItemDecoration {
    private final int spanCount;
    private final int spacing;


    public GridSpacesItemDecoration(int spanCount, int spacing) {
        this.spanCount = spanCount;
        this.spacing = spacing;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        int position = parent.getChildAdapterPosition(view);
        int column = position % spanCount;

        outRect.left = spacing - column * spacing / spanCount;
        outRect.right = (column + 1) * spacing / spanCount;

        if (position < spanCount) outRect.top = spacing;

        outRect.bottom = spacing;
    }
}
