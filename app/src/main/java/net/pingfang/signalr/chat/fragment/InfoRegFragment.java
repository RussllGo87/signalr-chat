package net.pingfang.signalr.chat.fragment;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import net.pingfang.signalr.chat.R;
import net.pingfang.signalr.chat.activity.SelectCitiesDialogActivity;
import net.pingfang.signalr.chat.listener.OnRegisterInteractionListener;
import net.pingfang.signalr.chat.util.CommonTools;

/**
 * A simple {@link Fragment} subclass.
 */
public class InfoRegFragment extends Fragment implements View.OnClickListener{

    public static final int REQ_ADDRESS_PICKER = 0x1001;

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

    EditText et_phone_reg;
    EditText et_nick_name_reg;
    EditText et_pwd_reg;
    EditText et_pwd_retype_reg;
//    EditText et_qq_reg;
//    EditText et_email_reg;

    String province;
    String city;
    String area;
    String detail;

    Button btn_address;
    RadioGroup rg_gender;
    RadioButton rb_gender_male;
    RadioButton rb_gender_female;

    String gender;

    Button btn_info_reg_submit;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_info_reg, container, false);
        et_phone_reg = (EditText) view.findViewById(R.id.et_phone_reg);
        et_phone_reg.setText(phone);
        et_phone_reg.setEnabled(false);
        btn_info_reg_submit = (Button) view.findViewById(R.id.btn_info_reg_submit);
        btn_info_reg_submit.setOnClickListener(this);
        et_nick_name_reg = (EditText) view.findViewById(R.id.et_nick_name_reg);
        et_pwd_reg = (EditText) view.findViewById(R.id.et_pwd_reg);
        et_pwd_retype_reg = (EditText) view.findViewById(R.id.et_pwd_retype_reg);
//        et_qq_reg = (EditText) view.findViewById(R.id.et_qq_reg);
//        et_email_reg = (EditText) view.findViewById(R.id.et_email_reg);
        btn_address = (Button) view.findViewById(R.id.btn_address);
        btn_address.setOnClickListener(this);
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
//                String qq = et_pwd_reg.getText().toString().trim();
//                String email = et_email_reg.getText().toString().trim();
                if(!TextUtils.isEmpty(password) && password.equals(passwordR)
                        && CommonTools.checkRegParams(nickname,password)
                        && !TextUtils.isEmpty(province)
                        && !TextUtils.isEmpty(city)
                        && !TextUtils.isEmpty(detail)) {
                    mListener.submitInfo(phone,nickname,password,gender,province,city,area,detail);
                }
                break;
            case R.id.btn_address:
                Intent intent = new Intent(getContext(),SelectCitiesDialogActivity.class);
                startActivityForResult(intent, REQ_ADDRESS_PICKER);
                break;
        }
    }

    public void submitInfo() {
        String nickname = et_nick_name_reg.getText().toString().trim();
        String password = et_pwd_reg.getText().toString().trim();
        String passwordR = et_pwd_retype_reg.getText().toString().trim();
//        String qq = et_pwd_reg.getText().toString().trim();
//        String email = et_email_reg.getText().toString().trim();
        if(!TextUtils.isEmpty(password) && password.equals(passwordR)
                && CommonTools.checkRegParams(nickname,password)
                && !TextUtils.isEmpty(province)
                && !TextUtils.isEmpty(city)
                && !TextUtils.isEmpty(detail)) {
            mListener.submitInfo(phone,nickname,password,gender,province,city,area,detail);
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == Activity.RESULT_OK) {
            if(requestCode == REQ_ADDRESS_PICKER) {
                Bundle bundle = data.getExtras();
                province = bundle.getString("province");
                city = bundle.getString("City");
                area = bundle.getString("AreaName");
                detail = bundle.getString("Addre");
            }
        }
    }
}
