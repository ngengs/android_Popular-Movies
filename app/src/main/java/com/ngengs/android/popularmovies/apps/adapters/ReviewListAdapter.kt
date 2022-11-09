package com.ngengs.android.popularmovies.apps.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ngengs.android.popularmovies.apps.R
import com.ngengs.android.popularmovies.apps.data.remote.ReviewDetail
import com.ngengs.android.popularmovies.apps.databinding.ItemReviewListBinding

/**
 * Created by rizky.kharisma on 09/11/22.
 * @ngengs
 */
class ReviewListAdapter(
    private val context: Context,
    private val clickListener: ClickListener,
    private val data: MutableList<ReviewDetail> = mutableListOf(),
) : RecyclerView.Adapter<ReviewListAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemReviewListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Log.d(TAG, "onBindViewHolder: position: $position size: $itemCount")
        val review = data[position]
        holder.binding.reviewPeople.text = review.author
        holder.binding.reviewText.text = context.getString(R.string.review_text, review.content)
        holder.binding.reviewSeparator.visibility =
            if (position == itemCount - 1) View.GONE else View.VISIBLE
        holder.binding.rootReview.setOnClickListener { clickListener.onClickListener(position) }
    }

    override fun getItemCount(): Int = data.size

    fun add(data: List<ReviewDetail>) {
        val oldSize = itemCount
        this.data.addAll(data)
        notifyItemRangeInserted(oldSize, data.size)
    }

    fun clear() {
        val lastSize = itemCount
        data.clear()
        notifyItemRangeRemoved(0, lastSize)
    }

    fun get(position: Int): ReviewDetail? = data.getOrNull(position)
    fun get(): List<ReviewDetail> = data

    interface ClickListener {
        fun onClickListener(position: Int)
    }

    class ViewHolder(val binding: ItemReviewListBinding) : RecyclerView.ViewHolder(binding.root)

    companion object {
        private const val TAG = "ReviewListAdapter"
    }
}