package net.pingfang.signalr.chat.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
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
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import net.pingfang.signalr.chat.R;
import net.pingfang.signalr.chat.adapter.ShieldListCursorAdapter;
import net.pingfang.signalr.chat.constant.app.AppConstants;
import net.pingfang.signalr.chat.database.AppContract;
import net.pingfang.signalr.chat.database.User;
import net.pingfang.signalr.chat.message.MessageConstructor;
import net.pingfang.signalr.chat.service.ChatService;
import net.pingfang.signalr.chat.util.SharedPreferencesHelper;

public class ListShieldsActivity extends AppCompatActivity implements View.OnClickListener,LoaderManager.LoaderCallbacks<Cursor>{

    private static final int LOADER_ID = 0x03;

    TextView btn_activity_back;
    ListView lv_list_shield;
    private ShieldListCursorAdapter listCursorAdapter;

    SharedPreferencesHelper sharedPreferencesHelper;

    ChatService mService;
    boolean mBound = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_shield);

        sharedPreferencesHelper = SharedPreferencesHelper.newInstance(getApplicationContext());
        initView();

        initCommunicate();
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

        Intent intent = new Intent(this, ChatService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

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

        Uri baseUri = AppContract.ShieldListView.CONTENT_URI;

        String selection = AppContract.ShieldListView.COLUMN_NAME_OWNER+ " = ?";
        String[] selectionArgs = new String[]{uid};

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
}
