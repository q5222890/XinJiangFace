package com.zack.xjht.ui;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.zack.xjht.Constants;
import com.zack.xjht.R;
import com.zack.xjht.Utils.DialogUtils;
import com.zack.xjht.Utils.SharedUtils;
import com.zack.xjht.Utils.Utils;
import com.zack.xjht.db.DBManager;
import com.zack.xjht.db.gen.AlarmLogBeanDao;
import com.zack.xjht.entity.AlarmLogBean;
import com.zack.xjht.event.EventConsts;
import com.zack.xjht.event.PowerStatusEvent;
import com.zack.xjht.event.StatusEvent;
import com.zack.xjht.hardware.Sensor;
import com.zack.xjht.humiture.OnHumitureListener;
import com.zack.xjht.serial.SerialPortUtil;
import com.zack.xjht.ui.dialog.AlarmDialog;
import com.zx.zxlibrary.SystemUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Activity基类
 */

public class BaseActivity extends AppCompatActivity implements OnHumitureListener {

    private static final String TAG = "BaseActivity";
    @BindView(R.id.ac_top_back)
    protected ImageView ac_top_back;     //返回
    @BindView(R.id.ac_top_temper_txt)
    protected TextView ac_top_temper_txt;  //温度
    @BindView(R.id.ac_top_humidity_txt)
    protected TextView ac_top_humidity_txt;  //湿度
    @BindView(R.id.ac_top_net_txt)
    protected TextView ac_top_net_txt;     //网络状态
    @BindView(R.id.ac_top_date_txt)
    protected TextView ac_top_date_txt;    //当前日期
    @BindView(R.id.ac_top_power_txt)
    protected TextView acTopPowerTxt;
    @BindView(R.id.ac_top_date_img)
    protected ImageView acTopDateImg;
    // 最大的屏幕亮度
    float maxLight;
    //当前的亮度
    float currentLight;
    //用来控制屏幕亮度
    Handler handler;
    //延时时间
    long delayTime = 3 * 60 * 1000L;
//    long delayTime = 20 * 1000L;

    protected long millisInFuture = 10 * 60; //开门报警时长
    private Handler timeHandler = new Handler();

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            millisInFuture--;
//            Log.i(TAG,"剩余时间：" + millisInFuture + "s");
            timeHandler.postDelayed(this, 1000);
            if (millisInFuture == 0) {
                Log.i(TAG, "run 倒计时结束: ");
                timeHandler.removeCallbacks(runnable);
//                finish();
            }
        }
    };
    private int durationTime;
    private boolean isStopTimer;

    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e(TAG, "onCreate: ");
