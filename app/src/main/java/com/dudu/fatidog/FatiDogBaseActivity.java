package com.dudu.fatidog;

import android.content.Intent;
import android.os.Bundle;

import com.dudu.commonlib.share.base.DuSimpleActivity;
import com.dudu.commonlib.utils.activity.ActivitiesManager;
import com.dudu.fatidog.util.CommonViewHolder;

public abstract class FatiDogBaseActivity extends DuSimpleActivity {
    protected CommonViewHolder mViewHolder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivitiesManager.getInstance().addActivity(this);
        mViewHolder = new CommonViewHolder(getContentView());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ActivitiesManager.getInstance().removeActivity(this);
    }
}

