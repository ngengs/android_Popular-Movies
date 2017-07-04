package com.ngengs.android.popularmovies.apps.utils;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by ngengs on 6/16/2017.
 */

@SuppressWarnings("DefaultFileTemplate")
public class GridSpacesItemDecoration extends RecyclerView.ItemDecoration {
    private final int mSpanCount;
    private final int mSpacing;


    public GridSpacesItemDecoration(int spanCount, int spacing) {
        this.mSpanCount = spanCount;
        this.mSpacing = spacing;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent,
                               RecyclerView.State state) {
        int position = parent.getChildAdapterPosition(view);
        int column = position % mSpanCount;

        outRect.left = mSpacing - column * mSpacing / mSpanCount;
        outRect.right = (column + 1) * mSpacing / mSpanCount;

        if (position < mSpanCount) outRect.top = mSpacing;

        outRect.bottom = mSpacing;
    }
}
