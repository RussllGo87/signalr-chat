package net.pingfang.signalr.chat.util;

import android.content.Context;
import android.os.Build;
import android.os.StrictMode;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;

import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

import net.pingfang.signalr.chat.R;
import net.pingfang.signalr.chat.constant.wechat.WechatConstants;

import java.util.Locale;

/**
 * Created by gongguopei87@gmail.com on 2015/8/13.
 */
public class GlobalApplication extends MultiDexApplication {

    public static final String ACTION_INTENT_TEXT_MESSAGE_INCOMING = "ACTION_INTENT_TEXT_MESSAGE_INCOMING";
    public static final String ACTION_INTENT_IMAGE_MESSAGE_INCOMING = "ACTION_INTENT_IMAGE_MESSAGE_INCOMING";
    public static final String ACTION_INTENT_VOICE_MESSAGE_INCOMING = "ACTION_INTENT_VOICE_MESSAGE_INCOMING";
    public static final String ACTION_INTENT_VIDEO_MESSAGE_INCOMING = "ACTION_INTENT_VIDEO_MESSAGE_INCOMING";

    public static final String ACTION_INTENT_UPDATE_ONLINE_LIST = "ACTION_INTENT_UPDATE_ONLINE_LIST";
    public static final String ACTION_INTENT_ONLINE_MESSAGE_INCOMING = "ACTION_INTENT_ONLINE_MESSAGE_INCOMING";
    public static final String ACTION_INTENT_ONLINE_MESSAGE_SEND = "ACTION_INTENT_ONLINE_MESSAGE_SEND";
    public static final String ACTION_INTENT_OFFLINE_USER_LIST_INCOMING = "ACTION_INTENT_OFFLINE_USER_LIST_INCOMING";
    public static final String ACTION_INTENT_OFFLINE_MESSAGE_LIST_INCOMING = "ACTION_INTENT_OFFLINE_MESSAGE_LIST_INCOMING";
    public static final String ACTION_INTENT_OFFLINE_MESSAGE_LIST_COUNT_UPDATE = "ACTION_INTENT_OFFLINE_MESSAGE_LIST_COUNT_UPDATE";

    public static final String IMAGE_TITLE_NAME_PREFIX = "IMAAGE_";
    public static final String VOICE_FILE_NAME_PREFIX = "VOICE_";
    public static final String VOICE_FILE_NAME_SUFFIX = ".3gp";



    // 腾讯地图webservice接口相关
    public static final String T_MAP_KEY = "YFLBZ-6PQAR-7PPWA-WU7LO-BKKRK-YLF73";

    private Locale myLocale;
    SharedPreferencesHelper helper;

    /**
     * IWXAPI 是第三方app和微信通信的openapi接口
     */
    private IWXAPI api;

    @Override
    public void onCreate() {
        super.onCreate();

        helper = SharedPreferencesHelper.newInstance(getApplicationContext());
        loadLocale();

        api = WXAPIFactory.createWXAPI(getApplicationContext(), WechatConstants.APP_ID,false);
        api.registerApp(WechatConstants.APP_ID);

        if (Config.DEVELOPER_MODE && Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectAll().penaltyDialog().build());
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().detectAll().penaltyDeath().build());
        }

        initImageLoader(getApplicationContext());
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
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

    public static class Config {
        public static final boolean DEVELOPER_MODE = false;
    }

    public static void initImageLoader(Context context) {

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context)
                .threadPriority(Thread.NORM_PRIORITY - 2)
                .denyCacheImageMultipleSizesInMemory()
                .discCacheFileNameGenerator(new Md5FileNameGenerator())
                .tasksProcessingOrder(QueueProcessingType.LIFO)
                .writeDebugLogs() // Remove for release app
                .build();

        ImageLoader.getInstance().init(config);
    }
}
