package com.dudu.fatidog.core;

import android.content.Context;

import com.dudu.commonlib.CommonLib;
import com.dudu.commonlib.utils.file.SharedPreferencesUtil;
import com.dudu.fatidog.bean.FatiDogControlBean;
import com.dudu.fatidog.bean.FatiDogExistStatusEvent;
import com.dudu.fatidog.map.MapManager;
import com.dudu.fatidog.map.bean.SpeedChangeEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.greenrobot.event.EventBus;

/**
 * Author: Robert
 * Date:  2016-12-20
 * Copyright (c) 2016,dudu Co.,Ltd. All rights reserved.
 * Desc:
 */
public class FatiDogValueHelper {

    private Logger logger = LoggerFactory.getLogger("FatiDog.app.FatiDogValueHelper");

    public static final String KEY_IS_FATIDOG_FIRST_OPEN = "IS_FATIDOG_FIRST_OPEN"; //是否是首次启动
    public static final String KEY_IS_OPEN = "FATIDOG_IS_OPEN";
    public static final String KEY_IS_BOOT_START = "FATIDOG_IS_BOOT_START";
    public static final String KEY_IS_EXPERIENCE_MODE = "FATIDOG_IS_EXPERIENCE_MODE";
    public static final String KEY_IS_FACE_SCANED = "FATIDOG_IS_FACE_SCANED";
//    public static final String KEY_IS_DEVICE_CONNECTED = "FATIDOG_IS_DEVICE_CONNECTED"; //无需保存，每次上点默认未连接

    private Context context = null;
    private FatiDogControlBean fatiDogControlData = null;
    private int speedType = -1; //车速范围类型
    private int speedMode = -1; //车速敏感度
    private boolean isManualWarnSet = false; //是否使用手动设置
    private static FatiDogValueHelper instance = null;

    public FatiDogValueHelper() {
        fatiDogControlData = new FatiDogControlBean();
    }

    public static FatiDogValueHelper getInstance(){
        if(instance == null){
            instance = new FatiDogValueHelper();
        }
        return instance;
    }
    public void initConfig(Context context){
        logger.info("初始化疲劳预警模块初始化参数");
        this.context = context;
        boolean isFatiDogFirstOpened = SharedPreferencesUtil.getBooleanValue(context,KEY_IS_FATIDOG_FIRST_OPEN,true);

        boolean isBootStart = SharedPreferencesUtil.getBooleanValue(context,KEY_IS_BOOT_START,false);
        fatiDogControlData.setBootStart(isBootStart);

        if(isBootStart){
            fatiDogControlData.setOpen(true);
            SharedPreferencesUtil.putBooleanValue(context,KEY_IS_OPEN,fatiDogControlData.isOpen());
        }else{
            boolean isOpen = SharedPreferencesUtil.getBooleanValue(context,KEY_IS_OPEN,false);
            fatiDogControlData.setOpen(isOpen);
        }

        boolean isExperienceMode = SharedPreferencesUtil.getBooleanValue(context,KEY_IS_EXPERIENCE_MODE,false);
        fatiDogControlData.setExperienceMode(isExperienceMode);

        boolean isFaceScaned = SharedPreferencesUtil.getBooleanValue(context,KEY_IS_FACE_SCANED,false);
        fatiDogControlData.setFaceScaned(isFaceScaned);

        fatiDogControlData.setDeviceConnected(false);

        if(isFatiDogFirstOpened){ //首次启动需要保存

            SharedPreferencesUtil.putBooleanValue(context,KEY_IS_FATIDOG_FIRST_OPEN,false);
            SharedPreferencesUtil.putBooleanValue(context,KEY_IS_OPEN,fatiDogControlData.isOpen()); //true);//
            SharedPreferencesUtil.putBooleanValue(context,KEY_IS_BOOT_START,fatiDogControlData.isBootStart());
            SharedPreferencesUtil.putBooleanValue(context,KEY_IS_EXPERIENCE_MODE,fatiDogControlData.isExperienceMode());
            SharedPreferencesUtil.putBooleanValue(context,KEY_IS_FACE_SCANED,fatiDogControlData.isFaceScaned());
        }
    }

