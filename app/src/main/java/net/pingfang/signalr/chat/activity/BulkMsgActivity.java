package net.pingfang.signalr.chat.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.provider.MediaStore;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import net.pingfang.signalr.chat.R;
import net.pingfang.signalr.chat.constant.app.AppConstants;
import net.pingfang.signalr.chat.database.AppContract;
import net.pingfang.signalr.chat.database.ChatMessageManager;
import net.pingfang.signalr.chat.database.User;
import net.pingfang.signalr.chat.database.UserManager;
import net.pingfang.signalr.chat.listener.OnDialogListItemListener;
import net.pingfang.signalr.chat.message.ChatMessageProcessor;
import net.pingfang.signalr.chat.message.MessageConstant;
import net.pingfang.signalr.chat.message.MessageConstructor;
import net.pingfang.signalr.chat.model.BulkRule;
import net.pingfang.signalr.chat.net.HttpBaseCallback;
import net.pingfang.signalr.chat.net.OkHttpCommonUtil;
import net.pingfang.signalr.chat.service.ChatService;
import net.pingfang.signalr.chat.ui.dialog.ListDialogFragment;
import net.pingfang.signalr.chat.ui.dialog.SingleButtonDialogFragment;
import net.pingfang.signalr.chat.util.CommonTools;
import net.pingfang.signalr.chat.util.DateTimeUtil;
import net.pingfang.signalr.chat.util.FileUtil;
import net.pingfang.signalr.chat.util.GlobalApplication;
import net.pingfang.signalr.chat.util.MediaFileUtils;
import net.pingfang.signalr.chat.util.SharedPreferencesHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class BulkMsgActivity extends AppCompatActivity implements View.OnClickListener, OnDialogListItemListener {

    public static final String TAG = BulkMsgActivity.class.getSimpleName();

    public static final String BULK_MSG_TYPE_TXT = "BULK_MSG_TYPE_TXT";
    public static final String BULK_MSG_TYPE_CAMERA = "BULK_MSG_TYPE_CAMERA";
    public static final String BULK_MSG_TYPE_PIC = "BULK_MSG_TYPE_PIC";
    public static final String BULK_MSG_TYPE_VOICE = "BULK_MSG_TYPE_VOICE";

    public static final String URL_ACCOUNT_INFO_LOAD = GlobalApplication.URL_WEB_API_HOST + "/api/WebAPI/User/GetUser";
    public static final String KEY_URL_ACCOUNT_INFO_LOAD_UID = "id";

    public static final String URL_BULK_MSG_RULE =
            GlobalApplication.URL_WEB_API_HOST + "/api/WebAPI/BroadcastIntegralDistance/GetBroadcastIntegralDistances";

    TextView btn_activity_back;
    TextView tv_activity_title;
    TextView tv_menu_drop_down;
    ScrollView sv_message_container;
    LinearLayout ll_message_container;

    ImageView iv_quick_voice_txt_switcher;
    Button btn_voice_record;
    EditText et_message;
    TextView btn_send;
    ImageView iv_msg_type_chooser;

    LinearLayout ll_msg_type_buttons_container;
    ImageView iv_msg_type_txt;
    ImageView iv_msg_type_camera;
    ImageView iv_msg_type_pic;
    ImageView iv_msg_type_voice;

    SharedPreferencesHelper sharedPreferencesHelper;
    String uid;
    String nickname;
    String portrait;

    MediaRecorder mRecorder;
    String mFileName;
    boolean mStartRecording = false;
    MediaPlayer mPlayer;

    ChatService mService;
    boolean mBound = false;
    MessageReceiver receiver;
    ChatMessageProcessor chatMessageProcessor;
    Uri targetUri;
    String tmpFilePath;

    int currentIntegration = 0;
    double currentDistance = 0.0d;
    int currentMaxMassTimes = 1;
    List<BulkRule> rulelist = new ArrayList<BulkRule>();
    List<String> listRuleString = new ArrayList<>();

    Handler mHandler = new Handler(Looper.getMainLooper());

    /**
     * Defines callbacks for service binding, passed to bindService()
     */
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            ChatService.ChatBinder binder = (ChatService.ChatBinder) service;
            mService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName className) {
            mBound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        sharedPreferencesHelper = SharedPreferencesHelper.newInstance(getApplicationContext());
        chatMessageProcessor = new ChatMessageProcessor(getApplicationContext());
        uid = sharedPreferencesHelper.getStringValue(AppConstants.KEY_SYS_CURRENT_UID);
        nickname = sharedPreferencesHelper.getStringValue(AppConstants.KEY_SYS_CURRENT_NICKNAME);
        portrait = sharedPreferencesHelper.getStringValue(AppConstants.KEY_SYS_CURRENT_PORTRAIT);

        initView();
        loadBulkRule();
        loadLocalMessage();
        initCommunicate();
    }

    private void initView() {
        btn_activity_back = (TextView) findViewById(R.id.btn_activity_back);
        btn_activity_back.setOnClickListener(this);

        tv_activity_title = (TextView) findViewById(R.id.tv_activity_title);
        tv_activity_title.setText(getString(R.string.title_activity_bulk_msg));

        tv_menu_drop_down = (TextView) findViewById(R.id.tv_offline_message);
        tv_menu_drop_down.setText(R.string.tv_activity_bulk_msg_menu_drop_down);
        tv_menu_drop_down.setVisibility(View.VISIBLE);
        tv_menu_drop_down.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //                popupMenu(view);
                loadBulkRule();
            }
        });

        sv_message_container = (ScrollView) findViewById(R.id.sv_message_container);
        ll_message_container = (LinearLayout) findViewById(R.id.ll_message_container);
        ll_message_container.setOnClickListener(this);

        iv_quick_voice_txt_switcher = (ImageView) findViewById(R.id.iv_quick_voice_txt_switcher);
        iv_quick_voice_txt_switcher.setOnClickListener(this);
        iv_quick_voice_txt_switcher.setSelected(false);

        btn_voice_record = (Button) findViewById(R.id.btn_voice_record);
        btn_voice_record.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int eventCode = event.getAction();
                switch (eventCode) {
                    case MotionEvent.ACTION_DOWN:
                        startRecording();
                        return true;
                    case MotionEvent.ACTION_UP:
                        stopRecording();
                        return true;
                }
                return false;
            }
        });

        et_message = (EditText) findViewById(R.id.et_message);
        et_message.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    sendMessage();
                    handled = true;
                }
                return handled;
            }
        });
        et_message.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    iv_msg_type_chooser.setVisibility(View.GONE);
                    btn_send.setVisibility(View.VISIBLE);
                } else {
                    iv_msg_type_chooser.setVisibility(View.VISIBLE);
                    btn_send.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        btn_send = (TextView) findViewById(R.id.btn_send);
        btn_send.setOnClickListener(this);

        iv_msg_type_chooser = (ImageView) findViewById(R.id.iv_msg_type_chooser);
        iv_msg_type_chooser.setOnClickListener(this);
        iv_msg_type_chooser.setSelected(false);

        ll_msg_type_buttons_container = (LinearLayout) findViewById(R.id.ll_msg_type_buttons_container);

        iv_msg_type_txt = (ImageView) findViewById(R.id.iv_msg_type_txt);
        iv_msg_type_txt.setOnClickListener(this);
        iv_msg_type_camera = (ImageView) findViewById(R.id.iv_msg_type_camera);
        iv_msg_type_camera.setOnClickListener(this);
        iv_msg_type_pic = (ImageView) findViewById(R.id.iv_msg_type_pic);
        iv_msg_type_pic.setOnClickListener(this);
        iv_msg_type_voice = (ImageView) findViewById(R.id.iv_msg_type_voice);
        iv_msg_type_voice.setOnClickListener(this);
    }

    private void loadBulkRule() {
        OkHttpCommonUtil okHttpCommon = OkHttpCommonUtil.newInstance(getApplicationContext());
        okHttpCommon.getRequest(URL_BULK_MSG_RULE, null, new HttpBaseCallback() {
            @Override
            public void onFailure(Request request, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), R.string.toast_load_bulk_msg_rule_error, Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onResponse(Response response) throws IOException {
                String jsonResponse = response.body().string();
                //                sharedPreferencesHelper.putStringValue(AppConstants.KEY_SYS_BULK_MSG_RULE, jsonResponse);
                rulelist.clear();
                try {
                    JSONArray jsonArray = new JSONArray(jsonResponse);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        int id = jsonObject.getInt("BroadcastIntegralDistanceId");
                        int integration = jsonObject.getInt("Integration");
                        double distance = jsonObject.getDouble("Distance");
                        int maxMassTimes = jsonObject.getInt("MaxMassTimes");

                        BulkRule bulkRule = new BulkRule(id, integration, distance, maxMassTimes);
                        rulelist.add(bulkRule);
                    }

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), R.string.toast_load_bulk_msg_rule_ok, Toast.LENGTH_LONG).show();
                            showRuleList();
                        }
                    });

                } catch (JSONException e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), R.string.toast_parse_bulk_msg_rule_error, Toast.LENGTH_LONG).show();
                        }
                    });
                }

            }
        });

    }

    private void showRuleList() {
        listRuleString.clear();
        for (BulkRule rule : rulelist) {
            if (rule.getDistance() < 1000) {
                listRuleString.add(rule.getDistance() + "米");
            } else {
                listRuleString.add((rule.getDistance() / 1000.0) + "公里");
            }
        }

        ListDialogFragment listDialogFragment = ListDialogFragment.newInstance(this, listRuleString);
        listDialogFragment.show(getSupportFragmentManager(), "ListDialogFragment");
    }

    @Override
    public void onDialogItemClick(int position) {
        BulkRule rule = rulelist.get(position);
        currentIntegration = rule.getIntegration();
        currentDistance = rule.getDistance();
        currentMaxMassTimes = rule.getMaxMassTimes();

        Toast.makeText(getApplicationContext(), listRuleString.get(position), Toast.LENGTH_LONG).show();
    }

    private void loadLocalMessage() {
        new LoadLocalMessageTask().execute(uid, String.valueOf(MessageConstant.MESSAGE_TYPE_BULK));
    }

    private void initCommunicate() {
        registerReceiver();

        Intent intent = new Intent(this, ChatService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }


    public void registerReceiver() {
        receiver = new MessageReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(GlobalApplication.ACTION_INTENT_BULK_MESSAGE_INCOMING);
        registerReceiver(receiver, filter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(receiver != null) {
            unregisterReceiver(receiver);
        }

        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
    }



    @Override
    public void onClick(View view) {
        int viewId = view.getId();
        switch(viewId) {
            case R.id.btn_activity_back:
                navigateUp();
                break;
            case R.id.ll_message_container:
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(et_message.getWindowToken(), 0);
                break;
            case R.id.iv_quick_voice_txt_switcher:
                onVoiceTxtSwitchClick(iv_quick_voice_txt_switcher.isSelected());
                break;
            case R.id.btn_send:
                checkSendCondition(BULK_MSG_TYPE_TXT);
                break;
            case R.id.iv_msg_type_chooser:
                onMsgTypeChooserClick();
                break;
            case R.id.iv_msg_type_txt:
                onVoiceTxtSwitchClick(true);
                break;
            case R.id.iv_msg_type_camera:
                checkSendCondition(BULK_MSG_TYPE_CAMERA);
                break;
            case R.id.iv_msg_type_pic:
                checkSendCondition(BULK_MSG_TYPE_PIC);
                break;
            case R.id.iv_msg_type_voice:
                checkSendCondition(BULK_MSG_TYPE_VOICE);
                break;
        }
    }

    private void checkSendCondition(final String type) {
        Toast.makeText(getApplicationContext(), "正在同步群消息发送记录数据", Toast.LENGTH_SHORT).show();
        OkHttpCommonUtil okHttp = OkHttpCommonUtil.newInstance(getApplicationContext());
        okHttp.getRequest(URL_ACCOUNT_INFO_LOAD,
                new OkHttpCommonUtil.Param[]{
                        new OkHttpCommonUtil.Param(KEY_URL_ACCOUNT_INFO_LOAD_UID,
                                sharedPreferencesHelper.getStringValue(AppConstants.KEY_SYS_CURRENT_UID))
                },
                new HttpBaseCallback() {
                    @Override
                    public void onResponse(Response response) throws IOException {
                        String result = response.body().string();
                        Log.d(TAG, "URL_ACCOUNT_INFO_LOAD return " + result);
                        JSONObject jsonObject = null;
                        try {
                            jsonObject = new JSONObject(result);
                            int status = jsonObject.getInt("status");
                            String message = jsonObject.getString("message");
                            if (status == 0) {
                                JSONObject resultJson = jsonObject.getJSONObject("result");
                                String nickName = resultJson.getString("nickname");
                                String portraitUrl = resultJson.getString("portrait");
                                final int exp = resultJson.getInt("exp");
                                final int massTimes = resultJson.getInt("massTimes");

                                ContentValues values = new ContentValues();
                                String selection = AppContract.UserEntry.COLUMN_NAME_ENTRY_UID + " = ?";
                                values.put(AppContract.UserEntry.COLUMN_NAME_NICK_NAME, nickName);
                                values.put(AppContract.UserEntry.COLUMN_NAME_PORTRAIT, portraitUrl);
                                getApplicationContext().getContentResolver().update(AppContract.UserEntry.CONTENT_URI,
                                        values,
                                        selection,
                                        new String[]{sharedPreferencesHelper.getStringValue(AppConstants.KEY_SYS_CURRENT_UID)});

                                sharedPreferencesHelper.putStringValue(AppConstants.KEY_SYS_CURRENT_NICKNAME, nickName);
                                sharedPreferencesHelper.putStringValue(AppConstants.KEY_SYS_CURRENT_PORTRAIT, portraitUrl);
                                sharedPreferencesHelper.putInt(AppConstants.KEY_SYS_CURRENT_USER_EXP, exp);
                                sharedPreferencesHelper.putInt(AppConstants.KEY_SYS_CURRENT_USER_SEND_TIME, massTimes);

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(getApplicationContext(), "同步群消息发送记录数据成功", Toast.LENGTH_SHORT).show();

                                        Intent intent = new Intent();
                                        intent.setAction(GlobalApplication.ACTION_INTENT_ACCOUNT_INFO_UPDATE);
                                        sendBroadcast(intent);

                                        if (!(currentIntegration > exp) && massTimes < currentMaxMassTimes) {
                                            if (type.equals(BULK_MSG_TYPE_TXT)) {
                                                sendMessage();
                                                hideKeyboard();
                                                return;
                                            }

                                            if (type.equals(BULK_MSG_TYPE_CAMERA)) {
                                                openCamera();
                                                onVoiceTxtSwitchClick(true);
                                                return;
                                            }

                                            if (type.equals(BULK_MSG_TYPE_PIC)) {
                                                sendImage();
                                                onVoiceTxtSwitchClick(true);
                                                return;
                                            }

                                            if (type.equals(BULK_MSG_TYPE_VOICE)) {
                                                onVoiceTxtSwitchClick(false);
                                            }
                                        } else if (currentIntegration > exp) {
                                            SingleButtonDialogFragment dialogFragment = SingleButtonDialogFragment.newInstance(getString(R.string.dialog_msg_bulk_msg_send_no_integration));
                                            dialogFragment.show(getSupportFragmentManager(), "SingleButtonDialogFragment");
                                            hideKeyboard();
                                        } else if (!(massTimes < currentMaxMassTimes)) {
                                            SingleButtonDialogFragment dialogFragment = SingleButtonDialogFragment.newInstance(getString(R.string.dialog_msg_bulk_msg_send_more_than_max_times));
                                            dialogFragment.show(getSupportFragmentManager(), "SingleButtonDialogFragment");
                                            hideKeyboard();
                                        }
                                    }
                                });
                            } else {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        SingleButtonDialogFragment dialogFragment = SingleButtonDialogFragment.newInstance(getString(R.string.dialog_msg_bulk_msg_load_info_error));
                                        dialogFragment.show(getSupportFragmentManager(), "SingleButtonDialogFragment");
                                        hideKeyboard();
                                    }
                                });
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    SingleButtonDialogFragment dialogFragment = SingleButtonDialogFragment.newInstance(getString(R.string.dialog_msg_bulk_msg_send_invalidate));
                                    dialogFragment.show(getSupportFragmentManager(), "SingleButtonDialogFragment");
                                    hideKeyboard();
                                }
                            });

                        }
                    }
                });
    }

    private void onMsgTypeChooserClick() {
        iv_quick_voice_txt_switcher.setSelected(false);
        btn_voice_record.setVisibility(View.GONE);
        et_message.setVisibility(View.VISIBLE);
        if (ll_msg_type_buttons_container.getVisibility() == View.VISIBLE) {
            ll_msg_type_buttons_container.setVisibility(View.GONE);
        } else if (ll_msg_type_buttons_container.getVisibility() == View.GONE) {
            ll_msg_type_buttons_container.setVisibility(View.VISIBLE);
        }

        if (et_message.isFocused()) {
            hideKeyboard();
        } else {
            et_message.requestFocus();
            showKeyboard();
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    sv_message_container.fullScroll(ScrollView.FOCUS_DOWN);
                }
            });
        }
    }

    private void onVoiceTxtSwitchClick(boolean selectedState) {
        if (!selectedState) {
            iv_quick_voice_txt_switcher.setSelected(true);
            btn_voice_record.setVisibility(View.VISIBLE);
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(et_message.getWindowToken(), 0);
            et_message.setVisibility(View.GONE);
            btn_send.setVisibility(View.GONE);
            iv_msg_type_chooser.setVisibility(View.VISIBLE);
            ll_msg_type_buttons_container.setVisibility(View.GONE);
        } else {
            iv_quick_voice_txt_switcher.setSelected(false);
            btn_voice_record.setVisibility(View.GONE);
            et_message.setVisibility(View.VISIBLE);
            et_message.requestFocus();
            showKeyboard();
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    sv_message_container.fullScroll(ScrollView.FOCUS_DOWN);
                }
            });
            if (!TextUtils.isEmpty(et_message.getText().toString().trim())) {
                btn_send.setVisibility(View.VISIBLE);
                iv_msg_type_chooser.setVisibility(View.GONE);
            } else {
                btn_send.setVisibility(View.GONE);
                iv_msg_type_chooser.setVisibility(View.VISIBLE);
            }
            ll_msg_type_buttons_container.setVisibility(View.GONE);
        }
    }

    private void showKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInputFromInputMethod(view.getWindowToken(), 0);
        }
    }

    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void popupMenu(View view) {
        ContextThemeWrapper wrapper = new ContextThemeWrapper(getApplicationContext(), R.style.AppMainTheme);
        PopupMenu popup = new PopupMenu(wrapper, view);
        final MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.menu_bulk_msg, popup.getMenu());
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.menu_bulk_action_radius_meter_100:
                        tv_menu_drop_down.setText(R.string.menu_bulk_action_radius_meter_100);
                        break;
                    case R.id.menu_bulk_action_radius_kilometer_1:
                        tv_menu_drop_down.setText(R.string.menu_bulk_action_radius_kilometer_1);
                        break;
                    case R.id.menu_bulk_action_radius_kilometer_5:
                        tv_menu_drop_down.setText(R.string.menu_bulk_action_radius_kilometer_5);
                        break;
                    case R.id.menu_bulk_action_radius_kilometer_10:
                        tv_menu_drop_down.setText(R.string.menu_bulk_action_radius_kilometer_10);
                        break;
                    case R.id.menu_bulk_action_radius_kilometer_100:
                        tv_menu_drop_down.setText(R.string.menu_bulk_action_radius_kilometer_100);
                        break;
                    case R.id.menu_bulk_action_radius_kilometer_1000:
                        tv_menu_drop_down.setText(R.string.menu_bulk_action_radius_kilometer_1000);
                        break;
                }
                return true;
            }
        });
        popup.show();
    }

    private void sendMessage() {
        String content = et_message.getText().toString().trim();
        et_message.setText("");
        if(!TextUtils.isEmpty(content)) {
            String datetime = DateTimeUtil.TimeConvertString();
            String messageBody = MessageConstructor.constructBulkTxtMsgReq(
                    uid,
                    nickname,
                    portrait,
                    content,
                    datetime,
                    sharedPreferencesHelper.getStringValue(AppConstants.KEY_SYS_LOCATION_LNG),
                    sharedPreferencesHelper.getStringValue(AppConstants.KEY_SYS_LOCATION_LAT),
                    currentIntegration,
                    currentDistance,
                    currentMaxMassTimes);
            // 消息发送
            mService.sendMessage("BulkMssaging", messageBody);
            chatMessageProcessor.onSendMessage("BulkMssaging",messageBody);
            inflaterTxtMessage(nickname, true, content, datetime);
        }
    }

    private void inflaterTxtMessage(String nameFrom, boolean direction, String content, String datetime) {
        LinearLayout ll = new LinearLayout(getApplicationContext());
        ll.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        ll.setOrientation(LinearLayout.VERTICAL);

        TextView tv_datetime = new TextView(getApplicationContext());
        tv_datetime.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        tv_datetime.setGravity(Gravity.CENTER_HORIZONTAL);
        tv_datetime.setText(datetime);
        tv_datetime.setTextColor(Color.BLACK);

        TextView tv_name = new TextView(getApplicationContext());
        tv_name.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        tv_name.setText(nameFrom);
        tv_name.setTextColor(Color.BLACK);
        if(direction) {
            tv_name.setTextColor(Color.RED);
            ll.setGravity(Gravity.LEFT);
        } else {
            tv_name.setTextColor(Color.BLACK);
            ll.setGravity(Gravity.RIGHT);
        }

        TextView tv_msg = new TextView(getApplicationContext());
        tv_msg.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT));
        tv_msg.setTextColor(Color.BLACK);
        tv_msg.setPadding(0, 0, MediaFileUtils.dpToPx(getApplicationContext(), 20), 0);
        tv_msg.setGravity(Gravity.CENTER_VERTICAL);
        tv_msg.setText(content);
        if(direction) {
            tv_msg.setBackgroundResource(R.drawable.msg_me);
        } else {
            tv_msg.setBackgroundResource(R.drawable.msg_buddy);
        }



        ll.addView(tv_datetime);
        ll.addView(tv_name);
        ll.addView(tv_msg);

        ll_message_container.addView(ll);

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                sv_message_container.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });
    }

    private void sendImage() {
        Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
        getIntent.setType("image/*");

        Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickIntent.setType("image/*");

        if (getIntent.resolveActivity(getPackageManager()) != null ||
                pickIntent.resolveActivity(getPackageManager()) != null) {

            Intent chooserIntent = Intent.createChooser(getIntent, getString(R.string.action_select_image));
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{pickIntent});

            startActivityForResult(chooserIntent, GlobalApplication.REQUEST_IMAGE_GET);
        }
    }

    private void openCamera() {
        tmpFilePath = MediaFileUtils.genarateFilePath(getApplicationContext(),
                Environment.DIRECTORY_PICTURES, "Photos", "jpg");
        File file = new File(tmpFilePath);
        if (file.exists()) {
            file.delete();
        }

        targetUri = Uri.fromFile(file);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, targetUri);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, GlobalApplication.REQUEST_IMAGE_CAPTURE);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        String datetime = DateTimeUtil.TimeConvertString();
        if(resultCode == Activity.RESULT_OK) {
            if(data != null && data.getData() != null) {
                if(requestCode == GlobalApplication.REQUEST_IMAGE_GET) {
                    Uri uri = data.getData();
                    if(uri != null) {
                        String filePath = FileUtil.getPath(getApplicationContext(), uri);
                        Bitmap bitmap = MediaFileUtils.decodeBitmapFromPath(filePath,
                                MediaFileUtils.dpToPx(getApplicationContext(), 150),
                                MediaFileUtils.dpToPx(getApplicationContext(), 150));
                        inflaterImgMessage(bitmap, uri, true, sharedPreferencesHelper.getStringValue(AppConstants.KEY_SYS_CURRENT_NICKNAME), datetime);
                        String fileExtension = MediaFileUtils.getFileExtension(filePath);
                        String fileBody = CommonTools.bitmapToBase64(bitmap);
                        if(!TextUtils.isEmpty(fileExtension) && !TextUtils.isEmpty(fileBody)) {
                            String messageBody = MessageConstructor.constructBulkFileMsgReq(
                                    uid, nickname, portrait, "Picture", fileExtension, fileBody, datetime,
                                    sharedPreferencesHelper.getStringValue(AppConstants.KEY_SYS_LOCATION_LNG),
                                    sharedPreferencesHelper.getStringValue(AppConstants.KEY_SYS_LOCATION_LAT),
                                    currentIntegration,
                                    currentDistance,
                                    currentMaxMassTimes);
                            Log.d(TAG, "messageBody = " + messageBody);
                            mService.sendMessage("BulkMssaging", messageBody);
                            chatMessageProcessor.onSendMessage("BulkMssaging", messageBody);
                        }
                    } else {
                        Log.d(TAG, "no data");
                    }
                }
            } else if(requestCode == GlobalApplication.REQUEST_IMAGE_CAPTURE) {
                String filePath = tmpFilePath;
                Bitmap bitmap = MediaFileUtils.decodeBitmapFromPath(filePath,
                        MediaFileUtils.dpToPx(getApplicationContext(), 150),
                        MediaFileUtils.dpToPx(getApplicationContext(), 150));
                inflaterImgMessage(bitmap, targetUri, true, sharedPreferencesHelper.getStringValue(AppConstants.KEY_SYS_CURRENT_NICKNAME), datetime);
                String fileExtension = MediaFileUtils.getFileExtension(filePath);
                String fileBody = CommonTools.bitmapToBase64(bitmap);
                if(!TextUtils.isEmpty(fileExtension) && !TextUtils.isEmpty(fileBody)) {
                    String messageBody = MessageConstructor.constructBulkFileMsgReq(
                            uid, nickname, portrait, "Picture", fileExtension, fileBody, datetime,
                            sharedPreferencesHelper.getStringValue(AppConstants.KEY_SYS_LOCATION_LNG),
                            sharedPreferencesHelper.getStringValue(AppConstants.KEY_SYS_LOCATION_LAT),
                            currentIntegration,
                            currentDistance,
                            currentMaxMassTimes);
                    Log.d(TAG, "messageBody = " + messageBody);
                    mService.sendMessage("BulkMssaging", messageBody);
                    chatMessageProcessor.onSendMessage("BulkMssaging", messageBody);
                }
            }
        }
    }

    private void inflaterImgMessage(Bitmap bitmap,Uri uri,boolean direction,String from,String datetime) {

        LinearLayout ll = new LinearLayout(getApplicationContext());
        ll.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        ll.setOrientation(LinearLayout.VERTICAL);

        TextView tv_datetime = new TextView(getApplicationContext());
        tv_datetime.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        tv_datetime.setGravity(Gravity.CENTER_HORIZONTAL);
        tv_datetime.setText(datetime);
        tv_datetime.setTextColor(Color.BLACK);

        TextView tv_name = new TextView(getApplicationContext());
        tv_name.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        tv_name.setText(from);
        if(direction) {
            tv_name.setTextColor(Color.RED);
            ll.setGravity(Gravity.LEFT);
        } else {
            tv_name.setTextColor(Color.BLACK);
            ll.setGravity(Gravity.RIGHT);
        }


        ImageView imageView = new ImageView(getApplicationContext());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(MediaFileUtils.dpToPx(getApplicationContext(),150),
                MediaFileUtils.dpToPx(getApplicationContext(),150));
        imageView.setLayoutParams(params);
        imageView.setPadding(MediaFileUtils.dpToPx(getApplicationContext(),10),
                MediaFileUtils.dpToPx(getApplicationContext(),10),
                MediaFileUtils.dpToPx(getApplicationContext(),10),
                MediaFileUtils.dpToPx(getApplicationContext(),10));
        imageView.setImageBitmap(bitmap);
        if(direction) {
            imageView.setBackgroundResource(R.drawable.msg_me);
        } else {
            imageView.setBackgroundResource(R.drawable.msg_buddy);
        }
        imageView.setTag(uri);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = (Uri) v.getTag();
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.setData(uri);
                intent.setDataAndType(uri, "image/*");
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                }
            }
        });

        ll.addView(tv_datetime);
        ll.addView(tv_name);
        ll.addView(imageView);

        ll_message_container.addView(ll);

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                sv_message_container.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });
    }

    private void startRecording() {

        mFileName = MediaFileUtils.genarateFilePath(getApplicationContext(),
                Environment.DIRECTORY_MUSIC, "voice", GlobalApplication.VOICE_FILE_NAME_SUFFIX);

        if(!TextUtils.isEmpty(mFileName) && !mStartRecording) {
            mRecorder = new MediaRecorder();
            mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mRecorder.setOutputFile(mFileName);
            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

            try {
                mRecorder.prepare();
            } catch (IOException e) {
                Log.e(TAG, "prepare() failed");
            }

            mRecorder.start();

            mStartRecording = true;
            btn_voice_record.setText(R.string.btn_voice_record_up);

        }

    }

    private void stopRecording() {
        if(mStartRecording) {
            mRecorder.stop();
            mRecorder.release();
            mRecorder = null;

            mStartRecording = false;
            btn_voice_record.setText(R.string.btn_voice_record);

            String datetime = DateTimeUtil.TimeConvertString();
            Uri uri = Uri.parse(mFileName);
            inflaterVoiceMessage(uri, true, sharedPreferencesHelper.getStringValue(AppConstants.KEY_SYS_CURRENT_NICKNAME), datetime);

            String fileExtension = MediaFileUtils.getFileExtension(mFileName);
            String fileBody = CommonTools.fileToBase64(mFileName);

            if(!TextUtils.isEmpty(fileExtension) && !TextUtils.isEmpty(fileBody)) {
                String messageBody = MessageConstructor.constructBulkFileMsgReq(uid, nickname, portrait,
                        "Audio", fileExtension, fileBody, datetime,
                        sharedPreferencesHelper.getStringValue(AppConstants.KEY_SYS_LOCATION_LNG),
                        sharedPreferencesHelper.getStringValue(AppConstants.KEY_SYS_LOCATION_LAT),
                        currentIntegration,
                        currentDistance,
                        currentMaxMassTimes);
                Log.d(TAG, "messageBody = " + messageBody);
                mService.sendMessage("BulkMssaging", messageBody);
                chatMessageProcessor.onSendMessage("BulkMssaging", messageBody);
            }
        }

    }

    private void inflaterVoiceMessage(Uri uri,boolean direction,String from, String datetime) {

        LinearLayout ll = new LinearLayout(getApplicationContext());
        ll.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        ll.setOrientation(LinearLayout.VERTICAL);

        TextView tv_datetime = new TextView(getApplicationContext());
        tv_datetime.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        tv_datetime.setGravity(Gravity.CENTER_HORIZONTAL);
        tv_datetime.setText(datetime);
        tv_datetime.setTextColor(Color.BLACK);

        TextView tv_name = new TextView(getApplicationContext());
        tv_name.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        tv_name.setText(from);
        if(direction) {
            tv_name.setTextColor(Color.RED);
            ll.setGravity(Gravity.LEFT);
        } else {
            tv_name.setTextColor(Color.BLACK);
            ll.setGravity(Gravity.RIGHT);
        }

        ImageView imageView = new ImageView(getApplicationContext());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(250,250);
        if(direction) {
            imageView.setLayoutParams(params);
            imageView.setImageResource(R.drawable.voice_me);
            imageView.setBackgroundResource(R.drawable.msg_me);

        } else {
            imageView.setLayoutParams(params);
            imageView.setImageResource(R.drawable.voice_buddy);
            imageView.setBackgroundResource(R.drawable.msg_buddy);
        }

        imageView.setLayoutParams(params);
        imageView.setTag(uri);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri fileUri = (Uri) v.getTag();
                if (mPlayer != null) {
                    mPlayer.release();
                    mPlayer = null;
                }
                mPlayer = MediaPlayer.create(getApplicationContext(), fileUri);
                mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        mp.release();
                    }
                });
                mPlayer.start();
            }
        });

        ll.addView(tv_datetime);
        ll.addView(tv_name);
        ll.addView(imageView);

        ll_message_container.addView(ll);

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                sv_message_container.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });
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

    private class MessageReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action.equals(GlobalApplication.ACTION_INTENT_BULK_MESSAGE_INCOMING)) {
                Bundle args = intent.getBundleExtra("message");
                Uri messageUri = args.getParcelable("messageUri");
                new ProcessMessageTask().execute(messageUri);
            }
        }
    }

    // 加载本地已有消息
    private class LoadLocalMessageTask extends AsyncTask<String,String,String > {
        @Override
        protected String doInBackground(String... params) {
            String selection =
                    AppContract.ChatMessageEntry.COLUMN_NAME_M_OWNER + " = ? " +
                    " AND " +
                    AppContract.ChatMessageEntry.COLUMN_NAME_M_TYPE + " = ? ";

            String owner = params[0];
            String msgType = params[1];
            String[] selectionArgs = new String[] {owner, msgType};

            Cursor cursor = getApplicationContext().getContentResolver().query(AppContract.ChatMessageEntry.CONTENT_URI,
                    null, selection, selectionArgs, null);

            UserManager userManager = new UserManager(getApplicationContext());
            if(cursor != null && cursor.getCount() > 0) {
                cursor.moveToPrevious();
                while(cursor.moveToNext()) {
                    String contentType = cursor.getString(cursor.getColumnIndex(AppContract.ChatMessageEntry.COLUMN_NAME_M_CONTENT_TYPE));
                    String datetime = cursor.getString(cursor.getColumnIndex(AppContract.ChatMessageEntry.COLUMN_NAME_M_DATETIME));
                    String from = cursor.getString(cursor.getColumnIndex(AppContract.ChatMessageEntry.COLUMN_NAME_ENTRY_M_FROM));
                    String messageType = cursor.getString(cursor.getColumnIndex(AppContract.ChatMessageEntry.COLUMN_NAME_M_TYPE));
                    String messageStatus = cursor.getString(cursor.getColumnIndex(AppContract.ChatMessageEntry.COLUMN_NAME_M_STATUS));

                    User user = userManager.queryUserByUid(from);
                    if(user != null) {
                        if(contentType.equals("Text")) {
                            String content = cursor.getString(cursor.getColumnIndex(AppContract.ChatMessageEntry.COLUMN_NAME_M_CONTENT));
                            publishProgress(from, user.getNickname(), contentType, content, datetime, messageType, messageStatus);
                        } else if(contentType.equals("Picture")){
                            String content = cursor.getString(cursor.getColumnIndex(AppContract.ChatMessageEntry.COLUMN_NAME_M_CONTENT));
                            publishProgress(from, user.getNickname(), contentType, content, datetime, messageType, messageStatus);
                        } else if(contentType.equals("Audio")) {
                            String content = cursor.getString(cursor.getColumnIndex(AppContract.ChatMessageEntry.COLUMN_NAME_M_CONTENT));
                            publishProgress(from, user.getNickname(), contentType, content, datetime, messageType, messageStatus);
                        }
                    }
                }
                cursor.close();
            }

            return "ok";
        }

        @Override
        protected void onProgressUpdate(String... values) {
            String from = values[0];
            String nameFrom = values[1];
            String contentType = values[2];
            String content = values[3];
            String newDatetime = values[4];
            String msgType = values[5];
            String msgStatus = values[6];

            boolean direction = true;
            if(!uid.equals(from)) {
                direction = false;
                int msgTypeInt = Integer.valueOf(msgType);
                int msgStatusInt = Integer.valueOf(msgStatus);
                if (msgTypeInt == MessageConstant.MESSAGE_TYPE_BULK && msgStatusInt == MessageConstant.MESSAGE_STATUS_NOT_READ) {
                    new UpdateMsgListStatus().execute(uid, from);
                }
            }

            if(!TextUtils.isEmpty(contentType)) {
                if(contentType.equals("Picture")) {
                    Bitmap bitmap = MediaFileUtils.decodeBitmapFromPath(content,
                            MediaFileUtils.dpToPx(getApplicationContext(),150),
                            MediaFileUtils.dpToPx(getApplicationContext(),150));

                    Uri uri = Uri.fromFile(new File(content));
                    inflaterImgMessage(bitmap,uri,direction, nameFrom, newDatetime);
                } else if(contentType.equals("Audio")) {
                    Uri uri = Uri.fromFile(new File(content));
                    inflaterVoiceMessage(uri,direction, nameFrom, newDatetime);
                } else if(contentType.equals("Text")) {
                    inflaterTxtMessage(nameFrom,direction,content, newDatetime);
                }
            }

            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    sv_message_container.fullScroll(ScrollView.FOCUS_DOWN);
                }
            });
        }
    }

    private class UpdateMsgListStatus extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... params) {
            String selection =
                    AppContract.ChatMessageEntry.COLUMN_NAME_M_OWNER + " = ? " +
                            " AND " +
                            AppContract.ChatMessageEntry.COLUMN_NAME_ENTRY_M_FROM + " = ? " +
                            " AND " +
                            AppContract.ChatMessageEntry.COLUMN_NAME_M_TYPE + " = ? " +
                            " AND " +
                            AppContract.ChatMessageEntry.COLUMN_NAME_M_STATUS + " = ? ";
            String owner = params[0];
            String buddy = params[1];

            // 更新消息表状态
            ContentValues values = new ContentValues();
            values.put(AppContract.ChatMessageEntry.COLUMN_NAME_M_STATUS, MessageConstant.MESSAGE_STATUS_READ);
            int update = getApplicationContext().getContentResolver().update(AppContract.ChatMessageEntry.CONTENT_URI,
                    values,
                    selection,
                    new String[]{
                            owner,
                            buddy,
                            String.valueOf(MessageConstant.MESSAGE_TYPE_BULK),
                            String.valueOf(MessageConstant.MESSAGE_STATUS_NOT_READ)});

            // 获取最近消息表未读消息数
            String selectionRecent = AppContract.RecentContactView.COLUMN_NAME_OWNER + " = ? " +
                    "AND " +
                    AppContract.RecentContactView.COLUMN_NAME_UID + " = ?";
            Cursor cursor = getApplicationContext().getContentResolver().query(AppContract.RecentContactView.CONTENT_URI,
                    null, selectionRecent, new String[]{owner, buddy}, null);
            int count = 0;
            if (cursor != null && cursor.moveToNext()) {
                count = cursor.getInt(cursor.getColumnIndex(AppContract.RecentContactView.COLUMN_NAME_COUNT));
                cursor.close();
            }

            // 更新最近消息列表未读消息数
            int total = count - update;
            String selectionUpdate = AppContract.RecentContactEntry.COLUMN_NAME_OWNER + " = ? " +
                    "AND " +
                    AppContract.RecentContactEntry.COLUMN_NAME_BUDDY + " = ?";
            if (count > update) {
                ContentValues updateValues = new ContentValues();
                updateValues.put(AppContract.RecentContactEntry.COLUMN_NAME_COUNT, total);
                getApplicationContext().getContentResolver().update(AppContract.RecentContactEntry.CONTENT_URI,
                        updateValues, selectionUpdate, new String[]{owner, buddy});
            } else {
                ContentValues updateValues = new ContentValues();
                updateValues.put(AppContract.RecentContactEntry.COLUMN_NAME_COUNT, 0);
                getApplicationContext().getContentResolver().update(AppContract.RecentContactEntry.CONTENT_URI,
                        updateValues, selectionUpdate, new String[]{owner, buddy});
            }

            return "ok";
        }

        @Override
        protected void onPostExecute(String s) {
            if (!TextUtils.isEmpty(s) && s.equals("ok")) {
                Intent newIntent = new Intent();
                newIntent.setAction(GlobalApplication.ACTION_INTENT_MSG_UPDATE);
                newIntent.putExtra("message", "update offline message count");
                getApplicationContext().sendBroadcast(newIntent);
            }
        }
    }

    // 接收到的消息处理
    private class ProcessMessageTask extends AsyncTask<Uri,String,String> {
        @Override
        protected String doInBackground(Uri... params) {
            Uri messageUri = params[0];
            ChatMessageManager chatMessageManager = new ChatMessageManager(getApplicationContext());
            Cursor cursor = null;
            if(messageUri != null) {
                chatMessageManager.updateStatus(messageUri, MessageConstant.MESSAGE_STATUS_READ);
                cursor = getApplicationContext().getContentResolver().query(messageUri, null, null, null, null);
            }
            UserManager userManager = new UserManager(getApplicationContext());
            if(cursor != null && cursor.getCount() > 0 && cursor.moveToFirst()) {
                String from = cursor.getString(cursor.getColumnIndex(AppContract.ChatMessageEntry.COLUMN_NAME_ENTRY_M_FROM));
                String contentType = cursor.getString(cursor.getColumnIndex(AppContract.ChatMessageEntry.COLUMN_NAME_M_CONTENT_TYPE));
                String datetime = cursor.getString(cursor.getColumnIndex(AppContract.ChatMessageEntry.COLUMN_NAME_M_DATETIME));

                String messageType = cursor.getString(cursor.getColumnIndex(AppContract.ChatMessageEntry.COLUMN_NAME_M_TYPE));
                String messageStatus = cursor.getString(cursor.getColumnIndex(AppContract.ChatMessageEntry.COLUMN_NAME_M_STATUS));

                User user = userManager.queryUserByUid(from);
                if(contentType.equals("Text")) {
                    String content = cursor.getString(cursor.getColumnIndex(AppContract.ChatMessageEntry.COLUMN_NAME_M_CONTENT));
                    publishProgress(from, user.getNickname(), contentType, content, datetime, messageType, messageStatus);
                } else if(contentType.equals("Picture")){
                    String content = cursor.getString(cursor.getColumnIndex(AppContract.ChatMessageEntry.COLUMN_NAME_M_CONTENT));
                    publishProgress(from, user.getNickname(), contentType, content, datetime, messageType, messageStatus);
                } else if(contentType.equals("Audio")) {
                    String content = cursor.getString(cursor.getColumnIndex(AppContract.ChatMessageEntry.COLUMN_NAME_M_CONTENT));
                    publishProgress(from, user.getNickname(), contentType, content, datetime, messageType, messageStatus);
                }
                cursor.close();
            }
            return "ok";
        }

        @Override
        protected void onProgressUpdate(String... values) {
            String from = values[0];
            String nickname = values[1];
            String contentType = values[2];
            String content = values[3];
            String newDatetime = values[4];
            String msgType = values[5];
            String msgStatus = values[6];

            boolean direction = true;
            if (!uid.equals(from)) {
                direction = false;
                int msgTypeInt = Integer.valueOf(msgType);
                int msgStatusInt = Integer.valueOf(msgStatus);
                if (msgTypeInt == MessageConstant.MESSAGE_TYPE_BULK && msgStatusInt == MessageConstant.MESSAGE_STATUS_NOT_READ) {
                    new UpdateMsgListStatus().execute(uid, from);
                }
            }

            if(!TextUtils.isEmpty(contentType)) {
                if(contentType.equals("Picture")) {
                    Bitmap bitmap = MediaFileUtils.decodeBitmapFromPath(content,
                            MediaFileUtils.dpToPx(getApplicationContext(),150),
                            MediaFileUtils.dpToPx(getApplicationContext(),150));

                    Uri uri = Uri.fromFile(new File(content));
                    inflaterImgMessage(bitmap, uri, direction, nickname, newDatetime);
                } else if(contentType.equals("Audio")) {
                    Uri uri = Uri.fromFile(new File(content));
                    inflaterVoiceMessage(uri, direction, nickname, newDatetime);
                } else if(contentType.equals("Text")) {
                    inflaterTxtMessage(nickname, direction, content, newDatetime);
                }
            }

            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    sv_message_container.fullScroll(ScrollView.FOCUS_DOWN);
                }
            });
        }
    }
}
