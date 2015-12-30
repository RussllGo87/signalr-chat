package net.pingfang.signalr.chat.fragment;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.pingfang.signalr.chat.R;
import net.pingfang.signalr.chat.activity.AdUploadListActivity;
import net.pingfang.signalr.chat.activity.ListShieldsActivity;
import net.pingfang.signalr.chat.activity.ResourceListActivity;
import net.pingfang.signalr.chat.activity.ResourceUploadListActivity;
import net.pingfang.signalr.chat.activity.SettingsActivity;
import net.pingfang.signalr.chat.constant.app.AppConstants;
import net.pingfang.signalr.chat.listener.OnFragmentInteractionListener;
import net.pingfang.signalr.chat.net.OkHttpCommonUtil;
import net.pingfang.signalr.chat.util.GlobalApplication;
import net.pingfang.signalr.chat.util.SharedPreferencesHelper;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AccountFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AccountFragment extends Fragment implements View.OnClickListener{

    LinearLayout ll_container_account_item_me;
    ImageView iv_account_portrait;
    TextView tv_account_item_me;
    TextView tv_account_current_exp;
    //    ImageView btn_qr_code;
    TextView tv_account_item_resource_upload;
    TextView tv_account_item_uploaded;
    TextView tv_account_item_filter_list;

    TextView tv_account_item_ad_upload;

    TextView tv_account_item_settings;
    SharedPreferencesHelper helper;
    MessageReceiver receiver;
    private OnFragmentInteractionListener mListener;

    public static AccountFragment newInstance(OnFragmentInteractionListener mListener) {
        AccountFragment fragment = new AccountFragment();
        fragment.mListener = mListener;
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        registerReceiver();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_account, container, false);
        ll_container_account_item_me = (LinearLayout) view.findViewById(R.id.ll_container_account_item_me);
        ll_container_account_item_me.setOnClickListener(this);
        tv_account_item_me = (TextView) view.findViewById(R.id.tv_account_item_me);
        iv_account_portrait = (ImageView) view.findViewById(R.id.iv_account_portrait);
        tv_account_current_exp = (TextView) view.findViewById(R.id.tv_account_current_exp);
        tv_account_item_uploaded = (TextView) view.findViewById(R.id.tv_account_item_uploaded);
        tv_account_item_uploaded.setOnClickListener(this);
        tv_account_item_filter_list = (TextView) view.findViewById(R.id.tv_account_item_filter_list);
        tv_account_item_filter_list.setOnClickListener(this);

        //        btn_qr_code = (ImageView) view.findViewById(R.id.btn_qr_code);
        //        btn_qr_code.setOnClickListener(this);

        tv_account_item_resource_upload = (TextView) view.findViewById(R.id.tv_account_item_resource_upload);
        tv_account_item_resource_upload.setOnClickListener(this);
        tv_account_item_ad_upload = (TextView) view.findViewById(R.id.tv_account_item_ad_upload);
        tv_account_item_ad_upload.setOnClickListener(this);

        tv_account_item_settings = (TextView) view.findViewById(R.id.tv_account_item_settings);
        tv_account_item_settings.setOnClickListener(this);
        return view;
    }

    public void registerReceiver() {
        receiver = new MessageReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(GlobalApplication.ACTION_INTENT_ACCOUNT_INFO_UPDATE);
        getContext().registerReceiver(receiver, filter);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        helper = SharedPreferencesHelper.newInstance(getContext());
        if(mListener != null) {
            mListener.loadAccountInfo();
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        String nickname = helper.getStringValue(AppConstants.KEY_SYS_CURRENT_NICKNAME);
        String portrait = helper.getStringValue(AppConstants.KEY_SYS_CURRENT_PORTRAIT);
        int exp = helper.getInt(AppConstants.KEY_SYS_CURRENT_USER_EXP, 0);

        updateAccountInfo(nickname, portrait, exp);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (receiver != null) {
            getContext().unregisterReceiver(receiver);
        }
    }


    public void updateAccountInfo(String nickname, String portrait, int exp) {
        if (!TextUtils.isEmpty(nickname)) {
            tv_account_item_me.setText(nickname);
        }

        if(!TextUtils.isEmpty(portrait)) {
//            portrait = GlobalApplication.PORTRAIT_URL_PREFIX + portrait;
            OkHttpCommonUtil okHttpCommonUtil = OkHttpCommonUtil.newInstance(getContext());
            okHttpCommonUtil.display(iv_account_portrait, portrait, R.drawable.hale_default_user_portrait);
        } else {
            iv_account_portrait.setImageResource(R.drawable.hale_default_user_portrait);
        }

        tv_account_current_exp.setText(getString(R.string.tv_account_current_exp, exp));
    }

    @Override
    public void onClick(View view) {
        int viewId = view.getId();
        switch (viewId) {
            //            case R.id.btn_qr_code:
            //                showQrCodeInfo();
            //                break;
            case R.id.ll_container_account_item_me:
                //                Intent accountInfoIntent = new Intent();
                //                accountInfoIntent.setClass(getContext(), AccountInfoActivity.class);
                //                getContext().startActivity(accountInfoIntent);
                break;
            case R.id.tv_account_item_uploaded:
                Intent resourceListIntent = new Intent();
                resourceListIntent.setClass(getContext(), ResourceListActivity.class);
                getContext().startActivity(resourceListIntent);
                break;
            case R.id.tv_account_item_filter_list:
                Intent shieldsListIntent = new Intent();
                shieldsListIntent.setClass(getContext(), ListShieldsActivity.class);
                startActivity(shieldsListIntent);
                break;
            case R.id.tv_account_item_resource_upload:
                Intent resourceUploadIntent = new Intent();
                resourceUploadIntent.setClass(getContext(), ResourceUploadListActivity.class);
                startActivity(resourceUploadIntent);
                break;
            case R.id.tv_account_item_ad_upload:
                Intent adUploadIntent = new Intent();
                adUploadIntent.setClass(getContext(), AdUploadListActivity.class);
                startActivity(adUploadIntent);
                break;
            case R.id.tv_account_item_settings:
                Intent intent = new Intent();
                intent.setClass(getActivity(), SettingsActivity.class);
                getContext().startActivity(intent);
                break;
        }
    }

    //    public void showQrCodeInfo() {
    //
    //    }

    private class MessageReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (mListener != null) {
                mListener.loadAccountInfo();
            }
        }
    }
}
