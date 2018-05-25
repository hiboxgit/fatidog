package com.dudu.fatidog.util;

import android.content.Context;
import android.support.annotation.IdRes;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.lang.reflect.Field;

/**
 * Author: Robert
 * Date:  2016-12-31
 * Copyright (c) 2016,dudu Co.,Ltd. All rights reserved.
 * Desc:
 */
public class CommonViewHolder extends RecyclerView.ViewHolder {
    private SparseArray<View> sparseArray;

    public CommonViewHolder(View itemView) {
        super(itemView);
        sparseArray = new SparseArray();
    }

    public View v(@IdRes int resId) {
        View view = sparseArray.get(resId);
        if (view == null) {
            view = itemView.findViewById(resId);
            sparseArray.put(resId, view);
        }
        return view;
    }

    public RecyclerView reV(@IdRes int resId) {
        return (RecyclerView) v(resId);
    }

    public RecyclerView reV(String idName) {
        return (RecyclerView) viewByName(idName);
    }

    public View v(String idName) {
        return viewByName(idName);
    }

    public View view(@IdRes int resId) {
        return v(resId);
    }

    /**
     * 返回 TextView
     */
    public TextView tV(@IdRes int resId) {
        return (TextView) v(resId);
    }

    public TextView textView(@IdRes int resId) {
        return tV(resId);
    }

    /**
     * 返回 ImageView
     */
    public ImageView imgV(@IdRes int resId) {
        return (ImageView) v(resId);
    }

    public ImageView imageView(@IdRes int resId) {
        return imgV(resId);
    }

    /**
     * 返回 ViewGroup
     */
    public ViewGroup groupV(@IdRes int resId) {
        return (ViewGroup) v(resId);
    }

    public ViewGroup gV(@IdRes int resId) {
        return (ViewGroup) v(resId);
    }

    public ViewGroup viewGroup(@IdRes int resId) {
        return groupV(resId);
    }

    public RecyclerView r(@IdRes int resId) {
        return (RecyclerView) v(resId);
    }

    public View viewByName(String name) {
        View view = v(getIdByName(name, "id"));
        return view;
    }

    /**
     * 根据name, 在主题中 寻找资源id
     */
    private int getIdByName(String name, String type) {
        Context context = itemView.getContext();
        return context.getResources().getIdentifier(name, type, context.getPackageName());
    }

    public void fillView(Object bean) {
        Field[] fields = bean.getClass().getDeclaredFields();
        for (Field f : fields) {
            String name = f.getName();

            try {
                View view = viewByName(name);
                if (view instanceof TextView) {
                    ((TextView) view).setText((String) f.get(bean));
                } else if (view instanceof ImageView) {

                }

            } catch (Exception e) {
            }
        }
    }
}
