package com.dudu.recoac.frament;

import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.dudu.commonlib.CommonLib;
import com.dudu.commonlib.share.constants.VoiceConstants;
import com.dudu.commonlib.share.contentprovider.voice.VoiceHelper;
import com.dudu.fatidog.R;
import com.dudu.fatidog.util.DialogUtils;
import com.dudu.recoac.bean.FaceAddEvent;
import com.dudu.recoac.bean.FaceAddResultEvent;
import com.dudu.recoac.bean.FaceRecogAddFaceEvent;
import com.dudu.recoac.bean.FaceRecogAddFaceStartEvent;
import com.dudu.recoac.bean.FaceSwitchPageEvent;
import com.dudu.recoac.core.FaceRecogValueHelper;
import com.dudu.recoac.core.FaceRecogWorkFlow;
import com.dudu.recoac.utils.view.ScanView;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.greenrobot.event.EventBus;

/**
 * @author luo zha
 * @CreateDate 2017-02-05 17:07.
 */
public class HumanFaceFragment extends BaseFragment implements View.OnClickListener {
    private Logger logger = LoggerFactory.getLogger("FatiDog.app.recoac.HumanFaceFragment");
    private FrameLayout mAddFaceContainer;
    private RelativeLayout mScanFaceContainer;
    private TextView mAddFaceTV;
    private ScanView scanView;
    private TextView mAddFaceTip;

    private int unFacedPersonIndex;
    private int faceType;
    @Override
    protected int initLayoutId() {
        return R.layout.fragment_human_face;
    }

    @Override
    protected void initView(View rootView) {
        mAddFaceContainer = (FrameLayout) rootView.findViewById(R.id.add_human_face_container);
        mScanFaceContainer = (RelativeLayout) rootView.findViewById(R.id.scan_human_face_container);
        mAddFaceTV = (TextView) rootView.findViewById(R.id.add_human_face_tv);
        scanView = (ScanView) rootView.findViewById(R.id.face_scan_view);
        mAddFaceTip = (TextView) rootView.findViewById(R.id.face_camera_tv);
    }

    @Override
    protected void initData() {

    }

    @Override
    protected void initListener() {
        mAddFaceTV.setOnClickListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        logger.debug("onResume");
        updateView();
        EventBus.getDefault().unregister(this);
        EventBus.getDefault().register(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        logger.debug("onPause");
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.add_human_face_tv:
//                if(!FatiDogValueHelper.getInstance().isOpen()){
//                    VoiceHelper.getInstance().startSpeaking(
//                            CommonLib.getInstance().getContext().getString(R.string.reg_face_need_open_fatidog), VoiceConstants.TTS_DONOTHING, false);
//                    return;
//                }
//                int unFacedIndex = FaceRecogValueHelper.getInstance().getOneWhiteListPersonUnRegFace();
//                if(unFacedIndex == -1){
//                    VoiceHelper.getInstance().startSpeaking(
//                            CommonLib.getInstance().getContext().getString(R.string.reg_person_is_full), VoiceConstants.TTS_DONOTHING, false);
//                    EventBus.getDefault().post(new FaceSwitchPageEvent(1));
//                    return;
//                }
//                switch2ScanState();
//                FaceRecogValueHelper.getInstance().setAddingFace(true);
//
//                startAddRightFace();

                FaceRecogWorkFlow.getInstance().startRegFace();
                break;
            default:
                break;
        }
    }

    private void updateView(){
        logger.info("刷脸人脸识别添加人脸界面！");
        if(FaceRecogValueHelper.getInstance().isAddingFace()){
            switch2ScanState();
        }else{
            switch2UnScanState();
        }
    }

    private void switch2ScanState(){
        logger.info("切回初始未进行添加人脸界面");
        if(scanView != null){
            mAddFaceContainer.setVisibility(View.INVISIBLE);
            mScanFaceContainer.setVisibility(View.VISIBLE);
            scanView.startScan();
        }
    }

    private void switch2UnScanState(){
        logger.info("切到添加人脸进行中界面");
        if(scanView != null){
            scanView.stopScan();
            mScanFaceContainer.setVisibility(View.INVISIBLE);
            mAddFaceContainer.setVisibility(View.VISIBLE);
        }
    }

    public void onEventMainThread(FaceRecogAddFaceStartEvent event){
        logger.info("接收到开始注册人脸消息通知...");
        if(event.isStart()){
            switch2ScanState();
        }
    }

