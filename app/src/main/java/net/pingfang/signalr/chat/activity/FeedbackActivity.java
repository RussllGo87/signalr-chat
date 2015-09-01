package net.pingfang.signalr.chat.activity;

import android.content.Intent;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.squareup.okhttp.Request;

import net.pingfang.signalr.chat.R;
import net.pingfang.signalr.chat.net.OkHttpCommonUtil;
import net.pingfang.signalr.chat.util.GlobalApplication;

import java.util.HashMap;

public class FeedbackActivity extends AppCompatActivity implements View.OnClickListener{

    TextView btn_activity_back;
    Button btn_feedback_submit;

    EditText et_keyword;
    TextView tv_result;

    OkHttpCommonUtil okHttpCommonUtil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);

        okHttpCommonUtil = OkHttpCommonUtil.newInstance(getApplicationContext());

        initView();
    }

    private void initView() {
        btn_activity_back = (TextView) findViewById(R.id.btn_activity_back);
        btn_activity_back.setOnClickListener(this);

        btn_feedback_submit = (Button) findViewById(R.id.btn_feedback_submit);
        btn_feedback_submit.setOnClickListener(this);

        et_keyword = (EditText) findViewById(R.id.et_keyword);
        tv_result = (TextView) findViewById(R.id.tv_result);
    }

    @Override
    public void onClick(View view) {
        int viewId = view.getId();
        switch(viewId) {
            case R.id.btn_activity_back:
                navigateUp();
                break;
            case R.id.btn_feedback_submit:
                submitFeedback();
                break;
        }
    }

    private void submitFeedback() {
        String keyword =  et_keyword.getText().toString().trim();
        if(TextUtils.isEmpty(keyword)) {
            Request request =  OkHttpCommonUtil.buildGetReq("http://apis.map.qq.com/ws/district/v1/list",
                    new OkHttpCommonUtil.Param[]{new OkHttpCommonUtil.Param("key",GlobalApplication.T_MAP_KEY)});
            okHttpCommonUtil.enqueue(request);
        } else {
            HashMap<String,String> map = new HashMap<>();
            map.put("key",GlobalApplication.T_MAP_KEY);
            map.put("keyword",keyword);
            map.put("output","json");
            Request request =  OkHttpCommonUtil.buildGetReq("http://apis.map.qq.com/ws/district/v1/search",
                    new OkHttpCommonUtil.Param[]{new OkHttpCommonUtil.Param("key",GlobalApplication.T_MAP_KEY),
                    new OkHttpCommonUtil.Param("keyword",keyword),
                    new OkHttpCommonUtil.Param("output","json")});
            okHttpCommonUtil.enqueue(request);
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
