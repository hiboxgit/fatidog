package com.dudu.fatidog.map;

import android.content.Context;
import android.location.LocationManager;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Author: Robert
 * Date:  2017-03-21
 * Copyright (c) 2016,dudu Co.,Ltd. All rights reserved.
 * Desc:
 */
public class CarMapLocation implements AMapLocationListener, ILocation{
    private Logger logger = LoggerFactory.getLogger("FatiDog.map.CarMapLocation");
    private boolean isStarted = false;
    private Context mContext;
    private AMapLocationClient locationClient = null;
    private AMapLocationClientOption locationOption = null;
    private LocationManager locationManager;
    private ILocationListener mILocationListener = null;

    @Override
    public void startLocation(Context context) {
        logger.info("开启定位   isStarted:{}", isStarted);
        if (!isStarted) {
            logger.info("设置定位的参数...");
            mContext = context;
            locationClient = new AMapLocationClient(mContext);
            locationOption = new AMapLocationClientOption();
            locationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
            locationClient.setLocationListener(this);
            locationOption.setNeedAddress(true);
            locationOption.setInterval(3000);
            locationClient.setLocationOption(locationOption);
            locationClient.startLocation();
            locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            isStarted = true;
        }else{
            logger.info("定位已经开启...");
        }
    }

    @Override
    public void stopLocation() {
        logger.info("停止定位");
        if (locationClient != null) {
            locationClient.unRegisterLocationListener(this);
            locationClient.stopLocation();
            locationClient.onDestroy();
        }
        locationManager = null;
        isStarted = false;
    }

    @Override
    public void setLocationListener(ILocationListener iLocationListener) {
        mILocationListener = iLocationListener;
    }

    @Override
    public boolean isLocation() {
        return locationClient != null && locationClient.isStarted();
    }

    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {
//        logger.info("onLocationChanged - getErrorCode: {},getErrorInfo: {}",aMapLocation.getErrorCode(),aMapLocation.getErrorInfo());
//        logger.info("onLocationChanged - 纬度: {},经度: {}",aMapLocation.getAltitude(),aMapLocation.getLongitude());
//        logger.info("onLocationChanged - getAddress: {},getCity: {}",aMapLocation.getAddress(),aMapLocation.getCity());
//        logger.info("onLocationChanged - getTime: {}",aMapLocation.getTime());
        if (mILocationListener != null && aMapLocation != null) {
            mILocationListener.onLocationResult(aMapLocation);
        }
    }
}
