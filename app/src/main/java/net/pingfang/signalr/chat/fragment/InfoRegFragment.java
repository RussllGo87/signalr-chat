package net.pingfang.signalr.chat.fragment;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
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
 */
public class InfoRegFragment extends Fragment implements View.OnClickListener{

    private OnRegisterInteractionListener mListener;

    String phone;

    public static InfoRegFragment newInstance(String phone) {
        InfoRegFragment fragment = new InfoRegFragment();
        fragment.phone = phone;
        return fragment;
    }

    public InfoRegFragment() {
        // Required empty public constructor
    }

    EditText et_nick_name_reg;
    EditText et_pwd_reg;
    EditText et_pwd_retype_reg;
    EditText et_qq_reg;
    EditText et_email_reg;
    Button btn_info_reg_submit;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_info_reg, container, false);
        btn_info_reg_submit = (Button) view.findViewById(R.id.btn_info_reg_submit);
        btn_info_reg_submit.setOnClickListener(this);
        et_nick_name_reg = (EditText) view.findViewById(R.id.et_nick_name_reg);
        et_pwd_reg = (EditText) view.findViewById(R.id.et_pwd_reg);
        et_pwd_retype_reg = (EditText) view.findViewById(R.id.et_pwd_retype_reg);
        et_qq_reg = (EditText) view.findViewById(R.id.et_qq_reg);
        et_email_reg = (EditText) view.findViewById(R.id.et_email_reg);
        return view;
    }

    @Override
    public void onClick(View view) {
        int viewId = view.getId();
        switch(viewId) {
            case R.id.btn_info_reg_submit:
                String nickname = et_nick_name_reg.getText().toString().trim();
                String password = et_pwd_reg.getText().toString().trim();
                String passwordR = et_pwd_retype_reg.getText().toString().trim();
                String qq = et_pwd_reg.getText().toString().trim();
                String email = et_email_reg.getText().toString().trim();
                if(!TextUtils.isEmpty(password) && password.equals(passwordR)
                        && CommonTools.checkRegParams(nickname,password,qq,email)) {
                    mListener.submitInfo(phone,nickname,password,qq,email);
                }
                break;
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
