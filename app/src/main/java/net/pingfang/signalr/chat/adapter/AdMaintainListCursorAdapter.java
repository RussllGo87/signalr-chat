package net.pingfang.signalr.chat.adapter;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import net.pingfang.signalr.chat.R;
import net.pingfang.signalr.chat.database.Advertisement;
import net.pingfang.signalr.chat.database.AppContract;
import net.pingfang.signalr.chat.util.ImageUtils;

/**
 * Created by gongguopei87@gmail.com on 2015/12/10.
 */
public class AdMaintainListCursorAdapter extends CursorAdapter {

    private LayoutInflater cursorInflater;

    public AdMaintainListCursorAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
        cursorInflater = (LayoutInflater) context.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return cursorInflater.inflate(R.layout.list_item_resource, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ImageView iv_ad_profile = (ImageView) view.findViewById(R.id.iv_resource_profile);
        String path1 = cursor.getString(cursor.getColumnIndex(AppContract.AdvertisementEntry.COLUMN_NAME_AD_PATH_P1));
        ImageUtils.ImageSize actualImageSize = ImageUtils.getImageSize(path1);
        ImageUtils.ImageSize imageViewSize = ImageUtils.getImageViewSize(iv_ad_profile);
        int inSampleSize = ImageUtils.calculateInSampleSize(actualImageSize, imageViewSize);
        BitmapFactory.Options ops = new BitmapFactory.Options();
        ops.inJustDecodeBounds = false;
        ops.inSampleSize = inSampleSize;
        final Bitmap bm = BitmapFactory.decodeFile(path1, ops);
        iv_ad_profile.setImageBitmap(bm);

        TextView tv_ad_code = (TextView) view.findViewById(R.id.tv_resource_post_time);
        String code = cursor.getString(cursor.getColumnIndex(AppContract.AdvertisementEntry.COLUMN_NAME_AD_CODE));
        tv_ad_code.setText("广告编号: " + code);

        TextView tv_ad_post_address = (TextView) view.findViewById(R.id.tv_resource_post_address);
        String address = cursor.getString(cursor.getColumnIndex(AppContract.AdvertisementEntry.COLUMN_NAME_AD_ADDRESS));
        tv_ad_post_address.setText("所在地: " + address);

        TextView tv_ad_remark = (TextView) view.findViewById(R.id.tv_resource_post_contact);
        String remark = cursor.getString(cursor.getColumnIndex(AppContract.AdvertisementEntry.COLUMN_NAME_AD_REMARK));
        tv_ad_remark.setText("备注说明: " + remark);

        TextView tv_resource_post_contact_info = (TextView) view.findViewById(R.id.tv_resource_post_contact_info);
        tv_resource_post_contact_info.setVisibility(View.GONE);

        int id = cursor.getInt(cursor.getColumnIndex(AppContract.AdvertisementEntry._ID));
        String uid = cursor.getString(cursor.getColumnIndex(AppContract.AdvertisementEntry.COLUMN_NAME_AD_UID));
        String length = cursor.getString(cursor.getColumnIndex(AppContract.AdvertisementEntry.COLUMN_NAME_AD_LENGTH));
        String width = cursor.getString(cursor.getColumnIndex(AppContract.AdvertisementEntry.COLUMN_NAME_AD_WIDTH));
        String lat = cursor.getString(cursor.getColumnIndex(AppContract.AdvertisementEntry.COLUMN_NAME_AD_LAT));
        String lng = cursor.getString(cursor.getColumnIndex(AppContract.AdvertisementEntry.COLUMN_NAME_AD_LNG));
        String path2 = cursor.getString(cursor.getColumnIndex(AppContract.AdvertisementEntry.COLUMN_NAME_AD_PATH_P2));
        String path3 = cursor.getString(cursor.getColumnIndex(AppContract.AdvertisementEntry.COLUMN_NAME_AD_PATH_P3));
        String path4 = cursor.getString(cursor.getColumnIndex(AppContract.AdvertisementEntry.COLUMN_NAME_AD_PATH_P4));
        int status = cursor.getInt(cursor.getColumnIndex(AppContract.AdvertisementEntry.COLUMN_NAME_AD_STATUS));

        Advertisement advertisement = new Advertisement(uid);
        advertisement.setId(id);
        advertisement.setUid(uid);
        advertisement.setAddress(address);
        advertisement.setCode(code);
        advertisement.setLength(length);
        advertisement.setWidth(width);
        advertisement.setRemark(remark);
        advertisement.setLat(lat);
        advertisement.setLng(lng);
        advertisement.setPath1(path1);
        advertisement.setPath2(path2);
        advertisement.setPath3(path3);
        advertisement.setPath4(path4);
        advertisement.setStatus(status);

        view.setTag(advertisement);
    }
}
