package net.pingfang.signalr.chat.net;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by gongguopei87@gmail.com on 2015/12/9.
 */
public class NetUtil {

    /**
     * 检查当前网络是否可用
     *
     * @param context
     * @return
     */
    public static boolean isConnected(Context context) {

        boolean connectedState = isNetworkAvailable(context) || isWifiAvailable(context);

        return connectedState;
    }

    /**
     * 检查手机网络是否可用
     *
     * @param context
     * @return
     */
    public static boolean isNetworkAvailable(Context context) {

        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mobileNetworkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        return mobileNetworkInfo != null && mobileNetworkInfo.isConnected();

    }

    /**
     * 检查wifi是否可用
     *
     * @param context
     * @return
     */
    public static boolean isWifiAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiNetworkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return wifiNetworkInfo != null && wifiNetworkInfo.isConnected();
    }
}
