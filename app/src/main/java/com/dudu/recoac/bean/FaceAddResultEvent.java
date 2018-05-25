package com.dudu.recoac.bean;

/**
 * Author: Robert
 * Date:  2017-05-16
 * Copyright (c) 2016,dudu Co.,Ltd. All rights reserved.
 * Desc:
 */
public class FaceAddResultEvent {
    private int type; //正脸or侧脸 1：正 2：侧
    private boolean isOk; //成功OR失败

    public FaceAddResultEvent(int type, boolean isOk) {
        this.type = type;
        this.isOk = isOk;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public boolean isOk() {
        return isOk;
    }

    public void setOk(boolean ok) {
        isOk = ok;
    }

    @Override
    public String toString() {
        return "FaceAddResultEvent{" +
                "type=" + type +
                ", isOk=" + isOk +
                '}';
    }
}
