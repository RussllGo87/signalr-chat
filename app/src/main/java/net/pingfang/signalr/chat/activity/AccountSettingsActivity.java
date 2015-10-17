package net.pingfang.signalr.chat.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import net.pingfang.signalr.chat.R;

public class AccountSettingsActivity extends AppCompatActivity implements View.OnClickListener{

    TextView btn_activity_back;
    TextView tv_settings_item_change_pwd;
    TextView tv_settings_item_update_info;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_settings);

        initView();
    }

    private void initView() {
        btn_activity_back = (TextView) findViewById(R.id.btn_activity_back);
        btn_activity_back.setOnClickListener(this);

        tv_settings_item_change_pwd = (TextView) findViewById(R.id.tv_settings_item_change_pwd);
        tv_settings_item_change_pwd.setOnClickListener(this);
        tv_settings_item_update_info = (TextView) findViewById(R.id.tv_settings_item_update_info);
        tv_settings_item_update_info.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        int viewId = view.getId();
        switch (viewId) {
            case R.id.btn_activity_back:
                navigateUp();
                break;
            case R.id.tv_settings_item_change_pwd:
                Intent changePwdIntent = new Intent();
                changePwdIntent.setClass(getApplicationContext(),ChangePwdActivity.class);
                startActivity(changePwdIntent);
                break;
            case R.id.tv_settings_item_update_info:
                Intent updateInfoIntent = new Intent();
                updateInfoIntent.setClass(getApplicationContext(),AccountInfoUpdateActivity.class);
                startActivity(updateInfoIntent);
                break;

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
