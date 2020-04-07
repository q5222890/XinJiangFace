package com.zack.xjht.ui;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.androidhiddencamera.CameraConfig;
import com.androidhiddencamera.CameraError;
import com.androidhiddencamera.HiddenCameraActivity;
import com.androidhiddencamera.HiddenCameraUtils;
import com.androidhiddencamera.config.CameraFacing;
import com.androidhiddencamera.config.CameraFocus;
import com.androidhiddencamera.config.CameraImageFormat;
import com.androidhiddencamera.config.CameraResolution;
import com.yanzhenjie.nohttp.rest.Response;
import com.zack.xjht.Constants;
import com.zack.xjht.R;
import com.zack.xjht.Utils.DialogUtils;
import com.zack.xjht.Utils.LogUtil;
import com.zack.xjht.Utils.SharedUtils;
import com.zack.xjht.Utils.SoundPlayUtil;
import com.zack.xjht.Utils.ToastUtil;
import com.zack.xjht.Utils.Utils;
import com.zack.xjht.adapter.InStoreListAdapter;
import com.zack.xjht.db.DBManager;
import com.zack.xjht.entity.CabInfoBean;
import com.zack.xjht.entity.InStoreListBean;
import com.zack.xjht.entity.ScrapListBean;
import com.zack.xjht.entity.SubCabBean;
import com.zack.xjht.entity.UserBean;
import com.zack.xjht.event.EventConsts;
import com.zack.xjht.event.MessageEvent;
import com.zack.xjht.event.PowerStatusEvent;
import com.zack.xjht.event.StatusEvent;
import com.zack.xjht.hardware.Sensor;
import com.zack.xjht.http.HttpClient;
import com.zack.xjht.http.HttpListener;
import com.zack.xjht.serial.SerialPortUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 报废任务清单
 */
public class ScrapListActivity extends HiddenCameraActivity {
    private static final String TAG = "ScrapListActivity";
    private static final int REQ_CODE_CAMERA_PERMISSION = 133;
    @BindView(R.id.scrap_task_list_tv_tittle)
    TextView scrapTaskListTvTittle;
    @BindView(R.id.scrap_task_list_tv_subcab_no)
    TextView scrapTaskListTvSubcabNo;
    @BindView(R.id.scrap_task_list_tv_subcab_type)
    TextView scrapTaskListTvSubcabType;
    @BindView(R.id.scrap_task_list_tv_gun_type)
    TextView scrapTaskListTvGunType;
    @BindView(R.id.scrap_task_list_tv_gun_no)
    TextView scrapTaskListTvGunNo;
    @BindView(R.id.scrap_task_list_tv_ammo_num)
    TextView scrapTaskListTvAmmoNum;
    @BindView(R.id.scrap_task_list_tv_operate)
    TextView scrapTaskListTvOperate;
    @BindView(R.id.scrap_task_list_ll_title)
    LinearLayout scrapTaskListLlTitle;
    @BindView(R.id.scrap_task_rv_list)
    RecyclerView scrapTaskRvList;
    @BindView(R.id.scrap_btn_pre_page)
    Button scrapBtnPrePage;
    @BindView(R.id.scrap_tv_cur_page)
    TextView scrapTvCurPage;
    @BindView(R.id.scrap_btn_next_page)
    Button scrapBtnNextPage;
    @BindView(R.id.scrap_btn_open_door)
    Button scrapBtnOpenDoor;
    @BindView(R.id.scrap_btn_open_lock)
    Button scrapBtnOpenLock;
    @BindView(R.id.scrap_btn_ok)
    Button scrapBtnOk;
    @BindView(R.id.scrap_btn_finish)
    Button scrapBtnFinish;
    @BindView(R.id.scrap_btn_back)
    Button scrapBtnBack;

