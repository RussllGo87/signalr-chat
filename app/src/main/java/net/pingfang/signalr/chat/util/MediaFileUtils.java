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

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileNotFoundException;
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

    public static String genarateFilePath(Context context, String type, String albumName, String fileExtension) {
        Calendar c = Calendar.getInstance();
        File fileDir = getAlbumStorageDir(context, type, albumName);
        StringBuilder stringBuilder = new StringBuilder();
        if(!TextUtils.isEmpty(type)) {
            if(type.equals(Environment.DIRECTORY_PICTURES)) {
                stringBuilder.append(GlobalApplication.IMAGE_TITLE_NAME_PREFIX);
            } else if(type.equals(Environment.DIRECTORY_MUSIC)) {
                stringBuilder.append(GlobalApplication.VOICE_FILE_NAME_PREFIX);
            }

            stringBuilder.append(c.get(Calendar.YEAR));
            stringBuilder.append(c.get(Calendar.MONTH) + 1);
            stringBuilder.append(c.get(Calendar.DAY_OF_MONTH));
            stringBuilder.append(c.get(Calendar.HOUR_OF_DAY));
            stringBuilder.append(c.get(Calendar.MINUTE));
            stringBuilder.append(c.get(Calendar.MILLISECOND));

            if(type.equals(Environment.DIRECTORY_PICTURES)) {
                stringBuilder.append(".");
                stringBuilder.append(fileExtension);
            } else if(type.equals(Environment.DIRECTORY_MUSIC)) {
                stringBuilder.append(GlobalApplication.VOICE_FILE_NAME_SUFFIX);
            }

            String fileName = stringBuilder.toString();

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

        return  null;
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


    public static String processReceiveFile(Context context, String fileBody, String fileType,String fileExtension) {
        try {
            StringBuffer buffer = new StringBuffer();
            String filePath = null;
            if(!TextUtils.isEmpty(fileType) && !TextUtils.isEmpty(fileExtension)) {
                if(fileType.equals("IMAGE")) {
                    filePath = genarateFilePath(context, Environment.DIRECTORY_PICTURES, "pic", fileExtension);
                } else if(fileType.equals("AUDIO")) {
                    filePath = genarateFilePath(context, Environment.DIRECTORY_MUSIC, "voice", GlobalApplication.VOICE_FILE_NAME_SUFFIX);
                }

                File file = null;
                if(filePath != null && !TextUtils.isEmpty(filePath)) {
                    file = new File(filePath);
                    if(!file.exists()) {
                        file.createNewFile();
                    }

                    byte[] bitmapArray = Base64.decode(fileBody, Base64.DEFAULT);

                    if(file != null) {
                        FileUtils.writeByteArrayToFile(file, bitmapArray);
                    } else {
                        Log.e("MediaFileUtils", "file not created!");
                    }

                } else {
                    Log.e("MediaFileUtils", "file path error");
                }

                return filePath;
            } else {
                Log.e("MediaFileUtils", "file type error");
                return null;
            }

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

    public static Bitmap decodeBitmapFromRes(Context context,int resId,int reqWidth, int reqHeight) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(context.getResources(), resId,options);

        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(context.getResources(), resId,options);
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
