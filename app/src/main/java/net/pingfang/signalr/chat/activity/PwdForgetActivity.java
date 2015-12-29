package net.pingfang.signalr.chat.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import net.pingfang.signalr.chat.R;
import net.pingfang.signalr.chat.constant.app.AppConstants;
import net.pingfang.signalr.chat.net.HttpBaseCallback;
import net.pingfang.signalr.chat.net.OkHttpCommonUtil;
import net.pingfang.signalr.chat.util.CommonTools;
import net.pingfang.signalr.chat.util.GlobalApplication;
import net.pingfang.signalr.chat.util.SharedPreferencesHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class PwdForgetActivity extends AppCompatActivity implements View.OnClickListener{

    private static final String TAG = PwdForgetActivity.class.getSimpleName();

    public static final String URL_LOAD_VALIDATE_CODE = GlobalApplication.URL_WEB_API_HOST + "/api/WebAPI/User/RequestIdentifying";
    public static final String KEY_LOAD_VALIDATE_CODE_ACCOUNT_PHONE = "MobilePhone";
    public static final String KEY_LOAD_VALIDATE_CODE_REQUEST_TYPE = "RequestType";

    public static final String URL_PWD_FORGET = GlobalApplication.URL_WEB_API_HOST + "/api/WebAPI/User/ResetPassword";
    public static final String KEY_PWD_FORGET_ACCOUNT_PHONE = "MobilePhone";
//    public static final String KEY_PWD_FORGET_VCODE = "vcode";
    public static final String KEY_PWD_FORGET_PASSWORD_NEW = "NewPassword";

    TextView btn_activity_back;

    EditText et_pwd_update_account_phone;
    EditText et_pwd_update_account_password;
    EditText et_pwd_update_account_password_retype;
    EditText et_account_validate_code;

    TextView btn_load_validate_code;

    TextView btn_pwd_update_submit;
    TextView btn_pwd_update_cancel;

    SharedPreferencesHelper sharedPreferencesHelper;
    private Handler mHandler = new Handler(Looper.getMainLooper());
    private int i = 60;
    ClassCutThreads thread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pwd_forget);

        sharedPreferencesHelper = SharedPreferencesHelper.newInstance(getApplicationContext());
        initView();
    }

    private void initView() {
        btn_activity_back = (TextView) findViewById(R.id.btn_activity_back);
        btn_activity_back.setOnClickListener(this);

        et_pwd_update_account_phone = (EditText) findViewById(R.id.et_pwd_update_account_phone);
        et_pwd_update_account_password = (EditText) findViewById(R.id.et_pwd_update_account_password);
        et_pwd_update_account_password_retype = (EditText) findViewById(R.id.et_pwd_update_account_password_retype);
        et_account_validate_code = (EditText) findViewById(R.id.et_account_validate_code);

        btn_load_validate_code = (TextView) findViewById(R.id.btn_load_validate_code);
        btn_load_validate_code.setOnClickListener(this);

        btn_pwd_update_submit = (TextView) findViewById(R.id.btn_pwd_update_submit);
        btn_pwd_update_submit.setOnClickListener(this);
        btn_pwd_update_cancel = (TextView) findViewById(R.id.btn_pwd_update_cancel);
        btn_pwd_update_cancel.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        int viewId = view.getId();
        switch(viewId) {
            case R.id.btn_activity_back:
                navBack();
                break;
            case R.id.btn_load_validate_code:
                loadValidateCode();
                break;
            case R.id.btn_pwd_update_submit:
                updatePwd();
                break;
            case R.id.btn_pwd_update_cancel:
                navBack();
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        sharedPreferencesHelper.putStringValue(AppConstants.KEY_SYS_FP_PHONE, null);
        sharedPreferencesHelper.putStringValue(AppConstants.KEY_SYS_FP_CODE, null);
    }

    private void updatePwd() {
        String phoneNo = et_pwd_update_account_phone.getText().toString().trim();
        if(!TextUtils.isDigitsOnly(phoneNo)) {
            Toast.makeText(getApplicationContext(), R.string.toast_phone_no_error_txt, Toast.LENGTH_LONG).show();
            return;
        }
        if(phoneNo.length() != 11) {
            Toast.makeText(getApplicationContext(),R.string.toast_phone_no_error_length,Toast.LENGTH_LONG).show();
            return;
        }
        if(!CommonTools.isPhoneNumber(phoneNo)) {
            Toast.makeText(getApplicationContext(),R.string.toast_phone_no_error_invalidate,Toast.LENGTH_LONG).show();
            return;
        }

        String tmpPhone = sharedPreferencesHelper.getStringValue(AppConstants.KEY_SYS_FP_PHONE);
        if(TextUtils.isEmpty(tmpPhone)) {
            Toast.makeText(getApplicationContext(), "当前电话号码没有检验,需要重新获取验证码", Toast.LENGTH_LONG).show();
            if(thread != null && thread.isRunning) {
                thread.setIsRunning(false);
                btn_load_validate_code.setClickable(true);
            }
            return;
        }

        if(!phoneNo.equals(tmpPhone)) {
            Toast.makeText(getApplicationContext(), "你已经更换了电话号码,需要重新获取验证码", Toast.LENGTH_LONG).show();
            if(thread != null && thread.isRunning) {
                thread.setIsRunning(false);
                btn_load_validate_code.setClickable(true);
            }
            return;
        }


        String password = et_pwd_update_account_password.getText().toString().trim();
        String passwordR = et_pwd_update_account_password_retype.getText().toString().trim();
        if(TextUtils.isEmpty(password)) {
            Toast.makeText(getApplicationContext(),R.string.toast_info_reg_error_password_empty,Toast.LENGTH_LONG).show();
            return;
        }

        if (password.length() < 6) {
            Toast.makeText(getApplicationContext(), R.string.toast_info_reg_error_password_length_less_than_6, Toast.LENGTH_LONG).show();
            return;
        }

        if(TextUtils.isEmpty(passwordR)) {
            Toast.makeText(getApplicationContext(),R.string.toast_info_reg_error_passwordr_empty,Toast.LENGTH_LONG).show();
            return;
        }

        if(!password.equals(passwordR)) {
            Toast.makeText(getApplicationContext(),R.string.toast_info_reg_error_password_not_same,Toast.LENGTH_LONG).show();
            return;
        }

        String validateCode = et_account_validate_code.getText().toString().trim();
        if(TextUtils.isEmpty(validateCode)) {
            Toast.makeText(getApplicationContext(),R.string.toast_pwd_forget_validate_code_empty,Toast.LENGTH_LONG).show();
            return;
        }

        String code = sharedPreferencesHelper.getStringValue(AppConstants.KEY_SYS_FP_CODE);
        if(TextUtils.isEmpty(code)) {
            Toast.makeText(getApplicationContext(), "电话号码没有检验,需要重新获取验证码", Toast.LENGTH_LONG).show();
            if(thread != null && thread.isRunning) {
                thread.setIsRunning(false);
                btn_load_validate_code.setClickable(true);
            }
            return;
        }
        if(!validateCode.equals(code)) {
            Toast.makeText(getApplicationContext(), "验证码输出了,需要重新获取验证码", Toast.LENGTH_LONG).show();
            if(thread != null && thread.isRunning) {
                thread.setIsRunning(false);
                btn_load_validate_code.setClickable(true);
            }
            return;
        }

        if(thread != null && thread.isRunning()) {
            thread.setIsRunning(false);
        }

        OkHttpCommonUtil okHttp = OkHttpCommonUtil.newInstance(getApplicationContext());
        okHttp.getRequest(
                URL_PWD_FORGET,
                new OkHttpCommonUtil.Param[] {
                    new OkHttpCommonUtil.Param(KEY_PWD_FORGET_ACCOUNT_PHONE, phoneNo),
                    new OkHttpCommonUtil.Param(KEY_PWD_FORGET_PASSWORD_NEW, password),
//                    new OkHttpCommonUtil.Param(KEY_PWD_FORGET_VCODE, validateCode)
                },
                new HttpBaseCallback() {
                    @Override
                    public void onFailure(Request request, IOException e) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(), R.string.toast_pwd_forget_update_pwd_error, Toast.LENGTH_LONG).show();
                                btn_load_validate_code.setClickable(true);
                            }
                        });
                    }

                    @Override
                    public void onResponse(Response response) throws IOException {
                        String responseStr = response.body().string();
                        Log.d(TAG, "URL_PWD_FORGET == " + responseStr);
                        JSONObject jsonObject;
                        try {
                            jsonObject = new JSONObject(responseStr);
                            int status = jsonObject.getInt("Status");
                            if(status == 0) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(getApplicationContext(), R.string.toast_pwd_forget_update_pwd_ok, Toast.LENGTH_LONG).show();
                                        Intent intent = new Intent();
                                        intent.setClass(getApplicationContext(), LoginActivity.class);
                                        startActivity(intent);
                                        finish();
                                    }
                                });
                            } else {
                                // 其他请求错误
                                btn_load_validate_code.setClickable(true);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getApplicationContext(), R.string.toast_pwd_forget_update_pwd_invalidate, Toast.LENGTH_LONG).show();
                                    btn_load_validate_code.setClickable(true);
                                }
                            });
                        }
                    }
                });

    }

