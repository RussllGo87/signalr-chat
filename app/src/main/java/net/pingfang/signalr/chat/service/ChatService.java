package net.pingfang.signalr.chat.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import net.pingfang.signalr.chat.message.ChatMessageListener;
import net.pingfang.signalr.chat.message.ChatMessageProcessor;
import net.pingfang.signalr.chat.message.OnChatServiceConnectionChanged;
import net.pingfang.signalr.chat.net.NetUtil;
import net.pingfang.signalr.chat.util.GlobalApplication;
import net.pingfang.signalr.chat.util.SharedPreferencesHelper;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import microsoft.aspnet.signalr.client.ConnectionState;
import microsoft.aspnet.signalr.client.LogLevel;
import microsoft.aspnet.signalr.client.Logger;
import microsoft.aspnet.signalr.client.Platform;
import microsoft.aspnet.signalr.client.SignalRFuture;
import microsoft.aspnet.signalr.client.http.android.AndroidPlatformComponent;
import microsoft.aspnet.signalr.client.hubs.HubConnection;
import microsoft.aspnet.signalr.client.hubs.HubProxy;
import microsoft.aspnet.signalr.client.hubs.SubscriptionHandler2;
import microsoft.aspnet.signalr.client.transport.AutomaticTransport;

public class ChatService extends Service {

    public static final String TAG = ChatService.class.getSimpleName();

    public static final String INTENT_ACTION_CONNECTION_STATUS = "INTENT_ACTION_CONNECTION_STATUS";
    public static final String KEY_CONNECTION_STATUS = "KEY_CONNECTION_STATUS";

    public static final String FLAG_SERVICE_CMD = "FLAG_SERVICE_CMD";

    public static final int FLAG_INIT_CONNECTION = 0x01;
    public static final String FLAG_INIT_CONNECTION_QS = "FLAG_INIT_CONNECTION";

    public static final String URL = GlobalApplication.URL_COMMUNICATION_API_HOST + "/signalr/hubs/";
    // Binder given to clients
    private final IBinder mBinder = new ChatBinder();
    HubConnection connection;
    HubProxy hub;
    SignalRFuture<Void> awaitConnection;
    ChatMessageListener messageListener;
    boolean isReconnectWhenDisconnect = false;
    String qs;
    private long lastSendTime = 0L;
    Handler mHandler = new Handler(Looper.getMainLooper());

    SharedPreferencesHelper sharedPreferencesHelper;

