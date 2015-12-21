package net.pingfang.signalr.chat.database;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

/**
 * Created by gongguopei87@gmail.com on 2015/12/21.
 */
public class DbUtils {
    public static int count(Context context, Uri uri,String selection,String[] selectionArgs) {
        Cursor cursor = context.getContentResolver().query(uri,new String[] {"count(*) AS count"},
                selection, selectionArgs, null);
        if(cursor == null) {
            return 0;
        }
        if (cursor.getCount() == 0) {
            cursor.close();
            return 0;
        } else {
            cursor.moveToFirst();
            int result = cursor.getInt(0);
            cursor.close();
            return result;
        }
    }
}
