package com.ngengs.android.popularmovies.apps.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ngengs.android.popularmovies.apps.R
import com.ngengs.android.popularmovies.apps.data.MoviesDetail
import com.ngengs.android.popularmovies.apps.databinding.ItemMovieListBinding
import com.ngengs.android.popularmovies.apps.utils.ResourceHelpers
import com.squareup.picasso.Picasso

/**
 * Created by rizky.kharisma on 09/11/22.
 * @ngengs
 */
class MovieListAdapter @JvmOverloads constructor(
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
        val imageUrl = data[position].posterPath
        if (imageUrl != null) Picasso.get().load(imageUrl).noFade()
            .placeholder(ResourceHelpers.getDrawable(context, R.drawable.ic_collections_white))
            .into(holder.binding.imagePoster)
        holder.binding.imagePoster.setOnClickListener { clickListener.onClickListener(position) }
        holder.binding.rankPosition.text =
            context.resources.getString(R.string.movie_position, position + 1)
    }

    override fun getItemCount(): Int = data.size

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

    fun get(position: Int): MoviesDetail? = data.getOrNull(position)
    fun get(): List<MoviesDetail> = data

    fun clear() {
        val lastSize = itemCount
        data.clear()
        notifyItemRangeRemoved(0, lastSize)
    }

    interface ClickListener {
        fun onClickListener(position: Int)
    }

    class ViewHolder(val binding: ItemMovieListBinding) : RecyclerView.ViewHolder(binding.root)

    companion object {
        private const val TAG = "MovieListAdapter"
    }
}