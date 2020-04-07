package com.zack.xjht.ui;

import android.app.Dialog;
import android.content.pm.ActivityInfo;
import android.graphics.Point;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.arcsoft.face.ErrorInfo;
import com.arcsoft.face.FaceEngine;
import com.arcsoft.face.FaceFeature;
import com.arcsoft.face.FaceInfo;
import com.arcsoft.face.LivenessInfo;
import com.arcsoft.face.VersionInfo;
import com.arcsoft.face.enums.DetectFaceOrientPriority;
import com.arcsoft.face.enums.DetectMode;
import com.yanzhenjie.nohttp.rest.Response;
import com.zack.xjht.Constants;
import com.zack.xjht.R;
import com.zack.xjht.Utils.DialogUtils;
import com.zack.xjht.Utils.LogUtil;
import com.zack.xjht.Utils.SharedUtils;
import com.zack.xjht.Utils.ToastUtil;
import com.zack.xjht.Utils.TransformUtil;
import com.zack.xjht.Utils.Utils;
import com.zack.xjht.db.DBManager;
import com.zack.xjht.entity.UserBean;
import com.zack.xjht.entity.UserBiosBean;
import com.zack.xjht.face.faceserver.FaceServer;
import com.zack.xjht.face.model.FacePreviewInfo;
import com.zack.xjht.face.util.ConfigUtil;
import com.zack.xjht.face.util.DrawHelper;
import com.zack.xjht.face.util.camera.CameraHelper;
import com.zack.xjht.face.util.camera.CameraListener;
import com.zack.xjht.face.util.face.FaceHelper;
import com.zack.xjht.face.util.face.FaceListener;
import com.zack.xjht.http.HttpClient;
import com.zack.xjht.http.HttpListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * 用于人脸注册
 */
public class RegisterFaceActivity extends AppCompatActivity implements ViewTreeObserver.OnGlobalLayoutListener, FaceServer.onRegisterResultListener {
    private static final String TAG = "RegisterFaceActivity";

    @BindView(R.id.single_camera_texture_preview)
    TextureView previewView;
    @BindView(R.id.face_register_tv_msg)
    TextView faceRegisterTvMsg;
    @BindView(R.id.face_register_btn)
    Button faceRegisterBtn;

    private static final int MAX_DETECT_NUM = 10;
    @BindView(R.id.face_register_btn_finish)
    Button faceRegisterBtnFinish;
    @BindView(R.id.face_register_btn_delete)
    Button faceRegisterBtnDelete;

    private CameraHelper cameraHelper;
    private DrawHelper drawHelper;
    private Camera.Size previewSize;
    /**
     * 优先打开的摄像头，本界面主要用于单目RGB摄像头设备，因此默认打开前置
     */
    private Integer rgbCameraID = Camera.CameraInfo.CAMERA_FACING_FRONT;

    /**
     * VIDEO模式人脸检测引擎，用于预览帧人脸追踪
     */
    private FaceEngine ftEngine;
    /**
     * 用于特征提取的引擎
     */
    private FaceEngine frEngine;
    /**
     * IMAGE模式活体检测引擎，用于预览帧人脸活体检测
     */
    private FaceEngine flEngine;

    private int ftInitCode = -1;
    private int frInitCode = -1;
    private int flInitCode = -1;
    private FaceHelper faceHelper;
    /**
     * 注册人脸状态码，准备注册
     */
    private static final int REGISTER_STATUS_READY = 0;
    /**
     * 注册人脸状态码，注册中
     */
    private static final int REGISTER_STATUS_PROCESSING = 1;
    /**
     * 注册人脸状态码，注册结束（无论成功失败）
     */
    private static final int REGISTER_STATUS_DONE = 2;

