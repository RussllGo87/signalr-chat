package net.pingfang.signalr.chat.service;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import microsoft.aspnet.signalr.client.ConnectionState;
import microsoft.aspnet.signalr.client.LogLevel;
import microsoft.aspnet.signalr.client.Logger;
import microsoft.aspnet.signalr.client.Platform;
import microsoft.aspnet.signalr.client.SignalRFuture;
import microsoft.aspnet.signalr.client.http.android.AndroidPlatformComponent;
import microsoft.aspnet.signalr.client.hubs.HubConnection;
import microsoft.aspnet.signalr.client.hubs.HubProxy;
import microsoft.aspnet.signalr.client.hubs.SubscriptionHandler2;

public class NewChatService extends Service {

    public static final String TAG = NewChatService.class.getSimpleName();

    public static final String FLAG_SERVICE_CMD = "FLAG_SERVICE_CMD";

    public static final int FLAF_INIT_CONNECTION = 0x01;
    public static final String FLAG_INIT_CONNECTION_QS = "FLAG_INIT_CONNECTION";

    public static final String URL = "http://192.168.0.254/signalr/hubs/";
    HubConnection connection;
    HubProxy hub;
    SignalRFuture<Void> awaitConnection;

    // Binder given to clients
    private final IBinder mBinder = new ChatBinder();

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class ChatBinder extends Binder {
        public NewChatService getService() {
            return NewChatService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int requestFlag = intent.getIntExtra(FLAG_SERVICE_CMD,FLAF_INIT_CONNECTION);
        switch(requestFlag) {
            case FLAF_INIT_CONNECTION:
                String qs = intent.getStringExtra(FLAG_INIT_CONNECTION_QS);
                initConnection(qs);
                break;
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

        Log.d(TAG, "qs == " + qs);

        connection = new HubConnection(URL,qs, false, new Logger() {
            @Override
            public void log(String s, LogLevel logLevel) {

            }
        });
        String newQs = connection.getQueryString();
        Log.d(TAG,"newQs == " + newQs);

        hub = connection.createHubProxy("communicationHub");
        hub.on("broadcastMessage",
                new SubscriptionHandler2<String,String>() {
                    @Override
                    public void run(String msgType, String msg) {
                        Log.d(TAG,"msgType == " + msgType);
                        Log.d(TAG, "msg == " + msg);
                    }
                },
                String.class,String.class);


        awaitConnection = connection.start();
    }

    public void sendMessage(String message) {
        new MessageSendTask().execute(message);
    }

    public void destroy() {
        if(connection != null && connection.getState() == ConnectionState.Connected) {
            connection.stop();
        }
    }

    private class MessageSendTask extends AsyncTask<String,String,String> {
        @Override
        protected String doInBackground(String... params) {
            hub.invoke("send","directory",params[0]);
            return "ok";
        }
    }
}
