package com.dudu.convert;

import com.dudu.convert.bean.FaceRecoFindPersonInfoBean;
import com.dudu.convert.bean.FaceRecoPersonInfoBean;
import com.dudu.convert.bean.FaceRecoRegFaceInfoBean;
import com.dudu.convert.bean.FaceRecoRegPersonCountBean;
import com.dudu.convert.bean.FaceRecoRegPersonRespBean;
import com.dudu.convert.bean.FaceRecoResultBean;
import com.dudu.convert.bean.FatiDogCaptureSliceInfoBean;
import com.dudu.convert.bean.FatiDogFacePositionInfoBean;
import com.dudu.convert.bean.FatiDogResponseBean;
import com.dudu.convert.bean.FatiDogVersionInfoBean;
import com.dudu.convert.bean.FatiDogWaringRecordBean;
import com.dudu.convert.bean.FatiDogWarningBean;

import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.Arrays;

import rx.Observable;


/**
 * Author: Robert
 * Date:  2016-12-15
 * Copyright (c) 2016,dudu Co.,Ltd. All rights reserved.
 * Desc: 疲劳预警协议执行者 (包括各种数据订阅和命令执行，是输入输出流的包裹器)
 */
public class FatiDogWorker {
    public static Logger logger = LoggerFactory.getLogger("FatiDog.convert.FatiDogWorker");
    private static FatiDogWorker instance = null;

    /**
     * 协议字节位置定义
     */
    private static final int BIT_FRAME_HEADER = 0;
    private static final int BIT_LENGTH_LOW = 1;
    private static final int BIT_LENGTH_HIGH = 2;
    private static final int BIT_LENGTH_HIGH_X = 3;
    private static final int BIT_LENGTH_HIGH_XX = 4;
    private static final int BIT_MSG_MAIN_TYPE = 9;
    private static final int BIT_MSG_SUB_TYPE_LOW = 10;
    private static final int BIT_MSG_SUB_TYPE_HIGH = 11;
    private static final int BIT_RETURN_CODE_LOW = 12;
    private static final int BIT_RETURN_CODE_HIGH = 13;
    private static final int BIT_SETTING_MASK_0 = 14;
    private static final int BIT_SETTING_MASK_1 = 15;
    private static final int BIT_SETTING_MASK_2 = 16;
    private static final int BIT_SETTING_MASK_3 = 17;
    private static final int BIT_SPEED = 18;
    private static final int BIT_SPEED_MODE = 19;

    private static final int BIT_PARITY_DEFAULT = 14; //简单短命令的校验位位置
    private static final int BIT_DATA_BASE = 14; //数据段的起始偏移

    public static final int FAULT_CODE_UNSUPPORT = 0xFF05;

    private static final int MAX_SLICE_SIZE = 512;
    private byte[] imageOffset = new byte[4];
    private byte[] imageMd5 = new byte[16];
    private int imageTotalSize = 0; //以imageTotalSize是否为0来判断是否是首个图像分片

    /**
     * 返回码(错误码)定义
     */
    public static final int RETURN_CODE_OK = 0x0000; //命令执行成功返回码
    public static final int RETURN_CODE_ERROR_TIME_OUT = 0xff01; //报文接收超时
    public static final int RETURN_CODE_ERROR_MSG_EXTRACT = 0xff02; //报文协议解析错误
    public static final int RETURN_CODE_ERROR_CHECK = 0xff03; //报文校验错误
    public static final int RETURN_CODE_ERROR_TYPE_NOT_EXIST = 0xff04; //报文类型不存在
    public static final int RETURN_CODE_ERROR_CMD_NOT_EXIST = 0xff05; //报文命令不存在
    public static final int RETURN_CODE_ERROR_MSG_DATA_EXTRACT = 0xff06; //报文数据解析错误
    public static final int RETURN_CODE_ERROR_DATA_BASE_CONNECT = 0xff10; //数据库连接失败
    public static final int RETURN_CODE_ERROR_DATA_BASE_NOT_EXIST = 0xff11; //数据库文件不存在
    public static final int RETURN_CODE_ERROR_DATA_BASE_QUREY_TIME_OUT = 0xff12; //数据库查询超时
    public static final int RETURN_CODE_ERROR_MEMERY_NOT_ENOUGH = 0xff20; //系统内存不足
    public static final int RETURN_CODE_ERROR_FLASH_NOT_ENOUGH = 0xff21; //系统永久存储空间不足
    public static final int RETURN_CODE_ERROR_CAMERA_CONNECT = 0xff31; //摄像头连接失败
    public static final int RETURN_CODE_ERROR_GET_DATA_NOT_EXIST = 0xff41; //获取的数据不存在
    public static final int RETURN_CODE_ERROR_PEOPLE_HAS_EXIST = 0xfe01; //人员 ID 已经存在
    public static final int RETURN_CODE_ERROR_PEOPLE_NOT_EXIST = 0xfe02; //人员 ID 不存在
    public static final int RETURN_CODE_ERROR_FACE_NOT_REGISTER = 0xfe03; //人员没有注册人脸
    public static final int RETURN_CODE_ERROR_FACE_NOT_EXIST = 0xfe04; //查询的人脸不存在
    public static final int RETURN_CODE_ERROR_FACE_FETURE_LENGTH = 0xfe05; //人脸特征长度错误
    public static final int RETURN_CODE_ERROR_FACE_PITURE_NOT_EXIST = 0xfe06; //查询的人脸图片不存在
    public static final int RETURN_CODE_ERROR_REGISTER_OVER_LIMIT = 0xfe07; //人员注册记录达到上限
    public static final int RETURN_CODE_ERROR_RECORD_NOT_COMPLETE = 0xfd01; //录像未完成
    public static final int RETURN_CODE_ERROR_RECORD_NOT_EXIST = 0xfd02; //录像不存在
    public static final int RETURN_CODE_ERROR_SLICE_OFFSET = 0xfd03; //错误的录像分片偏移


    /**
     * 疲劳预警协议订阅的接收事件
     */
    private Observable<ByteBuffer> subscribeFatiDogReturnCode = null;
    private Observable<FatiDogResponseBean> subscribeSimpleReturnCode = null;
    private Observable<FatiDogFacePositionInfoBean> subscribeFacePosInfoReturnCode = null;
    private Observable<FatiDogWarningBean> subscribeWarningInfo = null;
    private Observable<FatiDogWaringRecordBean> subscribeWarningRecordInfo = null;
    private Observable<FatiDogResponseBean> subscribeHeartBeat = null; //
    private Observable<FatiDogVersionInfoBean> subscribeVersionInfo = null;
    private Observable<FatiDogResponseBean> subStartFatiMonitorResult = null; //
    private Observable<FatiDogResponseBean> subStopFatiMonitorResult = null; //(
    private Observable<FatiDogResponseBean> subParamSetResult = null;
    private Observable<FatiDogResponseBean> subSystemTimeSetResult = null;
    private Observable<FatiDogResponseBean> subChangeSerialBaudResult = null;
    private Observable<FatiDogCaptureSliceInfoBean> subscribeCaptureSliceInfo = null;
    private Observable<FatiDogResponseBean> subscribeSystemRebootResult = null;
    private Observable<FaceRecoRegPersonRespBean> subRegPersonResult = null;
    private Observable<FatiDogResponseBean> subscribeDeletePersonResult = null;
    private Observable<FaceRecoRegPersonCountBean> subscribeRegPersonCountResult = null;
    private Observable<FaceRecoRegFaceInfoBean> subscribeRegFaceResult = null;
    private Observable<FaceRecoResultBean> subFaceRecogResult = null;
    private Observable<FaceRecoFindPersonInfoBean> subFindPersonResult = null;
    //

    public FatiDogWorker() {

    }

    //单例访问
    public static FatiDogWorker getInstance(){
        if(instance == null){
            instance = new FatiDogWorker();
        }
        return instance;
    }

    public void startWork(){
//        Observable.timer(0, TimeUnit.SECONDS)
//                .subscribeOn(Schedulers.io())
//                .subscribe(t -> {
//                    logger.info("单独开一个线程开启串口流的观察 > open -> 当前进程ID：{},当前线程ID：{}",android.os.Process.myPid(),Thread.currentThread().getId());

                    FatiDogStream.getInstance().open();
//                }, throwable -> logger.error("startWork", throwable));


    }

