package com.dudu.serialcom.manager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Author: Robert
 * Date:  2016-08-30
 * Copyright (c) 2016,dudu Co.,Ltd. All rights reserved.
 * Desc: GPIO控制工具类
 */
public class GpioControlManager {
    private static Logger log = LoggerFactory.getLogger("FatiDog.serialcom.GpioControlManager");

    public static final String HIGH = "1";
    public static final String LOW = "0";

//    public static final String POWERON_OBD = "/sys/devices/soc.0/obd_gpio.68/obd_power_enable";
//    public static final String POWERON_TPMS = "/sys/devices/soc.0/obd_gpio.68/tire_power_enable";

    public static final String POWERON_OBD = "/sys/bus/platform/devices/soc.0/obd_gpio.68/obd_power_enable";
    public static final String POWERON_TPMS = "/sys/bus/platform/devices/soc.0/obd_gpio.68/tire_power_enable";

    public static final String WAKE_OBD = "/sys/devices/soc.0/obd_gpio.68/obd_wakeup";
    public static final String RESET_OBD = "/sys/bus/platform/devices/obd_gpio.68/obd_reset_enable";
    public static final String POWERON_GPS = "/sys/bus/platform/devices/obd_gpio.68/gps_ant_enable";

    private static GpioControlManager instance = null;

    public GpioControlManager() {
    }

    public static GpioControlManager getInstance(){
        if(instance == null){
            instance = new GpioControlManager();
        }
        return instance;
    }

    /**
     * 向device指定的设备中输入数据value.
     *
     * @param device the device
     * @param value  the value
     * @return the boolean
     */
    public boolean writeDevice(String device, String value) {
        try {
            BufferedWriter bufWriter = null;
            bufWriter = new BufferedWriter(new FileWriter(device));
            bufWriter.write(value);  // 写操作
            bufWriter.close();
            log.info("write ok");
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            log.info("write error");
            return false;
        }
    }

    public void wakeObd() {
//        log.info("唤醒obd开始");
        if (writeDevice(WAKE_OBD, HIGH)) {
//            log.info("唤醒obd完成");
        } else {
//            log.info("唤醒obd失败");
        }
    }

    public void powerOnFatidog() {
        log.info("fatidog 上电");
        powerOnObd();
    }

    public void powerOnObd() {
        if (writeDevice(POWERON_OBD, HIGH)) {
        } else {
        }
    }

    public void powerOffFatidog() {
        log.info("fatidog 下电");
        powerOffObd();
    }

    public void powerOffObd() {
        if (writeDevice(POWERON_OBD, LOW)) {
        } else {
        }
    }
    public void powerOnTpms() {
        log.info("TPMS上电");
        if (writeDevice(POWERON_TPMS, HIGH)) {
            log.info("TPMS上电完成");
        } else {
            log.info("TPMS上电失败");
        }
    }

    public void powerOffTpms() {
        log.info("TPMS下电");
        if (writeDevice(POWERON_TPMS, LOW)) {
            log.info("TPMS下电完成");
        } else {
            log.info("TPMS下电失败");
        }
    }

    public void powerOnGps() {
        log.info("GPS上电开始");
        if (writeDevice(POWERON_GPS, HIGH)) {
            log.info("GPS上电完成");
        } else {
            log.info("GPS上电失败");
        }
    }

    public void powerOffGPS() {
        log.info("GPS下电开始");
        if (writeDevice(POWERON_GPS, LOW)) {
            log.info("GPS下电完成");
        } else {
            log.info("GPS下电失败");
        }
    }
}
