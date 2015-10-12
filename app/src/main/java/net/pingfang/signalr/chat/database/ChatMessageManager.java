package net.pingfang.signalr.chat.database;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

/**
 * Created by gongguopei87@gmail.com on 2015/10/12.
 */
public class ChatMessageManager {
    Context context;

    public ChatMessageManager(Context context) {
        this.context = context;
    }

    public Uri insert(String from, String to, int messageType, String contentType,
                      String content, String datetime, int status) {
        ContentResolver contentResolver = context.getContentResolver();

        ContentValues values = new ContentValues();

        values.put(AppContract.ChatMessageEntry.COLUMN_NAME_ENTRY_M_FROM, from);
        values.put(AppContract.ChatMessageEntry.COLUMN_NAME_NICK_M_TO, to);
        values.put(AppContract.ChatMessageEntry.COLUMN_NAME_M_TYPE, messageType);
        values.put(AppContract.ChatMessageEntry.COLUMN_NAME_M_CONTENT_TYPE, contentType);
        values.put(AppContract.ChatMessageEntry.COLUMN_NAME_M_CONTENT, content);
        values.put(AppContract.ChatMessageEntry.COLUMN_NAME_M_DATETIME, datetime);
        values.put(AppContract.ChatMessageEntry.COLUMN_NAME_M_STATUS, status);

        Uri uri = contentResolver.insert(AppContract.ChatMessageEntry.CONTENT_URI, values);
        return uri;
    }

    public Cursor queryByColumn(String columnName, String value) {
        String selection = columnName + " = ?";
        String[] selectionArgs = new String[]{value};

        ContentResolver contentResolver = context.getContentResolver();

        Cursor cursor = context.getContentResolver().query(AppContract.UserEntry.CONTENT_URI, null, selection, selectionArgs, null);

        return cursor;
    }

    public int updateStatus(String uid, int status) {

        ContentValues values = new ContentValues();
        values.put(AppContract.UserEntry.COLUMN_NAME_STATUS,status);

        String selection = AppContract.UserEntry.COLUMN_NAME_ENTRY_UID + " = ?";
        String[] selectionArgs = new String[]{uid};

        ContentResolver contentResolver = context.getContentResolver();

        int count = contentResolver.update(AppContract.UserEntry.CONTENT_URI, values, selection, selectionArgs);

        return count;
    }

    public int updateStatus(Uri uri, int status) {

        ContentValues values = new ContentValues();
        values.put(AppContract.UserEntry.COLUMN_NAME_STATUS,status);


        ContentResolver contentResolver = context.getContentResolver();

        int count = contentResolver.update(AppContract.UserEntry.CONTENT_URI, values, null, null);

        return count;
    }
}
