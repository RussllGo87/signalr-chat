package net.pingfang.signalr.chat.activity;

import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.auth.WeiboAuth;
import com.sina.weibo.sdk.auth.WeiboAuthListener;
import com.sina.weibo.sdk.auth.sso.SsoHandler;
import com.sina.weibo.sdk.exception.WeiboException;
import com.sina.weibo.sdk.openapi.legacy.UsersAPI;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.tencent.connect.UserInfo;
import com.tencent.connect.auth.QQAuth;
import com.tencent.connect.common.Constants;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;

import net.pingfang.signalr.chat.R;
import net.pingfang.signalr.chat.constant.app.AppConstants;
import net.pingfang.signalr.chat.constant.qq.TencentConstants;
import net.pingfang.signalr.chat.constant.weibo.WeiboConstants;
import net.pingfang.signalr.chat.constant.weibo.WeiboRequestListener;
import net.pingfang.signalr.chat.database.AppContract;
import net.pingfang.signalr.chat.database.NewUserManager;
import net.pingfang.signalr.chat.database.UserManager;
import net.pingfang.signalr.chat.net.HttpBaseCallback;
import net.pingfang.signalr.chat.net.OkHttpCommonUtil;
import net.pingfang.signalr.chat.ui.dialog.SingleButtonDialogFragment;
import net.pingfang.signalr.chat.util.MediaFileUtils;
import net.pingfang.signalr.chat.util.SharedPreferencesHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class LoginActivity extends AppCompatActivity {

    public static final String TAG = LoginActivity.class.getSimpleName();

    public static final String LOGIN_URL = "http://192.168.0.152:8090/api/WebAPI/User/Login";
    public static final String LOGIN_KEY_ACCOUNT = "account";
    public static final String LOGIN_KEY_PASSWORD = "password";

    public static final String NEW_LOGIN_URL = "http://192.168.0.152:8090/api/WebAPI/User/GetUser";
    public static final String NEW_LOGIN_KEY_TID = "tid";
    public static final String NEW_LOGIN_KEY_WID = "wid";
    public static final String NEW_LOGIN_KEY_WXID = "wxid";
    public static final String NEW_LOGIN_KEY_NICK_NAME = "nickname";
    public static final String NEW_LOGIN_KEY_PORTRAIT = "portrait";

    public static final String NEW_LGOIN_PARAM_PLATFROM_QQ = "qq";
    public static final String NEW_LGOIN_PARAM_PLATFROM_WEIBO = "weibo";
    public static final String NEW_LGOIN_PARAM_PLATFROM_WECHAT = "wechat";


    LinearLayout ll_form_container;
    EditText et_login_no;
    EditText et_login_pwd;
    CheckBox cb_show_pwd;

    ImageView btn_login_pattern_qq;
    ImageView btn_login_pattern_wechat;
    ImageView btn_login_pattern_weibo;
    LinearLayout ll_progress_bar_container;
    ProgressBar pb_operation;
    TextView tv_pb_operation;


    private Handler mDelivery;

    SharedPreferencesHelper sharedPreferencesHelper;
    String savedAccount;

    // 微博登录相关参数
    /** 微博 Web 授权类，提供登陆等功能  */
    private WeiboAuth mWeiboAuth;
    /** 封装了 "access_token"，"expires_in"，"refresh_token"，并提供了他们的管理功能  */
    private Oauth2AccessToken mAccessToken;
    /** 注意：SsoHandler 仅当 SDK 支持 SSO 时有效 */
    private SsoHandler mWeiboSsoHandler;

    // 腾讯qq实例
    Tencent mTencent;

    int currentClickViewId = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mDelivery = new Handler(Looper.getMainLooper());
        sharedPreferencesHelper = SharedPreferencesHelper.newInstance(getApplicationContext());
        savedAccount = sharedPreferencesHelper.getStringValue("account");

        initLoginConfig();
        initView();
    }

    /**
     * 初始化第三方登录配置信息
     */
    private void initLoginConfig() {
        // 创建微博实例
        mWeiboAuth = new WeiboAuth(this, WeiboConstants.APP_KEY, WeiboConstants.REDIRECT_URL, WeiboConstants.SCOPE);

        // 创建腾讯qq实例
        mTencent = Tencent.createInstance(TencentConstants.APP_ID,getApplicationContext());
    }

    private void initView() {
        ll_form_container = (LinearLayout) findViewById(R.id.ll_form_container);
        et_login_no = (EditText) findViewById(R.id.et_login_no);
        if(!TextUtils.isEmpty(savedAccount)) {
            et_login_no.setText(savedAccount);
        }
        et_login_pwd = (EditText) findViewById(R.id.et_login_pwd);

        cb_show_pwd = (CheckBox) findViewById(R.id.cb_show_pwd);
        cb_show_pwd.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    et_login_pwd.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    et_login_pwd.setSelection(et_login_pwd.getText().length());
                } else {
                    et_login_pwd.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    et_login_pwd.setSelection(et_login_pwd.getText().length());
                }
            }
        });

        btn_login_pattern_qq = (ImageView) findViewById(R.id.btn_login_pattern_qq);
        btn_login_pattern_qq.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currentClickViewId = view.getId();
                if (!mTencent.isSessionValid()) {
                    mTencent.login(LoginActivity.this, TencentConstants.SCOPE, loginListener);
                }
            }
        });
        btn_login_pattern_wechat = (ImageView) findViewById(R.id.btn_login_pattern_wechat);
        btn_login_pattern_weibo = (ImageView) findViewById(R.id.btn_login_pattern_weibo);
        btn_login_pattern_weibo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                currentClickViewId = view.getId();

                mWeiboSsoHandler = new SsoHandler(LoginActivity.this, mWeiboAuth);
                mWeiboSsoHandler.authorize(new WeiboAuthListener() {
                    @Override
                    public void onComplete(Bundle bundle) {
                        // 从 Bundle 中解析 Token
                        mAccessToken = Oauth2AccessToken.parseAccessToken(bundle);
                        if (mAccessToken.isSessionValid()) {
                            // 保存 Token 到 SharedPreferences
                            SharedPreferencesHelper.writeAccessToken(mAccessToken);
                            Toast.makeText(getApplicationContext(),
                                    R.string.weibosdk_demo_toast_auth_success, Toast.LENGTH_SHORT).show();

                            loadWbAccountInfo();

//                            Intent intent = new Intent();
//                            intent.setClass(getApplicationContext(), HomeActivity.class);
//                            startActivity(intent);
//                            finish();
                        } else {
                            // 当您注册的应用程序签名不正确时，就会收到 Code，请确保签名正确
                            String code = bundle.getString("code");
                            String message = getString(R.string.weibosdk_demo_toast_auth_failed);
                            if (!TextUtils.isEmpty(code)) {
                                message = message + "\nObtained the code: " + code;
                            }
                            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onWeiboException(WeiboException e) {
                        Toast.makeText(getApplicationContext(),
                                "Auth exception : " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onCancel() {
                        Toast.makeText(getApplicationContext(),
                                R.string.weibosdk_demo_toast_auth_canceled, Toast.LENGTH_LONG).show();
                    }
                });
            }
        });

        ll_progress_bar_container = (LinearLayout) findViewById(R.id.ll_progress_bar_container);
        pb_operation = (ProgressBar) findViewById(R.id.pb_operation);
        tv_pb_operation = (TextView) findViewById(R.id.tv_pb_operation);

    }

    private void loadWbAccountInfo() {
        long uid = Long.parseLong(mAccessToken.getUid());
        new UsersAPI(mAccessToken).show(uid, new WeiboRequestListener() {
            @Override
            public void onComplete(String response) {
                if (!TextUtils.isEmpty(response)) {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        String screenname = jsonObject.getString(WeiboConstants.PARAM_WB_SCREEN_NAME);
                        String profileImageUrl = jsonObject.getString(WeiboConstants.PARAM_WB_PROFILE_IMAGE_URL);
                        if (TextUtils.isEmpty(profileImageUrl)) {
                            profileImageUrl = jsonObject.getString(WeiboConstants.PARAM_WB_PROFILE_URL);
                        }
                        sharedPreferencesHelper.putStringValue(WeiboConstants.KEY_WB_SCREEN_NAME, screenname);
                        sharedPreferencesHelper.putStringValue(WeiboConstants.KEY_WB_PROFILE_IMAGE_URL, profileImageUrl);

                        mDelivery.post(new Runnable() {
                            @Override
                            public void run() {

                                //                                Intent intent = new Intent();
                                //                                intent.setClass(getApplicationContext(), HomeActivity.class);
                                //                                startActivity(intent);
                                //                                finish();

                                login(NEW_LGOIN_PARAM_PLATFROM_WEIBO);
                            }
                        });

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private IUiListener loginListener = new IUiListener() {
        @Override
        public void onComplete(Object response) {
            if (null == response) {
                SingleButtonDialogFragment dialogFragment = SingleButtonDialogFragment.newInstance("登录失败","返回为空");
                dialogFragment.show(getSupportFragmentManager(),"SingleButtonDialogFragment");
                return;
            }

            JSONObject jsonResponse = (JSONObject) response;
            if(jsonResponse.length() == 0) {
                SingleButtonDialogFragment dialogFragment = SingleButtonDialogFragment.newInstance("登录失败","返回为空");
                dialogFragment.show(getSupportFragmentManager(),"SingleButtonDialogFragment");
                return;
            }

            Toast.makeText(getApplicationContext(),getString(R.string.weibosdk_demo_toast_auth_success),Toast.LENGTH_SHORT).show();
            doComplete(jsonResponse);

//            Intent intent = new Intent();
//            intent.setClass(getApplicationContext(), HomeActivity.class);
//            startActivity(intent);
//            finish();
        }

        public void doComplete(JSONObject jsonObject) {
            try {
                String token = jsonObject.getString(Constants.PARAM_ACCESS_TOKEN);
                String expires = jsonObject.getString(Constants.PARAM_EXPIRES_IN);
                String openId = jsonObject.getString(Constants.PARAM_OPEN_ID);
                if (!TextUtils.isEmpty(token) && !TextUtils.isEmpty(expires)
                        && !TextUtils.isEmpty(openId)) {

                    SharedPreferencesHelper.writeAccessToken(token, expires, openId);

                    mTencent.setAccessToken(token, expires);
                    mTencent.setOpenId(openId);

                    loadQQAccountInfo();

                }
            } catch(Exception e) {
//                Toast.makeText(getApplicationContext(),getString(R.string.weibosdk_demo_toast_auth_failed),Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onError(UiError uiError) {
            mDelivery.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(),getString(R.string.weibosdk_demo_toast_auth_failed),Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override
        public void onCancel() {
            mDelivery.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(),getString(R.string.weibosdk_demo_toast_auth_canceled),Toast.LENGTH_SHORT).show();
                }
            });
        }
    };

    private void loadQQAccountInfo() {
        UserInfo userInfo = new UserInfo(getApplicationContext(),
                QQAuth.createInstance(TencentConstants.APP_ID, getApplicationContext()),
                mTencent.getQQToken());

        userInfo.getUserInfo(new IUiListener() {
            @Override
            public void onComplete(Object response) {
                if (null == response) {
                    Toast.makeText(getApplicationContext(), getString(R.string.resp_return_empty), Toast.LENGTH_SHORT).show();
                    return;
                }

                JSONObject jsonResponse = (JSONObject) response;
                if (jsonResponse.length() == 0) {
                    Toast.makeText(getApplicationContext(), getString(R.string.resp_return_empty), Toast.LENGTH_SHORT).show();
                    return;
                }

                doComplete(jsonResponse);
            }

            public void doComplete(JSONObject jsonObject) {
                try {
                    String nickname = jsonObject.getString(TencentConstants.PARAM_NICK_NAME);
                    String figureurl_qq_1 = jsonObject.getString(TencentConstants.PARAM_QQ_PORTRAIT);
                    sharedPreferencesHelper.putStringValue(TencentConstants.KEY_QQ_NICK_NAME, nickname);
                    sharedPreferencesHelper.putStringValue(TencentConstants.KEY_QQ_PORTRAIT, figureurl_qq_1);
                    mDelivery.post(new Runnable() {
                        @Override
                        public void run() {

                            //                            Intent intent = new Intent();
                            //                            intent.setClass(getApplicationContext(), HomeActivity.class);
                            //                            startActivity(intent);
                            //                            finish();

                            login(NEW_LGOIN_PARAM_PLATFROM_QQ);
                        }
                    });

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(UiError uiError) {

            }

            @Override
            public void onCancel() {

            }
        });
    }

    private void login(String platform) {

        OkHttpCommonUtil.Param[] params = new OkHttpCommonUtil.Param[0];
        if(!TextUtils.isEmpty(platform)) {
            if(platform.equals(NEW_LGOIN_PARAM_PLATFROM_QQ)) {
                if(mTencent.isSessionValid()) {
                    params = new OkHttpCommonUtil.Param[] {
                            new OkHttpCommonUtil.Param(NEW_LOGIN_KEY_TID,mTencent.getOpenId()),
                            new OkHttpCommonUtil.Param(NEW_LOGIN_KEY_NICK_NAME,
                                    sharedPreferencesHelper.getStringValue(TencentConstants.KEY_QQ_NICK_NAME)),
                            new OkHttpCommonUtil.Param(NEW_LOGIN_KEY_PORTRAIT,
                                    sharedPreferencesHelper.getStringValue(TencentConstants.KEY_QQ_PORTRAIT))
                    };
                }

            } else if(platform.equals(NEW_LGOIN_PARAM_PLATFROM_WEIBO)) {
                if(mAccessToken.isSessionValid()) {
                    params = new OkHttpCommonUtil.Param[]{
                            new OkHttpCommonUtil.Param(NEW_LOGIN_KEY_WID,mAccessToken.getUid()),
                            new OkHttpCommonUtil.Param(NEW_LOGIN_KEY_NICK_NAME,
                                    sharedPreferencesHelper.getStringValue(WeiboConstants.KEY_WB_SCREEN_NAME)),
                            new OkHttpCommonUtil.Param(NEW_LOGIN_KEY_WID,
                                    sharedPreferencesHelper.getStringValue(WeiboConstants.KEY_WB_PROFILE_IMAGE_URL))
                    };
                } else {
                    return;
                }

            } else if(platform.equals(NEW_LGOIN_PARAM_PLATFROM_WECHAT)) {
                params = new OkHttpCommonUtil.Param[]{};
            } else {
                return;
            }
        }

        OkHttpCommonUtil okHttpCommonUtil = OkHttpCommonUtil.newInstance(getApplicationContext());
        okHttpCommonUtil.getRequest(NEW_LOGIN_URL, params, new HttpBaseCallback() {
            @Override
            public void onFailure(Request request, IOException e) {

            }

            @Override
            public void onResponse(Response response) throws IOException {
                String json = response.body().string();
                Log.d(TAG, "NEW_LOGIN_URL return " + json);
                JSONObject jsonObject;
                try {
                    jsonObject = new JSONObject(json);
                    int status = jsonObject.getInt("status");
                    String message = jsonObject.getString("message");
                    if (status == 0) {
                        JSONObject result = jsonObject.getJSONObject("result");
                        final String id = result.getString("id");
                        final String nickname = result.getString("nickname");
                        final String portrait = result.getString("portrait");

                        UserManager userManager = new UserManager(getApplicationContext());
                        userManager.addRecord(id, nickname, portrait);

                        JSONArray jsonArray = jsonObject.getJSONArray("list");
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject user = jsonArray.getJSONObject(i);
                            String fuid = user.getString("uid");
                            String fnickname = user.getString("nickname");
                            String fportrait = user.getString("portrait");

                            userManager.addRecord(fuid, fnickname, fportrait);
                        }

                        mDelivery.post(new Runnable() {
                            @Override
                            public void run() {
                                sharedPreferencesHelper.putStringValue(AppConstants.KEY_SYS_CURRENT_UID, id);
                                sharedPreferencesHelper.putStringValue(AppConstants.KEY_SYS_CURRENT_NICKNAME, nickname);
                                sharedPreferencesHelper.putStringValue(AppConstants.KEY_SYS_CURRENT_PORTRAIT, portrait);

                                Intent intent = new Intent();
                                intent.setClass(getApplicationContext(), HomeActivity.class);
                                startActivity(intent);
                                finish();
                            }
                        });
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * 当 SSO 授权 Activity 退出时，该函数被调用。
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // SSO 授权回调
        // 重要：发起 SSO 登陆的 Activity 必须重写 onActivityResult
        if (mWeiboSsoHandler != null && currentClickViewId == btn_login_pattern_weibo.getId()) {
            mWeiboSsoHandler.authorizeCallBack(requestCode, resultCode, data);
        }

        if(mTencent != null && currentClickViewId == btn_login_pattern_qq.getId()) {
            Tencent.onActivityResultData(requestCode, resultCode, data, loginListener);
            if (requestCode == Constants.REQUEST_API) {
                if (resultCode == Constants.RESULT_LOGIN) {
                    Tencent.handleResultData(data, loginListener);
                }
            }
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        // Checks whether a hardware keyboard is available
        if (newConfig.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_NO) {
            RelativeLayout.LayoutParams layoutParams =
                    (RelativeLayout.LayoutParams)ll_form_container.getLayoutParams();
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
            layoutParams.topMargin = MediaFileUtils.dpToPx(getApplicationContext(),0);
            ll_form_container.setLayoutParams(layoutParams);
        } else if (newConfig.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_YES) {
            RelativeLayout.LayoutParams layoutParams =
                    (RelativeLayout.LayoutParams)ll_form_container.getLayoutParams();
            layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
            layoutParams.topMargin = MediaFileUtils.dpToPx(getApplicationContext(),0);
            ll_form_container.setLayoutParams(layoutParams);
        }
    }

    public void login(View view) {
        final String account = et_login_no.getText().toString().trim();
        String password = et_login_pwd.getText().toString().trim();
        if(!TextUtils.isEmpty(account) && !TextUtils.isEmpty(password)) {
            ll_progress_bar_container.setVisibility(View.VISIBLE);
            tv_pb_operation.setText(R.string.pb_message_login_now);

            OkHttpCommonUtil okHttpCommonUtil = OkHttpCommonUtil.newInstance(getApplicationContext());
            okHttpCommonUtil.getRequest(LOGIN_URL, new OkHttpCommonUtil.Param[]{
                new OkHttpCommonUtil.Param(LOGIN_KEY_ACCOUNT, account),
                new OkHttpCommonUtil.Param(LOGIN_KEY_PASSWORD, password)
            }, new HttpBaseCallback() {

                @Override
                public void onFailure(Request request, IOException e) {
                    super.onFailure(request, e);

                    mDelivery.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), R.string.pb_message_login_failure, Toast.LENGTH_SHORT).show();
                            ll_progress_bar_container.setVisibility(View.GONE);
                        }
                    });
                }

                @Override
                public void onResponse(Response response) throws IOException {
                    String json = response.body().string();
                    Log.d(TAG, "LOGIN_URL return " + json);
                    JSONObject jsonObject;
                    try {
                        jsonObject = new JSONObject(json);
                        int status = jsonObject.getInt("status");
                        String message = jsonObject.getString("message");
                        if (status == 0) {
                            JSONObject result = jsonObject.getJSONObject("result");
                            final String id = result.getString("id");
//                            final String nickname = result.getString("nickname");
//                            final String portrait = result.getString("portrait");

                            NewUserManager userManager = new NewUserManager(getApplicationContext());
                            JSONArray list = jsonObject.getJSONArray("list");
                            if(list != null && list.length() > 0) {
                                for(int i = 0; i < list.length(); i++) {
                                    JSONObject tmpJson = list.getJSONObject(i);
                                    String item_uid = tmpJson.getString("id");
                                    String item_nickname = tmpJson.getString("nickname");
                                    String item_portrait = tmpJson.getString("portrait");
//                                    UserManager userManager = new UserManager(getApplicationContext());
                                    if(item_portrait != null && !TextUtils.isEmpty(item_portrait) && !"null".equals(item_portrait)) {

                                        userManager.addRecord(item_uid,item_nickname,item_portrait);
                                    } else {
                                        userManager.addRecord(item_uid,item_nickname,"");
                                    }
                                }
                            }

                            Cursor cursor = userManager.queryByUid(id);

                            if(cursor != null && cursor.getCount() > 0 && cursor.moveToFirst()) {
                                final String nickname = cursor.getString(cursor.getColumnIndex(AppContract.UserEntry.COLUMN_NAME_NICK_NAME));
                                final String portrait = cursor.getString(cursor.getColumnIndex(AppContract.UserEntry.COLUMN_NAME_PORTRAIT));
                                mDelivery.post(new Runnable() {
                                    @Override
                                    public void run() {

                                        Toast.makeText(getApplicationContext(), R.string.pb_message_login_ok, Toast.LENGTH_SHORT).show();
                                        ll_progress_bar_container.setVisibility(View.GONE);

                                        sharedPreferencesHelper.putStringValue(AppConstants.KEY_SYS_CURRENT_UID,id);
                                        sharedPreferencesHelper.putStringValue(AppConstants.KEY_SYS_CURRENT_NICKNAME, nickname);
                                        sharedPreferencesHelper.putStringValue(AppConstants.KEY_SYS_CURRENT_PORTRAIT, portrait);

                                        Intent intent = new Intent();
                                        intent.setClass(getApplicationContext(), HomeActivity.class);
                                        startActivity(intent);
                                        finish();

                                    }
                                });
                            } else {
                                mDelivery.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(getApplicationContext(), R.string.pb_message_login_failure, Toast.LENGTH_SHORT).show();
                                        ll_progress_bar_container.setVisibility(View.GONE);
                                    }
                                });
                            }
                        } else {
                            mDelivery.post(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getApplicationContext(), R.string.pb_message_login_failure, Toast.LENGTH_SHORT).show();
                                    ll_progress_bar_container.setVisibility(View.GONE);
                                }
                            });
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();

                        Log.d(TAG, "LOGIN_URL return " + e.getMessage());
                    }

                }
                });
        }
    }

    public void register(View view) {
        Intent intent = new Intent();
        intent.setClass(getApplicationContext(),RegisterActivity.class);
        startActivity(intent);
        finish();
    }
}
