package net.pingfang.signalr.chat.database;

import android.provider.BaseColumns;

/**
 * Created by gongguopei87@gmail.com on 2015/9/16.
 */
public final class AppContract {

    public AppContract() {}

    public static abstract class UserEntry implements BaseColumns {
        public static final String TABLE_NAME = "t_user";
        public static final String COLUMN_NAME_ENTRY_UID = "uid";
        public static final String COLUMN_NAME_NICK_NAME = "nickname";
        public static final String COLUMN_NAME_PORTRAIT = "portrait";
    }
}
