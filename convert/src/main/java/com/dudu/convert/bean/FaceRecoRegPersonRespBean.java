package com.dudu.convert.bean;

/**
 * Author: Robert
 * Date:  2017-02-09
 * Copyright (c) 2016,dudu Co.,Ltd. All rights reserved.
 * Desc:
 */
public class FaceRecoRegPersonRespBean {
    private int returnCode; //返回码 例如： 0x0000
    private long personId; //成功注册的人员 id

    public FaceRecoRegPersonRespBean() {
    }

    public FaceRecoRegPersonRespBean(int returnCode, long personId) {
        this.returnCode = returnCode;
        this.personId = personId;
    }

    public int getReturnCode() {
        return returnCode;
    }

    public void setReturnCode(int returnCode) {
        this.returnCode = returnCode;
    }

    public long getPersonId() {
        return personId;
    }


    public void setPersonId(long personId) {
        this.personId = personId;
    }

    @Override
    public String toString() {
        return "FaceRecoRegPersonRespBean{" +
                "returnCode=" + returnCode +
                ", personId=" + personId +
                '}';
    }
}
