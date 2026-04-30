package com.example.movietracker.data.recommendation;

import com.example.movietracker.model.RecommendationItem;
import com.example.movietracker.model.Show;
import com.example.movietracker.model.WatchStatus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class LocalRecommendationEngine {
    private static final int MAX_SEEDS = 6;

    public List<String> buildSeedQueries(List<Show> trackedShows) {
        Map<String, Float> genreWeights = new HashMap<>();
        Map<String, Integer> titleTerms = new HashMap<>();
        Set<String> seen = new HashSet<>();
        List<String> seeds = new ArrayList<>();

        for (Show show : trackedShows) {
            if (!isPositiveSignal(show)) {
                continue;
            }

            float weight = getPreferenceWeight(show);
            for (String genre : splitGenres(show.getGenre())) {
                genreWeights.put(genre, genreWeights.getOrDefault(genre, 0f) + weight);
            }

            String title = show.getTitle();
            if (title == null) {
                continue;
            }
            String[] terms = title.toLowerCase(Locale.US).split("[^a-z0-9]+");
            for (String term : terms) {
                if (term.length() < 4) {
                    continue;
                }
                titleTerms.put(term, titleTerms.getOrDefault(term, 0) + 1);
            }
        }

        List<Map.Entry<String, Float>> sortedGenres = new ArrayList<>(genreWeights.entrySet());
        sortedGenres.sort((left, right) -> Float.compare(right.getValue(), left.getValue()));
        for (Map.Entry<String, Float> entry : sortedGenres) {
            String seed = entry.getKey();
            if (seen.add(seed)) {
                seeds.add(seed);
            }
            if (seeds.size() >= MAX_SEEDS) {
                return seeds;
            }
        }

        List<Map.Entry<String, Integer>> sortedTerms = new ArrayList<>(titleTerms.entrySet());
        sortedTerms.sort((left, right) -> Integer.compare(right.getValue(), left.getValue()));
        for (Map.Entry<String, Integer> entry : sortedTerms) {
            String seed = entry.getKey();
            if (seen.add(seed)) {
                seeds.add(seed);
            }
            if (seeds.size() >= MAX_SEEDS) {
                return seeds;
            }
        }

        if (seeds.isEmpty()) {
            seeds.add("best movies");
            seeds.add("best series");
        }

        return seeds;
    }

    public List<RecommendationItem> rankRecommendations(List<Show> trackedShows, List<Show> candidateShows, int limit) {
        Map<String, Float> genreWeights = new HashMap<>();
        Map<String, Float> typeWeights = new HashMap<>();

        for (Show show : trackedShows) {
            if (!isPositiveSignal(show)) {
                continue;
            }

            float weight = getPreferenceWeight(show);
            String type = normalize(show.getType());
            if (!type.isEmpty()) {
                typeWeights.put(type, typeWeights.getOrDefault(type, 0f) + weight);
            }
            for (String genre : splitGenres(show.getGenre())) {
                genreWeights.put(genre, genreWeights.getOrDefault(genre, 0f) + weight);
            }
        }

        List<RecommendationItem> scored = new ArrayList<>();
        for (Show candidate : candidateShows) {
            float score = 0f;

            String type = normalize(candidate.getType());
            score += typeWeights.getOrDefault(type, 0f) * 1.1f;

            float genreScore = 0f;
            String topGenre = "";
            for (String genre : splitGenres(candidate.getGenre())) {
                float weight = genreWeights.getOrDefault(genre, 0f);
                genreScore += weight;
                if (weight > 0f && topGenre.isEmpty()) {
                    topGenre = genre;
                }
            }
            score += genreScore * 1.4f;

            float imdb = parseImdbRating(candidate.getImdbRating());
            score += imdb * 0.35f;

            int year = parseYear(candidate.getYear());
            if (year >= 2018) {
                score += 0.6f;
            } else if (year >= 2010) {
                score += 0.3f;
            }

            if (score > 0f) {
                String reason = topGenre.isEmpty() ? "High match for your ratings" : "Because you like " + topGenre;
                scored.add(new RecommendationItem(candidate, score, reason));
            }
        }

        scored.sort((left, right) -> Float.compare(right.getScore(), left.getScore()));
        if (scored.size() > limit) {
            return new ArrayList<>(scored.subList(0, limit));
        }
        return scored;
    }

    private boolean isPositiveSignal(Show show) {
        if (show == null) {
            return false;
        }
        if (show.hasUserScore() && show.getUserScore() >= 6f) {
            return true;
        }
        return show.getWatchStatus() == WatchStatus.COMPLETED || show.getWatchStatus() == WatchStatus.WATCHING;
    }

    private float getPreferenceWeight(Show show) {
        float weight = 1f;
        if (show.hasUserScore()) {
            weight += Math.max(0f, show.getUserScore() - 5f) / 2f;
        }
        if (show.getWatchStatus() == WatchStatus.COMPLETED) {
            weight += 0.4f;
        } else if (show.getWatchStatus() == WatchStatus.WATCHING) {
            weight += 0.2f;
        }
        return weight;
    }

    private List<String> splitGenres(String genreText) {
        if (genreText == null || genreText.trim().isEmpty()) {
            return Collections.emptyList();
        }
        List<String> genres = new ArrayList<>();
        String[] parts = genreText.toLowerCase(Locale.US).split(",");
        for (String part : parts) {
            String genre = normalize(part);
            if (!genre.isEmpty()) {
                genres.add(genre);
            }
        }
        return genres;
    }

    private String normalize(String text) {
        return text == null ? "" : text.trim().toLowerCase(Locale.US);
    }

    private float parseImdbRating(String rating) {
        if (rating == null || rating.trim().isEmpty() || "N/A".equalsIgnoreCase(rating)) {
            return 0f;
        }
        try {
            return Float.parseFloat(rating.trim());
        } catch (NumberFormatException ignored) {
            return 0f;
        }
    }

    private int parseYear(String yearText) {
        if (yearText == null || yearText.trim().isEmpty()) {
            return 0;
        }
        String[] split = yearText.trim().split("[^0-9]");
        if (split.length == 0 || split[0].isEmpty()) {
            return 0;
        }
        try {
            return Integer.parseInt(split[0]);
        } catch (NumberFormatException ignored) {
            return 0;
        }
    }
}
