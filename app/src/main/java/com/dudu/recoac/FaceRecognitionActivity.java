package com.dudu.recoac;


import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;

import com.dudu.commonlib.CommonLib;
import com.dudu.commonlib.share.constants.VoiceConstants;
import com.dudu.commonlib.share.contentprovider.voice.VoiceHelper;
import com.dudu.commonlib.ui.dialog.SimpleDialog;
import com.dudu.fatidog.FatiDogBaseActivity;
import com.dudu.fatidog.R;
import com.dudu.fatidog.bean.FatidogExitEvent;
import com.dudu.fatidog.bean.VoiceControlEvent;
import com.dudu.fatidog.core.FatiDogValueHelper;
import com.dudu.recoac.bean.FaceSwitchPageEvent;
import com.dudu.recoac.core.FaceRecogValueHelper;
import com.dudu.recoac.frament.HumanFaceFragment;
import com.dudu.recoac.frament.HumanFaceListFragment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

public class FaceRecognitionActivity extends FatiDogBaseActivity {
    private Logger logger = LoggerFactory.getLogger("FatiDog.app.recoac.FaceRecognitionActivity");
    private ViewPager mViewPager;
    private ImageView iv_anchor1, iv_anchor2;
    private Switch mSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        logger.info("onCreate");
    }

    @Override
    public void initView(Bundle savedInstanceState) {
        logger.info("initView");
        mViewPager = (ViewPager) findViewById(R.id.guide_vp);
        iv_anchor1 = (ImageView) findViewById(R.id.iv_anchor1);
        iv_anchor2 = (ImageView) findViewById(R.id.iv_anchor2);
        mSwitch = (Switch) findViewById(R.id.face_switch);
        initPageView();
    }


    @Override
    public void initData() {
        boolean isOpen = FaceRecogValueHelper.getInstance().isFaceRecogOpened();
        mSwitch.setChecked(isOpen);
        logger.info("initData - isOpen：{}",isOpen);

        boolean isDevConnected = FatiDogValueHelper.getInstance().isDeviceConnected();//是否已连接
        if(!isDevConnected){
            VoiceHelper.getInstance().startSpeaking(
                    CommonLib.getInstance().getContext().getString(R.string.face_recog_connected), VoiceConstants.TTS_DONOTHING, false);
            showUnValidDialog();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        logger.info("onNewIntent");
        initData();
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
        initEvent();
    }

    @Override
    protected void onResume() {
        super.onResume();
        logger.info("onResume");
        EventBus.getDefault().unregister(this);
        EventBus.getDefault().register(this);
    }

    @Override
    public int getLayoutResId() {
        return R.layout.activity_face_recognition;
    }

    @Override
    protected void onPause() {
        super.onPause();
        logger.info("onPause");
        EventBus.getDefault().unregister(this);
    }

    private void initEvent(){
        logger.info("初始化人脸识别页面事件处理。。。");
        mSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){
                    if(FaceRecogValueHelper.getInstance().isRegisteredFacePersonEmpty()){
                        VoiceHelper.getInstance().startSpeaking(
                                CommonLib.getInstance().getContext().getString(R.string.unregister_face), VoiceConstants.TTS_DONOTHING, false);
                    }
                    //已经有人脸被注册过
                    FaceRecogValueHelper.getInstance().setFaceRecogOpen(true);
                }else{
                    FaceRecogValueHelper.getInstance().setFaceRecogOpen(false);
                }


                if(b){
                    logger.debug("打开人脸识别");
                }else{
                    logger.debug("关闭人脸识别");
                }
            }
        });
    }

    private void initPageView() {
        mViewPager.setAdapter(new FaceRecognitionAdapter(getSupportFragmentManager()));
        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageSelected(int arg0) {
                onAnchorChangeIndex(arg0);
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {
            }

            @Override
            public void onPageScrollStateChanged(int arg0) {
            }
        });

        mViewPager.setCurrentItem(0);
    }

    private void onAnchorChangeIndex(int index) {
        switch (index) {
            case 0:
                iv_anchor1.setImageResource(R.drawable.dot_white);
                iv_anchor2.setImageResource(R.drawable.dot_black);
                break;
            case 1:
                iv_anchor1.setImageResource(R.drawable.dot_black);
                iv_anchor2.setImageResource(R.drawable.dot_white);
                break;
            default:
                break;
        }
    }

    private class FaceRecognitionAdapter extends FragmentPagerAdapter {

        private List<Fragment> fragments = new ArrayList<>();

        public FaceRecognitionAdapter(FragmentManager fm) {
            super(fm);
            fragments.add(new HumanFaceFragment());
            fragments.add(new HumanFaceListFragment());
        }

        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }
    }

    public void showUnValidDialog() {
        logger.info("弹框提示当前不可用！");
        try{
            SimpleDialog tipDialog = new SimpleDialog(this);
            tipDialog.setText(CommonLib.getInstance().getContext().getString(R.string.face_recog_connected))
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

    public void onEventMainThread(FaceSwitchPageEvent event){
        logger.info("接收到切换页面消息！：{}",event.toString());
        int pageIndex = event.getPageIndex();
        try{
            mViewPager.setCurrentItem(pageIndex);
        }catch (Exception e){
            logger.error("切换页面失败：{}",e.toString());
        }

    }

    public void onEventMainThread(VoiceControlEvent event) {
        logger.info("接到退出当前人脸识别页面消息");
        if(event.getType() == VoiceControlEvent.VoiceControlType.EXIT_FATIDOG){
            this.finish();
        }
    }

    public void onEventMainThread(FatidogExitEvent event) {
        logger.info("接到退出当前人脸识别页面消息");
        this.finish();
    }
}
