package com.crea3d.remind2call;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

/**
 * Created by clucera on 06/03/15.
 */
public class ReminderProvider extends ContentProvider {

    private static final UriMatcher matcher = buildUriMatcher();

    DatabaseHelper db;

    @Override
    public boolean onCreate() {
        db = new DatabaseHelper(getContext());
        return false;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        Cursor cursor = null;
        switch (matcher.match(uri)){
            case REMINDERS:
                cursor =  db.getReadableDatabase().query(ReminderContract.Reminders.TABLE_NAME,projection,selection,selectionArgs,null,null,sortOrder);
                break;
            case SINGLE_REMINDER:
                cursor = db.getReadableDatabase().query(ReminderContract.Reminders.TABLE_NAME,projection,selection,selectionArgs,null,null,sortOrder);
                break;
        }

        if(cursor != null){
            cursor.setNotificationUri(getContext().getContentResolver(), uri);
        }
        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        switch (matcher.match(uri)){
            case REMINDERS:
                return ReminderContract.Reminders.CONTENT_TYPE;
            case SINGLE_REMINDER:
                return ReminderContract.Reminders.CONTENT_ITEM_TYPE;
            default:
                return null;
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {

        Uri returnUri = null;

        long _id = db.getWritableDatabase().insert(ReminderContract.Reminders.TABLE_NAME, null, values);

        if ( _id > 0 ) {
            returnUri = ReminderContract.Reminders.buildUri(_id);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        db.close();

        return returnUri;

    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int deletedRow = 0;

        switch (matcher.match(uri)){
            case REMINDERS:
                deletedRow = db.getWritableDatabase().delete(ReminderContract.Reminders.TABLE_NAME,null,null);
                break;
            case SINGLE_REMINDER:
                deletedRow = db.getWritableDatabase().delete(ReminderContract.Reminders.TABLE_NAME, selection, selectionArgs);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        db.close();

        return deletedRow;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        int rowsUpdated = 0;

        switch (matcher.match(uri)) {
           case SINGLE_REMINDER:
               rowsUpdated = db.getWritableDatabase().update(
                        ReminderContract.Reminders.TABLE_NAME,
                        values,
                        selection,
                        selectionArgs
                );
               break;
        }

        getContext().getContentResolver().notifyChange(uri, null);
        db.close();

        return rowsUpdated;
    }


    private static final int REMINDERS = 100;
    private static final int SINGLE_REMINDER = 101;

    private static UriMatcher buildUriMatcher() {

        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = ReminderContract.CONTENT_AUTHORITY;

        matcher.addURI(authority, ReminderContract.Reminders.BASE_PATH, REMINDERS);
        matcher.addURI(authority, ReminderContract.Reminders.BASE_PATH + "/#", SINGLE_REMINDER);

        return matcher;
    }
}
