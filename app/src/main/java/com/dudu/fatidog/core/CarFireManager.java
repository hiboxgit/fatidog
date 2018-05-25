package com.dudu.fatidog.core;

import android.content.Intent;
import android.os.Environment;

import com.dudu.commonlib.CommonLib;
import com.dudu.commonlib.share.constants.VoiceConstants;
import com.dudu.commonlib.share.contentprovider.fire.FireHelper;
import com.dudu.commonlib.share.contentprovider.voice.VoiceHelper;
import com.dudu.convert.FatiDogWorker;
import com.dudu.fatidog.BuildConfig;
import com.dudu.fatidog.R;
import com.dudu.fatidog.map.MapManager;
import com.dudu.fatidog.util.FatiDogConstants;
import com.dudu.fatidog.util.IAsyncTaskCallBack;
import com.dudu.recoac.core.FaceRecogValueHelper;
import com.dudu.recoac.core.FaceRecogWorkFlow;
import com.dudu.serialcom.manager.GpioControlManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rx.Observable;
import rx.Subscription;
import rx.schedulers.Schedulers;


/**
 * Author: Robert
 * Date:  2017-01-21
 * Copyright (c) 2016,dudu Co.,Ltd. All rights reserved.
 * Desc:
 */
public class CarFireManager {
    private Logger logger = LoggerFactory.getLogger("FatiDog.app.CarFireManager");
    private static CarFireManager instance = null;
    private Boolean isFired = false; //是否已经点火
    private boolean isFireWorkRunning = false; //点火或者熄火操作流程正在运行
    private long fireOnTime = 0;
    private long fireOffTime = 0;
    private Subscription fireOnTaskSubscription;
    private Subscription fireOffTaskSubscription;

    public static CarFireManager getInstance(){
        if(instance == null){
            instance = new CarFireManager();
        }
        return instance;
    }

    public CarFireManager() {
        setFired(false);
    }

    public synchronized void initStartWork(boolean fired){
        logger.info("点火/熄火流程处理.....本次状态：{}",fired);
        if(!FireHelper.getInstance().getDisclaimerInfo()){
            logger.info("免责声明未通过，强制认定为熄火...");
            fired = false;
        }

        //记录点火熄火时间
        if(fired){
            setFireOnTime(System.currentTimeMillis());
        }else {
            setFireOffTime(System.currentTimeMillis());
        }

        logger.info("<---(之前状态,之前流程是否在执行，本次是否是点火)--->");
        //如果之前的操作流程还没完成，不进行新的操作
        if(!hasFired() //上次是熄火
                && !isFireWorkRunning() //并且熄火流程没有在跑
                && !fired){ //本次是点火
            logger.info("点火流程控制器：（0,0,0） -> 直接返回... ");
        }else if(!hasFired()
                && !isFireWorkRunning()
                && fired){
            logger.info("点火流程控制器：（0,0,1） -> 执行点火操作... ");
            fireOnTask();
        }else if(!hasFired()
                && isFireWorkRunning()
                && !fired){
            logger.info("点火流程控制器：（0,1,0） -> 直接返回... ");
        }else if(!hasFired()
                && isFireWorkRunning()
                && fired){
            logger.info("点火流程控制器：（0,1,1） -> 取消上次的熄火流程，然后再执行本次点火流程... ");
            cancelFireOffTask();
            fireOnTask();
        }else if(hasFired()
                && !isFireWorkRunning()
                && !fired){
            logger.info("1,0,0");
            logger.info("点火流程控制器：（1,0,0） -> 执行熄火操作... ");
            fireOffTask();
        }else if(hasFired()
                && !isFireWorkRunning()
                && fired){
            logger.info("点火流程控制器：（1,0,1） -> 之前已经点火，并跑完了点火流程，本次直接返回... ");

        }else if(hasFired()
                && isFireWorkRunning()
                && !fired){
            logger.info("点火流程控制器：（1,1,0） -> 取消之前的正在进行的点火流程，执行本次熄火操作... ");
            cancelFireOnTask();
            fireOffTask();
        }else if(hasFired()
                && isFireWorkRunning()
                && fired){
            logger.info("点火流程控制器：（1,1,1） -> 之前的点火操作正在执行，本次直接返回... ");
        }

        setFired(fired); //更新点火状态
    }

