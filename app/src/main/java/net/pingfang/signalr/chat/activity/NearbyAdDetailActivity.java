package net.pingfang.signalr.chat.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import net.pingfang.signalr.chat.R;

public class NearbyAdDetailActivity extends AppCompatActivity implements View.OnClickListener{

    TextView btn_activity_back;

    private ImageView advimageshow;
    private TextView advtext;
    private Bitmap bitmap;
    private String advt,url;
    private ImageLoader loder;
    DisplayImageOptions optionss;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nearby_ad_detail);

        initView();

        loder = ImageLoader.getInstance();
        advimageshow=(ImageView) this.findViewById(R.id.advimageshow);
        advtext=(TextView) this.findViewById(R.id.advtext);
        Bundle bundle=this.getIntent().getExtras();
        advt=bundle.getString("text");
        url=bundle.getString("url");
        System.out.println("advt"+advt+":url:"+url);
        //显示图片
        loder.displayImage(url, advimageshow, optionss);
        advtext.setText(advt);
    }

    private void initView() {
        btn_activity_back = (TextView) findViewById(R.id.btn_activity_back);
        btn_activity_back.setOnClickListener(this);

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
