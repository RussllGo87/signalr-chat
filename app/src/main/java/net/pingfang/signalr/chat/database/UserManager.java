package net.pingfang.signalr.chat.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * Created by gongguopei87@gmail.com on 2015/9/16.
 */
public class UserManager {

    private final AppDbHelper dbHelper;
    private SQLiteDatabase database;

    public UserManager(Context context) {
        dbHelper = new AppDbHelper(context);
    }

    public SQLiteDatabase openWritableDatabase(){
        synchronized (dbHelper) {
            if(database == null) {
                return dbHelper.getWritableDatabase();
            } else if(!database.isOpen()) {
                return dbHelper.getWritableDatabase();
            } else {
                return database;
            }
        }
    }

    public void insert(String uid, String nickname, String portrait) {
        insert(uid,nickname,portrait,0);
    }

    public void insert(String uid, String nickname, String portrait, int status) {
        database = openWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(AppContract.UserEntry.COLUMN_NAME_ENTRY_UID,uid);
        values.put(AppContract.UserEntry.COLUMN_NAME_NICK_NAME,nickname);
        values.put(AppContract.UserEntry.COLUMN_NAME_PORTRAIT,portrait);
        values.put(AppContract.UserEntry.COLUMN_NAME_STATUS,status);

        database.insert(AppContract.UserEntry.TABLE_NAME,null,values);
    }

    public Cursor query(String uid) {

        database = openWritableDatabase();

        String[] projection = {
                AppContract.UserEntry._ID,
                AppContract.UserEntry.COLUMN_NAME_ENTRY_UID,
                AppContract.UserEntry.COLUMN_NAME_NICK_NAME,
                AppContract.UserEntry.COLUMN_NAME_PORTRAIT
        };

        Cursor c = database.query(
                AppContract.UserEntry.TABLE_NAME,
                projection,
                AppContract.UserEntry.COLUMN_NAME_ENTRY_UID + " = ?" ,
                new String[]{uid},
                null,
                null,
                null
        );

        return c;
    }

    public int update(String uid, String nickname, String portrait) {
        database = openWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(AppContract.UserEntry.COLUMN_NAME_NICK_NAME,nickname);
        values.put(AppContract.UserEntry.COLUMN_NAME_PORTRAIT,portrait);

        String selection = AppContract.UserEntry.COLUMN_NAME_ENTRY_UID + " = ?";
        String[] selectionArgs = new String[]{uid};

        return database.update(AppContract.UserEntry.TABLE_NAME,values,selection,selectionArgs);
    }

    public int updateStatus(String uid, int status) {
        database = openWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(AppContract.UserEntry.COLUMN_NAME_STATUS,status);

        String selection = AppContract.UserEntry.COLUMN_NAME_ENTRY_UID + " = ?";
        String[] selectionArgs = new String[]{uid};

        return database.update(AppContract.UserEntry.TABLE_NAME,values,selection,selectionArgs);
    }

    public boolean isExist(String uid) {
        Cursor cursor = query(uid);
        if(cursor != null && cursor.getCount() > 0) {
            return true;
        }

        return false;
    }

    public void addRecord(String uid, String nickname, String portrait) {
        if(isExist(uid)) {
            update(uid,nickname,portrait);
        } else {
            insert(uid,nickname,portrait);
        }
    }

}
