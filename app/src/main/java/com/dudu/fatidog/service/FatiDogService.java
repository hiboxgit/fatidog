package com.dudu.fatidog.service;

import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import com.dudu.commonlib.CommonLib;
import com.dudu.commonlib.share.car.CarStatusUtils;
import com.dudu.commonlib.share.constants.BroadcastConstants;
import com.dudu.commonlib.share.constants.VoiceConstants;
import com.dudu.commonlib.share.contentprovider.fire.FireHelper;
import com.dudu.commonlib.share.contentprovider.share.ShareHelper;
import com.dudu.commonlib.share.contentprovider.voice.VoiceHelper;
import com.dudu.fatidog.FatiDogMainActivity;
import com.dudu.fatidog.R;
import com.dudu.fatidog.bean.FatiDogWarningEvent;
import com.dudu.fatidog.bean.RemoteControlEvent;
import com.dudu.fatidog.bean.SwitchServiceEvent;
import com.dudu.fatidog.core.CarFireManager;
import com.dudu.fatidog.core.FatiDogValueHelper;
import com.dudu.fatidog.core.FatiDogWorkFlow;
import com.dudu.fatidog.map.bean.SpeedChangeEvent;
import com.dudu.fatidog.util.FatiDogConstants;
import com.dudu.fatidog.util.SoundPlayUtils;
import com.dudu.recoac.FaceRecognitionActivity;
import com.dudu.recoac.core.FaceRecogValueHelper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.greenrobot.event.EventBus;
import rx.schedulers.Schedulers;

public class FatiDogService extends Service implements VoiceHelper.IVoiceChangeListener, FireHelper.IFireStatusListener,ShareHelper.OnEventStatusListener {
    private Logger logger = LoggerFactory.getLogger("FatiDog.app.FatiDogService");
    private Context context = null;

    public FatiDogService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        logger.info("onCreate...");
        logger.info("onCreate -> 当前进程ID：{},当前线程ID：{}",android.os.Process.myPid(),Thread.currentThread().getId());

        context = this;

        Notification notification = new Notification();
        notification.flags = Notification.FLAG_ONGOING_EVENT;
        notification.flags |= Notification.FLAG_NO_CLEAR;
        notification.flags |= Notification.FLAG_FOREGROUND_SERVICE;
        startForeground(1, notification); //设置为前台进程

        addFireStatusListener();
        addShareDataListener();
        addVoiceListener();

        logger.info("疲劳预警服务启动3...");
        EventBus.getDefault().unregister(this);
        EventBus.getDefault().register(this);

