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
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.siyamed.shapeimageview.BubbleImageView;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import net.pingfang.signalr.chat.R;
import net.pingfang.signalr.chat.constant.app.AppConstants;
import net.pingfang.signalr.chat.database.AppContract;
import net.pingfang.signalr.chat.database.ChatMessageManager;
import net.pingfang.signalr.chat.database.User;
import net.pingfang.signalr.chat.message.ChatMessageProcessor;
import net.pingfang.signalr.chat.message.MessageConstant;
import net.pingfang.signalr.chat.message.MessageConstructor;
import net.pingfang.signalr.chat.net.HttpBaseCallback;
import net.pingfang.signalr.chat.net.OkHttpCommonUtil;
import net.pingfang.signalr.chat.service.ChatService;
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

public class ChatActivity extends AppCompatActivity implements View.OnClickListener{

    public static final String TAG = ChatActivity.class.getSimpleName();

    public static final String URL_LOAD_OFFLINE_MSG = GlobalApplication.URL_WEB_API_HOST + "/api/WebAPI/User/RequestOfflineMsg";
    public static final String KEY_LOAD_OFFLINE_MSG_TO = "Receiver";
    public static final String KEY_LOAD_OFFLINE_MSG_FROM = "Sender";
    public static final String KEY_LOAD_OFFLINE_MSG_CURRENT_PAGE = "Page";
    public static final String KEY_LOAD_OFFLINE_MSG_PAGE_SIZE = "Rows";

    TextView btn_activity_back;
    TextView tv_activity_title;
    TextView tv_activity_connection_status;
    TextView tv_offline_message;
    ScrollView sv_message_container;
    LinearLayout ll_message_container;

    LinearLayout ll_record_voice_indicator;

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


    MessageReceiver receiver;

    String buddyName;
    String buddyUid;

    MediaRecorder mRecorder;
    String mFileName;
    boolean mStartRecording = false;
    MediaPlayer mPlayer;

    ChatService mService;
    boolean mBound = false;

    SharedPreferencesHelper helper;
    String uid;
    String nickname;
    String portrait;

    Uri targetUri;
    String tmpFilePath;

    Handler mHandler = new Handler(Looper.getMainLooper());

    private long currentTime = 0L;

    ChatMessageProcessor chatMessageProcessor;
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

//            String offlineMessageReq = MessageConstructor.constructOfflineMsgReq(buddyUid, uid, 1, 10);
//            Log.d("ChatActivity", offlineMessageReq);
//
//            mService.sendMessage("RequestOfflineMsg", offlineMessageReq);
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

        helper = SharedPreferencesHelper.newInstance(getApplicationContext());
        chatMessageProcessor = new ChatMessageProcessor(getApplicationContext());
        uid = helper.getStringValue(AppConstants.KEY_SYS_CURRENT_UID);
        nickname = helper.getStringValue(AppConstants.KEY_SYS_CURRENT_NICKNAME);
        portrait = helper.getStringValue(AppConstants.KEY_SYS_CURRENT_PORTRAIT);

        Intent intent = getIntent();
        User buddy = intent.getParcelableExtra("user");
        buddyName = buddy.getNickname();
        buddyUid = buddy.getUid();

