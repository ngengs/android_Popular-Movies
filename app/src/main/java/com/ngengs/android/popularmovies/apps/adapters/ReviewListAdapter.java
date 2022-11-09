package com.ngengs.android.popularmovies.apps.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ngengs.android.popularmovies.apps.R;
import com.ngengs.android.popularmovies.apps.data.ReviewDetail;
import com.ngengs.android.popularmovies.apps.databinding.ItemReviewListBinding;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ngengs on 7/1/2017.
 */

@SuppressWarnings("DefaultFileTemplate")
public class ReviewListAdapter extends RecyclerView.Adapter<ReviewListAdapter.ViewHolder> {
    private static final String TAG = "ReviewListAdapter";

    private final Context context;
    private final List<ReviewDetail> data;
    private final ReviewListAdapter.ClickListener clickListener;

    public ReviewListAdapter(Context context, ClickListener clickListener) {
        this.context = context;
        this.clickListener = clickListener;
        data = new ArrayList<>();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemReviewListBinding binding = ItemReviewListBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Log.d(TAG, "onBindViewHolder: position: " + position + " size: " + getItemCount());
        ReviewDetail review = data.get(position);
        holder.binding.reviewPeople.setText(review.getAuthor());
        holder.binding.reviewText.setText(context.getString(R.string.review_text, review.getContent()));
        if (position == (getItemCount() - 1)) holder.binding.reviewSeparator.setVisibility(View.GONE);
        else holder.binding.reviewSeparator.setVisibility(View.VISIBLE);
        holder.binding.rootReview.setOnClickListener(view -> holder.itemClick());
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public void add(List<ReviewDetail> data) {
        int oldSize = getItemCount();
        this.data.addAll(data);
        notifyItemRangeInserted(oldSize, data.size());
    }

    public void clear() {
        int lastSize = getItemCount();
        data.clear();
        notifyItemRangeRemoved(0, lastSize);
    }

    public ReviewDetail get(int position) {
        return data.get(position);
    }

    public List<ReviewDetail> get() {
        return data;
    }

    public interface ClickListener {
        void onClickListener(int position);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        ItemReviewListBinding binding;

        ViewHolder(ItemReviewListBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void itemClick() {
            clickListener.onClickListener(getAdapterPosition());
        }
    }
}
