package com.ngengs.android.popularmovies.apps.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ngengs.android.popularmovies.apps.R;
import com.ngengs.android.popularmovies.apps.data.remote.MoviesDetail;
import com.ngengs.android.popularmovies.apps.utils.ResourceHelpers;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by ngengs on 6/15/2017.
 */

@SuppressWarnings("DefaultFileTemplate")
public class MovieListAdapter extends RecyclerView.Adapter<MovieListAdapter.ViewHolder> {
    @SuppressWarnings("unused")
    private static final String TAG = "MovieListAdapter";
    private final Context mContext;
    private final List<MoviesDetail> mData;
    private final MovieListAdapter.ClickListener mClickListener;

    public MovieListAdapter(Context context, MovieListAdapter.ClickListener clickListener) {
        this.mContext = context;
        this.mData = new ArrayList<>();
        this.mClickListener = clickListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext())
                                      .inflate(R.layout.item_movie_list, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        String imageUrl = mData.get(position).getPosterPath();
        if (imageUrl != null) {
            Picasso.with(mContext)
                    .load(imageUrl)
                    .noFade()
                    .placeholder(
                            ResourceHelpers.getDrawable(mContext, R.drawable.ic_collections_white))
                    .into(holder.mImage);
        }
        holder.mRankPosition.setText(
                mContext.getResources().getString(R.string.movie_position, (position + 1)));
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public void add(List<MoviesDetail> data) {
        int lastSize = getItemCount();
        this.mData.addAll(data);
        notifyItemRangeInserted(lastSize, data.size());
    }

    public void add(MoviesDetail item) {
        mData.add(item);
        notifyItemInserted(getItemCount());
    }

    public void deleteById(int movieId) {
        int position = 0;
        for (MoviesDetail item : mData) {
            if (item.getId() == movieId) break;
            position++;
        }
        notifyItemRemoved(position);
        mData.remove(position);
        Log.d(TAG, "deleteById: position: " + position + " itemCount: " + getItemCount());
        if (position < getItemCount()) notifyItemRangeChanged(position, getItemCount() - position);
    }

    public MoviesDetail get(int position) {
        return mData.get(position);
    }

    public List<MoviesDetail> get() {
        return mData;
    }

    public void clear() {
        int lastSize = getItemCount();
        mData.clear();
        notifyItemRangeRemoved(0, lastSize);
    }

    public interface ClickListener {
        void OnClickListener(int position);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.imagePoster)
        ImageView mImage;
        @BindView(R.id.rankPosition)
        TextView mRankPosition;

        ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        @OnClick(R.id.itemRoot)
        void itemClick() {
            mClickListener.OnClickListener(getAdapterPosition());
        }
    }
}