    public void stopWork(){
        subscribeFatiDogReturnCode = null;
        subscribeSimpleReturnCode = null;
        subscribeFacePosInfoReturnCode = null;
        subscribeWarningInfo = null;
        subscribeWarningRecordInfo = null;
        subscribeHeartBeat = null;
        subscribeVersionInfo = null;
        subStartFatiMonitorResult = null;
        subStopFatiMonitorResult = null;
        subParamSetResult = null;
        subSystemTimeSetResult = null;
        subChangeSerialBaudResult = null;
        subscribeCaptureSliceInfo = null;
        subscribeDeletePersonResult = null;
        subscribeRegPersonCountResult = null;
        subscribeRegFaceResult = null;
        subFaceRecogResult = null;
        subFindPersonResult = null;
        FatiDogStream.getInstance().close();
    }

    //开启疲劳驾驶检测 (主控 ->模块)
    public void startFatiMonitor(){
        sendSimpleCmd(0x02,0x0201,0xF1);
    }

    //订阅“开启疲劳驾驶检测”执行结果 (模块 ->主控)
    public Observable<FatiDogResponseBean> subscribeStartFatiMonitorResult(){
        if(subStartFatiMonitorResult == null){
            subStartFatiMonitorResult = getSimpleReturnCode(0x12,0x0201);
        }
        return subStartFatiMonitorResult;
    }

    //关闭疲劳驾驶检测 (主控 ->模块)
    public void stopFatiMonitor(){
        sendSimpleCmd(0x02,0x0202,0xF0);
    }

    //订阅“关闭疲劳驾驶检测”执行结果 (模块 ->主控)
    public Observable<FatiDogResponseBean> subscribeStopFatiMonitorResult(){
        if(subStopFatiMonitorResult == null){
            subStopFatiMonitorResult = getSimpleReturnCode(0x12,0x0202);//.publish().autoConnect();
        }
        return subStopFatiMonitorResult;
    }

    //发送参数设置长命令 (目前只支持设置速度和速度模式) (主控 ->模块)
    public void sendParamSetCmd(int speed, int speedMode){
        byte[] cmd = new byte[51];
        Arrays.fill(cmd,(byte)0x00);

        cmd[BIT_FRAME_HEADER] = (byte)0xFB;
        cmd[BIT_LENGTH_LOW] = (byte)0x33;
        cmd[BIT_LENGTH_HIGH] = (byte)0x00;
        cmd[BIT_MSG_MAIN_TYPE] = (byte)0x02;
        cmd[BIT_MSG_SUB_TYPE_LOW] = (byte)(0x03);
        cmd[BIT_MSG_SUB_TYPE_HIGH] = (byte)(0x02);
        cmd[BIT_RETURN_CODE_LOW] = (byte)(0x00);
        cmd[BIT_RETURN_CODE_HIGH] = (byte)(0x00);
        cmd[BIT_SETTING_MASK_0] = (byte)0x03; //速度和速度模式置位
        cmd[BIT_SETTING_MASK_1] = (byte)0x00;
        cmd[BIT_SETTING_MASK_2] = (byte)0x00;
        cmd[BIT_SETTING_MASK_3] = (byte)0x00;
        cmd[BIT_SPEED] = (byte)(speed&0x00FF);
        cmd[BIT_SPEED_MODE] = (byte)(speedMode&0x00FF);

        FatiDogStream.getInstance().send(cmd,false);
    }

    //订阅“参数设置”执行结果 (模块 ->主控)
    public Observable<FatiDogResponseBean> subscribeParamSetResult(){

        if(subParamSetResult == null){
            subParamSetResult = getSimpleReturnCode(0x12,0x0203);
        }
        return subParamSetResult;
    }

    //获取人脸位置信息 (主控 ->模块)
    public void getFacePosition(){
        sendSimpleCmd(0x02,0x0204,0xEE);
    }
    //订阅“获取人脸位置信息”执行结果 (模块 ->主控)
    public Observable<FatiDogFacePositionInfoBean> subscribeGetFacePositionResult(){
        int mainType = 0x12;
        int subType = 0x0204;
        if (subscribeFacePosInfoReturnCode == null) {
            subscribeFacePosInfoReturnCode = getFatiDogReturnCode(mainType,subType)
                    .map(dataBuf -> {
                        FatiDogFacePositionInfoBean result = new FatiDogFacePositionInfoBean();

                        int returnCode = getInt(new byte[]{dataBuf.get(BIT_RETURN_CODE_LOW),dataBuf.get(BIT_RETURN_CODE_HIGH)});
                        int faceCount = dataBuf.get(BIT_DATA_BASE+0);
                        int oppLeft = getInt(new byte[]{dataBuf.get(BIT_DATA_BASE+1),dataBuf.get(BIT_DATA_BASE+2)});
                        int oppTop = getInt(new byte[]{dataBuf.get(BIT_DATA_BASE+3),dataBuf.get(BIT_DATA_BASE+4)});
                        int oppRight = getInt(new byte[]{dataBuf.get(BIT_DATA_BASE+5),dataBuf.get(BIT_DATA_BASE+6)});
                        int oppBottom = getInt(new byte[]{dataBuf.get(BIT_DATA_BASE+7),dataBuf.get(BIT_DATA_BASE+8)});
                        int angle = dataBuf.get(BIT_DATA_BASE+9);
                        int light = dataBuf.get(BIT_DATA_BASE+10);

                        result.setReturnCode(returnCode);
                        result.setFaceCount(faceCount);
                        result.setOppLeft(oppLeft);
                        result.setOppTop(oppTop);
                        result.setOppRight(oppRight);
                        result.setOppBottom(oppBottom);
                        result.setAngle(angle);
                        result.setLight(light);

                        return result;
                    });
        }
        return subscribeFacePosInfoReturnCode;
    }

    //订阅“告警信息” (模块 ->主控)
    public Observable<FatiDogWarningBean> subscribeWarningInfo(){
        int mainType = 0x02;
        int subType = 0x0206;
        if (subscribeWarningInfo == null) {
            subscribeWarningInfo = getFatiDogReturnCode(mainType,subType)
                    .map(dataBuf -> {
                        int returnCode = getInt(new byte[]{dataBuf.get(BIT_RETURN_CODE_LOW),dataBuf.get(BIT_RETURN_CODE_HIGH)});
                        int warnType = dataBuf.get(BIT_DATA_BASE+0);
                        FatiDogWarningBean result = new FatiDogWarningBean(returnCode,warnType);
                        return result;
                    });
        }
        return subscribeWarningInfo;
    }

    //发送“告警信息”响应 (主控 ->模块)
    public void sendWarningInfoResponse(){
        sendSimpleCmd(0x12,0x0206,0xDC);
    }

    //订阅“告警信息(开启告警录像后)” (模块 ->主控)
    public Observable<FatiDogWaringRecordBean> subscribeWarningRecordInfo(){
        int mainType = 0x02;
        int subType = 0x0206;
        if (subscribeWarningRecordInfo == null) {
            subscribeWarningRecordInfo = getFatiDogReturnCode(mainType,subType)
                    .map(dataBuf -> {
                        int returnCode = getInt(new byte[]{dataBuf.get(BIT_RETURN_CODE_LOW),dataBuf.get(BIT_RETURN_CODE_HIGH)});
                        int warnType = dataBuf.get(BIT_DATA_BASE+0);
                        int timeStampLow = getInt(new byte[]{dataBuf.get(BIT_DATA_BASE+1),dataBuf.get(BIT_DATA_BASE+2),dataBuf.get(BIT_DATA_BASE+3),dataBuf.get(BIT_DATA_BASE+4)});
                        int timeStampHigh = getInt(new byte[]{dataBuf.get(BIT_DATA_BASE+5),dataBuf.get(BIT_DATA_BASE+6),dataBuf.get(BIT_DATA_BASE+7),dataBuf.get(BIT_DATA_BASE+8)});
                        int videoId = getInt(new byte[]{dataBuf.get(BIT_DATA_BASE+9),dataBuf.get(BIT_DATA_BASE+10)});

                        FatiDogWaringRecordBean result = new FatiDogWaringRecordBean(returnCode,warnType,timeStampLow,timeStampHigh,videoId);
                        return result;
                    });
        }
        return subscribeWarningRecordInfo;
    }

