package com.dudu.recoac.bean;

/**
 * Author: Robert
 * Date:  2017-05-16
 * Copyright (c) 2016,dudu Co.,Ltd. All rights reserved.
 * Desc:
 */
public class FaceRecogAddFaceEvent {
    private int type; //采集人脸类型， 正脸or侧脸， 1是正脸，2是侧脸
    private boolean isStart;

    public FaceRecogAddFaceEvent(int type, boolean isStart) {
        this.type = type;
        this.isStart = isStart;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public boolean isStart() {
        return isStart;
    }

    public void setStart(boolean start) {
        isStart = start;
    }

    @Override
    public String toString() {
        return "FaceRecogAddFaceEvent{" +
                "type=" + type +
                ", isStart=" + isStart +
                '}';
    }
}
