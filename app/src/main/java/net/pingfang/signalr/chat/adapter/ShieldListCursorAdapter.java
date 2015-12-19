package net.pingfang.signalr.chat.adapter;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import net.pingfang.signalr.chat.R;
import net.pingfang.signalr.chat.database.AppContract;
import net.pingfang.signalr.chat.database.User;
import net.pingfang.signalr.chat.net.OkHttpCommonUtil;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by gongguopei87@gmail.com on 2015/10/29.
 */
public class ShieldListCursorAdapter extends CursorAdapter {

    private LayoutInflater cursorInflater;

    public ShieldListCursorAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
        cursorInflater = (LayoutInflater) context.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return cursorInflater.inflate(R.layout.list_item_shield,parent,false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        String uid = cursor.getString(cursor.getColumnIndex(AppContract.UserStatusView.COLUMN_NAME_UID));
        String nickname = cursor.getString(cursor.getColumnIndex(AppContract.UserStatusView.COLUMN_NAME_NICKNAME));
        String portraitUrl = cursor.getString(cursor.getColumnIndex(AppContract.UserStatusView.COLUMN_NAME_PORTRAIT));

        CircleImageView iv_shield_portrait = (CircleImageView) view.findViewById(R.id.iv_shield_portrait);
        if(!TextUtils.isEmpty(portraitUrl)) {
            OkHttpCommonUtil okHttpCommonUtil = OkHttpCommonUtil.newInstance(context);
            okHttpCommonUtil.display(iv_shield_portrait, portraitUrl, R.drawable.hale_default_user_portrait);
        } else {
            iv_shield_portrait.setImageResource(R.drawable.hale_default_user_portrait);
        }

        TextView tv_shield_nickname = (TextView) view.findViewById(R.id.tv_shield_nickname);
        tv_shield_nickname.setText(nickname);

        User user = new User(uid, nickname, portraitUrl);
        view.setTag(user);
    }
}
