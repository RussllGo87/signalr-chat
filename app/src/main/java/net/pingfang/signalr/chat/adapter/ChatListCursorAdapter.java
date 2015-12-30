package net.pingfang.signalr.chat.adapter;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import net.pingfang.signalr.chat.R;
import net.pingfang.signalr.chat.database.AppContract;
import net.pingfang.signalr.chat.database.User;
import net.pingfang.signalr.chat.net.OkHttpCommonUtil;
import net.pingfang.signalr.chat.util.DateTimeUtil;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by gongguopei87@gmail.com on 2015/10/15.
 */
public class ChatListCursorAdapter extends CursorAdapter {
    Context context;
    private LayoutInflater cursorInflater;

    public ChatListCursorAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
        this.context = context;
        cursorInflater = (LayoutInflater) context.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return cursorInflater.inflate(R.layout.list_item_message, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        CircleImageView iv_account_portrait = (CircleImageView) view.findViewById(R.id.iv_account_portrait);
        String portraitUrl = cursor.getString(cursor.getColumnIndex(AppContract.RecentContactView.COLUMN_NAME_PORTRAIT));
        if (portraitUrl != null && !TextUtils.isEmpty(portraitUrl) && !"null".equals(portraitUrl)) {
//            portraitUrl = GlobalApplication.PORTRAIT_URL_PREFIX + portraitUrl;
            OkHttpCommonUtil okHttpCommonUtil = OkHttpCommonUtil.newInstance(context);
            okHttpCommonUtil.display(iv_account_portrait, portraitUrl, R.drawable.hale_default_user_portrait);
        } else {
            iv_account_portrait.setImageResource(R.drawable.hale_default_user_portrait);
        }

        TextView tv_friends_name = (TextView) view.findViewById(R.id.tv_friends_name);
        String remark = cursor.getString(cursor.getColumnIndex(AppContract.RecentContactView.COLUMN_NAME_STATUS_REMARK));
        String nickname = cursor.getString(cursor.getColumnIndex(AppContract.RecentContactView.COLUMN_NAME_NICKNAME));
        if(!TextUtils.isEmpty(remark)) {
            tv_friends_name.setText(remark + "(" + nickname + ")" );
        } else {
            tv_friends_name.setText(nickname);
        }

        TextView tv_message_update = (TextView) view.findViewById(R.id.tv_message_update);
        String content = cursor.getString(cursor.getColumnIndex(AppContract.RecentContactView.COLUMN_NAME_CONTENT));
        if (!TextUtils.isEmpty(content) && content.length() > 10) {
            content = content.substring(0, 10);
            content = content.concat("...");
        }
        tv_message_update.setText(content);

        TextView tv_msg_update_time = (TextView) view.findViewById(R.id.tv_msg_update_time);
        String datetime = cursor.getString(cursor.getColumnIndex(AppContract.RecentContactView.COLUMN_NAME_UPDATE_TIME));
        int timeFlag = DateTimeUtil.convertDatetimeFormat(datetime);
        datetime = DateTimeUtil.displayDateOrTime(context, datetime, timeFlag);
        tv_msg_update_time.setText(datetime);

        TextView tv_status_shield = (TextView) view.findViewById(R.id.tv_status_shield);
        int statusShield = cursor.getInt(cursor.getColumnIndex(AppContract.RecentContactView.COLUMN_NAME_STATUS_SHIELD));
        if(statusShield == User.USER_STATUS_SHIELD_LIST_OUT) {
            tv_status_shield.setVisibility(View.GONE);
        } else {
            tv_status_shield.setVisibility(View.VISIBLE);
        }

        TextView tv_msg_not_read = (TextView) view.findViewById(R.id.tv_msg_not_read);
        int count = cursor.getInt(cursor.getColumnIndex(AppContract.RecentContactView.COLUMN_NAME_COUNT));
        if(count > 0) {
            tv_msg_not_read.setVisibility(View.VISIBLE);
            if (count > 99) {
                tv_msg_not_read.setText("99+");
            } else {
                tv_msg_not_read.setText(count + "");
            }

        } else {
            tv_msg_not_read.setVisibility(View.GONE);
        }

        int msgListStatus = cursor.getInt(cursor.getColumnIndex(AppContract.RecentContactView.COLUMN_NAME_STATUS_MSG));
        if(msgListStatus == User.USER_STATUS_MSG_LIST_IN) {
            Log.d("ChatListCursorAdapter", "ChatListCursorAdapter bindView msgListStatus == USER_STATUS_MSG_LIST_IN");
        } else {
            Log.d("ChatListCursorAdapter", "ChatListCursorAdapter bindView msgListStatus == USER_STATUS_MSG_LIST_OUT");
        }
        String uid = cursor.getString(cursor.getColumnIndex(AppContract.RecentContactView.COLUMN_NAME_UID));

        User user = new User(uid, nickname, portraitUrl);
        view.setTag(user);
    }
}
