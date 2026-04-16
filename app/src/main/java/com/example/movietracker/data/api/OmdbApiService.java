package com.example.movietracker.data.api;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
import com.example.movietracker.model.SearchResponse;
import com.example.movietracker.model.SeasonResponse;
import com.example.movietracker.model.Show;

public interface OmdbApiService {
    @GET("/")
    Call<SearchResponse> searchShows(
        @Query("s") String query,
        @Query("apikey") String apiKey,
        @Query("type") String type,
        @Query("page") int page
    );

    @GET("/")
    Call<Show> getShowById(
        @Query("i") String imdbId,
        @Query("apikey") String apiKey
    );

    @GET("/")
    Call<Show> getShowByTitle(
        @Query("t") String title,
        @Query("apikey") String apiKey
    );

    @GET("/")
    Call<SeasonResponse> getSeasonEpisodes(
        @Query("i") String imdbId,
        @Query("Season") int season,
        @Query("apikey") String apiKey
    );
}
