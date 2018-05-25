package com.dudu.fatidog.bean;

/**
 * Author: Robert
 * Date:  2016-12-20
 * Copyright (c) 2016,dudu Co.,Ltd. All rights reserved.
 * Desc:
 */
public class FatiDogControlBean {

    private boolean isOpen; //疲劳预警功能是否打开            //需要存储
    private boolean isBootStart; //是否开机启动               //需要存储
    private boolean isExperienceMode; //是否开启体验模式      //需要存储
    private boolean isFaceScaned; //是否已经刷脸              //需要存储
    private boolean isDeviceConnected; //是否设备已经连接     //不需要存储

    public FatiDogControlBean() {
    }

    public FatiDogControlBean(boolean isOpen, boolean isBootStart, boolean isExperienceMode, boolean isFaceScaned, boolean isDeviceConnected) {
        this.isOpen = isOpen;
        this.isBootStart = isBootStart;
        this.isExperienceMode = isExperienceMode;
        this.isFaceScaned = isFaceScaned;
        this.isDeviceConnected = isDeviceConnected;
    }

    public boolean isOpen() {
        return isOpen;
    }

    public void setOpen(boolean open) {
        isOpen = open;
    }

    public boolean isBootStart() {
        return isBootStart;
    }

    public void setBootStart(boolean bootStart) {
        isBootStart = bootStart;
    }

    public boolean isExperienceMode() {
        return isExperienceMode;
    }

    public void setExperienceMode(boolean experienceMode) {
        isExperienceMode = experienceMode;
    }

    public boolean isFaceScaned() {
        return isFaceScaned;
    }

    public void setFaceScaned(boolean faceScaned) {
        isFaceScaned = faceScaned;
    }

    public boolean isDeviceConnected() {
        return isDeviceConnected;
    }

    public void setDeviceConnected(boolean deviceConnected) {
        isDeviceConnected = deviceConnected;
    }
}
