package com.dudu.recoac.bean;

/**
 * Author: Robert
 * Date:  2017-02-10
 * Copyright (c) 2016,dudu Co.,Ltd. All rights reserved.
 * Desc:
 */
public class FaceAddEvent {
    private int index;
    private int faceType; //1:正脸 2：侧脸
    private boolean isSuccess;

    public FaceAddEvent() {
    }

    public FaceAddEvent(int index, int faceType, boolean isSuccess) {
        this.index = index;
        this.faceType = faceType;
        this.isSuccess = isSuccess;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getFaceType() {
        return faceType;
    }

    public void setFaceType(int faceType) {
        this.faceType = faceType;
    }

    public boolean isSuccess() {
        return isSuccess;
    }

    public void setSuccess(boolean success) {
        isSuccess = success;
    }

    @Override
    public String toString() {
        return "FaceAddEvent{" +
                "index=" + index +
                ", faceType=" + faceType +
                ", isSuccess=" + isSuccess +
                '}';
    }
}
