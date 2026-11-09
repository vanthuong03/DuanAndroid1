package com.example.rssreader;

import android.app.Application;
import android.util.Log;

import androidx.appcompat.app.AppCompatDelegate;

import com.example.rssreader.util.PrefsManager;

public class RssApp extends Application {
    private static final String TAG = "RssApp";

    @Override
    public void onCreate() {
        super.onCreate();
        try {
            PrefsManager prefsManager = new PrefsManager(this);
            int darkMode = prefsManager.getDarkMode();
            AppCompatDelegate.setDefaultNightMode(darkMode);
        } catch (Exception e) {
            Log.e(TAG, "Error initializing app", e);
        }
    }
}
