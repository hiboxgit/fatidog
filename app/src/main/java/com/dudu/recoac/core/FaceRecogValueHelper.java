package com.dudu.recoac.core;

import android.content.Context;

import com.dudu.commonlib.CommonLib;
import com.dudu.commonlib.utils.file.SharedPreferencesUtil;
import com.dudu.recoac.bean.FaceRecogPersonData;
import com.dudu.recoac.bean.FaceRecogRunningData;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Author: Robert
 * Date:  2017-02-09
 * Copyright (c) 2016,dudu Co.,Ltd. All rights reserved.
 * Desc:
 */
public class FaceRecogValueHelper {
    private Logger logger = LoggerFactory.getLogger("FatiDog.app.recoac.FaceRecogValueHelper");

    public static final String KEY_IS_RECOAC_FIRST_START = "RECOAC_IS_FIRST_START";
    public static final String KEY_IS_RECOAC_DATA_RESET = "RECOAC_IS_DATA_RESET"; //人脸识别模块中的人员数据是否已经重置过，也就是全部清空过...
    public static final String KEY_IS_RECOAC_OPEN = "RECOAC_IS_OPEN";


    public static final String KEY_RECOAC_PERSON_INFO_BASE = "RECOAC_PERSON_INFO_BASE_";
    public static final String KEY_RECOAC_PERSON_INFO_ID = "RECOAC_PERSON_INFO_ID";
    public static final String KEY_RECOAC_PERSON_INFO_NAME = "RECOAC_PERSON_INFO_NAME";
    public static final String KEY_RECOAC_PERSON_INFO_CARD = "RECOAC_PERSON_INFO_CARD";
    public static final String KEY_RECOAC_PERSON_INFO_REG_TIME = "RECOAC_PERSON_INFO_REG_TIME";
    public static final String KEY_RECOAC_PERSON_INFO_HAS_REGISTERFACE = "RECOAC_PERSON_INFO_HAS_REGISTERFACE";


    public static final int WHITE_LIST_MAX = 3;
    public static final int TRUST_PERSON_BASE = 101; //目前只有三个人，也就是101,102,103

    private Context context = null;
    private FaceRecogRunningData runningData = null;
    private boolean isPersonReset = false;
    private boolean isAddingFace = false; //是否正在采集人脸
    private int addingFaceType = 0; //当前正在采集的人脸类型
    private String curDriver = "";
    private ArrayList<FaceRecogPersonData>  registeredFacePersons; //白名单ID当中已经注册了人脸人员列表.
    private static FaceRecogValueHelper instance = null;

    public FaceRecogValueHelper() {
        runningData = new FaceRecogRunningData();
        FaceRecogPersonData[] personList = new FaceRecogPersonData[WHITE_LIST_MAX];
        for(int i=0;i<WHITE_LIST_MAX;i++){
            personList[i] = new FaceRecogPersonData();
        }
        runningData.setWhiteListPersons(personList);

        registeredFacePersons = new ArrayList<>();

        this.context = CommonLib.getInstance().getContext();
    }

