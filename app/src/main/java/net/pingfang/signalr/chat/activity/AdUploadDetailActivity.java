package net.pingfang.signalr.chat.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import net.pingfang.signalr.chat.database.Advertisement;
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

public class AdUploadDetailActivity extends AppCompatActivity implements View.OnClickListener {

    public static final String TAG = AdUploadDetailActivity.class.getSimpleName();

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
    SharedPreferencesHelper sharedPreferencesHelper;
    Advertisement advertisement;
    private TextView btn_activity_back;
    private TextView tv_ad_detail_length;
    private TextView tv_ad_detail_width;
    private TextView tv_ad_detail_code;
    private TextView tv_ad_detail_address;
    private TextView tv_ad_detail_remark;
    private ImageView iv_content_photo_1;
    private ImageView iv_content_photo_2;
    private ImageView iv_content_photo_3;
    private ImageView iv_content_photo_4;
    private Button btn_ad_maintain_save_info;
    private Button btn_ad_maintain_cancel_info;
    private LinearLayout ll_progress_bar_container;
    private ProgressBar pb_operation;
    private TextView tv_pb_operation;
    private Handler mDelivery = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ad_upload_detail);

        sharedPreferencesHelper = SharedPreferencesHelper.newInstance(getApplicationContext());
        Intent intent = getIntent();
        advertisement = intent.getParcelableExtra("advertisement");

        initView();
    }

    private void initView() {
        btn_activity_back = (TextView) findViewById(R.id.btn_activity_back);
        btn_activity_back.setOnClickListener(this);

        tv_ad_detail_length = (TextView) findViewById(R.id.tv_ad_detail_length);
        tv_ad_detail_length.setText(getString(R.string.tv_ad_detail_length, advertisement.getLength()));
        tv_ad_detail_width = (TextView) findViewById(R.id.tv_ad_detail_width);
        tv_ad_detail_width.setText(getString(R.string.tv_ad_detail_width, advertisement.getWidth()));
        tv_ad_detail_code = (TextView) findViewById(R.id.tv_ad_detail_code);
        tv_ad_detail_code.setText(getString(R.string.tv_ad_detail_code, advertisement.getCode()));
        tv_ad_detail_address = (TextView) findViewById(R.id.tv_ad_detail_address);
        tv_ad_detail_address.setText(getString(R.string.tv_ad_detail_address, advertisement.getAddress()));
        tv_ad_detail_remark = (TextView) findViewById(R.id.tv_ad_detail_remark);
        tv_ad_detail_remark.setText(getString(R.string.tv_ad_detail_remark, advertisement.getRemark()));

        iv_content_photo_1 = (ImageView) findViewById(R.id.iv_content_photo_1);
        iv_content_photo_2 = (ImageView) findViewById(R.id.iv_content_photo_2);
        iv_content_photo_3 = (ImageView) findViewById(R.id.iv_content_photo_3);
        iv_content_photo_4 = (ImageView) findViewById(R.id.iv_content_photo_4);

        btn_ad_maintain_save_info = (Button) findViewById(R.id.btn_ad_maintain_save_info);
        btn_ad_maintain_save_info.setOnClickListener(this);
        btn_ad_maintain_cancel_info = (Button) findViewById(R.id.btn_ad_maintain_cancel_info);
        btn_ad_maintain_cancel_info.setOnClickListener(this);

        ll_progress_bar_container = (LinearLayout) findViewById(R.id.ll_progress_bar_container);
        pb_operation = (ProgressBar) findViewById(R.id.pb_operation);
        tv_pb_operation = (TextView) findViewById(R.id.tv_pb_operation);

        new LoadImageContentTask().execute(advertisement.getPath1(), advertisement.getPath2(), advertisement.getPath3(), advertisement.getPath4());
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
                            new OkHttpCommonUtil.Param(KEY_URL_AD_MAINTAIN_ADDRESS, advertisement.getAddress()),
                            new OkHttpCommonUtil.Param(KEY_URL_AD_MAINTAIN_CODE, advertisement.getCode()),
                            new OkHttpCommonUtil.Param(KEY_URL_AD_MAINTAIN_LENGTH, advertisement.getLength()),
                            new OkHttpCommonUtil.Param(KEY_URL_AD_MAINTAIN_WIDTH, advertisement.getWidth()),
                            new OkHttpCommonUtil.Param(KEY_URL_AD_MAINTAIN_REMARK, advertisement.getRemark()),
                            new OkHttpCommonUtil.Param(KEY_URL_AD_MAINTAIN_LOCATION_LAT, advertisement.getLat()),
                            new OkHttpCommonUtil.Param(KEY_URL_AD_MAINTAIN_LOCATION_LNG, advertisement.getLng()),
                            new OkHttpCommonUtil.Param(KEY_URL_AD_MAINTAIN_PIC, tmpContent)
                    },
                    new HttpBaseCallback() {

                        @Override
                        public void onFailure(Request request, IOException e) {
                            super.onFailure(request, e);
                            mDelivery.post(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getApplicationContext(), "当前网络状况不好，上传失败", Toast.LENGTH_SHORT).show();
                                    ll_progress_bar_container.setVisibility(View.GONE);
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
                                final String message = jsonObject.getString("message");
                                if (status == 0) {
                                    String selection = AppContract.AdvertisementEntry._ID + " = ?";
                                    String[] selectionArgs = new String[]{String.valueOf(advertisement.getId())};
                                    getApplicationContext().getContentResolver().delete(AppContract.AdvertisementEntry.CONTENT_URI,
                                            selection, selectionArgs);
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
                                    if(status == -1) {
                                        String selection = AppContract.AdvertisementEntry._ID + " = ?";
                                        String[] selectionArgs = new String[]{String.valueOf(advertisement.getId())};
                                        getApplicationContext().getContentResolver().delete(AppContract.AdvertisementEntry.CONTENT_URI,
                                                selection, selectionArgs);
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                ll_progress_bar_container.setVisibility(View.GONE);
                                                Toast.makeText(getApplicationContext(),
                                                        message,
                                                        Toast.LENGTH_SHORT).show();

                                            }
                                        });
                                    } else {
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
                            } catch (JSONException e) {
                                e.printStackTrace();
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

    @Override
    public void onClick(View view) {
        int viewId = view.getId();
        switch (viewId) {
            case R.id.btn_activity_back:
                navigateUp();
                break;
            case R.id.btn_ad_maintain_save_info:
                if (NetUtil.isConnected(getApplicationContext())) {
                    storeOnWeb();
                } else {
                    Toast.makeText(getApplicationContext(), "当前没有网络连接", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.btn_ad_maintain_cancel_info:
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

                String fileContent = CommonTools.imagePath2Base64(path);

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
