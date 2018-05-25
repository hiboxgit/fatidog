package com.dudu.convert.bean;

/**
 * Author: Robert
 * Date:  2016-12-16
 * Copyright (c) 2016,dudu Co.,Ltd. All rights reserved.
 * Desc:
 */
public class FatiDogVersionInfoBean {

    private int returnCode; //返回码 例如： 0x0000

    private String hardwareVersion;
    private String firmwareVersion;
    private String softwareVersion;


    public FatiDogVersionInfoBean() {
    }

    public FatiDogVersionInfoBean(int returnCode, String hardwareVersion, String firmwareVersion, String softwareVersion) {
        this.returnCode = returnCode;
        this.hardwareVersion = hardwareVersion;
        this.firmwareVersion = firmwareVersion;
        this.softwareVersion = softwareVersion;
    }

    public int getReturnCode() {
        return returnCode;
    }

    public void setReturnCode(int returnCode) {
        this.returnCode = returnCode;
    }

    public String getHardwareVersion() {
        return hardwareVersion;
    }

    public void setHardwareVersion(String hardwareVersion) {
        this.hardwareVersion = hardwareVersion;
    }

    public String getFirmwareVersion() {
        return firmwareVersion;
    }

    public void setFirmwareVersion(String firmwareVersion) {
        this.firmwareVersion = firmwareVersion;
    }

    public String getSoftwareVersion() {
        return softwareVersion;
    }

    public void setSoftwareVersion(String softwareVersion) {
        this.softwareVersion = softwareVersion;
    }
}
