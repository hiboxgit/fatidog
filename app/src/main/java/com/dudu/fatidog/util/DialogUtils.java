package com.dudu.fatidog.util;

import android.widget.Toast;

import com.dudu.commonlib.CommonLib;
import com.dudu.commonlib.ui.dialog.SimpleDialog;
import com.dudu.commonlib.ui.toast.SimpleToast;
import com.dudu.fatidog.R;

/**
 * Author: Robert
 * Date:  2016-12-31
 * Copyright (c) 2016,dudu Co.,Ltd. All rights reserved.
 * Desc:
 */
public class DialogUtils {
    private static SimpleDialog mFaceScanOkDialog = null;

    public static void showTipDialog(int resId) {

        if (mFaceScanOkDialog != null && mFaceScanOkDialog.isShowing()) {
            return;
        }
        mFaceScanOkDialog = new SimpleDialog(CommonLib.getInstance().getContext());
        mFaceScanOkDialog.setText(CommonLib.getInstance().getContext().getResources().getString(resId))
                .show();
    }

    public static void showTipDialog(String tip) {

        if (mFaceScanOkDialog != null && mFaceScanOkDialog.isShowing()) {
            return;
        }
        mFaceScanOkDialog = new SimpleDialog(CommonLib.getInstance().getContext());
        mFaceScanOkDialog.setText(tip)
                .show();
    }

    public static void showTipToast(String tip){
        SimpleToast.make(CommonLib.getInstance().getContext(),tip, Toast.LENGTH_SHORT).show();
    }

    public static void showTipToast(int resId){
        showTipToast(CommonLib.getInstance().getContext().getResources().getString(resId));
    }

    public static void showWarningToast(String warnText){
        SimpleToast.make(CommonLib.getInstance().getContext(),R.drawable.fatidog_warn,warnText, Toast.LENGTH_SHORT).show();
    }

    public static void showWarningToast(int resId){
        showWarningToast(CommonLib.getInstance().getContext().getResources().getString(resId));
    }
}
