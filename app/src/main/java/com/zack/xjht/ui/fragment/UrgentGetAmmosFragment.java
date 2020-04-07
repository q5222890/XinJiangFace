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
import android.support.annotation.Nullable;
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
import com.zack.xjht.entity.CabInfoBean;
import com.zack.xjht.entity.SubCabBean;
import com.zack.xjht.entity.UrgentOutBean;
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
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;


/**
 * 紧急领取弹药接口
 */
public class UrgentGetAmmosFragment extends HiddenCameraFragment {
    private static final String TAG = "UrgentGetAmmosFragment";
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
    Unbinder unbinder;
    @BindView(R.id.urgent_get_btn_open_all)
    Button urgentGetBtnOpenAll;
    @BindView(R.id.urgent_get_btn_open_cab)
    Button urgentGetBtnOpenCab;
    @BindView(R.id.urgent_get_ammo_tv_msg)
    TextView urgentGetAmmoTvMsg;
    @BindView(R.id.urgent_get_btn_finish)
    Button urgentGetBtnFinish;
    private UrgentDataAdapter urgentDataAdapter;
    private List<SubCabBean> subCabsList = new ArrayList<>();
    private int index = 0;
    private int pageCount = 8;
    private Context mContext;
    private FragmentActivity activity;
    private UserBean firstPolice, secondPolice;
    private CabInfoBean cabInfoBean;
    private CommonLogBeanDao commonLogBeanDao;
    private CameraConfig mCameraConfig;
    private long startTime;

    public UrgentGetAmmosFragment() {
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
        Log.i(TAG, "onCreateView: ");
        View view = inflater.inflate(R.layout.fragment_urgent_get_ammos, container, false);
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
        urgentGetBtnPrePage.setVisibility(View.INVISIBLE);
        urgentGetBtnNextPage.setVisibility(View.INVISIBLE);

        LinearLayoutManager llm = new LinearLayoutManager(getContext(),
                LinearLayoutManager.VERTICAL, false);
        urgentGetRecyclerView.setLayoutManager(llm);
        urgentDataAdapter = new UrgentDataAdapter(subCabsList, index, pageCount, "ammo");
        urgentGetRecyclerView.setAdapter(urgentDataAdapter);

        if (Utils.isNetworkAvailable()) {
            getCabData();
        } else {
            urgentGetAmmoTvMsg.setText("请检查网络连接");
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
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            //Start camera preview
            startCamera(mCameraConfig);
        }

        Log.i(TAG, "configCamera: " + (System.currentTimeMillis() - startTime));
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

    @Override
    public void onResume() {
        super.onResume();
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
            urgentGetBtnOpenAll.setVisibility(View.GONE);
            urgentGetBtnConfirm.setVisibility(View.GONE);
        } else if (message.equals(EventConsts.EVENT_POST_FAILURE)) {
            //提交失败
            Log.i(TAG, "onSuccessEvent 提交失败: ");
        }
    }

