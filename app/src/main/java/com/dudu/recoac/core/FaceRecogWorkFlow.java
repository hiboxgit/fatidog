package com.dudu.recoac.core;

import com.dudu.commonlib.CommonLib;
import com.dudu.commonlib.share.constants.VoiceConstants;
import com.dudu.commonlib.share.contentprovider.voice.VoiceHelper;
import com.dudu.convert.FatiDogWorker;
import com.dudu.convert.bean.FaceRecoPersonInfoBean;
import com.dudu.fatidog.R;
import com.dudu.fatidog.bean.UpdateFaceListEvent;
import com.dudu.fatidog.util.FatiDogConstants;
import com.dudu.fatidog.util.IAsyncTaskCallBack;
import com.dudu.recoac.bean.FaceAddEvent;
import com.dudu.recoac.bean.FaceAddResultEvent;
import com.dudu.recoac.bean.FaceRecogAddFaceEvent;
import com.dudu.recoac.bean.FaceRecogAddFaceStartEvent;
import com.dudu.recoac.bean.FaceRecogPersonData;
import com.dudu.recoac.bean.FaceSwitchPageEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

import de.greenrobot.event.EventBus;
import rx.Observable;
import rx.Subscription;
import rx.schedulers.Schedulers;

/**
 * Author: Robert
 * Date:  2017-02-10
 * Copyright (c) 2016,dudu Co.,Ltd. All rights reserved.
 * Desc:
 */
public class FaceRecogWorkFlow {
    private Logger logger = LoggerFactory.getLogger("FatiDog.app.recoac.FaceRecogWorkFlow");
    private static FaceRecogWorkFlow instance = null;

    private Subscription subCleanRegisteredPerson = null;
    private Subscription subRegisterPerson = null;
    private Subscription subRegisterFace = null;
    private Subscription subRecogFace = null;
    private Subscription subGetPersonInfo = null;
    private Subscription subCleanFaceResult = null;

    public static FaceRecogWorkFlow getInstance(){
        if(instance == null){
            instance = new FaceRecogWorkFlow();
        }
        return instance;
    }

    public FaceRecogWorkFlow() {
    }

    public void stopAllSubscription(){
        logger.info("停止人脸识别所有订阅...");
        if(subCleanRegisteredPerson!= null && !subCleanRegisteredPerson.isUnsubscribed()){
            subCleanRegisteredPerson.unsubscribe();
            subCleanRegisteredPerson = null;
        }

        if(subRegisterPerson!= null && !subRegisterPerson.isUnsubscribed()){
            subRegisterPerson.unsubscribe();
            subRegisterPerson = null;
        }

        if(subRegisterFace!= null && !subRegisterFace.isUnsubscribed()){
            subRegisterFace.unsubscribe();
            subRegisterFace = null;
        }

        if(subRecogFace!= null && !subRecogFace.isUnsubscribed()){
            subRecogFace.unsubscribe();
            subRecogFace = null;
        }

        if(subGetPersonInfo!= null && !subGetPersonInfo.isUnsubscribed()){
            subGetPersonInfo.unsubscribe();
            subGetPersonInfo = null;
        }

        if(subCleanFaceResult!= null && !subCleanFaceResult.isUnsubscribed()){
            subCleanFaceResult.unsubscribe();
            subCleanFaceResult = null;
        }
    }
    public void doStartWork(IAsyncTaskCallBack callBack){
        boolean isDataReset = FaceRecogValueHelper.getInstance().isDataReset();
        if(!isDataReset){
            logger.info("注册人员需要重置初始化...");

            FaceRecogWorkFlow.getInstance().resetRegisteredPerson(new IAsyncTaskCallBack() {
                @Override
                public void onComplete() {
                    logger.info("清空重置人员成功！");
                    FaceRecogWorkFlow.getInstance().startRegAllWhitListPerson(new IAsyncTaskCallBack() {
                        @Override
                        public void onComplete() {
                            logger.info("注册白名单人员成功！");
                            FaceRecogValueHelper.getInstance().setDataResetValue(true); //保存人员已经重置过的状态.

                            if(callBack != null){
                                callBack.onComplete();
                            }
                        }

                        @Override
                        public void onError() {
                            logger.error("注册白名单人员失败！");
                            if(callBack != null){
                                callBack.onError();
                            }
                        }
                    });
                }

                @Override
                public void onError() {
                    logger.error("清空人员失败！");
                    if(callBack != null){
                        callBack.onError();
                    }
                }
            });
        }else{
            logger.debug("注册人员已经重置过，无需重置...");

//            getAllRegPersonInfo(new CmdCompleteCallBack() {
//                @Override
//                public void onComplete() {
//                    if(callBack != null){
//                        callBack.onComplete();
//                    }
//                }
//
//                @Override
//                public void onError() {
//                    if(callBack != null){
//                        callBack.onError();
//                    }
//                }
//            });

            if(callBack != null){
                callBack.onComplete();
            }
        }
    }

