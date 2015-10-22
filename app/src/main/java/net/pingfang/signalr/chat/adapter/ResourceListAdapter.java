package net.pingfang.signalr.chat.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import net.pingfang.signalr.chat.R;
import net.pingfang.signalr.chat.model.ResourceInfo;
import net.pingfang.signalr.chat.net.OkHttpCommonUtil;
import net.pingfang.signalr.chat.util.GlobalApplication;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by gongguopei87@gmail.com on 2015/10/20.
 */
public class ResourceListAdapter extends BaseAdapter {

    private Context context;
    private List<ResourceInfo> resourceInfoList;

    public ResourceListAdapter(Context context) {
        this.context = context;
        this.resourceInfoList = new ArrayList<>();
    }

    public void add(ResourceInfo resourceInfo) {
        resourceInfoList.add(resourceInfo);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return resourceInfoList.size();
    }

    @Override
    public Object getItem(int position) {
        return resourceInfoList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ResourceInfo resourceInfo = resourceInfoList.get(position);
        OkHttpCommonUtil okHttp = OkHttpCommonUtil.newInstance(context);
        if(convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = layoutInflater.inflate(R.layout.list_item_resource,null);
            ImageView iv_resource_profile = (ImageView) view.findViewById(R.id.iv_resource_profile);
            String url = resourceInfo.getUrl();
            if(!TextUtils.isEmpty(url)) {
                url = GlobalApplication.RESOURCE_PIC_URL_PREFIX + url;
                okHttp.display(iv_resource_profile,url,R.mipmap.ic_launcher);
            }
            TextView tv_resource_post_time = (TextView) view.findViewById(R.id.tv_resource_post_time);
            tv_resource_post_time.setText(resourceInfo.getPostTime());
            TextView tv_resource_post_address = (TextView) view.findViewById(R.id.tv_resource_post_address);
            tv_resource_post_address.setText(resourceInfo.getAddress());
            TextView tv_resource_post_contact = (TextView) view.findViewById(R.id.tv_resource_post_contact);
            tv_resource_post_contact.setText(resourceInfo.getContact());
            TextView tv_resource_post_contact_info = (TextView) view.findViewById(R.id.tv_resource_post_contact_info);
            tv_resource_post_contact_info.setText(resourceInfo.getContactInfo());
            convertView = view;
        } else {
            ImageView iv_resource_profile = (ImageView) convertView.findViewById(R.id.iv_resource_profile);
            String url = resourceInfo.getUrl();
            if(!TextUtils.isEmpty(url)) {
                url = GlobalApplication.RESOURCE_PIC_URL_PREFIX + url;
                okHttp.display(iv_resource_profile,url,R.mipmap.ic_launcher);
            }
            TextView tv_resource_post_time = (TextView) convertView.findViewById(R.id.tv_resource_post_time);
            tv_resource_post_time.setText(resourceInfo.getPostTime());
            TextView tv_resource_post_address = (TextView) convertView.findViewById(R.id.tv_resource_post_address);
            tv_resource_post_address.setText(resourceInfo.getAddress());
            TextView tv_resource_post_contact = (TextView) convertView.findViewById(R.id.tv_resource_post_contact);
            tv_resource_post_contact.setText(resourceInfo.getContact());
            TextView tv_resource_post_contact_info = (TextView) convertView.findViewById(R.id.tv_resource_post_contact_info);
            tv_resource_post_contact_info.setText(resourceInfo.getContactInfo());
        }

        convertView.setTag(resourceInfo);
        return convertView;
    }
}
