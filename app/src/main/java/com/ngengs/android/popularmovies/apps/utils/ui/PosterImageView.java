package com.ngengs.android.popularmovies.apps.utils.ui;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;

/**
 * Created by ngengs on 7/16/2017.
 */

@SuppressWarnings("DefaultFileTemplate")
public class PosterImageView extends android.support.v7.widget.AppCompatImageView {
    private static final float ASPECT_RATIO = 1.5f;

    public PosterImageView(Context context) {
        super(context);
    }

    public PosterImageView(Context context,
                           @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public PosterImageView(Context context,
                           @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = Math.round(width * ASPECT_RATIO);

        setMeasuredDimension(width, height);
    }
}
