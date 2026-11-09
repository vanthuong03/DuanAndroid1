package com.example.rssreader.util;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String PREF_NAME = "rss_user_session";
    private static final String KEY_USER_ID = "key_user_id";
    private static final String KEY_USER_EMAIL = "key_user_email";
    private static final String KEY_USER_FULL_NAME = "key_user_full_name";
    private static final String KEY_IS_LOGGED_IN = "key_is_logged_in";

    private final SharedPreferences prefs;

    public SessionManager(Context context) {
        this.prefs = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void saveUserSession(long userId, String email, String fullName) {
        prefs.edit()
                .putLong(KEY_USER_ID, userId)
                .putString(KEY_USER_EMAIL, email)
                .putString(KEY_USER_FULL_NAME, fullName)
                .putBoolean(KEY_IS_LOGGED_IN, true)
                .apply();
    }

    public boolean isLoggedIn() {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false) && getUserId() > 0;
    }

    public long getUserId() {
        return prefs.getLong(KEY_USER_ID, -1);
    }

    public String getUserEmail() {
        return prefs.getString(KEY_USER_EMAIL, "");
    }

    public String getUserFullName() {
        return prefs.getString(KEY_USER_FULL_NAME, "");
    }

    public void clearSession() {
        prefs.edit().clear().apply();
    }
}
