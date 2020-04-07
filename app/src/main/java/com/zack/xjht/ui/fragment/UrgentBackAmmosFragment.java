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
import com.zack.xjht.entity.UrgentBackBean;
import com.zack.xjht.entity.UrgentGetListBean;
import com.zack.xjht.entity.UrgentOutBean;
import com.zack.xjht.entity.UrgentTaskBean;
import com.zack.xjht.entity.UrgentTaskInfoBean;
import com.zack.xjht.entity.UserBean;
import com.zack.xjht.event.EventConsts;
import com.zack.xjht.event.MessageEvent;
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
 * 紧急弹药归还
 */
public class UrgentBackAmmosFragment extends HiddenCameraFragment {
    private static final String TAG = "UrgentBackAmmosFragment";
    private static final int REQ_CODE_CAMERA_PERMISSION = 133;

    @BindView(R.id.urgent_back_ammos_ll_tittle)
    LinearLayout urgentBackAmmosLlTittle;
    @BindView(R.id.urgent_back_recycler_view)
    RecyclerView urgentBackRecyclerView;
    @BindView(R.id.urgent_back_btn_pre_page)
    Button urgentBackBtnPrePage;
    @BindView(R.id.urgent_back_tv_cur_page)
    TextView urgentBackTvCurPage;
    @BindView(R.id.urgent_back_btn_next_page)
    Button urgentBackBtnNextPage;
    @BindView(R.id.urgent_back_btn_open_lock)
    Button urgentBackBtnOpenLock;
    @BindView(R.id.urgent_back_btn_finish)
    Button urgentBackBtnFinish;
    @BindView(R.id.urgent_back_bottom_view)
    LinearLayout urgentBackBottomView;
    Unbinder unbinder;
    @BindView(R.id.back_rv_task_list)
    RecyclerView backRvTaskList;
    @BindView(R.id.urgent_back_rl_task_info)
    RelativeLayout urgentBackRlTaskInfo;
    @BindView(R.id.urgent_back_btn_open_cab)
    Button urgentBackBtnOpenCab;
    @BindView(R.id.urgent_back_ammo_tv_msg)
    TextView urgentBackAmmoTvMsg;
    @BindView(R.id.urgent_back_btn_confirm)
    Button urgentBackBtnConfirm;
    private int index = 0;
    private int pageCount = 8;
    private BackDataAdapter backDataAdapter;
    private List<UrgentGetListBean> operBeanList;
    private UserBean apply, approve;
    private Context mContext;
    private FragmentActivity mActivity;
    private List<UrgentOutBean> urgentTaskList;
    private UrgentTaskAdapter urgentTaskAdapter;
    private Long urgentTaskId;
    private CommonLogBeanDao commonLogBeanDao;
    private CameraConfig mCameraConfig;
    private long startTime;

    public UrgentBackAmmosFragment() {
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.i(TAG, "onAttach: ");
        mContext = context;
        mActivity = getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_urgent_back_ammos, container, false);
        unbinder = ButterKnife.bind(this, view);
         startTime = System.currentTimeMillis();
        Constants.isCheckGunStatus =false;
        Constants.isIllegalGetGunAlarm =true;
        Constants.isIllegalOpenCabAlarm =true;
        //置为正在任务操作
        Constants.isExecuteTask =true;
        initData();

