package com.dudu.convert.bean;

/**
 * Author: Robert
 * Date:  2016-12-16
 * Copyright (c) 2016,dudu Co.,Ltd. All rights reserved.
 * Desc:
 */
public class FatiDogWaringRecordBean {

    private int returnCode; //返回码 例如： 0x0000

    private int warningType; //告警类型 1： 小心驾驶 2： 请正视前方3： 危险 4： 疲劳驾驶
    private int timeStampLow; //告警时间戳低位
    private int timeStampHigh; //告警时间戳高位
    private int videoId; //对应的录像 ID

    public FatiDogWaringRecordBean() {
    }

    public FatiDogWaringRecordBean(int returnCode, int warningType, int timeStampLow, int timeStampHigh, int videoId) {
        this.returnCode = returnCode;
        this.warningType = warningType;
        this.timeStampLow = timeStampLow;
        this.timeStampHigh = timeStampHigh;
        this.videoId = videoId;
    }

    public int getReturnCode() {
        return returnCode;
    }

    public void setReturnCode(int returnCode) {
        this.returnCode = returnCode;
    }

    public int getWarningType() {
        return warningType;
    }

    public void setWarningType(int warningType) {
        this.warningType = warningType;
    }

    public int getTimeStampLow() {
        return timeStampLow;
    }

    public void setTimeStampLow(int timeStampLow) {
        this.timeStampLow = timeStampLow;
    }

    public int getTimeStampHigh() {
        return timeStampHigh;
    }

    public void setTimeStampHigh(int timeStampHigh) {
        this.timeStampHigh = timeStampHigh;
    }

    public int getVideoId() {
        return videoId;
    }

    public void setVideoId(int videoId) {
        this.videoId = videoId;
    }
}
