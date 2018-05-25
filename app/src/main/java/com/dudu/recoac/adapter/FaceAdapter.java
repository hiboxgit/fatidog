package com.dudu.recoac.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.dudu.fatidog.R;
import com.dudu.recoac.bean.FaceRecogPersonData;

import java.util.List;

/**
 * @author luo zha
 * @CreateDate 2017-02-06 11:21.
 */
public class FaceAdapter extends BaseAdapter {
    private List<FaceRecogPersonData> data;
    private LayoutInflater inflater;
    private Context context;
    private int editStatus = 0;
    public static final int EDIT_STATUS = 0;
    public static final int CANCEL_STATUS = 1;
    private OnViewListener listener;

    public FaceAdapter(List<FaceRecogPersonData> data, Context context) {
        this.data = data;
        this.context = context;
        inflater = LayoutInflater.from(context);
    }

    public void setListener(OnViewListener listener) {
        this.listener = listener;
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int i) {
        return data.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    public void setData(List<FaceRecogPersonData> data) {
        this.data = data;
        notifyDataSetChanged();
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder viewHolder = null;
        if (view == null) {
            viewHolder = new ViewHolder();
            view = inflater.inflate(R.layout.face_list_item, viewGroup, false);
            viewHolder.faceName = (TextView) view.findViewById(R.id.face_name);
            viewHolder.deleteTV = (TextView) view.findViewById(R.id.button_delete);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }
        viewHolder.faceName.setText(data.get(i).getName());
        if (editStatus == CANCEL_STATUS) {
            viewHolder.deleteTV.setVisibility(View.VISIBLE);
        } else {
            viewHolder.deleteTV.setVisibility(View.INVISIBLE);
        }
        viewHolder.deleteTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listener != null) {
                    listener.onViewClick(view, i);
                }
            }
        });
        return view;
    }

    public void cancelEditFace() {
        editStatus = EDIT_STATUS;
        notifyDataSetChanged();
    }

    public void editFace() {
        editStatus = CANCEL_STATUS;
        notifyDataSetChanged();
    }

    public int getEditStatus() {
        return editStatus;
    }

    class ViewHolder {
        TextView faceName;
        TextView deleteTV;
    }

    public interface OnViewListener {
        void onViewClick(View view, int position);
    }

}
