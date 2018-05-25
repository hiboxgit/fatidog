package com.dudu.fatidog.util;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;

import com.dudu.fatidog.R;

import java.util.HashMap;

/**
 * Author: Robert
 * Date:  2017-01-05
 * Copyright (c) 2016,dudu Co.,Ltd. All rights reserved.
 * Desc: 音频播放工具类
 */
public class SoundPlayUtils {

    private MediaPlayer mPlayer;
    private SoundPool mSound;
    private HashMap<Integer, Integer> soundPoolMap;

    private static SoundPlayUtils instance = null;
    public static SoundPlayUtils getInstance(){
        if(instance == null){
            instance = new SoundPlayUtils();
        }
        return instance;
    }
    /**
     * 初始化声音
     */
    public void initSounds2(Context context) {
        // 设置播放音效
//        mPlayer = MediaPlayer.create(context, R.raw.drive_carefully);
        mPlayer = new MediaPlayer();
        // 第一个参数为同时播放数据流的最大个数，第二数据流类型，第三为声音质量
        mSound = new SoundPool(4, AudioManager.STREAM_MUSIC, 100);
        soundPoolMap = new HashMap<Integer, Integer>();
        soundPoolMap.put(FatiDogConstants.FATIDOG_WARNING_TYPE_BE_CAREFUL, mSound.load(context, R.raw.drive_carefully, 1));
        soundPoolMap.put(FatiDogConstants.FATIDOG_WARNING_TYPE_LOOK_FORWARD, mSound.load(context, R.raw.look_forward, 1));
        soundPoolMap.put(FatiDogConstants.FATIDOG_WARNING_TYPE_DANGER, mSound.load(context, R.raw.danagerous, 1));
//        soundPoolMap.put(FatiDogConstants.FATIDOG_WARNING_TYPE_TIRED, mSound.load(context, R.raw.tired_carefully, 1));
//        soundPoolMap.put(FatiDogConstants.FATIDOG_WARNING_TYPE_INVALID_DRIVER, mSound.load(context, R.raw.invalid_driver, 1));
//        soundPoolMap.put(FatiDogConstants.FATIDOG_WARNING_TYPE_MULTILE_DISTRACTION, mSound.load(context, R.raw.multiple_distractions, 1));
//        soundPoolMap.put(0, sp.load(context.getAssets().openFd("music/enter.mp3"),1));
        //可以在后面继续put音效文件
    }

    public void initSounds(Context context) {
        // 设置播放音效
        mPlayer = new MediaPlayer();
        // 第一个参数为同时播放数据流的最大个数，第二数据流类型，第三为声音质量
        soundPoolMap = new HashMap<Integer, Integer>();
        soundPoolMap.put(FatiDogConstants.FATIDOG_WARNING_TYPE_BE_CAREFUL, R.raw.drive_carefully);
        soundPoolMap.put(FatiDogConstants.FATIDOG_WARNING_TYPE_LOOK_FORWARD, R.raw.look_forward);
        soundPoolMap.put(FatiDogConstants.FATIDOG_WARNING_TYPE_DANGER, R.raw.danagerous);
    }

    /**
     * soundPool播放
     *
     * @param soundIndex
     *            播放第一个
     * @param loop
     *            是否循环
     */
    public void playSound2(Context context,int soundIndex, int loop) {
        try{
            AudioManager mgr = (AudioManager) context
                    .getSystemService(Context.AUDIO_SERVICE);
            // 获取系统声音的当前音量
            float currentVolume = mgr.getStreamVolume(AudioManager.STREAM_MUSIC);
            // 获取系统声音的最大音量
            float maxVolume = mgr.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            // 获取当前音量的百分比
            float volume = currentVolume / maxVolume;

            // 第一个参数是声效ID,第二个是左声道音量，第三个是右声道音量，第四个是流的优先级，最低为0，第五个是是否循环播放，0为不循环，-1为循环;第六个播放速度(1.0 =正常播放,范围0.5 - 2.0)
            mSound.play(soundPoolMap.get(soundIndex), 1.0f, 1.0f, 0x7FFFFFFF, loop, 1.0f);

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void playSound(Context context,int soundIndex, int loop) {
        try{
            mPlayer.reset();
            mPlayer=MediaPlayer.create(context, soundPoolMap.get(soundIndex));//重新设置要播放的音频
            mPlayer.start();//开始播放

        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
