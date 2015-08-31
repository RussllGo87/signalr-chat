package net.pingfang.signalr.chat.net;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.squareup.okhttp.Cache;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.HttpUrl;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.Callback;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by gongguopei87@gmail.com on 2015/8/28.<br>
 * 对OkHttp请求过程进行封装，本工具类仅用于Restful的客户端测试。<br>
 * 如项目允许建议使用
 * <a href="http://square.github.io/okhttp/">OkHttp</a> +
 * <a href="http://square.github.io/retrofit/">Retrofit</a>方式实现类似服务。<br>
 */
public class OkHttpCommonUtil {

    private static final String TAG = OkHttpCommonUtil.class.getSimpleName();
    private static final int MAX_RESPONSE_STRING_SIZE = 1024 * 1024;

    private static OkHttpClient mOkHttpClient;
    public static OkHttpCommonUtil okHttpCommonUtil;

    private OkHttpCommonUtil(Context context) {
        int cacheSize = 10 * 1024 * 1024;
        Cache cache = new Cache(context.getCacheDir(),cacheSize);
        mOkHttpClient = new OkHttpClient();
        mOkHttpClient.setConnectTimeout(30, TimeUnit.SECONDS);
        mOkHttpClient.setWriteTimeout(10, TimeUnit.SECONDS);
        mOkHttpClient.setReadTimeout(30, TimeUnit.SECONDS);
        mOkHttpClient.setCache(cache);
    }

    /**
     * 获取系统OkHttpCommonUtil实例
     * @param context 建议使用系统的getApplicationContext()方法获取
     * @return OkHttpCommonUtil实例
     */
    public static OkHttpCommonUtil newInstance(Context context) {
        if(okHttpCommonUtil == null) {
            okHttpCommonUtil = new OkHttpCommonUtil(context);
        }

        return okHttpCommonUtil;
    }

    /**
     * 该不会开启异步线程。
     * @param request
     * @return
     * @throws IOException
     */
    public Response execute(Request request) throws IOException {
        return mOkHttpClient.newCall(request).execute();
    }

    /**
     * 开启异步线程访问网络
     * @param request
     * @param responseCallback
     */
    public void enqueue(Request request, Callback responseCallback) {
        mOkHttpClient.newCall(request).enqueue(responseCallback);
    }

    /**
     * 开启异步线程访问网络(不关注返回的请求)
     * @param request
     */
    public void enqueue(Request request) {
        mOkHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {

            }

            @Override
            public void onResponse(Response response) throws IOException {
                if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
                if(response.body().string().length() < MAX_RESPONSE_STRING_SIZE) {
                    Log.d(TAG, response.body().string());
                } else {

                }


            }
        });
    }

    /**
     * 取消请求.
     * 请求必须是buildGetFormReq(String, String, String, Map, String)
     * 或buildGetFormReq(String, String, String, Map, String)
     * 构建的
     * @param tag 请求标记
     */
    public static void cancleReqWithTag(String tag) {
        if(mOkHttpClient == null) {
            return;
        }

        mOkHttpClient.cancel(tag);
    }

    /**
     * 模拟html表单http post请求
     * @param url 服务器url
     * @param map 参数map集合
     * @return OKHttp请求Request对象
     */
    public static Request buildPostFormReq(String url, Map<String,String> map) {
        return buildPostFormReq(url, map, null);
    }

    /**
     * 模拟html表单http post请求
     * @param url 服务器url
     * @param map 参数map集合
     * @param tag 请求标记,取消请求时可用
     * @return OKHttp请求Request对象
     */
    public static Request buildPostFormReq(String url, Map<String,String> map,String tag) {
        FormEncodingBuilder formEncodingBuilder = new FormEncodingBuilder();
        for(String key : map.keySet()) {
            formEncodingBuilder.add(key,map.get(key));
        }
        RequestBody formBody = formEncodingBuilder.build();
        Request.Builder requestBuilder = new Request.Builder();
        requestBuilder.url(url).post(formBody);
        if(!TextUtils.isEmpty(tag)) {
            requestBuilder.tag(tag);
        }
        Request request = requestBuilder.build();;
        return request;
    }

    /**
     * 模拟html表单http get请求
     * @param scheme  http请求类型
     * @param host    http服务端主机地址
     * @param pathSegment  http请求路径
     * @param map   请求参数map集合
     * @return OKHttp请求Request对象
     */
    public static Request buildGetFormReq(String scheme, String host, String pathSegment, Map<String,String> map) {
        return buildGetFormReq(scheme,host,pathSegment,map,null);
    }

    /**
     * 模拟html表单http get请求
     * @param scheme  http请求类型
     * @param host    http服务端主机地址
     * @param pathSegment  http请求路径
     * @param map   请求参数map集合
     * @param tag 请求标记,取消请求时可用
     * @return OKHttp请求Request对象
     */
    public static Request buildGetFormReq(String scheme, String host, String pathSegment, Map<String,String> map,String tag) {

        HttpUrl.Builder builder = new HttpUrl.Builder();
        builder.scheme(scheme)
               .host(host)
               .encodedPath(pathSegment);
        for(String key : map.keySet()) {
            builder.addQueryParameter(key,map.get(key));
        }
        HttpUrl httpUrl = builder.build();
        Request.Builder requestBuilder = new Request.Builder();
        requestBuilder.url(httpUrl);
        if(!TextUtils.isEmpty(tag)) {
            requestBuilder.tag(tag);
        }
        Request request = requestBuilder.build();
        return request;
    }

}
