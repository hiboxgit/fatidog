package com.dudu.fatidog.util;

import com.dudu.commonlib.utils.screen.PowerManagerUtils;

/**
 * Author: Robert
 * Date:  2017-01-04
 * Copyright (c) 2016,dudu Co.,Ltd. All rights reserved.
 * Desc: 屏保控制管理器
 */
public class ScreenSaverManager {
    private static String TAG = "ScreenSaverManager";
    private static ScreenSaverManager instance = null;
    private PowerManagerUtils powerManagerInstance = null;

    public ScreenSaverManager() {
        powerManagerInstance = new PowerManagerUtils();
    }

    public static ScreenSaverManager getInstance(){
        if(instance == null){
            instance = new ScreenSaverManager();
        }
        return instance;
    }

    public void wakeOnce(){
        powerManagerInstance.doUserActivity();
    }

    public void wakeKeepEnable(){
        powerManagerInstance.stopPreventScreanSaver(TAG); //每次都先停再开，确保只有一个。
        powerManagerInstance.startPreventScreanSaver(TAG);
    }

    public void wakeKeepDisable(){
        powerManagerInstance.stopPreventScreanSaver(TAG);
    }
}