    //发送“告警信息(开启告警录像后)”响应 (主控 ->模块)
    public void sendWarningRecordResponse(){
        sendSimpleCmd(0x12,0x0207,0xDB);
    }

    //发送"获取告警录像信息"命令 (主控 ->模块)
    private void sendQureyWarningRecordCmd(int videoId, int offset, int size){
        byte[] cmd = new byte[25];
        Arrays.fill(cmd,(byte)0x00);

        cmd[BIT_FRAME_HEADER] = (byte)0xFB;
        cmd[BIT_LENGTH_LOW] = (byte)0x19;
        cmd[BIT_LENGTH_HIGH] = (byte)0x00;
        cmd[BIT_MSG_MAIN_TYPE] = (byte)0x02;
        cmd[BIT_MSG_SUB_TYPE_LOW] = (byte)(0x08);
        cmd[BIT_MSG_SUB_TYPE_HIGH] = (byte)(0x02);
        cmd[BIT_RETURN_CODE_LOW] = (byte)(0x00);
        cmd[BIT_RETURN_CODE_HIGH] = (byte)(0x00);

        byte[] tmp = null;
        tmp = int2bytes(videoId, 2);
        cmd[BIT_DATA_BASE] = (byte)tmp[0];
        cmd[BIT_DATA_BASE+1] = (byte)tmp[1];
        tmp = int2bytes(offset, 4);
        cmd[BIT_DATA_BASE+2] = (byte)tmp[0];
        cmd[BIT_DATA_BASE+3] = (byte)tmp[1];
        cmd[BIT_DATA_BASE+4] = (byte)tmp[2];
        cmd[BIT_DATA_BASE+5] = (byte)tmp[3];
        tmp = int2bytes(size, 4);
        cmd[BIT_DATA_BASE+6] = (byte)tmp[0];
        cmd[BIT_DATA_BASE+7] = (byte)tmp[1];
        cmd[BIT_DATA_BASE+8] = (byte)tmp[2];
        cmd[BIT_DATA_BASE+9] = (byte)tmp[3];

        FatiDogStream.getInstance().send(cmd,false);
    }

    //订阅“获取告警录像信息”执行结果 //后面接收的数据过多，包括视频数据，以后另行处理
//    public Observable<FatiDogWaringRecordInfoBean> subscribeQureyWarningRecordResult(){
//        return ;
//    }

    //订阅“心跳信息” (模块 ->主控)
    public Observable<FatiDogResponseBean> subscribeHeartBeat(){
        int mainType = 0x09;
        int subType = 0x0901;
        if (subscribeHeartBeat == null) {
            subscribeHeartBeat = getFatiDogReturnCode(mainType,subType)
                    .map(dataBuf -> {
                        logger.info("subscribeHeartBeat -> 当前进程ID：{},当前线程ID：{}",android.os.Process.myPid(),Thread.currentThread().getId());
                        int returnCode = getInt(new byte[]{dataBuf.get(BIT_RETURN_CODE_LOW),dataBuf.get(BIT_RETURN_CODE_HIGH)});
                        FatiDogResponseBean result = new FatiDogResponseBean(mainType,subType,returnCode);
                        return result;
                    });
        }
        return subscribeHeartBeat;
    }

    //发送“心跳信息”响应 (主控 ->模块)
    public void sendHeartBeatResponse(){
        sendSimpleCmd(0x12,0x0901,0xDA);
    }


    //查询版本信息(主控 ->模块)
    public void getFatiDogVersionInfo(){
        sendSimpleCmd(0x09,0x0902,0xE2);
    }

    //订阅“查询版本信息”执行结果 (模块 ->主控)
    public Observable<FatiDogVersionInfoBean> subscribeVersionInfoResult(){
        logger.info("subscribeVersionInfoResult -> -> 当前进程ID：{},当前线程ID：{}",android.os.Process.myPid(),Thread.currentThread().getId());
        int mainType = 0x12;
        int subType = 0x0902;
        if (subscribeVersionInfo == null) {
            subscribeVersionInfo = getFatiDogReturnCode(mainType,subType)
                    .map(dataBuf -> {
                        logger.info("subscribeVersionInfoResult -> 查询版本信息 -> 当前进程ID：{},当前线程ID：{}",android.os.Process.myPid(),Thread.currentThread().getId());
                        int returnCode = getInt(new byte[]{dataBuf.get(BIT_RETURN_CODE_LOW),dataBuf.get(BIT_RETURN_CODE_HIGH)});

                        byte[] tmpBytes = new byte[20];
                        System.arraycopy(dataBuf.array(), BIT_DATA_BASE, tmpBytes, 0, 20);
                        String hardwareVersion = new String(tmpBytes);

                        System.arraycopy(dataBuf.array(), BIT_DATA_BASE+20, tmpBytes, 0, 20);
                        String firmwareVersion = new String(tmpBytes);

                        System.arraycopy(dataBuf.array(), BIT_DATA_BASE+40, tmpBytes, 0, 20);
                        String softwareVersion = new String(tmpBytes);

                        FatiDogVersionInfoBean result = new FatiDogVersionInfoBean(returnCode,hardwareVersion,firmwareVersion,softwareVersion);
                        return result;
                    });
        }
        return subscribeVersionInfo;
    }

    //发送“设置系统时间”命令 (主控->模块)
    public void sendSystemTimeSet(String sysTime){
        byte[] cmd = new byte[34]; //15+19
        Arrays.fill(cmd,(byte)0x00);

        cmd[BIT_FRAME_HEADER] = (byte)0xFB;
        cmd[BIT_LENGTH_LOW] = (byte)0x22;
        cmd[BIT_LENGTH_HIGH] = (byte)0x00;
        cmd[BIT_MSG_MAIN_TYPE] = (byte)0x09;
        cmd[BIT_MSG_SUB_TYPE_LOW] = (byte)(0x03);
        cmd[BIT_MSG_SUB_TYPE_HIGH] = (byte)(0x09);
        cmd[BIT_RETURN_CODE_LOW] = (byte)(0x00);
        cmd[BIT_RETURN_CODE_HIGH] = (byte)(0x00);

        byte[] tmp = sysTime.getBytes();
        if(tmp.length != 19){
            logger.error("设置时间格式错误,长度 : {}",tmp.length);
            return;
        }
        System.arraycopy(tmp, 0, cmd, BIT_DATA_BASE, 19); //从tmp拷贝到cmd的数据段中
        FatiDogStream.getInstance().send(cmd,false);
    }

    //订阅“设置系统时间”执行结果 (模块 ->主控)
    public Observable<FatiDogResponseBean> subscribeSystemTimeSetResult(){
        if(subSystemTimeSetResult == null){
            subSystemTimeSetResult = getSimpleReturnCode(0x12,0x0903);//.publish().autoConnect();
        }
        return subSystemTimeSetResult;
    }

    //发送“抓图”命令 (主控->模块)
    public void capture(){
        sendSimpleCmd(0x09,0x0904,0xE0);
    }

    //订阅“抓图”执行结果  (模块 ->主控)//这个也是后面要接收长数据
//    public Observable<FatiDogResponseBean> subscribeCaptureResult(){
//        return getSimpleReturnCode(0x12,0x0904);
//    }

    //发送“修改串口波特率”命令 (主控->模块)
    private void ChangeSerialBaud(int baud){
        byte[] cmd = new byte[19]; //15+4
        Arrays.fill(cmd,(byte)0x00);

        cmd[BIT_FRAME_HEADER] = (byte)0xFB;
        cmd[BIT_LENGTH_LOW] = (byte)0x13;
        cmd[BIT_LENGTH_HIGH] = (byte)0x00;
        cmd[BIT_MSG_MAIN_TYPE] = (byte)0x09;
        cmd[BIT_MSG_SUB_TYPE_LOW] = (byte)(0x05);
        cmd[BIT_MSG_SUB_TYPE_HIGH] = (byte)(0x09);
        cmd[BIT_RETURN_CODE_LOW] = (byte)(0x00);
        cmd[BIT_RETURN_CODE_HIGH] = (byte)(0x00);

        byte[] tmp = int2bytes(baud,4);
        System.arraycopy(tmp, 0, cmd, BIT_DATA_BASE, 4); //从tmp拷贝到cmd的数据段中

        FatiDogStream.getInstance().send(cmd,false);
    }