    private int index = 0;
    private int pageCount = 8;
    private List<InStoreListBean> list = new ArrayList<>();
    private InStoreListAdapter inStoreListAdapter;
    private String scrapTaskId;
    private Map<Integer, String> gunStatus = new HashMap<>();
    private UserBean apply;
    private boolean isQueryStatus = true;
    private CameraConfig mCameraConfig;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scrap_list);
        ButterKnife.bind(this);
        initView();
        if (SharedUtils.getIsCaptureOpen()) {
            configCamera();
        }
        Constants.isIllegalOpenCabAlarm =true;
        //置为正在任务操作
        Constants.isExecuteTask =true;
    }

    private void initView() {
        scrapTaskId = getIntent().getStringExtra("scrapTaskId");
        if (!TextUtils.isEmpty(scrapTaskId)) {
            if (Utils.isNetworkAvailable()) {
                getCabData();
            }
        }

        scrapBtnPrePage.setVisibility(View.INVISIBLE);
        scrapBtnNextPage.setVisibility(View.INVISIBLE);

        LinearLayoutManager llm = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL,
                false);
        scrapTaskRvList.setLayoutManager(llm);
        inStoreListAdapter = new InStoreListAdapter(list, index, pageCount);
        scrapTaskRvList.setAdapter(inStoreListAdapter);

        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSubscriber(PowerStatusEvent event) {
        String message = event.getMessage();
        switch (message) {
            case EventConsts.EVENT_POWER_NORMAL:
//                acTopPowerTxt.setText("市电正常");
                SharedUtils.savePowerStatus(0);
                break;
            case EventConsts.EVENT_BACKUP_POWER:
//                acTopPowerTxt.setText("备用电源");
                if (!isFinishing()) {
                    if (SharedUtils.getIsAlarmOpen()
                            && SharedUtils.getPowerStatus() == 0
                            && !Constants.isPowerOffAlarm) {
//                        Sensor.getInstance().alarmSwitch(1);
                        SerialPortUtil.getInstance().openAlertor(Integer.parseInt(SharedUtils.getLeftCabNo()));
                        SharedUtils.savePowerStatus(1);
//                        alarmDialog = new AlarmDialog(this, "智能柜断电",
//                                Constants.ALARM_POWER_ABNORMAL);
//                        hideDialogBottomUIMenu(alarmDialog);
//                        if (!alarmDialog.isShowing()) {
//                            alarmDialog.show();
//                            Constants.isPowerOffAlarm = true;
//                        }
//                        cancelAlarm(alarmDialog);
                    }
                }
                break;
            case EventConsts.EVENT_BACKUP_CLOSE: //钥匙开启正常
                SharedUtils.saveBackupOpenStatus(0);
                break;
            case EventConsts.EVENT_BACKUP_OPEN: //钥匙开启
//                backUpOpenAlarm();
                break;
            case EventConsts.EVENT_CAB_CLOSE://枪柜门关闭
//                durationTime = 0;
//                isStopTimer = true;
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
                break;
            case EventConsts.EVENT_CAB_OPEN://枪柜门开启
                if (SharedUtils.getCabOpenStatus() == 0) {
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
//                    durationTime = 50;  //门开启3分钟产生一次报警
//                    isStopTimer = false;
//                    final Timer timer = new Timer();
//                    final TimerTask timerTask = new TimerTask() {
//                        @Override
//                        public void run() {
////                            Log.i(TAG, "timerTask run: ");
//                            if (durationTime > 0) {
//                                durationTime--;
//                                Log.i(TAG, "run durationTime: " + durationTime);
//                            } else {
//                                timer.cancel();
//                                if (!isStopTimer) {
//                                    //产生报警
//                                    runOnUiThread(new Runnable() {
//                                        @Override
//                                        public void run() {
//                                            cabOpenOvertimeAlarm();
//                                        }
//                                    });
//                                }
//                            }
//                        }
//                    };
//                    timer.schedule(timerTask, 1000, 6 * 1000);
                }
                break;
            case EventConsts.EVENT_VIBRATION_NORMAL://震动正常
                SharedUtils.saveVibrationStatus(0);
                break;
            case EventConsts.EVENT_VIBRATION_ABNORMAL://震动异常
//                vibrationAlarm();
                break;
        }
    }

    private void configCamera() {
        Log.i(TAG, "captureImage: ");
        //Setting camera configuration 设置相机配置
        mCameraConfig = new CameraConfig()
                .getBuilder(this)
                .setCameraFacing(CameraFacing.FRONT_FACING_CAMERA)
                .setCameraResolution(CameraResolution.MEDIUM_RESOLUTION)
                .setImageFormat(CameraImageFormat.FORMAT_JPEG)
                .setCameraFocus(CameraFocus.NO_FOCUS)
//                .setImageRotation(CameraRotation.ROTATION_90)
                .build();

        //Start camera preview
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            startCamera(mCameraConfig);
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                //Take picture using the camera without preview.
                Log.i(TAG, "onViewClicked takePicture: ");
                if (SharedUtils.getIsCaptureOpen()) {
                    takePicture();
                }
            }
        }).start();
    }

    /**
     * 动态隐藏导航栏和状态栏
     */
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        Log.i(TAG, "onWindowFocusChanged: ");
        if (hasFocus && Build.VERSION.SDK_INT >= 19) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void onSuccessEvent(MessageEvent event) {
        EventBus.getDefault().removeStickyEvent(event);
        inStoreListAdapter.setDisableAll();
        String message = event.getMessage();
        apply = event.getApply();
        if (message.equals(EventConsts.EVENT_POST_SUCCESS)) {
            //提交成功
            Log.i(TAG, "onSuccessEvent 提交成功: ");
            scrapBtnOpenDoor.setVisibility(View.VISIBLE);
            scrapBtnOpenLock.setVisibility(View.VISIBLE);
            scrapBtnFinish.setVisibility(View.VISIBLE);
            scrapBtnOk.setVisibility(View.GONE);
            scrapBtnBack.setVisibility(View.GONE);
            isQueryStatus = true;
            //查询枪支状态
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Log.i(TAG, "run isQueryStatus : " + isQueryStatus);
                    while (isQueryStatus) {
                        List<InStoreListBean> selectedList = inStoreListAdapter.getSelectedList();
                        if (!selectedList.isEmpty()) {
                            Log.i(TAG, "run  selectedList size: " + selectedList.size());
                            for (int i = 0; i < selectedList.size(); i++) {
                                InStoreListBean inStoreListBean = selectedList.get(i);
                                int locationNo = inStoreListBean.getLocationNo();
                                SerialPortUtil.getInstance().checkStatus(locationNo);
                                Log.i(TAG, "run 查询: " + locationNo + "号枪锁状态");
                                try {
                                    Thread.sleep(200);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                if (!isQueryStatus) {
                                    break;
                                }
                            }
                        } else {
                            Log.i(TAG, "run selectedList is null: ");
                        }
                    }
                }
            }).start();
        } else {
            //提交失败
            Log.i(TAG, "onSuccessEvent 提交失败: ");
        }
    }

    private Map<Integer, Boolean> gunStatusMap = new HashMap<>();

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
                Log.i(TAG, "onSubscriber : " + address + "枪锁开启");
            } else if (status == 1) {//关闭
                Log.i(TAG, "onSubscriber : " + address + "枪锁关闭");
            } else if (status == 2) {//异常
                Log.i(TAG, "onSubscriber : " + address + "枪锁异常");
                ToastUtil.showShort(address + "号枪锁异常！");
            }
        } else if (category == 2) { //枪支在位状态
            if (status == 0) {//离位
                Log.i(TAG, "onSubscriber : " + address + "枪离位");
                gunStatusMap.put(address, false);
            } else if (status == 1) {//在位
                Log.i(TAG, "onSubscriber : " + address + "枪在位");
                gunStatusMap.put(address, true);
            }
        }
