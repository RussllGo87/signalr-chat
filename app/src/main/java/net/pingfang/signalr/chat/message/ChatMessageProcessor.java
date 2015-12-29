package net.pingfang.signalr.chat.message;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import net.pingfang.signalr.chat.R;
import net.pingfang.signalr.chat.constant.app.AppConstants;
import net.pingfang.signalr.chat.database.AppContract;
import net.pingfang.signalr.chat.database.ChatMessageManager;
import net.pingfang.signalr.chat.database.DbUtils;
import net.pingfang.signalr.chat.database.User;
import net.pingfang.signalr.chat.database.UserManager;
import net.pingfang.signalr.chat.util.DateTimeUtil;
import net.pingfang.signalr.chat.util.GlobalApplication;
import net.pingfang.signalr.chat.util.MediaFileUtils;
import net.pingfang.signalr.chat.util.SharedPreferencesHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by gongguopei87@gmail.com on 2015/9/25.
 */
public class ChatMessageProcessor implements ChatMessageListener {

    public static final boolean MSG_ACTION_SEND = true;
    public static final boolean MSG_ACTION_REC = false;

    Context context;

    public ChatMessageProcessor(Context context) {
        this.context = context;
    }

    @Override
    public void onMessageReceive(String messageType, String message) {
        new ProcessMessageTask().execute(messageType, message);
    }

    public void onSendMessage(String messgeType, String messageBody) {
        new ProcessSendMessageTask().execute(messgeType,messageBody);
    }


    /**
     * 实现获取用户在线列表
     * @param message
     */
    private void processOnlineList(String message) {
        SharedPreferencesHelper sharedPreferencesHelper = SharedPreferencesHelper.newInstance(context);
        JSONArray jsonArray;

        try {
            jsonArray = new JSONArray(message);
            UserManager userManager = new UserManager(context);
            for(int i = 0; i < jsonArray.length(); i++) {
                JSONObject object = jsonArray.getJSONObject(i);
                String uid = object.getString("UserId");
                String nickName = object.getString("NickName");
                String headPortrait = object.getString("HeadPortrait");
                String distance = object.getString("Distance");
                int gender;
                boolean sex = object.getBoolean("Sex");
                if (sex) {
                    gender = User.USER_GENDER_MALE;
                } else {
                    gender = User.USER_GENDER_FEMALE;
                }

                boolean isExist = userManager.isExist(uid);
                if (isExist) {
                    String selection = AppContract.UserEntry.COLUMN_NAME_ENTRY_UID + " = ?";
                    ContentValues values = new ContentValues();
                    values.put(AppContract.UserEntry.COLUMN_NAME_NICK_NAME, nickName);
                    values.put(AppContract.UserEntry.COLUMN_NAME_PORTRAIT, headPortrait);
                    values.put(AppContract.UserEntry.COLUMN_NAME_GENDER, gender);
                    values.put(AppContract.UserEntry.COLUMN_NAME_DISTANCE, distance);
                    // 将用户附近状态标记设置为开启状态
                    values.put(AppContract.UserEntry.COLUMN_NAME_STATUS_NEARBY_LIST, User.USER_STATUS_NEARBY_LIST_IN);
                    context.getContentResolver().update(AppContract.UserEntry.CONTENT_URI,
                            values,
                            selection,
                            new String[]{uid});

                    String selectionStatus =
                            AppContract.UserStatusEntry.COLUMN_NAME_ENTRY_UID + " = ?" +
                                    " AND " +
                                    AppContract.UserStatusEntry.COLUMN_NAME_ENTRY_OWNER + " = ?";
                    String[] selectionArgsStatus = new String[]{uid, sharedPreferencesHelper.getStringValue(AppConstants.KEY_SYS_CURRENT_UID)};

                    ContentValues statusValues = new ContentValues();
                    statusValues.put(AppContract.UserStatusEntry.COLUMN_NAME_STATUS_NEARBY, User.USER_STATUS_NEARBY_LIST_IN);
                    statusValues.put(AppContract.UserStatusEntry.COLUMN_NAME_DISTANCE, distance);

                    context.getContentResolver().update(AppContract.UserStatusEntry.CONTENT_URI,
                            statusValues,
                            selectionStatus,
                            selectionArgsStatus);
                } else {
                    ContentValues values = new ContentValues();
                    values.put(AppContract.UserEntry.COLUMN_NAME_ENTRY_UID, uid);
                    values.put(AppContract.UserEntry.COLUMN_NAME_NICK_NAME, nickName);
                    values.put(AppContract.UserEntry.COLUMN_NAME_PORTRAIT, headPortrait);
                    values.put(AppContract.UserEntry.COLUMN_NAME_GENDER, gender);
                    //当前添加用户为新创建用户，不可能在出现在消息列表里
                    values.put(AppContract.UserEntry.COLUMN_NAME_STATUS_MSG_LIST, User.USER_STATUS_MSG_LIST_OUT);
                    // 将用户附近状态标记设置为开启状态
                    values.put(AppContract.UserEntry.COLUMN_NAME_STATUS_NEARBY_LIST, User.USER_STATUS_NEARBY_LIST_IN);
                    values.put(AppContract.UserEntry.COLUMN_NAME_DISTANCE, distance);

                    context.getContentResolver().insert(AppContract.UserEntry.CONTENT_URI, values);

                    ContentValues statusValues = new ContentValues();
                    statusValues.put(AppContract.UserStatusEntry.COLUMN_NAME_ENTRY_UID, uid);
                    statusValues.put(AppContract.UserStatusEntry.COLUMN_NAME_ENTRY_OWNER, sharedPreferencesHelper.getStringValue(AppConstants.KEY_SYS_CURRENT_UID));
                    statusValues.put(AppContract.UserStatusEntry.COLUMN_NAME_STATUS_MSG, User.USER_STATUS_MSG_LIST_OUT);
                    statusValues.put(AppContract.UserStatusEntry.COLUMN_NAME_STATUS_NEARBY, User.USER_STATUS_NEARBY_LIST_IN);
                    statusValues.put(AppContract.UserStatusEntry.COLUMN_NAME_STATUS_SHIELD, User.USER_STATUS_SHIELD_LIST_OUT);
                    statusValues.put(AppContract.UserStatusEntry.COLUMN_NAME_DISTANCE, distance);
                    statusValues.put(AppContract.UserStatusEntry.COLUMN_NAME_STATUS_REMARK, "");
                    context.getContentResolver().insert(AppContract.UserStatusEntry.CONTENT_URI, statusValues);
                }
            }
            Intent intent = new Intent();
            intent.setAction(GlobalApplication.ACTION_INTENT_UPDATE_ONLINE_LIST);
            context.sendBroadcast(intent);
        } catch (JSONException e) {
            e.printStackTrace();
            Log.d("ChatMessageProcessor", "processOnlineList() parse json error");
        }
    }

