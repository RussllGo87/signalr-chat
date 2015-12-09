package net.pingfang.signalr.chat.ui.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import net.pingfang.signalr.chat.R;
import net.pingfang.signalr.chat.listener.OnDialogListItemListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by gongguopei87@gmail.com on 2015/12/4.
 */
public class ListDialogFragment extends DialogFragment {

    OnDialogListItemListener mOnDialogListItemListener;
    List<String> dataRes = new ArrayList<>();



    public static ListDialogFragment newInstance(OnDialogListItemListener mOnDialogListItemListener, List<String> dataRes) {
        ListDialogFragment fragment = new ListDialogFragment();
        fragment.mOnDialogListItemListener = mOnDialogListItemListener;
        fragment.dataRes = dataRes;
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.Theme_AppCompat_Light_Dialog_Alert);
        AlertDialog dialog;
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.fragment_dialog_list, null);
        ListView lv_dialog_list = (ListView) view.findViewById(R.id.lv_dialog_list);
        lv_dialog_list.setAdapter(new DialogListAdapter());
        lv_dialog_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mOnDialogListItemListener.onDialogItemClick(position);
                dismiss();
            }
        });

        builder.setView(view);

        dialog = builder.create();
        return dialog;
    }

    private class DialogListAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return dataRes.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                LayoutInflater inflater = getActivity().getLayoutInflater();
                View view = inflater.inflate(R.layout.item_dialog_list, null);
                TextView textView = (TextView) view.findViewById(R.id.tv_dialog_list);
                textView.setText(dataRes.get(position));
                convertView = view;
            } else {
                TextView textView = (TextView) convertView.findViewById(R.id.tv_dialog_list);
                textView.setText(dataRes.get(position));
            }

            return convertView;
        }
    }
}
