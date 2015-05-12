package com.crea3d.remind2call;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.crea3d.remind2call.data.Contact;

import java.util.concurrent.TimeUnit;

/**
 * Created by clucera on 05/04/15.
 */
public class DelayService extends IntentService {
    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public DelayService() {
        super(DelayService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Contact contact = (Contact) intent.getSerializableExtra(AlarmReceiver.EXTRA_CONTACT);

        if(contact == null) {
            return;
        }

        String ns = Context.NOTIFICATION_SERVICE;
        NotificationManager nMgr = (NotificationManager) this.getSystemService(ns);
        nMgr.cancel((int) contact.getId());

        contact.setDateTime(System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(5));


        ContentValues values = new ContentValues();
        values.put(ReminderContract.Reminders.COLUMN_CONTACT_NAME, contact.getName());
        values.put(ReminderContract.Reminders.COLUMN_CONTACT_NUMBER, contact.getNumber());
        values.put(ReminderContract.Reminders.COLUMN_PHOTO_URI, contact.getPhotoUri());
        values.put(ReminderContract.Reminders.COLUMN_DATETIME, contact.getDateTime());
        values.put(ReminderContract.Reminders.COLUMN_NOTE, contact.getNote());
        values.put(ReminderContract.Reminders.COLUMN_CONTACT_NAME, contact.getName());

        if(contact.isNew()){
            Uri uri = getContentResolver().insert(ReminderContract.Reminders.CONTENT_URI, values);
            contact.setId(ContentUris.parseId(uri));
        } else {
            String where = ReminderContract.Reminders._ID + " = ?";
            String[] selectionArgs = {""+contact.getId()};
            getContentResolver().update(ReminderContract.Reminders.buildUri(contact.getId()),values, where,selectionArgs);
        }


        new AlarmReceiver().setAlarm(this, contact);
    }
}
