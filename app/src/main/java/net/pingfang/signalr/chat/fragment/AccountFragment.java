package net.pingfang.signalr.chat.fragment;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import net.pingfang.signalr.chat.R;
import net.pingfang.signalr.chat.activity.SettingsActivity;
import net.pingfang.signalr.chat.constant.qq.TencentConstants;
import net.pingfang.signalr.chat.constant.weibo.WeiboConstants;
import net.pingfang.signalr.chat.listener.OnFragmentInteractionListener;
import net.pingfang.signalr.chat.net.OkHttpCommonUtil;
import net.pingfang.signalr.chat.util.SharedPreferencesHelper;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AccountFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AccountFragment extends Fragment implements View.OnClickListener{

    private OnFragmentInteractionListener mListener;

    ImageView iv_account_portrait;
    TextView tv_settings_item_me;
    ImageView btn_qr_code;
    TextView tv_account_item_settings;

    SharedPreferencesHelper helper;

    public static AccountFragment newInstance(OnFragmentInteractionListener mListener) {
        AccountFragment fragment = new AccountFragment();
        fragment.mListener = mListener;
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_account, container, false);
        iv_account_portrait = (ImageView) view.findViewById(R.id.iv_account_portrait);
        tv_settings_item_me = (TextView) view.findViewById(R.id.tv_settings_item_me);
        btn_qr_code = (ImageView) view.findViewById(R.id.btn_qr_code);
        btn_qr_code.setOnClickListener(this);
        tv_account_item_settings = (TextView) view.findViewById(R.id.tv_account_item_settings);
        tv_account_item_settings.setOnClickListener(this);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        helper = SharedPreferencesHelper.newInstance(getContext());
        mListener.loadAccountInfo();
    }

    public void updateQqAccountInfo() {
        String nickname = helper.getStringValue(TencentConstants.KEY_QQ_NICK_NAME);
        String portrait = helper.getStringValue(TencentConstants.KEY_QQ_PORTRAIT);
        if (!TextUtils.isEmpty(nickname)) {
            tv_settings_item_me.setText(nickname);
        }

        if(!TextUtils.isEmpty(portrait)) {
            OkHttpCommonUtil okHttpCommonUtil = OkHttpCommonUtil.newInstance(getContext());
            okHttpCommonUtil.display(iv_account_portrait,portrait,R.mipmap.ic_launcher);
        }
    }

    public void updateWbAccountInfo() {

        String nickname = helper.getStringValue(WeiboConstants.KEY_WB_SCREEN_NAME);
        String portrait = helper.getStringValue(WeiboConstants.KEY_WB_PROFILE_IMAGE_URL);
        if (!TextUtils.isEmpty(nickname)) {
            tv_settings_item_me.setText(nickname);
        }

        if(!TextUtils.isEmpty(portrait)) {
            OkHttpCommonUtil okHttpCommonUtil = OkHttpCommonUtil.newInstance(getContext());
            okHttpCommonUtil.display(iv_account_portrait,portrait,R.mipmap.ic_launcher);
        }
    }

    @Override
    public void onClick(View view) {
        int viewId = view.getId();
        switch (viewId) {
            case R.id.btn_qr_code:
                showQrCodeInfo();
                break;
            case R.id.tv_account_item_settings:
                Intent intent = new Intent();
                intent.setClass(getActivity(), SettingsActivity.class);
                getActivity().startActivity(intent);
                break;
        }
    }

    public void showQrCodeInfo() {

    }
}
