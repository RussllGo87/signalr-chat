package net.pingfang.signalr.chat.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.okhttp.Response;

import net.pingfang.signalr.chat.R;
import net.pingfang.signalr.chat.adapter.ResourceListAdapter;
import net.pingfang.signalr.chat.constant.app.AppConstants;
import net.pingfang.signalr.chat.model.ResourceInfo;
import net.pingfang.signalr.chat.net.HttpBaseCallback;
import net.pingfang.signalr.chat.net.OkHttpCommonUtil;
import net.pingfang.signalr.chat.util.GlobalApplication;
import net.pingfang.signalr.chat.util.SharedPreferencesHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class ResourceListActivity extends AppCompatActivity implements View.OnClickListener{

    public static final String TAG = ResourceListActivity.class.getSimpleName();

    public static final String URL_LIST_POST_RESOURCE =
            GlobalApplication.URL_WEB_API_HOST + "/api/WebAPI/ResourceWall/UploadResourceWallNote";
    public static final String KEY_LIST_RESOURCE_UID = "userId";
    SharedPreferencesHelper helper;
    private TextView btn_activity_back;
    private TextView tv_menu_drop_down;
    private ListView list_resource_upload;
    private ResourceListAdapter listAdapter;
    private ImageView iv_resource_empty_place_holder;
    private Handler mHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resource_list);

        helper = SharedPreferencesHelper.newInstance(getApplicationContext());
        initView();
        initData();
    }

    private void initView() {
        btn_activity_back = (TextView) findViewById(R.id.btn_activity_back);
        btn_activity_back.setOnClickListener(this);

        tv_menu_drop_down = (TextView) findViewById(R.id.tv_menu_drop_down);
        tv_menu_drop_down.setVisibility(View.GONE);
        tv_menu_drop_down.setOnClickListener(this);

        list_resource_upload = (ListView) findViewById(R.id.list_resource_upload);
        listAdapter = new ResourceListAdapter(getApplicationContext());
        list_resource_upload.setAdapter(listAdapter);

        list_resource_upload.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ResourceInfo resourceInfo = (ResourceInfo) view.getTag();

                Intent intent = new Intent();
                intent.setClass(getApplicationContext(), ResourceDetailActivity.class);
                intent.putExtra("resource", resourceInfo);
                startActivity(intent);
            }
        });

        iv_resource_empty_place_holder = (ImageView) findViewById(R.id.iv_resource_empty_place_holder);
        iv_resource_empty_place_holder.setImageResource(R.drawable.drawable_iv_resource_empty);
        iv_resource_empty_place_holder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), ResourcePostActivity.class);
                startActivity(intent);
            }
        });
    }

    private void initData() {
        OkHttpCommonUtil okHttp = OkHttpCommonUtil.newInstance(getApplicationContext());
        okHttp.getRequest(URL_LIST_POST_RESOURCE,
                new OkHttpCommonUtil.Param[]{
                        new OkHttpCommonUtil.Param(KEY_LIST_RESOURCE_UID,
                                helper.getStringValue(AppConstants.KEY_SYS_CURRENT_UID))},
                new HttpBaseCallback() {
                    @Override
                    public void onResponse(Response response) throws IOException {
                        super.onResponse(response);
                        String result = response.body().string();
                        Log.d(TAG, "URL_LIST_AD return " + result);
                        JSONObject jsonObject = null;
                        try {
                            jsonObject = new JSONObject(result);
                            int status = jsonObject.getInt("status");
                            String message = jsonObject.getString("message");
                            if (status == 1) {
                                JSONArray array = jsonObject.getJSONArray("list");
                                if (array != null && array.length() > 0) {
                                    for (int i = 0; i < array.length(); i++) {
                                        JSONObject js = array.getJSONObject(i);
                                        final String width = js.getString("Length");
                                        final String height = js.getString("Width");
                                        final String resStatus = js.getString("IsProcessed");
                                        final String remark = js.getString("Remark");
                                        final String url = js.getString("ResourcePicture");
                                        final String address = js.getString("Address");
                                        final String postTime = js.getString("PublishTime");
                                        final String contact = js.getString("ContactPerson");
                                        final String contactInfo = js.getString("ContactInformation");
                                        mHandler.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                list_resource_upload.setVisibility(View.VISIBLE);

                                                ResourceInfo resourceInfo = new ResourceInfo();
                                                resourceInfo.setWidth(width);
                                                resourceInfo.setHeight(height);
                                                resourceInfo.setResStatus(resStatus);
                                                resourceInfo.setRemark(remark);
                                                resourceInfo.setUrl(url);
                                                resourceInfo.setPostTime(postTime);
                                                resourceInfo.setAddress(address);
                                                resourceInfo.setContact(contact);
                                                resourceInfo.setContactInfo(contactInfo);
                                                listAdapter.add(resourceInfo);
                                            }
                                        });
                                    }
                                } else {
                                    mHandler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(getApplicationContext(), R.string.toast_resource_error_no_record, Toast.LENGTH_LONG).show();
                                            list_resource_upload.setVisibility(View.GONE);
                                        }
                                    });
                                }
                            } else {
                                mHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(getApplicationContext(), R.string.toast_resource_error_no_record, Toast.LENGTH_LONG).show();
                                        list_resource_upload.setVisibility(View.GONE);
                                    }
                                });
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();

                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getApplicationContext(), R.string.toast_resource_error_in_load, Toast.LENGTH_LONG).show();
                                    list_resource_upload.setVisibility(View.GONE);
                                }
                            });
                        }

                    }
                });
    }


    @Override
    public void onClick(View view) {
        int viewId = view.getId();
        switch(viewId) {
            case R.id.btn_activity_back:
                navigateUp();
                break;
            case R.id.tv_menu_drop_down:
                popupMenu(view);
                break;
        }
    }

    private void popupMenu(View view) {
        ContextThemeWrapper wrapper = new ContextThemeWrapper(getApplicationContext(), R.style.AppTheme);
        PopupMenu popup = new PopupMenu(wrapper, view);
        final MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.menu_resource_list, popup.getMenu());
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_range_all:
                        tv_menu_drop_down.setText(R.string.action_range_all);
                        break;
                    case R.id.action_range_1:
                        tv_menu_drop_down.setText(R.string.action_range_1);
                        break;
                    case R.id.action_range_2:
                        tv_menu_drop_down.setText(R.string.action_range_2);
                        break;
                }
                return true;
            }
        });
        popup.show();
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
