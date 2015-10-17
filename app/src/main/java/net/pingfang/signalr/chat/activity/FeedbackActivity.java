package net.pingfang.signalr.chat.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

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

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class FeedbackActivity extends AppCompatActivity implements View.OnClickListener{

    public static final String URL_FEED_BACK = "http://192.168.0.158/api/WebAPI/ProblemFeedback/Ques";

    TextView btn_activity_back;

    private EditText feedback_text;
    private Spinner spinner;
    private ArrayAdapter<String> adapter;
    private List<String> list = new ArrayList<String>();
    private String fbtype,fbcontent,pic="null",fbid="1",messages;
    boolean net;
    SharedPreferences mySharedPreferences;
    SharedPreferences.Editor editor ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);

        mySharedPreferences= getSharedPreferences("feed", Activity.MODE_PRIVATE);
        editor = mySharedPreferences.edit();

        initView();
    }

    private void initView() {
        btn_activity_back = (TextView) findViewById(R.id.btn_activity_back);
        btn_activity_back.setOnClickListener(this);

        real();
        feedback_text=(EditText) this.findViewById(R.id.feedback_text);
        spinner=(Spinner) this.findViewById(R.id.spinner1);
        list.add("bug反馈");
        list.add("举报");
        list.add("合作");
        list.add("其他");
        adapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, list);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        if(fbcontent.length()>0){
            feedback_text.setText(fbcontent);
        }
        spinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener(){
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                // 启动时会默认选择第一项    adapter.getItem(arg2)
                System.out.println("您选择的是："+ adapter.getItem(arg2));
                fbtype=adapter.getItem(arg2);
                arg0.setVisibility(View.VISIBLE);
            }
            public void onNothingSelected(AdapterView<?> arg0) {
                arg0.setVisibility(View.VISIBLE);
            }
        });
    }

    public void real(){
        fbcontent =mySharedPreferences.getString("fbcontent", "");
    }

    public void save(){
        editor.putString("fbcontent", fbcontent);
        editor.commit();
        Toast.makeText(getApplicationContext(), "当前没有网络，数据已经保存到了草稿", Toast.LENGTH_SHORT).show();
    }

    public void feedupdate(View view){
        fbcontent=feedback_text.getText().toString().trim();
        if(fbcontent.equals("")){
            Toast.makeText(getApplicationContext(), "请输入反馈内容！", Toast.LENGTH_SHORT).show();
            return;
        }

        net= CommonUtil.isConnected(this);
        if(net){
            MyTasks mtsk=new MyTasks();
            mtsk.execute();
        }else{
            save();
        }

    }

    class MyTasks extends AsyncTask<String, Integer, String>{

        @Override
        protected String doInBackground(String... arg0) {

            int id = 0;
            if(fbtype.equals("bug反馈")){
                id=0;
            }else if(fbtype.equals("举报")){
                id=1;
            }else if(fbtype.equals("合作")){
                id=2;
            }else if(fbtype.equals("其他")){
                id=3;
            }

            System.out.println("开始准备数据上传.....");


            System.out.println("uriAPI" + URL_FEED_BACK);
			 /*建立HTTPost对象*/
            HttpPost httpRequest = new HttpPost(URL_FEED_BACK);
			 /*
	         * NameValuePair实现请求参数的封装
	         * yingerjian3
	        */

            List <NameValuePair> params = new ArrayList<NameValuePair>();
            System.out.println("问题反馈的内容是："+id);
            params.add(new BasicNameValuePair("type", id+""));
            params.add(new BasicNameValuePair("pic", ""));
            params.add(new BasicNameValuePair("content", URLEncoder.encode(fbcontent)));
            params.add(new BasicNameValuePair("userId", fbid));

            try
            {
	          /* 添加请求参数到请求对象*/
                httpRequest.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
	          /*发送请求并等待响应
	           * */
                HttpResponse httpResponse = new DefaultHttpClient().execute(httpRequest);
	          /*若状态码为200 ok*/
                System.out.println("获得的状态吗是："+httpResponse.getStatusLine().getStatusCode());
                if(httpResponse.getStatusLine().getStatusCode() == 200)
                {
	            /*读返回数据*/
                    String strResult = EntityUtils.toString(httpResponse.getEntity());
                    System.out.println("获得的数据是："+strResult);
                    JSONObject json=new JSONObject(strResult);
                    messages=json.getString("message");
                }
                else
                {  //请求错误后返回的其他数据
                    String errors="Error Response: "+httpResponse.getStatusLine().toString();
                    System.out.println(errors);
                }
            }
            catch (ClientProtocolException e)
            {
                System.out.println(e.getMessage().toString());
                e.printStackTrace();
            }
            catch (IOException e)
            {
                System.out.println(e.getMessage().toString());
                e.printStackTrace();
            }
            catch (Exception e)
            {
                System.out.println(e.getMessage().toString());
                e.printStackTrace();
            }



            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            editor.clear();
            editor.commit();
            Toast.makeText(getApplicationContext(), messages+".谢谢你的反馈，我们会继续努力", Toast.LENGTH_SHORT).show();
            finish();
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
