package com.dudu.fatidog.bean;

/**
 * Author: Robert
 * Date:  2017-05-23
 * Copyright (c) 2016,dudu Co.,Ltd. All rights reserved.
 * Desc:
 */
public class RemoteControlEvent {
    private String action;
    private String extra;

    public RemoteControlEvent(String action) {
        this.action = action;
        this.extra = "";
    }

    public RemoteControlEvent(String action, String extra) {
        this.action = action;
        this.extra = extra;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getExtra() {
        return extra;
    }

    public void setExtra(String extra) {
        this.extra = extra;
    }

    @Override
    public String toString() {
        return "RemoteControlEvent{" +
                "action='" + action + '\'' +
                ", extra='" + extra + '\'' +
                '}';
    }
}
