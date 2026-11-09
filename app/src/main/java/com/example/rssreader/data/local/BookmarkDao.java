package com.example.rssreader.data.local;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.rssreader.data.model.Article;

import java.util.ArrayList;
import java.util.List;

public class BookmarkDao {
    private final DatabaseHelper dbHelper;

    public BookmarkDao(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    public long addBookmark(long userId, long articleId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(DatabaseHelper.COL_BOOKMARK_USER_ID, userId);
        cv.put(DatabaseHelper.COL_BOOKMARK_ARTICLE_ID, articleId);
        cv.put(DatabaseHelper.COL_BOOKMARK_SAVED_AT, System.currentTimeMillis());
        return db.insertWithOnConflict(DatabaseHelper.TABLE_BOOKMARKS, null, cv, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public int removeBookmark(long userId, long articleId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String where = DatabaseHelper.COL_BOOKMARK_USER_ID + " = ? AND " + DatabaseHelper.COL_BOOKMARK_ARTICLE_ID + " = ?";
        String[] whereArgs = { String.valueOf(userId), String.valueOf(articleId) };
        return db.delete(DatabaseHelper.TABLE_BOOKMARKS, where, whereArgs);
    }

    public boolean isBookmarked(long userId, long articleId) {
        if (userId <= 0 || articleId <= 0) return false;
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT 1 FROM " + DatabaseHelper.TABLE_BOOKMARKS +
                " WHERE " + DatabaseHelper.COL_BOOKMARK_USER_ID + " = ? AND " +
                DatabaseHelper.COL_BOOKMARK_ARTICLE_ID + " = ?";
        String[] args = { String.valueOf(userId), String.valueOf(articleId) };

        try (Cursor cursor = db.rawQuery(query, args)) {
            return cursor != null && cursor.getCount() > 0;
        }
    }

    public List<Article> getBookmarkedArticles(long userId) {
        List<Article> list = new ArrayList<>();
        if (userId <= 0) return list;

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String sql = "SELECT a.*, f." + DatabaseHelper.COL_FEED_TITLE + " AS feed_title, 1 AS is_bookmarked " +
                "FROM " + DatabaseHelper.TABLE_ARTICLES + " a " +
                "JOIN " + DatabaseHelper.TABLE_BOOKMARKS + " b ON b." + DatabaseHelper.COL_BOOKMARK_ARTICLE_ID + " = a." + DatabaseHelper.COL_ARTICLE_ID + " " +
                "LEFT JOIN " + DatabaseHelper.TABLE_FEEDS + " f ON f." + DatabaseHelper.COL_FEED_ID + " = a." + DatabaseHelper.COL_ARTICLE_FEED_ID + " " +
                "WHERE b." + DatabaseHelper.COL_BOOKMARK_USER_ID + " = ? " +
                "ORDER BY b." + DatabaseHelper.COL_BOOKMARK_SAVED_AT + " DESC";

        try (Cursor cursor = db.rawQuery(sql, new String[]{ String.valueOf(userId) })) {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    Article article = cursorToArticle(cursor);
                    article.setBookmarked(true);
                    list.add(article);
                } while (cursor.moveToNext());
            }
        }
        return list;
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
        int feedTitleIdx = cursor.getColumnIndex("feed_title");
        if (feedTitleIdx != -1) {
            article.setFeedTitle(cursor.getString(feedTitleIdx));
        }
        return article;
    }
}
