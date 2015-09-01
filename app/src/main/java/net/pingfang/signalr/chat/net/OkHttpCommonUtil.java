package net.pingfang.signalr.chat.net;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.squareup.okhttp.Cache;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.Headers;
import com.squareup.okhttp.HttpUrl;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.MultipartBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.File;
import java.io.IOException;
import java.net.FileNameMap;
import java.net.URLConnection;
import java.util.concurrent.TimeUnit;

/**
 * Created by gongguopei87@gmail.com on 2015/8/28.<br>
 * 对OkHttp请求过程进行封装，本工具类仅用于Restful的客户端测试。<br>
 * 如项目允许建议使用
 * <a href="http://square.github.io/okhttp/">OkHttp</a> +
 * <a href="http://square.github.io/retrofit/">Retrofit</a>方式实现类似服务。<br>
 * 参考:<a href="https://github.com/square/okhttp/wiki/Recipes">Recipes</a><br/>
 * <a href="http://blog.csdn.net/lmj623565791/article/details/47911083">Android OkHttp完全解析 是时候来了解OkHttp了</a><br/>
 * <a href="http://blog.csdn.net/djk_dong/article/details/47861367"> android 使用OkHttp上传多张图片</a>
 */
public class OkHttpCommonUtil {

    private static final String TAG = OkHttpCommonUtil.class.getSimpleName();
    private static final int MAX_RESPONSE_STRING_SIZE = 1024 * 1024;

    private static OkHttpClient mOkHttpClient;
    private static OkHttpCommonUtil okHttpCommonUtil;

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
            synchronized(OkHttpCommonUtil.class) {
                if(okHttpCommonUtil == null) {
                    okHttpCommonUtil = new OkHttpCommonUtil(context);
                }
            }
        }

        return okHttpCommonUtil;
    }

    /**
     * 使用简单url开启同步GET请求
     * @param url 请求url
     * @return 响应
     * @throws IOException
     */
    private Response getSyncResp(String url) throws IOException {
        final Request request = new Request.Builder()
                .url(url)
                .build();
        Call call = mOkHttpClient.newCall(request);
        Response response = call.execute();
        return response;
    }

    /**
     * 开启同步GET请求
     * @param request 已构建的HTTP GET请求
     * @return 响应
     * @throws IOException
     */
    private Response getSyncResp(Request request) throws IOException {
        return mOkHttpClient.newCall(request).execute();
    }

    /**
     * 使用简单url开启同步GET请求,获取文本格式响应
     * @param url 请求url
     * @return 响应
     * @throws IOException
     */
    private String getSyncString(String url) throws IOException{
        Response response = getSyncResp(url);
        return response.body().string();
    }

    /**
     * 开启同步GET请求,获取文本格式响应
     * @param request 已构建的HTTP GET请求
     * @return 响应
     * @throws IOException
     */
    private String getSyncString(Request request) throws IOException {
        Response response = getSyncResp(request);
        return response.body().string();
    }

    /**
     * 开启异步HTTP GET请求
     * @param url 请求url
     * @param responseCallback 响应回调接口
     */
    private void getAsync(String url, Callback responseCallback) {
        final Request request = new Request.Builder()
                .url(url)
                .build();
        mOkHttpClient.newCall(request).enqueue(responseCallback);
    }

    /**
     * 开启异步HTTP GET请求
     * @param request 已构建的HTTP GET请求
     * @param responseCallback 响应回调接口
     */
    private void getAsync(Request request, Callback responseCallback) {
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
                if (!response.isSuccessful())
                    throw new IOException("Unexpected code " + response);
                if (response.body().string().length() < MAX_RESPONSE_STRING_SIZE) {
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
     * 模拟html表单http get请求
     * @param url 请求url
     * @param params 参数params
     * @return OKHttp请求Request对象
     */
    public static Request buildGetReq(String url, Param[] params) {
        return buildGetReq(url, params, null);
    }

    /**
     * 模拟html表单http get请求
     * @param url 请求url
     * @param params 参数params
     * @param tag 请求标记,取消请求时可用
     * @return OKHttp请求Request对象
     */
    public static Request buildGetReq(String url, Param[] params,String tag) {
        HttpUrl.Builder builder = HttpUrl.parse(url).newBuilder();
        for (Param param : params) {
            builder.addQueryParameter(param.key, param.value);
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


    /**
     * 模拟html表单http post请求
     * @param url 服务器url
     * @param params 参数params
     * @return OKHttp请求Request对象
     */
    public static Request buildPostReq(String url, Param[] params) {
        return buildPostReq(url, params, null);
    }

    /**
     * 模拟html表单http post请求
     * @param url 服务器url
     * @param params 参数params
     * @param tag 请求标记,取消请求时可用
     * @return OKHttp请求Request对象
     */
    public static Request buildPostReq(String url, Param[] params,String tag) {
        if(params == null) {
            params = new Param[0];
        }

        FormEncodingBuilder formEncodingBuilder = new FormEncodingBuilder();
        for (Param param : params) {
            formEncodingBuilder.add(param.key, param.value);
        }

        RequestBody formBody = formEncodingBuilder.build();
        Request.Builder requestBuilder = new Request.Builder();
        requestBuilder.url(url).post(formBody);
        if(!TextUtils.isEmpty(tag)) {
            requestBuilder.tag(tag);
        }
        Request request = requestBuilder.build();
        return request;
    }

    /**
     * 模拟浏览器HTTP POST上传文件请求
     * @param url 请求url
     * @param params  请求参数
     * @param files  上传文件
     * @param fileKey 模拟上传表单(form)对应的key
     * @return 已构建Request请求对象
     */
    public static Request buildMultipartFormRequest(String url, Param[] params, File[] files,String fileKey) {

        params = validateParam(params);

        MultipartBuilder multipartBuilder = new MultipartBuilder();
        multipartBuilder.type(MultipartBuilder.FORM);

        for(Param param : params) {
            multipartBuilder.addPart(Headers.of("Content-Disposition", "form-data; name=\"" + param.key + "\""),
                    RequestBody.create(null, param.value));
        }

        if(files != null && files.length > 0) {
            RequestBody fileBody = null;
            for (File file : files) {
                String fileName = file.getName();
                fileBody = RequestBody.create(MediaType.parse(guessMimeType(fileName)), file);
                multipartBuilder.addPart(Headers.of("Content-Disposition",
                                "form-data; name=\"" + fileKey + "\"; filename=\"" + fileName + "\""),
                        fileBody);
            }
        }

        RequestBody requestBody = multipartBuilder.build();
        return new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();
    }



    public static Param[] validateParam(Param[] params) {
        if (params == null)
            return new Param[0];
        else return params;
    }

    public static String guessMimeType(String path) {
        FileNameMap fileNameMap = URLConnection.getFileNameMap();
        String contentTypeFor = fileNameMap.getContentTypeFor(path);
        if (contentTypeFor == null) {
            contentTypeFor = "application/octet-stream";
        }
        return contentTypeFor;
    }

    public static class Param {
        public Param() {
        }

        public Param(String key, String value) {
            this.key = key;
            this.value = value;
        }

        String key;
        String value;
    }

}
