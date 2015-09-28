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
import net.pingfang.signalr.chat.net.OkHttpCommonUtil;

/**
 * Created by gongguopei87@gmail.com on 2015/9/28.
 */
public class ListCursorAdapter extends CursorAdapter {

    private LayoutInflater cursorInflater;

    public ListCursorAdapter(Context context, Cursor c, int flags) {
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
        ImageView iv_user_portrait = (ImageView) view.findViewById(R.id.iv_user_portrait);
        String portraitUrl = cursor.getString(cursor.getColumnIndex(AppContract.UserEntry.COLUMN_NAME_PORTRAIT));
        if(portraitUrl != null && !TextUtils.isEmpty(portraitUrl) && !"null".equals(portraitUrl)) {
            OkHttpCommonUtil okHttpCommonUtil = OkHttpCommonUtil.newInstance(context);
            okHttpCommonUtil.display(iv_user_portrait,portraitUrl,R.mipmap.ic_launcher);
        } else {
            iv_user_portrait.setImageResource(R.mipmap.ic_launcher);
        }

        TextView tv_user_nickname = (TextView) view.findViewById(R.id.tv_user_nickname);
        tv_user_nickname.setText(cursor.getString(cursor.getColumnIndex(AppContract.UserEntry.COLUMN_NAME_NICK_NAME)));

        String uid = cursor.getString(cursor.getColumnIndex(AppContract.UserEntry.COLUMN_NAME_ENTRY_UID));

        UserHolder userHolder = new UserHolder();
        userHolder.uid = uid;
        userHolder.nickname = cursor.getString(cursor.getColumnIndex(AppContract.UserEntry.COLUMN_NAME_NICK_NAME));
        userHolder.portrait = cursor.getString(cursor.getColumnIndex(AppContract.UserEntry.COLUMN_NAME_PORTRAIT));
        view.setTag(userHolder);
    }

    public static class UserHolder {
        private String uid;
        private String nickname;
        private String portrait;

        public String getUid() {
            return uid;
        }

        public String getNickname() {
            return nickname;
        }

        public String getPortrait() {
            return portrait;
        }
    }
}
