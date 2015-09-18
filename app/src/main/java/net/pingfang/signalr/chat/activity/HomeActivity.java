package net.pingfang.signalr.chat.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.content.IntentCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.openapi.LogoutAPI;
import com.sina.weibo.sdk.utils.LogUtil;
import com.squareup.okhttp.Response;
import com.tencent.tauth.Tencent;

import net.pingfang.signalr.chat.R;
import net.pingfang.signalr.chat.adapter.CollectionPagerAdapter;
import net.pingfang.signalr.chat.constant.app.AppConstants;
import net.pingfang.signalr.chat.constant.qq.TencentConstants;
import net.pingfang.signalr.chat.constant.weibo.WeiboConstants;
import net.pingfang.signalr.chat.constant.weibo.WeiboRequestListener;
import net.pingfang.signalr.chat.fragment.AccountFragment;
import net.pingfang.signalr.chat.fragment.BuddyFragment;
import net.pingfang.signalr.chat.fragment.MessageFragment;
import net.pingfang.signalr.chat.listener.OnFragmentInteractionListener;
import net.pingfang.signalr.chat.net.HttpBaseCallback;
import net.pingfang.signalr.chat.net.OkHttpCommonUtil;
import net.pingfang.signalr.chat.util.SharedPreferencesHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class HomeActivity extends AppCompatActivity implements View.OnClickListener {

    TextView tv_activity_title;
    TextView tv_menu_drop_down;
    FrameLayout fl_container;
    ViewPager pager;

    MessageFragment messageFragment;
    BuddyFragment buddyFragment;
    AccountFragment accountFragment;

    Button btn_list_chat;
    Button btn_list_friend;
    Button btn_account_management;

    CollectionPagerAdapter adapter;

    private Handler mDelivery;
    SharedPreferencesHelper helper;

    /** 微博相关参数,封装了 "access_token"，"expires_in"，"refresh_token"，并提供了他们的管理功能  */
    private Oauth2AccessToken mAccessToken;

    // qq 登录配置
    Tencent mTencent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        mDelivery = new Handler(Looper.getMainLooper());
        helper = SharedPreferencesHelper.newInstance(getApplicationContext());
        refreshToken();
        initView();
        initAdapter();
    }

    private void refreshToken() {
        mAccessToken = SharedPreferencesHelper.readAccessToken();
        if(!TextUtils.isEmpty(mAccessToken.getUid()) && !mAccessToken.isSessionValid()) {
            OkHttpCommonUtil okHttpCommonUtil = OkHttpCommonUtil.newInstance(getApplicationContext());
            okHttpCommonUtil.postRequest("https://api.weibo.com/oauth2/access_token", new OkHttpCommonUtil.Param[]{
                    new OkHttpCommonUtil.Param(WeiboConstants.PARAM_WB_CLIENT_ID, WeiboConstants.APP_KEY),
                    new OkHttpCommonUtil.Param(WeiboConstants.PARAM_WB_CLIENT_SECRET, WeiboConstants.APP_SECRET),
                    new OkHttpCommonUtil.Param(WeiboConstants.PARAM_WB_GRANT_TYPE, "refresh_token"),
                    new OkHttpCommonUtil.Param(WeiboConstants.PARAM_WB_REDIRECT_URL, WeiboConstants.REDIRECT_URL),
                    new OkHttpCommonUtil.Param(WeiboConstants.PARAM_WB_REFRESH_TOKEN, mAccessToken.getRefreshToken())
            }, new HttpBaseCallback() {
                @Override
                public void onResponse(final Response response) throws IOException {
                    String jsonStr = response.body().string();
                    if (!TextUtils.isEmpty(jsonStr)) {
                        try {
                            JSONObject jsonObject = new JSONObject(jsonStr);
                            String accessToken = jsonObject.getString("access_token");
                            long expiresIn = jsonObject.getLong("expires_in");
                            mAccessToken.setToken(accessToken);
                            mAccessToken.setExpiresTime(expiresIn);
                            SharedPreferencesHelper.writeAccessToken(mAccessToken);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else {
                        mDelivery.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(), getString(R.string.weibo_toast_auth_expired), Toast.LENGTH_SHORT).show();
                                logout();
                            }
                        });

                    }

                }
            });
        }

        mTencent = Tencent.createInstance(TencentConstants.APP_ID,getApplicationContext());
        String token = helper.getStringValue(TencentConstants.KEY_ACCESS_TOKEN);
        String expires = helper.getStringValue(TencentConstants.KEY_EXPIRES_IN);
        String openId = helper.getStringValue(TencentConstants.KEY_OPEN_ID);
        if (!TextUtils.isEmpty(token) && !TextUtils.isEmpty(expires)
                && !TextUtils.isEmpty(openId)) {
            mTencent.setAccessToken(token, expires);
            mTencent.setOpenId(openId);

            if(!mTencent.isSessionValid()) {
                logout();
            }
        }

    }

    private void logout() {
        helper.clearKey(AppConstants.KEY_SYS_CURRENT_UID);
        helper.clearKey(AppConstants.KEY_SYS_CURRENT_NICKNAME);
        helper.clearKey(AppConstants.KEY_SYS_CURRENT_PORTRAIT);

        mAccessToken = SharedPreferencesHelper.readAccessToken();
        if(mAccessToken != null && mAccessToken.isSessionValid()) {
            new LogoutAPI(mAccessToken).logout(new WeiboRequestListener() {
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
                                mAccessToken = null;
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

        if(mTencent.isSessionValid()) {
            SharedPreferencesHelper.clearQqAccessToken();
            mTencent.logout(getApplicationContext());
        }

        Intent exitIntent = new Intent();
        exitIntent.setClass(getApplicationContext(), LoginActivity.class);
        exitIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | IntentCompat.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(exitIntent);
        finish();
    }

    private void initView() {
        tv_activity_title = (TextView) findViewById(R.id.tv_activity_title);
        tv_menu_drop_down = (TextView) findViewById(R.id.tv_menu_drop_down);
        tv_menu_drop_down.setOnClickListener(this);
        fl_container = (FrameLayout) findViewById(R.id.fl_container);
        fl_container = (FrameLayout) findViewById(R.id.fl_container);
        pager = (ViewPager) findViewById(R.id.pager);
        btn_list_chat = (Button) findViewById(R.id.btn_list_chat);
        btn_list_chat.setOnClickListener(this);
        btn_list_friend = (Button) findViewById(R.id.btn_list_friend);
        btn_list_friend.setOnClickListener(this);
        btn_account_management = (Button) findViewById(R.id.btn_account_management);
        btn_account_management.setOnClickListener(this);
    }

    private void initAdapter() {
        messageFragment = MessageFragment.newInstance(onFragmentInteractionListener);
        buddyFragment = BuddyFragment.newInstance();
        accountFragment = AccountFragment.newInstance(onFragmentInteractionListener);
        adapter = new CollectionPagerAdapter(getSupportFragmentManager());
        adapter.add(messageFragment);
        adapter.add(buddyFragment);
        adapter.add(accountFragment);
        pager.setAdapter(adapter);

        pager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                switch (position) {
                    case 1:
                        tv_activity_title.setText(R.string.tv_activity_title_roster);
                        break;
                    case 2:
                        tv_activity_title.setText(R.string.tv_activity_title_account);

                        break;
                    case 0:
                        tv_activity_title.setText(R.string.tv_activity_title_message);
                        break;
                }
            }
        });
    }

    private OnFragmentInteractionListener onFragmentInteractionListener = new OnFragmentInteractionListener() {

        @Override
        public void loadAccountInfo() {

        }

        @Override
        public void loadMessage() {
            MessageFragment fragment = (MessageFragment) adapter.getItem(0);
            fragment.updateMessage("server", "0001", "");
        }

        @Override
        public void onFragmentInteraction(String name, String uid) {
            Intent intent = new Intent();
            intent.setClass(getApplicationContext(),ChatActivity.class);
            intent.putExtra("name", name);
            intent.putExtra("uid", uid);
            startActivity(intent);
        }
    };

    @Override
    public void onClick(View view) {
        int viewId = view.getId();
        switch(viewId) {
            case R.id.tv_menu_drop_down:
                popupMenu(view);
                break;
            case R.id.btn_list_chat:
                pager.setCurrentItem(0);
                break;
            case R.id.btn_list_friend:
                pager.setCurrentItem(1);
                break;
            case R.id.btn_account_management:
                pager.setCurrentItem(2);
                break;
        }
    }

    private void popupMenu(View view) {
        ContextThemeWrapper wrapper = new ContextThemeWrapper(getApplicationContext(), R.style.AppTheme);
        PopupMenu popup = new PopupMenu(wrapper, view);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.menu_home, popup.getMenu());
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_scan:
                        IntentIntegrator integrator = new IntentIntegrator(HomeActivity.this);
                        integrator.setCaptureActivity(CaptureActivityAnyOrientation.class);
                        integrator.setOrientationLocked(true);
                        integrator.initiateScan();
                        break;
                    case R.id.action_resource:
                        break;
                    case R.id.action_maintain:

                        break;
                }
                return true;
            }
        });
        popup.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if(result != null) {
            if(result.getContents() == null) {
                Toast.makeText(getApplicationContext(), "Cancelled", Toast.LENGTH_LONG).show();
            } else {
                String content = result.getContents();
                Toast.makeText(getApplicationContext(), "Scanned: " + content, Toast.LENGTH_LONG).show();
            }
        } else {
            Log.d("MainActivity", "Weird");
            // This is important, otherwise the result will not be passed to the fragment
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
