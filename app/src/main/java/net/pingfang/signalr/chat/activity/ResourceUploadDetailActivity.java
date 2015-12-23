package net.pingfang.signalr.chat.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import net.pingfang.signalr.chat.R;
import net.pingfang.signalr.chat.constant.app.AppConstants;
import net.pingfang.signalr.chat.database.AdResource;
import net.pingfang.signalr.chat.database.AppContract;
import net.pingfang.signalr.chat.net.HttpBaseCallback;
import net.pingfang.signalr.chat.net.NetUtil;
import net.pingfang.signalr.chat.net.OkHttpCommonUtil;
import net.pingfang.signalr.chat.util.CommonTools;
import net.pingfang.signalr.chat.util.GlobalApplication;
import net.pingfang.signalr.chat.util.ImageUtils;
import net.pingfang.signalr.chat.util.SharedPreferencesHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class ResourceUploadDetailActivity extends AppCompatActivity implements View.OnClickListener{

    public static final String TAG = ResourceUploadDetailActivity.class.getSimpleName();

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

    TextView btn_activity_back;

    TextView tv_resource_upload_detail_width;
    TextView tv_resource_upload_detail_height;

    TextView tv_resource_upload_detail_contact;
    TextView tv_resource_upload_detail_contact_info;

    TextView tv_resource_upload_detail_address;
    TextView tv_resource_upload_detail_material;
    TextView tv_resource_upload_detail_remark;

    ImageView iv_content_photo_1;
    ImageView iv_content_photo_2;
    ImageView iv_content_photo_3;
    ImageView iv_content_photo_4;

    Button btn_resource_upload_save_info;
    Button btn_resource_upload_cancel_info;

    private LinearLayout ll_progress_bar_container;
    private ProgressBar pb_operation;
    private TextView tv_pb_operation;

    SharedPreferencesHelper sharedPreferencesHelper;
    AdResource resource;
    String[] fileContentArray = new String[4];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resource_upload_detail);

        sharedPreferencesHelper  = SharedPreferencesHelper.newInstance(getApplicationContext());
        Intent intent = getIntent();
        resource = intent.getParcelableExtra("resource");
        initView();
        initValues();
    }

    private void initView() {
        btn_activity_back = (TextView) findViewById(R.id.btn_activity_back);
        btn_activity_back.setOnClickListener(this);

        tv_resource_upload_detail_width = (TextView) findViewById(R.id.tv_resource_upload_detail_width);
        tv_resource_upload_detail_height = (TextView) findViewById(R.id.tv_resource_upload_detail_height);
        tv_resource_upload_detail_contact = (TextView) findViewById(R.id.tv_resource_upload_detail_contact);
        tv_resource_upload_detail_contact_info = (TextView) findViewById(R.id.tv_resource_upload_detail_contact_info);
        tv_resource_upload_detail_address = (TextView) findViewById(R.id.tv_resource_upload_detail_address);
        tv_resource_upload_detail_material = (TextView) findViewById(R.id.tv_resource_upload_detail_material);
        tv_resource_upload_detail_remark = (TextView) findViewById(R.id.tv_resource_upload_detail_remark);

        iv_content_photo_1 = (ImageView) findViewById(R.id.iv_content_photo_1);
        iv_content_photo_2 = (ImageView) findViewById(R.id.iv_content_photo_2);
        iv_content_photo_3 = (ImageView) findViewById(R.id.iv_content_photo_3);
        iv_content_photo_4 = (ImageView) findViewById(R.id.iv_content_photo_4);

        btn_resource_upload_save_info = (Button) findViewById(R.id.btn_resource_upload_save_info);
        btn_resource_upload_save_info.setOnClickListener(this);
        btn_resource_upload_cancel_info = (Button) findViewById(R.id.btn_resource_upload_cancel_info);
        btn_resource_upload_cancel_info.setOnClickListener(this);

        ll_progress_bar_container = (LinearLayout) findViewById(R.id.ll_progress_bar_container);
        pb_operation = (ProgressBar) findViewById(R.id.pb_operation);
        tv_pb_operation = (TextView) findViewById(R.id.tv_pb_operation);
    }

    private void initValues() {
        tv_resource_upload_detail_width.setText(getString(R.string.tv_resource_detail_width, resource.getLength()));
        tv_resource_upload_detail_height.setText(getString(R.string.tv_resource_detail_height, resource.getWidth()));
        tv_resource_upload_detail_contact.setText(getString(R.string.tv_resource_detail_contact,resource.getContact()));
        tv_resource_upload_detail_contact_info.setText(getString(R.string.tv_resource_detail_contact_info, resource.getPhone()));
        tv_resource_upload_detail_address.setText(getString(R.string.tv_resource_detail_address, resource.getAddress()));
        tv_resource_upload_detail_material.setText(getString(R.string.tv_resource_detail_meterial, resource.getMaterial()));
        tv_resource_upload_detail_remark.setText(getString(R.string.tv_resource_detail_remark, resource.getRemark()));

        new LoadImageContentTask().execute(resource.getPath1(), resource.getPath2(), resource.getPath3(), resource.getPath4());
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
            OkHttpCommonUtil.Param[] params = new OkHttpCommonUtil.Param[]{
                    new OkHttpCommonUtil.Param(KEY_RESOURCE_POST_UID, sharedPreferencesHelper.getStringValue(AppConstants.KEY_SYS_CURRENT_UID)),
                    new OkHttpCommonUtil.Param(KEY_RESOURCE_POST_WIDTH, resource.getLength()),
                    new OkHttpCommonUtil.Param(KEY_RESOURCE_POST_HEIGHT, resource.getWidth()),
                    new OkHttpCommonUtil.Param(KEY_RESOURCE_POST_ADDRESS, resource.getAddress()),
                    new OkHttpCommonUtil.Param(KEY_RESOURCE_POST_CONTACTS, resource.getContact()),
                    new OkHttpCommonUtil.Param(KEY_RESOURCE_POST_PHONE, resource.getPhone()),
                    new OkHttpCommonUtil.Param(KEY_RESOURCE_POST_MATERIAL, resource.getMaterial()),
                    new OkHttpCommonUtil.Param(KEY_RESOURCE_POST_REMARK, resource.getRemark()),
                    new OkHttpCommonUtil.Param(KEY_URL_RESOURCE_POST_LOCATION_LAT, resource.getLat()),
                    new OkHttpCommonUtil.Param(KEY_URL_RESOURCE_POST_LOCATION_LNG, resource.getLng()),
                    new OkHttpCommonUtil.Param(KEY_RESOURCE_POST_PROFILE, tmpContent)
            };
            OkHttpCommonUtil okHttp = OkHttpCommonUtil.newInstance(getApplicationContext());
            okHttp.postRequest(
                    URL_RESOURCE_POST,
                    params,
                    new HttpBaseCallback() {

                        @Override
                        public void onFailure(Request request, IOException e) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    ll_progress_bar_container.setVisibility(View.GONE);
                                    Toast.makeText(getApplicationContext(), "当前网络状况不好，数据已经保存到了本地", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }

                        @Override
                        public void onResponse(Response response) throws IOException {
                            String result = response.body().string();
                            Log.d(TAG, "URL_RESOURCE_POST return == " + result);
                            JSONObject jsonObject;
                            try {
                                jsonObject = new JSONObject(result);
                                int status = jsonObject.getInt("status");
                                String message = jsonObject.getString("message");
                                if(status == 0) {
                                    String selection = AppContract.AdResourceEntry._ID + " = ?";
                                    String[] selectionArgs = new String[]{String.valueOf(resource.getId())};
                                    getApplicationContext().getContentResolver().delete(AppContract.AdResourceEntry.CONTENT_URI,
                                            selection, selectionArgs);
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            ll_progress_bar_container.setVisibility(View.GONE);
                                            Toast.makeText(getApplicationContext(),
                                                    getString(R.string.toast_resource_post_ok),
                                                    Toast.LENGTH_SHORT).show();
                                            navigateUp();
                                        }
                                    });
                                } else {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            ll_progress_bar_container.setVisibility(View.GONE);
                                            Toast.makeText(getApplicationContext(),
                                                    getString(R.string.toast_resource_posting_error),
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        ll_progress_bar_container.setVisibility(View.GONE);
                                        Toast.makeText(getApplicationContext(),
                                                getString(R.string.toast_resource_post_error),
                                                Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        }
                    }
            );
        }
    }

    @Override
    public void onClick(View view) {
        int viewId = view.getId();
        switch (viewId) {
            case R.id.btn_activity_back:
                navigateUp();
                break;
            case R.id.btn_resource_upload_save_info:
                if (NetUtil.isConnected(getApplicationContext())) {
                    storeOnWeb();
                } else {
                    Toast.makeText(getApplicationContext(), "当前没有网络连接", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.btn_resource_upload_cancel_info:
                navigateUp();
                break;
        }
    }

    public void navigateUp() {
        Intent upIntent = NavUtils.getParentActivityIntent(this);
        if (NavUtils.shouldUpRecreateTask(this, upIntent)) {
            TaskStackBuilder.create(this)
                    .addNextIntentWithParentStack(upIntent)
                    .startActivities();
        } else {
            onBackPressed();
        }
    }

    class LoadImageContentTask extends AsyncTask<String, Object, String> {
        @Override
        protected String doInBackground(String... params) {

            for (int i = 0; i < params.length; i++) {
                String path = params[i];
                ImageUtils.ImageSize actualImageSize = ImageUtils.getImageSize(path);

                ImageUtils.ImageSize imageViewSize = ImageUtils.getImageViewSize(iv_content_photo_1);
                int inSampleSize = ImageUtils.calculateInSampleSize(actualImageSize, imageViewSize);
                BitmapFactory.Options ops = new BitmapFactory.Options();
                ops.inJustDecodeBounds = false;
                ops.inSampleSize = inSampleSize;
                final Bitmap bm = BitmapFactory.decodeFile(path, ops);

                String fileContent = CommonTools.bitmapToBase64(bm);

                publishProgress(i, bm, fileContent);
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(Object... values) {
            int i = (Integer) values[0];
            Bitmap bm = (Bitmap) values[1];
            String fileContent = (String) values[2];

            if (bm != null) {
                if (i == 0) {
                    fileContentArray[0] = fileContent;
                    iv_content_photo_1.setImageBitmap(bm);
                }
                if (i == 1) {
                    fileContentArray[1] = fileContent;
                    iv_content_photo_2.setImageBitmap(bm);
                }
                if (i == 2) {
                    fileContentArray[2] = fileContent;
                    iv_content_photo_3.setImageBitmap(bm);
                }
                if (i == 3) {
                    fileContentArray[3] = fileContent;
                    iv_content_photo_4.setImageBitmap(bm);
                }
            }
        }
    }
}
