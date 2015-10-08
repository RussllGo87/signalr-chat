package net.pingfang.signalr.chat.util;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;

/**
 * Created by gongguopei87@gmail.com on 2015/8/5.
 */
public class MediaFileUtils {



    public static File getAlbumStorageDir(Context context, String type, String albumName) {
        // Get the directory for the app's private pictures directory.
        File file = new File(context.getExternalFilesDir(
                type), albumName);
        if(!file.exists()) {
            file.mkdir();
            Log.e("MediaFileUtils", "Directory created");
        }
        return file;
    }

    public static String getVoiceFilePath(Context context, String albumName) {
        Calendar c = Calendar.getInstance();
        File fileDir = getAlbumStorageDir(context, Environment.DIRECTORY_MUSIC, albumName);
        String fileName = new StringBuilder().append(GlobalApplication.VOICE_FILE_NAME_PREFIX)
                .append(c.get(Calendar.YEAR))
                .append(c.get(Calendar.MONTH))
                .append(c.get(Calendar.DAY_OF_MONTH))
                .append(c.get(Calendar.HOUR_OF_DAY))
                .append(c.get(Calendar.MINUTE))
                .append(c.get(Calendar.SECOND))
                .append(c.get(Calendar.MILLISECOND))
                .append(GlobalApplication.VOICE_FILE_NAME_SUFFIX)
                .toString();
        File file = new File(fileDir,fileName);
        if(!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }
        return file.toString();
    }

    public static String getRealPathFromURI(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = { MediaStore.Images.Media.DATA };
            cursor = context.getContentResolver().query(contentUri,  proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public static String getFileExtension(String filePath) {
        if(!TextUtils.isEmpty(filePath)) {
            int lastIndex = filePath.lastIndexOf(".");
            String extension = filePath.substring(lastIndex + 1);
            return extension;
        }
        return null;
    }


    public static String processReceiveFile(Context context, String json, String fileType) {
        try {
            JSONObject jsonObject = new JSONObject(json);
            String fileExtension = jsonObject.getString("fileExtension");
            String fileBody = jsonObject.getString("fileBody");
            StringBuffer buffer = new StringBuffer();

            if(!TextUtils.isEmpty(fileType)) {
                if(fileType.equals("IMAGE")) {
                    buffer.append("IMAGE");
                }
                buffer.append(CommonTools.generateTimestamp());
                buffer.append(".");
                buffer.append(fileExtension);
                String fileName = buffer.toString();

                File path = getAlbumStorageDir(context, Environment.DIRECTORY_PICTURES, "RecievePic");
                File filePath = new File(path,fileName);
                if(!filePath.exists()) {
                    filePath.createNewFile();
                }

                byte[] bitmapArray = Base64.decode(fileBody, Base64.DEFAULT);

                FileOutputStream fileOutputStream = new FileOutputStream(filePath);
                fileOutputStream.write(bitmapArray);
                fileOutputStream.close();

                return filePath.getAbsolutePath();
            } else {
                Log.e("MediaFileUtils", "file type error");
                return null;
            }

        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("MediaFileUtils", "json errors");
            return null;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.e("MediaFileUtils", "file not found errors");
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("MediaFileUtils", "io error");
            return null;
        }
    }



    public static Bitmap decodeBitmapFromPath(String filePath,int reqWidth, int reqHeight) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath,options);

        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(filePath,options);

    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    public static int dpToPx(Context context, int dp)
    {
        float density = context.getResources().getDisplayMetrics().density;
        return Math.round((float)dp * density);
    }


}
