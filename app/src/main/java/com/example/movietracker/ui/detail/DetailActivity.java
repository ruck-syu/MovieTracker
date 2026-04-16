package com.example.movietracker.ui.detail;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.example.movietracker.R;
import com.example.movietracker.data.repository.ShowRepository;
import com.example.movietracker.model.Show;
import com.example.movietracker.model.WatchStatus;
import com.example.movietracker.util.NetworkUtils;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.slider.Slider;

import java.util.Locale;

public class DetailActivity extends AppCompatActivity {
    public static final String EXTRA_IMDB_ID = "extra_imdb_id";

    private ImageView ivPoster;
    private TextView tvTitle;
    private TextView tvYear;
    private TextView tvType;
    private TextView tvRating;
    private TextView tvRuntime;
    private TextView tvGenre;
    private TextView tvDirector;
    private TextView tvWriter;
    private TextView tvActors;
    private TextView tvPlot;
    private TextView tvTrackingStatus;
    private View layoutTrackingEpisodes;
    private TextView tvTrackingEpisodes;
    private TextView tvTrackingScore;
    private TextView tvManageTracking;
    private ProgressBar progressBar;
    private View contentLayout;

    private ShowRepository repository;
    private Show currentShow;
    private boolean isInList = false;
    private WatchStatus currentStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        repository = new ShowRepository(this);

