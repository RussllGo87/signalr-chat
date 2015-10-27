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
        public static final String DEFAULT_SORT_ORDER = "uid DESC";

        public static final String TABLE_NAME = "t_user";
        public static final String COLUMN_NAME_ENTRY_UID = "uid";
        public static final String COLUMN_NAME_NICK_NAME = "nickname";
        public static final String COLUMN_NAME_PORTRAIT = "portrait";
        public static final String COLUMN_NAME_STATUS = "status";
    }

    public static abstract class ChatMessageEntry implements BaseColumns {

        // 访问Uri
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/message");

        // 内容类型
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/net.pingfang.signalr.chat.message";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/net.pingfang.signalr.chat.message";

        // 默认排序常量
        public static final String DEFAULT_SORT_ORDER = "m_datetime ASC";

        public static final String TABLE_NAME = "t_message";
        public static final String COLUMN_NAME_ENTRY_M_FROM = "m_from";
        public static final String COLUMN_NAME_ENTRY_M_TO = "m_to";
        public static final String COLUMN_NAME_M_OWNER = "m_own";
        public static final String COLUMN_NAME_M_TYPE = "m_type";
        public static final String COLUMN_NAME_M_CONTENT_TYPE = "m_content_type";
        public static final String COLUMN_NAME_M_CONTENT = "m_content";
        public static final String COLUMN_NAME_M_DATETIME = "m_datetime";
        public static final String COLUMN_NAME_M_STATUS = "m_status";
    }

    public static abstract class RecentContactEntry implements BaseColumns {
        // 访问Uri
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/recent");

        // 内容类型
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/net.pingfang.signalr.chat.recent";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/net.pingfang.signalr.chat.recent";

        // 默认排序常量
        public static final String DEFAULT_SORT_ORDER = "update_time DESC";

        public static final String TABLE_NAME = "t_recent_contact";
        public static final String COLUMN_NAME_BUDDY = "buddy";
        public static final String COLUMN_NAME_OWNER = "owner";
        public static final String COLUMN_NAME_CONTENT = "content";
        public static final String COLUMN_NAME_UPDATE_TIME = "update_time";
        public static final String COLUMN_NAME_COUNT = "count";
    }

    public static abstract class RecentContactView implements BaseColumns{
        // 访问Uri
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/v_recent");

        // 内容类型
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/net.pingfang.signalr.chat.v_recent";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/net.pingfang.signalr.chat.v_recent";

        // 默认排序常量
        public static final String DEFAULT_SORT_ORDER = "update_time DESC";

        public static final String VIEW_NAME = "v_recent_contact";
        public static final String COLUMN_NAME_UID = "uid";
        public static final String COLUMN_NAME_NICKNAME ="nickname";
        public static final String COLUMN_NAME_PORTRAIT = "portrait";
        public static final String COLUMN_NAME_STATUS = "status";
        public static final String COLUMN_NAME_OWNER = "owner";
        public static final String COLUMN_NAME_CONTENT = "content";
        public static final String COLUMN_NAME_UPDATE_TIME = "update_time";
        public static final String COLUMN_NAME_COUNT = "count";
    }

}
