package com.example.fitnesstracker;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "FitnessDB";
    private static final int DB_VERSION = 2;

    public static final String TABLE_ACTIVITIES = "activities";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_WORKOUT = "workout";
    public static final String COLUMN_DURATION = "duration";
    public static final String COLUMN_CALORIES = "calories";
    public static final String COLUMN_CATEGORY = "category";
    public static final String COLUMN_DATE = "date";

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
                "CREATE TABLE " + TABLE_ACTIVITIES + "(" +
                        COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                        COLUMN_WORKOUT + " TEXT," +
                        COLUMN_DURATION + " INTEGER," +
                        COLUMN_CALORIES + " INTEGER," +
                        COLUMN_CATEGORY + " TEXT," +
                        COLUMN_DATE + " TEXT)"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_ACTIVITIES);
            onCreate(db);
        }
    }

    public long insertActivity(String workout, int duration, int calories, String category, String date) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_WORKOUT, workout);
        cv.put(COLUMN_DURATION, duration);
        cv.put(COLUMN_CALORIES, calories);
        cv.put(COLUMN_CATEGORY, category);
        cv.put(COLUMN_DATE, date);
        return db.insert(TABLE_ACTIVITIES, null, cv);
    }

    public int updateActivity(int id, String workout, int duration, int calories, String category, String date) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_WORKOUT, workout);
        cv.put(COLUMN_DURATION, duration);
        cv.put(COLUMN_CALORIES, calories);
        cv.put(COLUMN_CATEGORY, category);
        cv.put(COLUMN_DATE, date);
        return db.update(TABLE_ACTIVITIES, cv, COLUMN_ID + "=?", new String[]{String.valueOf(id)});
    }

    public void deleteActivity(int id) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_ACTIVITIES, COLUMN_ID + "=?", new String[]{String.valueOf(id)});
    }

    public Cursor getActivities() {
        SQLiteDatabase db = getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_ACTIVITIES + " ORDER BY " + COLUMN_DATE + " DESC", null);
    }

    public Cursor searchActivities(String query) {
        SQLiteDatabase db = getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_ACTIVITIES + " WHERE " + COLUMN_WORKOUT + " LIKE ?", new String[]{"%" + query + "%"});
    }

    public Cursor getWeeklyStats() {
        SQLiteDatabase db = getReadableDatabase();
        return db.rawQuery("SELECT " + COLUMN_DATE + ", SUM(" + COLUMN_CALORIES + ") FROM " + TABLE_ACTIVITIES + 
                " GROUP BY " + COLUMN_DATE + " ORDER BY " + COLUMN_DATE + " DESC LIMIT 7", null);
    }
}