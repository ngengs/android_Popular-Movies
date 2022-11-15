package com.ngengs.android.popularmovies.apps.screen.movielist

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ngengs.android.popularmovies.apps.R
import com.ngengs.android.popularmovies.apps.data.remote.MoviesDetail
import com.ngengs.android.popularmovies.apps.data.remote.getPosterPath
import com.ngengs.android.popularmovies.apps.databinding.ItemMovieListBinding
import com.ngengs.android.popularmovies.apps.globals.Values
import com.ngengs.android.popularmovies.apps.utils.ResourceHelpers
import com.ngengs.android.popularmovies.apps.utils.images.GlideUtils

/**
 * Created by rizky.kharisma on 09/11/22.
 * @ngengs
 */
class MovieListAdapter(
    private val context: Context,
    private val clickListener: ClickListener,
    private val data: MutableList<MoviesDetail> = mutableListOf(),
) : RecyclerView.Adapter<MovieListAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemMovieListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val imageUrl = data[position].getPosterPath(Values.TYPE_DEFAULT_IMAGE_THUMB)
        if (imageUrl != null) {
            Glide.with(holder.binding.imagePoster.context)
                .load(imageUrl)
                .thumbnail(
                    GlideUtils.thumbnailBuilder(
                        holder.binding.imagePoster.context,
                        data[position].getPosterPath(1)
                    )
                )
                .placeholder(ResourceHelpers.getDrawable(context, R.drawable.ic_collections_daynight))
                .into(holder.binding.imagePoster)
        } else {
            Glide.with(holder.binding.imagePoster.context).clear(holder.binding.imagePoster)
        }
        holder.binding.imagePoster.setOnClickListener {
            clickListener.onClickListener(position, data[position])
        }
        holder.binding.rankPosition.text =
            context.resources.getString(R.string.movie_position, position + 1)
    }

    override fun getItemCount(): Int = data.size

    override fun getItemId(position: Int): Long = data[position].id.toLong()

    fun add(data: List<MoviesDetail>) {
        val lastSize = itemCount
        this.data.addAll(data)
        notifyItemRangeInserted(lastSize, data.size)
    }

    fun add(item: MoviesDetail) {
        data.add(item)
        notifyItemInserted(itemCount)
    }

    fun deleteById(movieId: Int) {
        var position = 0
        for (item in data) {
            if (item.id == movieId) break
            position++
        }
        notifyItemRemoved(position)
        data.removeAt(position)
        Log.d(TAG, "deleteById: position: $position itemCount: $itemCount")
        if (position < itemCount) notifyItemRangeChanged(position, itemCount - position)
    }

    fun clear() {
        val lastSize = itemCount
        data.clear()
        notifyItemRangeRemoved(0, lastSize)
    }

    interface ClickListener {
        fun onClickListener(position: Int, movie: MoviesDetail)
    }

    class ViewHolder(val binding: ItemMovieListBinding) : RecyclerView.ViewHolder(binding.root)

    companion object {
        private const val TAG = "MovieListAdapter"
    }
}