package com.dudu.convert.bean;

/**
 * Author: Robert
 * Date:  2017-02-09
 * Copyright (c) 2016,dudu Co.,Ltd. All rights reserved.
 * Desc:
 */
public class FaceRecoRegPersonCountBean {
    private int returnCode;
    private int personCount;

    public FaceRecoRegPersonCountBean(int returnCode, int personCount) {
        this.returnCode = returnCode;
        this.personCount = personCount;
    }

    public int getReturnCode() {
        return returnCode;
    }

    public void setReturnCode(int returnCode) {
        this.returnCode = returnCode;
    }

    public int getPersonCount() {
        return personCount;
    }

    public void setPersonCount(int personCount) {
        this.personCount = personCount;
    }

    @Override
    public String toString() {
        return "FaceRecoRegPersonCountBean{" +
                "returnCode=" + returnCode +
                ", personCount=" + personCount +
                '}';
    }
}
