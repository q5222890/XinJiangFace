package com.zack.xjht.ui.fragment;


import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.GridLayoutManager;
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
import com.zack.xjht.Utils.SharedUtils;
import com.zack.xjht.Utils.Utils;
import com.zack.xjht.adapter.TempInfoAdapter;
import com.zack.xjht.entity.CabInfoBean;
import com.zack.xjht.entity.SubCabBean;
import com.zack.xjht.entity.UserBean;
import com.zack.xjht.http.HttpClient;
import com.zack.xjht.http.HttpListener;
import com.zack.xjht.ui.dialog.TemporaryDialog;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * 临时存放枪支
 */
public class TempInFragment extends HiddenCameraFragment {
    private static final String TAG = "TempInFragment";
    private static final int REQ_CODE_CAMERA_PERMISSION = 133;
    @BindView(R.id.temp_in_tittle)
    TextView tempInTittle;
    @BindView(R.id.temp_in_recycler_view)
    RecyclerView tempInRecyclerView;
    @BindView(R.id.temp_in_finish)
    Button tempInFinish;
    @BindView(R.id.temp_in_ll_view)
    LinearLayout tempInLlView;
    Unbinder unbinder;
    @BindView(R.id.temp_in_tv_msg)
    TextView tempInTvMsg;
    @BindView(R.id.temp_in_btn_pre_page)
    Button tempInBtnPrePage;
    @BindView(R.id.temp_in_tv_cur_page)
    TextView tempInTvCurPage;
    @BindView(R.id.temp_in_btn_next_page)
    Button tempInBtnNextPage;
    private TempInfoAdapter adapter;
    private List<SubCabBean> subCabsList;
    private UserBean manager1, manager2;
    private Context mContext;
    private FragmentActivity mActivity;
    private CabInfoBean cabInfoBean;
    private int index = 0;
    private int pageCount = 24;
    private CameraConfig mCameraConfig;

