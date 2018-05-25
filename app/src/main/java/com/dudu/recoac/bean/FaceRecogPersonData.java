package com.dudu.recoac.bean;

/**
 * Author: Robert
 * Date:  2017-02-09
 * Copyright (c) 2016,dudu Co.,Ltd. All rights reserved.
 * Desc:
 */
public class FaceRecogPersonData {
    private long personId; //人员注册ID
    private String name; //人员名称
    private String card; //证件号
    private String regTime; //注册时间

    private boolean hasRegisteredFace; //是否已经注册人脸

    public FaceRecogPersonData(long personId, String name, String card, String regTime, boolean hasRegisteredFace) {
        this.personId = personId;
        this.name = name;
        this.card = card;
        this.regTime = regTime;
        this.hasRegisteredFace = hasRegisteredFace;
    }

    public FaceRecogPersonData() {
    }

    public long getPersonId() {
        return personId;
    }

    public void setPersonId(long personId) {
        this.personId = personId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCard() {
        return card;
    }

    public void setCard(String card) {
        this.card = card;
    }

    public String getRegTime() {
        return regTime;
    }

    public void setRegTime(String regTime) {
        this.regTime = regTime;
    }

    public boolean isHasRegisteredFace() {
        return hasRegisteredFace;
    }

    public void setHasRegisteredFace(boolean hasRegisteredFace) {
        this.hasRegisteredFace = hasRegisteredFace;
    }

    @Override
    public String toString() {
        return "FaceRecogPersonData{" +
                "personId=" + personId +
                ", name='" + name + '\'' +
                ", card='" + card + '\'' +
                ", regTime='" + regTime + '\'' +
                ", hasRegisteredFace=" + hasRegisteredFace +
                '}';
    }
}