    //订阅“修改串口波特率”执行结果 (模块 ->主控)
    public Observable<FatiDogResponseBean> subscribeChangeSerialBaudResult(){
        if(subChangeSerialBaudResult == null){
            subChangeSerialBaudResult = getSimpleReturnCode(0x12,0x0905);
        }
        return subChangeSerialBaudResult;
    }

    //发送“分片抓图”命令 (主控->模块)
    public void captureSlice(boolean isFirst){
        byte[] cmd = new byte[39];
        int requestSliceSize = 0;
        int leftSize = 0;
        int msgType = 0x09;
        int cmdType = 0x0925;

        cmd[BIT_FRAME_HEADER] = (byte)0xFB;
        cmd[BIT_LENGTH_LOW] = (byte)0x27;
        cmd[BIT_LENGTH_HIGH] = (byte)0x00;
        cmd[BIT_MSG_MAIN_TYPE] = (byte)msgType;
        cmd[BIT_MSG_SUB_TYPE_LOW] = (byte)(cmdType&0x00FF);
        cmd[BIT_MSG_SUB_TYPE_HIGH] = (byte)((cmdType&0xFF00)>>8);
        cmd[BIT_RETURN_CODE_LOW] = (byte)0x00;
        cmd[BIT_RETURN_CODE_HIGH] = (byte)0x00;

        if(imageTotalSize == 0 || isFirst){ //是否是首片
            imageTotalSize = 0;
            Arrays.fill(imageOffset,(byte)0);
            Arrays.fill(imageMd5,(byte)0); //md5区清零
        }

        System.arraycopy(imageMd5, 0, cmd, BIT_DATA_BASE, imageMd5.length);
        System.arraycopy(imageOffset, 0, cmd, BIT_DATA_BASE+16, imageOffset.length);

        int offset = getInt(imageOffset);
        if(imageTotalSize <= 0){
            requestSliceSize = MAX_SLICE_SIZE;
        }else{
            leftSize = imageTotalSize - offset;

            if(leftSize < MAX_SLICE_SIZE){
                requestSliceSize = leftSize;
            }else{
                requestSliceSize = MAX_SLICE_SIZE;
            }
        }
        byte[] sliceSize = int2bytes(requestSliceSize,4);
        System.arraycopy(sliceSize, 0, cmd, BIT_DATA_BASE+20, sliceSize.length);

        cmd[38] = (byte)0x00;

        logger.info("imageTotalSize:{},imageOffset:{},requestSliceSize:{},leftSize:{}",imageTotalSize,imageOffset,requestSliceSize,leftSize);
        FatiDogStream.getInstance().send(cmd,false);
    }



    //订阅“分片抓图”执行结果  (模块 ->主控)//这个也是后面要接收长数据
    public Observable<FatiDogCaptureSliceInfoBean> subscribeCaptureSilceResult(){
//        logger.info("subscribeCaptureSliceInfo -> -> 当前进程ID：{},当前线程ID：{}",android.os.Process.myPid(),Thread.currentThread().getId());
        int mainType = 0x12;
        int subType = 0x0925;

        if(subscribeCaptureSliceInfo == null){
            subscribeCaptureSliceInfo  = getFatiDogReturnCode(mainType,subType)
                    .map(dataBuf -> {
//                        logger.info("subscribeCaptureSliceInfo -> 分片抓拍 -> 当前进程ID：{},当前线程ID：{}",android.os.Process.myPid(),Thread.currentThread().getId());
                        int returnCode = getInt(new byte[]{dataBuf.get(BIT_RETURN_CODE_LOW),dataBuf.get(BIT_RETURN_CODE_HIGH)});

                        if(returnCode != 0){
                            FatiDogCaptureSliceInfoBean result = new FatiDogCaptureSliceInfoBean(returnCode,
                                    0,
                                    null,
                                    0,
                                    0,
                                    null,
                                    0);
                            return result;
                        }

                        int totalSize = getInt(new byte[]{dataBuf.get(BIT_DATA_BASE),dataBuf.get(BIT_DATA_BASE+1),dataBuf.get(BIT_DATA_BASE+2),dataBuf.get(BIT_DATA_BASE+3)});
                        System.arraycopy(dataBuf.array(), BIT_DATA_BASE+4, imageMd5, 0, 16);
                        int offset = getInt(new byte[]{dataBuf.get(BIT_DATA_BASE+20),dataBuf.get(BIT_DATA_BASE+21),dataBuf.get(BIT_DATA_BASE+22),dataBuf.get(BIT_DATA_BASE+23)});
                        int size = getInt(new byte[]{dataBuf.get(BIT_DATA_BASE+24),dataBuf.get(BIT_DATA_BASE+25),dataBuf.get(BIT_DATA_BASE+26),dataBuf.get(BIT_DATA_BASE+27)});

                        //获取图片数据数组
                        byte[] imageData = null;
                        if(size > 0){
                            imageData = new byte[size];
                            System.arraycopy(dataBuf.array(), BIT_DATA_BASE+28, imageData, 0, size);
                        }

                        int sliceIndex = 0;
                        //更新图片记录信息
                        int newOffset = offset+size;
                        int2bytes(imageOffset,newOffset);
                        if(newOffset >= totalSize){
                            imageTotalSize = 0; //已经读完了

                            sliceIndex = 1;
                        }else{
                            imageTotalSize = totalSize;
                        }
                        logger.info("接收分片信息。。。newOffset:{},imageTotalSize:{}",newOffset,imageTotalSize);

                        FatiDogCaptureSliceInfoBean result = new FatiDogCaptureSliceInfoBean(returnCode,
                                totalSize,
                                imageMd5,
                                offset,
                                size,
                                imageData,
                                sliceIndex);
                        return result;
                    });
        }

        return subscribeCaptureSliceInfo;
    }

    //固件升级，暂时不做

    //系统重启
    public void systemReboot(){
        sendSimpleCmd(0x09,0x0930);
    }

    //订阅“系统重启响应” (模块 ->主控) //成功没有返回码，错误会返回错误码
    public Observable<FatiDogResponseBean> subscribeSystemRebootResult(){
        int mainType = 0x12;
        int subType = 0x0930;
        if (subscribeSystemRebootResult == null) {
            subscribeSystemRebootResult = getFatiDogReturnCode(mainType,subType)
                    .map(dataBuf -> {
                        logger.info("subscribeHeartBeat -> 当前进程ID：{},当前线程ID：{}",android.os.Process.myPid(),Thread.currentThread().getId());
                        int returnCode = getInt(new byte[]{dataBuf.get(BIT_RETURN_CODE_LOW),dataBuf.get(BIT_RETURN_CODE_HIGH)});
                        FatiDogResponseBean result = new FatiDogResponseBean(mainType,subType,returnCode);
                        return result;
                    });
        }
        return subscribeSystemRebootResult;
    }
    /***********************人脸识别****************************/
    //注册人员
    public void registerPerson(FaceRecoPersonInfoBean data){
        int mainType = 0x01;
        int subType = 0x0101;
        byte[] cmd = new byte[367];
        Arrays.fill(cmd, (byte)0); //清0

        cmd[BIT_FRAME_HEADER] = (byte)0xFB;
        cmd[BIT_LENGTH_LOW] = (byte)0x6F; //367
        cmd[BIT_LENGTH_HIGH] = (byte)0x01;
        cmd[BIT_LENGTH_HIGH_X] = (byte)0x00;
        cmd[BIT_LENGTH_HIGH_XX] = (byte)0x00;
        cmd[BIT_MSG_MAIN_TYPE] = (byte)mainType;
        cmd[BIT_MSG_SUB_TYPE_LOW] = (byte)(subType&0x00FF);
        cmd[BIT_MSG_SUB_TYPE_HIGH] = (byte)((subType&0xFF00)>>8);
        cmd[BIT_RETURN_CODE_LOW] = (byte)0x00;
        cmd[BIT_RETURN_CODE_HIGH] = (byte)0x00;

        //将人员信息复制到指定字节
        cmd[BIT_DATA_BASE+0] = (byte)(data.getPersonId()&0x00FF);
        cmd[BIT_DATA_BASE+1] = (byte)((data.getPersonId()>>8)&0x00FF);
        cmd[BIT_DATA_BASE+2] = (byte)((data.getPersonId()>>16)&0x00FF);
        cmd[BIT_DATA_BASE+3] = (byte)((data.getPersonId()>>24)&0x00FF);
        cmd[BIT_DATA_BASE+4] = (byte)((data.getPersonId()>>32)&0x00FF);
        cmd[BIT_DATA_BASE+5] = (byte)((data.getPersonId()>>40)&0x00FF);
        cmd[BIT_DATA_BASE+6] = (byte)((data.getPersonId()>>48)&0x00FF);
        cmd[BIT_DATA_BASE+7] = (byte)((data.getPersonId()>>56)&0x00FF);

        int nameBase = BIT_DATA_BASE+8;
        int nameLen = data.getName().getBytes().length<64?data.getName().getBytes().length:64;
        System.arraycopy(data.getName().getBytes(),0,cmd,nameBase,nameLen);

        int cardBase = nameBase+64;
        int cardLen = data.getCard().getBytes().length<20?data.getCard().getBytes().length:20;
        System.arraycopy(data.getCard().getBytes(),0,cmd,cardBase,cardLen);

        int regTimeBase = cardBase+20;
        int regTimeLen = data.getRegTime().length()<20?data.getRegTime().length():20;
        System.arraycopy(data.getRegTime().getBytes(),0,cmd,regTimeBase,regTimeLen);

        //其他的属性暂时先不加
        FatiDogStream.getInstance().send(cmd,false);
    }

