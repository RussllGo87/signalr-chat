package net.pingfang.signalr.chat.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

/**
 * Created by gongguopei87@gmail.com on 2015/9/16.
 */
public class UserManager {

    private SQLiteDatabase database;

    private UserManager(Context context) {
        AppDbHelper dbHelper = new AppDbHelper(context);
        if(database == null) {
            synchronized (dbHelper) {
                database = dbHelper.getWritableDatabase();
            }
        }
    }

    public void insert(String uid, String nickname, String portrait) {

    }

}
