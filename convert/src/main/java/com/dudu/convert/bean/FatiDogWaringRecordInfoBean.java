package com.dudu.convert.bean;

/**
 * Author: Robert
 * Date:  2016-12-16
 * Copyright (c) 2016,dudu Co.,Ltd. All rights reserved.
 * Desc:
 */
public class FatiDogWaringRecordInfoBean {

    private int returnCode; //返回码 例如： 0x0000

    private byte[] md5;
    private int totalSize;
    private int timeStampLow; //告警时间戳低位
    private int timeStampHigh; //告警时间戳高位
    private int frameRate;
    private int duration;
    private int offset;
    private int size;
    private byte[] recordFileBytes;
}