    //订阅 人员注册 结果
    public Observable<FaceRecoRegPersonRespBean> subscribeRegisterPersonResult(){
        int mainType = 0x12;
        int subType = 0x0101;
        if (subRegPersonResult == null) {
            subRegPersonResult = getFatiDogReturnCode(mainType,subType)
                    .map(dataBuf -> {
                        logger.debug("subscribeRegisterPersonResult -> 当前进程ID：{},当前线程ID：{}",android.os.Process.myPid(),Thread.currentThread().getId());
                        if(dataBuf.array().length == 23){
                            int returnCode = getInt(new byte[]{dataBuf.get(BIT_RETURN_CODE_LOW),dataBuf.get(BIT_RETURN_CODE_HIGH)});
                            long personId = getLong(new byte[]{dataBuf.get(BIT_DATA_BASE+0),
                                    dataBuf.get(BIT_DATA_BASE+1),
                                    dataBuf.get(BIT_DATA_BASE+2),
                                    dataBuf.get(BIT_DATA_BASE+3),
                                    dataBuf.get(BIT_DATA_BASE+4),
                                    dataBuf.get(BIT_DATA_BASE+5),
                                    dataBuf.get(BIT_DATA_BASE+6),
                                    dataBuf.get(BIT_DATA_BASE+7)});

                            FaceRecoRegPersonRespBean result = new FaceRecoRegPersonRespBean(returnCode,personId);
                            return result;
                        }else{
                            logger.error("人员注册结果长度异常！{}",String.copyValueOf(Hex.encodeHex(dataBuf.array())).toUpperCase());
                            FaceRecoRegPersonRespBean result = new FaceRecoRegPersonRespBean();
                            return result;
                        }

                    });
        }
        return subRegPersonResult;
    }

    //删除人员
    public void deletePerson(long personId){
        int mainType = 0x01;
        int subType = 0x0102;
        byte[] cmd = new byte[23];
        Arrays.fill(cmd, (byte)0); //清0

        cmd[BIT_FRAME_HEADER] = (byte)0xFB;
        cmd[BIT_LENGTH_LOW] = (byte)0x17; //367
        cmd[BIT_LENGTH_HIGH] = (byte)0x00;
        cmd[BIT_LENGTH_HIGH_X] = (byte)0x00;
        cmd[BIT_LENGTH_HIGH_XX] = (byte)0x00;
        cmd[BIT_MSG_MAIN_TYPE] = (byte)mainType;
        cmd[BIT_MSG_SUB_TYPE_LOW] = (byte)(subType&0x00FF);
        cmd[BIT_MSG_SUB_TYPE_HIGH] = (byte)((subType&0xFF00)>>8);
        cmd[BIT_RETURN_CODE_LOW] = (byte)0x00;
        cmd[BIT_RETURN_CODE_HIGH] = (byte)0x00;

        //写入personId
        byte[] personBytes = new byte[8];
        long2Byte(personBytes, personId);
        System.arraycopy(personBytes,0,cmd,BIT_DATA_BASE,8);

        FatiDogStream.getInstance().send(cmd,false);
    }

    //订阅删除人员结果
    public Observable<FatiDogResponseBean> subscribeDeletePersonResult(){
        int mainType = 0x12;
        int subType = 0x0102;

        return getSimpleReturnCode(mainType,subType);
    }

    //清空人员
    public void cleanPerson(){
        sendSimpleCmd(0x01,0x0103);
    }
    //订阅清空人员结果
    public Observable<FatiDogResponseBean> subscribeCleanPersonResult(){
        int mainType = 0x12;
        int subType = 0x0103;

        return getSimpleReturnCode(mainType,subType);
    }

    //修改人员信息
    public void editPerson(FaceRecoPersonInfoBean data){
        int mainType = 0x01;
        int subType = 0x0104;
        byte[] cmd = new byte[367];
        Arrays.fill(cmd, (byte)0); //清0

        cmd[BIT_FRAME_HEADER] = (byte)0xFB;
        cmd[BIT_LENGTH_LOW] = (byte)0x6F; //367
        cmd[BIT_LENGTH_HIGH] = (byte)0x01;
        cmd[BIT_LENGTH_HIGH_X] = (byte)0x00;
        cmd[BIT_LENGTH_HIGH_XX] = (byte)0x00;
        cmd[BIT_MSG_MAIN_TYPE] = (byte)mainType;
        cmd[BIT_MSG_SUB_TYPE_LOW] = (byte)(subType&0x00FF);
        cmd[BIT_MSG_SUB_TYPE_HIGH] = (byte)((subType&0xFF00)>>8);
        cmd[BIT_RETURN_CODE_LOW] = (byte)0x00;
        cmd[BIT_RETURN_CODE_HIGH] = (byte)0x00;

        //将人员信息复制到指定字节
        cmd[BIT_DATA_BASE+0] = (byte)(data.getPersonId()&0x00FF);
        cmd[BIT_DATA_BASE+1] = (byte)((data.getPersonId()>>8)&0x00FF);
        cmd[BIT_DATA_BASE+2] = (byte)((data.getPersonId()>>16)&0x00FF);
        cmd[BIT_DATA_BASE+3] = (byte)((data.getPersonId()>>24)&0x00FF);
        cmd[BIT_DATA_BASE+4] = (byte)((data.getPersonId()>>32)&0x00FF);
        cmd[BIT_DATA_BASE+5] = (byte)((data.getPersonId()>>40)&0x00FF);
        cmd[BIT_DATA_BASE+6] = (byte)((data.getPersonId()>>48)&0x00FF);
        cmd[BIT_DATA_BASE+7] = (byte)((data.getPersonId()>>56)&0x00FF);

        int nameBase = BIT_DATA_BASE+8;
        int nameLen = data.getName().getBytes().length<64?data.getName().getBytes().length:64;
        System.arraycopy(data.getName().getBytes(),0,cmd,nameBase,nameLen);

        int cardBase = nameBase+64;
        int cardLen = data.getCard().getBytes().length<20?data.getCard().getBytes().length:20;
        System.arraycopy(data.getCard().getBytes(),0,cmd,cardBase,cardLen);

        int regTimeBase = cardBase+20;
        int regTimeLen = data.getRegTime().length()<20?data.getRegTime().length():20;
        System.arraycopy(data.getRegTime().getBytes(),0,cmd,regTimeBase,regTimeLen);

        //其他的属性暂时先不加
        FatiDogStream.getInstance().send(cmd,false);
    }
    //订阅修改人员信息结果
    public Observable<FatiDogResponseBean> subscribeEditPersonResult(){
        int mainType = 0x12;
        int subType = 0x0104;

        return getSimpleReturnCode(mainType,subType);
    }

