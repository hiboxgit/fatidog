package com.dudu.convert.bean;

/**
 * Author: Robert
 * Date:  2017-02-09
 * Copyright (c) 2016,dudu Co.,Ltd. All rights reserved.
 * Desc:
 */
public class FaceRecoResultBean {
    private int returnCode; //返回码
    private int state; //状态码： 0 识别中， 1 识别成功， 2 超时， 3 识别失败（ 没有人员相匹配）
    private long recoID; //识别记录 id
    private long id; //识别到的人员 id,识别失败设置为 0.
    private int faceCount; //检测到的人员数量
    private int oppLeft; //人脸 1 在图片中的相对位置 left
    private int oppTop; //人脸 1 在图片中的相对位置 top
    private int oppRight; //人脸 1 在图片中的相对位置 right
    private int oppBottom; //人脸 1 在图片中的相对位置 bottom
    private int angle; //人脸 1 在图片中的角度，（ 0-100）100 表示最佳
    private int light; //人脸 1 在图片中的亮度，（ 0-100）100 表示最佳

    public FaceRecoResultBean() {
    }

    public FaceRecoResultBean(int returnCode, int state, long recoID, long id, int faceCount, int oppLeft, int oppTop, int oppRight, int oppBottom, int angle, int light) {
        this.returnCode = returnCode;
        this.state = state;
        recoID = recoID;
        this.id = id;
        this.faceCount = faceCount;
        this.oppLeft = oppLeft;
        this.oppTop = oppTop;
        this.oppRight = oppRight;
        this.oppBottom = oppBottom;
        this.angle = angle;
        this.light = light;
    }

    public int getReturnCode() {
        return returnCode;
    }

    public void setReturnCode(int returnCode) {
        this.returnCode = returnCode;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public long getRecoID() {
        return this.recoID;
    }

    public void setRecoID(long recoID) {
        this.recoID = recoID;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getFaceCount() {
        return faceCount;
    }

    public void setFaceCount(int faceCount) {
        this.faceCount = faceCount;
    }

    public int getOppLeft() {
        return oppLeft;
    }

    public void setOppLeft(int oppLeft) {
        this.oppLeft = oppLeft;
    }

    public int getOppTop() {
        return oppTop;
    }

    public void setOppTop(int oppTop) {
        this.oppTop = oppTop;
    }

    public int getOppRight() {
        return oppRight;
    }

    public void setOppRight(int oppRight) {
        this.oppRight = oppRight;
    }

    public int getOppBottom() {
        return oppBottom;
    }

    public void setOppBottom(int oppBottom) {
        this.oppBottom = oppBottom;
    }

    public int getAngle() {
        return angle;
    }

    public void setAngle(int angle) {
        this.angle = angle;
    }

    public int getLight() {
        return light;
    }

    public void setLight(int light) {
        this.light = light;
    }

    @Override
    public String toString() {
        return "FaceRecoResultBean{" +
                "returnCode=" + returnCode +
                ", state=" + state +
                ", RecoID=" + recoID +
                ", id=" + id +
                ", faceCount=" + faceCount +
                ", oppLeft=" + oppLeft +
                ", oppTop=" + oppTop +
                ", oppRight=" + oppRight +
                ", oppBottom=" + oppBottom +
                ", angle=" + angle +
                ", light=" + light +
                '}';
    }
}
