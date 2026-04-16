import re

with open('app/src/main/java/com/example/movietracker/ui/profile/ProfileFragment.java', 'r') as f:
    content = f.read()

# Add imports
imports = """import androidx.recyclerview.widget.RecyclerView;
import com.example.movietracker.model.Show;
import java.util.List;
import java.util.Map;
import androidx.appcompat.app.AlertDialog;
"""
content = re.sub(r'import androidx\.appcompat\.app\.AlertDialog;', imports, content)

# Add fields
fields = """    private TextView tvSummary;

    private FavoritesAdapter favoritesAdapter;
    private View tvFavoritesTitle;
    private RecyclerView rvFavorites;
    private View tvAnalyticsTitle;
    private View cvAnalytics;
    private PieChartView pieChartView;"""
content = re.sub(r'    private TextView tvSummary;', fields, content)

# Add findViews
views = """        tvSummary = view.findViewById(R.id.tvProfileSummary);

        tvFavoritesTitle = view.findViewById(R.id.tvFavoritesTitle);
        rvFavorites = view.findViewById(R.id.rvFavorites);
        tvAnalyticsTitle = view.findViewById(R.id.tvAnalyticsTitle);
        cvAnalytics = view.findViewById(R.id.cvAnalytics);
        pieChartView = view.findViewById(R.id.pieChartView);

        favoritesAdapter = new FavoritesAdapter();
        rvFavorites.setAdapter(favoritesAdapter);"""
content = re.sub(r'        tvSummary = view\.findViewById\(R\.id\.tvProfileSummary\);', views, content)

# Update loadProfile
load_profile = """    private void loadProfile() {
        repository.getProfileStats(this::bindProfile);
        repository.getTopRatedShows(10, this::bindFavorites);
        repository.getTypeDistribution(this::bindAnalytics);
    }"""
content = re.sub(r'    private void loadProfile\(\) \{\n        repository\.getProfileStats\(this::bindProfile\);\n    \}', load_profile, content)

# Add bind methods
bind_methods = """    private void bindProfile(ProfileStats stats) {
        tvTotalTracked.setText(String.valueOf(stats.getTotalTracked()));
        tvWatched.setText(String.valueOf(stats.getCompletedCount()));
        tvWatchingNow.setText(String.valueOf(stats.getWatchingCount()));
        tvPlannedQueue.setText(String.valueOf(stats.getPlannedCount()));

        if (stats.hasAverageRating()) {
            tvAverageRating.setText(String.format(Locale.getDefault(), "%.1f / 10", stats.getAverageRating()));
        } else {
            tvAverageRating.setText(R.string.profile_no_ratings);
        }

        if (stats.getTotalTracked() == 0) {
            tvSummary.setText(R.string.profile_summary_empty);
        } else if (stats.getCompletedCount() == 0) {
            tvSummary.setText(getString(
                R.string.profile_summary_waiting_for_completed,
                stats.getTotalTracked()));
        } else {
            tvSummary.setText(getString(
                R.string.profile_summary_ready,
                stats.getCompletedCount(),
                stats.getTotalTracked()));
        }
    }

    private void bindFavorites(List<Show> shows) {
        if (shows != null && !shows.isEmpty()) {
            tvFavoritesTitle.setVisibility(View.VISIBLE);
            rvFavorites.setVisibility(View.VISIBLE);
            favoritesAdapter.setShows(shows);
        } else {
            tvFavoritesTitle.setVisibility(View.GONE);
            rvFavorites.setVisibility(View.GONE);
        }
    }

    private void bindAnalytics(Map<String, Integer> distribution) {
        if (distribution != null && !distribution.isEmpty()) {
            tvAnalyticsTitle.setVisibility(View.VISIBLE);
            cvAnalytics.setVisibility(View.VISIBLE);
            pieChartView.setData(distribution);
        } else {
            tvAnalyticsTitle.setVisibility(View.GONE);
            cvAnalytics.setVisibility(View.GONE);
        }
    }
}"""
content = re.sub(r'    private void bindProfile\(ProfileStats stats\) \{[\s\S]*^\}', bind_methods, content, flags=re.MULTILINE)

with open('app/src/main/java/com/example/movietracker/ui/profile/ProfileFragment.java', 'w') as f:
    f.write(content)

