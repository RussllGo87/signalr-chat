package net.pingfang.signalr.chat.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.sina.weibo.sdk.auth.Oauth2AccessToken;

import net.pingfang.signalr.chat.R;
import net.pingfang.signalr.chat.constant.qq.TencentConstants;
import net.pingfang.signalr.chat.constant.wechat.WxConstants;
import net.pingfang.signalr.chat.constant.wechat.WxOauth2AccessToken;
import net.pingfang.signalr.chat.constant.weibo.WeiboConstants;

/**
 * Created by gongguopei87@gmail.com on 2015/8/13.
 */
public class SharedPreferencesHelper {
    SharedPreferences sp;
    SharedPreferences.Editor editor;

    public static SharedPreferencesHelper helper;

    private SharedPreferencesHelper(Context context) {
        String pref = context.getResources().getString(R.string.prefs_name);
        sp = context.getSharedPreferences(pref, Context.MODE_PRIVATE);
    }

    public static SharedPreferencesHelper newInstance(Context context) {
        if (helper == null)
        {
            helper = new SharedPreferencesHelper(context);
        }
        return helper;
    }

    public void putStringValue(String key, String value) {
        editor = sp.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public String getStringValue(String key) {
        return getStringValue(key,null);
    }

    public String getStringValue(String key,String defalut)
    {
        return sp.getString(key, defalut);
    }

    public void setBoolean(String key,boolean value) {
        editor = sp.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    public boolean getBoolean(String key,boolean defaultValue) {
        return sp.getBoolean(key, defaultValue);
    }

    public void putInt(String key, int value) {
        editor = sp.edit();
        editor.putInt(key, value);
        editor.apply();
    }

    public int getInt(String key,int defaultV) {
        return sp.getInt(key, defaultV);
    }

    public void putFloat(String key,float value) {
        editor = sp.edit();
        editor.putFloat(key, value);
        editor.apply();
    }

    public float getFloat(String key,float defaultV) {
        return sp.getFloat(key, defaultV);
    }

    public void putLong(String key,long value) {
        editor = sp.edit();
        editor.putLong(key, value);
        editor.apply();
    }

    public long getLong(String key,long defaultV) {
        return sp.getLong(key, defaultV);
    }

    public void clearKey(String key) {
        editor = sp.edit();
        editor.remove(key);
        editor.apply();
    }


    /**
     * 保存 Token 对象到 SharedPreferences。
     *
     * @param token   Token 对象
     */
    public static void writeAccessToken(Oauth2AccessToken token) {
        if (null == token) {
            return;
        }
        helper.putStringValue(WeiboConstants.KEY_UID, token.getUid());
        helper.putStringValue(WeiboConstants.KEY_ACCESS_TOKEN, token.getToken());
        helper.putLong(WeiboConstants.KEY_EXPIRES_IN, token.getExpiresTime());
    }

    /**
     * 从 SharedPreferences 读取 Token 信息。
     * @return 返回 Token 对象
     */
    public static Oauth2AccessToken readAccessToken() {
        Oauth2AccessToken token = new Oauth2AccessToken();
        token.setUid(helper.getStringValue(WeiboConstants.KEY_UID, ""));
        token.setToken(helper.getStringValue(WeiboConstants.KEY_ACCESS_TOKEN, ""));
        token.setExpiresTime(helper.getLong(WeiboConstants.KEY_EXPIRES_IN, 0));
        return token;
    }

    public static void clearAccessToken() {
        helper.clearKey(WeiboConstants.KEY_UID);
        helper.clearKey(WeiboConstants.KEY_ACCESS_TOKEN);
        helper.clearKey(WeiboConstants.KEY_EXPIRES_IN);
    }

    public static void writeAccessToken(String token,String expires,String openId) {
        helper.putStringValue(TencentConstants.KEY_ACCESS_TOKEN, token);
        helper.putStringValue(TencentConstants.KEY_EXPIRES_IN, expires);
        helper.putStringValue(TencentConstants.KEY_OPEN_ID, openId);
    }

    public static void clearQqAccessToken() {
        helper.clearKey(TencentConstants.KEY_ACCESS_TOKEN);
        helper.clearKey(TencentConstants.KEY_EXPIRES_IN);
        helper.clearKey(TencentConstants.KEY_OPEN_ID);
    }

    public static void writeAccessToken(WxOauth2AccessToken token) {
        if (null == token) {
            return;
        }
        helper.putStringValue(WxConstants.KEY_WX_OPEN_ID, token.getOpenId());
        helper.putStringValue(WxConstants.KEY_WX_ACCESS_TOKEN, token.getToken());
        helper.putLong(WxConstants.KEY_WX_EXPIRES_IN, token.getExpiresTime());
        helper.putStringValue(WxConstants.KEY_WX_REFRESH_TOKEN, token.getRefreshToken());
    }

}
