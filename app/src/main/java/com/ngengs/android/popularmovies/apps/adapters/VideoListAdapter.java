package com.ngengs.android.popularmovies.apps.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.ngengs.android.popularmovies.apps.R;
import com.ngengs.android.popularmovies.apps.data.VideosDetail;
import com.ngengs.android.popularmovies.apps.utils.ResourceHelpers;
import com.squareup.picasso.Picasso;

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
    private static final String TAG = "VideoListAdapter";

    private List<VideosDetail> data;
    private Context context;
    private VideoListAdapter.ClickListener clickListener;

    public VideoListAdapter(@NonNull Context context, @NonNull ClickListener clickListener) {
        this.context = context;
        this.clickListener = clickListener;
        this.data = new ArrayList<>();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_video_list, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        VideosDetail videos = data.get(position);
        Log.d(TAG, "onBindViewHolder: " + position);
        Log.d(TAG, "onBindViewHolder: " + videos.getType());
        if (videos.isYoutubeVideo()) {
            Log.d(TAG, "onBindViewHolder: " + videos.getYoutubeThumbnail());
            holder.itemRoot.setVisibility(View.VISIBLE);
            Picasso.with(context)
                    .load(videos.getYoutubeThumbnail())
                    .noFade()
                    .placeholder(ResourceHelpers.getDrawable(context, R.drawable.ic_collections_white))
                    .into(holder.imageVideo);

        } else {
            holder.itemRoot.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public void add(List<VideosDetail> data) {
        int oldSize = getItemCount();
        this.data.addAll(data);
        notifyItemRangeInserted(oldSize, data.size());
    }

    public void clear() {
        int lastSize = getItemCount();
        data.clear();
        notifyItemRangeRemoved(0, lastSize);
    }

    public VideosDetail get(int position) {
        return data.get(position);
    }

    public List<VideosDetail> get() {
        return data;
    }

    public interface ClickListener {
        void onClickListener(int position);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.imageVideo)
        ImageView imageVideo;
        @BindView(R.id.itemRoot)
        FrameLayout itemRoot;

        ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

        @OnClick(R.id.itemRoot)
        void itemClick() {
            clickListener.onClickListener(getAdapterPosition());
        }
    }
}
