package com.ngengs.android.popularmovies.apps.utils

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

/**
 * Created by rizky.kharisma on 09/11/22.
 * @ngengs
 */
class GridSpacesItemDecoration(
    private val spanCount: Int,
    private val spacing: Int,
): RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val position = parent.getChildAdapterPosition(view)
        val column = position % spanCount

        outRect.left = spacing - column * spacing / spanCount
        outRect.right = (column + 1) * spacing / spanCount

        if (position < spanCount) outRect.top = spacing

        outRect.bottom = spacing
    }
}