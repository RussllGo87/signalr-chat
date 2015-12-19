package net.pingfang.signalr.chat.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

import net.pingfang.signalr.chat.R;
import net.pingfang.signalr.chat.database.AppContract;
import net.pingfang.signalr.chat.database.AppDbHelper;

import java.util.HashMap;

/**
 * Created by gongguopei87@gmail.com on 2015/9/28.
 */
public class ChatAppProvider extends ContentProvider {

    // Uri工具类
    private static final UriMatcher sUriMatcher;
    // 查询、更新条件
    private static final int USER = 1;
    private static final int USER_COLUMN_ID = 2;
    private static final int MESSAGE = 3;
    private static final int MESSAGE_COLUMN_ID = 4;
    private static final int RECENT = 5;
    private static final int RECENT_COLUMN_ID = 6;
    private static final int RECENT_VIEW = 7;
    private static final int RECENT_VIEW_COLUMN_ID = 8;
    private static final int SHIELD = 9;
    private static final int SHIELD_COLUMN_ID = 10;
    private static final int SHIELD_VIEW = 11;
    private static final int SHIELD_VIEW_COLUMN_ID = 12;
    private static final int ADVERTISEMENT = 13;
    private static final int ADVERTISEMENT_COLUMN_ID = 14;
    private static final int USER_STATUS = 15;
    private static final int USER_STATUS_COLUMN_ID = 16;
    private static final int USER_STATUS_VIEW = 17;
    private static final int USER_STATUS_VIEW_COLUMN_ID = 18 ;


    // 查询列集合
    private static HashMap<String, String> userProjectionMap;
    private static HashMap<String, String> userStatusProjectionMap;
    private static HashMap<String, String> vUserStatusProjectionMap;
    private static HashMap<String, String> adProjectionMap;
    private static HashMap<String, String> messageProjectionMap;
    private static HashMap<String, String> recentProjectionMap;
    private static HashMap<String, String> vRecentProjectionMap;
    private static HashMap<String, String> shieldProjectionMap;
    private static HashMap<String, String> vShieldProjectionMap;

