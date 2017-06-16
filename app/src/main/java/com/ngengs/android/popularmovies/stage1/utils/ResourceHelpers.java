package com.ngengs.android.popularmovies.stage1.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v7.widget.AppCompatDrawableManager;

/**
 * Created by ngengs on 6/16/2017.
 */

@SuppressWarnings({"unused", "DefaultFileTemplate"})
public class ResourceHelpers {

    public static int getColor(Context context, int colorId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return context.getResources().getColor(colorId, null);
        } else {
            //noinspection deprecation
            return context.getResources().getColor(colorId);
        }
    }

    @SuppressLint("RestrictedApi")
    public static Drawable getDrawable(Context context, int drawableId) {
        Drawable placeholder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            placeholder = context.getResources().getDrawable(drawableId, null);
        } else {
            placeholder = AppCompatDrawableManager.get().getDrawable(context, drawableId);
        }
        return placeholder;
    }
}