    public void getAllRegPersonInfo(IAsyncTaskCallBack callBack){
        logger.info("开始查询所有白名单人员信息...");
        logger.info("开始查询第1个人员");
        getRegPersonInfo(0, new IAsyncTaskCallBack() {
            @Override
            public void onComplete() {
                logger.info("开始查询第2个人员");
                getRegPersonInfo(1, new IAsyncTaskCallBack() {
                    @Override
                    public void onComplete() {
                        logger.info("开始查询第3个人员");
                        getRegPersonInfo(2, new IAsyncTaskCallBack() {
                            @Override
                            public void onComplete() {
                                logger.info("白名单人员信息全部查询完毕");

                                if(callBack != null){
                                    callBack.onComplete();
                                }
                            }

                            @Override
                            public void onError() {
                                logger.error("查询人员3信息失败");
                                if(callBack != null){
                                    callBack.onError();
                                }
                            }
                        });
                    }

                    @Override
                    public void onError() {
                        logger.error("查询人员2信息失败");
                        if(callBack != null){
                            callBack.onError();
                        }
                    }
                });
            }

            @Override
            public void onError() {
                logger.error("查询人员1信息失败");
                if(callBack != null){
                    callBack.onError();
                }

            }
        });

    }

    public void getRegPersonInfo(int index,IAsyncTaskCallBack callBack){
        logger.info("查询人员信息...");
        if(subGetPersonInfo!=null && !subGetPersonInfo.isUnsubscribed()){
            subGetPersonInfo.unsubscribe();
            subGetPersonInfo = null;
        }

        subGetPersonInfo  = FatiDogWorker.getInstance().subcribeFindPersonResult()
                .timeout(FatiDogConstants.FATIDOG_CMD_RESP_TIME_OUT, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.newThread())
                .subscribe(personInfoResult -> {
                    logger.debug("接收到查询人员信息结果：{}",personInfoResult.toString());
                    if(subCleanRegisteredPerson!=null && !subCleanRegisteredPerson.isUnsubscribed()){
                        subCleanRegisteredPerson.unsubscribe();
                        subCleanRegisteredPerson = null;
                    }

                    if(personInfoResult.getReturnCode() == 0x00){
                        logger.debug("查询人员信息成功！");
                        if(callBack != null){
                            callBack.onComplete();
                        }
                    }else {
                        logger.error("查询人员信息失败！");
                        if(callBack != null){
                            callBack.onError();
                        }
                    }
                },throwable -> {
                    logger.error("subGetPersonInfo-查询人员信息失败 :{}", throwable.toString());
                    if(subGetPersonInfo!=null && !subGetPersonInfo.isUnsubscribed()){
                        subGetPersonInfo.unsubscribe();
                        subGetPersonInfo = null;
                    }

                    if(callBack != null){
                        callBack.onError();
                    }
                });

        FatiDogWorker.getInstance().findRegPersonInfo(FaceRecogValueHelper.getInstance().getPersonId(index));
        logger.debug("查询人员信息命令发送完毕!");

    }