    private void getCabData() {
        HttpClient.getInstance().getCabByMac(mContext, new HttpListener<String>() {
            @Override
            public void onSucceed(int what, Response<String> response) throws JSONException {
                Log.i(TAG, "onSucceed endTime: " + (System.currentTimeMillis() - startTime));
                try {
                    LogUtil.i(TAG, "onSucceed  getCabData response: " + response.get());
                    if (!TextUtils.isEmpty(response.get())) {
                        cabInfoBean = JSON.parseObject(response.get(), CabInfoBean.class);
                        if (cabInfoBean != null) {
                            List<SubCabBean> listLocation = cabInfoBean.getListLocation();
                            if (!listLocation.isEmpty()) {
                                subCabsList.clear();
                                for (SubCabBean subCabBean : listLocation) {
                                    String isUse = subCabBean.getIsUse();
                                    if (!TextUtils.isEmpty(isUse) && isUse.equals("yes")) {
                                        String locationType = subCabBean.getLocationType();
                                        int objectNumber = subCabBean.getObjectNumber();
                                        if(!TextUtils.isEmpty(locationType)){
                                            if (locationType.equals(Constants.TYPE_AMMO) && objectNumber > 0) {
                                                subCabsList.add(subCabBean);
                                            }
                                        }
                                    }
                                }
                                Collections.sort(subCabsList);
                                if (!subCabsList.isEmpty()) {
                                    urgentDataAdapter.setSubCabsBeanList(subCabsList);
                                    urgentDataAdapter.notifyDataSetChanged();
                                    initPreNextBtn();

                                    if (SharedUtils.getIsCaptureOpen()) {
                                        configCamera();
                                    }
                                } else {
                                    if (urgentGetAmmoTvMsg != null) {
                                        urgentGetAmmoTvMsg.setText("没有可领取弹药数据");
                                    }
                                }
                            } else {
                                //                            ToastUtil.showShort("获取枪弹数据为空");
                                if (urgentGetAmmoTvMsg != null) {
                                    urgentGetAmmoTvMsg.setText("获取枪弹数据为空");
                                }
                            }
                        } else {
                            if (urgentGetAmmoTvMsg != null) {
                                urgentGetAmmoTvMsg.setText("获取枪柜数据为空");
                            }
                        }
                    } else {
                        if (urgentGetAmmoTvMsg != null) {
                            urgentGetAmmoTvMsg.setText("获取枪柜数据失败");
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailed(int what, Response<String> response) {
                if (urgentGetAmmoTvMsg != null) {
                    urgentGetAmmoTvMsg.setText("请求出错");
                }
            }
        });
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
    public void onDestroyView() {
        super.onDestroyView();
        Log.i(TAG, "onDestroyView: ");
        if (unbinder != null) {
            unbinder.unbind();
        }
        Constants.isIllegalGetGunAlarm =false;
        Constants.isIllegalOpenCabAlarm =false;
        Constants.isCheckGunStatus =true;
        //置为正在任务操作
        Constants.isExecuteTask =false;
        if(EventBus.getDefault().isRegistered(this)){
            EventBus.getDefault().unregister(this);
        }
    }

    @OnClick({R.id.urgent_get_btn_pre_page, R.id.urgent_get_btn_next_page,
            R.id.urgent_get_btn_open_lock, R.id.urgent_get_btn_confirm, R.id.urgent_get_btn_open_all,
            R.id.urgent_get_btn_open_cab, R.id.urgent_get_btn_finish})
    public void onViewClicked(View view) {
        final String cabType = SharedUtils.getCabType();
        switch (view.getId()) {
            case R.id.urgent_get_btn_pre_page://上一页
                prePager();
                break;
            case R.id.urgent_get_btn_next_page://下一页
                nexPager();
                break;
            case R.id.urgent_get_btn_open_all: //选择全部弹药
                urgentDataAdapter.selectAll();
                break;
            case R.id.urgent_get_btn_open_cab: //打开枪柜
                if (cabType.equals(Constants.TYPE_MIX_CAB)) {
                    SerialPortUtil.getInstance().openLock(SharedUtils.getRightCabNo());
                } else {
                    SerialPortUtil.getInstance().openLock(SharedUtils.getLeftCabNo());
                }
                SoundPlayUtil.getInstance().play(R.raw.bullet_cab_open);
                DBManager.getInstance().insertCommLog(mContext, firstPolice,
                        firstPolice.getUserName() + "打开弹柜门");
                SerialPortUtil.getInstance().openLED();//打开枪锁数码管led
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                SerialPortUtil.getInstance().openLED(SharedUtils.getPowerAddress());//打开枪锁数码管led
                break;
            case R.id.urgent_get_btn_open_lock://打开枪锁
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
                                        firstPolice.getUserName() + "打开" + locationNo + "号弹仓");
                            }
                            SoundPlayUtil.getInstance().play(R.raw.bullet_subcab_open);
                        }
                    }
                }).start();
                break;
            case R.id.urgent_get_btn_confirm://确认
                List<SubCabBean> checkedList = urgentDataAdapter.getCheckedList();
                LogUtil.i(TAG, "onViewClicked checked list : " + JSON.toJSONString(checkedList));
                if (checkedList.isEmpty()) {
                    ToastUtil.showShort("没有选择枪支或弹药");
                    return;
                }
                Intent intent = new Intent(mContext, VerifyActivity.class);
                intent.putExtra("activity", Constants.ACTIVITY_URGENT_GET_AMMO);
                intent.putExtra("data", JSON.toJSONString(checkedList));
                intent.putExtra("cabId", cabInfoBean.getId());
                startActivity(intent);
                break;
            case R.id.urgent_get_btn_finish:
                if (Constants.isDebug || Constants.isOldBoard) {
                    activity.finish();
                }else {
                    if (SharedUtils.getCabOpenStatus() == 1) {
                        ToastUtil.showShort("柜门未关闭");
                    } else {
                        activity.finish();
                    }
                }
                break;
        }
    }

    private void openLockAndPostTask(List<SubCabBean> checkedList) {
        UrgentOutBean urgentOutBean = new UrgentOutBean();
        urgentOutBean.setApply(String.valueOf(firstPolice.getUserId()));
        urgentOutBean.setApproval(String.valueOf(secondPolice.getUserId()));
        if (cabInfoBean != null) {
            urgentOutBean.setGunCabinetId(cabInfoBean.getId());
        }
        urgentOutBean.setOutTime(Utils.longTime2String(System.currentTimeMillis()));
        List<UrgentTaskListBean> urgentList = new ArrayList<>();
        if (!checkedList.isEmpty()) {
            for (SubCabBean subCabBean : checkedList) {
                UrgentTaskListBean urgentTaskBean = new UrgentTaskListBean();
                urgentTaskBean.setGunCabinetLocationId(subCabBean.getId());
                urgentTaskBean.setLocationType(subCabBean.getLocationType());
                urgentTaskBean.setObjectId(subCabBean.getObjectId());
                urgentTaskBean.setOutObjectNumber(String.valueOf(subCabBean.getObjectNumber()));
                urgentList.add(urgentTaskBean);
            }
        }
//        urgentOutBean.setUrgentTaskList(urgentList);
//        String jsonString = JSON.toJSONString(urgentOutBean, SerializerFeature.WriteMapNullValue);
//        LogUtil.i(TAG, "openLockAndPostTask  jsonString: " + jsonString);
//        postGetGunData(jsonString);
    }

    private void postGetGunData(String jsonString) {
        HttpClient.getInstance().postUrgentGet(mContext, jsonString, new HttpListener<String>() {
            @Override
            public void onSucceed(int what, Response<String> response) throws JSONException {
                Log.i(TAG, "onSucceed postGetGunData response: " + response.get());
                try {
                    if (!TextUtils.isEmpty(response.get())) {
                        if (response.get().equals("success")) {
                            activity.finish();
                        } else {
                            Log.i(TAG, "onSucceed 提交失败: ");
                        }
                    } else {
                        Log.i(TAG, "onSucceed 提交失败: ");
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
//        urgentDataAdapter.setIndex(index);
        urgentGetTvCurPage.setText(index + 1 + "/" + (int) Math.ceil((double) subCabsList.size() / pageCount));
        //隐藏上一个或下一个按钮
        checkButton();
        Log.i(TAG, "prePager index: " + index);
    }

    private void nexPager() {
        index++;
//        urgentDataAdapter.setIndex(index);
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
    public void onPause() {
        super.onPause();
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
