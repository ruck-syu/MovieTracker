package com.example.movietracker.ui.search;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.movietracker.R;
import com.example.movietracker.data.repository.ShowRepository;
import com.example.movietracker.model.Show;
import com.example.movietracker.ui.adapter.ShowAdapter;
import com.example.movietracker.ui.detail.DetailActivity;
import com.example.movietracker.util.NetworkUtils;

import java.util.ArrayList;
import java.util.List;

public class SearchFragment extends Fragment {
    private EditText etSearch;
    private Spinner spType;
    private RecyclerView rvResults;
    private ProgressBar progressBar;
    private TextView tvNoResults;

    private ShowAdapter adapter;
    private ShowRepository repository;
    private String currentType = "";

    public SearchFragment() {
        super(R.layout.fragment_search);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        repository = new ShowRepository(requireContext());

        etSearch = view.findViewById(R.id.etSearch);
        spType = view.findViewById(R.id.spType);
        Button btnSearch = view.findViewById(R.id.btnSearch);
        rvResults = view.findViewById(R.id.rvResults);
        progressBar = view.findViewById(R.id.progressBar);
        tvNoResults = view.findViewById(R.id.tvNoResults);

        setupRecyclerView();
        setupSpinner();
        btnSearch.setOnClickListener(v -> performSearch());
    }

    private void setupRecyclerView() {
        adapter = new ShowAdapter();
        adapter.setOnShowClickListener(this::openDetail);
        rvResults.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvResults.setAdapter(adapter);
    }

    private void setupSpinner() {
        List<String> types = new ArrayList<>();
        types.add("All");
        types.add("Movie");
        types.add("Series");
        types.add("Episode");

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
            requireContext(), android.R.layout.simple_spinner_item, types);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spType.setAdapter(spinnerAdapter);

        spType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentType = position == 0 ? "" : types.get(position).toLowerCase();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void performSearch() {
        if (getView() != null) {
            InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
            }
        }

        String query = etSearch.getText().toString().trim();
        if (query.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter a search term", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!NetworkUtils.isNetworkAvailable(requireContext())) {
            Toast.makeText(requireContext(), "No internet connection", Toast.LENGTH_SHORT).show();
            return;
        }

        showLoading(true);
        repository.searchShows(query, currentType, 1, new ShowRepository.SearchCallback() {
            @Override
            public void onSuccess(List<Show> shows, int totalResults) {
                showLoading(false);
                if (shows.isEmpty()) {
                    showNoResults(true);
                } else {
                    showNoResults(false);
                    adapter.setShows(shows);
                }
            }

            @Override
            public void onError(String error) {
                showLoading(false);
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
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
        rvResults.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    private void showNoResults(boolean show) {
        tvNoResults.setVisibility(show ? View.VISIBLE : View.GONE);
        rvResults.setVisibility(show ? View.GONE : View.VISIBLE);
    }
}
