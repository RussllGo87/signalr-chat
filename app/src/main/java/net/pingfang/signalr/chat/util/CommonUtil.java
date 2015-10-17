package net.pingfang.signalr.chat.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore.Images.ImageColumns;
import android.util.Base64;
import android.util.Log;

public class CommonUtil {
    /**
     * 防止按钮重复点击
     */

    private static long lastClickTime;
    public synchronized static boolean isFastClick() {
        long time = System.currentTimeMillis();
        if ( time - lastClickTime < 1000) {
            return true;
        }
        lastClickTime = time;
        return false;
    }

    //邮箱验证
    public static boolean EmailUtil(String str){
        Pattern pattern = Pattern.compile("^([a-zA-Z0-9_\\-\\.]+)@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.)|(([a-zA-Z0-9\\-]+\\.)+))([a-zA-Z]{2,4}|[0-9]{1,3})(\\]?)$");
        Matcher matcher = pattern.matcher(str);
        //System.out.println("邮箱的验证结果："+matcher.matches());
        return matcher.matches();
    }
    //判斷電話號碼是否正確
    public static boolean phoneUtil(String str){
        Pattern p = Pattern.compile("^((13[0-9])|(15[^4,\\D])|(18[0,5-9]))\\d{8}$");
        Matcher m = p.matcher(str);
        System.out.println("手机号码的验证结果："+m.matches());
        return m.matches();

    }
    //判断网址是否正确
    public static boolean UrlUtil(String str){
        Pattern p = Pattern.compile("^(\\w+)://([^/:]+)(:\\d*)?([^#\\s]*)$");
        Matcher m = p.matcher(str);
        System.out.println("手机号码的验证结果："+m.matches());
        return m.matches();

    }
    /**
     * 判断网络是否连接
     *
     * @param context
     * @return
     */
    public static boolean isConnected(Context context) {
        ConnectivityManager connectivity = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        if (null != connectivity) {
            NetworkInfo info = connectivity.getActiveNetworkInfo();
            if (null != info && info.isConnected()) {
                if (info.getState() == NetworkInfo.State.CONNECTED) {
                    return true;
                }
            }
        }
        return false;
    }



    /**
     * 格式化byte
     * 将bitmap转换成base64字符串
     * @param b
     * @return
     */
    public static String bitmapToBase64(Bitmap bitmap) {

        String result = null;
        ByteArrayOutputStream baos = null;
        try {
            if (bitmap != null) {
                baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                baos.flush();
                baos.close();
                byte[] bitmapBytes = baos.toByteArray();
                result = Base64.encodeToString(bitmapBytes, Base64.DEFAULT);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (baos != null) {
                    baos.flush();
                    baos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    /**
     *
     * @return
     * true表示 有sdcard false表示没有sdcard
     */
    public static boolean hasSdcard() {
        String state = Environment.getExternalStorageState();
        if (state.equals(Environment.MEDIA_MOUNTED)) {
            return true;
        } else {
            return false;
        }
    }



    /**
     * 获取SD卡路径
     * @return
     */
    public static String getSDPath() {
        String sdPath = null;
        // 判断sd卡是否存在
        boolean sdCardExit = Environment.getExternalStorageState().equals(
                android.os.Environment.MEDIA_MOUNTED);
        if (sdCardExit) {
            // 获取根目录
            sdPath = Environment.getExternalStorageDirectory().toString();
        }
        return sdPath;
    }

    /**
     * 返回32位UUID字符串
     * @return
     */
    public static String getUUID32(){
        UUID uuid = UUID.randomUUID();
        return uuid.toString().replaceAll("-", "");
    }
    /**
     * 将uri转换成路径地址
     *
     * @param context
     * @param uri
     * @return the file path or null
     */
    public static String getRealFilePath( final Context context, final Uri uri ) {
        if ( null == uri ) return null;
        final String scheme = uri.getScheme();
        String data = null;
        if ( scheme == null )
            data = uri.getPath();
        else if ( ContentResolver.SCHEME_FILE.equals( scheme ) ) {
            data = uri.getPath();
        } else if ( ContentResolver.SCHEME_CONTENT.equals( scheme ) ) {
            Cursor cursor = context.getContentResolver().query( uri, new String[] { ImageColumns.DATA }, null, null, null );
            if ( null != cursor ) {
                if ( cursor.moveToFirst() ) {
                    int index = cursor.getColumnIndex( ImageColumns.DATA );
                    if ( index > -1 ) {
                        data = cursor.getString( index );
                    }
                }
                cursor.close();
            }
        }
        return data;
    }

    /**
     * 加载本地图片
     * @param url
     * @return
     */
    public static Bitmap getBitmapInLocal(String path) {
        try {
            FileInputStream fis = new FileInputStream(path);
            return BitmapFactory.decodeStream(fis);  ///把流转化为Bitmap图片
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 删除文件
     * @param filePath
     */
    public static void deleteFile(String filePath){
        if(filePath == null || "".equals(filePath)){
            return;
        }
        File file = new File(filePath);
        if(file.exists()){
            file.delete();
        }
    }


    /**
     * 判断文件夹是否存在如果不存在则生成文件夹
     * @param filePath
     */
    public static void makeRootDirectory(String filePath) {
        File file = null;
        try {
            file = new File(filePath);
            if (!file.exists()) {
                file.mkdir();
            }
        } catch (Exception e) {
            Log.i("error:", e + "");
        }
    }

    /**
     * 判断文件是否存在
     * @param str
     * @return
     */
    public static boolean fileIsExists(String str){
        try{
            File f=new File(str);
            if(!f.exists()){
                return false;
            }

        }catch (Exception e) {
            // TODO: handle exception
            return false;
        }
        return true;
    }

    /**101.200.75.158
     * 图片文件进行压缩处理
     * @param sourceFilePath
     * @param targetFilePath
     * @return
     */
    public static boolean dealImage(String sourceFilePath, String targetFilePath){
        try{
            int dh = 1024;
            int dw = 768;
            BitmapFactory.Options factory=new BitmapFactory.Options();
            factory.inJustDecodeBounds=true; //当为true时  允许查询图片不为 图片像素分配内存
            InputStream is = new FileInputStream(sourceFilePath);
            Bitmap bmp = BitmapFactory.decodeStream(is,null,factory);
            int hRatio=(int)Math.ceil(factory.outHeight/(float)dh); //图片是高度的几倍
            int wRatio=(int)Math.ceil(factory.outWidth/(float)dw); //图片是宽度的几倍
            System.out.println("hRatio:"+hRatio+"  wRatio:"+wRatio);
            //缩小到  1/ratio的尺寸和 1/ratio^2的像素
            if(hRatio>1||wRatio>1){
                if(hRatio>wRatio){
                    factory.inSampleSize=hRatio;
                }
                else
                    factory.inSampleSize=wRatio;
            }
            factory.inJustDecodeBounds=false;
            is.close();
            is = new FileInputStream(sourceFilePath);
            bmp=BitmapFactory.decodeStream(is, null, factory);
            OutputStream outFile = new FileOutputStream(targetFilePath);
            bmp.compress(Bitmap.CompressFormat.JPEG, 60, outFile);
            outFile.close();
        }catch(Exception e){
            e.printStackTrace();
            return false;
        }
        return true;
    }

}