    //清空已经注册的人员
    public void resetRegisteredPerson(IAsyncTaskCallBack callBack){
        logger.info("开始清空人员...");
        if(subCleanRegisteredPerson!=null && !subCleanRegisteredPerson.isUnsubscribed()){
            subCleanRegisteredPerson.unsubscribe();
            subCleanRegisteredPerson = null;
        }

        subCleanRegisteredPerson  = FatiDogWorker.getInstance().subscribeCleanPersonResult()
                .timeout(FatiDogConstants.FATIDOG_CMD_RESP_TIME_OUT, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.newThread())
                .subscribe(cleanResult -> {
                    logger.debug("接收到清空人员命令结果：{}",cleanResult.toString());
                    if(subCleanRegisteredPerson!=null && !subCleanRegisteredPerson.isUnsubscribed()){
                        subCleanRegisteredPerson.unsubscribe();
                        subCleanRegisteredPerson = null;
                    }

                    if(cleanResult.getReturnCode() == 0x00){
                        logger.debug("清空人员成功！");
                        if(callBack != null){
                            callBack.onComplete();
                        }
                    }else {
                        logger.error("清空人员失败！");
                        if(callBack != null){
                            callBack.onError();
                        }
                    }
                },throwable -> {
                    logger.error("subCleanRegisteredPerson-清空人员失败 :{}", throwable.toString());
                    if(subCleanRegisteredPerson!=null && !subCleanRegisteredPerson.isUnsubscribed()){
                        subCleanRegisteredPerson.unsubscribe();
                        subCleanRegisteredPerson = null;
                    }

                    if(callBack != null){
                        callBack.onError();
                    }
                });

        FatiDogWorker.getInstance().cleanPerson();
        logger.debug("清空人员命令发送完毕!");
    }


    private void startRegPerson(int index,IAsyncTaskCallBack callBack){
        logger.info("开始注册人员...index：{}",index);
        if(subRegisterPerson!=null && !subRegisterPerson.isUnsubscribed()){
            subRegisterPerson.unsubscribe();
            subRegisterPerson = null;
        }

        subRegisterPerson = FatiDogWorker.getInstance().subscribeRegisterPersonResult()
                .subscribeOn(Schedulers.newThread())
                .subscribe(regPersonResult -> {
                    logger.debug("接收到注册人员命令结果：{}",regPersonResult.toString());
                    if(subRegisterPerson!=null && !subRegisterPerson.isUnsubscribed()){
                        subRegisterPerson.unsubscribe();
                        subRegisterPerson = null;
                    }

                    if(regPersonResult.getReturnCode() == 0x00){
                        logger.debug("注册人员成功！,更新注册人员ID");
                        FaceRecogValueHelper.getInstance().setPersonId(index,regPersonResult.getPersonId());

                        if(callBack != null){
                            callBack.onComplete();
                        }
                    }else {
                        logger.error("注册人员失败！");
                        if(callBack != null){
                            callBack.onError();
                        }
                    }
                },throwable -> {
                    logger.error("subRegisterPerson-注册人员失败 :{}", throwable.toString());
                    if(subRegisterPerson!=null && !subRegisterPerson.isUnsubscribed()){
                        subRegisterPerson.unsubscribe();
                        subRegisterPerson = null;
                    }

                    if(callBack != null){
                        callBack.onError();
                    }
                });

        FaceRecogPersonData personData = FaceRecogValueHelper.getInstance().getRecoacPersonData(index);
        FaceRecoPersonInfoBean registerData = new FaceRecoPersonInfoBean(personData.getPersonId(),
                personData.getName(),
                personData.getCard(),
                personData.getRegTime(),
                "",
                "",
                "",
                "",
                "");
        FatiDogWorker.getInstance().registerPerson(registerData);
        logger.debug("注册人员命令发送完毕!,index:{}",index);
    }

