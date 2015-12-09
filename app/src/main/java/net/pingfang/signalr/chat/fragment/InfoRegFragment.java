package net.pingfang.signalr.chat.fragment;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import net.pingfang.signalr.chat.R;
import net.pingfang.signalr.chat.listener.OnRegisterInteractionListener;
import net.pingfang.signalr.chat.util.CommonTools;

/**
 * A simple {@link Fragment} subclass.
 */
public class InfoRegFragment extends Fragment implements View.OnClickListener{

    public static final int REQ_ADDRESS_PICKER = 0x1001;
    String phone;
    EditText et_phone_reg;
    EditText et_nick_name_reg;
    EditText et_pwd_reg;
    EditText et_pwd_retype_reg;
    //    Button btn_address;
    RadioGroup rg_gender;
    RadioButton rb_gender_male;
    RadioButton rb_gender_female;
//    EditText et_qq_reg;
//    EditText et_email_reg;
    String gender;
    TextView btn_info_reg_submit;
    private OnRegisterInteractionListener mListener;

    public InfoRegFragment() {
        // Required empty public constructor
    }

    public static InfoRegFragment newInstance(String phone) {
        InfoRegFragment fragment = new InfoRegFragment();
        fragment.phone = phone;
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_info_reg, container, false);
        et_phone_reg = (EditText) view.findViewById(R.id.et_phone_reg);
        et_phone_reg.setText(phone);
        et_phone_reg.setEnabled(false);
        btn_info_reg_submit = (TextView) view.findViewById(R.id.btn_info_reg_submit);
        btn_info_reg_submit.setOnClickListener(this);
        et_nick_name_reg = (EditText) view.findViewById(R.id.et_nick_name_reg);
        et_pwd_reg = (EditText) view.findViewById(R.id.et_pwd_reg);
        et_pwd_retype_reg = (EditText) view.findViewById(R.id.et_pwd_retype_reg);
//        et_qq_reg = (EditText) view.findViewById(R.id.et_qq_reg);
//        et_email_reg = (EditText) view.findViewById(R.id.et_email_reg);
//        btn_address = (Button) view.findViewById(R.id.btn_address);
//        btn_address.setOnClickListener(this);
        rg_gender = (RadioGroup) view.findViewById(R.id.rg_gender);
        rb_gender_male = (RadioButton) view.findViewById(R.id.rb_gender_male);
        rb_gender_female = (RadioButton) view.findViewById(R.id.rb_gender_female);
        rg_gender.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if(rb_gender_male.getId() == checkedId) {
                    gender = "1";
                } else {
                    gender = "0";
                }
            }
        });
        gender = "1";
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
                if (TextUtils.isEmpty(nickname)) {
                    Toast.makeText(getContext(),R.string.toast_info_reg_error_nickname_empty,Toast.LENGTH_LONG).show();
                } else if(TextUtils.isEmpty(password)) {
                    Toast.makeText(getContext(),R.string.toast_info_reg_error_password_empty,Toast.LENGTH_LONG).show();
                } else if(TextUtils.isEmpty(passwordR)) {
                    Toast.makeText(getContext(),R.string.toast_info_reg_error_passwordr_empty,Toast.LENGTH_LONG).show();
                } else if (password.length() < 6) {
                    Toast.makeText(getContext(), R.string.toast_info_reg_error_password_length_less_than_6, Toast.LENGTH_LONG).show();
                } else if(!password.equals(passwordR)) {
                    Toast.makeText(getContext(),R.string.toast_info_reg_error_password_not_same,Toast.LENGTH_LONG).show();
                } else {
                    mListener.submitInfo(phone, nickname, password, gender);
                }
                break;
//            case R.id.btn_address:
//                Intent intent = new Intent(getContext(),SelectCitiesDialogActivity.class);
//                startActivityForResult(intent, REQ_ADDRESS_PICKER);
//                break;
        }
    }

    public void submitInfo() {
        String nickname = et_nick_name_reg.getText().toString().trim();
        String password = et_pwd_reg.getText().toString().trim();
        String passwordR = et_pwd_retype_reg.getText().toString().trim();
//        String qq = et_pwd_reg.getText().toString().trim();
//        String email = et_email_reg.getText().toString().trim();
        if(!TextUtils.isEmpty(password) && password.equals(passwordR)
                && CommonTools.checkRegParams(nickname,password)) {
            mListener.submitInfo(phone,nickname,password,gender);
        } else if(TextUtils.isEmpty(nickname)) {
            Toast.makeText(getContext(),R.string.toast_info_reg_error_nickname_empty,Toast.LENGTH_LONG).show();
        } else if(TextUtils.isEmpty(password)) {
            Toast.makeText(getContext(),R.string.toast_info_reg_error_password_empty,Toast.LENGTH_LONG).show();
        } else if (password.length() < 6) {
            Toast.makeText(getContext(), R.string.toast_info_reg_error_password_length_less_than_6, Toast.LENGTH_LONG).show();
        } else if(TextUtils.isEmpty(passwordR)) {
            Toast.makeText(getContext(),R.string.toast_info_reg_error_passwordr_empty,Toast.LENGTH_LONG).show();
        } else if(!password.equals(passwordR)) {
            Toast.makeText(getContext(),R.string.toast_info_reg_error_password_not_same,Toast.LENGTH_LONG).show();
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
