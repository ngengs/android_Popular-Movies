package com.ngengs.android.popularmovies.apps.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ngengs.android.popularmovies.apps.R;
import com.ngengs.android.popularmovies.apps.data.remote.ReviewDetail;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by ngengs on 7/1/2017.
 */

@SuppressWarnings("DefaultFileTemplate")
public class ReviewListAdapter extends RecyclerView.Adapter<ReviewListAdapter.ViewHolder> {
    private static final String TAG = "ReviewListAdapter";

    private final Context mContext;
    private final List<ReviewDetail> mData;
    private final ReviewListAdapter.ClickListener mClickListener;

    public ReviewListAdapter(Context context, ClickListener clickListener) {
        this.mContext = context;
        this.mClickListener = clickListener;
        mData = new ArrayList<>();
    }

    @Override
    public ReviewListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ReviewListAdapter.ViewHolder(LayoutInflater.from(parent.getContext())
                                                        .inflate(R.layout.item_review_list, parent,
                                                                 false));
    }

    @Override
    public void onBindViewHolder(ReviewListAdapter.ViewHolder holder, int position) {
        Log.d(TAG, "onBindViewHolder: position: " + position + " size: " + getItemCount());
        ReviewDetail review = mData.get(position);
        holder.mReviewPeople.setText(review.getAuthor());
        holder.mReviewText.setText(mContext.getString(R.string.review_text, review.getContent()));
        if (position == (getItemCount() - 1)) holder.mReviewSeparator.setVisibility(View.GONE);
        else holder.mReviewSeparator.setVisibility(View.VISIBLE);
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public void add(List<ReviewDetail> data) {
        int oldSize = getItemCount();
        this.mData.addAll(data);
        notifyItemRangeInserted(oldSize, data.size());
    }

    public void clear() {
        int lastSize = getItemCount();
        mData.clear();
        notifyItemRangeRemoved(0, lastSize);
    }

    public ReviewDetail get(int position) {
        return mData.get(position);
    }

    public List<ReviewDetail> get() {
        return mData;
    }

    public interface ClickListener {
        void onClickListener(int position);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.reviewText)
        TextView mReviewText;
        @BindView(R.id.reviewPeople)
        TextView mReviewPeople;
        @BindView(R.id.rootReview)
        LinearLayout mRootReview;
        @BindView(R.id.reviewSeparator)
        View mReviewSeparator;

        ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

        @OnClick(R.id.rootReview)
        void itemClick() {
            mClickListener.onClickListener(getAdapterPosition());
        }
    }
}
