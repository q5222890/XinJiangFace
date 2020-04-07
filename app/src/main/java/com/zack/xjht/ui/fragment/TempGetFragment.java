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
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
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
import com.zack.xjht.db.DBManager;
import com.zack.xjht.entity.TempGetBean;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * 临时存放领取枪支
 */
public class TempGetFragment extends HiddenCameraFragment {
    private static final String TAG = "TempGetFragment";
    private static final int REQ_CODE_CAMERA_PERMISSION = 133;

    @BindView(R.id.temp_get_recycler_view)
    RecyclerView tempGetRecyclerView;
    @BindView(R.id.temp_get_btn_confirm)
    Button tempGetBtnConfirm;
    @BindView(R.id.temp_get_ll_bottom)
    LinearLayout tempGetLlBottom;
    Unbinder unbinder;
    @BindView(R.id.temp_get_ll_top)
    LinearLayout tempGetLlTop;
    @BindView(R.id.temp_get_btn_open_cab)
    Button tempGetBtnOpenCab;
    @BindView(R.id.temp_get_btn_open_lock)
    Button tempGetBtnOpenLock;
    @BindView(R.id.temp_get_tv_msg)
    TextView tempGetTvMsg;
    @BindView(R.id.temp_get_btn_finish)
    Button tempGetBtnFinish;
    @BindView(R.id.temp_get_btn_pre)
    Button tempGetBtnPre;
    @BindView(R.id.temp_get_tv_curpage)
    TextView tempGetTvCurpage;
    @BindView(R.id.temp_get_btn_next)
    Button tempGetBtnNext;
    private List<TempGetBean> tempGetBeanList = new ArrayList<>();
    private List<TempGetBean> checkedList = new ArrayList<>();
    private TempDataAdapter tempDataAdapter;
    private UserBean manager1, manager2;
    private Context mContext;
    private FragmentActivity mActivity;
    private int index = 0;
    private int pageCount = 8;
    private UserBean apply;
    private boolean isQueryStatus = true;
    private CameraConfig mCameraConfig;

    public TempGetFragment() {
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
        View view = inflater.inflate(R.layout.fragment_temp_get, container, false);
        unbinder = ButterKnife.bind(this, view);
        initView();

        Constants.isIllegalOpenCabAlarm =true;
        //置为正在任务操作
        Constants.isExecuteTask =true;
        return view;
    }

