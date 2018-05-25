package com.dudu.convert.bean;

/**
 * Author: Robert
 * Date:  2016-12-16
 * Copyright (c) 2016,dudu Co.,Ltd. All rights reserved.
 * Desc:
 */
public class FatiDogWarningBean {

    private int returnCode; //返回码 例如： 0x0000
    private int warningType; //告警类型 1： 请小心驾驶 2： 请正视前方3： 危险 4： 疲劳驾驶

    public FatiDogWarningBean() {
    }

    public FatiDogWarningBean(int returnCode, int warningType) {
        this.returnCode = returnCode;
        this.warningType = warningType;
    }

    public int getReturnCode() {
        return returnCode;
    }

    public void setReturnCode(int returnCode) {
        this.returnCode = returnCode;
    }

    public int getWarningType() {
        return warningType;
    }

    public void setWarningType(int warningType) {
        this.warningType = warningType;
    }
}
