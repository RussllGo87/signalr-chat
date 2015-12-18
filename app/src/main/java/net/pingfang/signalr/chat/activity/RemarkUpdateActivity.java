package net.pingfang.signalr.chat.activity;

import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import net.pingfang.signalr.chat.R;
import net.pingfang.signalr.chat.constant.app.AppConstants;
import net.pingfang.signalr.chat.database.AppContract;
import net.pingfang.signalr.chat.database.User;
import net.pingfang.signalr.chat.util.GlobalApplication;
import net.pingfang.signalr.chat.util.SharedPreferencesHelper;

public class RemarkUpdateActivity extends AppCompatActivity implements View.OnClickListener {

    TextView btn_activity_back;
    TextView btn_remark_update_ok;

    EditText et_remark_name;

    SharedPreferencesHelper sharedPreferencesHelper;
    User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remark_update);

        Intent intent = getIntent();
        user = intent.getParcelableExtra("user");

        sharedPreferencesHelper = SharedPreferencesHelper.newInstance(getApplicationContext());
        initView();
    }

    private void initView() {
        btn_activity_back = (TextView) findViewById(R.id.btn_activity_back);
        btn_activity_back.setOnClickListener(this);

        btn_remark_update_ok = (TextView) findViewById(R.id.btn_remark_update_ok);
        btn_remark_update_ok.setOnClickListener(this);

        et_remark_name = (EditText) findViewById(R.id.et_remark_name);
    }

    @Override
    public void onClick(View view) {
        int viewId = view.getId();
        switch (viewId) {
            case R.id.btn_activity_back:
                navigateUp();
                break;
            case R.id.btn_remark_update_ok:
                String remarkName = et_remark_name.getText().toString().trim();
                if(!TextUtils.isEmpty(remarkName)) {
                    String selection = AppContract.UserStatusEntry.COLUMN_NAME_ENTRY_UID  + " = ? " +
                                       " AND " +
                                       AppContract.UserStatusEntry.COLUMN_NAME_ENTRY_OWNER  + " = ?";
                    String[] selectionArgs = new String[]{user.getUid(), sharedPreferencesHelper.getStringValue(AppConstants.KEY_SYS_CURRENT_UID)};
                    ContentValues values = new ContentValues();
                    values.put(AppContract.UserStatusEntry.COLUMN_NAME_STATUS_REMARK, remarkName);
                    getApplicationContext().getContentResolver().update(AppContract.UserStatusEntry.CONTENT_URI, values, selection, selectionArgs);

                    Intent intent = new Intent();
                    intent.setAction(GlobalApplication.ACTION_INTENT_REMARK_UPDATE);
                    intent.putExtra("user",user);
                    sendBroadcast(intent);

                    navigateUp();
                }
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