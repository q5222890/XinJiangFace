package com.zack.xjht.ui.fragment;

import android.Manifest;
import android.annotation.NonNull;
import android.annotation.Nullable;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
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
import com.zack.xjht.adapter.UrgentDataAdapter;
import com.zack.xjht.db.DBManager;
import com.zack.xjht.db.gen.CommonLogBeanDao;
import com.zack.xjht.db.gen.SubCabBeanDao;
import com.zack.xjht.entity.CabInfoBean;
import com.zack.xjht.entity.SubCabBean;
import com.zack.xjht.entity.UrgentOutBean;
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

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * 紧急领取枪支
 */
public class UrgentGetGunsFragment extends HiddenCameraFragment {
    private static final String TAG = "UrgentGetGunsFragment";
    private static final int REQ_CODE_CAMERA_PERMISSION = 133;

    @BindView(R.id.urgent_get_ll_list_tittle)
    LinearLayout urgentGetLlListTittle;
    @BindView(R.id.urgent_get_recycler_view)
    RecyclerView urgentGetRecyclerView;
    @BindView(R.id.urgent_get_btn_pre_page)
    Button urgentGetBtnPrePage;
    @BindView(R.id.urgent_get_tv_cur_page)
    TextView urgentGetTvCurPage;
    @BindView(R.id.urgent_get_btn_next_page)
    Button urgentGetBtnNextPage;
    @BindView(R.id.urgent_get_btn_open_lock)
    Button urgentGetBtnOpenLock;
    @BindView(R.id.urgent_get_btn_confirm)
    Button urgentGetBtnConfirm;
    @BindView(R.id.urgent_get_ll_bottom)
    LinearLayout urgentGetLlBottom;
    @BindView(R.id.urgent_get_btn_open_cab)
    Button urgentGetBtnOpenCab;
    @BindView(R.id.urgent_get_btn_open_all_cab)
    Button urgentGetBtnOpenAllCab;
    @BindView(R.id.urgent_get_gun_tv_msg)
    TextView urgentGetGunTvMsg;
    @BindView(R.id.urgent_get_btn_finish)
    Button urgentGetBtnFinish;
    Unbinder unbinder;
    private UrgentDataAdapter urgentDataAdapter;
    private List<SubCabBean> subCabsList = new ArrayList<>();
    private int index = 0;
    private int pageCount = 8;
    private Context mContext;
    private FragmentActivity activity;
    private UserBean firstPolice, secondPolice;
    private CommonLogBeanDao commonLogBeanDao;
    private boolean isQueryStatus = true;
    private CameraConfig mCameraConfig;
    private SubCabBeanDao subCabBeanDao;

    public UrgentGetGunsFragment() {
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.i(TAG, "onAttach: ");
        mContext = context;
        activity = getActivity();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_urgent_get_guns, container, false);
        unbinder = ButterKnife.bind(this, view);
        Constants.isCheckGunStatus = false;
        Constants.isIllegalGetGunAlarm = true;
        Constants.isIllegalOpenCabAlarm = true;
        //置为正在任务操作
        Constants.isExecuteTask = true;
        subCabBeanDao = DBManager.getInstance().getSubCabBeanDao();

        initData();

