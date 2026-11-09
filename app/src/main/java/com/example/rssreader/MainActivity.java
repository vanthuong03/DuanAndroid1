package com.example.rssreader;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.example.rssreader.databinding.ActivityMainBinding;
import com.example.rssreader.util.PrefsManager;
import com.example.rssreader.worker.RefreshWorker;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private ActivityMainBinding binding;
    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);

        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();
            NavigationUI.setupWithNavController(binding.bottomNavigation, navController);

            // Hide BottomNavigationView on auth and detail screens
            navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
                int id = destination.getId();
                if (id == R.id.loginFragment || id == R.id.registerFragment ||
                        id == R.id.detailFragment || id == R.id.profileFragment ||
                        id == R.id.historyFragment) {
                    binding.bottomNavigation.setVisibility(View.GONE);
                } else {
                    binding.bottomNavigation.setVisibility(View.VISIBLE);
                }
            });
        }

        // Safely schedule periodic work
        try {
            PrefsManager prefsManager = new PrefsManager(this);
            RefreshWorker.schedulePeriodicWork(this, prefsManager.getSyncIntervalMinutes());
        } catch (Exception e) {
            Log.e(TAG, "Error scheduling worker", e);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        return (navController != null && navController.navigateUp()) || super.onSupportNavigateUp();
    }
}