    public void doOnFireOn(){
        logger.info("疲劳预警模块开始点火操作....");

        GpioControlManager.getInstance().powerOnGps(); //GPS模块上电
        GpioControlManager.getInstance().powerOnFatidog(); //疲劳预警模块上电
        try{
            Thread.sleep(1000);
        }catch (Exception e){
            e.printStackTrace();
        }
        //初始化启动地图导航 //主要为了获取车速
        MapManager.getInstance().init(CommonLib.getInstance().getContext());
        //初始化疲劳预警
        initWork();

        setFireWorkRunning(false);
    }
    public void doOnFireOff(){
        logger.info("疲劳预警模块开始熄火操作....");
        deInitWork();
        MapManager.getInstance().release();

        GpioControlManager.getInstance().powerOffGPS(); //GPS下电
        GpioControlManager.getInstance().powerOffFatidog(); //疲劳预警模块下电

        setFireWorkRunning(false);
    }

    public Boolean hasFired() {
        return isFired;
    }

    public void setFired(Boolean fired) {
        logger.info("setFired - 保存本次的点火熄火状态 : {}",fired);
        isFired = fired;
    }

    public boolean isFireWorkRunning() {
        return isFireWorkRunning;
    }

    public void setFireWorkRunning(boolean fireWorkRunning) {
        isFireWorkRunning = fireWorkRunning;
    }

    public void startFaceRecoDriver(){
        //人脸识别钱需要先把疲劳预警关闭.
        FatiDogWorkFlow.getInstance().stopMonitor(new IAsyncTaskCallBack() {

            @Override
            public void onComplete() {
                logger.info("疲劳预警关闭成功...");
                doFaceRecoDriver(new IAsyncTaskCallBack() {
                    @Override
                    public void onComplete() {
                        logger.info("人脸识别工作执行完毕，开始进行疲劳预警初始化。。。");
                        FaceRecogWorkFlow.getInstance().doStartWork(new IAsyncTaskCallBack() {
                            @Override
                            public void onComplete() {
                                FatiDogWorkFlow.getInstance().doFatiDogStartTask();
                            }

                            @Override
                            public void onError() {
                                FatiDogWorkFlow.getInstance().doFatiDogStartTask();
                            }
                        });
                    }

                    @Override
                    public void onError() {

                    }
                });
            }

            @Override
            public void onError() {
                logger.error("疲劳预警关闭失败...无法进行人脸识别，注册人员以及后续启动疲劳预警工作");

            }
        });
    }
    public void doFaceRecoDriver(IAsyncTaskCallBack callBack){
        logger.info("开始识别Task！");
        if( FatiDogValueHelper.getInstance().isDeviceConnected()){
            logger.debug("设备已经连接。。。");
            if(FaceRecogValueHelper.getInstance().isFaceRecogOpened()){
                logger.debug("人脸识别功能已打开。。。");
                if(FaceRecogValueHelper.getInstance().isRegisteredFacePersonEmpty()){
                    logger.error("注册过人脸的人员里列表为空。。。,暂不进行人脸识别");
                    if(BuildConfig.DEBUG){//只在debug模式才进行语音提示
                        VoiceHelper.getInstance().startSpeaking("注册过人脸的人员里列表为空,暂不进行人脸识别", VoiceConstants.TTS_DONOTHING, false);
                    }
                }else{
                    logger.info("注册过人脸的人员里列表不为空，可以进行人脸识别。。。");
                    FaceRecogWorkFlow.getInstance().checkPerson(new IAsyncTaskCallBack() {
                        @Override
                        public void onComplete() {
                            logger.info("人员认证通过！！！");

                            String driverName = FaceRecogValueHelper.getInstance().getCurDriver();
                            if(!driverName.equals("") &&
                                    !driverName.equals(" ")&&
                                    !driverName.startsWith("人员")){
                                driverName = driverName+"你好，";
                            }else{
                                driverName = "";
                            }

                            if(BuildConfig.DEBUG){//只在debug模式才进行语音提示
                                VoiceHelper.getInstance().startSpeaking(driverName+CommonLib.getInstance().getContext().getString(R.string.right_driver_welcome), VoiceConstants.TTS_DONOTHING, false);
                            }else{
                                VoiceHelper.getInstance().startSpeaking(CommonLib.getInstance().getContext().getString(R.string.right_driver_welcome), VoiceConstants.TTS_DONOTHING, false);
                            }
                            if(callBack != null){
                                callBack.onComplete();
                            }

                        }

                        @Override
                        public void onError() {
                            logger.error("人员认证不通过！要告警！先抓一张图");
                            if(BuildConfig.DEBUG){
                                VoiceHelper.getInstance().startSpeaking(CommonLib.getInstance().getContext().getString(R.string.notice_stranger), VoiceConstants.TTS_DONOTHING, false);
                            }

                            FatiDogWorkFlow.getInstance().capture(new IAsyncTaskCallBack() {
                                @Override
                                public void onComplete() {
                                    String snapPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath().toString()+"/mySnap.jpg";
                                    logger.info("抓拍完成，开始发送广播通知发给微信公众号！，snapPath：{}",snapPath);
                                    if(BuildConfig.DEBUG){
                                        VoiceHelper.getInstance().startSpeaking("抓拍完成，开始发送广播通知发给微信公众号", VoiceConstants.TTS_DONOTHING, false);
                                    }
                                    try{
                                        Intent intent = new Intent(FatiDogConstants.UNTRUST_WARN);
                                        intent.putExtra(FatiDogConstants.UNVALIDE_DRIVER_SNAP_PATH,snapPath);
                                        intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                                        CommonLib.getInstance().getContext().sendBroadcast(intent);

                                    }catch (Exception e){
                                        logger.error("发送非认证人员驾车失败{}",e.toString());
                                    }

                                    if(callBack != null){
                                        callBack.onComplete();
                                    }
                                }
                                @Override
                                public void onError() {
                                    logger.error("抓拍失败！");
                                    if(BuildConfig.DEBUG){//只在debug模式才进行语音提示
                                        VoiceHelper.getInstance().startSpeaking("抓拍失败", VoiceConstants.TTS_DONOTHING, false);
                                    }
                                    if(callBack != null){
                                        callBack.onComplete();
                                    }
                                }
                            });

                        }
                    });
                }
            }else{
                logger.debug("人脸识别功能未打开。。。");
                if(BuildConfig.DEBUG){//只在debug模式才进行语音提示
                    VoiceHelper.getInstance().startSpeaking("人脸识别功能未打开", VoiceConstants.TTS_DONOTHING, false);
                }
                if(callBack != null){
                    callBack.onComplete();
                }
            }

        }else{
            logger.error("设备未连接。。。");
            if(BuildConfig.DEBUG){//只在debug模式才进行语音提示
                VoiceHelper.getInstance().startSpeaking("设备未连接", VoiceConstants.TTS_DONOTHING, false);
            }
            if(callBack != null){
                callBack.onComplete();
            }
        }
    }

