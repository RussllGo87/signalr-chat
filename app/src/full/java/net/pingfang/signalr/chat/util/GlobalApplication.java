package net.pingfang.signalr.chat.util;

import android.app.Application;

import com.baidu.mapapi.SDKInitializer;

import net.pingfang.signalr.chat.R;

import java.util.Locale;

/**
 * Created by gongguopei87@gmail.com on 2015/8/13.
 */
public class GlobalApplication extends Application {

    public static final int REQUEST_IMAGE_CAPTURE = 0x01;
    public static final int REQUEST_IMAGE_GET = 0x02;

    //    public static final String URL_WEB_API_HOST = "http://hale.redstream.cn";
    //    public static final String URL_COMMUNICATION_API_HOST = "http://hale.redstream.cn:10086";

    public static final String URL_WEB_API_HOST = "http://hale.hlqcm.cn";
    public static final String URL_COMMUNICATION_API_HOST = "http://hale.hlqcm.cn:10086";

    //    public static final String PORTRAIT_URL_PREFIX = URL_WEB_API_HOST + "/UpLoad/Head/";
    //    public static final String PIC_URL_PREFIX = URL_WEB_API_HOST + "/UpLoad/";
    //    public static final String RESOURCE_PIC_URL_PREFIX = PIC_URL_PREFIX + "Resource/";

    //    public static final String ACTION_INTENT_TEXT_MESSAGE_INCOMING = "ACTION_INTENT_TEXT_MESSAGE_INCOMING";
    //    public static final String ACTION_INTENT_IMAGE_MESSAGE_INCOMING = "ACTION_INTENT_IMAGE_MESSAGE_INCOMING";
    //    public static final String ACTION_INTENT_VOICE_MESSAGE_INCOMING = "ACTION_INTENT_VOICE_MESSAGE_INCOMING";
    //    public static final String ACTION_INTENT_VIDEO_MESSAGE_INCOMING = "ACTION_INTENT_VIDEO_MESSAGE_INCOMING";

    public static final String ACTION_INTENT_MSG_LIST_UPDATE = "ACTION_INTENT_MSG_LIST_UPDATE";
    public static final String ACTION_INTENT_MSG_UPDATE = "ACTION_INTENT_MSG_UPDATE";
    public static final String ACTION_INTENT_UPDATE_ONLINE_LIST = "ACTION_INTENT_UPDATE_ONLINE_LIST";
    public static final String ACTION_INTENT_ONLINE_MESSAGE_INCOMING = "ACTION_INTENT_ONLINE_MESSAGE_INCOMING";
    public static final String ACTION_INTENT_ONLINE_MESSAGE_SEND = "ACTION_INTENT_ONLINE_MESSAGE_SEND";
    public static final String ACTION_INTENT_OFFLINE_USER_LIST_INCOMING = "ACTION_INTENT_OFFLINE_USER_LIST_INCOMING";
    public static final String ACTION_INTENT_OFFLINE_MESSAGE_LIST_INCOMING = "ACTION_INTENT_OFFLINE_MESSAGE_LIST_INCOMING";
    public static final String ACTION_INTENT_OFFLINE_MESSAGE_LIST_COUNT_UPDATE = "ACTION_INTENT_OFFLINE_MESSAGE_LIST_COUNT_UPDATE";
    public static final String ACTION_INTENT_BULK_MESSAGE_INCOMING = "ACTION_INTENT_BULK_MESSAGE_INCOMING";
    public static final String ACTION_INTENT_BULK_MESSAGE_SEND = "ACTION_INTENT_BULK_MESSAGE_SEND";
    public static final String ACTION_INTENT_SHIELD_LIST_UPDATE = "ACTION_INTENT_SHIELD_LIST_UPDATE";
    public static final String ACTION_INTENT_SHIELD_LIST_ADD = "ACTION_INTENT_SHIELD_LIST_ADD";
    public static final String ACTION_INTENT_SHIELD_LIST_BEFORE = "ACTION_INTENT_SHIELD_LIST_BEFORE";

    public static final String ACTION_INTENT_ACCOUNT_INFO_UPDATE = "ACTION_INTENT_ACCOUNT_INFO_UPDATE";
    public static final String ACTION_INTENT_REMARK_UPDATE = "ACTION_INTENT_REMARK_UPDATE";

    public static final String IMAGE_TITLE_NAME_PREFIX = "IMAGE_";
    public static final String VOICE_FILE_NAME_PREFIX = "VOICE_";
    public static final String VOICE_FILE_NAME_SUFFIX = ".3gp";


    public static final String ACTION_WX_AUTH_OK = "ACTION_WX_AUTH_OK";
    SharedPreferencesHelper helper;
    private Locale myLocale;

    @Override
    public void onCreate() {
        super.onCreate();

        helper = SharedPreferencesHelper.newInstance(getApplicationContext());
        // 语言切换
        loadLocale();

        // 初始化百度地图SDK
        SDKInitializer.initialize(getApplicationContext());
    }

    public void changeLang(String lang) {
        if (lang.equalsIgnoreCase(""))
            return;
        myLocale = new Locale(lang);
        saveLocale(lang);
        Locale.setDefault(myLocale);
        android.content.res.Configuration config = new android.content.res.Configuration();
        config.locale = myLocale;
        getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
    }

    public void saveLocale(String lang) {
        String langPref = getResources().getString(R.string.prefs_language);
        helper.putStringValue(langPref, lang);
    }


    public void loadLocale() {
        String langPref = getResources().getString(R.string.prefs_language);
        String language = helper.getStringValue(langPref, "zh");
        changeLang(language);
    }
}
