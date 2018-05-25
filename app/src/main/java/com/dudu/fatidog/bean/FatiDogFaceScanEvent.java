package com.dudu.fatidog.bean;

/**
 * Author: Robert
 * Date:  2017-01-04
 * Copyright (c) 2016,dudu Co.,Ltd. All rights reserved.
 * Desc:
 */
public class FatiDogFaceScanEvent {
    private int eventType;
    private String extra;

    public FatiDogFaceScanEvent() {
    }

    public FatiDogFaceScanEvent(int eventType, String extra) {
        this.eventType = eventType;
        this.extra = extra;
    }

    public int getEventType() {
        return eventType;
    }

    public void setEventType(int eventType) {
        this.eventType = eventType;
    }

    public String getExtra() {
        return extra;
    }

    public void setExtra(String extra) {
        this.extra = extra;
    }
}
