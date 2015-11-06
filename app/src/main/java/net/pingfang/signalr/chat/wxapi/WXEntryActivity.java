package net.pingfang.signalr.chat.wxapi;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.tencent.mm.sdk.constants.ConstantsAPI;
import com.tencent.mm.sdk.modelbase.BaseReq;
import com.tencent.mm.sdk.modelbase.BaseResp;
import com.tencent.mm.sdk.modelmsg.SendAuth;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

import net.pingfang.signalr.chat.R;
import net.pingfang.signalr.chat.constant.wechat.WxConstants;

/**
 * Created by gongguopei87@gmail.com on 2015/11/6.
 */
public class WXEntryActivity extends Activity implements IWXAPIEventHandler {

    public static final String TAG = WXEntryActivity.class.getSimpleName();

    private IWXAPI api;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 通过WXAPIFactory工厂，获取IWXAPI的实例
        api = WXAPIFactory.createWXAPI(this, WxConstants.APP_ID, true);
        api.handleIntent(getIntent(), this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        setIntent(intent);
        api.handleIntent(intent, this);
        finish();
    }

    // 微信发送请求到第三方应用时，会回调到该方法
    @Override
    public void onReq(BaseReq baseReq) {
        switch (baseReq.getType()) {
            case ConstantsAPI.COMMAND_GETMESSAGE_FROM_WX:
                Toast.makeText(getApplicationContext(), "get message from wx, processed here", Toast.LENGTH_LONG).show();
                break;

            case ConstantsAPI.COMMAND_SHOWMESSAGE_FROM_WX:
                Toast.makeText(getApplicationContext(), "show message from wx, processed here", Toast.LENGTH_LONG).show();
                break;

            default:
                break;
        }
    }

    // 第三方应用发送到微信的请求处理后的响应结果，会回调到该方法
    @Override
    public void onResp(BaseResp baseResp) {
        int result = 0;
        switch (baseResp.errCode) {
            case BaseResp.ErrCode.ERR_OK:
                result = R.string.errcode_success;
                break;
            case BaseResp.ErrCode.ERR_USER_CANCEL:
                result = R.string.errcode_cancel;
                break;
            case BaseResp.ErrCode.ERR_AUTH_DENIED:
                result = R.string.errcode_deny;
                break;
            default:
                result = R.string.errcode_unknown;
                break;
        }

        Log.d(TAG, "baseResp.getType() == " + baseResp.getType());
        switch (baseResp.getType()) {
            case ConstantsAPI.COMMAND_SENDAUTH:
                Toast.makeText(getApplicationContext(), "get auth resp, processed here", Toast.LENGTH_LONG).show();
                SendAuth.Resp resp = (SendAuth.Resp) baseResp;
                int errorCode = resp.errCode;
                if(errorCode == BaseResp.ErrCode.ERR_OK) {
                    String accessCode = resp.code;
//                    getWxAccessToken(accessCode);
                } else {
                    if(errorCode == BaseResp.ErrCode.ERR_AUTH_DENIED) {

                    } else if(errorCode == BaseResp.ErrCode.ERR_USER_CANCEL){
                        Toast.makeText(getApplicationContext(),R.string.weibosdk_demo_toast_auth_canceled,Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getApplicationContext(),R.string.weibosdk_demo_toast_auth_failed,Toast.LENGTH_SHORT).show();
                    }
                }
                break;

            case ConstantsAPI.COMMAND_SENDMESSAGE_TO_WX:
                // 处理微信主程序返回的SendMessageToWX.Resp
                break;

            default:
                break;
        }

        Toast.makeText(this, result, Toast.LENGTH_LONG).show();
    }
}
