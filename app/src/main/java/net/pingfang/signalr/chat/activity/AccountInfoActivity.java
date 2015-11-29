package net.pingfang.signalr.chat.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.okhttp.Response;

import net.pingfang.signalr.chat.R;
import net.pingfang.signalr.chat.constant.app.AppConstants;
import net.pingfang.signalr.chat.net.HttpBaseCallback;
import net.pingfang.signalr.chat.net.OkHttpCommonUtil;
import net.pingfang.signalr.chat.util.GlobalApplication;
import net.pingfang.signalr.chat.util.SharedPreferencesHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class AccountInfoActivity extends AppCompatActivity implements View.OnClickListener {

    public static final String TAG = AccountInfoActivity.class.getSimpleName();

    public static final String URL_ACCOUNT_INFO_LOAD = GlobalApplication.URL_WEB_API_HOST + "/api/WebAPI/User/GetUser";
    public static final String KEY_URL_ACCOUNT_INFO_LOAD_UID = "id";

    TextView btn_activity_back;
    TextView tv_account_nick_name;
    TextView tv_account_current_exp;
    ImageView iv_account_portrait;

    SharedPreferencesHelper sharedPreferencesHelper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_info);

        sharedPreferencesHelper = SharedPreferencesHelper.newInstance(getApplicationContext());

        initView();
        loadAccountInfo();
    }

    private void initView() {
        btn_activity_back = (TextView) findViewById(R.id.btn_activity_back);
        btn_activity_back.setOnClickListener(this);

        String nickname = sharedPreferencesHelper.getStringValue(AppConstants.KEY_SYS_CURRENT_NICKNAME);
        String portrait = sharedPreferencesHelper.getStringValue(AppConstants.KEY_SYS_CURRENT_PORTRAIT);
        int exp = sharedPreferencesHelper.getInt(AppConstants.KEY_SYS_CURRENT_USER_EXP, 0);

        tv_account_nick_name = (TextView) findViewById(R.id.tv_account_nick_name);
        tv_account_nick_name.setText(nickname);
        tv_account_current_exp = (TextView) findViewById(R.id.tv_account_current_exp);
        tv_account_current_exp.setText(getString(R.string.tv_account_current_exp, exp));
        iv_account_portrait = (ImageView) findViewById(R.id.iv_account_portrait);
        //        if (!TextUtils.isEmpty(portrait)) {
        //            OkHttpCommonUtil okHttp = OkHttpCommonUtil.newInstance(getApplicationContext());
        //            okHttp.display(iv_account_portrait, portrait, R.mipmap.ic_launcher);
        //        }
    }

    public void loadAccountInfo() {
        OkHttpCommonUtil okHttp = OkHttpCommonUtil.newInstance(getApplicationContext());
        okHttp.getRequest(URL_ACCOUNT_INFO_LOAD,
                new OkHttpCommonUtil.Param[]{
                        new OkHttpCommonUtil.Param(KEY_URL_ACCOUNT_INFO_LOAD_UID,
                                sharedPreferencesHelper.getStringValue(AppConstants.KEY_SYS_CURRENT_UID))
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
                                final String nickName = resultJson.getString("nickname");
                                final String portraitUrl = resultJson.getString("portrait");
                                final int exp = resultJson.getInt("exp");
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(getApplicationContext(),
                                                getString(R.string.toast_account_info_load_ok),
                                                Toast.LENGTH_SHORT).show();

                                        if (!TextUtils.isEmpty(nickName)) {
                                            tv_account_nick_name.setText(nickName);
                                        }

                                        tv_account_current_exp.setText(getString(R.string.tv_account_current_exp, exp));

                                        if (!TextUtils.isEmpty(portraitUrl)) {
                                            OkHttpCommonUtil okHttp = OkHttpCommonUtil.newInstance(getApplicationContext());
                                            okHttp.display(iv_account_portrait, portraitUrl, R.mipmap.ic_launcher);
                                        }


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


    @Override
    public void onClick(View view) {
        int viewId = view.getId();
        switch (viewId) {
            case R.id.btn_activity_back:
                navigateUp();
                break;
        }
    }

    public void navigateUp() {
        Intent upIntent = NavUtils.getParentActivityIntent(this);
        if (NavUtils.shouldUpRecreateTask(this, upIntent)) {
            TaskStackBuilder.create(this)
                    .addNextIntentWithParentStack(upIntent)
                    .startActivities();
        } else {
            onBackPressed();
        }
    }
}
