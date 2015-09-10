package net.pingfang.signalr.chat.constant;

import com.sina.weibo.sdk.exception.WeiboException;
import com.sina.weibo.sdk.net.RequestListener;
import com.sina.weibo.sdk.utils.LogUtil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Created by gongguopei87@gmail.com on 2015/9/10.
 */
public class WeiboRequestListener implements RequestListener{

    private static String TAG = WeiboRequestListener.class.getSimpleName();

    @Override
    public void onComplete(String response) {

    }

    @Override
    public void onComplete4binary(ByteArrayOutputStream responseOS) {
        LogUtil.e(TAG, "onComplete4binary...");
    }

    @Override
    public void onIOException(IOException e) {
        LogUtil.e(TAG, "onIOException： " + e.getMessage());
    }

    @Override
    public void onError(WeiboException e) {
        LogUtil.e(TAG, "WeiboException： " + e.getMessage());
    }
}
