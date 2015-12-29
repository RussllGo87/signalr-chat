package net.pingfang.signalr.chat.fragment;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import net.pingfang.signalr.chat.R;
import net.pingfang.signalr.chat.constant.app.AppConstants;
import net.pingfang.signalr.chat.listener.OnRegisterInteractionListener;
import net.pingfang.signalr.chat.net.HttpBaseCallback;
import net.pingfang.signalr.chat.net.OkHttpCommonUtil;
import net.pingfang.signalr.chat.util.CommonTools;
import net.pingfang.signalr.chat.util.GlobalApplication;
import net.pingfang.signalr.chat.util.SharedPreferencesHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnRegisterInteractionListener} interface
 * to handle interaction events.
 * Use the {@link PhoneFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PhoneFragment extends Fragment implements View.OnClickListener{

    private static final String TAG = PhoneFragment.class.getSimpleName();

    public static final String URL_LOAD_VALIDATE_CODE = GlobalApplication.URL_WEB_API_HOST + "/api/WebAPI/User/RequestIdentifying";
    public static final String KEY_LOAD_VALIDATE_CODE_ACCOUNT_PHONE = "MobilePhone";
    public static final String KEY_LOAD_VALIDATE_CODE_REQUEST_TYPE = "RequestType";

    EditText et_phone_reg;
    EditText et_validate_code_reg;
    TextView btn_load_validate_code;
    TextView btn_step_next_reg;

    private OnRegisterInteractionListener mListener;
    SharedPreferencesHelper sharedPreferencesHelper;

    private Handler mHandler;
    ClassCutThreads thread;
    private int i = 60;

    public PhoneFragment() {
        // Required empty public constructor
    }

    public static PhoneFragment newInstance() {
        return new PhoneFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mHandler = new Handler(Looper.getMainLooper());
        sharedPreferencesHelper = SharedPreferencesHelper.newInstance(getContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_phone, container, false);
        et_phone_reg = (EditText) view.findViewById(R.id.et_phone_reg);
        et_validate_code_reg = (EditText) view.findViewById(R.id.et_validate_code_reg);
        btn_load_validate_code = (TextView) view.findViewById(R.id.btn_load_validate_code);
        btn_load_validate_code.setOnClickListener(this);
        btn_step_next_reg = (TextView) view.findViewById(R.id.btn_step_next_reg);
        btn_step_next_reg.setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View view) {
        int viewId = view.getId();
        String phoneNo = et_phone_reg.getText().toString().trim();
        switch(viewId) {
            case R.id.btn_load_validate_code:
                if(CommonTools.isPhoneNumber(phoneNo)) {
//                    mListener.validate(phoneNo);
                    loadValidateCode(phoneNo);
                } else if(!TextUtils.isDigitsOnly(phoneNo)) {
                    Toast.makeText(getContext(),R.string.toast_phone_no_error_txt, Toast.LENGTH_LONG).show();
                } else if(phoneNo.length() != 11) {
                    Toast.makeText(getContext(),R.string.toast_phone_no_error_length,Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getContext(),R.string.toast_phone_no_error_invalidate,Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.btn_step_next_reg:
                if(isNextStep()) {
                    mListener.nextPage();
                }
                break;


        }
    }

    private void loadValidateCode(final String phoneNo) {
        OkHttpCommonUtil okHttp = OkHttpCommonUtil.newInstance(getContext());
        okHttp.getRequest(
                URL_LOAD_VALIDATE_CODE,
                new OkHttpCommonUtil.Param[] {
                        new OkHttpCommonUtil.Param(KEY_LOAD_VALIDATE_CODE_ACCOUNT_PHONE, phoneNo),
                        new OkHttpCommonUtil.Param(KEY_LOAD_VALIDATE_CODE_REQUEST_TYPE, "Register")
                },
                new HttpBaseCallback() {
                    @Override
                    public void onFailure(Request request, IOException e) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getContext(), R.string.toast_pwd_forget_load_validate_code_error, Toast.LENGTH_LONG).show();
                                btn_load_validate_code.setClickable(true);
                            }
                        });
                    }

                    @Override
                    public void onResponse(Response response) throws IOException {
                        String responseStr = response.body().string();
                        Log.d(TAG, "URL_LOAD_VALIDATE_CODE == " + responseStr);
                        JSONObject jsonObject;
                        try {
                            jsonObject = new JSONObject(responseStr);
                            int status = jsonObject.getInt("Status");
                            final String message = jsonObject.getString("Message");
                            if(status == 0) {
                                String code = jsonObject.getString("IdentifyingCode");
                                sharedPreferencesHelper.putStringValue(AppConstants.KEY_SYS_PHONE_REG, phoneNo);
                                sharedPreferencesHelper.putStringValue(AppConstants.KEY_SYS_CODE_REG, code);
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(getContext(), R.string.toast_pwd_forget_load_validate_code_ok, Toast.LENGTH_LONG).show();
                                        btn_load_validate_code.setClickable(false);
                                        thread = new ClassCutThreads();
                                        thread.start();
                                    }
                                });
                            } else {
                                // 其他请求错误
                                if(status == -1) {
                                    getActivity().runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
                                            btn_load_validate_code.setClickable(true);
                                        }
                                    });

                                }

                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getContext(), R.string.toast_pwd_forget_load_validate_code_invalidate, Toast.LENGTH_LONG).show();
                                    btn_load_validate_code.setClickable(true);
                                }
                            });
                        }
                    }
                });
    }

    public boolean isNextStep() {
        String phoneNo = et_phone_reg.getText().toString().trim();
        if(!TextUtils.isDigitsOnly(phoneNo)) {
            Toast.makeText(getContext(), R.string.toast_phone_no_error_txt, Toast.LENGTH_LONG).show();
            return false;
        }
        if(phoneNo.length() != 11) {
            Toast.makeText(getContext(),R.string.toast_phone_no_error_length,Toast.LENGTH_LONG).show();
            return false;
        }
        if(!CommonTools.isPhoneNumber(phoneNo)) {
            Toast.makeText(getContext(),R.string.toast_phone_no_error_invalidate,Toast.LENGTH_LONG).show();
            return false;
        }

        String tmpPhone = sharedPreferencesHelper.getStringValue(AppConstants.KEY_SYS_PHONE_REG);
        if(TextUtils.isEmpty(tmpPhone)) {
            Toast.makeText(getContext(), "当前电话号码没有检验,需要重新获取验证码", Toast.LENGTH_LONG).show();
            if(thread != null && thread.isRunning) {
                thread.setIsRunning(false);
                btn_load_validate_code.setClickable(true);
            }
            return false;
        }

        if(!phoneNo.equals(tmpPhone)) {
            Toast.makeText(getContext(), "你已经更换了电话号码,需要重新获取验证码", Toast.LENGTH_LONG).show();
            if(thread != null && thread.isRunning) {
                thread.setIsRunning(false);
                btn_load_validate_code.setClickable(true);
            }
            return false;
        }

        String validateCode = et_validate_code_reg.getText().toString().trim();
        if(TextUtils.isEmpty(validateCode)) {
            Toast.makeText(getContext(),R.string.toast_pwd_forget_validate_code_empty,Toast.LENGTH_LONG).show();
            return false;
        }

        String code = sharedPreferencesHelper.getStringValue(AppConstants.KEY_SYS_CODE_REG);
        if(TextUtils.isEmpty(code)) {
            Toast.makeText(getContext(), "电话号码没有检验,需要重新获取验证码", Toast.LENGTH_LONG).show();
            if(thread != null && thread.isRunning) {
                thread.setIsRunning(false);
                btn_load_validate_code.setClickable(true);
            }
            return false;
        }
        if(!validateCode.equals(code)) {
            Toast.makeText(getContext(), "验证码输出了,需要重新获取验证码", Toast.LENGTH_LONG).show();
            if(thread != null && thread.isRunning) {
                thread.setIsRunning(false);
                btn_load_validate_code.setClickable(true);
            }
            return false;
        }

        return true;
    }


    public void  validatePhoneNo() {
        String phoneNo = et_phone_reg.getText().toString().trim();
        if(CommonTools.isPhoneNumber(phoneNo)) {
            mListener.validate(phoneNo);
        } else if(!TextUtils.isDigitsOnly(phoneNo)) {
            Toast.makeText(getContext(),R.string.toast_phone_no_error_txt, Toast.LENGTH_LONG).show();
        } else if(phoneNo.length() != 11) {
            Toast.makeText(getContext(),R.string.toast_phone_no_error_length,Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(getContext(),R.string.toast_phone_no_error_invalidate,Toast.LENGTH_LONG).show();
        }
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mListener = (OnRegisterInteractionListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    class ClassCutThreads extends Thread {// 倒计时逻辑子线程

        private boolean isRunning = false;

        public void run() {
            isRunning = true;
            while (i > 0 && isRunning) {
                i--;
                mHandler.post(new Runnable() {// 通过它在UI主线程中修改显示的剩余时间
                    @Override
                    public void run() {
                        btn_load_validate_code.setText(i + "秒后重新获取");// 显示剩余时间
                    }
                });
                try {
                    Thread.sleep(1000);// 线程休眠一秒钟 这个就是倒计时的间隔时间
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    // TODO Auto-generated method stub
                    btn_load_validate_code.setText("获取验证码");// 一轮倒计时结束 修改剩余时间为一分钟
                    Toast.makeText(getContext(), "倒计时完成",
                            Toast.LENGTH_LONG).show();// 提示倒计时完成
                    btn_load_validate_code.setClickable(true);// 倒数完成之后按钮启用
                }
            });
            i = 60;// 修改倒计时剩余时间变量为60秒为下一次获取验证码初始化
        }

        public boolean isRunning() {
            return isRunning;
        }

        public void setIsRunning(boolean isRunning) {
            this.isRunning = isRunning;
        }
    }

}
