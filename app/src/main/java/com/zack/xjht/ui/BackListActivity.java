package com.zack.xjht.ui;

import android.Manifest;
import android.annotation.NonNull;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
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
import com.zack.xjht.entity.GoTaskInfoBean;
import com.zack.xjht.entity.PostGetDataBean;
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

public class BackListActivity extends HiddenCameraActivity {
    private static final String TAG = "BackListActivity";
    private static final int REQ_CODE_CAMERA_PERMISSION = 133;

    @BindView(R.id.back_btn_back)
    Button backBtnBack;
    @BindView(R.id.back_ll_task_item_title)
    LinearLayout backLlTaskItemTitle;
    @BindView(R.id.back_gun_recycler_view)
    RecyclerView backGunRecyclerView;
    @BindView(R.id.back_tv_msg)
    TextView backTvMsg;
    @BindView(R.id.back_btn_pre_page)
    Button backBtnPrePage;
    @BindView(R.id.back_tv_cur_page)
    TextView backTvCurPage;
    @BindView(R.id.back_btn_next_page)
    Button backBtnNextPage;
    @BindView(R.id.back_btn_open_cab)
    Button backBtnOpenCab;
    @BindView(R.id.back_btn_open_lock)
    Button backBtnOpenLock;
    @BindView(R.id.back_btn_confirm)
    Button backBtnConfirm;
    @BindView(R.id.back_btn_finish)
    Button backBtnFinish;
    @BindView(R.id.back_gun_ll_nav)
    LinearLayout backGunLlNav;
    @BindView(R.id.back_rl_bottom_view)
    RelativeLayout backRlBottomView;
    @BindView(R.id.back_list_tv_gun_no)
    TextView backListTvGunNo;
    @BindView(R.id.back_list_tv_back_num)
    TextView backListTvBackNum;