    /**
     * 处理在线消息的收发
     *
     * @param message   消息内容
     * @param direction true发送,false接收
     */
    private void processOnlineMessage(String message,boolean direction) {
        SharedPreferencesHelper sharedPreferencesHelper = SharedPreferencesHelper.newInstance(context);
        JSONObject object;
        try {
            object = new JSONObject(message);
            String from = object.getString("Sender");
            String to = object.getString("Receiver");
            String buddy = from;
            String owner = to;
            int status = MessageConstant.MESSAGE_STATUS_NOT_READ;

            // 如果是发送消息,消息状态设为已读
            if(direction) {
                buddy = to;
                owner = from;
                status = MessageConstant.MESSAGE_STATUS_READ;
            }

            String fromNickname = object.getString("SenderName");
            String fromPortrait = object.getString("SenderPortrait");
            String content = object.getString("Contents");
            String datetime = object.getString("SendTime");
            String contentType = object.getString("MessageType");
            if (!direction) {
                datetime = DateTimeUtil.convertServerTime(datetime);
            }

            // 如果是接收消息,需要添加用户信息到数据库用户表
            if(!direction) {
                UserManager userManager = new UserManager(context);
                boolean isExist = userManager.isExist(from);

                if (isExist) {
                    String selection = AppContract.UserEntry.COLUMN_NAME_ENTRY_UID + " = ?";
                    ContentValues values = new ContentValues();
                    values.put(AppContract.UserEntry.COLUMN_NAME_NICK_NAME, fromNickname);
                    values.put(AppContract.UserEntry.COLUMN_NAME_PORTRAIT, fromPortrait);
                    values.put(AppContract.UserEntry.COLUMN_NAME_STATUS_MSG_LIST, User.USER_STATUS_MSG_LIST_IN);
                    context.getContentResolver().update(AppContract.UserEntry.CONTENT_URI,
                            values,
                            selection,
                            new String[]{from});

                    String selectionStatus =
                            AppContract.UserStatusEntry.COLUMN_NAME_ENTRY_UID + " = ?" +
                            " AND " +
                            AppContract.UserStatusEntry.COLUMN_NAME_ENTRY_OWNER + " = ?";
                    String[] selectionArgsStatus = new String[]{from, sharedPreferencesHelper.getStringValue(AppConstants.KEY_SYS_CURRENT_UID)};

                    ContentValues statusValues = new ContentValues();
                    statusValues.put(AppContract.UserStatusEntry.COLUMN_NAME_STATUS_MSG, User.USER_STATUS_MSG_LIST_IN);

                    context.getContentResolver().update(AppContract.UserStatusEntry.CONTENT_URI,
                            statusValues,
                            selectionStatus,
                            selectionArgsStatus);

                } else {
                    ContentValues values = new ContentValues();
                    values.put(AppContract.UserEntry.COLUMN_NAME_ENTRY_UID, from);
                    values.put(AppContract.UserEntry.COLUMN_NAME_NICK_NAME, fromNickname);
                    values.put(AppContract.UserEntry.COLUMN_NAME_PORTRAIT, fromPortrait);
                    values.put(AppContract.UserEntry.COLUMN_NAME_STATUS_MSG_LIST, User.USER_STATUS_MSG_LIST_IN);
                    values.put(AppContract.UserEntry.COLUMN_NAME_STATUS_NEARBY_LIST, User.USER_STATUS_NEARBY_LIST_OUT);

                    context.getContentResolver().insert(AppContract.UserEntry.CONTENT_URI, values);

                    ContentValues statusValues = new ContentValues();
                    statusValues.put(AppContract.UserStatusEntry.COLUMN_NAME_ENTRY_UID, from);
                    statusValues.put(AppContract.UserStatusEntry.COLUMN_NAME_ENTRY_OWNER, sharedPreferencesHelper.getStringValue(AppConstants.KEY_SYS_CURRENT_UID));
                    statusValues.put(AppContract.UserStatusEntry.COLUMN_NAME_STATUS_MSG, User.USER_STATUS_MSG_LIST_IN);
                    statusValues.put(AppContract.UserStatusEntry.COLUMN_NAME_STATUS_NEARBY, User.USER_STATUS_NEARBY_LIST_OUT);
                    statusValues.put(AppContract.UserStatusEntry.COLUMN_NAME_STATUS_SHIELD, User.USER_STATUS_SHIELD_LIST_OUT);
                    statusValues.put(AppContract.UserStatusEntry.COLUMN_NAME_DISTANCE, 0);
                    statusValues.put(AppContract.UserStatusEntry.COLUMN_NAME_STATUS_REMARK, "");
                    context.getContentResolver().insert(AppContract.UserStatusEntry.CONTENT_URI, statusValues);
                }
            } else {
                String selection = AppContract.UserEntry.COLUMN_NAME_ENTRY_UID + " = ?";
                String[] selectionArgs = new String[]{to};

                ContentValues values = new ContentValues();
                values.put(AppContract.UserEntry.COLUMN_NAME_STATUS_MSG_LIST, User.USER_STATUS_MSG_LIST_IN);
                context.getContentResolver().update(AppContract.UserEntry.CONTENT_URI,
                        values,
                        selection,
                        selectionArgs);

                String selectionStatus =
                        AppContract.UserStatusEntry.COLUMN_NAME_ENTRY_UID + " = ?" +
                                " AND " +
                                AppContract.UserStatusEntry.COLUMN_NAME_ENTRY_OWNER + " = ?";
                String[] selectionArgsStatus = new String[]{to, sharedPreferencesHelper.getStringValue(AppConstants.KEY_SYS_CURRENT_UID)};

                ContentValues statusValues = new ContentValues();
                statusValues.put(AppContract.UserStatusEntry.COLUMN_NAME_STATUS_MSG, User.USER_STATUS_MSG_LIST_IN);

                context.getContentResolver().update(AppContract.UserStatusEntry.CONTENT_URI,
                        statusValues,
                        selectionStatus,
                        selectionArgsStatus);

                Intent intent = new Intent();
                intent.setAction(GlobalApplication.ACTION_INTENT_MSG_LIST_UPDATE);
                context.sendBroadcast(intent);
            }

            ChatMessageManager chatMessageManager = new ChatMessageManager(context);
            Uri messageUri = null;
            if(!TextUtils.isEmpty(contentType)) {
                ContentValues values = new ContentValues();
                if(contentType.equals("Text")) {
                   messageUri = chatMessageManager.insert(from, to, owner, MessageConstant.MESSAGE_TYPE_ON_LINE,
                            contentType, content, datetime, status);
                    values.put(AppContract.RecentContactEntry.COLUMN_NAME_CONTENT, content);
                } else if (contentType.equals("Picture")) {
                    //                    String fileExtension = object.getString("fileExtensionn");
                    String fileExtension = "jpg";
                    String filePath = MediaFileUtils.processReceiveFile(context, content,
                            MessageConstant.MESSAGE_FILE_TYPE_IMG, fileExtension);
                    messageUri = chatMessageManager.insert(from, to, owner, MessageConstant.MESSAGE_TYPE_ON_LINE,
                            contentType, filePath, datetime, status);
                    values.put(AppContract.RecentContactEntry.COLUMN_NAME_CONTENT, context.getResources().getString(R.string.content_type_pic));
                } else if(contentType.equals("Audio")) {
                    //                    String fileExtension = object.getString("fileExtension");
                    String fileExtension = "3gp";
                    String filePath = MediaFileUtils.processReceiveFile(context, content,
                            MessageConstant.MESSAGE_FILE_TYPE_AUDIO, fileExtension);
                    messageUri = chatMessageManager.insert(from, to, owner, MessageConstant.MESSAGE_TYPE_ON_LINE,
                            contentType, filePath, datetime, status);
                    values.put(AppContract.RecentContactEntry.COLUMN_NAME_CONTENT, context.getResources().getString(R.string.content_type_voice));
                }

                // 最近消息记录
                if(messageUri != null) {

                    String selection =
                            AppContract.RecentContactEntry.COLUMN_NAME_BUDDY + " = ? " +
                                    "AND " +
                                    AppContract.RecentContactEntry.COLUMN_NAME_OWNER + " = ?";
                    String[] selectionArgs = new String[]{buddy,owner};

                    Cursor newCursor = context.getContentResolver().query(AppContract.RecentContactEntry.CONTENT_URI,
                            null, selection, selectionArgs, null);

                    if(newCursor != null && newCursor.getCount() > 0 && newCursor.moveToFirst()){
                        int rowId = newCursor.getInt(newCursor.getColumnIndex(AppContract.RecentContactEntry._ID));
                        int count = newCursor.getInt(newCursor.getColumnIndex(AppContract.RecentContactEntry.COLUMN_NAME_COUNT));
                        Uri appendUri = Uri.withAppendedPath(AppContract.RecentContactEntry.CONTENT_URI, Integer.toString(rowId));

                        values.put(AppContract.RecentContactEntry.COLUMN_NAME_UPDATE_TIME, datetime);
                        if (status == MessageConstant.MESSAGE_STATUS_NOT_READ) {
                            count = count + 1;
                        }
                        values.put(AppContract.RecentContactEntry.COLUMN_NAME_COUNT, count);
                        context.getContentResolver().update(appendUri, values, null, null);

                        newCursor.close();
                    } else {
                        values.put(AppContract.RecentContactEntry.COLUMN_NAME_BUDDY,buddy);
                        values.put(AppContract.RecentContactEntry.COLUMN_NAME_UPDATE_TIME, datetime);
                        values.put(AppContract.RecentContactEntry.COLUMN_NAME_OWNER,owner);
                        if (status == MessageConstant.MESSAGE_STATUS_NOT_READ) {
                            values.put(AppContract.RecentContactEntry.COLUMN_NAME_COUNT, 1);
                        } else {
                            values.put(AppContract.RecentContactEntry.COLUMN_NAME_COUNT, 0);
                        }
                        context.getContentResolver().insert(AppContract.RecentContactEntry.CONTENT_URI,values);
                    }

                    if(!direction) {
                        Bundle args =  new Bundle();
                        args.putParcelable("messageUri", messageUri);
                        args.putString("fromUid", buddy);

                        Intent intent = new Intent();
                        intent.setAction(GlobalApplication.ACTION_INTENT_ONLINE_MESSAGE_INCOMING);
                        intent.putExtra("message", args);
                        context.sendBroadcast(intent);
                    } else {
                        Bundle args =  new Bundle();
                        args.putParcelable("messageUri", messageUri);
                        args.putString("fromUid", buddy);

                        Intent intent = new Intent();
                        intent.setAction(GlobalApplication.ACTION_INTENT_ONLINE_MESSAGE_SEND);
                        intent.putExtra("message", args);
                        context.sendBroadcast(intent);
                    }

                }

            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.d("ChatMessageProcessor", "processOnlineMessage() parse json error");
        }

    }

    private void processOfflineMsgShort(String message) {
        SharedPreferencesHelper sharedPreferencesHelper = SharedPreferencesHelper.newInstance(context);
        JSONArray jsonArray;
        UserManager userManager = new UserManager(context);
        try {
            jsonArray = new JSONArray(message);
            for(int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String fromUid = jsonObject.getString("Sender");
                String fromNickName = jsonObject.getString("NickName");
                String fromHeadPortrait = jsonObject.getString("HeadPortrait");
                if (!TextUtils.isEmpty(fromHeadPortrait)) {
                    if (!"null".equals(fromHeadPortrait)) {
                        fromHeadPortrait = GlobalApplication.URL_WEB_API_HOST + "/UpLoad/Head/" + fromHeadPortrait;
                    } else {
                        fromHeadPortrait = "";
                    }
                } else {
                    fromHeadPortrait = "";
                }
                int gender = User.USER_GENDER_MALE;
                boolean sex = jsonObject.getBoolean("Sex");
                if (sex) {
                    gender = User.USER_GENDER_MALE;
                } else {
                    gender = User.USER_GENDER_FEMALE;
                }
                int count = jsonObject.getInt("Count");
                String toUid = sharedPreferencesHelper.getStringValue(AppConstants.KEY_SYS_CURRENT_UID);

                boolean isExist = userManager.isExist(fromUid);
                if (isExist) {
                    String selection = AppContract.UserEntry.COLUMN_NAME_ENTRY_UID + " = ?";
                    ContentValues values = new ContentValues();
                    values.put(AppContract.UserEntry.COLUMN_NAME_NICK_NAME, fromNickName);
                    values.put(AppContract.UserEntry.COLUMN_NAME_PORTRAIT, fromHeadPortrait);
                    values.put(AppContract.UserEntry.COLUMN_NAME_GENDER, gender);
                    // 将用户消息状态标记设置为开启状态
                    values.put(AppContract.UserEntry.COLUMN_NAME_STATUS_MSG_LIST, User.USER_STATUS_MSG_LIST_IN);

                    context.getContentResolver().update(AppContract.UserEntry.CONTENT_URI,
                            values,
                            selection,
                            new String[]{fromUid});

                    String selectionStatus =
                            AppContract.UserStatusEntry.COLUMN_NAME_ENTRY_UID + " = ?" +
                                    " AND " +
                                    AppContract.UserStatusEntry.COLUMN_NAME_ENTRY_OWNER + " = ?";
                    String[] selectionArgsStatus = new String[] {fromUid,sharedPreferencesHelper.getStringValue(AppConstants.KEY_SYS_CURRENT_UID)};

                    ContentValues statusValues = new ContentValues();
                    statusValues.put(AppContract.UserStatusEntry.COLUMN_NAME_STATUS_MSG, User.USER_STATUS_MSG_LIST_IN);

                    context.getContentResolver().update(AppContract.UserStatusEntry.CONTENT_URI,
                            statusValues,
                            selectionStatus,
                            selectionArgsStatus);

                } else {
                    ContentValues values = new ContentValues();
                    values.put(AppContract.UserEntry.COLUMN_NAME_ENTRY_UID, fromUid);
                    values.put(AppContract.UserEntry.COLUMN_NAME_NICK_NAME, fromNickName);
                    values.put(AppContract.UserEntry.COLUMN_NAME_PORTRAIT, fromHeadPortrait);
                    values.put(AppContract.UserEntry.COLUMN_NAME_GENDER, gender);
                    // 将用户消息列表状态标记设置为开启状态
                    values.put(AppContract.UserEntry.COLUMN_NAME_STATUS_MSG_LIST, User.USER_STATUS_MSG_LIST_IN);
                    //当前添加用户为新创建用户，不可能在出现在附近列表里
                    values.put(AppContract.UserEntry.COLUMN_NAME_STATUS_NEARBY_LIST, User.USER_STATUS_NEARBY_LIST_OUT);

                    context.getContentResolver().insert(AppContract.UserEntry.CONTENT_URI, values);

                    ContentValues statusValues = new ContentValues();
                    statusValues.put(AppContract.UserStatusEntry.COLUMN_NAME_ENTRY_UID, fromUid);
                    statusValues.put(AppContract.UserStatusEntry.COLUMN_NAME_ENTRY_OWNER, sharedPreferencesHelper.getStringValue(AppConstants.KEY_SYS_CURRENT_UID));
                    statusValues.put(AppContract.UserStatusEntry.COLUMN_NAME_STATUS_MSG, User.USER_STATUS_MSG_LIST_IN);
                    statusValues.put(AppContract.UserStatusEntry.COLUMN_NAME_STATUS_NEARBY, User.USER_STATUS_NEARBY_LIST_OUT);
                    statusValues.put(AppContract.UserStatusEntry.COLUMN_NAME_STATUS_SHIELD, User.USER_STATUS_SHIELD_LIST_OUT);
                    statusValues.put(AppContract.UserStatusEntry.COLUMN_NAME_DISTANCE, 0);
                    statusValues.put(AppContract.UserStatusEntry.COLUMN_NAME_STATUS_REMARK, "");
                    context.getContentResolver().insert(AppContract.UserStatusEntry.CONTENT_URI, statusValues);
                }


                String selectionCountOnlineMsg =
                        AppContract.ChatMessageEntry.COLUMN_NAME_M_OWNER + " = ? " +
                        " AND " +
                        AppContract.ChatMessageEntry.COLUMN_NAME_ENTRY_M_FROM + " = ? " +
                        " AND " +
                        AppContract.ChatMessageEntry.COLUMN_NAME_M_STATUS + " = ?";


                // 查询未读非离线消息数
                int countOnlineMsg = DbUtils.count(
                        context,
                        AppContract.ChatMessageEntry.CONTENT_URI,
                        selectionCountOnlineMsg,
                        new String[]{toUid, fromUid, String.valueOf(MessageConstant.MESSAGE_STATUS_NOT_READ)});

                String selection =
                        AppContract.RecentContactEntry.COLUMN_NAME_BUDDY + " = ? " +
                                "AND " +
                                AppContract.RecentContactEntry.COLUMN_NAME_OWNER + " = ?";
                Cursor newCursor = context.getContentResolver().query(AppContract.RecentContactEntry.CONTENT_URI,
                        null, selection, new String[]{fromUid,toUid}, null);

                ContentValues recentValues = new ContentValues();
                recentValues.put(AppContract.RecentContactEntry.COLUMN_NAME_CONTENT, context.getResources().getString(R.string.content_new_offline_msg));

                // 最近消息记录
                if(newCursor != null && newCursor.getCount() > 0 && newCursor.moveToFirst()){
                    int rowId = newCursor.getInt(newCursor.getColumnIndex(AppContract.RecentContactEntry._ID));
                    Uri appendUri = Uri.withAppendedPath(AppContract.RecentContactEntry.CONTENT_URI, Integer.toString(rowId));

                    recentValues.put(AppContract.RecentContactEntry.COLUMN_NAME_CONTENT, "[新的离线消息]");
                    recentValues.put(AppContract.RecentContactEntry.COLUMN_NAME_UPDATE_TIME, DateTimeUtil.TimeConvertString());
                    recentValues.put(AppContract.RecentContactEntry.COLUMN_NAME_COUNT, (countOnlineMsg + count));
                    context.getContentResolver().update(appendUri, recentValues, null, null);

                    newCursor.close();
                } else {
                    recentValues.put(AppContract.RecentContactEntry.COLUMN_NAME_BUDDY, fromUid);
                    recentValues.put(AppContract.RecentContactEntry.COLUMN_NAME_UPDATE_TIME, DateTimeUtil.TimeConvertString());
                    recentValues.put(AppContract.RecentContactEntry.COLUMN_NAME_OWNER, toUid);
                    recentValues.put(AppContract.RecentContactEntry.COLUMN_NAME_COUNT, count);
                    recentValues.put(AppContract.RecentContactEntry.COLUMN_NAME_CONTENT, "[新的离线消息]");
                    context.getContentResolver().insert(AppContract.RecentContactEntry.CONTENT_URI, recentValues);
                }
            }

            Intent intent = new Intent();
            intent.setAction(GlobalApplication.ACTION_INTENT_OFFLINE_USER_LIST_INCOMING);
            context.sendBroadcast(intent);
        } catch (JSONException e) {
            e.printStackTrace();
        }


    }

    private void processOfflineMessageList(String message) {
        try {
            JSONArray jsonArray = new JSONArray(message);
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
                ChatMessageManager chatMessageManager = new ChatMessageManager(context);
                Uri messageUri = null;
                ContentValues values = new ContentValues();
                if(!TextUtils.isEmpty(contentType)) {
                    if(contentType.equals("Text")) {
                        messageUri = chatMessageManager.insert(fromUid, to, owner, MessageConstant.MESSAGE_TYPE_OFF_LINE,
                                contentType, content, datetime, MessageConstant.MESSAGE_STATUS_READ);
                        values.put(AppContract.RecentContactEntry.COLUMN_NAME_CONTENT, content);
                    } else if(contentType.equals("Picture")){
                        String fileExtension = "png";
                        String filePath = MediaFileUtils.processReceiveFile(context, content,
                                MessageConstant.MESSAGE_FILE_TYPE_IMG, fileExtension);
                        messageUri = chatMessageManager.insert(fromUid, to, owner, MessageConstant.MESSAGE_TYPE_OFF_LINE,
                                contentType, filePath, datetime, MessageConstant.MESSAGE_STATUS_READ);
                        values.put(AppContract.RecentContactEntry.COLUMN_NAME_CONTENT, context.getResources().getString(R.string.content_type_pic));
                    } else if(contentType.equals("Audio")) {
                        String fileExtension = GlobalApplication.VOICE_FILE_NAME_SUFFIX;
                        String filePath = MediaFileUtils.processReceiveFile(context, content,
                                MessageConstant.MESSAGE_FILE_TYPE_AUDIO, fileExtension);
                        messageUri = chatMessageManager.insert(fromUid, to, owner, MessageConstant.MESSAGE_TYPE_OFF_LINE,
                                contentType, filePath, datetime, MessageConstant.MESSAGE_STATUS_READ);
                        values.put(AppContract.RecentContactEntry.COLUMN_NAME_CONTENT, context.getResources().getString(R.string.content_type_voice));
                    }
                }

                // // 最近消息记录
                if(messageUri != null) {
                    uriArrayList.add(messageUri);

                    String selection =
                            AppContract.RecentContactEntry.COLUMN_NAME_BUDDY + " = ? " +
                                    "AND " +
                                    AppContract.RecentContactEntry.COLUMN_NAME_OWNER + " = ?";
                    Cursor newCursor = context.getContentResolver().query(AppContract.RecentContactEntry.CONTENT_URI,
                            null, selection, new String[]{fromUid,to}, null);

                    if(newCursor != null && newCursor.getCount() > 0 && newCursor.moveToFirst()){
                        int rowId = newCursor.getInt(newCursor.getColumnIndex(AppContract.RecentContactEntry._ID));
                        Uri appendUri = Uri.withAppendedPath(AppContract.RecentContactEntry.CONTENT_URI, Integer.toString(rowId));

                        int currentCount = newCursor.getInt(newCursor.getColumnIndex(AppContract.RecentContactEntry.COLUMN_NAME_COUNT));

                        values.put(AppContract.RecentContactEntry.COLUMN_NAME_UPDATE_TIME, datetime);
                        values.put(AppContract.RecentContactEntry.COLUMN_NAME_COUNT,(currentCount -1));

                        Log.d("ChatMessageProcessor", "processOfflineMessageList currentCount == " + currentCount);

                        context.getContentResolver().update(appendUri, values, null, null);

                        newCursor.close();
                    } else {
                        values.put(AppContract.RecentContactEntry.COLUMN_NAME_BUDDY,fromUid);
                        values.put(AppContract.RecentContactEntry.COLUMN_NAME_UPDATE_TIME, datetime);
                        values.put(AppContract.RecentContactEntry.COLUMN_NAME_OWNER,to);
                        values.put(AppContract.RecentContactEntry.COLUMN_NAME_COUNT,0);
                        context.getContentResolver().insert(AppContract.RecentContactEntry.CONTENT_URI,values);
                    }
                }
            }

            Intent intent = new Intent();
            intent.setAction(GlobalApplication.ACTION_INTENT_OFFLINE_MESSAGE_LIST_INCOMING);
            intent.putParcelableArrayListExtra("message", uriArrayList);
            context.sendBroadcast(intent);

        } catch (JSONException e) {
            e.printStackTrace();
        }


    }

