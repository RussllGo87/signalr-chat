package net.pingfang.signalr.chat.activity;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.okhttp.Response;

import net.pingfang.signalr.chat.R;
import net.pingfang.signalr.chat.adapter.ShieldListCursorAdapter;
import net.pingfang.signalr.chat.constant.app.AppConstants;
import net.pingfang.signalr.chat.database.AppContract;
import net.pingfang.signalr.chat.database.User;
import net.pingfang.signalr.chat.database.UserManager;
import net.pingfang.signalr.chat.message.MessageConstructor;
import net.pingfang.signalr.chat.net.HttpBaseCallback;
import net.pingfang.signalr.chat.net.OkHttpCommonUtil;
import net.pingfang.signalr.chat.service.ChatService;
import net.pingfang.signalr.chat.util.GlobalApplication;
import net.pingfang.signalr.chat.util.SharedPreferencesHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class ListShieldsActivity extends AppCompatActivity implements View.OnClickListener,LoaderManager.LoaderCallbacks<Cursor>{

    public static final String TAG = ListShieldsActivity.class.getSimpleName();

    public static final String URL_LOAD_SHIELD_LIST = GlobalApplication.URL_WEB_API_HOST + "/api/WebAPI/User/GetShields";
    public static final String KEY_LOAD_SHIELD_LIST_UID = "userId";

    private static final int LOADER_ID = 0x03;

    TextView btn_activity_back;
    ListView lv_list_shield;
    private ShieldListCursorAdapter listCursorAdapter;

    SharedPreferencesHelper sharedPreferencesHelper;

    ChatService mService;
    boolean mBound = false;

    MessageReceiver messageReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_shield);

        sharedPreferencesHelper = SharedPreferencesHelper.newInstance(getApplicationContext());
        initView();
        initCommunicate();
        loadShieldList();
    }

    private void initView() {
        btn_activity_back = (TextView) findViewById(R.id.btn_activity_back);
        btn_activity_back.setOnClickListener(this);

        lv_list_shield = (ListView) findViewById(R.id.lv_list_shield);
        listCursorAdapter = new ShieldListCursorAdapter(getApplicationContext(),null, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
        lv_list_shield.setAdapter(listCursorAdapter);
        lv_list_shield.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                User user = (User) view.getTag();

                if(mBound) {
                    mService.sendMessage("UnShield",
                            MessageConstructor.constructUnShieldMsgReq(
                                    sharedPreferencesHelper.getStringValue(AppConstants.KEY_SYS_CURRENT_UID),
                                    user.getUid()
                            ));
                }
            }
        });

        String uid = sharedPreferencesHelper.getStringValue(AppConstants.KEY_SYS_CURRENT_UID);
        Bundle args = new Bundle();
        args.putString(AppConstants.KEY_SYS_CURRENT_UID, uid);

        getSupportLoaderManager().initLoader(LOADER_ID, args, this);
    }

    private void initCommunicate() {

        messageReceiver = new MessageReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(GlobalApplication.ACTION_INTENT_SHIELD_LIST_UPDATE);
        registerReceiver(messageReceiver, filter);

        Intent intent = new Intent(this, ChatService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    private void loadShieldList() {
        OkHttpCommonUtil okHttpCommonUtil = OkHttpCommonUtil.newInstance(getApplicationContext());
        okHttpCommonUtil.getRequest(URL_LOAD_SHIELD_LIST,
                new OkHttpCommonUtil.Param[]{
                        new OkHttpCommonUtil.Param(KEY_LOAD_SHIELD_LIST_UID, sharedPreferencesHelper.getStringValue(AppConstants.KEY_SYS_CURRENT_UID))
                },
                new HttpBaseCallback() {
                    @Override
                    public void onResponse(Response response) throws IOException {
                        super.onResponse(response);
                        String result = response.body().string();
                        Log.d(TAG, "URL_LOAD_SHIELD_LIST return " + result);
                        JSONArray jsonArray = null;
                        UserManager userManager = new UserManager(getApplicationContext());

                        try {
                            jsonArray = new JSONArray(result);
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                String uid = jsonObject.getString("userId");
                                String nickname = jsonObject.getString("nickname");
                                String portrait = jsonObject.getString("portrait");
                                boolean isExist = userManager.isExist(uid);
                                ContentValues values = new ContentValues();
                                if (isExist) {
                                    String selection = AppContract.UserEntry.COLUMN_NAME_ENTRY_UID + " = ?";
                                    values.put(AppContract.UserEntry.COLUMN_NAME_NICK_NAME, nickname);
                                    values.put(AppContract.UserEntry.COLUMN_NAME_PORTRAIT, portrait);
                                    getApplicationContext().getContentResolver().update(AppContract.UserEntry.CONTENT_URI,
                                            values,
                                            selection,
                                            new String[]{uid});

                                    String selectionStatus =
                                            AppContract.UserStatusEntry.COLUMN_NAME_ENTRY_UID + " = ?" +
                                                    " AND " +
                                                    AppContract.UserStatusEntry.COLUMN_NAME_ENTRY_OWNER + " = ?";
                                    String[] selectionArgsStatus = new String[]{uid, sharedPreferencesHelper.getStringValue(AppConstants.KEY_SYS_CURRENT_UID)};

                                    ContentValues statusValues = new ContentValues();
                                    statusValues.put(AppContract.UserStatusEntry.COLUMN_NAME_STATUS_SHIELD, User.USER_STATUS_SHIELD_LIST_IN);

                                    getApplicationContext().getContentResolver().update(AppContract.UserStatusEntry.CONTENT_URI,
                                            statusValues,
                                            selectionStatus,
                                            selectionArgsStatus);

                                } else {

                                    values.put(AppContract.UserEntry.COLUMN_NAME_ENTRY_UID, uid);
                                    values.put(AppContract.UserEntry.COLUMN_NAME_NICK_NAME, nickname);
                                    values.put(AppContract.UserEntry.COLUMN_NAME_PORTRAIT, portrait);
                                    values.put(AppContract.UserEntry.COLUMN_NAME_STATUS_MSG_LIST, User.USER_STATUS_MSG_LIST_OUT);
                                    values.put(AppContract.UserEntry.COLUMN_NAME_STATUS_NEARBY_LIST, User.USER_STATUS_NEARBY_LIST_OUT);

                                    getApplicationContext().getContentResolver().insert(AppContract.UserEntry.CONTENT_URI, values);

                                    ContentValues statusValues = new ContentValues();
                                    statusValues.put(AppContract.UserStatusEntry.COLUMN_NAME_ENTRY_UID, uid);
                                    statusValues.put(AppContract.UserStatusEntry.COLUMN_NAME_ENTRY_OWNER, sharedPreferencesHelper.getStringValue(AppConstants.KEY_SYS_CURRENT_UID));
                                    statusValues.put(AppContract.UserStatusEntry.COLUMN_NAME_STATUS_MSG, User.USER_STATUS_MSG_LIST_OUT);
                                    statusValues.put(AppContract.UserStatusEntry.COLUMN_NAME_STATUS_NEARBY, User.USER_STATUS_NEARBY_LIST_OUT);
                                    statusValues.put(AppContract.UserStatusEntry.COLUMN_NAME_STATUS_SHIELD, User.USER_STATUS_SHIELD_LIST_IN);
                                    statusValues.put(AppContract.UserStatusEntry.COLUMN_NAME_DISTANCE, 0);
                                    statusValues.put(AppContract.UserStatusEntry.COLUMN_NAME_STATUS_REMARK, "");
                                    getApplicationContext().getContentResolver().insert(AppContract.UserStatusEntry.CONTENT_URI, statusValues);
                                }

                            }

                            if(!(jsonArray.length() > 0)) {
                                String selectionStatus =
                                        AppContract.UserStatusEntry.COLUMN_NAME_ENTRY_OWNER + " = ?";
                                String[] selectionArgsStatus = new String[]{sharedPreferencesHelper.getStringValue(AppConstants.KEY_SYS_CURRENT_UID)};

                                ContentValues statusValues = new ContentValues();
                                statusValues.put(AppContract.UserStatusEntry.COLUMN_NAME_STATUS_SHIELD, User.USER_STATUS_SHIELD_LIST_OUT);

                                getApplicationContext().getContentResolver().update(AppContract.UserStatusEntry.CONTENT_URI,
                                        statusValues,
                                        selectionStatus,
                                        selectionArgsStatus);
                            }

                            Intent intent = new Intent();
                            intent.setAction(GlobalApplication.ACTION_INTENT_SHIELD_LIST_UPDATE);
                            getApplicationContext().sendBroadcast(intent);

                        } catch (JSONException e) {
                            e.printStackTrace();

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getApplicationContext(), R.string.toast_list_shield_error_invalidate, Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(messageReceiver != null) {
            unregisterReceiver(messageReceiver);
        }

        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
    }

    @Override
    public void onClick(View view) {
        int viewId = view.getId();
        switch (viewId) {
            case R.id.btn_activity_back:
                navigateUp();
                break;
        }
    }

    /** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            ChatService.ChatBinder binder = (ChatService.ChatBinder) service;
            mService = (ChatService) binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName className) {
            mBound = false;
        }
    };

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        String uid = args.getString(AppConstants.KEY_SYS_CURRENT_UID);

        Uri baseUri = AppContract.UserStatusView.CONTENT_URI;

        String selection = AppContract.UserStatusView.COLUMN_NAME_OWNER + " = ?" +
                " AND " +
                AppContract.UserStatusView.COLUMN_NAME_STATUS_SHIELD + " != ?";
        String[] selectionArgs = new String[]{uid, String.valueOf(User.USER_STATUS_SHIELD_LIST_OUT)};

        return new CursorLoader(getApplicationContext(),baseUri,null,selection,selectionArgs,null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        listCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        listCursorAdapter.swapCursor(null);
    }


    public void navigateUp() {
        Intent upIntent = NavUtils.getParentActivityIntent(this);
        if(NavUtils.shouldUpRecreateTask(this, upIntent)) {
            TaskStackBuilder.create(this)
                    .addNextIntentWithParentStack(upIntent)
                    .startActivities();
        } else {
            onBackPressed();
        }
    }

    private class MessageReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action.equals(GlobalApplication.ACTION_INTENT_SHIELD_LIST_UPDATE)) {
                String uid = sharedPreferencesHelper.getStringValue(AppConstants.KEY_SYS_CURRENT_UID);
                Bundle args = new Bundle();
                args.putString(AppConstants.KEY_SYS_CURRENT_UID, uid);

                getSupportLoaderManager().restartLoader(LOADER_ID, args, ListShieldsActivity.this);
            }
        }
    }
}
