package com.example.movietracker.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class SearchResponse {
    @SerializedName("Search")
    private List<Show> search;
    @SerializedName("totalResults")
    private String totalResults;
    @SerializedName("Response")
    private String response;
    @SerializedName("Error")
    private String error;

    public List<Show> getSearch() { return search; }
    public String getTotalResults() { return totalResults; }
    public String getResponse() { return response; }
    public String getError() { return error; }
    public boolean isSuccess() { return "True".equalsIgnoreCase(response); }
}