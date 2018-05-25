package com.dudu.fatidog.map;

import android.content.Context;

import com.amap.api.location.AMapLocation;
import com.dudu.fatidog.map.bean.SpeedChangeEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.greenrobot.event.EventBus;

/**
 * Author: Robert
 * Date:  2017-03-21
 * Copyright (c) 2016,dudu Co.,Ltd. All rights reserved.
 * Desc:
 */
public class MapManager implements ILocationListener{
    private Logger logger = LoggerFactory.getLogger("FatiDog.map.MapManager");
    private static MapManager instance = null;
    private ILocation iLocation;
    private float speed = 0; //单位是m/s

    public MapManager() {
        iLocation = new CarMapLocation();
        iLocation.setLocationListener(this);
    }

    public static MapManager getInstance(){
        if(instance == null){
            instance = new MapManager();
        }
        return instance;
    }
    public void init(Context context) {
        logger.info("地图初始化启动..");
        iLocation.startLocation(context);
    }

    public void release(){
        logger.info("地图停止定位..");
        iLocation.stopLocation();
    }

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    @Override
    public void onLocationResult(Object locationInfo) {
        if(locationInfo instanceof AMapLocation){
            AMapLocation aMapLocation = (AMapLocation)locationInfo;
            speed = aMapLocation.getSpeed();

            EventBus.getDefault().post(new SpeedChangeEvent(speed*3.6f));
        }else {
            logger.error("定位回调异常");
        }

    }
}
