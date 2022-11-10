package com.ngengs.android.popularmovies.apps.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ngengs.android.popularmovies.apps.R
import com.ngengs.android.popularmovies.apps.data.remote.VideosDetail
import com.ngengs.android.popularmovies.apps.data.remote.isYoutubeVideo
import com.ngengs.android.popularmovies.apps.data.remote.youtubeSmallThumbnail
import com.ngengs.android.popularmovies.apps.data.remote.youtubeThumbnail
import com.ngengs.android.popularmovies.apps.databinding.ItemVideoListBinding
import com.ngengs.android.popularmovies.apps.utils.ResourceHelpers
import com.ngengs.android.popularmovies.apps.utils.images.GlideUtils

/**
 * Created by rizky.kharisma on 09/11/22.
 * @ngengs
 */
class VideoListAdapter(
    private val context: Context,
    private val clickListener: ClickListener,
    private val data: MutableList<VideosDetail> = mutableListOf(),
) : RecyclerView.Adapter<VideoListAdapter.ViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val binding =
            ItemVideoListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val videos = data[position]
        Log.d(TAG, "onBindViewHolder: $position")
        Log.d(TAG, "onBindViewHolder: " + videos.type)
        if (videos.isYoutubeVideo) {
            Log.d(TAG, "onBindViewHolder: " + videos.youtubeThumbnail)
            holder.binding.itemRoot.visibility = View.VISIBLE
            Glide.with(holder.binding.imageVideo.context)
                .load(videos.youtubeThumbnail)
                .thumbnail(
                    GlideUtils.thumbnailBuilder(
                        holder.binding.imageVideo.context,
                        videos.youtubeSmallThumbnail
                    )
                )
                .placeholder(ResourceHelpers.getDrawable(context, R.drawable.ic_collections_white))
                .into(holder.binding.imageVideo)
        } else {
            Glide.with(holder.binding.imageVideo.context).clear(holder.binding.imageVideo)
            holder.binding.itemRoot.visibility = View.GONE
        }
        holder.binding.itemRoot.setOnClickListener { clickListener.onClickListener(position) }
    }

    override fun getItemCount(): Int = data.size

    fun add(data: List<VideosDetail>) {
        val oldSize = itemCount
        this.data.addAll(data)
        notifyItemRangeInserted(oldSize, data.size)
    }

    fun clear() {
        val lastSize = itemCount
        data.clear()
        notifyItemRangeRemoved(0, lastSize)
    }

    fun get(position: Int): VideosDetail? = data.getOrNull(position)
    fun get(): List<VideosDetail> = data

    interface ClickListener {
        fun onClickListener(position: Int)
    }

    class ViewHolder(val binding: ItemVideoListBinding) : RecyclerView.ViewHolder(binding.root)

    companion object {
        private const val TAG = "VideoListAdapter"
    }
}