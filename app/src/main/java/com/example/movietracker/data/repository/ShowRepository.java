package com.example.movietracker.data.repository;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.example.movietracker.data.api.ApiClient;
import com.example.movietracker.data.api.OmdbApiService;
import com.example.movietracker.data.database.DatabaseHelper;
import com.example.movietracker.model.ProfileStats;
import com.example.movietracker.model.SearchResponse;
import com.example.movietracker.model.Show;
import com.example.movietracker.model.WatchStatus;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ShowRepository {
    private final OmdbApiService apiService;
    private final DatabaseHelper databaseHelper;
    private final ExecutorService executor;
    private final Handler mainHandler;

    public interface SearchCallback {
        void onSuccess(List<Show> shows, int totalResults);
        void onError(String error);
    }

    public interface DetailCallback {
        void onSuccess(Show show);
        void onError(String error);
    }

    public interface DbOperationCallback {
        void onSuccess();
        void onError(String error);
    }

    public interface ListCallback {
        void onSuccess(List<Show> shows);
    }

    public interface ProfileStatsCallback {
        void onSuccess(ProfileStats stats);
    }

    public ShowRepository(Context context) {
        this.apiService = ApiClient.getApiService();
        this.databaseHelper = new DatabaseHelper(context);
        this.executor = Executors.newSingleThreadExecutor();
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    public void searchShows(String query, String type, int page, SearchCallback callback) {
        apiService.searchShows(query, ApiClient.API_KEY, type, page).enqueue(new Callback<SearchResponse>() {
            @Override
            public void onResponse(Call<SearchResponse> call, Response<SearchResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    SearchResponse searchResponse = response.body();
                    if (searchResponse.isSuccess()) {
                        int total = 0;
                        try {
                            total = Integer.parseInt(searchResponse.getTotalResults());
                        } catch (NumberFormatException ignored) {}
                        callback.onSuccess(searchResponse.getSearch(), total);
                    } else {
                        callback.onError(searchResponse.getError() != null ?
                            searchResponse.getError() : "No results found");
                    }
                } else {
                    callback.onError("Failed to fetch results");
                }
            }

            @Override
            public void onFailure(Call<SearchResponse> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    public void getShowDetails(String imdbId, DetailCallback callback) {
        apiService.getShowById(imdbId, ApiClient.API_KEY).enqueue(new Callback<Show>() {
            @Override
            public void onResponse(Call<Show> call, Response<Show> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Show show = response.body();
                    Show trackedShow = databaseHelper.getShowById(show.getImdbId());
                    if (trackedShow != null) {
                        show.setWatchStatus(trackedShow.getWatchStatus());
                        show.setEpisodeProgress(trackedShow.getEpisodeProgress());
                        show.setUserScore(trackedShow.getUserScore());
                        databaseHelper.updateShowDetails(show);
                    }
                    callback.onSuccess(show);
                } else {
                    callback.onError("Failed to fetch show details");
                }
            }

            @Override
            public void onFailure(Call<Show> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    public void addToList(Show show, WatchStatus status, DbOperationCallback callback) {
        executor.execute(() -> {
            try {
                databaseHelper.insertShow(show, status);
                mainHandler.post(callback::onSuccess);
            } catch (Exception e) {
                final String error = e.getMessage();
                mainHandler.post(() -> callback.onError(error));
            }
        });
    }

    public void updateStatus(Show show, WatchStatus status, DbOperationCallback callback) {
        executor.execute(() -> {
            try {
                if (databaseHelper.isShowInList(show.getImdbId())) {
                    databaseHelper.updateTrackedShow(show, status);
                } else {
                    databaseHelper.insertShow(show, status);
                }
                mainHandler.post(callback::onSuccess);
            } catch (Exception e) {
                final String error = e.getMessage();
                mainHandler.post(() -> callback.onError(error));
            }
        });
    }

    public void removeFromList(String imdbId, DbOperationCallback callback) {
        executor.execute(() -> {
            try {
                databaseHelper.deleteShow(imdbId);
                mainHandler.post(callback::onSuccess);
            } catch (Exception e) {
                final String error = e.getMessage();
                mainHandler.post(() -> callback.onError(error));
            }
        });
    }

    public void getShowsByStatus(WatchStatus status, ListCallback callback) {
        executor.execute(() -> {
            List<Show> shows = databaseHelper.getShowsByStatus(status);
            mainHandler.post(() -> callback.onSuccess(shows));
        });
    }

    public void getAllShows(ListCallback callback) {
        executor.execute(() -> {
            List<Show> shows = databaseHelper.getAllShows();
            mainHandler.post(() -> callback.onSuccess(shows));
        });
    }

    public void getProfileStats(ProfileStatsCallback callback) {
        executor.execute(() -> {
            ProfileStats stats = databaseHelper.getProfileStats();
            mainHandler.post(() -> callback.onSuccess(stats));
        });
    }

    public void isInList(String imdbId, IsInListCallback callback) {
        executor.execute(() -> {
            boolean exists = databaseHelper.isShowInList(imdbId);
            WatchStatus status = databaseHelper.getShowStatus(imdbId);
            mainHandler.post(() -> callback.onResult(exists, status));
        });
    }

    public interface IsInListCallback {
        void onResult(boolean exists, WatchStatus status);
    }
}
