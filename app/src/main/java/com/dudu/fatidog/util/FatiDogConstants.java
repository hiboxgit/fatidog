package com.dudu.fatidog.util;

/**
 * Author: Robert
 * Date:  2016-12-31
 * Copyright (c) 2016,dudu Co.,Ltd. All rights reserved.
 * Desc:
 */
public class FatiDogConstants {
    public static final String TAG = "";
    public static final int FATIDOG_CMD_RESP_TIME_OUT = 5; //疲劳预警协议命令响应超时时间为3s
    public static final int FATIDOG_GET_POS_DELAY = 1000; //延时1000ms执行获取人脸位置信息命令
    public static final int FATIDOG_GET_POS_MAX_TIMES = 10; //刷脸最大次数

    //FatiDogFaceScanEvent 刷脸事件
    public static final int FATIDOG_EVENT_FACE_SCAN_SUCCESS = 1; //刷脸成功
    public static final int FATIDOG_EVENT_FACE_SCAN_FAIL = 2; //刷脸失败
    public static final int FATIDOG_EVENT_OPEN_FATIDOG_SUCCESS = 3; //开启疲劳预警成功

    //疲劳告警事件名称
    public static final String FATIDOG_WARNING_TYPE_NAME[] = {"正常","请小心驾驶","请正视前方","注意危险","疲劳驾驶"};
    //疲劳告警事件定义
    public static final int FATIDOG_WARNING_TYPE_NONE = 0; //正常
    public static final int FATIDOG_WARNING_TYPE_BE_CAREFUL = 1; //请小心驾驶
    public static final int FATIDOG_WARNING_TYPE_LOOK_FORWARD = 2; //请正视前方
    public static final int FATIDOG_WARNING_TYPE_DANGER = 3; //注意危险
    public static final int FATIDOG_WARNING_TYPE_TIRED = 4; //疲劳驾驶
    public static final int FATIDOG_WARNING_TYPE_INVALID_DRIVER = 5; //无效驾驶员
    public static final int FATIDOG_WARNING_TYPE_MULTILE_DISTRACTION = 6; //分散注意力

    //疲劳告警规则
    public static final int FATIDOG_WARNING_KEEP_ON_TIME = 3000; //同样的告警如果要显示，需要距离上次告警显示时间


    //非法人员驾驶车辆广播
    public static final String UNTRUST_WARN = "android.intent.action.fatidog.UNTRUSTPERSON";
    //抓拍路径
    public static final String UNVALIDE_DRIVER_SNAP_PATH = "SNAP_PATH";
    //震动广播
    public static final String VIBRATE_NOTIFY = "DeviceService.ACTION_SET_VIBRATE";
    //震感
    public static final String VIBRATE_FEEL = "DeviceService.VIBRATE_STRENGTH";
}
