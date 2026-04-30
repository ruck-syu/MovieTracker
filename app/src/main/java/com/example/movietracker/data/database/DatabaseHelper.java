package com.example.movietracker.data.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.example.movietracker.model.ProfileStats;
import com.example.movietracker.model.Show;
import com.example.movietracker.model.WatchStatus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class DatabaseHelper extends SQLiteOpenHelper {
    public enum WatchlistSort {
        RECENT,
        TITLE_ASC,
        TITLE_DESC,
        SCORE_DESC
    }
    private static final int DATABASE_VERSION = 5;
    private static final String TABLE_SHOWS = "shows";
    private static final String COL_IMDB_ID = "imdb_id";
    private static final String COL_TITLE = "title";
    private static final String COL_YEAR = "year";
    private static final String COL_POSTER = "poster";
    private static final String COL_TYPE = "type";
    private static final String COL_GENRE = "genre";
    private static final String COL_LANGUAGE = "language";
    private static final String COL_IMDB_RATING = "imdb_rating";
    private static final String COL_STATUS = "status";
    private static final String COL_EPISODE_PROGRESS = "episode_progress";
    private static final String COL_TOTAL_EPISODES = "total_episodes";
    private static final String COL_USER_SCORE = "user_score";
    private static final String COL_DATE_ADDED = "date_added";

    private static final String CREATE_TABLE_SHOWS =
        "CREATE TABLE " + TABLE_SHOWS + " (" +
        COL_IMDB_ID + " TEXT PRIMARY KEY, " +
        COL_TITLE + " TEXT, " +
        COL_YEAR + " TEXT, " +
        COL_POSTER + " TEXT, " +
        COL_TYPE + " TEXT, " +
        COL_GENRE + " TEXT, " +
        COL_LANGUAGE + " TEXT, " +
        COL_IMDB_RATING + " TEXT, " +
        COL_STATUS + " TEXT, " +
        COL_EPISODE_PROGRESS + " INTEGER DEFAULT 0, " +
        COL_TOTAL_EPISODES + " INTEGER DEFAULT 0, " +
        COL_USER_SCORE + " REAL, " +
        COL_DATE_ADDED + " INTEGER)";

    private static String getUidDatabaseName() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            return "MovieTracker_" + user.getUid() + ".db";
        }
        return "MovieTracker_default.db";
    }

    public DatabaseHelper(Context context) {
        super(context, getUidDatabaseName(), null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_SHOWS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE " + TABLE_SHOWS + " ADD COLUMN " + COL_IMDB_RATING + " TEXT");
        }
        if (oldVersion < 3) {
            db.execSQL("ALTER TABLE " + TABLE_SHOWS + " ADD COLUMN " + COL_EPISODE_PROGRESS + " INTEGER DEFAULT 0");
            db.execSQL("ALTER TABLE " + TABLE_SHOWS + " ADD COLUMN " + COL_USER_SCORE + " REAL");
        }
        if (oldVersion < 4) {
            db.execSQL("ALTER TABLE " + TABLE_SHOWS + " ADD COLUMN " + COL_GENRE + " TEXT");
        }
        if (oldVersion < 5) {
            db.execSQL("ALTER TABLE " + TABLE_SHOWS + " ADD COLUMN " + COL_LANGUAGE + " TEXT");
            db.execSQL("ALTER TABLE " + TABLE_SHOWS + " ADD COLUMN " + COL_TOTAL_EPISODES + " INTEGER DEFAULT 0");
        }
    }

    public long insertShow(Show show, WatchStatus status) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_IMDB_ID, show.getImdbId());
        values.put(COL_TITLE, show.getTitle());
        values.put(COL_YEAR, show.getYear());
        values.put(COL_POSTER, show.getPoster());
        values.put(COL_TYPE, show.getType());
        values.put(COL_GENRE, show.getGenre());
        values.put(COL_LANGUAGE, show.getLanguage());
        values.put(COL_IMDB_RATING, show.getImdbRating());
        values.put(COL_STATUS, status.name());
        values.put(COL_EPISODE_PROGRESS, show.getEpisodeProgress());
        values.put(COL_TOTAL_EPISODES, show.getTotalEpisodes());
        if (show.getUserScore() != null) {
            values.put(COL_USER_SCORE, show.getUserScore());
        } else {
            values.putNull(COL_USER_SCORE);
        }
        values.put(COL_DATE_ADDED, System.currentTimeMillis());
        return db.insertWithOnConflict(TABLE_SHOWS, null, values, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public int updateTrackedShow(Show show, WatchStatus status) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_TITLE, show.getTitle());
        values.put(COL_YEAR, show.getYear());
        values.put(COL_POSTER, show.getPoster());
        values.put(COL_TYPE, show.getType());
        values.put(COL_GENRE, show.getGenre());
        values.put(COL_LANGUAGE, show.getLanguage());
        values.put(COL_IMDB_RATING, show.getImdbRating());
        values.put(COL_STATUS, status.name());
        values.put(COL_EPISODE_PROGRESS, show.getEpisodeProgress());
        values.put(COL_TOTAL_EPISODES, show.getTotalEpisodes());
        if (show.getUserScore() != null) {
            values.put(COL_USER_SCORE, show.getUserScore());
        } else {
            values.putNull(COL_USER_SCORE);
        }
        return db.update(TABLE_SHOWS, values, COL_IMDB_ID + " = ?", new String[]{show.getImdbId()});
    }

    public int updateShowDetails(Show show) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_TITLE, show.getTitle());
        values.put(COL_YEAR, show.getYear());
        values.put(COL_POSTER, show.getPoster());
        values.put(COL_TYPE, show.getType());
        values.put(COL_GENRE, show.getGenre());
        values.put(COL_LANGUAGE, show.getLanguage());
        values.put(COL_IMDB_RATING, show.getImdbRating());
        values.put(COL_TOTAL_EPISODES, show.getTotalEpisodes());
        return db.update(TABLE_SHOWS, values, COL_IMDB_ID + " = ?", new String[]{show.getImdbId()});
    }

    public int deleteShow(String imdbId) {
        SQLiteDatabase db = getWritableDatabase();
        return db.delete(TABLE_SHOWS, COL_IMDB_ID + " = ?", new String[]{imdbId});
    }

    public Show getShowById(String imdbId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_SHOWS, null, COL_IMDB_ID + " = ?", new String[]{imdbId}, null, null, null);
        Show show = null;
        if (cursor.moveToFirst()) {
            show = cursorToShow(cursor);
        }
        cursor.close();
        return show;
    }

    public List<Show> getShowsByStatus(WatchStatus status) {
        return getShowsByStatus(status, WatchlistSort.RECENT);
    }

    public List<Show> getShowsByStatus(WatchStatus status, WatchlistSort sort) {
        List<Show> shows = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        String orderBy;
        switch (sort) {
            case TITLE_ASC:
                orderBy = COL_TITLE + " COLLATE NOCASE ASC";
                break;
            case TITLE_DESC:
                orderBy = COL_TITLE + " COLLATE NOCASE DESC";
                break;
            case SCORE_DESC:
                orderBy = "CASE WHEN " + COL_USER_SCORE + " IS NULL THEN 1 ELSE 0 END, "
                    + COL_USER_SCORE + " DESC, "
                    + COL_DATE_ADDED + " DESC";
                break;
            case RECENT:
            default:
                orderBy = COL_DATE_ADDED + " DESC";
                break;
        }
        Cursor cursor = db.query(TABLE_SHOWS, null, COL_STATUS + " = ?", new String[]{status.name()}, null, null, orderBy);
        while (cursor.moveToNext()) {
            shows.add(cursorToShow(cursor));
        }
        cursor.close();
        return shows;
    }

    public List<Show> getAllShows() {
        List<Show> shows = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_SHOWS, null, null, null, null, null, COL_DATE_ADDED + " DESC");
        while (cursor.moveToNext()) {
            shows.add(cursorToShow(cursor));
        }
        cursor.close();
        return shows;
    }

    public List<String> getTrackedImdbIds() {
        List<String> imdbIds = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_SHOWS, new String[]{COL_IMDB_ID}, null, null, null, null, null);
        while (cursor.moveToNext()) {
            imdbIds.add(cursor.getString(0));
        }
        cursor.close();
        return imdbIds;
    }

    public boolean isShowInList(String imdbId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_SHOWS, new String[]{COL_IMDB_ID}, COL_IMDB_ID + " = ?", new String[]{imdbId}, null, null, null);
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    public WatchStatus getShowStatus(String imdbId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_SHOWS, new String[]{COL_STATUS}, COL_IMDB_ID + " = ?", new String[]{imdbId}, null, null, null);
        WatchStatus status = null;
        if (cursor.moveToFirst()) {
            status = WatchStatus.fromString(cursor.getString(0));
        }
        cursor.close();
        return status;
    }

    public ProfileStats getProfileStats() {
        SQLiteDatabase db = getReadableDatabase();
        ProfileStats stats = new ProfileStats();

        stats.setTotalTracked(getCount(db,
            "SELECT COUNT(*) FROM " + TABLE_SHOWS,
            null));
        stats.setWatchingCount(getCount(db,
            "SELECT COUNT(*) FROM " + TABLE_SHOWS + " WHERE " + COL_STATUS + " = ?",
            new String[]{WatchStatus.WATCHING.name()}));
        stats.setPlannedCount(getCount(db,
            "SELECT COUNT(*) FROM " + TABLE_SHOWS + " WHERE " + COL_STATUS + " = ?",
            new String[]{WatchStatus.PLANNED.name()}));
        stats.setCompletedCount(getCount(db,
            "SELECT COUNT(*) FROM " + TABLE_SHOWS + " WHERE " + COL_STATUS + " = ?",
            new String[]{WatchStatus.COMPLETED.name()}));

        Cursor cursor = db.rawQuery(
            "SELECT AVG(" + COL_USER_SCORE + "), COUNT(*) FROM " + TABLE_SHOWS +
                " WHERE " + COL_STATUS + " = ? AND " + COL_USER_SCORE + " IS NOT NULL" +
                " AND " + COL_USER_SCORE + " > 0",
            new String[]{WatchStatus.COMPLETED.name()});

        if (cursor.moveToFirst()) {
            stats.setAverageRating(cursor.isNull(0) ? 0d : cursor.getDouble(0));
            stats.setRatedCompletedCount(cursor.getInt(1));
        }
        cursor.close();

        return stats;
    }

    public List<Show> getTopRatedShows(int limit) {
        List<Show> shows = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_SHOWS, null, COL_USER_SCORE + " > 0", null, null, null, COL_USER_SCORE + " DESC, " + COL_DATE_ADDED + " DESC", String.valueOf(limit));
        while (cursor.moveToNext()) {
            shows.add(cursorToShow(cursor));
        }
        cursor.close();
        return shows;
    }

    public java.util.Map<String, Integer> getTypeDistribution() {
        java.util.Map<String, Integer> distribution = new java.util.HashMap<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT LOWER(" + COL_TYPE + "), COUNT(*) FROM " + TABLE_SHOWS + " GROUP BY LOWER(" + COL_TYPE + ")", null);
        while (cursor.moveToNext()) {
            String type = cursor.getString(0);
            int count = cursor.getInt(1);
            if (type != null) {
                distribution.put(type, count);
            }
        }
        cursor.close();
        return distribution;
    }

    public Map<String, Integer> getReleaseYearDistribution() {
        TreeMap<Integer, Integer> yearDistribution = new TreeMap<>();
        for (Show show : getAllShows()) {
            int year = parseReleaseYear(show.getYear());
            if (year > 0) {
                yearDistribution.put(year, yearDistribution.getOrDefault(year, 0) + 1);
            }
        }

        LinkedHashMap<String, Integer> result = new LinkedHashMap<>();
        for (Map.Entry<Integer, Integer> entry : yearDistribution.entrySet()) {
            result.put(String.valueOf(entry.getKey()), entry.getValue());
        }
        return result;
    }

    public Map<String, Integer> getLanguageDistribution() {
        HashMap<String, Integer> distribution = new HashMap<>();
        for (Show show : getAllShows()) {
            for (String language : parseLanguages(show.getLanguage())) {
                distribution.put(language, distribution.getOrDefault(language, 0) + 1);
            }
        }

        List<Map.Entry<String, Integer>> entries = new ArrayList<>(distribution.entrySet());
        entries.sort((left, right) -> Integer.compare(right.getValue(), left.getValue()));

        LinkedHashMap<String, Integer> sorted = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> entry : entries) {
            sorted.put(entry.getKey(), entry.getValue());
        }
        return sorted;
    }

    public Map<String, Integer> getEpisodeCountDistribution() {
        LinkedHashMap<String, Integer> buckets = new LinkedHashMap<>();
        buckets.put("1", 0);
        buckets.put("2-12", 0);
        buckets.put("13-24", 0);
        buckets.put("25-50", 0);
        buckets.put("50+", 0);

        for (Show show : getAllShows()) {
            int episodeCount = getTotalEpisodesForAnalytics(show);
            if (episodeCount <= 0) {
                continue;
            }
            String key;
            if (episodeCount <= 1) {
                key = "1";
            } else if (episodeCount <= 12) {
                key = "2-12";
            } else if (episodeCount <= 24) {
                key = "13-24";
            } else if (episodeCount <= 50) {
                key = "25-50";
            } else {
                key = "50+";
            }
            buckets.put(key, buckets.get(key) + 1);
        }

        return buckets;
    }

    private Show cursorToShow(Cursor cursor) {
        Show show = new Show();
        show.setImdbId(cursor.getString(cursor.getColumnIndexOrThrow(COL_IMDB_ID)));
        show.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(COL_TITLE)));
        show.setYear(cursor.getString(cursor.getColumnIndexOrThrow(COL_YEAR)));
        show.setPoster(cursor.getString(cursor.getColumnIndexOrThrow(COL_POSTER)));
        show.setType(cursor.getString(cursor.getColumnIndexOrThrow(COL_TYPE)));
        show.setGenre(cursor.getString(cursor.getColumnIndexOrThrow(COL_GENRE)));
        show.setLanguage(cursor.getString(cursor.getColumnIndexOrThrow(COL_LANGUAGE)));
        show.setImdbRating(cursor.getString(cursor.getColumnIndexOrThrow(COL_IMDB_RATING)));
        show.setWatchStatus(WatchStatus.fromString(cursor.getString(cursor.getColumnIndexOrThrow(COL_STATUS))));
        show.setEpisodeProgress(cursor.getInt(cursor.getColumnIndexOrThrow(COL_EPISODE_PROGRESS)));
        show.setTotalEpisodes(cursor.getInt(cursor.getColumnIndexOrThrow(COL_TOTAL_EPISODES)));
        int userScoreIndex = cursor.getColumnIndexOrThrow(COL_USER_SCORE);
        if (cursor.isNull(userScoreIndex)) {
            show.setUserScore(null);
        } else {
            show.setUserScore(cursor.getFloat(userScoreIndex));
        }
        show.setDateAdded(cursor.getLong(cursor.getColumnIndexOrThrow(COL_DATE_ADDED)));
        return show;
    }

    private int getCount(SQLiteDatabase db, String query, String[] args) {
        Cursor cursor = db.rawQuery(query, args);
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        return count;
    }

    private int parseReleaseYear(String yearText) {
        if (yearText == null || yearText.trim().isEmpty()) {
            return 0;
        }
        String trimmed = yearText.trim();
        StringBuilder digits = new StringBuilder();
        for (int i = 0; i < trimmed.length(); i++) {
            char c = trimmed.charAt(i);
            if (Character.isDigit(c)) {
                digits.append(c);
                if (digits.length() == 4) {
                    break;
                }
            } else if (digits.length() > 0) {
                break;
            }
        }
        if (digits.length() != 4) {
            return 0;
        }
        try {
            return Integer.parseInt(digits.toString());
        } catch (NumberFormatException ignored) {
            return 0;
        }
    }

    private List<String> parseLanguages(String languageText) {
        if (languageText == null || languageText.trim().isEmpty() || "N/A".equalsIgnoreCase(languageText)) {
            return Collections.emptyList();
        }
        String[] parts = languageText.split(",");
        List<String> languages = new ArrayList<>();
        for (String part : parts) {
            String language = part.trim();
            if (!language.isEmpty()) {
                languages.add(language);
            }
        }
        return languages;
    }

    private int getTotalEpisodesForAnalytics(Show show) {
        if (show == null) {
            return 0;
        }
        if (show.getTotalEpisodes() > 0) {
            return show.getTotalEpisodes();
        }
        if (show.getEpisodeProgress() > 0) {
            return show.getEpisodeProgress();
        }
        return 0;
    }
}