    private int registerStatus = REGISTER_STATUS_DONE;
    private UserBean user;
    private int faceId;
    private Dialog tipDialog;
    private List<UserBiosBean> userBiosList = new ArrayList<>();
    private boolean isRegistered;
    private String bioId;
    private Integer biometricsNumber;
    private List<Integer> fingerIdList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_face);
        ButterKnife.bind(this);
        //保持亮屏
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        // Activity启动后就锁定为启动时的方向
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
        //本地人脸库初始化
        FaceServer.getInstance().init(this);
        //在布局结束后才做初始化操作
        previewView.getViewTreeObserver().addOnGlobalLayoutListener(this);

        user = (UserBean) getIntent().getSerializableExtra("user");
        if (user != null) {
            Log.i(TAG, "onCreate username: " + user.getUserName());
        }

        if (SharedUtils.getIsServerOnline()) {
            getUserChar();
        }

        FaceServer.getInstance().setOnRegisterResultListener(this);
    }

    public int getFaceId() {
        Log.i(TAG, "getFingerPrintId: ");
        if (!userBiosList.isEmpty()) {
            for (UserBiosBean userBiosBean : userBiosList) {
//                Log.i(TAG, "getFingerPrintId policeBiosBean: " + JSON.toJSONString(userBiosBean));
                String biometricsType = userBiosBean.getBiometricsType(); //设备类型
                if (!TextUtils.isEmpty(biometricsType) && biometricsType.equals(Constants.DEVICE_FACE)) {
                    int fingerprintId = userBiosBean.getBiometricsNumber();
                    fingerIdList.add(fingerprintId);
                }
//                Log.i(TAG, "getFingerId: " + fingerprintId);
            }
        }

        if (!fingerIdList.isEmpty()) {
            int emptyId = Collections.min(Utils.compare(fingerIdList, 1000)); //获取最大值
            Log.i(TAG, "initView emptyId: " + emptyId);
            return emptyId;
        }
        return 1;
    }

    /**
     * 获取用户注册人脸数据
     */
    private void getUserChar() {
        HttpClient.getInstance().getCharList(this, "",
                new HttpListener<String>() {
                    @Override
                    public void onSucceed(int what, Response<String> response) throws JSONException {
                        LogUtil.i(TAG, "onSucceed getUserChar response: " + response.get());
                        try {
                            if (!TextUtils.isEmpty(response.get())) {
                                userBiosList = JSON.parseArray(response.get(), UserBiosBean.class);
                                if (!userBiosList.isEmpty()) {
                                    initData();
                                }
                            } else {
                                ToastUtil.showShort("获取生物特征失败");
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            ToastUtil.showShort("获取特征发生错误");
                        }
                    }

                    @Override
                    public void onFailed(int what, Response<String> response) {
                        ToastUtil.showShort("网络请求失败！");
                    }
                });
    }

    public void initData() {
        if(user ==null){
            ToastUtil.showShort("获取用户失败！");
            return;
        }
        isRegistered = false; //设置为未注册状态
        faceRegisterTvMsg.setText("未注册，点击开始注册");
        setImageByBioType(false);
        if (!userBiosList.isEmpty()) {
            for (UserBiosBean userBiosBean : userBiosList) {
                String deviceType = userBiosBean.getBiometricsType();
                int userId = userBiosBean.getUserId();
                if (user.getUserId() == userId && deviceType.equals(Constants.DEVICE_FACE)) {
                    bioId = userBiosBean.getId();
                    biometricsNumber = userBiosBean.getBiometricsNumber();
                    setImageByBioType(true);
                }
            }
        } else { //没有指纹数据则在切换时重置
            setImageByBioType(false);
        }
    }

    private void setImageByBioType(boolean isRegister) {
        if (isRegister) {
            Log.i(TAG, "setImageByBioType 已注册: ");
//            Bitmap bitmap = BitmapUtils.readBitMap(
//                    this, R.drawable.enrolled_face);
            faceRegisterTvMsg.setText("已注册, 点击修改");
            faceRegisterBtn.setText("修改");
            faceRegisterBtnDelete.setVisibility(View.VISIBLE);
            isRegistered = true;
        } else {
            Log.i(TAG, "setImageByBioType 未注册: ");
//            Bitmap bitmap = BitmapUtils.readBitMap(
//                    this, R.drawable.unenroll_face);
            faceRegisterTvMsg.setText("未注册, 点击开始注册");
            faceRegisterBtn.setText("注册");
            faceRegisterBtnDelete.setVisibility(View.INVISIBLE);
            isRegistered = false;
        }
    }

    private void showDialog(String msg) {
        tipDialog = DialogUtils.creatTipDialog(this, "提示", msg, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tipDialog.dismiss();
                finish();
            }
        });
        if (!tipDialog.isShowing()) {
            tipDialog.show();
        }
    }

    @Override
    public void onGlobalLayout() {
        Log.i(TAG, "onGlobalLayout: ");
        previewView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
        initEngine();
        initCamera();
    }

    /**
     * 初始化引擎
     */
    private void initEngine() {
        Log.i(TAG, "initEngine: ");
        ftEngine = new FaceEngine();
        ftInitCode = ftEngine.init(this, DetectMode.ASF_DETECT_MODE_VIDEO, ConfigUtil.getFtOrient(this),
                16, MAX_DETECT_NUM, FaceEngine.ASF_FACE_DETECT);

        frEngine = new FaceEngine();
        frInitCode = frEngine.init(this, DetectMode.ASF_DETECT_MODE_IMAGE, DetectFaceOrientPriority.ASF_OP_0_ONLY,
                16, MAX_DETECT_NUM, FaceEngine.ASF_FACE_RECOGNITION);

        flEngine = new FaceEngine();
        flInitCode = flEngine.init(this, DetectMode.ASF_DETECT_MODE_IMAGE, DetectFaceOrientPriority.ASF_OP_0_ONLY,
                16, MAX_DETECT_NUM, FaceEngine.ASF_LIVENESS);


        VersionInfo versionInfo = new VersionInfo();
        ftEngine.getVersion(versionInfo);
        Log.i(TAG, "initEngine:  init: " + ftInitCode + "  version:" + versionInfo);

        if (ftInitCode != ErrorInfo.MOK) {
            String error = getString(R.string.specific_engine_init_failed, "ftEngine", ftInitCode);
            Log.i(TAG, "initEngine: " + error);
            faceRegisterTvMsg.setText(error);
        }
        if (frInitCode != ErrorInfo.MOK) {
            String error = getString(R.string.specific_engine_init_failed, "frEngine", frInitCode);
            Log.i(TAG, "initEngine: " + error);
            faceRegisterTvMsg.setText(error);
        }
        if (flInitCode != ErrorInfo.MOK) {
            String error = getString(R.string.specific_engine_init_failed, "flEngine", flInitCode);
            Log.i(TAG, "initEngine: " + error);
            faceRegisterTvMsg.setText(error);
        }
    }

    /**
     * 销毁引擎，faceHelper中可能会有特征提取耗时操作仍在执行，加锁防止crash
     */
    private void unInitEngine() {
        Log.i(TAG, "unInitEngine: ");
        if (ftInitCode == ErrorInfo.MOK && ftEngine != null) {
            synchronized (ftEngine) {
                int ftUnInitCode = ftEngine.unInit();
                Log.i(TAG, "unInitEngine: " + ftUnInitCode);
            }
        }
        if (frInitCode == ErrorInfo.MOK && frEngine != null) {
            synchronized (frEngine) {
                int frUnInitCode = frEngine.unInit();
                Log.i(TAG, "unInitEngine: " + frUnInitCode);
            }
        }
        if (flInitCode == ErrorInfo.MOK && flEngine != null) {
            synchronized (flEngine) {
                int flUnInitCode = flEngine.unInit();
                Log.i(TAG, "unInitEngine: " + flUnInitCode);
            }
        }
    }


    /**
     * 初始化相机
     */
    private void initCamera() {
        Log.i(TAG, "initCamera: ");
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        final FaceListener faceListener = new FaceListener() {
            @Override
            public void onFail(Exception e) {
                Log.e(TAG, "onFail: " + e.getMessage());
            }

            //请求FR的回调
            @Override
            public void onFaceFeatureInfoGet(@Nullable final FaceFeature faceFeature,
                                             final Integer requestId, final Integer errorCode) {
                Log.i(TAG, "onFaceFeatureInfoGet: ");
            }

            @Override
            public void onFaceLivenessInfoGet(@Nullable LivenessInfo livenessInfo,
                                              final Integer requestId, Integer errorCode) {
                Log.i(TAG, "onFaceLivenessInfoGet: ");
            }
        };

        //摄像头监听
        CameraListener cameraListener = new CameraListener() {
            @Override
            public void onCameraOpened(Camera camera, int cameraId, int displayOrientation, boolean isMirror) {
                Log.i(TAG, "onCameraOpened: ");
                Camera.Size lastPreviewSize = previewSize;
                previewSize = camera.getParameters().getPreviewSize();
                drawHelper = new DrawHelper(previewSize.width, previewSize.height, previewView.getWidth(), previewView.getHeight(), displayOrientation
                        , cameraId, isMirror, false, false);
                Log.i(TAG, "onCameraOpened: " + drawHelper.toString());
                // 切换相机的时候可能会导致预览尺寸发生变化
                if (faceHelper == null ||
                        lastPreviewSize == null ||
                        lastPreviewSize.width != previewSize.width || lastPreviewSize.height != previewSize.height) {
                    Integer trackedFaceCount = null;
                    // 记录切换时的人脸序号
                    if (faceHelper != null) {
                        trackedFaceCount = faceHelper.getTrackedFaceCount();
                        faceHelper.release();
                    }
                    faceHelper = new FaceHelper.Builder()
                            .ftEngine(ftEngine)
                            .frEngine(frEngine)
                            .flEngine(flEngine)
                            .frQueueSize(MAX_DETECT_NUM)
                            .flQueueSize(MAX_DETECT_NUM)
                            .previewSize(previewSize)
                            .faceListener(faceListener)
                            .trackedFaceCount(trackedFaceCount == null ? ConfigUtil.getTrackedFaceCount(RegisterFaceActivity.this.getApplicationContext()) : trackedFaceCount)
                            .build();
                }
            }

            @Override
            public void onPreview(final byte[] nv21, Camera camera) {
                List<FacePreviewInfo> facePreviewInfoList = faceHelper.onPreviewFrame(nv21);
                /**
                 * 注册人脸识别
                 */
                registerFace(nv21, facePreviewInfoList);
            }

            @Override
            public void onCameraClosed() {
                Log.i(TAG, "onCameraClosed: ");
            }

            @Override
            public void onCameraError(Exception e) {
                Log.i(TAG, "onCameraError: " + e.getMessage());
            }

            @Override
            public void onCameraConfigurationChanged(int cameraID, int displayOrientation) {
                Log.i(TAG, "onCameraConfigurationChanged: " + cameraID + "  " + displayOrientation);
                if (drawHelper != null) {
                    drawHelper.setCameraDisplayOrientation(displayOrientation);
                }
            }
        };

        //初始化cameraHelper
        cameraHelper = new CameraHelper.Builder()
                //设置预览尺寸
                .previewViewSize(new Point(previewView.getMeasuredWidth(), previewView.getMeasuredHeight()))
                //设置方向
                .rotation(getWindowManager().getDefaultDisplay().getRotation())
                //设置摄像头
                .specificCameraId(rgbCameraID != null ? rgbCameraID : Camera.CameraInfo.CAMERA_FACING_FRONT)
                //设置是否镜像显示
                .isMirror(false)
                //设置预览
                .previewOn(previewView)
                //设置摄像头监听
                .cameraListener(cameraListener)
                .build();
        //初始化
        cameraHelper.init();
        //启动
        cameraHelper.start();
    }

    /**
     * 注册人脸
     *
     * @param nv21                人脸特征数据
     * @param facePreviewInfoList
     */
    private void registerFace(final byte[] nv21, final List<FacePreviewInfo> facePreviewInfoList) {
        /**
         * 注册状态为准备状态
         */
        if (registerStatus == REGISTER_STATUS_READY && facePreviewInfoList != null && facePreviewInfoList.size() > 0) {
            registerStatus = REGISTER_STATUS_PROCESSING;
            Observable.create(new ObservableOnSubscribe<Boolean>() {
                @Override
                public void subscribe(ObservableEmitter<Boolean> emitter) {
//                    Log.i(TAG, "subscribe: ");
                    FaceInfo faceInfo = facePreviewInfoList.get(0).getFaceInfo();
                    boolean success = FaceServer.getInstance().registerNv21(
                            RegisterFaceActivity.this, nv21.clone(),
                            previewSize.width, previewSize.height, faceInfo,
                            String.valueOf(faceId));

                    Log.i(TAG, "subscribe success: " + success + " faceId: " + faceInfo.getFaceId()
                            + "registered : " + faceHelper.getTrackedFaceCount());
                    emitter.onNext(success);
                }
            })
                    .subscribeOn(Schedulers.computation())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<Boolean>() {
                        @Override
                        public void onSubscribe(Disposable d) {

                        }

                        @Override
                        public void onNext(Boolean success) {
                            String result = success ? "注册成功!" : "注册失败!";
                            Log.i(TAG, "onNext 注册结果: " + result);
                            faceRegisterTvMsg.setText(result);
                            registerStatus = REGISTER_STATUS_DONE;
                        }

                        @Override
                        public void onError(Throwable e) {
                            e.printStackTrace();
                            faceRegisterTvMsg.setText("注册失败!");
                            registerStatus = REGISTER_STATUS_DONE;
                        }

                        @Override
                        public void onComplete() {

                        }
                    });
        }
    }


    private void uploadChar(byte[] feature, String faceID) {
        if (user == null) {
            Log.i(TAG, "uploadChar membersBean is null: ");
            return;
        }
        UserBiosBean postPoliceBio = new UserBiosBean();
        String key = TransformUtil.toHexString(feature);

        postPoliceBio.setBiometricsNumber(Integer.parseInt(faceID));
        postPoliceBio.setBiometricsPart("1");
        postPoliceBio.setUserId(user.getUserId()); //警员id
        postPoliceBio.setUserName(user.getUserName());
        postPoliceBio.setBiometricsType(Constants.DEVICE_FACE);  //指纹类型
        postPoliceBio.setBiometricsKey(key);
        String jsonBody = JSON.toJSONString(postPoliceBio);
        LogUtil.i(TAG, "onEnrollStatus jsonBody: " + jsonBody);

        HttpClient.getInstance().postUserChar(this, jsonBody, new HttpListener<String>() {
            @Override
            public void onSucceed(int what, Response<String> response) throws JSONException {
                Log.i(TAG, "onSucceed postUserChar response: " + response.get());
                try {
                    if (!TextUtils.isEmpty(response.get())) {
                        if (response.get().equals("success")) {
                            DBManager.getInstance().insertCommLog(RegisterFaceActivity.this, user,
                                    user.getUserName() + "注册人脸特征");
                            showDialog("注册成功");
                        } else {
                            showDialog("上传数据失败，注册失败");
                        }
                    } else {
                        showDialog("返回数据为空，注册失败");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    showDialog("上传数据出现错误！注册失败！");
                }
            }

            @Override
            public void onFailed(int what, Response<String> response) {
                Log.i(TAG, "onFailed error: " + response.getException().getMessage());
                showDialog("糟糕！网络请求错误，注册失败！");
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy: ");
        if (cameraHelper != null) {
            cameraHelper.release();
            cameraHelper = null;
        }

        unInitEngine();//销毁引擎
        if (faceHelper != null) {
            ConfigUtil.setTrackedFaceCount(this, faceHelper.getTrackedFaceCount());
            faceHelper.release();
            faceHelper = null;
        }

        FaceServer.getInstance().unInit();
    }

    @OnClick({R.id.face_register_btn, R.id.face_register_btn_finish, R.id.face_register_btn_delete})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.face_register_btn_finish: //退出
                finish();
                break;
            case R.id.face_register_btn:
                if (isRegistered) { //已注册 删除人脸特征
                    if (!TextUtils.isEmpty(bioId) && biometricsNumber != null) {
                        deleteBios(1);
                    } else {
                        ToastUtil.showShort("特征Id为空");
                    }
                } else { //未注册 注册人脸特征
                    faceId = getFaceId(); //获取生物特征id
                    //注册
                    if (registerStatus == REGISTER_STATUS_DONE) {
                        registerStatus = REGISTER_STATUS_READY;
                    }
                }
                break;
            case R.id.face_register_btn_delete: //删除人脸特征
                if (isRegistered) {
                    if (!TextUtils.isEmpty(bioId) && biometricsNumber != null) {
                        deleteBios(2);
                    } else {
                        ToastUtil.showShort("特征Id为空");
                    }
                }
                break;
        }
    }

    /**
     * 删除生物特征
     */
    private void deleteBios(final int type) {
        HttpClient.getInstance().deleteUserChar(this, bioId, new HttpListener<String>() {
            @Override
            public void onSucceed(int what, Response<String> response) throws JSONException {
                Log.i(TAG, "deleteFingerBios onSucceed response: " + response.get());
                try {
                    if (!TextUtils.isEmpty(response.get())) {
                        if (response.get().equals("success")) {
                            Log.i(TAG, "onSucceed 人脸删除成功！: ");
                            DBManager.getInstance().insertCommLog(RegisterFaceActivity.this, user,
                                    user.getUserName() + "删除人脸特征");
                            //根据id删除本地特征
                            String id = String.valueOf(biometricsNumber);
                            if (!TextUtils.isEmpty(id)) {
                                boolean result = FaceServer.getInstance().deleteFace(RegisterFaceActivity.this, id);
                                if (result) {
                                    Log.i(TAG, "onSucceed 删除本地特征成功: ");
                                } else {
                                    Log.i(TAG, "onSucceed 删除本地特征失败: ");
                                }
                            }
                            if (type == 1) {
                                //注册
                                ToastUtil.showShort("删除特征成功！开始注册人脸");
                                faceId = getFaceId();
                                if (registerStatus == REGISTER_STATUS_DONE) {
                                    registerStatus = REGISTER_STATUS_READY;
                                }
                            } else {
                                ToastUtil.showShort("删除特征成功！");
                                finish();
                            }
                        } else {
                            Log.i(TAG, "onSucceed 人脸特征删除失败！: ");
                            showDialog("删除特征失败！");
                        }
                    } else {
                        Log.i(TAG, "onSucceed 特征删除失败！: ");
                        showDialog("删除特征失败！");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    showDialog("删除特征发生错误！");
                }
            }

            @Override
            public void onFailed(int what, Response<String> response) {
                Log.i(TAG, "deleteFingerBios onFailed error: " + response.getException().getMessage());
                showDialog("网络请求失败！");
            }
        });
    }

    @Override
    public void onRegisterResult(String id, byte[] feature, boolean success) {
        if (success) {
            try {
                uploadChar(feature, id);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            showDialog("注册失败！");
        }
    }

}
