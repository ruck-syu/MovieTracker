package com.example.movietracker.model;

public class ProfileStats {
    private int totalTracked;
    private int watchingCount;
    private int plannedCount;
    private int completedCount;
    private int ratedCompletedCount;
    private double averageRating;

    public int getTotalTracked() {
        return totalTracked;
    }

    public void setTotalTracked(int totalTracked) {
        this.totalTracked = totalTracked;
    }

    public int getWatchingCount() {
        return watchingCount;
    }

    public void setWatchingCount(int watchingCount) {
        this.watchingCount = watchingCount;
    }

    public int getPlannedCount() {
        return plannedCount;
    }

    public void setPlannedCount(int plannedCount) {
        this.plannedCount = plannedCount;
    }

    public int getCompletedCount() {
        return completedCount;
    }

    public void setCompletedCount(int completedCount) {
        this.completedCount = completedCount;
    }

    public int getRatedCompletedCount() {
        return ratedCompletedCount;
    }

    public void setRatedCompletedCount(int ratedCompletedCount) {
        this.ratedCompletedCount = ratedCompletedCount;
    }

    public double getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(double averageRating) {
        this.averageRating = averageRating;
    }

    public boolean hasAverageRating() {
        return ratedCompletedCount > 0;
    }
}
