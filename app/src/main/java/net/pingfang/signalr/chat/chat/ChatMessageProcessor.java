package net.pingfang.signalr.chat.chat;

import android.content.ContentValues;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.text.TextUtils;

import net.pingfang.signalr.chat.database.AppContract;
import net.pingfang.signalr.chat.database.UserManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by gongguopei87@gmail.com on 2015/9/25.
 */
public class ChatMessageProcessor implements ChatMessageListener {

    Context context;
    Handler handler;

    public ChatMessageProcessor(Context context, Handler handler) {
        this.context = context;
        this.handler = handler;
    }

    @Override
    public void onMessageReceive(String messageType, String message) {
        new ProcessMessageTask().execute(messageType, message);
//        if(messageType.equals("OnlineList")) {
//            new ProcessOnlineListTask().execute(message);
//        } else if(messageType.equals("Online")){
//            updaeUserStatus(message,0);
//        } else if(messageType.equals("Offline")) {
//            updaeUserStatus(message, 1);
//        } else if(messageType.equals("OfflineMsgShort")) {
//            processOfflineMsgShort(message);
//        } else if(messageType.equals("OfflineMsg")) {
//            processOfflineMsgShort(message);
//        } else if(messageType.equals("OnlineMsg")) {
//
//        } else if(messageType.equals("BulkMssaging")) {
//
//        } else if(messageType.equals("RemainingTimes")) {
//
//        } else if(messageType.equals("")) {
//
//        }
    }


    /**
     * 实现获取用户在线列表
     * @param message
     */
    private void processOnlineList(String message) {

        String selection = AppContract.UserEntry.COLUMN_NAME_ENTRY_UID + " = ?";

        JSONArray jsonArray;

        try {
            jsonArray = new JSONArray(message);
            for(int i = 0; i < jsonArray.length(); i++) {
                JSONObject object = jsonArray.getJSONObject(i);
                String uid = object.getString("UserId");
                ContentValues values = new ContentValues();
                values.put(AppContract.UserEntry.COLUMN_NAME_STATUS,1);
                context.getContentResolver().update(AppContract.UserEntry.CONTENT_URI,
                        values,
                        selection,
                        new String[]{uid});
            }


        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void updateUserStatus(String message,int status) {
        String selection = AppContract.UserEntry.COLUMN_NAME_ENTRY_UID + " = ?";
        JSONObject object;
        ContentValues values = new ContentValues();
        try {
            object = new JSONObject(message);
            String uid = object.getString("UserId");
            if(uid != null && !TextUtils.isEmpty(uid) && !uid.equals("0")) {
                UserManager userManager = new UserManager(context);
                boolean isExist = userManager.isExist(uid);
                values.put(AppContract.UserEntry.COLUMN_NAME_STATUS,status);
                if(isExist) {
                    context.getContentResolver().update(AppContract.UserEntry.CONTENT_URI,
                            values,
                            selection,
                            new String[]{uid});
                } else {
                    if(status == 1) {
                        String nickname = object.getString("NickName");
                        String portrait = object.getString("HeadPortrait");
                        values.put(AppContract.UserEntry.COLUMN_NAME_ENTRY_UID,uid);
                        values.put(AppContract.UserEntry.COLUMN_NAME_NICK_NAME,nickname);

                        if(portrait != null && !TextUtils.isEmpty(portrait) && !"null".equals(portrait)) {
                            values.put(AppContract.UserEntry.COLUMN_NAME_PORTRAIT,portrait);
                        } else {
                            values.put(AppContract.UserEntry.COLUMN_NAME_PORTRAIT, "");
                        }
                        context.getContentResolver().insert(AppContract.UserEntry.CONTENT_URI, values);
                    }


                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void exitApp() {
        ContentValues values = new ContentValues();
        values.put(AppContract.UserEntry.COLUMN_NAME_STATUS,0);
        context.getContentResolver().update(AppContract.UserEntry.CONTENT_URI, values, null, null);
    }

    private void processOfflineMsgShort(String message) {

    }


    private class ProcessMessageTask extends AsyncTask<String,String,String> {
        @Override
        protected String doInBackground(String... params) {
            String messageType = params[0];
            String message = params[1];
            if(messageType.equals("OnlineList")) {
                processOnlineList(message);
            } else if(messageType.equals("Online")){
                updateUserStatus(message, 1);
            } else if(messageType.equals("Offline")) {
                updateUserStatus(message, 0);
            } else if(messageType.equals("exitApp")) {
                exitApp();
            }

            return "ok";
        }
    }

}