    private void initWork(){
        logger.info("疲劳预警模块初始化...");
        logger.info("startFatiDogWork -> 当前进程ID：{},当前线程ID：{}",android.os.Process.myPid(),Thread.currentThread().getId());

//        FatiDogWorker.getInstance().startWork(); //第一次订阅里面发现没打开会自动打开串口,还能同步执行,也不错...
        FatiDogWorkFlow.getInstance().isDeviceExist(new IAsyncTaskCallBack() {
            @Override
            public void onComplete() {
                startFaceRecoDriver(); //点火进行人脸识别
            }

            @Override
            public void onError() {

                logger.error("设备不在线，不启动人脸识别功能...");
            }
        });
    }

    private void deInitWork(){
        logger.info("疲劳预警模块销毁...");
        FaceRecogWorkFlow.getInstance().stopAllSubscription();
        FatiDogWorkFlow.getInstance().stopAllSubscription();
        FatiDogWorker.getInstance().stopWork();
        FatiDogValueHelper.getInstance().updateDeviceConnectStatus(false);
    }

    public void fireOnTask(){
        logger.info("执行点火操作流程->");
        setFireWorkRunning(true); //设置当前处于点火操作流程，正在跑，还没完...
        cancelFireOnTask();

        fireOnTaskSubscription = Observable.just(1)
                .observeOn(Schedulers.newThread())
                .subscribe(s -> {
                    doOnFireOn();
                });
    }

    public void cancelFireOnTask(){
        if(fireOnTaskSubscription!=null && !fireOnTaskSubscription.isUnsubscribed()){
            fireOnTaskSubscription.unsubscribe();
            fireOnTaskSubscription = null;
        }
    }

    public void fireOffTask(){
        logger.info("执行熄火操作流程->");
        setFireWorkRunning(true); //设置当前处于点火操作流程，正在跑，还没完...
        cancelFireOffTask();

        fireOffTaskSubscription = Observable.just(1)
                .observeOn(Schedulers.newThread())
                .subscribe(s -> {
                    doOnFireOff();
                });
    }

    public void cancelFireOffTask(){
        if(fireOffTaskSubscription!=null && !fireOffTaskSubscription.isUnsubscribed()){
            fireOffTaskSubscription.unsubscribe();
            fireOffTaskSubscription = null;
        }
    }

    public Logger getLogger() {
        return logger;
    }

    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    public long getFireOnTime() {
        return fireOnTime;
    }

    public void setFireOnTime(long fireOnTime) {
        this.fireOnTime = fireOnTime;
    }

    public long getFireOffTime() {
        return fireOffTime;
    }

    public void setFireOffTime(long fireOffTime) {
        this.fireOffTime = fireOffTime;
    }

    public long getTimeHasFired(){ //获取点火到现在过了多长时间
        long curTime = System.currentTimeMillis();
        long timeGone = curTime-fireOnTime;
        return timeGone;
    }
}
