package net.pingfang.signalr.chat.message;

/**
 * Created by gongguopei87@gmail.com on 2015/9/25.
 */
public interface ChatMessageListener {

    public void onMessageReceive(String messageType, String message);
}
