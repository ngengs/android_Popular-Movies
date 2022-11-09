package com.ngengs.android.popularmovies.apps.utils.ui

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import kotlin.math.round

/**
 * Created by rizky.kharisma on 10/11/22.
 * @ngengs
 */
class PosterImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height = round(width * ASPECT_RATIO).toInt()
        setMeasuredDimension(width, height)
    }

    companion object {
        private const val ASPECT_RATIO = 1.5F
    }
}