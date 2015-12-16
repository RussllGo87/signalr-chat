package net.pingfang.signalr.chat.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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

import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.model.LatLng;

import net.pingfang.signalr.chat.R;
import net.pingfang.signalr.chat.constant.app.AppConstants;
import net.pingfang.signalr.chat.location.LocationListenerImpl;
import net.pingfang.signalr.chat.location.LocationNotify;
import net.pingfang.signalr.chat.util.ImageUtils;
import net.pingfang.signalr.chat.util.SharedPreferencesHelper;

public class MainActivity extends AppCompatActivity implements LocationNotify {

    private static final String TAG = MainActivity.class.getSimpleName();

    SharedPreferencesHelper sharedPreferencesHelper;

    ImageView iv_app_logo;
    LinearLayout ll_container_buttons;

    TextView btn_login;
    TextView btn_register;
    Handler mHandler = new Handler(Looper.getMainLooper());
    private LatLng currentLatLng;
    private LocationClient locationClient;
    private LocationListenerImpl locationListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initLocation();
        initView();

        sharedPreferencesHelper = SharedPreferencesHelper.newInstance(getApplicationContext());
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

        locationClient = new LocationClient(getApplicationContext(),option);
        locationListener = new LocationListenerImpl(this);
        locationClient.registerLocationListener(locationListener);
        locationClient.start();
    }

    @Override
    public void updateLoc(BDLocation bdLocation) {

        Log.d(TAG,"update Location");

        if (bdLocation != null) {
            currentLatLng = new LatLng(bdLocation.getLatitude(),
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
