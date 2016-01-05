package net.pingfang.signalr.chat.activity;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.content.IntentCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.model.LatLng;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import net.pingfang.signalr.chat.R;
import net.pingfang.signalr.chat.constant.app.AppConstants;
import net.pingfang.signalr.chat.location.LocationListenerImpl;
import net.pingfang.signalr.chat.location.LocationNotify;
import net.pingfang.signalr.chat.net.HttpBaseCallback;
import net.pingfang.signalr.chat.net.OkHttpCommonUtil;
import net.pingfang.signalr.chat.ui.dialog.DoubleButtonDialogFragment;
import net.pingfang.signalr.chat.util.GlobalApplication;
import net.pingfang.signalr.chat.util.ImageUtils;
import net.pingfang.signalr.chat.util.SharedPreferencesHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class MainActivity extends AppCompatActivity implements LocationNotify {

    private static final String TAG = MainActivity.class.getSimpleName();

    public static final String URL_APP_VERSION_CHECK = GlobalApplication.URL_WEB_API_HOST + "/api/WebAPI/User/GetApkVersion";
    public static final String URL_APP_DOWNLOAD = GlobalApplication.URL_WEB_API_HOST + "/hale.apk";

    SharedPreferencesHelper sharedPreferencesHelper;

    ImageView iv_app_logo;
    LinearLayout ll_container_buttons;

    TextView btn_login;
    TextView btn_register;
    Handler mHandler = new Handler(Looper.getMainLooper());

    PackageInfo info;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPreferencesHelper = SharedPreferencesHelper.newInstance(getApplicationContext());
        initLocation();
        initView();
        checkLatestVersion();

        String uid = sharedPreferencesHelper.getStringValue(AppConstants.KEY_SYS_CURRENT_UID);
        if(!TextUtils.isEmpty(uid)) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent();
                    intent.setClass(getApplicationContext(), HomeActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | IntentCompat.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                }
            },1000);

        } else {
            ll_container_buttons.setVisibility(View.VISIBLE);
        }

    }

    /**
     * 初始化定位设置并开始定位
     */
    public void initLocation() {
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        option.setCoorType("bd09ll");
        option.setOpenGps(true);
        option.setIsNeedAddress(true);
        option.setIgnoreKillProcess(false);

        LocationClient locationClient = new LocationClient(getApplicationContext(),option);
        LocationListenerImpl locationListener = new LocationListenerImpl(this);
        locationClient.registerLocationListener(locationListener);
        locationClient.start();
    }

    @Override
    public void updateLoc(BDLocation bdLocation) {

        Log.d(TAG, "update Location");

        if (bdLocation != null) {
            LatLng currentLatLng = new LatLng(bdLocation.getLatitude(),
                    bdLocation.getLongitude());

            sharedPreferencesHelper.putStringValue(AppConstants.KEY_SYS_LOCATION_LAT, Double.toString(currentLatLng.latitude));
            sharedPreferencesHelper.putStringValue(AppConstants.KEY_SYS_LOCATION_LNG, Double.toString(currentLatLng.longitude));
        }
    }

    private void initView() {
        iv_app_logo = (ImageView) findViewById(R.id.iv_app_logo);
        ImageUtils.ImageSize actualImageSize = ImageUtils.getImageSize(getResources(),R.drawable.hale_logo);
        ImageUtils.ImageSize imageViewSize = ImageUtils.getImageViewSize(iv_app_logo);
        int inSampleSize = ImageUtils.calculateInSampleSize(actualImageSize, imageViewSize);
        BitmapFactory.Options ops = new BitmapFactory.Options();
        ops.inJustDecodeBounds = false;
        ops.inSampleSize = inSampleSize;
        final Bitmap bm = BitmapFactory.decodeResource(getResources(),R.drawable.hale_logo,ops);
        iv_app_logo.setImageBitmap(bm);

        ll_container_buttons = (LinearLayout) findViewById(R.id.ll_container_buttons);

        btn_login = (TextView) findViewById(R.id.btn_login);
        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                login(view);
            }
        });
        btn_register = (TextView) findViewById(R.id.btn_register);
        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                register(view);
            }
        });

        try {
            info = getPackageManager().getPackageInfo(getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), R.string.toast_current_app_version_info_check_error, Toast.LENGTH_LONG).show();
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
                                try {
                                    dialogFragment.show(getSupportFragmentManager(), "DoubleButtonDialogFragment");
                                } catch (IllegalStateException e) {
                                    Log.d(TAG, "IllegalStateException when show dialog");
                                }

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
