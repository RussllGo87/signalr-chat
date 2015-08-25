package net.pingfang.signalr.chat.activity;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import net.pingfang.signalr.chat.R;

import java.io.File;


public class AppAboutActivity extends AppCompatActivity implements View.OnClickListener{

    TextView btn_activity_back;
    TextView tv_app_name_version_code;
    TextView tv_about_item_suggestion;

    TextView tv_about_item_share_apk;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        initView();
    }

    private void initView() {

        btn_activity_back = (TextView) findViewById(R.id.btn_activity_back);
        btn_activity_back.setOnClickListener(this);

        tv_app_name_version_code = (TextView) findViewById(R.id.tv_app_name_version_code);
        try {
            PackageInfo info = getPackageManager().getPackageInfo(getPackageName(),0);
            tv_app_name_version_code.setText(getString(R.string.tv_app_name_version_code,info.versionName));
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        tv_about_item_suggestion = (TextView) findViewById(R.id.tv_about_item_suggestion);
        tv_about_item_suggestion.setOnClickListener(this);

        tv_about_item_share_apk = (TextView) findViewById(R.id.tv_about_item_share_apk);
        tv_about_item_share_apk.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        int viewId = view.getId();
        switch(viewId) {
            case R.id.btn_activity_back:
                navigateUp();
                break;
            case R.id.tv_about_item_suggestion:
                Intent intent = new Intent();
                intent.setClass(getApplicationContext(),FeedbackActivity.class);
                startActivity(intent);
                break;
            case R.id.tv_about_item_share_apk:
                getApkSourceInfo();
                break;
        }
    }

    private void getApkSourceInfo() {
        try {
            PackageInfo info = getPackageManager().getPackageInfo(getPackageName(),0);
            ApplicationInfo appInfo = info.applicationInfo;
            String sourceDir = appInfo.sourceDir;
            File file = new File(sourceDir);
            Uri sourceUri = Uri.fromFile(file);
            Intent sharingIntent = new Intent();
            sharingIntent.setAction(Intent.ACTION_SEND);
            sharingIntent.setType("application/*");
            sharingIntent.setPackage("com.android.bluetooth");
            sharingIntent.putExtra(Intent.EXTRA_STREAM, sourceUri);

            startActivity(Intent.createChooser(sharingIntent, "Share Application"));

            Toast.makeText(getApplicationContext(),sourceDir,Toast.LENGTH_SHORT).show();
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
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
