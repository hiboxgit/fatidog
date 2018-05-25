package com.dudu.fatidog.bean;

/**
 * Author: Robert
 * Date:  2017-03-23
 * Copyright (c) 2016,dudu Co.,Ltd. All rights reserved.
 * Desc:
 */
public class VoiceControlEvent {
    private VoiceControlType type;

    public VoiceControlEvent(VoiceControlType type) {
        this.type = type;
    }

    public VoiceControlType getType() {
        return type;
    }

    public void setType(VoiceControlType type) {
        this.type = type;
    }

    public enum VoiceControlType{
        DEFAULT_TYPE,
        EXIT_FATIDOG, //1: 退出疲劳预警
        EXIT_RECOG //2： 退出人脸识别
    }
}
