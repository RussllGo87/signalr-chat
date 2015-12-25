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
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
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
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import net.pingfang.signalr.chat.R;
import net.pingfang.signalr.chat.adapter.PhotoGridViewAdapter;
import net.pingfang.signalr.chat.constant.app.AppConstants;
import net.pingfang.signalr.chat.database.AdResource;
import net.pingfang.signalr.chat.database.AppContract;
import net.pingfang.signalr.chat.location.LocationListenerImpl;
import net.pingfang.signalr.chat.location.LocationNotify;
import net.pingfang.signalr.chat.location.LocationUtil;
import net.pingfang.signalr.chat.net.HttpBaseCallback;
import net.pingfang.signalr.chat.net.NetUtil;
import net.pingfang.signalr.chat.net.OkHttpCommonUtil;
import net.pingfang.signalr.chat.util.CommonTools;
import net.pingfang.signalr.chat.util.FileUtil;
import net.pingfang.signalr.chat.util.GlobalApplication;
import net.pingfang.signalr.chat.util.ImageUtils;
import net.pingfang.signalr.chat.util.MediaFileUtils;
import net.pingfang.signalr.chat.util.SharedPreferencesHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

public class ResourcePostActivity extends AppCompatActivity implements View.OnClickListener, LocationNotify {

    public static final String TAG = ResourcePostActivity.class.getSimpleName();

    public static final String URL_ACCOUNT_INFO_LOAD = GlobalApplication.URL_WEB_API_HOST + "/api/WebAPI/User/GetUser";
    public static final String KEY_URL_ACCOUNT_INFO_LOAD_UID = "id";

    public static final String URL_RESOURCE_POST = GlobalApplication.URL_WEB_API_HOST + "/api/WebAPI/ResourceWall/PublishResource";
    public static final String KEY_RESOURCE_POST_UID = "userId";
    public static final String KEY_RESOURCE_POST_WIDTH = "length";
    public static final String KEY_RESOURCE_POST_HEIGHT = "width";
    public static final String KEY_RESOURCE_POST_ADDRESS = "address";
    public static final String KEY_RESOURCE_POST_CONTACTS = "contactName";
    public static final String KEY_RESOURCE_POST_PHONE = "mobile";
    public static final String KEY_RESOURCE_POST_MATERIAL = "wallType";
    public static final String KEY_RESOURCE_POST_REMARK = "remark";
    public static final String KEY_RESOURCE_POST_PROFILE = "pic";
    public static final String KEY_URL_RESOURCE_POST_LOCATION_LAT = "lat";
    public static final String KEY_URL_RESOURCE_POST_LOCATION_LNG = "lng";

    public LocationListenerImpl locationListener;
    PhotoGridViewAdapter adapter;
    SharedPreferencesHelper sharedPreferencesHelper;
    Dialog dialog;
    Uri targetUri;
    String tmpFilePath;
    String fileContent;
    //    List<String> fileContentList = new ArrayList<>();
    String[] fileContentArray = new String[4];
    String[] filePathArray = new String[4];
    // GPS 状态相关变量
    boolean gpsStatus = false;
    private TextView btn_activity_back;
    private EditText et_resource_width;
    private EditText et_resource_height;
    private EditText et_resource_location;
    private EditText et_resource_contacts;
    private EditText et_resource_phone;
    private EditText et_resource_material;
    private EditText et_resource_remark;
    private ImageView iv_content_photo_1;
    private ImageView iv_content_photo_2;
    private ImageView iv_content_photo_3;
    private ImageView iv_content_photo_4;

    //    private GridView gv_camera;
    //    private TextView tv_add_pic;
    //    private ImageView iv_resource_profile;

