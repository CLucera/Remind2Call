package com.crea3d.remind2call.data;

import android.database.Cursor;

import com.crea3d.remind2call.ReminderContract;

import java.io.Serializable;

/**
 * Created by clucera on 15/03/15.
 */
public class Contact implements Serializable {

    public static final int STATUS_REMIND = 0;
    public static final int STATUS_CALLED = 1;

    public static final String[] PROJECTION = {ReminderContract.Reminders._ID,
            ReminderContract.Reminders.COLUMN_CONTACT_NAME,
            ReminderContract.Reminders.COLUMN_CONTACT_NUMBER,
            ReminderContract.Reminders.COLUMN_PHOTO_URI,
            ReminderContract.Reminders.COLUMN_DATETIME,
            ReminderContract.Reminders.COLUMN_NOTE,
            ReminderContract.Reminders.COLUMN_STATUS
    };

    private long id = -1;
    private String name;
    private String photoUri;
    private String number;
    private long dateTime = -1;
    private String note ="";
    private int status;

    public Contact()
    {

    }

    public Contact(Cursor cursor)
    {
        setId(cursor.getLong(cursor.getColumnIndex(ReminderContract.Reminders._ID)));
        setName(cursor.getString(cursor.getColumnIndex(ReminderContract.Reminders.COLUMN_CONTACT_NAME)));
        setNumber(cursor.getString(cursor.getColumnIndex(ReminderContract.Reminders.COLUMN_CONTACT_NUMBER)));
        setPhotoUri(cursor.getString(cursor.getColumnIndex(ReminderContract.Reminders.COLUMN_PHOTO_URI)));
        setDateTime(cursor.getLong(cursor.getColumnIndex(ReminderContract.Reminders.COLUMN_DATETIME)));
        setNote(cursor.getString(cursor.getColumnIndex(ReminderContract.Reminders.COLUMN_NOTE)));
        setStatus(cursor.getInt(cursor.getColumnIndex(ReminderContract.Reminders.COLUMN_STATUS)));
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhotoUri() {
        return photoUri;
    }

    public void setPhotoUri(String photoUri) {
        this.photoUri = photoUri;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public long getDateTime() {
        return dateTime;
    }

    public void setDateTime(long dateTime) {
        this.dateTime = dateTime;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public boolean isNew(){
        return id < 0;
    }
}
