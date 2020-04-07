package com.zack.xjht.ui;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.graphics.Point;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.arcsoft.face.AgeInfo;
import com.arcsoft.face.ErrorInfo;
import com.arcsoft.face.FaceEngine;
import com.arcsoft.face.FaceFeature;
import com.arcsoft.face.GenderInfo;
import com.arcsoft.face.LivenessInfo;
import com.arcsoft.face.VersionInfo;
import com.arcsoft.face.enums.DetectFaceOrientPriority;
import com.arcsoft.face.enums.DetectMode;
import com.google.gson.Gson;
import com.yanzhenjie.nohttp.rest.Response;
import com.zack.xjht.Constants;
import com.zack.xjht.R;
import com.zack.xjht.Utils.DialogUtils;
import com.zack.xjht.Utils.LogUtil;
import com.zack.xjht.Utils.SharedUtils;
import com.zack.xjht.Utils.SoundPlayUtil;
import com.zack.xjht.Utils.ToastUtil;
import com.zack.xjht.Utils.Utils;
import com.zack.xjht.alcohol.Alcohol;
import com.zack.xjht.alcohol.OnAlcoholValueListener;
import com.zack.xjht.db.DBManager;
import com.zack.xjht.db.gen.OfflineTaskDao;
import com.zack.xjht.db.gen.OfflineTaskItemDao;
import com.zack.xjht.db.gen.OperLogBeanDao;
import com.zack.xjht.db.gen.SubCabBeanDao;
import com.zack.xjht.db.gen.UrgentBackListBeanDao;
import com.zack.xjht.db.gen.UrgentGetListBeanDao;
import com.zack.xjht.db.gen.UrgentOutBeanDao;
import com.zack.xjht.db.gen.UserBeanDao;
import com.zack.xjht.db.gen.UserBiosBeanDao;
import com.zack.xjht.entity.OfflineTask;
import com.zack.xjht.entity.OfflineTaskItem;
import com.zack.xjht.entity.OperLogBean;
import com.zack.xjht.entity.SubCabBean;
import com.zack.xjht.entity.UploadOpenMsg;
import com.zack.xjht.entity.UrgentBackListBean;
import com.zack.xjht.entity.UrgentGetListBean;
import com.zack.xjht.entity.UrgentOutBean;
import com.zack.xjht.entity.UserBean;
import com.zack.xjht.entity.UserBiosBean;
import com.zack.xjht.event.EventConsts;
import com.zack.xjht.event.MessageEvent;
import com.zack.xjht.face.faceserver.CompareResult;
import com.zack.xjht.face.faceserver.FaceServer;
import com.zack.xjht.face.model.DrawInfo;
import com.zack.xjht.face.model.FacePreviewInfo;
import com.zack.xjht.face.util.ConfigUtil;
import com.zack.xjht.face.util.DrawHelper;
import com.zack.xjht.face.util.camera.CameraHelper;
import com.zack.xjht.face.util.camera.CameraListener;
import com.zack.xjht.face.util.face.FaceHelper;
import com.zack.xjht.face.util.face.FaceListener;
import com.zack.xjht.face.util.face.LivenessType;
import com.zack.xjht.face.util.face.RecognizeColor;
import com.zack.xjht.face.util.face.RequestFeatureStatus;
import com.zack.xjht.face.util.face.RequestLivenessStatus;
import com.zack.xjht.face.widget.FaceRectView;
import com.zack.xjht.finger.FingerManager;
import com.zack.xjht.http.HttpClient;
import com.zack.xjht.http.HttpListener;
import com.zack.xjht.iris.IrisManager;
import com.zack.xjht.serial.SerialPortUtil;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;