    public void startRegAllWhitListPerson(IAsyncTaskCallBack callBack){
        logger.info("开始注册第1个人。");
        startRegPerson(0, new IAsyncTaskCallBack() {
            @Override
            public void onComplete() {
                logger.info("开始注册第2个人。");
                startRegPerson(1, new IAsyncTaskCallBack() {
                    @Override
                    public void onComplete() {
                        logger.info("开始注册第3个人。");
                        startRegPerson(2, new IAsyncTaskCallBack() {
                            @Override
                            public void onComplete() {
                                logger.error("白名单人员全部注册完毕！");
                                if(callBack != null){
                                    callBack.onComplete();
                                }
                            }

                            @Override
                            public void onError() {
                                logger.error("第3个人注册失败");
                                if(callBack != null){
                                    callBack.onError();
                                }
                            }
                        });
                    }

                    @Override
                    public void onError() {
                        logger.error("第2个人注册失败");
                        if(callBack != null){
                            callBack.onError();
                        }
                    }
                });
            }

            @Override
            public void onError() {
                logger.error("第1个人注册失败");
                if(callBack != null){
                    callBack.onError();
                }
            }
        });
    }

    public void startAddFaceForPersonById(long personId,IAsyncTaskCallBack callBack){
        logger.info("开始注册人脸2...");
        if(subRegisterFace!=null && !subRegisterFace.isUnsubscribed()){
            subRegisterFace.unsubscribe();
            subRegisterFace = null;
        }

        subRegisterFace  = FatiDogWorker.getInstance().subscribeRegisterFaceResult()
                .timeout(30, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.newThread())
                .subscribe(regFaceResult -> {
                    logger.debug("接收到注册人脸命令结果：{}",regFaceResult.toString());

                    if(regFaceResult.getReturnCode() == 0x00){
                        int regFaceState = regFaceResult.getState();
                        logger.debug("当前人脸注册状态：{}",regFaceState);
                        if(regFaceState == 1){

                            logger.debug("采集人脸成功！");
                            if(subRegisterFace!=null && !subRegisterFace.isUnsubscribed()){
                                subRegisterFace.unsubscribe();
                                subRegisterFace = null;
                            }

                            if(callBack != null){
                                callBack.onComplete();
                            }
                        }else if(regFaceState == 2){
                            logger.error("注册人脸超时！");
                            if(subRegisterFace!=null && !subRegisterFace.isUnsubscribed()){
                                subRegisterFace.unsubscribe();
                                subRegisterFace = null;
                            }

                            if(callBack != null){
                                callBack.onError();
                            }
                        }else{
                            logger.info("注册人脸采集中！");
                        }


                    }else {
                        logger.error("注册人脸失败！");
                        if(subRegisterFace!=null && !subRegisterFace.isUnsubscribed()){
                            subRegisterFace.unsubscribe();
                            subRegisterFace = null;
                        }
                        if(regFaceResult.getReturnCode() == FatiDogWorker.FAULT_CODE_UNSUPPORT){
                            VoiceHelper.getInstance().startSpeaking(
                                    CommonLib.getInstance().getContext().getString(R.string.unsupport_func), VoiceConstants.TTS_DONOTHING, false);
                        }

                        if(callBack != null){
                            callBack.onError();
                        }
                    }
                },throwable -> {
                    logger.error("subRegisterFace-注册人脸失败 :{}", throwable.toString());
                    if(subRegisterFace!=null && !subRegisterFace.isUnsubscribed()){
                        subRegisterFace.unsubscribe();
                        subRegisterFace = null;
                    }

                    if(callBack != null){
                        callBack.onError();
                    }
                });

        FatiDogWorker.getInstance().registerFace(personId);
        logger.debug("注册人脸命令发送完毕!");
    }

