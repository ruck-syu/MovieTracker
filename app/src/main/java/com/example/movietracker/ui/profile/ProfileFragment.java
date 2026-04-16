package com.example.movietracker.ui.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.movietracker.R;
import com.example.movietracker.data.repository.ShowRepository;
import com.example.movietracker.model.ProfileStats;

import java.util.Locale;

public class ProfileFragment extends Fragment {
    private ShowRepository repository;

    private TextView tvTotalTracked;
    private TextView tvWatched;
    private TextView tvAverageRating;
    private TextView tvWatchingNow;
    private TextView tvPlannedQueue;
    private TextView tvSummary;

    public ProfileFragment() {
        super(R.layout.fragment_profile);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        repository = new ShowRepository(requireContext());

        tvTotalTracked = view.findViewById(R.id.tvProfileTotalTracked);
        tvWatched = view.findViewById(R.id.tvProfileWatched);
        tvAverageRating = view.findViewById(R.id.tvProfileAverageRating);
        tvWatchingNow = view.findViewById(R.id.tvProfileWatchingNow);
        tvPlannedQueue = view.findViewById(R.id.tvProfilePlannedQueue);
        tvSummary = view.findViewById(R.id.tvProfileSummary);

        loadProfile();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (repository != null) {
            loadProfile();
        }
    }

    private void loadProfile() {
        repository.getProfileStats(this::bindProfile);
    }

    private void bindProfile(ProfileStats stats) {
        tvTotalTracked.setText(String.valueOf(stats.getTotalTracked()));
        tvWatched.setText(String.valueOf(stats.getCompletedCount()));
        tvWatchingNow.setText(String.valueOf(stats.getWatchingCount()));
        tvPlannedQueue.setText(String.valueOf(stats.getPlannedCount()));

        if (stats.hasAverageRating()) {
            tvAverageRating.setText(String.format(Locale.getDefault(), "%.1f / 10", stats.getAverageRating()));
        } else {
            tvAverageRating.setText(R.string.profile_no_ratings);
        }

        if (stats.getTotalTracked() == 0) {
            tvSummary.setText(R.string.profile_summary_empty);
        } else if (stats.getCompletedCount() == 0) {
            tvSummary.setText(getString(
                R.string.profile_summary_waiting_for_completed,
                stats.getTotalTracked()));
        } else {
            tvSummary.setText(getString(
                R.string.profile_summary_ready,
                stats.getCompletedCount(),
                stats.getTotalTracked()));
        }
    }
}
