package net.pingfang.signalr.chat.ui.dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import net.pingfang.signalr.chat.R;

/**
 * Created by gongguopei87@gmail.com on 2015/9/7.
 */
public class SingleButtonDialogFragment extends DialogFragment{

    String message;

    public static SingleButtonDialogFragment newInstance(String message) {
        SingleButtonDialogFragment dialogFragment = new SingleButtonDialogFragment();
        dialogFragment.message = message;
        return dialogFragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(message);
        builder.setPositiveButton(R.string.btn_positive, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        return builder.create();
    }
}
