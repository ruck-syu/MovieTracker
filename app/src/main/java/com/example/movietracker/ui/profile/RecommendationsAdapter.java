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
import com.example.movietracker.model.RecommendationItem;
import com.example.movietracker.model.Show;
import com.example.movietracker.ui.detail.DetailActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class RecommendationsAdapter extends RecyclerView.Adapter<RecommendationsAdapter.ViewHolder> {
    private List<RecommendationItem> recommendations = new ArrayList<>();

    public void setRecommendations(List<RecommendationItem> recommendations) {
        this.recommendations = recommendations != null ? recommendations : new ArrayList<>();
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
        RecommendationItem item = recommendations.get(position);
        Show show = item.getShow();
        Context context = holder.itemView.getContext();

        holder.tvTitle.setText(show.getTitle());
        holder.tvScore.setText(context.getString(
            R.string.recommendation_score_badge,
            String.format(Locale.getDefault(), "%.1f", item.getScore())));

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
        return recommendations.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageView ivPoster;
        private final TextView tvTitle;
        private final TextView tvScore;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPoster = itemView.findViewById(R.id.ivFavoritePoster);
            tvTitle = itemView.findViewById(R.id.tvFavoriteTitle);
            tvScore = itemView.findViewById(R.id.tvFavoriteScore);
        }
    }
}
