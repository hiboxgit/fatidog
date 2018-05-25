package com.dudu.convert.bean;

/**
 * Author: Robert
 * Date:  2016-12-15
 * Copyright (c) 2016,dudu Co.,Ltd. All rights reserved.
 * Desc:
 */
public class FatiDogResponseBean {
    private int msgType; //报文类型 例如： 0x12
    private int cmdType; //命令码 例如: 0x0901
    private int returnCode; //返回码 例如： 0x0000

    public FatiDogResponseBean() {
    }

    public FatiDogResponseBean(int msgType, int cmdType, int returnCode) {
        this.msgType = msgType;
        this.cmdType = cmdType;
        this.returnCode = returnCode;
    }

    public int getMsgType() {
        return msgType;
    }

    public void setMsgType(int msgType) {
        this.msgType = msgType;
    }

    public int getCmdType() {
        return cmdType;
    }

    public void setCmdType(int cmdType) {
        this.cmdType = cmdType;
    }

    public int getReturnCode() {
        return returnCode;
    }

    public void setReturnCode(int returnCode) {
        this.returnCode = returnCode;
    }

    @Override
    public String toString() {
        return "FatiDogResponseBean{" +
                "msgType=" + msgType +
                ", cmdType=" + cmdType +
                ", returnCode=" + returnCode +
                '}';
    }
}
