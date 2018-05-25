package com.dudu.fatidog;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.dudu.commonlib.CommonLib;
import com.dudu.convert.FatiDogWorker;
import com.dudu.fatidog.bean.SwitchServiceEvent;
import com.dudu.fatidog.core.CarFireManager;
import com.dudu.fatidog.core.FatiDogValueHelper;
import com.dudu.fatidog.core.FatiDogWarningManager;
import com.dudu.fatidog.core.FatiDogWorkFlow;
import com.dudu.fatidog.map.MapManager;
import com.dudu.fatidog.service.FatiDogService;
import com.dudu.fatidog.util.IAsyncTaskCallBack;
import com.dudu.recoac.FaceRecognitionActivity;

import org.slf4j.LoggerFactory;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import de.greenrobot.event.EventBus;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class DebugActivity extends FragmentActivity {

    private static String TAG = "FatiDog.DebugActivity";
    private static org.slf4j.Logger logger = LoggerFactory.getLogger("FatiDog.app.DebugActivity");
    private TextView displayComData = null;
    private EditText inputSpeed = null;
    private EditText inputSpeedMode = null;
    private ImageView snapShow = null;
    private EditText speedEdit = null;
    private EditText speedModeEdit = null;

    private Button switchTimeBtn = null;
    private Button switchSpeedBtn = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debug);

        initView();
    }

    private void initView(){
        displayComData = (TextView)this.findViewById(R.id.id_display_result);
        inputSpeed = (EditText)this.findViewById(R.id.inputSpeed);
        inputSpeedMode = (EditText)this.findViewById(R.id.inputSpeedMode);
        //id_iv_snap
        snapShow = (ImageView)this.findViewById(R.id.id_iv_snap);

        switchTimeBtn = (Button)this.findViewById(R.id.id_swtich_time_limit);
        switchSpeedBtn = (Button)this.findViewById(R.id.id_swtich_speed_limit);

        speedEdit = (EditText)this.findViewById(R.id.id_edit_speed);
        speedModeEdit = (EditText)this.findViewById(R.id.id_edit_speedMode);


        if(FatiDogWarningManager.getInstance().getCarDrivingTimeLimit() == 30 * 60 * 1000){
            switchTimeBtn.setText("取消时间限制");
        }else{
            switchTimeBtn.setText("恢复时间限制");
        }

        if(FatiDogWarningManager.getInstance().getCarDrvingSpeedLimitL() == 30.0f){
            switchSpeedBtn.setText("取消速度限制");
        }else{
            switchSpeedBtn.setText("恢复速度限制");
        }
    }
    public void onPlanManual(View view){
        FatiDogValueHelper.getInstance().setManualWarnSet(true);
        String speedStr = speedEdit.getText().toString();
        String speedModeStr = speedModeEdit.getText().toString();
        int speed = Integer.parseInt(speedStr);
        int speedMode = Integer.parseInt(speedModeStr);

        logger.info("手动设置防疲劳告警参数，速度：{},速度模式：{}",speed,speedMode);
        FatiDogWorkFlow.getInstance().setWarningModeParam(speed,speedMode,null);
    }

    public void onUnPlanManual(View view){
        FatiDogValueHelper.getInstance().setManualWarnSet(false);
        FatiDogWorkFlow.getInstance().updateWarnModeSet((MapManager.getInstance().getSpeed()*3.6f));

    }
    public void onOpenSerialCom(View v){
        Observable.timer(0, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.newThread())
                .observeOn(Schedulers.newThread())
                .subscribe(l -> {
                    logger.info("打开串口");
                    FatiDogWorker.getInstance().startWork();

                }, throwable -> {
                    logger.error("异常：{}", throwable.toString());
                });

    }
    public void onCloseSerialCom(View v){
        Observable.timer(0, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.newThread())
                .observeOn(Schedulers.newThread())
                .subscribe(l -> {
                    logger.info("关闭串口");
                    FatiDogWorker.getInstance().stopWork();

                }, throwable -> {
                    logger.error("异常：{}", throwable.toString());
                });

    }

    public void onStartMonitor(View v){
        FatiDogWorker.getInstance().subscribeStartFatiMonitorResult()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(startMonitorResult -> {
                    logger.info("接收到开启防疲劳检测结果信息。。。");
                    displayComData.setText("开启疲劳检测结果： ReturnCode :"+startMonitorResult.getReturnCode()+",CmdType :"+startMonitorResult.getCmdType()+",MsgType:"+startMonitorResult.getMsgType());

                }, throwable -> {
                    logger.error("异常：{}", throwable.toString());
                });

        Observable.timer(0, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.newThread())
                .observeOn(Schedulers.newThread())
                .subscribe(l -> {
                    FatiDogWorker.getInstance().startFatiMonitor();

                }, throwable -> {
                    logger.error("异常：{}", throwable.toString());
                });
    }
    public void onStopMonitor(View v){
        FatiDogWorker.getInstance().subscribeStopFatiMonitorResult()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(stopMonitorResult -> {
                    logger.info("接收到停止防疲劳结果信息。。。");
                    displayComData.setText("停止疲劳检测结果： ReturnCode :"+stopMonitorResult.getReturnCode()+",CmdType :"+stopMonitorResult.getCmdType()+",MsgType:"+stopMonitorResult.getMsgType());

                }, throwable -> {
                    logger.error("异常：{}", throwable.toString());
                });

        Observable.timer(0, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.newThread())
                .observeOn(Schedulers.newThread())
                .subscribe(l -> {
                    FatiDogWorker.getInstance().stopFatiMonitor();

                }, throwable -> {
                    logger.error("异常：{}", throwable.toString());
                });

    }
    public void onSubWarning(View v){
        FatiDogWorker.getInstance().subscribeWarningInfo()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(warnInfo -> {
                    logger.info("接收到告警信息。。。");
                    displayComData.setText("告警信息： ReturnCode :"+warnInfo.getReturnCode()+",WarningType :"+warnInfo.getWarningType());

                }, throwable -> {
                    logger.error("异常：{}", throwable.toString());
                });
    }

    public void onSubHeartBeat(View v){
        FatiDogWorker.getInstance().subscribeHeartBeat()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(heartBeat -> {
                    logger.info("接收到心跳信息。。。");
                    displayComData.setText("心跳： ReturnCode :"+heartBeat.getReturnCode()+",WarningType :"+heartBeat.getCmdType());

                }, throwable -> {
                    logger.error("异常：{}", throwable.toString());
                });
    }

    public void onGetFacePosInfo(View v){
        FatiDogWorker.getInstance().subscribeGetFacePositionResult()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(facePosInfo -> {
                    logger.info("接收到人脸位置信息。。。");
                    displayComData.setText("人脸位置： ReturnCode :"+facePosInfo.getReturnCode()+"人脸个数："+facePosInfo.getFaceCount()+",角度："+facePosInfo.getAngle()+",亮度："+facePosInfo.getLight()
                            +",左:"+facePosInfo.getOppLeft()+",上 :"+facePosInfo.getOppTop()+", 右:"+facePosInfo.getOppRight()+",下 :"+facePosInfo.getOppBottom());

                }, throwable -> {
                    logger.error("异常：{}", throwable.toString());
                });

        FatiDogWorker.getInstance().getFacePosition();
    }

    public void onGetVersion(View v){
        FatiDogWorker.getInstance().subscribeVersionInfoResult()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(versionInfo -> {
                    logger.info("接收到版本信息。。。");
                    displayComData.setText("版本信息： ReturnCode :"+versionInfo.getReturnCode()+",FirmwareVersion:"+versionInfo.getFirmwareVersion()+",HardwareVersion :"+versionInfo.getHardwareVersion()+",SoftwareVersion :"+versionInfo.getSoftwareVersion());

                }, throwable -> {
                    logger.error("异常：{}", throwable.toString());
                });

        FatiDogWorker.getInstance().getFatiDogVersionInfo();
    }

    public void onSetSysTime(View v){
        FatiDogWorker.getInstance().subscribeSystemTimeSetResult()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(sysTimeSetResult -> {
                    logger.info("接收到时间设置结果信息。。。");
                    displayComData.setText("设置时间结果： ReturnCode :"+sysTimeSetResult.getReturnCode()+",CmdType :"+sysTimeSetResult.getCmdType()+",MsgType:"+sysTimeSetResult.getMsgType());

                }, throwable -> {
                    logger.error("异常：{}", throwable.toString());
                });


        SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");//设置日期格式
        String curTime = df.format(new Date());// new Date()为获取当前系统时间
        logger.info("当前时间是 ： {}",curTime);
        FatiDogWorker.getInstance().sendSystemTimeSet(curTime);
    }

    public void onSetParam(View v){
        FatiDogWorker.getInstance().subscribeParamSetResult()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(paramSetResult -> {
                    logger.info("接收到参数设置结果信息。。。");
                    displayComData.setText("参数设置结果： ReturnCode :"+paramSetResult.getReturnCode()+",CmdType :"+paramSetResult.getCmdType()+",MsgType:"+paramSetResult.getMsgType());

                }, throwable -> {
                    logger.error("异常：{}", throwable.toString());
                });

        try{
            String speed = inputSpeed.getText().toString();
            String speedMode = inputSpeedMode.getText().toString();
            int speedInt = Integer.valueOf(speed);
            int speedModeInt = Integer.valueOf(speedMode);
            logger.info("设置速度和速度模式：{},{}",speedInt,speedModeInt);
            FatiDogWorker.getInstance().sendParamSetCmd(speedInt,speedModeInt);

        }catch (Exception e){
            e.printStackTrace();
        }

    }

    public void onOpenMain(View v){
        this.startActivity(new Intent(this,FatiDogMainActivity.class));
    }

    public void onOpenRecoacPage(View v){
        this.startActivity(new Intent(this,FaceRecognitionActivity.class));
    }

    public void onStartFatiService(View v){
        logger.info("启动疲劳预警服务...");
        Intent fatiDogServiceIntent = new Intent(this, FatiDogService.class);
        startService(fatiDogServiceIntent);
    }

    public void onSendBroadcast(View v){
        logger.info("发送测试广播...");
        Intent intent = new Intent("android.intent.action.fatidog.start");
//        intent.setAction("tm.dudu.fatidog.start");
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        CommonLib.getInstance().getContext().sendBroadcast(intent);
    } //
    public void onSnap(View v){
        logger.info("开始抓拍...");
        try{
            FatiDogWorkFlow.getInstance().capture(new IAsyncTaskCallBack() {
                @Override
                public void onComplete() {
                    logger.error("抓拍接收完毕！");
                }

                @Override
                public void onError() {

                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void onShowPhoto(View v){
        logger.info("开始显示抓拍图片...");
        try{
            String snapPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath().toString()+"/mySnap.jpg";
            logger.info(" 人脸识别抓拍存储路径为：{}",snapPath);
            snapShow.setImageURI(Uri.fromFile(new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),"mySnap.jpg")));
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void onResetPhoto(View v){
        logger.info("开始显示抓拍图片...");
        try{
            snapShow.setImageResource(R.drawable.andy_icon);
        }catch (Exception e){
            e.printStackTrace();
        }
    } //

    public void onRebootDevice(View v){
        logger.info("开始重启人脸模块...");
        FatiDogWorker.getInstance().subscribeSystemRebootResult()
                .timeout(1, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> {
                    logger.info("接收到人脸模块重启结果信息。。。");
                    displayComData.setText("人脸模块重启失败： ReturnCode :"+result.getReturnCode());

                }, throwable -> {
                    logger.error("异常：{}", throwable.toString());
                    logger.info("人脸模块重启成功！");

                });

        FatiDogWorker.getInstance().systemReboot();
    }

    public void onGetFireTime(View v){

    }

    public void onRecogPerson(View v){
        CarFireManager.getInstance().doFaceRecoDriver(null);
    }


    public void onNoTimeLimit(View v){
        if(FatiDogWarningManager.getInstance().getCarDrivingTimeLimit() == 30 * 60 * 1000){
            switchTimeBtn.setText("恢复时间限制");
            FatiDogWarningManager.getInstance().setCarDrivingTimeLimit(0);
        }else{
            switchTimeBtn.setText("取消时间限制");
            FatiDogWarningManager.getInstance().setCarDrivingTimeLimit(30 * 60 * 1000);
        }

    }

    public void onNoSpeedLimit(View v){
        if(FatiDogWarningManager.getInstance().getCarDrvingSpeedLimitL() == 30.0f){
            switchSpeedBtn.setText("恢复速度限制");
            FatiDogWarningManager.getInstance().setCarDrvingSpeedLimitH(-1);
            FatiDogWarningManager.getInstance().setCarDrvingSpeedLimitL(-1);
        }else{
            switchSpeedBtn.setText("取消速度限制");
            FatiDogWarningManager.getInstance().setCarDrvingSpeedLimitH(70.0f);
            FatiDogWarningManager.getInstance().setCarDrvingSpeedLimitL(30.0f);
        }

    }

    public void onGetCarSpeed(View v){
        float speed = MapManager.getInstance().getSpeed()*3.6f;
        Toast.makeText(this,"当前车速："+speed+"km/h",Toast.LENGTH_LONG).show();
    }
    public void onGetDrivePeriod(View v){
        long timeGone = CarFireManager.getInstance().getTimeHasFired();
        Toast.makeText(this,"行驶时长："+timeGone/(60*1000)+"min",Toast.LENGTH_LONG).show();
    }

    public void onStopService(View v){
        logger.info("关闭服务。。");
        EventBus.getDefault().post(new SwitchServiceEvent(false));
    }
}
