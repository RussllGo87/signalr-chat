package net.pingfang.signalr.chat.activity;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import net.pingfang.signalr.chat.R;
import net.pingfang.signalr.chat.net.OkHttpCommonUtil;
import net.pingfang.signalr.chat.util.MediaFileUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class LoginActivity extends AppCompatActivity {

    public static final String LOGIN_URL = "http://api.hale.com/1400";
    public static final String LOGIN_KEY_ACCOUNT = "account";
    public static final String LOGIN_KEY_PASSWORD = "password";

    LinearLayout ll_form_container;
    EditText et_login_no;
    EditText et_login_pwd;
    CheckBox cb_show_pwd;

    private Handler mDelivery;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mDelivery = new Handler(Looper.getMainLooper());
        initView();
    }

    private void initView() {
        ll_form_container = (LinearLayout) findViewById(R.id.ll_form_container);
        et_login_no = (EditText) findViewById(R.id.et_login_no);
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
        String account = et_login_no.getText().toString().trim();
        String password = et_login_pwd.getText().toString().trim();
        if(!TextUtils.isEmpty(account) && !TextUtils.isEmpty(password)) {
            OkHttpCommonUtil okHttpCommonUtil = OkHttpCommonUtil.newInstance(getApplicationContext());
            okHttpCommonUtil.postRequest(LOGIN_URL, new OkHttpCommonUtil.Param[]{
                    new OkHttpCommonUtil.Param(LOGIN_KEY_ACCOUNT, account),
                    new OkHttpCommonUtil.Param(LOGIN_KEY_PASSWORD, password)
            }, new Callback() {
                @Override
                public void onFailure(Request request, IOException e) {

                }

                @Override
                public void onResponse(Response response) throws IOException {
                    String json = response.body().string();
                    JSONObject jsonObject = null;
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
                                    Intent intent = new Intent();
                                    intent.setClass(getApplicationContext(), HomeActivity.class);
                                    intent.putExtra("id", id);
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


    }

    public void register(View view) {
        Intent intent = new Intent();
        intent.setClass(getApplicationContext(),RegisterActivity.class);
        startActivity(intent);
        finish();
    }
}
