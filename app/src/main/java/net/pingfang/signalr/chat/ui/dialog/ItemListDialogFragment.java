package net.pingfang.signalr.chat.ui.dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import net.pingfang.signalr.chat.R;
import net.pingfang.signalr.chat.database.User;
import net.pingfang.signalr.chat.listener.OnItemListInteractionListener;

/**
 * Created by gongguopei87@gmail.com on 2015/12/7.
 */
public class ItemListDialogFragment extends DialogFragment {

    User user;

    OnItemListInteractionListener mOnItemListInteractionListener;

    public static ItemListDialogFragment newInstance(OnItemListInteractionListener mOnItemListInteractionListener, User user) {
        ItemListDialogFragment fragment = new ItemListDialogFragment();
        fragment.mOnItemListInteractionListener = mOnItemListInteractionListener;
        fragment.user = user;
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.Theme_AppCompat_Light_Dialog_Alert);
        builder.setItems(R.array.msg_operate, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    mOnItemListInteractionListener.onItemShield(user);
                }

                if (which == 1) {
                    mOnItemListInteractionListener.onItemDelete(user);
                }

                dialog.dismiss();
            }
        });

        return builder.create();
    }
}
