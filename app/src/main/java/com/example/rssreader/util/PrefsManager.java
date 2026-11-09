package com.example.rssreader.util;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;

import com.example.rssreader.data.model.FilterState;

public class PrefsManager {
    private static final String PREF_NAME = "rss_settings";
    private static final String KEY_DARK_MODE = "key_dark_mode";
    private static final String KEY_FONT_SIZE = "key_font_size"; // 0: Nhỏ, 1: Vừa, 2: Lớn
    private static final String KEY_SYNC_INTERVAL = "key_sync_interval"; // phút (ví dụ 15, 30, 60)
    private static final String KEY_TTS_SPEED = "key_tts_speed";

    // Filter & Sort state keys
    private static final String KEY_FILTER_TYPE = "key_filter_type";
    private static final String KEY_FILTER_FEED_ID = "key_filter_feed_id";
    private static final String KEY_SORT_ORDER = "key_sort_order";

    // Reader Mode preferences
    private static final String KEY_READER_FONT_SIZE = "key_reader_font_size"; // 0: Nhỏ, 1: Vừa, 2: Lớn, 3: Rất lớn
    private static final String KEY_READER_THEME = "key_reader_theme"; // 0: Sáng (Light), 1: Tối (Dark), 2: Sepia

    private final SharedPreferences prefs;

    public PrefsManager(Context context) {
        this.prefs = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public int getDarkMode() {
        return prefs.getInt(KEY_DARK_MODE, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
    }

    public void setDarkMode(int mode) {
        prefs.edit().putInt(KEY_DARK_MODE, mode).apply();
    }

    public int getFontSize() {
        return prefs.getInt(KEY_FONT_SIZE, 1); // 1 = Vừa
    }

    public void setFontSize(int size) {
        prefs.edit().putInt(KEY_FONT_SIZE, size).apply();
    }

    public int getSyncIntervalMinutes() {
        return prefs.getInt(KEY_SYNC_INTERVAL, 30);
    }

    public void setSyncIntervalMinutes(int minutes) {
        prefs.edit().putInt(KEY_SYNC_INTERVAL, minutes).apply();
    }

    public float getTtsSpeed() {
        return prefs.getFloat(KEY_TTS_SPEED, 1.0f);
    }

    public void setTtsSpeed(float speed) {
        prefs.edit().putFloat(KEY_TTS_SPEED, speed).apply();
    }

    // Filter & Sort
    public void saveFilterState(FilterState state) {
        if (state == null) return;
        prefs.edit()
                .putString(KEY_FILTER_TYPE, state.getFilterType().name())
                .putLong(KEY_FILTER_FEED_ID, state.getSelectedFeedId())
                .putString(KEY_SORT_ORDER, state.getSortOrder().name())
                .apply();
    }

    public FilterState getFilterState() {
        FilterState state = new FilterState();
        try {
            String typeStr = prefs.getString(KEY_FILTER_TYPE, FilterState.FilterType.ALL.name());
            state.setFilterType(FilterState.FilterType.valueOf(typeStr));
        } catch (Exception e) {
            state.setFilterType(FilterState.FilterType.ALL);
        }

        state.setSelectedFeedId(prefs.getLong(KEY_FILTER_FEED_ID, -1));

        try {
            String sortStr = prefs.getString(KEY_SORT_ORDER, FilterState.SortOrder.NEWEST.name());
            state.setSortOrder(FilterState.SortOrder.valueOf(sortStr));
        } catch (Exception e) {
            state.setSortOrder(FilterState.SortOrder.NEWEST);
        }
        return state;
    }

    // Reader Mode
    public int getReaderFontSize() {
        return prefs.getInt(KEY_READER_FONT_SIZE, 1); // 1 = Vừa (16sp)
    }

    public void setReaderFontSize(int size) {
        prefs.edit().putInt(KEY_READER_FONT_SIZE, size).apply();
    }

    public int getReaderTheme() {
        return prefs.getInt(KEY_READER_THEME, 0); // 0 = Sáng, 1 = Tối, 2 = Sepia
    }

    public void setReaderTheme(int theme) {
        prefs.edit().putInt(KEY_READER_THEME, theme).apply();
    }
}