    public boolean isFaceScaned(){
        if(fatiDogControlData != null){
            return fatiDogControlData.isFaceScaned();
        }else{
            return false;
        }
    }

    public FatiDogControlBean getFatiDogControlData() {
        return fatiDogControlData;
    }

    public void setFatiDogControlData(FatiDogControlBean fatiDogControlData) {
        this.fatiDogControlData = fatiDogControlData;
        saveFatiDogControlData();
    }

    public void setFaceScaned(boolean isScaned){
        fatiDogControlData.setFaceScaned(isScaned);
        setFatiDogControlData(fatiDogControlData);
    }
    public boolean isDeviceConnected(){
        return fatiDogControlData.isDeviceConnected();
    }
    public void updateDeviceConnectStatus(boolean isConnected){
        logger.info("更新设备在线状态: {}",isConnected);
        fatiDogControlData.setDeviceConnected(isConnected);

        EventBus.getDefault().post(new FatiDogExistStatusEvent(isConnected));
    }
    public boolean isOpen(){
        return fatiDogControlData.isOpen();
    }

    public void setOpen(boolean isSet){
        fatiDogControlData.setOpen(isSet);
        SharedPreferencesUtil.putBooleanValue(context,KEY_IS_OPEN,fatiDogControlData.isOpen());
    }

    public boolean isExperienceMode(){
        return fatiDogControlData.isExperienceMode();
    }

    public void setExperienceMode(boolean isSet){
        fatiDogControlData.setExperienceMode(isSet);

        //打开关闭体验模式更新一下速度模式
        if(isSet){
            FatiDogWorkFlow.getInstance().updateWarnModeSet(180f);
        }else{
            FatiDogWorkFlow.getInstance().updateWarnModeSet(MapManager.getInstance().getSpeed()*3.6f);
        }

    }

    private void saveFatiDogControlData(){
        if(context == null){
            context = CommonLib.getInstance().getContext();
        }
        boolean isFatiDogFirstOpened = SharedPreferencesUtil.getBooleanValue(context,KEY_IS_FATIDOG_FIRST_OPEN,true);
        if(isFatiDogFirstOpened) { //首次启动需要保存
            SharedPreferencesUtil.putBooleanValue(context,KEY_IS_FATIDOG_FIRST_OPEN,false);
        }
        SharedPreferencesUtil.putBooleanValue(context,KEY_IS_OPEN,fatiDogControlData.isOpen());
        SharedPreferencesUtil.putBooleanValue(context,KEY_IS_BOOT_START,fatiDogControlData.isBootStart());
//        SharedPreferencesUtil.putBooleanValue(context,KEY_IS_EXPERIENCE_MODE,fatiDogControlData.isExperienceMode());
        SharedPreferencesUtil.putBooleanValue(context,KEY_IS_FACE_SCANED,fatiDogControlData.isFaceScaned());
    }

    public void reInitVaule(){
        speedType = 0;
        speedMode = 0;
    }

    public int getFatidogSpeedType(float speed){
        int type = 0;
        if( (speed>=0f) && (speed<10f)){
            type = 0;
        }else if( (speed>=10f) && (speed<40f)){
            type = 1;
        }else if( (speed>=40f) && (speed<80f)){
            type = 2;
        }else if( (speed>=80f) && (speed<255f)){
            type = 3;
        }else{
            type = 3;
        }
        return type;
    }

    public int getFatidogSpeedMode(int speedType){
        int mode = 0;

        if(speedType == 0){
            mode = 0; //低灵敏度
        }else if(speedType == 1){
            mode = 0; //低灵敏度
        }else if(speedType == 2){
            mode = 2; //中灵敏度
        }else {//if(speedType == 3)
            mode = 1; //高灵敏度
        }
        return mode;
    }

    public int getSpeedType() {
        return speedType;
    }

    public void setSpeedType(int speedType) {
        this.speedType = speedType;
    }

    public int getSpeedMode() {
        return speedMode;
    }

    public void setSpeedMode(int speedMode) {
        this.speedMode = speedMode;
    }

    public boolean isManualWarnSet() {
        return isManualWarnSet;
    }

    public void setManualWarnSet(boolean manualWarnSet) {
        isManualWarnSet = manualWarnSet;
    }
}
