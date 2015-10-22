package net.pingfang.signalr.chat.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.AppCompatActivity;
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

import java.io.IOException;

public class ResourcePostActivity extends AppCompatActivity implements View.OnClickListener, LocationNotify {

    public static final String TAG = ResourcePostActivity.class.getSimpleName();

    public static final String URL_RESOURCE_POST = GlobalApplication.URL_WEB_API_HOST + "/api/WebAPI/ResourceWall/PublishResource";
    public static final String KEY_RESOURCE_POST_UID = "userId";
    public static final String KEY_RESOURCE_POST_WIDTH = "length";
    public static final String KEY_RESOURCE_POST_HEIGHT = "width";
    public static final String KEY_RESOURCE_POST_ADDRESS = "address";
    public static final String KEY_RESOURCE_POST_CONTACTS = "contactName";
    public static final String KEY_RESOURCE_POST_PHONE = "mobile";
    public static final String KEY_RESOURCE_POST_REMARK = "remark";
    public static final String KEY_RESOURCE_POST_PROFILE = "pic";

    private String profile;

    private TextView btn_activity_back;
    private EditText et_resource_width;
    private EditText et_resource_height;
    private EditText et_resource_location;
    private EditText et_resource_contacts;
    private EditText et_resource_phone;
    private EditText et_resource_remark;
    private ImageView iv_resource_profile;
    private Button btn_resource_save;
    private Button btn_resource_cancel;


    private LocationClient locationClient;
    public LocationListenerImpl locationListener;
    private LatLng currentLatlng;

    SharedPreferencesHelper sharedPreferencesHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resource_post);

        sharedPreferencesHelper = SharedPreferencesHelper.newInstance(getApplicationContext());
        initView();
        initLocation();
    }

    private void initView() {
        btn_activity_back = (TextView) findViewById(R.id.btn_activity_back);
        btn_activity_back.setOnClickListener(this);

        et_resource_width = (EditText) findViewById(R.id.et_resource_width);
        et_resource_height = (EditText) findViewById(R.id.et_resource_height);
        et_resource_location = (EditText) findViewById(R.id.et_resource_location);
        et_resource_contacts = (EditText) findViewById(R.id.et_resource_contacts);
        et_resource_phone = (EditText) findViewById(R.id.et_resource_phone);
        et_resource_remark = (EditText) findViewById(R.id.et_resource_remark);
        iv_resource_profile = (ImageView) findViewById(R.id.iv_resource_profile);
        iv_resource_profile.setOnClickListener(this);

        btn_resource_save = (Button) findViewById(R.id.btn_resource_save);
        btn_resource_save.setOnClickListener(this);
        btn_resource_cancel = (Button) findViewById(R.id.btn_resource_cancel);
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
        et_resource_location.setText(bdLocation.getAddrStr());
    }

    @Override
    public void onClick(View view) {
        int viewId = view.getId();
        switch(viewId) {
            case R.id.btn_activity_back:
                navigateUp();
                break;
            case R.id.iv_resource_profile:
                pickImage();
                break;
            case R.id.btn_resource_save:
                storeOrPostRes();
                break;
            case R.id.btn_resource_cancel:
                navigateUp();
                break;
        }
    }

    private void storeOrPostRes() {
        OkHttpCommonUtil okhtp = OkHttpCommonUtil.newInstance(getApplicationContext());
        okhtp.postRequest(
                URL_RESOURCE_POST,
                new OkHttpCommonUtil.Param[]{
                        new OkHttpCommonUtil.Param(KEY_RESOURCE_POST_UID, sharedPreferencesHelper.getStringValue(AppConstants.KEY_SYS_CURRENT_UID)),
                        new OkHttpCommonUtil.Param(KEY_RESOURCE_POST_WIDTH, et_resource_width.getText().toString().trim()),
                        new OkHttpCommonUtil.Param(KEY_RESOURCE_POST_HEIGHT, et_resource_height.getText().toString().trim()),
                        new OkHttpCommonUtil.Param(KEY_RESOURCE_POST_ADDRESS, et_resource_location.getText().toString().trim()),
                        new OkHttpCommonUtil.Param(KEY_RESOURCE_POST_CONTACTS, et_resource_contacts.getText().toString().trim()),
                        new OkHttpCommonUtil.Param(KEY_RESOURCE_POST_PHONE, et_resource_phone.getText().toString().trim()),
                        new OkHttpCommonUtil.Param(KEY_RESOURCE_POST_REMARK, et_resource_remark.getText().toString().trim()),
                        new OkHttpCommonUtil.Param(KEY_RESOURCE_POST_PROFILE, profile)
                },
                new HttpBaseCallback() {
                    @Override
                    public void onResponse(Response response) throws IOException {
                        String result = response.body().string();
                        Log.d(TAG, "URL_RESOURCE_POST return == " + result);
                        JSONObject jsonObject = null;
                        try {
                            jsonObject = new JSONObject(result);
                            int status = jsonObject.getInt("status");
                            String message = jsonObject.getString("message");
                            if(status == 0) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(getApplicationContext(), getString(R.string.toast_resource_post_ok), Toast.LENGTH_SHORT).show();
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

    private void pickImage() {
        Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
        getIntent.setType("image/*");

        Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickIntent.setType("image/*");

        if (getIntent.resolveActivity(getPackageManager()) != null ||
                pickIntent.resolveActivity(getPackageManager()) != null) {

            Intent chooserIntent = Intent.createChooser(getIntent, getString(R.string.action_select_image));
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{pickIntent});

            startActivityForResult(chooserIntent, GlobalApplication.REQUEST_IMAGE_GET);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(resultCode == Activity.RESULT_OK) {
            if(data != null && data.getData() != null) {
                if(requestCode == GlobalApplication.REQUEST_IMAGE_GET) {
                    Uri uri = data.getData();
                    if(uri != null) {
                        String filePath = MediaFileUtils.getRealPathFromURI(getApplicationContext(), uri);
                        Bitmap bitmap = MediaFileUtils.decodeBitmapFromPath(filePath,
                                MediaFileUtils.dpToPx(getApplicationContext(), 150),
                                MediaFileUtils.dpToPx(getApplicationContext(), 150));
                        iv_resource_profile.setImageBitmap(bitmap);

                        profile = CommonTools.bitmapToBase64(bitmap);
                    }
                }
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
