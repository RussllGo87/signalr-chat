package net.pingfang.signalr.chat.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by gongguopei87@gmail.com on 2015/9/16.
 */
public class AppDbHelper extends SQLiteOpenHelper {

    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "HaleApp.db";

    private static final String PRIMARY_KEY = " PRIMARY KEY";
    private static final String NOT_NULL = " NOT NULL";
    private static final String INTERER_TYPE = " INTEGER";
    private static final String TEXT_TYPE = " TEXT";
    private static final String UNIQUE = " UNIQUE";
    private static final String COMMA_SEP = ",";


    private static final String SQL_CREATE_ENTRY_USER =
            "CREATE TABLE " + AppContract.UserEntry.TABLE_NAME + " (" +
            AppContract.UserEntry._ID + INTERER_TYPE + PRIMARY_KEY + COMMA_SEP +
            AppContract.UserEntry.COLUMN_NAME_ENTRY_UID + TEXT_TYPE + NOT_NULL + UNIQUE + COMMA_SEP +
            AppContract.UserEntry.COLUMN_NAME_NICK_NAME + TEXT_TYPE + NOT_NULL + COMMA_SEP +
            AppContract.UserEntry.COLUMN_NAME_PORTRAIT + TEXT_TYPE +
            " )";
    private static final String SQL_DELETE_ENTRY_USER =
            "DROP TABLE IF EXISTS " + AppContract.UserEntry.TABLE_NAME;

    public AppDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRY_USER);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_ENTRY_USER);
        onCreate(db);
    }
}
