package net.pingfang.signalr.chat.activity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import net.pingfang.signalr.chat.R;
import net.pingfang.signalr.chat.constant.app.AppConstants;
import net.pingfang.signalr.chat.util.CommonUtil;
import net.pingfang.signalr.chat.util.GlobalApplication;
import net.pingfang.signalr.chat.util.SharedPreferencesHelper;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class ChangePwdActivity extends AppCompatActivity implements View.OnClickListener{

    public static final String URL_PWD_UPDATE = GlobalApplication.URL_WEB_API_HOST + "/api/WebAPI/User/UpPassword";

    TextView btn_activity_back;

    private EditText oldpassword,newpassword,newpasswords;
    private String oldpwd,npwd,npwds;
    private String message;

    SharedPreferencesHelper helper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_pwd);

        helper = SharedPreferencesHelper.newInstance(getApplicationContext());

        initView();
    }

    private void initView() {
        btn_activity_back = (TextView) findViewById(R.id.btn_activity_back);
        btn_activity_back.setOnClickListener(this);

        oldpassword=(EditText) this.findViewById(R.id.oldpassword);
        newpassword=(EditText) this.findViewById(R.id.newpassword);
        newpasswords=(EditText) this.findViewById(R.id.newpasswords);
    }

    public void newpwd(View v){
        oldpwd=oldpassword.getText().toString().trim();
        npwd=newpassword.getText().toString().trim();
        npwds=newpasswords.getText().toString().trim();
        if(oldpwd.length()<=0){
            Toast.makeText(getApplicationContext(), "请输入旧密码", Toast.LENGTH_SHORT).show();
            return;
        }else if(npwd.length()<=0){
            Toast.makeText(getApplicationContext(), "请输入新密码", Toast.LENGTH_SHORT).show();
            return;
        }else if(npwds.length()<=0){
            Toast.makeText(getApplicationContext(), "请再次输入新密码", Toast.LENGTH_SHORT).show();
            return;
        }else if(!npwd.equals(npwds)){
            Toast.makeText(getApplicationContext(), "两次输入的新密码不一致", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean net= CommonUtil.isConnected(this);
        if(net){
            AsynTaskpwd asyn=new AsynTaskpwd();
            asyn.execute(oldpwd,npwd);
        }else{
            Toast.makeText(getApplicationContext(), "当前没有网络请打开网络", Toast.LENGTH_SHORT).show();
        }

    }

    class AsynTaskpwd extends AsyncTask<String, Integer, String> {

        @Override
        protected String doInBackground(String... params) {
            String oldPwd = params[0];
            String newPwd = params[1];

            //得到HttpClient对象
            //DefaultHttpClient是默认的一个Http客户端,用他可以创建一个Http连接
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(URL_PWD_UPDATE);
            stringBuilder.append("?id=");
            stringBuilder.append(helper.getStringValue(AppConstants.KEY_SYS_CURRENT_UID));
            stringBuilder.append("&oldpassword=");
            stringBuilder.append(oldPwd);
            stringBuilder.append("&newpassword=");
            stringBuilder.append(newPwd);

            String urls= stringBuilder.toString();
            System.out.println("urls:"+urls);
            try {
                HttpClient httpClient = new DefaultHttpClient();
                //HttpGet连接对象
                HttpGet httpRequest = new HttpGet(urls);
                //请求HttpClient,取得HttpResponse
                HttpResponse httpResponse;
                httpResponse = httpClient.execute(httpRequest);
                if(httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK){ //请求成功
                    //取得返回的字符串
                    /**
                     * 13530745127
                     */
                    String result = EntityUtils.toString(httpResponse.getEntity());
                    System.out.println("result:"+result);
                    try {
                        JSONObject json=new JSONObject(result);
                        message=json.getString("message");
                    } catch (JSONException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                }else{
                    System.out.println("Apache get方式结果：出现错误"+httpResponse.getStatusLine().getStatusCode());
                }
            } catch (ClientProtocolException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            if(message.equals("修改密码成功")){
                finish();
            }else{
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
