package com.example.movietracker.model;

public enum WatchStatus {
    WATCHING("Watching"),
    PLANNED("Planned to Watch"),
    COMPLETED("Completed");

    private final String displayName;

    WatchStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static WatchStatus fromString(String status) {
        if (status == null) return null;
        for (WatchStatus ws : values()) {
            if (ws.name().equalsIgnoreCase(status)) {
                return ws;
            }
        }
        return null;
    }
}