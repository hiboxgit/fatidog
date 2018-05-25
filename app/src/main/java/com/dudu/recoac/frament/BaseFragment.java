package com.dudu.recoac.frament;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * @author luo zha
 * @CreateDate 2017-02-05 17:04.
 */
public abstract class BaseFragment extends Fragment {
    private View mRootView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mRootView = LayoutInflater.from(getActivity()).inflate(initLayoutId(), container, false);
        initView(mRootView);
        initData();
        initListener();
        return mRootView;
    }

    protected abstract int initLayoutId();

    protected abstract void initView(View rootView);

    protected abstract void initData();

    protected abstract void initListener();
}
