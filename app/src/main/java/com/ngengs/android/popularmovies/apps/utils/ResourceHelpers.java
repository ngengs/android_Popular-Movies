package com.ngengs.android.popularmovies.apps.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import androidx.appcompat.widget.AppCompatDrawableManager;
import androidx.core.content.res.ResourcesCompat;

/**
 * Created by ngengs on 6/16/2017.
 */

@SuppressWarnings({"unused"})
public class ResourceHelpers {

    public static int getColor(Context context, int colorId) {
        return ResourcesCompat.getColor(context.getResources(), colorId, context.getTheme());
    }

    @SuppressLint("RestrictedApi")
    public static Drawable getDrawable(Context context, int drawableId) {
        return ResourcesCompat.getDrawable(context.getResources(), drawableId, context.getTheme());
    }
}