    private void processBulkMessage(String message,boolean direction) {
        SharedPreferencesHelper sharedPreferencesHelper = SharedPreferencesHelper.newInstance(context);
        JSONObject object;
        try {
            object = new JSONObject(message);
            // 发送群消息
            String from = sharedPreferencesHelper.getStringValue(AppConstants.KEY_SYS_CURRENT_UID);
            String to = "0";
            String owner = sharedPreferencesHelper.getStringValue(AppConstants.KEY_SYS_CURRENT_UID);
            int status = MessageConstant.MESSAGE_STATUS_READ;


            // 接收群消息
            if (!direction) {
                from = object.getString("Sender");
                to = sharedPreferencesHelper.getStringValue(AppConstants.KEY_SYS_CURRENT_UID);
                owner = sharedPreferencesHelper.getStringValue(AppConstants.KEY_SYS_CURRENT_UID);
                status = MessageConstant.MESSAGE_STATUS_NOT_READ;
            }

            String fromNickname = object.getString("SenderName");
            String fromPortrait = object.getString("SenderPortrait");
            String content = object.getString("Contents");
            String datetime = object.getString("SendTime");
            String contentType = object.getString("MessageType");
            if (!direction) {
                datetime = DateTimeUtil.convertServerTime(datetime);
            }

            // 如果是接收消息,需要添加用户信息到数据库用户表
            if (!direction) {
                UserManager userManager = new UserManager(context);
                boolean isExist = userManager.isExist(from);
                ContentValues values = new ContentValues();
                if (isExist) {
                    String selection = AppContract.UserEntry.COLUMN_NAME_ENTRY_UID + " = ?";
                    values.put(AppContract.UserEntry.COLUMN_NAME_NICK_NAME, fromNickname);
                    values.put(AppContract.UserEntry.COLUMN_NAME_PORTRAIT, fromPortrait);
                    values.put(AppContract.UserEntry.COLUMN_NAME_STATUS_MSG_LIST, User.USER_STATUS_MSG_LIST_IN);
                    context.getContentResolver().update(AppContract.UserEntry.CONTENT_URI,
                            values,
                            selection,
                            new String[]{from});

                    String selectionStatus =
                            AppContract.UserStatusEntry.COLUMN_NAME_ENTRY_UID + " = ?" +
                            " AND " +
                            AppContract.UserStatusEntry.COLUMN_NAME_ENTRY_OWNER + " = ?";
                    String[] selectionArgsStatus = new String[] {from,sharedPreferencesHelper.getStringValue(AppConstants.KEY_SYS_CURRENT_UID)};

                    ContentValues statusValues = new ContentValues();
                    statusValues.put(AppContract.UserStatusEntry.COLUMN_NAME_STATUS_MSG, User.USER_STATUS_MSG_LIST_IN);

                    context.getContentResolver().update(AppContract.UserStatusEntry.CONTENT_URI,
                            statusValues,
                            selectionStatus,
                            selectionArgsStatus);

                } else {
                    values.put(AppContract.UserEntry.COLUMN_NAME_ENTRY_UID, from);
                    values.put(AppContract.UserEntry.COLUMN_NAME_NICK_NAME, fromNickname);
                    values.put(AppContract.UserEntry.COLUMN_NAME_PORTRAIT, fromPortrait);
                    values.put(AppContract.UserEntry.COLUMN_NAME_STATUS_MSG_LIST, User.USER_STATUS_MSG_LIST_IN);
                    values.put(AppContract.UserEntry.COLUMN_NAME_STATUS_NEARBY_LIST, User.USER_STATUS_NEARBY_LIST_OUT);

                    context.getContentResolver().insert(AppContract.UserEntry.CONTENT_URI, values);

                    ContentValues statusValues = new ContentValues();
                    statusValues.put(AppContract.UserStatusEntry.COLUMN_NAME_ENTRY_UID, from);
                    statusValues.put(AppContract.UserStatusEntry.COLUMN_NAME_ENTRY_OWNER, sharedPreferencesHelper.getStringValue(AppConstants.KEY_SYS_CURRENT_UID));
                    statusValues.put(AppContract.UserStatusEntry.COLUMN_NAME_STATUS_MSG, User.USER_STATUS_MSG_LIST_IN);
                    statusValues.put(AppContract.UserStatusEntry.COLUMN_NAME_STATUS_NEARBY, User.USER_STATUS_NEARBY_LIST_OUT);
                    statusValues.put(AppContract.UserStatusEntry.COLUMN_NAME_STATUS_SHIELD, User.USER_STATUS_SHIELD_LIST_OUT);
                    statusValues.put(AppContract.UserStatusEntry.COLUMN_NAME_DISTANCE, 0);
                    statusValues.put(AppContract.UserStatusEntry.COLUMN_NAME_STATUS_REMARK, "");
                    context.getContentResolver().insert(AppContract.UserStatusEntry.CONTENT_URI, statusValues);
                }
            }

            ChatMessageManager chatMessageManager = new ChatMessageManager(context);
            Uri messageUri = null;
            if(!TextUtils.isEmpty(contentType)) {
                ContentValues values = new ContentValues();
                if(contentType.equals("Text")) {
                    messageUri = chatMessageManager.insert(from, to, owner, MessageConstant.MESSAGE_TYPE_BULK,
                            contentType, content, datetime, status);
                    values.put(AppContract.RecentContactEntry.COLUMN_NAME_CONTENT, "(群)" + content);
                } else if(contentType.equals("Picture")){
                    String fileExtension = "jpg";
                    String filePath = MediaFileUtils.processReceiveFile(context, content,
                            MessageConstant.MESSAGE_FILE_TYPE_IMG, fileExtension);
                    messageUri = chatMessageManager.insert(from, to, owner, MessageConstant.MESSAGE_TYPE_BULK,
                            contentType, filePath, datetime, status);
                    values.put(AppContract.RecentContactEntry.COLUMN_NAME_CONTENT, "(群)" + context.getResources().getString(R.string.content_type_pic));
                } else if(contentType.equals("Audio")) {
                    String fileExtension = "3gp";
                    String filePath = MediaFileUtils.processReceiveFile(context, content,
                            MessageConstant.MESSAGE_FILE_TYPE_AUDIO, fileExtension);
                    messageUri = chatMessageManager.insert(from, to, owner, MessageConstant.MESSAGE_TYPE_BULK,
                            contentType, filePath, datetime, status);
                    values.put(AppContract.RecentContactEntry.COLUMN_NAME_CONTENT, "(群)" + context.getResources().getString(R.string.content_type_voice));
                }

                // 最近消息记录
                if(messageUri != null) {

                    if (!direction) {
                        String selection =
                                AppContract.RecentContactEntry.COLUMN_NAME_BUDDY + " = ? " +
                                        "AND " +
                                        AppContract.RecentContactEntry.COLUMN_NAME_OWNER + " = ?";
                        String[] selectionArgs = new String[]{from, owner};

                        Cursor newCursor = context.getContentResolver().query(AppContract.RecentContactEntry.CONTENT_URI,
                                null, selection, selectionArgs, null);

                        if (newCursor != null && newCursor.getCount() > 0 && newCursor.moveToFirst()) {
                            int rowId = newCursor.getInt(newCursor.getColumnIndex(AppContract.RecentContactEntry._ID));
                            int count = newCursor.getInt(newCursor.getColumnIndex(AppContract.RecentContactEntry.COLUMN_NAME_COUNT));
                            Uri appendUri = Uri.withAppendedPath(AppContract.RecentContactEntry.CONTENT_URI, Integer.toString(rowId));

                            values.put(AppContract.RecentContactEntry.COLUMN_NAME_UPDATE_TIME, datetime);
                            count = count + 1;
                            values.put(AppContract.RecentContactEntry.COLUMN_NAME_COUNT, count);

                            context.getContentResolver().update(appendUri, values, null, null);

                            newCursor.close();
                        } else {
                            values.put(AppContract.RecentContactEntry.COLUMN_NAME_BUDDY, from);
                            values.put(AppContract.RecentContactEntry.COLUMN_NAME_UPDATE_TIME, datetime);
                            values.put(AppContract.RecentContactEntry.COLUMN_NAME_OWNER, owner);
                            values.put(AppContract.RecentContactEntry.COLUMN_NAME_COUNT, 0);
                            context.getContentResolver().insert(AppContract.RecentContactEntry.CONTENT_URI, values);
                        }
                    }

                    if (!direction) {
                        Bundle args = new Bundle();
                        args.putParcelable("messageUri", messageUri);
                        args.putString("fromUid", from);

                        Intent intent = new Intent();
                        intent.setAction(GlobalApplication.ACTION_INTENT_BULK_MESSAGE_INCOMING);
                        intent.putExtra("message", args);
                        context.sendBroadcast(intent);
                    } else {
                        Bundle args = new Bundle();
                        args.putParcelable("messageUri", messageUri);
                        args.putString("fromUid", from);

                        Intent intent = new Intent();
                        intent.setAction(GlobalApplication.ACTION_INTENT_BULK_MESSAGE_SEND);
                        intent.putExtra("message", args);
                        context.sendBroadcast(intent);
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.d("ChatMessageProcessor", "processBulkMessage() parse json error");
        }

    }


    private void processShieldMsg(String message) {
        JSONObject jsonObject;
        try {
            jsonObject = new JSONObject(message);
            String owner = jsonObject.getString("UserId");
            String shield = jsonObject.getString("ShieldedObjectId");

            String selectionStatus =
                    AppContract.UserStatusEntry.COLUMN_NAME_ENTRY_UID + " = ?" +
                            " AND " +
                            AppContract.UserStatusEntry.COLUMN_NAME_ENTRY_OWNER + " = ?";
            String[] selectionArgsStatus = new String[] {shield,owner};

            ContentValues statusValues = new ContentValues();
            statusValues.put(AppContract.UserStatusEntry.COLUMN_NAME_STATUS_SHIELD, User.USER_STATUS_SHIELD_LIST_IN);

            context.getContentResolver().update(AppContract.UserStatusEntry.CONTENT_URI,
                    statusValues,
                    selectionStatus,
                    selectionArgsStatus);

            String selection =
                    AppContract.ShieldListView.COLUMN_NAME_UID + " = ? " +
                            "AND " +
                            AppContract.ShieldListView.COLUMN_NAME_OWNER + " = ?";

            String[] selectionArgs = new String[]{shield,owner};

            Cursor newCursor = context.getContentResolver().query(AppContract.ShieldListView.CONTENT_URI,
                    null, selection, selectionArgs, null);

            if(newCursor != null && newCursor.getCount() > 0) {
                Intent intent = new Intent();
                intent.setAction(GlobalApplication.ACTION_INTENT_SHIELD_LIST_BEFORE);
                intent.putExtra("uid", shield);
                context.sendBroadcast(intent);
                return;
            } else {
                ContentValues values = new ContentValues();
                values.put(AppContract.ShieldEntry.COLUMN_NAME_SHIELD, shield);
                values.put(AppContract.ShieldEntry.COLUMN_NAME_OWNER, owner);
                context.getContentResolver().insert(AppContract.ShieldEntry.CONTENT_URI, values);

                Intent intent = new Intent();
                intent.setAction(GlobalApplication.ACTION_INTENT_SHIELD_LIST_ADD);
                intent.putExtra("uid", shield);
                context.sendBroadcast(intent);
            }

        } catch (JSONException e) {
            e.printStackTrace();
            Log.d("ChatMessageProcessor", "processShieldMsg() parse json error");
        }
    }

    private void processShieldListMsg(String message) {
        JSONArray jsonArray;
        try {
            jsonArray = new JSONArray(message);
            for(int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String owner = jsonObject.getString("UserId");
                String shield = jsonObject.getString("ShieldedObjectId");
                String selection =
                        AppContract.ShieldListView.COLUMN_NAME_UID + " = ? " +
                                "AND " +
                                AppContract.ShieldListView.COLUMN_NAME_OWNER + " = ?";

                String[] selectionArgs = new String[]{shield,owner};

                Cursor newCursor = context.getContentResolver().query(AppContract.ShieldListView.CONTENT_URI,
                        null, selection, selectionArgs, null);

                if(newCursor != null && newCursor.getCount() > 0) {
                    continue;
                } else {
                    ContentValues values = new ContentValues();
                    values.put(AppContract.ShieldEntry.COLUMN_NAME_SHIELD,shield);
                    values.put(AppContract.ShieldEntry.COLUMN_NAME_OWNER,owner);
                    context.getContentResolver().insert(AppContract.ShieldEntry.CONTENT_URI,values);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.d("ChatMessageProcessor", "processShieldListMsg() parse json error");
        }
    }

    private void processUnShieldMsg(String message) {
        JSONObject jsonObject;
        try {
            jsonObject = new JSONObject(message);
            String owner = jsonObject.getString("UserId");
            String unshield = jsonObject.getString("ShieldedObjectId");

            String selectionStatus =
                    AppContract.UserStatusEntry.COLUMN_NAME_ENTRY_UID + " = ?" +
                    " AND " +
                    AppContract.UserStatusEntry.COLUMN_NAME_ENTRY_OWNER + " = ?";
            String[] selectionArgsStatus = new String[] {unshield,owner};

            ContentValues statusValues = new ContentValues();
            statusValues.put(AppContract.UserStatusEntry.COLUMN_NAME_STATUS_SHIELD, User.USER_STATUS_SHIELD_LIST_OUT);

            context.getContentResolver().update(AppContract.UserStatusEntry.CONTENT_URI,
                    statusValues,
                    selectionStatus,
                    selectionArgsStatus);


//            String selection =
//                    AppContract.ShieldEntry.COLUMN_NAME_SHIELD + " = ? " +
//                            "AND " +
//                            AppContract.ShieldEntry.COLUMN_NAME_OWNER + " = ?";
//
//            String[] selectionArgs = new String[]{unshield,owner};
//            context.getContentResolver().delete(AppContract.ShieldEntry.CONTENT_URI,selection,selectionArgs);

            Intent intent = new Intent();
            intent.setAction(GlobalApplication.ACTION_INTENT_SHIELD_LIST_UPDATE);
            context.sendBroadcast(intent);

        } catch (JSONException e) {
            e.printStackTrace();
            Log.d("ChatMessageProcessor", "processUnShieldMsg() parse json error");
        }
    }

    private void processRemainingMessage(String message) {
        JSONObject jsonObject;
        try {
            jsonObject = new JSONObject(message);
            int exp = jsonObject.getInt("Integration");
            SharedPreferencesHelper helper = SharedPreferencesHelper.newInstance(context);
            helper.putInt(AppConstants.KEY_SYS_CURRENT_USER_EXP, exp);
        } catch (JSONException e) {
            e.printStackTrace();
            Log.d("ChatMessageProcessor", "processRemainingMessage() parse json error");
        }
    }


    private class ProcessMessageTask extends AsyncTask<String,String,String> {
        @Override
        protected String doInBackground(String... params) {
                String messageType = params[0];
                String message = params[1];
            if(messageType.equals("OnlineList")) {
                processOnlineList(message);
            } else if(messageType.equals("OnlineMsg")) {
                processOnlineMessage(message, false);
            } else if(messageType.equals("OfflineMsgShort")) {
                processOfflineMsgShort(message);
            } else if(messageType.equals("OfflineMsg")) {
                processOfflineMessageList(message);
            } else if(messageType.equals("Shield")) {
                processShieldMsg(message);
            } else if(messageType.equals("Shields")) {
                processShieldListMsg(message);
            } else if(messageType.equals("UnShield")) {
                processUnShieldMsg(message);
            } else if(messageType.equals("BulkMssaging")) {
                processBulkMessage(message, false);
            } else if (messageType.equals("Remaining")) {
                processRemainingMessage(message);
            }

            return "ok";
        }
    }

    private class ProcessSendMessageTask extends AsyncTask<String,String,String> {
        @Override
        protected String doInBackground(String... params) {
            String messageType = params[0];
            String message = params[1];
            if(messageType.equals("OnlineMsg")) {
                processOnlineMessage(message,true);
            } else if(messageType.equals("BulkMssaging")) {
                processBulkMessage(message,true);
            }

            return "ok";
        }
    }

}