    //发送查询人员信息命令
    public void findRegPersonInfo(long personId){
        int mainType = 0x01;
        int subType = 0x0105;
        byte[] cmd = new byte[23];
        Arrays.fill(cmd, (byte)0); //清0

        cmd[BIT_FRAME_HEADER] = (byte)0xFB;
        cmd[BIT_LENGTH_LOW] = (byte)0x17;
        cmd[BIT_LENGTH_HIGH] = (byte)0x00;
        cmd[BIT_LENGTH_HIGH_X] = (byte)0x00;
        cmd[BIT_LENGTH_HIGH_XX] = (byte)0x00;
        cmd[BIT_MSG_MAIN_TYPE] = (byte)mainType;
        cmd[BIT_MSG_SUB_TYPE_LOW] = (byte)(subType&0x00FF);
        cmd[BIT_MSG_SUB_TYPE_HIGH] = (byte)((subType&0xFF00)>>8);
        cmd[BIT_RETURN_CODE_LOW] = (byte)0x00;
        cmd[BIT_RETURN_CODE_HIGH] = (byte)0x00;

        //写入personId
        byte[] personBytes = new byte[8];
        long2Byte(personBytes, personId);
        System.arraycopy(personBytes,0,cmd,BIT_DATA_BASE,8);

        FatiDogStream.getInstance().send(cmd,false);
    }

    //订阅查询人员信息结果
    public Observable<FaceRecoFindPersonInfoBean> subcribeFindPersonResult(){
        int mainType = 0x12;
        int subType = 0x0105;
        if (subFindPersonResult == null) {
            subFindPersonResult = getFatiDogReturnCode(mainType,subType)
                    .map(dataBuf -> {
                        logger.debug("subFindPersonResult -> 当前进程ID：{},当前线程ID：{}",android.os.Process.myPid(),Thread.currentThread().getId());
                        int returnCode = getInt(new byte[]{dataBuf.get(BIT_RETURN_CODE_LOW),dataBuf.get(BIT_RETURN_CODE_HIGH)});

                        if(dataBuf.array().length == 368){
                            FaceRecoFindPersonInfoBean result = new FaceRecoFindPersonInfoBean();

                            long personId = getLong(new byte[]{dataBuf.get(BIT_DATA_BASE+0),
                                    dataBuf.get(BIT_DATA_BASE+1),
                                    dataBuf.get(BIT_DATA_BASE+2),
                                    dataBuf.get(BIT_DATA_BASE+3),
                                    dataBuf.get(BIT_DATA_BASE+4),
                                    dataBuf.get(BIT_DATA_BASE+5),
                                    dataBuf.get(BIT_DATA_BASE+6),
                                    dataBuf.get(BIT_DATA_BASE+7)});

                            int faceCount = dataBuf.get(BIT_DATA_BASE+8);
                            byte[] nameBytes = new byte[64];
                            System.arraycopy(dataBuf.array(),BIT_DATA_BASE+9,nameBytes,0,64);
                            String name = String.valueOf(nameBytes);

                            byte[] cardBytes = new byte[20];
                            System.arraycopy(dataBuf.array(),BIT_DATA_BASE+9+64,cardBytes,0,20);
                            String card = String.valueOf(cardBytes);

                            byte[] regTimeBytes = new byte[20];
                            System.arraycopy(dataBuf.array(),BIT_DATA_BASE+9+64+20,regTimeBytes,0,20);
                            String regTime = String.valueOf(regTimeBytes);

                            byte[] attr1Bytes = new byte[48];
                            System.arraycopy(dataBuf.array(),BIT_DATA_BASE+9+64+20+20,attr1Bytes,0,48);
                            String attr1 = String.valueOf(attr1Bytes);

                            byte[] attr2Bytes = new byte[48];
                            System.arraycopy(dataBuf.array(),BIT_DATA_BASE+9+64+20+20+48,attr2Bytes,0,48);
                            String attr2 = String.valueOf(attr2Bytes);

                            byte[] attr3Bytes = new byte[48];
                            System.arraycopy(dataBuf.array(),BIT_DATA_BASE+9+64+20+20+48+48,attr3Bytes,0,48);
                            String attr3 = String.valueOf(attr3Bytes);

                            byte[] attr4Bytes = new byte[48];
                            System.arraycopy(dataBuf.array(),BIT_DATA_BASE+9+64+20+20+48+48+48,attr4Bytes,0,48);
                            String attr4 = String.valueOf(attr4Bytes);

                            byte[] attr5Bytes = new byte[48];
                            System.arraycopy(dataBuf.array(),BIT_DATA_BASE+9+64+20+20+48+48+48+48,attr5Bytes,0,48);
                            String attr5 = String.valueOf(attr5Bytes);

                            result.setReturnCode(returnCode);
                            result.setPersonId(personId);
                            result.setName(name);
                            result.setCard(card);
                            result.setRegTime(regTime);
                            result.setAttr1(attr1);
                            result.setAttr2(attr2);
                            result.setAttr3(attr3);
                            result.setAttr4(attr4);
                            result.setAttr5(attr5);

                            return result;
                        }else{
                            logger.error("查询人员信息结果长度异常");
                            FaceRecoFindPersonInfoBean result = new FaceRecoFindPersonInfoBean();
                            result.setReturnCode(returnCode);
                            return result;
                        }

                    });
        }
        return subFindPersonResult;
    }

    //发送命令 查询已经注册的人员数量
    public void getPersonCount(){
        sendSimpleCmd(0x01,0x0107);
    }
    //订阅 已经注册的人员数量结果 //
    public Observable<FaceRecoRegPersonCountBean> subRegPersonCountResult(){
        int mainType = 0x12;
        int subType = 0x0101;
        if (subscribeRegPersonCountResult == null) {
            subscribeRegPersonCountResult = getFatiDogReturnCode(mainType,subType)
                    .map(dataBuf -> {
                        logger.debug("subscribeRegPersonCountResult -> 当前进程ID：{},当前线程ID：{}",android.os.Process.myPid(),Thread.currentThread().getId());
                        int returnCode = getInt(new byte[]{dataBuf.get(BIT_RETURN_CODE_LOW),dataBuf.get(BIT_RETURN_CODE_HIGH)});
                        int personCount = getInt(new byte[]{dataBuf.get(BIT_DATA_BASE+0),
                                dataBuf.get(BIT_DATA_BASE+1),
                                dataBuf.get(BIT_DATA_BASE+2),
                                dataBuf.get(BIT_DATA_BASE+3)});

                        FaceRecoRegPersonCountBean result = new FaceRecoRegPersonCountBean(returnCode,personCount);
                        return result;
                    });
        }
        return subscribeRegPersonCountResult;
    }
    //为某个已经注册的人员添加人脸
    public void registerFace(long personId){
        int mainType = 0x01;
        int subType = 0x0111;
        byte[] cmd = new byte[23];
//        Arrays.fill(cmd, (byte)0); //清0

        cmd[BIT_FRAME_HEADER] = (byte)0xFB;
        cmd[BIT_LENGTH_LOW] = (byte)0x17;
        cmd[BIT_LENGTH_HIGH] = (byte)0x00;
        cmd[BIT_LENGTH_HIGH_X] = (byte)0x00;
        cmd[BIT_LENGTH_HIGH_XX] = (byte)0x00;
        cmd[BIT_MSG_MAIN_TYPE] = (byte)mainType;
        cmd[BIT_MSG_SUB_TYPE_LOW] = (byte)(subType&0x00FF);
        cmd[BIT_MSG_SUB_TYPE_HIGH] = (byte)((subType&0xFF00)>>8);
        cmd[BIT_RETURN_CODE_LOW] = (byte)0x00;
        cmd[BIT_RETURN_CODE_HIGH] = (byte)0x00;

        //将人员信息复制到指定字节
        cmd[BIT_DATA_BASE+0] = (byte)(personId&0x00FF);
        cmd[BIT_DATA_BASE+1] = (byte)((personId>>8)&0x00FF);
        cmd[BIT_DATA_BASE+2] = (byte)((personId>>16)&0x00FF);
        cmd[BIT_DATA_BASE+3] = (byte)((personId>>24)&0x00FF);
        cmd[BIT_DATA_BASE+4] = (byte)((personId>>32)&0x00FF);
        cmd[BIT_DATA_BASE+5] = (byte)((personId>>40)&0x00FF);
        cmd[BIT_DATA_BASE+6] = (byte)((personId>>48)&0x00FF);
        cmd[BIT_DATA_BASE+7] = (byte)((personId>>56)&0x00FF);

        //其他的属性暂时先不加
        FatiDogStream.getInstance().send(cmd,false);
    }

