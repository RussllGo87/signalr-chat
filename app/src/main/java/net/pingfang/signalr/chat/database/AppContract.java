package net.pingfang.signalr.chat.database;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by gongguopei87@gmail.com on 2015/9/16.
 */
public final class AppContract {

    public static final String AUTHORITY = "net.pingfang.signalr.chat.provider";


    public AppContract() {
    }

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
        public static final String COLUMN_NAME_REMARK = "remark";
        public static final String COLUMN_NAME_GENDER = "gender";
        public static final String COLUMN_NAME_STATUS_MSG_LIST = "status_msg_list";
        public static final String COLUMN_NAME_STATUS_NEARBY_LIST = "status_nearby_list";
        public static final String COLUMN_NAME_EXP = "experience";
        //        public static final String COLUMN_NAME_LNG = "lng";
        //        public static final String COLUMN_NAME_LAT = "lat";
        public static final String COLUMN_NAME_DISTANCE = "distance";
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
        public static final String COLUMN_NAME_STATUS_MSG_LIST = "status_msg_list";
        public static final String COLUMN_NAME_STATUS_NEARBY_LIST = "status_nearby_list";
        public static final String COLUMN_NAME_OWNER = "owner";
        public static final String COLUMN_NAME_CONTENT = "content";
        public static final String COLUMN_NAME_UPDATE_TIME = "update_time";
        public static final String COLUMN_NAME_COUNT = "count";
    }

    public static abstract class ShieldEntry implements BaseColumns {
        // 访问Uri
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/shield");

        // 内容类型
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/net.pingfang.signalr.chat.shield";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/net.pingfang.signalr.chat.shield";

        // 默认排序常量
        public static final String DEFAULT_SORT_ORDER = "shield DESC";

        public static final String TABLE_NAME = "t_shield";
        public static final String COLUMN_NAME_SHIELD = "shield";
        public static final String COLUMN_NAME_OWNER = "owner";
    }

    public static abstract class ShieldListView implements BaseColumns {
        // 访问Uri
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/v_shield");

        // 内容类型
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/net.pingfang.signalr.chat.v_shield";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/net.pingfang.signalr.chat.v_shield";

        // 默认排序常量
        public static final String DEFAULT_SORT_ORDER = "uid DESC";

        public static final String VIEW_NAME = "v_list_shield";
        public static final String COLUMN_NAME_UID = "uid";
        public static final String COLUMN_NAME_NICKNAME ="nickname";
        public static final String COLUMN_NAME_PORTRAIT = "portrait";
        public static final String COLUMN_NAME_STATUS_MSG_LIST = "status_msg_list";
        public static final String COLUMN_NAME_STATUS_NEARBY_LIST = "status_nearby_list";
        public static final String COLUMN_NAME_OWNER = "owner";
    }

    //广告数据
    public static abstract class AdvertisementEntry implements BaseColumns {
        // 访问Uri
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/");


        // 内容类型
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/net.pingfang.signalr.chat.advertisement";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/net.pingfang.signalr.chat.advertisement";

        public static final String TABLE_NAME = "t_advertisement";
        public static final String COLUMN_NAME_AD_UID = "t_uid";
        public static final String COLUMN_NAME_AD_ADDRESS = "t_address";
        public static final String COLUMN_NAME_AD_CODE = "t_code";
        public static final String COLUMN_NAME_AD_LENGTH = "t_length";
        public static final String COLUMN_NAME_AD_WIDTH = "t_width";
        public static final String COLUMN_NAME_AD_REMARK = "t_remark";
        public static final String COLUMN_NAME_AD_LAT = "t_lat";
        public static final String COLUMN_NAME_AD_LNG = "t_lng";
        public static final String COLUMN_NAME_AD_PATH_P1 = "t_path_p1";
        public static final String COLUMN_NAME_AD_PATH_P2 = "t_path_p2";
        public static final String COLUMN_NAME_AD_PATH_P3 = "t_path_p3";
        public static final String COLUMN_NAME_AD_PATH_P4 = "t_path_p4";
        public static final String COLUMN_NAME_AD_STATUS = "t_status";


        // 默认排序常量
        public static final String DEFAULT_SORT_ORDER = AdvertisementEntry._ID + " ASC";
    }
}
