package com.dudu.fatidog.bean;

/**
 * Author: Robert
 * Date:  2017-01-11
 * Copyright (c) 2016,dudu Co.,Ltd. All rights reserved.
 * Desc:
 */
public class FatiDogUpdateViewEvent {
    private int eventType;

    public FatiDogUpdateViewEvent(int eventType) {
        this.eventType = eventType;
    }

    public int getEventType() {
        return eventType;
    }

    public void setEventType(int eventType) {
        this.eventType = eventType;
    }
}
