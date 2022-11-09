package com.ngengs.android.popularmovies.apps.adapters;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ngengs.android.popularmovies.apps.R;
import com.ngengs.android.popularmovies.apps.data.VideosDetail;
import com.ngengs.android.popularmovies.apps.databinding.ItemVideoListBinding;
import com.ngengs.android.popularmovies.apps.utils.ResourceHelpers;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ngengs on 6/30/2017.
 */

@SuppressWarnings("DefaultFileTemplate")
public class VideoListAdapter extends RecyclerView.Adapter<VideoListAdapter.ViewHolder> {
    private static final String TAG = "VideoListAdapter";

    private final List<VideosDetail> data;
    private final Context context;
    private final VideoListAdapter.ClickListener clickListener;

    public VideoListAdapter(@NonNull Context context, @NonNull ClickListener clickListener) {
        this.context = context;
        this.clickListener = clickListener;
        this.data = new ArrayList<>();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemVideoListBinding binding = ItemVideoListBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        VideosDetail videos = data.get(position);
        Log.d(TAG, "onBindViewHolder: " + position);
        Log.d(TAG, "onBindViewHolder: " + videos.getType());
        if (videos.isYoutubeVideo()) {
            Log.d(TAG, "onBindViewHolder: " + videos.getYoutubeThumbnail());
            holder.binding.itemRoot.setVisibility(View.VISIBLE);
            Picasso.get()
                    .load(videos.getYoutubeThumbnail())
                    .noFade()
                    .placeholder(ResourceHelpers.getDrawable(context, R.drawable.ic_collections_white))
                    .into(holder.binding.imageVideo);

        } else {
            holder.binding.itemRoot.setVisibility(View.GONE);
        }
        holder.binding.itemRoot.setOnClickListener(view -> clickListener.onClickListener(position));
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
        ItemVideoListBinding binding;

        ViewHolder(ItemVideoListBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
