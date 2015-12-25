package net.pingfang.signalr.chat.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import org.apache.commons.io.FileUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by gongguopei87@gmail.com on 2015/8/21.
 */
public class CommonTools {

    public static boolean isPhoneNumber(String phoneNumber) {
        if(!TextUtils.isEmpty(phoneNumber)) {
            String regex = "((^(13|15|18)[0-9]{9}$)|(^0[1,2]{1}\\d{1}-?\\d{8}$)|(^0[3-9] {1}\\d{2}-?\\d{7,8}$)|(^0[1,2]{1}\\d{1}-?\\d{8}-(\\d{1,4})$)|(^0[3-9]{1}\\d{2}-? \\d{7,8}-(\\d{1,4})$))";
            Pattern p = Pattern.compile(regex);
            Matcher matcher = p.matcher(phoneNumber);
            return matcher.find();
        } else {
            return false;
        }
    }

    public static boolean isValidPwd(String password) {
        if(TextUtils.isEmpty(password)) {
            return false;
        }

        return !(password.length() < 5 || password.length() > 16);
    }

    public static boolean isAvailableVc(String vc) {
        return !TextUtils.isEmpty(vc) && TextUtils.isDigitsOnly(vc) && vc.length() == 6;
    }

    public static boolean isAvailableEmail(String email) {
        if(!TextUtils.isEmpty(email)) {
            String regex = "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$";
            Pattern p = Pattern.compile(regex,Pattern.CASE_INSENSITIVE);
            Matcher matcher = p .matcher(email);
            return matcher.find();
        } else {
            return false;
        }
    }

    public static boolean checkRegParams(String... args) {
        String nick = args[0];
        String password = args[1];
        return !TextUtils.isEmpty(nick) && !TextUtils.isEmpty(password);
    }

    public static boolean checkUrl(String url) {
        String reg = "^(http|https|ftp)\\://([a-zA-Z0-9\\.\\-]+(\\:[a-zA-Z0-9\\.&amp;%\\$\\-]+)*@)?" +
                "((25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[1-9])\\.(25[0-5]|2[0-4][0-9]" +
                "|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[1-9]|0)\\.(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|" +
                "[1-9]{1}[0-9]{1}|[1-9]|0)\\.(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|" +
                "[0-9])|([a-zA-Z0-9\\-]+\\.)*[a-zA-Z0-9\\-]+\\.[a-zA-Z]{2,4})(\\:[0-9]+)?" +
                "(/[^/][a-zA-Z0-9\\.\\,\\?\\'\\\\/\\+&amp;%\\$#\\=~_\\-@]*)*$";
        Pattern p = Pattern.compile(reg);
        Matcher m = p.matcher(url);
        return m.matches();
    }

    public static Bitmap convertToGrayscale(Bitmap bitmap) {
        int imageHeight = bitmap.getHeight();
        int imageWidth = bitmap.getWidth();

        float[] arrayForColorMatrix = new float[] {0, 0, 1, 0, 0,
                0, 0, 1, 0, 0,
                0, 0, 1, 0, 0,
                0, 0, 0, 1, 0};

        Bitmap.Config config = bitmap.getConfig();
        Bitmap grayScaleBitmap = Bitmap.createBitmap(imageHeight, imageWidth, config);

        Canvas c = new Canvas(grayScaleBitmap);
        Paint paint = new Paint();

        ColorMatrix matrix = new ColorMatrix(arrayForColorMatrix);
        matrix.setSaturation(0);

        ColorMatrixColorFilter filter = new ColorMatrixColorFilter(matrix);
        paint.setColorFilter(filter);

        c.drawBitmap(bitmap, 0, 0, paint);

        return grayScaleBitmap;
    }

    /**
     * 将bitmap转换成Base64格式字符串
     * 格式化byte
     *
     * @param bitmap
     * @return
     */
    public static String bitmapToBase64(Bitmap bitmap) {

        String result = null;
        ByteArrayOutputStream baos = null;
        try {
            if (bitmap != null) {
                baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);

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

    public static String fileToBase64(String filePath) {

        String result = null;

        if(TextUtils.isEmpty(filePath)) {
            return result;
        }

        File file = new File(filePath);
        if(!file.exists()) {
            return result;
        }

        try {
            byte[] fileToByteArray = FileUtils.readFileToByteArray(file);
            result = Base64.encodeToString(fileToByteArray,Base64.DEFAULT);
        } catch (IOException e) {
            Log.e("CommonTools", "IOException");
            e.printStackTrace();
        }

        return result;
    }


    public static String imagePath2Base64(String sourceFilePath){
        try{
            int dh = 1024;
            int dw = 768;
            BitmapFactory.Options factory = new BitmapFactory.Options();
            factory.inJustDecodeBounds = true;
            InputStream is = new FileInputStream(sourceFilePath);
            BitmapFactory.decodeStream(is, null, factory);
            int hRatio = (int)Math.ceil(factory.outHeight / (float)dh);
            int wRatio = (int)Math.ceil(factory.outWidth / (float)dw);

            if(hRatio > 1|| wRatio>1){
                if(hRatio> wRatio){
                    factory.inSampleSize = hRatio;
                }
                else
                    factory.inSampleSize = wRatio;
            }
            factory.inJustDecodeBounds = false;
            is.close();
            is = new FileInputStream(sourceFilePath);
            Bitmap bmp = BitmapFactory.decodeStream(is, null, factory);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bmp.compress(Bitmap.CompressFormat.JPEG, 80, baos);
            baos.flush();
            baos.close();

            byte[] bitmapBytes = baos.toByteArray();
            return Base64.encodeToString(bitmapBytes, Base64.DEFAULT);

        }catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }
}
