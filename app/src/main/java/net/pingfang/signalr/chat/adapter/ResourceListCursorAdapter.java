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
import net.pingfang.signalr.chat.database.AdResource;
import net.pingfang.signalr.chat.database.AppContract;
import net.pingfang.signalr.chat.util.ImageUtils;

/**
 * Created by gongguopei87@gmail.com on 2015/12/23.
 */
public class ResourceListCursorAdapter extends CursorAdapter {

    private LayoutInflater cursorInflater;

    public ResourceListCursorAdapter(Context context, Cursor c, int flags) {
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
        String path1 = cursor.getString(cursor.getColumnIndex(AppContract.AdResourceEntry.COLUMN_NAME_RESOURCE_PATH_P1));
        ImageUtils.ImageSize actualImageSize = ImageUtils.getImageSize(path1);
        ImageUtils.ImageSize imageViewSize = ImageUtils.getImageViewSize(iv_ad_profile);
        int inSampleSize = ImageUtils.calculateInSampleSize(actualImageSize, imageViewSize);
        BitmapFactory.Options ops = new BitmapFactory.Options();
        ops.inJustDecodeBounds = false;
        ops.inSampleSize = inSampleSize;
        final Bitmap bm = BitmapFactory.decodeFile(path1, ops);
        iv_ad_profile.setImageBitmap(bm);

        TextView tv_resource_post_time = (TextView) view.findViewById(R.id.tv_resource_post_time);
        String length = cursor.getString(cursor.getColumnIndex(AppContract.AdResourceEntry.COLUMN_NAME_RESOURCE_LENGTH));
        String width = cursor.getString(cursor.getColumnIndex(AppContract.AdResourceEntry.COLUMN_NAME_RESOURCE_WIDTH));
        tv_resource_post_time.setText("长:" + length + " 宽: " + width);

        TextView tv_resource_post_address = (TextView) view.findViewById(R.id.tv_resource_post_address);
        String address = cursor.getString(cursor.getColumnIndex(AppContract.AdResourceEntry.COLUMN_NAME_RESOURCE_ADDRESS));
        tv_resource_post_address.setText(address);

        TextView tv_resource_post_contact = (TextView) view.findViewById(R.id.tv_resource_post_contact);
        String contact = cursor.getString(cursor.getColumnIndex(AppContract.AdResourceEntry.COLUMN_NAME_RESOURCE_CONTACT));
        tv_resource_post_contact.setText(contact);

        TextView tv_resource_post_contact_info = (TextView) view.findViewById(R.id.tv_resource_post_contact_info);
        String phone = cursor.getString(cursor.getColumnIndex(AppContract.AdResourceEntry.COLUMN_NAME_RESOURCE_PHONE));
        tv_resource_post_contact_info.setText(phone);

        int id = cursor.getInt(cursor.getColumnIndex(AppContract.AdResourceEntry._ID));
        String uid = cursor.getString(cursor.getColumnIndex(AppContract.AdResourceEntry.COLUMN_NAME_RESOURCE_UID));
        String material = cursor.getString(cursor.getColumnIndex(AppContract.AdResourceEntry.COLUMN_NAME_RESOURCE_MATERIAL));
        String remark = cursor.getString(cursor.getColumnIndex(AppContract.AdResourceEntry.COLUMN_NAME_RESOURCE_REMARK));
        String lat = cursor.getString(cursor.getColumnIndex(AppContract.AdResourceEntry.COLUMN_NAME_RESOURCE_LAT));
        String lng = cursor.getString(cursor.getColumnIndex(AppContract.AdResourceEntry.COLUMN_NAME_RESOURCE_LNG));
        String path2 = cursor.getString(cursor.getColumnIndex(AppContract.AdResourceEntry.COLUMN_NAME_RESOURCE_PATH_P2));
        String path3 = cursor.getString(cursor.getColumnIndex(AppContract.AdResourceEntry.COLUMN_NAME_RESOURCE_PATH_P3));
        String path4 = cursor.getString(cursor.getColumnIndex(AppContract.AdResourceEntry.COLUMN_NAME_RESOURCE_PATH_P4));
        int status = cursor.getInt(cursor.getColumnIndex(AppContract.AdResourceEntry.COLUMN_NAME_RESOURCE_STATUS));

        AdResource resource = new AdResource(uid);
        resource.setId(id);
        resource.setLength(length);
        resource.setWidth(width);
        resource.setAddress(address);
        resource.setContact(contact);
        resource.setPhone(phone);
        resource.setMaterial(material);
        resource.setRemark(remark);
        resource.setLat(lat);
        resource.setLng(lng);
        resource.setPath1(path1);
        resource.setPath2(path2);
        resource.setPath3(path3);
        resource.setPath4(path4);
        resource.setStatus(status);

        view.setTag(resource);
    }
}