    private Button btn_resource_save;
    private Button btn_resource_cancel;
    private LocationClient locationClient;
    private LatLng currentLatLng;
    private int currentViewId = 0;
    private LinearLayout ll_progress_bar_containerp;
    private ProgressBar pb_operationp;
    private TextView tv_pb_operationp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resource_post);

        sharedPreferencesHelper = SharedPreferencesHelper.newInstance(getApplicationContext());
        checkGpsStatus();
        initView();
        initLocation();
        loadAccountInfo();
    }

    /**
     * 检查gps状态并提示用户打开gps
     */
    public void checkGpsStatus() {
        gpsStatus = LocationUtil.isGpsOPen(getApplicationContext());
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

    private void initView() {
        btn_activity_back = (TextView) findViewById(R.id.btn_activity_back);
        btn_activity_back.setOnClickListener(this);

        et_resource_width = (EditText) findViewById(R.id.et_resource_width);
        et_resource_height = (EditText) findViewById(R.id.et_resource_height);
        et_resource_location = (EditText) findViewById(R.id.et_resource_location);
        et_resource_location.setEnabled(false);
        et_resource_contacts = (EditText) findViewById(R.id.et_resource_contacts);
        et_resource_phone = (EditText) findViewById(R.id.et_resource_phone);
        et_resource_material = (EditText) findViewById(R.id.et_resource_material);
        et_resource_remark = (EditText) findViewById(R.id.et_resource_remark);

        iv_content_photo_1 = (ImageView) findViewById(R.id.iv_content_photo_1);
        iv_content_photo_1.setOnClickListener(this);
        iv_content_photo_2 = (ImageView) findViewById(R.id.iv_content_photo_2);
        iv_content_photo_2.setOnClickListener(this);
        iv_content_photo_3 = (ImageView) findViewById(R.id.iv_content_photo_3);
        iv_content_photo_3.setOnClickListener(this);
        iv_content_photo_4 = (ImageView) findViewById(R.id.iv_content_photo_4);
        iv_content_photo_4.setOnClickListener(this);

        ll_progress_bar_containerp = (LinearLayout) findViewById(R.id.ll_progress_bar_containerp);
        pb_operationp = (ProgressBar) findViewById(R.id.pb_operationp);
        tv_pb_operationp = (TextView) findViewById(R.id.tv_pb_operationp);

        //        gv_camera = (GridView) findViewById(R.id.gv_camera);
        //        adapter = new PhotoGridViewAdapter(getApplicationContext(),this);
        //        gv_camera.setAdapter(adapter);
//        iv_resource_profile = (ImageView) findViewById(R.id.iv_resource_profile);
//        iv_resource_profile.setOnClickListener(this);
        //        tv_add_pic = (TextView) findViewById(R.id.tv_add_pic);
        //        tv_add_pic.setOnClickListener(this);

        btn_resource_save = (Button) findViewById(R.id.btn_resource_save);
        btn_resource_save.setOnClickListener(this);
        btn_resource_cancel = (Button) findViewById(R.id.btn_resource_cancel);
        btn_resource_cancel.setOnClickListener(this);
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

        currentLatLng = new LatLng(bdLocation.getLatitude(),
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
//            case R.id.tv_add_pic:
//                if (fileContentList.size() < 4) {
//                    showDialog();
//                } else {
//                    Toast.makeText(getApplicationContext(),
//                            getString(R.string.toast_resource_post_pic_num_error_2),
//                            Toast.LENGTH_SHORT).show();
//                }
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
            case R.id.btn_resource_save:
                storeOrPostRes();
                break;
            case R.id.btn_resource_cancel:
                navigateUp();
                break;
        }
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
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

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

//                fileContent = CommonTools.bitmapToBase64(bm);

                if (currentViewId == iv_content_photo_1.getId()) {
//                    fileContentArray[0] = fileContent;
                    filePathArray[0] = filePath;
                    iv_content_photo_1.setImageBitmap(bm);
                    new LoadImageContentTask().execute(filePathArray[0],"0");
                } else if (currentViewId == iv_content_photo_2.getId()) {
//                    fileContentArray[1] = fileContent;
                    filePathArray[1] = filePath;
                    iv_content_photo_2.setImageBitmap(bm);
                    new LoadImageContentTask().execute(filePathArray[1], "1");
                } else if (currentViewId == iv_content_photo_3.getId()) {
//                    fileContentArray[2] = fileContent;
                    filePathArray[2] = filePath;
                    iv_content_photo_3.setImageBitmap(bm);
                    new LoadImageContentTask().execute(filePathArray[2], "2");
                } else if (currentViewId == iv_content_photo_4.getId()) {
//                    fileContentArray[3] = fileContent;
                    filePathArray[3] = filePath;
                    iv_content_photo_4.setImageBitmap(bm);
                    new LoadImageContentTask().execute(filePathArray[3], "3");
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
//                    fileContent = CommonTools.bitmapToBase64(bm);
                    if (currentViewId == iv_content_photo_1.getId()) {
//                        fileContentArray[0] = fileContent;
                        filePathArray[0] = filePath;
                        iv_content_photo_1.setImageBitmap(bm);
                        new LoadImageContentTask().execute(filePathArray[0], "0");
                    } else if (currentViewId == iv_content_photo_2.getId()) {
//                        fileContentArray[1] = fileContent;
                        filePathArray[1] = filePath;
                        iv_content_photo_2.setImageBitmap(bm);
                        new LoadImageContentTask().execute(filePathArray[1], "1");
                    } else if (currentViewId == iv_content_photo_3.getId()) {
//                        fileContentArray[2] = fileContent;
                        filePathArray[2] = filePath;
                        iv_content_photo_3.setImageBitmap(bm);
                        new LoadImageContentTask().execute(filePathArray[2], "2");
                    } else if (currentViewId == iv_content_photo_4.getId()) {
//                        fileContentArray[3] = fileContent;
                        filePathArray[3] = filePath;
                        iv_content_photo_4.setImageBitmap(bm);
                        new LoadImageContentTask().execute(filePathArray[3],"3");
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

    //获得用户名和密码
    public void loadAccountInfo() {
        OkHttpCommonUtil okHttp = OkHttpCommonUtil.newInstance(getApplicationContext());
        okHttp.getRequest(URL_ACCOUNT_INFO_LOAD,
                new OkHttpCommonUtil.Param[]{
                        new OkHttpCommonUtil.Param(KEY_URL_ACCOUNT_INFO_LOAD_UID,
                                sharedPreferencesHelper.getStringValue(AppConstants.KEY_SYS_CURRENT_UID))
                },
                new HttpBaseCallback() {

                    @Override
                    public void onFailure(Request request, IOException e) {
                        super.onFailure(request, e);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(),
                                        getString(R.string.toast_account_info_load_failure),
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    @Override
                    public void onResponse(Response response) throws IOException {
                        String result = response.body().string();
                        Log.d(TAG, "URL_ACCOUNT_INFO_LOAD return " + result);
                        JSONObject jsonObject = null;
                        try {
                            jsonObject = new JSONObject(result);
                            int status = jsonObject.getInt("status");
                            String message = jsonObject.getString("message");
                            if (status == 0) {
                                JSONObject resultJson = jsonObject.getJSONObject("result");
                                final String phoneNo = resultJson.getString("phone");

                                final String realname = resultJson.getString("realname");

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(getApplicationContext(),
                                                getString(R.string.toast_account_info_load_ok),
                                                Toast.LENGTH_SHORT).show();

                                        if (phoneNo.length() > 0) {
                                            et_resource_phone.setText(phoneNo);
                                            et_resource_phone.setEnabled(false);
                                        }


                                        if (realname.length() > 0) {
                                            et_resource_contacts.setText(realname);
                                            et_resource_contacts.setEnabled(false);
                                        }

                                        //
                                    }
                                });
                            } else {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(getApplicationContext(),
                                                getString(R.string.toast_account_info_load_failure),
                                                Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getApplicationContext(),
                                            getString(R.string.toast_account_info_load_failure),
                                            Toast.LENGTH_SHORT).show();

                                }
                            });

                        }
                    }
                });
    }

    private void storeOrPostRes() {

        if (et_resource_width.getText().toString().trim().length() <= 0) {
            Toast.makeText(getApplicationContext(), "长度不能为空", Toast.LENGTH_SHORT).show();
            return;
        } else if (et_resource_height.getText().toString().trim().length() <= 0) {
            Toast.makeText(getApplicationContext(), "宽度不能为空", Toast.LENGTH_SHORT).show();
            return;
        } else if (et_resource_location.getText().toString().trim().equals("")) {
            Toast.makeText(getApplicationContext(), "地址不能为空", Toast.LENGTH_SHORT).show();
            return;
        } else if (et_resource_contacts.getText().toString().trim().equals("")) {
            Toast.makeText(getApplicationContext(), "联系人不能为空", Toast.LENGTH_SHORT).show();
            return;
        } else if (et_resource_phone.getText().toString().trim().equals("")) {
            Toast.makeText(getApplicationContext(), "联系电话不能为空", Toast.LENGTH_SHORT).show();
            return;
        } else if (et_resource_material.getText().toString().trim().equals("")) {
            Toast.makeText(getApplicationContext(), "墙体材质不能为空", Toast.LENGTH_SHORT).show();
            return;
        } else if (!CommonTools.isPhoneNumber(et_resource_phone.getText().toString().trim())) {
            Toast.makeText(getApplicationContext(), "请输入正确的电话号码", Toast.LENGTH_SHORT).show();
            return;
        }

        for (int i = 0; i < filePathArray.length; i++) {
            String path = filePathArray[i];
            if (TextUtils.isEmpty(path)) {
                Toast.makeText(getApplicationContext(), getString(R.string.toast_resource_posting_error_pic_no, (i + 1)), Toast.LENGTH_LONG).show();
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

    private void storeOnLocalDb() {
        ContentValues values = new ContentValues();
        values.put(AppContract.AdResourceEntry.COLUMN_NAME_RESOURCE_UID, sharedPreferencesHelper.getStringValue(AppConstants.KEY_SYS_CURRENT_UID));
        values.put(AppContract.AdResourceEntry.COLUMN_NAME_RESOURCE_LENGTH, et_resource_width.getText().toString().trim());
        values.put(AppContract.AdResourceEntry.COLUMN_NAME_RESOURCE_WIDTH, et_resource_height.getText().toString().trim());
        values.put(AppContract.AdResourceEntry.COLUMN_NAME_RESOURCE_ADDRESS, et_resource_location.getText().toString().trim());
        values.put(AppContract.AdResourceEntry.COLUMN_NAME_RESOURCE_CONTACT, et_resource_contacts.getText().toString().trim());
        values.put(AppContract.AdResourceEntry.COLUMN_NAME_RESOURCE_PHONE, et_resource_phone.getText().toString().trim());
        values.put(AppContract.AdResourceEntry.COLUMN_NAME_RESOURCE_MATERIAL, et_resource_material.getText().toString().trim());
        values.put(AppContract.AdResourceEntry.COLUMN_NAME_RESOURCE_REMARK, et_resource_remark.getText().toString().trim());
        values.put(AppContract.AdResourceEntry.COLUMN_NAME_RESOURCE_LAT, currentLatLng.latitude);
        values.put(AppContract.AdResourceEntry.COLUMN_NAME_RESOURCE_LNG, currentLatLng.longitude);
        values.put(AppContract.AdResourceEntry.COLUMN_NAME_RESOURCE_PATH_P1, filePathArray[0]);
        values.put(AppContract.AdResourceEntry.COLUMN_NAME_RESOURCE_PATH_P2, filePathArray[1]);
        values.put(AppContract.AdResourceEntry.COLUMN_NAME_RESOURCE_PATH_P3, filePathArray[2]);
        values.put(AppContract.AdResourceEntry.COLUMN_NAME_RESOURCE_PATH_P4, filePathArray[3]);
        values.put(AppContract.AdResourceEntry.COLUMN_NAME_RESOURCE_STATUS, AdResource.RESOURCE_STATUS_UPLOAD_ERROR);

        getContentResolver().insert(AppContract.AdResourceEntry.CONTENT_URI, values);
    }

    public void storeOnWeb() {
        ll_progress_bar_containerp.setVisibility(View.VISIBLE);
        tv_pb_operationp.setText("数据上传中");

        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < fileContentArray.length; i++) {
            sb.append(fileContentArray[i]);
            if (i != (fileContentArray.length - 1)) {
                sb.append(";");
            }
        }

        String tmpContent = sb.toString();
        if (!TextUtils.isEmpty(tmpContent)) {
            OkHttpCommonUtil.Param[] params = new OkHttpCommonUtil.Param[]{
                    new OkHttpCommonUtil.Param(KEY_RESOURCE_POST_UID, sharedPreferencesHelper.getStringValue(AppConstants.KEY_SYS_CURRENT_UID)),
                    new OkHttpCommonUtil.Param(KEY_RESOURCE_POST_WIDTH, et_resource_width.getText().toString().trim()),
                    new OkHttpCommonUtil.Param(KEY_RESOURCE_POST_HEIGHT, et_resource_height.getText().toString().trim()),
                    new OkHttpCommonUtil.Param(KEY_RESOURCE_POST_ADDRESS, et_resource_location.getText().toString().trim()),
                    new OkHttpCommonUtil.Param(KEY_RESOURCE_POST_CONTACTS, et_resource_contacts.getText().toString().trim()),
                    new OkHttpCommonUtil.Param(KEY_RESOURCE_POST_PHONE, et_resource_phone.getText().toString().trim()),
                    new OkHttpCommonUtil.Param(KEY_RESOURCE_POST_MATERIAL, et_resource_material.getText().toString().trim()),
                    new OkHttpCommonUtil.Param(KEY_RESOURCE_POST_REMARK, et_resource_remark.getText().toString().trim()),
                    new OkHttpCommonUtil.Param(KEY_URL_RESOURCE_POST_LOCATION_LAT, currentLatLng.latitude),
                    new OkHttpCommonUtil.Param(KEY_URL_RESOURCE_POST_LOCATION_LNG, currentLatLng.longitude),
                    new OkHttpCommonUtil.Param(KEY_RESOURCE_POST_PROFILE, tmpContent)
            };
            OkHttpCommonUtil okHttp = OkHttpCommonUtil.newInstance(getApplicationContext());
            okHttp.postRequest(
                    URL_RESOURCE_POST,
                    params,
                    new HttpBaseCallback() {

                        @Override
                        public void onFailure(Request request, IOException e) {
                            storeOnLocalDb();
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    ll_progress_bar_containerp.setVisibility(View.GONE);
                                    Toast.makeText(getApplicationContext(), "当前网络状况不好，数据已经保存到了本地", Toast.LENGTH_SHORT).show();
                                    navigateUp();
                                }
                            });
                        }

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
                                            ll_progress_bar_containerp.setVisibility(View.GONE);
                                            Toast.makeText(getApplicationContext(),
                                                    getString(R.string.toast_resource_post_ok),
                                                    Toast.LENGTH_SHORT).show();
                                            navigateUp();
                                        }
                                    });
                                } else {
                                    storeOnLocalDb();
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            ll_progress_bar_containerp.setVisibility(View.GONE);
                                            Toast.makeText(getApplicationContext(),
                                                    getString(R.string.toast_resource_posting_error),
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
                                        ll_progress_bar_containerp.setVisibility(View.GONE);
                                        Toast.makeText(getApplicationContext(),
                                                getString(R.string.toast_resource_post_error),
                                                Toast.LENGTH_SHORT).show();
                                        navigateUp();
                                    }
                                });
                            }
                        }
                    }
            );
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

    class LoadImageContentTask extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... params) {
            String path = params[0];
            String position = params[1];

            String content = CommonTools.imagePath2Base64(path);

            publishProgress(position, content);

            return "ok";
        }

        @Override
        protected void onProgressUpdate(String... values) {
            String position = values[0];
            String content = values[1];

            int positionInt = Integer.valueOf(position);
            if(positionInt < 4) {
                fileContentArray[positionInt] = content;
            }
        }
    }

}