public class VerifyActivity extends BaseActivity implements
        IrisManager.OnReceiveCallback, FingerManager.IFingerStatus, OnAlcoholValueListener {
    private static final String TAG = "VerifyActivity";
    private static final int MAX_DETECT_NUM = 10;
    /**
     * 当FR成功，活体未成功时，FR等待活体的时间
     */
    private static final int WAIT_LIVENESS_INTERVAL = 100;
    /**
     * 失败重试间隔时间（ms）
     */
    private static final long FAIL_RETRY_INTERVAL = 1000;
    /**
     * 出错重试最大次数
     */
    private static final int MAX_RETRY_TIME = 3;

    @BindView(R.id.verify_iv_img)
    ImageView verifyIvImg;
    @BindView(R.id.verify_tv_msg)
    TextView verifyTvMsg;
    @BindView(R.id.verify_alcohol_tv_msg)
    TextView verifyAlcoholTvMsg;
    @BindView(R.id.verify_alcohol_tv_value)
    TextView verifyAlcoholTvValue;
    @BindView(R.id.verify_ll_alcohol)
    LinearLayout verifyLlAlcohol;
    @BindView(R.id.verify_alcohol_tv_countdown)
    TextView verifyAlcoholTvCountdown;
    @BindView(R.id.verify_tv_user)
    TextView verifyTvUser;
    @BindView(R.id.single_camera_texture_preview)
    TextureView previewView;
    @BindView(R.id.single_camera_face_rect_view)
    FaceRectView faceRectView;
    private int streamId;
    private boolean isStop;
    private int sendTime = 10;
    private String target;
    private boolean isVerifyFirstUser = true; //是否验证第一个人员身份
    private UserBean firstPolice, secondPolice;

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    if (verifyAlcoholTvCountdown != null) {
                        verifyAlcoholTvCountdown.setText((String) msg.obj);
                    }
                    break;
                case 1:
                    if (verifyAlcoholTvValue != null) {
                        verifyAlcoholTvValue.setText("当前酒精溶度：" + (int) msg.obj + "mg/100ml");
                    }
                    break;
                case 2:  //验证人员结果
                    if (verifyTvUser != null) {
                        verifyTvUser.setText((String) msg.obj);
                    }
                    break;
                case 3:  //验证过程
                    if (verifyTvMsg != null) {
                        verifyTvMsg.setText((String) msg.obj);
                    }
                    break;
            }
        }
    };
    private List<UserBean> userList;
    private List<UserBiosBean> userBiosBeanList;
    private UserBeanDao userBeanDao;
    private UserBiosBeanDao userBiosBeanDao;
    private OfflineTaskItemDao offlineTaskItemDao;
    private SubCabBeanDao subCabBeanDao;
    private OfflineTaskDao offlineTaskDao;
    private OperLogBeanDao operLogBeanDao;
    private UrgentOutBeanDao urgentOutBeanDao;
    private UrgentGetListBeanDao urgentGetListBeanDao;
    private UrgentBackListBeanDao urgentBackListBeanDao;

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
     * 活体检测的开关
     */
    private boolean livenessDetect = true;
    /**
     * 用于记录人脸识别相关状态
     */
    private ConcurrentHashMap<Integer, Integer> requestFeatureStatusMap = new ConcurrentHashMap<>();
    /**
     * 用于记录人脸特征提取出错重试次数
     */
    private ConcurrentHashMap<Integer, Integer> extractErrorRetryMap = new ConcurrentHashMap<>();
    /**
     * 用于存储活体值
     */
    private ConcurrentHashMap<Integer, Integer> livenessMap = new ConcurrentHashMap<>();
    /**
     * 用于存储活体检测出错重试次数
     */
    private ConcurrentHashMap<Integer, Integer> livenessErrorRetryMap = new ConcurrentHashMap<>();

    private CompositeDisposable getFeatureDelayedDisposables = new CompositeDisposable();
    private CompositeDisposable delayFaceTaskCompositeDisposable = new CompositeDisposable();

    /**
     * 识别阈值
     */
    private static final float SIMILAR_THRESHOLD = 0.8F;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify);
        ButterKnife.bind(this);

        offlineTaskItemDao = DBManager.getInstance().getOfflineTaskItemDao();
        subCabBeanDao = DBManager.getInstance().getSubCabBeanDao();
        offlineTaskDao = DBManager.getInstance().getOfflineTaskDao();
        operLogBeanDao = DBManager.getInstance().getOperLogBeanDao();
        urgentOutBeanDao = DBManager.getInstance().getUrgentOutBeanDao();
        urgentGetListBeanDao = DBManager.getInstance().getUrgentGetListBeanDao();
        urgentBackListBeanDao = DBManager.getInstance().getUrgentBackListBeanDao();

        //保持亮屏
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        //本地人脸库初始化
        FaceServer.getInstance().init(this);

        target = getIntent().getStringExtra("activity");

        useLocalData();

        Alcohol.getInstance().setOnAlcoholValueListener(this);
    }

    private void useLocalData() {
        userBeanDao = DBManager.getInstance().getUserBeanDao();
        userBiosBeanDao = DBManager.getInstance().getUserBiosBeanDao();

        userList = userBeanDao.loadAll();
        userBiosBeanList = userBiosBeanDao.loadAll();
        if (!userList.isEmpty() && !userBiosBeanList.isEmpty()) {
            verify();
        } else {
            showDialog("无人员或生物特征数据");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "onResume: ");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "onPause: ");

        isStop = true;
    }

    @Override
    public void onReceiveInfo(byte[] info) {
        String id = Utils.getIrisID(info, info.length);
        if (!TextUtils.isEmpty(id)) {
            IrisManager.getInstance().cancelAction();
            Log.i(TAG, "onReceiveInfo 识别成功 : " + id);
            if (isVerifyFirstUser) {
                firstVerify(Integer.parseInt(id), Constants.DEVICE_IRIS);
            } else {
                secondVerify(Integer.parseInt(id), Constants.DEVICE_IRIS);
            }
        } else {
            sendMsg(2, "虹膜识别失败！");
        }
    }

    @Override
    public void didVerify(int id, boolean success) {
        Log.i(TAG, "didVerify  id: " + id + " success: " + success);
        if (success) {
            if (isVerifyFirstUser) { //第一人验证
                //获取人员人员信息
                firstVerify(id, Constants.DEVICE_FINGER);
            } else {
                secondVerify(id, Constants.DEVICE_FINGER);
            }
        } else {
            sendMsg(2, "验证失败，请重试！");
            streamId = SoundPlayUtil.getInstance().play(R.raw.retry);
            retry(Constants.DEVICE_FINGER);
        }
    }

    /**
     * 第二人验证身份
     *
     * @param id
     * @param deviceType
     */
    private void secondVerify(int id, String deviceType) {
        Log.i(TAG, "secondVerify 验证第二人身份结果: ");
        //根据id和类型获取当前人员身份
        UserBean userBean = verifyIdentity(id, deviceType);
        if (userBean != null) {
            if (secondPolice != null) { //第二次验证第二人
                //Log.i(TAG, "secondVerify second police is not null: ");
                //判断两次验证是否同一人
                if (secondPolice.getUserId() != userBean.getUserId()) {
                    //两次验证人员不一致
                    sendMsg(2, "两次验证人员不一致");
                    streamId = SoundPlayUtil.getInstance().play(R.raw.twice_verify_diff);
                    retry(deviceType);
                } else {
//                    verifySecondOk(deviceType, userBean);
                    String roleKeys = userBean.getRoleKeys();
                    if (!TextUtils.isEmpty(roleKeys)) {
                        String[] split = roleKeys.split(",");
                        List<String> roleList = Arrays.asList(split);
                        if (roleList != null && !roleList.isEmpty()) {
                            // 判断是否是领导或枪管员
                            if (roleList.contains(Constants.ROLE_MANAGER)
                                    || roleList.contains(Constants.ROLE_APPROVER)) {
                                //首次验证警员身份
//                                    secondPolice = userBean;
//                                    streamId = SoundPlayUtil.getInstance().play(R.raw.verify_success);
                                Log.i(TAG, "secondVerify 验证成功: " + deviceType);
                                verifySecondOk(deviceType, userBean);
                            } else {
                                sendMsg(2, "权限不足");
                                streamId = SoundPlayUtil.getInstance().play(R.raw.no_permission);
                                retry(deviceType);
                            }
                        } else {
                            sendMsg(2, "当前人员没有权限");
                            streamId = SoundPlayUtil.getInstance().play(R.raw.no_permission);
                            retry(deviceType);
                        }
                    } else {
                        sendMsg(2, "当前人员没有权限");
                        streamId = SoundPlayUtil.getInstance().play(R.raw.no_permission);
                        retry(deviceType);
                    }
                }
            } else { //第一次验证第二人
                String roleKeys = userBean.getRoleKeys();
                if (!TextUtils.isEmpty(roleKeys)) {
                    String[] split = roleKeys.split(",");
                    List<String> roleList = Arrays.asList(split);
                    if (roleList != null && !roleList.isEmpty()) {
                        if (target.equals(Constants.ACTIVITY_GET)
                                || target.equals(Constants.ACTIVITY_BACK)
                                || target.equals(Constants.ACTIVITY_OFFLINE_GET)
                                || target.equals(Constants.ACTIVITY_OFFLINE_BACK)
                                || target.equals(Constants.ACTIVITY_URGENT_GET_GUN)
                                || target.equals(Constants.ACTIVITY_URGENT_GET_AMMO)
                                || target.equals(Constants.ACTIVITY_URGENT_BACK_GUN)
                                || target.equals(Constants.ACTIVITY_URGENT_BACK_AMMO)) {
                            // 紧急领枪 判断是否是领导或枪管员
                            if (isGetLeader) { //验证了领导
                                if (roleList.contains(Constants.ROLE_MANAGER)
                                        || roleList.contains(Constants.ROLE_APPROVER)) {
                                    //首次验证警员身份
                                    secondPolice = userBean;
//                                streamId = SoundPlayUtil.getInstance().play(R.raw.verify_success);
                                    verifySecondOk(deviceType, userBean);
                                } else {
                                    sendMsg(2, "当前人员没有权限");
                                    streamId = SoundPlayUtil.getInstance().play(R.raw.no_permission);
                                    retry(deviceType);
                                }
                            } else {//没有验证领导
                                //验证必须是领导  否则提示错误
                                if (roleList.contains(Constants.ROLE_APPROVER)) {
                                    //有领导权限
                                    secondPolice = userBean;
//                                streamId = SoundPlayUtil.getInstance().play(R.raw.verify_success);
                                    verifySecondOk(deviceType, userBean);
                                } else { //无领导权限
                                    sendMsg(2, "必须要验证一个领导");
                                    streamId = SoundPlayUtil.getInstance().play(R.raw.need_one_leader);
                                    retry(deviceType);
                                }
                            }
                        } else {
                            // 判断是否是领导或枪管员
                            if (roleList.contains(Constants.ROLE_MANAGER)
                                    || roleList.contains(Constants.ROLE_APPROVER)) {
                                //首次验证警员身份
                                secondPolice = userBean;
//                                streamId = SoundPlayUtil.getInstance().play(R.raw.verify_success);
                                verifySecondOk(deviceType, userBean);
                            } else {
                                sendMsg(2, "当前人员没有权限");
                                streamId = SoundPlayUtil.getInstance().play(R.raw.no_permission);
                                retry(deviceType);
                            }
                        }
                    } else {
                        sendMsg(2, "当前人员没有权限");
                        streamId = SoundPlayUtil.getInstance().play(R.raw.no_permission);
                        retry(deviceType);
                    }
                } else {
                    sendMsg(2, "当前人员没有权限");
                    streamId = SoundPlayUtil.getInstance().play(R.raw.no_permission);
                    retry(deviceType);
                }
            }
        } else {
            Log.i(TAG, "人员信息获取失败: ");
            sendMsg(2, "人员信息获取失败！");
            streamId = SoundPlayUtil.getInstance().play(R.raw.get_user_info_failed);
            retry(deviceType);
        }
    }

    private boolean isSameUser = false;
    private boolean isGetLeader = false;

    private void verifySecondOk(String deviceType, UserBean userBean) {
        int policeId = userBean.getUserId();
        String name = userBean.getUserName();

        String content = "";
        if (deviceType.equals(Constants.DEVICE_FINGER)) {
            content = "【" + name + "】验证指纹成功";
        } else if (deviceType.equals(Constants.DEVICE_FACE)) {
            content = "【" + name + "】识别人脸成功";
        } else if (deviceType.equals(Constants.DEVICE_IRIS)) {
            content = "【" + name + "】验证虹膜成功";
        }
        DBManager.getInstance().insertCommLog(this, userBean, content);

        Log.i(TAG, "run policeId: " + policeId + " 姓名：" + name);
//        streamId = SoundPlayUtil.getInstance().play(R.raw.verify_success);
        sendMsg(2, "验证成功！ 当前人员：" + name);
        if (firstPolice.getUserId() == secondPolice.getUserId()) {
            //判断两次验证警员是否相同
            sendMsg(2, "两次验证人员相同");
            streamId = SoundPlayUtil.getInstance().play(R.raw.twice_verify_user_same);
            isSameUser = true;
            secondPolice = null;
            verifyUser(SharedUtils.getSecondUserVerify(), 2);
        } else {
            isSameUser = false;
            //查询第二人验证身份
            switch (SharedUtils.getSecondUserVerify()) {
                case 1://指纹
                case 2://人脸
                case 3://虹膜
                    //验证成功 判断是否检测第二名警员酒精
                    if (SharedUtils.getSecondVerifyAlcohol()) {
                        if (target.equals(Constants.ACTIVITY_GET)) {
                            //检测第二人酒精
                            Log.i(TAG, "firstVerify 检测第二人酒精: ");
                            streamId = SoundPlayUtil.getInstance().play(R.raw.detect_alcohol);
                            detectAlcohol();
                        } else {
                            //验证完成 直接执行下一步操作 跳转 或提交数据
                            Log.i(TAG, "secondVerify 验证完成: ");
                            sendMsg(2, "验证成功！");
                            streamId = SoundPlayUtil.getInstance().play(R.raw.verify_success);
                            postData();
                        }
                    } else {
                        //验证完成 直接执行下一步操作 跳转 或提交数据
                        Log.i(TAG, "secondVerify 验证完成: ");
                        sendMsg(2, "验证成功！");
                        streamId = SoundPlayUtil.getInstance().play(R.raw.verify_success);
                        postData();
                    }
                    break;
                case 4: //指纹+人脸
                    if (deviceType.equals(Constants.DEVICE_FINGER)) {
                        //验证第二人人脸识别
                        Log.i(TAG, "verifySecondOk 验证第二人人脸: ");
                        verifyFace();
                    } else {
                        if (SharedUtils.getSecondVerifyAlcohol()) {
                            if (target.equals(Constants.ACTIVITY_GET)) {
                                //检测第二人酒精
                                Log.i(TAG, "firstVerify 检测第二人酒精: ");
                                streamId = SoundPlayUtil.getInstance().play(R.raw.detect_alcohol);
                                detectAlcohol();
                            } else {
                                //第二人验证完成 跳转 或提交数据
                                Log.i(TAG, "secondVerify 验证完成: ");
                                sendMsg(2, "验证成功！");
                                streamId = SoundPlayUtil.getInstance().play(R.raw.verify_success);
                                postData();
                            }
                        } else {
                            //第二人验证完成 跳转 或提交数据
                            Log.i(TAG, "secondVerify 验证完成: ");
                            sendMsg(2, "验证成功！");
                            streamId = SoundPlayUtil.getInstance().play(R.raw.verify_success);
                            postData();
                        }
                    }
                    break;
                case 5: //指纹+虹膜
                    if (deviceType.equals(Constants.DEVICE_FINGER)) {
                        //验证第二人虹膜识别
                        sendMsg(2, "验证第二人虹膜");
                        verifyIris();
                    } else {
                        if (SharedUtils.getSecondVerifyAlcohol()) {
                            if (target.equals(Constants.ACTIVITY_GET)) {
                                //检测第二人酒精
                                Log.i(TAG, "firstVerify 检测第二人酒精: ");
                                streamId = SoundPlayUtil.getInstance().play(R.raw.detect_alcohol);
                                detectAlcohol();
                            } else {
                                //第二人验证完成 跳转 或提交数据
                                Log.i(TAG, "secondVerify 验证完成: ");
                                sendMsg(2, "验证完成！");
                                streamId = SoundPlayUtil.getInstance().play(R.raw.verify_success);
                                postData();
                            }
                        } else {
                            //第二人验证完成 跳转 或提交数据
                            Log.i(TAG, "secondVerify 验证完成: ");
                            sendMsg(2, "验证完成！");
                            streamId = SoundPlayUtil.getInstance().play(R.raw.verify_success);
                            postData();
                        }
                    }
                    break;
                case 6://人脸+虹膜
                    if (deviceType.equals(Constants.DEVICE_FACE)) {
                        //验证第二人虹膜识别
                        verifyIris();
                    } else {
                        if (SharedUtils.getSecondVerifyAlcohol()) {
                            if (target.equals(Constants.ACTIVITY_GET)) {
                                //检测第二人酒精
                                Log.i(TAG, "firstVerify 检测第二人酒精: ");
                                streamId = SoundPlayUtil.getInstance().play(R.raw.detect_alcohol);
                                detectAlcohol();
                            } else {
                                //第二人验证完成 跳转 或提交数据
                                Log.i(TAG, "secondVerify 验证完成: ");
                                sendMsg(2, "验证完成！");
                                streamId = SoundPlayUtil.getInstance().play(R.raw.verify_success);
                                postData();
                            }
                        } else {
                            //第二人验证完成 跳转 或提交数据
                            Log.i(TAG, "secondVerify 验证完成: ");
                            sendMsg(2, "验证完成！");
                            streamId = SoundPlayUtil.getInstance().play(R.raw.verify_success);
                            postData();
                        }
                    }
                    break;
            }
        }
    }

    private void postData() {
        //获取传递的数据
        switch (target) {
            case Constants.ACTIVITY_GET: //取枪
                String data = getIntent().getStringExtra("data");
                if (!TextUtils.isEmpty(data)) {
                    postGetData(data, secondPolice, firstPolice);
                }
                break;
            case Constants.ACTIVITY_BACK: //还枪
                data = getIntent().getStringExtra("data");
                if (!TextUtils.isEmpty(data)) {
                    postGetData(data, firstPolice, secondPolice);
                }
                break;
            case Constants.ACTIVITY_URGENT_GET_GUN:
                data = getIntent().getStringExtra("data");
                if (!TextUtils.isEmpty(data)) {
                    List<SubCabBean> subCabBeans = JSON.parseArray(data, SubCabBean.class);
                    if (!subCabBeans.isEmpty()) {
                        openLockAndPostTask(subCabBeans);
                    } else {
                        showDialog("提交数据失败！");
                        EventBus.getDefault().post(new MessageEvent(EventConsts.EVENT_POST_FAILURE));
                    }
                } else {
                    showDialog("提交数据失败！");
                    EventBus.getDefault().post(new MessageEvent(EventConsts.EVENT_POST_FAILURE));
                }
                break;
            case Constants.ACTIVITY_URGENT_BACK_GUN:
                data = getIntent().getStringExtra("data");
                Long urgentTaskId = (Long) getIntent().getSerializableExtra("urgentTaskId");
                if (!TextUtils.isEmpty(data)) {
                    modifyUrgentBackTask(data, urgentTaskId);
                } else {
                    showDialog("提交失败！");
                    EventBus.getDefault().post(new MessageEvent(EventConsts.EVENT_POST_FAILURE));
                }
                break;
            case Constants.ACTIVITY_URGENT_GET_AMMO:
                //验证成功 打开柜门
                openLockAndPostTask("应急处突领取弹药", 0);
                break;
            case Constants.ACTIVITY_URGENT_BACK_AMMO:
                //验证成功 打开柜门
                openLockAndPostTask("应急处突归还弹药", 1);
                break;
            case Constants.ACTIVITY_KEEP: //保养任务
                data = getIntent().getStringExtra("data");
                if (!TextUtils.isEmpty(data)) {
                    postKeepGetData(data);
                }
                break;
            case Constants.ACTIVITY_SCRAP:
                data = getIntent().getStringExtra("data");
                if (!TextUtils.isEmpty(data)) {
                    postScrapData(data);
                }
                break;
            case Constants.ACTIVITY_IN_STORE:
                data = getIntent().getStringExtra("data");
                if (!TextUtils.isEmpty(data)) {
                    postInstoreData(data);
                }
                break;
            case Constants.ACTIVITY_TEMP_IN: //临时存放
                data = getIntent().getStringExtra("data");
                if (!TextUtils.isEmpty(data)) {
                    postTempStore(data);
                }
                break;
            case Constants.ACTIVITY_TEMP_GET: //临时存放领出
                data = getIntent().getStringExtra("data");
                if (!TextUtils.isEmpty(data)) {
                    postTempGunGet(data);
                }
                break;
            case Constants.ACTIVITY_OPEN_CAB:
                SerialPortUtil.getInstance().openLock(SharedUtils.getLeftCabNo());
//                try {
//                    Thread.sleep(500);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//                SerialPortUtil.getInstance().openLock(SharedUtils.getRightCabNo());
                SerialPortUtil.getInstance().openLED();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showDialog("打开枪柜成功");
                    }
                });
                break;
            case Constants.ACTIVITY_OFFLINE_GET: //离线领枪
                Log.i(TAG, "postData : 离线领枪");
                try {
                    String data1 = getIntent().getStringExtra("data");
                    if (!TextUtils.isEmpty(data1)) {
                        List<SubCabBean> subCabBeans = JSON.parseArray(data1, SubCabBean.class);
                        if (!subCabBeans.isEmpty()) {
                            OfflineTask offlineTask = new OfflineTask();
                            offlineTask.setStartTime(System.currentTimeMillis());
                            offlineTask.setTaskStatus(1);
                            offlineTask.setApplyId(firstPolice.getUId());
                            offlineTask.setApproveId(secondPolice.getUId());
                            long offlineTaskId = offlineTaskDao.insert(offlineTask);
                            if (offlineTaskId > 0) {
                                Log.i(TAG, "postData offlineTask: "+JSON.toJSONString(offlineTask));
                                for (SubCabBean subCabBean : subCabBeans) {
                                    //保存离线任务数据
                                    OfflineTaskItem offlineTaskItem = new OfflineTaskItem();
                                    offlineTaskItem.setLocationId(subCabBean.getId());
                                    offlineTaskItem.setLocationNo(subCabBean.getLocationNo());
                                    String locationType = subCabBean.getLocationType();
                                    offlineTaskItem.setLocationType(locationType);
                                    offlineTaskItem.setObjectId(subCabBean.getObjectId());
                                    offlineTaskItem.setSubCabId(subCabBean.getSId());
                                    offlineTaskItem.setUserId(subCabBean.getUserId());
                                    String userName = subCabBean.getUserName();
                                    Log.i(TAG, "postData  userName: " + userName);
                                    offlineTaskItem.setUserName(userName);
                                    int getNum = subCabBean.getGetNum();
                                    if (!TextUtils.isEmpty(locationType)) {
                                        if (locationType.equals(Constants.TYPE_AMMO)) {
                                            Log.i(TAG, "postData 领取数量: " + getNum);
                                            offlineTaskItem.setObjectNum(getNum);
                                        } else {
                                            offlineTaskItem.setObjectNum(1);
                                            offlineTaskItem.setGunNo(subCabBean.getGunNo());
                                        }
                                    }

                                    offlineTaskItem.setObjectType(subCabBean.getObjectName());
                                    offlineTaskItem.setStatus(1);
                                    offlineTaskItem.setTaskId(offlineTaskId);
                                    long taskItemId = offlineTaskItemDao.insert(offlineTaskItem);
                                    if (taskItemId > 0) {
                                        Log.i(TAG, "postData  offlineTaskItem: "+JSON.toJSONString(offlineTaskItem));
                                        //保存子弹数量和枪支状态
                                        if (!TextUtils.isEmpty(locationType)) {
                                            if (locationType.equals(Constants.TYPE_AMMO)) {
                                                subCabBean.setObjectNumber(subCabBean.getObjectNumber() - getNum);
                                            } else {
                                                subCabBean.setGunState("out");
                                            }
                                        }
                                        subCabBeanDao.update(subCabBean);
                                        SubCabBean subCabBean1 = subCabBeanDao.queryBuilder().where(
                                                SubCabBeanDao.Properties.SId.eq(subCabBean.getSId())).unique();
                                        Log.i(TAG, "postData subCabBean1: "+JSON.toJSONString(subCabBean1));

                                        /**
                                         * 添加领枪日志
                                         */
                                        OperLogBean operLogBean = new OperLogBean();
                                        operLogBean.setGunManagementId(String.valueOf(firstPolice.getUserId()));
                                        operLogBean.setGunManagementName(firstPolice.getUserName());
                                        operLogBean.setApproveId(String.valueOf(secondPolice.getUserId()));
                                        operLogBean.setApproveName(secondPolice.getUserName());
                                        operLogBean.setPoliceId(String.valueOf(subCabBean.getUserId()));
                                        operLogBean.setPoliceName(userName);
                                        operLogBean.setGunCabinetId(SharedUtils.getGunCabId());
                                        operLogBean.setGunCabinetLocationId(subCabBean.getId());
                                        if (!TextUtils.isEmpty(locationType)) {
                                            if (locationType.equals(Constants.TYPE_AMMO)) {
                                                operLogBean.setOutCount(String.valueOf(subCabBean.getGetNum()));
                                            } else {
                                                operLogBean.setGunNo(subCabBean.getGunNo());
                                            }
                                        }
                                        operLogBean.setOutTime(Utils.longTime2String(System.currentTimeMillis()));
                                        operLogBean.setObjectId(subCabBean.getObjectId());
                                        //operLogBean.setPoliceId("");
                                        operLogBean.setType(locationType);
                                        operLogBean.setTypeName(subCabBean.getObjectName());
                                        operLogBean.setStatus("1");
                                        operLogBean.setUploadStatus(0); //未上传
                                        operLogBean.setTaskId(offlineTaskId);
                                        operLogBean.setTaskItemId(taskItemId);

                                        long operLogId = operLogBeanDao.insert(operLogBean);
                                        String jsonString = JSON.toJSONString(operLogBean);
                                        Log.i(TAG, "postData operLogBean jsonString: " + jsonString);
                                        if (operLogId > 0) {
                                            Log.i(TAG, "postData 插入数据成功: ");
                                            if(SharedUtils.getIsServerOnline()){
                                                //提交数据到后台
                                                postOfflineTaskLog(operLogBean, 1);
                                            }
                                        } else {
                                            Log.i(TAG, "postData 插入数据失败: ");
                                        }
                                    } else {
                                        Log.i(TAG, "postData 插入数据库失败: ");
                                    }
                                }
//                                showDialog("领取成功！");
                                ToastUtil.showShort("领取成功");
                                EventBus.getDefault().postSticky(new MessageEvent(
                                        EventConsts.EVENT_POST_SUCCESS, firstPolice, secondPolice));
                            } else {
//                                showDialog("领取失败！");
                                ToastUtil.showShort("领取失败");
                                EventBus.getDefault().postSticky(new MessageEvent(
                                        EventConsts.EVENT_POST_FAILURE, firstPolice, secondPolice));
                            }
                        } else {
//                            showDialog("领取失败！");
                            ToastUtil.showShort("领取数据出错");
                        }
                    } else {
//                        showDialog("领取失败！");
                        ToastUtil.showShort("领取数据为空");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    ToastUtil.showShort("领取出错");
                }
                finish();
                break;
            case Constants.ACTIVITY_OFFLINE_BACK://离线还枪
                try {
                    Log.i(TAG, "postData : 离线还枪");
                    data = getIntent().getStringExtra("data");
                    if (!TextUtils.isEmpty(data)) {
                        List<OfflineTaskItem> offlineTaskItemList = JSON.parseArray(data, OfflineTaskItem.class);
                        if (!offlineTaskItemList.isEmpty()) {
                            Long taskId = 0L;
                            for (OfflineTaskItem offlineTaskItem : offlineTaskItemList) {
                                taskId = offlineTaskItem.getTaskId();
                                offlineTaskItem.setStatus(2);//设置状态归还
                                int backNum = offlineTaskItem.getBackNum();
                                String locationType = offlineTaskItem.getLocationType();
                                Log.i(TAG, "postData backNum: " + backNum);
                                //更新数据
                                offlineTaskItemDao.update(offlineTaskItem);
                                long subCabId = offlineTaskItem.getSubCabId();

                                SubCabBean unique = subCabBeanDao.queryBuilder().where(
                                        SubCabBeanDao.Properties.SId.eq(subCabId)).unique();
                                if (unique != null) {
                                    String locationType1 = unique.getLocationType();
                                    if (!TextUtils.isEmpty(locationType1)) {
                                        if (locationType1.equals(Constants.TYPE_AMMO)) {
                                            unique.setObjectNumber(unique.getObjectNumber() + backNum);
                                        } else {
                                            unique.setGunState("in");
                                        }
                                    }
                                    subCabBeanDao.update(unique);
                                }
                                //获取领枪日志
                                OperLogBean operLog = operLogBeanDao.queryBuilder().where(
                                        OperLogBeanDao.Properties.TaskItemId.eq(offlineTaskItem.getId())).unique();
                                if (operLog != null) {
                                    if (locationType.equals(Constants.TYPE_AMMO)) {
                                        //子弹归还数量
                                        operLog.setInCount(String.valueOf(backNum));
                                        //计算损耗数量
                                        String outCount = operLog.getOutCount();
                                        String inCount = operLog.getInCount();
                                        int outCountInt = Integer.parseInt(outCount);
                                        int inCountInt = Integer.parseInt(inCount);
                                        int loseCount = outCountInt - inCountInt;
                                        operLog.setLoseCount(String.valueOf(loseCount));
                                    }
                                    operLog.setInTime(Utils.longTime2String(System.currentTimeMillis()));
                                    operLog.setStatus("2");
                                    operLogBeanDao.update(operLog);

                                    String jsonString = JSON.toJSONString(operLog);
                                    Log.i(TAG, "postData operLog jsonString: " + jsonString);

                                    postOfflineTaskLog(operLog, 2);
                                }
                            }

                            String jsonString = JSON.toJSONString(offlineTaskItemList);
                            LogUtil.i(TAG, "postData: jsonString " + jsonString);

                            //查询归还数和领取是否相同
                            OfflineTask offlineTask = offlineTaskDao.queryBuilder().where(
                                    OfflineTaskDao.Properties.Id.eq(taskId)).unique();
                            List<OfflineTaskItem> offlineTaskItems = offlineTask.getOfflineTaskItemList();
                            if (!offlineTaskItems.isEmpty()) {
                                int statusSize = 0;
                                for (OfflineTaskItem offlineTaskItem : offlineTaskItems) {
                                    int status = offlineTaskItem.getStatus();
                                    if (status == 1) {//当前未归还状态
                                        statusSize++;
                                    }
                                }
                                int taskItemSize = offlineTaskItems.size();
                                int backSize = offlineTaskItemList.size();
                                Log.i(TAG, "postData taskItemSize : " + taskItemSize + " backSize:" + backSize);
                                if (backSize == taskItemSize || statusSize == 0) {
                                    offlineTask.setUpdateTime(System.currentTimeMillis());
                                    offlineTask.setFinishTime(System.currentTimeMillis());
                                    offlineTask.setTaskStatus(2);
                                    offlineTaskDao.update(offlineTask);
                                }
                            }
                            ToastUtil.showShort("归还成功");
//                            showDialog("归还成功！");
                            EventBus.getDefault().postSticky(new MessageEvent(
                                    EventConsts.EVENT_POST_SUCCESS, firstPolice, secondPolice));
                        } else {
                            ToastUtil.showShort("归还数据为空");
//                            showDialog("归还失败！");
                            EventBus.getDefault().postSticky(new MessageEvent(
                                    EventConsts.EVENT_POST_FAILURE, firstPolice, secondPolice));
                        }
                    } else {
                        ToastUtil.showShort("归还数据为空！");
//                        showDialog("归还失败！");
                        EventBus.getDefault().postSticky(new MessageEvent(
                                EventConsts.EVENT_POST_FAILURE, firstPolice, secondPolice));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    ToastUtil.showShort("归还出错");
//                    showDialog("归还失败！");
                    EventBus.getDefault().postSticky(new MessageEvent(
                            EventConsts.EVENT_POST_FAILURE, firstPolice, secondPolice));
                }
                finish();
                break;
        }
    }

    private void postOfflineTaskLog(final OperLogBean operLogBean, final int status) {
        HttpClient.getInstance().postOfflineTaskLog(this, JSON.toJSONString(operLogBean),
                new HttpListener<String>() {
                    @Override
                    public void onSucceed(int what, Response<String> response) throws JSONException {
                        String responseStr = response.get();
                        Log.i(TAG, "postOfflineTaskLog onSucceed responseStr: " + responseStr);
                        if (!TextUtils.isEmpty(responseStr) && responseStr.equals("success")) {
                            //数据提交成功 修改同步状态
                            if (status == 1) {
                                //已领取上传
                                operLogBean.setUploadStatus(1);
                            } else if (status == 2) {
                                //已归还上传
                                operLogBean.setUploadStatus(2);
                            }
                            operLogBeanDao.update(operLogBean);
                        } else {
                            Log.i(TAG, "onSucceed  上传失败: ");
                        }
                    }

                    @Override
                    public void onFailed(int what, Response<String> response) {

                    }
                });

    }

    /**
     * 检测酒精
     */
    private void detectAlcohol() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                verifyLlAlcohol.setVisibility(View.VISIBLE);
                previewView.setVisibility(View.INVISIBLE);
                verifyIvImg.setVisibility(View.INVISIBLE);
                verifyTvMsg.setVisibility(View.INVISIBLE);
                verifyTvUser.setVisibility(View.INVISIBLE);
            }
        });
        sendTime = 10;
        new Thread(new SendValueBufferTask()).start();
    }

    @Override
    public void timeout() {
        finish();
    }

    @OnClick(R.id.ac_top_back)
    public void onViewClicked() {
        finish();
    }

    /**
     * 酒精检测值
     *
     * @param alcohol
     */
    @Override
    public void onAlcoholValue(String alcohol) {
        if (!TextUtils.isEmpty(alcohol)) {
            Log.i(TAG, "onAlcoholValue ethanol : " + alcohol);
            float v = Float.parseFloat(alcohol);
            Log.i(TAG, "onAlcoholValue  v: " + v);
            final int driver = (int) ((v / 255.0) * 10); //单位为mg/100ml
            sendMsg(1, driver); //发送酒精检测结果值
            handler.post(new Runnable() {
                @Override
                public void run() {
                    if (driver > 103) { //2.5V
                        showDialog("酒精溶度检测超过阀值，禁止领取枪支和弹药");
                        streamId = SoundPlayUtil.getInstance().play(R.raw.alcohol_over_value);
                    } else {
                        if (sendTime == 0) {
                            sendMsg(0, "酒精浓度值检测正常");
                            streamId = SoundPlayUtil.getInstance().play(R.raw.alcohol_normal);
                            if (isVerifyFirstUser) {
                                //第一人检测酒精完成
                                isVerifyFirstUser = false;
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        verifyLlAlcohol.setVisibility(View.INVISIBLE);
                                        previewView.setVisibility(View.INVISIBLE);
                                        verifyIvImg.setVisibility(View.VISIBLE);
                                        verifyTvMsg.setVisibility(View.VISIBLE);
                                    }
                                });
                                verifyUser(SharedUtils.getSecondUserVerify(), 2);
                            } else {
                                //第二人酒精检测完成
                                //执行跳转操作
                                postData();
                            }
                        }
                    }
                }
            });
        }
    }

    /**
     * 第一次验证人员身份
     *
     * @param id
     * @param deviceType
     */
    private void firstVerify(int id, String deviceType) {
        //根据id和类型获取当前警员身份
        UserBean userBean = verifyIdentity(id, deviceType);
        if (userBean != null) {
            if (firstPolice != null) { //已验证第一人员身份
                //判断两次验证是否同一人
                if (firstPolice.getUserId() != userBean.getUserId()) {
                    //两次验证警员不一致
                    sendMsg(2, "两次验证人员不一致");
                    streamId = SoundPlayUtil.getInstance().play(R.raw.twice_verify_diff);
                    retry(deviceType);
                } else {
//                    verifyOk(deviceType, userBean);
                    String roleKeys = userBean.getRoleKeys();
                    if (!TextUtils.isEmpty(roleKeys)) {
                        String[] split = roleKeys.split(",");
                        List<String> roleList = Arrays.asList(split);
                        if (roleList != null && !roleList.isEmpty()) {
                            // 判断是否是领导或枪管员
                            if (roleList.contains(Constants.ROLE_APPROVER)) {
                                isGetLeader = true;
                            }
                            if (roleList.contains(Constants.ROLE_APPROVER) ||
                                    roleList.contains(Constants.ROLE_MANAGER)) {
                                //验证完成 提交数据
                                firstPolice = userBean;
                                verifyOk(deviceType, userBean);
                            } else {
                                sendMsg(2, "权限不足");
                                streamId = SoundPlayUtil.getInstance().play(R.raw.no_permission);
                                retry(deviceType);
                            }
                        } else {
                            sendMsg(2, "没有权限!");
                            retry(deviceType);
                        }
                    } else {
                        sendMsg(2, "没有权限！");
                        retry(deviceType);
                    }
                }
            } else { //首次验证人员身份
                String roleKeys = userBean.getRoleKeys();
                Log.i(TAG, "firstVerify roleKeys: " + roleKeys);
                if (!TextUtils.isEmpty(roleKeys)) {
                    String[] split = roleKeys.split(",");
                    List<String> roleList = Arrays.asList(split);
                    if (roleList != null && !roleList.isEmpty()) {
//                        if (!target.equals(Constants.ACTIVITY_GET)
//                                && !target.equals(Constants.ACTIVITY_BACK)) { //不是领枪或还枪
                        // 判断是否是领导或枪管员
                        if (roleList.contains(Constants.ROLE_APPROVER)) {
                            isGetLeader = true;
                        }
                        if (roleList.contains(Constants.ROLE_MANAGER) ||
                                roleList.contains(Constants.ROLE_APPROVER)) {
                            //首次验证警员身份
                            firstPolice = userBean;
                            verifyOk(deviceType, userBean);
                        } else {
                            sendMsg(2, "当前人员权限不足，请重新验证！");
                            retry(deviceType);
                        }
//                        } else {
//                            //首次验证警员身份
//                            firstPolice = userBean;
//                            verifyOk(deviceType, userBean);
//                        }
                    } else {
                        sendMsg(2, "当前人员没有权限");
                        retry(deviceType);
                    }
                } else {
                    sendMsg(2, "当前人员没有权限");
                    retry(deviceType);
                }
            }
        } else {
            Log.i(TAG, "用户不存在 ");
            sendMsg(2, "人员信息获取失败！");
            streamId = SoundPlayUtil.getInstance().play(R.raw.get_user_info_failed);
            retry(deviceType);
        }
    }

    private void verifyOk(String deviceType, UserBean userBean) {
        int policeId = userBean.getUserId();
        String name = userBean.getUserName();

        String content = "";
        if (deviceType.equals(Constants.DEVICE_FINGER)) {
            content = "人员【" + name + "】验证指纹成功";
        } else if (deviceType.equals(Constants.DEVICE_FACE)) {
            content = "人员【" + name + "】识别人脸成功";
        } else if (deviceType.equals(Constants.DEVICE_IRIS)) {
            content = "人员【" + name + "】验证虹膜成功";
        }
        DBManager.getInstance().insertCommLog(this, userBean, content);

        Log.i(TAG, "run policeId: " + policeId + " 姓名：" + name);
        sendMsg(2, "验证成功！当前人员:" + name);
//        streamId = SoundPlayUtil.getInstance().play(R.raw.verify_success);
        //查询第一人验证身份
        switch (SharedUtils.getFirstUserVerify()) {
            case 1://指纹
            case 2://人脸
            case 3://虹膜
                // 判断是否检测酒精
                if (SharedUtils.getFirstVerifyAlcohol()) {
                    if (target.equals(Constants.ACTIVITY_GET)) { //领枪 酒精检测
                        //检测第一人酒精
                        Log.i(TAG, "firstVerify 检测第一人员酒精: ");
                        streamId = SoundPlayUtil.getInstance().play(R.raw.detect_alcohol);
                        detectAlcohol();
                    } else {
                        verifySecondUser();
                    }
                } else { //不检测酒精  //验证第二警员身份
                    verifySecondUser();
                }
                break;
            case 4://指纹+人脸
                //验证第一人人脸
                if (deviceType.equals(Constants.DEVICE_FINGER)) {
                    //如果设备类型为指纹  开始识别人脸
                    sendMsg(2, "请识别人脸");
                    verifyFace();
                } else {
                    if (SharedUtils.getFirstVerifyAlcohol()) {
                        if (target.equals(Constants.ACTIVITY_GET)) {
                            //检测第一人酒精
                            Log.i(TAG, "firstVerify 检测第一人员酒精: ");
                            streamId = SoundPlayUtil.getInstance().play(R.raw.detect_alcohol);
                            detectAlcohol();
                        } else {
                            //验证第二警员身份
                            verifySecondUser();
                        }
                    } else {
                        //人脸识别完成 执行第二人
                        verifySecondUser();
                    }
                }
                break;
            case 5: //指纹+虹膜
                if (deviceType.equals(Constants.DEVICE_FINGER)) {
                    //验证指纹完成 验证虹膜
                    sendMsg(2, "请识别虹膜");
                    verifyIris();
                } else {
                    if (SharedUtils.getFirstVerifyAlcohol()) {//酒精检测
                        if (target.equals(Constants.ACTIVITY_GET)) {
                            //检测第一人酒精
                            Log.i(TAG, "firstVerify 检测第一人员酒精: ");
                            streamId = SoundPlayUtil.getInstance().play(R.raw.detect_alcohol);
                            detectAlcohol();
                        } else {
                            //验证第二警员身份
                            verifySecondUser();
                        }
                    } else {//不检测酒精
                        //验证虹膜完成 开始第二人验证
                        verifySecondUser();
                    }
                }
                break;
            case 6: //人脸+虹膜
                if (deviceType.equals(Constants.DEVICE_FACE)) {
                    //验证第一人虹膜
                    sendMsg(2, "请识别虹膜");
                    verifyIris();
                } else {
                    if (SharedUtils.getFirstVerifyAlcohol()) {
                        if (target.equals(Constants.ACTIVITY_GET)) {
                            //检测第一人酒精
                            Log.i(TAG, "firstVerify 检测第一人酒精: ");
                            streamId = SoundPlayUtil.getInstance().play(R.raw.detect_alcohol);
                            detectAlcohol();
                        } else {
                            //验证第二警员身份
                            verifySecondUser();
                        }
                    } else {
                        //验证虹膜完成 开始第二人验证
                        verifySecondUser();
                    }
                }
                break;
        }
    }

    private void verifySecondUser() {
        //验证第二人员身份

        isVerifyFirstUser = false;
        Log.i(TAG, "firstVerify 验证第二人员身份: ");
        sendMsg(2, "请验证第二人员身份");
        verifyUser(SharedUtils.getSecondUserVerify(), 2);
    }

    private void retry(String deviceType) {
        switch (deviceType) {
            case Constants.DEVICE_FINGER://指纹
                FingerManager.getInstance().searchfp(verifyTvMsg, this);
                break;
            case Constants.DEVICE_FACE://人脸
                verifyFace();
                break;
            case Constants.DEVICE_IRIS://虹膜
                IrisManager.getInstance().recognition(this, verifyTvMsg);
                break;
        }
    }

    private void verifyUser(int userVerify, int userTime) {
        switch (userVerify) {
            case 1://验证指纹
                Log.i(TAG, "verifyUser 验证指纹: ");
                if (userTime == 1) {
                    sendMsg(2, "请第一人验证指纹");
                    streamId = SoundPlayUtil.getInstance().play(R.raw.first_verify_finger);
                } else {
                    if (!isSameUser) {
                        sendMsg(2, "请第二人验证指纹");
                        streamId = SoundPlayUtil.getInstance().play(R.raw.second_verify_finger);
                    }
                }
                verifyFinger();
                break;
            case 2://验证人脸
                Log.i(TAG, "verifyUser: 验证人脸");
                if (userTime == 1) {
                    sendMsg(2, "请第一人验证人脸");
                    streamId = SoundPlayUtil.getInstance().play(R.raw.first_verify_face);
                } else {
                    if (!isSameUser) {
                        sendMsg(2, "请第二人验证人脸");
                        streamId = SoundPlayUtil.getInstance().play(R.raw.second_verify_face);
                        //延迟3s执行
                        try {
                            Thread.sleep(3 * 1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                verifyFace();
                break;
            case 3://验证虹膜
                Log.i(TAG, "verifyUser 验证虹膜: ");
                if (userTime == 1) {
                    sendMsg(2, "请第一人验证虹膜");
                    streamId = SoundPlayUtil.getInstance().play(R.raw.first_verify_iris);
                } else {
                    if (!isSameUser) {
                        sendMsg(2, "请第二人验证虹膜");
                        streamId = SoundPlayUtil.getInstance().play(R.raw.second_verfify_iris);
                    }
                }
                verifyIris();
                break;
            case 4://验证指纹+人脸
                Log.i(TAG, "verifyUser 验证指纹+人脸: ");
                if (userTime == 1) {
                    streamId = SoundPlayUtil.getInstance().play(R.raw.first_verify_finger);
                    sendMsg(2, "请第一人验证指纹");
                } else {
                    if (!isSameUser) {
                        streamId = SoundPlayUtil.getInstance().play(R.raw.second_verify_finger);
                        sendMsg(2, "请第二人验证指纹");
                    }
                }
                verifyFinger();
                break;
            case 5://验证指纹+虹膜
                Log.i(TAG, "verifyUser 验证指纹+虹膜: ");
                if (userTime == 1) {
                    streamId = SoundPlayUtil.getInstance().play(R.raw.first_verify_finger);
                    sendMsg(2, "请第一人验证指纹");
                } else {
                    if (!isSameUser) {
                        streamId = SoundPlayUtil.getInstance().play(R.raw.second_verify_finger);
                        sendMsg(2, "请第二人验证指纹");
                    }
                }
                verifyFinger();
                break;
            case 6://验证人脸+虹膜
                Log.i(TAG, "verifyUser 验证人脸+虹膜: ");
                if (userTime == 1) {
                    sendMsg(2, "请第一人识别人脸");
                    streamId = SoundPlayUtil.getInstance().play(R.raw.first_verify_face);
                } else {
                    if (!isSameUser) {
                        sendMsg(2, "请第二人识别人脸");
                        streamId = SoundPlayUtil.getInstance().play(R.raw.second_verify_face);
                        //延迟3s执行
                        try {
                            Thread.sleep(3 * 1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                verifyFace();
                break;
        }
    }

    private void verifyIris() {
        Log.i(TAG, "verifyIris 验证虹膜");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                previewView.setVisibility(View.INVISIBLE);
                verifyIvImg.setVisibility(View.VISIBLE);
                verifyIvImg.setImageResource(R.drawable.ic_iris_manage);
                if (!Constants.isIrisInit) {
                    verifyTvMsg.setText("虹膜未连接或初始化失败");
                }
                verifyTvMsg.setText("正在进行虹膜识别");
            }
        });

        IrisManager.getInstance().recognition(this, verifyTvMsg);
    }

    private void verifyFace() {
        Log.i(TAG, "verifyFace 验证人脸");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                verifyIvImg.setVisibility(View.INVISIBLE);
                previewView.setVisibility(View.VISIBLE);
                if (!Constants.isFaceInit) {
                    verifyTvMsg.setText("人脸识别未初始化");
                }
                verifyTvMsg.setText("正在进行人脸识别");
            }
        });
        initEngine();
        initCamera();
    }

    private void verifyFinger() {
        Log.i(TAG, "verifyFinger 验证指纹: ");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                previewView.setVisibility(View.INVISIBLE);
                verifyIvImg.setVisibility(View.VISIBLE);
                verifyIvImg.setImageResource(R.drawable.finger_verify);
                if (!Constants.isFingerConnect) {
                    verifyTvMsg.setText("指纹未连接");
                }
                if (!Constants.isFingerInit) {
                    verifyTvMsg.setText("指纹未初始化");
                }
                verifyTvMsg.setText("正在进行指纹识别");
            }
        });

        FingerManager.getInstance().fpsearch = false;
        FingerManager.getInstance().searchfp(verifyTvMsg, this);
    }

    /**
     * 发送酒精检测数据
     *
     * @param msg
     */
    private void sendMsg(int what, Object msg) {
        Message message = handler.obtainMessage(what, msg);
        message.sendToTarget();
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
            verifyTvMsg.setText(error);
        }
        if (frInitCode != ErrorInfo.MOK) {
            String error = getString(R.string.specific_engine_init_failed, "frEngine", frInitCode);
            Log.i(TAG, "initEngine: " + error);
            verifyTvMsg.setText(error);
        }
        if (flInitCode != ErrorInfo.MOK) {
            String error = getString(R.string.specific_engine_init_failed, "flEngine", flInitCode);
            Log.i(TAG, "initEngine: " + error);
            verifyTvMsg.setText(error);
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
//                Log.i(TAG, "onFaceFeatureInfoGet requestId: " + requestId + "  errorCode: " + errorCode);
                //FR成功
                if (faceFeature != null) {
//                    Log.i(TAG, "onFaceFeatureInfoGet: fr end = " + System.currentTimeMillis() + " trackId = " + requestId);
                    Integer liveness = livenessMap.get(requestId);
                    //不做活体检测的情况，直接搜索
                    if (!livenessDetect) {
                        searchFace(faceFeature, requestId);
                    } else if (liveness != null && liveness == LivenessInfo.ALIVE) {
                        //活体检测通过，搜索特征
                        searchFace(faceFeature, requestId);
                    } else {//活体检测未出结果，或者非活体，延迟执行该函数
                        if (requestFeatureStatusMap.containsKey(requestId)) {
                            Observable.timer(WAIT_LIVENESS_INTERVAL, TimeUnit.MILLISECONDS)
                                    .subscribe(new Observer<Long>() {
                                        Disposable disposable;

                                        @Override
                                        public void onSubscribe(Disposable d) {
                                            disposable = d;
                                            getFeatureDelayedDisposables.add(disposable);
                                        }

                                        @Override
                                        public void onNext(Long aLong) {
                                            onFaceFeatureInfoGet(faceFeature, requestId, errorCode);
                                        }

                                        @Override
                                        public void onError(Throwable e) {

                                        }

                                        @Override
                                        public void onComplete() {
                                            getFeatureDelayedDisposables.remove(disposable);
                                        }
                                    });
                        }
                    }
                } else {//特征提取失败
                    if (increaseAndGetValue(extractErrorRetryMap, requestId) > MAX_RETRY_TIME) {
                        extractErrorRetryMap.put(requestId, 0);

                        String msg;
                        // 传入的FaceInfo在指定的图像上无法解析人脸，此处使用的是RGB人脸数据，一般是人脸模糊
                        if (errorCode != null && errorCode == ErrorInfo.MERR_FSDK_FACEFEATURE_LOW_CONFIDENCE_LEVEL) {
                            msg = getString(R.string.low_confidence_level);
                        } else {
                            msg = "ExtractCode:" + errorCode;
                        }
                        faceHelper.setName(requestId, getString(R.string.recognize_failed_notice, msg));
                        // 在尝试最大次数后，特征提取仍然失败，则认为识别未通过
                        requestFeatureStatusMap.put(requestId, RequestFeatureStatus.FAILED);
                        retryRecognizeDelayed(requestId);
                    } else {
                        requestFeatureStatusMap.put(requestId, RequestFeatureStatus.TO_RETRY);
                    }
                }
            }

            @Override
            public void onFaceLivenessInfoGet(@Nullable LivenessInfo livenessInfo,
                                              final Integer requestId, Integer errorCode) {
                Log.i(TAG, "onFaceLivenessInfoGet requestId: " + requestId + " errorCode: " + errorCode);
                if (livenessInfo != null) {
                    int liveness = livenessInfo.getLiveness();
                    livenessMap.put(requestId, liveness);
                    // 非活体，重试
                    if (liveness == LivenessInfo.NOT_ALIVE) {
                        faceHelper.setName(requestId, getString(R.string.recognize_failed_notice, "NOT_ALIVE"));
                        // 延迟 FAIL_RETRY_INTERVAL 后，将该人脸状态置为UNKNOWN，帧回调处理时会重新进行活体检测
                        retryLivenessDetectDelayed(requestId);
                    }
                } else {
                    if (increaseAndGetValue(livenessErrorRetryMap, requestId) > MAX_RETRY_TIME) {
                        livenessErrorRetryMap.put(requestId, 0);
                        String msg;
                        // 传入的FaceInfo在指定的图像上无法解析人脸，此处使用的是RGB人脸数据，一般是人脸模糊
                        if (errorCode != null && errorCode == ErrorInfo.MERR_FSDK_FACEFEATURE_LOW_CONFIDENCE_LEVEL) {
                            msg = getString(R.string.low_confidence_level);
                        } else {
                            msg = "ProcessCode:" + errorCode;
                        }
                        faceHelper.setName(requestId, getString(R.string.recognize_failed_notice, msg));
                        retryLivenessDetectDelayed(requestId);
                    } else {
                        livenessMap.put(requestId, LivenessInfo.UNKNOWN);
                    }
                }
            }
        };


        //摄像头监听
        CameraListener cameraListener = new CameraListener() {
            @Override
            public void onCameraOpened(Camera camera, int cameraId, int displayOrientation, boolean isMirror) {
                Camera.Size lastPreviewSize = previewSize;
                previewSize = camera.getParameters().getPreviewSize();
                drawHelper = new DrawHelper(previewSize.width, previewSize.height, previewView.getWidth(), previewView.getHeight(), displayOrientation
                        , cameraId, isMirror, false, false);
//                Log.i(TAG, "onCameraOpened: " + drawHelper.toString());
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
                            .trackedFaceCount(trackedFaceCount == null ? ConfigUtil.getTrackedFaceCount(
                                    VerifyActivity.this.getApplicationContext()) : trackedFaceCount)
                            .build();
                }
            }

            @Override
            public void onPreview(final byte[] nv21, Camera camera) {
                if (faceRectView != null) {
                    faceRectView.clearFaceInfo();
                }
                //处理帧数据  返回相机预览回传的NV21数据
                List<FacePreviewInfo> facePreviewInfoList = faceHelper.onPreviewFrame(nv21);
                if (facePreviewInfoList != null && faceRectView != null && drawHelper != null) {
                    Log.i(TAG, "onPreview facePreviewInfoList size: "+facePreviewInfoList.size());
                    drawPreviewInfo(facePreviewInfoList);
                }
                /**
                 * 删除已经离开的人脸
                 */
                clearLeftFace(facePreviewInfoList);

                if (facePreviewInfoList != null && facePreviewInfoList.size() > 0 && previewSize != null) {
                    for (int i = 0; i < facePreviewInfoList.size(); i++) {
                        Integer status = requestFeatureStatusMap.get(facePreviewInfoList.get(i).getTrackId());
                        /**
                         * 在活体检测开启，在人脸识别状态不为成功或人脸活体状态不为处理中（ANALYZING）
                         * 且不为处理完成（ALIVE、NOT_ALIVE）时重新进行活体检测
                         */
                        if (livenessDetect && (status == null || status != RequestFeatureStatus.SUCCEED)) {
                            Integer liveness = livenessMap.get(facePreviewInfoList.get(i).getTrackId());
                            if (liveness == null ||
                                    (liveness != LivenessInfo.ALIVE
                                            && liveness != LivenessInfo.NOT_ALIVE
                                            && liveness != RequestLivenessStatus.ANALYZING)) {
                                livenessMap.put(facePreviewInfoList.get(i).getTrackId(),
                                        RequestLivenessStatus.ANALYZING);
                                faceHelper.requestFaceLiveness(nv21, facePreviewInfoList.get(i).getFaceInfo(),
                                        previewSize.width, previewSize.height, FaceEngine.CP_PAF_NV21,
                                        facePreviewInfoList.get(i).getTrackId(), LivenessType.RGB);
                            }
                        }
                        /**
                         * 对于每个人脸，若状态为空或者为失败，则请求特征提取（可根据需要添加其他判断以限制特征提取次数），
                         * 特征提取回传的人脸特征结果在{@link FaceListener#onFaceFeatureInfoGet(FaceFeature, Integer, Integer)}中回传
                         */
                        if (status == null || status == RequestFeatureStatus.TO_RETRY) {
                            requestFeatureStatusMap.put(facePreviewInfoList.get(i).getTrackId(), RequestFeatureStatus.SEARCHING);
                            faceHelper.requestFaceFeature(nv21,
                                    facePreviewInfoList.get(i).getFaceInfo(),
                                    previewSize.width, previewSize.height,
                                    FaceEngine.CP_PAF_NV21,
                                    facePreviewInfoList.get(i).getTrackId());
//                            Log.i(TAG, "onPreview: fr start = " + System.currentTimeMillis()
//                            + " trackId = " + facePreviewInfoList.get(i).getTrackedFaceCount());
                        }
                    }
                }
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
                if (drawHelper != null) {
                    drawHelper.setCameraDisplayOrientation(displayOrientation);
                }
                Log.i(TAG, "onCameraConfigurationChanged: " + cameraID + "  " + displayOrientation);
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

    //绘制预览信息
    private void drawPreviewInfo(List<FacePreviewInfo> facePreviewInfoList) {
        List<DrawInfo> drawInfoList = new ArrayList<>();
        for (int i = 0; i < facePreviewInfoList.size(); i++) {
            String name = faceHelper.getName(facePreviewInfoList.get(i).getTrackId());
            Integer liveness = livenessMap.get(facePreviewInfoList.get(i).getTrackId());
            Integer recognizeStatus = requestFeatureStatusMap.get(facePreviewInfoList.get(i).getTrackId());

            // 根据识别结果和活体结果设置颜色
            int color = RecognizeColor.COLOR_UNKNOWN;
            if (recognizeStatus != null) {
                if (recognizeStatus == RequestFeatureStatus.FAILED) {
                    color = RecognizeColor.COLOR_FAILED;
                }
                if (recognizeStatus == RequestFeatureStatus.SUCCEED) {
                    color = RecognizeColor.COLOR_SUCCESS;
                }
            }
            if (liveness != null && liveness == LivenessInfo.NOT_ALIVE) {
                color = RecognizeColor.COLOR_FAILED;
            }

            drawInfoList.add(new DrawInfo(drawHelper.adjustRect(facePreviewInfoList.get(i).getFaceInfo().getRect()),
                    GenderInfo.UNKNOWN, AgeInfo.UNKNOWN_AGE, liveness == null ? LivenessInfo.UNKNOWN : liveness, color,
                    name == null ? String.valueOf(facePreviewInfoList.get(i).getTrackId()) : name));
        }
        drawHelper.draw(faceRectView, drawInfoList);
    }

    /**
     * 删除已经离开的人脸
     *
     * @param facePreviewInfoList 人脸和trackId列表
     */
    private void clearLeftFace(List<FacePreviewInfo> facePreviewInfoList) {
        if (facePreviewInfoList == null || facePreviewInfoList.size() == 0) {
            requestFeatureStatusMap.clear();
            livenessMap.clear();
            livenessErrorRetryMap.clear();
            extractErrorRetryMap.clear();
            if (getFeatureDelayedDisposables != null) {
                getFeatureDelayedDisposables.clear();
            }
            return;
        }
        Enumeration<Integer> keys = requestFeatureStatusMap.keys();
        while (keys.hasMoreElements()) {
            int key = keys.nextElement();
            boolean contained = false;
            for (FacePreviewInfo facePreviewInfo : facePreviewInfoList) {
                if (facePreviewInfo.getTrackId() == key) {
                    contained = true;
                    break;
                }
            }
            if (!contained) {
                requestFeatureStatusMap.remove(key);
                livenessMap.remove(key);
                livenessErrorRetryMap.remove(key);
                extractErrorRetryMap.remove(key);
            }
        }
    }

    /**
     * 搜索人脸
     *
     * @param frFace
     * @param requestId
     */
    private void searchFace(final FaceFeature frFace, final Integer requestId) {
        Log.i(TAG, "searchFace: ");
        Observable.create(new ObservableOnSubscribe<CompareResult>() {
            @Override
            public void subscribe(ObservableEmitter<CompareResult> emitter) {
//                        Log.i(TAG, "subscribe: fr search start = " + System.currentTimeMillis() + " trackId = " + requestId);
                /**
                 * 在特征库中搜索
                 */
                CompareResult compareResult = FaceServer.getInstance().getTopOfFaceLib(frFace);
//                        Log.i(TAG, "subscribe: fr search end = " + System.currentTimeMillis() + " trackId = " + requestId);
                Log.i(TAG, "subscribe 用户名: " + compareResult.getUserName()
                        + "  相似度：" + compareResult.getSimilar()
                        + " TrackId: " + compareResult.getTrackId());
                emitter.onNext(compareResult);
            }
        })
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<CompareResult>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(CompareResult compareResult) {
                        if (compareResult == null || compareResult.getUserName() == null) {
                            requestFeatureStatusMap.put(requestId, RequestFeatureStatus.FAILED);
                            faceHelper.setName(requestId, "VISITOR " + requestId);
                            return;
                        }

//                        Log.i(TAG, "onNext: fr search get result  = " + System.currentTimeMillis() + " trackId = " + requestId + "  similar = " + compareResult.getSimilar());
                        /**
                         * 识别阀值 相似度大于0.8 即识别成功 否则识别失败
                         */
                        if (compareResult.getSimilar() > SIMILAR_THRESHOLD) {
//                            boolean isAdded = false;
//                            if (compareResultList == null) {
//                                requestFeatureStatusMap.put(requestId, RequestFeatureStatus.FAILED);
//                                if(faceHelper !=null){
//                                    faceHelper.setName(requestId, "VISITOR " + requestId);
//                                }
//                                return;
//                            }
//                            for (CompareResult compareResult1 : compareResultList) {
//                                if (compareResult1.getTrackId() == requestId) {
//                                    isAdded = true;
//                                    break;
//                                }
//                            }
//                            if (!isAdded) {
//                                //对于多人脸搜索，假如最大显示数量为 MAX_DETECT_NUM 且有新的人脸进入，则以队列的形式移除
//                                if (compareResultList.size() >= MAX_DETECT_NUM) {
//                                    compareResultList.remove(0);
//                                    adapter.notifyItemRemoved(0);
//                                }
//                                //添加显示人员时，保存其trackId
//                                compareResult.setTrackId(requestId);
//                                compareResultList.add(compareResult);
//                                adapter.notifyItemInserted(compareResultList.size() - 1);
//                            }
                            requestFeatureStatusMap.put(requestId, RequestFeatureStatus.SUCCEED);
                            if (faceHelper != null) {
                                faceHelper.setName(requestId, getString(R.string.recognize_success_notice, compareResult.getUserName()));
                            }
                            try {
                                unInitFace();
                                int id = Integer.parseInt(compareResult.getUserName());
                                if (isVerifyFirstUser) { //第一人验证
                                    //获取人员人员信息
                                    firstVerify(id, Constants.DEVICE_FACE);
                                } else {
                                    secondVerify(id, Constants.DEVICE_FACE);
                                }
                                faceRectView.clearFaceInfo();
//                                ToastUtil.showShort("验证成功！ " + compareResult.getUserName());
                            } catch (NumberFormatException e) {
                                e.printStackTrace();
                            }
                        } else {
                            /**
                             * 相似度太低 比对失败  提示未注册
                             */
                            if (faceHelper != null) {
                                faceHelper.setName(requestId, getString(R.string.recognize_failed_notice, "NOT_REGISTERED"));
                            }
                            retryRecognizeDelayed(requestId);

                            verifyTvMsg.setText("人脸识别失败，请重试！");
                            streamId = SoundPlayUtil.getInstance().play(R.raw.retry);
//                            retry(Constants.DEVICE_FACE);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        faceHelper.setName(requestId, getString(R.string.recognize_failed_notice, "NOT_REGISTERED"));
                        retryRecognizeDelayed(requestId);
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    /**
     * 将map中key对应的value增1回传
     *
     * @param countMap map
     * @param key      key
     * @return 增1后的value
     */
    public int increaseAndGetValue(Map<Integer, Integer> countMap, int key) {
        if (countMap == null) {
            return 0;
        }
        Integer value = countMap.get(key);
        if (value == null) {
            value = 0;
        }
        countMap.put(key, ++value);
        return value;
    }


    /**
     * 延迟 FAIL_RETRY_INTERVAL 重新进行人脸识别 1秒之后重试
     *
     * @param requestId 人脸ID
     */
    private void retryRecognizeDelayed(final Integer requestId) {
        requestFeatureStatusMap.put(requestId, RequestFeatureStatus.FAILED);
        Observable.timer(FAIL_RETRY_INTERVAL, TimeUnit.MILLISECONDS)
                .subscribe(new Observer<Long>() {
                    Disposable disposable;

                    @Override
                    public void onSubscribe(Disposable d) {
                        disposable = d;
                        delayFaceTaskCompositeDisposable.add(disposable);
                    }

                    @Override
                    public void onNext(Long aLong) {

                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onComplete() {
                        // 将该人脸特征提取状态置为FAILED，帧回调处理时会重新进行活体检测
                        faceHelper.setName(requestId, Integer.toString(requestId));
                        requestFeatureStatusMap.put(requestId, RequestFeatureStatus.TO_RETRY);
                        delayFaceTaskCompositeDisposable.remove(disposable);
                    }
                });
    }

    /**
     * 延迟 FAIL_RETRY_INTERVAL 重新进行活体检测
     *
     * @param requestId 人脸ID
     */
    private void retryLivenessDetectDelayed(final Integer requestId) {
        Observable.timer(FAIL_RETRY_INTERVAL, TimeUnit.MILLISECONDS)
                .subscribe(new Observer<Long>() {
                    Disposable disposable;

                    @Override
                    public void onSubscribe(Disposable d) {
                        disposable = d;
                        delayFaceTaskCompositeDisposable.add(disposable);
                    }

                    @Override
                    public void onNext(Long aLong) {

                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onComplete() {
                        // 将该人脸状态置为UNKNOWN，帧回调处理时会重新进行活体检测
                        if (livenessDetect) {
                            faceHelper.setName(requestId, Integer.toString(requestId));
                        }
                        livenessMap.put(requestId, LivenessInfo.UNKNOWN);
                        delayFaceTaskCompositeDisposable.remove(disposable);
                    }
                });
    }

    /**
     * 销毁引擎，faceHelper中可能会有特征提取耗时操作仍在执行，加锁防止crash
     */
    private void unInitEngine() {
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

    private class SendValueBufferTask implements Runnable {
        @Override
        public void run() {
            while (!isStop && sendTime > 0) {
                Log.i(TAG, "run readTime: " + sendTime);
                sendMsg(0, "剩余时间：" + sendTime + "秒"); //发送酒精检测剩余时间
                //每次读取不断减少次数
                sendTime--;
                Alcohol.getInstance().checkAlcohol();
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private Dialog dialog;

    protected void showDialog(String msg) {
        if (dialog == null) {
            dialog = DialogUtils.creatTipDialog(this, "提示",
                    msg, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                            finish();
                        }
                    });
            if (!isFinishing() && !dialog.isShowing()) {
                dialog.show();
            }
        }
    }

    private UserBean verifyIdentity(int id, String deviceType) {
        //根据指纹id获取警员信息
        if (!userBiosBeanList.isEmpty()) {
            for (UserBiosBean userBiosBean : userBiosBeanList) {
                int biometricsNumber = userBiosBean.getBiometricsNumber();
                String biometricsType = userBiosBean.getBiometricsType();
                if (biometricsNumber == id && biometricsType.equals(deviceType)) {
                    int userId = userBiosBean.getUserId();
                    if (!userList.isEmpty()) {
                        for (UserBean userBean : userList) {
                            int userId1 = userBean.getUserId();
                            if (userId == userId1) {
                                LogUtil.i(TAG, "getIdentity  policeId: " + userId
                                        + " ===人员姓名: " + userBean.getUserName());
                                return userBean;
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    /**
     * 开始验证
     */
    private void verify() {
        //第一个验证警员所需要的验证方式
        verifyUser(SharedUtils.getFirstUserVerify(), 1);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(TAG, "onStop: ");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy: ");
        previewView.setVisibility(View.INVISIBLE);
        FingerManager.getInstance().fpsearch = true;
        SoundPlayUtil.getInstance().stop(streamId);
        if(SharedUtils.getFirstUserVerify() == 3 || SharedUtils.getSecondUserVerify() ==3){
            IrisManager.getInstance().cancelAction();
        }
        isStop = true;

        Alcohol.getInstance().close();

        unInitFace();
        FaceServer.getInstance().unInit();
    }

    private void unInitFace() {
        previewView.setVisibility(View.INVISIBLE);

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
        if (getFeatureDelayedDisposables != null) {
            getFeatureDelayedDisposables.clear();
        }
        if (delayFaceTaskCompositeDisposable != null) {
            delayFaceTaskCompositeDisposable.clear();
        }


    }

    /**
     * 提交临时存放枪支数据
     *
     * @param jsonString
     */
    private void postTempStore(String jsonString) {
        Log.i(TAG, "postTempStore jsonString: " + jsonString);
        HttpClient.getInstance().postTempStoreGun(this, jsonString, new HttpListener<String>() {
            @Override
            public void onSucceed(int what, Response<String> response) throws JSONException {
                Log.i(TAG, "postTempStore onSucceed  response: " + response.get());
                try {
                    if (!TextUtils.isEmpty(response.get())) {
                        if (response.get().equals("success")) {
                            showDialog("提交成功");
                            EventBus.getDefault().postSticky(new MessageEvent(
                                    EventConsts.EVENT_POST_SUCCESS, firstPolice, secondPolice));
                        } else {
                            showDialog("提交失败");
                            EventBus.getDefault().post(new MessageEvent(EventConsts.EVENT_POST_FAILURE));
                        }
                    } else {
                        showDialog("提交失败");
                        EventBus.getDefault().post(new MessageEvent(EventConsts.EVENT_POST_FAILURE));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailed(int what, Response<String> response) {
                Log.i(TAG, "onFailed error: " + response.getException().getMessage());
                showDialog("糟糕！网络错误导致提交失败");
                EventBus.getDefault().post(new MessageEvent(EventConsts.EVENT_POST_FAILURE));
            }
        });
    }

    /**
     * 提交临时存放枪支领取
     *
     * @param jsonString
     */
    private void postTempGunGet(String jsonString) {
        HttpClient.getInstance().postTempStoreGunGet(this, jsonString, new HttpListener<String>() {
            @Override
            public void onSucceed(int what, Response<String> response) throws JSONException {
                Log.i(TAG, "onSucceed postTempStoreGet response: " + response.get());
                try {
                    if (!TextUtils.isEmpty(response.get())) {
                        if (response.get().equals("success")) {
                            showDialog("提交成功！");
                            EventBus.getDefault().postSticky(new MessageEvent(
                                    EventConsts.EVENT_POST_SUCCESS, firstPolice, secondPolice));
                        } else {
                            showDialog("提交失败！");
                            EventBus.getDefault().post(new MessageEvent(EventConsts.EVENT_POST_FAILURE));
                        }
                    } else {
                        showDialog("提交数据失败！");
                        EventBus.getDefault().post(new MessageEvent(EventConsts.EVENT_POST_FAILURE));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailed(int what, Response<String> response) {
                showDialog("糟糕！网络错误导致提交失败");
                EventBus.getDefault().post(new MessageEvent(EventConsts.EVENT_POST_FAILURE));
            }
        });
    }

    /**
     * 提交入库任务数据
     *
     * @param jsonBody
     */
    private void postInstoreData(String jsonBody) {
        HttpClient.getInstance().postInstoreData(this, jsonBody, new HttpListener<String>() {
            @Override
            public void onSucceed(int what, Response<String> response) throws JSONException {
                Log.i(TAG, "onSucceed postInstoreData response: " + response.get());
                try {
                    if (!TextUtils.isEmpty(response.get())) {
                        if (response.get().equals("success")) {
                            showDialog("提交成功了");
                            EventBus.getDefault().postSticky(new MessageEvent(EventConsts.EVENT_POST_SUCCESS, firstPolice, secondPolice));
                        } else {
                            showDialog("提交失败了");
                            EventBus.getDefault().postSticky(new MessageEvent(EventConsts.EVENT_POST_FAILURE));
                        }
                    } else {
                        showDialog("糟糕！提交失败");
                        EventBus.getDefault().postSticky(new MessageEvent(EventConsts.EVENT_POST_FAILURE));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailed(int what, Response<String> response) {
                showDialog("糟糕！网络错误导致提交失败");
                EventBus.getDefault().postSticky(new MessageEvent(EventConsts.EVENT_POST_FAILURE));
            }
        });
    }

    /**
     * 提交报废任务
     *
     * @param jsonBody
     */
    private void postScrapData(String jsonBody) {
        HttpClient.getInstance().postScrapData(this, jsonBody, new HttpListener<String>() {
            @Override
            public void onSucceed(int what, Response<String> response) throws JSONException {
                Log.i(TAG, "onSucceed postScrapData response: " + response.get());
                try {
                    if (!TextUtils.isEmpty(response.get())) {
                        if (response.get().equals("success")) {
                            showDialog("提交成功了");
                            EventBus.getDefault().postSticky(new MessageEvent(EventConsts.EVENT_POST_SUCCESS, firstPolice, secondPolice));
                        } else {
                            showDialog("提交失败了");
                            EventBus.getDefault().postSticky(new MessageEvent(EventConsts.EVENT_POST_FAILURE));
                        }
                    } else {
                        showDialog("提交数据失败");
                        EventBus.getDefault().postSticky(new MessageEvent(EventConsts.EVENT_POST_FAILURE));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailed(int what, Response<String> response) {
                showDialog("糟糕！网络错误导致提交失败");
                EventBus.getDefault().postSticky(new MessageEvent(EventConsts.EVENT_POST_FAILURE));
            }
        });
    }

    /**
     * 提交保养任务
     *
     * @param jsonString
     */
    private void postKeepGetData(String jsonString) {
        HttpClient.getInstance().postKeepData(this, jsonString, new HttpListener<String>() {
            @Override
            public void onSucceed(int what, Response<String> response) throws JSONException {
                Log.i(TAG, "onSucceed postKeepGetData response: " + response.get());
                try {
                    if (!TextUtils.isEmpty(response.get())) {
                        if (response.get().equals("success")) {
                            showDialog("提交成功！");
                            EventBus.getDefault().postSticky(new MessageEvent(EventConsts.EVENT_POST_SUCCESS, firstPolice, secondPolice));
                        } else {
                            showDialog("提交失败！");
                            EventBus.getDefault().postSticky(new MessageEvent(EventConsts.EVENT_POST_FAILURE));
                        }
                    } else {
                        showDialog("提交数据失败");
                        EventBus.getDefault().postSticky(new MessageEvent(EventConsts.EVENT_POST_FAILURE));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailed(int what, Response<String> response) {
                showDialog("糟糕！网络错误导致提交失败");
                EventBus.getDefault().postSticky(new MessageEvent(EventConsts.EVENT_POST_FAILURE));
            }
        });
    }

    /**
     * 提交紧急出警归还枪弹数据
     *
     * @param data
     * @param urgentTaskId
     */
    private void modifyUrgentBackTask(String data, Long urgentTaskId) {
        List<UrgentGetListBean> urgentGetListBeans = JSON.parseArray(data, UrgentGetListBean.class);
        if (!urgentGetListBeans.isEmpty()) {
            List<UrgentBackListBean> urgentBackListBeanList = new ArrayList<>();
            for (UrgentGetListBean taskInfo : urgentGetListBeans) {
                UrgentBackListBean urgentBackListBean = new UrgentBackListBean();
                urgentBackListBean.setUrgentTaskListId(taskInfo.getUrgentTaskListId());
                urgentBackListBean.setGunCabinetLocationId(taskInfo.getGunCabinetLocationId());
                urgentBackListBean.setInObjectNumber(taskInfo.getOutObjectNumber());
                urgentBackListBean.setLocationNo(taskInfo.getLocationNo());
                urgentBackListBean.setLocationType(taskInfo.getLocationType());
                urgentBackListBean.setObjectId(taskInfo.getObjectId());
                urgentBackListBean.setObjectType(taskInfo.getObjectType());
                urgentBackListBean.setTaskBackId(urgentTaskId);

                int locationNo = taskInfo.getLocationNo();        //位置编号
                //拿到本地数据
                SubCabBean subCabBean = subCabBeanDao.queryBuilder().where(
                        SubCabBeanDao.Properties.LocationNo.eq(locationNo)).unique();
                //修改枪支状态为在库
                subCabBean.setGunState("in");
                subCabBeanDao.update(subCabBean); //更新数据
                urgentBackListBeanList.add(urgentBackListBean);
            }
            urgentBackListBeanDao.insertInTx(urgentBackListBeanList);
            //修改本地数据库枪支数据状态 然后将任务置为结束状态
//        List<UrgentBackListBean> urgentBackListBeans = JSON.parseArray(data, UrgentBackListBean.class);
            UrgentOutBean urgentOutBean = urgentOutBeanDao.queryBuilder().where(
                    UrgentOutBeanDao.Properties.TId.eq(urgentTaskId)).unique();
            if (urgentOutBean != null) {
                urgentOutBean.setUrgentBackList(urgentBackListBeanList);
                urgentOutBean.setTaskFinish(true);
                urgentOutBean.setInTime(Utils.longTime2String(System.currentTimeMillis()));
                urgentOutBean.setUpdateTime(Utils.longTime2String(System.currentTimeMillis()));
                urgentOutBeanDao.update(urgentOutBean);
            }
            //上传后台服务器
            if (SharedUtils.getIsServerOnline()) {
                postUrgentData(urgentOutBean, 1);
            }
            showDialog("提交成功！");
            EventBus.getDefault().post(new MessageEvent(
                    EventConsts.EVENT_POST_SUCCESS, firstPolice, secondPolice));
        } else {
            showDialog("提交失败！");
            EventBus.getDefault().post(new MessageEvent(EventConsts.EVENT_POST_FAILURE));
        }
    }

    /**
     * 提交紧急领枪归还枪支数据
     *
     * @param urgentOutBean
     */
    private void postUrgentBackData(final UrgentOutBean urgentOutBean) {
        String jsonBody = JSON.toJSONString(urgentOutBean);
        LogUtil.i(TAG, "postUrgentBackData jsonBody: " + jsonBody);
        HttpClient.getInstance().postUrgenTaskBackData(this, jsonBody, new HttpListener<String>() {
            @Override
            public void onSucceed(int what, Response<String> response) throws JSONException {
                Log.i(TAG, "onSucceed postUrgentBackData response: " + response.get());
                try {
                    if (!TextUtils.isEmpty(response.get())) {
                        if (response.get().equals("success")) {
                            urgentOutBean.setIsBackUpload(true);
                            urgentOutBeanDao.update(urgentOutBean);
                            ToastUtil.showShort("提交成功");
                        } else {
                            ToastUtil.showShort("提交失败");
                        }
                    } else {
                        ToastUtil.showShort("提交失败");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    ToastUtil.showShort("提交出错");
                }
            }

            @Override
            public void onFailed(int what, Response<String> response) {
                ToastUtil.showShort("网络错误导致提交失败");
            }
        });
    }

    /**
     * 提交紧急领枪数据
     *
     * @param checkedList
     */
    private void openLockAndPostTask(List<SubCabBean> checkedList) {
        UrgentOutBean urgentOutBean = new UrgentOutBean();
        urgentOutBean.setApply(String.valueOf(firstPolice.getUserId()));
        urgentOutBean.setApplyName(firstPolice.getUserName());
        urgentOutBean.setApproval(String.valueOf(secondPolice.getUserId()));
        urgentOutBean.setApprovalName(secondPolice.getUserName());
        urgentOutBean.setGunCabinetId(SharedUtils.getGunCabId());
        urgentOutBean.setOutTime(Utils.longTime2String(System.currentTimeMillis()));
        urgentOutBean.setUpdateTime(Utils.longTime2String(System.currentTimeMillis()));

        List<UrgentGetListBean> urgentGetList = new ArrayList<>();

        for (SubCabBean subCabBean : checkedList) {
            UrgentGetListBean urgentTaskBean = new UrgentGetListBean();
            urgentTaskBean.setUrgentTaskListId(UUID.randomUUID().toString());
            urgentTaskBean.setGunCabinetLocationId(subCabBean.getId());
            urgentTaskBean.setLocationType(subCabBean.getLocationType());
            urgentTaskBean.setObjectId(subCabBean.getObjectId());
            urgentTaskBean.setLocationNo(subCabBean.getLocationNo());
            urgentTaskBean.setObjectType(subCabBean.getObjectName());
            urgentTaskBean.setGunNo(subCabBean.getGunNo());
            urgentGetList.add(urgentTaskBean);
        }

        urgentGetListBeanDao.insertInTx(urgentGetList);
        urgentOutBean.setUrgentGetList(urgentGetList);
        urgentOutBean.setIsGetUpload(false);//默认领出 未提交后台，
        urgentOutBean.setIsBackUpload(false);//默认归还 未提交后台，
        urgentOutBean.setTaskFinish(false); //任务未结束
        urgentOutBean.setUrgentTaskId(UUID.randomUUID().toString()); //主键
        long urgentOutId = urgentOutBeanDao.insert(urgentOutBean);
        Log.i(TAG, "openLockAndPostTask urgentOutId: " + urgentOutId);
        if (urgentOutId > 0) { //提交成功 获取主键
            if (!urgentGetList.isEmpty()) {
                for (UrgentGetListBean urgentGetListBean : urgentGetList) {
                    urgentGetListBean.setTaskGetId(urgentOutId); //任务传入主键
                    urgentGetListBeanDao.update(urgentGetListBean); //更新任务数据
                }
            }
            //修改当前枪支和子弹在库情况
            for (SubCabBean subCabBean : checkedList) {
                String id = subCabBean.getId();
                Log.i(TAG, "openLockAndPostTask subcab id: " + id);
                SubCabBean unique = subCabBeanDao.queryBuilder().where(
                        SubCabBeanDao.Properties.Id.eq(id)).unique();
                Log.i(TAG, "openLockAndPostTask unique: " + JSON.toJSONString(unique));
                unique.setGunState("out"); //枪支设置为领出状态
                subCabBeanDao.update(unique); //更新数据表
                SubCabBean unique2 = subCabBeanDao.queryBuilder().where(
                        SubCabBeanDao.Properties.Id.eq(id)).unique();
                Log.i(TAG, "openLockAndPostTask unique2: " + JSON.toJSONString(unique2));
            }
            showDialog("提交成功！");
            EventBus.getDefault().post(new MessageEvent(
                    EventConsts.EVENT_POST_SUCCESS, firstPolice, secondPolice));
            //将领取数据提交到后台
            if (SharedUtils.getIsServerOnline()) {
                postUrgentData(urgentOutBean, 0);
            }
        } else {
            showDialog("提交失败！");
            EventBus.getDefault().post(new MessageEvent(
                    EventConsts.EVENT_POST_FAILURE));
        }
//        urgentOutBean.setUrgentTaskList(urgentList);
//        String jsonString = JSON.toJSONString(urgentOutBean, SerializerFeature.WriteMapNullValue);
//        LogUtil.i(TAG, "openLockAndPostTask  jsonString: " + jsonString);
//        postUrgentGetData(jsonString, 1);
    }

    /**
     * 提交紧急领取弹数据
     */
    private void openLockAndPostTask(String remark, int type) {
        UrgentOutBean urgentOutBean = new UrgentOutBean();
        //获取警员数据
        urgentOutBean.setApply(String.valueOf(firstPolice.getUserId()));
        urgentOutBean.setApproval(String.valueOf(secondPolice.getUserId()));
        urgentOutBean.setGunCabinetId(SharedUtils.getGunCabId());
        if (type == 0) {
            urgentOutBean.setOutTime(Utils.longTime2String(System.currentTimeMillis()));
        } else {
            urgentOutBean.setInTime(Utils.longTime2String(System.currentTimeMillis()));
        }
        urgentOutBean.setRemark(remark);
        urgentOutBean.setIsGetUpload(false);
        urgentOutBean.setIsBackUpload(false);
        SerialPortUtil.getInstance().openLock(SharedUtils.getLeftCabNo());

        if (SharedUtils.getIsServerOnline()) {
            postUrgentData(urgentOutBean, 2);
//            postUrgentGetData(urgentOutBean);
        }

        showDialog("验证成功，柜门已打开!");
        ToastUtil.showShort("验证成功，柜门已打开!");
        SoundPlayUtil.getInstance().play(R.raw.cab_open);
    }

    /**
     * 提交紧急出警领枪数据
     *
     * @param urgentOutBean
     */
    private void postUrgentData(final UrgentOutBean urgentOutBean, final int type) {
        Gson gson = new Gson();
        String jsonString = gson.toJson(urgentOutBean);
//        String jsonString = JSON.toJSONString(urgentOutBean);
        LogUtil.i(TAG, "postUrgentGetData  jsonString: " + jsonString);
        HttpClient.getInstance().postUrgentData(this, jsonString, new HttpListener<String>() {
            @Override
            public void onSucceed(int what, Response<String> response) throws JSONException {
                Log.i(TAG, "onSucceed postUrgentData response: " + response.get());
                if (!SharedUtils.getIsServerOnline()) {
                    SharedUtils.setIsServerOnline(true);
                }
                try {
                    if (!TextUtils.isEmpty(response.get())) {
                        if (response.get().equals("success")) {
                            //提交成功将任务领出设置为已提交
                            if (type == 0) {
                                urgentOutBean.setIsGetUpload(true);
                                urgentOutBean.setUpdateTime(Utils.longTime2String(System.currentTimeMillis()));
                                urgentOutBeanDao.update(urgentOutBean);
                            } else if (type == 1) {
                                urgentOutBean.setIsBackUpload(true);
                                urgentOutBean.setUpdateTime(Utils.longTime2String(System.currentTimeMillis()));
                                urgentOutBeanDao.update(urgentOutBean);
                            } else if (type == 2) {
                                urgentOutBean.setUpdateTime(Utils.longTime2String(System.currentTimeMillis()));
                                urgentOutBeanDao.insert(urgentOutBean);
                            }
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
                Log.i(TAG, "onSucceed 提交失败: " + response.get());
                if (SharedUtils.getIsServerOnline()) {
                    SharedUtils.setIsServerOnline(false);
                }
            }
        });
    }

    /**
     * 提交紧急领枪数据
     *
     * @param urgentOutBean
     */
    private void postUrgentGetData(final UrgentOutBean urgentOutBean) {
//        String jsonString = JSON.toJSONString(urgentOutBean);
        Gson gson = new Gson();
        String jsonString = gson.toJson(urgentOutBean);
        LogUtil.i(TAG, "postUrgentGetData  jsonString: " + jsonString);
        HttpClient.getInstance().postUrgentGet(this, jsonString, new HttpListener<String>() {
            @Override
            public void onSucceed(int what, Response<String> response) throws JSONException {
                Log.i(TAG, "onSucceed postUrgentGetData response: " + response.get());
                try {
                    if (!TextUtils.isEmpty(response.get())) {
                        if (response.get().equals("success")) {
                            //领取枪支
                            //提交成功将任务领出设置为已提交
                            Log.i(TAG, "onSucceed 提交紧急领枪成功: ");
                            urgentOutBean.setIsGetUpload(true);
                            urgentOutBean.setIsBackUpload(true);
                            urgentOutBean.setTaskFinish(true);
                            urgentOutBean.setUpdateTime(Utils.longTime2String(System.currentTimeMillis()));
                            long insert = urgentOutBeanDao.insert(urgentOutBean);
                            if (insert > 0) {
                                Log.i(TAG, "onSucceed 插入数据成功: ");
                            } else {
                                Log.i(TAG, "onSucceed 插入数据失败: ");
                            }
                        } else {
                            Log.i(TAG, "onSucceed 提交失败: ");
                        }
                    } else {
                        Log.i(TAG, "onSucceed 提交失败: ");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.i(TAG, "onSucceed 提交出错: ");
                }
            }

            @Override
            public void onFailed(int what, Response<String> response) {
                Log.i(TAG, "onFailed: 网络请求出错，提交失败!");
            }
        });
    }

    /**
     * 提交出警领还枪数据
     *
     * @param jsonBody
     */
    private void postGetData(String jsonBody, final UserBean firstPolice, final UserBean secondPolice) {
        HttpClient.getInstance().postPoliceTaskData(this, jsonBody, new HttpListener<String>() {
            @Override
            public void onSucceed(int what, Response<String> response) throws JSONException {
                Log.i(TAG, "onSucceed postGetData response: " + response.get());
                try {
                    if (!TextUtils.isEmpty(response.get())) {
                        if (response.get().equals("success")) {
                            showDialog("提交成功！");
                            try {
                                //上传开柜数据
                                if (Constants.isUploadMessage) {
                                    uploadOpenMessage(firstPolice, secondPolice);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            EventBus.getDefault().postSticky(new MessageEvent(EventConsts.EVENT_POST_SUCCESS, firstPolice, secondPolice));
                        } else {
                            //                        ToastUtil.showShort("提交数据失败！");
                            showDialog("提交数据失败！");
                            EventBus.getDefault().postSticky(new MessageEvent(EventConsts.EVENT_POST_FAILURE));
                        }
                    } else {
                        //                    ToastUtil.showShort("提交数据失败");
                        showDialog("提交数据失败！");
                        EventBus.getDefault().postSticky(new MessageEvent(EventConsts.EVENT_POST_FAILURE));
                    }
                } catch (Exception e) {
                    showDialog("提交数据出错！");
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailed(int what, Response<String> response) {
                showDialog("网络错误,提交失败");
                EventBus.getDefault().postSticky(new MessageEvent(EventConsts.EVENT_POST_FAILURE));
            }
        });
    }

    private void uploadOpenMessage(UserBean firstPolice, UserBean secondPolice) {
        UploadOpenMsg uploadOpenMsg = new UploadOpenMsg();
        uploadOpenMsg.setcID(SharedUtils.getGunCabId());
        uploadOpenMsg.setIdentityNumber1(firstPolice.getCardId());
        uploadOpenMsg.setIdentityNumber2(secondPolice.getCardId());
        uploadOpenMsg.setQzsl(1);
        uploadOpenMsg.setTime(new Date(System.currentTimeMillis()));
        uploadOpenMsg.setType("0");
        uploadOpenMsg.setZdlx(1);
        uploadOpenMsg.setZdsl(100);

        String jsonString = JSON.toJSONString(uploadOpenMsg);
        Log.i(TAG, "uploadOpenMessage jsonString: " + jsonString);
        HttpClient.getInstance().uploadOpenMessage(this, jsonString, new HttpListener<String>() {
            @Override
            public void onSucceed(int what, Response<String> response) throws JSONException {

            }

            @Override
            public void onFailed(int what, Response<String> response) {

            }
        });
    }


}
