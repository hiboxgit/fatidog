package com.dudu.recoac.bean;

/**
 * Author: Robert
 * Date:  2017-02-11
 * Copyright (c) 2016,dudu Co.,Ltd. All rights reserved.
 * Desc:
 */
public class FaceSwitchPageEvent {
    private int pageIndex;
    private int extra;

    public FaceSwitchPageEvent(int pageIndex, int extra) {
        this.pageIndex = pageIndex;
        this.extra = extra;
    }

    public FaceSwitchPageEvent(int pageIndex) {
        this.pageIndex = pageIndex;
        this.extra = 0;
    }

    public int getPageIndex() {
        return pageIndex;
    }

    @Override
    public String toString() {
        return "FaceSwitchPageEvent{" +
                "pageIndex=" + pageIndex +
                ", extra=" + extra +
                '}';
    }

    public void setPageIndex(int pageIndex) {
        this.pageIndex = pageIndex;
    }

    public int getExtra() {
        return extra;
    }

    public void setExtra(int extra) {
        this.extra = extra;
    }
}
