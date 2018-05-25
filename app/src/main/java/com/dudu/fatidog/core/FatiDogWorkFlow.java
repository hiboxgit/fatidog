package com.dudu.fatidog.core;

import android.content.Intent;
import android.os.Environment;

import com.dudu.commonlib.CommonLib;
import com.dudu.commonlib.share.constants.VoiceConstants;
import com.dudu.commonlib.share.contentprovider.voice.VoiceHelper;
import com.dudu.commonlib.utils.file.FileUtil;
import com.dudu.convert.FatiDogWorker;
import com.dudu.convert.bean.FatiDogFacePositionInfoBean;
import com.dudu.fatidog.R;
import com.dudu.fatidog.bean.FatiDogFaceScanEvent;
import com.dudu.fatidog.bean.FatiDogUpdateViewEvent;
import com.dudu.fatidog.bean.FatiDogWarningEvent;
import com.dudu.fatidog.map.MapManager;
import com.dudu.fatidog.map.bean.SpeedChangeEvent;
import com.dudu.fatidog.util.FatiDogConstants;
import com.dudu.fatidog.util.IAsyncTaskCallBack;
import com.dudu.fatidog.util.ScreenSaverManager;

import org.slf4j.LoggerFactory;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import de.greenrobot.event.EventBus;
import rx.Subscription;
import rx.schedulers.Schedulers;

import static com.dudu.fatidog.util.FatiDogConstants.FATIDOG_GET_POS_MAX_TIMES;

/**
 * Author: Robert
 * Date:  2016-12-31
 * Copyright (c) 2016,dudu Co.,Ltd. All rights reserved.
 * Desc:
 */
public class FatiDogWorkFlow {

    private static org.slf4j.Logger logger = LoggerFactory.getLogger("FatiDog.app.FatiDogWorkFlow");
    private Subscription  subHeartBeat = null;
    private Subscription  subSetSysTimeResult = null;
    private Subscription  subWarningModeParamResult = null;
    private Subscription subGetFacePosition = null;
    private Subscription subStartMonitor = null;
    private Subscription subMonitorWarning = null;
    private Subscription subStopMonitor = null;
    private Subscription subCaptureResult = null;
    private Subscription subGetPersonFaceResult = null;


    private static FatiDogWorkFlow instance = null;

    private int getFaceCount = 0;

    public static FatiDogWorkFlow getInstance(){
        if(instance == null){
            instance = new FatiDogWorkFlow();
        }
        return instance;
    }

    public FatiDogWorkFlow() {
    }

    public void stopAllSubscription(){
        logger.info("停止疲劳预警所有订阅...");
        if(subHeartBeat!= null && !subHeartBeat.isUnsubscribed()){
            subHeartBeat.unsubscribe();
            subHeartBeat = null;
        }

        if(subSetSysTimeResult!= null && !subSetSysTimeResult.isUnsubscribed()){
            subSetSysTimeResult.unsubscribe();
            subSetSysTimeResult = null;
        }

        if(subWarningModeParamResult!= null && !subWarningModeParamResult.isUnsubscribed()){
            subWarningModeParamResult.unsubscribe();
            subWarningModeParamResult = null;
        }

        if(subGetFacePosition!= null && !subGetFacePosition.isUnsubscribed()){
            subGetFacePosition.unsubscribe();
            subGetFacePosition = null;
        }
        if(subStartMonitor!= null && !subStartMonitor.isUnsubscribed()){
            subStartMonitor.unsubscribe();
            subStartMonitor = null;
        }


        if(subMonitorWarning!= null && !subMonitorWarning.isUnsubscribed()){
            subMonitorWarning.unsubscribe();
            subMonitorWarning = null;
        }

        if(subStopMonitor!= null && !subStopMonitor.isUnsubscribed()){
            subStopMonitor.unsubscribe();
            subStopMonitor = null;
        }

        if(subCaptureResult!= null && !subCaptureResult.isUnsubscribed()){
            subCaptureResult.unsubscribe();
            subCaptureResult = null;
        }

        if(subGetPersonFaceResult!= null && !subGetPersonFaceResult.isUnsubscribed()){
            subGetPersonFaceResult.unsubscribe();
            subGetPersonFaceResult = null;
        }
    }