    public void startAddFaceForPerson(int index,int faceType,IAsyncTaskCallBack callBack){
        logger.info("开始注册人脸...");
        if(subRegisterFace!=null && !subRegisterFace.isUnsubscribed()){
            subRegisterFace.unsubscribe();
            subRegisterFace = null;
        }

        subRegisterFace  = FatiDogWorker.getInstance().subscribeRegisterFaceResult()
                .timeout(30, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.newThread())
                .subscribe(regFaceResult -> {
                    logger.debug("接收到注册人脸命令结果：{}",regFaceResult.toString());

                    if(regFaceResult.getReturnCode() == 0x00){

                        int regFaceState = regFaceResult.getState();
                        logger.debug("当前人脸注册状态：{}",regFaceState);
                        if(regFaceState == 1){
//                            FatiDogWorkFlow.getInstance().isFaceRighted();
                            logger.debug("采集人脸成功！");
                            if(subRegisterFace!=null && !subRegisterFace.isUnsubscribed()){
                                subRegisterFace.unsubscribe();
                                subRegisterFace = null;
                            }

                            //更新当前人员人脸是否采集过的状态
                            if(!FaceRecogValueHelper.getInstance().isPersonRegisteredFace(index) && (faceType==2)){ //两个脸都采集了才算
                                FaceRecogValueHelper.getInstance().setPersonRegisterFace(index,true);
                            }else{
                                logger.info("当前人员index:{}已经注册过人脸...",index);
                            }

                            //发送界面更新
                            logger.debug("发送人脸采集成功通知!");
                            EventBus.getDefault().post(new FaceAddEvent(index,faceType,true));

                            if(callBack != null){
                                callBack.onComplete();
                            }
                        }else if(regFaceState == 2){
                            logger.error("注册人脸超时！");
                            if(subRegisterFace!=null && !subRegisterFace.isUnsubscribed()){
                                subRegisterFace.unsubscribe();
                                subRegisterFace = null;
                            }

                            //发送界面更新
                            logger.debug("发送人脸采集失败通知!");
                            EventBus.getDefault().post(new FaceAddEvent(index,faceType,false));

                            if(callBack != null){
                                callBack.onError();
                            }
                        }else{
                            logger.info("注册人脸采集中！");
                        }


                    }else {
                        logger.error("注册人脸失败！");
                        if(subRegisterFace!=null && !subRegisterFace.isUnsubscribed()){
                            subRegisterFace.unsubscribe();
                            subRegisterFace = null;
                        }
                        if(regFaceResult.getReturnCode() == FatiDogWorker.FAULT_CODE_UNSUPPORT){
                            VoiceHelper.getInstance().startSpeaking(
                                    CommonLib.getInstance().getContext().getString(R.string.unsupport_func), VoiceConstants.TTS_DONOTHING, false);
                        }
                        logger.debug("发送人脸采集失败通知!");
                        EventBus.getDefault().post(new FaceAddEvent(index,faceType,false));

                        if(callBack != null){
                            callBack.onError();
                        }
                    }
                },throwable -> {
                    logger.error("subRegisterFace-注册人脸失败 :{}", throwable.toString());
                    if(subRegisterFace!=null && !subRegisterFace.isUnsubscribed()){
                        subRegisterFace.unsubscribe();
                        subRegisterFace = null;
                    }

                    logger.debug("发送人脸采集失败通知!");
                    EventBus.getDefault().post(new FaceAddEvent(index,faceType,false));

                    if(callBack != null){
                        callBack.onError();
                    }
                });

        FaceRecogPersonData personData = FaceRecogValueHelper.getInstance().getRecoacPersonData(index);
        FatiDogWorker.getInstance().registerFace(personData.getPersonId());
        logger.debug("注册人脸命令发送完毕!");
    }

    private int reCheckCount = 0;
    private static int MAX_CHECK_TIMES = 8; //每次识别失败的确认超时的时长大概是10s左右，中间不足延迟，识别8次大概80s左右
    private static int CHECK_TIME_PERIOD = 3000;

