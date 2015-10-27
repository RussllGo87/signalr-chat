package net.pingfang.signalr.chat.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import net.pingfang.signalr.chat.R;
import net.pingfang.signalr.chat.constant.app.AppConstants;
import net.pingfang.signalr.chat.message.MessageConstructor;
import net.pingfang.signalr.chat.service.ChatService;
import net.pingfang.signalr.chat.util.SharedPreferencesHelper;

public class ListShieldsActivity extends AppCompatActivity implements View.OnClickListener{

    TextView btn_activity_back;

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
    }

    private void initCommunicate() {
        if(!mBound) {
            Intent intent = new Intent(this, ChatService.class);
            bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
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
            mService.sendMessage("Shields",
                    MessageConstructor.constructShieldsListMsgReq(sharedPreferencesHelper.getStringValue(AppConstants.KEY_SYS_CURRENT_UID)));
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName className) {
            mBound = false;
        }
    };

    @Override
    public void onClick(View view) {
        int viewId = view.getId();
        switch (viewId) {
            case R.id.btn_activity_back:
                navigateUp();
                break;
        }
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
