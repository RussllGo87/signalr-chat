package net.pingfang.signalr.chat.constant.wechat;

/**
 * Created by gongguopei87@gmail.com on 2015/9/11.
 */
public abstract class WxConstants {

    public static final String APP_ID = "wx1b94e2667b92c9b3";
    public static final String APP_SECRET = "d4624c36b6795d1d99dcf0547af5443d";
    public static final String SCOPE = "snsapi_userinfo";

    // 访问标记(access token)相关
    public static final String KEY_WX_OPEN_ID = "KEY_WX_OPEN_ID";
    public static final String KEY_WX_ACCESS_TOKEN = "KEY_WX_ACCESS_TOKEN";
    public static final String KEY_WX_EXPIRES_IN = "KEY_WX_EXPIRES_IN";
    public static final String KEY_WX_REFRESH_TOKEN = "KEY_WX_REFRESH_TOKEN";

    // 用户信息相关参数
    public static final String PARAM_WX_NICKNAME = "nickname";
    public static final String PARAM_WX_HEAD_IMG_URL = "headimgurl";

    //用户信息本地存储相关KEY
    public static final String KEY_WX_NICKNAME = "KEY_WX_NICKNAME";
    public static final String KEY_WX_HEAD_IMG_URL = "KEY_WX_HEAD_IMG_URL";
}