    public static FaceRecogValueHelper getInstance(){
        if(instance == null){
            instance = new FaceRecogValueHelper();
        }
        return instance;
    }
    public void initConfig(Context context){
        logger.info("初始化人脸识别参数开始");
        this.context = context;
        boolean isFirstStart = SharedPreferencesUtil.getBooleanValue(context,KEY_IS_RECOAC_FIRST_START,true);

        if(isFirstStart){ //首次启动需要初始化信息，并保存
            logger.info("人脸识别首次启动，初始化信息并保存...");
            SharedPreferencesUtil.putBooleanValue(context,KEY_IS_RECOAC_FIRST_START,false);
            SharedPreferencesUtil.putBooleanValue(context,KEY_IS_RECOAC_OPEN,false);
            SharedPreferencesUtil.putBooleanValue(context,KEY_IS_RECOAC_DATA_RESET,false);

            isPersonReset = false;
            runningData.setOpen(false);

            for(int i=0;i<WHITE_LIST_MAX;i++){
                runningData.getWhiteListPersons()[i].setPersonId(TRUST_PERSON_BASE+i); //101,102,103
                runningData.getWhiteListPersons()[i].setName("人员"+(i+1)); //人员1，人员2，人员3
                runningData.getWhiteListPersons()[i].setCard(String.valueOf(i+1)); // 1,2,3

                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
                String curTime = df.format(new Date());// new Date()为获取当前系统时间
                runningData.getWhiteListPersons()[i].setRegTime(curTime); //2014-10-31 12:34:56
                runningData.getWhiteListPersons()[i].setHasRegisteredFace(false);

                //保存人员信息
                saveRecoacPersonData(i,
                        runningData.getWhiteListPersons()[i].getPersonId(),
                        runningData.getWhiteListPersons()[i].getName(),
                        runningData.getWhiteListPersons()[i].getCard(),
                        runningData.getWhiteListPersons()[i].getRegTime(),
                        runningData.getWhiteListPersons()[i].isHasRegisteredFace());
            }

        }else{ //非首次
            logger.info("人脸识别非首次启动，查询数据库初始化信息...");
            isPersonReset = SharedPreferencesUtil.getBooleanValue(context,KEY_IS_RECOAC_DATA_RESET,false);
            boolean isOpen = SharedPreferencesUtil.getBooleanValue(context,KEY_IS_RECOAC_OPEN,false);
            runningData.setOpen(isOpen);

            for(int i=0;i<WHITE_LIST_MAX;i++){
                initRecoacPersonData(i);
            }
        }

        //更新当前已经注册了人脸的人员列表.
        updateRegFacedlist();
        logger.info("初始化人脸识别参数结束");
    }

    public FaceRecogPersonData initRecoacPersonData(int index){

        long personId = SharedPreferencesUtil.getLongValue(context,KEY_RECOAC_PERSON_INFO_BASE+KEY_RECOAC_PERSON_INFO_ID+index,100);
        String name = SharedPreferencesUtil.getStringValue(context,KEY_RECOAC_PERSON_INFO_BASE+KEY_RECOAC_PERSON_INFO_NAME+index,"人员0");
        String card = SharedPreferencesUtil.getStringValue(context,KEY_RECOAC_PERSON_INFO_BASE+KEY_RECOAC_PERSON_INFO_CARD+index,"123");
        String regTime = SharedPreferencesUtil.getStringValue(context,KEY_RECOAC_PERSON_INFO_BASE+KEY_RECOAC_PERSON_INFO_REG_TIME+index,"2017-02-10 00:00:00");
        Boolean hasRegisteredFace = SharedPreferencesUtil.getBooleanValue(context,KEY_RECOAC_PERSON_INFO_BASE+KEY_RECOAC_PERSON_INFO_HAS_REGISTERFACE+index,false);

        runningData.getWhiteListPersons()[index].setPersonId(personId);
        runningData.getWhiteListPersons()[index].setName(name);
        runningData.getWhiteListPersons()[index].setCard(card);
        runningData.getWhiteListPersons()[index].setRegTime(regTime);
        runningData.getWhiteListPersons()[index].setHasRegisteredFace(hasRegisteredFace);

        return runningData.getWhiteListPersons()[index];
    }
    public void saveRecoacPersonData(int index, long personId, String name, String card, String regTime, boolean hasRegisteredFace){
        SharedPreferencesUtil.putLongValue(context,KEY_RECOAC_PERSON_INFO_BASE+KEY_RECOAC_PERSON_INFO_ID+index,personId);
        SharedPreferencesUtil.putStringValue(context,KEY_RECOAC_PERSON_INFO_BASE+KEY_RECOAC_PERSON_INFO_NAME+index,name);
        SharedPreferencesUtil.putStringValue(context,KEY_RECOAC_PERSON_INFO_BASE+KEY_RECOAC_PERSON_INFO_CARD+index,card);
        SharedPreferencesUtil.putStringValue(context,KEY_RECOAC_PERSON_INFO_BASE+KEY_RECOAC_PERSON_INFO_REG_TIME+index,regTime);
        SharedPreferencesUtil.putBooleanValue(context,KEY_RECOAC_PERSON_INFO_BASE+KEY_RECOAC_PERSON_INFO_HAS_REGISTERFACE+index,hasRegisteredFace);
    }

    public boolean isFaceRecogOpened(){
        logger.error("isFaceRecogOpened - ");
        return runningData.isOpen();
    }

