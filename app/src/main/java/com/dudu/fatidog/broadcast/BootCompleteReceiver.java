package com.dudu.fatidog.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.dudu.fatidog.service.FatiDogService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BootCompleteReceiver extends BroadcastReceiver {

    private Logger logger = LoggerFactory.getLogger("FatiDog.app.BootCompleteReceiver");

    public BootCompleteReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        logger.info("接收到开机广播/lAUNCHER启动广播，开始启动疲劳预警服务...action : {}",intent.getAction());
        Intent fatiDogServiceIntent = new Intent(context, FatiDogService.class);
        context.startService(fatiDogServiceIntent);
    }
}
