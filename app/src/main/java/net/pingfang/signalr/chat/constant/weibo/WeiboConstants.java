package net.pingfang.signalr.chat.constant.weibo;

/**
 * Created by gongguopei87@gmail.com on 2015/9/10.
 * 该类定义了微博授权时所需要的参数。
 */
public abstract class WeiboConstants {

    // 应用配置相关
    public static final String APP_KEY = "1632292707";
    public static final String APP_SECRET = "ad5a4a3290dd238475f2923e76c7ebb5";
    public static final String REDIRECT_URL = "https://api.weibo.com/oauth2/default.html";
    public static final String SCOPE =
            "email,direct_messages_read,direct_messages_write,"
                    + "friendships_groups_read,friendships_groups_write,statuses_to_me_read,"
                    + "follow_app_official_microblog," + "invitation_write";

    // 访问标记(access token)相关
    public static final String KEY_UID           = "wb_uid";
    public static final String KEY_ACCESS_TOKEN  = "wb_access_token";
    public static final String KEY_EXPIRES_IN    = "wb_expires_in";

    // 授权相关请求相关KEY配置
    public static final String KEY_CLIENT_ID = "client_id";
    public static final String KEY_CLIENT_SECRET = "client_secret";
    public static final String KEY_GRANT_TYPE = "grant_type";
    public static final String KEY_REDIRECT_URL = "redirect_uri";
    public static final String KEY_REFRESH_TOKEN = "refresh_token";

}
