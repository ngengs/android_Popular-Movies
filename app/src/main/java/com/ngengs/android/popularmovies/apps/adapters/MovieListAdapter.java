package com.ngengs.android.popularmovies.apps.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ngengs.android.popularmovies.apps.R;
import com.ngengs.android.popularmovies.apps.data.MoviesDetail;
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

@SuppressWarnings({"CanBeFinal", "DefaultFileTemplate"})
public class MovieListAdapter extends RecyclerView.Adapter<MovieListAdapter.ViewHolder> {
    @SuppressWarnings("unused")
    private static final String TAG = "MovieListAdapter";
    private Context context;
    private List<MoviesDetail> data;
    private MovieListAdapter.ClickListener clickListener;

    @SuppressWarnings("SameParameterValue")
    public MovieListAdapter(Context context, List<MoviesDetail> data, MovieListAdapter.ClickListener clickListener) {
        this.context = context;
        if (data != null) this.data = data;
        else this.data = new ArrayList<>();
        this.clickListener = clickListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_movie_list, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        String imageUrl = data.get(position).getPosterPath();
        if (imageUrl != null)
            Picasso.with(context).load(imageUrl).noFade().placeholder(ResourceHelpers.getDrawable(context, R.drawable.ic_collections_white)).into(holder.image);
        holder.rankPosition.setText(context.getResources().getString(R.string.movie_position, (position + 1)));
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

    @SuppressWarnings("UnusedParameters")
    public interface ClickListener {
        void OnClickListener(int position, View view);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.imagePoster)
        ImageView image;
        @BindView(R.id.rankPosition)
        TextView rankPosition;

        ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        @OnClick(R.id.itemRoot)
        void itemClick(View v) {
            clickListener.OnClickListener(getAdapterPosition(), v);
        }
    }
}
