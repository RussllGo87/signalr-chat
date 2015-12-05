package net.pingfang.signalr.chat.service;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import net.pingfang.signalr.chat.message.ChatMessageListener;
import net.pingfang.signalr.chat.message.ChatMessageProcessor;
import net.pingfang.signalr.chat.message.OnChatServiceConnectionChanged;
import net.pingfang.signalr.chat.util.GlobalApplication;

import microsoft.aspnet.signalr.client.ConnectionState;
import microsoft.aspnet.signalr.client.LogLevel;
import microsoft.aspnet.signalr.client.Logger;
import microsoft.aspnet.signalr.client.Platform;
import microsoft.aspnet.signalr.client.SignalRFuture;
import microsoft.aspnet.signalr.client.http.android.AndroidPlatformComponent;
import microsoft.aspnet.signalr.client.hubs.HubConnection;
import microsoft.aspnet.signalr.client.hubs.HubProxy;
import microsoft.aspnet.signalr.client.hubs.SubscriptionHandler2;

public class ChatService extends Service {

    public static final String TAG = ChatService.class.getSimpleName();

    public static final String FLAG_SERVICE_CMD = "FLAG_SERVICE_CMD";

    public static final int FLAF_INIT_CONNECTION = 0x01;
    public static final String FLAG_INIT_CONNECTION_QS = "FLAG_INIT_CONNECTION";

    public static final String URL = GlobalApplication.URL_COMMUNICATION_API_HOST + "/signalr/hubs/";
    // Binder given to clients
    private final IBinder mBinder = new ChatBinder();
    HubConnection connection;
    HubProxy hub;
    SignalRFuture<Void> awaitConnection;
    ChatMessageListener messageListener;
    private long lastSendTime = 0L;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Bundle args = intent.getBundleExtra("args");
        int requestFlag = args.getInt(FLAG_SERVICE_CMD, FLAF_INIT_CONNECTION);
        switch(requestFlag) {
            case FLAF_INIT_CONNECTION:
                String qs = args.getString(FLAG_INIT_CONNECTION_QS);
                initConnection(qs);
                return START_REDELIVER_INTENT;
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        destroy();
    }

    private void initConnection(String qs) {
        Platform.loadPlatformComponent(new AndroidPlatformComponent());

        connection = new HubConnection(URL,qs, false, new Logger() {
            @Override
            public void log(String s, LogLevel logLevel) {
                if (logLevel == LogLevel.Information) {
                    Log.i(TAG, "HubConnection LogLevel.Information == " + s);
                }

                if (logLevel == LogLevel.Verbose) {
                    Log.d(TAG, "HubConnection LogLevel.Verbose == " + s);
                }

                if (logLevel == LogLevel.Critical) {
                    Log.e(TAG, "HubConnection LogLevel.Critical == " + s);
                }
            }
        });

        connection.stateChanged(new OnChatServiceConnectionChanged() {
            @Override
            public void stateChanged(ConnectionState oldState, ConnectionState newState) {
                if (oldState == ConnectionState.Disconnected && newState == ConnectionState.Connecting) {
                    Log.d(TAG, "通信端正在连接");
                }

                if (oldState == ConnectionState.Connecting && newState == ConnectionState.Connected) {
                    Log.d(TAG, "通信端连接成功");
                }

                if (oldState == ConnectionState.Connected && newState == ConnectionState.Disconnected) {
                    Log.d(TAG, "通信端连接断开");
                }

                if (oldState == ConnectionState.Disconnected && newState == ConnectionState.Reconnecting) {
                    Log.d(TAG, "正在尝试重新连接");
                }

                if (oldState == ConnectionState.Reconnecting && newState == ConnectionState.Connected) {
                    Log.d(TAG, "重新连接成功");
                }

                if (oldState == ConnectionState.Reconnecting && newState == ConnectionState.Disconnected) {
                    Log.d(TAG, "重新连接失败");
                }
            }
        });

        messageListener = new ChatMessageProcessor(getApplicationContext());

        hub = connection.createHubProxy("communicationHub");
        hub.on("broadcastMessage",
                new SubscriptionHandler2<String, String>() {
                    @Override
                    public void run(String msgType, String msg) {
                        Log.d(TAG, "msgType == " + msgType);
                        Log.d(TAG, "msg == " + msg);

                        messageListener.onMessageReceive(msgType, msg);
                    }
                },
                String.class, String.class);

        awaitConnection = connection.start();

    }

    public void sendMessage(String messageType, String message) {
        long currentTime = System.currentTimeMillis();
        long delta = currentTime - lastSendTime;
        if(delta > 300) {
            new MessageSendTask().execute(messageType, message);
            lastSendTime = currentTime;
        }

    }

    public void destroy() {

        if(messageListener != null) {
            messageListener.onMessageReceive("exitApp","");
        }

        if(connection != null && connection.getState() == ConnectionState.Connected) {
            connection.stop();
        }
    }

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class ChatBinder extends Binder {
        public ChatService getService() {
            return ChatService.this;
        }
    }

    private class MessageSendTask extends AsyncTask<String,String,String> {
        @Override
        protected String doInBackground(String... params) {
            Log.d(TAG,"send msgType == " + params[0]);
            Log.d(TAG,"send msg == " + params[1]);
            hub.invoke("send",params[0],params[1]);
            return "ok";
        }
    }
}
