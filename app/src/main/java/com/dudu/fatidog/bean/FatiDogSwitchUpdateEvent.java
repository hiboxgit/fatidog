package com.dudu.fatidog.bean;

/**
 * Author: Robert
 * Date:  2017-05-16
 * Copyright (c) 2016,dudu Co.,Ltd. All rights reserved.
 * Desc:
 */
public class FatiDogSwitchUpdateEvent {
    private int index; //开关索引： 1:疲劳预警开关 2：开机启动开关 3:体验模式开关
    private boolean isOpen; //是否打开

    public FatiDogSwitchUpdateEvent(int index, boolean isOpen) {
        this.index = index;
        this.isOpen = isOpen;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public boolean isOpen() {
        return isOpen;
    }

    public void setOpen(boolean open) {
        isOpen = open;
    }

    @Override
    public String toString() {
        return "FatiDogSwitchUpdateEvent{" +
                "index=" + index +
                ", isOpen=" + isOpen +
                '}';
    }
}
