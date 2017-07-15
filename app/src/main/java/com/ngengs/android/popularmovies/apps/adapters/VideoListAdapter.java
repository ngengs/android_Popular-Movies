package com.ngengs.android.popularmovies.apps.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.ngengs.android.popularmovies.apps.R;
import com.ngengs.android.popularmovies.apps.data.remote.VideosDetail;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by ngengs on 6/30/2017.
 */

@SuppressWarnings("DefaultFileTemplate")
public class VideoListAdapter extends RecyclerView.Adapter<VideoListAdapter.ViewHolder> {

    private final List<VideosDetail> mData;
    private final Context mContext;
    private final VideoListAdapter.ClickListener mClickListener;

    public VideoListAdapter(@NonNull Context context, @NonNull ClickListener clickListener) {
        this.mContext = context;
        this.mClickListener = clickListener;
        this.mData = new ArrayList<>();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext())
                                      .inflate(R.layout.item_video_list, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        VideosDetail videos = mData.get(position);
        if (videos.isYoutubeVideo()) {
            holder.mItemRoot.setVisibility(View.VISIBLE);
            Glide.with(mContext)
                    .load(videos.getYoutubeThumbnail())
                    .asBitmap()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .crossFade()
                    .thumbnail(0.05f)
                    .into(holder.mImageVideo);
        } else {
            holder.mItemRoot.setVisibility(View.GONE);
            Glide.clear(holder.mImageVideo);
        }
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public void add(List<VideosDetail> data) {
        int oldSize = getItemCount();
        this.mData.addAll(data);
        notifyItemRangeInserted(oldSize, data.size());
    }

    public void clear() {
        int lastSize = getItemCount();
        mData.clear();
        notifyItemRangeRemoved(0, lastSize);
    }

    public VideosDetail get(int position) {
        return mData.get(position);
    }

    public List<VideosDetail> get() {
        return mData;
    }

    public interface ClickListener {
        void onClickListener(int position);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.imageVideo)
        ImageView mImageVideo;
        @BindView(R.id.itemRoot)
        FrameLayout mItemRoot;

        ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

        @OnClick(R.id.itemRoot)
        void itemClick() {
            mClickListener.onClickListener(getAdapterPosition());
        }
    }
}
