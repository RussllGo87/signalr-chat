package net.pingfang.signalr.chat.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import net.pingfang.signalr.chat.R;
import net.pingfang.signalr.chat.listener.OnRegisterInteractionListener;
import net.pingfang.signalr.chat.util.CommonTools;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnRegisterInteractionListener} interface
 * to handle interaction events.
 * Use the {@link PhoneFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PhoneFragment extends Fragment implements View.OnClickListener{

    EditText et_phone_reg;
    Button btn_captcha_req;
    EditText et_validate_code;
    Button btn_captcha_submit;

    private OnRegisterInteractionListener mListener;

    public static PhoneFragment newInstance() {
        PhoneFragment fragment = new PhoneFragment();
        return fragment;
    }

    public PhoneFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_phone, container, false);
        et_phone_reg = (EditText) view.findViewById(R.id.et_phone_reg);
        btn_captcha_req = (Button) view.findViewById(R.id.btn_captcha_req);
        btn_captcha_req.setOnClickListener(this);
        et_validate_code = (EditText) view.findViewById(R.id.et_validate_code);
        btn_captcha_submit = (Button) view.findViewById(R.id.btn_captcha_submit);
        btn_captcha_submit.setOnClickListener(this);
        return view;
    }

    @Override
    public void onClick(View view) {
        int viewId = view.getId();
        String phoneNo = et_phone_reg.getText().toString().trim();
        switch(viewId) {
            case R.id.btn_captcha_req:
                if(CommonTools.isPhoneNumber(phoneNo)) {
                    mListener.loadCode(phoneNo);
                }
                break;
            case R.id.btn_captcha_submit:
                String vc = et_validate_code.getText().toString().trim();
                if(CommonTools.isPhoneNumber(phoneNo) && CommonTools.isAvailableVc(vc)) {
                    mListener.submitCode(phoneNo,vc);
                }

                break;
        }
    }

    public void submitCode() {
        String phoneNo = et_phone_reg.getText().toString().trim();
        String vc = et_validate_code.getText().toString().trim();
        if(CommonTools.isPhoneNumber(phoneNo) && CommonTools.isAvailableVc(vc)) {
            mListener.submitCode(phoneNo,vc);
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

}