    public void onEventMainThread(FaceRecogAddFaceEvent event){
        logger.info("接收到更新采集人脸提示消息通知:{}",event.toString());
        if(event.getType() == 1){
            mAddFaceTip.setText(R.string.please_right_to_camera);
        }else if(event.getType() == 2){
            mAddFaceTip.setText(R.string.please_side_to_camera);
        }
    }

    public void onEventMainThread(FaceAddResultEvent event) {
        logger.info("接收到更新采集人脸结果消息通知:{}", event.toString());
        if(event.isOk()){ //正侧脸已经全部采集完毕
            //先切到人员列表页面
            EventBus.getDefault().post(new FaceSwitchPageEvent(1));

            switch2UnScanState();
            DialogUtils.showTipToast(R.string.setting_ok_has_saved_face);
        }else{
            if(event.getType() == 1){
                DialogUtils.showTipToast(R.string.face_right_register_fail);
            }else if(event.getType() == 2){
                DialogUtils.showTipToast(R.string.face_side_register_fail);
            }
            switch2UnScanState();
        }
    }

    //-------------------------------------------------------------------------------------------------
    private void doAddFace(){
        logger.debug("开始添加人脸...");
        FaceRecogWorkFlow.getInstance().startAddFaceForPerson(unFacedPersonIndex,faceType, null);
    }

    private void startAddRightFace(){
        logger.debug("开始采集正脸");
        unFacedPersonIndex = FaceRecogValueHelper.getInstance().getOneWhiteListPersonUnRegFace();
        faceType = 1;
        mAddFaceTip.setText(R.string.please_right_to_camera);

        VoiceHelper.getInstance().startSpeaking(
                CommonLib.getInstance().getContext().getString(R.string.please_right_face_to_camera), VoiceConstants.TTS_DONOTHING, false);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    Thread.sleep(3000);
                }catch (Exception e){
                    logger.error("{}",e.toString());
                }
                doAddFace();
            }
        }).start();
    }
    private void startAddSideFace(){
        logger.debug("开始采集侧脸");
        faceType++;
        mAddFaceTip.setText(R.string.please_side_to_camera);

        VoiceHelper.getInstance().startSpeaking(
                CommonLib.getInstance().getContext().getString(R.string.please_drive_face_to_camera), VoiceConstants.TTS_DONOTHING, false);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    Thread.sleep(4000);
                }catch (Exception e){
                    logger.error("{}",e.toString());
                }
                doAddFace();
            }
        }).start();
    }


    public void onEventMainThread(FaceAddEvent event) {

        logger.info("接到采集人脸事件 :{}",event.toString());
        if(event.isSuccess()){
            logger.info("采集人脸成功");

            if(event.getFaceType() == 1){
//                DialogUtils.showTipToast(R.string.face_right_register_success);
                VoiceHelper.getInstance().startSpeaking(
                        CommonLib.getInstance().getContext().getString(R.string.right_face_ok), VoiceConstants.TTS_DONOTHING, false);

                startAddSideFace();
            }else {
//                DialogUtils.showTipToast(R.string.face_side_register_success);
                VoiceHelper.getInstance().startSpeaking(
                        CommonLib.getInstance().getContext().getString(R.string.side_face_ok), VoiceConstants.TTS_DONOTHING, false);

                //先切到人员列表页面
                EventBus.getDefault().post(new FaceSwitchPageEvent(1));
                //再重置注册人脸页面
                faceType = 0;
                switch2UnScanState();
                DialogUtils.showTipToast(R.string.setting_ok_has_saved_face);
            }

        }else{
            logger.error("采集人脸失败！");

            if(event.getFaceType() == 1){
                DialogUtils.showTipToast(R.string.face_right_register_fail);
                VoiceHelper.getInstance().startSpeaking(
                        CommonLib.getInstance().getContext().getString(R.string.face_right_register_fail), VoiceConstants.TTS_DONOTHING, false);
                faceType = 0; //使得下次开始自动采集正脸
            }else {
                DialogUtils.showTipToast(R.string.face_side_register_fail);
                VoiceHelper.getInstance().startSpeaking(
                        CommonLib.getInstance().getContext().getString(R.string.face_side_register_fail), VoiceConstants.TTS_DONOTHING, false);

                faceType = 1;
            }

            switch2UnScanState(); //使得下次开始自动采集侧脸
        }
    }
}
