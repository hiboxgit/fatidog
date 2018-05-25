package com.dudu.convert.bean;

/**
 * Author: Robert
 * Date:  2017-02-09
 * Copyright (c) 2016,dudu Co.,Ltd. All rights reserved.
 * Desc:
 */
public class FaceRecoFaceCheckBean {
    private int state; //状态码： 0 校验中， 1 校验成功， 2 超时， 3 校验失败
    private int verifID; //校验记录 id
    private int faceCount; //检测到的人员数量
    private int oppLeft; //人脸 1 在图片中的相对位置 left
    private int oppTop; //人脸 1 在图片中的相对位置 top
    private int oppRight; //人脸 1 在图片中的相对位置 right
    private int oppBottom; //人脸 1 在图片中的相对位置 bottom
    private int angle; //人脸 1 在图片中的角度，（ 0-100）100 表示最佳
    private int light; //人脸 1 在图片中的亮度，（ 0-100）100 表示最佳

    public FaceRecoFaceCheckBean() {
    }

    public FaceRecoFaceCheckBean(int state, int verifID, int faceCount, int oppLeft, int oppTop, int oppRight, int oppBottom, int angle, int light) {
        this.state = state;
        this.verifID = verifID;
        this.faceCount = faceCount;
        this.oppLeft = oppLeft;
        this.oppTop = oppTop;
        this.oppRight = oppRight;
        this.oppBottom = oppBottom;
        this.angle = angle;
        this.light = light;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public int getVerifID() {
        return verifID;
    }

    public void setVerifID(int verifID) {
        this.verifID = verifID;
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
        return "FaceRecoFaceCheckBean{" +
                "state=" + state +
                ", verifID=" + verifID +
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