//    private void loadValidateCode() {
//        String phoneNo = et_pwd_update_account_phone.getText().toString().trim();
//        if(!TextUtils.isDigitsOnly(phoneNo)) {
//            Toast.makeText(getApplicationContext(), R.string.toast_phone_no_error_txt, Toast.LENGTH_LONG).show();
//            return;
//        }
//        if(phoneNo.length() != 11) {
//            Toast.makeText(getApplicationContext(),R.string.toast_phone_no_error_length,Toast.LENGTH_LONG).show();
//            return;
//        }
//        if(!CommonTools.isPhoneNumber(phoneNo)) {
//            Toast.makeText(getApplicationContext(),R.string.toast_phone_no_error_invalidate,Toast.LENGTH_LONG).show();
//            return;
//        }
//
//        OkHttpCommonUtil okHttp = OkHttpCommonUtil.newInstance(getApplicationContext());
//        okHttp.getRequest(
//                URL_LOAD_VALIDATE_CODE,
//                new OkHttpCommonUtil.Param[] {
//                        new OkHttpCommonUtil.Param(KEY_LOAD_VALIDATE_CODE_ACCOUNT_PHONE,phoneNo)
//                },
//                new HttpBaseCallback() {
//                    @Override
//                    public void onFailure(Request request, IOException e) {
//                        runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                Toast.makeText(getApplicationContext(), R.string.toast_pwd_forget_load_validate_code_error, Toast.LENGTH_LONG).show();
//                                btn_load_validate_code.setClickable(true);
//                            }
//                        });
//                    }
//
//                    @Override
//                    public void onResponse(Response response) throws IOException {
//                        String responseStr = response.body().string();
//                        Log.d(TAG, "URL_LOAD_VALIDATE_CODE == " + responseStr);
//                        JSONObject jsonObject;
//                        try {
//                            jsonObject = new JSONObject(responseStr);
//                            int status = jsonObject.getInt("status");
//                            if(status == 0) {
//                                runOnUiThread(new Runnable() {
//                                    @Override
//                                    public void run() {
//                                        Toast.makeText(getApplicationContext(), R.string.toast_pwd_forget_load_validate_code_ok, Toast.LENGTH_LONG).show();
//                                        btn_load_validate_code.setClickable(false);
//                                        thread = new ClassCutThreads();
//                                        thread.start();
//                                    }
//                                });
//                            } else {
//                                // 其他请求错误
//                                btn_load_validate_code.setClickable(true);
//                            }
//                        } catch (JSONException e) {
//                            e.printStackTrace();
//                            runOnUiThread(new Runnable() {
//                                @Override
//                                public void run() {
//                                    Toast.makeText(getApplicationContext(), R.string.toast_pwd_forget_load_validate_code_invalidate, Toast.LENGTH_LONG).show();
//                                    btn_load_validate_code.setClickable(true);
//                                }
//                            });
//                        }
//                    }
//                });
//    }

    private void loadValidateCode() {
        final String phoneNo = et_pwd_update_account_phone.getText().toString().trim();
        if(!TextUtils.isDigitsOnly(phoneNo)) {
            Toast.makeText(getApplicationContext(), R.string.toast_phone_no_error_txt, Toast.LENGTH_LONG).show();
            return;
        }
        if(phoneNo.length() != 11) {
            Toast.makeText(getApplicationContext(),R.string.toast_phone_no_error_length,Toast.LENGTH_LONG).show();
            return;
        }
        if(!CommonTools.isPhoneNumber(phoneNo)) {
            Toast.makeText(getApplicationContext(),R.string.toast_phone_no_error_invalidate,Toast.LENGTH_LONG).show();
            return;
        }

        OkHttpCommonUtil okHttp = OkHttpCommonUtil.newInstance(getApplicationContext());
        okHttp.getRequest(
                URL_LOAD_VALIDATE_CODE,
                new OkHttpCommonUtil.Param[] {
                        new OkHttpCommonUtil.Param(KEY_LOAD_VALIDATE_CODE_ACCOUNT_PHONE, phoneNo),
                        new OkHttpCommonUtil.Param(KEY_LOAD_VALIDATE_CODE_REQUEST_TYPE, "ForgetPassword")
                },
                new HttpBaseCallback() {
                    @Override
                    public void onFailure(Request request, IOException e) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(), R.string.toast_pwd_forget_load_validate_code_error, Toast.LENGTH_LONG).show();
                                btn_load_validate_code.setClickable(true);
                            }
                        });
                    }

                    @Override
                    public void onResponse(Response response) throws IOException {
                        String responseStr = response.body().string();
                        Log.d(TAG, "URL_LOAD_VALIDATE_CODE == " + responseStr);
                        JSONObject jsonObject;
                        try {
                            jsonObject = new JSONObject(responseStr);
                            int status = jsonObject.getInt("Status");
                            String message = jsonObject.getString("Message");
                            if(status == 0) {
                                String code = jsonObject.getString("IdentifyingCode");
                                sharedPreferencesHelper.putStringValue(AppConstants.KEY_SYS_FP_PHONE, phoneNo);
                                sharedPreferencesHelper.putStringValue(AppConstants.KEY_SYS_FP_CODE, code);
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(getApplicationContext(), R.string.toast_pwd_forget_load_validate_code_ok, Toast.LENGTH_LONG).show();
                                        btn_load_validate_code.setClickable(false);
                                        thread = new ClassCutThreads();
                                        thread.start();
                                    }
                                });
                            } else {
                                // 其他请求错误
                                btn_load_validate_code.setClickable(true);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getApplicationContext(), R.string.toast_pwd_forget_load_validate_code_invalidate, Toast.LENGTH_LONG).show();
                                    btn_load_validate_code.setClickable(true);
                                }
                            });
                        }
                    }
                });
    }

    private void navBack() {
        finish();
    }

    @Override
    public void onBackPressed() {
        navBack();
    }

    class ClassCutThreads extends Thread {// 倒计时逻辑子线程

        private boolean isRunning = false;

        public void run() {
            isRunning = true;
            while (i > 0 && isRunning) {
                i--;
                mHandler.post(new Runnable() {// 通过它在UI主线程中修改显示的剩余时间
                    @Override
                    public void run() {
                        btn_load_validate_code.setText(i + "秒后重新获取");// 显示剩余时间
                    }
                });
                try {
                    Thread.sleep(1000);// 线程休眠一秒钟 这个就是倒计时的间隔时间
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    // TODO Auto-generated method stub
                    btn_load_validate_code.setText("获取验证码");// 一轮倒计时结束 修改剩余时间为一分钟
                    Toast.makeText(getApplicationContext(), "倒计时完成",
                            Toast.LENGTH_LONG).show();// 提示倒计时完成
                    btn_load_validate_code.setClickable(true);// 倒数完成之后按钮启用
                }
            });
            i = 60;// 修改倒计时剩余时间变量为60秒为下一次获取验证码初始化
        }

        public boolean isRunning() {
            return isRunning;
        }

        public void setIsRunning(boolean isRunning) {
            this.isRunning = isRunning;
        }
    }
}
