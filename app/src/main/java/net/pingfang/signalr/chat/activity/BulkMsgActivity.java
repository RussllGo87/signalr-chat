package net.pingfang.signalr.chat.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
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
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.text.TextUtils;
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

import net.pingfang.signalr.chat.R;
import net.pingfang.signalr.chat.constant.app.AppConstants;
import net.pingfang.signalr.chat.database.AppContract;
import net.pingfang.signalr.chat.database.ChatMessageManager;
import net.pingfang.signalr.chat.database.User;
import net.pingfang.signalr.chat.database.UserManager;
import net.pingfang.signalr.chat.message.ChatMessageProcessor;
import net.pingfang.signalr.chat.message.MessageConstant;
import net.pingfang.signalr.chat.message.MessageConstructor;
import net.pingfang.signalr.chat.service.ChatService;
import net.pingfang.signalr.chat.util.CommonTools;
import net.pingfang.signalr.chat.util.FileUtil;
import net.pingfang.signalr.chat.util.GlobalApplication;
import net.pingfang.signalr.chat.util.MediaFileUtils;
import net.pingfang.signalr.chat.util.SharedPreferencesHelper;

import java.io.File;
import java.io.IOException;

public class BulkMsgActivity extends AppCompatActivity implements View.OnClickListener{

    public static final String TAG = BulkMsgActivity.class.getSimpleName();

    TextView btn_activity_back;
    TextView tv_activity_title;
    TextView tv_offline_message;
    ScrollView sv_message_container;
    LinearLayout ll_message_container;
    EditText et_message;
    Button btn_voice_record;
    Button btn_send;

    SharedPreferencesHelper helper;
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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        helper = SharedPreferencesHelper.newInstance(getApplicationContext());
        chatMessageProcessor = new ChatMessageProcessor(getApplicationContext());
        uid = helper.getStringValue(AppConstants.KEY_SYS_CURRENT_UID);
        nickname = helper.getStringValue(AppConstants.KEY_SYS_CURRENT_NICKNAME);
        portrait = helper.getStringValue(AppConstants.KEY_SYS_CURRENT_PORTRAIT);

