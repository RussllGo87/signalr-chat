package net.pingfang.signalr.chat.activity;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import net.pingfang.signalr.chat.R;
import net.pingfang.signalr.chat.adapter.ResourceListCursorAdapter;
import net.pingfang.signalr.chat.constant.app.AppConstants;
import net.pingfang.signalr.chat.database.AdResource;
import net.pingfang.signalr.chat.database.AppContract;
import net.pingfang.signalr.chat.util.SharedPreferencesHelper;

public class ResourceUploadListActivity extends AppCompatActivity implements View.OnClickListener, LoaderManager.LoaderCallbacks<Cursor>{

    private static final int LOADER_ID = 0x06;

    TextView btn_activity_back;
    ListView lv_ad_resource_upload;

    ResourceListCursorAdapter adResourceListCursorAdapter;

    SharedPreferencesHelper sharedPreferencesHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resource_upload_list);

        sharedPreferencesHelper  = SharedPreferencesHelper.newInstance(getApplicationContext());
        initView();
        initAdapter();
    }

    private void initView() {
        btn_activity_back = (TextView) findViewById(R.id.btn_activity_back);
        btn_activity_back.setOnClickListener(this);

        lv_ad_resource_upload = (ListView) findViewById(R.id.lv_ad_resource_upload);
    }

    private void initAdapter() {
        adResourceListCursorAdapter = new ResourceListCursorAdapter(getApplicationContext(), null, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
        lv_ad_resource_upload.setAdapter(adResourceListCursorAdapter);
        lv_ad_resource_upload.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                AdResource resource = (AdResource) view.getTag();

                Intent intent = new Intent();
                intent.setClass(getApplicationContext(), ResourceUploadDetailActivity.class);
                intent.putExtra("resource", resource);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        String uid = sharedPreferencesHelper.getStringValue(AppConstants.KEY_SYS_CURRENT_UID);
        Bundle args = new Bundle();
        args.putString(AppConstants.KEY_SYS_CURRENT_UID, uid);

        getSupportLoaderManager().initLoader(LOADER_ID, args, this);
    }

    @Override
    public void onStop() {
        super.onStop();

        getSupportLoaderManager().destroyLoader(LOADER_ID);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String uid = args.getString(AppConstants.KEY_SYS_CURRENT_UID);

        Uri baseUri = AppContract.AdResourceEntry.CONTENT_URI;
        String selection = AppContract.AdResourceEntry.COLUMN_NAME_RESOURCE_UID + " = ? " +
                "AND " +
                AppContract.AdResourceEntry.COLUMN_NAME_RESOURCE_STATUS + " = ? ";
        String[] selectionArgs = new String[]{uid, String.valueOf(AdResource.RESOURCE_STATUS_UPLOAD_ERROR)};
        String sortOrder = AppContract.AdResourceEntry.DEFAULT_SORT_ORDER;

        return new CursorLoader(getApplicationContext(), baseUri, null, selection, selectionArgs, sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        adResourceListCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adResourceListCursorAdapter.swapCursor(null);
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
        if (NavUtils.shouldUpRecreateTask(this, upIntent)) {
            TaskStackBuilder.create(this)
                    .addNextIntentWithParentStack(upIntent)
                    .startActivities();
        } else {
            onBackPressed();
        }
    }
}