    public TempInFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_temp_in, container, false);
        unbinder = ButterKnife.bind(this, view);
        initData();
        Constants.isIllegalOpenCabAlarm = true;
        //置为正在任务操作
        Constants.isExecuteTask = true;
        return view;
    }

    private void initData() {
        subCabsList = new ArrayList<>();
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 6);
        tempInRecyclerView.setLayoutManager(gridLayoutManager);
        adapter = new TempInfoAdapter(subCabsList, index, pageCount);
        tempInRecyclerView.setAdapter(adapter);
        adapter.setOnItemClickListener(new TempInfoAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View v, final int position) {
                Log.i(TAG, "onItemClick  position:" + position);
                //点击设置临时存放枪支数据
                SubCabBean subCabsBean = subCabsList.get(position);
                if (subCabsBean != null) {
                    Log.i(TAG, "onItemClick subCabsBean: " + JSON.toJSONString(subCabsBean));
                    TemporaryDialog temporaryDialog = new TemporaryDialog(mContext,
                            subCabsBean, manager1, manager2);
                    if (!temporaryDialog.isShowing()) {
                        temporaryDialog.show();
                    }
                }
            }
        });
        if (Utils.isNetworkAvailable()) {
            getCabData();
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

    @Override
    public void onResume() {
        super.onResume();
        if (Utils.isNetworkAvailable()) {
            getCabData();
        }
    }

    /**
     * 获取枪柜数据
     */
    private void getCabData() {
        HttpClient.getInstance().getCabByMac(mContext, new HttpListener<String>() {
            @Override
            public void onSucceed(int what, Response<String> response) throws JSONException {
                Log.i(TAG, "onSucceed  getCabData response: " + response.get());
                try {
                    if (!TextUtils.isEmpty(response.get())) {
                        cabInfoBean = JSON.parseObject(response.get(), CabInfoBean.class);
                        if (cabInfoBean != null) {
                            List<SubCabBean> listLocation = cabInfoBean.getListLocation();
                            if (!listLocation.isEmpty()) {
                                subCabsList.clear();
                                for (SubCabBean subCabBean : listLocation) {
                                    String isUse = subCabBean.getIsUse();
                                    String locationType = subCabBean.getLocationType();
                                    //判断是否存放枪支
                                    if (!TextUtils.isEmpty(isUse) && isUse.equals("no")) {
                                        if (!TextUtils.isEmpty(locationType)) {
                                            if (locationType.equals("shortGun") || locationType.equals("longGun")) {
                                                subCabsList.add(subCabBean);
                                            }
                                        }
                                    }
                                }
                                Collections.sort(subCabsList);
                                adapter.notifyDataSetChanged();
                                initPreNextBtn();

                                if (SharedUtils.getIsCaptureOpen()) {
                                    configCamera();
                                }
                            } else {
                                if (tempInTvMsg != null) {
                                    tempInTvMsg.setText("获取位置数据为空");
                                }
                            }
                        } else {
                            if (tempInTvMsg != null) {
                                tempInTvMsg.setText("获取枪柜数据为空");
                            }
                        }
                    } else {
                        if (tempInTvMsg != null) {
                            tempInTvMsg.setText("获取数据为空");
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    if (tempInTvMsg != null) {
                        tempInTvMsg.setText("获取数据出错");
                    }
                }
            }

            @Override
            public void onFailed(int what, Response<String> response) {
                if (tempInTvMsg != null) {
                    tempInTvMsg.setText("网络请求出现错误，获取数据失败");
                }
            }
        });
    }

    /**
     * 显示翻页按钮
     */
    private void initPreNextBtn() {
        if (subCabsList.isEmpty()) {
            tempInTvCurPage.setText(index + 1 + "/1");
        } else {
            if (subCabsList.size() <= pageCount) {
                tempInBtnNextPage.setVisibility(View.INVISIBLE);
            } else {
                tempInBtnNextPage.setVisibility(View.VISIBLE);
            }
            tempInTvCurPage.setText(index + 1 + "/" + (int) Math.ceil((double) subCabsList.size() / pageCount));
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
        mActivity = getActivity();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();

        Constants.isIllegalOpenCabAlarm = false;
        //置为正在任务操作
        Constants.isExecuteTask = false;
    }

    @OnClick(R.id.temp_in_finish)
    public void onViewClicked() {
        Objects.requireNonNull(getActivity()).finish();
    }

    @OnClick({R.id.temp_in_btn_pre_page, R.id.temp_in_btn_next_page})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.temp_in_btn_pre_page: //上页
                prePager();
                break;
            case R.id.temp_in_btn_next_page://下页
                nexPager();
                break;
        }
    }

    private void prePager() {
        index--;
        adapter.setIndex(index);
        tempInTvCurPage.setText(index + 1 + "/" + (int) Math.ceil((double) subCabsList.size() / pageCount));
        //隐藏上一个或下一个按钮
        checkButton();
//        Log.i(TAG, "prePager index: " + index);
    }

    private void nexPager() {
        index++;
        adapter.setIndex(index);
        tempInTvCurPage.setText(index + 1 + "/" + (int) Math.ceil((double) subCabsList.size() / pageCount));
        //隐藏上一个或下一个按钮
        checkButton();
//        Log.i(TAG, "nexPager index: " + index);
    }

    private void checkButton() {
        if (index <= 0) {
            tempInBtnPrePage.setVisibility(View.INVISIBLE);
            tempInBtnNextPage.setVisibility(View.VISIBLE);
        } else if (subCabsList.size() - index * pageCount <= pageCount) {    //数据总数减每页数当小于每页可显示的数字时既是最后一页
            tempInBtnPrePage.setVisibility(View.VISIBLE);
            tempInBtnNextPage.setVisibility(View.INVISIBLE);
        } else {
            tempInBtnNextPage.setVisibility(View.VISIBLE);
            tempInBtnPrePage.setVisibility(View.VISIBLE);
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