        initView();
        loadLocalMessage();
        initCommunicate();
    }

    private void initView() {
        btn_activity_back = (TextView) findViewById(R.id.btn_activity_back);
        btn_activity_back.setOnClickListener(this);

        tv_activity_title = (TextView) findViewById(R.id.tv_activity_title);
        tv_activity_title.setText(getString(R.string.title_activity_bulk_msg));

        tv_offline_message = (TextView) findViewById(R.id.tv_offline_message);

        sv_message_container = (ScrollView) findViewById(R.id.sv_message_container);
        ll_message_container = (LinearLayout) findViewById(R.id.ll_message_container);

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

        btn_send = (Button) findViewById(R.id.btn_send);
        btn_send.setOnClickListener(this);
        btn_send.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                popupMenu(view);
                return true;
            }
        });
    }

    private void loadLocalMessage() {
        new LoadLocalMessageTask().execute(uid, Integer.toString(MessageConstant.MESSAGE_TYPE_BULK));
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

    private void popupMenu(View view) {
        ContextThemeWrapper wrapper = new ContextThemeWrapper(getApplicationContext(), R.style.AppMainTheme);
        PopupMenu popup = new PopupMenu(wrapper, view);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.menu_message_actions, popup.getMenu());
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_text:
                        btn_voice_record.setVisibility(View.GONE);
                        et_message.setVisibility(View.VISIBLE);
                        break;
                    case R.id.action_photo:
                        btn_voice_record.setVisibility(View.GONE);
                        et_message.setVisibility(View.VISIBLE);
                        openCamera();
                        break;
                    case R.id.action_image:
                        btn_voice_record.setVisibility(View.GONE);
                        et_message.setVisibility(View.VISIBLE);
                        sendImage();
                        break;
                    case R.id.action_voice:
                        btn_voice_record.setVisibility(View.VISIBLE);
                        et_message.setVisibility(View.GONE);
                        break;
                }
                return true;
            }
        });
        popup.show();
    }

    /** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            ChatService.ChatBinder binder = (ChatService.ChatBinder) service;
            mService = (ChatService) binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName className) {
            mBound = false;
        }
    };

    @Override
    public void onClick(View view) {
        int viewId = view.getId();
        switch(viewId) {
            case R.id.btn_activity_back:
                navigateUp();
                break;
            case R.id.btn_send:
                sendMessage();
                hideKeyboard();
                break;
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
            String datetime = CommonTools.TimeConvertString();
            String messageBody = MessageConstructor.constructBulkTxtMsgReq(uid,nickname,portrait,content,datetime);
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

        sv_message_container.fullScroll(View.FOCUS_DOWN);
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
        String datetime = CommonTools.TimeConvertString();
        if(resultCode == Activity.RESULT_OK) {
            if(data != null && data.getData() != null) {
                if(requestCode == GlobalApplication.REQUEST_IMAGE_GET) {
                    Uri uri = data.getData();
                    if(uri != null) {
                        String filePath = FileUtil.getPath(getApplicationContext(), uri);
                        Bitmap bitmap = MediaFileUtils.decodeBitmapFromPath(filePath,
                                MediaFileUtils.dpToPx(getApplicationContext(), 150),
                                MediaFileUtils.dpToPx(getApplicationContext(), 150));
                        inflaterImgMessage(bitmap,uri, true, helper.getStringValue(AppConstants.KEY_SYS_CURRENT_NICKNAME), datetime);
                        String fileExtension = MediaFileUtils.getFileExtension(filePath);
                        String fileBody = CommonTools.bitmapToBase64(bitmap);
                        if(!TextUtils.isEmpty(fileExtension) && !TextUtils.isEmpty(fileBody)) {
                            String messageBody = MessageConstructor.constructBulkFileMsgReq(
                                    uid,nickname,portrait,"Picture",fileExtension,fileBody,datetime);
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
                inflaterImgMessage(bitmap, targetUri, true, helper.getStringValue(AppConstants.KEY_SYS_CURRENT_NICKNAME), datetime);
                String fileExtension = MediaFileUtils.getFileExtension(filePath);
                String fileBody = CommonTools.bitmapToBase64(bitmap);
                if(!TextUtils.isEmpty(fileExtension) && !TextUtils.isEmpty(fileBody)) {
                    String messageBody = MessageConstructor.constructBulkFileMsgReq(
                            uid, nickname, portrait, "Picture", fileExtension, fileBody, datetime);
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

        sv_message_container.fullScroll(View.FOCUS_DOWN);
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

            String datetime = CommonTools.TimeConvertString();
            Uri uri = Uri.parse(mFileName);
            inflaterVoiceMessage(uri, true, helper.getStringValue(AppConstants.KEY_SYS_CURRENT_NICKNAME), datetime);

            String fileExtension = MediaFileUtils.getFileExtension(mFileName);
            String fileBody = CommonTools.fileToBase64(mFileName);

            if(!TextUtils.isEmpty(fileExtension) && !TextUtils.isEmpty(fileBody)) {
                String messageBody = MessageConstructor.constructBulkFileMsgReq(uid, nickname, portrait,
                        "Audio", fileExtension, fileBody, datetime);
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

        sv_message_container.fullScroll(View.FOCUS_DOWN);
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
                    User user = userManager.queryUserByUid(from);
                    if(user != null) {
                        if(contentType.equals("Text")) {
                            String content = cursor.getString(cursor.getColumnIndex(AppContract.ChatMessageEntry.COLUMN_NAME_M_CONTENT));
                            publishProgress(from,user.getNickname(),contentType,content,datetime);
                        } else if(contentType.equals("Picture")){
                            String content = cursor.getString(cursor.getColumnIndex(AppContract.ChatMessageEntry.COLUMN_NAME_M_CONTENT));
                            publishProgress(from,user.getNickname(),contentType,content,datetime);
                        } else if(contentType.equals("Audio")) {
                            String content = cursor.getString(cursor.getColumnIndex(AppContract.ChatMessageEntry.COLUMN_NAME_M_CONTENT));
                            publishProgress(from,user.getNickname(),contentType,content,datetime);
                        }
                    }
                }
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

            boolean direction = true;
            if(!uid.equals(from)) {
                direction = false;
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
        }
    }

    // 接收到的消息处理
    private class ProcessMessageTask extends AsyncTask<Uri,String,String> {
        @Override
        protected String doInBackground(Uri... params) {
            Uri messageUri = params[0];
            ChatMessageManager chatMessageManager = new ChatMessageManager(getApplicationContext());
            chatMessageManager.updateStatus(messageUri, MessageConstant.MESSAGE_STATUS_READ);
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
                User user = userManager.queryUserByUid(from);
                if(contentType.equals("Text")) {
                    String content = cursor.getString(cursor.getColumnIndex(AppContract.ChatMessageEntry.COLUMN_NAME_M_CONTENT));
                    publishProgress(user.getNickname(),contentType,content,datetime);
                } else if(contentType.equals("Picture")){
                    String content = cursor.getString(cursor.getColumnIndex(AppContract.ChatMessageEntry.COLUMN_NAME_M_CONTENT));
                    publishProgress(user.getNickname(),contentType,content,datetime);
                } else if(contentType.equals("Audio")) {
                    String content = cursor.getString(cursor.getColumnIndex(AppContract.ChatMessageEntry.COLUMN_NAME_M_CONTENT));
                    publishProgress(user.getNickname(),contentType,content,datetime);
                }
                cursor.close();
            }
            return "ok";
        }

        @Override
        protected void onProgressUpdate(String... values) {
            String nickname = values[0];
            String contentType = values[1];
            String content = values[2];
            String newDatetime = values[3];

            if(!TextUtils.isEmpty(contentType)) {
                if(contentType.equals("Picture")) {
                    Bitmap bitmap = MediaFileUtils.decodeBitmapFromPath(content,
                            MediaFileUtils.dpToPx(getApplicationContext(),150),
                            MediaFileUtils.dpToPx(getApplicationContext(),150));

                    Uri uri = Uri.fromFile(new File(content));
                    inflaterImgMessage(bitmap,uri,false, nickname, newDatetime);
                } else if(contentType.equals("Audio")) {
                    Uri uri = Uri.fromFile(new File(content));
                    inflaterVoiceMessage(uri,false, nickname, newDatetime);
                } else if(contentType.equals("Text")) {
                    inflaterTxtMessage(nickname,false,content, newDatetime);
                }
            }
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
