package com.crea3d.remind2call;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v4.app.NotificationCompat;
import android.text.format.DateUtils;
import android.util.Log;
import android.widget.Toast;

import com.crea3d.remind2call.data.Contact;

import java.util.Calendar;

public class AlarmReceiver extends BroadcastReceiver {

    private AlarmManager alarmManager;
    private PendingIntent alarmIntent;

    public static final int REQUEST_CHECK = 0;
    public static final String ACTION_CHECK = "actionCheck";
    public static final String EXTRA_CONTACT = "extraContact";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() != null && intent.getAction().equals(ACTION_CHECK)) {
            Contact contact = (Contact) intent.getSerializableExtra(EXTRA_CONTACT);
            if(contact != null) {
                showNotification(context, contact);
            }
        } else {
            //notUsed
        }
    }


    public void setAlarm(Context context, Contact contact) {
        if (alarmManager != null) {
            return;
        }

        alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.setAction(ACTION_CHECK);
        intent.putExtra(EXTRA_CONTACT, contact);

        alarmIntent = PendingIntent.getBroadcast(context, (int) contact.getId(), intent, PendingIntent.FLAG_UPDATE_CURRENT);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, contact.getDateTime(), alarmIntent);
        }
        else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, contact.getDateTime(), alarmIntent);
        }


    }


    public void startAlarms(Context context) {
        String selections = ReminderContract.Reminders.COLUMN_DATETIME + " > ?";
        String[] selectionArgs = {"" + System.currentTimeMillis()};
        Cursor cursor = context.getContentResolver().query(ReminderContract.Reminders.CONTENT_URI, Contact.PROJECTION, selections, selectionArgs, null);

        while(cursor.moveToNext()) {
          setAlarm(context, new Contact(cursor));
        }
        cursor.close();

    }


    private void showNotification(Context context, Contact contact) {

        String where = ReminderContract.Reminders._ID + " = ?";
        String[] selectionArgs = {""+contact.getId()};
        Cursor cursor =  context.getContentResolver().query(ReminderContract.Reminders.buildUri(contact.getId()), Contact.PROJECTION, where, selectionArgs, null);

        if(cursor != null && cursor.moveToFirst()) {
            Contact checkContact = new Contact(cursor);

            if(contact.getDateTime() != checkContact.getDateTime()){
                setAlarm(context, checkContact);
                return;
            }

            cursor.close();
        }

        Intent openIntent = new Intent(context, MainActivity.class);
        openIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        Intent callIntent = new Intent(Intent.ACTION_DIAL);
        callIntent.setData(Uri.parse("tel:" + contact.getNumber()));

        Intent delayIntent = new Intent(context, DelayService.class);
        delayIntent.putExtra(EXTRA_CONTACT, contact);

        PendingIntent pendingOpen = PendingIntent.getActivity(context, 0, openIntent, 0);
        PendingIntent pendingCall = PendingIntent.getActivity(context, (int) contact.getId(), callIntent, 0);
        PendingIntent pendingDelay= PendingIntent.getService(context, (int) contact.getId(), delayIntent, 0);

        Bitmap largeIcon = null;

        try{
            Uri imageUri = Uri.parse(contact.getPhotoUri());
            largeIcon = MediaStore.Images.Media.getBitmap(context.getContentResolver(),imageUri);
        } catch (Exception ex){

            largeIcon = BitmapFactory.decodeResource(context.getResources(), R.drawable.contact);
        }


        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context);
        notificationBuilder.setDefaults(Notification.DEFAULT_SOUND);
        notificationBuilder.setContentTitle(context.getString(R.string.app_name));
        notificationBuilder.setSmallIcon(R.drawable.icon);
        notificationBuilder.setLargeIcon(largeIcon);
        notificationBuilder.setContentText(context.getString(R.string.notify_remind, contact.getName()));
        notificationBuilder.setContentIntent(pendingOpen);
        notificationBuilder.setGroup(context.getString(R.string.app_name));
        notificationBuilder.addAction(android.R.drawable.ic_menu_call, context.getString(R.string.action_call), pendingCall);
        notificationBuilder.addAction(android.R.drawable.ic_popup_reminder, context.getString(R.string.action_later), pendingDelay);
        notificationBuilder.setAutoCancel(true);

        Notification notification = notificationBuilder.build();
        NotificationManager notificationmanager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationmanager.notify((int) contact.getId(), notification);
    }

}