        initViews();
        loadShowDetails();
    }

    private void initViews() {
        ivPoster = findViewById(R.id.ivPoster);
        tvTitle = findViewById(R.id.tvTitle);
        tvYear = findViewById(R.id.tvYear);
        tvType = findViewById(R.id.tvType);
        tvRating = findViewById(R.id.tvRating);
        tvRuntime = findViewById(R.id.tvRuntime);
        tvGenre = findViewById(R.id.tvGenre);
        tvDirector = findViewById(R.id.tvDirector);
        tvWriter = findViewById(R.id.tvWriter);
        tvActors = findViewById(R.id.tvActors);
        tvPlot = findViewById(R.id.tvPlot);
        tvTrackingStatus = findViewById(R.id.tvTrackingStatus);
        layoutTrackingEpisodes = findViewById(R.id.layoutTrackingEpisodes);
        tvTrackingEpisodes = findViewById(R.id.tvTrackingEpisodes);
        tvTrackingScore = findViewById(R.id.tvTrackingScore);
        tvManageTracking = findViewById(R.id.tvAddToList);
        progressBar = findViewById(R.id.progressBar);
        contentLayout = findViewById(R.id.contentLayout);

        tvManageTracking.setOnClickListener(v -> showTrackingBottomSheet());
    }

    private void loadShowDetails() {
        String imdbId = getIntent().getStringExtra(EXTRA_IMDB_ID);
        if (imdbId == null) {
            Toast.makeText(this, "Invalid show", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if (!NetworkUtils.isNetworkAvailable(this)) {
            Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show();
            return;
        }

        showLoading(true);
        repository.getShowDetails(imdbId, new ShowRepository.DetailCallback() {
            @Override
            public void onSuccess(Show show) {
                showLoading(false);
                currentShow = show;
                currentStatus = show.getWatchStatus();
                isInList = currentStatus != null;
                displayShow(show);
                updateTrackingSection();
                loadEpisodeCapIfAvailable(show);
            }

            @Override
            public void onError(String error) {
                showLoading(false);
                Toast.makeText(DetailActivity.this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayShow(Show show) {
        tvTitle.setText(show.getTitle());
        tvYear.setText(show.getYear());
        tvType.setText(show.getDisplayType());
        tvRating.setText(show.getIMDbRating() + "/10");
        tvRuntime.setText(show.getRuntime() != null ? show.getRuntime() : "N/A");
        tvGenre.setText(show.getGenre() != null ? show.getGenre() : "N/A");
        tvDirector.setText(show.getDirector() != null ? show.getDirector() : "N/A");
        tvWriter.setText(show.getWriter() != null ? show.getWriter() : "N/A");
        tvActors.setText(show.getActors() != null ? show.getActors() : "N/A");
        tvPlot.setText(show.getPlot() != null ? show.getPlot() : "N/A");

        if (show.getPoster() != null && !show.getPoster().equals("N/A")) {
            Glide.with(this)
                .load(show.getPoster())
                .transition(DrawableTransitionOptions.withCrossFade())
                .placeholder(R.drawable.ic_placeholder)
                .error(R.drawable.ic_placeholder)
                .into(ivPoster);
        } else {
            ivPoster.setImageResource(R.drawable.ic_placeholder);
        }
    }

    private void updateTrackingSection() {
        if (currentShow == null) {
            return;
        }

        if (isInList && currentStatus != null) {
            tvTrackingStatus.setText(currentStatus.getDisplayName());
            tvManageTracking.setText(R.string.detail_edit_tracking);
        } else {
            tvTrackingStatus.setText(R.string.detail_tracking_not_added);
            tvManageTracking.setText(R.string.detail_add_to_my_list);
        }

        if (currentShow.supportsEpisodeTracking()) {
            layoutTrackingEpisodes.setVisibility(View.VISIBLE);
            if (currentShow.hasEpisodeCap()) {
                tvTrackingEpisodes.setText(getString(
                    R.string.detail_tracking_episodes_value_capped,
                    currentShow.getEpisodeProgress(),
                    currentShow.getTotalEpisodes()));
            } else {
                tvTrackingEpisodes.setText(getString(
                    R.string.detail_tracking_episodes_value,
                    currentShow.getEpisodeProgress()));
            }
        } else {
            layoutTrackingEpisodes.setVisibility(View.GONE);
        }

        if (currentShow.hasUserScore()) {
            tvTrackingScore.setText(getString(
                R.string.detail_tracking_score_value,
                String.format(Locale.getDefault(), "%.1f", currentShow.getUserScore())));
        } else {
            tvTrackingScore.setText(R.string.detail_tracking_no_score);
        }
    }

    private void showTrackingBottomSheet() {
        if (currentShow == null) {
            return;
        }

        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View sheetView = LayoutInflater.from(this).inflate(R.layout.bottom_sheet_tracking, null);
        dialog.setContentView(sheetView);

        Spinner spSheetStatus = sheetView.findViewById(R.id.spSheetStatus);
        View layoutEpisodeControls = sheetView.findViewById(R.id.layoutEpisodeControls);
        TextView tvEpisodeCount = sheetView.findViewById(R.id.tvEpisodeCount);
        TextView tvSheetScoreValue = sheetView.findViewById(R.id.tvSheetScoreValue);
        Slider sliderScore = sheetView.findViewById(R.id.sliderScore);
        Button btnDecreaseEpisode = sheetView.findViewById(R.id.btnDecreaseEpisode);
        Button btnIncreaseEpisode = sheetView.findViewById(R.id.btnIncreaseEpisode);
        Button btnSaveTracking = sheetView.findViewById(R.id.btnSaveTracking);
        Button btnRemoveFromList = sheetView.findViewById(R.id.btnRemoveFromList);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
            this,
            android.R.layout.simple_spinner_item,
            new String[]{
                WatchStatus.WATCHING.getDisplayName(),
                WatchStatus.PLANNED.getDisplayName(),
                WatchStatus.COMPLETED.getDisplayName()
            });
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spSheetStatus.setAdapter(adapter);

        WatchStatus selectedStatus = currentStatus != null ? currentStatus : WatchStatus.PLANNED;
        spSheetStatus.setSelection(selectedStatus.ordinal());

        final int[] episodeCount = {clampEpisodeCount(currentShow.getEpisodeProgress(), currentShow)};
        if (currentShow.supportsEpisodeTracking()) {
            layoutEpisodeControls.setVisibility(View.VISIBLE);
            updateEpisodeCountLabel(tvEpisodeCount, episodeCount[0], currentShow);
        } else {
            layoutEpisodeControls.setVisibility(View.GONE);
        }

        btnDecreaseEpisode.setOnClickListener(v -> {
            if (episodeCount[0] > 0) {
                episodeCount[0]--;
                updateEpisodeCountLabel(tvEpisodeCount, episodeCount[0], currentShow);
            }
        });

        btnIncreaseEpisode.setOnClickListener(v -> {
            if (!currentShow.hasEpisodeCap() || episodeCount[0] < currentShow.getTotalEpisodes()) {
                episodeCount[0]++;
                updateEpisodeCountLabel(tvEpisodeCount, episodeCount[0], currentShow);
            }
        });

        float initialScore = currentShow.hasUserScore() ? currentShow.getUserScore() : 0f;
        sliderScore.setValue(initialScore);
        updateScoreValueLabel(tvSheetScoreValue, initialScore);
        sliderScore.addOnChangeListener((slider, value, fromUser) -> updateScoreValueLabel(tvSheetScoreValue, value));

        btnSaveTracking.setText(isInList ? R.string.detail_sheet_save_changes : R.string.detail_sheet_add_to_list);
        btnSaveTracking.setOnClickListener(v -> {
            WatchStatus status = WatchStatus.values()[spSheetStatus.getSelectedItemPosition()];
            currentShow.setWatchStatus(status);
            currentShow.setEpisodeProgress(currentShow.supportsEpisodeTracking()
                ? clampEpisodeCount(episodeCount[0], currentShow)
                : 0);

            float sliderValue = sliderScore.getValue();
            currentShow.setUserScore(sliderValue > 0f ? sliderValue : null);

            saveTracking(status, dialog);
        });

        btnRemoveFromList.setVisibility(isInList ? View.VISIBLE : View.GONE);
        btnRemoveFromList.setOnClickListener(v -> {
            dialog.dismiss();
            removeFromList();
        });

        dialog.show();
    }

    private void saveTracking(WatchStatus status, BottomSheetDialog dialog) {
        ShowRepository.DbOperationCallback callback = new ShowRepository.DbOperationCallback() {
            @Override
            public void onSuccess() {
                int messageRes = isInList ? R.string.detail_status_updated : R.string.detail_added_to_list;
                Toast.makeText(DetailActivity.this, messageRes, Toast.LENGTH_SHORT).show();
                isInList = true;
                currentStatus = status;
                currentShow.setWatchStatus(status);
                updateTrackingSection();
                dialog.dismiss();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(DetailActivity.this, error, Toast.LENGTH_SHORT).show();
            }
        };

        if (isInList) {
            repository.updateStatus(currentShow, status, callback);
        } else {
            repository.addToList(currentShow, status, callback);
        }
    }

    private void updateEpisodeCountLabel(TextView textView, int count, Show show) {
        if (show != null && show.hasEpisodeCap()) {
            textView.setText(getString(
                R.string.detail_sheet_episode_counter_capped,
                count,
                show.getTotalEpisodes()));
            return;
        }
        textView.setText(getString(R.string.detail_sheet_episode_counter, count));
    }

    private void updateScoreValueLabel(TextView textView, float value) {
        if (value <= 0f) {
            textView.setText(R.string.detail_sheet_score_empty);
        } else {
            textView.setText(getString(
                R.string.detail_sheet_score_value,
                String.format(Locale.getDefault(), "%.1f", value)));
        }
    }

    private void removeFromList() {
        if (currentShow == null) return;

        new AlertDialog.Builder(this)
            .setTitle("Remove from list")
            .setMessage("Remove this show from your list?")
            .setPositiveButton("Yes", (dialog, which) -> {
                repository.removeFromList(currentShow.getImdbId(), new ShowRepository.DbOperationCallback() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(DetailActivity.this, R.string.detail_removed_from_list, Toast.LENGTH_SHORT).show();
                        isInList = false;
                        currentStatus = null;
                        currentShow.setWatchStatus(null);
                        currentShow.setEpisodeProgress(0);
                        currentShow.setUserScore(null);
                        updateTrackingSection();
                    }

                    @Override
                    public void onError(String error) {
                        Toast.makeText(DetailActivity.this, error, Toast.LENGTH_SHORT).show();
                    }
                });
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        contentLayout.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    private void loadEpisodeCapIfAvailable(Show show) {
        if (show == null || !show.supportsEpisodeTracking()) {
            return;
        }

        int totalSeasons = parseTotalSeasons(show.getTotalSeasons());
        if (totalSeasons <= 0) {
            return;
        }

        repository.getTotalEpisodesCount(show.getImdbId(), totalSeasons, new ShowRepository.EpisodeCountCallback() {
            @Override
            public void onSuccess(int totalEpisodes) {
                if (currentShow == null || !show.getImdbId().equals(currentShow.getImdbId())) {
                    return;
                }
                currentShow.setTotalEpisodes(totalEpisodes);
                currentShow.setEpisodeProgress(clampEpisodeCount(currentShow.getEpisodeProgress(), currentShow));
                updateTrackingSection();
            }

            @Override
            public void onError(String error) {
                if (currentShow == null || !show.getImdbId().equals(currentShow.getImdbId())) {
                    return;
                }
                currentShow.setTotalEpisodes(0);
            }
        });
    }

    private int parseTotalSeasons(String totalSeasonsText) {
        if (totalSeasonsText == null || totalSeasonsText.trim().isEmpty()) {
            return 0;
        }
        try {
            return Math.max(0, Integer.parseInt(totalSeasonsText.trim()));
        } catch (NumberFormatException ignored) {
            return 0;
        }
    }

    private int clampEpisodeCount(int value, Show show) {
        int safeValue = Math.max(0, value);
        if (show != null && show.hasEpisodeCap()) {
            return Math.min(safeValue, show.getTotalEpisodes());
        }
        return safeValue;
    }
}
