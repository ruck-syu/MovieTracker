package com.example.movietracker.data.repository;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.example.movietracker.data.api.ApiClient;
import com.example.movietracker.data.api.OmdbApiService;
import com.example.movietracker.data.database.DatabaseHelper;
import com.example.movietracker.data.recommendation.LocalRecommendationEngine;
import com.example.movietracker.model.ProfileStats;
import com.example.movietracker.model.RecommendationItem;
import com.example.movietracker.model.SearchResponse;
import com.example.movietracker.model.SeasonResponse;
import com.example.movietracker.model.Show;
import com.example.movietracker.model.WatchStatus;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ShowRepository {
    private final OmdbApiService apiService;
    private final DatabaseHelper databaseHelper;
    private final LocalRecommendationEngine recommendationEngine;
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

    public interface EpisodeCountCallback {
        void onSuccess(int totalEpisodes);
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

    public interface TypeDistributionCallback {
        void onSuccess(java.util.Map<String, Integer> distribution);
    }

    public interface DistributionCallback {
        void onSuccess(java.util.Map<String, Integer> distribution);
    }

    public interface RecommendationsCallback {
        void onSuccess(List<RecommendationItem> recommendations);
        void onError(String error);
    }

    public interface ImportCallback {
        void onSuccess(int importedCount);
        void onError(String error);
    }

    public ShowRepository(Context context) {
        this.apiService = ApiClient.getApiService();
        this.databaseHelper = new DatabaseHelper(context);
        this.recommendationEngine = new LocalRecommendationEngine();
        this.executor = Executors.newSingleThreadExecutor();
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    public void searchShows(String query, String type, int page, SearchCallback callback) {
        apiService.getShowByTitle(query, ApiClient.API_KEY).enqueue(new Callback<Show>() {
            @Override
            public void onResponse(Call<Show> call, Response<Show> titleResponse) {
                Show exactMatch = null;
                if (titleResponse.isSuccessful() && titleResponse.body() != null) {
                    Show tempMatch = titleResponse.body();
                    if (tempMatch.getTitle() != null && tempMatch.getImdbId() != null) {
                        if (type == null || type.isEmpty() || type.equalsIgnoreCase(tempMatch.getType())) {
                            exactMatch = tempMatch;
                        }
                    }
                }
                
                final Show finalExactMatch = exactMatch;
                performSearchQuery(query, type, page, finalExactMatch, callback);
            }

            @Override
            public void onFailure(Call<Show> call, Throwable t) {
                performSearchQuery(query, type, page, null, callback);
            }
        });
    }

    private void performSearchQuery(String query, String type, int page, Show exactMatch, SearchCallback callback) {
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
                        mergeAndReturnResults(searchResponse.getSearch(), total, exactMatch, callback);
                    } else {
                        if ("Too many results.".equalsIgnoreCase(searchResponse.getError())) {
                            String fallbackQuery;
                            if ("movie".equalsIgnoreCase(type)) {
                                fallbackQuery = query + " movie";
                            } else if ("series".equalsIgnoreCase(type)) {
                                fallbackQuery = query + " series";
                            } else {
                                fallbackQuery = query + " movie";
                            }

                            apiService.searchShows(fallbackQuery, ApiClient.API_KEY, type, page).enqueue(new Callback<SearchResponse>() {
                                @Override
                                public void onResponse(Call<SearchResponse> call, Response<SearchResponse> fallbackResponse) {
                                    if (fallbackResponse.isSuccessful() && fallbackResponse.body() != null) {
                                        SearchResponse fallbackSearchResponse = fallbackResponse.body();
                                        if (fallbackSearchResponse.isSuccess()) {
                                            int total = 0;
                                            try {
                                                total = Integer.parseInt(fallbackSearchResponse.getTotalResults());
                                            } catch (NumberFormatException ignored) {}
                                            mergeAndReturnResults(fallbackSearchResponse.getSearch(), total, exactMatch, callback);
                                            return;
                                        }
                                    }

                                    if (type == null || type.isEmpty()) {
                                        apiService.searchShows(query + " series", ApiClient.API_KEY, type, page).enqueue(new Callback<SearchResponse>() {
                                            @Override
                                            public void onResponse(Call<SearchResponse> call, Response<SearchResponse> seriesResponse) {
                                                if (seriesResponse.isSuccessful() && seriesResponse.body() != null) {
                                                    SearchResponse seriesSearchResponse = seriesResponse.body();
                                                    if (seriesSearchResponse.isSuccess()) {
                                                        int total = 0;
                                                        try {
                                                            total = Integer.parseInt(seriesSearchResponse.getTotalResults());
                                                        } catch (NumberFormatException ignored) {}
                                                        mergeAndReturnResults(seriesSearchResponse.getSearch(), total, exactMatch, callback);
                                                        return;
                                                    }
                                                }
                                                handleSearchFailure(searchResponse.getError(), exactMatch, callback);
                                            }

                                            @Override
                                            public void onFailure(Call<SearchResponse> call, Throwable t) {
                                                handleSearchFailure(searchResponse.getError(), exactMatch, callback);
                                            }
                                        });
                                    } else {
                                        handleSearchFailure(searchResponse.getError(), exactMatch, callback);
                                    }
                                }

                                @Override
                                public void onFailure(Call<SearchResponse> call, Throwable t) {
                                    handleSearchFailure(searchResponse.getError(), exactMatch, callback);
                                }
                            });
                        } else {
                            handleSearchFailure(searchResponse.getError(), exactMatch, callback);
                        }
                    }
                } else {
                    handleSearchFailure("Failed to fetch results", exactMatch, callback);
                }
            }

            @Override
            public void onFailure(Call<SearchResponse> call, Throwable t) {
                handleSearchFailure("Network error: " + t.getMessage(), exactMatch, callback);
            }
        });
    }

    private void mergeAndReturnResults(List<Show> searchList, int totalResults, Show exactMatch, SearchCallback callback) {
        List<Show> mergedList = new ArrayList<>();
        int extraCount = 0;

        if (exactMatch != null) {
            mergedList.add(exactMatch);
            extraCount = 1;
        }

        if (searchList != null) {
            for (Show s : searchList) {
                if (exactMatch == null || !s.getImdbId().equalsIgnoreCase(exactMatch.getImdbId())) {
                    mergedList.add(s);
                }
            }
        }

        callback.onSuccess(mergedList, totalResults + extraCount);
    }

    private void handleSearchFailure(String error, Show exactMatch, SearchCallback callback) {
        if (exactMatch != null) {
            List<Show> singleList = new ArrayList<>();
            singleList.add(exactMatch);
            callback.onSuccess(singleList, 1);
        } else {
            callback.onError(error != null ? error : "No results found");
        }
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
                        show.setTotalEpisodes(estimateTotalEpisodes(show, trackedShow.getTotalEpisodes()));
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

    public void getTotalEpisodesCount(String imdbId, int totalSeasons, EpisodeCountCallback callback) {
        if (imdbId == null || imdbId.trim().isEmpty()) {
            callback.onError("Invalid IMDb id");
            return;
        }
        if (totalSeasons <= 0) {
            callback.onError("Episode data unavailable");
            return;
        }
        fetchSeasonEpisodeCount(imdbId, 1, totalSeasons, 0, callback);
    }

    private void fetchSeasonEpisodeCount(
        String imdbId,
        int currentSeason,
        int totalSeasons,
        int runningTotal,
        EpisodeCountCallback callback
    ) {
        apiService.getSeasonEpisodes(imdbId, currentSeason, ApiClient.API_KEY)
            .enqueue(new Callback<SeasonResponse>() {
                @Override
                public void onResponse(Call<SeasonResponse> call, Response<SeasonResponse> response) {
                    if (!response.isSuccessful() || response.body() == null) {
                        callback.onError("Episode data unavailable");
                        return;
                    }

                    SeasonResponse seasonResponse = response.body();
                    if (!seasonResponse.isSuccess() || seasonResponse.getEpisodes() == null) {
                        callback.onError(seasonResponse.getError() != null
                            ? seasonResponse.getError()
                            : "Episode data unavailable");
                        return;
                    }

                    int newTotal = runningTotal + seasonResponse.getEpisodes().size();
                    if (currentSeason >= totalSeasons) {
                        if (newTotal > 0) {
                            callback.onSuccess(newTotal);
                        } else {
                            callback.onError("Episode data unavailable");
                        }
                        return;
                    }

                    fetchSeasonEpisodeCount(imdbId, currentSeason + 1, totalSeasons, newTotal, callback);
                }

                @Override
                public void onFailure(Call<SeasonResponse> call, Throwable t) {
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
        getShowsByStatus(status, DatabaseHelper.WatchlistSort.RECENT, callback);
    }

    public void getShowsByStatus(WatchStatus status, DatabaseHelper.WatchlistSort sort, ListCallback callback) {
        executor.execute(() -> {
            List<Show> shows = databaseHelper.getShowsByStatus(status, sort);
            mainHandler.post(() -> callback.onSuccess(shows));
        });
    }

    public void getAllShows(ListCallback callback) {
        executor.execute(() -> {
            List<Show> shows = databaseHelper.getAllShows();
            mainHandler.post(() -> callback.onSuccess(shows));
        });
    }

    public void mergeImportedShows(List<Show> importedShows, ImportCallback callback) {
        executor.execute(() -> {
            try {
                if (importedShows == null || importedShows.isEmpty()) {
                    mainHandler.post(() -> callback.onError("Backup file has no tracked shows"));
                    return;
                }

                int importedCount = 0;
                for (Show show : importedShows) {
                    if (show == null || show.getImdbId() == null || show.getImdbId().trim().isEmpty()) {
                        continue;
                    }

                    WatchStatus status = show.getWatchStatus() != null
                        ? show.getWatchStatus()
                        : WatchStatus.PLANNED;
                    databaseHelper.insertShow(show, status);
                    importedCount++;
                }

                int finalImportedCount = importedCount;
                mainHandler.post(() -> callback.onSuccess(finalImportedCount));
            } catch (Exception e) {
                String error = e.getMessage() != null ? e.getMessage() : "Import failed";
                mainHandler.post(() -> callback.onError(error));
            }
        });
    }

    public void getProfileStats(ProfileStatsCallback callback) {
        executor.execute(() -> {
            ProfileStats stats = databaseHelper.getProfileStats();
            mainHandler.post(() -> callback.onSuccess(stats));
        });
    }

    public void getTopRatedShows(int limit, ListCallback callback) {
        executor.execute(() -> {
            List<Show> shows = databaseHelper.getTopRatedShows(limit);
            mainHandler.post(() -> callback.onSuccess(shows));
        });
    }

    public void getTypeDistribution(TypeDistributionCallback callback) {
        executor.execute(() -> {
            java.util.Map<String, Integer> distribution = databaseHelper.getTypeDistribution();
            mainHandler.post(() -> callback.onSuccess(distribution));
        });
    }

    public void getReleaseYearDistribution(DistributionCallback callback) {
        executor.execute(() -> {
            java.util.Map<String, Integer> distribution = databaseHelper.getReleaseYearDistribution();
            mainHandler.post(() -> callback.onSuccess(distribution));
        });
    }

    public void getLanguageDistribution(DistributionCallback callback) {
        executor.execute(() -> {
            java.util.Map<String, Integer> distribution = databaseHelper.getLanguageDistribution();
            mainHandler.post(() -> callback.onSuccess(distribution));
        });
    }

    public void getEpisodeCountDistribution(DistributionCallback callback) {
        executor.execute(() -> {
            java.util.Map<String, Integer> distribution = databaseHelper.getEpisodeCountDistribution();
            mainHandler.post(() -> callback.onSuccess(distribution));
        });
    }

    public void getRecommendations(int limit, RecommendationsCallback callback) {
        executor.execute(() -> {
            List<Show> trackedShows = databaseHelper.getAllShows();
            if (trackedShows == null || trackedShows.isEmpty()) {
                mainHandler.post(() -> callback.onError("Add a few titles to get recommendations"));
                return;
            }

            List<String> seedQueries = recommendationEngine.buildSeedQueries(trackedShows);
            Set<String> excludedImdbIds = new HashSet<>(databaseHelper.getTrackedImdbIds());
            int candidateTarget = Math.max(24, limit * 3);
            fetchCandidateIds(seedQueries, excludedImdbIds, candidateTarget, result -> {
                if (result.isEmpty()) {
                    mainHandler.post(() -> callback.onError("Could not find candidate titles right now"));
                    return;
                }
                fetchCandidateDetails(result, candidateTarget, hydratedShows -> {
                    executor.execute(() -> {
                        List<RecommendationItem> ranked = recommendationEngine.rankRecommendations(
                            trackedShows,
                            hydratedShows,
                            limit
                        );
                        if (ranked.isEmpty()) {
                            mainHandler.post(() -> callback.onError("Not enough matching titles yet"));
                        } else {
                            mainHandler.post(() -> callback.onSuccess(ranked));
                        }
                    });
                });
            });
        });
    }

    private interface CandidateIdsCallback {
        void onResult(List<String> imdbIds);
    }

    private interface CandidateDetailsCallback {
        void onResult(List<Show> shows);
    }

    private void fetchCandidateIds(
        List<String> seedQueries,
        Set<String> excludedImdbIds,
        int maxCandidates,
        CandidateIdsCallback callback
    ) {
        List<String> safeSeeds = new ArrayList<>(seedQueries);
        if (safeSeeds.isEmpty()) {
            safeSeeds.add("best movies");
            safeSeeds.add("best series");
        }

        Set<String> collectedIds = new HashSet<>();
        AtomicInteger pending = new AtomicInteger(safeSeeds.size());
        for (String seed : safeSeeds) {
            apiService.searchShows(seed, ApiClient.API_KEY, "", 1).enqueue(new Callback<SearchResponse>() {
                @Override
                public void onResponse(Call<SearchResponse> call, Response<SearchResponse> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        List<Show> searchItems = response.body().getSearch();
                        if (searchItems != null) {
                            for (Show item : searchItems) {
                                if (item == null || item.getImdbId() == null) {
                                    continue;
                                }
                                if (!excludedImdbIds.contains(item.getImdbId())) {
                                    collectedIds.add(item.getImdbId());
                                }
                                if (collectedIds.size() >= maxCandidates) {
                                    break;
                                }
                            }
                        }
                    }
                    maybeCompleteCandidateIdFetch(pending, collectedIds, callback, maxCandidates);
                }

                @Override
                public void onFailure(Call<SearchResponse> call, Throwable t) {
                    maybeCompleteCandidateIdFetch(pending, collectedIds, callback, maxCandidates);
                }
            });
        }
    }

    private void maybeCompleteCandidateIdFetch(
        AtomicInteger pending,
        Set<String> collectedIds,
        CandidateIdsCallback callback,
        int maxCandidates
    ) {
        if (pending.decrementAndGet() == 0) {
            List<String> ids = new ArrayList<>(collectedIds);
            if (ids.size() > maxCandidates) {
                callback.onResult(ids.subList(0, maxCandidates));
            } else {
                callback.onResult(ids);
            }
        }
    }

    private void fetchCandidateDetails(
        List<String> imdbIds,
        int maxCandidates,
        CandidateDetailsCallback callback
    ) {
        if (imdbIds.isEmpty()) {
            callback.onResult(new ArrayList<>());
            return;
        }

        int fetchLimit = Math.min(imdbIds.size(), maxCandidates);
        List<Show> hydrated = java.util.Collections.synchronizedList(new ArrayList<>());
        AtomicInteger pending = new AtomicInteger(fetchLimit);

        for (int i = 0; i < fetchLimit; i++) {
            String imdbId = imdbIds.get(i);
            apiService.getShowById(imdbId, ApiClient.API_KEY).enqueue(new Callback<Show>() {
                @Override
                public void onResponse(Call<Show> call, Response<Show> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        Show show = response.body();
                        if (show.getImdbId() != null) {
                            hydrated.add(show);
                        }
                    }
                    if (pending.decrementAndGet() == 0) {
                        callback.onResult(hydrated);
                    }
                }

                @Override
                public void onFailure(Call<Show> call, Throwable t) {
                    if (pending.decrementAndGet() == 0) {
                        callback.onResult(hydrated);
                    }
                }
            });
        }
    }

    private int estimateTotalEpisodes(Show show, int fallback) {
        if (fallback > 0) {
            return fallback;
        }
        if (show == null || !show.supportsEpisodeTracking()) {
            return 1;
        }
        String totalSeasonsText = show.getTotalSeasons();
        if (totalSeasonsText == null || totalSeasonsText.trim().isEmpty()) {
            return 0;
        }
        try {
            int seasons = Integer.parseInt(totalSeasonsText.trim());
            if (seasons <= 0) {
                return 0;
            }
            return seasons * 10;
        } catch (NumberFormatException ignored) {
            return 0;
        }
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
