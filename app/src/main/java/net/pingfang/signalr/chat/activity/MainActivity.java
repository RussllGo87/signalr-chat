package net.pingfang.signalr.chat.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;

import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.tencent.tauth.Tencent;

import net.pingfang.signalr.chat.R;
import net.pingfang.signalr.chat.constant.qq.TencentConstants;
import net.pingfang.signalr.chat.util.SharedPreferencesHelper;

public class MainActivity extends AppCompatActivity {

    SharedPreferencesHelper sharedPreferencesHelper;
    /** 封装了 "access_token"，"expires_in"，"refresh_token"，并提供了他们的管理功能  */
    private Oauth2AccessToken mAccessToken;

    // 配置腾讯qq登录
    private Tencent mTencent;

    String token;
    String expires;
    String openId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPreferencesHelper = SharedPreferencesHelper.newInstance(getApplicationContext());
        mAccessToken = SharedPreferencesHelper.readAccessToken();
        if(mAccessToken.isSessionValid()) {
            Intent intent = new Intent();
            intent.setClass(getApplicationContext(), HomeActivity.class);
            startActivity(intent);
            finish();
        }

        mTencent = Tencent.createInstance(TencentConstants.APP_ID, getApplicationContext());
        token = sharedPreferencesHelper.getStringValue(TencentConstants.KEY_ACCESS_TOKEN);
        expires = sharedPreferencesHelper.getStringValue(TencentConstants.KEY_EXPIRES_IN);
        openId = sharedPreferencesHelper.getStringValue(TencentConstants.KEY_OPEN_ID);
        if(!TextUtils.isEmpty(token) && !TextUtils.isEmpty(expires) && !TextUtils.isEmpty(openId)) {
            mTencent.setAccessToken(token, expires);
            mTencent.setOpenId(openId);
            if(mTencent.isSessionValid()) {
                Intent intent = new Intent();
                intent.setClass(getApplicationContext(), HomeActivity.class);
                startActivity(intent);
                finish();
            }
        }
    }

    public void login(View view) {
        Intent intent = new Intent();
        intent.setClass(getApplicationContext(),LoginActivity.class);
        startActivity(intent);
        finish();
    }

    public void register(View view) {
        Intent intent = new Intent();
        intent.setClass(getApplicationContext(),RegisterActivity.class);
        startActivity(intent);
        finish();
    }


}
