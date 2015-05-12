package com.crea3d.remind2call;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;

import com.crea3d.remind2call.data.Contact;

import java.io.Externalizable;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by clucera on 11/03/15.
 */
public class SetDataActivity extends FragmentActivity {

    public static final String BUNDLE_CONTACT = "contactBundle";

    private Contact contact;

    private Calendar calendar;
    private ImageView photo;
    private TextView nome, data, ora;
    private EditText note;
    private Button delete, save;

    private java.text.DateFormat dateFormat = java.text.DateFormat.getDateInstance(java.text.DateFormat.SHORT);
    private java.text.DateFormat timeFormat = java.text.DateFormat.getTimeInstance(java.text.DateFormat.SHORT);

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(getIntent() != null){
            contact = (Contact) getIntent().getSerializableExtra(BUNDLE_CONTACT);
        }

        if(contact == null){
            setResult(RESULT_CANCELED);
            finish();
        }
        calendar = Calendar.getInstance();
        if(contact.getDateTime() < 0){

            calendar.add(Calendar.MINUTE, 10);
            contact.setDateTime(calendar.getTimeInMillis());

        }

        calendar.setTimeInMillis(contact.getDateTime());
        setContentView(R.layout.activity_set_reminder);

        photo = (ImageView) findViewById(R.id.contact_photo);
        nome = (TextView) findViewById(R.id.contact_contact);
        data = (TextView) findViewById(R.id.contact_data);
        ora = (TextView) findViewById(R.id.contact_time);
        note = (EditText) findViewById(R.id.contact_note);
        delete = (Button) findViewById(R.id.button_delete);
        save = (Button) findViewById(R.id.button_save);


        if(contact.getPhotoUri() != null) {
            Uri imageUri = Uri.parse(contact.getPhotoUri());
            try {
                photo.setImageDrawable(Drawable.createFromStream(getContentResolver().openInputStream(imageUri), null));
            } catch (Exception ex){
                ex.printStackTrace();
            }
        }

        nome.setText(String.format(getString(R.string.contact_remind), contact.getName()));
        note.setText(contact.getNote());
        data.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDatePicker();
            }
        });
        ora.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openTimePicker();
            }
        });

        delete.setVisibility(contact.isNew() ? View.GONE : View.VISIBLE);
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO da implementare
            }
        });

        note.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                //not used
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                contact.setNote(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
                //not used
            }
        });

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDeleteAlert();
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(calendar.before(Calendar.getInstance())){
                    showSaveAlert();
                } else {
                    saveContact(true);
                }
            }
        });
    }
    private void showDeleteAlert(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.delete_confirm_title);
        builder.setMessage(getString(R.string.delete_confirm_message, contact.getName(), java.text.DateFormat.getDateTimeInstance().format(new Date(contact.getDateTime()))));
        builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                Uri uri = ReminderContract.Reminders.buildUri(contact.getId());
                String selection = ReminderContract.Reminders._ID + " = ?";
                String[] selectionArgs = {""+contact.getId()};
                getContentResolver().delete(uri, selection, selectionArgs);
                finish();
            }
        });
        builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    private void showSaveAlert(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.save_confirm_title);
        builder.setMessage(getString(R.string.save_confirm_message, contact.getName(), java.text.DateFormat.getDateTimeInstance().format(new Date(contact.getDateTime()))));
        builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                saveContact(false);
                finish();
            }
        });
        builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    private void saveContact(boolean setAlarm){

        calendar.set(Calendar.SECOND, 0);

        ContentValues values = new ContentValues();
        values.put(ReminderContract.Reminders.COLUMN_CONTACT_NAME, contact.getName());
        values.put(ReminderContract.Reminders.COLUMN_CONTACT_NUMBER, contact.getNumber());
        values.put(ReminderContract.Reminders.COLUMN_PHOTO_URI, contact.getPhotoUri());
        values.put(ReminderContract.Reminders.COLUMN_DATETIME, calendar.getTimeInMillis());
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

        contact.setDateTime(calendar.getTimeInMillis());

        if(setAlarm) {
            new AlarmReceiver().setAlarm(this, contact);
        }
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshUI();
    }

    public void refreshUI(){
        calendar.setTimeInMillis(contact.getDateTime());
        calendar.set(Calendar.SECOND, 0);
        Calendar today = Calendar.getInstance();
        if(calendar.before(today)){
            if(DateUtils.isToday(calendar.getTimeInMillis())){
                data.setError(null);
            } else {
                data.setError("insert a future date");
            }
            ora.setError("insert a future date");

        } else {
            data.setError(null);
            ora.setError(null);
        }
        data.setText(dateFormat.format(calendar.getTime()));
        ora.setText(timeFormat.format(calendar.getTime()));

    }

    public void openTimePicker(){
        new TimePickerFragment().show(getSupportFragmentManager(), "DIALOG");
    }

    public void openDatePicker(){
        new DatePickerFragment().show(getSupportFragmentManager(), "DIALOG");
    }


    @SuppressLint("ValidFragment")
    public class TimePickerFragment extends DialogFragment
            implements TimePickerDialog.OnTimeSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);

            TimePickerDialog dialog = new TimePickerDialog(getActivity(), this, hour, minute,
                    DateFormat.is24HourFormat(getActivity()));

            // Create a new instance of TimePickerDialog and return it
            return dialog;
        }

        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
            calendar.set(Calendar.MINUTE, minute);
            contact.setDateTime(calendar.getTimeInMillis());
            refreshUI();
        }

    }

    @SuppressLint("ValidFragment")
    public class DatePickerFragment extends DialogFragment
            implements DatePickerDialog.OnDateSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker

            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog dialog = new DatePickerDialog(getActivity(), this, year, month, day);
            return dialog;
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DATE, day);
            contact.setDateTime(calendar.getTimeInMillis());
            refreshUI();
        }
    }
}

