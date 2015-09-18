package net.pingfang.signalr.chat.net;

import android.util.Log;

import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;

/**
 * Created by gongguopei87@gmail.com on 2015/9/18.
 */
public class HttpBaseCallback implements Callback{

    @Override
    public void onFailure(Request request, IOException e) {
        Log.d("HttpBaseCallback",e.getMessage());
    }

    @Override
    public void onResponse(Response response) throws IOException {

    }
}