    //获取注册人脸结果
    public Observable<FaceRecoRegFaceInfoBean> subscribeRegisterFaceResult(){
        int mainType = 0x12;
        int subType = 0x0111;
        if (subscribeRegFaceResult == null) {
            subscribeRegFaceResult = getFatiDogReturnCode(mainType,subType)
                    .map(dataBuf -> {
                        logger.debug("subscribeRegisterFaceResult -> 当前进程ID：{},当前线程ID：{}",android.os.Process.myPid(),Thread.currentThread().getId());
                        int returnCode = 0;
                        if(dataBuf.array().length > 14){
                            returnCode = getInt(new byte[]{dataBuf.get(BIT_RETURN_CODE_LOW),dataBuf.get(BIT_RETURN_CODE_HIGH)});
                        }

                        if(dataBuf.array().length == 27){
                            FaceRecoRegFaceInfoBean result = new FaceRecoRegFaceInfoBean();
                            int state = dataBuf.get(BIT_DATA_BASE+0);
                            int faceCount = dataBuf.get(BIT_DATA_BASE+1);
                            int oppLeft = getInt(new byte[]{dataBuf.get(BIT_DATA_BASE+2),dataBuf.get(BIT_DATA_BASE+3)});
                            int oppTop = getInt(new byte[]{dataBuf.get(BIT_DATA_BASE+4),dataBuf.get(BIT_DATA_BASE+5)});
                            int oppRight = getInt(new byte[]{dataBuf.get(BIT_DATA_BASE+6),dataBuf.get(BIT_DATA_BASE+7)});
                            int oppBottom = getInt(new byte[]{dataBuf.get(BIT_DATA_BASE+8),dataBuf.get(BIT_DATA_BASE+9)});
                            int angle = dataBuf.get(BIT_DATA_BASE+10);
                            int light = dataBuf.get(BIT_DATA_BASE+11);

                            result.setReturnCode(returnCode);
                            result.setState(state);
                            result.setFaceCount(faceCount);
                            result.setOppLeft(oppLeft);
                            result.setOppTop(oppTop);
                            result.setOppRight(oppRight);
                            result.setOppBottom(oppBottom);
                            result.setAngle(angle);
                            result.setLight(light);

                            return result;
                        }else{
                            logger.error("注册人脸结果长度异常！{}",String.copyValueOf(Hex.encodeHex(dataBuf.array())).toUpperCase());
                            FaceRecoRegFaceInfoBean result = new FaceRecoRegFaceInfoBean();
                            result.setReturnCode(returnCode);
                            return result;
                        }
                    });
        }
        return subscribeRegFaceResult;
    }

    //清空人脸
    public void cleanFace(long personId){
        int mainType = 0x01;
        int subType = 0x0112;
        byte[] cmd = new byte[23];
//        Arrays.fill(cmd, (byte)0); //清0

        cmd[BIT_FRAME_HEADER] = (byte)0xFB;
        cmd[BIT_LENGTH_LOW] = (byte)0x17;
        cmd[BIT_LENGTH_HIGH] = (byte)0x00;
        cmd[BIT_LENGTH_HIGH_X] = (byte)0x00;
        cmd[BIT_LENGTH_HIGH_XX] = (byte)0x00;
        cmd[BIT_MSG_MAIN_TYPE] = (byte)mainType;
        cmd[BIT_MSG_SUB_TYPE_LOW] = (byte)(subType&0x00FF);
        cmd[BIT_MSG_SUB_TYPE_HIGH] = (byte)((subType&0xFF00)>>8);
        cmd[BIT_RETURN_CODE_LOW] = (byte)0x00;
        cmd[BIT_RETURN_CODE_HIGH] = (byte)0x00;

        //将人员信息复制到指定字节
        cmd[BIT_DATA_BASE+0] = (byte)(personId&0x00FF);
        cmd[BIT_DATA_BASE+1] = (byte)((personId>>8)&0x00FF);
        cmd[BIT_DATA_BASE+2] = (byte)((personId>>16)&0x00FF);
        cmd[BIT_DATA_BASE+3] = (byte)((personId>>24)&0x00FF);
        cmd[BIT_DATA_BASE+4] = (byte)((personId>>32)&0x00FF);
        cmd[BIT_DATA_BASE+5] = (byte)((personId>>40)&0x00FF);
        cmd[BIT_DATA_BASE+6] = (byte)((personId>>48)&0x00FF);
        cmd[BIT_DATA_BASE+7] = (byte)((personId>>56)&0x00FF);

        //其他的属性暂时先不加
        FatiDogStream.getInstance().send(cmd,false);
    }
    //订阅 清空人脸结果
    public Observable<FatiDogResponseBean> subscribeCleanFaceResult(){
        int mainType = 0x12;
        int subType = 0x0112;

        return getSimpleReturnCode(mainType,subType);
    }

    //发送人脸识别命令
    public void faceRecognition(){
        int mainType = 0x01;
        int subType = 0x0131;

        sendSimpleCmd(mainType, subType);
    }

