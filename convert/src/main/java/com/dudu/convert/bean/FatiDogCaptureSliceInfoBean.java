package com.dudu.convert.bean;

import java.util.Arrays;

/**
 * Author: Robert
 * Date:  2017-02-07
 * Copyright (c) 2016,dudu Co.,Ltd. All rights reserved.
 * Desc:
 */
public class FatiDogCaptureSliceInfoBean {

    private int returnCode; //返回码 例如： 0x0000
    private int totalSize;
    private byte[] md5;
    private int offset;
    private int size;
    private byte[] imageData;
    private int sliceIndex; //第一个分片的话为0，最后一个分片为1，中间分片为2 //最后一个分片状态优先

    public FatiDogCaptureSliceInfoBean() {
    }

    public FatiDogCaptureSliceInfoBean(int returnCode, int totalSize, byte[] md5, int offset, int size, byte[] imageData,int sliceIndex) {
        this.returnCode = returnCode;
        this.totalSize = totalSize;
        this.md5 = md5;
        this.offset = offset;
        this.size = size;
        this.imageData = imageData;
        this.sliceIndex = sliceIndex;
    }

    public int getReturnCode() {
        return returnCode;
    }

    public void setReturnCode(int returnCode) {
        this.returnCode = returnCode;
    }

    public int getTotalSize() {
        return totalSize;
    }

    public void setTotalSize(int totalSize) {
        this.totalSize = totalSize;
    }

    public byte[] getMd5() {
        return md5;
    }

    public void setMd5(byte[] md5) {
        this.md5 = md5;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public byte[] getImageData() {
        return imageData;
    }

    public void setImageData(byte[] imageData) {
        this.imageData = imageData;
    }

    public int getSliceIndex() {
        return sliceIndex;
    }

    public void setSliceIndex(int sliceIndex) {
        this.sliceIndex = sliceIndex;
    }

    @Override
    public String toString() {
        return "FatiDogCaptureSliceInfoBean{" +
                "returnCode=" + returnCode +
                ", totalSize=" + totalSize +
                ", offset=" + offset +
                ", size=" + size +
                ", sliceIndex=" + sliceIndex +
                ", md5=" + Arrays.toString(md5) +
                ", imageData=" + Arrays.toString(imageData) +
                '}';
    }
}