        return view;
    }

    private void initData() {
        urgentGetBtnPrePage.setVisibility(View.INVISIBLE);
        urgentGetBtnNextPage.setVisibility(View.INVISIBLE);

        LinearLayoutManager llm = new LinearLayoutManager(getContext(),
                LinearLayoutManager.VERTICAL, false);
        urgentGetRecyclerView.setLayoutManager(llm);
        urgentDataAdapter = new UrgentDataAdapter(subCabsList, index, pageCount, "gun");
        urgentGetRecyclerView.setAdapter(urgentDataAdapter);

//        if (!SharedUtils.getIsServerOnline()) {
            List<SubCabBean> subCabBeans = subCabBeanDao.loadAll();
//            LogUtil.i(TAG, "initData  subCabBeans: " + JSON.toJSONString(subCabBeans));
            loadData(subCabBeans);
//        } else {
//            getCabData();
//        }

        commonLogBeanDao = DBManager.getInstance().getCommonLogBeanDao();
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
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.CAMERA)
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
        urgentDataAdapter.setDisableAll();
        String message = event.getMessage();
        firstPolice = event.getApply();
        secondPolice = event.getApprove();
        Log.i(TAG, "onPostSuccessEvent message: " + message
                + "  申请人:" + firstPolice.getUserName()
                + " 审批人:" + secondPolice.getUserName());
        if (message.equals(EventConsts.EVENT_POST_SUCCESS)) {
            //提交成功
            Log.i(TAG, "onSuccessEvent 提交成功: ");
            urgentGetBtnOpenCab.setVisibility(View.VISIBLE);
            urgentGetBtnOpenLock.setVisibility(View.VISIBLE);
            urgentGetBtnFinish.setVisibility(View.VISIBLE);
            urgentGetBtnOpenAllCab.setVisibility(View.GONE);
            urgentGetBtnConfirm.setVisibility(View.GONE);
            if(Constants.isDebug || Constants.isOldBoard){
                isQueryStatus = false;
            }else{ //正式环境 开启查询
                isQueryStatus =true;
            }
            //查询枪支状态
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Log.i(TAG, "run isQueryStatus : " + isQueryStatus);
                    while (isQueryStatus) {
                        List<SubCabBean> selectedList = urgentDataAdapter.getCheckedList();
                        if (!selectedList.isEmpty()) {
                            Log.i(TAG, "run  selectedList size: " + selectedList.size());
                            for (int i = 0; i < selectedList.size(); i++) {
                                SubCabBean subCabBean = selectedList.get(i);
                                int locationNo = subCabBean.getLocationNo();
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

    /**
     * 获取枪柜数据
     */
    private void getCabData() {
        HttpClient.getInstance().getCabByMac(mContext, new HttpListener<String>() {
            @Override
            public void onSucceed(int what, Response<String> response) throws JSONException {
                try {
                    LogUtil.i(TAG, "onSucceed  getCabData response: " + response.get());
                    if (!TextUtils.isEmpty(response.get())) {
                        CabInfoBean cabInfoBean = JSON.parseObject(response.get(), CabInfoBean.class);
                        if (cabInfoBean != null) {
                            SharedUtils.saveGunCabId(cabInfoBean.getId());
                            List<SubCabBean> listLocation = cabInfoBean.getListLocation();
                            loadData(listLocation);
                        } else {
                            // ToastUtil.showShort("获取枪柜数据为空！");
                            setMsg("获取枪弹数据为空");
                        }
                    } else {
                        //                    ToastUtil.showShort("获取枪柜数据失败！");
                        setMsg("获取枪柜数据失败");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    setMsg("获取枪柜数据出错");
                }
            }

            @Override
            public void onFailed(int what, Response<String> response) {
                setMsg("网络错误，获取枪柜数据失败！");
            }
        });
    }

    private void loadData(List<SubCabBean> listLocation) {
        if (!listLocation.isEmpty()) {
            subCabsList.clear();
            for (SubCabBean subCabBean : listLocation) {
                String isUse = subCabBean.getIsUse();
                String gunState = subCabBean.getGunState();
                String isTemporary = subCabBean.getIsTemporary();
                //判断已存放枪支和枪支在库
                if (!TextUtils.isEmpty(isUse) && !TextUtils.isEmpty(gunState)) {
                    if (isUse.equals("yes") && gunState.equals("in")) {
                        String locationType = subCabBean.getLocationType();
                        if(!TextUtils.isEmpty(locationType)){
                            if (locationType.equals("shortGun") || locationType.equals("longGun")) {
                                if (!TextUtils.isEmpty(isTemporary) && isTemporary.equals("no")) {
                                    subCabsList.add(subCabBean);
                                }
                            }
                        }
                    }
                }
            }
            if (!subCabsList.isEmpty()) {
                Collections.sort(subCabsList);
                urgentDataAdapter.setSubCabsBeanList(subCabsList);
                urgentDataAdapter.notifyDataSetChanged();
                initPreNextBtn();

                if (SharedUtils.getIsCaptureOpen()) {
                    configCamera();
                }
            } else {
                //提示没有可操作数据
                setMsg("没有可领取枪支数据");
            }
        } else {
            setMsg("获取枪弹数据为空");
        }
    }

    private void setMsg(String msg) {
        if (urgentGetRecyclerView != null) {
            urgentGetRecyclerView.setVisibility(View.INVISIBLE);
        }
        if (urgentGetGunTvMsg != null) {
            urgentGetGunTvMsg.setVisibility(View.VISIBLE);
        }
        if (urgentGetGunTvMsg != null) {
            urgentGetGunTvMsg.setText(msg);
        }
    }

    private void initPreNextBtn() {
        if (subCabsList.isEmpty()) {
            urgentGetTvCurPage.setText(index + 1 + "/1");
        } else {
            if (subCabsList.size() <= pageCount) {
                urgentGetBtnNextPage.setVisibility(View.INVISIBLE);
            } else {
                urgentGetBtnNextPage.setVisibility(View.VISIBLE);
            }
            urgentGetTvCurPage.setText(index + 1 + "/" + (int) Math.ceil((double) subCabsList.size() / pageCount));
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
        if (unbinder != null) {
            unbinder.unbind();
        }
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }

        Constants.isCheckGunStatus = true;
        Constants.isIllegalGetGunAlarm = false;
        Constants.isIllegalOpenCabAlarm = false;
        //置为正在任务操作
        Constants.isExecuteTask = false;
        isQueryStatus = false;
    }

    @OnClick({R.id.urgent_get_btn_pre_page, R.id.urgent_get_btn_next_page,
            R.id.urgent_get_btn_open_lock, R.id.urgent_get_btn_confirm, R.id.urgent_get_btn_open_cab,
            R.id.urgent_get_btn_open_all_cab, R.id.urgent_get_btn_finish})
    public void onViewClicked(View view) {
        List<SubCabBean> checkedList = urgentDataAdapter.getCheckedList();
        switch (view.getId()) {
            case R.id.urgent_get_btn_pre_page://上一页
                prePager();
                break;
            case R.id.urgent_get_btn_next_page://下一页
                nexPager();
                break;
            case R.id.urgent_get_btn_open_cab: //开柜
                SharedUtils.setIsCheckStatus(false);//查询状态
                SerialPortUtil.getInstance().openLock(SharedUtils.getLeftCabNo());
                SoundPlayUtil.getInstance().play(R.raw.gun_cab_open);
                DBManager.getInstance().insertCommLog(mContext, firstPolice, firstPolice.getUserName() + "打开枪柜门");
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
            case R.id.urgent_get_btn_open_lock:  //打开枪锁
                SharedUtils.setIsCheckStatus(false);//查询状态
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        List<SubCabBean> checkedList = urgentDataAdapter.getCheckedList();
                        if (!checkedList.isEmpty()) {
                            for (SubCabBean goTaskInfoBean : checkedList) {
                                int locationNo = goTaskInfoBean.getLocationNo();
                                SerialPortUtil.getInstance().openLock(locationNo);
                                try {
                                    Thread.sleep(300);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                DBManager.getInstance().insertCommLog(mContext, firstPolice,
                                        firstPolice.getUserName() + "打开" + locationNo + "号枪锁");
                            }
                            SoundPlayUtil.getInstance().play(R.raw.gun_lock_open);
                            try {
                                Thread.sleep(300);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            SharedUtils.setIsCheckStatus(true);//查询状态
                        }
                    }
                }).start();
                break;
            case R.id.urgent_get_btn_confirm://提交领枪数据
                if (!checkedList.isEmpty()) {//提交数据
                    String jsonString = JSON.toJSONString(checkedList);
                    LogUtil.i(TAG, "onViewClicked jsonString: " + jsonString);
                    Intent intent = new Intent(mContext, VerifyActivity.class);
                    intent.putExtra("activity", Constants.ACTIVITY_URGENT_GET_GUN);
                    intent.putExtra("data", jsonString);
                    startActivity(intent);
                }
                break;
            case R.id.urgent_get_btn_open_all_cab://选择全部枪支
                String openAll = urgentGetBtnOpenAllCab.getText().toString();
                if (openAll.equals("选择全部")) {
                    urgentDataAdapter.selectAll();
                    urgentGetBtnOpenAllCab.setText("取消全选");
                } else {
                    urgentDataAdapter.setSubCabsBeanList(subCabsList);
                    urgentGetBtnOpenAllCab.setText("选择全部");
                }
                break;
            case R.id.urgent_get_btn_finish:
                if (Constants.isDebug || Constants.isOldBoard) {
                    activity.finish();
                    isQueryStatus = false;
                } else {
                    if (gunStatusMap.isEmpty()) {
                        ToastUtil.showShort("枪支未取出");
                        SoundPlayUtil.getInstance().play(R.raw.gun_not_out);
                        return;
                    }
                    if (!checkedList.isEmpty()) {
                        for (SubCabBean subCabBean : checkedList) {
                            int locationNo = subCabBean.getLocationNo();
                            if (gunStatusMap.containsKey(locationNo)) {
                                //枪支状态  true 在位 false 不在位
                                boolean aBoolean = gunStatusMap.get(locationNo);
                                if (aBoolean) {
                                    Log.i(TAG, "onViewClicked  : " + locationNo + "号枪支未取出");
                                    ToastUtil.showShort(locationNo + "号枪支未取出");
                                    SoundPlayUtil.getInstance().play(R.raw.gun_not_out);
                                    return;
                                }
                            }
                        }
                    }
                    //停止查询枪支状态
                    isQueryStatus = false;

                    if (SharedUtils.getCabOpenStatus() == 1) { //柜门开启状态
                        ToastUtil.showShort("柜门未关闭!");
                        SoundPlayUtil.getInstance().play(R.raw.cab_not_close);
                    } else {
                        activity.finish();
                    }
                }
                break;
        }
    }

    private void prePager() {
        index--;
        urgentDataAdapter.setIndex(index);
        urgentGetTvCurPage.setText(index + 1 + "/" + (int) Math.ceil((double) subCabsList.size() / pageCount));
        //隐藏上一个或下一个按钮
        checkButton();
        Log.i(TAG, "prePager index: " + index);
    }

    private void nexPager() {
        index++;
        urgentDataAdapter.setIndex(index);
        urgentGetTvCurPage.setText(index + 1 + "/" + (int) Math.ceil((double) subCabsList.size() / pageCount));
        //隐藏上一个或下一个按钮
        checkButton();
        Log.i(TAG, "nexPager index: " + index);
    }

    private void checkButton() {
        if (index <= 0) {
            urgentGetBtnPrePage.setVisibility(View.INVISIBLE);
            urgentGetBtnNextPage.setVisibility(View.VISIBLE);
        } else if (subCabsList.size() - index * pageCount <= pageCount) {    //数据总数减每页数当小于每页可显示的数字时既是最后一页
            urgentGetBtnPrePage.setVisibility(View.VISIBLE);
            urgentGetBtnNextPage.setVisibility(View.INVISIBLE);
        } else {
            urgentGetBtnNextPage.setVisibility(View.VISIBLE);
            urgentGetBtnPrePage.setVisibility(View.VISIBLE);
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
