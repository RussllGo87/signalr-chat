package net.pingfang.signalr.chat.constant.wechat;

import android.os.Bundle;
import android.text.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by gongguopei87@gmail.com on 2015/11/2.
 */
public class WxOauth2AccessToken {
    
    private String mOpenId = "";
    private String mAccessToken = "";
    private String mRefreshToken = "";
    private long mExpiresTime = 0L;

    public WxOauth2AccessToken() {
    }

    public WxOauth2AccessToken(String openId, String accessToken, String expiresIn, String refreshToken) {
        this.mOpenId = openId;
        this.mAccessToken = accessToken;
        this.mExpiresTime = System.currentTimeMillis();
        if(expiresIn != null) {
            this.mExpiresTime += Long.parseLong(expiresIn) * 1000L;
        }
        this.mRefreshToken = refreshToken;

    }

    public static WxOauth2AccessToken parseAccessToken(String responseJsonText) {
        if(!TextUtils.isEmpty(responseJsonText) && responseJsonText.indexOf("{") >= 0) {
            try {
                JSONObject e = new JSONObject(responseJsonText);
                WxOauth2AccessToken token = new WxOauth2AccessToken();
                token.setOpenId(e.optString("openid"));
                token.setToken(e.optString("access_token"));
                token.setExpiresIn(e.optString("expires_in"));
                token.setRefreshToken(e.optString("refresh_token"));
                return token;
            } catch (JSONException var3) {
                var3.printStackTrace();
            }
        }

        return null;
    }

    public static WxOauth2AccessToken parseAccessToken(Bundle bundle) {
        if(bundle != null) {
            WxOauth2AccessToken accessToken = new WxOauth2AccessToken();
            accessToken.setOpenId(getString(bundle, "openid", ""));
            accessToken.setToken(getString(bundle, "access_token", ""));
            accessToken.setExpiresIn(getString(bundle, "expires_in", ""));
            accessToken.setRefreshToken(getString(bundle, "refresh_token", ""));
            return accessToken;
        } else {
            return null;
        }
    }

    public boolean isSessionValid() {
        return !TextUtils.isEmpty(this.mAccessToken) && this.mExpiresTime != 0L && System.currentTimeMillis() < this.mExpiresTime;
    }

    public Bundle toBundle() {
        Bundle bundle = new Bundle();
        bundle.putString("openid", this.mOpenId);
        bundle.putString("access_token", this.mAccessToken);
        bundle.putString("refresh_token", this.mRefreshToken);
        bundle.putString("expires_in", Long.toString(this.mExpiresTime));
        return bundle;
    }

    public String toString() {
        return "openid: " + this.mOpenId + ", " + "access_token" + ": " + this.mAccessToken + ", " + "refresh_token" + ": " + this.mRefreshToken + ", " + "expires_in" + ": " + Long.toString(this.mExpiresTime);
    }

    public String getOpenId() {
        return this.mOpenId;
    }

    public void setOpenId(String mOpenId) {
        this.mOpenId = mOpenId;
    }

    public String getToken() {
        return this.mAccessToken;
    }

    public void setToken(String mToken) {
        this.mAccessToken = mToken;
    }

    public String getRefreshToken() {
        return this.mRefreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.mRefreshToken = refreshToken;
    }

    public long getExpiresTime() {
        return this.mExpiresTime;
    }

    public void setExpiresTime(long mExpiresTime) {
        this.mExpiresTime = mExpiresTime;
    }

    public void setExpiresIn(String expiresIn) {
        if(!TextUtils.isEmpty(expiresIn) && !expiresIn.equals("0")) {
            this.setExpiresTime(System.currentTimeMillis() + Long.parseLong(expiresIn) * 1000L);
        }

    }

    private static String getString(Bundle bundle, String key, String defaultValue) {
        if(bundle != null) {
            String value = bundle.getString(key);
            return value != null?value:defaultValue;
        } else {
            return defaultValue;
        }
    }

}
