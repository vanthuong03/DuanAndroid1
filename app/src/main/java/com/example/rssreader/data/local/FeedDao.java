package com.example.rssreader.data.local;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.rssreader.data.model.Feed;

import java.util.ArrayList;
import java.util.List;

public class FeedDao {
    private final DatabaseHelper dbHelper;

    public FeedDao(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    public long insert(Feed feed) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(DatabaseHelper.COL_FEED_TITLE, feed.getTitle());
        cv.put(DatabaseHelper.COL_FEED_URL, feed.getUrl());
        cv.put(DatabaseHelper.COL_FEED_ICON_URL, feed.getIconUrl());
        cv.put(DatabaseHelper.COL_FEED_CATEGORY, feed.getCategory());
        cv.put(DatabaseHelper.COL_FEED_IS_ENABLED, feed.isEnabled() ? 1 : 0);
        return db.insertWithOnConflict(DatabaseHelper.TABLE_FEEDS, null, cv, SQLiteDatabase.CONFLICT_IGNORE);
    }

    public int update(Feed feed) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(DatabaseHelper.COL_FEED_TITLE, feed.getTitle());
        cv.put(DatabaseHelper.COL_FEED_URL, feed.getUrl());
        cv.put(DatabaseHelper.COL_FEED_ICON_URL, feed.getIconUrl());
        cv.put(DatabaseHelper.COL_FEED_CATEGORY, feed.getCategory());
        cv.put(DatabaseHelper.COL_FEED_IS_ENABLED, feed.isEnabled() ? 1 : 0);

        String where = DatabaseHelper.COL_FEED_ID + " = ?";
        String[] whereArgs = { String.valueOf(feed.getId()) };
        return db.update(DatabaseHelper.TABLE_FEEDS, cv, where, whereArgs);
    }

    public int setFeedEnabled(long feedId, boolean isEnabled) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(DatabaseHelper.COL_FEED_IS_ENABLED, isEnabled ? 1 : 0);
        String where = DatabaseHelper.COL_FEED_ID + " = ?";
        String[] whereArgs = { String.valueOf(feedId) };
        return db.update(DatabaseHelper.TABLE_FEEDS, cv, where, whereArgs);
    }

    public int delete(long id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String where = DatabaseHelper.COL_FEED_ID + " = ?";
        String[] whereArgs = { String.valueOf(id) };
        return db.delete(DatabaseHelper.TABLE_FEEDS, where, whereArgs);
    }

    public List<Feed> getAllFeeds() {
        List<Feed> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String orderBy = DatabaseHelper.COL_FEED_TITLE + " ASC";

        try (Cursor cursor = db.query(DatabaseHelper.TABLE_FEEDS, null, null, null, null, null, orderBy)) {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    list.add(cursorToFeed(cursor));
                } while (cursor.moveToNext());
            }
        }
        return list;
    }

    public List<Feed> getEnabledFeeds() {
        List<Feed> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String selection = DatabaseHelper.COL_FEED_IS_ENABLED + " = 1";

        try (Cursor cursor = db.query(DatabaseHelper.TABLE_FEEDS, null, selection, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    list.add(cursorToFeed(cursor));
                } while (cursor.moveToNext());
            }
        }
        return list;
    }

    public Feed getFeedById(long id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String selection = DatabaseHelper.COL_FEED_ID + " = ?";
        String[] selectionArgs = { String.valueOf(id) };

        try (Cursor cursor = db.query(DatabaseHelper.TABLE_FEEDS, null, selection, selectionArgs, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                return cursorToFeed(cursor);
            }
        }
        return null;
    }

    public List<String> getCategories() {
        List<String> categories = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT DISTINCT " + DatabaseHelper.COL_FEED_CATEGORY +
                " FROM " + DatabaseHelper.TABLE_FEEDS +
                " WHERE " + DatabaseHelper.COL_FEED_CATEGORY + " IS NOT NULL AND " +
                DatabaseHelper.COL_FEED_CATEGORY + " != '' ORDER BY " + DatabaseHelper.COL_FEED_CATEGORY + " ASC";

        try (Cursor cursor = db.rawQuery(query, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    categories.add(cursor.getString(0));
                } while (cursor.moveToNext());
            }
        }
        return categories;
    }

    private Feed cursorToFeed(Cursor cursor) {
        long id = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_FEED_ID));
        String title = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_FEED_TITLE));
        String url = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_FEED_URL));
        String iconUrl = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_FEED_ICON_URL));
        String category = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_FEED_CATEGORY));
        boolean isEnabled = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_FEED_IS_ENABLED)) == 1;

        return new Feed(id, title, url, iconUrl, category, isEnabled);
    }
}
