package net.pingfang.signalr.chat.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
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
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.CustomerIntentIntegrator;
import com.google.zxing.integration.android.CustomerIntentResult;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.openapi.LogoutAPI;
import com.sina.weibo.sdk.utils.LogUtil;
import com.squareup.okhttp.Response;
import com.tencent.tauth.Tencent;

import net.pingfang.signalr.chat.R;
import net.pingfang.signalr.chat.adapter.CollectionPagerAdapter;
import net.pingfang.signalr.chat.constant.app.AppConstants;
import net.pingfang.signalr.chat.constant.qq.TencentConstants;
import net.pingfang.signalr.chat.constant.wechat.WxConstants;
import net.pingfang.signalr.chat.constant.wechat.WxOauth2AccessToken;
import net.pingfang.signalr.chat.constant.weibo.WeiboConstants;
import net.pingfang.signalr.chat.constant.weibo.WeiboRequestListener;
import net.pingfang.signalr.chat.database.User;
import net.pingfang.signalr.chat.demo.GreyBitmapActivity;
import net.pingfang.signalr.chat.fragment.AccountFragment;
import net.pingfang.signalr.chat.fragment.BuddyFragment;
import net.pingfang.signalr.chat.fragment.DiscoveryFragment;
import net.pingfang.signalr.chat.fragment.MessageFragment;
import net.pingfang.signalr.chat.listener.OnFragmentInteractionListener;
import net.pingfang.signalr.chat.message.MessageConstructor;
import net.pingfang.signalr.chat.net.HttpBaseCallback;
import net.pingfang.signalr.chat.net.OkHttpCommonUtil;
import net.pingfang.signalr.chat.service.ChatService;
import net.pingfang.signalr.chat.ui.dialog.DoubleButtonDialogFragment;
import net.pingfang.signalr.chat.util.CommonTools;
import net.pingfang.signalr.chat.util.SharedPreferencesHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class HomeActivity extends AppCompatActivity implements View.OnClickListener {

    public static final String TAG = HomeActivity.class.getSimpleName();

    TextView tv_activity_title;
    TextView tv_msg_bulk;
    TextView tv_menu_drop_down;
    FrameLayout fl_container;
    ViewPager pager;

    MessageFragment messageFragment;
    BuddyFragment buddyFragment;
    DiscoveryFragment discoveryFragment;
    AccountFragment accountFragment;

    ImageView iv_discovery;
    ImageView iv_list_chat;
    ImageView iv_list_friend;
    ImageView iv_account_management;

    CollectionPagerAdapter adapter;
    SharedPreferencesHelper helper;
    // qq 登录配置
    Tencent mTencent;
    //    ChatService chatService;
    ChatService mService;
    boolean mBound = false;
    private Handler mDelivery;
    private boolean mReturningWithResult = false;
    /** 微博相关参数,封装了 "access_token"，"expires_in"，"refresh_token"，并提供了他们的管理功能  */
    private Oauth2AccessToken mAccessToken;
    // 微信登录配置
    private WxOauth2AccessToken mWxOauth2AccessToken;
    /**
     * Defines callbacks for service binding, passed to bindService()
     */
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            ChatService.ChatBinder binder = (ChatService.ChatBinder) service;
            mService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName className) {
            mBound = false;
        }
    };
    private OnFragmentInteractionListener onFragmentInteractionListener = new OnFragmentInteractionListener() {

        @Override
        public void loadAccountInfo() {
            String nickname = helper.getStringValue(AppConstants.KEY_SYS_CURRENT_NICKNAME);
            String portrait = helper.getStringValue(AppConstants.KEY_SYS_CURRENT_PORTRAIT);

            AccountFragment fragment = (AccountFragment) adapter.getItem(3);
            fragment.updateAccountInfo(nickname, portrait);
        }

        @Override
        public void shield(User user) {
            if (mBound) {
                mService.sendMessage("Shield",
                        MessageConstructor.constructShieldMsgReq(
                                helper.getStringValue(AppConstants.KEY_SYS_CURRENT_UID),
                                user.getUid()));
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        mDelivery = new Handler(Looper.getMainLooper());
        helper = SharedPreferencesHelper.newInstance(getApplicationContext());
        refreshToken();
        initView();
        initAdapter();

        initCommunicate();
        bindChatService();
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

        mTencent = Tencent.createInstance(TencentConstants.APP_ID, getApplicationContext());
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

        mWxOauth2AccessToken = SharedPreferencesHelper.readWxAccessToken();
        if(!TextUtils.isEmpty(mWxOauth2AccessToken.getOpenId()) && !mWxOauth2AccessToken.isSessionValid()) {
            OkHttpCommonUtil okHttpCommonUtil = OkHttpCommonUtil.newInstance(getApplicationContext());
            okHttpCommonUtil.getRequest(
                    "https://api.weixin.qq.com/sns/oauth2/refresh_token",
                    new OkHttpCommonUtil.Param[]{
                            new OkHttpCommonUtil.Param("appid", WxConstants.APP_ID),
                            new OkHttpCommonUtil.Param("grant_type","refresh_token"),
                            new OkHttpCommonUtil.Param("refresh_token",mWxOauth2AccessToken.getRefreshToken())
                    },
                    new HttpBaseCallback() {
                        @Override
                        public void onResponse(Response response) throws IOException {
                            String body = response.body().string();
                            mWxOauth2AccessToken = WxOauth2AccessToken.parseAccessToken(body);
                            if (mWxOauth2AccessToken != null && mWxOauth2AccessToken.isSessionValid()) {
                                // 保存 Token 到 SharedPreferences
                                SharedPreferencesHelper.writeAccessToken(mWxOauth2AccessToken);
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

        mWxOauth2AccessToken = SharedPreferencesHelper.readWxAccessToken();
        if(mWxOauth2AccessToken != null && mWxOauth2AccessToken.isSessionValid()) {
            mWxOauth2AccessToken = null;
            SharedPreferencesHelper.clearWxAccessToken();
        }


        Intent exitIntent = new Intent();
        exitIntent.setClass(getApplicationContext(), LoginActivity.class);
        exitIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | IntentCompat.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(exitIntent);
        finish();
    }

    private void initView() {
        tv_activity_title = (TextView) findViewById(R.id.tv_activity_title);
        tv_msg_bulk = (TextView) findViewById(R.id.tv_msg_bulk);
        tv_msg_bulk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setClass(getApplicationContext(), BulkMsgActivity.class);
                startActivity(intent);
            }
        });
        tv_menu_drop_down = (TextView) findViewById(R.id.tv_menu_drop_down);
        tv_menu_drop_down.setOnClickListener(this);
        tv_menu_drop_down.setVisibility(View.GONE);
        fl_container = (FrameLayout) findViewById(R.id.fl_container);
        fl_container = (FrameLayout) findViewById(R.id.fl_container);
        pager = (ViewPager) findViewById(R.id.pager);
        iv_discovery = (ImageView) findViewById(R.id.iv_discovery);
        iv_discovery.setOnClickListener(this);
        iv_list_chat = (ImageView) findViewById(R.id.iv_list_chat);
        iv_list_chat.setOnClickListener(this);
        iv_list_friend = (ImageView) findViewById(R.id.iv_list_friend);
        iv_list_friend.setOnClickListener(this);
        iv_account_management = (ImageView) findViewById(R.id.iv_account_management);
        iv_account_management.setOnClickListener(this);
    }

    private void initAdapter() {
        discoveryFragment = DiscoveryFragment.newInstance();
        messageFragment = MessageFragment.newInstance(onFragmentInteractionListener);
        buddyFragment = BuddyFragment.newInstance(onFragmentInteractionListener);
        accountFragment = AccountFragment.newInstance(onFragmentInteractionListener);
        adapter = new CollectionPagerAdapter(getSupportFragmentManager());
        adapter.add(discoveryFragment);
        adapter.add(messageFragment);
        adapter.add(buddyFragment);
        adapter.add(accountFragment);
        pager.setAdapter(adapter);
        setImageViewGroupSelected(0);
        pager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                switch (position) {
                    case 1:
                        tv_activity_title.setText(R.string.tv_activity_title_message);
                        setImageViewGroupSelected(1);
                        break;
                    case 2:
                        tv_activity_title.setText(R.string.tv_activity_title_roster);
                        setImageViewGroupSelected(2);
                        break;
                    case 3:
                        tv_activity_title.setText(R.string.tv_activity_title_account);
                        setImageViewGroupSelected(3);
                        break;
                    case 0:
                        tv_activity_title.setText(R.string.tv_activity_title_discovery);
                        setImageViewGroupSelected(0);
                        break;
                }
            }
        });
    }

    private void setImageViewGroupSelected(int index) {
        if (index == 0) {
            iv_discovery.setSelected(true);
        } else {
            iv_discovery.setSelected(false);
        }

        if (index == 1) {
            iv_list_chat.setSelected(true);
        } else {
            iv_list_chat.setSelected(false);
        }

        if (index == 2) {
            iv_list_friend.setSelected(true);
        } else {
            iv_list_friend.setSelected(false);
        }

        if (index == 3) {
            iv_account_management.setSelected(true);
        } else {
            iv_account_management.setSelected(false);
        }
    }

    private void initCommunicate() {
        String qs = constructLogin(helper.getStringValue(AppConstants.KEY_SYS_CURRENT_UID));
        Log.d(TAG, "qs == " + qs);
        Intent intent = new Intent(getApplicationContext(),ChatService.class);
        Bundle args = new Bundle();
        args.putInt(ChatService.FLAG_SERVICE_CMD, ChatService.FLAF_INIT_CONNECTION);
        args.putString(ChatService.FLAG_INIT_CONNECTION_QS, qs);
        intent.putExtra("args", args);
        startService(intent);
    }

    private void bindChatService() {
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
        switch(viewId) {
            case R.id.tv_menu_drop_down:
                popupMenu(view);
                break;
            case R.id.iv_discovery:
                pager.setCurrentItem(0);
                setImageViewGroupSelected(0);
                break;
            case R.id.iv_list_chat:
                pager.setCurrentItem(1);
                setImageViewGroupSelected(1);
                break;
            case R.id.iv_list_friend:
                pager.setCurrentItem(2);
                setImageViewGroupSelected(2);
                break;
            case R.id.iv_account_management:
                pager.setCurrentItem(3);
                setImageViewGroupSelected(3);
                break;
        }
    }

    private void popupMenu(View view) {
        ContextThemeWrapper wrapper = new ContextThemeWrapper(getApplicationContext(), R.style.AppTheme);
        PopupMenu popup = new PopupMenu(wrapper, view);
        final MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.menu_home, popup.getMenu());
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_scan:
                        CustomerIntentIntegrator integrator = new CustomerIntentIntegrator(HomeActivity.this);
                        integrator.setCaptureActivity(CaptureActivityAnyOrientation.class);
                        integrator.setOrientationLocked(true);
                        integrator.initiateScan();
                        break;
                    case R.id.action_resource:
                        Intent resourceAddIntent = new Intent();
                        resourceAddIntent.setClass(getApplicationContext(),ResourcePostActivity.class);
                        startActivity(resourceAddIntent);
                        break;
                    case R.id.action_maintain:
                        Intent adMaintainIntent = new Intent();
                        adMaintainIntent.setClass(getApplicationContext(), AdMaintainActivity.class);
                        startActivity(adMaintainIntent);
                        break;
                    case R.id.action_convert:
                        Intent convertGrayIntent = new Intent();
                        convertGrayIntent.setClass(getApplicationContext(), GreyBitmapActivity.class);
                        startActivity(convertGrayIntent);
                        break;
                }
                return true;
            }
        });
        popup.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        CustomerIntentResult result = CustomerIntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if(result != null) {
            if(result.getContents() == null) {
                Toast.makeText(getApplicationContext(), getString(R.string.btn_ad_maintain_code_scan_cancelled), Toast.LENGTH_LONG).show();
            } else {
                final String content = result.getContents();
                Toast.makeText(getApplicationContext(), getString(R.string.btn_ad_maintain_code_scan_ok), Toast.LENGTH_LONG).show();
                if(CommonTools.checkUrl(content)) {
                    Log.d(TAG, "CommonTools.checkUrl(content) == " + true);
                    DoubleButtonDialogFragment dialogFragment = DoubleButtonDialogFragment.newInstance(
                            getApplicationContext().getString(R.string.dialog_message_url),
                            new DoubleButtonDialogFragment.DoubleButtonDialogClick() {
                                @Override
                                public void onPositiveButtonClick() {
                                    Intent intent = new Intent();
                                    intent.setAction(Intent.ACTION_VIEW);
                                    intent.setData(Uri.parse(content));
                                    if (intent.resolveActivity(getPackageManager()) != null) {
                                        startActivity(intent);
                                    }
                                }
                            });
                    dialogFragment.show(getSupportFragmentManager(), "DoubleButtonDialogFragment");
                }

            }
        } else {
            Log.d("MainActivity", "Weird");
        }
    }

    private String constructLogin(String uid) {
        StringBuffer stringBuffer = new StringBuffer();
        String lat = helper.getStringValue(AppConstants.KEY_SYS_LOCATION_LAT,"0.0");
        String lng = helper.getStringValue(AppConstants.KEY_SYS_LOCATION_LNG, "0.0");
        if(!TextUtils.isEmpty(uid)) {
            stringBuffer.append("Android=");
            stringBuffer.append("{");
            stringBuffer.append("\"UserId\":");
            stringBuffer.append(uid);
            stringBuffer.append(",");
            stringBuffer.append("\"Longitude\":");
            stringBuffer.append(lng);
            stringBuffer.append(",");
            stringBuffer.append("\"Latitude\":");
            stringBuffer.append(lat);
            stringBuffer.append("}");
        }

        return stringBuffer.toString();
    }

}
