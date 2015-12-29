package net.pingfang.signalr.chat.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import net.pingfang.signalr.chat.R;
import net.pingfang.signalr.chat.constant.app.AppConstants;
import net.pingfang.signalr.chat.fragment.InfoRegFragment;
import net.pingfang.signalr.chat.fragment.PhoneFragment;
import net.pingfang.signalr.chat.listener.OnRegisterInteractionListener;
import net.pingfang.signalr.chat.net.HttpBaseCallback;
import net.pingfang.signalr.chat.net.OkHttpCommonUtil;
import net.pingfang.signalr.chat.ui.dialog.SingleButtonDialogFragment;
import net.pingfang.signalr.chat.util.GlobalApplication;
import net.pingfang.signalr.chat.util.SharedPreferencesHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener,OnRegisterInteractionListener{

    public static final String TAG = RegisterActivity.class.getSimpleName();

    public static final String VC_LOAD_URL = "http://api.hale.com/1100";
    public static final String VC_LOAD_KEY_PHONE = "phone";
    public static final String VC_SUBMIT_URL = "http://api.hale.com/1200";
    public static final String VC_SUBMIT_KEY_PHONE = "phone";
    public static final String VC_SUBMIT_KEY_CODE = "vcode";

    public static final String VALIDATE_PHONE_URL = GlobalApplication.URL_WEB_API_HOST + "/api/WebAPI/User/CheckPhone";
    public static final String VALIDATE_PHONE_KEY_PHONE_NO = "phone";

    public static final String SUBMIT_REG_INFORMATION_URL = GlobalApplication.URL_WEB_API_HOST + "/api/WebAPI/User/Register";
    public static final String SUBMIT_REG_INFORMATION_KEY_PHONE = "phone";
    public static final String SUBMIT_REG_INFORMATION_KEY_NICKNAME = "nickname";
    public static final String SUBMIT_REG_INFORMATION_KEY_PASSWORD = "password";
    public static final String SUBMIT_REG_INFORMATION_KEY_EMAIL = "email";
    public static final String SUBMIT_REG_INFORMATION_KEY_QQ = "qq";
    public static final String SUBMIT_REG_INFORMATION_KEY_GENDER = "sex";


    public static final int STEP_1 = 1;
    public static final int STEP_2 = 2;

    TextView btn_step_previous;
    TextView btn_step_next;

    FrameLayout fl_container_reg;

    LinearLayout ll_progress_bar_container;
    ProgressBar pb_operation;
    TextView tv_pb_operation;

    int requestStep = STEP_1;

    private Handler mDelivery;
    private SharedPreferencesHelper sharedPreferencesHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mDelivery = new Handler(Looper.getMainLooper());
        sharedPreferencesHelper = SharedPreferencesHelper.newInstance(getApplicationContext());

        initView();
        initFragment();
    }

    private void initView() {
        btn_step_previous = (TextView) findViewById(R.id.btn_step_previous);
        btn_step_previous.setOnClickListener(this);
        btn_step_next = (TextView) findViewById(R.id.btn_step_next);
        btn_step_next.setOnClickListener(this);

        fl_container_reg = (FrameLayout) findViewById(R.id.fl_container_reg);
        ll_progress_bar_container = (LinearLayout) findViewById(R.id.ll_progress_bar_container);
        pb_operation = (ProgressBar) findViewById(R.id.pb_operation);
        tv_pb_operation = (TextView) findViewById(R.id.tv_pb_operation);

    }

    private void initFragment() {
        if(requestStep == STEP_1) {
            btn_step_previous.setText(R.string.btn_activity_back);
            PhoneFragment phoneFragment = PhoneFragment.newInstance();
            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            ft.replace(R.id.fl_container_reg, phoneFragment, "PhoneFragment");
            ft.commit();
        }
    }

    @Override
    public void onClick(View view) {
        int viewId = view.getId();
        switch(viewId) {
            case R.id.btn_step_previous:
                navBack();
                break;
            case R.id.btn_step_next:
                if(requestStep == STEP_1) {
                    PhoneFragment fragment = (PhoneFragment) getSupportFragmentManager().findFragmentByTag("PhoneFragment");
//                    fragment.submitCode();
//                    fragment.validatePhone();

//                    requestStep = STEP_2;
//                    btn_step_previous.setText(R.string.btn_step_previous);
//                    InfoRegFragment infoFragment = InfoRegFragment.newInstance("18576685313");
//                    FragmentManager fm = getSupportFragmentManager();
//                    FragmentTransaction ft = fm.beginTransaction();
//                    ft.replace(R.id.fl_container_reg,infoFragment,"InfoRegFragment");
//                    ft.commit();

                    boolean isNext = fragment.isNextStep();
                    if(isNext) {
                        nextPage();
                    }
//                    fragment.validatePhoneNo();

                } else {
                    InfoRegFragment infoFragment = (InfoRegFragment) getSupportFragmentManager().findFragmentByTag("InfoRegFragment");
                    infoFragment.submitInfo();
                }
                break;
        }
    }

    public void nextPage() {
        requestStep = STEP_2;
        btn_step_previous.setText(R.string.btn_step_previous);
        String tmpPhone = sharedPreferencesHelper.getStringValue(AppConstants.KEY_SYS_PHONE_REG);
        InfoRegFragment infoFragment = InfoRegFragment.newInstance(tmpPhone);
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.fl_container_reg, infoFragment, "InfoRegFragment");
        ft.commit();
    }

    private void navBack() {
        if(requestStep == STEP_1) {
            Intent intent = new Intent();
            intent.setClass(getApplicationContext(),LoginActivity.class);
            startActivity(intent);
            finish();
        } else {
            requestStep = STEP_1;
            initFragment();
        }
    }

    public void validate(String phoneNo) {
        ll_progress_bar_container.setVisibility(View.VISIBLE);
        tv_pb_operation.setText(R.string.pb_messsage_validate_phone);
        OkHttpCommonUtil okHttpCommonUtil = OkHttpCommonUtil.newInstance(getApplicationContext());
        okHttpCommonUtil.getRequest(VALIDATE_PHONE_URL, new OkHttpCommonUtil.Param[]{
                new OkHttpCommonUtil.Param(VALIDATE_PHONE_KEY_PHONE_NO, phoneNo)
        }, new HttpBaseCallback() {

            @Override
            public void onFailure(Request request, IOException e) {
                super.onFailure(request, e);
                mDelivery.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), R.string.pb_message_validate_phone_failure, Toast.LENGTH_SHORT).show();
                        ll_progress_bar_container.setVisibility(View.GONE);
                    }
                });
            }

            @Override
            public void onResponse(Response response) throws IOException {
                String json = response.body().string();
                Log.d(TAG, "VALIDATE_PHONE_URL return " + json);
                JSONObject jsonObject;
                try {
                    jsonObject = new JSONObject(json);
                    int status = jsonObject.getInt("status");
                    String message = jsonObject.getString("message");
                    if (status == 0) {
                        JSONObject result = jsonObject.getJSONObject("result");
                        final String phone = result.getString("phone");
                        mDelivery.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(), R.string.pb_message_validate_phone_ok, Toast.LENGTH_SHORT).show();
                                ll_progress_bar_container.setVisibility(View.GONE);
                                requestStep = STEP_2;
                                btn_step_previous.setText(R.string.btn_step_previous);
                                InfoRegFragment infoFragment = InfoRegFragment.newInstance(phone);
                                FragmentManager fm = getSupportFragmentManager();
                                FragmentTransaction ft = fm.beginTransaction();
                                ft.replace(R.id.fl_container_reg, infoFragment, "InfoRegFragment");
                                ft.commit();
                            }
                        });
                    } else {
                        mDelivery.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(), R.string.pb_message_validate_phone_failure, Toast.LENGTH_SHORT).show();
                                ll_progress_bar_container.setVisibility(View.GONE);
                            }
                        });
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.d(TAG, "SUBMIT_REG_INFORMATION_URL return " + e.getMessage());
                }
            }
        });

    }

    /**
     * 实现获取验证码功能
     * @param phoneNo 注册使用的手机号码
     */
    @Override
    public void loadCode(String phoneNo) {
        OkHttpCommonUtil okHttpCommonUtil = OkHttpCommonUtil.newInstance(getApplicationContext());
        okHttpCommonUtil.postRequest(VC_LOAD_URL, new OkHttpCommonUtil.Param[]{
                new OkHttpCommonUtil.Param(VC_LOAD_KEY_PHONE,phoneNo)
        }, new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {

            }

            @Override
            public void onResponse(Response response) throws IOException {
                String json = response.body().string();
                JSONObject jsonObject;
                try {
                    jsonObject = new JSONObject(json);
                    int status = jsonObject.getInt("status");
                    String message = jsonObject.getString("message");
                    if (status == 0) {
                        JSONObject result = jsonObject.getJSONObject("result");
                        final String phone = result.getString("phone");
                        mDelivery.post(new Runnable() {
                            @Override
                            public void run() {
                                String message = getString(R.string.dialog_message_captcha, phone);
                                SingleButtonDialogFragment dialogFragment = SingleButtonDialogFragment.newInstance(message);
                                dialogFragment.show(getSupportFragmentManager(), "SingleButtonDialogFragment");
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
     *
     * @param phoneNo 手机号码
     * @param vc  通过短信获取的验证码
     */
    @Override
    public void submitCode(String phoneNo, String vc) {
        OkHttpCommonUtil okHttpCommonUtil = OkHttpCommonUtil.newInstance(getApplicationContext());
        okHttpCommonUtil.getRequest(VC_SUBMIT_URL, new OkHttpCommonUtil.Param[]{
                new OkHttpCommonUtil.Param(VC_SUBMIT_KEY_PHONE, phoneNo),
                new OkHttpCommonUtil.Param(VC_SUBMIT_KEY_CODE, vc)
        }, new HttpBaseCallback() {

            @Override
            public void onResponse(Response response) throws IOException {
                String json = response.body().string();
                JSONObject jsonObject;
                try {
                    jsonObject = new JSONObject(json);
                    int status = jsonObject.getInt("status");
                    String message = jsonObject.getString("message");
                    if (status == 0) {
                        JSONObject result = jsonObject.getJSONObject("result");
                        final String phone = result.getString("phone");
                        mDelivery.post(new Runnable() {
                            @Override
                            public void run() {
                                requestStep = STEP_2;
                                btn_step_previous.setText(R.string.btn_step_previous);
                                InfoRegFragment infoFragment = InfoRegFragment.newInstance(phone);
                                FragmentManager fm = getSupportFragmentManager();
                                FragmentTransaction ft = fm.beginTransaction();
                                ft.replace(R.id.fl_container_reg, infoFragment, "InfoRegFragment");
                                ft.commit();
                            }
                        });
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

    }

    @Override
    public void submitInfo(String... args) {
        String phone = args[0];
        String nickname = args[1];
        String password = args[2];
        String gender = args[3];
        ll_progress_bar_container.setVisibility(View.VISIBLE);
        tv_pb_operation.setText(R.string.pb_message_submit_infor);
        OkHttpCommonUtil okHttpCommonUtil = OkHttpCommonUtil.newInstance(getApplicationContext());
        okHttpCommonUtil.getRequest(SUBMIT_REG_INFORMATION_URL, new OkHttpCommonUtil.Param[]{
                new OkHttpCommonUtil.Param(SUBMIT_REG_INFORMATION_KEY_PHONE, phone),
                new OkHttpCommonUtil.Param(SUBMIT_REG_INFORMATION_KEY_NICKNAME, nickname),
                new OkHttpCommonUtil.Param(SUBMIT_REG_INFORMATION_KEY_PASSWORD, password),
                new OkHttpCommonUtil.Param(SUBMIT_REG_INFORMATION_KEY_GENDER, gender)
        }, new HttpBaseCallback() {

            @Override
            public void onFailure(Request request, IOException e) {
                super.onFailure(request, e);

                mDelivery.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), R.string.pb_message_submit_infor_failure, Toast.LENGTH_SHORT).show();
                        ll_progress_bar_container.setVisibility(View.GONE);
                    }
                });
            }

            @Override
            public void onResponse(Response response) throws IOException {
                String json = response.body().string();
                Log.d(TAG,"SUBMIT_REG_INFORMATION_URL return " + json);
                JSONObject jsonObject;
                try {
                    jsonObject = new JSONObject(json);
                    int status = jsonObject.getInt("status");
                    String message = jsonObject.getString("message");
                    if (status == 0) {
                        mDelivery.post(new Runnable() {
                            @Override
                            public void run() {
                                ll_progress_bar_container.setVisibility(View.GONE);
                                Toast.makeText(getApplicationContext(), R.string.pb_message_submit_infor_ok, Toast.LENGTH_SHORT).show();

                                Intent intent = new Intent();
                                intent.setClass(getApplicationContext(), LoginActivity.class);
                                startActivity(intent);
                                finish();
                            }
                        });
                    } else {
                        mDelivery.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(), R.string.pb_message_submit_infor_failure, Toast.LENGTH_SHORT).show();
                                ll_progress_bar_container.setVisibility(View.GONE);
                            }
                        });
                    }
                } catch (JSONException e) {
                    e.printStackTrace();

                    Log.d(TAG, "SUBMIT_REG_INFORMATION_URL return " + e.getMessage());
                }
            }
        });

    }

    @Override
    public void onBackPressed() {
        navBack();
    }

}
