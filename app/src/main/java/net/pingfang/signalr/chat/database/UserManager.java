package net.pingfang.signalr.chat.database;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

/**
 * Created by gongguopei87@gmail.com on 2015/10/10.
 */
public class UserManager {

    Context context;

    public UserManager(Context context) {
        this.context = context;
    }

    public boolean isExist(String uid) {
        String selection = AppContract.UserEntry.COLUMN_NAME_ENTRY_UID + " = ?";
        String[] selectionArgs = new String[]{uid};
        Cursor cursor = context.getContentResolver().query(AppContract.UserEntry.CONTENT_URI, null, selection, selectionArgs, null);
        if (cursor != null && cursor.getCount() > 0) {
            Log.d("UserManager", "return isExist == true");
            cursor.close();
            return true;
        } else {
            Log.d("UserManager", "return isExist == false");
            return false;
        }
    }

    public User queryUserByUid(String uid) {
        String selection = AppContract.UserEntry.COLUMN_NAME_ENTRY_UID + " = ?";
        String[] selectionArgs = new String[]{uid};
        Cursor cursor = context.getContentResolver().query(AppContract.UserEntry.CONTENT_URI, null, selection, selectionArgs, null);
        if(cursor != null && cursor.getCount() > 0) {
            cursor.moveToPrevious();
            if(cursor.moveToNext()) {
                String nickname = cursor.getString(cursor.getColumnIndex(AppContract.UserEntry.COLUMN_NAME_NICK_NAME));
                String portrait = cursor.getString(cursor.getColumnIndex(AppContract.UserEntry.COLUMN_NAME_PORTRAIT));
                //                int status = cursor.getInt(cursor.getColumnIndex(AppContract.UserEntry.COLUMN_NAME_STATUS));
                User user = new User(uid, nickname, portrait);
                return user;
            }
        }

        return null;
    }
}