    //如果还没有成功刷过脸,就执行这个进行刷脸，
    public void doAtFirstIn(){
        if(!FatiDogValueHelper.getInstance().isDeviceConnected()){
            logger.error("疲劳模块未连接");
            return;
        }

        boolean isScaned = FatiDogValueHelper.getInstance().isFaceScaned();
        if(!isScaned){ //首次进入保存参数,并设置为非第一次进入.
            logger.info("第一次进入自动启动刷脸");
            startScanFace();
        }else{
            logger.info("脸已经刷过,不自动启动刷脸");
        }
    }

    public void startScanFace(){
        boolean isOpen = FatiDogValueHelper.getInstance().isOpen();
        if(!isOpen){
            logger.error("刷脸需要先打开疲劳预警开关");
            FatiDogWorkFlow.getInstance().startMonitor(new IAsyncTaskCallBack() {
                @Override
                public void onComplete() {
                    logger.info("刷脸自动打开疲劳预警开关成功,开始进行刷脸");
                    doScanFace();
                }

                @Override
                public void onError() {
                    logger.info("刷脸自动打开疲劳预警开关失败,开始进行刷脸");
                    doScanFace();
                }
            });
        }else{
            logger.info("疲劳预警已经打开，直接进行刷脸");
            doScanFace();
        }
    }

    public void doScanFace(){
        //刷脸过程中关闭告警
        stopSubcribeWarning();
        /*语音提示用户"请微调机器，让人脸正对摄像头哦"*/
        VoiceHelper.getInstance().startSpeaking(
                CommonLib.getInstance().getContext().getString(R.string.correct_face_position_tip), VoiceConstants.TTS_DONOTHING, false);
        //发送开始播放刷脸动画消息
        EventBus.getDefault().post(new FatiDogUpdateViewEvent(FatiDogConstants.FATIDOG_EVENT_OPEN_FATIDOG_SUCCESS));
        /*开始刷脸*/
        getScanFacePos();
    }

    public void getScanFacePos(){
        if(subGetFacePosition!=null && !subGetFacePosition.isUnsubscribed()){
            subGetFacePosition.unsubscribe();
            subGetFacePosition = null;
        }

        //保存刷脸失败状态
//        FatiDogValueHelper.getInstance().setFaceScaned(false);

        subGetFacePosition  = FatiDogWorker.getInstance().subscribeGetFacePositionResult()
                .timeout(FatiDogConstants.FATIDOG_CMD_RESP_TIME_OUT, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.newThread())
                .subscribe(facePosition -> {
                    logger.info("subGetFacePosition -> 当前进程ID：{},当前线程ID：{}",android.os.Process.myPid(),Thread.currentThread().getId());
                    logger.info("接收到人脸位置信息【左上右下】：({},{},{},{}), - 【角度，亮度】({},{})",facePosition.getOppLeft(),facePosition.getOppTop(),facePosition.getOppRight(),facePosition.getOppBottom(),facePosition.getAngle(),facePosition.getAngle(),facePosition.getLight());
                    if(isFaceRighted(facePosition)){ //刷脸成功
                        //取消订阅
                        getFaceCount = 0;
                        stopScanFace();
                        VoiceHelper.getInstance().startSpeaking(
                                CommonLib.getInstance().getContext().getString(R.string.face_scan_ok), VoiceConstants.TTS_DONOTHING, false);

                        //保存已刷脸成功状态
                        FatiDogValueHelper.getInstance().setFaceScaned(true);

                        //刷脸成功自动打开疲劳预警开关并保存
                        FatiDogValueHelper.getInstance().setOpen(true);

                        //发送界面更新
                        EventBus.getDefault().post(new FatiDogFaceScanEvent(FatiDogConstants.FATIDOG_EVENT_FACE_SCAN_SUCCESS,""));

                        //刷脸成功订阅告警
                        if(FatiDogValueHelper.getInstance().isFaceScaned()){
                            startSubcribeWarning(); //订阅告警事件
                        }

                    }else{
                        if(!retryScanFace()){
                            VoiceHelper.getInstance().startSpeaking(
                                    CommonLib.getInstance().getContext().getString(R.string.face_scan_fail), VoiceConstants.TTS_DONOTHING, false);
                            return;
                        }
                    }
                },throwable -> {
                    logger.error("subGetFacePosition :{}", throwable.toString());
                    if(!retryScanFace()){
                        VoiceHelper.getInstance().startSpeaking(
                                CommonLib.getInstance().getContext().getString(R.string.face_scan_fail), VoiceConstants.TTS_DONOTHING, false);
                        return;
                    }
                });
        FatiDogWorker.getInstance().getFacePosition();
    }

