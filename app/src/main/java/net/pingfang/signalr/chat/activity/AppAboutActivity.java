package net.pingfang.signalr.chat.activity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import net.pingfang.signalr.chat.R;
import net.pingfang.signalr.chat.net.HttpBaseCallback;
import net.pingfang.signalr.chat.net.OkHttpCommonUtil;
import net.pingfang.signalr.chat.ui.dialog.DoubleButtonDialogFragment;
import net.pingfang.signalr.chat.util.GlobalApplication;
import net.pingfang.signalr.chat.util.MediaFileUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.List;


public class AppAboutActivity extends AppCompatActivity implements View.OnClickListener{

    public static final String TAG = AppAboutActivity.class.getSimpleName();

    public static final String URL_APP_VERSION_CHECK = GlobalApplication.URL_WEB_API_HOST + "/api/WebAPI/User/GetApkVersion";
    public static final String URL_APP_DOWNLOAD = GlobalApplication.URL_WEB_API_HOST + "/hale.apk";

    TextView btn_activity_back;
    ImageView iv_app_logo;
    TextView tv_app_name_version_code;

    LinearLayout ll_about_item_version;
    TextView tv_about_item_version;
    TextView tv_new_version_found;
    TextView tv_about_item_guide;
    TextView tv_about_item_suggestion;

    TextView tv_about_item_share_apk;

    PackageInfo info;

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
            info = getPackageManager().getPackageInfo(getPackageName(), 0);
            tv_app_name_version_code.setText(getString(R.string.tv_app_name_version_code,info.versionName));
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), R.string.toast_current_app_version_info_check_error, Toast.LENGTH_LONG).show();
        }

        ll_about_item_version = (LinearLayout) findViewById(R.id.ll_about_item_version);
        tv_about_item_version = (TextView) findViewById(R.id.tv_about_item_version);
        ll_about_item_version.setOnClickListener(this);
        tv_new_version_found = (TextView) findViewById(R.id.tv_new_version_found);
        tv_about_item_guide = (TextView) findViewById(R.id.tv_about_item_guide);
        tv_about_item_guide.setOnClickListener(this);
        tv_about_item_suggestion = (TextView) findViewById(R.id.tv_about_item_suggestion);
        tv_about_item_suggestion.setOnClickListener(this);

        tv_about_item_share_apk = (TextView) findViewById(R.id.tv_about_item_share_apk);
        tv_about_item_share_apk.setOnClickListener(this);

        iv_app_logo = (ImageView) findViewById(R.id.iv_app_logo);
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.hale_icon);
        bitmap = Bitmap.createScaledBitmap(bitmap,
                MediaFileUtils.dpToPx(getApplicationContext(), 150),
                MediaFileUtils.dpToPx(getApplicationContext(), 120),
                true);
        iv_app_logo.setImageBitmap(bitmap);
//        OkHttpCommonUtil instance = OkHttpCommonUtil.newInstance(getApplicationContext());
//        instance.display(iv_app_logo,"http://www.baidu.com/img/bd_logo1.png",R.mipmap.ic_launcher);

    }

    @Override
    public void onClick(View view) {
        int viewId = view.getId();
        switch(viewId) {
            case R.id.btn_activity_back:
                navigateUp();
                break;
            case R.id.ll_about_item_version:
                checkLatestVersion();
                break;
            case R.id.tv_about_item_guide:
                Intent guideIntent = new Intent();
                guideIntent.setClass(getApplicationContext(),GuideActivity.class);
                startActivity(guideIntent);
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

    private void checkLatestVersion() {
        OkHttpCommonUtil okHttp = OkHttpCommonUtil.newInstance(getApplicationContext());
        okHttp.getRequest(URL_APP_VERSION_CHECK, null, new HttpBaseCallback() {
            @Override
            public void onFailure(Request request, IOException e) {
                super.onFailure(request, e);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), R.string.network_error, Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onResponse(Response response) throws IOException {
                String responseStr = response.body().string();
                Log.d(TAG, "URL_APP_VERSION_CHECK return == " + responseStr);
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(responseStr);
                    int versionCode = jsonObject.getInt("VersionCode");
                    //                    String versionName = jsonObject.getString("VersionName");

                    int currentVersionCode = info.versionCode;
                    //                    String currentVersionName = info.versionName;

                    if (versionCode > currentVersionCode) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                tv_new_version_found.setVisibility(View.VISIBLE);
                                DoubleButtonDialogFragment dialogFragment = DoubleButtonDialogFragment.newInstance(
                                        getApplicationContext().getString(R.string.dialog_message_url_latest_app_download),
                                        new DoubleButtonDialogFragment.DoubleButtonDialogClick() {
                                            @Override
                                            public void onPositiveButtonClick() {
                                                Intent intent = new Intent();
                                                intent.setAction(Intent.ACTION_VIEW);
                                                intent.setData(Uri.parse(URL_APP_DOWNLOAD));
                                                if (intent.resolveActivity(getPackageManager()) != null) {
                                                    startActivity(intent);
                                                }
                                            }
                                        });
                                dialogFragment.show(getSupportFragmentManager(), "DoubleButtonDialogFragment");
                            }
                        });
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(), R.string.toast_app_name_version_latest, Toast.LENGTH_LONG).show();
                            }
                        });
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), R.string.toast_latest_app_version_info_check_error, Toast.LENGTH_LONG).show();
                        }
                    });
                }

            }
        });
    }

    private void getApkSourceInfo() {
        try {
            PackageInfo info = getPackageManager().getPackageInfo(getPackageName(),0);
            ApplicationInfo appInfo = info.applicationInfo;
            String sourceDir = appInfo.publicSourceDir;
            File file = new File(sourceDir);
            Uri sourceUri = Uri.fromFile(file);
            Intent sharingIntent = new Intent();
            sharingIntent.setAction(Intent.ACTION_SEND);
            sharingIntent.setType("application/*");
            sharingIntent.putExtra(Intent.EXTRA_STREAM, sourceUri);

            PackageManager pm = getApplicationContext().getPackageManager();
            List<ResolveInfo> resolveInfoList =  pm.queryIntentActivities(sharingIntent, PackageManager.MATCH_DEFAULT_ONLY);
            String sharePackageName = null;
            for(ResolveInfo resolveInfo : resolveInfoList) {
                ActivityInfo activityInfo = resolveInfo.activityInfo;
                Log.d(TAG, "activityInfo packageName == " + activityInfo.packageName);
                if(activityInfo.packageName.contains("bluetooth")) {
                    sharePackageName = activityInfo.packageName;
                    break;
                }
            }

            if(!TextUtils.isEmpty(sharePackageName)) {
                sharingIntent.setPackage(sharePackageName);
                startActivity(Intent.createChooser(sharingIntent, "Share Application"));
                Toast.makeText(getApplicationContext(), sourceDir, Toast.LENGTH_SHORT).show();
            } else {
                sharePackageName = "com.android.bluetooth";
                sharingIntent.setPackage(sharePackageName);
                startActivity(Intent.createChooser(sharingIntent, "Share Application"));
                Toast.makeText(getApplicationContext(), sourceDir, Toast.LENGTH_SHORT).show();
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), R.string.toast_share_not_support, Toast.LENGTH_SHORT).show();
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
