package com.dudu.serialcom.manager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.security.InvalidParameterException;

import android_serialport_api.SerialPort;

/**
 * Author: Robert
 * Date:  2016-12-15
 * Copyright (c) 2016,dudu Co.,Ltd. All rights reserved.
 * Desc:
 */
public class SerialHelper {

    private static Logger logger = LoggerFactory.getLogger("FatiDog.serilacom.SerialHelper");

    private static final String NODE_FATIDOG = "/dev/ttyHSL1"; //疲劳预警设备节点
    private static final int BAUDRATE_FATIDOG = 115200; //疲劳预警通信波特率
    private SerialPort fatiDogCom = null;

    private static SerialHelper instance = null;

    public static SerialHelper getInstance() {
        if(instance == null){
            instance = new SerialHelper();
        }
        return instance;
    }

    private SerialHelper() {

    }

    public SerialPort openFatiDogCom() {
        if (fatiDogCom == null) {
            try {
                fatiDogCom = getSerialPort(NODE_FATIDOG, BAUDRATE_FATIDOG);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return fatiDogCom;
    }

    public void closeFatiDogCom() {
        try{
            if (fatiDogCom != null) {
                fatiDogCom.close();
                fatiDogCom = null;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private SerialPort getSerialPort(String path, int baudrate) throws SecurityException, IOException, InvalidParameterException {
        /* Read serial port parameters */
        logger.info("config:path=" + path + " bandrate=" + baudrate);
        /* Check parameters */
        if ((path.length() == 0) || (baudrate == -1)) {
            throw new InvalidParameterException();
        }

        /* Open the serial port */
        return new SerialPort(new File(path), baudrate, 0);
    }
}
