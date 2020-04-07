package com.zack.xjht.ui.fragment;


import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.androidhiddencamera.CameraConfig;
import com.androidhiddencamera.CameraError;
import com.androidhiddencamera.HiddenCameraFragment;
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
import com.zack.xjht.adapter.BackDataAdapter;
import com.zack.xjht.adapter.UrgentTaskAdapter;
import com.zack.xjht.db.DBManager;
import com.zack.xjht.db.gen.CommonLogBeanDao;
import com.zack.xjht.db.gen.UrgentOutBeanDao;
import com.zack.xjht.entity.UrgentBackBean;
import com.zack.xjht.entity.UrgentGetListBean;
import com.zack.xjht.entity.UrgentOutBean;
import com.zack.xjht.entity.UrgentTaskBean;
import com.zack.xjht.entity.UrgentTaskInfoBean;
import com.zack.xjht.entity.UserBean;
import com.zack.xjht.event.EventConsts;
import com.zack.xjht.event.MessageEvent;
import com.zack.xjht.event.StatusEvent;
import com.zack.xjht.http.HttpClient;
import com.zack.xjht.http.HttpListener;
import com.zack.xjht.serial.SerialPortUtil;
import com.zack.xjht.ui.VerifyActivity;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * 紧急用枪归还
 */
public class UrgentBackGunsFragment extends HiddenCameraFragment {
    private static final String TAG = "UrgentBackGunsFragment";
    private static final int REQ_CODE_CAMERA_PERMISSION = 133;

    @BindView(R.id.urgent_back_guns_ll_tittle)
    LinearLayout urgentBackGunsLlTittle;
    @BindView(R.id.urgent_back_recycler_view)
    RecyclerView urgentBackRecyclerView;
    @BindView(R.id.urgent_back_btn_pre_page)
    Button urgentBackBtnPrePage;
    @BindView(R.id.urgent_back_tv_cur_page)
    TextView urgentBackTvCurPage;
    @BindView(R.id.urgent_back_btn_next_page)
    Button urgentBackBtnNextPage;
    @BindView(R.id.urgent_back_btn_finish)
    Button urgentBackBtnFinish;
    @BindView(R.id.urgent_back_bottom_view)
    LinearLayout urgentBackBottomView;
    @BindView(R.id.back_rv_task_list)
    RecyclerView backRvTaskList;
    @BindView(R.id.urgent_back_btn_open_cab)
    Button urgentBackBtnOpenCab;
    @BindView(R.id.urgent_back_gun_tv_msg)
    TextView urgentBackGunTvMsg;
    @BindView(R.id.urgent_back_btn_check_all)
    Button urgentBackBtnCheckAll;
    @BindView(R.id.urgent_back_btn_confirm)
    Button urgentBackBtnConfirm;
    @BindView(R.id.urgent_back_btn_close_lock)
    Button urgentBackBtnCloseLock;
    @BindView(R.id.urgent_back_btn_open_lock)
    Button urgentBackBtnOpenLock;
    Unbinder unbinder;
    private BackDataAdapter backDataAdapter;
    private List<UrgentGetListBean> operBeanList;
    private UserBean apply, approve;
    private int index = 0;
    private int pageCount = 8;
    private Context mContext;
    private FragmentActivity mActivity;
    private List<UrgentOutBean> urgentTaskList;
    private UrgentTaskAdapter urgentTaskAdapter;
    private Long urgentTaskId;
    private boolean isQueryStatus = true;
    private CameraConfig mCameraConfig;
    private UrgentOutBeanDao urgentOutBeanDao;

