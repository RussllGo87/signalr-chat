package net.pingfang.signalr.chat.activity;

import android.content.Intent;
import android.os.Bundle;
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

import net.pingfang.signalr.chat.R;
import net.pingfang.signalr.chat.constant.WeiboRequestListener;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        sharedPreferencesHelper = SharedPreferencesHelper.newInstance(getApplicationContext());
        initView();
    }

    private void initView() {
        btn_activity_back = (TextView) findViewById(R.id.btn_activity_back);
        btn_activity_back.setOnClickListener(this);
        tv_settings_item_about = (TextView) findViewById(R.id.tv_settings_item_about);
        tv_settings_item_about.setOnClickListener(this);
        tv_settings_item_exit = (TextView) findViewById(R.id.tv_settings_item_exit);
        tv_settings_item_exit.setOnClickListener(this);
    }

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
                sharedPreferencesHelper.putStringValue("uid", "");

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
