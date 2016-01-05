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
    private int uidIndex;
    private int nickNameIndex;
    private int portraitIndex;
    private int genderIndex;
    private int distanceIndex;
    private int remarkIndex;
    private int messageListIndex;
    private int nearbyListIndex;

    public BuddyListCursorAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
        cursorInflater = (LayoutInflater) context.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        uidIndex = c.getColumnIndex(AppContract.UserEntry.COLUMN_NAME_ENTRY_UID);
        nickNameIndex = c.getColumnIndex(AppContract.UserEntry.COLUMN_NAME_NICK_NAME);
        portraitIndex = c.getColumnIndex(AppContract.UserEntry.COLUMN_NAME_PORTRAIT);
        genderIndex = c.getColumnIndex(AppContract.UserEntry.COLUMN_NAME_GENDER);
        distanceIndex = c.getColumnIndex(AppContract.UserEntry.COLUMN_NAME_DISTANCE);
        remarkIndex = c.getColumnIndex(AppContract.UserEntry.COLUMN_NAME_REMARK);
        messageListIndex = c.getColumnIndex(AppContract.UserEntry.COLUMN_NAME_STATUS_MSG_LIST);
        nearbyListIndex = c.getColumnIndex(AppContract.UserEntry.COLUMN_NAME_STATUS_NEARBY_LIST);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = cursorInflater.inflate(R.layout.list_item_buddy,null);
        ViewDataHolder viewHolder = new ViewDataHolder();
        viewHolder.iv_user_portrait = (CircleImageView) view.findViewById(R.id.iv_user_portrait);
        viewHolder.tv_user_nickname = (TextView) view.findViewById(R.id.tv_user_nickname);
        viewHolder.iv_user_gender = (ImageView) view.findViewById(R.id.iv_user_gender);
        viewHolder.tv_user_distance = (TextView) view.findViewById(R.id.tv_user_distance);
        view.setTag(viewHolder);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        ViewDataHolder viewDataHolder = (ViewDataHolder) view.getTag();
        String portraitUrl = cursor.getString(portraitIndex);
        if(portraitUrl != null && !TextUtils.isEmpty(portraitUrl) && !"null".equals(portraitUrl)) {
            OkHttpCommonUtil okHttpCommonUtil = OkHttpCommonUtil.newInstance(context);
            okHttpCommonUtil.display(viewDataHolder.iv_user_portrait, portraitUrl, R.drawable.hale_default_user_portrait);
        } else {
            viewDataHolder.iv_user_portrait.setImageResource(R.drawable.hale_default_user_portrait);
        }

        String nickname = cursor.getString(nickNameIndex);
        viewDataHolder.tv_user_nickname.setText(nickname);

        int gender = cursor.getInt(genderIndex);
        if (gender == User.USER_GENDER_MALE) {
            viewDataHolder.iv_user_gender.setImageResource(R.drawable.icon_hale_male);
        } else {
            viewDataHolder.iv_user_gender.setImageResource(R.drawable.icon_hale_female);
        }

        String distance = cursor.getString(distanceIndex);
        if(!TextUtils.isEmpty(distance)) {
            viewDataHolder.tv_user_distance.setText("");
        }
        viewDataHolder.tv_user_distance.setText(context.getResources().getString(R.string.tv_user_distance, distance));

        viewDataHolder.uid = cursor.getString(uidIndex);
        viewDataHolder.nickname = nickname;
        viewDataHolder.portraitUrl = portraitUrl;
        viewDataHolder.gender = gender;
        viewDataHolder.distance = distance;
        viewDataHolder.remark = cursor.getString(remarkIndex);
        viewDataHolder.msgListStatus = cursor.getInt(messageListIndex);
        viewDataHolder.nearbyStatus = cursor.getInt(nearbyListIndex);

        view.setTag(viewDataHolder);
    }

    public static class ViewDataHolder {

        String uid;
        String nickname;
        String portraitUrl;
        int gender;
        String distance;
        String remark;
        int msgListStatus;
        int nearbyStatus;

        public String getUid() {
            return uid;
        }

        public String getNickname() {
            return nickname;
        }

        public String getPortraitUrl() {
            return portraitUrl;
        }

        public int getGender() {
            return gender;
        }

        public String getDistance() {
            return distance;
        }

        public String getRemark() {
            return remark;
        }

        public int getMsgListStatus() {
            return msgListStatus;
        }

        public int getNearbyStatus() {
            return nearbyStatus;
        }

        CircleImageView iv_user_portrait;
        TextView tv_user_nickname;
        ImageView iv_user_gender;
        TextView tv_user_distance;


    }
}
