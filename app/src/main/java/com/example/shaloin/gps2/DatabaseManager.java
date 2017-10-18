package com.example.shaloin.gps2;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

/**
 * Created by shaloin on 16/10/17.
 */

public class DatabaseManager {

    private UserDatabase dbHelper;

    private Context context;

    private SQLiteDatabase database;

    public DatabaseManager(Context c) {
        context = c;
    }

    public DatabaseManager open() throws SQLException {
        dbHelper = new UserDatabase(context);
        database = dbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        dbHelper.close();
    }

    public void insert(String name, String number) {
        ContentValues contentValue = new ContentValues();
        contentValue.put(UserDatabase.NAME, name);
        contentValue.put(UserDatabase.NUMBER, number);
        database.insert(UserDatabase.TABLE_NAME, null, contentValue);
    }

    public Cursor fetch() {
        String[] columns = new String[] { UserDatabase._ID, UserDatabase.NAME, UserDatabase.NUMBER };
        Cursor cursor = database.query(UserDatabase.TABLE_NAME, columns, null, null, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
        }
        return cursor;
    }

    public int update(long _id, String name, String number) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(UserDatabase.NAME, name);
        contentValues.put(UserDatabase.NUMBER, number);
        int i = database.update(UserDatabase.TABLE_NAME, contentValues, UserDatabase._ID + " = " + _id, null);
        return i;
    }

    public void delete(long _id) {
        database.delete(UserDatabase.TABLE_NAME, UserDatabase._ID + "=" + _id, null);
    }

}
