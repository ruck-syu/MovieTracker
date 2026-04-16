package com.example.movietracker.model;

import com.google.gson.annotations.SerializedName;

public class Show {
    @SerializedName("imdbID")
    private String imdbId;
    @SerializedName("Title")
    private String title;
    @SerializedName("Year")
    private String year;
    @SerializedName("Type")
    private String type;
    @SerializedName("Poster")
    private String poster;
    @SerializedName("Rated")
    private String rated;
    @SerializedName("Released")
    private String released;
    @SerializedName("Runtime")
    private String runtime;
    @SerializedName("Genre")
    private String genre;
    @SerializedName("Director")
    private String director;
    @SerializedName("Writer")
    private String writer;
    @SerializedName("Actors")
    private String actors;
    @SerializedName("Plot")
    private String plot;
    @SerializedName("imdbRating")
    private String imdbRating;
    @SerializedName("totalSeasons")
    private String totalSeasons;

    private WatchStatus watchStatus;
    private long dateAdded;
    private int episodeProgress;
    private Float userScore;

    public Show() {}

    public String getImdbId() { return imdbId; }
    public void setImdbId(String imdbId) { this.imdbId = imdbId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getYear() { return year; }
    public void setYear(String year) { this.year = year; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getPoster() { return poster; }
    public void setPoster(String poster) { this.poster = poster; }
    public String getRated() { return rated; }
    public void setRated(String rated) { this.rated = rated; }
    public String getReleased() { return released; }
    public void setReleased(String released) { this.released = released; }
    public String getRuntime() { return runtime; }
    public void setRuntime(String runtime) { this.runtime = runtime; }
    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }
    public String getDirector() { return director; }
    public void setDirector(String director) { this.director = director; }
    public String getWriter() { return writer; }
    public void setWriter(String writer) { this.writer = writer; }
    public String getActors() { return actors; }
    public void setActors(String actors) { this.actors = actors; }
    public String getPlot() { return plot; }
    public void setPlot(String plot) { this.plot = plot; }
    public String getImdbRating() { return imdbRating; }
    public void setImdbRating(String imdbRating) { this.imdbRating = imdbRating; }
    public String getTotalSeasons() { return totalSeasons; }
    public void setTotalSeasons(String totalSeasons) { this.totalSeasons = totalSeasons; }
    public WatchStatus getWatchStatus() { return watchStatus; }
    public void setWatchStatus(WatchStatus watchStatus) { this.watchStatus = watchStatus; }
    public long getDateAdded() { return dateAdded; }
    public void setDateAdded(long dateAdded) { this.dateAdded = dateAdded; }
    public int getEpisodeProgress() { return episodeProgress; }
    public void setEpisodeProgress(int episodeProgress) { this.episodeProgress = Math.max(0, episodeProgress); }
    public Float getUserScore() { return userScore; }
    public void setUserScore(Float userScore) { this.userScore = userScore; }
    public String getDisplayType() {
        if (type == null) return "N/A";
        return type.substring(0, 1).toUpperCase() + type.substring(1);
    }
    public String getIMDbRating() {
        return imdbRating != null ? imdbRating : "N/A";
    }
    public boolean hasUserScore() {
        return userScore != null && userScore > 0f;
    }
    public boolean supportsEpisodeTracking() {
        return "series".equalsIgnoreCase(type);
    }
}
