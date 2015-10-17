package net.pingfang.signalr.chat.activity;

import android.app.Activity;
import android.app.Dialog;
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
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import net.pingfang.signalr.chat.R;
import net.pingfang.signalr.chat.constant.app.AppConstants;
import net.pingfang.signalr.chat.util.CommonUtil;
import net.pingfang.signalr.chat.util.SharedPreferencesHelper;
import net.pingfang.signalr.chat.view.CircularImage;

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

public class AccountInfoUpdateActivity extends AppCompatActivity implements View.OnClickListener{

    public static final String URL_ACCOUNT_INFO_UPDATE = "http://192.168.0.158/api/WebAPI/User/PerfectInfo";

    TextView btn_activity_back;

    private EditText regusername,regalName,regaddress,regqq;
    private String username,realname,address,qq, message;
    private CircularImage headpic;
    Dialog dialog;
    /**拍照所需参数*/
    private String imagename;
    private  String FILE_PATH = "";//SD卡的路径是：/storage/sdcard0
    String path = "",newpath="",pic;

    SharedPreferences mySharedPreferences;
    SharedPreferences.Editor editor ;
    boolean net;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_acount_info_update);

        mySharedPreferences= getSharedPreferences("reginfo", Activity.MODE_PRIVATE);
        editor = mySharedPreferences.edit();
        String pathsd= CommonUtil.getSDPath();
        FILE_PATH=pathsd+"/photosed/";
        System.out.println("图片存储文件夹位置为："+FILE_PATH);
        CommonUtil.makeRootDirectory(FILE_PATH);
        real();

        initView();
    }

    private void initView() {
        btn_activity_back = (TextView) findViewById(R.id.btn_activity_back);
        btn_activity_back.setOnClickListener(this);

        regusername=(EditText) this.findViewById(R.id.regusername);
        regalName=(EditText) this.findViewById(R.id.regalName);
        regaddress=(EditText) this.findViewById(R.id.regaddress);
        headpic=(CircularImage) this.findViewById(R.id.headpic);
        regqq=(EditText) this.findViewById(R.id.regqq);
        if(newpath.length()>1){

            regusername.setText(username);
            regalName.setText(realname);
            regaddress.setText(address);
            regqq.setText(qq);
            headpic.setImageBitmap(CommonUtil.getBitmapInLocal(newpath));
        }

        headpic.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                showDialog();
            }
        });
    }

    public void regok(View v){
        username=regusername.getText().toString().trim();
        realname=regalName.getText().toString().trim();
        address=regaddress.getText().toString().trim();
        qq=regqq.getText().toString().trim();
        if(username.length()<=0){
            Toast.makeText(getApplicationContext(), "请输入你的用户名!", Toast.LENGTH_SHORT).show();
            return;
        }else if(realname.length()<=0){
            Toast.makeText(getApplicationContext(), "请输入你的真实姓名！", Toast.LENGTH_SHORT).show();
            return;
        }else if(address.length()<=0){
            Toast.makeText(getApplicationContext(), "请输入你的居住地址！", Toast.LENGTH_SHORT).show();
            return;
        }else if(newpath.length()<=0){
            Toast.makeText(getApplicationContext(), "请选择或拍摄照片！", Toast.LENGTH_SHORT).show();
            return;
        }
        path="";
        //图片压缩
        SimpleDateFormat sDateFormat = new SimpleDateFormat(
                "yyyy-MM-dd hh:mm:ss");
        String date = sDateFormat.format(new java.util.Date());
        imagename=date + ".jpg";
        path = FILE_PATH + imagename;
        CommonUtil.dealImage(newpath, path);
        System.out.println("传入图片的路径："+path);
        Bitmap bt=CommonUtil.getBitmapInLocal(path);
        pic=CommonUtil.bitmapToBase64(bt);
        net=CommonUtil.isConnected(this);
        if(net){
            AsynTasks asyn=new AsynTasks();
            asyn.execute();
        }else{
            save();
        }

    }

    //在没有网络的状态下把数据保存到SharedPreferences
    public void save(){
        //用putString的方法保存数据
        editor.putString("pic",newpath );
        editor.putString("username", username);
        editor.putString("realname", realname);
        editor.putString("qq", qq);
        editor.putString("address", address);
        //提交当前数据
        editor.commit();
        //使用toast信息提示框提示成功写入数据
        Toast.makeText(this, "当前没有网络数据已经保存到草稿，等到有网络是在发送" ,
                Toast.LENGTH_LONG).show();

    }
    //读取SharedPreferences中的数据读出来
    public void real(){

        newpath =mySharedPreferences.getString("pic", "");
        username=mySharedPreferences.getString("username", "");
        realname=mySharedPreferences.getString("realname", "");
        qq=mySharedPreferences.getString("qq", "");
        address=mySharedPreferences.getString("address", "");

    }


    public void showinfo(View v){
        editor.clear();
        editor.commit();
        finish();

    }

    private void showDialog() {
        View view = getLayoutInflater().inflate(R.layout.photo_choose_dialog,
                null);
        dialog = new Dialog(this, R.style.transparentFrameWindowStyle);
        dialog.setContentView(view, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,
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
                break;
            case R.id.openPhones:
                //打开Android 系统相册，选择图片之后在onActivityResult 里面返回选中图片uri地址
                Intent intent = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,"image/*");
                startActivityForResult(intent, 112);
                break;
            case R.id.cancel:
                dialog.cancel();
                break;
            default:
                break;
        }
    }


    // 打开照相机
    private void openCamera() {
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        System.out.println("requestCode:"+requestCode);
        if (requestCode == 0) {
            boolean ok=CommonUtil.fileIsExists(path);
            if(ok){
                newpath=path;
            }
            System.out.println("path"+path);
            System.out.println("newpath"+newpath);
            //压缩图片
            CommonUtil.dealImage(newpath, newpath);
            //删除原图192.168.0.152
            //new File(defaultPhotoAddress).delete();
            File file = new File(newpath);
            System.out.println(newpath);
            Uri uri = Uri.fromFile(file);
            headpic.setImageURI(uri);
        } else if (requestCode == 1) {
            headpic.setImageURI(data.getData());
        }else{
            headpic.setImageURI(data.getData());
            //压缩图片
            newpath=CommonUtil.getRealFilePath(this, data.getData());
            System.out.println("从图库中选择图片的地址："+newpath);
            CommonUtil.dealImage(newpath, newpath);
        }

    }

    //数据上传保存
    class AsynTasks extends AsyncTask<String, Integer, String> {

        @Override
        protected String doInBackground(String... arg0) {

            System.out.println("开始准备数据上传.....");
            /*建立HTTPost对象*/
            HttpPost httpRequest = new HttpPost(URL_ACCOUNT_INFO_UPDATE);
				 /*
		         * NameValuePair实现请求参数的封装
		         * yingerjian3
		         */
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            //用户id
            SharedPreferencesHelper helper = SharedPreferencesHelper.newInstance(getApplicationContext());
            params.add(new BasicNameValuePair("id", helper.getStringValue(AppConstants.KEY_SYS_CURRENT_UID)));
            params.add(new BasicNameValuePair("userName", URLEncoder.encode(username)));
            // params.add(new BasicNameValuePair("nickName",URLEncoder.encode("MYG")));
            params.add(new BasicNameValuePair("realName",URLEncoder.encode(realname)));//URLEncoder.encode("白祖念")
            // params.add(new BasicNameValuePair("mobile", "15817419383"));
            params.add(new BasicNameValuePair("qq",qq));
            params.add(new BasicNameValuePair("pic", pic));
            params.add(new BasicNameValuePair("address", URLEncoder.encode(address)));
            try{
		          /* 添加请求参数到请求对象*/
                httpRequest.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
		          /*发送请求并等待响应
		           *
		           * */
                HttpResponse httpResponse = new DefaultHttpClient().execute(httpRequest);
		          /*若状态码为200 ok*/
                System.out.println("获得的状态吗是："+httpResponse.getStatusLine().getStatusCode());
                if(httpResponse.getStatusLine().getStatusCode() == 200){
		            /*读返回数据*/
                    String strResult = EntityUtils.toString(httpResponse.getEntity());
                    System.out.println(strResult);
                    JSONObject json=new JSONObject(strResult);
                    message = json.getString("message");
                }else {  //请求错误后返回的其他数据
                    String errors="Error Response: "+httpResponse.getStatusLine().toString();
                    System.out.println(errors);
                }
            }
            catch (ClientProtocolException e) {
                System.out.println(e.getMessage().toString());
                e.printStackTrace();
            }
            catch (IOException e){
                System.out.println(e.getMessage().toString());
                e.printStackTrace();
            }
            catch (Exception e){
                System.out.println(e.getMessage().toString());
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            //Toast.makeText(getApplicationContext(),  message, Toast.LENGTH_SHORT).show();
            if (message.equals("更新成功")) {
                editor.clear();
                editor.commit();
                System.out.println(message);
                finish();
            } else {
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onClick(View view) {
        int viewId = view.getId();
        switch (viewId) {
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
