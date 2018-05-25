package com.dudu.fatidog.core;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.dudu.fatidog.DebugActivity;
import com.dudu.fatidog.FatiDogBaseActivity;
import com.dudu.fatidog.R;
import com.dudu.fatidog.bean.FatiDogControlBean;
import com.dudu.fatidog.map.MapManager;
import com.dudu.fatidog.map.bean.SpeedChangeEvent;
import com.dudu.fatidog.service.FatiDogService;
import com.dudu.fatidog.util.CommonViewHolder;
import com.dudu.fatidog.util.IAsyncTaskCallBack;

import org.slf4j.LoggerFactory;

import de.greenrobot.event.EventBus;

/**
 * Author: Robert
 * Date:  2016-12-20
 * Copyright (c) 2016,dudu Co.,Ltd. All rights reserved.
 * Desc:
 */
public class FatiDogViewModel {

    private static org.slf4j.Logger logger = LoggerFactory.getLogger("FatiDog.app.FatiDogViewModel");
    private FatiDogBaseActivity mBaseActivity;
    private CommonViewHolder mBaseHolder;

    /*页面UI元素*/
    private Switch mFatiDogSwitch;
    private Switch mBootStartSwitch;
    private Switch mExperienceSwitch;

    private ImageView mFaceScanIv;
    private TextView mFaceScanTextTv;
    private TextView mFaceScanTipTv;

    private ImageView mConnectFlagIv;
    private TextView mConnectTipTv;

    public FatiDogViewModel(FatiDogBaseActivity mActivity) {
        this.mBaseActivity = mActivity;
    }

    public void initViewModel(CommonViewHolder viewHolder){
        mBaseHolder = viewHolder;
        FatiDogValueHelper.getInstance().setExperienceMode(false);

        initAllView();
        if(FatiDogValueHelper.getInstance().isDeviceConnected()){
            initAllListener();
        }else{
            logger.error("设备未连接不进行事件处理,主动拉起服务");
            Intent fatiDogServiceIntent = new Intent(mBaseActivity, FatiDogService.class);
            mBaseActivity.startService(fatiDogServiceIntent);
        }
        forTest();
        initAllData();
    }

