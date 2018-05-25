package com.dudu.fatidog.bean;

/**
 * Author: Robert
 * Date:  2017-06-08
 * Copyright (c) 2016,dudu Co.,Ltd. All rights reserved.
 * Desc:
 */
public class FatidogExitEvent {
    private boolean isNeedToExit; //是否需要退出

    public FatidogExitEvent(boolean isNeedToExit) {
        this.isNeedToExit = isNeedToExit;
    }

    public boolean isNeedToExit() {
        return isNeedToExit;
    }

    public void setNeedToExit(boolean needToExit) {
        isNeedToExit = needToExit;
    }

    @Override
    public String toString() {
        return "FatidogExitEvent{" +
                "isNeedToExit=" + isNeedToExit +
                '}';
    }
}
