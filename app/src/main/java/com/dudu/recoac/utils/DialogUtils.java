package com.dudu.recoac.utils;

import android.content.Context;
import android.content.DialogInterface;

import com.dudu.recoac.dialog.InputFaceNameDialog;

/**
 * @author luo zha
 * @CreateDate 2017-02-10 10:19.
 */
public class DialogUtils {
    private static InputFaceNameDialog mInputFaceNameDialog;

    public static void showInputFaceNameDialog(Context context, String faceName, InputFaceNameDialog.OnViewClickListener listener) {
        if (mInputFaceNameDialog == null) {
            mInputFaceNameDialog = new InputFaceNameDialog(context, faceName);
            mInputFaceNameDialog.setOnViewClickListener(listener);
            mInputFaceNameDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialogInterface) {
                    dismissInputFaceNameDialog();
                }
            });
        }
        mInputFaceNameDialog.show();
    }

    public static void dismissInputFaceNameDialog() {
        if (mInputFaceNameDialog != null) {
            mInputFaceNameDialog.dismiss();
            mInputFaceNameDialog = null;
        }
    }
}
