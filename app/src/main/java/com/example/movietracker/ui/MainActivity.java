package com.example.movietracker.ui;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.movietracker.R;
import com.example.movietracker.ui.profile.ProfileFragment;
import com.example.movietracker.ui.search.SearchFragment;
import com.example.movietracker.ui.watchlist.WatchlistFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {
    private static final String TAG_LISTS = "tab_lists";
    private static final String TAG_SEARCH = "tab_search";
    private static final String TAG_PROFILE = "tab_profile";

    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            showTab(item.getItemId());
            return true;
        });

        if (savedInstanceState == null) {
            bottomNavigationView.setSelectedItemId(R.id.navigation_lists);
        } else {
            updateTitle(bottomNavigationView.getSelectedItemId());
        }
    }

    private void showTab(int itemId) {
        String tag = getTagForItem(itemId);
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment targetFragment = fragmentManager.findFragmentByTag(tag);
        Fragment currentFragment = getVisibleFragment(fragmentManager);

        if (targetFragment == null) {
            targetFragment = createFragmentForItem(itemId);
        }

        FragmentTransaction transaction = fragmentManager.beginTransaction();
        if (currentFragment != null && currentFragment != targetFragment) {
            transaction.hide(currentFragment);
        }

        if (targetFragment.isAdded()) {
            transaction.show(targetFragment);
        } else {
            transaction.add(R.id.fragmentContainer, targetFragment, tag);
        }

        transaction.commit();
        updateTitle(itemId);
    }

    private Fragment getVisibleFragment(FragmentManager fragmentManager) {
        for (Fragment fragment : fragmentManager.getFragments()) {
            if (fragment != null && fragment.isVisible()) {
                return fragment;
            }
        }
        return null;
    }

    private Fragment createFragmentForItem(int itemId) {
        if (itemId == R.id.navigation_search) {
            return new SearchFragment();
        }
        if (itemId == R.id.navigation_profile) {
            return new ProfileFragment();
        }
        return new WatchlistFragment();
    }

    private String getTagForItem(int itemId) {
        if (itemId == R.id.navigation_search) {
            return TAG_SEARCH;
        }
        if (itemId == R.id.navigation_profile) {
            return TAG_PROFILE;
        }
        return TAG_LISTS;
    }

    private void updateTitle(int itemId) {
        if (itemId == R.id.navigation_search) {
            setTitle(R.string.title_search);
            return;
        }
        if (itemId == R.id.navigation_profile) {
            setTitle(R.string.title_profile);
            return;
        }
        setTitle(R.string.title_my_list);
    }
}
