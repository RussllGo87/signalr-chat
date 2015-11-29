package net.pingfang.signalr.chat.activity;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.content.IntentCompat;
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

import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.model.LatLng;
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
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.SendAuth;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;

import net.pingfang.signalr.chat.R;
import net.pingfang.signalr.chat.constant.app.AppConstants;
import net.pingfang.signalr.chat.constant.qq.TencentConstants;
import net.pingfang.signalr.chat.constant.wechat.WxConstants;
import net.pingfang.signalr.chat.constant.wechat.WxOauth2AccessToken;
import net.pingfang.signalr.chat.constant.weibo.WeiboConstants;
import net.pingfang.signalr.chat.constant.weibo.WeiboRequestListener;
import net.pingfang.signalr.chat.location.LocationListenerImpl;
import net.pingfang.signalr.chat.location.LocationNotify;
import net.pingfang.signalr.chat.net.HttpBaseCallback;
import net.pingfang.signalr.chat.net.OkHttpCommonUtil;
import net.pingfang.signalr.chat.net.ResultCallbackImpl;
import net.pingfang.signalr.chat.ui.dialog.SingleButtonDialogFragment;
import net.pingfang.signalr.chat.util.CommonTools;
import net.pingfang.signalr.chat.util.GlobalApplication;
import net.pingfang.signalr.chat.util.MediaFileUtils;
import net.pingfang.signalr.chat.util.SharedPreferencesHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class LoginActivity extends AppCompatActivity implements LocationNotify{

    public static final String TAG = LoginActivity.class.getSimpleName();

    public static final String LOGIN_URL = GlobalApplication.URL_WEB_API_HOST + "/api/WebAPI/User/Login";
    public static final String LOGIN_KEY_ACCOUNT = "account";
    public static final String LOGIN_KEY_PASSWORD = "password";

//    public static final String NEW_LOGIN_URL = GlobalApplication.URL_WEB_API_HOST + "/api/WebAPI/User/thirdLogin";
    public static final String NEW_LOGIN_URL = GlobalApplication.URL_WEB_API_HOST + "/api/WebAPI/User/thirdLogin";
    public static final String NEW_LOGIN_KEY_TID = "key";
    public static final String NEW_LOGIN_KEY_WID = "key";
    public static final String NEW_LOGIN_KEY_WXID = "key";
    public static final String NEW_LOGIN_KEY_NICK_NAME = "nickname";
    public static final String NEW_LOGIN_KEY_PORTRAIT = "pic";

    public static final String NEW_LOGIN_PARAM_PLATFORM_QQ = "qq";
    public static final String NEW_LOGIN_PARAM_PLATFORM_WEIBO = "weibo";
    public static final String NEW_LOGIN_PARAM_PLATFORM_WECHAT = "wechat";

    public static final String URL_ACCOUNT_INFO_LOAD = GlobalApplication.URL_WEB_API_HOST + "/api/WebAPI/User/GetUser";
    public static final String KEY_URL_ACCOUNT_INFO_LOAD_UID = "id";

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
    SharedPreferencesHelper sharedPreferencesHelper;
    String savedAccount;
    // 腾讯qq登录相关
    // 腾讯qq实例
    Tencent mTencent;

    // 微博登录相关参数
    // 微信登录相关
    WxOauth2AccessToken mWxOauth2AccessToken;
    IWXAPI api;
    int currentClickViewId = 0;
    private Handler mDelivery;
    /**
     * 微博 Web 授权类，提供登陆等功能
     */
    private WeiboAuth mWeiboAuth;
    /**
     * 封装了 "access_token"，"expires_in"，"refresh_token"，并提供了他们的管理功能
     */
    private Oauth2AccessToken mAccessToken;
    /**
     * 注意：SsoHandler 仅当 SDK 支持 SSO 时有效
     */
    private SsoHandler mWeiboSsoHandler;
    private LatLng currentLatLng;
    private LocationClient locationClient;
    private LocationListenerImpl locationListener;
    private IUiListener loginListener = new IUiListener() {
        @Override
        public void onComplete(Object response) {
            if (null == response) {
                ll_progress_bar_container.setVisibility(View.GONE);
                SingleButtonDialogFragment dialogFragment = SingleButtonDialogFragment.newInstance("登录失败", "返回为空");
                dialogFragment.show(getSupportFragmentManager(), "SingleButtonDialogFragment");
                return;
            }

            JSONObject jsonResponse = (JSONObject) response;
            if (jsonResponse.length() == 0) {
                ll_progress_bar_container.setVisibility(View.GONE);
                SingleButtonDialogFragment dialogFragment = SingleButtonDialogFragment.newInstance("登录失败", "返回为空");
                dialogFragment.show(getSupportFragmentManager(), "SingleButtonDialogFragment");
                return;
            }

            tv_pb_operation.setText("登录qq成功");
            Toast.makeText(getApplicationContext(), getString(R.string.weibosdk_demo_toast_auth_success), Toast.LENGTH_SHORT).show();
            doComplete(jsonResponse);
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

                    tv_pb_operation.setText("获取qq个人信息");
                    loadQQAccountInfo();

                }
            } catch (Exception e) {
                //                Toast.makeText(getApplicationContext(),getString(R.string.weibosdk_demo_toast_auth_failed),Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onError(UiError uiError) {
            mDelivery.post(new Runnable() {
                @Override
                public void run() {
                    ll_progress_bar_container.setVisibility(View.GONE);
                    Toast.makeText(getApplicationContext(), getString(R.string.weibosdk_demo_toast_auth_failed), Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override
        public void onCancel() {
            mDelivery.post(new Runnable() {
                @Override
                public void run() {
                    ll_progress_bar_container.setVisibility(View.GONE);
                    Toast.makeText(getApplicationContext(), getString(R.string.weibosdk_demo_toast_auth_canceled), Toast.LENGTH_SHORT).show();
                }
            });
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mDelivery = new Handler(Looper.getMainLooper());
        sharedPreferencesHelper = SharedPreferencesHelper.newInstance(getApplicationContext());
        savedAccount = sharedPreferencesHelper.getStringValue("account");

        initLoginConfig();
        initLocation();
        initView();
        initValues();

        // 注册微信API
        api = WXAPIFactory.createWXAPI(getApplicationContext(), WxConstants.APP_ID, true);
        api.registerApp(WxConstants.APP_ID);
    }

    @Override
    protected void onStart() {
        super.onStart();

        loadWxLoginCode();
    }

    private void loadWxLoginCode() {
        Intent intent = getIntent();
        String code = intent.getStringExtra("accessCode");
        if (!TextUtils.isEmpty(code)) {
            getWxAccessToken(code);
        }
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

    /**
     * 初始化定位设置并开始定位
     */
    public void initLocation() {
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        option.setCoorType("bd09ll");
        option.setOpenGps(true);
        option.setIsNeedAddress(true);
        option.setIgnoreKillProcess(false);

        locationClient = new LocationClient(getApplicationContext(),option);
        locationListener = new LocationListenerImpl(this);
        locationClient.registerLocationListener(locationListener);
        locationClient.start();
    }

    @Override
    public void updateLoc(BDLocation bdLocation) {
        if (bdLocation == null)
            return;

        currentLatLng = new LatLng(bdLocation.getLatitude(),
                bdLocation.getLongitude());

        sharedPreferencesHelper.putStringValue(AppConstants.KEY_SYS_LOCATION_LAT, Double.toString(currentLatLng.latitude));
        sharedPreferencesHelper.putStringValue(AppConstants.KEY_SYS_LOCATION_LNG, Double.toString(currentLatLng.longitude));

        String uid = sharedPreferencesHelper.getStringValue(AppConstants.KEY_SYS_CURRENT_UID);
        if(!TextUtils.isEmpty(uid)) {
            Intent intent = new Intent();
            intent.setClass(getApplicationContext(), HomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | IntentCompat.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
    }

    private void initValues() {
        String currentPhone = sharedPreferencesHelper.getStringValue(AppConstants.KEY_SYS_CURRENT_USER_PHONE);
        if(!TextUtils.isEmpty(currentPhone)) {
            et_login_no.setText(currentPhone);
        }
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
                    ll_progress_bar_container.setVisibility(View.VISIBLE);
                    tv_pb_operation.setText("登录qq");
                    mTencent.login(LoginActivity.this, TencentConstants.SCOPE, loginListener);
                }
            }
        });
        btn_login_pattern_wechat = (ImageView) findViewById(R.id.btn_login_pattern_wechat);
        btn_login_pattern_wechat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currentClickViewId = view.getId();
                if(api.isWXAppInstalled()) {
                    final SendAuth.Req req = new SendAuth.Req();
                    req.scope = WxConstants.SCOPE;
                    req.state = "signal_r_chat";
                    api.sendReq(req);
                } else {
                    Toast.makeText(getApplicationContext(), R.string.error_msg_wechat_not_installed, Toast.LENGTH_SHORT);
                }
            }
        });
        btn_login_pattern_weibo = (ImageView) findViewById(R.id.btn_login_pattern_weibo);
        btn_login_pattern_weibo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                currentClickViewId = view.getId();

                ll_progress_bar_container.setVisibility(View.VISIBLE);
                tv_pb_operation.setText("登录微博");

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
                        tv_pb_operation.setText("登录微博异常");
                        ll_progress_bar_container.setVisibility(View.GONE);
                        Toast.makeText(getApplicationContext(),
                                "Auth exception : " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onCancel() {
                        tv_pb_operation.setText("用户取消登录");
                        ll_progress_bar_container.setVisibility(View.GONE);
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
        tv_pb_operation.setText("加载微博个人信息");
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

                        final String tmpProfile = profileImageUrl;

                        mDelivery.post(new Runnable() {
                            @Override
                            public void run() {
                                tv_pb_operation.setText("微博个人信息加载成功");
                                login(NEW_LOGIN_PARAM_PLATFORM_WEIBO, tmpProfile);
                            }
                        });

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onIOException(IOException e) {
                super.onIOException(e);

                mDelivery.post(new Runnable() {
                    @Override
                    public void run() {
                        ll_progress_bar_container.setVisibility(View.GONE);
                        Toast.makeText(getApplicationContext(), "微博个人信息加载失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onError(WeiboException e) {
                super.onError(e);

                mDelivery.post(new Runnable() {
                    @Override
                    public void run() {
                        ll_progress_bar_container.setVisibility(View.GONE);
                        Toast.makeText(getApplicationContext(), "微博个人信息加载失败", Toast.LENGTH_SHORT).show();
                    }
                });

            }
        });
    }

    private void loadQQAccountInfo() {
        UserInfo userInfo = new UserInfo(getApplicationContext(),
                QQAuth.createInstance(TencentConstants.APP_ID, getApplicationContext()),
                mTencent.getQQToken());

        userInfo.getUserInfo(new IUiListener() {
            @Override
            public void onComplete(Object response) {
                if (null == response) {
                    Toast.makeText(getApplicationContext(), getString(R.string.resp_return_empty), Toast.LENGTH_SHORT).show();
                    tv_pb_operation.setText("qq个人信息为空");
                    return;
                }

                JSONObject jsonResponse = (JSONObject) response;
                if (jsonResponse.length() == 0) {
                    Toast.makeText(getApplicationContext(), getString(R.string.resp_return_empty), Toast.LENGTH_SHORT).show();
                    tv_pb_operation.setText("qq个人信息为空");
                    return;
                }

                tv_pb_operation.setText("qq个人信息获取成功");
                doComplete(jsonResponse);
            }

            public void doComplete(JSONObject jsonObject) {
                try {
                    String nickname = jsonObject.getString(TencentConstants.PARAM_NICK_NAME);
                    final String figureurl_qq_1 = jsonObject.getString(TencentConstants.PARAM_QQ_PORTRAIT);
                    sharedPreferencesHelper.putStringValue(TencentConstants.KEY_QQ_NICK_NAME, nickname);
                    sharedPreferencesHelper.putStringValue(TencentConstants.KEY_QQ_PORTRAIT, figureurl_qq_1);
                    mDelivery.post(new Runnable() {
                        @Override
                        public void run() {
                            login(NEW_LOGIN_PARAM_PLATFORM_QQ, figureurl_qq_1);
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

    private void getWxAccessToken(String accessCode) {

        ll_progress_bar_container.setVisibility(View.VISIBLE);
        tv_pb_operation.setText("获取微信access_token");

        OkHttpCommonUtil okHttp = OkHttpCommonUtil.newInstance(getApplicationContext());
        okHttp.postRequest(
                "https://api.weixin.qq.com/sns/oauth2/access_token",
                new OkHttpCommonUtil.Param[]{
                        new OkHttpCommonUtil.Param("appid", WxConstants.APP_ID),
                        new OkHttpCommonUtil.Param("secret", WxConstants.APP_SECRET),
                        new OkHttpCommonUtil.Param("code", accessCode),
                        new OkHttpCommonUtil.Param("grant_type", "authorization_code")
                },
                new HttpBaseCallback() {

                    @Override
                    public void onResponse(Response response) throws IOException {
                        String body = response.body().string();
                        mWxOauth2AccessToken = WxOauth2AccessToken.parseAccessToken(body);
                        if (mWxOauth2AccessToken != null && mWxOauth2AccessToken.isSessionValid()) {
                            // 保存 Token 到 SharedPreferences
                            SharedPreferencesHelper.writeAccessToken(mWxOauth2AccessToken);
                            mDelivery.post(new Runnable() {
                                @Override
                                public void run() {
                                    loadWxAccountInfo();
                                }
                            });

                        } else { // 授权失败
                            Log.d(TAG, "body == " + body);
                            mDelivery.post(new Runnable() {
                                @Override
                                public void run() {
                                    ll_progress_bar_container.setVisibility(View.GONE);
                                    Toast.makeText(getApplicationContext(), "微信access_token获取异常", Toast.LENGTH_SHORT).show();
                                }
                            });

                        }
                    }
                });
    }

    private void loadWxAccountInfo() {

        tv_pb_operation.setText("获取微信用户个人信息");

        String openId = WxConstants.APP_ID;
        String accessToken = mWxOauth2AccessToken.getToken();
        OkHttpCommonUtil okHttp = OkHttpCommonUtil.newInstance(getApplicationContext());
        okHttp.postRequest("https://api.weixin.qq.com/sns/userinfo",
                new OkHttpCommonUtil.Param[]{
                        new OkHttpCommonUtil.Param("access_token", accessToken),
                        new OkHttpCommonUtil.Param("openid", openId)
                },
                new HttpBaseCallback() {
                    @Override
                    public void onResponse(Response response) throws IOException {
                        String body = response.body().string();
                        Log.d(TAG, " WX ACCOUNT INFO return == " + body);
                        JSONObject jsonObject;
                        try {
                            jsonObject = new JSONObject(body);
                            String nickname = jsonObject.getString(WxConstants.PARAM_WX_NICKNAME);
                            final String headimgurl = jsonObject.getString(WxConstants.PARAM_WX_HEAD_IMG_URL);

                            sharedPreferencesHelper.putStringValue(WxConstants.KEY_WX_NICKNAME, nickname);
                            sharedPreferencesHelper.putStringValue(WxConstants.KEY_WX_HEAD_IMG_URL, headimgurl);

                            mDelivery.post(new Runnable() {
                                @Override
                                public void run() {
                                    login(NEW_LOGIN_PARAM_PLATFORM_WECHAT, headimgurl);
                                }
                            });

                        } catch (Exception e) {  // 加载微信个人信息失败
                            e.printStackTrace();
                            mDelivery.post(new Runnable() {
                                @Override
                                public void run() {
                                    ll_progress_bar_container.setVisibility(View.GONE);
                                    Toast.makeText(getApplicationContext(), "加载微信个人信息失败", Toast.LENGTH_SHORT).show();
                                }
                            });

                        }
                    }
                }
        );
    }

    private void downloadPortrait(final String platform,String portraitUrl) {

        String portraitDest = MediaFileUtils.genarateFilePath(getApplicationContext(),
                Environment.DIRECTORY_PICTURES, "Portrait", "jpg");
        OkHttpCommonUtil okHttp = OkHttpCommonUtil.newInstance(getApplicationContext());
        okHttp.downloadFileAsync(portraitUrl, portraitDest, new ResultCallbackImpl<String>() {
            @Override
            public void onError(Request request, Exception e) {
                Log.e(TAG, e.getMessage());
            }

            @Override
            public void onResponse(String response) {
                String path = response;
                String base64File = CommonTools.fileToBase64(path);
                login(platform, base64File);
            }

            @Override
            public void publishProgress(long dowloaded, long target) {
                Log.d(TAG, "total == " + target + " downloaded ==" + dowloaded);
            }
        });
    }

    private void login(String platform, String base64File) {
        tv_pb_operation.setText("向服务器注册个人信息");
        OkHttpCommonUtil.Param[] params = new OkHttpCommonUtil.Param[0];
        if(!TextUtils.isEmpty(platform)) {
            if(platform.equals(NEW_LOGIN_PARAM_PLATFORM_QQ)) {
                if(mTencent.isSessionValid()) {
                    params = new OkHttpCommonUtil.Param[] {
                            new OkHttpCommonUtil.Param(NEW_LOGIN_KEY_TID,mTencent.getOpenId()),
                            new OkHttpCommonUtil.Param(NEW_LOGIN_KEY_NICK_NAME,
                                    sharedPreferencesHelper.getStringValue(TencentConstants.KEY_QQ_NICK_NAME)),
                            new OkHttpCommonUtil.Param(NEW_LOGIN_KEY_PORTRAIT, base64File)
                    };
                }

            } else if(platform.equals(NEW_LOGIN_PARAM_PLATFORM_WEIBO)) {
                if(mAccessToken.isSessionValid()) {
                    params = new OkHttpCommonUtil.Param[]{
                            new OkHttpCommonUtil.Param(NEW_LOGIN_KEY_WID,mAccessToken.getUid()),
                            new OkHttpCommonUtil.Param(NEW_LOGIN_KEY_NICK_NAME,
                                    sharedPreferencesHelper.getStringValue(WeiboConstants.KEY_WB_SCREEN_NAME)),
                            new OkHttpCommonUtil.Param(NEW_LOGIN_KEY_PORTRAIT,base64File)
                    };
                } else {
                    return;
                }

            } else if(platform.equals(NEW_LOGIN_PARAM_PLATFORM_WECHAT)) {
                    params = new OkHttpCommonUtil.Param[]{
                            new OkHttpCommonUtil.Param(NEW_LOGIN_KEY_WXID,mWxOauth2AccessToken.getOpenId()),
                            new OkHttpCommonUtil.Param(NEW_LOGIN_KEY_NICK_NAME,
                                    sharedPreferencesHelper.getStringValue(WxConstants.KEY_WX_NICKNAME)),
                            new OkHttpCommonUtil.Param(NEW_LOGIN_KEY_PORTRAIT,base64File)
                    };
            } else {
                return;
            }
        }

        OkHttpCommonUtil okHttp = OkHttpCommonUtil.newInstance(getApplicationContext());
        okHttp.getRequest(NEW_LOGIN_URL, params, new HttpBaseCallback() {
            @Override
            public void onFailure(Request request, IOException e) {
                Log.d("HttpBaseCallback", e.getMessage());
            }

            @Override
            public void onResponse(Response response) throws IOException {
                String json = response.body().string();
                Log.d(TAG, "NEW_LOGIN_URL return " + json);
                JSONObject jsonObject;
                try {
                    jsonObject = new JSONObject(json);
                    int status = -1;
                    status = jsonObject.getInt("status");
                    String message = jsonObject.getString("message");
                    if (status == 0) {
                        JSONObject result = jsonObject.getJSONObject("result");
                        final String id = result.getString("id");
                        mDelivery.post(new Runnable() {
                            @Override
                            public void run() {
                                tv_pb_operation.setText(R.string.pb_message_login_now);
                                tv_pb_operation.setText(R.string.pb_message_load_info);
                                loadAccountInfo(id, false);
                            }
                        });


                        //                        final int exp = result.getInt("exp");
                        //
                        //                        UserManager userManager = new UserManager(getApplicationContext());
                        //                        JSONArray list = jsonObject.getJSONArray("list");
                        //                        if (list != null && list.length() > 0) {
                        //                            for (int i = 0; i < list.length(); i++) {
                        //                                JSONObject tmpJson = list.getJSONObject(i);
                        //                                String item_uid = tmpJson.getString("id");
                        //                                String item_nickname = tmpJson.getString("nickname");
                        //                                String item_portrait = tmpJson.getString("portrait");
                        //                                if (item_portrait != null && !TextUtils.isEmpty(item_portrait) && !"null".equals(item_portrait)) {
                        //                                    userManager.addRecord(item_uid, item_nickname, item_portrait);
                        //                                } else {
                        //                                    userManager.addRecord(item_uid, item_nickname, "");
                        //                                }
                        //                            }
                        //                        }
                        //
                        //                        Cursor cursor = userManager.queryByUid(id);
                        //
                        //                        if (cursor != null && cursor.getCount() > 0 && cursor.moveToFirst()) {
                        //                            final String nickname = cursor.getString(cursor.getColumnIndex(AppContract.UserEntry.COLUMN_NAME_NICK_NAME));
                        //                            final String portrait = cursor.getString(cursor.getColumnIndex(AppContract.UserEntry.COLUMN_NAME_PORTRAIT));
                        //                            mDelivery.post(new Runnable() {
                        //                                @Override
                        //                                public void run() {
                        //                                    ll_progress_bar_container.setVisibility(View.GONE);
                        //                                    Toast.makeText(getApplicationContext(), R.string.pb_message_login_ok, Toast.LENGTH_SHORT).show();
                        //
                        //                                    sharedPreferencesHelper.putStringValue(AppConstants.KEY_SYS_CURRENT_UID, id);
                        //                                    sharedPreferencesHelper.putStringValue(AppConstants.KEY_SYS_CURRENT_NICKNAME, nickname);
                        //                                    sharedPreferencesHelper.putStringValue(AppConstants.KEY_SYS_CURRENT_PORTRAIT, portrait);
                        //                                    sharedPreferencesHelper.putInt(AppConstants.KEY_SYS_CURRENT_USER_EXP, exp);
                        //
                        //                                    sharedPreferencesHelper.clearKey(AppConstants.KEY_SYS_CURRENT_USER_PHONE);
                        //
                        //                                    Intent intent = new Intent();
                        //                                    intent.setClass(getApplicationContext(), HomeActivity.class);
                        //                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | IntentCompat.FLAG_ACTIVITY_CLEAR_TASK);
                        //                                    startActivity(intent);
                        //                                    finish();
                        //
                        //                                }
                        //                            });
                        //                        } else {
                        //                            mDelivery.post(new Runnable() {
                        //                                @Override
                        //                                public void run() {
                        //                                    ll_progress_bar_container.setVisibility(View.GONE);
                        //                                    Toast.makeText(getApplicationContext(), R.string.pb_message_login_failure, Toast.LENGTH_SHORT).show();
                        //                                }
                        //                            });
                        //                        }
                    } else {
                        mDelivery.post(new Runnable() {
                            @Override
                            public void run() {
                                ll_progress_bar_container.setVisibility(View.GONE);
                                Toast.makeText(getApplicationContext(), R.string.pb_message_login_failure, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    mDelivery.post(new Runnable() {
                        @Override
                        public void run() {
                            ll_progress_bar_container.setVisibility(View.GONE);
                            Toast.makeText(getApplicationContext(), getString(R.string.debug_http_response_invalid), Toast.LENGTH_LONG).show();
                        }
                    });

                    Log.e(TAG, "NEW_LOGIN_URL return " + getString(R.string.debug_http_response_invalid));
                    Log.e(TAG, "NEW_LOGIN_URL return " + e.getMessage());
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

        // 微博发起授权返回
        // SSO 授权回调
        // 重要：发起 SSO 登陆的 Activity 必须重写 onActivityResult
        if (mWeiboSsoHandler != null && currentClickViewId == btn_login_pattern_weibo.getId()) {
            mWeiboSsoHandler.authorizeCallBack(requestCode, resultCode, data);
        }

        // qq发起授权返回
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

        if(CommonTools.isPhoneNumber(account) && CommonTools.isValidPwd(password)) {
            ll_progress_bar_container.setVisibility(View.VISIBLE);
            tv_pb_operation.setText(R.string.pb_message_login_now);

            OkHttpCommonUtil okHttp = OkHttpCommonUtil.newInstance(getApplicationContext());
            okHttp.getRequest(LOGIN_URL, new OkHttpCommonUtil.Param[]{
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
                            mDelivery.post(new Runnable() {
                                @Override
                                public void run() {
                                    tv_pb_operation.setText(R.string.pb_message_login_now);
                                    tv_pb_operation.setText(R.string.pb_message_load_info);
                                    loadAccountInfo(id, true);
                                }
                            });


                            //                            final int exp = result.getInt("exp");
                            //                            UserManager userManager = new UserManager(getApplicationContext());
                            //                            JSONArray list = jsonObject.getJSONArray("list");
                            //                            if (list != null && list.length() > 0) {
                            //                                for (int i = 0; i < list.length(); i++) {
                            //                                    JSONObject tmpJson = list.getJSONObject(i);
                            //                                    String item_uid = tmpJson.getString("id");
                            //                                    String item_nickname = tmpJson.getString("nickname");
                            //                                    String item_portrait = tmpJson.getString("portrait");
                            //
                            //                                    if(item_uid.equals(id)) {
                            //                                        if (item_portrait != null && !TextUtils.isEmpty(item_portrait) && !"null".equals(item_portrait)) {
                            //                                            userManager.addRecord(item_uid, item_nickname, item_portrait);
                            //                                        } else {
                            //                                            userManager.addRecord(item_uid, item_nickname, "");
                            //                                        }
                            //                                    }
                            //                                }
                            //                            }
                            //
                            //                            Cursor cursor = userManager.queryByUid(id);
                            //                            if (cursor != null && cursor.getCount() > 0 && cursor.moveToFirst()) {
                            //                                final String nickname = cursor.getString(cursor.getColumnIndex(AppContract.UserEntry.COLUMN_NAME_NICK_NAME));
                            //                                final String portrait = cursor.getString(cursor.getColumnIndex(AppContract.UserEntry.COLUMN_NAME_PORTRAIT));
                            //                                mDelivery.post(new Runnable() {
                            //                                    @Override
                            //                                    public void run() {
                            //                                        ll_progress_bar_container.setVisibility(View.GONE);
                            //                                        Toast.makeText(getApplicationContext(), R.string.pb_message_login_ok, Toast.LENGTH_SHORT).show();
                            //
                            //                                        sharedPreferencesHelper.putStringValue(AppConstants.KEY_SYS_CURRENT_UID, id);
                            //                                        sharedPreferencesHelper.putStringValue(AppConstants.KEY_SYS_CURRENT_NICKNAME, nickname);
                            //                                        sharedPreferencesHelper.putStringValue(AppConstants.KEY_SYS_CURRENT_PORTRAIT, portrait);
                            //                                        sharedPreferencesHelper.putInt(AppConstants.KEY_SYS_CURRENT_USER_EXP, exp);
                            //
                            //                                        sharedPreferencesHelper.putStringValue(AppConstants.KEY_SYS_CURRENT_USER_PHONE,account);
                            //
                            //                                        Intent intent = new Intent();
                            //                                        intent.setClass(getApplicationContext(), HomeActivity.class);
                            //                                        startActivity(intent);
                            //                                        finish();
                            //
                            //                                    }
                            //                                });
                            //                            } else {
                            //                                mDelivery.post(new Runnable() {
                            //                                    @Override
                            //                                    public void run() {
                            //                                        ll_progress_bar_container.setVisibility(View.GONE);
                            //                                        Toast.makeText(getApplicationContext(), R.string.pb_message_login_failure, Toast.LENGTH_SHORT).show();
                            //                                    }
                            //                                });
                            //                            }
                        } else {
                            mDelivery.post(new Runnable() {
                                @Override
                                public void run() {
                                    ll_progress_bar_container.setVisibility(View.GONE);
                                    Toast.makeText(getApplicationContext(), R.string.pb_message_login_failure, Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        mDelivery.post(new Runnable() {
                            @Override
                            public void run() {
                                ll_progress_bar_container.setVisibility(View.GONE);
                                Toast.makeText(getApplicationContext(), R.string.debug_http_response_invalid, Toast.LENGTH_SHORT).show();
                            }
                        });

                        Log.d(TAG, "LOGIN_URL return " + e.getMessage());
                    }

                }
            });
        }
    }

    public void loadAccountInfo(final String uid, final boolean normalLogin) {
        OkHttpCommonUtil okHttp = OkHttpCommonUtil.newInstance(getApplicationContext());
        okHttp.getRequest(URL_ACCOUNT_INFO_LOAD,
                new OkHttpCommonUtil.Param[]{
                        new OkHttpCommonUtil.Param(KEY_URL_ACCOUNT_INFO_LOAD_UID,
                                uid)
                },
                new HttpBaseCallback() {
                    @Override
                    public void onResponse(Response response) throws IOException {
                        String result = response.body().string();
                        Log.d(TAG, "URL_ACCOUNT_INFO_LOAD return " + result);
                        JSONObject jsonObject = null;
                        try {
                            jsonObject = new JSONObject(result);
                            int status = jsonObject.getInt("status");
                            String message = jsonObject.getString("message");
                            if (status == 0) {
                                JSONObject resultJson = jsonObject.getJSONObject("result");
                                String tmpPhoneNo = "";
                                if (normalLogin) {
                                    tmpPhoneNo = resultJson.getString("phone");
                                }
                                final String phoneNo = tmpPhoneNo;
                                final String nickName = resultJson.getString("nickname");
                                final String portraitUrl = resultJson.getString("portrait");
                                final int exp = resultJson.getInt("exp");
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        tv_pb_operation.setText(R.string.toast_account_info_load_ok);
                                        ll_progress_bar_container.setVisibility(View.GONE);

                                        sharedPreferencesHelper.putStringValue(AppConstants.KEY_SYS_CURRENT_UID, uid);
                                        sharedPreferencesHelper.putStringValue(AppConstants.KEY_SYS_CURRENT_NICKNAME, nickName);
                                        sharedPreferencesHelper.putStringValue(AppConstants.KEY_SYS_CURRENT_PORTRAIT, portraitUrl);
                                        sharedPreferencesHelper.putInt(AppConstants.KEY_SYS_CURRENT_USER_EXP, exp);

                                        if (normalLogin) {
                                            sharedPreferencesHelper.putStringValue(AppConstants.KEY_SYS_CURRENT_USER_PHONE, phoneNo);
                                        } else {
                                            sharedPreferencesHelper.clearKey(AppConstants.KEY_SYS_CURRENT_USER_PHONE);
                                        }


                                        Intent intent = new Intent();
                                        intent.setClass(getApplicationContext(), HomeActivity.class);
                                        startActivity(intent);
                                        finish();

                                    }
                                });
                            } else {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(getApplicationContext(),
                                                getString(R.string.toast_account_info_load_failure),
                                                Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getApplicationContext(),
                                            getString(R.string.toast_account_info_load_failure),
                                            Toast.LENGTH_SHORT).show();

                                }
                            });

                        }
                    }
                });
    }


    public void register(View view) {
        Intent intent = new Intent();
        intent.setClass(getApplicationContext(),RegisterActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(locationClient != null && locationClient.isStarted()) {
            locationClient.stop();
        }
    }
}
