package com.dudu.fatidog;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.dudu.commonlib.CommonLib;
import com.dudu.commonlib.share.constants.VoiceConstants;
import com.dudu.commonlib.share.contentprovider.voice.VoiceHelper;
import com.dudu.commonlib.ui.dialog.SimpleDialog;
import com.dudu.fatidog.bean.FatiDogExistStatusEvent;
import com.dudu.fatidog.bean.FatiDogFaceScanEvent;
import com.dudu.fatidog.bean.FatiDogUpdateViewEvent;
import com.dudu.fatidog.bean.FatidogExitEvent;
import com.dudu.fatidog.bean.VoiceControlEvent;
import com.dudu.fatidog.core.FatiDogValueHelper;
import com.dudu.fatidog.core.FatiDogViewModel;
import com.dudu.fatidog.core.FatiDogWorkFlow;
import com.dudu.fatidog.util.DialogUtils;
import com.dudu.fatidog.util.FatiDogConstants;

import org.slf4j.LoggerFactory;

import de.greenrobot.event.EventBus;

public class FatiDogMainActivity extends FatiDogBaseActivity {

    private org.slf4j.Logger logger = LoggerFactory.getLogger("FatiDog.app.FatiDogMainActivity");
    private FatiDogViewModel mViewModel;

    private static final int MSG_ID_DO_FIRST_START = 1; //首次进入，自动执行刷脸

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch(msg.what){
                case MSG_ID_DO_FIRST_START:
                {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            FatiDogWorkFlow.getInstance().doAtFirstIn();
                        }
                    }).start();
                }
                    break;
                default:
                    break;
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        logger.info("onCreate...");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        logger.info("onDestroy...");
        if(mViewModel != null){
            mViewModel.scanFaceAnimStop();
        }
        FatiDogValueHelper.getInstance().setExperienceMode(false); //退出体验模式
    }

    @Override
    public int getLayoutResId() {
        return R.layout.activity_fati_dog_main;
    }

    @Override
    public void initView(Bundle savedInstanceState) {

    }

    @Override
    public void initData() { //第一次加载onResume的时候调用
        mViewModel = new FatiDogViewModel(this);
        mViewModel.initViewModel(mViewHolder);

        boolean isDevConnected = FatiDogValueHelper.getInstance().isDeviceConnected();//是否已连接
        if(!isDevConnected){
            VoiceHelper.getInstance().startSpeaking(
                    CommonLib.getInstance().getContext().getString(R.string.fatidog_not_connected), VoiceConstants.TTS_DONOTHING, false);
            showUnValidDialog();
        }
    }

    @Override
    public boolean hasTitleBar() {
        return false;
    }

    @Override
    public boolean hasBackBtn() {
        return false;
    }

    @Override
    public void initListener() {

    }

    @Override
    protected void onResume() {
        super.onResume();
        mViewModel.showOrHideScanTip(false); //隐藏刷脸提示语
        mViewModel.resetFaceScanDefault(); //重置刷脸默认界面

        EventBus.getDefault().unregister(this);
        EventBus.getDefault().register(this);
        mHandler.sendEmptyMessageDelayed(MSG_ID_DO_FIRST_START, 1500);
    }

    @Override
    protected void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
        FatiDogWorkFlow.getInstance().stopScanFace();
        if(mViewModel != null){
            mViewModel.scanFaceAnimStop();
        }
        FatiDogValueHelper.getInstance().setExperienceMode(false); //退出体验模式
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        logger.debug("onNewIntent");
        initData();
    }

    public void onEventMainThread(FatiDogFaceScanEvent event) {
        logger.info("接到刷脸事件：EventType :{}",event.getEventType());
        if(event.getEventType() == FatiDogConstants.FATIDOG_EVENT_FACE_SCAN_SUCCESS){
            DialogUtils.showTipToast(R.string.face_scan_ok);
            if(mViewModel != null){
                mViewModel.scanFaceAnimStop(); //停止刷脸动画
                mViewModel.showOrHideScanTip(false); //隐藏刷脸提示语
                mViewModel.resetFaceScanDefault(); //重置刷脸动画

                mViewModel.switchFatiDog(true);
            }
        }else if(event.getEventType() == FatiDogConstants.FATIDOG_EVENT_FACE_SCAN_FAIL){
            DialogUtils.showTipToast(R.string.face_scan_fail);
            if(mViewModel != null){
                mViewModel.scanFaceAnimStop(); //停止刷脸动画
                mViewModel.showOrHideScanTip(false); //隐藏刷脸提示语
                mViewModel.resetFaceScanDefault(); //重置刷脸动画
            }
        }
    }

    public void showUnValidDialog() {
        logger.info("弹框提示当前不可用！");
        try{
            SimpleDialog tipDialog = new SimpleDialog(this);
            tipDialog.setText(CommonLib.getInstance().getContext().getString(R.string.fatidog_not_connected))
                    .setLeftButton(R.string.click_sure, new SimpleDialog.OnClickListener() {
                        @Override
                        public void onClick(Dialog dialog) {
                            EventBus.getDefault().post(new FatidogExitEvent(true));
                        }
                    })
                    .show();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void onEventMainThread(FatiDogUpdateViewEvent event) {
        logger.info("接到开始播放刷脸动画事件：EventType :{}", event.getEventType());
        if(event.getEventType() == FatiDogConstants.FATIDOG_EVENT_OPEN_FATIDOG_SUCCESS){
            if(mViewModel != null){
                mViewModel.scanFaceAnimStart(); //开启刷脸动画
                mViewModel.showOrHideScanTip(true); //显示刷脸提示语
            }

        }
    }

    public void onEventMainThread(VoiceControlEvent event) {
        logger.info("接到退出当前疲劳预警页面消息");
        if(event.getType() == VoiceControlEvent.VoiceControlType.EXIT_FATIDOG){
            this.finish();
        }
    }

    public void onEventMainThread(FatiDogExistStatusEvent event) {
        logger.info("接到更新设备在线状态消息");
        mViewModel.updateDeviceOnlineStatus(event.isOnline());
    }

    public void onEventMainThread(FatidogExitEvent event) {
        logger.info("接到退出当前防疲劳页面消息");
        this.finish();
    }
}
