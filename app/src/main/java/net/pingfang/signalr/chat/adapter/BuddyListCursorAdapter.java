package net.pingfang.signalr.chat.adapter;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import net.pingfang.signalr.chat.R;
import net.pingfang.signalr.chat.database.AppContract;
import net.pingfang.signalr.chat.database.User;
import net.pingfang.signalr.chat.net.OkHttpCommonUtil;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by gongguopei87@gmail.com on 2015/9/28.
 */
public class BuddyListCursorAdapter extends CursorAdapter {

    private LayoutInflater cursorInflater;

    public BuddyListCursorAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
        cursorInflater = (LayoutInflater) context.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return cursorInflater.inflate(R.layout.list_item_buddy,parent,false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        CircleImageView iv_user_portrait = (CircleImageView) view.findViewById(R.id.iv_user_portrait);
        String portraitUrl = cursor.getString(cursor.getColumnIndex(AppContract.UserEntry.COLUMN_NAME_PORTRAIT));
        if(portraitUrl != null && !TextUtils.isEmpty(portraitUrl) && !"null".equals(portraitUrl)) {
//            portraitUrl = GlobalApplication.PORTRAIT_URL_PREFIX + portraitUrl;
            OkHttpCommonUtil okHttpCommonUtil = OkHttpCommonUtil.newInstance(context);
            okHttpCommonUtil.display(iv_user_portrait, portraitUrl, R.drawable.hale_default_user_portrait);
        } else {
            iv_user_portrait.setImageResource(R.drawable.hale_default_user_portrait);
        }

        TextView tv_user_nickname = (TextView) view.findViewById(R.id.tv_user_nickname);
        tv_user_nickname.setText(cursor.getString(cursor.getColumnIndex(AppContract.UserEntry.COLUMN_NAME_NICK_NAME)));

        ImageView iv_user_gener = (ImageView) view.findViewById(R.id.iv_user_gender);
        int gender = cursor.getInt(cursor.getColumnIndex(AppContract.UserEntry.COLUMN_NAME_GENDER));
        if (gender == User.USER_GENDER_MALE) {
            iv_user_gener.setImageResource(R.drawable.icon_hale_male);
        } else {
            iv_user_gener.setImageResource(R.drawable.icon_hale_female);
        }

        TextView tv_user_distance = (TextView) view.findViewById(R.id.tv_user_distance);
        String distance = cursor.getString(cursor.getColumnIndex(AppContract.UserEntry.COLUMN_NAME_DISTANCE));
        tv_user_distance.setText(context.getResources().getString(R.string.tv_user_distance, distance));

        String uid = cursor.getString(cursor.getColumnIndex(AppContract.UserEntry.COLUMN_NAME_ENTRY_UID));
        String nickname = cursor.getString(cursor.getColumnIndex(AppContract.UserEntry.COLUMN_NAME_NICK_NAME));
        String remark = cursor.getString(cursor.getColumnIndex(AppContract.UserEntry.COLUMN_NAME_REMARK));
        int msgListStatus = cursor.getInt(cursor.getColumnIndex(AppContract.UserEntry.COLUMN_NAME_STATUS_MSG_LIST));
        int nearbyStatus = cursor.getInt(cursor.getColumnIndex(AppContract.UserEntry.COLUMN_NAME_STATUS_NEARBY_LIST));

        User user = new User(uid, nickname, portraitUrl, remark, gender,
                msgListStatus, nearbyStatus, 0, distance);
        view.setTag(user);
    }

}