    /**
     *  为了防止角度不对误判，连续识别多次，有任何一次识别成功即认为当前驾驶员合法并返回。
     * @param callBack
     */
    public void checkPerson(IAsyncTaskCallBack callBack){

        doCheckPerson(new IAsyncTaskCallBack() {
            @Override
            public void onComplete() {
                reCheckCount = 0;
                callBack.onComplete();
            }

            @Override
            public void onError() {
                reCheckCount++;
                if(reCheckCount<MAX_CHECK_TIMES){
                    try{
//                        Thread.sleep(CHECK_TIME_PERIOD);
                    }catch (Exception e){
                        logger.error("休眠异常：{}", e.toString());
                    }
                    checkPerson(callBack);
                }else{
                    reCheckCount = 0;
                    callBack.onError();
                }
            }
        });
    }

    //识别当前人员是否是白名单人员
    public void doCheckPerson(IAsyncTaskCallBack callBack){
        logger.info("开始识别人脸...");
        if(subRecogFace!=null && !subRecogFace.isUnsubscribed()){
            subRecogFace.unsubscribe();
            subRecogFace = null;
        }

        subRecogFace  = FatiDogWorker.getInstance().subscribeFaceRecognitionResult()
                .subscribeOn(Schedulers.newThread())
                .subscribe(recogFaceInfoResult -> {
                    logger.debug("接收到识别人脸命令结果：{}",recogFaceInfoResult.toString());


                    if(recogFaceInfoResult.getReturnCode() == 0){
                        if(recogFaceInfoResult.getState() == 1){
                            logger.info("识别成功");
                        if(subRecogFace!=null && !subRecogFace.isUnsubscribed()){
                            subRecogFace.unsubscribe();
                            subRecogFace = null;
                        }else{
                            logger.info("人脸识别无需取消...");
                        }
                            if(FaceRecogValueHelper.getInstance().isBelongToWhiteList(recogFaceInfoResult.getId())){
                                logger.info("当前识别人员认证成功！属于白名单成员。。");

                                int pIndex = FaceRecogValueHelper.getInstance().getPersonIndex(recogFaceInfoResult.getId());
                                String pName = FaceRecogValueHelper.getInstance().getRegPersonName(pIndex);
                                FaceRecogValueHelper.getInstance().setCurDriver(pName);

                                if(callBack != null){
                                    callBack.onComplete();
                                }
                            }else{
                                logger.info("当然识别人员认证失败！不属于白名单成员!需要告警。。。");
                                if(callBack != null){
                                    callBack.onError();
                                }
                            }
                        }else if( (recogFaceInfoResult.getState() == 2) ||
                                (recogFaceInfoResult.getState() == 3)){
                            logger.debug("人脸识别失败...");
                            if(subRecogFace!=null && !subRecogFace.isUnsubscribed()){
                                subRecogFace.unsubscribe();
                                subRecogFace = null;
                            }
                            if(callBack != null){
                                callBack.onError();
                            }
                        }else{ //0
                            logger.debug("正在识别中...");
                        }

                    }else{
                        logger.error("识别人脸失败");
                        if(subRecogFace!=null && !subRecogFace.isUnsubscribed()){
                            subRecogFace.unsubscribe();
                            subRecogFace = null;
                        }
                        if(callBack != null){
                            callBack.onError();
                        }
                    }
                },throwable -> {
                    logger.error("subRecogFace-识别人脸失败 :{}", throwable.toString());
                    if(subRecogFace!=null && !subRecogFace.isUnsubscribed()){
                        subRecogFace.unsubscribe();
                        subRecogFace = null;
                    }

                    if(callBack != null){
                        callBack.onError();
                    }
                });

        FatiDogWorker.getInstance().faceRecognition();
        logger.debug("人脸识别命令发送完毕!");
    }

