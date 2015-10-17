package net.pingfang.signalr.chat.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;

import net.pingfang.signalr.chat.R;
import net.pingfang.signalr.chat.util.CommonUtil;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class AdMaintainActivity extends AppCompatActivity implements View.OnClickListener{

    TextView btn_activity_back;

    public static final String URL_AD_MAINTAIN = "http://192.168.0.158/api/WebAPI/Advertisement/PublishAD";

    private EditText slogan, iuaddress, adcode;
    private ImageView upimage;
    MapView mMapView = null; // 地图View
    BaiduMap mBaiduMap = null;
    private LocationClient mLocClient;
    public MyLocationListenner myListener = new MyLocationListenner();
    boolean isFirstLoc = true;
    String slogans, adresss, adcodes, message,FILE_PATH, pic;
    private double dLati, dLong;// 经纬度
    // 动态获取SD卡路径
    String path = "", newpath = "";

    SharedPreferences mySharedPreferences;
    SharedPreferences.Editor editor ;
    boolean net;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_ad_maintain);
        String pathsd=CommonUtil.getSDPath();
        FILE_PATH=pathsd+"/photosed/";
        mySharedPreferences= getSharedPreferences("image", Activity.MODE_PRIVATE);
        editor = mySharedPreferences.edit();

        initView();
    }

    private void initView() {
        btn_activity_back = (TextView) findViewById(R.id.btn_activity_back);
        btn_activity_back.setOnClickListener(this);

        real();
        slogan = (EditText) this.findViewById(R.id.slogan);
        iuaddress = (EditText) this.findViewById(R.id.iuaddress);
        upimage = (ImageView) this.findViewById(R.id.upimage);
        adcode = (EditText) this.findViewById(R.id.adcode);
        mMapView = (MapView) this.findViewById(R.id.iumap);
        CommonUtil.makeRootDirectory(FILE_PATH);
        if(newpath.length()>1){
            slogan.setText(slogans);
            iuaddress.setText(adresss);
            adcode.setText(adcodes);
            upimage.setImageBitmap(CommonUtil.getBitmapInLocal(newpath));
        }
        mBaiduMap = mMapView.getMap(); // 获取BaiduMap对象
        mLocClient = new LocationClient(this); // 地图在tabhost中，请传入getApplicationContext()
        mLocClient.registerLocationListener(myListener); // 绑定定位监听
        LocationClientOption option = new LocationClientOption(); // 配置参数
        option.setOpenGps(true);
        option.setAddrType("all"); // 设置使其可以获取具体的位置，把精度纬度换成具体地址
        option.setCoorType("bd09ll");
        option.setScanSpan(1000);
        mLocClient.setLocOption(option);
        mLocClient.start(); // 开始定位

        upimage.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // 调用手机摄像头进行拍照
                Intent intent = new Intent();
                intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
                intent.addCategory(Intent.CATEGORY_DEFAULT);
                SimpleDateFormat sDateFormat = new SimpleDateFormat(
                        "yyyy-MM-dd hh:mm:ss");
                String date = sDateFormat.format(new java.util.Date());
                path = FILE_PATH + date + ".jpg";
                File file = new File(path);
                if (file.exists()) {
                    file.delete();
                }
                // 把文件地址转换成Uri格式
                Uri uri = Uri.fromFile(file);
                // 设置系统相机拍摄照片完成后图片文件的存放地址
                intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                startActivityForResult(intent, 0);

            }
        });
    }

    public  void quxiaos(View v){
        editor.clear();
        editor.commit();
        finish();
    }

    //往SharedPreferences里面写数据
    public void save(){
        editor.putString("slogans",slogans );
        editor.putString("adresss",adresss );
        editor.putString("adcodes",adcodes );
        editor.putString("newpath",newpath );
        editor.commit();
        Toast.makeText(getApplicationContext(), "当前没有网络，数据已经保存到了草稿", Toast.LENGTH_SHORT).show();

    }
    //往SharedPreferences里面读数据
    public void real(){
        newpath =mySharedPreferences.getString("newpath", "");
        adcodes=mySharedPreferences.getString("adcodes", "");
        adresss=mySharedPreferences.getString("adresss", "");
        slogans=mySharedPreferences.getString("slogans", "");
    }

    public void quedings(View v) {
        slogans = slogan.getText().toString().trim();
        adresss = iuaddress.getText().toString().trim();
        adcodes = adcode.getText().toString().trim();
        if (slogans.length() <= 0) {
            Toast.makeText(getApplicationContext(), "请输入广告语", Toast.LENGTH_SHORT).show();
            return;
        } else if (adresss.length() <= 0) {
            Toast.makeText(getApplicationContext(), "请输入地址", Toast.LENGTH_SHORT).show();
            return;
        } else if (newpath.length() <= 0) {
            Toast.makeText(getApplicationContext(), "", Toast.LENGTH_SHORT).show();
            return;
        }
        Bitmap bitmaps = CommonUtil.getBitmapInLocal(newpath);
        pic = CommonUtil.bitmapToBase64(bitmaps);
        net=CommonUtil.isConnected(this);
        if(net){
            MyTask mytask = new MyTask();
            mytask.execute();
        }else{
            save();
        }

    }

    class MyTask extends AsyncTask<String, Integer, String> {

        @Override
        protected String doInBackground(String... arg0) {

			/* 建立HTTPost对象 */
            HttpPost httpRequest = new HttpPost(URL_AD_MAINTAIN);
			/*
			 * NameValuePair实现请求参数的封装
			 */
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("content", URLEncoder
                    .encode(slogans)));
            params.add(new BasicNameValuePair("pic", pic));
            params.add(new BasicNameValuePair("address", URLEncoder
                    .encode(adresss)));
            params.add(new BasicNameValuePair("adcode", URLEncoder
                    .encode(adcodes)));
            params.add(new BasicNameValuePair("userId", "1"));
            try {
				/* 添加请求参数到请求对象 */
                httpRequest.setEntity(new UrlEncodedFormEntity(params,
                        HTTP.UTF_8));
				/* 发送请求并等待响应 */
                HttpResponse httpResponse = new DefaultHttpClient()
                        .execute(httpRequest);
				/* 若状态码为200 ok */
                System.out.println("状态码为:"
                        + httpResponse.getStatusLine().getStatusCode());
                if (httpResponse.getStatusLine().getStatusCode() == 200) {
					/* 读返回数据 */
                    String strResult = EntityUtils.toString(httpResponse
                            .getEntity());
                    /** 解析json获取数据上传结果 */
                    JSONObject json2 = new JSONObject(strResult);
                    message = json2.getString("message");
                    System.out.println(strResult);
                } else {
                    System.out.println("Error Response: "
                            + httpResponse.getStatusLine().toString());
                }
            } catch (ClientProtocolException e) {
                System.out.println("ClientProtocolException"
                        + e.getMessage().toString());
                e.printStackTrace();
            } catch (IOException e) {
                System.out.println("IOException:" + e.getMessage().toString());
                e.printStackTrace();
            } catch (Exception e) {
                System.out.println("e.getMessage().toString()");
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            if (message.equals("发布成功")) {
                editor.clear();
                editor.commit();
                finish();
            } else {
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
            }
        }
    }


    /**
     * 定位 并显示我的位置
     *
     * @author Administrator
     *
     */
    public class MyLocationListenner implements BDLocationListener { // 定位

        @Override
        public void onReceiveLocation(BDLocation location) {
            if (location == null || mMapView == null)
                return;
            // 设置定位参数
            MyLocationData locData = new MyLocationData.Builder()
                    .accuracy(location.getRadius()).direction(100)
                    .latitude(location.getLatitude())
                    .longitude(location.getLongitude()).build();
            mBaiduMap.setMyLocationData(locData);
            if (isFirstLoc) {
                isFirstLoc = false;
                LatLng ll = new LatLng(location.getLatitude(),
                        location.getLongitude());
                MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(ll);
                mBaiduMap.animateMapStatus(u);

                adresss = location.getAddrStr();
                dLati = location.getLatitude();
                dLong = location.getLongitude();
                if(adresss.length()>1){
                    // 把定位到的地址传递给文本框
                    iuaddress.setText(adresss);
                }

            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 0) {
            boolean ok = CommonUtil.fileIsExists(path);
            if (ok) {
                newpath = path;
                // 压缩图片
                CommonUtil.dealImage(newpath, newpath);
                File file = new File(newpath);
                System.out.println(newpath);
                Uri uri = Uri.fromFile(file);
                upimage.setImageURI(uri);
            }

        } else if (requestCode == 1) {
            upimage.setImageURI(data.getData());
        }
    }

    @Override
    public void onClick(View view) {
        int viewId = view.getId();
        switch(viewId) {
            case R.id.btn_activity_back:
                navigateUp();
                break;
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