//        ButterKnife.bind(this);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        if (Constants.isOldBoard) {
            initLight();
        }
        new Thread(timeTask).start();
        SerialPortUtil.getInstance().setOnHumitureValueListener(this);

        //隐藏系统虚拟导航栏
        SystemUtils.systembar(this, true);
    }

    protected boolean isShowing() {
        // 获取当前屏幕内容的高度
        int screenHeight = getWindow().getDecorView().getHeight();

        // 获取View可见区域的bottom
        Rect rect = new Rect();
        getWindow().getDecorView().getWindowVisibleDisplayFrame(rect);

        return screenHeight > rect.bottom;
    }

    private void initLight() {
        Log.i(TAG, "initLight: ");
        handler = new Handler(Looper.getMainLooper());
        maxLight = GetLightness(this); //-1.0
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSubscriber(PowerStatusEvent event) {
        String message = event.getMessage();
        switch (message) {
            case EventConsts.EVENT_POWER_NORMAL:
                acTopPowerTxt.setText("市电正常");
                SharedUtils.savePowerStatus(0);
                break;
            case EventConsts.EVENT_BACKUP_POWER:
                acTopPowerTxt.setText("备用电源");
                if (!isFinishing()) {
                    if (SharedUtils.getIsAlarmOpen() && SharedUtils.getPowerStatus() == 0
                            && !Constants.isPowerOffAlarm) {
//                        Sensor.getInstance().alarmSwitch(1);
                        SerialPortUtil.getInstance().openAlertor(Integer.parseInt(SharedUtils.getLeftCabNo()));
                        SharedUtils.savePowerStatus(1);
                        alarmDialog = new AlarmDialog(this, "智能柜断电",
                                Constants.ALARM_POWER_ABNORMAL);
//                        hideDialogBottomUIMenu(alarmDialog);
                        if (!alarmDialog.isShowing()) {
                            alarmDialog.show();
                            Constants.isPowerOffAlarm = true;
                        }
                        cancelAlarm(alarmDialog);
                    }
                }
                break;
            case EventConsts.EVENT_BACKUP_CLOSE: //钥匙开启正常
                SharedUtils.saveBackupOpenStatus(0);
                break;
            case EventConsts.EVENT_BACKUP_OPEN: //钥匙开启
                if (SharedUtils.getCabOpenStatus() == 0 && !Constants.isExecuteTask) {
                    backUpOpenAlarm();
                }
                break;
            case EventConsts.EVENT_CAB_CLOSE://枪柜门关闭
                durationTime = 0;
                isStopTimer = true;
                if (SharedUtils.getCabOpenStatus() == 1) {
                    //设置不查询状态
                    SharedUtils.setIsCheckStatus(false);
                    for (int i = 0; i < 3; i++) {
                        SerialPortUtil.getInstance().closeLED();//关闭枪锁数码管led
                        try {
                            Thread.sleep(200);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        SerialPortUtil.getInstance().closeLED(254);//关闭枪锁数码管led
                        try {
                            Thread.sleep(200);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    //设置查询状态
                    SharedUtils.setIsCheckStatus(true);
                }
                SharedUtils.saveOpenOvertimeStatus(0);
                SharedUtils.saveCabOpenStatus(0);
                SharedUtils.saveOpenAbnormalStatus(0);
                Constants.isCheckGunStatus = false;
                break;
            case EventConsts.EVENT_CAB_OPEN://枪柜门开启
                if (SharedUtils.getCabOpenStatus() == 0) {
                    //打开继电器
                    SerialPortUtil.getInstance().powerOn();
                    //设置不查询状态
                    SharedUtils.setIsCheckStatus(false);
                    for (int i = 0; i < 3; i++) {
                        SerialPortUtil.getInstance().openLED();//打开枪锁数码管led
                        try {
                            Thread.sleep(200);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        SerialPortUtil.getInstance().openLED(254);//打开枪锁数码管led
                        try {
                            Thread.sleep(200);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    //设置查询状态
                    SharedUtils.setIsCheckStatus(true);
                    SharedUtils.saveCabOpenStatus(1);

                    //判断是否正在执行任务
                    if (!Constants.isExecuteTask) {
                        //产生非正常开启柜门报警
                        cabOpenAbnormalAlarm();
                    }
                    //查询枪锁状态
                    Constants.isCheckGunStatus = true;

                    durationTime = 50;  //柜门开启时间超过5分钟后产生一次报警
                    isStopTimer = false;
                    final Timer timer = new Timer();
                    final TimerTask timerTask = new TimerTask() {
                        @Override
                        public void run() {
//                            Log.i(TAG, "timerTask run: ");
                            if (durationTime > 0) {
                                durationTime--;
                                Log.i(TAG, "run durationTime: " + durationTime);
                            } else {
                                timer.cancel();
                                if (!isStopTimer) {
                                    //产生报警
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            cabOpenOvertimeAlarm();
                                        }
                                    });
                                }
                            }
                        }
                    };
                    timer.schedule(timerTask, 1000, 6 * 1000);
                }
                break;
            case EventConsts.EVENT_VIBRATION_NORMAL://震动正常
                SharedUtils.saveVibrationStatus(0);
                break;
            case EventConsts.EVENT_VIBRATION_ABNORMAL://震动异常
                if (SharedUtils.getCabOpenStatus() == 0 && !Constants.isExecuteTask) {
                    vibrationAlarm();
                }
                break;
        }
    }

    private Map<Integer, Integer> gunStatus = new HashMap<>();

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onGunStatusSubscriber(StatusEvent event) {
        int address = event.getAddress();
        int category = event.getCategory();
        int status = event.getStatus();
        String message = event.getMessage();
        Log.i(TAG, "onSubscriber message: " + message);
        Log.i(TAG, "onSubscriber  address : " + address + " category:" + category + " status:" + status);
        if (category == 1) { //锁开关状态
            if (status == 0) { //开启
                Log.i(TAG, "onSubscriber : " + address + "枪锁打开");
                //如果不是正常任务开枪锁 产生一条报警数据
                if (gunStatus.containsKey(address)) {
                    Integer status1 = gunStatus.get(address);
                    if (status1 != null && status1 == 1) {
                        gunStatus.put(address, status);
                        //触发报警
                        if (SharedUtils.getCabOpenStatus() == 1) {
                            illegalGetGunAlarm();
                        }
                    }
                } else {
                    gunStatus.put(address, status);
                    //触发报警
                    if (SharedUtils.getCabOpenStatus() == 1) {
                        illegalGetGunAlarm();
                    }
                }
            } else if (status == 1) {//关闭
                Log.i(TAG, "onSubscriber : " + address + "枪锁锁闭");
                gunStatus.put(address, status);
            } else if (status == 2) {//异常
                Log.i(TAG, "onSubscriber : " + address + "枪锁异常");
            } else if (status == 41) {  //弹仓打开
                Log.i(TAG, "onSubscriber : " + address + "弹仓打开");
                //如果不是正常任务开枪锁 产生一条报警数据
                if (gunStatus.containsKey(address)) {
                    Integer status1 = gunStatus.get(address);
                    if (status1 != null && status1 == 61) {
                        gunStatus.put(address, status);
                        //触发报警
                        if (SharedUtils.getCabOpenStatus() == 1) {
                            illegalGetGunAlarm();
                        }
                    }
                } else {
                    gunStatus.put(address, status);
                    //触发报警
                    if (SharedUtils.getCabOpenStatus() == 1) {
                        illegalGetGunAlarm();
                    }
                }
            } else if (status == 61) {  //弹仓关闭
                Log.i(TAG, "onSubscriber : " + address + "弹仓关闭");
                gunStatus.put(address, status);
            }
        } else if (category == 2) { //枪支在位状态
            if (status == 0) {//离位
                Log.i(TAG, "onSubscriber : " + address + "枪不在位");
            } else if (status == 1) {//在位
                Log.i(TAG, "onSubscriber : " + address + "枪在位");
            }
        }
    }

    /**
     * 开门超时报警
     */
    private void illegalGetGunAlarm() {
//        int illegalGetGunStatus = SharedUtils.getIllegalGetGunStatus();
//        Log.i(TAG, "onKeyDown openOvertimeStatus: " + openOvertimeStatus);
//        if (SharedUtils.getIsAlarmOpen() && illegalGetGunStatus == 0 && !Constants.isIllegalGetGunAlarm) {
        if (SharedUtils.getIsAlarmOpen() && !Constants.isIllegalGetGunAlarm) {
//            SharedUtils.saveIllegalGetGunStatus(1);
//            Sensor.getInstance().alarmSwitch(1);//打开报警
            SerialPortUtil.getInstance().openAlertor(Integer.parseInt(SharedUtils.getLeftCabNo()));
            if (!isFinishing()) {
                alarmDialog = new AlarmDialog(BaseActivity.this,
                        "非正常领取枪支或弹药", Constants.ALARM_GET_GUN_ABNORMAL);
//                hideDialogBottomUIMenu(alarmDialog);
                if (!alarmDialog.isShowing()) {
                    alarmDialog.show();
                    Constants.isIllegalGetGunAlarm = true;
                }
                cancelAlarm(alarmDialog);
            } else {
//                Sensor.getInstance().alarmSwitch(0);//关闭报警
                SerialPortUtil.getInstance().closeAlertor(Integer.parseInt(SharedUtils.getLeftCabNo()));
            }
        }
    }

    /**
     * 开门超时报警
     */
    private void cabOpenOvertimeAlarm() {
        int openOvertimeStatus = SharedUtils.getOpenOvertimeStatus();
//        Log.i(TAG, "onKeyDown openOvertimeStatus: " + openOvertimeStatus);
        if (SharedUtils.getIsAlarmOpen() && openOvertimeStatus == 0 && !Constants.isOverTimeAlarm) {
            SharedUtils.saveOpenOvertimeStatus(1);
//            Sensor.getInstance().alarmSwitch(1);//打开报警
            SerialPortUtil.getInstance().openAlertor(Integer.parseInt(SharedUtils.getLeftCabNo()));
            if (!isFinishing()) {
                alarmDialog = new AlarmDialog(BaseActivity.this,
                        "柜门开启超时报警", Constants.ALARM_OPEN_CAB_OVERTIME);
//                hideDialogBottomUIMenu(alarmDialog);
                if (!alarmDialog.isShowing()) {
                    alarmDialog.show();
                    Constants.isOverTimeAlarm = true;
                }
                cancelAlarm(alarmDialog);
            } else {
//                Sensor.getInstance().alarmSwitch(0);//关闭报警
                SerialPortUtil.getInstance().closeAlertor(Integer.parseInt(SharedUtils.getLeftCabNo()));
            }
        }
    }

    /**
     * 非正常开启柜门报警
     */
    private void cabOpenAbnormalAlarm() {
        int openAbnormalStatus = SharedUtils.getOpenAbnormalStatus();
//        Log.i(TAG, "onKeyDown openOvertimeStatus: " + openOvertimeStatus);
        if (SharedUtils.getIsAlarmOpen() && openAbnormalStatus == 0 && !Constants.isIllegalOpenCabAlarm) {
            SharedUtils.saveOpenAbnormalStatus(1);
//            Sensor.getInstance().alarmSwitch(1);//打开报警
            if (!isFinishing()) {
                alarmDialog = new AlarmDialog(BaseActivity.this,
                        "非正常开启柜门报警", Constants.ALARM_OPEN_CAB_ABNORMAL);
//                hideDialogBottomUIMenu(alarmDialog);
                if (!alarmDialog.isShowing()) {
                    alarmDialog.show();
                    SerialPortUtil.getInstance().openAlertor(Integer.parseInt(SharedUtils.getLeftCabNo()));
                    Constants.isIllegalOpenCabAlarm = true;
                }
                cancelAlarm(alarmDialog);
            } else {
//                Sensor.getInstance().alarmSwitch(0);//关闭报警
                SerialPortUtil.getInstance().closeAlertor(Integer.parseInt(SharedUtils.getLeftCabNo()));
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e(TAG, "onResume: ");
        /**
         * 开启休眠任务
         */
        if (Constants.isOldBoard) {
            startSleepTask();
        }

        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }

        if (ac_top_humidity_txt != null && ac_top_temper_txt != null) {
            ac_top_temper_txt.setText(SharedUtils.getTemperatureValue() + "℃");
            ac_top_humidity_txt.setText(SharedUtils.getHumidityValue() + "%");
        }

        registerReceiver(networkStateReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    /**
     * 开启休眠任务
     */
    void startSleepTask() {
        Log.i(TAG, "startSleepTask: ");
        SetLight(this, (int) maxLight);
        handler.removeCallbacks(sleepWindowTask);
        handler.postDelayed(sleepWindowTask, delayTime);
    }

    /**
     * 结束休眠任务
     */
    void stopSleepTask() {
        Log.i(TAG, "stopSleepTask: ");
        handler.removeCallbacks(sleepWindowTask);
    }

    /**
     * 休眠任务
     */
    Runnable sleepWindowTask = new Runnable() {

        @Override
        public void run() {
            SetLight(BaseActivity.this, 1);
        }
    };

    /**
     * 设置亮度
     *
     * @param context
     * @param light
     */
    private void SetLight(Activity context, int light) {
        Log.i(TAG, "SetLight: " + light);
        if (light == 1) {//
            if (SharedUtils.getCabOpenStatus() == 0) {
                SerialPortUtil.getInstance().powerOff();
            }
        } else {
            SerialPortUtil.getInstance().powerOn();
        }
        currentLight = light;
        WindowManager.LayoutParams localLayoutParams = context.getWindow().getAttributes();
        localLayoutParams.screenBrightness = (light / 255.0F);
        context.getWindow().setAttributes(localLayoutParams);
    }

    /**
     * 获取亮度
     *
     * @param context
     * @return
     */
    private float GetLightness(Activity context) {
        WindowManager.LayoutParams localLayoutParams = context.getWindow().getAttributes();
        float light = localLayoutParams.screenBrightness;
        return light;
    }

//    /**
//     * 隐藏虚拟按键，并且全屏
//     */
//    protected void hideDialogBottomUIMenu(final Dialog mDialog) {
//        //隐藏虚拟按键，并且全屏
//        mDialog.getWindow().getDecorView().setSystemUiVisibility(
//                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
//        mDialog.getWindow().getDecorView().setOnSystemUiVisibilityChangeListener(
//                new View.OnSystemUiVisibilityChangeListener() {
//                    @Override
//                    public void onSystemUiVisibilityChange(int visibility) {
//                        int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
//                                //布局位于状态栏下方
//                                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
//                                //全屏
//                                View.SYSTEM_UI_FLAG_FULLSCREEN |
//                                //隐藏导航栏
//                                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
//                                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
//                        if (Build.VERSION.SDK_INT >= 19) {
//                            uiOptions |= 0x00001000;
//                        } else {
//                            uiOptions |= View.SYSTEM_UI_FLAG_LOW_PROFILE;
//                        }
//                        mDialog.getWindow().getDecorView().setSystemUiVisibility(uiOptions);
//                    }
//                });
//    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.e(TAG, "onPause: ");
        /**
         * 停止休眠任务
         */
        if (Constants.isOldBoard) {
            stopSleepTask();
        }
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
        unregisterReceiver(networkStateReceiver);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.e(TAG, "onStop:");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e(TAG, "onDestroy");
        if (alarmDialog != null) {
            alarmDialog.dismiss();
        }
    }

//    @Override
//    public boolean dispatchTouchEvent(MotionEvent ev) {
//        switch (ev.getAction()) {
//            case MotionEvent.ACTION_DOWN:
////                Log.i(TAG, "dispatchTouchEvent ACTION_DOWN: ");
//                break;
//            case MotionEvent.ACTION_MOVE:
////                Log.i(TAG, "dispatchTouchEvent ACTION_MOVE: ");
//                break;
//            case MotionEvent.ACTION_UP:
////                Log.i(TAG, "dispatchTouchEvent ACTION_UP: ");
//                if (Constants.isOldBoard) {
//                    stopSleepTask();
//                }
//                break;
//        }
//        return super.dispatchTouchEvent(ev);
//    }

    private Runnable timeTask = new Runnable() {
        @Override
        public void run() {
            while (true) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        CharSequence time = DateFormat.format("yyyy-MM-dd HH:mm:ss",
                                System.currentTimeMillis());
                        if (ac_top_date_txt != null) {
                            ac_top_date_txt.setText(time);
                        }
//                        Log.i(TAG, "run: " + time);
//                        Log.i(TAG, "run isShowing: "+isShowing());
                    }
                });
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    @OnClick(R.id.ac_top_back)
    protected void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.ac_top_back://系统设置
                finish();
                break;
        }
    }

    private AlarmDialog alarmDialog;
    private BroadcastReceiver networkStateReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                String action = intent.getAction();
                Log.i(TAG, "onReceive action:  " + action);
                if (ConnectivityManager.CONNECTIVITY_ACTION.equals(action)) {
                    ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                    NetworkInfo info = cm.getActiveNetworkInfo();
                    if (info != null && info.isConnected()) {
                        Log.i(TAG, getString(R.string.net_status_connect));
                        ac_top_net_txt.setText(R.string.net_status_connect);
                        Drawable drawable = getResources().getDrawable(R.drawable.ethernet_connect);
                        drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
                        ac_top_net_txt.setCompoundDrawables(drawable, null, null, null);
                        int networkStatus = SharedUtils.getNetworkStatus();
                        if (networkStatus == 1) {
                            SharedUtils.saveNetworkStatus(0);
                        }
                    } else {
                        if (SharedUtils.getIsServerOnline()) {
                            SharedUtils.setIsServerOnline(false);
                        }
                        //连接断开 报警 产生日志
                        Log.i(TAG, getString(R.string.net_disconnect));
                        ac_top_net_txt.setText(R.string.net_disconnect);
                        Drawable drawable = getResources().getDrawable(R.drawable.ethernet_disconnect);
                        drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
                        ac_top_net_txt.setCompoundDrawables(drawable, null, null, null);
                        //添加到报警日志
                        if (SharedUtils.getNetworkStatus() == 0) {
                            AlarmLogBean alarmLogBean = new AlarmLogBean();
                            alarmLogBean.setWarningTime(Utils.longTime2String(System.currentTimeMillis()));
                            alarmLogBean.setWarningType(String.valueOf(Constants.ALARM_NETWORK_DISCONNECT));
                            alarmLogBean.setWarningState(String.valueOf(2));
                            alarmLogBean.setWarningContent("网络断开连接");
                            alarmLogBean.setIsSync(false);
                            alarmLogBean.setGunCabinetId(SharedUtils.getGunCabId());
                            AlarmLogBeanDao alarmLogBeanDao = DBManager.getInstance().getAlarmLogBeanDao();
                            alarmLogBeanDao.insert(alarmLogBean);
                            SharedUtils.saveNetworkStatus(1);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    private void cancelAlarm(AlarmDialog alarmDialog) {
        if (alarmDialog != null) {
            alarmDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
//                    Sensor.getInstance().alarmSwitch(0);
                    SerialPortUtil.getInstance().closeAlertor(Integer.parseInt(SharedUtils.getLeftCabNo()));
                    if (Constants.isPowerOffAlarm) {
                        Constants.isPowerOffAlarm = false;
                    }
                    if (Constants.isKeyOpenAlarm) {
                        Constants.isKeyOpenAlarm = false;
                    }
                    if (Constants.isOverTimeAlarm) {
                        Constants.isOverTimeAlarm = false;
                    }
                    if (Constants.isVibrationAlarm) {
                        Constants.isVibrationAlarm = false;
                    }

                    if (Constants.isHumitureAalrm) {
                        Constants.isHumitureAalrm = false;
                    }

                    if (Constants.isIllegalOpenCabAlarm) {
                        Constants.isIllegalOpenCabAlarm = false;
                    }

                    if (Constants.isIllegalGetGunAlarm) {
                        Constants.isIllegalGetGunAlarm = false;
                    }
                    Log.i(TAG, "onDismiss : ");
                }
            });
        }
    }

    /**
     * 震动报警
     */
    private void vibrationAlarm() {
        int openCabStatus = SharedUtils.getVibrationStatus();
//        Log.i(TAG, "onKeyDown open: " + alarmOpen + " status:" + openCabStatus);
        if (SharedUtils.getIsAlarmOpen() && openCabStatus == 0 && !Constants.isVibrationAlarm) {
            SharedUtils.saveVibrationStatus(1);
//            Sensor.getInstance().alarmSwitch(1);
            SerialPortUtil.getInstance().openAlertor(Integer.parseInt(SharedUtils.getLeftCabNo()));
            if (!isFinishing()) {
                alarmDialog = new AlarmDialog(BaseActivity.this,
                        "柜体异常震动报警", Constants.ALARM_VIBRATION);
//                hideDialogBottomUIMenu(alarmDialog);
                if (!alarmDialog.isShowing()) {
                    alarmDialog.show();
                    Constants.isVibrationAlarm = true;
                }
                cancelAlarm(alarmDialog);
            }
        }
    }

    /**
     * 备用钥匙开启柜门报警
     */
    private void backUpOpenAlarm() {
        int leftOpenStatus = SharedUtils.getBackupOpenStatus();
//        Log.i(TAG, "onKeyDown backupOpenCabStatus: " + leftOpenStatus);
        if (SharedUtils.getIsAlarmOpen() && leftOpenStatus == 0 && !Constants.isKeyOpenAlarm) {
            SharedUtils.saveBackupOpenStatus(1);
//            Sensor.getInstance().alarmSwitch(1);//打开报警
            SerialPortUtil.getInstance().openAlertor(Integer.parseInt(SharedUtils.getLeftCabNo()));
            if (!isFinishing()) {
                alarmDialog = new AlarmDialog(BaseActivity.this,
                        "备用钥匙打开柜门", Constants.ALARM_BACKUP_OPEN_GUN_LOCK);
//                hideDialogBottomUIMenu(alarmDialog);
                if (!alarmDialog.isShowing()) {
                    alarmDialog.show();
                    Constants.isKeyOpenAlarm = true;
                }
                cancelAlarm(alarmDialog);
            } else {
//                Sensor.getInstance().alarmSwitch(0);//关闭报警
                SerialPortUtil.getInstance().closeAlertor(Integer.parseInt(SharedUtils.getLeftCabNo()));
            }
        }
    }

    /**
     * 温湿度异常报警
     */
    private void humitureAbnormalAlarm(final String reason) {
        int humitureAlarmStatus = SharedUtils.getHumitureAlarmStatus();
//        Log.i(TAG, "onKeyDown backupOpenCabStatus: " + humitureAlarmStatus);
        if (SharedUtils.getIsAlarmOpen() && humitureAlarmStatus == 0 && !Constants.isKeyOpenAlarm) {
            SharedUtils.saveHumitureAlarmStatus(1);
//            Sensor.getInstance().alarmSwitch(1);//打开报警
            SerialPortUtil.getInstance().openAlertor(Integer.parseInt(SharedUtils.getLeftCabNo()));
            if (!isFinishing()) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        alarmDialog = new AlarmDialog(BaseActivity.this,
                                reason, Constants.ALARM_HUMITURE_ABNORMAL);
//                        hideDialogBottomUIMenu(alarmDialog);
                        if (!alarmDialog.isShowing()) {
                            alarmDialog.show();
                            Constants.isHumitureAalrm = true;
                        }
                        cancelAlarm(alarmDialog);
                    }
                });
            } else {
//                Sensor.getInstance().alarmSwitch(0);//关闭报警
                SerialPortUtil.getInstance().closeAlertor(Integer.parseInt(SharedUtils.getLeftCabNo()));
            }
        }
    }

    private Dialog dialog;

    protected void showDialog(String msg) {
        dialog = DialogUtils.creatTipDialog(this, "提示", msg,
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //获取最新的数据 并刷新适配器
                        dialog.dismiss();
                    }
                });
        if (dialog != null && !dialog.isShowing()) {
            dialog.show();
        }
    }

    private Dialog tipDialog;

    protected void showDialogAndFinish(String msg) {
        if (!isFinishing()) {
            if (tipDialog != null) {
                if (!tipDialog.isShowing()) {
                    tipDialog.show();
                }
                DialogUtils.setTipText(msg);
                Log.i(TAG, "dialog is not null ");
            } else { //dialog为null
                tipDialog = DialogUtils.creatTipDialog(this, "提示", msg,
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                //获取最新的数据 并刷新适配器
                                tipDialog.dismiss();
                                finish();
                            }
                        });
                if (!tipDialog.isShowing()) {
                    tipDialog.show();
                }
                Log.i(TAG, "dialog is null");
            }
        }
    }

    @Override
    public void onHumitureValue(final float temperature, final float humidity) {
        try {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (ac_top_humidity_txt != null && ac_top_temper_txt != null) {
                        ac_top_temper_txt.setText(temperature + "℃");
                        ac_top_humidity_txt.setText(humidity + "%");
                    }
                }
            });

            SharedUtils.saveHumidityValue(humidity);
            SharedUtils.saveTemperatureValue(temperature);

            int humitureAlarmValue = SharedUtils.getHumitureAlarmValue();
            int tempratureAlarmValue = SharedUtils.getTempratureAlarmValue();

            if (humidity >= humitureAlarmValue) {
                humitureAbnormalAlarm("湿度异常报警");
            } else {
                SharedUtils.saveHumitureAlarmStatus(0);
            }

            if (temperature >= tempratureAlarmValue) {
                humitureAbnormalAlarm("温度异常报警");
            } else {
                SharedUtils.saveHumitureAlarmStatus(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
