package com.zack.xjht.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import com.alibaba.fastjson.serializer.SerializerFeature;
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
import com.zack.xjht.Utils.SharedUtils;
import com.zack.xjht.Utils.SoundPlayUtil;
import com.zack.xjht.Utils.ToastUtil;
import com.zack.xjht.Utils.Utils;
import com.zack.xjht.adapter.InStoreListAdapter;
import com.zack.xjht.db.DBManager;
import com.zack.xjht.entity.InStoreBean;
import com.zack.xjht.entity.InStoreListBean;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class InStoreListActivity extends HiddenCameraActivity {
    private static final String TAG = "InStoreListActivity";
    private static final int REQ_CODE_CAMERA_PERMISSION = 133;
    @BindView(R.id.store_in_btn_back)
    Button storeInBtnBack;
    @BindView(R.id.instore_task_list_tv_tittle)
    TextView instoreTaskListTvTittle;
    @BindView(R.id.instore_task_list_tv_subcab_no)
    TextView instoreTaskListTvSubcabNo;
    @BindView(R.id.instore_task_list_tv_subcab_type)
    TextView instoreTaskListTvSubcabType;
    @BindView(R.id.instore_task_list_tv_gun_type)
    TextView instoreTaskListTvGunType;
    @BindView(R.id.instore_task_list_tv_gun_no)
    TextView instoreTaskListTvGunNo;
    @BindView(R.id.instore_task_list_tv_ammo_num)
    TextView instoreTaskListTvAmmoNum;
    @BindView(R.id.instore_task_list_tv_operate)
    TextView instoreTaskListTvOperate;
    @BindView(R.id.instore_task_list_ll_title)
    LinearLayout instoreTaskListLlTitle;
    @BindView(R.id.instore_task_rv_list)
    RecyclerView instoreTaskRvList;
    @BindView(R.id.instore_task_tv_msg)
    TextView instoreTaskTvMsg;
    @BindView(R.id.in_store_btn_pre_page)
    Button inStoreBtnPrePage;
    @BindView(R.id.in_store_tv_cur_page)
    TextView inStoreTvCurPage;
    @BindView(R.id.in_store_btn_next_page)
    Button inStoreBtnNextPage;
    @BindView(R.id.store_in_btn_open_door)
    Button storeInBtnOpenDoor;
    @BindView(R.id.store_in_btn_open_lock)
    Button storeInBtnOpenLock;
    @BindView(R.id.store_in_btn_ok)
    Button storeInBtnOk;
    @BindView(R.id.store_in_btn_finish)
    Button storeInBtnFinish;

    private int index = 0;
    private int pageCount = 8;
    private List<InStoreListBean> list;
    private InStoreListAdapter inStoreListAdapter;
    private String instoreTaskId;
    private UserBean apply;
    private CameraConfig mCameraConfig;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instore_list);
        ButterKnife.bind(this);

        initView();

        if (SharedUtils.getCabType().equals(Constants.TYPE_AMMO_CAB)) {
            instoreTaskListTvGunNo.setVisibility(View.GONE);
        }
        if (SharedUtils.getIsCaptureOpen()) {
            configCamera();
        }

        Constants.isIllegalOpenCabAlarm = true;
        //置为正在任务操作
        Constants.isExecuteTask = true;
    }

    private void initView() {
        inStoreBtnPrePage.setVisibility(View.INVISIBLE);
        inStoreBtnNextPage.setVisibility(View.INVISIBLE);

        if (SharedUtils.getCabType().equals(Constants.TYPE_AMMO_CAB)) {
            storeInBtnOpenDoor.setText("打开弹柜");
            storeInBtnOpenLock.setText("打开弹仓");
        } else { //枪柜类型
            storeInBtnOpenDoor.setText("打开枪柜");
            storeInBtnOpenLock.setText("打开枪锁");
        }

        list = new ArrayList<>();

        LinearLayoutManager llm = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL,
                false);
        instoreTaskRvList.setLayoutManager(llm);
        inStoreListAdapter = new InStoreListAdapter(list, index, pageCount);
        instoreTaskRvList.setAdapter(inStoreListAdapter);

        instoreTaskId = getIntent().getStringExtra("instoreTaskId");
        if (!TextUtils.isEmpty(instoreTaskId)) {
            Log.i(TAG, "onCreate  instoreTaskId: " + instoreTaskId);
            if (Utils.isNetworkAvailable()) {
                getInStoreTaskInfoList(instoreTaskId);
            } else {
                ToastUtil.showShort("网络断开，无法获取数据");
            }
        } else {
            ToastUtil.showShort("获取任务id为空");
        }
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
                if (SharedUtils.getPowerStatus() == 1) {
                    SharedUtils.savePowerStatus(0);
                }
                break;
            case EventConsts.EVENT_BACKUP_POWER:
                if (SharedUtils.getPowerStatus() == 0) {
                    SharedUtils.savePowerStatus(1);
                }
                break;
            case EventConsts.EVENT_BACKUP_CLOSE: //钥匙开启正常
                if (SharedUtils.getBackupOpenStatus() == 1) {
                    SharedUtils.saveBackupOpenStatus(0);
                }
                break;
            case EventConsts.EVENT_BACKUP_OPEN: //钥匙开启
                if (SharedUtils.getBackupOpenStatus() == 0) {
                    SharedUtils.saveBackupOpenStatus(1);
                }
                break;
            case EventConsts.EVENT_CAB_CLOSE://枪柜门关闭
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
                if(SharedUtils.getVibrationStatus()==1){
                    SharedUtils.saveVibrationStatus(0);
                }
                break;
            case EventConsts.EVENT_VIBRATION_ABNORMAL://震动异常
                if(SharedUtils.getVibrationStatus()==0){
                    SharedUtils.saveVibrationStatus(1);
                }
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

        //Check for the camera permission for the runtime
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            //Start camera preview
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
            storeInBtnOpenDoor.setVisibility(View.VISIBLE);
            storeInBtnOpenLock.setVisibility(View.VISIBLE);
            storeInBtnFinish.setVisibility(View.VISIBLE);
            storeInBtnBack.setVisibility(View.GONE);
            storeInBtnOk.setVisibility(View.GONE);
            isQueryStatus = true;
            //查询枪支状态
            if (!SharedUtils.getCabType().equals(Constants.TYPE_AMMO_CAB)) {
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
                                        Thread.sleep(150);
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
            }
        } else {
            //提交失败
            Log.i(TAG, "onSuccessEvent 提交失败: ");
        }
    }

    private Map<Integer, Boolean> gunStatus = new HashMap<>();

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
//                gunInfoAdapter.setGunState(address, status);
                gunStatus.put(address, false);
            } else if (status == 1) {//在位
                Log.i(TAG, "onSubscriber : " + address + "枪在位");
//                gunInfoAdapter.setGunState(address, status);
                gunStatus.put(address, true);
            }
        }