    private List<GoTaskInfoBean> taskInfoList = new ArrayList<>();
    private int index = 0;
    private int pageCount = 10;
    private TaskItemAdapter taskItemAdapter;
    private CommonLogBeanDao commonLogBeanDao;
    private UserBean apply, approve;
    private boolean isQueryStatus = true;
    private String backTaskId;
    private int policeId;
    private CameraConfig mCameraConfig;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_back_list);
        ButterKnife.bind(this);
        initView();
        if (SharedUtils.getIsCaptureOpen()) {
            configCamera();
        }
        Constants.isCheckGunStatus =false;
        Constants.isIllegalGetGunAlarm =true;
        Constants.isIllegalOpenCabAlarm =true;

        //置为正在任务操作
        Constants.isExecuteTask =true;
    }

    private void initView() {
        backBtnPrePage.setVisibility(View.INVISIBLE);
        backBtnNextPage.setVisibility(View.INVISIBLE);
        commonLogBeanDao = DBManager.getInstance().getCommonLogBeanDao();

        backGunRecyclerView.setLayoutManager(new LinearLayoutManager(this,
                LinearLayout.VERTICAL, false));
        taskItemAdapter = new TaskItemAdapter(taskInfoList, index, pageCount, "back");
        backGunRecyclerView.setAdapter(taskItemAdapter);

        backTaskId = getIntent().getStringExtra("backTaskId");
        policeId = getIntent().getIntExtra("policeId", 0);
        Log.i(TAG, "initView backTaskId: " + backTaskId + " policeId:" + policeId);
        if (SharedUtils.getCabType().equals(Constants.TYPE_AMMO_CAB)) {
            backListTvGunNo.setVisibility(View.GONE);
            backListTvBackNum.setVisibility(View.VISIBLE);
        }
        if (!TextUtils.isEmpty(backTaskId)) {
            if (Utils.isNetworkAvailable()) {
                getTaskInfo(backTaskId);
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
                                String inFinishState = goTaskInfoBean.getInFinishState();
                                String outFinishState = goTaskInfoBean.getOutFinishState();
                                if (!TextUtils.isEmpty(inFinishState) && !TextUtils.isEmpty(outFinishState)) {
                                    if (inFinishState.equals("no") && outFinishState.equals("yes")) {
                                        taskInfoList.add(goTaskInfoBean);
                                    }
                                }
                            }
                            if (taskInfoList.isEmpty()) {
                                backTvMsg.setText("没有可归还的枪弹");
                            } else {
                                backTvMsg.setVisibility(View.INVISIBLE);
                            }
                            taskItemAdapter.setList(taskInfoList);
                            taskItemAdapter.notifyDataSetChanged();
                            initPreNextBtn();
                        } else {
//                            showDialog("获取任务清单为空！");
                            backTvMsg.setText("获取任务清单为空");
                        }
                    } else {
//                        showDialog("获取任务清单失败！");
                        backTvMsg.setText("获取任务清单失败");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
//                    showDialog("获取任务清单出现错误");
                    backTvMsg.setText("获取任务清单出现错误");
                }
            }

            @Override
            public void onFailed(int what, Response<String> response) {
                backTvMsg.setText("网络请求出错，获取任务清单失败！");
            }
        });
    }

    private void initPreNextBtn() {
        if (taskInfoList.isEmpty()) {
            backTvCurPage.setText(index + 1 + "/1");
        } else {
            if (taskInfoList.size() <= pageCount) {
                backBtnNextPage.setVisibility(View.INVISIBLE);
            } else {
                backBtnNextPage.setVisibility(View.VISIBLE);
            }
            backTvCurPage.setText(index + 1 + "/" + (int) Math.ceil((double) taskInfoList.size() / pageCount));
        }
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
            backBtnOpenCab.setVisibility(View.VISIBLE);
            backBtnOpenLock.setVisibility(View.VISIBLE);
            String cabType = SharedUtils.getCabType();
            if (cabType.equals(Constants.TYPE_AMMO_CAB)) {
                //领取弹药和归还弹药
                backBtnOpenCab.setText("打开弹柜");
                backBtnOpenLock.setText("打开弹仓");
            }
            backBtnFinish.setVisibility(View.VISIBLE);
            backBtnConfirm.setVisibility(View.GONE);
            backBtnBack.setVisibility(View.GONE);
            isQueryStatus = true;
            //查询枪支状态
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

    @OnClick({R.id.back_btn_pre_page, R.id.back_btn_next_page, R.id.back_btn_open_cab,
            R.id.back_btn_open_lock, R.id.back_btn_confirm, R.id.back_btn_finish,
            R.id.back_btn_back})
    public void onViewClicked(View view) {
        List<GoTaskInfoBean> checkedList = taskItemAdapter.getCheckedList();
        final String cabType = SharedUtils.getCabType();
        switch (view.getId()) {
            case R.id.back_btn_pre_page:  //上一页
                prePager();
                break;
            case R.id.back_btn_next_page:   //下一页
                nexPager();
                break;
            case R.id.back_btn_open_cab: //开枪柜


                SharedUtils.setIsCheckStatus(false);//不查询状态
                if (cabType.equals(Constants.TYPE_AMMO_CAB)) {
                    SerialPortUtil.getInstance().openLock(SharedUtils.getLeftCabNo());
                    SoundPlayUtil.getInstance().play(R.raw.bullet_cab_open);
                    DBManager.getInstance().insertCommLog(BackListActivity.this, apply,
                            apply.getUserName() + "打开弹柜门");
                } else { //枪柜
                    SerialPortUtil.getInstance().openLock(SharedUtils.getLeftCabNo());
                    SoundPlayUtil.getInstance().play(R.raw.gun_cab_open);
                    DBManager.getInstance().insertCommLog(BackListActivity.this, apply,
                            apply.getUserName() + "打开枪柜门");
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
            case R.id.back_btn_open_lock: //开锁
                SharedUtils.setIsCheckStatus(false);//不查询状态
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        List<GoTaskInfoBean> checkedList = taskItemAdapter.getCheckedList();
                        synchronized (checkedList) {
                            if (!checkedList.isEmpty()) {
                                for (GoTaskInfoBean goTaskInfoBean : checkedList) {
                                    String locationType = goTaskInfoBean.getLocationType();
                                    int locationNo = goTaskInfoBean.getLocationNo();
                                    SerialPortUtil.getInstance().openLock(locationNo);
                                    try {
                                        Thread.sleep(300);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                    if (locationType.equals(Constants.TYPE_AMMO)) {
                                        //归还弹药
                                        DBManager.getInstance().insertCommLog(BackListActivity.this, apply,
                                                apply.getUserName() + "打开" + locationNo + "号弹仓");
                                    } else {
                                        DBManager.getInstance().insertCommLog(BackListActivity.this, apply,
                                                apply.getUserName() + "打开" + locationNo + "号枪锁");
                                    }
                                }
                                if (cabType.equals(Constants.TYPE_AMMO_CAB)) {
                                    //归还弹药
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
            case R.id.back_btn_confirm:
                if (!checkedList.isEmpty()) {
                    PostGetDataBean postGetDataBean = new PostGetDataBean();
                    postGetDataBean.setOperation("in");
                    postGetDataBean.setPoliceTaskId(backTaskId);
                    List<PostGetDataBean.ListPoliceTaskListBean> policeTaskList = new ArrayList<>();
                    for (GoTaskInfoBean goTaskInfo : checkedList) {
                        PostGetDataBean.ListPoliceTaskListBean policeTaskListBean = new PostGetDataBean.ListPoliceTaskListBean();
                        String gunCabinetLocationId = goTaskInfo.getGunCabinetLocationId();
                        policeTaskListBean.setId(goTaskInfo.getId());
                        policeTaskListBean.setLocationType(goTaskInfo.getLocationType());
                        policeTaskListBean.setObjectId(goTaskInfo.getObjectId());
                        policeTaskListBean.setObjectNumber(goTaskInfo.getObjectNumber());
                        policeTaskListBean.setReturnNumber(goTaskInfo.getReturnNumber());
                        policeTaskListBean.setGunCabinetLocationId(gunCabinetLocationId);
                        policeTaskListBean.setAmmunitionType(goTaskInfo.getAmmunitionType());
                        policeTaskList.add(policeTaskListBean);
                    }
                    postGetDataBean.setListPoliceTaskList(policeTaskList);
                    String jsonBody = JSON.toJSONString(postGetDataBean);
                    LogUtil.i(TAG, "onViewClicked post back data jsonBody: " + jsonBody);
//                    postBackData(jsonBody);

                    Intent intent = new Intent(this, VerifyActivity.class);
                    intent.putExtra("activity", Constants.ACTIVITY_BACK);
                    intent.putExtra("data", jsonBody);
//                    intent.putExtra("taskId", getTaskId);
                    intent.putExtra("policeId", policeId);
                    startActivity(intent);
                }
                break;
            case R.id.back_btn_finish:
                if (Constants.isDebug || Constants.isOldBoard) {
                    finish();
                    isQueryStatus = false;
                } else {
                    if (!SharedUtils.getCabType().equals(Constants.TYPE_AMMO_CAB)) {
                        if (gunStatusMap.isEmpty()) {
                            ToastUtil.showShort("枪支未放置在位");
                            SoundPlayUtil.getInstance().play(R.raw.gun_not_in_position);
                            return;
                        }

                        if (!checkedList.isEmpty()) {
                            for (GoTaskInfoBean goTaskInfoBean : checkedList) {
                                int locationNo = goTaskInfoBean.getLocationNo();
                                if (gunStatusMap.containsKey(locationNo)) {
                                    //枪支状态  true 在位 false 不在位
                                    boolean aBoolean = gunStatusMap.get(locationNo);
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
                    isQueryStatus =false;

                    if (SharedUtils.getCabOpenStatus() == 1) {
                        ToastUtil.showShort("柜门未关闭");
                        SoundPlayUtil.getInstance().play(R.raw.cab_not_close);
                    } else {
                        finish();
                    }
                }
                break;
            case R.id.back_btn_back:
                finish();
                break;
        }
    }

    private void prePager() {
        index--;
        taskItemAdapter.setIndex(index);
        backTvCurPage.setText(index + 1 + "/" + (int) Math.ceil((double) taskInfoList.size() / pageCount));
        //隐藏上一个或下一个按钮
        checkButton();
        Log.i(TAG, "prePager index: " + index);
    }

    private void nexPager() {
        index++;
        taskItemAdapter.setIndex(index);
        backTvCurPage.setText(index + 1 + "/" + (int) Math.ceil((double) taskInfoList.size() / pageCount));
        //隐藏上一个或下一个按钮
        checkButton();
        Log.i(TAG, "nexPager index: " + index);
    }

    private void checkButton() {
        if (index <= 0) {
            backBtnPrePage.setVisibility(View.INVISIBLE);
            backBtnNextPage.setVisibility(View.VISIBLE);
        } else if (taskInfoList.size() - index * pageCount <= pageCount) {    //数据总数减每页数当小于每页可显示的数字时既是最后一页
            backBtnPrePage.setVisibility(View.VISIBLE);
            backBtnNextPage.setVisibility(View.INVISIBLE);
        } else {
            backBtnNextPage.setVisibility(View.VISIBLE);
            backBtnPrePage.setVisibility(View.VISIBLE);
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

        Constants.isCheckGunStatus =true;
        Constants.isIllegalGetGunAlarm =false;
        Constants.isIllegalOpenCabAlarm =false;
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
}
