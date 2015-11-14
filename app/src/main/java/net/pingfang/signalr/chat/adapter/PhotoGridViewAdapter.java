package net.pingfang.signalr.chat.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import net.pingfang.signalr.chat.R;
import net.pingfang.signalr.chat.listener.OnGridViewItemClick;
import net.pingfang.signalr.chat.util.MediaFileUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by gongguopei87@gmail.com on 2015/11/13.
 */
public class PhotoGridViewAdapter extends BaseAdapter{

    private Context context;

    private List<String> pathList = new ArrayList<>();

    private OnGridViewItemClick mOnGridViewItemClick;

    public PhotoGridViewAdapter(Context context, OnGridViewItemClick mOnGridViewItemClick) {
        this.context = context;
        this.mOnGridViewItemClick = mOnGridViewItemClick;
    }

    @Override
    public int getCount() {
        return pathList.size();
    }

    @Override
    public Object getItem(int position) {
        return pathList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if(convertView == null){
            View view = LayoutInflater.from(context).inflate(R.layout.grid_item_photo, null);
            holder = new ViewHolder();
            holder.imageView = (ImageView)view.findViewById(R.id.grid_item_photo);

            view.setTag(holder);

            convertView = view;
        }else{
            holder = (ViewHolder) convertView.getTag();
        }

        String filePath = pathList.get(position);
        Bitmap bitmap = BitmapFactory.decodeFile(filePath);
        bitmap = Bitmap.createScaledBitmap(bitmap, MediaFileUtils.dpToPx(context, 180), MediaFileUtils.dpToPx(context, 200), true);
        holder.imageView.setImageBitmap(bitmap);
        holder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mOnGridViewItemClick.onItemClick(pathList.get(position));
            }
        });
        holder.imageView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                deletePhoto(position);
                mOnGridViewItemClick.onLongItemClick(position);
                return true;
            }
        });

        return convertView;
    }

    public void addPhoto(String path) {
        pathList.add(path);
        notifyDataSetChanged();
    }

    public void deletePhoto(int position) {
        pathList.remove(position);
        notifyDataSetChanged();
    }

    private class ViewHolder{
        ImageView imageView;
    }
}
