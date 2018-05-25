package com.dudu.recoac.bean;

/**
 * Author: Robert
 * Date:  2017-02-10
 * Copyright (c) 2016,dudu Co.,Ltd. All rights reserved.
 * Desc:
 */
public class UnTrustUserEvent {
    private long personId;
    private String snapPath;

    public UnTrustUserEvent(long personId, String snapPath) {
        this.personId = personId;
        this.snapPath = snapPath;
    }

    public UnTrustUserEvent() {
    }

    public long getPersonId() {
        return personId;
    }

    public void setPersonId(long personId) {
        this.personId = personId;
    }

    public String getSnapPath() {
        return snapPath;
    }

    public void setSnapPath(String snapPath) {
        this.snapPath = snapPath;
    }
}
