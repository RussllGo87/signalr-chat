package net.pingfang.signalr.chat.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.okhttp.Response;

import net.pingfang.signalr.chat.R;
import net.pingfang.signalr.chat.constant.app.AppConstants;
import net.pingfang.signalr.chat.net.HttpBaseCallback;
import net.pingfang.signalr.chat.net.OkHttpCommonUtil;
import net.pingfang.signalr.chat.util.GlobalApplication;
import net.pingfang.signalr.chat.util.SharedPreferencesHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class FeedbackActivity extends AppCompatActivity implements View.OnClickListener{

    public static final String TAG = FeedbackActivity.class.getSimpleName();

    public static final String URL_FEED_BACK = GlobalApplication.URL_WEB_API_HOST + "/api/WebAPI/ProblemFeedback/Ques";
    public static final String KEY_URL_FEED_BACK_UID = "userId";
    public static final String KEY_URL_FEED_BACK_CONTENT = "content";
    public static final String KEY_URL_FEED_BACK_TYPE = "type";
    public static final String KEY_URL_FEED_BACK_PIC = "pic";

    TextView btn_activity_back;

    private EditText et_feedback;
    private Spinner sp_feedback_type;
    private Button btn_feedback_submit;
    private ArrayAdapter<String> adapter;

    SharedPreferencesHelper helper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);

        helper = SharedPreferencesHelper.newInstance(getApplicationContext());

        initView();
    }

    private void initView() {
        btn_activity_back = (TextView) findViewById(R.id.btn_activity_back);
        btn_activity_back.setOnClickListener(this);

        et_feedback = (EditText) findViewById(R.id.et_feedback);
        sp_feedback_type = (Spinner) findViewById(R.id.sp_feedback_type);
        btn_feedback_submit = (Button) findViewById(R.id.btn_feedback_submit);
        btn_feedback_submit.setOnClickListener(this);

        adapter = new ArrayAdapter<String>(getApplicationContext(),
                R.layout.simple_spinner_item,
                getResources().getStringArray(R.array.feedback_type));
        adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);

        sp_feedback_type.setAdapter(adapter);
        sp_feedback_type.setOnItemSelectedListener(new Spinner.OnItemSelectedListener(){
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

            }
            public void onNothingSelected(AdapterView<?> parent) {
                parent.setVisibility(View.VISIBLE);
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
            case R.id.btn_feedback_submit:
                submitFeedback();
                break;

        }
    }

    private void submitFeedback() {
        String feedbackContent = et_feedback.getText().toString().trim();
        if(!TextUtils.isEmpty(feedbackContent) && sp_feedback_type.getSelectedItemPosition() != AdapterView.INVALID_POSITION) {
            OkHttpCommonUtil okHttp = OkHttpCommonUtil.newInstance(getApplicationContext());
            okHttp.postRequest(URL_FEED_BACK,
                    new OkHttpCommonUtil.Param[] {
                            new OkHttpCommonUtil.Param(KEY_URL_FEED_BACK_UID, helper.getStringValue(AppConstants.KEY_SYS_CURRENT_UID)),
                            new OkHttpCommonUtil.Param(KEY_URL_FEED_BACK_CONTENT,feedbackContent),
                            new OkHttpCommonUtil.Param(KEY_URL_FEED_BACK_TYPE, sp_feedback_type.getSelectedItemPosition()),
                            new OkHttpCommonUtil.Param(KEY_URL_FEED_BACK_PIC, "")
                    },
                    new HttpBaseCallback() {
                        @Override
                        public void onResponse(Response response) throws IOException {
                            String result = response.body().string();
                            Log.d(TAG, "URL_FEED_BACK return == " + result);
                            JSONObject jsonObject = null;
                            try {
                                jsonObject = new JSONObject(result);
                                int status = jsonObject.getInt("status");
                                String message = jsonObject.getString("message");
                                if(status == 0) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(getApplicationContext(),getString(R.string.toast_feedback_ok), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });
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