    private void forTest(){
        //长按进入debug页面
        mConnectFlagIv.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                try{
                    mBaseActivity.startActivity(new Intent(mBaseActivity,DebugActivity.class));
                }catch (Exception e){
                    e.printStackTrace();
                }
                return true;
            }
        });
    }
    private void initAllView(){
        mFatiDogSwitch = (Switch) mBaseHolder.v(R.id.sw_fatidog);
        mBootStartSwitch = (Switch) mBaseHolder.v(R.id.sw_boot_start);
        mExperienceSwitch = (Switch) mBaseHolder.v(R.id.sw_experience_mode);
        mFaceScanIv = mBaseHolder.imgV(R.id.id_anim_face_scan);
        mFaceScanTextTv = mBaseHolder.tV(R.id.id_tv_face_scan_text);
        mFaceScanTipTv = mBaseHolder.tV(R.id.id_tv_face_scan_tip);
        mConnectFlagIv = mBaseHolder.imgV(R.id.id_iv_connect_flag);
        mConnectTipTv = mBaseHolder.tV(R.id.id_tv_connect_tip);
    }

    private void initAllData(){
        FatiDogControlBean controlData = FatiDogValueHelper.getInstance().getFatiDogControlData();
        mFatiDogSwitch.setChecked(controlData.isOpen());
        mBootStartSwitch.setChecked(controlData.isBootStart());
        mExperienceSwitch.setChecked(controlData.isExperienceMode());

        updateDeviceOnlineStatus(controlData.isDeviceConnected());

        if(controlData.isFaceScaned()){

        }else{

        }
    }

    public void updateDeviceOnlineStatus(boolean isOnline){
        if(isOnline){
            mConnectFlagIv.setImageResource(R.drawable.connect_success);
            mConnectTipTv.setText(R.string.device_connect_ok_tip);
        }else{
            mConnectFlagIv.setImageResource(R.drawable.connect_fail);
            mConnectTipTv.setText(R.string.device_connect_fail_tip);
        }
    }

    private void initAllListener(){
        mFatiDogSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                logger.debug("点击了...疲劳预警开关...{}",b);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        FatiDogValueHelper.getInstance().setOpen(b);

                        if(b){
                            FatiDogWorkFlow.getInstance().startMonitor(new IAsyncTaskCallBack() {
                                @Override
                                public void onComplete() {
                                    boolean isExMode = FatiDogValueHelper.getInstance().isExperienceMode();
                                    if(!isExMode){
                                        EventBus.getDefault().post(new SpeedChangeEvent(MapManager.getInstance().getSpeed()));
                                    }
                                }

                                @Override
                                public void onError() {

                                }
                            });
                        }else{
                            FatiDogWorkFlow.getInstance().stopMonitor(null);
                        }
                    }
                }).start();

            }
        });

        mBootStartSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                logger.debug("点击了...开机启动开关...{}",b);
                FatiDogControlBean controlData = FatiDogValueHelper.getInstance().getFatiDogControlData();
                controlData.setBootStart(b);
                FatiDogValueHelper.getInstance().setFatiDogControlData(controlData);
            }
        });

        mExperienceSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                logger.debug("点击了...体验模式开关...{}",b);
                //体验模式仅在当前页面有效，退出即失效，无需保存.
                FatiDogValueHelper.getInstance().setExperienceMode(b);
            }
        });

        mFaceScanTextTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try{
                    if(mFaceScanIv.getDrawable() instanceof  AnimationDrawable){
                        //开始or停止刷脸动画
                        AnimationDrawable animationDrawable = (AnimationDrawable) mFaceScanIv.getDrawable();
                        if(animationDrawable != null && animationDrawable.isRunning()){
                            logger.error("当前已经正在刷脸！");
                        }else{
                            FatiDogWorkFlow.getInstance().startScanFace();
                        }
                    }else{
                        FatiDogWorkFlow.getInstance().startScanFace();
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });

    }
    public void scanFaceAnimStart(){
        try{
            mFaceScanTextTv.setVisibility(View.INVISIBLE); //刷脸动画过程中隐藏点击重新刷脸按钮

            mFaceScanIv.setImageResource(R.drawable.face_scan_animlist);
            AnimationDrawable animationDrawable = (AnimationDrawable) mFaceScanIv.getDrawable();
            animationDrawable.setOneShot(false);
            animationDrawable.start();
        }catch (Exception e){
            logger.error("开始刷脸动画失败：{}",e.toString());
        }

    }
    public void scanFaceAnimStop(){
        try{
            if(mFaceScanIv.getDrawable() instanceof AnimationDrawable){
                AnimationDrawable animationDrawable = (AnimationDrawable) mFaceScanIv.getDrawable();
                if(animationDrawable != null && animationDrawable.isRunning()) {
                    animationDrawable.stop();
                }
            }
        }catch (Exception e){
            logger.error("停止刷脸动画失败：{}",e.toString());
        }
    }

    public void resetFaceScanDefault(){
        mFaceScanIv.setImageResource(R.drawable.face_recognition_frame_one);
        mFaceScanTextTv.setVisibility(View.VISIBLE);

        if(FatiDogValueHelper.getInstance().isFaceScaned()){
            mFaceScanTextTv.setText(R.string.refresh_face);
        }else{
            mFaceScanTextTv.setText(R.string.click_to_face);
        }
    }
    public void switchFatiDog(boolean isOpen){
        mFatiDogSwitch.setChecked(isOpen);
    }

    public void showOrHideScanTip(boolean isShow){
        if(isShow){
            mFaceScanTipTv.setVisibility(View.VISIBLE);
        }else{
            mFaceScanTipTv.setVisibility(View.INVISIBLE);
        }
    }
}
