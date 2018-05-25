package com.dudu.recoac.bean;

import java.util.Arrays;

/**
 * Author: Robert
 * Date:  2017-02-09
 * Copyright (c) 2016,dudu Co.,Ltd. All rights reserved.
 * Desc:
 */
public class FaceRecogRunningData {
    private FaceRecogPersonData[] whiteListPersons; //白名单人员列表 //即 人脸列表
    private boolean isOpen; //人脸识别功能是否打开

    public FaceRecogRunningData() {
    }

    public FaceRecogRunningData(FaceRecogPersonData[] whiteListPersons, boolean isOpen) {
        this.whiteListPersons = whiteListPersons;
        this.isOpen = isOpen;
    }

    public FaceRecogPersonData[] getWhiteListPersons() {
        return whiteListPersons;
    }

    public void setWhiteListPersons(FaceRecogPersonData[] whiteListPersons) {
        this.whiteListPersons = whiteListPersons;
    }

    public boolean isOpen() {
        return isOpen;
    }

    public void setOpen(boolean open) {
        isOpen = open;
    }

    @Override
    public String toString() {
        return "FaceRecogRunningData{" +
                "whiteListPersons=" + Arrays.toString(whiteListPersons) +
                ", isOpen=" + isOpen +
                '}';
    }
}