    public UrgentBackGunsFragment() {
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
        mActivity = getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_urgent_back_guns, container, false);
        unbinder = ButterKnife.bind(this, view);
        Constants.isCheckGunStatus = false;
        Constants.isIllegalGetGunAlarm = true;
        Constants.isIllegalOpenCabAlarm = true;
        //置为正在任务操作
        Constants.isExecuteTask = true;
        initData();
        if (SharedUtils.getIsCaptureOpen()) {
            configCamera();
        }
        return view;
    }

    private void initData() {
        operBeanList = new ArrayList<>();
        urgentTaskList = new ArrayList<>();
        urgentBackBtnPrePage.setVisibility(View.INVISIBLE);
        urgentBackBtnNextPage.setVisibility(View.INVISIBLE);

        urgentOutBeanDao = DBManager.getInstance().getUrgentOutBeanDao();
        urgentTaskList = urgentOutBeanDao.queryBuilder().where(
                UrgentOutBeanDao.Properties.TaskFinish.eq(false)).list();
        Log.i(TAG, "initData urgentTaskList: "+JSON.toJSONString(urgentTaskList));
        if (urgentTaskList.isEmpty()) {
            urgentBackGunTvMsg.setVisibility(View.VISIBLE);
            urgentBackGunTvMsg.setText("获取任务数据为空！");
        }

        LinearLayoutManager llm = new LinearLayoutManager(getContext(),
                LinearLayoutManager.VERTICAL, false);
        urgentBackRecyclerView.setLayoutManager(llm);
        backDataAdapter = new BackDataAdapter(operBeanList, index, pageCount);
        urgentBackRecyclerView.setAdapter(backDataAdapter);

        backRvTaskList.setLayoutManager(new LinearLayoutManager(mContext,
                LinearLayout.VERTICAL, false));
        urgentTaskAdapter = new UrgentTaskAdapter(urgentTaskList, index, pageCount);
        backRvTaskList.setAdapter(urgentTaskAdapter);

        urgentTaskAdapter.setOnTaskIdListener(new UrgentTaskAdapter.OnTaskIdListener() {
            @Override
            public void onTaskId(Long taskId) {
                urgentTaskId = taskId;
                List<UrgentOutBean> urgentOutBeans = urgentOutBeanDao.loadAll();
                LogUtil.i(TAG, "onTaskId urgentOutBeans: "+JSON.toJSONString(urgentOutBeans));
                UrgentOutBean unique = urgentOutBeanDao.queryBuilder().where(
                        UrgentOutBeanDao.Properties.TId.eq(taskId)).unique();
                if (unique != null) {
                    List<UrgentGetListBean> urgentTaskList = unique.getUrgentGetList();
                    Log.i(TAG, "onTaskId urgentTaskList size: " + urgentTaskList.size());
                    if (!urgentTaskList.isEmpty()) {
                        operBeanList.clear();
                        for (UrgentGetListBean urgentTaskInfo : urgentTaskList) {
                            String locationType = urgentTaskInfo.getLocationType();
                            if (!locationType.equals(Constants.TYPE_AMMO)) {
                                operBeanList.add(urgentTaskInfo);
                            }
                        }
                        if (!operBeanList.isEmpty()) {
                            backDataAdapter.setOperList(operBeanList);
                        } else {
                            urgentBackGunTvMsg.setVisibility(View.VISIBLE);
                            urgentBackGunTvMsg.setText("没有可归还数据");
                        }
                        Collections.sort(operBeanList);
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                backDataAdapter.notifyDataSetChanged();
                            }
                        }, 300);
                        initPreNextBtn();
                        backDataAdapter.selectAll();
                        backDataAdapter.setDisableAll();
                    } else {
//                        unique.setTaskFinish(true);
//                        urgentOutBeanDao.update(unique);
                        urgentBackGunTvMsg.setVisibility(View.VISIBLE);
                        urgentBackGunTvMsg.setText("没有获取到任务数据");
                    }
                } else {
                    urgentBackGunTvMsg.setVisibility(View.VISIBLE);
                    urgentBackGunTvMsg.setText("没有获取到任务数据");
                }
            }
        });
        urgentBackBtnCheckAll.setVisibility(View.GONE);
        EventBus.getDefault().register(this);
    }

    private void configCamera() {
        Log.i(TAG, "captureImage: ");
        //Setting camera configuration 设置相机配置
        mCameraConfig = new CameraConfig()
                .getBuilder(getActivity())
                .setCameraFacing(CameraFacing.FRONT_FACING_CAMERA)
                .setCameraResolution(CameraResolution.MEDIUM_RESOLUTION)
                .setImageFormat(CameraImageFormat.FORMAT_JPEG)
                .setCameraFocus(CameraFocus.NO_FOCUS)
//                .setImageRotation(CameraRotation.ROTATION_90)
                .build();

        //Check for the camera permission for the runtime
        if (ActivityCompat.checkSelfPermission(mActivity, Manifest.permission.CAMERA)
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSuccessEvent(MessageEvent event) {
        backDataAdapter.setDisableAll();
        String message = event.getMessage();
        apply = event.getApply();
        approve = event.getApprove();
        Log.i(TAG, "onPostSuccessEvent message: " + message
                + "  申请人:" + apply.getUserName()
                + " 审批人:" + approve.getUserName());
        if (message.equals(EventConsts.EVENT_POST_SUCCESS)) {
            //提交成功
            Log.i(TAG, "onSuccessEvent 提交成功: ");
            urgentBackBtnOpenCab.setVisibility(View.VISIBLE);
//            urgentBackBtnCloseLock.setVisibility(View.VISIBLE);
            urgentBackBtnOpenLock.setVisibility(View.VISIBLE);
            urgentBackBtnFinish.setVisibility(View.VISIBLE);
            urgentBackBtnCheckAll.setVisibility(View.GONE);
            urgentBackBtnConfirm.setVisibility(View.GONE);
            isQueryStatus = true;
            //查询枪支状态
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Log.i(TAG, "run isQueryStatus : " + isQueryStatus);
                    while (isQueryStatus) {
                        List<UrgentGetListBean> selectedList = backDataAdapter.getCheckedList();
                        if (!selectedList.isEmpty()) {
                            Log.i(TAG, "run  selectedList size: " + selectedList.size());
                            for (int i = 0; i < selectedList.size(); i++) {
                                UrgentGetListBean urgentTaskInfoBean = selectedList.get(i);
                                int locationNo = urgentTaskInfoBean.getLocationNo();
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

    private void initPreNextBtn() {
        if (operBeanList.isEmpty()) {
            urgentBackTvCurPage.setText(index + 1 + "/1");
        } else {
            if (operBeanList.size() <= pageCount) {
                urgentBackBtnNextPage.setVisibility(View.INVISIBLE);
            } else {
                urgentBackBtnNextPage.setVisibility(View.VISIBLE);
            }
            urgentBackTvCurPage.setText(index + 1 + "/" + (int) Math.ceil((double) operBeanList.size() / pageCount));
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        isQueryStatus = false;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
        Constants.isCheckGunStatus = true;
        Constants.isIllegalGetGunAlarm = false;
        Constants.isIllegalOpenCabAlarm = false;
        //置为正在任务操作
        Constants.isExecuteTask = false;
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
        isQueryStatus = false;
    }

    @OnClick({R.id.urgent_back_btn_pre_page, R.id.urgent_back_btn_next_page,
            R.id.urgent_back_btn_close_lock, R.id.urgent_back_btn_finish, R.id.urgent_back_btn_open_lock,
            R.id.urgent_back_btn_open_cab, R.id.urgent_back_btn_check_all, R.id.urgent_back_btn_confirm})
    public void onViewClicked(View view) {
        List<UrgentGetListBean> checkedList = backDataAdapter.getCheckedList();
        switch (view.getId()) {
            case R.id.urgent_back_btn_pre_page:
                prePager();
                break;
            case R.id.urgent_back_btn_next_page:
                nexPager();
                break;
            case R.id.urgent_back_btn_open_cab:     //开枪柜
                SharedUtils.setIsCheckStatus(false);//查询状态
                SerialPortUtil.getInstance().openLock(SharedUtils.getLeftCabNo());
                SoundPlayUtil.getInstance().play(R.raw.gun_cab_open);

                DBManager.getInstance().insertCommLog(mContext, apply, apply.getUserName() + "打开枪柜门");
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
            case R.id.urgent_back_btn_open_lock:    //开枪锁
                SharedUtils.setIsCheckStatus(false);//查询状态
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        List<UrgentGetListBean> checkedList = backDataAdapter.getCheckedList();
                        synchronized (checkedList) {
                            if (!checkedList.isEmpty()) {
                                for (UrgentGetListBean goTaskInfoBean : checkedList) {
                                    int locationNo = goTaskInfoBean.getLocationNo();
                                    SerialPortUtil.getInstance().openLock(locationNo);
                                    try {
                                        Thread.sleep(300);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                    DBManager.getInstance().insertCommLog(mContext, apply,
                                            apply.getUserName() + "打开" + locationNo + "号枪锁");
                                }
                                SoundPlayUtil.getInstance().play(R.raw.gun_lock_open);
                                try {
                                    Thread.sleep(500);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                SharedUtils.setIsCheckStatus(true);//查询状态
                            }
                        }
                    }
                }).start();
                break;
            case R.id.urgent_back_btn_close_lock:   //关枪锁
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        List<UrgentGetListBean> checkedList = backDataAdapter.getCheckedList();
                        synchronized (checkedList) {
                            if (!checkedList.isEmpty()) {
                                for (UrgentGetListBean goTaskInfoBean : checkedList) {
                                    int locationNo = goTaskInfoBean.getLocationNo();
                                    SerialPortUtil.getInstance().closeLock(locationNo);
                                    try {
                                        Thread.sleep(300);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                }

                            }
                        }
                    }
                }).start();
                break;
            case R.id.urgent_back_btn_confirm:
                if (checkedList.isEmpty()) {
                    ToastUtil.showShort("请选择归还枪支");
                    return;
                }

                String jsonBody = JSON.toJSONString(checkedList);
                Log.i(TAG, "onViewClicked  jsonBody: " + jsonBody);
                Intent intent = new Intent(mContext, VerifyActivity.class);
                intent.putExtra("activity", Constants.ACTIVITY_URGENT_BACK_GUN);
                intent.putExtra("data", jsonBody);
                intent.putExtra("urgentTaskId", urgentTaskId);
                startActivity(intent);
                break;
            case R.id.urgent_back_btn_finish: //确认领枪
                if (Constants.isDebug || Constants.isOldBoard) {
                    mActivity.finish();
                    isQueryStatus = false;
                } else {
                    if (gunStatusMap.isEmpty()) {
                        ToastUtil.showShort("枪支未放置在位");
                        SoundPlayUtil.getInstance().play(R.raw.gun_not_in_position);
                        return;
                    }

                    if (!checkedList.isEmpty()) {
                        for (UrgentGetListBean urgentTaskInfoBean : checkedList) {
                            int locationNo = urgentTaskInfoBean.getLocationNo();
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
                    //停止查询枪支状态
                    isQueryStatus = false;

                    if (SharedUtils.getCabOpenStatus() == 1) {
                        ToastUtil.showShort("柜门未关闭");
                        SoundPlayUtil.getInstance().play(R.raw.cab_not_close);
                    } else {
                        mActivity.finish();
                    }
                }
                break;
            case R.id.urgent_back_btn_check_all:
                String openAll = urgentBackBtnCheckAll.getText().toString();
                if (openAll.equals("选择全部")) {
                    backDataAdapter.selectAll();
                    urgentBackBtnCheckAll.setText("取消全选");
                } else {
                    backDataAdapter.setOperList(operBeanList);
                    urgentBackBtnCheckAll.setText("选择全部");
                }
                break;
        }
    }


    @Override
    public void onPause() {
        super.onPause();
        isQueryStatus = false;
    }

    private void prePager() {
        index--;
        backDataAdapter.setIndex(index);
        urgentBackTvCurPage.setText(index + 1 + "/" +
                (int) Math.ceil((double) operBeanList.size() / pageCount));
        //隐藏上一个或下一个按钮
        checkButton();
        Log.i(TAG, "prePager index: " + index);
    }

    private void nexPager() {
        index++;
        backDataAdapter.setIndex(index);
        urgentBackTvCurPage.setText(index + 1 + "/" +
                (int) Math.ceil((double) operBeanList.size() / pageCount));
        //隐藏上一个或下一个按钮
        checkButton();
        Log.i(TAG, "nexPager index: " + index);
    }

    private void checkButton() {
        if (index <= 0) {
            urgentBackBtnPrePage.setVisibility(View.INVISIBLE);
            urgentBackBtnNextPage.setVisibility(View.VISIBLE);
        } else if (operBeanList.size() - index * pageCount <= pageCount) {    //数据总数减每页数当小于每页可显示的数字时既是最后一页
            urgentBackBtnPrePage.setVisibility(View.VISIBLE);
            urgentBackBtnNextPage.setVisibility(View.INVISIBLE);
        } else {
            urgentBackBtnNextPage.setVisibility(View.VISIBLE);
            urgentBackBtnPrePage.setVisibility(View.VISIBLE);
        }
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
        HttpClient.getInstance().postCapturePhoto(mContext, base64String,
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
                    Toast.makeText(getContext(), R.string.error_cannot_open, Toast.LENGTH_LONG).show();
//                    Toast.makeText(getContext(), "Cannot open camera.", Toast.LENGTH_LONG).show();
                    Log.i(TAG, "onCameraError: ");
                    break;
                case CameraError.ERROR_IMAGE_WRITE_FAILED:
                    //Image write failed. Please check if you have provided WRITE_EXTERNAL_STORAGE permission
                    Toast.makeText(getContext(), R.string.error_cannot_write, Toast.LENGTH_LONG).show();
                    break;
                case CameraError.ERROR_CAMERA_PERMISSION_NOT_AVAILABLE:
                    //camera permission is not available
                    //Ask for the camera permission before initializing it.
                    Toast.makeText(getContext(), R.string.error_cannot_get_permission, Toast.LENGTH_LONG).show();
                    break;
                case CameraError.ERROR_DOES_NOT_HAVE_OVERDRAW_PERMISSION:
                    //Display information dialog to the user with steps to grant "Draw over other app"
                    //permission for the app.
                    HiddenCameraUtils.openDrawOverPermissionSetting(getContext());
                    break;
                case CameraError.ERROR_DOES_NOT_HAVE_FRONT_CAMERA:
                    Toast.makeText(getContext(), R.string.error_not_having_camera, Toast.LENGTH_LONG).show();
                    break;
            }
        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
        }
    }
}
