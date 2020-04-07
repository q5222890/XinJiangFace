package com.zack.xjht.ui.fragment;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.yanzhenjie.nohttp.rest.Response;
import com.zack.xjht.Constants;
import com.zack.xjht.R;
import com.zack.xjht.Utils.DialogUtils;
import com.zack.xjht.Utils.LogUtil;
import com.zack.xjht.Utils.SharedUtils;
import com.zack.xjht.Utils.SoundPlayUtil;
import com.zack.xjht.Utils.ToastUtil;
import com.zack.xjht.Utils.TransformUtil;
import com.zack.xjht.Utils.Utils;
import com.zack.xjht.db.DBManager;
import com.zack.xjht.db.gen.CommonLogBeanDao;
import com.zack.xjht.db.gen.UserBeanDao;
import com.zack.xjht.db.gen.UserBiosBeanDao;
import com.zack.xjht.entity.SubCabBean;
import com.zack.xjht.entity.UrgentOutBean;
import com.zack.xjht.entity.UserBean;
import com.zack.xjht.entity.UserBiosBean;
import com.zack.xjht.event.EventConsts;
import com.zack.xjht.event.MessageEvent;
import com.zack.xjht.finger.FingerManager;
import com.zack.xjht.http.HttpClient;
import com.zack.xjht.http.HttpListener;
import com.zack.xjht.serial.SerialPortUtil;
import com.zack.xjht.ui.BackActivity;
import com.zack.xjht.ui.GetActivity;
import com.zack.xjht.ui.InStoreActivity;
import com.zack.xjht.ui.KeepActivity;
import com.zack.xjht.ui.LoginActivity;
import com.zack.xjht.ui.ScrapActivity;
import com.zack.xjht.ui.SettingsActivity;
import com.zack.xjht.ui.TempStoreActivity;
import com.zack.xjht.ui.UrgentGoActivity;
import com.zack.xjht.ui.UserActivity;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * 指纹验证
 */
public class VerifyFingerFragment extends Fragment implements FingerManager.IFingerStatus {
    private static final String TAG = "VerifyFingerFragment";

