package net.pingfang.signalr.chat.net;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

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

import net.pingfang.signalr.chat.util.ImageUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
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
    private Handler mDelivery;

    private OkHttpCommonUtil(Context context) {
        int cacheSize = 10 * 1024 * 1024;
        mDelivery = new Handler(Looper.getMainLooper());
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
     * 模拟html表单http get请求同步提交
     * @param url 请求url
     * @param params 参数params
     * @param tag 请求标记,取消请求时可用
     * @return OKHttp响应Response对象
     * @throws IOException
     */
    private Response getSyncResp(String url, Param[] params,String tag) throws IOException {
        HttpUrl.Builder builder = HttpUrl.parse(url).newBuilder();
        if(params == null) {
            params = new Param[0];
        }
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
        Call call = mOkHttpClient.newCall(request);
        Response response = call.execute();
        return response;
    }

    /**
     * 模拟html表单http get请求同步提交,获取文本格式响应
     * @param url 请求url
     * @param params 参数params
     * @param tag 请求标记,取消请求时可用
     * @return 请求文本
     * @throws IOException
     */
    private String getSyncString(String url, Param[] params,String tag) throws IOException{
        Response response = getSyncResp(url, params, tag);
        return response.body().string();
    }

    /**
     * 模拟html表单http get请求异步提交
     * @param url 请求url
     * @param params 参数params
     * @param tag 请求标记,取消请求时可用
     * @param responseCallback 响应回调接口
     */
    private void getAsync(String url, Param[] params,String tag, Callback responseCallback) {
        HttpUrl.Builder builder = HttpUrl.parse(url).newBuilder();
        if(params == null) {
            params = new Param[0];
        }
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
        mOkHttpClient.newCall(request).enqueue(responseCallback);
    }

    /**
     * 模拟html表单http POST请求同步提交
     * @param url 服务器url
     * @param params 参数params
     * @param tag 请求标记,取消请求时可用
     * @return 响应
     */
    private Response postSyncResp(String url, Param[] params,String tag) throws IOException {
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
        Response response = mOkHttpClient.newCall(request).execute();
        return response;
    }

    /**
     * 模拟html表单http POST请求同步提交,获取文本格式响应
     * @param url 服务器url
     * @param params 参数params
     * @param tag 请求标记,取消请求时可用
     * @return 字符串
     */
    private String postSyncString(String url, Param[] params,String tag) throws IOException {
        Response response = postSyncResp(url, params, tag);
        return response.body().string();
    }

    /**
     * 模拟html表单http POST请求异步提交
     * @param url 服务器url
     * @param params 参数params
     * @param tag 请求标记,取消请求时可用
     * @param callback 响应回调接口
     */
    private void postAsync(String url, Param[] params,String tag, Callback callback) {
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
        mOkHttpClient.newCall(request).enqueue(callback);
    }

    /**
     * HTTP POST同步上传文件
     * @param url 请求url
     * @param files 上传文件
     * @param fileKey 模拟上传表单(form)对应的key
     * @param params 请求参数
     * @return 响应
     */
    private Response postSyncUploadFile(String url, File[] files, String fileKey, Param... params) throws IOException {

        if(params == null) {
            params = new Param[0];
        }

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
        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();
        return mOkHttpClient.newCall(request).execute();
    }

    /**
     * HTTP POST异步上传文件
     * @param url 请求url
     * @param files 上传文件
     * @param fileKey 模拟上传表单(form)对应的key
     * @param params 请求参数
     * @return 响应
     */
    private void postAsyncUploadFile(String url,Callback callback, File[] files, String fileKey, Param... params) {

        if(params == null) {
            params = new Param[0];
        }

        MultipartBuilder multipartBuilder = new MultipartBuilder();
        multipartBuilder.type(MultipartBuilder.FORM);

        for(Param param : params) {
            multipartBuilder.addFormDataPart(param.key, param.value);
        }

        if(files != null && files.length > 0) {
            RequestBody fileBody = null;
            for (File file : files) {
                String fileName = file.getName();
                fileBody = RequestBody.create(MediaType.parse(guessMimeType(fileName)), file);
                multipartBuilder.addFormDataPart(fileKey, fileName, fileBody);
            }
        }

        RequestBody requestBody = multipartBuilder.build();
        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();
        mOkHttpClient.newCall(request).enqueue(callback);
    }

    private void downloadAsyn(final String url, final String filePath,final ResultCallback callback) {
        Request request = new Request.Builder()
                .url(url)
                .build();

        Call call = mOkHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                sendExceptionCallBack(request, e, callback);
            }

            @Override
            public void onResponse(Response response) throws IOException {
                byte[] buf = new byte[2048];
                int len = 0;

                InputStream is = null;
                FileOutputStream fos = null;

                int downloaded = 0;
                final long target = response.body().contentLength();

                try {
                    is = response.body().byteStream();
                    File file = new File(filePath);
                    fos = new FileOutputStream(file);
                    while (true) {
                        int read = is.read(buf);
                        if (read == -1) {
                            break;
                        }
                        fos.write(buf);
                        downloaded += read;
                        publishProgress(downloaded, target, callback);
                    }
                    fos.flush();
                    sendCallBack(file.getAbsolutePath(), callback);
                } catch (IOException e) {
                    sendExceptionCallBack(response.request(), e, callback);
                } finally {
                    try {
                        if (fos != null)
                            fos.close();
                        if (is != null)
                            is.close();
                    } catch (IOException e) {
                        Log.e(TAG, e.getMessage());
                    }
                }
            }
        });
    }

    /**
     * url加载图片
     * @param view 绑定的view
     * @param url 图片url
     * @param errorResId 加载错误默认的图片resID
     */
    private void displayImage(final ImageView view, final String url, final int errorResId) {
        Request request = new Request.Builder().url(url).build();
        Call call = mOkHttpClient.newCall(request);

        call.enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                mDelivery.post(new Runnable() {
                    @Override
                    public void run() {
                        view.setImageResource(errorResId);
                    }
                });
            }

            @Override
            public void onResponse(Response response) throws IOException {
                InputStream inputStream = null;
                try {
                    inputStream = response.body().byteStream();

                    ImageUtils.ImageSize actualImageSize = ImageUtils.getImageSize(inputStream);
                    ImageUtils.ImageSize imageViewSize = ImageUtils.getImageViewSize(view);
                    int inSampleSize = ImageUtils.calculateInSampleSize(actualImageSize, imageViewSize);

                    try {
                        inputStream.reset();
                    } catch (IOException e) {
                        response = getSyncResp(url, null, null);
                        inputStream = response.body().byteStream();
                    }

                    BitmapFactory.Options ops = new BitmapFactory.Options();
                    ops.inJustDecodeBounds = false;
                    ops.inSampleSize = inSampleSize;
                    final Bitmap bm = BitmapFactory.decodeStream(inputStream, null, ops);
                    mDelivery.post(new Runnable() {
                        @Override
                        public void run() {
                            view.setImageBitmap(bm);
                        }
                    });
                } catch (Exception e) {
                    mDelivery.post(new Runnable() {
                        @Override
                        public void run() {
                            view.setImageResource(errorResId);
                        }
                    });
                } finally {
                    try {
                        if (inputStream != null)
                            inputStream.close();
                    } catch (IOException e) {

                    }
                }
            }
        });
    }

    private String getFileName(String path) {
        int separatorIndex = path.lastIndexOf("/");
        return (separatorIndex < 0) ? path : path.substring(separatorIndex + 1, path.length());
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

    private void publishProgress(final long downloaded, final long target,final ResultCallback callback) {
        mDelivery.post(new Runnable() {
            @Override
            public void run() {
                if (callback != null)
                    callback.publishProgress(downloaded, target);
            }
        });
    }

    private void sendExceptionCallBack(final Request request, final Exception e, final ResultCallback callback) {
        mDelivery.post(new Runnable() {
            @Override
            public void run() {
                if (callback != null)
                    callback.onError(request, e);
            }
        });
    }

    private void sendCallBack(final Object object,final ResultCallback callback) {
        mDelivery.post(new Runnable() {
            @Override
            public void run() {
                if (callback != null) {
                    callback.onResponse(object);
                }
            }
        });
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

        public Param(String key, double value) {
            this.key = key;
            this.value = Double.toString(value);
        }

        public Param(String key, int value) {
            this.key = key;
            this.value = Integer.toString(value);
        }

        String key;
        String value;
    }


    public void query(String url,Param[] params, final TextView textView) {
        getAsync(url, params, null, new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                final String message = e.getMessage();
                mDelivery.post(new Runnable() {
                    @Override
                    public void run() {
                        textView.setText(message);
                    }
                });
            }

            @Override
            public void onResponse(Response response) throws IOException {
                final String message = response.body().string();
                mDelivery.post(new Runnable() {
                    @Override
                    public void run() {
                        textView.setText(message);
                    }
                });
            }
        });
    }

    public void postRequest(String url,Param[] params,Callback callback) {
        postAsync(url, params, null, callback);
    }

    public void getRequest(String url,Param[] params,Callback callback) {
        getAsync(url, params, null, callback);
    }

    public void display(final ImageView view, final String url, final int errorResId) {
        displayImage(view, url, errorResId);
    }

    public void uploadFileForm(String url, String fileKey, File[] files,Param[] params,Callback callback) {
        postAsyncUploadFile(url, callback, files, fileKey, params);
    }

    public void downloadFileAsync(final String url, final String filePath,final ResultCallback callback) {
        downloadAsyn(url,filePath,callback);
    }
}
