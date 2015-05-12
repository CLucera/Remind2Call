package com.crea3d.remind2call;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by clucera on 05/03/15.
 */
public class ReminderContract {

    public static final String CONTENT_AUTHORITY = "com.crea3d.remind2call";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static class Reminders implements BaseColumns {

        public static final String BASE_PATH = "reminders";

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(BASE_PATH).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE  + "/" + CONTENT_AUTHORITY + "/" + BASE_PATH;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE+ "/" + CONTENT_AUTHORITY + "/" + BASE_PATH;


        //Name of the table
        public static final String TABLE_NAME = BASE_PATH;

        public static final String COLUMN_DATETIME = "datetime";

        public static final String COLUMN_CONTACT_NAME = "name";
        public static final String COLUMN_CONTACT_NUMBER = "number";
        public static final String COLUMN_PHOTO_URI = "photouri";

        public static final String COLUMN_STATUS = "status";
        public static final String COLUMN_NOTE = "note";


        public static Uri buildUri(long id){
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }
}
