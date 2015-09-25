package net.pingfang.signalr.chat.chat;

import android.content.Context;
import android.os.Handler;

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
        if(messageType.equals("OnlineList")) {
            processOnlineList(message);
        } else if(messageType.equals("Online")){
            updaeUserStatus(message,0);
        } else if(messageType.equals("Offline")) {
            updaeUserStatus(message, 1);
        } else if(messageType.equals("OfflineMsgShort")) {
            processOfflineMsgShort(message);
        } else if(messageType.equals("OfflineMsg")) {
            processOfflineMsgShort(message);
        } else if(messageType.equals("OnlineMsg")) {

        } else if(messageType.equals("BulkMssaging")) {

        } else if(messageType.equals("RemainingTimes")) {

        } else if(messageType.equals("")) {

        }
    }


    /**
     * 实现获取用户在线列表
     * @param message
     */
    private void processOnlineList(String message) {

    }

    private void updaeUserStatus(String message,int status) {

    }

    private void processOfflineMsgShort(String message) {

    }


}