        return view;
    }

    private void initData() {
        urgentTaskList = new ArrayList<>();
        operBeanList = new ArrayList<>();
        urgentBackBtnPrePage.setVisibility(View.INVISIBLE);
        urgentBackBtnNextPage.setVisibility(View.INVISIBLE);
        urgentBackRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(),
                LinearLayoutManager.VERTICAL, false));

        backRvTaskList.setLayoutManager(new LinearLayoutManager(mContext,
                LinearLayout.VERTICAL, false));
        urgentTaskAdapter = new UrgentTaskAdapter(urgentTaskList, index, pageCount);
        backRvTaskList.setAdapter(urgentTaskAdapter);

        urgentTaskAdapter.setOnTaskIdListener(new UrgentTaskAdapter.OnTaskIdListener() {
            @Override
            public void onTaskId(Long taskId) {
                urgentTaskId = taskId;
                if (Utils.isNetworkAvailable()) {
//                    getTaskInfo(urgentTaskId);
                }else{
                    if(urgentBackAmmoTvMsg !=null){
                        urgentBackAmmoTvMsg.setVisibility(View.VISIBLE);
                        urgentBackAmmoTvMsg.setText("网络断开，无法获取数据");
                    }
                }
            }
        });
        if (Utils.isNetworkAvailable()) {
            getUrgentBackData();
        }else{
            if(urgentBackAmmoTvMsg !=null){
                urgentBackAmmoTvMsg.setVisibility(View.VISIBLE);
                urgentBackAmmoTvMsg.setText("网络断开，无法获取任务");
            }
        }
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
                if(SharedUtils.getIsCaptureOpen()){
                    takePicture();
                }
            }
        }).start();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSuccessEvent(MessageEvent event) {
        backDataAdapter.setDisableAll();
        String message = event.getMessage();
         apply = event.getApply();
         approve = event.getApprove();
        Log.i(TAG, "onPostSuccessEvent message: " + message
                +"  申请人:"+apply.getUserName()
                +" 审批人:"+approve.getUserName());
        if (message.equals(EventConsts.EVENT_POST_SUCCESS)) {
            //提交成功
            Log.i(TAG, "onSuccessEvent 提交成功: ");
            urgentBackBtnOpenCab.setVisibility(View.VISIBLE);
            urgentBackBtnOpenLock.setVisibility(View.VISIBLE);
            urgentBackBtnFinish.setVisibility(View.VISIBLE);
            urgentBackBtnConfirm.setVisibility(View.GONE);

        } else if(message.equals(EventConsts.EVENT_POST_FAILURE)){
            //提交失败
            Log.i(TAG, "onSuccessEvent 提交失败: ");
        }
    }

    private void getUrgentBackData() {
        urgentBackAmmoTvMsg.setVisibility(View.INVISIBLE);
        HttpClient.getInstance().getUrgentTask(mContext, new HttpListener<String>() {
            @Override
            public void onSucceed(int what, Response<String> response) throws JSONException {
                Log.i(TAG, "onSucceed getUrgentBackData: "+(System.currentTimeMillis() -startTime));
                LogUtil.i(TAG, "onSucceed getUrgentTask response: " + response.get());
                try {
                    if (!TextUtils.isEmpty(response.get())) {
                        List<UrgentOutBean> urgentTaskBeans = JSON.parseArray(response.get(), UrgentOutBean.class);
                        if (!urgentTaskBeans.isEmpty()) {
                            urgentTaskAdapter.setList(urgentTaskBeans);
                            urgentTaskAdapter.notifyDataSetChanged();
                        } else {
                            urgentBackAmmoTvMsg.setVisibility(View.VISIBLE);
                            urgentBackAmmoTvMsg.setText("获取任务失败");
                        }
                    } else {
                        urgentBackAmmoTvMsg.setVisibility(View.VISIBLE);
                        urgentBackAmmoTvMsg.setText("获取数据为空");
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

    /**
     * 通过taskId获取对应的任务数据
     * @param taskId
     */
    private void getTaskInfo(String taskId) {
        urgentBackAmmoTvMsg.setVisibility(View.INVISIBLE);
        HttpClient.getInstance().getUrgentTaskInfo(mContext, taskId, new HttpListener<String>() {
            @Override
            public void onSucceed(int what, Response<String> response) throws JSONException {
                Log.i(TAG, "onSucceed getTaskInfo: "+(System.currentTimeMillis() -startTime));
                Log.i(TAG, "onSucceed getUrgentTaskInfo response: " + response.get());
                try {
                    if (!TextUtils.isEmpty(response.get())) {
                        List<UrgentGetListBean> urgentTaskInfoBeans = JSON.parseArray(response.get(), UrgentGetListBean.class);
                        if (!urgentTaskInfoBeans.isEmpty()) {
                            operBeanList.clear();
                            for (UrgentGetListBean urgentTaskInfo : urgentTaskInfoBeans) {
                                String locationType = urgentTaskInfo.getLocationType();
                                if (locationType.equals(Constants.TYPE_AMMO)) {
                                    operBeanList.add(urgentTaskInfo);
                                }
                            }
                            if (!operBeanList.isEmpty()) {
                                Collections.sort(operBeanList);
                                backDataAdapter = new BackDataAdapter(operBeanList, index, pageCount);
                                urgentBackRecyclerView.setAdapter(backDataAdapter);
                                backDataAdapter.notifyDataSetChanged();
                                initPreNextBtn();

                                if(SharedUtils.getIsCaptureOpen()){
                                    configCamera();
                                }
                            } else {
                                urgentBackAmmoTvMsg.setVisibility(View.VISIBLE);
                                urgentBackAmmoTvMsg.setText("没有可归还数据");
                            }
                        } else {
                            urgentBackAmmoTvMsg.setVisibility(View.VISIBLE);
                            urgentBackAmmoTvMsg.setText("获取到任务清单数据失败");
                        }
                    } else {
                        urgentBackAmmoTvMsg.setVisibility(View.VISIBLE);
                        urgentBackAmmoTvMsg.setText("获取任务清单数据为空");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailed(int what, Response<String> response) {
                urgentBackAmmoTvMsg.setText("网络请求出错，获取数据失败！");
            }
        });
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
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
        Constants.isIllegalGetGunAlarm =false;
        Constants.isIllegalOpenCabAlarm =false;
        Constants.isCheckGunStatus =true;
        //置为正在任务操作
        Constants.isExecuteTask =false;
        EventBus.getDefault().unregister(this);
    }

    @OnClick({R.id.urgent_back_btn_pre_page, R.id.urgent_back_btn_next_page,
            R.id.urgent_back_btn_open_lock, R.id.urgent_back_btn_finish,
            R.id.urgent_back_btn_open_cab, R.id.urgent_back_btn_confirm})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.urgent_back_btn_pre_page://上一页
                prePager();
                break;
            case R.id.urgent_back_btn_next_page://下一页
                nexPager();
                break;
            case R.id.urgent_back_btn_open_cab: //开柜
                String cabType = SharedUtils.getCabType();
                if (cabType.equals(Constants.TYPE_MIX_CAB)) {
                    SerialPortUtil.getInstance().openLock(SharedUtils.getRightCabNo());
                } else {
                    SerialPortUtil.getInstance().openLock(SharedUtils.getLeftCabNo());
                }
                SoundPlayUtil.getInstance().play(R.raw.bullet_cab_open);
                DBManager.getInstance().insertCommLog(mContext, apply,
                        apply.getUserName() + "打开弹柜门");

                SerialPortUtil.getInstance().openLED();//打开枪锁数码管led
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                SerialPortUtil.getInstance().openLED(SharedUtils.getPowerAddress());//打开枪锁数码管led
                break;
            case R.id.urgent_back_btn_open_lock:// 打开枪锁
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        List<UrgentGetListBean> checkedList = backDataAdapter.getCheckedList();
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
                                        apply.getUserName() + "打开"+locationNo+"号弹仓");
                            }
                            SoundPlayUtil.getInstance().play(R.raw.bullet_subcab_open);
                        }
                    }
                }).start();
                break;
            case R.id.urgent_back_btn_confirm:
                List<UrgentGetListBean> checkedList = backDataAdapter.getCheckedList();
                if (checkedList.isEmpty() || checkedList.size() !=operBeanList.size() ) {
                    Log.i(TAG, "onViewClicked checked list null: ");
                    ToastUtil.showShort("请选择归还所有弹药");
                    return;
                }
                backDataAdapter.setConfirm(true);
                Map<String, Object> params = new HashMap<>();
                params.put("id", urgentTaskId);
                List<UrgentBackBean> urgentBackBeanList = new ArrayList<>();
                for (UrgentGetListBean taskInfo : checkedList) {
                    UrgentBackBean urgentBackBean = new UrgentBackBean();
                    urgentBackBean.setGunCabinetLocationId(taskInfo.getGunCabinetLocationId());
//                    urgentBackBean.setId(taskInfo.getId());
                    urgentBackBean.setInObjectNumber(String.valueOf(taskInfo.getOutObjectNumber()));
                    urgentBackBean.setLocationType(taskInfo.getLocationType());
                    urgentBackBean.setObjectId(taskInfo.getObjectId());
                    urgentBackBeanList.add(urgentBackBean);
                }
                params.put("urgentTaskList", urgentBackBeanList);
                String jsonBody = JSON.toJSONString(params, SerializerFeature.WriteMapNullValue);
                Log.i(TAG, "onViewClicked  jsonBody: " + jsonBody);
//                postUrgentBackData(jsonBody);

//                Intent intent = new Intent(mContext, LoginActivity.class);
                Intent intent = new Intent(mContext, VerifyActivity.class);
                intent.putExtra("activity", Constants.ACTIVITY_URGENT_BACK_AMMO);
                intent.putExtra("data", jsonBody);
                startActivity(intent);
                break;
            case R.id.urgent_back_btn_finish://提交数据
                if (Constants.isDebug || Constants.isOldBoard) {
                    mActivity.finish();
                }else {
                    if (SharedUtils.getCabOpenStatus() == 1) {
                        ToastUtil.showShort("柜门未关闭");
                    } else {
                        mActivity.finish();
                    }
                }
                break;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    private void prePager() {
        index--;
        backDataAdapter.setIndex(index);
        urgentBackTvCurPage.setText(index + 1 + "/" + (int) Math.ceil((double) operBeanList.size() / pageCount));
        //隐藏上一个或下一个按钮
        checkButton();
        Log.i(TAG, "prePager index: " + index);
    }

    private void nexPager() {
        index++;
        backDataAdapter.setIndex(index);
        urgentBackTvCurPage.setText(index + 1 + "/" + (int) Math.ceil((double) operBeanList.size() / pageCount));
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
