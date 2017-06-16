package com.ngengs.android.popularmovies.stage1.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import com.squareup.picasso.Transformation;

/**
 * Created by ngengs on 6/16/2017.
 */

@SuppressWarnings({"unused", "DefaultFileTemplate"})
public class CircleTransform implements Transformation {

    private boolean bordered = false;

    public CircleTransform() {
    }

    @SuppressWarnings("SameParameterValue")
    public CircleTransform(boolean bordered) {
        this.bordered = bordered;
    }

    @Override
    public Bitmap transform(Bitmap source) {
        int size = Math.min(source.getWidth(), source.getHeight());

        int x = (source.getWidth() - size) / 2;
        int y = (source.getHeight() - size) / 2;

        Bitmap squaredBitmap = Bitmap.createBitmap(source, x, y, size, size);
        if (squaredBitmap != source) {
            source.recycle();
        }

        Bitmap bitmap = Bitmap.createBitmap(size, size, source.getConfig());

        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        BitmapShader shader = new BitmapShader(squaredBitmap,
                BitmapShader.TileMode.CLAMP, BitmapShader.TileMode.CLAMP);
        paint.setShader(shader);
        paint.setAntiAlias(true);

        float r = size / 2f;
        canvas.drawCircle(r, r, r, paint);

        // personally I hate the float below, is it general enough?
        if (bordered) {
            Paint paintBorder = new Paint();
            paintBorder.setColor(Color.WHITE);
            paintBorder.setStyle(Paint.Style.STROKE);
            paintBorder.setAntiAlias(true);
            paintBorder.setStrokeWidth(8);
            canvas.drawCircle(r, r + 0.2f, r - 3.9f, paintBorder);
        }

        squaredBitmap.recycle();
        return bitmap;
    }

    @Override
    public String key() {
        return "CIRCLE";
    }
}
