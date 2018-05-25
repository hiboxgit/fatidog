package com.dudu.fatidog.bean;

/**
 * Author: Robert
 * Date:  2017-05-16
 * Copyright (c) 2016,dudu Co.,Ltd. All rights reserved.
 * Desc:
 */
public class UpdateFaceListEvent {
    private boolean isNeedToRefresh;

    public UpdateFaceListEvent(boolean isNeedToRefresh) {
        this.isNeedToRefresh = isNeedToRefresh;
    }

    public boolean isNeedToRefresh() {
        return isNeedToRefresh;
    }

    public void setNeedToRefresh(boolean needToRefresh) {
        isNeedToRefresh = needToRefresh;
    }

    @Override
    public String toString() {
        return "UpdateFaceListEvent{" +
                "isNeedToRefresh=" + isNeedToRefresh +
                '}';
    }
}
