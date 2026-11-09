package com.example.rssreader.data.local;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.rssreader.data.model.User;

public class UserDao {
    private final DatabaseHelper dbHelper;

    public UserDao(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    public long insert(User user) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(DatabaseHelper.COL_USER_FULL_NAME, user.getFullName());
        cv.put(DatabaseHelper.COL_USER_EMAIL, user.getEmail());
        cv.put(DatabaseHelper.COL_USER_PASSWORD_HASH, user.getPasswordHash());
        cv.put(DatabaseHelper.COL_USER_SALT, user.getSalt());
        cv.put(DatabaseHelper.COL_USER_CREATED_AT, user.getCreatedAt());
        return db.insert(DatabaseHelper.TABLE_USERS, null, cv);
    }

    public User findByEmail(String email) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String selection = DatabaseHelper.COL_USER_EMAIL + " = ?";
        String[] selectionArgs = { email };

        try (Cursor cursor = db.query(DatabaseHelper.TABLE_USERS, null, selection, selectionArgs, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                return cursorToUser(cursor);
            }
        }
        return null;
    }

    public User findById(long id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String selection = DatabaseHelper.COL_USER_ID + " = ?";
        String[] selectionArgs = { String.valueOf(id) };

        try (Cursor cursor = db.query(DatabaseHelper.TABLE_USERS, null, selection, selectionArgs, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                return cursorToUser(cursor);
            }
        }
        return null;
    }

    public boolean isEmailTaken(String email) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String selection = DatabaseHelper.COL_USER_EMAIL + " = ?";
        String[] selectionArgs = { email };

        try (Cursor cursor = db.query(DatabaseHelper.TABLE_USERS, new String[]{ DatabaseHelper.COL_USER_ID }, selection, selectionArgs, null, null, null)) {
            return cursor != null && cursor.getCount() > 0;
        }
    }

    public int updateFullName(long id, String newFullName) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(DatabaseHelper.COL_USER_FULL_NAME, newFullName);
        String where = DatabaseHelper.COL_USER_ID + " = ?";
        return db.update(DatabaseHelper.TABLE_USERS, cv, where, new String[]{ String.valueOf(id) });
    }

    public int updatePassword(long id, String newPasswordHash, String newSalt) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(DatabaseHelper.COL_USER_PASSWORD_HASH, newPasswordHash);
        cv.put(DatabaseHelper.COL_USER_SALT, newSalt);
        String where = DatabaseHelper.COL_USER_ID + " = ?";
        return db.update(DatabaseHelper.TABLE_USERS, cv, where, new String[]{ String.valueOf(id) });
    }

    public int deleteUser(long id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String where = DatabaseHelper.COL_USER_ID + " = ?";
        return db.delete(DatabaseHelper.TABLE_USERS, where, new String[]{ String.valueOf(id) });
    }

    private User cursorToUser(Cursor cursor) {
        long id = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_ID));
        String fullName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_FULL_NAME));
        String email = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_EMAIL));
        String passwordHash = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_PASSWORD_HASH));
        String salt = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_SALT));
        long createdAt = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_CREATED_AT));

        return new User(id, fullName, email, passwordHash, salt, createdAt);
    }
}
