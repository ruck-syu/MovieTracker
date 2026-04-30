package com.example.movietracker.ui.watchlist;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.movietracker.R;
import com.example.movietracker.data.database.DatabaseHelper;
import com.example.movietracker.data.repository.ShowRepository;
import com.example.movietracker.model.Show;
import com.example.movietracker.model.WatchStatus;
import com.example.movietracker.ui.adapter.ShowAdapter;
import com.example.movietracker.ui.detail.DetailActivity;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

public class WatchlistFragment extends Fragment {
    private enum WatchlistFilter {
        ALL,
        MOVIES,
        SERIES
    }

    private TabLayout tabLayout;
    private RecyclerView rvWatchlist;
    private TextView tvEmpty;
    private ProgressBar progressBar;
    private Spinner spFilter;
    private Spinner spSort;

    private ShowAdapter adapter;
    private ShowRepository repository;
    private WatchStatus currentStatus = WatchStatus.WATCHING;
    private final DatabaseHelper.WatchlistSort[] tabSortSelections = new DatabaseHelper.WatchlistSort[]{
        DatabaseHelper.WatchlistSort.RECENT,
        DatabaseHelper.WatchlistSort.RECENT,
        DatabaseHelper.WatchlistSort.RECENT
    };
    private final WatchlistFilter[] tabFilterSelections = new WatchlistFilter[]{
        WatchlistFilter.ALL,
        WatchlistFilter.ALL,
        WatchlistFilter.ALL
    };

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
        spFilter = view.findViewById(R.id.spFilter);
        spSort = view.findViewById(R.id.spSort);

        setupTabs();
        setupRecyclerView();
        setupFilterOptions();
        setupSortOptions();
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
                spFilter.setSelection(filterToIndex(tabFilterSelections[currentStatus.ordinal()]));
                spSort.setSelection(sortToIndex(tabSortSelections[currentStatus.ordinal()]));
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

    private void setupSortOptions() {
        String[] sortOptions = new String[]{
            getString(R.string.watchlist_sort_recent),
            getString(R.string.watchlist_sort_title_asc),
            getString(R.string.watchlist_sort_title_desc),
            getString(R.string.watchlist_sort_score_desc)
        };

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
            requireContext(),
            android.R.layout.simple_spinner_item,
            sortOptions
        );
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spSort.setAdapter(spinnerAdapter);
        spSort.setSelection(sortToIndex(tabSortSelections[currentStatus.ordinal()]));
        spSort.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                DatabaseHelper.WatchlistSort selectedSort = indexToSort(position);
                if (tabSortSelections[currentStatus.ordinal()] != selectedSort) {
                    tabSortSelections[currentStatus.ordinal()] = selectedSort;
                    loadShows(currentStatus);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void setupFilterOptions() {
        String[] filterOptions = new String[]{
            getString(R.string.watchlist_filter_all),
            getString(R.string.watchlist_filter_movies),
            getString(R.string.watchlist_filter_series)
        };

        ArrayAdapter<String> filterAdapter = new ArrayAdapter<>(
            requireContext(),
            android.R.layout.simple_spinner_item,
            filterOptions
        );
        filterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spFilter.setAdapter(filterAdapter);
        spFilter.setSelection(filterToIndex(tabFilterSelections[currentStatus.ordinal()]));
        spFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                WatchlistFilter selectedFilter = indexToFilter(position);
                if (tabFilterSelections[currentStatus.ordinal()] != selectedFilter) {
                    tabFilterSelections[currentStatus.ordinal()] = selectedFilter;
                    loadShows(currentStatus);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private int filterToIndex(WatchlistFilter filter) {
        if (filter == WatchlistFilter.MOVIES) return 1;
        if (filter == WatchlistFilter.SERIES) return 2;
        return 0;
    }

    private WatchlistFilter indexToFilter(int index) {
        if (index == 1) return WatchlistFilter.MOVIES;
        if (index == 2) return WatchlistFilter.SERIES;
        return WatchlistFilter.ALL;
    }

    private int sortToIndex(DatabaseHelper.WatchlistSort sort) {
        if (sort == DatabaseHelper.WatchlistSort.TITLE_ASC) return 1;
        if (sort == DatabaseHelper.WatchlistSort.TITLE_DESC) return 2;
        if (sort == DatabaseHelper.WatchlistSort.SCORE_DESC) return 3;
        return 0;
    }

    private DatabaseHelper.WatchlistSort indexToSort(int index) {
        if (index == 1) return DatabaseHelper.WatchlistSort.TITLE_ASC;
        if (index == 2) return DatabaseHelper.WatchlistSort.TITLE_DESC;
        if (index == 3) return DatabaseHelper.WatchlistSort.SCORE_DESC;
        return DatabaseHelper.WatchlistSort.RECENT;
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
        DatabaseHelper.WatchlistSort currentSort = tabSortSelections[status.ordinal()];
        repository.getShowsByStatus(status, currentSort, shows -> {
            showLoading(false);
            List<Show> filteredShows = applyFilter(shows, tabFilterSelections[status.ordinal()]);
            if (filteredShows.isEmpty()) {
                showEmpty(true);
            } else {
                showEmpty(false);
                adapter.setShows(filteredShows);
            }
        });
    }

    private List<Show> applyFilter(List<Show> shows, WatchlistFilter filter) {
        if (shows == null || shows.isEmpty() || filter == WatchlistFilter.ALL) {
            return shows != null ? shows : new ArrayList<>();
        }
        List<Show> filtered = new ArrayList<>();
        for (Show show : shows) {
            if (show == null) {
                continue;
            }
            if (filter == WatchlistFilter.MOVIES && "movie".equalsIgnoreCase(show.getType())) {
                filtered.add(show);
            } else if (filter == WatchlistFilter.SERIES && "series".equalsIgnoreCase(show.getType())) {
                filtered.add(show);
            }
        }
        return filtered;
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