    public void startRegFace(){
        logger.info("开始添加人脸！");
        //首先检查人脸库白名单是否已满
        int unFacedIndex = FaceRecogValueHelper.getInstance().getOneWhiteListPersonUnRegFace();
        if(unFacedIndex == -1){
            FaceRecogValueHelper.getInstance().setAddingFace(false);
            VoiceHelper.getInstance().startSpeaking(
                    CommonLib.getInstance().getContext().getString(R.string.reg_person_is_full), VoiceConstants.TTS_DONOTHING, false);
            EventBus.getDefault().post(new FaceSwitchPageEvent(1));
            return;
        }

        //发送通知更新界面，播放刷脸动画等
        EventBus.getDefault().post(new FaceRecogAddFaceStartEvent(true));

        //设置当前运行状态为正在刷脸
        FaceRecogValueHelper.getInstance().setAddingFace(true);

        //延时3s开始注册人脸
        doRegFace();
    }

    public void doRegFace(){
        logger.info("开始注册正脸...");
        //设置当前采集的人脸类型是正脸
        FaceRecogValueHelper.getInstance().setAddingFaceType(1);

        VoiceHelper.getInstance().startSpeaking(
                CommonLib.getInstance().getContext().getString(R.string.please_right_face_to_camera), VoiceConstants.TTS_DONOTHING, false);
        //发送通知更新界面，提示开始采集正脸
        EventBus.getDefault().post(new FaceRecogAddFaceEvent(1,true));

        Observable.timer(3, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.newThread())
                .subscribe(s -> {
                    //获取当前白名单中未添加过人脸的人员索引
                    int unFacedPersonIndex = FaceRecogValueHelper.getInstance().getOneWhiteListPersonUnRegFace();
                    long unFacedPersonId = FaceRecogValueHelper.getInstance().getRecoacPersonData(unFacedPersonIndex).getPersonId();
                    startAddFaceForPersonById(unFacedPersonId, new IAsyncTaskCallBack() {
                        @Override
                        public void onComplete() {
                            logger.info("正脸添加成功");
                            VoiceHelper.getInstance().startSpeaking(
                                    CommonLib.getInstance().getContext().getString(R.string.right_face_ok), VoiceConstants.TTS_DONOTHING, false);

                            doRegSideFace(unFacedPersonId);
                        }

                        @Override
                        public void onError() {
                            logger.error("正脸添加失败");
                            //注册失败重置注册人脸状态参数
                            try{
                                FaceRecogValueHelper.getInstance().setAddingFaceType(0);
                                FaceRecogValueHelper.getInstance().setAddingFace(false);

                                VoiceHelper.getInstance().startSpeaking(
                                        CommonLib.getInstance().getContext().getString(R.string.face_right_register_fail), VoiceConstants.TTS_DONOTHING, false);

                                //注册人脸失败，更新界面UI，重置为未进行添加状态.
                                EventBus.getDefault().post(new FaceAddResultEvent(1,false));

                            }catch (Exception e){
                                logger.error("执行正脸添加失败处理异常： {}",e.toString());
                            }

                        }
                    });
                },throwable -> {
                    logger.error("正脸添加异常:", throwable.toString());
                    //注册失败重置注册人脸状态参数
                    FaceRecogValueHelper.getInstance().setAddingFaceType(0);
                    FaceRecogValueHelper.getInstance().setAddingFace(false);

                    VoiceHelper.getInstance().startSpeaking(
                            CommonLib.getInstance().getContext().getString(R.string.face_right_register_fail), VoiceConstants.TTS_DONOTHING, false);

                    //注册人脸失败，更新界面UI，重置为未进行添加状态.
                    EventBus.getDefault().post(new FaceAddResultEvent(1,false));
                });
    }