    public void stopScanFace(){
        if(subGetFacePosition!=null && !subGetFacePosition.isUnsubscribed()){
            subGetFacePosition.unsubscribe();
            subGetFacePosition = null;
        }
    }

    public boolean retryScanFace(){
        stopScanFace();
        getFaceCount++;
        if(getFaceCount > FATIDOG_GET_POS_MAX_TIMES){
            logger.error("刷脸次数超限，停止刷脸，通知刷脸失败!");
            getFaceCount = 0;
            //保存刷脸失败状态
            FatiDogValueHelper.getInstance().setFaceScaned(false);

            if(!FatiDogValueHelper.getInstance().isOpen()){ //如果刷脸之前是关闭状态，那么要恢复成关闭状态，不过 不进行真实的关闭，只保留软件关闭也可以。

            }
            //发送界面更新
            EventBus.getDefault().post(new FatiDogFaceScanEvent(FatiDogConstants.FATIDOG_EVENT_FACE_SCAN_FAIL,""));
            return false;
        }

        try {
            Thread.sleep(FatiDogConstants.FATIDOG_GET_POS_DELAY);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //继续获取人脸位置
        getScanFacePos();

        return true;
    }
    public void startMonitor(IAsyncTaskCallBack callback){
        if(subStartMonitor!=null && !subStartMonitor.isUnsubscribed()){
            subStartMonitor.unsubscribe();
            subStartMonitor = null;
        }

        subStartMonitor = FatiDogWorker.getInstance().subscribeStartFatiMonitorResult()
                .subscribeOn(Schedulers.newThread())
                .subscribe(startMonitorResult -> {
                    if(subStartMonitor!=null && !subStartMonitor.isUnsubscribed()){
                        subStartMonitor.unsubscribe();
                        subStartMonitor = null;
                    }
                    logger.info("subStartMonitor -> 当前进程ID：{},当前线程ID：{}",android.os.Process.myPid(),Thread.currentThread().getId());
                    logger.info("接收到开启防疲劳检测结果信息。。。");
                    if(startMonitorResult.getReturnCode() == 0){
                        logger.info("开启防疲劳检测。。。成功");

                        if(FatiDogValueHelper.getInstance().isFaceScaned()){
                            startSubcribeWarning(); //订阅告警事件
                        }else{
                            logger.error("未刷脸不进行告警订阅");
                        }

//                        FatiDogValueHelper.getInstance().setOpen(true);
                        if(callback != null){
                            callback.onComplete();
                        }
                    }else{
                        logger.info("开启防疲劳检测。。。失败");
//                        FatiDogValueHelper.getInstance().setOpen(false);
                        if(callback != null){
                            callback.onError();
                        }
                    }
                }, throwable -> {
                    if(subStartMonitor!=null && !subStartMonitor.isUnsubscribed()){
                        subStartMonitor.unsubscribe();
                        subStartMonitor = null;
                    }
                    if(callback != null){
                        callback.onError();
                    }
//                    FatiDogValueHelper.getInstance().setOpen(false);
                    logger.error("异常：{}", throwable.toString());
                });
        FatiDogWorker.getInstance().startFatiMonitor();
    }

    public void stopMonitor(IAsyncTaskCallBack callBack){
        if(subStopMonitor!=null && !subStopMonitor.isUnsubscribed()){
            subStopMonitor.unsubscribe();
            subStopMonitor = null;
        }

        subStopMonitor = FatiDogWorker.getInstance().subscribeStopFatiMonitorResult()
                .timeout(1, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.newThread())
                .subscribe(stopMonitorResult -> {
                    if(subStopMonitor!=null && !subStopMonitor.isUnsubscribed()){
                        subStopMonitor.unsubscribe();
                        subStopMonitor = null;
                    }

                    logger.info("subStopMonitor -> 当前进程ID：{},当前线程ID：{}",android.os.Process.myPid(),Thread.currentThread().getId());
                    logger.info("接收到停止防疲劳结果信息。。。");
                    if(stopMonitorResult.getReturnCode() == 0){
                        logger.info("关闭防疲劳检测。。。成功");
                        stopSubcribeWarning();

                        if(callBack != null){
                            callBack.onComplete();
                        }
                    }else{
                        logger.info("关闭防疲劳检测。。。失败");
                        if(callBack != null){
                            callBack.onComplete();//onError();
                        }
                    }

                }, throwable -> {
                    if(subStopMonitor!=null && !subStopMonitor.isUnsubscribed()){
                        subStopMonitor.unsubscribe();
                        subStopMonitor = null;
                    }

                    logger.error("关闭防疲劳异常：{}", throwable.toString());
                    if(callBack != null){
                        callBack.onComplete();//onError();
                    }
                });

        FatiDogWorker.getInstance().stopFatiMonitor();
    }

    public void startSubcribeWarning(){
        if(subMonitorWarning!=null && !subMonitorWarning.isUnsubscribed()){
            subMonitorWarning.unsubscribe();
            subMonitorWarning = null;
        }

        subMonitorWarning = FatiDogWorker.getInstance().subscribeWarningInfo()
                .subscribeOn(Schedulers.newThread())
                .subscribe(warnInfo -> {
                    logger.info("subscribeWarningInfo -> 当前进程ID：{},当前线程ID：{}",android.os.Process.myPid(),Thread.currentThread().getId());
                    logger.info("接收到告警信息。。。");
                    if(FatiDogWarningManager.getInstance().isNeedToWarn(warnInfo.getWarningType())){
                        ScreenSaverManager.getInstance().wakeOnce(); //模拟点击去屏保
                        EventBus.getDefault().post(new FatiDogWarningEvent(warnInfo.getWarningType(),"unset")); //发送告警通知

                        logger.info("疲劳预警发送无线方控广播通知！");
                        Intent intent = new Intent(FatiDogConstants.VIBRATE_NOTIFY);
                        intent.putExtra(FatiDogConstants.VIBRATE_FEEL,1);
                        intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                        CommonLib.getInstance().getContext().sendBroadcast(intent);
                        return;
                    }else{
                        logger.info("当前未点火或者当前疲劳预警类型暂不提醒：{}",warnInfo.getWarningType());
                    }
                }, throwable -> {
                    logger.error("异常：{}", throwable.toString());
                });
    }
    public void stopSubcribeWarning(){

        if(subMonitorWarning!=null && !subMonitorWarning.isUnsubscribed()){
            subMonitorWarning.unsubscribe();
            subMonitorWarning = null;
        }
    }

    public void isDeviceExist(IAsyncTaskCallBack callBack){
        logger.info("开始订阅心跳");
        if(subHeartBeat != null && !subHeartBeat.isUnsubscribed()){
            subHeartBeat.unsubscribe();
            subHeartBeat = null;
        }
        //订阅心跳消息，接收到心跳就认为设备连接成功...
        subHeartBeat  = FatiDogWorker.getInstance().subscribeHeartBeat()
                .subscribeOn(Schedulers.newThread()) //被观察者要另外单独起一个线程，否则占用当前线程，导致subHeartBeat获取不到值，一直没办法返回.
                .subscribe(heartBeat -> {
                    logger.info("接收心跳信息成功...更新连接状态-> 当前进程ID：{},当前线程ID：{}",android.os.Process.myPid(),Thread.currentThread().getId());

                    if(subHeartBeat != null && !subHeartBeat.isUnsubscribed()){
                        subHeartBeat.unsubscribe();
                        subHeartBeat = null;
                    }

                    if(heartBeat.getReturnCode() == 0){
                        FatiDogValueHelper.getInstance().updateDeviceConnectStatus(true);

                        setSysTime(new IAsyncTaskCallBack() {
                            @Override
                            public void onComplete() {
                                logger.info("设置时间成功...");
                                if(callBack != null){
                                    callBack.onComplete();
                                }
                            }

                            @Override
                            public void onError() {
                                logger.error("设置时间失败...");
                                if(callBack != null){
                                    callBack.onComplete();
                                }
                            }
                        });

                    }else{
                        logger.error("心跳消息异常");
                        FatiDogValueHelper.getInstance().updateDeviceConnectStatus(false);

                        if(callBack != null){
                            callBack.onError();
                        }
                    }

                }, throwable -> {
                    logger.error("异常：{}", throwable.toString());
                    if(subHeartBeat != null && !subHeartBeat.isUnsubscribed()){
                        subHeartBeat.unsubscribe();
                        subHeartBeat = null;
                    }

                    FatiDogValueHelper.getInstance().updateDeviceConnectStatus(false);
                    if(callBack != null){
                        callBack.onError();
                    }
                });
        logger.info("心跳订阅完毕。。。");
    }

    public void doFatiDogStartTask(){
        logger.info("开始疲劳预警的初始化启动工作...");

        boolean isNeedOpen = false;
        //是否开机启动疲劳预警
        if(FatiDogValueHelper.getInstance().getFatiDogControlData().isOpen()){
            logger.info("疲劳预警-打开");
            isNeedOpen = true;

        }else if(FatiDogValueHelper.getInstance().getFatiDogControlData().isBootStart()){
            logger.info("疲劳预警-开机要打开");
            FatiDogValueHelper.getInstance().getFatiDogControlData().setOpen(true);
            isNeedOpen = true;

        }else{
            logger.info("疲劳预警-开机无需打开");
            isNeedOpen = false;
        }

        if(isNeedOpen){
            FatiDogWorkFlow.getInstance().startMonitor(new IAsyncTaskCallBack() {
                @Override
                public void onComplete() {
                    logger.info("防疲劳打开完毕，首次更新一下防疲劳速度模式");
                    EventBus.getDefault().post(new SpeedChangeEvent(MapManager.getInstance().getSpeed()));
                }

                @Override
                public void onError() {

                }
            });

        }else{

            FatiDogWorkFlow.getInstance().stopMonitor(null);
        }
    }
    public void setSysTime(IAsyncTaskCallBack callback){
        if(subSetSysTimeResult!=null && !subSetSysTimeResult.isUnsubscribed()){
            subSetSysTimeResult.unsubscribe();
            subSetSysTimeResult = null;
        }
        subSetSysTimeResult = FatiDogWorker.getInstance().subscribeSystemTimeSetResult()
                .subscribeOn(Schedulers.newThread())
                .subscribe(sysTimeSetResult -> {
                    if(subSetSysTimeResult!=null && !subSetSysTimeResult.isUnsubscribed()){
                        subSetSysTimeResult.unsubscribe();
                        subSetSysTimeResult = null;
                    }
                    logger.info("接收到时间设置结果信息。。。");
                    logger.info("设置时间结果： ReturnCode :"+sysTimeSetResult.getReturnCode()+",CmdType :"+sysTimeSetResult.getCmdType()+",MsgType:"+sysTimeSetResult.getMsgType());

                    if(callback != null){
                        callback.onComplete();
                    }
                }, throwable -> {
                    if(subSetSysTimeResult!=null && !subSetSysTimeResult.isUnsubscribed()){
                        subSetSysTimeResult.unsubscribe();
                        subSetSysTimeResult = null;
                    }
                    logger.error("设置系统时间异常：{}", throwable.toString());
                    if(callback != null){
                        callback.onError();
                    }
                });


        SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");//设置日期格式
        String curTime = df.format(new Date());// new Date()为获取当前系统时间
        logger.info("当前时间是 ： {}",curTime);
        FatiDogWorker.getInstance().sendSystemTimeSet(curTime);
    }

    public void updateWarnModeSet(float speed){
        logger.trace("开始更新防疲劳速度模式设置！");
        int oldSpeedType = FatiDogValueHelper.getInstance().getSpeedType();
        int newSpeedType = FatiDogValueHelper.getInstance().getFatidogSpeedType(speed);
        int newSpeedMode = FatiDogValueHelper.getInstance().getFatidogSpeedMode(newSpeedType);

        boolean hasChange = false;
        if(newSpeedType != oldSpeedType){
            hasChange = true;
        }else{
            hasChange = false;
        }
        //更新新的速度范围和模式
        FatiDogValueHelper.getInstance().setSpeedType(newSpeedType);
        FatiDogValueHelper.getInstance().setSpeedMode(newSpeedMode);

        if(hasChange && !FatiDogValueHelper.getInstance().isManualWarnSet()){
            setWarningModeParam((int)speed,newSpeedMode,null);
        }else{

        }

    }
    public void setWarningModeParam(int speedInt, int speedModeIn,IAsyncTaskCallBack callback){
        if(subWarningModeParamResult!=null && !subWarningModeParamResult.isUnsubscribed()){
            subWarningModeParamResult.unsubscribe();
            subWarningModeParamResult = null;
        }
        subWarningModeParamResult = FatiDogWorker.getInstance().subscribeParamSetResult()
                .subscribeOn(Schedulers.newThread())
                .subscribe(paramSetResult -> {
                    if(subWarningModeParamResult!=null && !subWarningModeParamResult.isUnsubscribed()){
                        subWarningModeParamResult.unsubscribe();
                        subWarningModeParamResult = null;
                    }
                    logger.info("接收到防疲劳参数设置结果信息。。。");
                    logger.info("参数设置结果： ReturnCode :"+paramSetResult.getReturnCode()+",CmdType :"+paramSetResult.getCmdType()+",MsgType:"+paramSetResult.getMsgType());

                    if(callback != null){
                        callback.onComplete();
                    }
                }, throwable -> {
                    logger.error("异常：{}", throwable.toString());
                    if(callback != null){
                        callback.onError();
                    }
                });

        try{
            logger.info("设置速度和速度模式：{},{}",speedInt,speedModeIn);
            FatiDogWorker.getInstance().sendParamSetCmd(speedInt,speedModeIn);

        }catch (Exception e){
            e.printStackTrace();
        }

    }
    //机器是否扶正，使得人脸对准摄像头
    public boolean isFaceRighted(FatiDogFacePositionInfoBean facePosition){

        if((isInCameraFrame(facePosition.getOppLeft()))
                &&(isInCameraFrame(facePosition.getOppTop()))
                &&(isInCameraFrame(facePosition.getOppRight()))
                &&(isInCameraFrame(facePosition.getOppBottom()))) {

            int xCenter = (facePosition.getOppLeft() + facePosition.getOppRight())/2;
            int yCenter = (facePosition.getOppTop() + facePosition.getOppBottom())/2;
            if(isInCameraCenterFrame(xCenter)
                    && isInCameraCenterFrame(yCenter)){
                logger.info("人脸已经完全对正！");
                return true;
            }else{
                logger.info("人脸中心不在中心框中，没完全对正");
                return false;
            }
        }else{
            logger.info("人脸位置信息四条边界并不全部在取景框范围内，人脸有部分在外面");
            return false;
        }

    }

    //是否在摄像头取景窗范围内
    private boolean isInCameraFrame(int pos){
        return (pos>0 && pos<10000);
    }

    //是否在摄像头取景窗中心框范围内
    private boolean isInCameraCenterFrame(int pos){
//        return (pos>0 && pos<10000);
        return (pos>2500 && pos<7500);
    }

    private void unSubscribeEvent(Subscription subscriber){
        if(subscriber!=null && !subscriber.isUnsubscribed()){
            subscriber.unsubscribe();
            subscriber = null;
        }
    }

    //-----------------------------------------------------------------
    public void capture(IAsyncTaskCallBack callback){

        if(subCaptureResult!=null && !subCaptureResult.isUnsubscribed()){
            subCaptureResult.unsubscribe();
            subCaptureResult = null;
        }

        subCaptureResult = FatiDogWorker.getInstance().subscribeCaptureSilceResult()
                .subscribeOn(Schedulers.newThread())
                .subscribe(captureSilceInfo -> {
                    logger.info("接收到抓拍图片分片信息。。。");
//                    logger.info("抓拍图片分片信息结果： captureSilceInfo:{},",captureSilceInfo.toString());

                    if(captureSilceInfo.getReturnCode() != 0){
                        logger.error("抓拍失败,returnCode：{}",captureSilceInfo.getReturnCode());
                        if(subCaptureResult!=null && !subCaptureResult.isUnsubscribed()){
                            subCaptureResult.unsubscribe();
                            subCaptureResult = null;
                        }
                        return;
                    }

                    try{
                        if(captureSilceInfo.getSize()>0){
                            pushDataToFile(captureSilceInfo.getImageData());
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }

                    if(captureSilceInfo.getSliceIndex() != 1){
                        logger.debug("还有图片分片没接收完，还要继续读...");
                        FatiDogWorker.getInstance().captureSlice(false);
                    }else {
                        logger.info("当前图片所有分片已经接收完毕！");
                        logger.info("图片传输成功，保存路径为：{}，{}",Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "mySnap.jpg");
                        if(subCaptureResult!=null && !subCaptureResult.isUnsubscribed()){
                            subCaptureResult.unsubscribe();
                            subCaptureResult = null;
                        }
                        if(callback != null){
                            callback.onComplete();
                        }
                    }

                }, throwable -> {
                    logger.error("异常：{}", throwable.toString());
                    if(callback != null){
                        callback.onError();
                    }
                });

        try{
//            File file1 = new File(CommonLib.getInstance().getContext().getFilesDir(), "mySnap.jpg");
//            file1.renameTo()
//            file1.delete();

            FileUtil.deleteOneFile(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)+ "/mySnap.jpg");
        }catch (Exception e){
            e.printStackTrace();
        }

        FatiDogWorker.getInstance().captureSlice(true);
    }

    //getExternalStorageDirectory
    private void pushDataToFile(byte[] data){
        try {
//            File file2 = new File(CommonLib.getInstance().getContext().getFilesDir(), "mySnap.jpg");
            File file2 = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "mySnap.jpg");
            BufferedOutputStream buf = new BufferedOutputStream(new FileOutputStream(file2,true));
            buf.write(data);
            buf.flush();
            buf.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
