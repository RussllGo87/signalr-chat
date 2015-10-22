package net.pingfang.signalr.chat.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.model.LatLng;
import com.squareup.okhttp.Response;

import net.pingfang.signalr.chat.R;
import net.pingfang.signalr.chat.constant.app.AppConstants;
import net.pingfang.signalr.chat.location.LocationListenerImpl;
import net.pingfang.signalr.chat.location.LocationNotify;
import net.pingfang.signalr.chat.net.HttpBaseCallback;
import net.pingfang.signalr.chat.net.OkHttpCommonUtil;
import net.pingfang.signalr.chat.util.CommonTools;
import net.pingfang.signalr.chat.util.GlobalApplication;
import net.pingfang.signalr.chat.util.MediaFileUtils;
import net.pingfang.signalr.chat.util.SharedPreferencesHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

public class AdMaintainActivity extends AppCompatActivity implements View.OnClickListener, LocationNotify {

    public static final String TAG = AdMaintainActivity.class.getSimpleName();

    public static final String URL_AD_MAINTAIN = GlobalApplication.URL_WEB_API_HOST + "/api/WebAPI/Advertisement/PublishAD";
    public static final String KEY_URL_AD_MAINTAIN_UID = "userId";
    public static final String KEY_URL_AD_MAINTAIN_CODE = "adcode";
    public static final String KEY_URL_AD_MAINTAIN_ADDRESS = "address";
    public static final String KEY_URL_AD_MAINTAIN_CONTENT = "content";
    public static final String KEY_URL_AD_MAINTAIN_PIC = "pic";

    TextView btn_activity_back;

    private EditText et_ad_maintain_slogan;
    private EditText et_ad_maintain_location;
    private EditText et_ad_maintain_code;
    private ImageView iv_ad_maintain_pic;
    private Button btn_ad_maintain_save;
    private Button btn_ad_maintain_cancel;

    private LocationClient locationClient;
    private LocationListenerImpl locationListener;
    private LatLng currentLatlng;

    SharedPreferencesHelper sharedPreferencesHelper;

    String filePath;
    String tmpFilePath;
    String fileContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ad_maintain);

        sharedPreferencesHelper = SharedPreferencesHelper.newInstance(getApplicationContext());

        initView();
        initLocation();
    }

    private void initView() {
        btn_activity_back = (TextView) findViewById(R.id.btn_activity_back);
        btn_activity_back.setOnClickListener(this);


        et_ad_maintain_slogan = (EditText) findViewById(R.id.et_ad_maintain_slogan);
        et_ad_maintain_location = (EditText) findViewById(R.id.et_ad_maintain_location);
        et_ad_maintain_code = (EditText) findViewById(R.id.et_ad_maintain_code);
        iv_ad_maintain_pic = (ImageView) findViewById(R.id.iv_ad_maintain_pic);
        iv_ad_maintain_pic.setOnClickListener(this);
        btn_ad_maintain_save = (Button) findViewById(R.id.btn_ad_maintain_save);
        btn_ad_maintain_save.setOnClickListener(this);
        btn_ad_maintain_cancel = (Button) findViewById(R.id.btn_ad_maintain_cancel);
        btn_ad_maintain_cancel.setOnClickListener(this);
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
        if (bdLocation == null)
            return;

        currentLatlng = new LatLng(bdLocation.getLatitude(),
                bdLocation.getLongitude());
        et_ad_maintain_location.setText(bdLocation.getAddrStr());
    }

    @Override
    public void onClick(View view) {
        int viewId = view.getId();
        switch(viewId) {
            case R.id.btn_activity_back:
                navigateUp();
                break;
            case R.id.iv_ad_maintain_pic:
                takePic();
                break;
            case R.id.btn_ad_maintain_save:
                storeOrPostAdMaintain();
                break;
        }
    }

    private void storeOrPostAdMaintain() {
        if(!TextUtils.isEmpty(tmpFilePath)) {
            OkHttpCommonUtil okhtp = OkHttpCommonUtil.newInstance(getApplicationContext());
            okhtp.postRequest(
                    URL_AD_MAINTAIN,
                    new OkHttpCommonUtil.Param[]{
                            new OkHttpCommonUtil.Param(KEY_URL_AD_MAINTAIN_UID, sharedPreferencesHelper.getStringValue(AppConstants.KEY_SYS_CURRENT_UID)),
                            new OkHttpCommonUtil.Param(KEY_URL_AD_MAINTAIN_CODE, et_ad_maintain_code.getText().toString().trim()),
                            new OkHttpCommonUtil.Param(KEY_URL_AD_MAINTAIN_ADDRESS, et_ad_maintain_location.getText().toString().trim()),
                            new OkHttpCommonUtil.Param(KEY_URL_AD_MAINTAIN_CONTENT, et_ad_maintain_slogan.getText().toString().trim()),
                            new OkHttpCommonUtil.Param(KEY_URL_AD_MAINTAIN_PIC, fileContent)
                    },
                    new HttpBaseCallback() {
                        @Override
                        public void onResponse(Response response) throws IOException {
                            String result = response.body().string();
                            Log.d(TAG, "URL_AD_MAINTAIN return == " + result);
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
                                                    getString(R.string.toast_ad_maintain_ok),
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
            );
        }
    }

    private void takePic() {
        tmpFilePath = MediaFileUtils.genarateFilePath(getApplicationContext(),
                Environment.DIRECTORY_PICTURES,"Photos","jpg");
        File file = new File(tmpFilePath);
        if (file.exists()) {
            file.delete();
        }

        Uri uri = Uri.fromFile(file);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT,uri);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, GlobalApplication.REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == Activity.RESULT_OK) {
            if(requestCode == GlobalApplication.REQUEST_IMAGE_CAPTURE) {
                filePath = tmpFilePath;
                Bitmap bitmap = MediaFileUtils.decodeBitmapFromPath(filePath,
                        MediaFileUtils.dpToPx(getApplicationContext(), 150),
                        MediaFileUtils.dpToPx(getApplicationContext(), 150));
                iv_ad_maintain_pic.setImageBitmap(bitmap);
                fileContent = CommonTools.bitmapToBase64(bitmap);
            }
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
