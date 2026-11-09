package com.example.rssreader.data.local;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "rss_reader.db";
    private static final int DATABASE_VERSION = 3;

    private static volatile DatabaseHelper instance;

    // Table Users
    public static final String TABLE_USERS = "users";
    public static final String COL_USER_ID = "id";
    public static final String COL_USER_FULL_NAME = "full_name";
    public static final String COL_USER_EMAIL = "email";
    public static final String COL_USER_PASSWORD_HASH = "password_hash";
    public static final String COL_USER_SALT = "salt";
    public static final String COL_USER_CREATED_AT = "created_at";

    // Table Feeds
    public static final String TABLE_FEEDS = "feeds";
    public static final String COL_FEED_ID = "id";
    public static final String COL_FEED_TITLE = "title";
    public static final String COL_FEED_URL = "url";
    public static final String COL_FEED_ICON_URL = "icon_url";
    public static final String COL_FEED_CATEGORY = "category";
    public static final String COL_FEED_IS_ENABLED = "is_enabled";

    // Table Articles
    public static final String TABLE_ARTICLES = "articles";
    public static final String COL_ARTICLE_ID = "id";
    public static final String COL_ARTICLE_FEED_ID = "feed_id";
    public static final String COL_ARTICLE_TITLE = "title";
    public static final String COL_ARTICLE_LINK = "link";
    public static final String COL_ARTICLE_DESCRIPTION = "description";
    public static final String COL_ARTICLE_IMAGE_URL = "image_url";
    public static final String COL_ARTICLE_PUB_DATE = "pub_date";
    public static final String COL_ARTICLE_IS_READ = "is_read";
    public static final String COL_ARTICLE_CONTENT = "content";

    // Table Bookmarks
    public static final String TABLE_BOOKMARKS = "bookmarks";
    public static final String COL_BOOKMARK_ID = "id";
    public static final String COL_BOOKMARK_USER_ID = "user_id";
    public static final String COL_BOOKMARK_ARTICLE_ID = "article_id";
    public static final String COL_BOOKMARK_SAVED_AT = "saved_at";

    // Table Reading History
    public static final String TABLE_READING_HISTORY = "reading_history";
    public static final String COL_HISTORY_ID = "id";
    public static final String COL_HISTORY_USER_ID = "user_id";
    public static final String COL_HISTORY_ARTICLE_ID = "article_id";
    public static final String COL_HISTORY_READ_AT = "read_at";

    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create users table
        db.execSQL("CREATE TABLE " + TABLE_USERS + " (" +
                COL_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_USER_FULL_NAME + " TEXT, " +
                COL_USER_EMAIL + " TEXT UNIQUE NOT NULL, " +
                COL_USER_PASSWORD_HASH + " TEXT NOT NULL, " +
                COL_USER_SALT + " TEXT NOT NULL, " +
                COL_USER_CREATED_AT + " INTEGER" +
                ");");

        // Create feeds table
        db.execSQL("CREATE TABLE " + TABLE_FEEDS + " (" +
                COL_FEED_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_FEED_TITLE + " TEXT, " +
                COL_FEED_URL + " TEXT UNIQUE NOT NULL, " +
                COL_FEED_ICON_URL + " TEXT, " +
                COL_FEED_CATEGORY + " TEXT, " +
                COL_FEED_IS_ENABLED + " INTEGER DEFAULT 1" +
                ");");

        // Create articles table (including content column for Reader mode)
        db.execSQL("CREATE TABLE " + TABLE_ARTICLES + " (" +
                COL_ARTICLE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_ARTICLE_FEED_ID + " INTEGER, " +
                COL_ARTICLE_TITLE + " TEXT, " +
                COL_ARTICLE_LINK + " TEXT UNIQUE NOT NULL, " +
                COL_ARTICLE_DESCRIPTION + " TEXT, " +
                COL_ARTICLE_IMAGE_URL + " TEXT, " +
                COL_ARTICLE_PUB_DATE + " INTEGER, " +
                COL_ARTICLE_IS_READ + " INTEGER DEFAULT 0, " +
                COL_ARTICLE_CONTENT + " TEXT, " +
                "FOREIGN KEY(" + COL_ARTICLE_FEED_ID + ") REFERENCES " + TABLE_FEEDS + "(" + COL_FEED_ID + ") ON DELETE CASCADE" +
                ");");

        // Create bookmarks table
        db.execSQL("CREATE TABLE " + TABLE_BOOKMARKS + " (" +
                COL_BOOKMARK_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_BOOKMARK_USER_ID + " INTEGER, " +
                COL_BOOKMARK_ARTICLE_ID + " INTEGER, " +
                COL_BOOKMARK_SAVED_AT + " INTEGER, " +
                "FOREIGN KEY(" + COL_BOOKMARK_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COL_USER_ID + ") ON DELETE CASCADE, " +
                "FOREIGN KEY(" + COL_BOOKMARK_ARTICLE_ID + ") REFERENCES " + TABLE_ARTICLES + "(" + COL_ARTICLE_ID + ") ON DELETE CASCADE, " +
                "UNIQUE(" + COL_BOOKMARK_USER_ID + ", " + COL_BOOKMARK_ARTICLE_ID + ")" +
                ");");

        // Create reading_history table
        db.execSQL("CREATE TABLE " + TABLE_READING_HISTORY + " (" +
                COL_HISTORY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_HISTORY_USER_ID + " INTEGER, " +
                COL_HISTORY_ARTICLE_ID + " INTEGER, " +
                COL_HISTORY_READ_AT + " INTEGER, " +
                "FOREIGN KEY(" + COL_HISTORY_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COL_USER_ID + ") ON DELETE CASCADE, " +
                "FOREIGN KEY(" + COL_HISTORY_ARTICLE_ID + ") REFERENCES " + TABLE_ARTICLES + "(" + COL_ARTICLE_ID + ") ON DELETE CASCADE, " +
                "UNIQUE(" + COL_HISTORY_USER_ID + ", " + COL_HISTORY_ARTICLE_ID + ")" +
                ");");

        // Insert suggested default feeds
        insertDefaultFeeds(db);
    }

    private void insertDefaultFeeds(SQLiteDatabase db) {
        insertFeed(db, "VnExpress - Tin mới nhất", "https://vnexpress.net/rss/tin-moi-nhat.rss", "", "Thời sự");
        insertFeed(db, "Tuổi Trẻ - Tin mới nhất", "https://tuoitre.vn/rss/tin-moi-nhat.rss", "", "Thời sự");
        insertFeed(db, "Thanh Niên - Tin mới nhất", "https://thanhnien.vn/rss/home.rss", "", "Thời sự");
        insertFeed(db, "VnExpress - Số hóa (Công nghệ)", "https://vnexpress.net/rss/so-hoa.rss", "", "Công nghệ");
        insertFeed(db, "VnExpress - Thể thao", "https://vnexpress.net/rss/the-thao.rss", "", "Thể thao");
        insertFeed(db, "VnExpress - Kinh doanh", "https://vnexpress.net/rss/kinh-doanh.rss", "", "Kinh doanh");
    }

    private void insertFeed(SQLiteDatabase db, String title, String url, String iconUrl, String category) {
        ContentValues cv = new ContentValues();
        cv.put(COL_FEED_TITLE, title);
        cv.put(COL_FEED_URL, url);
        cv.put(COL_FEED_ICON_URL, iconUrl);
        cv.put(COL_FEED_CATEGORY, category);
        cv.put(COL_FEED_IS_ENABLED, 1);
        db.insertWithOnConflict(TABLE_FEEDS, null, cv, SQLiteDatabase.CONFLICT_IGNORE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 3) {
            try {
                db.execSQL("ALTER TABLE " + TABLE_ARTICLES + " ADD COLUMN " + COL_ARTICLE_CONTENT + " TEXT;");
            } catch (Exception ignored) {}

            db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_READING_HISTORY + " (" +
                    COL_HISTORY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_HISTORY_USER_ID + " INTEGER, " +
                    COL_HISTORY_ARTICLE_ID + " INTEGER, " +
                    COL_HISTORY_READ_AT + " INTEGER, " +
                    "FOREIGN KEY(" + COL_HISTORY_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COL_USER_ID + ") ON DELETE CASCADE, " +
                    "FOREIGN KEY(" + COL_HISTORY_ARTICLE_ID + ") REFERENCES " + TABLE_ARTICLES + "(" + COL_ARTICLE_ID + ") ON DELETE CASCADE, " +
                    "UNIQUE(" + COL_HISTORY_USER_ID + ", " + COL_HISTORY_ARTICLE_ID + ")" +
                    ");");
        }
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Safe fallback - don't throw exception
    }
}
