package com.example.rssreader.data.local;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.rssreader.data.model.Article;
import com.example.rssreader.data.model.FilterState;

import java.util.ArrayList;
import java.util.List;

public class ArticleDao {
    private final DatabaseHelper dbHelper;

    public ArticleDao(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    public void insertArticles(List<Article> articles) {
        if (articles == null || articles.isEmpty()) return;
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            for (Article article : articles) {
                ContentValues cv = new ContentValues();
                cv.put(DatabaseHelper.COL_ARTICLE_FEED_ID, article.getFeedId());
                cv.put(DatabaseHelper.COL_ARTICLE_TITLE, article.getTitle());
                cv.put(DatabaseHelper.COL_ARTICLE_LINK, article.getLink());
                cv.put(DatabaseHelper.COL_ARTICLE_DESCRIPTION, article.getDescription());
                cv.put(DatabaseHelper.COL_ARTICLE_IMAGE_URL, article.getImageUrl());
                cv.put(DatabaseHelper.COL_ARTICLE_PUB_DATE, article.getPubDate());
                cv.put(DatabaseHelper.COL_ARTICLE_IS_READ, article.isRead() ? 1 : 0);

                db.insertWithOnConflict(DatabaseHelper.TABLE_ARTICLES, null, cv, SQLiteDatabase.CONFLICT_IGNORE);
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    public List<Article> getAllArticles(long currentUserId) {
        return queryArticles(new FilterState(), currentUserId, null);
    }

    public List<Article> getArticlesByCategory(String category, long currentUserId) {
        List<Article> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String sql = "SELECT a.*, f." + DatabaseHelper.COL_FEED_TITLE + " AS feed_title, " +
                "(CASE WHEN b." + DatabaseHelper.COL_BOOKMARK_ID + " IS NOT NULL THEN 1 ELSE 0 END) AS is_bookmarked " +
                "FROM " + DatabaseHelper.TABLE_ARTICLES + " a " +
                "JOIN " + DatabaseHelper.TABLE_FEEDS + " f ON f." + DatabaseHelper.COL_FEED_ID + " = a." + DatabaseHelper.COL_ARTICLE_FEED_ID + " " +
                "LEFT JOIN " + DatabaseHelper.TABLE_BOOKMARKS + " b ON b." + DatabaseHelper.COL_BOOKMARK_ARTICLE_ID + " = a." + DatabaseHelper.COL_ARTICLE_ID +
                " AND b." + DatabaseHelper.COL_BOOKMARK_USER_ID + " = ? " +
                "WHERE f." + DatabaseHelper.COL_FEED_IS_ENABLED + " = 1 " +
                "AND f." + DatabaseHelper.COL_FEED_CATEGORY + " = ? " +
                "ORDER BY a." + DatabaseHelper.COL_ARTICLE_PUB_DATE + " DESC";

        try (Cursor cursor = db.rawQuery(sql, new String[]{ String.valueOf(currentUserId), category })) {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    list.add(cursorToArticle(cursor));
                } while (cursor.moveToNext());
            }
        }
        return list;
    }

    public List<Article> queryArticles(FilterState filterState, long currentUserId, String searchQuery) {
        List<Article> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT a.*, f.").append(DatabaseHelper.COL_FEED_TITLE).append(" AS feed_title, ")
                .append("(CASE WHEN b.").append(DatabaseHelper.COL_BOOKMARK_ID).append(" IS NOT NULL THEN 1 ELSE 0 END) AS is_bookmarked ")
                .append("FROM ").append(DatabaseHelper.TABLE_ARTICLES).append(" a ")
                .append("JOIN ").append(DatabaseHelper.TABLE_FEEDS).append(" f ON f.").append(DatabaseHelper.COL_FEED_ID).append(" = a.").append(DatabaseHelper.COL_ARTICLE_FEED_ID).append(" ")
                .append("LEFT JOIN ").append(DatabaseHelper.TABLE_BOOKMARKS).append(" b ON b.").append(DatabaseHelper.COL_BOOKMARK_ARTICLE_ID).append(" = a.").append(DatabaseHelper.COL_ARTICLE_ID)
                .append(" AND b.").append(DatabaseHelper.COL_BOOKMARK_USER_ID).append(" = ? ")
                .append("WHERE f.").append(DatabaseHelper.COL_FEED_IS_ENABLED).append(" = 1 ");

        List<String> argsList = new ArrayList<>();
        argsList.add(String.valueOf(currentUserId));

        if (filterState != null) {
            if (filterState.getFilterType() == FilterState.FilterType.UNREAD) {
                sql.append("AND a.").append(DatabaseHelper.COL_ARTICLE_IS_READ).append(" = 0 ");
            } else if (filterState.getFilterType() == FilterState.FilterType.BY_FEED && filterState.getSelectedFeedId() > 0) {
                sql.append("AND a.").append(DatabaseHelper.COL_ARTICLE_FEED_ID).append(" = ? ");
                argsList.add(String.valueOf(filterState.getSelectedFeedId()));
            }
        }

        if (searchQuery != null && !searchQuery.trim().isEmpty()) {
            sql.append("AND (a.").append(DatabaseHelper.COL_ARTICLE_TITLE).append(" LIKE ? OR a.").append(DatabaseHelper.COL_ARTICLE_DESCRIPTION).append(" LIKE ?) ");
            String wild = "%" + searchQuery.trim() + "%";
            argsList.add(wild);
            argsList.add(wild);
        }

        if (filterState != null && filterState.getSortOrder() != null) {
            switch (filterState.getSortOrder()) {
                case OLDEST:
                    sql.append("ORDER BY a.").append(DatabaseHelper.COL_ARTICLE_PUB_DATE).append(" ASC");
                    break;
                case FEED_A_TO_Z:
                    sql.append("ORDER BY f.").append(DatabaseHelper.COL_FEED_TITLE).append(" COLLATE NOCASE ASC, a.").append(DatabaseHelper.COL_ARTICLE_PUB_DATE).append(" DESC");
                    break;
                case NEWEST:
                default:
                    sql.append("ORDER BY a.").append(DatabaseHelper.COL_ARTICLE_PUB_DATE).append(" DESC");
                    break;
            }
        } else {
            sql.append("ORDER BY a.").append(DatabaseHelper.COL_ARTICLE_PUB_DATE).append(" DESC");
        }

        String[] selectionArgs = argsList.toArray(new String[0]);
        try (Cursor cursor = db.rawQuery(sql.toString(), selectionArgs)) {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    list.add(cursorToArticle(cursor));
                } while (cursor.moveToNext());
            }
        }
        return list;
    }

    public List<Article> searchArticles(String query, long currentUserId) {
        return queryArticles(new FilterState(), currentUserId, query);
    }

    public Article getArticleById(long articleId, long currentUserId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String sql = "SELECT a.*, f." + DatabaseHelper.COL_FEED_TITLE + " AS feed_title, " +
                "(CASE WHEN b." + DatabaseHelper.COL_BOOKMARK_ID + " IS NOT NULL THEN 1 ELSE 0 END) AS is_bookmarked " +
                "FROM " + DatabaseHelper.TABLE_ARTICLES + " a " +
                "LEFT JOIN " + DatabaseHelper.TABLE_FEEDS + " f ON f." + DatabaseHelper.COL_FEED_ID + " = a." + DatabaseHelper.COL_ARTICLE_FEED_ID + " " +
                "LEFT JOIN " + DatabaseHelper.TABLE_BOOKMARKS + " b ON b." + DatabaseHelper.COL_BOOKMARK_ARTICLE_ID + " = a." + DatabaseHelper.COL_ARTICLE_ID +
                " AND b." + DatabaseHelper.COL_BOOKMARK_USER_ID + " = ? " +
                "WHERE a." + DatabaseHelper.COL_ARTICLE_ID + " = ?";

        try (Cursor cursor = db.rawQuery(sql, new String[]{ String.valueOf(currentUserId), String.valueOf(articleId) })) {
            if (cursor != null && cursor.moveToFirst()) {
                return cursorToArticle(cursor);
            }
        }
        return null;
    }

    public void updateContent(long articleId, String content) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(DatabaseHelper.COL_ARTICLE_CONTENT, content);
        db.update(DatabaseHelper.TABLE_ARTICLES, cv, DatabaseHelper.COL_ARTICLE_ID + " = ?", new String[]{ String.valueOf(articleId) });
    }

    public String getContent(long articleId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String sql = "SELECT " + DatabaseHelper.COL_ARTICLE_CONTENT + " FROM " + DatabaseHelper.TABLE_ARTICLES + " WHERE " + DatabaseHelper.COL_ARTICLE_ID + " = ?";
        try (Cursor cursor = db.rawQuery(sql, new String[]{ String.valueOf(articleId) })) {
            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getString(0);
            }
        }
        return null;
    }

    public void markAsRead(long articleId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(DatabaseHelper.COL_ARTICLE_IS_READ, 1);
        db.update(DatabaseHelper.TABLE_ARTICLES, cv, DatabaseHelper.COL_ARTICLE_ID + " = ?", new String[]{ String.valueOf(articleId) });
    }

    public void deleteOldArticlesExceptBookmarked(long maxAgeMillis) {
        long threshold = System.currentTimeMillis() - maxAgeMillis;
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String where = DatabaseHelper.COL_ARTICLE_PUB_DATE + " < ? AND " +
                DatabaseHelper.COL_ARTICLE_ID + " NOT IN (SELECT DISTINCT " + DatabaseHelper.COL_BOOKMARK_ARTICLE_ID + " FROM " + DatabaseHelper.TABLE_BOOKMARKS + ")";
        db.delete(DatabaseHelper.TABLE_ARTICLES, where, new String[]{ String.valueOf(threshold) });
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
