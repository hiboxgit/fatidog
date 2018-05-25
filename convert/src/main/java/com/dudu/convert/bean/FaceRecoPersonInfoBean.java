package com.dudu.convert.bean;

/**
 * Author: Robert
 * Date:  2017-02-09
 * Copyright (c) 2016,dudu Co.,Ltd. All rights reserved.
 * Desc:
 */
public class FaceRecoPersonInfoBean {
    private long personId; //内部保存的人员 ID //长度8
    private String name; //中文必须使用 UTF8 编码 //长度64
    private String card; //证件号， 支持数字和字母 //长度20
    private String regTime; //人员的注册时间 //长度20
    private String attr1; //人员属性自定义字段，不使用都设 0 //长度48
    private String attr2; //人员属性自定义字段，不使用都设 0 //长度48
    private String attr3; //人员属性自定义字段，不使用都设 0 //长度48
    private String attr4; //人员属性自定义字段，不使用都设 0 //长度48
    private String attr5; //人员属性自定义字段，不使用都设 0 //长度48

    public FaceRecoPersonInfoBean() {
    }

    public FaceRecoPersonInfoBean(long personId, String name, String card, String regTime, String attr1, String attr2, String attr3, String attr4, String attr5) {
        this.personId = personId;
        this.name = name;
        this.card = card;
        this.regTime = regTime;
        this.attr1 = attr1;
        this.attr2 = attr2;
        this.attr3 = attr3;
        this.attr4 = attr4;
        this.attr5 = attr5;
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

    public String getAttr1() {
        return attr1;
    }

    public void setAttr1(String attr1) {
        this.attr1 = attr1;
    }

    public String getAttr2() {
        return attr2;
    }

    public void setAttr2(String attr2) {
        this.attr2 = attr2;
    }

    public String getAttr3() {
        return attr3;
    }

    public void setAttr3(String attr3) {
        this.attr3 = attr3;
    }

    public String getAttr4() {
        return attr4;
    }

    public void setAttr4(String attr4) {
        this.attr4 = attr4;
    }

    public String getAttr5() {
        return attr5;
    }

    public void setAttr5(String attr5) {
        this.attr5 = attr5;
    }

    @Override
    public String toString() {
        return "FaceRecoPersonInfoBean{" +
                "personId=" + personId +
                ", name='" + name + '\'' +
                ", card='" + card + '\'' +
                ", regTime='" + regTime + '\'' +
                ", attr1='" + attr1 + '\'' +
                ", attr2='" + attr2 + '\'' +
                ", attr3='" + attr3 + '\'' +
                ", attr4='" + attr4 + '\'' +
                ", attr5='" + attr5 + '\'' +
                '}';
    }
}
