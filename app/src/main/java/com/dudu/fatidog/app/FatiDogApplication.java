package com.dudu.fatidog.app;

import android.support.multidex.MultiDexApplication;

import com.dudu.commonlib.CommonLib;
import com.facebook.drawee.backends.pipeline.Fresco;

/**
 * Author: Robert
 * Date:  2016-12-31
 * Copyright (c) 2016,dudu Co.,Ltd. All rights reserved.
 * Desc:
 */
public class FatiDogApplication extends MultiDexApplication {
    @Override
    public void onCreate() {
        super.onCreate();
        CommonLib.getInstance().init(this);
        Fresco.initialize(this);
    }
}
