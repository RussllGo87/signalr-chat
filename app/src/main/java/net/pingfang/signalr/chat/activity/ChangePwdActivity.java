package net.pingfang.signalr.chat.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.okhttp.Response;

import net.pingfang.signalr.chat.R;
import net.pingfang.signalr.chat.constant.app.AppConstants;
import net.pingfang.signalr.chat.net.HttpBaseCallback;
import net.pingfang.signalr.chat.net.OkHttpCommonUtil;
import net.pingfang.signalr.chat.util.CommonUtil;
import net.pingfang.signalr.chat.util.GlobalApplication;
import net.pingfang.signalr.chat.util.SharedPreferencesHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class ChangePwdActivity extends AppCompatActivity implements View.OnClickListener{

    public static final String TAG = ChangePwdActivity.class.getSimpleName();

    public static final String URL_PWD_UPDATE = GlobalApplication.URL_WEB_API_HOST + "/api/WebAPI/User/UpPassword";
    public static final String KEY_PWD_UPDATE_UID = "id";
    public static final String KEY_PWD_UPDATE_NOW = "oldpassword";
    public static final String KEY_PWD_UPDATE_NEW = "newpassword";

    TextView btn_activity_back;

    private EditText et_account_pwd_now;
    private EditText et_account_pwd_update;
    private EditText et_account_pwd_update_retype;
    private Button btn_account_pwd_update;

    SharedPreferencesHelper sharedPreferencesHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_pwd);

        sharedPreferencesHelper = SharedPreferencesHelper.newInstance(getApplicationContext());

        initView();
    }

    private void initView() {
        btn_activity_back = (TextView) findViewById(R.id.btn_activity_back);
        btn_activity_back.setOnClickListener(this);

        et_account_pwd_now = (EditText) findViewById(R.id.et_account_pwd_now);
        et_account_pwd_update = (EditText) findViewById(R.id.et_account_pwd_update);
        et_account_pwd_update_retype = (EditText) findViewById(R.id.et_account_pwd_update_retype);

        btn_account_pwd_update = (Button) findViewById(R.id.btn_account_pwd_update);
        btn_account_pwd_update.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        int viewId = view.getId();
        switch (viewId) {
            case R.id.btn_activity_back:
                navigateUp();
                break;
            case R.id.btn_account_pwd_update:
                updatePwd();
                break;
        }
    }

    private void updatePwd() {
        String pwdNow = et_account_pwd_now.getText().toString().trim();
        String pwdUpdate = et_account_pwd_update.getText().toString().trim();
        String pwdUpdateRetype = et_account_pwd_update_retype.getText().toString().trim();
        if(TextUtils.isEmpty(pwdNow)){
            Toast.makeText(getApplicationContext(), "请输入旧密码", Toast.LENGTH_SHORT).show();
            return;
        }else if(TextUtils.isEmpty(pwdUpdate)){
            Toast.makeText(getApplicationContext(), "请输入新密码", Toast.LENGTH_SHORT).show();
            return;
        }else if(TextUtils.isEmpty(pwdUpdateRetype)){
            Toast.makeText(getApplicationContext(), "请再次输入新密码", Toast.LENGTH_SHORT).show();
            return;
        }else if(!pwdUpdateRetype.equals(pwdUpdate)){
            Toast.makeText(getApplicationContext(), "两次输入的新密码不一致", Toast.LENGTH_SHORT).show();
            return;
        }

        if(CommonUtil.isConnected(getApplicationContext())){
            OkHttpCommonUtil okHttp = OkHttpCommonUtil.newInstance(getApplicationContext());
            okHttp.postRequest(URL_PWD_UPDATE,
                    new OkHttpCommonUtil.Param[] {
                            new OkHttpCommonUtil.Param(KEY_PWD_UPDATE_UID,sharedPreferencesHelper.getStringValue(AppConstants.KEY_SYS_CURRENT_UID)),
                            new OkHttpCommonUtil.Param(KEY_PWD_UPDATE_NOW,pwdNow),
                            new OkHttpCommonUtil.Param(KEY_PWD_UPDATE_NEW,pwdUpdate)
                    },
                    new HttpBaseCallback() {
                        @Override
                        public void onResponse(Response response) throws IOException {
                            String result = response.body().string();
                            Log.d(TAG, "URL_PWD_UPDATE result " + result);
                            JSONObject jsonObject = null;
                            try {
                                jsonObject = new JSONObject(result);
                                int status = jsonObject.getInt("status");
                                String message = jsonObject.getString("message");
                                if(status == 0) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(getApplicationContext(),
                                                    getString(R.string.toast_change_pwd_ok),
                                                    Toast.LENGTH_SHORT).show();
                                            navigateUp();
                                        }
                                    });
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });
        }else{
            Toast.makeText(getApplicationContext(), "当前没有网络请打开网络", Toast.LENGTH_SHORT).show();
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
