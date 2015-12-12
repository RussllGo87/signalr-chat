package net.pingfang.signalr.chat.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import net.pingfang.signalr.chat.R;
import net.pingfang.signalr.chat.activity.ChatActivity;
import net.pingfang.signalr.chat.adapter.ChatListCursorAdapter;
import net.pingfang.signalr.chat.constant.app.AppConstants;
import net.pingfang.signalr.chat.database.AppContract;
import net.pingfang.signalr.chat.database.User;
import net.pingfang.signalr.chat.database.UserManager;
import net.pingfang.signalr.chat.listener.OnFragmentInteractionListener;
import net.pingfang.signalr.chat.util.GlobalApplication;
import net.pingfang.signalr.chat.util.SharedPreferencesHelper;


public class MessageFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{

    private static final int LOADER_ID = 0x02;
    SharedPreferencesHelper sharedPreferencesHelper;
    MessageReceiver receiver;
    private OnFragmentInteractionListener mListener;
    private ListView mListView;
    private ChatListCursorAdapter chatListCursorAdapter;

    public static MessageFragment newInstance(OnFragmentInteractionListener mListener) {
        MessageFragment fragment = new MessageFragment();
        fragment.mListener = mListener;
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sharedPreferencesHelper = SharedPreferencesHelper.newInstance(getContext());
        registerReceiver();

        String uid = sharedPreferencesHelper.getStringValue(AppConstants.KEY_SYS_CURRENT_UID);
        Bundle args = new Bundle();
        args.putString(AppConstants.KEY_SYS_CURRENT_UID, uid);
        getLoaderManager().initLoader(LOADER_ID, args, this);
    }

    public void registerReceiver() {
        receiver = new MessageReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(GlobalApplication.ACTION_INTENT_SHIELD_LIST_ADD);
        filter.addAction(GlobalApplication.ACTION_INTENT_SHIELD_LIST_BEFORE);
        filter.addAction(GlobalApplication.ACTION_INTENT_MSG_LIST_UPDATE);
        filter.addAction(GlobalApplication.ACTION_INTENT_MSG_UPDATE);
        filter.addAction(GlobalApplication.ACTION_INTENT_ONLINE_MESSAGE_INCOMING);
        filter.addAction(GlobalApplication.ACTION_INTENT_ONLINE_MESSAGE_SEND);
        filter.addAction(GlobalApplication.ACTION_INTENT_OFFLINE_MESSAGE_LIST_COUNT_UPDATE);
        filter.addAction(GlobalApplication.ACTION_INTENT_OFFLINE_USER_LIST_INCOMING);
        filter.addAction(GlobalApplication.ACTION_INTENT_BULK_MESSAGE_INCOMING);
        getContext().registerReceiver(receiver, filter);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list_message, container, false);

        mListView = (ListView) view.findViewById(android.R.id.list);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        chatListCursorAdapter = new ChatListCursorAdapter(getContext(),null, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
        mListView.setAdapter(chatListCursorAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                User user = (User) view.getTag();

                Intent intent = new Intent();
                intent.setClass(getContext(), ChatActivity.class);
                intent.putExtra("user", user);
                startActivity(intent);
            }
        });
        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                User user = (User) view.getTag();
                if (mListener != null) {
                    //                    mListener.shield(user);
                    mListener.onMsgItemLongClick(position, user);
                    return true;
                }
                return false;
            }
        });


    }

    @Override
    public void onStart() {
        super.onStart();

        String uid = sharedPreferencesHelper.getStringValue(AppConstants.KEY_SYS_CURRENT_UID);
        Bundle args = new Bundle();
        args.putString(AppConstants.KEY_SYS_CURRENT_UID, uid);
        getLoaderManager().restartLoader(LOADER_ID, args, this);
    }

    @Override
    public void onStop() {
        super.onStop();

        getLoaderManager().destroyLoader(LOADER_ID);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if(receiver != null) {
            getContext().unregisterReceiver(receiver);
        }
    }

    public void setEmptyText(CharSequence emptyText) {
        View emptyView = mListView.getEmptyView();

        if (emptyView instanceof TextView) {
            ((TextView) emptyView).setText(emptyText);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        String uid = args.getString(AppConstants.KEY_SYS_CURRENT_UID);

        Uri baseUri = AppContract.RecentContactView.CONTENT_URI;

        String selection = AppContract.RecentContactView.COLUMN_NAME_OWNER + " = ?" +
                " AND " +
                AppContract.RecentContactView.COLUMN_NAME_STATUS_MSG_LIST + " != ?";
        String[] selectionArgs = new String[]{uid, String.valueOf(User.USER_STATUS_MSG_LIST_OUT)};

        return new CursorLoader(getContext(),baseUri,null,selection,selectionArgs,null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        chatListCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        chatListCursorAdapter.swapCursor(null);
    }

    private class MessageReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (!TextUtils.isEmpty(action)) {
                UserManager userManager = new UserManager(getContext());
                if (action.equals(GlobalApplication.ACTION_INTENT_SHIELD_LIST_ADD) || action.equals(GlobalApplication.ACTION_INTENT_SHIELD_LIST_BEFORE)) {
                    String uid = intent.getStringExtra("uid");
                    User user = userManager.queryUserByUid(uid);
                    if (user != null && !TextUtils.isEmpty(user.getNickname())) {
                        if (action.equals(GlobalApplication.ACTION_INTENT_SHIELD_LIST_ADD))
                            Toast.makeText(getContext(), user.getNickname() + "成功添加到屏蔽人名单", Toast.LENGTH_LONG).show();

                        if (action.equals(GlobalApplication.ACTION_INTENT_SHIELD_LIST_BEFORE))
                            Toast.makeText(getContext(), user.getNickname() + "已经在屏蔽人名单了", Toast.LENGTH_LONG).show();
                    }
                }
            }

            String uid = sharedPreferencesHelper.getStringValue(AppConstants.KEY_SYS_CURRENT_UID);
            Bundle args = new Bundle();
            args.putString(AppConstants.KEY_SYS_CURRENT_UID, uid);
            getLoaderManager().restartLoader(LOADER_ID, args, MessageFragment.this);
        }
    }
}
