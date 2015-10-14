package net.pingfang.signalr.chat.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.IntentCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.openapi.LogoutAPI;
import com.sina.weibo.sdk.utils.LogUtil;
import com.tencent.tauth.Tencent;

import net.pingfang.signalr.chat.R;
import net.pingfang.signalr.chat.constant.app.AppConstants;
import net.pingfang.signalr.chat.constant.qq.TencentConstants;
import net.pingfang.signalr.chat.constant.weibo.WeiboRequestListener;
import net.pingfang.signalr.chat.service.ChatService;
import net.pingfang.signalr.chat.util.SharedPreferencesHelper;

import org.json.JSONException;
import org.json.JSONObject;

public class SettingsActivity extends AppCompatActivity implements View.OnClickListener{

    TextView btn_activity_back;
    TextView tv_settings_item_about;
    TextView tv_settings_item_exit;

    SharedPreferencesHelper sharedPreferencesHelper;

    /** Access Token 实例  */
    private Oauth2AccessToken wbAccessToken;

//    ChatService chatService;
    ChatService mService;
    boolean mBound = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        sharedPreferencesHelper = SharedPreferencesHelper.newInstance(getApplicationContext());
        initView();
        initCommunicate();
    }

    private void initView() {
        btn_activity_back = (TextView) findViewById(R.id.btn_activity_back);
        btn_activity_back.setOnClickListener(this);
        tv_settings_item_about = (TextView) findViewById(R.id.tv_settings_item_about);
        tv_settings_item_about.setOnClickListener(this);
        tv_settings_item_exit = (TextView) findViewById(R.id.tv_settings_item_exit);
        tv_settings_item_exit.setOnClickListener(this);
    }

    private void initCommunicate() {
//        chatService = ChatService.newInstance(getApplicationContext());
        if(!mBound) {
            Intent intent = new Intent(this, ChatService.class);
            bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mBound) {
            unbindService(mConnection);
            mBound = false;
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
    public void onClick(View view) {
        int viewId = view.getId();
        switch (viewId) {
            case R.id.btn_activity_back:
                navigateUp();
                break;
            case R.id.tv_settings_item_about:
                Intent intent = new Intent();
                intent.setClass(getApplicationContext(),AppAboutActivity.class);
                startActivity(intent);
                break;
            case R.id.tv_settings_item_exit:
                sharedPreferencesHelper.clearKey(AppConstants.KEY_SYS_CURRENT_UID);
                sharedPreferencesHelper.clearKey(AppConstants.KEY_SYS_CURRENT_NICKNAME);
                sharedPreferencesHelper.clearKey(AppConstants.KEY_SYS_CURRENT_PORTRAIT);

                wbAccessToken = SharedPreferencesHelper.readAccessToken();
                if(wbAccessToken != null && wbAccessToken.isSessionValid()) {
                    new LogoutAPI(wbAccessToken).logout(new WeiboRequestListener() {
                        @Override
                        public void onComplete(String response) {
                            if (!TextUtils.isEmpty(response)) {
                                try {
                                    JSONObject obj = new JSONObject(response);
                                    String value = obj.getString("result");

                                    if ("true".equalsIgnoreCase(value)) {
                                        // XXX: 考虑是否需要将 AccessTokenKeeper 放到 SDK 中？？
                                        //AccessTokenKeeper.clear(getContext());
                                        // 清空当前 Token
                                        wbAccessToken = null;
                                        SharedPreferencesHelper.clearAccessToken();
                                        Toast.makeText(getApplicationContext(),
                                                R.string.weibosdk_demo_toast_auth_canceled, Toast.LENGTH_SHORT).show();
                                    }
                                } catch (JSONException e) {
                                    LogUtil.e(SettingsActivity.class.getSimpleName(), "onComplete JSONException...");
                                }
                            }
                        }
                    });
                }
                Tencent mTencent = Tencent.createInstance(TencentConstants.APP_ID, getApplicationContext());
                if(mTencent.isSessionValid()) {
                    SharedPreferencesHelper.clearQqAccessToken();
                    mTencent.logout(getApplicationContext());
                }

                mService.destroy();
                if (mBound) {
                    unbindService(mConnection);
                    mBound = false;
                }
                stopService(new Intent(getApplicationContext(),ChatService.class));

                Intent exitIntent = new Intent();
                exitIntent.setClass(getApplicationContext(), LoginActivity.class);
                exitIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK| IntentCompat.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(exitIntent);
                finish();
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
