package com.example.rssreader.ui.settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.rssreader.R;
import com.example.rssreader.databinding.FragmentSettingsBinding;
import com.example.rssreader.util.PrefsManager;
import com.example.rssreader.util.SessionManager;
import com.example.rssreader.worker.RefreshWorker;

public class SettingsFragment extends Fragment {

    private FragmentSettingsBinding binding;
    private SessionManager sessionManager;
    private PrefsManager prefsManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sessionManager = new SessionManager(requireContext());
        prefsManager = new PrefsManager(requireContext());

        setupAccountCard();
        setupPreferences();
    }

    @Override
    public void onResume() {
        super.onResume();
        updateAccountUI();
    }

    private void setupAccountCard() {
        binding.btnViewProfile.setOnClickListener(v -> {
            Navigation.findNavController(requireView()).navigate(R.id.action_settings_to_profile);
        });

        binding.btnHistoryFromSettings.setOnClickListener(v -> {
            Navigation.findNavController(requireView()).navigate(R.id.action_settings_to_history);
        });

        binding.btnAuthAction.setOnClickListener(v -> {
            if (sessionManager.isLoggedIn()) {
                sessionManager.clearSession();
                updateAccountUI();
                Toast.makeText(requireContext(), "Đã đăng xuất tài khoản", Toast.LENGTH_SHORT).show();
                Navigation.findNavController(requireView()).navigate(R.id.action_settings_to_home);
            } else {
                Navigation.findNavController(requireView()).navigate(R.id.action_settings_to_login);
            }
        });
    }

    private void updateAccountUI() {
        if (sessionManager.isLoggedIn()) {
            binding.tvUserName.setText(sessionManager.getUserFullName());
            binding.tvUserEmail.setText(sessionManager.getUserEmail());
            binding.tvUserEmail.setVisibility(View.VISIBLE);
            binding.layoutLoggedInActions.setVisibility(View.VISIBLE);
            binding.btnAuthAction.setText(R.string.btn_logout);
        } else {
            binding.tvUserName.setText(R.string.settings_not_logged_in);
            binding.tvUserEmail.setVisibility(View.GONE);
            binding.layoutLoggedInActions.setVisibility(View.GONE);
            binding.btnAuthAction.setText(R.string.btn_login);
        }
    }

    private void setupPreferences() {
        // Dark Mode
        int currentDarkMode = prefsManager.getDarkMode();
        binding.switchDarkMode.setChecked(currentDarkMode == AppCompatDelegate.MODE_NIGHT_YES);
        binding.switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            int newMode = isChecked ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO;
            prefsManager.setDarkMode(newMode);
            AppCompatDelegate.setDefaultNightMode(newMode);
        });

        // Font Size
        int fontSize = prefsManager.getFontSize();
        if (fontSize == 0) binding.rbFontSmall.setChecked(true);
        else if (fontSize == 2) binding.rbFontLarge.setChecked(true);
        else binding.rbFontMedium.setChecked(true);

        binding.rgFontSize.setOnCheckedChangeListener((group, checkedId) -> {
            int selectedSize = 1;
            if (checkedId == R.id.rbFontSmall) selectedSize = 0;
            else if (checkedId == R.id.rbFontLarge) selectedSize = 2;
            prefsManager.setFontSize(selectedSize);
        });

        // Sync Interval
        int interval = prefsManager.getSyncIntervalMinutes();
        if (interval <= 15) binding.rbSync15.setChecked(true);
        else if (interval >= 60) binding.rbSync60.setChecked(true);
        else binding.rbSync30.setChecked(true);

        binding.rgSyncInterval.setOnCheckedChangeListener((group, checkedId) -> {
            int minutes = 30;
            if (checkedId == R.id.rbSync15) minutes = 15;
            else if (checkedId == R.id.rbSync60) minutes = 60;
            prefsManager.setSyncIntervalMinutes(minutes);
            RefreshWorker.schedulePeriodicWork(requireContext(), minutes);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
