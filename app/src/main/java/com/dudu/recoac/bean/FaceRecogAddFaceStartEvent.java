package com.dudu.recoac.bean;

/**
 * Author: Robert
 * Date:  2017-05-16
 * Copyright (c) 2016,dudu Co.,Ltd. All rights reserved.
 * Desc:
 */
public class FaceRecogAddFaceStartEvent {
    private boolean isStart;

    public FaceRecogAddFaceStartEvent(boolean isStart) {
        this.isStart = isStart;
    }

    public boolean isStart() {
        return isStart;
    }

    public void setStart(boolean start) {
        isStart = start;
    }
}