        new Thread(new Runnable() { //这里启动另外开了一个线程，不在主线程执行，然后到创建流接收订阅的时候create默认又自己创建了一个新的线程
            @Override
            public void run() {
                //初始化音频播放
                SoundPlayUtils.getInstance().initSounds(context);
                FatiDogValueHelper.getInstance().initConfig(CommonLib.getInstance().getContext());
                FaceRecogValueHelper.getInstance().initConfig(CommonLib.getInstance().getContext());
                CarStatusUtils.isFired()
                        .subscribe(fired ->{
                            CarStatusUtils.updateFireStatus(fired);
                            CarFireManager.getInstance().initStartWork(fired);
                        });
            }
        }).start();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        logger.info("onStartCommand...");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        logger.info("onDestroy...");
        EventBus.getDefault().unregister(this);
        try{
            removeFireStatusListener();
            removeVoiceListener();
            removeShareDataListener();
            CarFireManager.getInstance().initStartWork(false);
        }catch (Exception e){
            logger.error("服务销毁处理失败...");
        }
    }

    /**
     * 监听点火熄火状态的改变
     */
    private void addFireStatusListener() {
        FireHelper.getInstance().registerFireContentResolver();
        FireHelper.getInstance().addIFireChangeListener(this);
    }

    /**
     * 去除点火熄火状态监听
     */
    private void removeFireStatusListener() {
        FireHelper.getInstance().removeIFireChangeListener(this);
        FireHelper.getInstance().unregisterFireContentResolver();
    }

    /**
     * 监听语音的改变
     */
    private void addVoiceListener() {
        VoiceHelper.getInstance().registerVoiceContentResolver();
        VoiceHelper.getInstance().addIVoiceChangeListener(this);
    }

    /**
     * 取消监听语音的改变
     */
    private void removeVoiceListener() {
        VoiceHelper.getInstance().removeIVoiceChangeListener(this);
        VoiceHelper.getInstance().unregisterVoiceContentResolver();
    }

    /**
     *  添加监听共享数据变化
     */
    private void addShareDataListener(){
        ShareHelper.getInstance().registerContentResolver();
        ShareHelper.getInstance().addListener(this);
    }

    /**
     * 取消监听共享数据变化
     */
    private void removeShareDataListener() {
        ShareHelper.getInstance().removeListener(this);
        ShareHelper.getInstance().unregisterContentResolver();
    }

    @Override
    public void onEventChange(int eventId, ShareHelper.EventVo eventVo) {
        if(ShareHelper.ID_AAC == eventId){
            logger.info("监听到点火/熄火状态的改变 - 立即熄火： {}",eventVo.value);
            CarStatusUtils.updateFireStatus(false);
            CarFireManager.getInstance().initStartWork(false);
        }
    }

    @Override
    public void changeStatus(boolean status) {
        logger.info("监听到点火/熄火状态的改变 :{}", status);
        new Thread(new Runnable() {
            @Override
            public void run() {
                //保存点火状态，重启后不保存
                if(status){
                    CarStatusUtils.updateFireStatus(true);
                    CarFireManager.getInstance().initStartWork(true);
                }
            }
        }).start();
    }

    @Override
    public void changeVoice(String name, String value) {

    }

    //疲劳预警弹窗事件
    public void onEventMainThread(FatiDogWarningEvent event) {
        try{//播放告警音频
            logger.info("接到疲劳告警事件：EventType :{}", FatiDogConstants.FATIDOG_WARNING_TYPE_NAME[event.getEventType()]);

            SoundPlayUtils.getInstance().playSound(context,event.getEventType(),0);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void onEventMainThread(SwitchServiceEvent event){
        logger.info("收到启停服务消息");
        if(!event.isOpen()){
            stopSelf();
        }
    }

    public void onEventMainThread(RemoteControlEvent event){
        logger.info("收到远程控制消息：{}",event.toString());

        String action = event.getAction();
        boolean isScaned = FatiDogValueHelper.getInstance().isFaceScaned();
        if(action.equals(BroadcastConstants.ACTION_DUDU_FATI_ON)){
            if(isScaned){
                FatiDogValueHelper.getInstance().setOpen(true);
                VoiceHelper.getInstance().startSpeaking(
                        CommonLib.getInstance().getContext().getString(R.string.fatidog_has_opened), VoiceConstants.TTS_DONOTHING, false);
            }else{
                Intent intent1 = new Intent(context, FatiDogMainActivity.class);
                intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent1);
            }
        }else if(action.equals(BroadcastConstants.ACTION_DUDU_FATI_OFF)){
            FatiDogValueHelper.getInstance().setOpen(false);
            VoiceHelper.getInstance().startSpeaking(
                    CommonLib.getInstance().getContext().getString(R.string.fatidog_has_closed), VoiceConstants.TTS_DONOTHING, false);

        }else if(action.equals(BroadcastConstants.ACTION_DUDU_FACE_RECOGNITION_ON)){
            if(FaceRecogValueHelper.getInstance().isRegisteredFacePersonEmpty()){
                VoiceHelper.getInstance().startSpeaking(
                        CommonLib.getInstance().getContext().getString(R.string.unregister_face), VoiceConstants.TTS_DONOTHING, false);

                FaceRecogValueHelper.getInstance().setFaceRecogOpen(true);
                Intent intent1 = new Intent(context, FaceRecognitionActivity.class);
                intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent1);
            }else{
                FaceRecogValueHelper.getInstance().setFaceRecogOpen(true);
                VoiceHelper.getInstance().startSpeaking(
                        CommonLib.getInstance().getContext().getString(R.string.recog_has_opened), VoiceConstants.TTS_DONOTHING, false);
            }
        }else if(action.equals(BroadcastConstants.ACTION_DUDU_FACE_RECOGNITION_OFF)){
            FaceRecogValueHelper.getInstance().setFaceRecogOpen(false);
            VoiceHelper.getInstance().startSpeaking(
                    CommonLib.getInstance().getContext().getString(R.string.recog_has_closed), VoiceConstants.TTS_DONOTHING, false);

        } else if(action.equals(BroadcastConstants.ACTION_DUDU_FATIDOG_FORCE_SWITCH_EXIT)){

            logger.info("强制关闭防疲劳(通过模拟熄火的方式)...");
            CarFireManager.getInstance().initStartWork(false);
            VoiceHelper.getInstance().startSpeaking(
                    CommonLib.getInstance().getContext().getString(R.string.fatidog_has_power_off), VoiceConstants.TTS_DONOTHING, false);
        }else if(action.equals(BroadcastConstants.ACTION_DUDU_FATIDOG_FORCE_SWITCH_OPEN)){
            logger.info("恢复防疲劳(需要判断当时是否点火)...");
            CarStatusUtils.isFired()
                    .subscribeOn(Schedulers.newThread())
                    .subscribe(fired ->{
                        if(fired){
                            logger.error("开始恢复防疲劳...");
                            CarFireManager.getInstance().initStartWork(true);
                        }
                    });

            VoiceHelper.getInstance().startSpeaking(
                    CommonLib.getInstance().getContext().getString(R.string.fatidog_has_power_on), VoiceConstants.TTS_DONOTHING, false);
        }else{
            logger.error("未定义广播接收类型 :{}",action);
        }
    }

    public void onEventBackgroundThread(SpeedChangeEvent event){
        logger.trace("接收到速度改变事件:{}",event.toString());
        if(!FatiDogValueHelper.getInstance().isExperienceMode()){
            FatiDogWorkFlow.getInstance().updateWarnModeSet(event.getSpeedKmPH());
        }
    }
}