    static {
        // Uri匹配工具类
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(AppContract.AUTHORITY, "entry_user", USER);
        sUriMatcher.addURI(AppContract.AUTHORITY, "entry_user/#", USER_COLUMN_ID);
        sUriMatcher.addURI(AppContract.AUTHORITY, "message", MESSAGE);
        sUriMatcher.addURI(AppContract.AUTHORITY, "message/#", MESSAGE_COLUMN_ID);
        sUriMatcher.addURI(AppContract.AUTHORITY, "entry_recent_contract", RECENT);
        sUriMatcher.addURI(AppContract.AUTHORITY, "entry_recent_contract/#", RECENT_COLUMN_ID);
        sUriMatcher.addURI(AppContract.AUTHORITY, "view_recent_contract", RECENT_VIEW);
        sUriMatcher.addURI(AppContract.AUTHORITY, "view_recent_contract/#", RECENT_VIEW_COLUMN_ID);
        sUriMatcher.addURI(AppContract.AUTHORITY, "shield", SHIELD);
        sUriMatcher.addURI(AppContract.AUTHORITY, "shield/#", SHIELD_COLUMN_ID);
        sUriMatcher.addURI(AppContract.AUTHORITY, "v_shield", SHIELD_VIEW);
        sUriMatcher.addURI(AppContract.AUTHORITY, "v_shield/#", SHIELD_VIEW_COLUMN_ID);
        sUriMatcher.addURI(AppContract.AUTHORITY, "entry_advertisement", ADVERTISEMENT);
        sUriMatcher.addURI(AppContract.AUTHORITY, "entry_advertisement/#", ADVERTISEMENT_COLUMN_ID);
        sUriMatcher.addURI(AppContract.AUTHORITY, "entry_user_status", USER_STATUS);
        sUriMatcher.addURI(AppContract.AUTHORITY, "entry_user_status/#", USER_STATUS_COLUMN_ID);
        sUriMatcher.addURI(AppContract.AUTHORITY, "view_user_status", USER_STATUS_VIEW);
        sUriMatcher.addURI(AppContract.AUTHORITY, "view_user_status/#", USER_STATUS_VIEW_COLUMN_ID);


        // 实例化查询列集合
        userProjectionMap = new HashMap<String, String>();
        // 添加查询列
        userProjectionMap.put(AppContract.UserEntry._ID, AppContract.UserEntry._ID);
        userProjectionMap.put(AppContract.UserEntry.COLUMN_NAME_ENTRY_UID, AppContract.UserEntry.COLUMN_NAME_ENTRY_UID);
        userProjectionMap.put(AppContract.UserEntry.COLUMN_NAME_NICK_NAME, AppContract.UserEntry.COLUMN_NAME_NICK_NAME);
        userProjectionMap.put(AppContract.UserEntry.COLUMN_NAME_PORTRAIT, AppContract.UserEntry.COLUMN_NAME_PORTRAIT);
        userProjectionMap.put(AppContract.UserEntry.COLUMN_NAME_REMARK, AppContract.UserEntry.COLUMN_NAME_REMARK);
        userProjectionMap.put(AppContract.UserEntry.COLUMN_NAME_GENDER, AppContract.UserEntry.COLUMN_NAME_GENDER);
        userProjectionMap.put(AppContract.UserEntry.COLUMN_NAME_STATUS_MSG_LIST, AppContract.UserEntry.COLUMN_NAME_STATUS_MSG_LIST);
        userProjectionMap.put(AppContract.UserEntry.COLUMN_NAME_STATUS_NEARBY_LIST, AppContract.UserEntry.COLUMN_NAME_STATUS_NEARBY_LIST);
        userProjectionMap.put(AppContract.UserEntry.COLUMN_NAME_EXP, AppContract.UserEntry.COLUMN_NAME_EXP);
        userProjectionMap.put(AppContract.UserEntry.COLUMN_NAME_DISTANCE, AppContract.UserEntry.COLUMN_NAME_DISTANCE);

        userStatusProjectionMap = new HashMap<String, String>();
        userStatusProjectionMap.put(AppContract.UserStatusEntry._ID, AppContract.UserStatusEntry._ID);
        userStatusProjectionMap.put(AppContract.UserStatusEntry.COLUMN_NAME_ENTRY_UID, AppContract.UserStatusEntry.COLUMN_NAME_ENTRY_UID);
        userStatusProjectionMap.put(AppContract.UserStatusEntry.COLUMN_NAME_ENTRY_OWNER, AppContract.UserStatusEntry.COLUMN_NAME_ENTRY_OWNER);
        userStatusProjectionMap.put(AppContract.UserStatusEntry.COLUMN_NAME_STATUS_MSG, AppContract.UserStatusEntry.COLUMN_NAME_STATUS_MSG);
        userStatusProjectionMap.put(AppContract.UserStatusEntry.COLUMN_NAME_STATUS_NEARBY, AppContract.UserStatusEntry.COLUMN_NAME_STATUS_NEARBY);
        userStatusProjectionMap.put(AppContract.UserStatusEntry.COLUMN_NAME_STATUS_SHIELD, AppContract.UserStatusEntry.COLUMN_NAME_STATUS_SHIELD);
        userStatusProjectionMap.put(AppContract.UserStatusEntry.COLUMN_NAME_DISTANCE, AppContract.UserStatusEntry.COLUMN_NAME_DISTANCE);
        userStatusProjectionMap.put(AppContract.UserStatusEntry.COLUMN_NAME_STATUS_REMARK, AppContract.UserStatusEntry.COLUMN_NAME_STATUS_REMARK);

        vUserStatusProjectionMap = new HashMap<String, String>();
        vUserStatusProjectionMap.put(AppContract.UserStatusView._ID, AppContract.UserStatusView._ID);
        vUserStatusProjectionMap.put(AppContract.UserStatusView.COLUMN_NAME_UID, AppContract.UserStatusView.COLUMN_NAME_UID);
        vUserStatusProjectionMap.put(AppContract.UserStatusView.COLUMN_NAME_NICKNAME, AppContract.UserStatusView.COLUMN_NAME_NICKNAME);
        vUserStatusProjectionMap.put(AppContract.UserStatusView.COLUMN_NAME_PORTRAIT, AppContract.UserStatusView.COLUMN_NAME_PORTRAIT);
        vUserStatusProjectionMap.put(AppContract.UserStatusView.COLUMN_NAME_OWNER, AppContract.UserStatusView.COLUMN_NAME_OWNER);
        vUserStatusProjectionMap.put(AppContract.UserStatusView.COLUMN_NAME_STATUS_MSG, AppContract.UserStatusView.COLUMN_NAME_STATUS_MSG);
        vUserStatusProjectionMap.put(AppContract.UserStatusView.COLUMN_NAME_STATUS_NEARBY, AppContract.UserStatusView.COLUMN_NAME_STATUS_NEARBY);
        vUserStatusProjectionMap.put(AppContract.UserStatusView.COLUMN_NAME_STATUS_SHIELD, AppContract.UserStatusView.COLUMN_NAME_STATUS_SHIELD);
        vUserStatusProjectionMap.put(AppContract.UserStatusView.COLUMN_NAME_DISTANCE, AppContract.UserStatusView.COLUMN_NAME_DISTANCE);
        vUserStatusProjectionMap.put(AppContract.UserStatusView.COLUMN_NAME_STATUS_REMARK, AppContract.UserStatusView.COLUMN_NAME_STATUS_REMARK);

        adProjectionMap = new HashMap<String, String>();
        adProjectionMap.put(AppContract.AdvertisementEntry._ID, AppContract.AdvertisementEntry._ID);
        adProjectionMap.put(AppContract.AdvertisementEntry.COLUMN_NAME_AD_UID, AppContract.AdvertisementEntry.COLUMN_NAME_AD_UID);
        adProjectionMap.put(AppContract.AdvertisementEntry.COLUMN_NAME_AD_ADDRESS, AppContract.AdvertisementEntry.COLUMN_NAME_AD_ADDRESS);
        adProjectionMap.put(AppContract.AdvertisementEntry.COLUMN_NAME_AD_CODE, AppContract.AdvertisementEntry.COLUMN_NAME_AD_CODE);
        adProjectionMap.put(AppContract.AdvertisementEntry.COLUMN_NAME_AD_LENGTH, AppContract.AdvertisementEntry.COLUMN_NAME_AD_LENGTH);
        adProjectionMap.put(AppContract.AdvertisementEntry.COLUMN_NAME_AD_WIDTH, AppContract.AdvertisementEntry.COLUMN_NAME_AD_WIDTH);
        adProjectionMap.put(AppContract.AdvertisementEntry.COLUMN_NAME_AD_REMARK, AppContract.AdvertisementEntry.COLUMN_NAME_AD_REMARK);
        adProjectionMap.put(AppContract.AdvertisementEntry.COLUMN_NAME_AD_LAT, AppContract.AdvertisementEntry.COLUMN_NAME_AD_LAT);
        adProjectionMap.put(AppContract.AdvertisementEntry.COLUMN_NAME_AD_LNG, AppContract.AdvertisementEntry.COLUMN_NAME_AD_LNG);
        adProjectionMap.put(AppContract.AdvertisementEntry.COLUMN_NAME_AD_PATH_P1, AppContract.AdvertisementEntry.COLUMN_NAME_AD_PATH_P1);
        adProjectionMap.put(AppContract.AdvertisementEntry.COLUMN_NAME_AD_PATH_P2, AppContract.AdvertisementEntry.COLUMN_NAME_AD_PATH_P2);
        adProjectionMap.put(AppContract.AdvertisementEntry.COLUMN_NAME_AD_PATH_P3, AppContract.AdvertisementEntry.COLUMN_NAME_AD_PATH_P3);
        adProjectionMap.put(AppContract.AdvertisementEntry.COLUMN_NAME_AD_PATH_P4, AppContract.AdvertisementEntry.COLUMN_NAME_AD_PATH_P4);
        adProjectionMap.put(AppContract.AdvertisementEntry.COLUMN_NAME_AD_STATUS, AppContract.AdvertisementEntry.COLUMN_NAME_AD_STATUS);

        messageProjectionMap = new HashMap<String, String>();
        messageProjectionMap.put(AppContract.ChatMessageEntry._ID, AppContract.ChatMessageEntry._ID);
        messageProjectionMap.put(AppContract.ChatMessageEntry.COLUMN_NAME_ENTRY_M_FROM, AppContract.ChatMessageEntry.COLUMN_NAME_ENTRY_M_FROM);
        messageProjectionMap.put(AppContract.ChatMessageEntry.COLUMN_NAME_ENTRY_M_TO, AppContract.ChatMessageEntry.COLUMN_NAME_ENTRY_M_TO);
        messageProjectionMap.put(AppContract.ChatMessageEntry.COLUMN_NAME_M_OWNER, AppContract.ChatMessageEntry.COLUMN_NAME_M_OWNER);
        messageProjectionMap.put(AppContract.ChatMessageEntry.COLUMN_NAME_M_TYPE, AppContract.ChatMessageEntry.COLUMN_NAME_M_TYPE);
        messageProjectionMap.put(AppContract.ChatMessageEntry.COLUMN_NAME_M_CONTENT_TYPE, AppContract.ChatMessageEntry.COLUMN_NAME_M_CONTENT_TYPE);
        messageProjectionMap.put(AppContract.ChatMessageEntry.COLUMN_NAME_M_CONTENT, AppContract.ChatMessageEntry.COLUMN_NAME_M_CONTENT);
        messageProjectionMap.put(AppContract.ChatMessageEntry.COLUMN_NAME_M_DATETIME, AppContract.ChatMessageEntry.COLUMN_NAME_M_DATETIME);
        messageProjectionMap.put(AppContract.ChatMessageEntry.COLUMN_NAME_M_STATUS, AppContract.ChatMessageEntry.COLUMN_NAME_M_STATUS);

        recentProjectionMap = new HashMap<String, String>();
        recentProjectionMap.put(AppContract.RecentContactEntry._ID, AppContract.RecentContactEntry._ID);
        recentProjectionMap.put(AppContract.RecentContactEntry.COLUMN_NAME_BUDDY, AppContract.RecentContactEntry.COLUMN_NAME_BUDDY);
        recentProjectionMap.put(AppContract.RecentContactEntry.COLUMN_NAME_CONTENT, AppContract.RecentContactEntry.COLUMN_NAME_CONTENT);
        recentProjectionMap.put(AppContract.RecentContactEntry.COLUMN_NAME_UPDATE_TIME, AppContract.RecentContactEntry.COLUMN_NAME_UPDATE_TIME);
        recentProjectionMap.put(AppContract.RecentContactEntry.COLUMN_NAME_OWNER, AppContract.RecentContactEntry.COLUMN_NAME_OWNER);
        recentProjectionMap.put(AppContract.RecentContactEntry.COLUMN_NAME_COUNT, AppContract.RecentContactEntry.COLUMN_NAME_COUNT);

        vRecentProjectionMap = new HashMap<String, String>();
        vRecentProjectionMap.put(AppContract.RecentContactView._ID, AppContract.RecentContactView._ID);
        vRecentProjectionMap.put(AppContract.RecentContactView.COLUMN_NAME_UID, AppContract.RecentContactView.COLUMN_NAME_UID);
        vRecentProjectionMap.put(AppContract.RecentContactView.COLUMN_NAME_NICKNAME, AppContract.RecentContactView.COLUMN_NAME_NICKNAME);
        vRecentProjectionMap.put(AppContract.RecentContactView.COLUMN_NAME_PORTRAIT, AppContract.RecentContactView.COLUMN_NAME_PORTRAIT);
        vRecentProjectionMap.put(AppContract.RecentContactView.COLUMN_NAME_OWNER, AppContract.RecentContactView.COLUMN_NAME_OWNER);
        vRecentProjectionMap.put(AppContract.RecentContactView.COLUMN_NAME_STATUS_MSG, AppContract.RecentContactView.COLUMN_NAME_STATUS_MSG);
        vRecentProjectionMap.put(AppContract.RecentContactView.COLUMN_NAME_STATUS_NEARBY, AppContract.RecentContactView.COLUMN_NAME_STATUS_NEARBY);
        vRecentProjectionMap.put(AppContract.RecentContactView.COLUMN_NAME_STATUS_SHIELD, AppContract.RecentContactView.COLUMN_NAME_STATUS_SHIELD);
        vRecentProjectionMap.put(AppContract.RecentContactView.COLUMN_NAME_DISTANCE, AppContract.RecentContactView.COLUMN_NAME_DISTANCE);
        vRecentProjectionMap.put(AppContract.RecentContactView.COLUMN_NAME_STATUS_REMARK, AppContract.RecentContactView.COLUMN_NAME_STATUS_REMARK);
        vRecentProjectionMap.put(AppContract.RecentContactView.COLUMN_NAME_CONTENT, AppContract.RecentContactView.COLUMN_NAME_CONTENT);
        vRecentProjectionMap.put(AppContract.RecentContactView.COLUMN_NAME_UPDATE_TIME, AppContract.RecentContactView.COLUMN_NAME_UPDATE_TIME);
        vRecentProjectionMap.put(AppContract.RecentContactView.COLUMN_NAME_COUNT, AppContract.RecentContactView.COLUMN_NAME_COUNT);

        shieldProjectionMap = new HashMap<String, String>();
        shieldProjectionMap.put(AppContract.ShieldEntry._ID, AppContract.ShieldEntry._ID);
        shieldProjectionMap.put(AppContract.ShieldEntry.COLUMN_NAME_SHIELD, AppContract.ShieldEntry.COLUMN_NAME_SHIELD);
        shieldProjectionMap.put(AppContract.ShieldEntry.COLUMN_NAME_OWNER, AppContract.ShieldEntry.COLUMN_NAME_OWNER);

        vShieldProjectionMap = new HashMap<String, String>();
        vShieldProjectionMap.put(AppContract.ShieldListView._ID, AppContract.ShieldListView._ID);
        vShieldProjectionMap.put(AppContract.ShieldListView.COLUMN_NAME_UID, AppContract.ShieldListView.COLUMN_NAME_UID);
        vShieldProjectionMap.put(AppContract.ShieldListView.COLUMN_NAME_NICKNAME, AppContract.ShieldListView.COLUMN_NAME_NICKNAME);
        vShieldProjectionMap.put(AppContract.ShieldListView.COLUMN_NAME_PORTRAIT, AppContract.ShieldListView.COLUMN_NAME_PORTRAIT);
        vShieldProjectionMap.put(AppContract.ShieldListView.COLUMN_NAME_STATUS_MSG_LIST, AppContract.ShieldListView.COLUMN_NAME_STATUS_MSG_LIST);
        vShieldProjectionMap.put(AppContract.ShieldListView.COLUMN_NAME_STATUS_MSG_LIST, AppContract.ShieldListView.COLUMN_NAME_STATUS_MSG_LIST);
        vShieldProjectionMap.put(AppContract.ShieldListView.COLUMN_NAME_OWNER, AppContract.ShieldListView.COLUMN_NAME_OWNER);
    }