        initView();
        updateMessageListStatus();
        loadOfflineMsg();
    }

    private void loadOfflineMsg() {
        OkHttpCommonUtil okHttpCommonUtil = OkHttpCommonUtil.newInstance(getApplicationContext());
        okHttpCommonUtil.getRequest(
                URL_LOAD_OFFLINE_MSG,
                new OkHttpCommonUtil.Param[] {
                    new OkHttpCommonUtil.Param(KEY_LOAD_OFFLINE_MSG_TO, uid),
                    new OkHttpCommonUtil.Param(KEY_LOAD_OFFLINE_MSG_FROM, buddyUid),
                    new OkHttpCommonUtil.Param(KEY_LOAD_OFFLINE_MSG_CURRENT_PAGE, 1),
                    new OkHttpCommonUtil.Param(KEY_LOAD_OFFLINE_MSG_PAGE_SIZE, 10)
                },
                new HttpBaseCallback() {

                    @Override
                    public void onFailure(Request request, IOException e) {
                        super.onFailure(request, e);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                loadLocalMessage();
                                initCommunicate();
                            }
                        });
                    }

                    @Override
                    public void onResponse(Response response) throws IOException {
                        String responseStr = response.body().string();
                        Log.d(TAG, "URL_LOAD_OFFLINE_MSG from == " + buddyUid);
                        Log.d(TAG, "URL_LOAD_OFFLINE_MSG return ==" + responseStr);
                        JSONArray jsonArray;
                        try {
                            jsonArray = new JSONArray(responseStr);
                            ArrayList<Uri> uriArrayList = new ArrayList<>();
                            for(int i = 0; i < jsonArray.length(); i++) {
                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                String fromUid = jsonObject.getString("Sender");
                                String to = jsonObject.getString("Receiver");
                                String owner = to;
                                String contentType = jsonObject.getString("MessageType");
                                String content = jsonObject.getString("Contents");
                                String datetime = jsonObject.getString("SendTime");
                                datetime = DateTimeUtil.convertServerTime(datetime);

                                // 消息存储
                                ChatMessageManager chatMessageManager = new ChatMessageManager(getApplicationContext());
                                Uri messageUri = null;
                                ContentValues values = new ContentValues();
                                if(!TextUtils.isEmpty(contentType)) {
                                    if(contentType.equals("Text")) {
                                        messageUri = chatMessageManager.insert(fromUid, to, owner, MessageConstant.MESSAGE_TYPE_OFF_LINE,
                                                contentType, content, datetime, MessageConstant.MESSAGE_STATUS_READ);
                                        values.put(AppContract.RecentContactEntry.COLUMN_NAME_CONTENT, content);
                                    } else if(contentType.equals("Picture")){
                                        String fileExtension = "png";
                                        String filePath = MediaFileUtils.processReceiveFile(getApplicationContext(), content,
                                                MessageConstant.MESSAGE_FILE_TYPE_IMG, fileExtension);
                                        messageUri = chatMessageManager.insert(fromUid, to, owner, MessageConstant.MESSAGE_TYPE_OFF_LINE,
                                                contentType, filePath, datetime, MessageConstant.MESSAGE_STATUS_READ);
                                        values.put(AppContract.RecentContactEntry.COLUMN_NAME_CONTENT, getApplicationContext().getResources().getString(R.string.content_type_pic));
                                    } else if(contentType.equals("Audio")) {
                                        String fileExtension = GlobalApplication.VOICE_FILE_NAME_SUFFIX;
                                        String filePath = MediaFileUtils.processReceiveFile(getApplicationContext(), content,
                                                MessageConstant.MESSAGE_FILE_TYPE_AUDIO, fileExtension);
                                        messageUri = chatMessageManager.insert(fromUid, to, owner, MessageConstant.MESSAGE_TYPE_OFF_LINE,
                                                contentType, filePath, datetime, MessageConstant.MESSAGE_STATUS_READ);
                                        values.put(AppContract.RecentContactEntry.COLUMN_NAME_CONTENT, getApplicationContext().getResources().getString(R.string.content_type_voice));
                                    }
                                }

                                //  最近消息记录
                                if(messageUri != null) {
                                    uriArrayList.add(messageUri);

                                    String selection =
                                            AppContract.RecentContactEntry.COLUMN_NAME_BUDDY + " = ? " +
                                                    "AND " +
                                                    AppContract.RecentContactEntry.COLUMN_NAME_OWNER + " = ?";
                                    Cursor newCursor = getApplicationContext().getContentResolver().query(AppContract.RecentContactEntry.CONTENT_URI,
                                            null, selection, new String[]{fromUid,to}, null);

                                    if(newCursor != null && newCursor.getCount() > 0 && newCursor.moveToFirst()){
                                        int rowId = newCursor.getInt(newCursor.getColumnIndex(AppContract.RecentContactEntry._ID));
                                        Uri appendUri = Uri.withAppendedPath(AppContract.RecentContactEntry.CONTENT_URI, Integer.toString(rowId));

                                        int currentCount = newCursor.getInt(newCursor.getColumnIndex(AppContract.RecentContactEntry.COLUMN_NAME_COUNT));

                                        values.put(AppContract.RecentContactEntry.COLUMN_NAME_UPDATE_TIME, datetime);
                                        values.put(AppContract.RecentContactEntry.COLUMN_NAME_COUNT,(currentCount -1));

                                        Log.d("ChatMessageProcessor", "processOfflineMessageList currentCount == " + currentCount);

                                        getApplicationContext().getContentResolver().update(appendUri, values, null, null);

                                        newCursor.close();
                                    } else {
                                        values.put(AppContract.RecentContactEntry.COLUMN_NAME_BUDDY,fromUid);
                                        values.put(AppContract.RecentContactEntry.COLUMN_NAME_UPDATE_TIME, datetime);
                                        values.put(AppContract.RecentContactEntry.COLUMN_NAME_OWNER,to);
                                        values.put(AppContract.RecentContactEntry.COLUMN_NAME_COUNT,0);
                                        getApplicationContext().getContentResolver().insert(AppContract.RecentContactEntry.CONTENT_URI,values);
                                    }
                                }
                            }

                            if( jsonArray.length() == 0) {
                                String selection =
                                        AppContract.RecentContactEntry.COLUMN_NAME_BUDDY + " = ? " +
                                        "AND " +
                                        AppContract.RecentContactEntry.COLUMN_NAME_OWNER + " = ?";
                                ContentValues values = new ContentValues();
                                values.put(AppContract.RecentContactEntry.COLUMN_NAME_COUNT,0);

                                getApplicationContext().getContentResolver().update(
                                        AppContract.RecentContactEntry.CONTENT_URI,
                                        values,
                                        selection,
                                        new String[]{buddyUid,uid});
                            }

                            Intent intent = new Intent();
                            intent.setAction(GlobalApplication.ACTION_INTENT_OFFLINE_MESSAGE_LIST_INCOMING);
                            intent.putParcelableArrayListExtra("message", uriArrayList);
                            getApplicationContext().sendBroadcast(intent);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getApplicationContext(),R.string.toast_load_offline_msg_invalidate,Toast.LENGTH_LONG).show();
                                }
                            });
                        } finally {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    loadLocalMessage();
                                    initCommunicate();
                                }
                            });
                        }
                    }
                });
    }

    private void initView() {
        btn_activity_back = (TextView) findViewById(R.id.btn_activity_back);
        btn_activity_back.setOnClickListener(this);

        tv_activity_title = (TextView) findViewById(R.id.tv_activity_title);
        tv_activity_title.setText(getString(R.string.title_activity_chat, buddyName));

        tv_activity_connection_status = (TextView) findViewById(R.id.tv_activity_connection_status);
        tv_activity_connection_status.setText(helper.getStringValue(ChatService.KEY_CONNECTION_STATUS,""));

        tv_offline_message = (TextView) findViewById(R.id.tv_offline_message);

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

        ll_record_voice_indicator = (LinearLayout) findViewById(R.id.ll_record_voice_indicator);
    }

    private void updateMessageListStatus() {
        new UpdateMsgListStatus().execute(uid, buddyUid);
    }

    private void loadLocalMessage() {
        new LoadLocalMessageTask().execute(uid, buddyUid);
    }

    private void initCommunicate() {
        registerReceiver();

        Intent intent = new Intent(this, ChatService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    public void registerReceiver() {
        receiver = new MessageReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(ChatService.INTENT_ACTION_CONNECTION_STATUS);
        filter.addAction(GlobalApplication.ACTION_INTENT_ONLINE_MESSAGE_INCOMING);
        filter.addAction(GlobalApplication.ACTION_INTENT_OFFLINE_MESSAGE_LIST_INCOMING);
        filter.addAction(GlobalApplication.ACTION_INTENT_BULK_MESSAGE_INCOMING);
        registerReceiver(receiver, filter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (receiver != null) {
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
                sendMessage();
                hideKeyboard();
                break;
            case R.id.iv_msg_type_chooser:
                onMsgTypeChooserClick();
                break;
            case R.id.iv_msg_type_txt:
                onVoiceTxtSwitchClick(true);
                break;
            case R.id.iv_msg_type_camera:
                openCamera();
                onVoiceTxtSwitchClick(true);
                break;
            case R.id.iv_msg_type_pic:
                sendImage();
                onVoiceTxtSwitchClick(true);
                break;
            case R.id.iv_msg_type_voice:
                onVoiceTxtSwitchClick(false);
                break;
        }
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

    private void sendMessage() {
        String content = et_message.getText().toString().trim();
        et_message.setText("");
        if(!TextUtils.isEmpty(content)) {
            String datetime = DateTimeUtil.TimeConvertString();
            String messageBody = MessageConstructor.constructTxtMessage(uid, nickname, portrait, buddyUid, content, datetime);
            // 消息发送
            mService.sendMessage("OnlineMsg", messageBody);
            chatMessageProcessor.onSendMessage("OnlineMsg", messageBody);
            inflaterTxtMessage(nickname, true, content, datetime);
        }
    }

    private void sendImage() {
        Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
        getIntent.setType("image/*");

        if (getIntent.resolveActivity(getPackageManager()) != null) {
            Intent chooserIntent = Intent.createChooser(getIntent, getString(R.string.action_select_image));
            startActivityForResult(chooserIntent, GlobalApplication.REQUEST_IMAGE_GET);
        }
    }

    private void openCamera() {
        tmpFilePath = MediaFileUtils.createFilePath(getApplicationContext(),
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
                    int i = 1;
                    String filePath = FileUtil.getPath(getApplicationContext(), uri);
                    Bitmap bitmap = MediaFileUtils.decodeBitmapFromPath(filePath,
                            MediaFileUtils.dpToPx(getApplicationContext(), 150),
                            MediaFileUtils.dpToPx(getApplicationContext(), 150));
                    inflaterImgMessage(bitmap,uri, true, helper.getStringValue(AppConstants.KEY_SYS_CURRENT_NICKNAME), datetime);
                    String fileExtension = MediaFileUtils.getFileExtension(filePath);
                    String fileBody = CommonTools.bitmapToBase64(bitmap);
                    if(!TextUtils.isEmpty(fileExtension) && !TextUtils.isEmpty(fileBody)) {
                        String messageBody = MessageConstructor.constructFileMessage(uid,nickname,portrait,
                                buddyUid,"Picture", fileExtension, fileBody,datetime);
                        mService.sendMessage("OnlineMsg", messageBody);
                        chatMessageProcessor.onSendMessage("OnlineMsg", messageBody);
                    }
                }
            } else if(requestCode == GlobalApplication.REQUEST_IMAGE_CAPTURE) {
                String filePath = tmpFilePath;
                Bitmap bitmap = MediaFileUtils.decodeBitmapFromPath(filePath,
                        MediaFileUtils.dpToPx(getApplicationContext(), 150),
                        MediaFileUtils.dpToPx(getApplicationContext(), 150));
                inflaterImgMessage(bitmap, targetUri, true, helper.getStringValue(AppConstants.KEY_SYS_CURRENT_NICKNAME), datetime);
                String fileExtension = MediaFileUtils.getFileExtension(filePath);
                String fileBody = CommonTools.bitmapToBase64(bitmap);
                int i = 1;
                if(!TextUtils.isEmpty(fileExtension) && !TextUtils.isEmpty(fileBody)) {
                    String messageBody = MessageConstructor.constructFileMessage(uid,nickname,portrait,
                            buddyUid,"Picture", fileExtension, fileBody, datetime);
                    mService.sendMessage("OnlineMsg", messageBody);
                    chatMessageProcessor.onSendMessage("OnlineMsg", messageBody);
                }
            }
        }
    }

    private void startRecording() {

        currentTime = System.currentTimeMillis();

        ll_record_voice_indicator.setVisibility(View.VISIBLE);
        mFileName = MediaFileUtils.createFilePath(getApplicationContext(),
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
                Log.e("ChatActivity", "prepare() failed");
            }

            mRecorder.start();

            mStartRecording = true;
            btn_voice_record.setText(R.string.btn_voice_record_up);

        }

    }

    private void stopRecording() {
        ll_record_voice_indicator.setVisibility(View.GONE);
        if(mStartRecording) {
            try {
                mRecorder.stop();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
            mRecorder.release();
            mRecorder = null;

            mStartRecording = false;
            btn_voice_record.setText(R.string.btn_voice_record);

            long now = System.currentTimeMillis();
            long delta = now - currentTime;
            currentTime = now;
            if(delta > 3000) {
                String datetime = DateTimeUtil.TimeConvertString();
                Uri uri = Uri.parse(mFileName);
                inflaterVoiceMessage(uri, true, helper.getStringValue(AppConstants.KEY_SYS_CURRENT_NICKNAME), datetime);

                String fileExtension = MediaFileUtils.getFileExtension(mFileName);
                String fileBody = CommonTools.fileToBase64(mFileName);
                int i = 1;
                if(!TextUtils.isEmpty(fileExtension) && !TextUtils.isEmpty(fileBody)) {
                    String messageBody = MessageConstructor.constructFileMessage(uid,nickname,portrait,
                            buddyUid, "Audio", fileExtension, fileBody,datetime);
                    mService.sendMessage("OnlineMsg", messageBody);
                    chatMessageProcessor.onSendMessage("OnlineMsg", messageBody);
                }
            } else {
                Toast.makeText(getApplicationContext(), "录音时间太短", Toast.LENGTH_LONG).show();
            }
        }

    }

    private void inflaterTxtMessage(String nameFrom, boolean direction, String content, String datetime) {
        inflaterTxtMessage(nameFrom, direction, content, datetime, true);
    }

    private void inflaterTxtMessage(String nameFrom, boolean direction, String content, String datetime, boolean isLast) {

        LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view;
        if(direction) {
            view = inflater.inflate(R.layout.ll_container_msg_me_txt, null);
            TextView tv_datetime = (TextView) view.findViewById(R.id.tv_chat_msg_me_time);
            tv_datetime.setText(datetime);
            TextView tv_name = (TextView) view.findViewById(R.id.tv_chat_msg_me_name);
            tv_name.setText(nameFrom);
            TextView tv_msg = (TextView) view.findViewById(R.id.tv_chat_msg_me_txt);
            tv_msg.setText(content);
            BubbleImageView iv_chat_msg_img = (BubbleImageView) view.findViewById(R.id.iv_chat_msg_me_img);
            iv_chat_msg_img.setVisibility(View.GONE);
            ImageView iv_chat_msg_voice = (ImageView) view.findViewById(R.id.iv_chat_msg_me_voice);
            iv_chat_msg_voice.setVisibility(View.GONE);
        } else {
            view = inflater.inflate(R.layout.ll_container_msg_buddy_txt, null);
            TextView tv_datetime = (TextView) view.findViewById(R.id.tv_chat_msg_buddy_time);
            tv_datetime.setText(datetime);
            TextView tv_name = (TextView) view.findViewById(R.id.tv_chat_msg_buddy_name);
            tv_name.setText(nameFrom);
            TextView tv_msg = (TextView) view.findViewById(R.id.tv_chat_msg_buddy_txt);
            tv_msg.setText(content);
            BubbleImageView iv_chat_msg_img = (BubbleImageView) view.findViewById(R.id.iv_chat_msg_buddy_img);
            iv_chat_msg_img.setVisibility(View.GONE);
            ImageView iv_chat_msg_voice = (ImageView) view.findViewById(R.id.iv_chat_msg_buddy_voice);
            iv_chat_msg_voice.setVisibility(View.GONE);
        }

        if(isLast) {
            ll_message_container.addView(view);
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    sv_message_container.fullScroll(ScrollView.FOCUS_DOWN);
                }
            });
        } else {
            ll_message_container.addView(view, 0);
        }

    }

    private void inflaterImgMessage(Bitmap bitmap,Uri uri,boolean direction,String from,String datetime) {
        inflaterImgMessage(bitmap, uri, direction, from, datetime, true);
    }

    private void inflaterImgMessage(Bitmap bitmap,Uri uri,boolean direction,String from,String datetime, boolean isLast) {

        LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view;
        if(direction) {
            view = inflater.inflate(R.layout.ll_container_msg_me_txt, null);
            TextView tv_datetime = (TextView) view.findViewById(R.id.tv_chat_msg_me_time);
            tv_datetime.setText(datetime);
            TextView tv_name = (TextView) view.findViewById(R.id.tv_chat_msg_me_name);
            tv_name.setText(from);
            TextView tv_msg = (TextView) view.findViewById(R.id.tv_chat_msg_me_txt);
            tv_msg.setVisibility(View.GONE);
            BubbleImageView iv_chat_msg_img = (BubbleImageView) view.findViewById(R.id.iv_chat_msg_me_img);
            iv_chat_msg_img.setImageBitmap(bitmap);
            iv_chat_msg_img.setTag(uri);
            iv_chat_msg_img.setOnClickListener(new View.OnClickListener() {
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
            ImageView iv_chat_msg_voice = (ImageView) view.findViewById(R.id.iv_chat_msg_me_voice);
            iv_chat_msg_voice.setVisibility(View.GONE);
        } else {
            view = inflater.inflate(R.layout.ll_container_msg_buddy_txt, null);
            TextView tv_datetime = (TextView) view.findViewById(R.id.tv_chat_msg_buddy_time);
            tv_datetime.setText(datetime);
            TextView tv_name = (TextView) view.findViewById(R.id.tv_chat_msg_buddy_name);
            tv_name.setText(from);
            TextView tv_msg = (TextView) view.findViewById(R.id.tv_chat_msg_buddy_txt);
            tv_msg.setVisibility(View.GONE);
            BubbleImageView iv_chat_msg_img = (BubbleImageView) view.findViewById(R.id.iv_chat_msg_buddy_img);
            iv_chat_msg_img.setImageBitmap(bitmap);
            iv_chat_msg_img.setTag(uri);
            iv_chat_msg_img.setOnClickListener(new View.OnClickListener() {
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
            ImageView iv_chat_msg_voice = (ImageView) view.findViewById(R.id.iv_chat_msg_buddy_voice);
            iv_chat_msg_voice.setVisibility(View.GONE);
        }

        if(isLast) {
            ll_message_container.addView(view);
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    sv_message_container.fullScroll(ScrollView.FOCUS_DOWN);
                }
            });
        } else {
            ll_message_container.addView(view, 0);
        }

    }

    private void inflaterVoiceMessage(Uri uri,boolean direction,String from, String datetime) {
        inflaterVoiceMessage(uri, direction, from, datetime,true);
    }

    private void inflaterVoiceMessage(Uri uri,boolean direction,String from, String datetime, boolean isLast) {

        LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view;
        if(direction) {
            view = inflater.inflate(R.layout.ll_container_msg_me_txt, null);
            TextView tv_datetime = (TextView) view.findViewById(R.id.tv_chat_msg_me_time);
            tv_datetime.setText(datetime);
            TextView tv_name = (TextView) view.findViewById(R.id.tv_chat_msg_me_name);
            tv_name.setText(from);
            TextView tv_msg = (TextView) view.findViewById(R.id.tv_chat_msg_me_txt);
            tv_msg.setVisibility(View.GONE);
            BubbleImageView iv_chat_msg_img = (BubbleImageView) view.findViewById(R.id.iv_chat_msg_me_img);
            iv_chat_msg_img.setVisibility(View.GONE);
            ImageView iv_chat_msg_voice = (ImageView) view.findViewById(R.id.iv_chat_msg_me_voice);
            iv_chat_msg_voice.setTag(uri);
            iv_chat_msg_voice.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Uri fileUri = (Uri) v.getTag();
                    if (mPlayer != null) {
                        mPlayer.release();
                        mPlayer = null;
                    }
                    mPlayer = MediaPlayer.create(getApplicationContext(), fileUri);
                    if(mPlayer != null) {
                        mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                            @Override
                            public void onCompletion(MediaPlayer mp) {
                                mp.release();
                            }
                        });
                        mPlayer.start();
                    }
                }
            });
        } else {
            view = inflater.inflate(R.layout.ll_container_msg_buddy_txt, null);
            TextView tv_datetime = (TextView) view.findViewById(R.id.tv_chat_msg_buddy_time);
            tv_datetime.setText(datetime);
            TextView tv_name = (TextView) view.findViewById(R.id.tv_chat_msg_buddy_name);
            tv_name.setText(from);
            TextView tv_msg = (TextView) view.findViewById(R.id.tv_chat_msg_buddy_txt);
            tv_msg.setVisibility(View.GONE);
            BubbleImageView iv_chat_msg_img = (BubbleImageView) view.findViewById(R.id.iv_chat_msg_buddy_img);
            iv_chat_msg_img.setVisibility(View.GONE);
            ImageView iv_chat_msg_voice = (ImageView) view.findViewById(R.id.iv_chat_msg_buddy_voice);
            iv_chat_msg_voice.setTag(uri);
            iv_chat_msg_voice.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Uri fileUri = (Uri) v.getTag();
                    if (mPlayer != null) {
                        mPlayer.release();
                        mPlayer = null;
                    }
                    mPlayer = MediaPlayer.create(getApplicationContext(), fileUri);
                    if(mPlayer != null) {
                        mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                            @Override
                            public void onCompletion(MediaPlayer mp) {
                                mp.release();
                            }
                        });
                        mPlayer.start();
                    }

                }
            });
        }
        if(isLast) {
            ll_message_container.addView(view);
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    sv_message_container.fullScroll(ScrollView.FOCUS_DOWN);
                }
            });
        } else {
            ll_message_container.addView(view, 0);
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

    private class MessageReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action.equals(GlobalApplication.ACTION_INTENT_ONLINE_MESSAGE_INCOMING)) {
                Bundle args = intent.getBundleExtra("message");
                Uri messageUri = args.getParcelable("messageUri");
                String fromUid = args.getString("fromUid");
                if(buddyUid.equals(fromUid)) {
                    new ProcessMessageTask().execute(messageUri);
                } else {
                    // build an notification on status bar when new online message is coming

                }
            } else if(action.equals(GlobalApplication.ACTION_INTENT_OFFLINE_MESSAGE_LIST_INCOMING)){
                ArrayList<Uri> uriArrayList  = intent.getParcelableArrayListExtra("message");
                for(int i = 0; i < uriArrayList.size(); i++) {
                    new ProcessMessageTask().execute(uriArrayList.get(i));
                }

                Intent newIntent = new Intent();
                newIntent.setAction(GlobalApplication.ACTION_INTENT_OFFLINE_MESSAGE_LIST_COUNT_UPDATE);
                newIntent.putExtra("message","update offline message count");
                context.sendBroadcast(newIntent);
            } else if (action.equals(GlobalApplication.ACTION_INTENT_BULK_MESSAGE_INCOMING)) {
                Bundle args = intent.getBundleExtra("message");
                Uri messageUri = args.getParcelable("messageUri");
                String fromUid = args.getString("fromUid");
                if (buddyUid.equals(fromUid)) {
                    new ProcessMessageTask().execute(messageUri);
                }
            } else if(action.equals(ChatService.INTENT_ACTION_CONNECTION_STATUS)) {
                Bundle args = intent.getBundleExtra("args");
                String message = args.getString("message");
                Log.d(TAG, "ChatService.INTENT_ACTION_CONNECTION_STATUS == " + message);
                tv_activity_connection_status.setText(message);
            }
        }
    }

    /**
     * 将存储本地的所有未读消息改为已读状态，并且更新未读消息数目
     */
    private class UpdateMsgListStatus extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... params) {
            String selection =
                    AppContract.ChatMessageEntry.COLUMN_NAME_M_OWNER + " = ? " +
                    " AND " +
                    AppContract.ChatMessageEntry.COLUMN_NAME_ENTRY_M_FROM + " = ? " +
                    " AND " +
                    AppContract.ChatMessageEntry.COLUMN_NAME_M_STATUS + " = ?";
            String owner = params[0];
            String buddy = params[1];

            // 更新消息表状态
            ContentValues values = new ContentValues();
            values.put(AppContract.ChatMessageEntry.COLUMN_NAME_M_STATUS, MessageConstant.MESSAGE_STATUS_READ);
            int update = getApplicationContext().getContentResolver().update(AppContract.ChatMessageEntry.CONTENT_URI,
                    values, selection, new String[]{owner, buddy, String.valueOf(MessageConstant.MESSAGE_STATUS_NOT_READ)});

            // 获取最近消息表未读消息数
            String selectionRecent =
                    AppContract.RecentContactView.COLUMN_NAME_OWNER + " = ? " +
                    "AND " +
                    AppContract.RecentContactView.COLUMN_NAME_UID + " = ?";
            Cursor cursor = getApplicationContext().getContentResolver().query(
                    AppContract.RecentContactView.CONTENT_URI,
                    null,
                    selectionRecent,
                    new String[]{owner, buddy},
                    null);
            int count = 0;
            if (cursor != null && cursor.moveToNext()) {
                count = cursor.getInt(cursor.getColumnIndex(AppContract.RecentContactView.COLUMN_NAME_COUNT));
                cursor.close();
            }

            // 更新最近消息列表未读消息数
            int total = count - update;
            String selectionUpdate =
                    AppContract.RecentContactEntry.COLUMN_NAME_OWNER + " = ? " +
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

    // 加载本地已有消息
    private class LoadLocalMessageTask extends AsyncTask<String,String,String > {
        @Override
        protected String doInBackground(String... params) {
            String selection =
                    AppContract.ChatMessageEntry.COLUMN_NAME_M_OWNER + " = ? " +
                    " AND " +
                    AppContract.ChatMessageEntry.COLUMN_NAME_ENTRY_M_FROM + " = ? " +
                    " OR " +
                    AppContract.ChatMessageEntry.COLUMN_NAME_ENTRY_M_TO + " = ?";

            String owner = params[0];
            String buddy = params[1];
            String[] selectionArgs = new String[] {owner,buddy,buddy};
            Cursor cursor = getApplicationContext().getContentResolver().query(AppContract.ChatMessageEntry.CONTENT_URI,
                    null, selection, selectionArgs, null);

            if(cursor == null) {
                return "query error";
            }

            if(!(cursor.getCount() > 0)) {
                return "no record";
            }

            cursor.moveToPrevious();
            while(cursor.moveToNext()) {
                String contentType = cursor.getString(cursor.getColumnIndex(AppContract.ChatMessageEntry.COLUMN_NAME_M_CONTENT_TYPE));
                String datetime = cursor.getString(cursor.getColumnIndex(AppContract.ChatMessageEntry.COLUMN_NAME_M_DATETIME));
                String from = cursor.getString(cursor.getColumnIndex(AppContract.ChatMessageEntry.COLUMN_NAME_ENTRY_M_FROM));
                int messageType = cursor.getInt(cursor.getColumnIndex(AppContract.ChatMessageEntry.COLUMN_NAME_M_TYPE));
                if (contentType.equals("Text")) {
                    String content = cursor.getString(cursor.getColumnIndex(AppContract.ChatMessageEntry.COLUMN_NAME_M_CONTENT));
                    publishProgress(from, contentType, content, datetime, String.valueOf(messageType));
                } else if (contentType.equals("Picture")) {
                    String content = cursor.getString(cursor.getColumnIndex(AppContract.ChatMessageEntry.COLUMN_NAME_M_CONTENT));
                    publishProgress(from, contentType, content, datetime, String.valueOf(messageType));
                } else if (contentType.equals("Audio")) {
                    String content = cursor.getString(cursor.getColumnIndex(AppContract.ChatMessageEntry.COLUMN_NAME_M_CONTENT));
                    publishProgress(from, contentType, content, datetime, String.valueOf(messageType));
                }
            }

            cursor.close();
            return "ok";
        }

        @Override
        protected void onProgressUpdate(String... values) {
            String from = values[0];
            String contentType = values[1];
            String content = values[2];
            String newDatetime = values[3];
            int messageType = Integer.valueOf(values[4]);

            String nameFrom = nickname;
            boolean direction = true;
            if(buddyUid.equals(from)) {
                direction = false;
                nameFrom = buddyName;
            }

            if(!TextUtils.isEmpty(contentType)) {
                if(contentType.equals("Picture")) {
                    Bitmap bitmap = MediaFileUtils.decodeBitmapFromPath(content,
                            MediaFileUtils.dpToPx(getApplicationContext(),150),
                            MediaFileUtils.dpToPx(getApplicationContext(),150));

                    Uri uri = Uri.fromFile(new File(content));
                    if (messageType == MessageConstant.MESSAGE_TYPE_BULK) {
                        String newBuddyName = nameFrom + "(群)";
                        inflaterImgMessage(bitmap, uri, direction, newBuddyName, newDatetime);
                    } else {
                        inflaterImgMessage(bitmap, uri, direction, nameFrom, newDatetime);
                    }
                } else if(contentType.equals("Audio")) {
                    Uri uri = Uri.fromFile(new File(content));
                    if (messageType == MessageConstant.MESSAGE_TYPE_BULK) {
                        String newBuddyName = nameFrom + "(群)";
                        inflaterVoiceMessage(uri, direction, newBuddyName, newDatetime);
                    } else {
                        inflaterVoiceMessage(uri, direction, nameFrom, newDatetime);
                    }
                } else if(contentType.equals("Text")) {
                    if (messageType == MessageConstant.MESSAGE_TYPE_BULK) {
                        String newBuddyName = nameFrom + "(群)";
                        inflaterTxtMessage(newBuddyName, direction, content, newDatetime);
                    } else {
                        inflaterTxtMessage(nameFrom, direction, content, newDatetime);
                    }
                }
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            Log.d(TAG,"LoadLocalMessageTask return == " + s);
        }
    }

    // 接收到的消息处理
    private class ProcessMessageTask extends AsyncTask<Uri,String,String> {
        @Override
        protected String doInBackground(Uri... params) {
            Uri messageUri = params[0];
            ChatMessageManager chatMessageManager = new ChatMessageManager(getApplicationContext());
            int update = chatMessageManager.updateStatus(messageUri, MessageConstant.MESSAGE_STATUS_READ);

            // 获取最近消息表未读消息数
            String selectionRecent =
                    AppContract.RecentContactView.COLUMN_NAME_OWNER + " = ? " +
                            "AND " +
                            AppContract.RecentContactView.COLUMN_NAME_UID + " = ?";
            Cursor countCursor = getApplicationContext().getContentResolver().query(AppContract.RecentContactView.CONTENT_URI,
                    null, selectionRecent, new String[]{uid, buddyUid}, null);
            int count = 0;
            if (countCursor != null && countCursor.moveToNext()) {
                count = countCursor.getInt(countCursor.getColumnIndex(AppContract.RecentContactView.COLUMN_NAME_COUNT));
                countCursor.close();
            }

            // 更新最近消息列表未读消息数
            int total = count - update;
            String selectionUpdate = AppContract.RecentContactEntry.COLUMN_NAME_OWNER + " = ? " +
                    "AND " +
                    AppContract.RecentContactEntry.COLUMN_NAME_BUDDY + " = ?";
            if (total > 0) {
                ContentValues updateValues = new ContentValues();
                updateValues.put(AppContract.RecentContactEntry.COLUMN_NAME_COUNT, total);
                getApplicationContext().getContentResolver().update(AppContract.RecentContactEntry.CONTENT_URI,
                        updateValues, selectionUpdate, new String[]{uid, buddyUid});

                Intent newIntent = new Intent();
                newIntent.setAction(GlobalApplication.ACTION_INTENT_MSG_UPDATE);
                newIntent.putExtra("message", "update offline message count");
                getApplicationContext().sendBroadcast(newIntent);
            } else {
                ContentValues updateValues = new ContentValues();
                updateValues.put(AppContract.RecentContactEntry.COLUMN_NAME_COUNT, 0);
                getApplicationContext().getContentResolver().update(AppContract.RecentContactEntry.CONTENT_URI,
                        updateValues, selectionUpdate, new String[]{uid, buddyUid});

                Intent newIntent = new Intent();
                newIntent.setAction(GlobalApplication.ACTION_INTENT_MSG_UPDATE);
                newIntent.putExtra("message", "update offline message count");
                getApplicationContext().sendBroadcast(newIntent);
            }

            Cursor cursor = null;
            if(messageUri != null) {
                cursor = getApplicationContext().getContentResolver().query(messageUri, null, null, null, null);
            }
            if(cursor != null && cursor.getCount() > 0 && cursor.moveToFirst()) {
                String contentType = cursor.getString(cursor.getColumnIndex(AppContract.ChatMessageEntry.COLUMN_NAME_M_CONTENT_TYPE));
                String datetime = cursor.getString(cursor.getColumnIndex(AppContract.ChatMessageEntry.COLUMN_NAME_M_DATETIME));
                int messageType = cursor.getInt(cursor.getColumnIndex(AppContract.ChatMessageEntry.COLUMN_NAME_M_TYPE));
                if(contentType.equals("Text")) {
                    String content = cursor.getString(cursor.getColumnIndex(AppContract.ChatMessageEntry.COLUMN_NAME_M_CONTENT));
                    publishProgress(contentType, content, datetime, String.valueOf(messageType));
                } else if(contentType.equals("Picture")){
                    String content = cursor.getString(cursor.getColumnIndex(AppContract.ChatMessageEntry.COLUMN_NAME_M_CONTENT));
                    publishProgress(contentType, content, datetime, String.valueOf(messageType));
                } else if(contentType.equals("Audio")) {
                    String content = cursor.getString(cursor.getColumnIndex(AppContract.ChatMessageEntry.COLUMN_NAME_M_CONTENT));
                    publishProgress(contentType, content, datetime, String.valueOf(messageType));
                }
                cursor.close();
            }
            return "ok";
        }

        @Override
        protected void onProgressUpdate(String... values) {
            String contentType = values[0];
            String content = values[1];
            String newDatetime = values[2];
            int messageType = Integer.valueOf(values[3]);

            if(!TextUtils.isEmpty(contentType)) {
                if(contentType.equals("Picture")) {
                    Bitmap bitmap = MediaFileUtils.decodeBitmapFromPath(content,
                            MediaFileUtils.dpToPx(getApplicationContext(),150),
                            MediaFileUtils.dpToPx(getApplicationContext(),150));

                    Uri uri = Uri.fromFile(new File(content));
                    if (messageType == MessageConstant.MESSAGE_TYPE_BULK) {
                        String newBuddyName = buddyName + "(群)";
                        inflaterImgMessage(bitmap, uri, false, newBuddyName, newDatetime);
                    } else {
                        inflaterImgMessage(bitmap, uri, false, buddyName, newDatetime);
                    }
                } else if(contentType.equals("Audio")) {
                    Uri uri = Uri.fromFile(new File(content));
                    if (messageType == MessageConstant.MESSAGE_TYPE_BULK) {
                        String newBuddyName = buddyName + "(群)";
                        inflaterVoiceMessage(uri, false, newBuddyName, newDatetime);
                    } else {
                        inflaterVoiceMessage(uri, false, buddyName, newDatetime);
                    }
                } else if(contentType.equals("Text")) {
                    if (messageType == MessageConstant.MESSAGE_TYPE_BULK) {
                        String newBuddyName = buddyName + "(群)";
                        inflaterTxtMessage(newBuddyName, false, content, newDatetime);
                    } else {
                        inflaterTxtMessage(buddyName, false, content, newDatetime);
                    }
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
