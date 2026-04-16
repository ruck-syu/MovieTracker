package com.example.movietracker.ui.profile;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;
import com.example.movietracker.model.Show;
import java.util.List;
import java.util.Map;
import androidx.appcompat.app.AlertDialog;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.movietracker.R;
import com.example.movietracker.data.repository.ShowRepository;
import com.example.movietracker.model.ProfileStats;

import java.util.Locale;

public class ProfileFragment extends Fragment {
    private static final String PREFS_NAME = "movie_tracker_prefs";
    private static final String KEY_PROFILE_NAME = "profile_name";

    private ShowRepository repository;

    private TextView tvProfileName;
    private TextView tvTotalTracked;
    private TextView tvWatched;
    private TextView tvAverageRating;
    private TextView tvWatchingNow;
    private TextView tvPlannedQueue;
    private TextView tvSummary;
    private TextView tvProfileTier;

    private FavoritesAdapter favoritesAdapter;
    private View tvFavoritesTitle;
    private RecyclerView rvFavorites;
    private View tvAnalyticsTitle;
    private View cvAnalytics;
    private PieChartView pieChartView;

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

        tvProfileName = view.findViewById(R.id.tvProfileName);
        TextView tvEditProfileName = view.findViewById(R.id.tvEditProfileName);
        tvTotalTracked = view.findViewById(R.id.tvProfileTotalTracked);
        tvWatched = view.findViewById(R.id.tvProfileWatched);
        tvAverageRating = view.findViewById(R.id.tvProfileAverageRating);
        tvWatchingNow = view.findViewById(R.id.tvProfileWatchingNow);
        tvPlannedQueue = view.findViewById(R.id.tvProfilePlannedQueue);
        tvSummary = view.findViewById(R.id.tvProfileSummary);
        tvProfileTier = view.findViewById(R.id.tvProfileTier);

        tvFavoritesTitle = view.findViewById(R.id.tvFavoritesTitle);
        rvFavorites = view.findViewById(R.id.rvFavorites);
        tvAnalyticsTitle = view.findViewById(R.id.tvAnalyticsTitle);
        cvAnalytics = view.findViewById(R.id.cvAnalytics);
        pieChartView = view.findViewById(R.id.pieChartView);

        favoritesAdapter = new FavoritesAdapter();
        rvFavorites.setAdapter(favoritesAdapter);

        tvProfileName.setText(getSavedProfileName());
        tvEditProfileName.setOnClickListener(v -> showEditNameDialog());

        loadProfile();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (repository != null) {
            loadProfile();
        }
    }

    private void showEditNameDialog() {
        Context context = requireContext();
        EditText input = new EditText(context);
        input.setText(tvProfileName.getText().toString());
        input.setHint(R.string.profile_name_hint);
        input.setSelection(input.getText().length());
        input.setPadding(40, 24, 40, 24);

        new AlertDialog.Builder(context)
            .setTitle(R.string.profile_edit_name_title)
            .setView(input)
            .setPositiveButton(R.string.profile_save, (dialog, which) -> {
                String newName = input.getText().toString().trim();
                if (newName.isEmpty()) {
                    newName = getString(R.string.profile_name);
                }
                saveProfileName(newName);
                tvProfileName.setText(newName);
            })
            .setNegativeButton(R.string.profile_cancel, null)
            .show();
    }

    private String getSavedProfileName() {
        SharedPreferences preferences = requireContext()
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getString(KEY_PROFILE_NAME, getString(R.string.profile_name));
    }

    private void saveProfileName(String name) {
        SharedPreferences preferences = requireContext()
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        preferences.edit().putString(KEY_PROFILE_NAME, name).apply();
    }

    private void loadProfile() {
        repository.getProfileStats(this::bindProfile);
        repository.getTopRatedShows(10, this::bindFavorites);
        repository.getTypeDistribution(this::bindAnalytics);
    }

    private void bindProfile(ProfileStats stats) {
        tvTotalTracked.setText(String.valueOf(stats.getTotalTracked()));
        tvWatched.setText(String.valueOf(stats.getCompletedCount()));
        tvWatchingNow.setText(String.valueOf(stats.getWatchingCount()));
        tvPlannedQueue.setText(String.valueOf(stats.getPlannedCount()));
        
        tvProfileTier.setText(getUserTier(stats.getCompletedCount()));

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

    private String getUserTier(int watchedCount) {
        if (watchedCount >= 250) return "Master Tracker";
        if (watchedCount >= 100) return "Cinephile";
        if (watchedCount >= 50) return "Film Buff";
        if (watchedCount >= 10) return "Regular Watcher";
        if (watchedCount > 0) return "Casual Viewer";
        return "Newcomer";
    }

    private void bindFavorites(List<Show> shows) {
        if (shows != null && !shows.isEmpty()) {
            tvFavoritesTitle.setVisibility(View.VISIBLE);
            rvFavorites.setVisibility(View.VISIBLE);
            favoritesAdapter.setShows(shows);
        } else {
            tvFavoritesTitle.setVisibility(View.GONE);
            rvFavorites.setVisibility(View.GONE);
        }
    }

    private void bindAnalytics(Map<String, Integer> distribution) {
        if (distribution != null && !distribution.isEmpty()) {
            tvAnalyticsTitle.setVisibility(View.VISIBLE);
            cvAnalytics.setVisibility(View.VISIBLE);
            pieChartView.setData(distribution);
        } else {
            tvAnalyticsTitle.setVisibility(View.GONE);
            cvAnalytics.setVisibility(View.GONE);
        }
    }
}
