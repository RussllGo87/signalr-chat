package net.pingfang.signalr.chat.wxapi;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
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
import net.pingfang.signalr.chat.activity.LoginActivity;
import net.pingfang.signalr.chat.constant.wechat.WxConstants;

/**
 * Created by gongguopei87@gmail.com on 2015/11/6.
 */
public class WXEntryActivity extends AppCompatActivity implements IWXAPIEventHandler {

    public static final String TAG = WXEntryActivity.class.getSimpleName();

    private IWXAPI api;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 通过WXAPIFactory工厂，获取IWXAPI的实例
        api = WXAPIFactory.createWXAPI(this, WxConstants.APP_ID, true);
        api.registerApp(WxConstants.APP_ID);
        api.handleIntent(getIntent(), this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        setIntent(intent);
        api.handleIntent(intent, this);
        finish();
    }

    /**
     * 微信发送请求到第三方应用时，会回调到该方法
     * @param baseReq
     */
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

    /**
     * 第三方应用发送到微信的请求处理后的响应结果，会回调到该方法
     * @param baseResp
     **/
    @Override
    public void onResp(BaseResp baseResp) {
        int respType = baseResp.getType();
        Log.d(TAG, "baseResp.getType() == " + baseResp.getType());
        switch (baseResp.errCode) {
            case BaseResp.ErrCode.ERR_OK:
                if(ConstantsAPI.COMMAND_SENDAUTH == respType) { // 微信第三方登录
                    SendAuth.Resp resp = (SendAuth.Resp) baseResp;
                    String accessCode = resp.code;

                    Intent authOKIntent = new Intent(getApplicationContext(), LoginActivity.class);
                    authOKIntent.putExtra("accessCode", accessCode);
                    authOKIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(authOKIntent);
                    finish();
                } else if(ConstantsAPI.COMMAND_SENDMESSAGE_TO_WX == respType) { // 发送消息到微信(可包括分享)

                }
                break;
            case BaseResp.ErrCode.ERR_USER_CANCEL:
                if(ConstantsAPI.COMMAND_SENDAUTH == respType) {  // 微信第三方登录
                    Toast.makeText(getApplicationContext(),R.string.weibosdk_demo_toast_auth_canceled,Toast.LENGTH_SHORT).show();
                    onBackPressed();
                } else if(ConstantsAPI.COMMAND_SENDMESSAGE_TO_WX == respType) { // 发送消息到微信(可包括分享)

                }
                break;
            case BaseResp.ErrCode.ERR_AUTH_DENIED:
                if(ConstantsAPI.COMMAND_SENDAUTH == respType) {  // 微信第三方登录
                    Toast.makeText(getApplicationContext(),R.string.weibosdk_demo_toast_auth_failed,Toast.LENGTH_SHORT).show();
                    onBackPressed();
                } else if(ConstantsAPI.COMMAND_SENDMESSAGE_TO_WX == respType) { // 发送消息到微信(可包括分享)

                }
                break;
            default:
                Toast.makeText(this, getString(R.string.weibosdk_demo_toast_auth_error_unknown), Toast.LENGTH_LONG).show();
                break;
        }


    }
}
