package com.example.rssreader.data.local;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.rssreader.data.model.Article;

import java.util.ArrayList;
import java.util.List;

public class HistoryDao {
    private final DatabaseHelper dbHelper;

    public HistoryDao(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    public void recordReading(long userId, long articleId) {
        if (userId <= 0 || articleId <= 0) return;
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put(DatabaseHelper.COL_HISTORY_USER_ID, userId);
        cv.put(DatabaseHelper.COL_HISTORY_ARTICLE_ID, articleId);
        cv.put(DatabaseHelper.COL_HISTORY_READ_AT, System.currentTimeMillis());

        db.insertWithOnConflict(DatabaseHelper.TABLE_READING_HISTORY, null, cv, SQLiteDatabase.CONFLICT_REPLACE);

        // Trim records to maximum 100 per user
        trimHistory(userId);
    }

    public void trimHistory(long userId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String sql = "DELETE FROM " + DatabaseHelper.TABLE_READING_HISTORY +
                " WHERE " + DatabaseHelper.COL_HISTORY_USER_ID + " = ? AND " +
                DatabaseHelper.COL_HISTORY_ID + " NOT IN (" +
                "  SELECT " + DatabaseHelper.COL_HISTORY_ID + " FROM " + DatabaseHelper.TABLE_READING_HISTORY +
                "  WHERE " + DatabaseHelper.COL_HISTORY_USER_ID + " = ? " +
                "  ORDER BY " + DatabaseHelper.COL_HISTORY_READ_AT + " DESC LIMIT 100)";
        db.execSQL(sql, new Object[]{ userId, userId });
    }

    public List<Article> getReadingHistory(long userId) {
        List<Article> list = new ArrayList<>();
        if (userId <= 0) return list;

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String sql = "SELECT a.*, f." + DatabaseHelper.COL_FEED_TITLE + " AS feed_title, " +
                "(CASE WHEN b." + DatabaseHelper.COL_BOOKMARK_ID + " IS NOT NULL THEN 1 ELSE 0 END) AS is_bookmarked " +
                "FROM " + DatabaseHelper.TABLE_ARTICLES + " a " +
                "JOIN " + DatabaseHelper.TABLE_READING_HISTORY + " h ON h." + DatabaseHelper.COL_HISTORY_ARTICLE_ID + " = a." + DatabaseHelper.COL_ARTICLE_ID + " " +
                "LEFT JOIN " + DatabaseHelper.TABLE_FEEDS + " f ON f." + DatabaseHelper.COL_FEED_ID + " = a." + DatabaseHelper.COL_ARTICLE_FEED_ID + " " +
                "LEFT JOIN " + DatabaseHelper.TABLE_BOOKMARKS + " b ON b." + DatabaseHelper.COL_BOOKMARK_ARTICLE_ID + " = a." + DatabaseHelper.COL_ARTICLE_ID +
                " AND b." + DatabaseHelper.COL_BOOKMARK_USER_ID + " = ? " +
                "WHERE h." + DatabaseHelper.COL_HISTORY_USER_ID + " = ? " +
                "ORDER BY h." + DatabaseHelper.COL_HISTORY_READ_AT + " DESC LIMIT 100";

        try (Cursor cursor = db.rawQuery(sql, new String[]{ String.valueOf(userId), String.valueOf(userId) })) {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    list.add(cursorToArticle(cursor));
                } while (cursor.moveToNext());
            }
        }
        return list;
    }

    public int deleteHistoryItem(long userId, long articleId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String where = DatabaseHelper.COL_HISTORY_USER_ID + " = ? AND " + DatabaseHelper.COL_HISTORY_ARTICLE_ID + " = ?";
        return db.delete(DatabaseHelper.TABLE_READING_HISTORY, where, new String[]{ String.valueOf(userId), String.valueOf(articleId) });
    }

    public int clearHistory(long userId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String where = DatabaseHelper.COL_HISTORY_USER_ID + " = ?";
        return db.delete(DatabaseHelper.TABLE_READING_HISTORY, where, new String[]{ String.valueOf(userId) });
    }

    private Article cursorToArticle(Cursor cursor) {
        long id = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ARTICLE_ID));
        long feedId = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ARTICLE_FEED_ID));
        String title = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ARTICLE_TITLE));
        String link = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ARTICLE_LINK));
        String description = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ARTICLE_DESCRIPTION));
        String imageUrl = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ARTICLE_IMAGE_URL));
        long pubDate = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ARTICLE_PUB_DATE));
        boolean isRead = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ARTICLE_IS_READ)) == 1;

        Article article = new Article(id, feedId, title, link, description, imageUrl, pubDate, isRead);

        int isBookmarkedIdx = cursor.getColumnIndex("is_bookmarked");
        if (isBookmarkedIdx != -1) {
            article.setBookmarked(cursor.getInt(isBookmarkedIdx) == 1);
        }

        int feedTitleIdx = cursor.getColumnIndex("feed_title");
        if (feedTitleIdx != -1) {
            article.setFeedTitle(cursor.getString(feedTitleIdx));
        }

        int contentIdx = cursor.getColumnIndex(DatabaseHelper.COL_ARTICLE_CONTENT);
        if (contentIdx != -1) {
            article.setContent(cursor.getString(contentIdx));
        }

        return article;
    }
}
