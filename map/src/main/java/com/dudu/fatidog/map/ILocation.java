package com.dudu.fatidog.map;

import android.content.Context;

/**
 * Author: Robert
 * Date:  2017-03-21
 * Copyright (c) 2016,dudu Co.,Ltd. All rights reserved.
 * Desc:
 */
public interface ILocation {
    public void startLocation(Context context);

    public void stopLocation();

    public void setLocationListener(ILocationListener iLocationListener);

    public boolean isLocation();
}
