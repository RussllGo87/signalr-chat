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
    private static final String INTEGER_TYPE = " INTEGER";
    private static final String TEXT_TYPE = " TEXT";
    private static final String DATETIME_TYPE = " DATETIME DEFAULT CURRENT_TIMESTAMP";
    private static final String UNIQUE = " UNIQUE";
    private static final String COMMA_SEP = ",";


    private static final String SQL_CREATE_ENTRY_USER =
            "CREATE TABLE " + AppContract.UserEntry.TABLE_NAME + " (" +
            AppContract.UserEntry._ID + INTEGER_TYPE + PRIMARY_KEY + COMMA_SEP +
            AppContract.UserEntry.COLUMN_NAME_ENTRY_UID + TEXT_TYPE + NOT_NULL + UNIQUE + COMMA_SEP +
            AppContract.UserEntry.COLUMN_NAME_NICK_NAME + TEXT_TYPE + NOT_NULL + COMMA_SEP +
            AppContract.UserEntry.COLUMN_NAME_PORTRAIT + TEXT_TYPE + COMMA_SEP +
                    AppContract.UserEntry.COLUMN_NAME_REMARK + TEXT_TYPE + COMMA_SEP +
                    AppContract.UserEntry.COLUMN_NAME_STATUS + INTEGER_TYPE + COMMA_SEP +
                    AppContract.UserEntry.COLUMN_NAME_EXP + INTEGER_TYPE +
            " )";
    private static final String SQL_DELETE_ENTRY_USER =
            "DROP TABLE IF EXISTS " + AppContract.UserEntry.TABLE_NAME;

    private static final String SQL_CREATE_ENTRY_MESSAGE =
            "CREATE TABLE " + AppContract.ChatMessageEntry.TABLE_NAME + " (" +
            AppContract.ChatMessageEntry._ID + INTEGER_TYPE + PRIMARY_KEY + COMMA_SEP +
            AppContract.ChatMessageEntry.COLUMN_NAME_ENTRY_M_FROM + TEXT_TYPE + NOT_NULL + COMMA_SEP +
            AppContract.ChatMessageEntry.COLUMN_NAME_ENTRY_M_TO + TEXT_TYPE + NOT_NULL + COMMA_SEP +
            AppContract.ChatMessageEntry.COLUMN_NAME_M_OWNER + TEXT_TYPE + NOT_NULL + COMMA_SEP +
            AppContract.ChatMessageEntry.COLUMN_NAME_M_TYPE + INTEGER_TYPE + NOT_NULL + COMMA_SEP +
            AppContract.ChatMessageEntry.COLUMN_NAME_M_CONTENT_TYPE + TEXT_TYPE + NOT_NULL +COMMA_SEP +
            AppContract.ChatMessageEntry.COLUMN_NAME_M_CONTENT + TEXT_TYPE + NOT_NULL + COMMA_SEP +
            AppContract.ChatMessageEntry.COLUMN_NAME_M_DATETIME + TEXT_TYPE + NOT_NULL + COMMA_SEP +
            AppContract.ChatMessageEntry.COLUMN_NAME_M_STATUS + TEXT_TYPE + NOT_NULL +
            " )";

    private static final String SQL_DELETE_ENTRY_MESSAGE =
            "DROP TABLE IF EXISTS " + AppContract.ChatMessageEntry.TABLE_NAME;

    private static final String SQL_CREATE_ENTRY_RECENT =
            "CREATE TABLE " + AppContract.RecentContactEntry.TABLE_NAME + " (" +
            AppContract.RecentContactEntry._ID + INTEGER_TYPE + PRIMARY_KEY + COMMA_SEP +
            AppContract.RecentContactEntry.COLUMN_NAME_BUDDY + TEXT_TYPE + NOT_NULL + COMMA_SEP +
            AppContract.RecentContactEntry.COLUMN_NAME_OWNER + TEXT_TYPE + NOT_NULL + COMMA_SEP +
            AppContract.RecentContactEntry.COLUMN_NAME_CONTENT + TEXT_TYPE + NOT_NULL + COMMA_SEP +
            AppContract.RecentContactEntry.COLUMN_NAME_UPDATE_TIME + DATETIME_TYPE + NOT_NULL + COMMA_SEP +
            AppContract.RecentContactEntry.COLUMN_NAME_COUNT + INTEGER_TYPE + NOT_NULL +
            " )";

    private static final String SQL_DELETE_ENTRY_RECENT =
            "DROP TABLE IF EXISTS " + AppContract.RecentContactEntry.TABLE_NAME;

    private static final String SQL_CREATE_VIEW_RECENT =
            "CREATE VIEW IF NOT EXISTS " + AppContract.RecentContactView.VIEW_NAME + " AS " +
            "SELECT " +
            "recent." + AppContract.RecentContactEntry._ID + " AS " + AppContract.RecentContactView._ID + COMMA_SEP +
            "user." + AppContract.UserEntry.COLUMN_NAME_ENTRY_UID + " AS " + AppContract.RecentContactView.COLUMN_NAME_UID + COMMA_SEP +
            "user." + AppContract.UserEntry.COLUMN_NAME_NICK_NAME + " AS " + AppContract.RecentContactView.COLUMN_NAME_NICKNAME + COMMA_SEP +
            "user." + AppContract.UserEntry.COLUMN_NAME_PORTRAIT + " AS " + AppContract.RecentContactView.COLUMN_NAME_PORTRAIT + COMMA_SEP +
            "user." + AppContract.UserEntry.COLUMN_NAME_STATUS + " AS " + AppContract.RecentContactView.COLUMN_NAME_STATUS + COMMA_SEP +
            "recent." + AppContract.RecentContactEntry.COLUMN_NAME_CONTENT + " AS " + AppContract.RecentContactView.COLUMN_NAME_CONTENT + COMMA_SEP +
            "recent." + AppContract.RecentContactEntry.COLUMN_NAME_UPDATE_TIME + " AS " + AppContract.RecentContactView.COLUMN_NAME_UPDATE_TIME + COMMA_SEP +
            "recent." + AppContract.RecentContactEntry.COLUMN_NAME_COUNT + " AS " + AppContract.RecentContactView.COLUMN_NAME_COUNT + COMMA_SEP +
            "recent." + AppContract.RecentContactEntry.COLUMN_NAME_OWNER + " AS " + AppContract.RecentContactView.COLUMN_NAME_OWNER + " " +
            "FROM " +
            AppContract.UserEntry.TABLE_NAME + " AS user, " +
            AppContract.RecentContactEntry.TABLE_NAME + " AS recent " +
            "WHERE " +
            "user." + AppContract.UserEntry.COLUMN_NAME_ENTRY_UID + " = " +
            "recent." + AppContract.RecentContactEntry.COLUMN_NAME_BUDDY;

    private static final String SQL_DELETE_VIEW_RECENT =
            "DROP VIEW IF EXISTS " + AppContract.RecentContactView.VIEW_NAME;


    private static final String SQL_CREATE_ENTRY_SHIELD =
            "CREATE TABLE " + AppContract.ShieldEntry.TABLE_NAME + " (" +
            AppContract.ShieldEntry._ID + INTEGER_TYPE + PRIMARY_KEY + COMMA_SEP +
            AppContract.ShieldEntry.COLUMN_NAME_SHIELD + TEXT_TYPE + NOT_NULL + COMMA_SEP +
            AppContract.ShieldEntry.COLUMN_NAME_OWNER + TEXT_TYPE + NOT_NULL +
            " )";

    private static final String SQL_DELETE_ENTRY_SHIELD =
            "DROP TABLE IF EXISTS " + AppContract.ShieldEntry.TABLE_NAME;


    private static final String SQL_CREATE_VIEW_LIST_SHIELD =
            "CREATE VIEW IF NOT EXISTS " + AppContract.ShieldListView.VIEW_NAME + " AS " +
            "SELECT " +
            "shield." + AppContract.ShieldEntry._ID + " AS " + AppContract.ShieldListView._ID + COMMA_SEP +
            "user." + AppContract.UserEntry.COLUMN_NAME_ENTRY_UID + " AS " + AppContract.ShieldListView.COLUMN_NAME_UID + COMMA_SEP +
            "user." + AppContract.UserEntry.COLUMN_NAME_NICK_NAME + " AS " + AppContract.ShieldListView.COLUMN_NAME_NICKNAME + COMMA_SEP +
            "user." + AppContract.UserEntry.COLUMN_NAME_PORTRAIT + " AS " + AppContract.ShieldListView.COLUMN_NAME_PORTRAIT + COMMA_SEP +
            "user." + AppContract.UserEntry.COLUMN_NAME_STATUS + " AS " + AppContract.ShieldListView.COLUMN_NAME_STATUS + COMMA_SEP +
            "shield." + AppContract.ShieldEntry.COLUMN_NAME_OWNER + " AS " + AppContract.ShieldListView.COLUMN_NAME_OWNER + " " +
            "FROM " +
            AppContract.UserEntry.TABLE_NAME + " AS user, " +
            AppContract.ShieldEntry.TABLE_NAME + " AS shield " +
            "WHERE " +
            "user." + AppContract.UserEntry.COLUMN_NAME_ENTRY_UID + " = " +
            "shield." + AppContract.ShieldEntry.COLUMN_NAME_SHIELD;

    private static final String SQL_DELETE_VIEW_LIST_SHIELD =
            "DROP VIEW IF EXISTS " + AppContract.ShieldListView.VIEW_NAME;


    public AppDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRY_USER);
        db.execSQL(SQL_CREATE_ENTRY_MESSAGE);
        db.execSQL(SQL_CREATE_ENTRY_RECENT);
        db.execSQL(SQL_CREATE_ENTRY_SHIELD);

        db.execSQL(SQL_CREATE_VIEW_RECENT);
        db.execSQL(SQL_CREATE_VIEW_LIST_SHIELD);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        db.execSQL(SQL_DELETE_VIEW_LIST_SHIELD);
        db.execSQL(SQL_DELETE_VIEW_RECENT);

        db.execSQL(SQL_DELETE_ENTRY_SHIELD);
        db.execSQL(SQL_DELETE_ENTRY_RECENT);
        db.execSQL(SQL_DELETE_ENTRY_MESSAGE);
        db.execSQL(SQL_DELETE_ENTRY_USER);
        onCreate(db);
    }
}
