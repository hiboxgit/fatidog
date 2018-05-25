package com.dudu.fatidog.core;

import com.dudu.fatidog.map.MapManager;
import com.dudu.fatidog.util.FatiDogConstants;

import org.slf4j.LoggerFactory;

/**
 * Author: Robert
 * Date:  2017-01-05
 * Copyright (c) 2016,dudu Co.,Ltd. All rights reserved.
 * Desc: 疲劳预警规则管理器
 */
public class FatiDogWarningManager {
    private static org.slf4j.Logger logger = LoggerFactory.getLogger("FatiDog.app.FatiDogWarningManager");
    private static float CAR_DRVING_SPEED_LIMIT_L = 30.0f; //驾驶速度限制 km/h
    private static float CAR_DRVING_SPEED_LIMIT_H = 70.0f; //驾驶速度限制 km/h
    private static int CAR_DRIVING_TIME_LIMIT = 5 * 60 * 1000; //驾驶时长限制 min
    private long lastWarningTime = 0;
    private long lastWarningShowTime = 0;
    private int lastWarningType = 0;

    private static FatiDogWarningManager instance = null;
    public static FatiDogWarningManager getInstance(){
        if(instance == null){
            instance = new FatiDogWarningManager();
        }
        return instance;
    }

    public FatiDogWarningManager() {
    }
    public boolean isTimeToShow(){
        long curTime = System.currentTimeMillis();
        long period = curTime - getLastWarningShowTime();
        if(period > FatiDogConstants.FATIDOG_WARNING_KEEP_ON_TIME){
            setLastWarningShowTime(curTime);
            return true;
        }else{
            return false;
        }
    }

    public boolean isNeedToWarn(int warningType){
        if(!CarFireManager.getInstance().hasFired()){
            return false;
        }
        if(warningType<1 || warningType>3){
            return false;
        }

        if(FatiDogValueHelper.getInstance().isExperienceMode()){
            logger.info("体验模式放开驾驶时间和行车速度判断");
            return true;
        }

        if(!isTimeToShow()){
            return false;
        }

        float speed = MapManager.getInstance().getSpeed()*3.6f; // m/s -> km/h
        long timeGone = CarFireManager.getInstance().getTimeHasFired();
        logger.info("当前-车速为： {}km/h, 点火已经过去的时间为： {}分钟",speed,timeGone/(60*1000));
        if(speed > CAR_DRVING_SPEED_LIMIT_H){
            return true;
        } else if(speed>CAR_DRVING_SPEED_LIMIT_L  && timeGone>CAR_DRIVING_TIME_LIMIT){
            return true;
        }else{
            return false;
        }
    }
    public long getLastWarningTime() {
        return lastWarningTime;
    }

    public void setLastWarningTime(long lastWarningTime) {
        this.lastWarningTime = lastWarningTime;
    }

    public long getLastWarningShowTime() {
        return lastWarningShowTime;
    }

    public void setLastWarningShowTime(long lastWarningShowTime) {
        this.lastWarningShowTime = lastWarningShowTime;
    }

    public int getLastWarningType() {
        return lastWarningType;
    }

    public void setLastWarningType(int lastWarningType) {
        this.lastWarningType = lastWarningType;
    }

    public static float getCarDrvingSpeedLimitL() {
        return CAR_DRVING_SPEED_LIMIT_L;
    }

    public static void setCarDrvingSpeedLimitL(float carDrvingSpeedLimitL) {
        CAR_DRVING_SPEED_LIMIT_L = carDrvingSpeedLimitL;
    }

    public static float getCarDrvingSpeedLimitH() {
        return CAR_DRVING_SPEED_LIMIT_H;
    }

    public static void setCarDrvingSpeedLimitH(float carDrvingSpeedLimitH) {
        CAR_DRVING_SPEED_LIMIT_H = carDrvingSpeedLimitH;
    }

    public static int getCarDrivingTimeLimit() {
        return CAR_DRIVING_TIME_LIMIT;
    }

    public static void setCarDrivingTimeLimit(int carDrivingTimeLimit) {
        CAR_DRIVING_TIME_LIMIT = carDrivingTimeLimit;
    }
}
