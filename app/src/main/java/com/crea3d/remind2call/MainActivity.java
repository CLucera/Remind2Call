package com.crea3d.remind2call;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.LoaderManager;
import android.support.v7.app.ActionBarActivity;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.crea3d.remind2call.data.Contact;
import com.crea3d.remind2call.widget.VerticalTextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


public class MainActivity extends ActionBarActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int REQUEST_PICK = 100;
    private static final int LOADER_REMINDER_ID = 900;
    private static final int LOADER_CONTACTS_ID = 901;

    private static final SimpleDateFormat timeFormatter = new SimpleDateFormat("HH:mm");
    private static final DateFormat dateFormatter = DateFormat.getDateInstance(DateFormat.SHORT);

    private ListView lista, listaContatti;
    private RemindersAdapter adapter;
    private TextView emptyText;
    Cursor cur;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setIcon(R.drawable.icon);
        setContentView(R.layout.activity_main);

        getSupportLoaderManager().initLoader(LOADER_REMINDER_ID, null, this);

        lista = (ListView) findViewById(R.id.list);
        listaContatti = (ListView) findViewById(R.id.list_contacts);

        emptyText = (TextView) findViewById(R.id.list_emptytext);


        Uri uri = ReminderContract.Reminders.CONTENT_URI;

        lista.setEmptyView(emptyText);

        if(listaContatti != null){
            getSupportLoaderManager().initLoader(LOADER_CONTACTS_ID, null, this);
        }

        // bind listeners

        emptyText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getNewContact();
            }
        });

        lista.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(MainActivity.this, SetDataActivity.class);
                intent.putExtra(SetDataActivity.BUNDLE_CONTACT, (Contact) lista.getAdapter().getItem(position));
                startActivityForResult(intent, 0);


            }
        });
        lista.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                showDeleteAlert((Contact) lista.getAdapter().getItem(position));
                return true;
            }
        });

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_add) {
            getNewContact();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void getNewContact()
    {
        Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        intent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
        startActivityForResult(intent, REQUEST_PICK);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data == null) return;
        Uri contactUri = data.getData();
        // We only need the NUMBER column, because there will be only one row in the result
        String[] projection = {ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER,
                ContactsContract.CommonDataKinds.Phone.PHOTO_URI};

        // Perform the query on the contact to get the NUMBER column
        // We don't need a selection or sort order (there's only one result for the given URI)
        // CAUTION: The query() method should be called from a separate thread to avoid blocking
        // your app's UI thread. (For simplicity of the sample, this code doesn't do that.)
        // Consider using CursorLoader to perform the query.
        Cursor cursor = getContentResolver()
                .query(contactUri, projection, null, null, null);
        cursor.moveToFirst();

        // Retrieve the phone number from the NUMBER column
        int columnName = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
        int columnPhone = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
        int columnPhoto = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI);

        Contact contact = new Contact();
        contact.setName(cursor.getString(columnName));
        contact.setNumber(cursor.getString(columnPhone));
        contact.setPhotoUri(cursor.getString(columnPhoto));

        cursor.close();

        Intent intent = new Intent(this, SetDataActivity.class);
        intent.putExtra(SetDataActivity.BUNDLE_CONTACT, contact);

        startActivityForResult(intent, 0);
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void showDeleteAlert(final Contact contact){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.delete_confirm_title);
        builder.setMessage(getString(R.string.delete_confirm_message, contact.getName(), DateFormat.getDateTimeInstance().format(new Date(contact.getDateTime()))));
        builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                Uri uri = ReminderContract.Reminders.buildUri(contact.getId());
                String selection = ReminderContract.Reminders._ID + " = ?";
                String[] selectionArgs = {""+contact.getId()};
                getContentResolver().delete(uri, selection, selectionArgs);
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

    @Override
    public android.support.v4.content.Loader<Cursor> onCreateLoader(int i, Bundle bundle) {

        if(i == LOADER_REMINDER_ID) {
            String selection = null;
            String[] selectionArgs = null;
            String order = ReminderContract.Reminders.COLUMN_DATETIME;
            return new android.support.v4.content.CursorLoader(this, ReminderContract.Reminders.CONTENT_URI, Contact.PROJECTION, selection, selectionArgs, order);
        } else {
            String[] projection = {ContactsContract.CommonDataKinds.Phone._ID, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,ContactsContract.CommonDataKinds.Phone.NUMBER, ContactsContract.CommonDataKinds.Phone.PHOTO_URI };
            String selection = ContactsContract.CommonDataKinds.Phone.HAS_PHONE_NUMBER + " = 1";
            String order = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME;
            return new android.support.v4.content.CursorLoader(this, ContactsContract.CommonDataKinds.Phone.CONTENT_URI, projection, selection, null, order);
        }
    }

    @Override
    public void onLoadFinished(android.support.v4.content.Loader<Cursor> cursorLoader, Cursor cursor) {
        if(cursorLoader.getId() == LOADER_REMINDER_ID) {
            this.cur = cursor;
            this.adapter = new RemindersAdapter(this, cur, true);
            this.lista.setAdapter(adapter);
        } else {
            this.listaContatti.setAdapter(new ContactsAdapter(this, cursor, true));
        }
    }

    @Override
    public void onLoaderReset(android.support.v4.content.Loader<Cursor> cursorLoader) {

    }

    private class RemindersAdapter extends CursorAdapter {

        public RemindersAdapter(Context context, Cursor c, boolean autoRequery) {
            super(context, c, autoRequery);
        }

        @Override
        public Object getItem(int position) {
            Contact contact = null;
            Cursor cursor = getCursor();
            if(cursor.moveToPosition(position)) {
                contact = new Contact(cursor);

            }

            return contact;
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            View view = LayoutInflater.from(context).inflate(R.layout.listitem_contact, parent, false);
            ViewHolder holder = new ViewHolder(view);
            view.setTag(holder);
            return view;
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {

            final String contactName = cursor.getString(cursor.getColumnIndex(ReminderContract.Reminders.COLUMN_CONTACT_NAME));
            final String contactNumber = cursor.getString(cursor.getColumnIndex(ReminderContract.Reminders.COLUMN_CONTACT_NUMBER));
            String contactImageUri = cursor.getString(cursor.getColumnIndex(ReminderContract.Reminders.COLUMN_PHOTO_URI));
            long timestampMillis = cursor.getLong(cursor.getColumnIndex(ReminderContract.Reminders.COLUMN_DATETIME));

            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(timestampMillis);

            //bindview
            ViewHolder holder = (ViewHolder) view.getTag();


            try {
                holder.contactImageView.setImageDrawable(Drawable.createFromStream(getContentResolver().openInputStream(Uri.parse(contactImageUri)), null));
            } catch (Exception ex) {
                holder.contactImageView.setImageResource(R.drawable.contact);
            }

            holder.contactNameTextView.setText(contactName);
            if(DateUtils.isToday(calendar.getTimeInMillis())) {
                holder.contactDate.setText(getString(R.string.list_today));
                holder.bg.setBackgroundResource(R.color.red);
                holder.innerBg.setBackgroundResource(R.color.light_red);
            }
            else if((calendar.before(Calendar.getInstance()))){
                holder.contactDate.setText(dateFormatter.format(calendar.getTime()));
                holder.bg.setBackgroundResource(R.color.red);
                holder.innerBg.setBackgroundResource(R.color.light_red);
            }
            else {
                holder.contactDate.setText(dateFormatter.format(calendar.getTime()));
                holder.bg.setBackgroundResource(R.color.accent_material_light);
                holder.innerBg.setBackgroundResource(R.color.light_green);
            }
            holder.contactTime.setText(getString(R.string.list_remind_at, timeFormatter.format(calendar.getTime())));
            holder.phoneIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_DIAL);
                    intent.setData(Uri.parse("tel:" + contactNumber));
                    try {
                        startActivity(intent);
                    }
                    catch (Exception ex){
                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                        builder.setTitle(R.string.no_activity_alert_title);
                        builder.setMessage(getString(R.string.no_activity_alert_message, contactName, contactNumber));
                        builder.setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                        builder.show();
                    }

                }
            });

        }
    }

    public static class ViewHolder {
        public final View bg, innerBg;
        public final ImageView contactImageView;
        public final TextView contactNameTextView;
        public final TextView contactTime;
        public final VerticalTextView contactDate;
        public final ImageView phoneIcon;

        public ViewHolder(View view) {
            bg =  view.findViewById(R.id.listitem_bg);
            innerBg =  view.findViewById(R.id.listitem_bg_inner);
            contactImageView = (ImageView) view.findViewById(R.id.listitem_contatto_img);
            contactNameTextView = (TextView) view.findViewById(R.id.listitem_nome);
            contactTime = (TextView) view.findViewById(R.id.listitem_remind);
            contactDate = (VerticalTextView) view.findViewById(R.id.listitem_date);
            phoneIcon = (ImageView) view.findViewById(R.id.listitem_phone);
        }
    }


    private class ContactsAdapter extends CursorAdapter {

        public ContactsAdapter(Context context, Cursor c, boolean autoRequery) {
            super(context, c, autoRequery);
        }


        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            View view = LayoutInflater.from(context).inflate(R.layout.listitem_contact_fromsystem, parent, false);
            ContactViewHolder holder = new ContactViewHolder(view);
            view.setTag(holder);
            return view;
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {

            final String contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            final String contactNumber = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            final String contactImageUri = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI));

            //bindview
            ContactViewHolder holder = (ContactViewHolder) view.getTag();


            try {
                holder.contactImageView.setImageDrawable(Drawable.createFromStream(getContentResolver().openInputStream(Uri.parse(contactImageUri)), null));
            } catch (Exception ex) {
                holder.contactImageView.setImageResource(R.drawable.contact);
            }

            holder.contactNameTextView.setText(contactName);

            holder.contactNumber.setText(contactNumber);
            holder.addIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Contact contact = new Contact();
                    contact.setName(contactName);
                    contact.setNumber(contactNumber);
                    contact.setPhotoUri(contactImageUri);

                    Intent intent = new Intent(MainActivity.this, SetDataActivity.class);
                    intent.putExtra(SetDataActivity.BUNDLE_CONTACT, contact);

                    startActivityForResult(intent, 0);
                }
            });

        }
    }

    public static class ContactViewHolder {
        public final ImageView contactImageView;
        public final TextView contactNameTextView;
        public final TextView contactNumber;
        public final ImageView addIcon;

        public ContactViewHolder(View view) {
            contactImageView = (ImageView) view.findViewById(R.id.listitem_contatto_img);
            contactNameTextView = (TextView) view.findViewById(R.id.listitem_nome);
            contactNumber= (TextView) view.findViewById(R.id.listitem_number);
            addIcon = (ImageView) view.findViewById(R.id.listitem_add);
        }
    }


}