//        edtLockReceiveMsg.setText(message);
//        setStatusTxt(message);
    }

    private void getInStoreTaskInfoList(String instoreTaskId) {
        HttpClient.getInstance().getInStoreTaskInfo(this, instoreTaskId, new HttpListener<String>() {
            @Override
            public void onSucceed(int what, Response<String> response) throws JSONException {
                try {
                    Log.i(TAG, "onSucceed getInStoreTaskInfoList response: " + response.get());
                    if (!TextUtils.isEmpty(response.get())) {
                        List<InStoreListBean> inStoreListBeans = JSON.parseArray(response.get(), InStoreListBean.class);
                        if (!inStoreListBeans.isEmpty()) {
                            list.clear();
                            for (InStoreListBean inStoreListBean : inStoreListBeans) {
                                String inFinishState = inStoreListBean.getInFinishState();
                                if (!TextUtils.isEmpty(inFinishState) && inFinishState.equals("no")) {
                                    list.add(inStoreListBean);
                                }
                            }
                            if (list.isEmpty()) {
                                instoreTaskRvList.setVisibility(View.INVISIBLE);
                                instoreTaskTvMsg.setVisibility(View.VISIBLE);
                                instoreTaskTvMsg.setText("没有可入库数据");
                                return;
                            }
                            Collections.sort(list);
                            inStoreListAdapter.notifyDataSetChanged();
                            initPreNextBtn();
                        } else {
                            //                        showDialog("获取入库数据为空");
                            instoreTaskRvList.setVisibility(View.INVISIBLE);
                            instoreTaskTvMsg.setVisibility(View.VISIBLE);
                            instoreTaskTvMsg.setText("获取入库列表为空");
                        }
                    } else {
                        //                    showDialog("获取任务清单失败!");
                        instoreTaskRvList.setVisibility(View.INVISIBLE);
                        instoreTaskTvMsg.setVisibility(View.VISIBLE);
                        instoreTaskTvMsg.setText("获取数据为空");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailed(int what, Response<String> response) {
                instoreTaskTvMsg.setText("网络请求发生错误，获取数据失败");

            }
        });
    }

    private void initPreNextBtn() {
        if (list.isEmpty()) {
            inStoreTvCurPage.setText(index + 1 + "/1");
        } else {
            if (list.size() <= pageCount) {
                inStoreBtnNextPage.setVisibility(View.INVISIBLE);
            } else {
                inStoreBtnNextPage.setVisibility(View.VISIBLE);
            }
            inStoreTvCurPage.setText(index + 1 + "/" + (int) Math.ceil((double) list.size() / pageCount));
        }
    }

    private Boolean isQueryStatus = true;

    @Override
    public void onResume() {
        super.onResume();
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

    @OnClick({R.id.in_store_btn_pre_page, R.id.in_store_btn_next_page, R.id.store_in_btn_open_door,
            R.id.store_in_btn_open_lock, R.id.store_in_btn_ok, R.id.store_in_btn_finish,
            R.id.store_in_btn_back})
    public void onViewClicked(View view) {
        final String cabType = SharedUtils.getCabType();
        final List<InStoreListBean> selectedList = inStoreListAdapter.getSelectedList();
        switch (view.getId()) {
            case R.id.in_store_btn_pre_page: //上一页
                prePager();
                break;
            case R.id.in_store_btn_next_page://下一页
                nexPager();
                break;
            case R.id.store_in_btn_open_door://打开柜门
                SharedUtils.setIsCheckStatus(false);//查询状态
                SerialPortUtil.getInstance().openLock(SharedUtils.getLeftCabNo());
                if (cabType.equals(Constants.TYPE_AMMO_CAB)) { //弹柜
                    SoundPlayUtil.getInstance().play(R.raw.bullet_cab_open);
                    DBManager.getInstance().insertCommLog(this, apply, apply.getUserName() + "打开弹柜门");
                } else {//枪柜
                    SoundPlayUtil.getInstance().play(R.raw.gun_cab_open);
                    DBManager.getInstance().insertCommLog(this, apply, apply.getUserName() + "打开枪柜门");
                }
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
                SerialPortUtil.getInstance().openLED(254);//打开枪锁数码管led
                SharedUtils.setIsCheckStatus(true);//查询状态
                break;
            case R.id.store_in_btn_open_lock://打开枪锁
                SharedUtils.setIsCheckStatus(false);//查询状态
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (!selectedList.isEmpty()) {
                            for (InStoreListBean instoreBean : selectedList) {
                                if (instoreBean != null) {
                                    final int locationNo = instoreBean.getLocationNo();
                                    SerialPortUtil.getInstance().openLock(locationNo);
                                    try {
                                        Thread.sleep(300);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                    if (SharedUtils.getCabType().equals(Constants.TYPE_AMMO_CAB)) { //弹柜
                                        DBManager.getInstance().insertCommLog(InStoreListActivity.this, apply,
                                                apply.getUserName() + "打开" + locationNo + "号弹仓");
                                    } else {//枪柜
                                        DBManager.getInstance().insertCommLog(InStoreListActivity.this, apply,
                                                apply.getUserName() + "打开" + locationNo + "号枪锁");
                                    }

                                }
                            }
                            if (cabType.equals(Constants.TYPE_AMMO_CAB)) {
                                //领取弹药和归还弹药
                                SoundPlayUtil.getInstance().play(R.raw.bullet_subcab_open);
                            } else {
                                SoundPlayUtil.getInstance().play(R.raw.gun_lock_open);
                            }
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
            case R.id.store_in_btn_ok://确认入库
                if (!selectedList.isEmpty()) {
                    List<InStoreBean> inStoreBeanList = new ArrayList<>();
                    Map<String, Object> params = new HashMap<>();
                    params.put("gunStorageId", instoreTaskId);
                    for (InStoreListBean inStoreListBean : selectedList) {
                        InStoreBean inStoreBean = new InStoreBean();
                        inStoreBean.setId(inStoreListBean.getId());
                        inStoreBean.setGunCabinetLocationId(inStoreListBean.getGunCabinetLocationId());
                        inStoreBean.setLocationType(inStoreListBean.getLocationType());
                        inStoreBean.setObjectType(inStoreListBean.getObjectType());
                        inStoreBean.setObjectNumber(inStoreListBean.getObjectNumber());
                        inStoreBean.setGunNo(inStoreListBean.getGunNo());
                        inStoreBean.setGunMac(inStoreListBean.getGunMac());
                        inStoreBeanList.add(inStoreBean);
                    }
                    params.put("listGunStorageList", inStoreBeanList);
                    String jsonBody = JSON.toJSONString(params, SerializerFeature.WriteMapNullValue);
                    Log.i(TAG, "onViewClicked post instore jsonBody: " + jsonBody);
//                    postInstoreData(jsonBody);

                    Intent intent = new Intent(this, VerifyActivity.class);
                    intent.putExtra("activity", Constants.ACTIVITY_IN_STORE);
                    intent.putExtra("data", jsonBody);
                    startActivity(intent);
                }
                break;
            case R.id.store_in_btn_finish:
                if (Constants.isDebug || Constants.isOldBoard) {
                    finish();
                    isQueryStatus = false;
                } else {
                    if (!SharedUtils.getCabType().equals(Constants.TYPE_AMMO_CAB)) {
                        if (gunStatus.isEmpty()) {
                            ToastUtil.showShort("枪支未放置在位");
                            SoundPlayUtil.getInstance().play(R.raw.gun_not_in_position);
                            return;
                        }
                        if (!selectedList.isEmpty()) {
                            for (InStoreListBean inStoreListBean : selectedList) {
                                int locationNo = inStoreListBean.getLocationNo();
                                if (gunStatus.containsKey(locationNo)) {
                                    //枪支状态  true 在位 false 不在位
                                    boolean aBoolean = gunStatus.get(locationNo);
                                    if (!aBoolean) {
                                        Log.i(TAG, "onViewClicked  : " + locationNo + "号枪支未放置在位");
                                        ToastUtil.showShort(locationNo + "号枪支未放置在位");
                                        SoundPlayUtil.getInstance().play(R.raw.gun_not_in_position);
                                        return;
                                    }
                                }
                            }
                        }
                    }
                    //停止查询枪支状态
                    isQueryStatus = false;
                    if (SharedUtils.getCabOpenStatus() == 1) {
                        ToastUtil.showShort("柜门未关闭");
                        SoundPlayUtil.getInstance().play(R.raw.cab_not_close);
                    } else {
                        finish();
                    }
                }
                break;
            case R.id.store_in_btn_back:
                finish();
                break;
        }
    }

    private void postInstoreData(String jsonBody) {
        HttpClient.getInstance().postInstoreData(this, jsonBody, new HttpListener<String>() {
            @Override
            public void onSucceed(int what, Response<String> response) throws JSONException {
                try {
                    Log.i(TAG, "onSucceed response: " + response.get());
                    if (!TextUtils.isEmpty(response.get())) {
                        if (response.get().equals("success")) {
//                            showDialogAndFinish("提交成功");
                        } else {
//                            showDialog("提交失败了");
                        }
                    } else {
//                        showDialog("糟糕！提交没有数据返回");
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
        inStoreTvCurPage.setText(index + 1 + "/" + (int) Math.ceil((double) list.size() / pageCount));
        //隐藏上一个或下一个按钮
        checkButton();
//        Log.i(TAG, "prePager index: " + index);
    }

    private void nexPager() {
        index++;
        inStoreListAdapter.setIndex(index);
        inStoreTvCurPage.setText(index + 1 + "/" + (int) Math.ceil((double) list.size() / pageCount));
        //隐藏上一个或下一个按钮
        checkButton();
//        Log.i(TAG, "nexPager index: " + index);
    }

    private void checkButton() {
        if (index <= 0) {
            inStoreBtnPrePage.setVisibility(View.INVISIBLE);
            inStoreBtnNextPage.setVisibility(View.VISIBLE);
        } else if (list.size() - index * pageCount <= pageCount) {    //数据总数减每页数当小于每页可显示的数字时既是最后一页
            inStoreBtnPrePage.setVisibility(View.VISIBLE);
            inStoreBtnNextPage.setVisibility(View.INVISIBLE);
        } else {
            inStoreBtnNextPage.setVisibility(View.VISIBLE);
            inStoreBtnPrePage.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
        isQueryStatus = false;
        Constants.isIllegalOpenCabAlarm = false;
        //置为正在任务操作
        Constants.isExecuteTask = false;
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
}
