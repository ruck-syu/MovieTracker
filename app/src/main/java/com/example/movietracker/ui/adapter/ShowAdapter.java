package com.example.movietracker.ui.adapter;

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

import java.util.ArrayList;
import java.util.List;

public class ShowAdapter extends RecyclerView.Adapter<ShowAdapter.ShowViewHolder> {
    private List<Show> shows = new ArrayList<>();
    private OnShowClickListener listener;
    private boolean showTrackingDetails;

    public interface OnShowClickListener {
        void onShowClick(Show show);
    }

    public void setOnShowClickListener(OnShowClickListener listener) {
        this.listener = listener;
    }

    public void setShows(List<Show> shows) {
        this.shows = shows != null ? shows : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void setShowTrackingDetails(boolean showTrackingDetails) {
        this.showTrackingDetails = showTrackingDetails;
    }

    @NonNull
    @Override
    public ShowViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_show, parent, false);
        return new ShowViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ShowViewHolder holder, int position) {
        holder.bind(shows.get(position));
    }

    @Override
    public int getItemCount() {
        return shows.size();
    }

    class ShowViewHolder extends RecyclerView.ViewHolder {
        private final ImageView ivPoster;
        private final TextView tvTitle;
        private final TextView tvYear;
        private final TextView tvType;
        private final View layoutTrackingMeta;
        private final TextView tvEpisodes;
        private final TextView tvUserScore;

        ShowViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPoster = itemView.findViewById(R.id.ivPoster);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvYear = itemView.findViewById(R.id.tvYear);
            tvType = itemView.findViewById(R.id.tvType);
            layoutTrackingMeta = itemView.findViewById(R.id.layoutTrackingMeta);
            tvEpisodes = itemView.findViewById(R.id.tvEpisodes);
            tvUserScore = itemView.findViewById(R.id.tvUserScore);

            itemView.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && listener != null) {
                    listener.onShowClick(shows.get(pos));
                }
            });
        }

        void bind(Show show) {
            tvTitle.setText(show.getTitle());
            tvYear.setText(show.getYear());
            tvType.setText(show.getDisplayType());

            if (showTrackingDetails) {
                layoutTrackingMeta.setVisibility(View.VISIBLE);

                if (show.supportsEpisodeTracking()) {
                    tvEpisodes.setVisibility(View.VISIBLE);
                    tvEpisodes.setText(itemView.getContext().getString(
                        R.string.card_episodes_watched,
                        show.getEpisodeProgress()));
                } else {
                    tvEpisodes.setVisibility(View.GONE);
                }

                if (show.hasUserScore()) {
                    tvUserScore.setText(itemView.getContext().getString(
                        R.string.card_user_score_value,
                        String.format(java.util.Locale.getDefault(), "%.1f", show.getUserScore())));
                } else {
                    tvUserScore.setText(R.string.card_user_score_empty);
                }
            } else {
                layoutTrackingMeta.setVisibility(View.GONE);
            }

            if (show.getPoster() != null && !show.getPoster().equals("N/A")) {
                Glide.with(itemView.getContext())
                    .load(show.getPoster())
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .placeholder(R.drawable.ic_placeholder)
                    .error(R.drawable.ic_placeholder)
                    .into(ivPoster);
            } else {
                ivPoster.setImageResource(R.drawable.ic_placeholder);
            }
        }
    }
}