    //订阅 人脸识别结果 Observable<FaceRecoResultBean>
    public Observable<FaceRecoResultBean> subscribeFaceRecognitionResult(){
        int mainType = 0x12;
        int subType = 0x0131;
        if (subFaceRecogResult == null)
        {
            subFaceRecogResult = getFatiDogReturnCode(mainType,subType)
                    .map(dataBuf -> {
                        logger.debug("subscribeRegisterFaceResult -> 当前进程ID：{},当前线程ID：{}",android.os.Process.myPid(),Thread.currentThread().getId());
                        if(dataBuf.array().length == 43){
                            FaceRecoResultBean result = new FaceRecoResultBean();

                            int returnCode = getInt(new byte[]{dataBuf.get(BIT_RETURN_CODE_LOW),dataBuf.get(BIT_RETURN_CODE_HIGH)});

                            int state = dataBuf.get(BIT_DATA_BASE+0);
                            long recoId = getLong(new byte[]{dataBuf.get(BIT_DATA_BASE+1),
                                    dataBuf.get(BIT_DATA_BASE+2),
                                    dataBuf.get(BIT_DATA_BASE+3),
                                    dataBuf.get(BIT_DATA_BASE+4),
                                    dataBuf.get(BIT_DATA_BASE+5),
                                    dataBuf.get(BIT_DATA_BASE+6),
                                    dataBuf.get(BIT_DATA_BASE+7),
                                    dataBuf.get(BIT_DATA_BASE+8)});

                            long id = getLong(new byte[]{dataBuf.get(BIT_DATA_BASE+9),
                                    dataBuf.get(BIT_DATA_BASE+10),
                                    dataBuf.get(BIT_DATA_BASE+11),
                                    dataBuf.get(BIT_DATA_BASE+12),
                                    dataBuf.get(BIT_DATA_BASE+13),
                                    dataBuf.get(BIT_DATA_BASE+14),
                                    dataBuf.get(BIT_DATA_BASE+15),
                                    dataBuf.get(BIT_DATA_BASE+16)});

                            int faceDataBase = BIT_DATA_BASE+17;
                            int faceCount = dataBuf.get(faceDataBase+0);
                            int oppLeft = getInt(new byte[]{dataBuf.get(faceDataBase+1),dataBuf.get(faceDataBase+2)});
                            int oppTop = getInt(new byte[]{dataBuf.get(faceDataBase+3),dataBuf.get(faceDataBase+4)});
                            int oppRight = getInt(new byte[]{dataBuf.get(faceDataBase+5),dataBuf.get(faceDataBase+6)});
                            int oppBottom = getInt(new byte[]{dataBuf.get(faceDataBase+7),dataBuf.get(faceDataBase+8)});
                            int angle = dataBuf.get(faceDataBase+9);
                            int light = dataBuf.get(faceDataBase+10);

                            result.setReturnCode(returnCode);
                            result.setState(state);
                            result.setRecoID(recoId);
                            result.setId(id);
                            result.setFaceCount(faceCount);
                            result.setOppLeft(oppLeft);
                            result.setOppTop(oppTop);
                            result.setOppRight(oppRight);
                            result.setOppBottom(oppBottom);
                            result.setAngle(angle);
                            result.setLight(light);

                            return result;
                        }else{
                            logger.error("人脸识别结果长度异常！{}",String.copyValueOf(Hex.encodeHex(dataBuf.array())).toUpperCase());
                            FaceRecoResultBean result = new FaceRecoResultBean();
                            return result;
                        }

                    });//.publish().autoConnect();
        }
        return subFaceRecogResult;
    }
    /*************************************************************************************************************************************************************************************************/
    //15个字节长度的简单短命令
    private void sendSimpleCmd(int msgType, int cmdType,int parity){
        byte[] cmd = {(byte)0xFB,(byte)0x0F,(byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0x00, (byte)0x00, (byte)0xFF};

        cmd[BIT_FRAME_HEADER] = (byte)0xFB;
        cmd[BIT_LENGTH_LOW] = (byte)0x0F;
        cmd[BIT_LENGTH_HIGH] = (byte)0x00;
        cmd[BIT_MSG_MAIN_TYPE] = (byte)msgType;
        cmd[BIT_MSG_SUB_TYPE_LOW] = (byte)(cmdType&0x00FF);
        cmd[BIT_MSG_SUB_TYPE_HIGH] = (byte)((cmdType&0xFF00)>>8);
        cmd[BIT_RETURN_CODE_LOW] = (byte)0x00;
        cmd[BIT_RETURN_CODE_HIGH] = (byte)0x00;
        cmd[BIT_PARITY_DEFAULT] = (byte)parity;

        FatiDogStream.getInstance().send(cmd,true);
    }

    //15个字节长度的简单短命令
    private void sendSimpleCmd(int msgType, int cmdType){
        byte[] cmd = new byte[15];

        cmd[BIT_FRAME_HEADER] = (byte)0xFB;
        cmd[BIT_LENGTH_LOW] = (byte)0x0F;
        cmd[BIT_LENGTH_HIGH] = (byte)0x00;
        cmd[BIT_MSG_MAIN_TYPE] = (byte)msgType;
        cmd[BIT_MSG_SUB_TYPE_LOW] = (byte)(cmdType&0x00FF);
        cmd[BIT_MSG_SUB_TYPE_HIGH] = (byte)((cmdType&0xFF00)>>8);
        cmd[BIT_RETURN_CODE_LOW] = (byte)0x00;
        cmd[BIT_RETURN_CODE_HIGH] = (byte)0x00;
//        cmd[BIT_PARITY_DEFAULT] = (byte)0xFF;

        FatiDogStream.getInstance().send(cmd,false);
    }

    //订阅对应类型的命令的返回结果
    private Observable<FatiDogResponseBean> getSimpleReturnCode(int mainType, int subType){
            subscribeSimpleReturnCode = getFatiDogReturnCode(mainType,subType)
                    .map(dataBuf -> {
                        byte[] codeBytes = {dataBuf.get(BIT_RETURN_CODE_LOW),dataBuf.get(BIT_RETURN_CODE_HIGH)};
                        int returnCode = getInt(codeBytes);
                        FatiDogResponseBean result = new FatiDogResponseBean(mainType,subType,returnCode);
                        return result;
                    }).publish().autoConnect();
        return subscribeSimpleReturnCode;
    }

    //订阅对应类型的命令的返回结果
    private Observable<ByteBuffer> getFatiDogReturnCode(int mainType, int subType){
//        logger.info("getFatiDogReturnCode -> 当前进程ID：{},当前线程ID：{}",android.os.Process.myPid(),Thread.currentThread().getId());

            subscribeFatiDogReturnCode = FatiDogStream.getInstance().getRawData()
                    .filter(dataBuf -> {
//                        logger.info("订阅 mainType: 0x{}, 当前 mainType: 0x{}",Integer.toHexString(mainType),Integer.toHexString(dataBuf.get(BIT_MSG_MAIN_TYPE)));
                        return (dataBuf.get(BIT_MSG_MAIN_TYPE) == (byte)mainType);
                    })
                    .filter(dataBuf -> {
                        byte[] codeBytes = {dataBuf.get(BIT_MSG_SUB_TYPE_LOW),dataBuf.get(BIT_MSG_SUB_TYPE_HIGH)};
                        int cmdCode = getInt(codeBytes);
//                        logger.info("订阅 subType: 0x{}, 当前 subType: 0x{}",Integer.toHexString(subType),Integer.toHexString(cmdCode));
//                        logger.info("getFatiDogReturnCode -> subtype -> 当前进程ID：{},当前线程ID：{}",android.os.Process.myPid(),Thread.currentThread().getId());
                        return (subType == cmdCode);
                    }).publish().autoConnect();

        logger.info("subscribeFatiDogReturnCode:{}",subscribeFatiDogReturnCode);
        return subscribeFatiDogReturnCode;
    }



    //两个字节的数组转换为整型数据，byte0放低位，byte1放高位
    private int getInt(byte[] bytes) {
        int result = 0;
        if(bytes.length == 2){
            result = (int) ((0xff & bytes[0]) | (0xff00 & (bytes[1] << 8)));
        }else if(bytes.length == 4){
            result = (int) ((0xff & bytes[0]) | (0xff00 & (bytes[1] << 8)));
            result = (int) (result | (0xff0000 & (bytes[2] << 16)));
            result = (int) (result | (0xff000000 & (bytes[3] << 24)));
        }else{

        }
        return result;
    }

    //小端方式
    private long getLong(byte[] bb) {
        return ((((long) bb[ 0] & 0xff) << 0)
                | (((long) bb[ 1] & 0xff) << 8)
                | (((long) bb[ 2] & 0xff) << 16)
                | (((long) bb[ 3] & 0xff) << 24)
                | (((long) bb[ 4] & 0xff) << 32)
                | (((long) bb[ 5] & 0xff) << 40)
                | (((long) bb[ 6] & 0xff) << 48)
                | (((long) bb[ 7] & 0xff) << 56));
    }
    private void long2Byte(byte[] bb, long x) {
        bb[ 0] = (byte) (x >> 0);
        bb[ 1] = (byte) (x >> 8);
        bb[ 2] = (byte) (x >> 16);
        bb[ 3] = (byte) (x >> 24);
        bb[ 4] = (byte) (x >> 32);
        bb[ 5] = (byte) (x >> 40);
        bb[ 6] = (byte) (x >> 48);
        bb[ 7] = (byte) (x >> 56);
    }

    private void int2bytes(byte[] targets, int src) {
        targets[0] = (byte) (src & 0xff);// 最低位
        targets[1] = (byte) ((src >> 8) & 0xff);// 次低位
        targets[2] = (byte) ((src >> 16) & 0xff);// 次高位
        targets[3] = (byte) ((src >> 24) & 0xff);// 最高位,无符号右移。
    }
    private byte[] int2bytes(int res, int num) {
        byte[] targets;
        if(num == 2){
            targets = new byte[2];
            targets[0] = (byte) (res & 0xff);// 最低位
            targets[1] = (byte) ((res >> 8) & 0xff);// 次低位
        }else if(num ==4){
            targets = new byte[4];
            targets[0] = (byte) (res & 0xff);// 最低位
            targets[1] = (byte) ((res >> 8) & 0xff);// 次低位
            targets[2] = (byte) ((res >> 16) & 0xff);// 次高位
            targets[3] = (byte) ((res >> 24) & 0xff);// 最高位,无符号右移。
        }else{
            targets = new byte[4];
        }

        return targets;
    }

    //用字符串存储超长整型数据
    private String getLongString(byte[] bytes) {

        return String.copyValueOf(Hex.encodeHex(bytes));
    }
}
