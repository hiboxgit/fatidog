package com.dudu.recoac.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.dudu.commonlib.CommonLib;
import com.dudu.commonlib.ui.toast.SimpleToast;
import com.dudu.commonlib.utils.string.StringTools;
import com.dudu.fatidog.R;

/**
 * @author luo zha
 * @CreateDate 2017-02-10 10:04.
 */
public class InputFaceNameDialog extends Dialog implements View.OnClickListener {
    private EditText mFaceNameEdit;
    private TextView mConfirmTV, mCancelTV;
    private String faceName;
    private Context mContext;
    private OnViewClickListener onViewClickListener;

    public InputFaceNameDialog(Context context, String faceName) {
        super(context, R.style.InputFaceNameDialogStyle);
        this.mContext = context;
        this.faceName = faceName;
    }

    public void setOnViewClickListener(OnViewClickListener onViewClickListener) {
        this.onViewClickListener = onViewClickListener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_human_face_edit);
        initView();
        initData();
        initListener();
    }

    private void initView() {
        mFaceNameEdit = (EditText) findViewById(R.id.face_name_edit);
        mConfirmTV = (TextView) findViewById(R.id.confirm_tv);
        mCancelTV = (TextView) findViewById(R.id.cancel_tv);
    }

    private void initData() {
        mFaceNameEdit.setText(faceName);
        if (!StringTools.isEmpty(faceName)){
            mFaceNameEdit.setSelection(faceName.length());
        }
    }

    private void initListener() {
        mConfirmTV.setOnClickListener(this);
        mCancelTV.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.confirm_tv:
                if (StringTools.isEmpty(mFaceNameEdit.getText().toString())) {
                    SimpleToast.make(mContext, mContext.getResources().getString(R.string.please_input_face_name), Toast.LENGTH_SHORT).show();
                    return;
                }
                if (onViewClickListener != null) {
                    onViewClickListener.onConfirmView(view, mFaceNameEdit.getText().toString());
                }
                break;
            case R.id.cancel_tv:
                if (onViewClickListener != null) {
                    onViewClickListener.onCancelView(view, mFaceNameEdit.getText().toString());
                }
                break;
            default:
                break;
        }
    }

    public interface OnViewClickListener {
        void onConfirmView(View view, String faceName);

        void onCancelView(View view, String faceName);
    }
}
