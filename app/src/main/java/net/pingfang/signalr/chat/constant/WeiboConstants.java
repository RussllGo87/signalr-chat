package net.pingfang.signalr.chat.constant;

/**
 * Created by gongguopei87@gmail.com on 2015/9/10.
 * 该类定义了微博授权时所需要的参数。
 */
public abstract class WeiboConstants {

    // 应用配置相关
    public static final String APP_KEY = "1632292707";
    public static final String REDIRECT_URL = "https://api.weibo.com/oauth2/default.html";
    public static final String SCOPE =
            "email,direct_messages_read,direct_messages_write,"
                    + "friendships_groups_read,friendships_groups_write,statuses_to_me_read,"
                    + "follow_app_official_microblog," + "invitation_write";

    // 访问标记(access token)相关
    public static final String KEY_UID           = "wb_uid";
    public static final String KEY_ACCESS_TOKEN  = "wb_access_token";
    public static final String KEY_EXPIRES_IN    = "wb_expires_in";


}
