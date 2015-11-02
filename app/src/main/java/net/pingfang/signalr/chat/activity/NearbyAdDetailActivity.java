package net.pingfang.signalr.chat.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import net.pingfang.signalr.chat.R;
import net.pingfang.signalr.chat.net.OkHttpCommonUtil;

public class NearbyAdDetailActivity extends AppCompatActivity implements View.OnClickListener{

    TextView btn_activity_back;

    private ImageView iv_ad_image;
    private TextView tv_ad_content;

    private String advt;
    private String url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nearby_ad_detail);

        Bundle bundle = getIntent().getExtras();
        advt = bundle.getString("text");
        url = bundle.getString("url");

        initView();

        initData();

    }

    private void initView() {
        btn_activity_back = (TextView) findViewById(R.id.btn_activity_back);
        btn_activity_back.setOnClickListener(this);

        iv_ad_image = (ImageView) findViewById(R.id.iv_ad_image);
        tv_ad_content = (TextView) findViewById(R.id.tv_ad_content);
    }

    private void initData() {
        OkHttpCommonUtil okHttp = OkHttpCommonUtil.newInstance(getApplicationContext());
        okHttp.display(iv_ad_image, url, R.drawable.ic_empty);
        tv_ad_content.setText(advt);
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
