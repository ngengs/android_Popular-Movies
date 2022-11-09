package com.ngengs.android.popularmovies.apps.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.ngengs.android.popularmovies.apps.R;
import com.ngengs.android.popularmovies.apps.data.MoviesDetail;
import com.ngengs.android.popularmovies.apps.databinding.ItemMovieListBinding;
import com.ngengs.android.popularmovies.apps.utils.ResourceHelpers;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ngengs on 6/15/2017.
 */

@SuppressWarnings("DefaultFileTemplate")
public class MovieListAdapter extends RecyclerView.Adapter<MovieListAdapter.ViewHolder> {
    @SuppressWarnings("unused")
    private static final String TAG = "MovieListAdapter";
    private final Context context;
    private final List<MoviesDetail> data;
    private final MovieListAdapter.ClickListener clickListener;

    public MovieListAdapter(Context context, MovieListAdapter.ClickListener clickListener) {
        this.context = context;
        this.data = new ArrayList<>();
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemMovieListBinding binding = ItemMovieListBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String imageUrl = data.get(position).getPosterPath();
        if (imageUrl != null)
            Picasso.with(context).load(imageUrl).noFade().placeholder(ResourceHelpers.getDrawable(context, R.drawable.ic_collections_white)).into(holder.binding.imagePoster);
        holder.binding.imagePoster.setOnClickListener(view -> holder.itemClick());
        holder.binding.rankPosition.setText(context.getResources().getString(R.string.movie_position, (position + 1)));
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public void add(List<MoviesDetail> data) {
        int lastSize = getItemCount();
        this.data.addAll(data);
        notifyItemRangeInserted(lastSize, data.size());
    }

    public void add(MoviesDetail item) {
        data.add(item);
        notifyItemInserted(getItemCount());
    }

    public void deleteById(int movieId) {
        int position = 0;
        for (MoviesDetail item : data) {
            if (item.getId() == movieId) break;
            position++;
        }
        notifyItemRemoved(position);
        data.remove(position);
        Log.d(TAG, "deleteById: position: " + position + " itemCount: " + getItemCount());
        if (position < getItemCount()) notifyItemRangeChanged(position, getItemCount() - position);
    }

    public MoviesDetail get(int position) {
        return data.get(position);
    }

    public List<MoviesDetail> get() {
        return data;
    }

    public void clear() {
        int lastSize = getItemCount();
        data.clear();
        notifyItemRangeRemoved(0, lastSize);
    }

    public interface ClickListener {
        void OnClickListener(int position);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        ItemMovieListBinding binding;

        ViewHolder(ItemMovieListBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void itemClick() {
            clickListener.OnClickListener(getAdapterPosition());
        }
    }
}
