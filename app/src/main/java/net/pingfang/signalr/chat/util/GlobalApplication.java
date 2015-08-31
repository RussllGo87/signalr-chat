package net.pingfang.signalr.chat.util;

import android.app.Application;

import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

import net.pingfang.signalr.chat.R;

import java.util.Locale;

/**
 * Created by gongguopei87@gmail.com on 2015/8/13.
 */
public class GlobalApplication extends Application {

    public static final String ACTION_INTENT_TEXT_MESSAGE_INCOMING = "ACTION_INTENT_TEXT_MESSAGE_INCOMING";
    public static final String ACTION_INTENT_IMAGE_MESSAGE_INCOMING = "ACTION_INTENT_IMAGE_MESSAGE_INCOMING";
    public static final String ACTION_INTENT_VOICE_MESSAGE_INCOMING = "ACTION_INTENT_VOICE_MESSAGE_INCOMING";
    public static final String ACTION_INTENT_VIDEO_MESSAGE_INCOMING = "ACTION_INTENT_VIDEO_MESSAGE_INCOMING";

    public static final String VOICE_FILE_NAME_PREFIX = "VOICE_";
    public static final String VOICE_FILE_NAME_SUFFIX = ".3gp";

    private Locale myLocale;
    SharedPreferencesHelper helper;

    // 微信相关参数
    public static final String APP_ID = "wx0fcc821b51b8c948";
    // 腾讯地图webservice接口相关
    public static final String T_MAP_KEY = "YFLBZ-6PQAR-7PPWA-WU7LO-BKKRK-YLF73";
    public static final String T_MAP_SCHEME = "http";
    public static final String T_MAP_HOST = "apis.map.qq.com";

    public static final String T_MAP_LIST_PATH = "/ws/district/v1/list";
    public static final String T_MAP_CHILDREN = "/ws/district/v1/getchildren";
    public static final String T_MAP_SEARCH = "/ws/district/v1/search";

    @Override
    public void onCreate() {
        super.onCreate();

        helper = SharedPreferencesHelper.newInstance(getApplicationContext());
        loadLocale();


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
        helper.putStringValue(langPref,lang);
    }


    public void loadLocale() {
        String langPref = getResources().getString(R.string.prefs_language);
        String language = helper.getStringValue(langPref,"zh");
        changeLang(language);
    }
}
