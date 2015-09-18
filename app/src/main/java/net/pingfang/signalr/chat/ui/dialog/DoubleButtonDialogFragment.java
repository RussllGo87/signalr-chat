package net.pingfang.signalr.chat.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;

import net.pingfang.signalr.chat.R;

/**
 * Created by gongguopei87@gmail.com on 2015/9/18.
 */
public class DoubleButtonDialogFragment extends DialogFragment {

    String message;
    String title;

    DoubleButtonDialogClick mListener;

    public interface DoubleButtonDialogClick {
        public void onPositiveButtonClick();
    }

    public static DoubleButtonDialogFragment newInstance(Context context, int resId,DoubleButtonDialogClick mListener) {
        return newInstance(context.getString(resId),mListener);
    }

    public static DoubleButtonDialogFragment newInstance(String message,DoubleButtonDialogClick mListener) {
        return newInstance(null,message,mListener);
    }

    public static DoubleButtonDialogFragment newInstance(String title,String message,DoubleButtonDialogClick mListener) {
        DoubleButtonDialogFragment dialogFragment = new DoubleButtonDialogFragment();
        dialogFragment.message = message;
        dialogFragment.mListener = mListener;
        if(!TextUtils.isEmpty(title)) {
            dialogFragment.title = title;
        }
        return dialogFragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        if(!TextUtils.isEmpty(title)) {
            builder.setTitle(title);
        }
        builder.setMessage(message);
        builder.setPositiveButton(R.string.btn_positive, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mListener.onPositiveButtonClick();
                dialog.dismiss();
            }
        });
        builder.setNegativeButton(R.string.btn_negative, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        return builder.create();
    }
}
