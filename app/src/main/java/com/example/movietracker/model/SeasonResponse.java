package com.example.movietracker.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class SeasonResponse {
    @SerializedName("Episodes")
    private List<Episode> episodes;
    @SerializedName("Response")
    private String response;
    @SerializedName("Error")
    private String error;

    public List<Episode> getEpisodes() {
        return episodes;
    }

    public String getError() {
        return error;
    }

    public boolean isSuccess() {
        return "True".equalsIgnoreCase(response);
    }

    public static class Episode {
    }
}
