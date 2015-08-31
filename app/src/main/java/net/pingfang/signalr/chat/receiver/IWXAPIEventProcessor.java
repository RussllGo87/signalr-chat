package net.pingfang.signalr.chat.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.tencent.mm.sdk.constants.ConstantsAPI;
import com.tencent.mm.sdk.modelbase.BaseReq;
import com.tencent.mm.sdk.modelbase.BaseResp;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

import net.pingfang.signalr.chat.util.GlobalApplication;

/**
 * Created by gongguopei87@gmail.com on 2015/8/25.
 */
public class IWXAPIEventProcessor extends BroadcastReceiver implements IWXAPIEventHandler {

    Context context;

    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;
        final IWXAPI api = WXAPIFactory.createWXAPI(context,GlobalApplication.APP_ID,true);
        if(api.handleIntent(intent,this)) {
            return;
        }
    }

    @Override
    public void onReq(BaseReq baseReq) {
        switch (baseReq.getType()) {
            case ConstantsAPI.COMMAND_GETMESSAGE_FROM_WX:
                Toast.makeText(context, "get message from wx, processed here", Toast.LENGTH_LONG).show();
                break;

            case ConstantsAPI.COMMAND_SHOWMESSAGE_FROM_WX:
                Toast.makeText(context, "show message from wx, processed here", Toast.LENGTH_LONG).show();
                break;

            default:
                break;
        }
    }

    @Override
    public void onResp(BaseResp baseResp) {
        switch (baseResp.getType()) {
            case ConstantsAPI.COMMAND_SENDAUTH:
                Toast.makeText(context, "get auth resp, processed here", Toast.LENGTH_LONG).show();
                break;

            case ConstantsAPI.COMMAND_SENDMESSAGE_TO_WX:
                // 处理微信主程序返回的SendMessageToWX.Resp
                break;

            default:
                break;
        }
    }
}