    public void setFaceRecogOpen(boolean isOpen){
        runningData.setOpen(isOpen);
        SharedPreferencesUtil.putBooleanValue(context,KEY_IS_RECOAC_OPEN,runningData.isOpen());
        logger.error("setFaceRecogOpen - isOpen:{}",isOpen);
    }
    public FaceRecogPersonData getRecoacPersonData(int index){
        return runningData.getWhiteListPersons()[index];
    }

    //设置人脸数据是否已经清空过...
    public void setDataResetValue(boolean isPersonCleaned){
        isPersonReset = isPersonCleaned;
        SharedPreferencesUtil.putBooleanValue(context,KEY_IS_RECOAC_DATA_RESET,isPersonReset);
    }

    //注册人员是否已经被重置过？
    public boolean isDataReset(){
        return isPersonReset;
    }

    public void setPersonId(int index,long personId){
        runningData.getWhiteListPersons()[index].setPersonId(personId);
        SharedPreferencesUtil.putLongValue(context,KEY_RECOAC_PERSON_INFO_BASE+KEY_RECOAC_PERSON_INFO_ID+index,personId);
    }
    public long getPersonId(int index){
        return runningData.getWhiteListPersons()[index].getPersonId();
    }

    public int getPersonIndex(long personId){
        for(int i=0;i<WHITE_LIST_MAX;i++){
            if(getPersonId(i) == personId){
                return i;
            }
        }
        logger.error("不存在这个personID");
        return 0;
    }

    public boolean isPersonRegisteredFace(int index){
        return runningData.getWhiteListPersons()[index].isHasRegisteredFace();
    }

    public void setPersonRegisterFace(int index, boolean isRegistered){
        runningData.getWhiteListPersons()[index].setHasRegisteredFace(isRegistered);
        SharedPreferencesUtil.putBooleanValue(context,KEY_RECOAC_PERSON_INFO_BASE+KEY_RECOAC_PERSON_INFO_HAS_REGISTERFACE+index,isPersonRegisteredFace(index));

        //更新已经注册过人脸的人员列表
        updateRegFacedlist();
    }

    //更新注册人脸人员的名字
    public void setRegPersonName(int index,String name){
        runningData.getWhiteListPersons()[index].setName(name);
        SharedPreferencesUtil.putStringValue(context,KEY_RECOAC_PERSON_INFO_BASE+KEY_RECOAC_PERSON_INFO_NAME+index,name);
    }

    public String getRegPersonName(int index){
        return runningData.getWhiteListPersons()[index].getName();
    }
    //----------
    public boolean isBelongToWhiteList(long personId){
        for(int i=0;i<WHITE_LIST_MAX;i++){
            if(getPersonId(i) == personId && isPersonRegisteredFace(i)){
                return true;
            }
        }
        return false;
    }

    public ArrayList<FaceRecogPersonData> getRegisteredFacePersonsCopy(){
        ArrayList<FaceRecogPersonData>  facedPersonsList = new ArrayList<>(registeredFacePersons);
        return facedPersonsList;
    }

    public ArrayList<FaceRecogPersonData> getRegisteredFacePersons(){
        return registeredFacePersons;
    }
    public void updateRegFacedlist(){
        registeredFacePersons.clear();
        for(int i=0;i<WHITE_LIST_MAX;i++){
            if(isPersonRegisteredFace(i)){
                registeredFacePersons.add(getRecoacPersonData(i));
            }
        }
    }

    //注册过人脸的人员列表是否为空
    public boolean isRegisteredFacePersonEmpty(){
        return registeredFacePersons.isEmpty();
    }

    //获取一个白名单列表中没有注册过人脸的序号 白名单已经注册满的话就返回-1
    public int getOneWhiteListPersonUnRegFace(){
        for(int i=0;i<WHITE_LIST_MAX;i++){
            if(!isPersonRegisteredFace(i)){
                return i;
            }
        }

        return -1;
    }

    public boolean isAddingFace() {
        return isAddingFace;
    }

    public void setAddingFace(boolean addingFace) {
        isAddingFace = addingFace;
    }

    public int getAddingFaceType() {
        return addingFaceType;
    }

    public void setAddingFaceType(int addingFaceType) {
        this.addingFaceType = addingFaceType;
    }

    public String getCurDriver() {
        return curDriver;
    }

    public void setCurDriver(String curDriver) {
        this.curDriver = curDriver;
    }
}
