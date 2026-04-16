package com.example.movietracker.ui.profile;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.example.movietracker.R;
import com.example.movietracker.model.Show;
import com.example.movietracker.ui.detail.DetailActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FavoritesAdapter extends RecyclerView.Adapter<FavoritesAdapter.ViewHolder> {
    private List<Show> shows = new ArrayList<>();

    public void setShows(List<Show> shows) {
        this.shows = shows;
        notifyDataSetDataSet();
    }

    private void notifyDataSetDataSet() {
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_favorite_show, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Show show = shows.get(position);
        Context context = holder.itemView.getContext();

        holder.tvTitle.setText(show.getTitle());
        holder.tvScore.setText(String.format(Locale.getDefault(), "⭐ %.1f", show.getUserScore()));

        if (show.getPoster() != null && !show.getPoster().equals("N/A")) {
            Glide.with(context)
                .load(show.getPoster())
                .transition(DrawableTransitionOptions.withCrossFade())
                .placeholder(R.drawable.ic_placeholder)
                .error(R.drawable.ic_placeholder)
                .into(holder.ivPoster);
        } else {
            holder.ivPoster.setImageResource(R.drawable.ic_placeholder);
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, DetailActivity.class);
            intent.putExtra(DetailActivity.EXTRA_IMDB_ID, show.getImdbId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return shows != null ? shows.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivPoster;
        TextView tvTitle;
        TextView tvScore;

        ViewHolder(View itemView) {
            super(itemView);
            ivPoster = itemView.findViewById(R.id.ivFavoritePoster);
            tvTitle = itemView.findViewById(R.id.tvFavoriteTitle);
            tvScore = itemView.findViewById(R.id.tvFavoriteScore);
        }
    }
}
