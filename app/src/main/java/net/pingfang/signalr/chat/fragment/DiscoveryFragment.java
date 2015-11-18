package net.pingfang.signalr.chat.fragment;


import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.CustomerIntentIntegrator;

import net.pingfang.signalr.chat.R;
import net.pingfang.signalr.chat.activity.AdMaintainActivity;
import net.pingfang.signalr.chat.activity.CaptureActivityAnyOrientation;
import net.pingfang.signalr.chat.activity.ResourcePostActivity;
import net.pingfang.signalr.chat.listener.OnFragmentInteractionListener;

import java.io.File;

public class DiscoveryFragment extends Fragment implements View.OnClickListener{

    private OnFragmentInteractionListener mListener;

    public static DiscoveryFragment newInstance() {
        DiscoveryFragment fragment = new DiscoveryFragment();
        return fragment;
    }

    public DiscoveryFragment() {
    }

    private TextView tv_account_item_scan;
    private TextView tv_account_item_resource;
    private TextView tv_account_item_maintain;
//    private TextView tv_account_item_nearby_friends;
    private TextView tv_account_item_share_apk;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_discovery, container, false);

        tv_account_item_scan = (TextView) view.findViewById(R.id.tv_account_item_scan);
        tv_account_item_scan.setOnClickListener(this);
        tv_account_item_resource = (TextView) view.findViewById(R.id.tv_account_item_resource);
        tv_account_item_resource.setOnClickListener(this);
        tv_account_item_maintain = (TextView) view.findViewById(R.id.tv_account_item_maintain);
        tv_account_item_maintain.setOnClickListener(this);
//        tv_account_item_nearby_friends = (TextView) view.findViewById(R.id.tv_account_item_nearby_friends);
//        tv_account_item_nearby_friends.setOnClickListener(this);
        tv_account_item_share_apk = (TextView) view.findViewById(R.id.tv_account_item_share_apk);
        tv_account_item_share_apk.setOnClickListener(this);
        return view;
    }

    @Override
    public void onClick(View view) {
        int viewId = view.getId();
        switch (viewId) {
            case R.id.tv_account_item_scan:
                CustomerIntentIntegrator integrator = new CustomerIntentIntegrator(getActivity());
                integrator.setCaptureActivity(CaptureActivityAnyOrientation.class);
                integrator.setOrientationLocked(true);
                integrator.initiateScan();
                break;
            case R.id.tv_account_item_resource:
                Intent resourceAddIntent = new Intent();
                resourceAddIntent.setClass(getContext(),ResourcePostActivity.class);
                startActivity(resourceAddIntent);
                break;
            case R.id.tv_account_item_maintain:
                Intent adMaintainIntent = new Intent();
                adMaintainIntent.setClass(getContext(), AdMaintainActivity.class);
                startActivity(adMaintainIntent);
                break;
//            case R.id.tv_account_item_nearby_friends:
//                Intent nearByFriendsIntent = new Intent();
//                nearByFriendsIntent.setClass(getContext(), NearbyFriendsActivity.class);
//                startActivity(nearByFriendsIntent);
//                break;
            case R.id.tv_account_item_share_apk:
                getApkSourceInfo();
                break;
        }
    }

    private void getApkSourceInfo() {
        try {
            PackageInfo info = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(),0);
            ApplicationInfo appInfo = info.applicationInfo;
            String sourceDir = appInfo.sourceDir;
            File file = new File(sourceDir);
            Uri sourceUri = Uri.fromFile(file);
            Intent sharingIntent = new Intent();
            sharingIntent.setAction(Intent.ACTION_SEND);
            sharingIntent.setType("application/*");
            sharingIntent.setPackage("com.android.bluetooth");
            sharingIntent.putExtra(Intent.EXTRA_STREAM, sourceUri);

            startActivity(Intent.createChooser(sharingIntent, "Share Application"));

            Toast.makeText(getContext(), sourceDir, Toast.LENGTH_SHORT).show();
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }
}