    AppDbHelper dbHelper;

    @Override
    public boolean onCreate() {
        dbHelper = new AppDbHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        String orderBy = null;
        switch (sUriMatcher.match(uri)) {
            case USER:
                qb.setTables(AppContract.UserEntry.TABLE_NAME);
                qb.setProjectionMap(userProjectionMap);
                if (TextUtils.isEmpty(sortOrder)) {
                    orderBy = AppContract.UserEntry.DEFAULT_SORT_ORDER;
                } else {
                    orderBy = sortOrder;
                }
                break;
            // 根据ID查询
            case USER_COLUMN_ID:
                qb.setTables(AppContract.UserEntry.TABLE_NAME);
                qb.setProjectionMap(userProjectionMap);
                qb.appendWhere(AppContract.UserEntry._ID + "=" + uri.getPathSegments().get(1));
                if (TextUtils.isEmpty(sortOrder)) {
                    orderBy = AppContract.UserEntry.DEFAULT_SORT_ORDER;
                } else {
                    orderBy = sortOrder;
                }
                break;
            case USER_STATUS:
                qb.setTables(AppContract.UserStatusEntry.TABLE_NAME);
                qb.setProjectionMap(userStatusProjectionMap);
                if (TextUtils.isEmpty(sortOrder)) {
                    orderBy = AppContract.UserStatusEntry.DEFAULT_SORT_ORDER;
                } else {
                    orderBy = sortOrder;
                }
                break;
            case USER_STATUS_COLUMN_ID:
                qb.setTables(AppContract.UserStatusEntry.TABLE_NAME);
                qb.setProjectionMap(userStatusProjectionMap);
                qb.appendWhere(AppContract.UserStatusEntry._ID + "=" + uri.getPathSegments().get(1));
                if (TextUtils.isEmpty(sortOrder)) {
                    orderBy = AppContract.UserStatusEntry.DEFAULT_SORT_ORDER;
                } else {
                    orderBy = sortOrder;
                }
                break;
            case USER_STATUS_VIEW:
                qb.setTables(AppContract.UserStatusView.VIEW_NAME);
                qb.setProjectionMap(vUserStatusProjectionMap);
                if (TextUtils.isEmpty(sortOrder)) {
                    orderBy = AppContract.UserStatusView.DEFAULT_SORT_ORDER;
                } else {
                    orderBy = sortOrder;
                }
                break;
            case USER_STATUS_VIEW_COLUMN_ID:
                qb.setTables(AppContract.UserStatusView.VIEW_NAME);
                qb.setProjectionMap(vUserStatusProjectionMap);
                qb.appendWhere(AppContract.UserStatusView._ID + "=" + uri.getPathSegments().get(1));
                if (TextUtils.isEmpty(sortOrder)) {
                    orderBy = AppContract.UserStatusView.DEFAULT_SORT_ORDER;
                } else {
                    orderBy = sortOrder;
                }
                break;
            case ADVERTISEMENT:
                qb.setTables(AppContract.AdvertisementEntry.TABLE_NAME);
                qb.setProjectionMap(adProjectionMap);
                if (TextUtils.isEmpty(sortOrder)) {
                    orderBy = AppContract.AdvertisementEntry.DEFAULT_SORT_ORDER;
                } else {
                    orderBy = sortOrder;
                }
                break;
            // 根据ID查询
            case ADVERTISEMENT_COLUMN_ID:
                qb.setTables(AppContract.AdvertisementEntry.TABLE_NAME);
                qb.setProjectionMap(adProjectionMap);
                qb.appendWhere(AppContract.AdvertisementEntry._ID + "=" + uri.getPathSegments().get(1));
                if (TextUtils.isEmpty(sortOrder)) {
                    orderBy = AppContract.AdvertisementEntry.DEFAULT_SORT_ORDER;
                } else {
                    orderBy = sortOrder;
                }
                break;
            case MESSAGE:
                qb.setTables(AppContract.ChatMessageEntry.TABLE_NAME);
                qb.setProjectionMap(messageProjectionMap);
                if (TextUtils.isEmpty(sortOrder)) {
                    orderBy = AppContract.ChatMessageEntry.DEFAULT_SORT_ORDER;
                } else {
                    orderBy = sortOrder;
                }
                break;
            case MESSAGE_COLUMN_ID:
                qb.setTables(AppContract.ChatMessageEntry.TABLE_NAME);
                qb.setProjectionMap(messageProjectionMap);
                qb.appendWhere(AppContract.ChatMessageEntry._ID + "=" + uri.getPathSegments().get(1));
                break;
            case RECENT:
                qb.setTables(AppContract.RecentContactEntry.TABLE_NAME);
                qb.setProjectionMap(recentProjectionMap);
                if (TextUtils.isEmpty(sortOrder)) {
                    orderBy = AppContract.RecentContactEntry.DEFAULT_SORT_ORDER;
                } else {
                    orderBy = sortOrder;
                }
                break;
            case RECENT_COLUMN_ID:
                qb.setTables(AppContract.RecentContactEntry.TABLE_NAME);
                qb.setProjectionMap(recentProjectionMap);
                qb.appendWhere(AppContract.RecentContactEntry._ID + "=" + uri.getPathSegments().get(1));
                break;
            case RECENT_VIEW:
                qb.setTables(AppContract.RecentContactView.VIEW_NAME);
                qb.setProjectionMap(vRecentProjectionMap);
                if (TextUtils.isEmpty(sortOrder)) {
                    orderBy = AppContract.RecentContactView.DEFAULT_SORT_ORDER;
                } else {
                    orderBy = sortOrder;
                }
                break;
            case RECENT_VIEW_COLUMN_ID:
                qb.setTables(AppContract.RecentContactView.VIEW_NAME);
                qb.setProjectionMap(vRecentProjectionMap);
                qb.appendWhere(AppContract.RecentContactView._ID + "=" + uri.getPathSegments().get(1));
                break;
            case SHIELD:
                qb.setTables(AppContract.ShieldEntry.TABLE_NAME);
                qb.setProjectionMap(shieldProjectionMap);
                if (TextUtils.isEmpty(sortOrder)) {
                    orderBy = AppContract.ShieldEntry.DEFAULT_SORT_ORDER;
                } else {
                    orderBy = sortOrder;
                }
                break;
            case SHIELD_COLUMN_ID:
                qb.setTables(AppContract.ShieldEntry.TABLE_NAME);
                qb.setProjectionMap(vShieldProjectionMap);
                qb.appendWhere(AppContract.ShieldEntry._ID + "=" + uri.getPathSegments().get(1));
                break;
            case SHIELD_VIEW:
                qb.setTables(AppContract.ShieldListView.VIEW_NAME);
                qb.setProjectionMap(vShieldProjectionMap);
                if (TextUtils.isEmpty(sortOrder)) {
                    orderBy = AppContract.ShieldListView.DEFAULT_SORT_ORDER;
                } else {
                    orderBy = sortOrder;
                }
                break;
            case SHIELD_VIEW_COLUMN_ID:
                qb.setTables(AppContract.ShieldListView.VIEW_NAME);
                qb.setProjectionMap(vShieldProjectionMap);
                qb.appendWhere(AppContract.ShieldListView._ID + "=" + uri.getPathSegments().get(1));
                break;
            default:
                throw new IllegalArgumentException(getContext().getString(R.string.illegal_uri_exception)  + " = " + uri);
        }

        // 获得数据库实例
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        // 返回游标集合
        Cursor c = qb.query(db, projection, selection, selectionArgs, null, null, orderBy);
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        // 获得数据库实例
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        long rowId = -1;
        switch (sUriMatcher.match(uri)) {
            case USER:
                rowId = db.insert(AppContract.UserEntry.TABLE_NAME, null, values);
                if(rowId > 0) {
                    Uri userUri = ContentUris.withAppendedId(AppContract.UserEntry.CONTENT_URI,rowId);
                    getContext().getContentResolver().notifyChange(userUri, null);
                    return userUri;
                }
                break;
            case USER_STATUS:
                rowId = db.insert(AppContract.UserStatusEntry.TABLE_NAME, null, values);
                if (rowId > 0) {
                    Uri userStatusUri = ContentUris.withAppendedId(AppContract.UserStatusEntry.CONTENT_URI, rowId);
                    getContext().getContentResolver().notifyChange(userStatusUri, null);
                    return userStatusUri;
                }
                break;
            case ADVERTISEMENT:
                rowId = db.insert(AppContract.AdvertisementEntry.TABLE_NAME, null, values);
                if (rowId > 0) {
                    Uri adUri = ContentUris.withAppendedId(AppContract.AdvertisementEntry.CONTENT_URI, rowId);
                    getContext().getContentResolver().notifyChange(adUri, null);
                    return adUri;
                }
                break;
            case MESSAGE:
                rowId = db.insert(AppContract.ChatMessageEntry.TABLE_NAME, null, values);
                if(rowId > 0) {
                    Uri messageUri = ContentUris.withAppendedId(AppContract.ChatMessageEntry.CONTENT_URI,rowId);
                    getContext().getContentResolver().notifyChange(messageUri, null);
                    return messageUri;
                }
                break;
            case RECENT:
                rowId = db.insert(AppContract.RecentContactEntry.TABLE_NAME, null, values);
                if(rowId > 0) {
                    Uri recentUri = ContentUris.withAppendedId(AppContract.RecentContactEntry.CONTENT_URI,rowId);
                    getContext().getContentResolver().notifyChange(recentUri, null);
                    return recentUri;
                }
            case SHIELD:
                rowId = db.insert(AppContract.ShieldEntry.TABLE_NAME, null, values);
                if(rowId > 0) {
                    Uri shieldUri = ContentUris.withAppendedId(AppContract.ShieldEntry.CONTENT_URI,rowId);
                    getContext().getContentResolver().notifyChange(shieldUri, null);
                    return shieldUri;
                }
                break;
        }
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // 获得数据库实例
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        // 获得数据库实例
        int count;
        switch (sUriMatcher.match(uri)) {
            // 根据指定条件删除
            case USER:
                count = db.delete(AppContract.UserEntry.TABLE_NAME, selection, selectionArgs);
                break;
            // 根据指定条件和ID删除
            case USER_COLUMN_ID:
                String userColumnId = uri.getPathSegments().get(1);
                count = db.delete(AppContract.UserEntry.TABLE_NAME, AppContract.UserEntry._ID + "=" + userColumnId
                        + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : ""), selectionArgs);
                break;
            case USER_STATUS:
                count = db.delete(AppContract.UserStatusEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case USER_STATUS_COLUMN_ID:
                String userStatusColumnId = uri.getPathSegments().get(1);
                count = db.delete(AppContract.UserStatusEntry.TABLE_NAME, AppContract.UserStatusEntry._ID + "=" + userStatusColumnId
                        + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : ""), selectionArgs);
                break;
            case ADVERTISEMENT:
                count = db.delete(AppContract.AdvertisementEntry.TABLE_NAME, selection, selectionArgs);
                break;
            // 根据指定条件和ID删除
            case ADVERTISEMENT_COLUMN_ID:
                String adColumnId = uri.getPathSegments().get(1);
                count = db.delete(AppContract.AdvertisementEntry.TABLE_NAME, AppContract.AdvertisementEntry._ID + "=" + adColumnId
                        + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : ""), selectionArgs);
                break;
            case MESSAGE:
                count = db.delete(AppContract.ChatMessageEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case MESSAGE_COLUMN_ID:
                String messageColumnId = uri.getPathSegments().get(1);
                count = db.delete(AppContract.ChatMessageEntry.TABLE_NAME, AppContract.ChatMessageEntry._ID + "=" + messageColumnId
                        + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : ""), selectionArgs);
                break;
            case RECENT:
                count = db.delete(AppContract.RecentContactEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case RECENT_COLUMN_ID:
                String recentColumnId = uri.getPathSegments().get(1);
                count = db.delete(AppContract.RecentContactEntry.TABLE_NAME, AppContract.RecentContactEntry._ID + "=" + recentColumnId
                        + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : ""), selectionArgs);
                break;
            case SHIELD:
                count = db.delete(AppContract.ShieldEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case SHIELD_COLUMN_ID:
                String shieldColumnId = uri.getPathSegments().get(1);
                count = db.delete(AppContract.ShieldEntry.TABLE_NAME, AppContract.ShieldEntry._ID + "=" + shieldColumnId
                        + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : ""), selectionArgs);
                break;
            default:
                throw new IllegalArgumentException(getContext().getString(R.string.illegal_uri_exception)  + " = " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // 获得数据库实例
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int count;
        switch (sUriMatcher.match(uri)) {
            // 根据指定条件更新
            case USER:
                count = db.update(AppContract.UserEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            // 根据指定条件和ID更新
            case USER_COLUMN_ID:
                String userColumnId = uri.getPathSegments().get(1);
                count = db.update(AppContract.UserEntry.TABLE_NAME, values, AppContract.UserEntry._ID + "=" + userColumnId
                        + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : ""), selectionArgs);
                break;
            // 根据指定条件更新
            case USER_STATUS:
                count = db.update(AppContract.UserStatusEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            // 根据指定条件和ID更新
            case USER_STATUS_COLUMN_ID:
                String userStatusColumnId = uri.getPathSegments().get(1);
                count = db.update(AppContract.UserStatusEntry.TABLE_NAME, values, AppContract.UserStatusEntry._ID + "=" + userStatusColumnId
                        + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : ""), selectionArgs);
                break;
            // 根据指定条件更新
            case ADVERTISEMENT:
                count = db.update(AppContract.AdvertisementEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            // 根据指定条件和ID更新
            case ADVERTISEMENT_COLUMN_ID:
                String adColumnId = uri.getPathSegments().get(1);
                count = db.update(AppContract.AdvertisementEntry.TABLE_NAME, values, AppContract.AdvertisementEntry._ID + "=" + adColumnId
                        + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : ""), selectionArgs);
                break;
            case MESSAGE:
                count = db.update(AppContract.ChatMessageEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            case MESSAGE_COLUMN_ID:
                String messageColumnId = uri.getPathSegments().get(1);
                count = db.update(AppContract.ChatMessageEntry.TABLE_NAME, values, AppContract.ChatMessageEntry._ID + "=" + messageColumnId
                        + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : ""), selectionArgs);
                break;
            case RECENT:
                count = db.update(AppContract.RecentContactEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            case RECENT_COLUMN_ID:
                String recentColumnId = uri.getPathSegments().get(1);
                count = db.update(AppContract.RecentContactEntry.TABLE_NAME, values, AppContract.RecentContactEntry._ID + "=" + recentColumnId
                        + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : ""), selectionArgs);
                break;
            case SHIELD:
                count = db.update(AppContract.ShieldEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            case SHIELD_COLUMN_ID:
                String shieldColumnId = uri.getPathSegments().get(1);
                count = db.update(AppContract.ShieldEntry.TABLE_NAME, values, AppContract.ShieldEntry._ID + "=" + shieldColumnId
                        + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : ""), selectionArgs);
                break;
            default:
                throw new IllegalArgumentException(getContext().getString(R.string.illegal_uri_exception)  + " = " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }
}