    private BroadcastReceiver mReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            String action = intent.getAction();
            if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                Timer timer = new Timer();
                timer.schedule(new QunXTask(),new Date());
             }
        }
    };

    class QunXTask extends TimerTask{

        @Override
        public void run() {
            String message = "";
            if (NetUtil.isConnected(getApplicationContext())) {
                Log.d(TAG, "网络连接成功");
                message = "网络连接成功";
                isReconnectWhenDisconnect = true;
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        initConnection();
                    }
                }, 500);
            } else {
                Log.d(TAG, "网络连接已经断开");
                message = "网络连接已经断开";
                isReconnectWhenDisconnect = false;
            }

            sharedPreferencesHelper.putStringValue(ChatService.KEY_CONNECTION_STATUS,message);
            Intent intent = new Intent();
            intent.setAction(INTENT_ACTION_CONNECTION_STATUS);
            Bundle args = new Bundle();
            args.putString("message",message);
            intent.putExtra("args",args);
            sendBroadcast(intent);
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();

        sharedPreferencesHelper = SharedPreferencesHelper.newInstance(getApplicationContext());
        IntentFilter mFilter = new IntentFilter();
        mFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(mReceiver, mFilter);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Bundle args = intent.getBundleExtra("args");
        int requestFlag = args.getInt(FLAG_SERVICE_CMD, FLAG_INIT_CONNECTION);
        switch(requestFlag) {
            case FLAG_INIT_CONNECTION:
                qs = args.getString(FLAG_INIT_CONNECTION_QS);
                initConnection();
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

        unregisterReceiver(mReceiver); // 删除广播
        destroy();
    }

    private void initConnection() {
        Platform.loadPlatformComponent(new AndroidPlatformComponent());

        connection = new HubConnection(URL, qs, false, new Logger() {
            @Override
            public void log(String s, LogLevel logLevel) {
                if (logLevel == LogLevel.Information) {
                    Log.i(TAG, s);
                }

                if (logLevel == LogLevel.Verbose) {
                    Log.v(TAG, s);
                }

                if (logLevel == LogLevel.Critical) {
                    Log.d(TAG, s);
                }

            }
        });

        connection.stateChanged(new OnChatServiceConnectionChanged() {
            @Override
            public void stateChanged(ConnectionState oldState, ConnectionState newState) {

                String message = "";

                if (oldState == ConnectionState.Disconnected && newState == ConnectionState.Connecting) {
                    Log.d(TAG, "聊天服务正在连接");
                    message = "聊天服务正在连接";
                }

                if (oldState == ConnectionState.Connecting && newState == ConnectionState.Connected) {
                    Log.d(TAG, "聊天服务连接成功");
                    message = "聊天服务连接成功";
                }

                if (oldState == ConnectionState.Connected && newState == ConnectionState.Disconnected) {
                    Log.d(TAG, "聊天服务连接断开");
                    message = "聊天服务连接断开";
                    if (isReconnectWhenDisconnect) {
                        mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                awaitConnection = connection.start(new AutomaticTransport());
                            }
                        }, 500);
                    }
                }

                if (oldState == ConnectionState.Disconnected && newState == ConnectionState.Reconnecting) {
                    Log.d(TAG, "正在重新连接聊天服务");
                    message = "正在重新连接聊天服务";
                }

                if (oldState == ConnectionState.Reconnecting && newState == ConnectionState.Connected) {
                    Log.d(TAG, "重新连接聊天服务成功");
                    message = "重新连接聊天服务成功";
                }

                if (oldState == ConnectionState.Reconnecting && newState == ConnectionState.Disconnected) {
                    Log.d(TAG, "重新连接聊天服务失败");
                    message = "重新连接聊天服务失败";
                    if (isReconnectWhenDisconnect) {
                        mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                awaitConnection = connection.start(new AutomaticTransport());
                            }
                        }, 10000);
                    }
                }

                sharedPreferencesHelper.putStringValue(ChatService.KEY_CONNECTION_STATUS,message);

                Intent intent = new Intent();
                intent.setAction(INTENT_ACTION_CONNECTION_STATUS);
                Bundle args = new Bundle();
                args.putString("message",message);
                intent.putExtra("args",args);
                sendBroadcast(intent);
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

        isReconnectWhenDisconnect = true;
        awaitConnection = connection.start(new AutomaticTransport());

    }


    public void sendMessage(String messageType, String message) {

        if (NetUtil.isConnected(getApplicationContext()) && connection.getState() == ConnectionState.Disconnected) {
            Toast.makeText(getApplicationContext(), "通信连接已断开，正在尝试重新连接", Toast.LENGTH_LONG).show();
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    initConnection();
                }
            }, 1000);
            return;
        }

        long currentTime = System.currentTimeMillis();
        long delta = currentTime - lastSendTime;
        if(delta > 300) {
            MessageSendTask sendTask = new MessageSendTask();
            sendTask.execute(messageType, message);
            lastSendTime = currentTime;
        }

    }

    private void setIsReconnectWhenDisconnect(boolean isReconnectWhenDisconnect) {
        this.isReconnectWhenDisconnect = isReconnectWhenDisconnect;
    }

    public void destroy() {
        if(connection != null && connection.getState() == ConnectionState.Connected) {
            setIsReconnectWhenDisconnect(false);
            connection.disconnect();
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

    class MessageSendTask extends AsyncTask<String,String,String> {
        @Override
        protected String doInBackground(String... params) {
            Log.d(TAG,"send msgType == " + params[0]);
            Log.d(TAG,"send msg == " + params[1]);
            hub.invoke("send", params[0], params[1]);
            return "ok";
        }
    }
}
