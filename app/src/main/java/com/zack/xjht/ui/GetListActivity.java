package com.zack.xjht.ui;

import android.Manifest;
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
import android.widget.RelativeLayout;
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
import com.zack.xjht.Utils.LogUtil;
import com.zack.xjht.Utils.SharedUtils;
import com.zack.xjht.Utils.SoundPlayUtil;
import com.zack.xjht.Utils.ToastUtil;
import com.zack.xjht.Utils.Utils;
import com.zack.xjht.adapter.TaskItemAdapter;
import com.zack.xjht.db.DBManager;
import com.zack.xjht.db.gen.CommonLogBeanDao;
import com.zack.xjht.entity.CabInfoBean;
import com.zack.xjht.entity.GoTaskInfoBean;
import com.zack.xjht.entity.PostGetDataBean;
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
import com.zack.xjht.ui.dialog.AlarmDialog;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class GetListActivity extends HiddenCameraActivity {
    private static final String TAG = "GetListActivity";
    private static final int REQ_CODE_CAMERA_PERMISSION = 133;

    @BindView(R.id.get_btn_back)
    Button getBtnBack;
    @BindView(R.id.get_ll_task_item_title)
    LinearLayout getLlTaskItemTitle;
    @BindView(R.id.get_gun_recycler_view)
    RecyclerView getGunRecyclerView;
    @BindView(R.id.get_tv_msg)
    TextView getTvMsg;
    @BindView(R.id.get_btn_pre_page)
    Button getBtnPrePage;
    @BindView(R.id.get_tv_cur_page)
    TextView getTvCurPage;
    @BindView(R.id.get_btn_next_page)
    Button getBtnNextPage;
    @BindView(R.id.get_btn_open_cab)
    Button getBtnOpenCab;
    @BindView(R.id.get_btn_open_lock)
    Button getBtnOpenLock;
    @BindView(R.id.get_btn_confirm)
    Button getBtnConfirm;
    @BindView(R.id.get_btn_finish)
    Button getBtnFinish;
    @BindView(R.id.get_gun_ll_nav)
    LinearLayout getGunLlNav;
    @BindView(R.id.get_rl_bottom_view)
    RelativeLayout getRlBottomView;
    @BindView(R.id.get_list_tv_gun_no)
    TextView getListTvGunNo;

    private int index = 0;
    private int pageCount = 10;
    private TaskItemAdapter taskItemAdapter;
    private UserBean apply, approve;
    private boolean isQueryStatus = true;
    private CommonLogBeanDao commonLogBeanDao;
    private List<GoTaskInfoBean> taskInfoList = new ArrayList<>();
    private String getTaskId;
    private int policeId;
    private Map<Integer, String> gunStatus = new HashMap<>();
    private Map<Integer, Integer> ammoNum = new HashMap<>();
    private CameraConfig mCameraConfig;
    private long startTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_list);
        ButterKnife.bind(this);
        startTime = System.currentTimeMillis();
        initView();
        if (SharedUtils.getIsCaptureOpen()) {
            configCamera();
        }
        Constants.isCheckGunStatus =false; //关闭查询枪支状态
        Constants.isIllegalGetGunAlarm =true; //关闭非正常领取枪支报警
        Constants.isIllegalOpenCabAlarm =true; //关闭非正常开启柜门报警

        //置为正在任务操作
        Constants.isExecuteTask =true;
    }

    private void initView() {
        commonLogBeanDao = DBManager.getInstance().getCommonLogBeanDao();

        getBtnPrePage.setVisibility(View.INVISIBLE);
        getBtnNextPage.setVisibility(View.INVISIBLE);
        getTvMsg.setVisibility(View.VISIBLE);
        getGunRecyclerView.setLayoutManager(new LinearLayoutManager(this,
                LinearLayout.VERTICAL, false));

        taskItemAdapter = new TaskItemAdapter(taskInfoList, index, pageCount, "get");
        getGunRecyclerView.setAdapter(taskItemAdapter);

        getTaskId = getIntent().getStringExtra("getTaskId");
        policeId = getIntent().getIntExtra("policeId", 0);
        Log.i(TAG, "initView getTaskId: " + getTaskId + " policeId:" + policeId);
        if (!TextUtils.isEmpty(getTaskId)) {
            if (Utils.isNetworkAvailable()) {
                getCabData();
            }
        }
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSubscriber(PowerStatusEvent event) {
        String message = event.getMessage();
        Log.i(TAG, "onSubscriber: "+message);
        switch (message) {
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
                            String gunCabinetType = cabInfoBean.getGunCabinetType();
                            if (gunCabinetType.equals(Constants.TYPE_AMMO_CAB)) {
                                getListTvGunNo.setVisibility(View.GONE);
                            } else {
                                getListTvGunNo.setVisibility(View.VISIBLE);
                            }
                            List<SubCabBean> listLocation = cabInfoBean.getListLocation();
                            if (!listLocation.isEmpty()) {
                                Log.i(TAG, "获取枪柜数据成功");
                                for (SubCabBean subCabBean : listLocation) {
                                    String locationType = subCabBean.getLocationType();
                                    int locationNo = subCabBean.getLocationNo();
                                    String gunState = subCabBean.getGunState();
                                    int objectNumber = subCabBean.getObjectNumber();
                                    if(!TextUtils.isEmpty(locationType)){
                                        if (locationType.equals(Constants.TYPE_AMMO)) { //子弹
                                            //保存位置和子弹数量
                                            if (objectNumber > 0) {
                                                ammoNum.put(locationNo, objectNumber);
                                            }
                                        } else if (locationType.equals(Constants.TYPE_LONG_GUN) ||
                                                locationType.equals(Constants.TYPE_SHORT_GUN)) { //长枪或短枪
                                            //枪支状态数据不为空并且为in
                                            if (!TextUtils.isEmpty(gunState) && gunState.equals("in")) {//
                                                gunStatus.put(locationNo, gunState);
                                            }
                                        }
                                    }
                                }
                                getTaskInfo(getTaskId);
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
                    Log.i(TAG, "获取枪柜数据为空");
                }
            }

            @Override
            public void onFailed(int what, Response<String> response) {
                Log.i(TAG, "获取枪柜数据为空");
            }
        });
    }

    /**
     * 获取任务信息
     *
     * @param taskId
     */
    private void getTaskInfo(String taskId) {
        HttpClient.getInstance().getPoliceListInfo(this, taskId, new HttpListener<String>() {
            @Override
            public void onSucceed(int what, Response<String> response) throws JSONException {
                LogUtil.i(TAG, "onSucceed getTaskInfo response: " + response.get());
                try {
                    if (!TextUtils.isEmpty(response.get())) {
                        List<GoTaskInfoBean> goTaskInfoBeans = JSON.parseArray(response.get(), GoTaskInfoBean.class);
                        if (!goTaskInfoBeans.isEmpty()) {
                            taskInfoList.clear();
                            for (GoTaskInfoBean goTaskInfoBean : goTaskInfoBeans) {
                                String outFinishState = goTaskInfoBean.getOutFinishState();
                                String inFinishState = goTaskInfoBean.getInFinishState();
                                int locationNo = goTaskInfoBean.getLocationNo();
                                String locationType = goTaskInfoBean.getLocationType();
                                int objectNumber = goTaskInfoBean.getObjectNumber();
                                if (outFinishState.equals("no")) {//未领取
                                    if (locationType.equals(Constants.TYPE_AMMO)) { //弹药
                                        if (!ammoNum.isEmpty() && ammoNum.containsKey(locationNo)) {
                                            int number = ammoNum.get(locationNo);
                                            Log.i(TAG, "onSucceed locationNo: " + locationNo + " number:" + number);
                                            if (number >= objectNumber) {
                                                taskInfoList.add(goTaskInfoBean);
                                            } else {
                                                ToastUtil.showShort("库存数量不足");
                                            }
                                        }
                                    } else { //枪支
                                        if (!gunStatus.isEmpty() && gunStatus.containsKey(locationNo)) {
                                            String status = gunStatus.get(locationNo);
                                            if (!TextUtils.isEmpty(status)) {
                                                if (status.equals("in")) {
                                                    taskInfoList.add(goTaskInfoBean);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            if (taskInfoList.isEmpty()) {
                                getTvMsg.setText("没有可领取的枪弹数据");
                            } else {
                                getTvMsg.setVisibility(View.INVISIBLE);
                            }
                            taskItemAdapter.setList(taskInfoList);
                            taskItemAdapter.notifyDataSetChanged();
                            initPreNextBtn();
                            Log.i(TAG, "onSucceed endTime: " + (System.currentTimeMillis() - startTime));
                        } else {
                            getTvMsg.setText("获取任务清单为空！");
                        }
                    } else {
                        getTvMsg.setText("获取数据为空！");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    getTvMsg.setText("获取数据出现错误！！");
                }
            }

            @Override
            public void onFailed(int what, Response<String> response) {
                getTvMsg.setText("网络错误 获取数据失败！");
            }
        });
    }

    private void initPreNextBtn() {
        if (taskInfoList.isEmpty()) {
            getTvCurPage.setText(index + 1 + "/1");
        } else {
            if (taskInfoList.size() <= pageCount) {
                getBtnNextPage.setVisibility(View.INVISIBLE);
            } else {
                getBtnNextPage.setVisibility(View.VISIBLE);
            }
            getTvCurPage.setText(index + 1 + "/" + (int) Math.ceil((double) taskInfoList.size() / pageCount));
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void onPostSuccessEvent(MessageEvent event) {
        EventBus.getDefault().removeStickyEvent(event);
        taskItemAdapter.setDisableAll();
        String message = event.getMessage();
        apply = event.getApply();
        approve = event.getApprove();
        Log.i(TAG, "onPostSuccessEvent message: " + message + "  申请人:" + apply.getUserName()
                + " 审批人:" + approve.getUserName());
        if (message.equals(EventConsts.EVENT_POST_SUCCESS)) {
            //提交成功
            Log.i(TAG, "onPostSuccessEvent 提交成功: ");
            getBtnOpenCab.setVisibility(View.VISIBLE);
            getBtnOpenLock.setVisibility(View.VISIBLE);
            String cabType = SharedUtils.getCabType();
            if (cabType.equals(Constants.TYPE_AMMO_CAB)) {
                //领取弹药和归还弹药
                getBtnOpenCab.setText("打开弹柜");
                getBtnOpenLock.setText("打开弹仓");
            }
            getBtnFinish.setVisibility(View.VISIBLE);
            getBtnConfirm.setVisibility(View.GONE);
            getBtnBack.setVisibility(View.GONE);
            isQueryStatus = true;
            //查询枪支状态
            if (!cabType.equals(Constants.TYPE_AMMO_CAB)) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Log.i(TAG, "run isQueryStatus : " + isQueryStatus);
                        while (isQueryStatus) {
                            List<GoTaskInfoBean> selectedList = taskItemAdapter.getCheckedList();
                            if (!selectedList.isEmpty()) {
                                Log.i(TAG, "run  selectedList size: " + selectedList.size());
                                for (int i = 0; i < selectedList.size(); i++) {
                                    GoTaskInfoBean goTaskInfoBean = selectedList.get(i);
                                    int locationNo = goTaskInfoBean.getLocationNo();
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
            }
        } else if (message.equals(EventConsts.EVENT_POST_FAILURE)) {
            //提交失败
            Log.i(TAG, "onPostSuccessEvent 提交失败: ");
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

    @OnClick({R.id.get_btn_pre_page, R.id.get_btn_next_page, R.id.get_btn_open_cab,
            R.id.get_btn_open_lock, R.id.get_btn_confirm, R.id.get_btn_finish,
            R.id.get_btn_back})
    public void onViewClicked(View view) {
        final String cabType = SharedUtils.getCabType();
        final List<GoTaskInfoBean> checkedList = taskItemAdapter.getCheckedList();
        switch (view.getId()) {
            case R.id.get_btn_open_cab:

                SharedUtils.setIsCheckStatus(false);//不查询状态
                if (cabType.equals(Constants.TYPE_AMMO_CAB)) {
                    SerialPortUtil.getInstance().openLock(SharedUtils.getLeftCabNo());
                    SoundPlayUtil.getInstance().play(R.raw.bullet_cab_open);
                    DBManager.getInstance().insertCommLog(GetListActivity.this, apply, apply.getUserName() + "打开弹柜门");
                } else { //枪柜
                    SerialPortUtil.getInstance().openLock(SharedUtils.getLeftCabNo());
                    SoundPlayUtil.getInstance().play(R.raw.gun_cab_open);
                    DBManager.getInstance().insertCommLog(GetListActivity.this, apply, apply.getUserName() + "打开枪柜门");
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
            case R.id.get_btn_open_lock:
                SharedUtils.setIsCheckStatus(false);//不查询状态
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        synchronized (checkedList) {
                            if (!checkedList.isEmpty()) {
                                for (GoTaskInfoBean goTaskInfoBean : checkedList) {
                                    int locationNo = goTaskInfoBean.getLocationNo();
                                    SerialPortUtil.getInstance().openLock(locationNo);
                                    try {
                                        Thread.sleep(300);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                    if (cabType.equals(Constants.TYPE_AMMO_CAB)) {
                                        DBManager.getInstance().insertCommLog(GetListActivity.this, apply,
                                                apply.getUserName() + "打开" + locationNo + "号弹仓");
                                    } else {
                                        DBManager.getInstance().insertCommLog(GetListActivity.this, apply,
                                                apply.getUserName() + "打开" + locationNo + "号枪锁");
                                    }
                                }
                                if (cabType.equals(Constants.TYPE_AMMO_CAB)) {
                                    //领取弹药和归还弹药
                                    SoundPlayUtil.getInstance().play(R.raw.bullet_subcab_open);
                                } else {
                                    SoundPlayUtil.getInstance().play(R.raw.gun_lock_open);
                                }
                                SharedUtils.setIsCheckStatus(true);//查询状态
                            }
                        }
                    }
                }).start();
                break;
            case R.id.get_btn_finish://结束并关闭
                if (Constants.isDebug || Constants.isOldBoard) {
                    finish();
                    isQueryStatus = false;
                } else {
                    if (!SharedUtils.getCabType().equals(Constants.TYPE_AMMO_CAB)) {
                        if (gunStatusMap.isEmpty()) {
                            ToastUtil.showShort("枪支未取出");
                            SoundPlayUtil.getInstance().play(R.raw.gun_not_out);
                            return;
                        }
                        if (!checkedList.isEmpty()) {
                            for (GoTaskInfoBean goTaskInfoBean : checkedList) {
                                int locationNo = goTaskInfoBean.getLocationNo();
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
                    }
                    //停止查询枪支状态
                    isQueryStatus =false;
                    if (SharedUtils.getCabOpenStatus() == 1) {
                        ToastUtil.showShort("柜门未关闭");
                    } else {
                        finish();
                    }
                }
                break;
            case R.id.get_btn_confirm: //确认领枪
                if (!checkedList.isEmpty()) {
                    PostGetDataBean postGetDataBean = new PostGetDataBean();
                    postGetDataBean.setOperation("out");
                    postGetDataBean.setPoliceTaskId(getTaskId);

                    List<PostGetDataBean.ListPoliceTaskListBean> policeTaskList = new ArrayList<>();
                    for (GoTaskInfoBean goTaskInfo : checkedList) {
                        PostGetDataBean.ListPoliceTaskListBean policeTaskListBean = new PostGetDataBean.ListPoliceTaskListBean();
                        String gunCabinetLocationId = goTaskInfo.getGunCabinetLocationId();
                        policeTaskListBean.setGunCabinetLocationId(gunCabinetLocationId);//
                        policeTaskListBean.setId(goTaskInfo.getId());//
                        policeTaskListBean.setLocationType(goTaskInfo.getLocationType());//
                        policeTaskListBean.setObjectId(goTaskInfo.getObjectId());//
                        policeTaskListBean.setObjectNumber(goTaskInfo.getObjectNumber());//
//                        policeTaskListBean.setReturnNumber(goTaskInfo.getReturnNumber());//
                        policeTaskListBean.setAmmunitionType(goTaskInfo.getAmmunitionType());
                        policeTaskList.add(policeTaskListBean);
                    }
                    postGetDataBean.setListPoliceTaskList(policeTaskList);
                    String jsonBody = JSON.toJSONString(postGetDataBean);
                    LogUtil.i(TAG, "onViewClicked post get data jsonBody: " + jsonBody);
//                    postGetData(jsonBody);

                    //验证两个管理人员身份 验证成功后打开枪柜枪锁和数据提交
                    Intent intent = new Intent(this, VerifyActivity.class);
                    intent.putExtra("activity", Constants.ACTIVITY_GET);
                    intent.putExtra("data", jsonBody);
//                    intent.putExtra("taskId", getTaskId);
                    intent.putExtra("policeId", policeId);
                    startActivity(intent);
                }
                break;
            case R.id.get_btn_pre_page: //上一页
                prePager();
                break;
            case R.id.get_btn_next_page: //下一页
                nexPager();
                break;
            case R.id.get_btn_back:
                finish();
                break;
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

    private void prePager() {
        index--;
        taskItemAdapter.setIndex(index);
        getTvCurPage.setText(index + 1 + "/" + (int) Math.ceil((double) taskInfoList.size() / pageCount));
        //隐藏上一个或下一个按钮
        checkButton();
//        Log.i(TAG, "prePager index: " + index);
    }

    private void nexPager() {
        index++;
        taskItemAdapter.setIndex(index);
        getTvCurPage.setText(index + 1 + "/" + (int) Math.ceil((double) taskInfoList.size() / pageCount));
        //隐藏上一个或下一个按钮
        checkButton();
//        Log.i(TAG, "nexPager index: " + index);
    }

    private void checkButton() {
        if (index <= 0) {
            getBtnPrePage.setVisibility(View.INVISIBLE);
            getBtnNextPage.setVisibility(View.VISIBLE);
        } else if (taskInfoList.size() - index * pageCount <= pageCount) {    //数据总数减每页数当小于每页可显示的数字时既是最后一页
            getBtnPrePage.setVisibility(View.VISIBLE);
            getBtnNextPage.setVisibility(View.INVISIBLE);
        } else {
            getBtnNextPage.setVisibility(View.VISIBLE);
            getBtnPrePage.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
        isQueryStatus = false;
        Constants.isCheckGunStatus =true;
        Constants.isIllegalGetGunAlarm =false;
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
                    Log.i(TAG, "onCameraError: ");
                    break;
                case CameraError.ERROR_IMAGE_WRITE_FAILED:
                    //Image write failed. Please check if you have provided WRITE_EXTERNAL_STORAGE permission
                    Toast.makeText(this, R.string.error_cannot_write, Toast.LENGTH_LONG).show();
                    break;
                case CameraError.ERROR_CAMERA_PERMISSION_NOT_AVAILABLE:
                    //camera permission is not available
                    //Ask for the camera permission before initializing it.
                    Toast.makeText(this, R.string.error_cannot_get_permission, Toast.LENGTH_LONG).show();
                    break;
                case CameraError.ERROR_DOES_NOT_HAVE_OVERDRAW_PERMISSION:
                    //Display information dialog to the user with steps to grant "Draw over other app"
                    //permission for the app.
                    HiddenCameraUtils.openDrawOverPermissionSetting(this);
                    break;
                case CameraError.ERROR_DOES_NOT_HAVE_FRONT_CAMERA:
                    Toast.makeText(this, R.string.error_not_having_camera, Toast.LENGTH_LONG).show();
                    break;
            }
        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
        }
    }

}
