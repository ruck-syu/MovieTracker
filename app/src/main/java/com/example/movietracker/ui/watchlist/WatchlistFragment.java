package com.example.movietracker.ui.watchlist;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.movietracker.R;
import com.example.movietracker.data.repository.ShowRepository;
import com.example.movietracker.model.Show;
import com.example.movietracker.model.WatchStatus;
import com.example.movietracker.ui.adapter.ShowAdapter;
import com.example.movietracker.ui.detail.DetailActivity;
import com.google.android.material.tabs.TabLayout;

public class WatchlistFragment extends Fragment {
    private TabLayout tabLayout;
    private RecyclerView rvWatchlist;
    private TextView tvEmpty;
    private ProgressBar progressBar;

    private ShowAdapter adapter;
    private ShowRepository repository;
    private WatchStatus currentStatus = WatchStatus.WATCHING;

    public WatchlistFragment() {
        super(R.layout.fragment_watchlist);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_watchlist, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        repository = new ShowRepository(requireContext());

        tabLayout = view.findViewById(R.id.tabLayout);
        rvWatchlist = view.findViewById(R.id.rvWatchlist);
        tvEmpty = view.findViewById(R.id.tvEmpty);
        progressBar = view.findViewById(R.id.progressBar);

        setupTabs();
        setupRecyclerView();
        loadShows(currentStatus);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (repository != null) {
            loadShows(currentStatus);
        }
    }

    private void setupTabs() {
        if (tabLayout.getTabCount() == 0) {
            tabLayout.addTab(tabLayout.newTab().setText("Watching"));
            tabLayout.addTab(tabLayout.newTab().setText("Planned"));
            tabLayout.addTab(tabLayout.newTab().setText("Completed"));
        }

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                currentStatus = WatchStatus.values()[tab.getPosition()];
                loadShows(currentStatus);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
    }

    private void setupRecyclerView() {
        adapter = new ShowAdapter();
        adapter.setShowTrackingDetails(true);
        adapter.setOnShowClickListener(this::openDetail);
        rvWatchlist.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvWatchlist.setAdapter(adapter);
    }

    private void loadShows(WatchStatus status) {
        showLoading(true);
        repository.getShowsByStatus(status, shows -> {
            showLoading(false);
            if (shows.isEmpty()) {
                showEmpty(true);
            } else {
                showEmpty(false);
                adapter.setShows(shows);
            }
        });
    }

    private void openDetail(Show show) {
        Intent intent = new Intent(requireContext(), DetailActivity.class);
        intent.putExtra(DetailActivity.EXTRA_IMDB_ID, show.getImdbId());
        startActivity(intent);
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        rvWatchlist.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    private void showEmpty(boolean show) {
        tvEmpty.setVisibility(show ? View.VISIBLE : View.GONE);
        rvWatchlist.setVisibility(show ? View.GONE : View.VISIBLE);
    }
}
