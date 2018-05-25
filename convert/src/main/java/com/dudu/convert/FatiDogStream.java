package com.dudu.convert;

import com.dudu.serialcom.manager.SerialHelper;
import com.dudu.serialcom.reader.FatiDogDataReader;

import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

import android_serialport_api.SerialPort;
import rx.Observable;
import rx.Subscriber;
import rx.observables.ConnectableObservable;

/**
 * Author: Robert
 * Date:  2016-12-15
 * Copyright (c) 2016,dudu Co.,Ltd. All rights reserved.
 * Desc:
 */
public class FatiDogStream {
    private static FatiDogStream instance = null;
    private static Logger logger = LoggerFactory.getLogger("FatiDog.convert.FatiDogStream");
    private InputStream inputStream = null;
    private OutputStream outputStream = null;
    private Observable<ByteBuffer> rawData = null;

    private FatiDogStream() {
        inputStream = null;
        outputStream = null;
        rawData = null;
    }

    public static FatiDogStream getInstance() {
        if(instance == null){
            instance = new FatiDogStream();
        }
        return instance;
    }

    public Observable<ByteBuffer> getRawData() {
        if(rawData == null){
            logger.error("串口流尚未打开...");
            open();
        }
        return rawData;
    }

    //打开疲劳串口流
    public synchronized void open() {
        logger.info("打开疲劳预警端口 - in");
        if (rawData == null) {
            SerialPort serialPort = SerialHelper.getInstance().openFatiDogCom();
            inputStream = serialPort.getInputStream();
            outputStream = serialPort.getOutputStream();
            rawData = from(new FatiDogDataReader(inputStream)).autoConnect();
        }else{
            logger.info("疲劳预警端口已经打开，无需重入");
        }
        logger.info("打开疲劳预警端口 - out, rawData :{}",rawData.toString());
    }

    //关闭疲劳串口流
    public void close() {
        logger.info("关闭疲劳预警端口");
        rawData = null;
        inputStream = null;
        outputStream = null;
        SerialHelper.getInstance().closeFatiDogCom();

    }

    //向串口发送指令，cmd中包含除去最后的校验位以外的所有字节
    public void send(byte[] cmd, boolean hasParity) {
//        logger.info("疲劳预警发送指令：hasParity : {}",hasParity);

        if(rawData == null){
            logger.error("串口流尚未打开...");
            open();
        }

        if(hasParity){
            logger.info("发送指令:{}", String.copyValueOf(Hex.encodeHex(cmd)).toUpperCase());
            if (outputStream != null) {
                try {
                    outputStream.write(cmd);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }else{
            byte[] target = new byte[cmd.length];
            System.arraycopy(cmd, 0, target, 0, cmd.length-1);

            int sum = 0;
            for (int i = 0; i < cmd.length-1; i++) {
                sum += cmd[i];
            }
            int parity = (0x0100 - (0xFF & sum));
            target[cmd.length-1] = (byte) (0xFF & parity);
            logger.info("发送指令:{}", String.copyValueOf(Hex.encodeHex(target)).toUpperCase());
            if (outputStream != null) {
                try {
                    outputStream.write(target);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static ConnectableObservable<ByteBuffer> from(final FatiDogDataReader reader) {
        logger.info("stream from step1 -> 当前进程ID：{},当前线程ID：{}",android.os.Process.myPid(),Thread.currentThread().getId());
        return Observable.create((Subscriber<? super ByteBuffer> subscriber) -> {
            logger.info("stream from step2 -> 当前进程ID：{},当前线程ID：{}",android.os.Process.myPid(),Thread.currentThread().getId());
            try {
                ByteBuffer line;
                if (subscriber!=null && subscriber.isUnsubscribed()) {
                    logger.info("stream from 没有订阅者或者被取消了订阅，直接返回。。。");
                    return;
                }
                while (!subscriber.isUnsubscribed() && (line = reader.readLine()) != null) {
                    logger.info("readLine:{}", String.copyValueOf(Hex.encodeHex(line.array())).toUpperCase());
                    subscriber.onNext(line);
                }
            } catch (Exception e) {
                subscriber.onError(e);
            }

            if (!subscriber.isUnsubscribed()) {
                subscriber.onCompleted();
            }
        }).publish();////replay(0, 1, TimeUnit.SECONDS); //重传缓冲置0,防止重复传输导致的一些逻辑上的异常情况
    }
}