//        edtLockReceiveMsg.setText(message);
//        setStatusTxt(message);
    }

    /**
     * 获取任务信息
     *
     * @param scrapTaskId
     */
    private void getScrapTaskInfo(String scrapTaskId) {
        HttpClient.getInstance().getScrapTaskInfo(this, scrapTaskId, new HttpListener<String>() {
            @Override
            public void onSucceed(int what, Response<String> response) throws JSONException {
                Log.i(TAG, "onSucceed getScrapTaskInfo response: " + response.get());
                try {
                    if (!TextUtils.isEmpty(response.get())) {
                        List<InStoreListBean> inStoreListBeans = JSON.parseArray(response.get(), InStoreListBean.class);
                        if (!inStoreListBeans.isEmpty()) {
                            list.clear();
                            for (InStoreListBean inStoreListBean : inStoreListBeans) {
                                int locationNo = inStoreListBean.getLocationNo();
                                if (gunStatus.containsKey(locationNo) && gunStatus.get(locationNo).equals("in")) {
                                    list.add(inStoreListBean);
                                }
                            }
                            if (list.isEmpty()) {
                                showDialog("获取数据为空");
                            }
                            inStoreListAdapter.notifyDataSetChanged();
                            initPreNextBtn();

                        } else {
                            showDialog("获取数据为空");
                        }
                    } else {
                        showDialog("获取数据失败");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailed(int what, Response<String> response) {
                showDialog("网络连接错误，获取数据失败");
            }
        });
    }

    private Dialog dialog;

    private void showDialog(String msg) {
        if (dialog != null) {
            if (!dialog.isShowing()) {
                dialog.show();
            }
            DialogUtils.setTipText(msg);
            Log.i(TAG, "dialog is not null ");
        } else { //dialog为null
            dialog = DialogUtils.creatTipDialog(this, "提示", msg,
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //获取最新的数据 并刷新适配器
                            dialog.dismiss();
//                            finish();
                        }
                    });
            if (!dialog.isShowing()) {
                dialog.show();
            }
            Log.i(TAG, "dialog is null");
        }
    }

    /**
     * 获取枪柜数据
     */
    private void getCabData() {
        HttpClient.getInstance().getCabByMac(this, new HttpListener<String>() {
            @Override
            public void onSucceed(int what, Response<String> response) throws JSONException {
                try {
                    LogUtil.i(TAG, "onSucceed cab data response: " + response.get());
                    String respData = response.get();
                    if (!TextUtils.isEmpty(respData)) {
                        CabInfoBean cabInfoBean = JSON.parseObject(respData, CabInfoBean.class);
                        if (cabInfoBean != null) { //枪柜数据
                            List<SubCabBean> listLocation = cabInfoBean.getListLocation();
                            if (!listLocation.isEmpty()) {
                                Log.i(TAG, "获取枪柜数据成功");
                                for (SubCabBean subCabBean : listLocation) {
                                    int locationNo = subCabBean.getLocationNo();
                                    String gunState = subCabBean.getGunState();
                                    //枪支状态不为空并且为in
                                    if (!TextUtils.isEmpty(gunState) && gunState.equals("in")) {
                                        gunStatus.put(locationNo, gunState);
                                    }
                                }
                                getScrapTaskInfo(scrapTaskId);
                            } else {
                                //do sth
                                Log.i(TAG, "获取枪柜数据为空");
                            }
                        } else {
                            //枪柜数据获取为空
                            Log.i(TAG, "获取枪柜信息为空");
                        }
                    } else {
                        //应答数据为空
                        Log.i(TAG, "获取枪柜数据为空");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailed(int what, Response<String> response) {
                Log.i(TAG, "获取枪柜数据为空");
            }
        });
    }

    /**
     * 初始化翻页按钮
     */
    private void initPreNextBtn() {
        if (list.isEmpty()) {
            scrapTvCurPage.setText(index + 1 + "/1");
        } else {
            if (list.size() <= pageCount) {
                scrapBtnNextPage.setVisibility(View.INVISIBLE);
            } else {
                scrapBtnNextPage.setVisibility(View.VISIBLE);
            }
            scrapTvCurPage.setText(index + 1 + "/" + (int) Math.ceil((double) list.size() / pageCount));
        }
    }

    private void postScrapData(String jsonBody) {
        HttpClient.getInstance().postScrapData(this, jsonBody, new HttpListener<String>() {
            @Override
            public void onSucceed(int what, Response<String> response) throws JSONException {
                Log.i(TAG, "onSucceed postScrapData response: " + response.get());
                try {
                    if (!TextUtils.isEmpty(response.get())) {
                        if (response.get().equals("success")) {
//                            showDialogAndFinish("提交数据可能成功了");
                        } else {
//                            showDialog("提交数据可能失败了");
                        }
                    } else {
//                        showDialog("提交数据异常");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailed(int what, Response<String> response) {

            }
        });
    }

    private void prePager() {
        index--;
        inStoreListAdapter.setIndex(index);
        scrapTvCurPage.setText(index + 1 + "/" + (int) Math.ceil((double) list.size() / pageCount));
        //隐藏上一个或下一个按钮
        checkButton();
//        Log.i(TAG, "prePager index: " + index);
    }

    private void nexPager() {
        index++;
        inStoreListAdapter.setIndex(index);
        scrapTvCurPage.setText(index + 1 + "/" + (int) Math.ceil((double) list.size() / pageCount));
        //隐藏上一个或下一个按钮
        checkButton();
//        Log.i(TAG, "nexPager index: " + index);
    }

    private void checkButton() {
        if (index <= 0) {
            scrapBtnPrePage.setVisibility(View.INVISIBLE);
            scrapBtnNextPage.setVisibility(View.VISIBLE);
        } else if (list.size() - index * pageCount <= pageCount) {    //数据总数减每页数当小于每页可显示的数字时既是最后一页
            scrapBtnPrePage.setVisibility(View.VISIBLE);
            scrapBtnNextPage.setVisibility(View.INVISIBLE);
        } else {
            scrapBtnPrePage.setVisibility(View.VISIBLE);
            scrapBtnNextPage.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        isQueryStatus = false;
    }

    @Override
    protected void onStop() {
        super.onStop();
        isQueryStatus = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
        isQueryStatus = false;
        Constants.isIllegalOpenCabAlarm =false;
        //置为正在任务操作
        Constants.isExecuteTask =false;

    }

    @Override
    public void onImageCapture(@NonNull File imageFile) {
        Log.i(TAG, "onImageCapture: ");
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        Bitmap bitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath(), options);
        if (bitmap != null) {
            String base64Str = Utils.bitmapToBase64Str(bitmap);
            postCapturePic(base64Str);
        }
    }

    private void postCapturePic(String base64String) {
        Log.i(TAG, "postCapturePic: ");
        HttpClient.getInstance().postCapturePhoto(this, base64String,
                new HttpListener<String>() {
                    @Override
                    public void onSucceed(int what, Response<String> response) throws JSONException {
                        Log.i(TAG, "postCapturePic onSucceed  response: " + response.get());
                        SharedUtils.setIsCapturing(false);
                    }

                    @Override
                    public void onFailed(int what, Response<String> response) {
                        Log.i(TAG, "onFailed error: " + response.getException().getMessage());
                    }
                });
    }

    @Override
    public void onCameraError(int errorCode) {
        Log.i(TAG, "onCameraError errorCode: " + errorCode);
        try {
            switch (errorCode) {
                case CameraError.ERROR_CAMERA_OPEN_FAILED:
                    //Camera open failed. Probably because another application
                    //is using the camera
                    Toast.makeText(this, R.string.error_cannot_open, Toast.LENGTH_LONG).show();
//                    Toast.makeText(this, "Cannot open camera.", Toast.LENGTH_LONG).show();
                    ToastUtil.showShort(R.string.error_cannot_open);
                    Log.i(TAG, "onCameraError: ");
                    break;
                case CameraError.ERROR_IMAGE_WRITE_FAILED:
                    //Image write failed. Please check if you have provided WRITE_EXTERNAL_STORAGE permission
//                    Toast.makeText(this, R.string.error_cannot_write, Toast.LENGTH_LONG).show();
                    ToastUtil.showShort(R.string.error_cannot_write);
                    break;
                case CameraError.ERROR_CAMERA_PERMISSION_NOT_AVAILABLE:
                    //camera permission is not available
                    //Ask for the camera permission before initializing it.
//                    Toast.makeText(this, R.string.error_cannot_get_permission, Toast.LENGTH_LONG).show();
                    ToastUtil.showShort(R.string.error_cannot_get_permission);
                    break;
                case CameraError.ERROR_DOES_NOT_HAVE_OVERDRAW_PERMISSION:
                    //Display information dialog to the user with steps to grant "Draw over other app"
                    //permission for the app.
                    HiddenCameraUtils.openDrawOverPermissionSetting(this);
                    break;
                case CameraError.ERROR_DOES_NOT_HAVE_FRONT_CAMERA:
//                    Toast.makeText(this, R.string.error_not_having_camera, Toast.LENGTH_LONG).show();
                    ToastUtil.showShort(R.string.error_not_having_camera);
                    break;
            }
        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
        }
    }

    @OnClick({R.id.scrap_btn_pre_page, R.id.scrap_btn_next_page, R.id.scrap_btn_open_door, R.id.scrap_btn_open_lock, R.id.scrap_btn_ok, R.id.scrap_btn_finish, R.id.scrap_btn_back})
    public void onViewClicked(View view) {
        List<InStoreListBean> selectedList = inStoreListAdapter.getSelectedList();
        switch (view.getId()) {
            case R.id.scrap_btn_pre_page:
                prePager();
                break;
            case R.id.scrap_btn_next_page:
                nexPager();
                break;
            case R.id.scrap_btn_open_door:
                SharedUtils.setIsCheckStatus(false);//查询状态
                SerialPortUtil.getInstance().openLock(SharedUtils.getLeftCabNo());
                SoundPlayUtil.getInstance().play(R.raw.gun_cab_open);
                DBManager.getInstance().insertCommLog(ScrapListActivity.this,
                        apply, apply.getUserName() + "打开枪柜门");
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                SerialPortUtil.getInstance().openLED();//打开枪锁数码管led
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                SerialPortUtil.getInstance().openLED(SharedUtils.getPowerAddress());//打开枪锁数码管led
                SharedUtils.setIsCheckStatus(true);//查询状态
                break;
            case R.id.scrap_btn_open_lock:
                SharedUtils.setIsCheckStatus(false);//查询状态
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        List<InStoreListBean> checkedList = inStoreListAdapter.getSelectedList();
                        if (!checkedList.isEmpty()) {
                            for (InStoreListBean goTaskInfoBean : checkedList) {
                                int locationNo = goTaskInfoBean.getLocationNo();
                                SerialPortUtil.getInstance().openLock(locationNo);
                                try {
                                    Thread.sleep(300);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                DBManager.getInstance().insertCommLog(ScrapListActivity.this,
                                        apply, apply.getUserName() + "打开" + locationNo + "号枪锁");
                            }
                            SoundPlayUtil.getInstance().play(R.raw.gun_lock_open);
                            try {
                                Thread.sleep(200);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            SharedUtils.setIsCheckStatus(true);//查询状态
                        }
                    }
                }).start();
                break;
            case R.id.scrap_btn_ok:
                List<ScrapListBean> scrapListBeanList = new ArrayList<>();
                if (!selectedList.isEmpty()) {
                    Map<String, Object> params = new HashMap<>();
                    params.put("taskId", scrapTaskId);
                    for (InStoreListBean inStoreListBean : selectedList) {
                        ScrapListBean scrapListBean = new ScrapListBean();
                        scrapListBean.setId(inStoreListBean.getId());
                        scrapListBean.setGunCabinetLocationId(inStoreListBean.getGunCabinetLocationId());
                        scrapListBean.setGunNo(inStoreListBean.getGunNo());
                        scrapListBean.setInFinishState("");
                        scrapListBean.setOutFinishState("yes");
                        scrapListBean.setObjectId(inStoreListBean.getGunId());
                        scrapListBean.setObjectNumber(inStoreListBean.getObjectNumber());
                        scrapListBean.setState("scrap");
                        scrapListBeanList.add(scrapListBean);
                    }
                    params.put("gunScrapList", scrapListBeanList);
                    String jsonBody = JSON.toJSONString(params);
                    Log.i(TAG, "onViewClicked jsonBody: " + jsonBody);
//                    postScrapData(jsonBody);

                    Intent intent = new Intent(this, VerifyActivity.class);
                    intent.putExtra("activity", Constants.ACTIVITY_SCRAP);
                    intent.putExtra("data", jsonBody);
                    startActivity(intent);
                }
                break;
            case R.id.scrap_btn_finish:
                if (Constants.isDebug || Constants.isOldBoard) {
                    finish();
                    isQueryStatus = false;
                } else {
                    if (gunStatusMap.isEmpty()) {
                        SoundPlayUtil.getInstance().play(R.raw.gun_not_out);
                        ToastUtil.showShort("枪支未取出");
                        return;
                    }
                    if (!selectedList.isEmpty()) {
                        for (InStoreListBean inStoreListBean : selectedList) {
                            int locationNo = inStoreListBean.getLocationNo();
                            if (gunStatusMap.containsKey(locationNo)) {
                                //枪支状态  true 在位 false 不在位
                                boolean aBoolean = gunStatusMap.get(locationNo);
                                if (aBoolean) {
                                    Log.i(TAG, "onViewClicked  : " + locationNo + "号枪支未取枪");
                                    ToastUtil.showShort(locationNo + "号枪支未取枪");
                                    SoundPlayUtil.getInstance().play(R.raw.gun_not_out);
                                    return;
                                }
                            }
                        }
                    }
                    //停止查询枪支状态
                    isQueryStatus =false;

                    if (SharedUtils.getCabOpenStatus() == 1) {
                        ToastUtil.showShort("柜门未关闭");
                        SoundPlayUtil.getInstance().play(R.raw.cab_not_close);
                    } else {
                        finish();
                    }
                }
                break;
            case R.id.scrap_btn_back:
                finish();
                break;
        }

    }
}
