package net.pingfang.signalr.chat.database;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by gongguopei87@gmail.com on 2015/9/16.
 */
public final class AppContract {

    public AppContract() {}


    public static final String AUTHORITY = "net.pingfang.signalr.chat.provider";

    public static abstract class UserEntry implements BaseColumns {

        // 访问Uri
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/user");

        // 内容类型
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/net.pingfang.signalr.chat.user";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/net.pingfang.signalr.chat.user";

        // 默认排序常量
        public static final String DEFAULT_SORT_ORDER = "uid DESC";// 按姓名排序

        public static final String TABLE_NAME = "t_user";
        public static final String COLUMN_NAME_ENTRY_UID = "uid";
        public static final String COLUMN_NAME_NICK_NAME = "nickname";
        public static final String COLUMN_NAME_PORTRAIT = "portrait";
    }
}