    private void initView() {
        LinearLayoutManager llm = new LinearLayoutManager(getContext(),
                LinearLayoutManager.VERTICAL, false);
        tempGetRecyclerView.setLayoutManager(llm);
        tempDataAdapter = new TempDataAdapter();
        tempGetRecyclerView.setAdapter(tempDataAdapter);

        if (Utils.isNetworkAvailable()) {
            getTempStoreTask();
        }
//        //注册eventbus
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
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

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void onMessageEvent(MessageEvent event) {
        EventBus.getDefault().removeStickyEvent(event);
        tempDataAdapter.setDisableAll(true);
        String message = event.getMessage();
        apply = event.getApply();
        if (message.equals(EventConsts.EVENT_POST_SUCCESS)) {
            Log.i(TAG, "onMessageEvent 提交成功！: ");
            tempGetBtnOpenCab.setVisibility(View.VISIBLE);
            tempGetBtnOpenLock.setVisibility(View.VISIBLE);
            tempGetBtnFinish.setVisibility(View.VISIBLE);
            tempGetBtnConfirm.setVisibility(View.GONE);
            isQueryStatus = true;
            //查询枪支状态
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Log.i(TAG, "run isQueryStatus : " + isQueryStatus);
                    while (isQueryStatus) {
                        if (!checkedList.isEmpty()) {
                            Log.i(TAG, "run  selectedList size: " + checkedList.size());
                            for (int i = 0; i < checkedList.size(); i++) {
                                TempGetBean tempGetBean = checkedList.get(i);
                                int locationNo = tempGetBean.getLocationNo();
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
        } else {
            Log.i(TAG, "onMessageEvent 提交失败！: ");
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
     * 获取临时存放枪支数据
     */
    private void getTempStoreTask() {
        HttpClient.getInstance().getTempStoreGun(mContext, new HttpListener<String>() {
            @Override
            public void onSucceed(int what, Response<String> response) throws JSONException {
                Log.i(TAG, "onSucceed getTempStoreGun response: " + response.get());
                try {
                    if (!TextUtils.isEmpty(response.get())) {
                        List<TempGetBean> tempGetBeans = JSON.parseArray(response.get(), TempGetBean.class);
                        if (!tempGetBeans.isEmpty()) {
                            tempGetBeanList.clear();
                            tempGetBeanList.addAll(tempGetBeans);
                            tempDataAdapter.notifyDataSetChanged();
                            initPreNextBtn();

                            if (SharedUtils.getIsCaptureOpen()) {
                                configCamera();
                            }
                        } else {
                            //                        ToastUtil.showShort("获取临时存枪数据为空！");
                            if (tempGetTvMsg != null) {
                                tempGetTvMsg.setText("获取临时存枪数据为空");
                            }
                        }
                    } else {
                        //                    ToastUtil.showShort("获取数据为空！");
                        if (tempGetTvMsg != null) {
                            tempGetTvMsg.setText("获取数据为空");
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    if (tempGetTvMsg != null) {
                        tempGetTvMsg.setText("获取数据出错");
                    }
                }
            }

            @Override
            public void onFailed(int what, Response<String> response) {
                if (tempGetTvMsg != null) {
                    tempGetTvMsg.setText("网络错误！获取数据出错");
                }
            }
        });
    }

    private void initPreNextBtn() {
        if (tempGetBeanList.isEmpty()) {
            tempGetTvCurpage.setText(index + 1 + "/1");
        } else {
            if (tempGetBeanList.size() <= pageCount) {
                tempGetBtnNext.setVisibility(View.INVISIBLE);
            } else {
                tempGetBtnNext.setVisibility(View.VISIBLE);
            }
            tempGetTvCurpage.setText(index + 1 + "/" + (int) Math.ceil((double) tempGetBeanList.size() / pageCount));
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        isQueryStatus = false;
    }

    @Override
    public void onStop() {
        super.onStop();
        isQueryStatus = false;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
        isQueryStatus = false;

        Constants.isIllegalOpenCabAlarm =false;
        //置为正在任务操作
        Constants.isExecuteTask =false;
        unbinder.unbind();
    }

    @OnClick({R.id.temp_get_btn_confirm, R.id.temp_get_btn_open_cab, R.id.temp_get_btn_open_lock,
            R.id.temp_get_btn_finish, R.id.temp_get_btn_pre, R.id.temp_get_btn_next})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.temp_get_btn_confirm://确认领出枪支
                if (!checkedList.isEmpty()) {
                    List<Map<String, Object>> paramList = new ArrayList<>();
                    for (TempGetBean tempGetBean : checkedList) {
                        String gunCabinetLocationId = tempGetBean.getGunCabinetLocationId();
                        String id = tempGetBean.getId();
                        if (!TextUtils.isEmpty(gunCabinetLocationId) && !TextUtils.isEmpty(id)) {
                            Map<String, Object> params = new HashMap<>();
                            params.put("gunCabinetLocationId", gunCabinetLocationId);
                            params.put("id", id);
                            paramList.add(params);
                        }
                    }
                    String jsonString = JSON.toJSONString(paramList, SerializerFeature.WriteMapNullValue);
                    LogUtil.i(TAG, "onViewClicked  jsonString: " + jsonString);
//                    postTempGunGet(jsonString);

                    Intent intent = new Intent(mContext, VerifyActivity.class);
                    intent.putExtra("activity", Constants.ACTIVITY_TEMP_GET);
                    intent.putExtra("data", jsonString);
                    mContext.startActivity(intent);
                }
                break;
            case R.id.temp_get_btn_finish: //结束
                if (Constants.isDebug || Constants.isOldBoard) {
                    mActivity.finish();
                    isQueryStatus = false;
                } else {
                    if (gunStatusMap.isEmpty()) {
                        ToastUtil.showShort("枪支未取出");
                        SoundPlayUtil.getInstance().play(R.raw.gun_not_out);
                        return;
                    }
                    if (!checkedList.isEmpty()) {
                        for (TempGetBean tempGetBean : checkedList) {
                            int locationNo = tempGetBean.getLocationNo();
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
                        mActivity.finish();
                    }
                }
                break;
            case R.id.temp_get_btn_open_cab: //开门
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
            case R.id.temp_get_btn_open_lock: //开锁
                SharedUtils.setIsCheckStatus(false);//查询状态
                if (!checkedList.isEmpty()) {
                    for (TempGetBean tempGetBean : checkedList) {
                        int locationNo = tempGetBean.getLocationNo();
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
                }
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                SharedUtils.setIsCheckStatus(true);//查询状态
                break;
            case R.id.temp_get_btn_pre: //上一页
                prePager();
                break;
            case R.id.temp_get_btn_next: //下一页
                nexPager();
                break;
        }
    }

    /**
     * 上一页
     */
    private void prePager() {
        index--;
        tempDataAdapter.setIndex(index);
        tempGetTvCurpage.setText(index + 1 + "/" + (int) Math.ceil((double) tempGetBeanList.size() / pageCount));
        //隐藏上一个或下一个按钮
        checkButton();
    }

    /**
     * 下一页
     */
    private void nexPager() {
        index++;
        tempDataAdapter.setIndex(index);
        tempGetTvCurpage.setText(index + 1 + "/" + (int) Math.ceil((double) tempGetBeanList.size() / pageCount));
        //隐藏上一个或下一个按钮
        checkButton();
    }

    /**
     * button显示和隐藏
     */
    private void checkButton() {
        if (index <= 0) {
            tempGetBtnPre.setVisibility(View.INVISIBLE);
            tempGetBtnNext.setVisibility(View.VISIBLE);
        } else if (tempGetBeanList.size() - index * pageCount <= pageCount) {    //数据总数减每页数当小于每页可显示的数字时既是最后一页
            tempGetBtnPre.setVisibility(View.VISIBLE);
            tempGetBtnNext.setVisibility(View.INVISIBLE);
        } else {
            tempGetBtnPre.setVisibility(View.VISIBLE);
            tempGetBtnNext.setVisibility(View.VISIBLE);
        }
    }

    private void postTempGunGet(String jsonString) {
        HttpClient.getInstance().postTempStoreGunGet(mContext, jsonString, new HttpListener<String>() {
            @Override
            public void onSucceed(int what, Response<String> response) throws JSONException {
                Log.i(TAG, "onSucceed postTempStoreGet response: " + response.get());
                try {
                    if (!TextUtils.isEmpty(response.get())) {
                        if (response.get().equals("success")) {
                            ToastUtil.showShort("提交成功！");
                            Objects.requireNonNull(getActivity()).finish();
                        } else {
                            ToastUtil.showShort("提交失败！");
                        }
                    } else {
                        ToastUtil.showShort("提交数据失败！");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailed(int what, Response<String> response) {
                ToastUtil.showShort("网络错误，提交数据失败！");
            }
        });
    }

    public class TempDataAdapter extends RecyclerView.Adapter<TempDataAdapter.TempGetDataViewHolder> {
        private static final String TAG = "TempDataAdapter";
        private Map<Integer, Boolean> checkStatus;
        private boolean isDisableAll = false;

        public TempDataAdapter() {
            checkStatus = new HashMap<>();
        }

        public void setDisableAll(boolean disableAll) {
            isDisableAll = disableAll;
            notifyDataSetChanged();
        }

        @Override
        public TempGetDataViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_temp_get_gun,
                    parent, false);
            return new TempGetDataViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final TempGetDataViewHolder holder, final int position) {
            final int pos = position + index * pageCount;
            holder.taskItemCbBack.setOnCheckedChangeListener(null);
            if (checkStatus.containsKey(pos)) {
                holder.taskItemCbBack.setChecked(checkStatus.get(pos));
            }

            if (isDisableAll) {
                holder.taskItemCbBack.setClickable(false);
                holder.taskItemCbBack.setEnabled(false);
            } else {
                holder.taskItemCbBack.setEnabled(true);
                holder.taskItemCbBack.setClickable(true);
            }

            final TempGetBean tempGetBean = tempGetBeanList.get(pos);
            int subCabNo = tempGetBean.getLocationNo(); //位置编号
            String gunType = tempGetBean.getGunType(); //枪支类型
            String gunNo = tempGetBean.getGunNo();
            String depositor = tempGetBean.getDepositor();
            String createTime = tempGetBean.getCreateTime();

            holder.tempItemTvGunNo.setText(gunNo);
            holder.tempItemTvObjectType.setText(gunType);
            holder.tempItemTvPosition.setText(String.valueOf(subCabNo));
            holder.tempItemTvGunStatus.setText(depositor);

            holder.taskItemCbBack.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    checkStatus.put(pos, isChecked);
                    if (isChecked) {//添加选中数据
                        checkedList.add(tempGetBean);
                    } else {  //取消选中从集合中移除
                        checkedList.remove(tempGetBean);
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            int current = index * pageCount;
            return tempGetBeanList.size() - current < pageCount ? tempGetBeanList.size() - current : pageCount;
        }

        public void setIndex(int i) {
            index = i;
            notifyDataSetChanged();
        }

        public class TempGetDataViewHolder extends RecyclerView.ViewHolder {
            @BindView(R.id.temp_item_tv_object_type)
            TextView tempItemTvObjectType;
            @BindView(R.id.temp_item_tv_position)
            TextView tempItemTvPosition;
            @BindView(R.id.temp_item_tv_gun_status)
            TextView tempItemTvGunStatus;
            @BindView(R.id.temp_item_tv_gun_no)
            TextView tempItemTvGunNo;
            @BindView(R.id.task_item_cb_back)
            CheckBox taskItemCbBack;
            @BindView(R.id.temp_list_item)
            LinearLayout tempListItem;

            TempGetDataViewHolder(View view) {
                super(view);
                ButterKnife.bind(this, view);
            }
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
