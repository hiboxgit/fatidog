package com.dudu.fatidog.bean;

/**
 * Author: Robert
 * Date:  2017-03-27
 * Copyright (c) 2016,dudu Co.,Ltd. All rights reserved.
 * Desc:
 */
public class FatiDogExistStatusEvent {
    private boolean isOnline;

    public FatiDogExistStatusEvent(boolean isOnline) {
        this.isOnline = isOnline;
    }

    public boolean isOnline() {
        return isOnline;
    }

    public void setOnline(boolean online) {
        isOnline = online;
    }
}
