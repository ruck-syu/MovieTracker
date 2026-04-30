package com.example.movietracker.ui.profile;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.movietracker.model.ListBackupPayload;
import com.example.movietracker.model.RecommendationItem;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import androidx.appcompat.app.AlertDialog;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.movietracker.R;
import com.example.movietracker.data.repository.ShowRepository;
import com.example.movietracker.model.ProfileStats;

import com.example.movietracker.ui.login.LoginActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ProfileFragment extends Fragment {
    private static final String PREFS_NAME = "movie_tracker_prefs";
    private static final String KEY_PROFILE_NAME = "profile_name";
    private static final String KEY_PROFILE_PIC_URI = "profile_pic_uri";
    private static final int BACKUP_FORMAT_VERSION = 1;

    private ShowRepository repository;
    private final Gson gson = new Gson();

    private TextView tvProfileName;
    private TextView tvTotalTracked;
    private TextView tvWatched;
    private TextView tvAverageRating;
    private TextView tvWatchingNow;
    private TextView tvPlannedQueue;
    private TextView tvSummary;
    private TextView tvProfileTier;

    private View cvReleaseYearAnalytics;
    private View cvEpisodeAnalytics;
    private View tvAnalyticsTitle;
    private View cvAnalytics;
    private PieChartView pieChartView;
    private LineChartView barReleaseYear;
    private VerticalBarChartView barEpisodes;
    private View tvRecommendationsTitle;
    private TextView tvRecommendationsEmpty;
    private androidx.recyclerview.widget.RecyclerView rvRecommendations;
    private RecommendationsAdapter recommendationsAdapter;

    private final ActivityResultLauncher<String> exportLauncher =
        registerForActivityResult(new ActivityResultContracts.CreateDocument("application/json"), this::handleExportUri);
    private final ActivityResultLauncher<String[]> importLauncher =
        registerForActivityResult(new ActivityResultContracts.OpenDocument(), this::handleImportUri);

    public ProfileFragment() {
        super(R.layout.fragment_profile);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        repository = new ShowRepository(requireContext());

        tvProfileName = view.findViewById(R.id.tvProfileName);
        TextView tvEditProfileName = view.findViewById(R.id.tvEditProfileName);
        tvTotalTracked = view.findViewById(R.id.tvProfileTotalTracked);
        tvWatched = view.findViewById(R.id.tvProfileWatched);
        tvAverageRating = view.findViewById(R.id.tvProfileAverageRating);
        tvWatchingNow = view.findViewById(R.id.tvProfileWatchingNow);
        tvPlannedQueue = view.findViewById(R.id.tvProfilePlannedQueue);
        tvSummary = view.findViewById(R.id.tvProfileSummary);
        tvProfileTier = view.findViewById(R.id.tvProfileTier);

        tvAnalyticsTitle = view.findViewById(R.id.tvAnalyticsTitle);
        cvAnalytics = view.findViewById(R.id.cvAnalytics);
        pieChartView = view.findViewById(R.id.pieChartView);
        cvReleaseYearAnalytics = view.findViewById(R.id.cvReleaseYearAnalytics);
        cvEpisodeAnalytics = view.findViewById(R.id.cvEpisodeAnalytics);
        barReleaseYear = view.findViewById(R.id.barReleaseYear);
        barEpisodes = view.findViewById(R.id.barEpisodes);
        tvRecommendationsTitle = view.findViewById(R.id.tvRecommendationsTitle);
        tvRecommendationsEmpty = view.findViewById(R.id.tvRecommendationsEmpty);
        rvRecommendations = view.findViewById(R.id.rvRecommendations);

        recommendationsAdapter = new RecommendationsAdapter();
        rvRecommendations.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        rvRecommendations.setAdapter(recommendationsAdapter);

        Button btnSignOut = view.findViewById(R.id.btnSignOut);
        Button btnExportList = view.findViewById(R.id.btnExportList);
        Button btnImportList = view.findViewById(R.id.btnImportList);
        btnSignOut.setOnClickListener(v -> signOut());
        btnExportList.setOnClickListener(v -> exportList());
        btnImportList.setOnClickListener(v -> importList());

        ImageView ivProfilePicture = view.findViewById(R.id.ivProfilePicture);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String displayName = user.getDisplayName();
            if (displayName != null && !displayName.isEmpty()) {
                tvProfileName.setText(displayName);
            } else if (user.getEmail() != null) {
                tvProfileName.setText(user.getEmail());
            } else {
                tvProfileName.setText(getSavedProfileName());
            }
        } else {
            tvProfileName.setText(getSavedProfileName());
        }
        
        tvEditProfileName.setVisibility(View.VISIBLE);
        tvEditProfileName.setOnClickListener(v -> showEditNameDialog());

        loadProfile();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (repository != null) {
            loadProfile();
        }
    }

    private void showEditNameDialog() {
        Context context = requireContext();
        EditText input = new EditText(context);
        input.setText(tvProfileName.getText().toString());
        input.setHint(R.string.profile_name_hint);
        input.setSelection(input.getText().length());
        input.setPadding(40, 24, 40, 24);

        new AlertDialog.Builder(context)
            .setTitle(R.string.profile_edit_name_title)
            .setView(input)
            .setPositiveButton(R.string.profile_save, (dialog, which) -> {
                String newName = input.getText().toString().trim();
                if (newName.isEmpty()) {
                    newName = getString(R.string.profile_name);
                }
                saveProfileName(newName);
                tvProfileName.setText(newName);
            })
            .setNegativeButton(R.string.profile_cancel, null)
            .show();
    }

    private String getSavedProfileName() {
        SharedPreferences preferences = requireContext()
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getString(KEY_PROFILE_NAME, getString(R.string.profile_name));
    }

    private void saveProfileName(String name) {
        SharedPreferences preferences = requireContext()
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        preferences.edit().putString(KEY_PROFILE_NAME, name).apply();
    }

    private void loadProfile() {
        repository.getProfileStats(this::bindProfile);
        repository.getTypeDistribution(this::bindAnalytics);
        repository.getReleaseYearDistribution(this::bindReleaseYearAnalytics);
        repository.getEpisodeCountDistribution(this::bindEpisodeAnalytics);
        repository.getRecommendations(10, new ShowRepository.RecommendationsCallback() {
            @Override
            public void onSuccess(List<RecommendationItem> recommendations) {
                bindRecommendations(recommendations);
            }

            @Override
            public void onError(String error) {
                showRecommendationEmpty(error);
            }
        });
    }

    private void bindProfile(ProfileStats stats) {
        tvTotalTracked.setText(String.valueOf(stats.getTotalTracked()));
        tvWatched.setText(String.valueOf(stats.getCompletedCount()));
        tvWatchingNow.setText(String.valueOf(stats.getWatchingCount()));
        tvPlannedQueue.setText(String.valueOf(stats.getPlannedCount()));
        
        tvProfileTier.setText(getUserTier(stats.getCompletedCount()));

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

    private String getUserTier(int watchedCount) {
        if (watchedCount >= 250) return "Master Tracker";
        if (watchedCount >= 100) return "Cinephile";
        if (watchedCount >= 50) return "Film Buff";
        if (watchedCount >= 10) return "Regular Watcher";
        if (watchedCount > 0) return "Casual Viewer";
        return "Newcomer";
    }

    private void bindAnalytics(Map<String, Integer> distribution) {
        if (distribution != null && !distribution.isEmpty()) {
            cvAnalytics.setVisibility(View.VISIBLE);
            pieChartView.setData(distribution);
        } else {
            cvAnalytics.setVisibility(View.GONE);
        }
        refreshAnalyticsTitleVisibility();
    }

    private void bindReleaseYearAnalytics(Map<String, Integer> distribution) {
        Map<String, Integer> prepared = limitTail(distribution, 10);
        if (!prepared.isEmpty()) {
            cvReleaseYearAnalytics.setVisibility(View.VISIBLE);
            barReleaseYear.setData(prepared);
        } else {
            cvReleaseYearAnalytics.setVisibility(View.GONE);
        }
        refreshAnalyticsTitleVisibility();
    }

    private void bindEpisodeAnalytics(Map<String, Integer> distribution) {
        if (distribution != null && !distribution.isEmpty()) {
            cvEpisodeAnalytics.setVisibility(View.VISIBLE);
            barEpisodes.setData(distribution);
        } else {
            cvEpisodeAnalytics.setVisibility(View.GONE);
        }
        refreshAnalyticsTitleVisibility();
    }

    private void bindRecommendations(List<RecommendationItem> recommendations) {
        if (recommendations != null && !recommendations.isEmpty()) {
            tvRecommendationsTitle.setVisibility(View.VISIBLE);
            rvRecommendations.setVisibility(View.VISIBLE);
            tvRecommendationsEmpty.setVisibility(View.GONE);
            recommendationsAdapter.setRecommendations(recommendations);
        } else {
            showRecommendationEmpty(getString(R.string.recommendation_empty_default));
        }
    }

    private void showRecommendationEmpty(String message) {
        tvRecommendationsTitle.setVisibility(View.VISIBLE);
        rvRecommendations.setVisibility(View.GONE);
        tvRecommendationsEmpty.setVisibility(View.VISIBLE);
        tvRecommendationsEmpty.setText(message != null ? message : getString(R.string.recommendation_empty_default));
        recommendationsAdapter.setRecommendations(null);
    }

    private void exportList() {
        String fileName = String.format(
            Locale.getDefault(),
            "movie-tracker-backup-%d.json",
            System.currentTimeMillis());
        exportLauncher.launch(fileName);
    }

    private void handleExportUri(Uri uri) {
        if (uri == null) {
            Toast.makeText(requireContext(), R.string.backup_export_cancelled, Toast.LENGTH_SHORT).show();
            return;
        }

        repository.getAllShows(shows -> {
            ListBackupPayload payload = new ListBackupPayload();
            payload.setVersion(BACKUP_FORMAT_VERSION);
            payload.setExportedAt(System.currentTimeMillis());
            payload.setShows(shows);

            try (OutputStream outputStream = requireContext().getContentResolver().openOutputStream(uri)) {
                if (outputStream == null) {
                    Toast.makeText(requireContext(), R.string.backup_export_failed, Toast.LENGTH_SHORT).show();
                    return;
                }
                outputStream.write(gson.toJson(payload).getBytes(java.nio.charset.StandardCharsets.UTF_8));
                Toast.makeText(requireContext(), R.string.backup_export_success, Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(requireContext(), getString(R.string.backup_export_failed_with_error, e.getMessage()), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void importList() {
        importLauncher.launch(new String[]{"application/json", "text/*"});
    }

    private void handleImportUri(Uri uri) {
        if (uri == null) {
            Toast.makeText(requireContext(), R.string.backup_import_cancelled, Toast.LENGTH_SHORT).show();
            return;
        }

        try (InputStream inputStream = requireContext().getContentResolver().openInputStream(uri)) {
            if (inputStream == null) {
                Toast.makeText(requireContext(), R.string.backup_import_failed, Toast.LENGTH_SHORT).show();
                return;
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            ListBackupPayload payload = gson.fromJson(reader, ListBackupPayload.class);
            if (payload == null || payload.getShows() == null) {
                Toast.makeText(requireContext(), R.string.backup_import_invalid, Toast.LENGTH_SHORT).show();
                return;
            }

            repository.mergeImportedShows(payload.getShows(), new ShowRepository.ImportCallback() {
                @Override
                public void onSuccess(int importedCount) {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.backup_import_success, importedCount),
                        Toast.LENGTH_SHORT
                    ).show();
                    loadProfile();
                }

                @Override
                public void onError(String error) {
                    Toast.makeText(requireContext(), error, Toast.LENGTH_LONG).show();
                }
            });
        } catch (Exception e) {
            Toast.makeText(requireContext(), getString(R.string.backup_import_failed_with_error, e.getMessage()), Toast.LENGTH_LONG).show();
        }
    }

    private void signOut() {
        FirebaseAuth.getInstance().signOut();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(requireActivity(), gso);
        mGoogleSignInClient.signOut().addOnCompleteListener(requireActivity(), task -> {
            Intent intent = new Intent(requireActivity(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }

    private Map<String, Integer> limitTail(Map<String, Integer> source, int limit) {
        LinkedHashMap<String, Integer> result = new LinkedHashMap<>();
        if (source == null || source.isEmpty() || limit <= 0) {
            return result;
        }

        List<Map.Entry<String, Integer>> entries = new ArrayList<>(source.entrySet());
        int start = Math.max(0, entries.size() - limit);
        for (int i = start; i < entries.size(); i++) {
            Map.Entry<String, Integer> entry = entries.get(i);
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }

    private void refreshAnalyticsTitleVisibility() {
        boolean hasAnyAnalytics =
            cvAnalytics.getVisibility() == View.VISIBLE
                || cvReleaseYearAnalytics.getVisibility() == View.VISIBLE
                || cvEpisodeAnalytics.getVisibility() == View.VISIBLE;
        tvAnalyticsTitle.setVisibility(hasAnyAnalytics ? View.VISIBLE : View.GONE);
    }
}
