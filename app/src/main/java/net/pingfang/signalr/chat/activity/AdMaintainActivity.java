package net.pingfang.signalr.chat.activity;

import android.app.Activity;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.model.LatLng;
import com.google.zxing.integration.android.CustomerIntentIntegrator;
import com.google.zxing.integration.android.CustomerIntentResult;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import net.pingfang.signalr.chat.R;
import net.pingfang.signalr.chat.constant.app.AppConstants;
import net.pingfang.signalr.chat.database.Advertisement;
import net.pingfang.signalr.chat.database.AppContract;
import net.pingfang.signalr.chat.location.LocationListenerImpl;
import net.pingfang.signalr.chat.location.LocationNotify;
import net.pingfang.signalr.chat.net.HttpBaseCallback;
import net.pingfang.signalr.chat.net.NetUtil;
import net.pingfang.signalr.chat.net.OkHttpCommonUtil;
import net.pingfang.signalr.chat.util.CommonTools;
import net.pingfang.signalr.chat.util.CommonUtil;
import net.pingfang.signalr.chat.util.FileUtil;
import net.pingfang.signalr.chat.util.GlobalApplication;
import net.pingfang.signalr.chat.util.ImageUtils;
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
    public static final String KEY_URL_AD_MAINTAIN_LOCATION_LAT = "latitude";
    public static final String KEY_URL_AD_MAINTAIN_LOCATION_LNG = "longitude";
    public static final String KEY_URL_AD_MAINTAIN_LENGTH = "length";
    public static final String KEY_URL_AD_MAINTAIN_WIDTH = "width";
    public static final String KEY_URL_AD_MAINTAIN_REMARK = "remark";
    public static final String KEY_URL_AD_MAINTAIN_PIC = "pic";

    String[] fileContentArray = new String[4];
    String[] filePathArray = new String[4];

    TextView btn_activity_back;
    SharedPreferencesHelper sharedPreferencesHelper;
    Dialog dialog;
    Uri targetUri;
    String tmpFilePath;
    String fileContent;
    // GPS 状态相关变量
    boolean gpsStatus = false;
    private EditText et_ad_maintain_location;
    private EditText et_ad_maintain_code;
    private TextView btn_ad_maintain_code_scan;
    private EditText et_ad_length;
    private EditText et_ad_width;
    private EditText et_ad_maintain_remark;
    private ImageView iv_content_photo_1;
    private ImageView iv_content_photo_2;
    private ImageView iv_content_photo_3;

    //    private ImageView iv_ad_maintain_pic;
    private ImageView iv_content_photo_4;
    private Button btn_ad_maintain_save;
    private Button btn_ad_maintain_cancel;
    private LinearLayout ll_progress_bar_container;
    private ProgressBar pb_operation;
    private TextView tv_pb_operation;
    private LocationClient locationClient;
    private LocationListenerImpl locationListener;
    private LatLng currentLatLng;
    private int currentViewId = 0;

    private Handler mDelivery = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ad_maintain);

        sharedPreferencesHelper = SharedPreferencesHelper.newInstance(getApplicationContext());

        initView();
        checkGpsStatus();
        initLocation();
    }

    private void initView() {
        btn_activity_back = (TextView) findViewById(R.id.btn_activity_back);
        btn_activity_back.setOnClickListener(this);

        et_ad_maintain_location = (EditText) findViewById(R.id.et_ad_maintain_location);
        et_ad_maintain_location.setEnabled(false);
        et_ad_maintain_code = (EditText) findViewById(R.id.et_ad_maintain_code);
        btn_ad_maintain_code_scan = (TextView) findViewById(R.id.btn_ad_maintain_code_scan);
        btn_ad_maintain_code_scan.setOnClickListener(this);
        et_ad_length = (EditText) findViewById(R.id.et_ad_length);
        et_ad_width = (EditText) findViewById(R.id.et_ad_width);
        et_ad_maintain_remark = (EditText) findViewById(R.id.et_ad_maintain_remark);

        //        iv_ad_maintain_pic = (ImageView) findViewById(R.id.iv_ad_maintain_pic);
        //        iv_ad_maintain_pic.setOnClickListener(this);
        iv_content_photo_1 = (ImageView) findViewById(R.id.iv_content_photo_1);
        iv_content_photo_1.setOnClickListener(this);
        iv_content_photo_2 = (ImageView) findViewById(R.id.iv_content_photo_2);
        iv_content_photo_2.setOnClickListener(this);
        iv_content_photo_3 = (ImageView) findViewById(R.id.iv_content_photo_3);
        iv_content_photo_3.setOnClickListener(this);
        iv_content_photo_4 = (ImageView) findViewById(R.id.iv_content_photo_4);
        iv_content_photo_4.setOnClickListener(this);

        btn_ad_maintain_save = (Button) findViewById(R.id.btn_ad_maintain_save);
        btn_ad_maintain_save.setOnClickListener(this);
        btn_ad_maintain_cancel = (Button) findViewById(R.id.btn_ad_maintain_cancel);
        btn_ad_maintain_cancel.setOnClickListener(this);

        ll_progress_bar_container = (LinearLayout) findViewById(R.id.ll_progress_bar_container);
        pb_operation = (ProgressBar) findViewById(R.id.pb_operation);
        tv_pb_operation = (TextView) findViewById(R.id.tv_pb_operation);
    }

    /**
     * 检查gps状态并提示用户打开gps
     */
    public void checkGpsStatus() {
        gpsStatus = CommonUtil.isGpsOPen(getApplicationContext());
        if (!gpsStatus) {
            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            if (!locationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER)) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(this);
                dialog.setMessage("请打开GPS");
                dialog.setPositiveButton("确定",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dilog, int which) {
                                Intent intent = new Intent(
                                        Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                startActivity(intent);
                            }
                        });
                dialog.show();
            }
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
        if (bdLocation == null) {
            Toast.makeText(getApplicationContext(),getString(R.string.toast_location_ok),Toast.LENGTH_LONG).show();
            return;
        }

        currentLatLng = new LatLng(bdLocation.getLatitude(),bdLocation.getLongitude());
        et_ad_maintain_location.setText(bdLocation.getAddrStr());
    }

    @Override
    public void onClick(View view) {
        int viewId = view.getId();
        switch(viewId) {
            case R.id.btn_activity_back:
                navigateUp();
                break;
            case R.id.btn_ad_maintain_code_scan:
                CustomerIntentIntegrator integrator = new CustomerIntentIntegrator(AdMaintainActivity.this);
                integrator.setCaptureActivity(CaptureActivityAnyOrientation.class);
                integrator.setOrientationLocked(true);
                integrator.initiateScan();
                break;
            //            case R.id.iv_ad_maintain_pic:
            //                showDialog();
            //                break;
            case R.id.iv_content_photo_1:
                currentViewId = R.id.iv_content_photo_1;
                showDialog();
                break;
            case R.id.iv_content_photo_2:
                currentViewId = R.id.iv_content_photo_2;
                showDialog();
                break;
            case R.id.iv_content_photo_3:
                currentViewId = R.id.iv_content_photo_3;
                showDialog();
                break;
            case R.id.iv_content_photo_4:
                currentViewId = R.id.iv_content_photo_4;
                showDialog();
                break;
            case R.id.btn_ad_maintain_save:
                storeOrPostAdMaintain();
                break;
            case R.id.btn_ad_maintain_cancel:
                navigateUp();
                break;
        }
    }

    private void storeOrPostAdMaintain() {

        if (TextUtils.isEmpty(et_ad_maintain_location.getText().toString().trim())) {
            Toast.makeText(getApplicationContext(), "地址不能为空", Toast.LENGTH_SHORT).show();
            return;
        } else if (TextUtils.isEmpty(et_ad_maintain_code.getText().toString().trim())) {
            Toast.makeText(getApplicationContext(), "请输入广告编码", Toast.LENGTH_SHORT).show();
            return;
        } else if (TextUtils.isEmpty(et_ad_length.getText().toString().trim())) {
            Toast.makeText(getApplicationContext(), "长度不能为空", Toast.LENGTH_SHORT).show();
            return;
        } else if (TextUtils.isEmpty(et_ad_width.getText().toString().trim())) {
            Toast.makeText(getApplicationContext(), "宽度不能为空", Toast.LENGTH_SHORT).show();
            return;
        }

        for (int i = 0; i < filePathArray.length; i++) {
            String path = filePathArray[i];
            if (TextUtils.isEmpty(path)) {
                Toast.makeText(getApplicationContext(),
                        getString(R.string.toast_resource_posting_error_pic_no, (i + 1)),
                        Toast.LENGTH_LONG).show();
                return;
            }
        }

        if (NetUtil.isConnected(getApplicationContext())) {
            storeOnWeb();
        } else {
            storeOnLocalDb();
            Toast.makeText(getApplicationContext(), "当前网络状况不好，数据已经保存到了本地", Toast.LENGTH_SHORT).show();
            navigateUp();
        }
    }

    public void storeOnWeb() {

        ll_progress_bar_container.setVisibility(View.VISIBLE);
        tv_pb_operation.setText("数据上传中");

        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < fileContentArray.length; i++) {
            sb.append(fileContentArray[i]);
            if (i != (fileContentArray.length - 1)) {
                sb.append(";");
            }
        }
        String tmpContent = sb.toString();

        if (!TextUtils.isEmpty(tmpContent)) {
            OkHttpCommonUtil okHttp = OkHttpCommonUtil.newInstance(getApplicationContext());
            okHttp.postRequest(
                    URL_AD_MAINTAIN,
                    new OkHttpCommonUtil.Param[]{
                            new OkHttpCommonUtil.Param(KEY_URL_AD_MAINTAIN_UID, sharedPreferencesHelper.getStringValue(AppConstants.KEY_SYS_CURRENT_UID)),
                            new OkHttpCommonUtil.Param(KEY_URL_AD_MAINTAIN_ADDRESS, et_ad_maintain_location.getText().toString().trim()),
                            new OkHttpCommonUtil.Param(KEY_URL_AD_MAINTAIN_CODE, et_ad_maintain_code.getText().toString().trim()),
                            new OkHttpCommonUtil.Param(KEY_URL_AD_MAINTAIN_LENGTH, et_ad_length.getText().toString().trim()),
                            new OkHttpCommonUtil.Param(KEY_URL_AD_MAINTAIN_WIDTH, et_ad_width.getText().toString().trim()),
                            new OkHttpCommonUtil.Param(KEY_URL_AD_MAINTAIN_REMARK, et_ad_maintain_remark.getText().toString().trim()),
                            new OkHttpCommonUtil.Param(KEY_URL_AD_MAINTAIN_LOCATION_LAT, currentLatLng.latitude),
                            new OkHttpCommonUtil.Param(KEY_URL_AD_MAINTAIN_LOCATION_LNG, currentLatLng.longitude),
                            new OkHttpCommonUtil.Param(KEY_URL_AD_MAINTAIN_PIC, tmpContent)
                    },
                    new HttpBaseCallback() {

                        @Override
                        public void onFailure(Request request, IOException e) {
                            super.onFailure(request, e);
                            storeOnLocalDb();
                            mDelivery.post(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getApplicationContext(), "当前网络状况不好，数据已经保存到了本地", Toast.LENGTH_SHORT).show();
                                    ll_progress_bar_container.setVisibility(View.GONE);
                                    navigateUp();
                                }
                            });
                        }

                        @Override
                        public void onResponse(Response response) throws IOException {
                            String result = response.body().string();
                            Log.d(TAG, "URL_AD_MAINTAIN return == " + result);
                            JSONObject jsonObject = null;
                            try {
                                jsonObject = new JSONObject(result);
                                int status = jsonObject.getInt("status");
                                String message = jsonObject.getString("message");
                                if (status == 0) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            ll_progress_bar_container.setVisibility(View.GONE);
                                            Toast.makeText(getApplicationContext(),
                                                    getString(R.string.toast_ad_maintain_ok),
                                                    Toast.LENGTH_SHORT).show();
                                            navigateUp();
                                        }
                                    });
                                } else {
                                    storeOnLocalDb();
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            ll_progress_bar_container.setVisibility(View.GONE);
                                            Toast.makeText(getApplicationContext(),
                                                    getString(R.string.toast_ad_maintain_error),
                                                    Toast.LENGTH_SHORT).show();
                                            navigateUp();
                                        }
                                    });
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                                storeOnLocalDb();
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        ll_progress_bar_container.setVisibility(View.GONE);
                                        Toast.makeText(getApplicationContext(),
                                                getString(R.string.toast_ad_maintain_error),
                                                Toast.LENGTH_SHORT).show();
                                        navigateUp();
                                    }
                                });
                            }
                        }
                    }
            );
        } else {
            Toast.makeText(getApplicationContext(), getString(R.string.image_data_null), Toast.LENGTH_SHORT).show();
        }
    }

    private void storeOnLocalDb() {
        ContentValues values = new ContentValues();
        values.put(AppContract.AdvertisementEntry.COLUMN_NAME_AD_UID, sharedPreferencesHelper.getStringValue(AppConstants.KEY_SYS_CURRENT_UID));
        values.put(AppContract.AdvertisementEntry.COLUMN_NAME_AD_ADDRESS, et_ad_maintain_location.getText().toString().trim());
        values.put(AppContract.AdvertisementEntry.COLUMN_NAME_AD_CODE, et_ad_maintain_code.getText().toString().trim());
        values.put(AppContract.AdvertisementEntry.COLUMN_NAME_AD_LENGTH, et_ad_length.getText().toString().trim());
        values.put(AppContract.AdvertisementEntry.COLUMN_NAME_AD_WIDTH, et_ad_width.getText().toString().trim());
        values.put(AppContract.AdvertisementEntry.COLUMN_NAME_AD_REMARK, et_ad_maintain_remark.getText().toString().trim());
        values.put(AppContract.AdvertisementEntry.COLUMN_NAME_AD_LAT, currentLatLng.latitude);
        values.put(AppContract.AdvertisementEntry.COLUMN_NAME_AD_LNG, currentLatLng.longitude);
        values.put(AppContract.AdvertisementEntry.COLUMN_NAME_AD_PATH_P1, filePathArray[0]);
        values.put(AppContract.AdvertisementEntry.COLUMN_NAME_AD_PATH_P2, filePathArray[1]);
        values.put(AppContract.AdvertisementEntry.COLUMN_NAME_AD_PATH_P3, filePathArray[2]);
        values.put(AppContract.AdvertisementEntry.COLUMN_NAME_AD_PATH_P4, filePathArray[3]);
        values.put(AppContract.AdvertisementEntry.COLUMN_NAME_AD_STATUS, Advertisement.AD_STATUS_UPLOAD_ERROR);

        getContentResolver().insert(AppContract.AdvertisementEntry.CONTENT_URI, values);
    }

    private void showDialog() {
        View view = getLayoutInflater().inflate(R.layout.photo_choose_dialog,
                null);
        dialog = new Dialog(this, R.style.transparentFrameWindowStyle);
        dialog.setContentView(view, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        Window window = dialog.getWindow();
        // 设置显示动画
        //        window.setWindowAnimations(R.style.main_menu_animstyle);
        WindowManager.LayoutParams wl = window.getAttributes();
        wl.x = 0;
        wl.y = getWindowManager().getDefaultDisplay().getHeight();
        // 以下这两句是为了保证按钮可以水平满屏
        wl.width = ViewGroup.LayoutParams.MATCH_PARENT;
        wl.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        // 设置显示位置
        dialog.onWindowAttributesChanged(wl);
        // 设置点击外围解散
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();
    }

    public void on_click(View v) {
        switch (v.getId()) {
            case R.id.openCamera:
                openCamera();
                dialog.cancel();
                break;
            case R.id.openPhones:
                pickImage();
                dialog.cancel();
                break;
            case R.id.cancel:
                dialog.cancel();
                break;
            default:
                break;
        }
    }

    private void openCamera() {
        tmpFilePath = MediaFileUtils.genarateFilePath(getApplicationContext(),
                Environment.DIRECTORY_PICTURES, "Photos", "jpg");
        File file = new File(tmpFilePath);
        if (file.exists()) {
            file.delete();
        }

        targetUri = Uri.fromFile(file);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, targetUri);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, GlobalApplication.REQUEST_IMAGE_CAPTURE);
        }

    }

    private void pickImage() {
        Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
        getIntent.setType("image/*");

        //        Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        //        pickIntent.setType("image/*");
        //
        //        if (getIntent.resolveActivity(getPackageManager()) != null ||
        //                pickIntent.resolveActivity(getPackageManager()) != null) {
        //
        //            Intent chooserIntent = Intent.createChooser(getIntent, getString(R.string.action_select_image));
        //            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{pickIntent});
        //
        //            startActivityForResult(chooserIntent, GlobalApplication.REQUEST_IMAGE_GET);
        //        }

        if (getIntent.resolveActivity(getPackageManager()) != null) {
            Intent chooserIntent = Intent.createChooser(getIntent, getString(R.string.action_select_image));
            startActivityForResult(chooserIntent, GlobalApplication.REQUEST_IMAGE_GET);
        }

        //        if(pickIntent.resolveActivity(getPackageManager()) != null) {
        //            Intent chooserIntent = Intent.createChooser(pickIntent, getString(R.string.action_select_image));
        //            startActivityForResult(chooserIntent, GlobalApplication.REQUEST_IMAGE_GET);
        //        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        CustomerIntentResult result = CustomerIntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if(result != null) {
            if(result.getContents() == null) {
                Toast.makeText(getApplicationContext(), getString(R.string.btn_ad_maintain_code_scan_cancelled), Toast.LENGTH_LONG).show();
            } else {
                final String content = result.getContents();
                Toast.makeText(getApplicationContext(), getString(R.string.btn_ad_maintain_code_scan_ok), Toast.LENGTH_LONG).show();
                et_ad_maintain_code.setText(content);
            }
        } else {
            Log.d("MainActivity", "Weird");
        }

        if(resultCode == Activity.RESULT_OK) {
            if(requestCode == GlobalApplication.REQUEST_IMAGE_CAPTURE) {
                String filePath = tmpFilePath;
                ImageUtils.ImageSize actualImageSize = ImageUtils.getImageSize(filePath);
                ImageUtils.ImageSize imageViewSize = ImageUtils.getImageViewSize(iv_content_photo_1);
                int inSampleSize = ImageUtils.calculateInSampleSize(actualImageSize, imageViewSize);
                BitmapFactory.Options ops = new BitmapFactory.Options();
                ops.inJustDecodeBounds = false;
                ops.inSampleSize = inSampleSize;
                final Bitmap bm = BitmapFactory.decodeFile(filePath, ops);
                fileContent = CommonTools.bitmapToBase64(bm);
                if (currentViewId == iv_content_photo_1.getId()) {
                    fileContentArray[0] = fileContent;
                    filePathArray[0] = filePath;
                    iv_content_photo_1.setImageBitmap(bm);
                } else if (currentViewId == iv_content_photo_2.getId()) {
                    fileContentArray[1] = fileContent;
                    filePathArray[1] = filePath;
                    iv_content_photo_2.setImageBitmap(bm);
                } else if (currentViewId == iv_content_photo_3.getId()) {
                    fileContentArray[2] = fileContent;
                    filePathArray[2] = filePath;
                    iv_content_photo_3.setImageBitmap(bm);
                } else if (currentViewId == iv_content_photo_4.getId()) {
                    fileContentArray[3] = fileContent;
                    filePathArray[3] = filePath;
                    iv_content_photo_4.setImageBitmap(bm);
                }
            } else if(requestCode == GlobalApplication.REQUEST_IMAGE_GET) {
                if(data != null && data.getData() != null) {
                    Uri uri = data.getData();
                    String filePath = FileUtil.getPath(getApplicationContext(), uri);
                    ImageUtils.ImageSize actualImageSize = ImageUtils.getImageSize(filePath);
                    ImageUtils.ImageSize imageViewSize = ImageUtils.getImageViewSize(iv_content_photo_1);
                    int inSampleSize = ImageUtils.calculateInSampleSize(actualImageSize, imageViewSize);
                    BitmapFactory.Options ops = new BitmapFactory.Options();
                    ops.inJustDecodeBounds = false;
                    ops.inSampleSize = inSampleSize;
                    final Bitmap bm = BitmapFactory.decodeFile(filePath, ops);
                    fileContent = CommonTools.bitmapToBase64(bm);
                    if (currentViewId == iv_content_photo_1.getId()) {
                        fileContentArray[0] = fileContent;
                        filePathArray[0] = filePath;
                        iv_content_photo_1.setImageBitmap(bm);
                    } else if (currentViewId == iv_content_photo_2.getId()) {
                        fileContentArray[1] = fileContent;
                        filePathArray[1] = filePath;
                        iv_content_photo_2.setImageBitmap(bm);
                    } else if (currentViewId == iv_content_photo_3.getId()) {
                        fileContentArray[2] = fileContent;
                        filePathArray[2] = filePath;
                        iv_content_photo_3.setImageBitmap(bm);
                    } else if (currentViewId == iv_content_photo_4.getId()) {
                        fileContentArray[3] = fileContent;
                        filePathArray[3] = filePath;
                        iv_content_photo_4.setImageBitmap(bm);
                    }
                } else if(data == null) {
                    Toast.makeText(getApplicationContext(),getString(R.string.image_get_data_null),Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(),getString(R.string.image_get_file_null),Toast.LENGTH_SHORT).show();
                }
            }
        } else if(resultCode == Activity.RESULT_CANCELED) {
            if(requestCode == GlobalApplication.REQUEST_IMAGE_CAPTURE) {
                Toast.makeText(getApplicationContext(),getString(R.string.image_capture_user_canceled),Toast.LENGTH_SHORT).show();
            }

            if(requestCode == GlobalApplication.REQUEST_IMAGE_GET) {
                Toast.makeText(getApplicationContext(),getString(R.string.image_get_file_user_canceled),Toast.LENGTH_SHORT).show();
            }

        } else {
            if(requestCode == GlobalApplication.REQUEST_IMAGE_CAPTURE) {
                Toast.makeText(getApplicationContext(),getString(R.string.image_capture_user_error),Toast.LENGTH_SHORT).show();
            }

            if(requestCode == GlobalApplication.REQUEST_IMAGE_GET) {
                Toast.makeText(getApplicationContext(),getString(R.string.image_get_file_user_error),Toast.LENGTH_SHORT).show();
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
