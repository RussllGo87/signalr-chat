package net.pingfang.signalr.chat.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;

import net.pingfang.signalr.chat.R;
import net.pingfang.signalr.chat.constant.app.AppConstants;
import net.pingfang.signalr.chat.util.SharedPreferencesHelper;

public class MainActivity extends AppCompatActivity {

    SharedPreferencesHelper sharedPreferencesHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPreferencesHelper = SharedPreferencesHelper.newInstance(getApplicationContext());
        String uid = sharedPreferencesHelper.getStringValue(AppConstants.KEY_SYS_CURRENT_UID);
        if(!TextUtils.isEmpty(uid)) {
            Intent intent = new Intent();
            intent.setClass(getApplicationContext(), HomeActivity.class);
            startActivity(intent);
            finish();
        }

    }

    public void login(View view) {
        Intent intent = new Intent();
        intent.setClass(getApplicationContext(),LoginActivity.class);
        startActivity(intent);
        finish();
    }

    public void register(View view) {
        Intent intent = new Intent();
        intent.setClass(getApplicationContext(),RegisterActivity.class);
        startActivity(intent);
        finish();
    }


}
