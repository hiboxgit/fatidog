package com.dudu.fatidog.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.dudu.fatidog.bean.RemoteControlEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.greenrobot.event.EventBus;

public class RemoteSwitchReceiver extends BroadcastReceiver {
    private Logger logger = LoggerFactory.getLogger("FatiDog.app.RemoteSwitchReceiver");
    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.

        String action = intent.getAction();
        logger.info("接收到：防疲劳远程强制开关广播： {}",action);
        EventBus.getDefault().post(new RemoteControlEvent(action));
    }
}