    public void doRegSideFace(long personId){
        logger.info("开始注册侧脸...");
        //设置当前采集的人脸类型是侧脸
        FaceRecogValueHelper.getInstance().setAddingFaceType(2);

        VoiceHelper.getInstance().startSpeaking(
                CommonLib.getInstance().getContext().getString(R.string.please_drive_face_to_camera), VoiceConstants.TTS_DONOTHING, false);
        //发送通知更新界面，提示开始采集侧脸
        EventBus.getDefault().post(new FaceRecogAddFaceEvent(2,true));

        Observable.timer(3, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.newThread())
                .subscribe(s -> {
                    startAddFaceForPersonById(personId, new IAsyncTaskCallBack() {
                        @Override
                        public void onComplete() {
                            logger.error("注册侧脸人脸成功！");
                            //侧脸采集成功，完成人脸采集，开始保存当前人员人脸注册状态
                            FaceRecogValueHelper.getInstance().setPersonRegisterFace(FaceRecogValueHelper.getInstance().getPersonIndex(personId),true);
                            FaceRecogValueHelper.getInstance().setAddingFaceType(0);
                            FaceRecogValueHelper.getInstance().setAddingFace(false);

                            //语音播放侧脸采集成功
                            VoiceHelper.getInstance().startSpeaking(
                                    CommonLib.getInstance().getContext().getString(R.string.side_face_ok), VoiceConstants.TTS_DONOTHING, false);

                            //注册人脸成功，更新界面UI，跳转到人脸列表界面
                            EventBus.getDefault().post(new FaceAddResultEvent(2,true));
                        }

                        @Override
                        public void onError() {
                            logger.error("注册侧脸人脸失败！");

                            //侧脸注册失败重置注册人脸状态参数
                            FaceRecogValueHelper.getInstance().setAddingFaceType(0);
                            FaceRecogValueHelper.getInstance().setAddingFace(false);

                            VoiceHelper.getInstance().startSpeaking(
                                    CommonLib.getInstance().getContext().getString(R.string.face_side_register_fail), VoiceConstants.TTS_DONOTHING, false);

                            //注册人脸失败，更新界面UI，重置为未进行添加状态.
                            EventBus.getDefault().post(new FaceAddResultEvent(2,false));
                        }
                    });
                },throwable -> {
                    logger.error("延时异常:", throwable.toString());

                    //侧脸注册失败重置注册人脸状态参数
                    FaceRecogValueHelper.getInstance().setAddingFaceType(0);
                    FaceRecogValueHelper.getInstance().setAddingFace(false);

                    VoiceHelper.getInstance().startSpeaking(
                            CommonLib.getInstance().getContext().getString(R.string.face_side_register_fail), VoiceConstants.TTS_DONOTHING, false);

                    //注册人脸失败，更新界面UI，重置为未进行添加状态.
                    EventBus.getDefault().post(new FaceAddResultEvent(2,false));
                });
    }

    //清空指定人员ID的人脸
    public void cleanPersonFace(long personId){
        if(subCleanFaceResult!= null && !subCleanFaceResult.isUnsubscribed()){
            subCleanFaceResult.unsubscribe();
            subCleanFaceResult = null;
        }

        subCleanFaceResult = FatiDogWorker.getInstance().subscribeCleanFaceResult()
                .subscribeOn(Schedulers.newThread())
                .subscribe(result -> {
                    if(subCleanFaceResult!= null && !subCleanFaceResult.isUnsubscribed()){
                        subCleanFaceResult.unsubscribe();
                        subCleanFaceResult = null;
                    }
                    logger.info("清空人脸结果:{},",result.getReturnCode());

                    if(result.getReturnCode() == 0){
                        logger.info("删除成功，开始更新列表...");
                        int index = FaceRecogValueHelper.getInstance().getPersonIndex(personId);
                        logger.info("将从白名单中去掉的人员索引：{}，ID：{}",index,personId);
                        FaceRecogValueHelper.getInstance().setPersonRegisterFace(index,false);

                        //再更新列表
                        FaceRecogValueHelper.getInstance().updateRegFacedlist();
                        EventBus.getDefault().post(new UpdateFaceListEvent(true));

                    }else {

                    }

                }, throwable -> {
                    logger.error("清空人员personId：{}人脸异常：{}", personId,throwable.toString());
                    if(subCleanFaceResult!= null && !subCleanFaceResult.isUnsubscribed()){
                        subCleanFaceResult.unsubscribe();
                        subCleanFaceResult = null;
                    }
                });

        FatiDogWorker.getInstance().cleanFace(personId);
        logger.info("清空人脸命令发送完毕! personId：{}",personId);
    }
}
