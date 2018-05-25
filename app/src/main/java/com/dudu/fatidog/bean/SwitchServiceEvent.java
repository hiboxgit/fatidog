package com.dudu.fatidog.bean;

/**
 * Author: Robert
 * Date:  2017-05-15
 * Copyright (c) 2016,dudu Co.,Ltd. All rights reserved.
 * Desc:
 */
public class SwitchServiceEvent {
    private boolean isOpen;

    public SwitchServiceEvent(boolean isOpen) {
        this.isOpen = isOpen;
    }

    public boolean isOpen() {
        return isOpen;
    }

    public void setOpen(boolean open) {
        isOpen = open;
    }
}
