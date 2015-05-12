package com.crea3d.remind2call;

import android.app.Activity;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by clucera on 05/03/15.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "reminderDB";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL("CREATE TABLE " + ReminderContract.Reminders.TABLE_NAME + " (" +
                    ReminderContract.Reminders._ID                      + " INTEGER PRIMARY KEY, " +
                    ReminderContract.Reminders.COLUMN_DATETIME          + " INTEGER, " +
                    ReminderContract.Reminders.COLUMN_CONTACT_NAME      + " VARCHAR, " +
                    ReminderContract.Reminders.COLUMN_CONTACT_NUMBER    + " VARCHAR, " +
                    ReminderContract.Reminders.COLUMN_PHOTO_URI         + " VARCHAR, " +
                    ReminderContract.Reminders.COLUMN_NOTE              + " TEXT, " +
                    ReminderContract.Reminders.COLUMN_STATUS            + " INTEGER)"

        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {


    }
}
