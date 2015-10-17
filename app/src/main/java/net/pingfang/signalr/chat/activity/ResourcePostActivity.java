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
import android.util.Log;
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
import com.baidu.mapapi.map.BaiduMapOptions;
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

public class ResourcePostActivity extends AppCompatActivity implements View.OnClickListener{

    TextView btn_activity_back;
    TextView tv_activity_title;

    public static final String URL_RESOURCE_POST = "http://192.168.0.152:8090/api/WebAPI/ResourceWall/PublishResource";

    private EditText plength,pbroad,upaddress,upmob,contacts,explain;
    private ImageView upimage;
    MapView mMapView = null; // 地图View
    BaiduMap mBaiduMap = null;
    private LocationClient mLocClient;
    public MyLocationListenner myListener = new MyLocationListenner();
    boolean isFirstLoc = true;
    boolean net;
    private double dLati, dLong;//经纬度
    private  String FILE_PATH = "";//SD卡的路径是：/storage/sdcard0
    private String imagename,msg="", adresss,pic,explains;
    String path = "",newpath="";
    //length长,pbroad 宽,upaddress地址,upmob电话,contacts;
    private String length,broad,uaddress,pmob,contactse;
    SharedPreferences mySharedPreferences;
    SharedPreferences.Editor editor ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_resource_post);
        String pathsd= CommonUtil.getSDPath();
        FILE_PATH=pathsd+"/photosed/";

        init();
        String sdPath= CommonUtil.getSDPath();
        System.out.println("SD卡的路径是："+sdPath);
}

    public void init(){

        btn_activity_back = (TextView) findViewById(R.id.btn_activity_back);
        btn_activity_back.setOnClickListener(this);

        tv_activity_title = (TextView) findViewById(R.id.tv_activity_title);

        mySharedPreferences= getSharedPreferences("test", Activity.MODE_PRIVATE);
        editor = mySharedPreferences.edit();
        plength=(EditText) this.findViewById(R.id.plength);
        pbroad=(EditText) this.findViewById(R.id.pbroad);
        contacts=(EditText) this.findViewById(R.id.contacts);
        upaddress=(EditText) this.findViewById(R.id.upaddress);
        upmob=(EditText) this.findViewById(R.id.upmob);
        explain=(EditText) this.findViewById(R.id.explain);
        upimage=(ImageView) this.findViewById(R.id.upimage);
        mMapView = (MapView) this.findViewById(R.id.upmapsess);
        CommonUtil.makeRootDirectory(FILE_PATH);
        real();
        System.out.println("newpath::"+newpath);
        if(uaddress.length()>0){
            explain.setText(explains);
            plength.setText(length);
            pbroad.setText(broad);
            upaddress.setText(uaddress);
            upmob.setText(pmob);
            contacts.setText(contactse);
            upimage.setImageBitmap(CommonUtil.getBitmapInLocal(newpath));
        }


        //地图设置
        BaiduMapOptions options = new BaiduMapOptions();
        options.compassEnabled(false); // 不允许指南针
        options.zoomControlsEnabled(false); // 不显示缩放按钮
        options.scaleControlEnabled(false); // 不显示比例尺
        mBaiduMap = mMapView.getMap(); // 获取BaiduMap对象
        mMapView.removeViewAt(1); // 去掉百度logo
        mBaiduMap.setMyLocationEnabled(false); // 不显示我的位置，样覆盖物代替
        mBaiduMap.setMaxAndMinZoomLevel(24, 16); // 地图的最大最小缩放比例3-18
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
                imagename=date + ".jpg";
                path = FILE_PATH + imagename;
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



    //点击确定按钮获取需要上传的数据
    public void quedings(View v){
        if (CommonUtil.isFastClick()) {
            Toast.makeText(getApplicationContext(), "请不要重复点击按钮", Toast.LENGTH_SHORT).show();

            return ;
        }

        //newpath 图片路径plength长,pbroad 宽,upaddress地址,upmob电话,contacts;
        //length,broad,uaddress,pmob,contactse;  newpath
        explains=explain.getText().toString().trim();
        length=plength.getText().toString().trim();
        broad=pbroad.getText().toString().trim();
        uaddress=upaddress.getText().toString().trim();
        pmob=upmob.getText().toString().trim();
        contactse=contacts.getText().toString().trim();
        if(length.equals("")){
            Toast.makeText(getApplicationContext(), "请输入资源长度", Toast.LENGTH_SHORT).show();
            return;
        }else if(broad.equals("")){
            Toast.makeText(getApplicationContext(), "请输入资源宽度", Toast.LENGTH_SHORT).show();
            return;
        }else if(uaddress.equals("")){
            Toast.makeText(getApplicationContext(), "请打开网络获取地址信息", Toast.LENGTH_SHORT).show();
            return;
        }else if(pmob.equals("")){
            Toast.makeText(getApplicationContext(), "请输入联系电话", Toast.LENGTH_SHORT).show();
            return;
        }else if(contactse.equals("")){
            Toast.makeText(getApplicationContext(), "请输入联系人", Toast.LENGTH_SHORT).show();
            return;
        }else if(!CommonUtil.phoneUtil(pmob)){
            Toast.makeText(getApplicationContext(), "请输入正确的电话号码", Toast.LENGTH_SHORT).show();
            return;
        }
        if(!newpath.equals("")){
            Bitmap bitmaps=CommonUtil.getBitmapInLocal(newpath);
            pic=CommonUtil.bitmapToBase64(bitmaps);
        }else{
            Toast.makeText(getApplicationContext(), "请拍摄照片", Toast.LENGTH_SHORT).show();
            return;
        }
        net=CommonUtil.isConnected(this);
        if(net){
            myAsynTask mat=new myAsynTask();
            mat.execute();
        }else{
            save();
        }

    }

    //在没有网络的状态下把数据保存到SharedPreferences
    public void save(){
        //实例化SharedPreferences对象（第一步）

        //实例化SharedPreferences.Editor对象（第二步）

        //用putString的方法保存数据
        editor.putString("pic",newpath );
        editor.putString("length", length);
        editor.putString("broad", broad);
        editor.putString("uaddress",uaddress );
        editor.putString("contactse",contactse );
        editor.putString("pmob",pmob );
        editor.putString("explains",explains );
        //提交当前数据
        editor.commit();
        //使用toast信息提示框提示成功写入数据
        Toast.makeText(this, "当前没有网络数据已经保存到草稿，等到有网络是在发送" ,
                Toast.LENGTH_LONG).show();

    }
    //读取SharedPreferences中的数据读出来
    public void real(){

        newpath =mySharedPreferences.getString("pic", "");
        length =mySharedPreferences.getString("length", "");
        broad =mySharedPreferences.getString("broad", "");
        uaddress =mySharedPreferences.getString("uaddress", "");
        contactse =mySharedPreferences.getString("contactse", "");
        pmob =mySharedPreferences.getString("pmob", "");
        explains =mySharedPreferences.getString("explains", "");
        System.out.println("读取SharedPreferences中的数据:" + newpath + length + uaddress);
    }
    class myAsynTask extends AsyncTask<String, Intent, String> {

        @Override
        protected String doInBackground(String... arg0) {
            System.out.println("开始准备数据上传.....");
			 /*建立HTTPost对象*/
            HttpPost httpRequest = new HttpPost(URL_RESOURCE_POST);
            List <NameValuePair> params = new ArrayList<>();
            //用户id
            params.add(new BasicNameValuePair("pic", pic));
            params.add(new BasicNameValuePair("length", length));
            params.add(new BasicNameValuePair("width", broad));
            params.add(new BasicNameValuePair("address", URLEncoder.encode(uaddress)));
            params.add(new BasicNameValuePair("contactName",URLEncoder.encode(contactse)));
            params.add(new BasicNameValuePair("mobile", pmob));
            params.add(new BasicNameValuePair("userId", "1"));
            params.add(new BasicNameValuePair("remark", URLEncoder.encode(explains)));
            try{
	          /* 添加请求参数到请求对象*/
                httpRequest.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
	          /*发送请求并等待响应
	           * */
                HttpResponse httpResponse = new DefaultHttpClient().execute(httpRequest);
	          /*若状态码为200 ok*/
                if(httpResponse.getStatusLine().getStatusCode() == 200){
	            /*读返回数据*/
                    String strResult = EntityUtils.toString(httpResponse.getEntity());
                    JSONObject json2 =new JSONObject(strResult);
                    msg=json2.getString("message");
                }else  {  //请求错误后返回的其他数据
                    String errors="Error Response: "+httpResponse.getStatusLine().toString();
                    System.out.println(errors);
                }
            }
            catch (ClientProtocolException e) {
                System.out.println(e.getMessage().toString());
                e.printStackTrace();
            }
            catch (IOException e)     {
                System.out.println(e.getMessage().toString());
                e.printStackTrace();
            }
            catch (Exception e)   {
                System.out.println(e.getMessage().toString());
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            editor.clear();
            editor.commit();
            Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();

            finish();
        }



    }


    public void quxiaos(View v){
        editor.clear();
        editor.commit();
        ResourcePostActivity.this.finish();

    }
    class picThread extends Thread{

        @Override
        public void run() {

        }
    }



    public void ijng(View v){

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
            Log.d("hck", "定位定位");
            System.out.println("开始定位...");
            if (location == null || mMapView == null)
                return;
            //设置定位参数
            MyLocationData locData = new MyLocationData.Builder()
                    .accuracy(location.getRadius()).direction(100)
                    .latitude(location.getLatitude())
                    .longitude(location.getLongitude()).build();
            mBaiduMap.setMyLocationData(locData);
            if (isFirstLoc) {
                Log.d("hck", "定位定位成功");
                isFirstLoc = false;
                LatLng ll = new LatLng(location.getLatitude(),
                        location.getLongitude());
                MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(ll);
                mBaiduMap.animateMapStatus(u);
                adresss = location.getAddrStr();
                dLati=location.getLatitude();
                dLong=location.getLongitude();
                upaddress.setText(adresss);
            }
        }


    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == 0) {
            boolean ok=CommonUtil.fileIsExists(path);
            if(ok){
                newpath=path;
                System.out.println("path"+path);
                System.out.println("newpath"+newpath);
                //压缩图片
                CommonUtil.dealImage(newpath, newpath);
                //删除原图192.168.0.152
                File file = new File(newpath);
                System.out.println(newpath);
                Uri uri = Uri.fromFile(file);
                upimage.setImageURI(uri);
            }else{

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
