package net.pingfang.signalr.chat.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import net.pingfang.signalr.chat.R;
import net.pingfang.signalr.chat.model.ResourceInfo;
import net.pingfang.signalr.chat.net.OkHttpCommonUtil;

public class ResourceDetailActivity extends AppCompatActivity implements View.OnClickListener{

    ResourceInfo resourceInfo;
    private TextView btn_activity_back;
    private TextView tv_menu_drop_down;
    private ImageView iv_resource_detail_profile;
    private ImageView iv_resource_detail_profile_2;
    private ImageView iv_resource_detail_profile_3;
    private ImageView iv_resource_detail_profile_4;
    private TextView tv_resource_detail_width;
    private TextView tv_resource_detail_height;
    private TextView tv_resource_detail_contact;
    private TextView tv_resource_detail_contact_info;
    private TextView tv_resource_detail_address;
    private TextView tv_resource_detail_update_time;
    private TextView tv_resource_detail_status;
    private TextView tv_resource_detail_remark;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resource_detail);

        initView();

        Intent intent = getIntent();
        resourceInfo = intent.getParcelableExtra("resource");

        initData();
    }

    private void initView() {
        btn_activity_back = (TextView) findViewById(R.id.btn_activity_back);
        btn_activity_back.setOnClickListener(this);

        tv_menu_drop_down = (TextView) findViewById(R.id.tv_menu_drop_down);
        tv_menu_drop_down.setVisibility(View.GONE);
        tv_menu_drop_down.setOnClickListener(this);

        iv_resource_detail_profile = (ImageView) findViewById(R.id.iv_resource_detail_profile);
        iv_resource_detail_profile_2 = (ImageView) findViewById(R.id.iv_resource_detail_profile_2);
        iv_resource_detail_profile_3 = (ImageView) findViewById(R.id.iv_resource_detail_profile_3);
        iv_resource_detail_profile_4 = (ImageView) findViewById(R.id.iv_resource_detail_profile_4);
        tv_resource_detail_width = (TextView) findViewById(R.id.tv_resource_detail_width);
        tv_resource_detail_height = (TextView) findViewById(R.id.tv_resource_detail_height);
        tv_resource_detail_contact = (TextView) findViewById(R.id.tv_resource_detail_contact);
        tv_resource_detail_contact_info = (TextView) findViewById(R.id.tv_resource_detail_contact_info);
        tv_resource_detail_address = (TextView) findViewById(R.id.tv_resource_detail_address);
        tv_resource_detail_update_time = (TextView) findViewById(R.id.tv_resource_detail_update_time);
        tv_resource_detail_status = (TextView) findViewById(R.id.tv_resource_detail_status);
        tv_resource_detail_remark = (TextView) findViewById(R.id.tv_resource_detail_remark);
    }

    private void initData() {
        if(resourceInfo != null) {
            OkHttpCommonUtil okHttp = OkHttpCommonUtil.newInstance(getApplicationContext());
            String url = resourceInfo.getUrl();
            if(!TextUtils.isEmpty(url)) {
//                url = GlobalApplication.RESOURCE_PIC_URL_PREFIX + url;
//                okHttp.display(iv_resource_detail_profile,url,R.mipmap.ic_launcher);
                String[] urls = url.split(";");
                if(urls != null && urls.length > 0) {
                    okHttp.display(iv_resource_detail_profile, urls[0], R.drawable.ic_empty);
                    if(urls.length > 1) {
                        okHttp.display(iv_resource_detail_profile_2, urls[1], R.drawable.ic_empty);
                        if(urls.length > 2) {
                            okHttp.display(iv_resource_detail_profile_3, urls[2], R.drawable.ic_empty);
                            if (urls.length > 3) {
                                okHttp.display(iv_resource_detail_profile_4, urls[3], R.drawable.ic_empty);
                            }
                        }

                    }
                }
            }
            tv_resource_detail_width.setText(getString(R.string.tv_resource_detail_width,resourceInfo.getWidth()));
            tv_resource_detail_height.setText(getString(R.string.tv_resource_detail_height,resourceInfo.getHeight()));
            tv_resource_detail_contact.setText(getString(R.string.tv_resource_detail_contact,resourceInfo.getContact()));
            tv_resource_detail_contact_info.setText(getString(R.string.tv_resource_detail_contact_info,resourceInfo.getContactInfo()));
            tv_resource_detail_address.setText(getString(R.string.tv_resource_detail_address,resourceInfo.getAddress()));
            tv_resource_detail_update_time.setText(getString(R.string.tv_resource_detail_update_time,resourceInfo.getPostTime()));
            tv_resource_detail_status.setText(getString(R.string.tv_resource_detail_status,resourceInfo.getResStatus()));
            tv_resource_detail_remark.setText(getString(R.string.tv_resource_detail_remark,resourceInfo.getRemark()));
        }
    }

    @Override
    public void onClick(View view) {
        int viewId = view.getId();
        switch(viewId) {
            case R.id.btn_activity_back:
                navigateUp();
                break;
            case R.id.tv_menu_drop_down:
//                popupMenu(view);
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
