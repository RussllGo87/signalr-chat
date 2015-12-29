package net.pingfang.signalr.chat.database;

import android.content.Context;
import android.database.Cursor;

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
        if(cursor == null) {
            return false;
        }

        if(!(cursor.getCount() > 0)) {
            cursor.close();
            return false;
        }

        cursor.close();
        return true;
    }

    public User queryUserByUid(String uid) {
        String selection = AppContract.UserEntry.COLUMN_NAME_ENTRY_UID + " = ?";
        String[] selectionArgs = new String[]{uid};
        Cursor cursor = context.getContentResolver().query(AppContract.UserEntry.CONTENT_URI, null, selection, selectionArgs, null);

        if(cursor == null) {
            return null;
        }

        if(!(cursor.getCount() > 0)) {
            cursor.close();
            return null;
        }

        cursor.moveToPrevious();
        if(cursor.moveToNext()) {
            String nickname = cursor.getString(cursor.getColumnIndex(AppContract.UserEntry.COLUMN_NAME_NICK_NAME));
            String portrait = cursor.getString(cursor.getColumnIndex(AppContract.UserEntry.COLUMN_NAME_PORTRAIT));
            return new User(uid, nickname, portrait);
        } else {
            return null;
        }
    }
}
