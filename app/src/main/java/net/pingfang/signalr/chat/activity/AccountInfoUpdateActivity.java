package net.pingfang.signalr.chat.activity;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.okhttp.Response;

import net.pingfang.signalr.chat.R;
import net.pingfang.signalr.chat.constant.app.AppConstants;
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

public class AccountInfoUpdateActivity extends AppCompatActivity implements View.OnClickListener{

    public static final String TAG = AccountInfoUpdateActivity.class.getSimpleName();

    public static final String URL_ACCOUNT_INFO_UPDATE = GlobalApplication.URL_WEB_API_HOST + "/api/WebAPI/User/PerfectInfo";
    public static final String KEY_URL_ACCOUNT_INFO_UPDATE_UID = "id";
    public static final String KEY_URL_ACCOUNT_INFO_UPDATE_USER_NAME = "userName";
    public static final String KEY_URL_ACCOUNT_INFO_UPDATE_NICKNAME = "nickname";
    public static final String KEY_URL_ACCOUNT_INFO_UPDATE_REAL_NAME = "realName";
    public static final String KEY_URL_ACCOUNT_INFO_UPDATE_PHONE = "mobile";
    public static final String KEY_URL_ACCOUNT_INFO_UPDATE_QQ = "qq";
    public static final String KEY_URL_ACCOUNT_INFO_UPDATE_PIC = "pic";
    public static final String KEY_URL_ACCOUNT_INFO_UPDATE_ADDRESS = "address";

    TextView btn_activity_back;

    private EditText et_account_username;
    private EditText et_account_nickname;
    private EditText et_account_realname;
    private EditText et_account_address;
    private EditText et_account_phone;
    private EditText et_account_qq;
    private ImageView iv_account_portrait;

    private Button btn_account_info_save;
    private Button btn_account_info_cancel;

    SharedPreferencesHelper sharedPreferencesHelper;

    Dialog dialog;
    Uri targetUri;
    String tmpFilePath;
    String fileContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_info_update);

        sharedPreferencesHelper = SharedPreferencesHelper.newInstance(getApplicationContext());
        initView();
    }

    private void initView() {
        btn_activity_back = (TextView) findViewById(R.id.btn_activity_back);
        btn_activity_back.setOnClickListener(this);

        et_account_username = (EditText) findViewById(R.id.et_account_username);
        et_account_nickname = (EditText) findViewById(R.id.et_account_nickname);
        et_account_realname = (EditText) findViewById(R.id.et_account_realname);
        et_account_address = (EditText) findViewById(R.id.et_account_address);
        et_account_phone = (EditText) findViewById(R.id.et_account_phone);
        et_account_qq = (EditText) findViewById(R.id.et_account_qq);

        iv_account_portrait = (ImageView) findViewById(R.id.iv_account_portrait);
        iv_account_portrait.setOnClickListener(this);

        btn_account_info_save = (Button) findViewById(R.id.btn_account_info_save);
        btn_account_info_save.setOnClickListener(this);
        btn_account_info_cancel = (Button) findViewById(R.id.btn_account_info_cancel);
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
            if(requestCode == GlobalApplication.REQUEST_IMAGE_CAPTURE) {

                String filePath = tmpFilePath;
                Bitmap bitmap = MediaFileUtils.decodeBitmapFromPath(filePath,
                        MediaFileUtils.dpToPx(getApplicationContext(), 150),
                        MediaFileUtils.dpToPx(getApplicationContext(), 150));
                iv_account_portrait.setImageBitmap(bitmap);
                fileContent = CommonTools.bitmapToBase64(bitmap);
            } else if(requestCode == GlobalApplication.REQUEST_IMAGE_GET) {
                if(data != null && data.getData() != null) {
                    Uri uri = data.getData();
                    String filePath = MediaFileUtils.getRealPathFromURI(getApplicationContext(), uri);
                    Bitmap bitmap = MediaFileUtils.decodeBitmapFromPath(filePath,
                            MediaFileUtils.dpToPx(getApplicationContext(), 150),
                            MediaFileUtils.dpToPx(getApplicationContext(), 150));
                    iv_account_portrait.setImageBitmap(bitmap);
                    fileContent = CommonTools.bitmapToBase64(bitmap);
                } else if(data == null) {
                    Toast.makeText(getApplicationContext(),getString(R.string.image_capture_data_null),Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(),getString(R.string.image_capture_file_null),Toast.LENGTH_SHORT).show();
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

    @Override
    public void onClick(View view) {
        int viewId = view.getId();
        switch (viewId) {
            case R.id.btn_activity_back:
                navigateUp();
                break;
            case R.id.iv_account_portrait:
                showDialog();
                break;
            case R.id.btn_account_info_save:
                saveOrUpdateAccountInfo();
                break;
        }
    }

    private void saveOrUpdateAccountInfo() {
        OkHttpCommonUtil okHttp = OkHttpCommonUtil.newInstance(getApplicationContext());
        okHttp.postRequest(URL_ACCOUNT_INFO_UPDATE,
                new OkHttpCommonUtil.Param[] {
                    new OkHttpCommonUtil.Param(KEY_URL_ACCOUNT_INFO_UPDATE_UID,sharedPreferencesHelper.getStringValue(AppConstants.KEY_SYS_CURRENT_UID)),
                    new OkHttpCommonUtil.Param(KEY_URL_ACCOUNT_INFO_UPDATE_USER_NAME,et_account_username.getText().toString().trim()),
                    new OkHttpCommonUtil.Param(KEY_URL_ACCOUNT_INFO_UPDATE_NICKNAME,et_account_nickname.getText().toString().trim()),
                    new OkHttpCommonUtil.Param(KEY_URL_ACCOUNT_INFO_UPDATE_REAL_NAME,et_account_realname.getText().toString().trim()),
                    new OkHttpCommonUtil.Param(KEY_URL_ACCOUNT_INFO_UPDATE_PHONE,et_account_phone.getText().toString().trim()),
                    new OkHttpCommonUtil.Param(KEY_URL_ACCOUNT_INFO_UPDATE_QQ,et_account_qq.getText().toString().trim()),
                    new OkHttpCommonUtil.Param(KEY_URL_ACCOUNT_INFO_UPDATE_PIC,fileContent),
                    new OkHttpCommonUtil.Param(KEY_URL_ACCOUNT_INFO_UPDATE_ADDRESS,et_account_address.getText().toString().trim())
                },
                new HttpBaseCallback() {
                    @Override
                    public void onResponse(Response response) throws IOException {
                        String result = response.body().string();
                        Log.d(TAG,"URL_ACCOUNT_INFO_UPDATE return " + result);
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
                                                getString(R.string.toast_account_info_update_ok),
                                                Toast.LENGTH_SHORT).show();
                                        navigateUp();
                                    }
                                });
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
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
