package com.example.movietracker.model;

public class RecommendationItem {
    private final Show show;
    private final float score;
    private final String reason;

    public RecommendationItem(Show show, float score, String reason) {
        this.show = show;
        this.score = score;
        this.reason = reason;
    }

    public Show getShow() {
        return show;
    }

    public float getScore() {
        return score;
    }

    public String getReason() {
        return reason;
    }
}