    @BindView(R.id.verify_finger_iv_img)
    ImageView verifyFingerIvImg;
    @BindView(R.id.verify_finger_tv_msg)
    TextView verifyFingerTvMsg;
    Unbinder unbinder;
    @BindView(R.id.verify_finger_tv_user)
    TextView verifyFingerTvUser;
    private int streamId;
    private String target;
    private Class<?> toClass;
    private LoginActivity login;
    private Context mContext;
    private FragmentActivity activity;
    private List<UserBean> userList = new ArrayList<>();
    private List<UserBiosBean> userBiosBeanList = new ArrayList<>();
    private UserBeanDao userBeanDao;
    private UserBiosBeanDao userBiosBeanDao;

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            String txtMsg = (String) msg.obj;
            switch (msg.what) {
                case 0:
                    if (verifyFingerTvUser != null && !TextUtils.isEmpty(txtMsg)) {
                        Log.i(TAG, "handleMessage msg: " + txtMsg);
                        verifyFingerTvUser.setText(txtMsg);
                    }
                    break;
                case 1:
                    if (verifyFingerTvMsg != null && !TextUtils.isEmpty(txtMsg)) {
                        Log.i(TAG, "handleMessage msg: " + txtMsg);
                        verifyFingerTvMsg.setText(txtMsg);
                    }
                    break;
            }
        }
    };
    private CommonLogBeanDao commonLogBeanDao;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.i(TAG, "onAttach: ");
        mContext = context;
        login = (LoginActivity) context;
        this.activity = getActivity();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_verify_finger, container, false);
        unbinder = ButterKnife.bind(this, view);
        target = activity.getIntent().getStringExtra("activity");
        Log.i(TAG, "onCreateView activity: " + target);
        if (!TextUtils.isEmpty(target)) {
            if (Constants.isFirstVerify) {
                //第一次验证
                if (target.equals(Constants.ACTIVITY_USER)) {
//                    streamId = SoundPlayUtil.getInstance().play(R.raw.admin_verify_finger);
                    verifyFingerTvUser.setText("请系统管理员验证指纹");
                } else {
//                    streamId = SoundPlayUtil.getInstance().play(R.raw.duty_manager_finger);
                    verifyFingerTvUser.setText("请枪管员或领导验证指纹");
                }
            } else {
                //第二次验证
                //验证值班管理员指纹
//                    streamId = SoundPlayUtil.getInstance().play(R.raw.duty_manager_finger);
                verifyFingerTvUser.setText("请枪管员或领导验证指纹");
            }
            switch (target) {
                case Constants.ACTIVITY_URGENT: //紧急出警
                    toClass = UrgentGoActivity.class;
                    break;
                case Constants.ACTIVITY_GET: //领取枪弹
                    toClass = GetActivity.class;
                    break;
                case Constants.ACTIVITY_BACK://归还枪弹
                    toClass = BackActivity.class;
                    break;
                case Constants.ACTIVITY_KEEP://保养枪支
                    toClass = KeepActivity.class;
                    break;
                case Constants.ACTIVITY_SCRAP://报废枪支
                    toClass = ScrapActivity.class;
                    break;
                case Constants.ACTIVITY_TEMP_IN://临时存放枪支
                    toClass = TempStoreActivity.class;
                    break;
                case Constants.ACTIVITY_IN_STORE://枪弹入库
                    toClass = InStoreActivity.class;
                    break;
                case Constants.ACTIVITY_SETTING://系统设置
                    toClass = SettingsActivity.class;
                    break;
                case Constants.ACTIVITY_USER://人员管理
                    toClass = UserActivity.class;
                    break;
            }
        }

        commonLogBeanDao = DBManager.getInstance().getCommonLogBeanDao();
        userBeanDao = DBManager.getInstance().getUserBeanDao();
        userBiosBeanDao = DBManager.getInstance().getUserBiosBeanDao();
        if (!Constants.isFingerConnect) {
            verifyFingerTvMsg.setText("指纹设备未连接");
        }

        if (!Constants.isFingerInit) {
            FingerManager.getInstance().init(mContext);
        }

        //获取人员数据和指纹数据
        if (SharedUtils.getIsServerOnline()) {
            getUserList();
            getCharList();
        } else {
            userList = userBeanDao.loadAll();
            userBiosBeanList = userBiosBeanDao.loadAll();
            if (!userList.isEmpty() || !userBiosBeanList.isEmpty()) {
                verify();
            } else {
                showDialog("无生物特征数据");
            }
        }
        return view;
    }

    private void getCharList() {
        HttpClient.getInstance().getCharList(mContext, "", new HttpListener<String>() {
            @Override
            public void onSucceed(int what, Response<String> response) throws JSONException {
                LogUtil.i(TAG, "onSucceed getCharList response: " + response.get());
                try {
                    if (!TextUtils.isEmpty(response.get())) {
                        userBiosBeanList = JSON.parseArray(response.get(), UserBiosBean.class);
//                        downCHar(userBiosBeanList);
                        verify();
                        //                    if(userBiosBeans.isEmpty()){
                        //                        ToastUtil.showShort("获取生物特征数据为空");
                        //                    }
                    } else {
                        if (!target.equals(Constants.ACTIVITY_USER)) {
                            showDialog("获取生物特征数据失败");
                        } else {
                            ToastUtil.showShort("获取生物特征数据失败");
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailed(int what, Response<String> response) {
                showDialog("获取生物特征数据失败");
            }
        });
    }

    //下发模版
    private void downCHar(List<UserBiosBean> userBiosBeans) {
        Log.i(TAG, "downCHar: ");
        if (!userBiosBeans.isEmpty()) {
            Log.i(TAG, "downCHar size: " + userBiosBeans.size());
            if (Constants.isFingerConnect && Constants.isFingerInit) {
                FingerManager.getInstance().clearAllFinger(); //清除设备中所有指纹
            }

            for (UserBiosBean policeBiosBean : userBiosBeans) {
                String deviceType = policeBiosBean.getBiometricsType();
                String key = policeBiosBean.getBiometricsKey();
                int id = policeBiosBean.getBiometricsNumber();
                Log.i(TAG, "downCHar id: " + id);
//                byte[] decodeKey = Base64.decode(key, Base64.DEFAULT);
                byte[] decodeKey = TransformUtil.hexStrToBytes(key);
                Log.i(TAG, "downCHar key " + key + " \n decodeKey: " + decodeKey.length);
                if (deviceType.equals("1")) {
                    if (Constants.isFingerConnect && Constants.isFingerInit) {
                        Log.i(TAG, "downCHar fingerID: " + id);
                        FingerManager.getInstance().fpDownChar(id, decodeKey);
                    }
                }
            }
//            showDialog("生物特征更新成功");
            Log.i(TAG, "downCHar: 生物特征更新成功!");
            verify();
        } else {
            if (!target.equals(Constants.ACTIVITY_USER)) {
                showDialog("无生物特征数据");
            } else {
                ToastUtil.showShort("无生物特征数据");
            }
            Log.i(TAG, "downCHar:无生物特征数据 ! ");
        }
    }

    private void verify() {
        //开始验证指纹
        FingerManager.getInstance().fpsearch = false;
        FingerManager.getInstance().searchfp(verifyFingerTvMsg, this);
    }

    private void getUserList() {
        HttpClient.getInstance().getUserList(mContext, "", new HttpListener<String>() {
            @Override
            public void onSucceed(int what, Response<String> response) throws JSONException {
                try {
                    Log.i(TAG, "onSucceed getUserList response: " + response.get());
                    if (!TextUtils.isEmpty(response.get())) {
                        userList = JSON.parseArray(response.get(), UserBean.class);
                        if (!userList.isEmpty()) {

                        } else {
                            if (!target.equals(Constants.ACTIVITY_USER)) {
                                showDialog("获取人员数据为空");
                            } else {
                                ToastUtil.showShort("获取人员数据为空");
                            }
                        }
                    } else {
                        if (!target.equals(Constants.ACTIVITY_USER)) {
                            showDialog("获取数据为空");
                        } else {
                            ToastUtil.showShort("获取数据为空");
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailed(int what, Response<String> response) {
                showDialog("网络连接出错，获取人员数据失败！");
            }
        });
    }

//    private Map<Integer, List<String>> roleMap = new HashMap<>();

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
        FingerManager.getInstance().fpsearch = true;
        SoundPlayUtil.getInstance().stop(streamId);
    }

    public void sendMsg(int what, Object obj) {
        Message message = mHandler.obtainMessage(what, obj);
        message.sendToTarget();
    }

    @Override
    public void didVerify(int id, boolean success) {
        if (success) {
            if (Constants.isFirstVerify) {
                //第一次验证指纹
                firstVerifyPolice(id);
            } else {
                //第二次验证指纹
                secondVerifyPolice(id);
            }
        }
    }

    @Override
    public void timeout() {
        activity.finish();//读取指纹超时结束
    }

    /**
     * 第一次验证警员身份
     *
     * @param id
     */
    private void firstVerifyPolice(int id) {
        //根据id获取当前警员身份
        UserBean userBean = verifyIdentity(id);
        if (userBean != null) {
            login.firstPolice = userBean;
            int policeId = userBean.getUserId();
            String name = userBean.getUserName();

            DBManager.getInstance().insertCommLog(mContext, userBean,
                    "【" + name + "】验证指纹成功");

            Log.i(TAG, "run policeId: " + policeId + " name：" + name);
            sendMsg(0, "验证成功，当前警员：" + name);
            String roleKeys = userBean.getRoleKeys();
            Log.i(TAG, "firstVerify roleKeys: " + roleKeys);
            if (!TextUtils.isEmpty(roleKeys)) {
                String[] split = roleKeys.split(",");
                List<String> roleList = Arrays.asList(split);
                if (roleList != null && !roleList.isEmpty()) { //当前用户有设置权限
                    if (target.equals(Constants.ACTIVITY_SETTING) ||
                            target.equals(Constants.ACTIVITY_USER)) {
                        if (roleList.contains(Constants.ROLE_ADMIN)
                                || roleList.contains(Constants.ROLE_ROOM_ADMIN)
                                || roleList.contains(Constants.ROLE_APPROVER)
                                || roleList.contains(Constants.ROLE_MANAGER)) {
                            sendMsg(0, "验证成功 当前用户:" + name);
                            streamId = SoundPlayUtil.getInstance().play(R.raw.verify_success);
                            Intent intent = new Intent(mContext, toClass);
                            intent.putExtra("firstPoliceInfo", login.firstPolice);
                            mContext.startActivity(intent);
                            activity.finish();
                        } else {  //非系统管理员
                            sendMsg(0, "权限不足！ 当前用户:" + name);
                            streamId = SoundPlayUtil.getInstance().play(R.raw.no_permission);
                            verifyFingerAgain();
                        }
                    } else if (target.equals(Constants.ACTIVITY_KEEP)
                            || target.equals(Constants.ACTIVITY_SCRAP)
                            || target.equals(Constants.ACTIVITY_IN_STORE)
                            || target.equals(Constants.ACTIVITY_TEMP_IN)
                            || target.equals(Constants.ACTIVITY_TEMP_GET)) {
                        //枪管或领导
                        if (roleList.contains(Constants.ROLE_APPROVER) ||
                                roleList.contains(Constants.ROLE_MANAGER)) {
                            sendMsg(0, "验证成功 当前用户:" + name);
//                        Intent intent = new Intent(mContext, toClass);
//                        intent.putExtra("firstPoliceInfo", login.firstPolice);
//                        mContext.startActivity(intent);
//                        activity.finish();
                            streamId = SoundPlayUtil.getInstance().play(R.raw.verify_success);
                            postData();
                        } else { //权限不足
                            sendMsg(0, "当前用户没有权限！ 当前用户:" + name);
                            streamId = SoundPlayUtil.getInstance().play(R.raw.no_permission);
                            verifyFingerAgain();
                        }
                    } else { //其它任务
//                    if (roleList.contains(Constants.ROLE_APPROVER) ||
//                            roleList.contains(Constants.ROLE_MANAGER)) {
                        //值班管理员验证指纹
                        sendMsg(0, "请枪管员或领导验证指纹");
                        streamId = SoundPlayUtil.getInstance().play(R.raw.manager_leader_verfiy_finger);
                        Constants.isFirstVerify = false;
                        FingerManager.getInstance().searchfp(verifyFingerTvMsg, this);
//                    } else {  //非系统管理员
//                        sendMsg(0, "当前用户没有权限！ 当前用户:" + name);
//                        verifyFingerAgain();
//                    }
                    }
                } else {
                    //没有设置权限
                    sendMsg(0, "没有警员权限!");
                    streamId = SoundPlayUtil.getInstance().play(R.raw.no_permission);
                    verifyFingerAgain();
                }
            } else {
                //没有设置权限
                sendMsg(0, "没有权限!");
                streamId = SoundPlayUtil.getInstance().play(R.raw.no_permission);
                verifyFingerAgain();
            }
        } else {
            Log.i(TAG, "获取用户信息失败: ");
            sendMsg(0, "获取用户信息失败！");
            streamId = SoundPlayUtil.getInstance().play(R.raw.retry);
            verifyFingerAgain();
        }
    }

    /**
     * 重新验证指纹
     */
    private void verifyFingerAgain() {
        FingerManager.getInstance().searchfp(verifyFingerTvMsg, this);
    }

    /**
     * 验证第二人指纹
     *
     * @param id
     */
    private void secondVerifyPolice(int id) {
        UserBean userBean = verifyIdentity(id);
        if (userBean != null) {
            login.secondPolice = userBean;
            int secondPoliceId = userBean.getUserId();
            String name = userBean.getUserName();

//            CommonLogBean commonLogBean = new CommonLogBean();
//            commonLogBean.setContent("【" + name + "】验证指纹成功");
//            commonLogBean.setAddTime(System.currentTimeMillis());
//            commonLogBean.setUserId(String.valueOf(secondPoliceId));
//            commonLogBean.setUserName(name);
//            commonLogBeanDao.insert(commonLogBean);

            DBManager.getInstance().insertCommLog(mContext, userBean,
                    "【" + name + "】验证指纹成功");
            Log.i(TAG, "run policeId: " + secondPoliceId + " 姓名：" + name);
            String roleKeys = userBean.getRoleKeys();
            Log.i(TAG, "firstVerify roleKeys: " + roleKeys);

            if (login.firstPolice.getUserId() == login.secondPolice.getUserId()) {
                //判断两次验证人员是否相同
                sendMsg(0, "两次验证人员相同");
                streamId = SoundPlayUtil.getInstance().play(R.raw.twice_verify_user_same);
                verifyFingerAgain();
            } else {
                //两次验证不是一个人
                if (!TextUtils.isEmpty(roleKeys)) {
                    String[] split = roleKeys.split(",");
                    List<String> roleList = Arrays.asList(split);
                    if (roleList != null && !roleList.isEmpty()) { //判断权限
                        //判断是否值班管理员或领导
                        sendMsg(0, "验证成功 当前用户:" + name);
                        //获取传递的数据
                        streamId = SoundPlayUtil.getInstance().play(R.raw.verify_success);
                        postData();
                    } else {  //没有权限
                        sendMsg(0, "没有获取到用户权限！ 请先设置权限");
                        streamId = SoundPlayUtil.getInstance().play(R.raw.no_permission);
                        verifyFingerAgain();
                    }
                }
            }
        } else {
            Log.i(TAG, "用户不存在: ");
            sendMsg(0, "获取用户信息失败！");
            streamId = SoundPlayUtil.getInstance().play(R.raw.retry);
            verifyFingerAgain();
        }
    }

    private void postData() {
        switch (target) {
            case Constants.ACTIVITY_GET: //取枪
                String data = activity.getIntent().getStringExtra("data");
                String taskId = activity.getIntent().getStringExtra("taskId");
                int policeId = activity.getIntent().getIntExtra("policeId", 0);
                Log.i(TAG, "secondVerifyPolice taskId: " + taskId);
                if (policeId == login.firstPolice.getUserId() ||
                        policeId == login.secondPolice.getUserId()) {
                    if (!TextUtils.isEmpty(data)) {
                        postGetData(data);
                    }
                } else {
                    showDialog("获取对应警员失败！");
                }
                break;
            case Constants.ACTIVITY_BACK: //还枪
                data = activity.getIntent().getStringExtra("data");
                taskId = activity.getIntent().getStringExtra("taskId");
                policeId = activity.getIntent().getIntExtra("policeId", 0);
                Log.i(TAG, "secondVerifyPolice taskId: " + taskId);
                if (policeId == login.firstPolice.getUserId() ||
                        policeId == login.secondPolice.getUserId()) {
                    if (!TextUtils.isEmpty(data)) {
                        postGetData(data);
                    }
                } else {
                    showDialog("获取对应警员失败！");
                }
                break;
            case Constants.ACTIVITY_URGENT_GET_GUN:
            case Constants.ACTIVITY_URGENT_GET_AMMO:
                data = activity.getIntent().getStringExtra("data");
                String cabId = activity.getIntent().getStringExtra("cabId");
                if (!TextUtils.isEmpty(data)) {
                    final List<SubCabBean> subCabBeans = JSON.parseArray(data, SubCabBean.class);
                    openLockAndPostTask(subCabBeans, cabId);
                }
                break;
            case Constants.ACTIVITY_URGENT_BACK_GUN:
            case Constants.ACTIVITY_URGENT_BACK_AMMO:
                data = activity.getIntent().getStringExtra("data");
                if (!TextUtils.isEmpty(data)) {
                    postUrgentBackData(data);
                }
                break;
            case Constants.ACTIVITY_KEEP: //保养任务
                data = activity.getIntent().getStringExtra("data");
                if (!TextUtils.isEmpty(data)) {
                    postKeepGetData(data);
                }
                break;
            case Constants.ACTIVITY_SCRAP:
                data = activity.getIntent().getStringExtra("data");
                if (!TextUtils.isEmpty(data)) {
                    postScrapData(data);
                }
                break;
            case Constants.ACTIVITY_IN_STORE:
                data = activity.getIntent().getStringExtra("data");
                if (!TextUtils.isEmpty(data)) {
                    postInstoreData(data);
                }
                break;
            case Constants.ACTIVITY_TEMP_IN: //临时存放
                data = activity.getIntent().getStringExtra("data");
                if (!TextUtils.isEmpty(data)) {
                    postTempStore(data);
                }
                break;
            case Constants.ACTIVITY_TEMP_GET: //临时存放领出
                data = activity.getIntent().getStringExtra("data");
                if (!TextUtils.isEmpty(data)) {
                    postTempGunGet(data);
                }
                break;
            case Constants.ACTIVITY_OPEN_CAB:
                SerialPortUtil.getInstance().openLock(SharedUtils.getLeftCabNo());
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                SerialPortUtil.getInstance().openLock(SharedUtils.getRightCabNo());
                showDialog("打开枪柜成功");
                break;
        }
    }

    /**
     * 提交临时存放枪支数据
     *
     * @param jsonString
     */
    private void postTempStore(String jsonString) {
        HttpClient.getInstance().postTempStoreGun(mContext, jsonString, new HttpListener<String>() {
            @Override
            public void onSucceed(int what, Response<String> response) throws JSONException {
                Log.i(TAG, "postTempStore onSucceed  response: " + response.get());
                try {
                    if (!TextUtils.isEmpty(response.get())) {
                        if (response.get().equals("success")) {
                            showDialog("提交成功");
                            EventBus.getDefault().post(new MessageEvent(EventConsts.EVENT_POST_SUCCESS));
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
        HttpClient.getInstance().postTempStoreGunGet(mContext, jsonString, new HttpListener<String>() {
            @Override
            public void onSucceed(int what, Response<String> response) throws JSONException {
                Log.i(TAG, "onSucceed postTempStoreGet response: " + response.get());
                try {
                    if (!TextUtils.isEmpty(response.get())) {
                        if (response.get().equals("success")) {
                            showDialog("提交成功！");
                            EventBus.getDefault().post(new MessageEvent(EventConsts.EVENT_POST_SUCCESS));
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
        HttpClient.getInstance().postInstoreData(mContext, jsonBody, new HttpListener<String>() {
            @Override
            public void onSucceed(int what, Response<String> response) throws JSONException {
                try {
                    Log.i(TAG, "onSucceed postInstoreData response: " + response.get());
                    if (!TextUtils.isEmpty(response.get())) {
                        if (response.get().equals("success")) {
                            showDialog("提交成功了");
                            EventBus.getDefault().postSticky(new MessageEvent(EventConsts.EVENT_POST_SUCCESS));
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
        HttpClient.getInstance().postScrapData(mContext, jsonBody, new HttpListener<String>() {
            @Override
            public void onSucceed(int what, Response<String> response) throws JSONException {
                Log.i(TAG, "onSucceed postScrapData response: " + response.get());
                try {
                    if (!TextUtils.isEmpty(response.get())) {
                        if (response.get().equals("success")) {
                            showDialog("提交成功了");
                            EventBus.getDefault().postSticky(new MessageEvent(EventConsts.EVENT_POST_SUCCESS));
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
        HttpClient.getInstance().postKeepData(mContext, jsonString, new HttpListener<String>() {
            @Override
            public void onSucceed(int what, Response<String> response) throws JSONException {
                Log.i(TAG, "onSucceed postKeepGetData response: " + response.get());
                try {
                    if (!TextUtils.isEmpty(response.get())) {
                        if (response.get().equals("success")) {
                            showDialog("提交成功！");
                            EventBus.getDefault().postSticky(new MessageEvent(EventConsts.EVENT_POST_SUCCESS));
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
     * 提交紧急领枪归还枪支数据
     *
     * @param jsonBody
     */
    private void postUrgentBackData(String jsonBody) {
        HttpClient.getInstance().postUrgenTaskBackData(mContext, jsonBody, new HttpListener<String>() {
            @Override
            public void onSucceed(int what, Response<String> response) throws JSONException {
                Log.i(TAG, "onSucceed postUrgentBackData response: " + response.get());
                try {
                    if (!TextUtils.isEmpty(response.get())) {
                        if (response.get().equals("success")) {
                            //                        Objects.requireNonNull(activity).finish();
                            showDialog("提交成功！");
                            EventBus.getDefault().post(new MessageEvent(EventConsts.EVENT_POST_SUCCESS));
                        } else {
                            showDialog("提交数据失败了！");
                            EventBus.getDefault().post(new MessageEvent(EventConsts.EVENT_POST_FAILURE));
                        }
                    } else {
                        showDialog("提交数据异常！");
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
     * 提交紧急领枪数据
     *
     * @param checkedList
     */
    private void openLockAndPostTask(List<SubCabBean> checkedList, String cabId) {
        UrgentOutBean urgentOutBean = new UrgentOutBean();
        //获取警员数据
        urgentOutBean.setApply(String.valueOf(login.firstPolice.getUserId()));
        urgentOutBean.setApproval(String.valueOf(login.secondPolice.getUserId()));
        urgentOutBean.setGunCabinetId(cabId);
        urgentOutBean.setOutTime(Utils.longTime2String(System.currentTimeMillis()));
        List<UrgentTaskListBean> urgentList = new ArrayList<>();
        if (!checkedList.isEmpty()) {
            for (SubCabBean subCabBean : checkedList) {
                UrgentTaskListBean urgentTaskBean = new UrgentTaskListBean();
                urgentTaskBean.setGunCabinetLocationId(subCabBean.getId());
                urgentTaskBean.setLocationType(subCabBean.getLocationType());
                urgentTaskBean.setObjectId(subCabBean.getObjectId());
                String locationType = subCabBean.getLocationType();
                if (!TextUtils.isEmpty(locationType)) {
                    if (locationType.equals(Constants.TYPE_AMMO)) {
                        urgentTaskBean.setOutObjectNumber(String.valueOf(subCabBean.getObjectNumber()));
                    } else {
                        urgentTaskBean.setOutObjectNumber("1");
                    }
                }
                urgentList.add(urgentTaskBean);
            }
        }
//        urgentOutBean.setUrgentTaskList(urgentList);
//        String jsonString = JSON.toJSONString(urgentOutBean, SerializerFeature.WriteMapNullValue);
//        LogUtil.i(TAG, "openLockAndPostTask  jsonString: " + jsonString);
//        postUrgentGetData(jsonString);
    }

    /**
     * 提交紧急领枪数据
     *
     * @param jsonString
     */
    private void postUrgentGetData(String jsonString) {
        HttpClient.getInstance().postUrgentGet(mContext, jsonString, new HttpListener<String>() {
            @Override
            public void onSucceed(int what, Response<String> response) throws JSONException {
                Log.i(TAG, "onSucceed postGetGunData response: " + response.get());
                try {
                    if (!TextUtils.isEmpty(response.get())) {
                        if (response.get().equals("success")) {
                            showDialog("提交成功！");
                            EventBus.getDefault().post(new MessageEvent(EventConsts.EVENT_POST_SUCCESS));
                        } else {
                            Log.i(TAG, "onSucceed 提交失败: ");
                            showDialog("提交失败！");
                            EventBus.getDefault().post(new MessageEvent(EventConsts.EVENT_POST_FAILURE));
                        }
                    } else {
                        showDialog("提交数据失败！");
                        Log.i(TAG, "onSucceed 提交失败: ");
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
     * 提交出警领还枪数据
     *
     * @param jsonBody
     */
    private void postGetData(String jsonBody) {
        HttpClient.getInstance().postPoliceTaskData(mContext, jsonBody, new HttpListener<String>() {
            @Override
            public void onSucceed(int what, Response<String> response) throws JSONException {
                Log.i(TAG, "onSucceed postGetData response: " + response.get());
                try {
                    if (!TextUtils.isEmpty(response.get())) {
                        if (response.get().equals("success")) {
                            showDialog("提交成功！");
                            EventBus.getDefault().postSticky(new MessageEvent(EventConsts.EVENT_POST_SUCCESS));
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
                showDialog("糟糕！网络错误导致提交失败");
                EventBus.getDefault().postSticky(new MessageEvent(EventConsts.EVENT_POST_FAILURE));
            }
        });
    }

    private UserBean verifyIdentity(int id) {
        //根据指纹id获取警员信息
        if (!userBiosBeanList.isEmpty()) {
            for (UserBiosBean userBiosBean : userBiosBeanList) {
                int biometricsNumber = userBiosBean.getBiometricsNumber();
                if (biometricsNumber == id) {
                    int userId = userBiosBean.getUserId();
                    if (!userList.isEmpty()) {
                        for (UserBean userBean : userList) {
                            int userId1 = userBean.getUserId();
                            if (userId == userId1) {
                                LogUtil.i(TAG, "getIdentity  policeId: " + userId
                                        + " ===警员姓名: " + userBean.getUserName());
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
     * 显示dialog关闭当前页面
     */
    private Dialog dialog;

    private void showDialog(String msg) {
        if (dialog != null) {
            if (!activity.isFinishing()) {
                dialog.show();
            }
            DialogUtils.setTipText(msg);
            Log.i(TAG, "dialog is not null ");
        } else { //dialog为null
            dialog = DialogUtils.creatTipDialog(mContext, "提示", msg,
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //获取最新的数据 并刷新适配器
                            dialog.dismiss();
                            Objects.requireNonNull(activity).finish();
                        }
                    });
            if (!activity.isFinishing()) {
                dialog.show();
            }
            Log.i(TAG, "dialog is null");
        }
    }

}
