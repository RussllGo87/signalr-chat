package net.pingfang.signalr.chat.net;

import com.squareup.okhttp.Request;

/**
 * Created by Guest on 2015/11/9.
 */
public class ResultCallbackImpl<String> extends ResultCallback<String>{

    public ResultCallbackImpl() {
        super();
    }

    @Override
    public void onError(Request request, Exception e) {

    }

    @Override
    public void onResponse(String response) {

    }
}
