package com.zack.xjht.ui.dialog;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
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
import com.zack.xjht.db.gen.AlarmLogBeanDao;
import com.zack.xjht.db.gen.UserBeanDao;
import com.zack.xjht.db.gen.UserBiosBeanDao;
import com.zack.xjht.entity.AlarmLogBean;
import com.zack.xjht.entity.UploadAlarmMsg;
import com.zack.xjht.entity.UserBean;
import com.zack.xjht.entity.UserBiosBean;
import com.zack.xjht.finger.FingerManager;
import com.zack.xjht.http.HttpClient;
import com.zack.xjht.http.HttpListener;
import com.zack.xjht.serial.SerialPortUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;


/**
 * 报警窗体
 */
public class AlarmDialog extends Dialog implements FingerManager.IFingerStatus {
    private static final String TAG = "AlarmDialog";
    private static final int MAX_SCREEN_BRIGHTNESS = 255;

    @BindView(R.id.dl_alarm_txt_msg)
    TextView dlAlarmTxtMsg;
    @BindView(R.id.dl_btn_relieve_alarm)
    Button dlBtnRelieveAlarm;
    @BindView(R.id.dl_img_alarm)
    ImageView dlImgAlarm;
    private Unbinder bind;
    private int streamId;
    @BindView(R.id.dl_alarm_txt_reason)
    TextView dlAlarmTxtReason;
    @BindView(R.id.dl_alarm_txt_verify)
    TextView dlAlarmTxtVerify;
    private Context mContext;
    private String reason;
    private int alarmType;
    private int retIdentify;
    private boolean isStop;
    private Handler mHandler = new Handler(Looper.getMainLooper());
    private List<UserBiosBean> userBiosBeanList = new ArrayList<>();
    private List<UserBean> userList = new ArrayList<>();
    private AlarmLogBeanDao alarmLogBeanDao;
    private long insert;
    private AlarmLogBean alarmLogBean;
    private UserBeanDao userBeanDao;
    private UserBiosBeanDao userBiosBeanDao;
//    private RoleBeanDao roleBeanDao;
//    private List<MembersBean> policeList =new ArrayList<>();

    public AlarmDialog(@NonNull Context context, String reason, int alarmLogType) {
        super(context, R.style.dialog);
        this.mContext = context;
        this.reason = reason;
        this.alarmType = alarmLogType;
        initView();
    }

    public void initView() {
        //MAX_SCREEN_BRIGHTNESS为255，brightness 在0和255之间

        setContentView(R.layout.dialog_alarm);
        bind = ButterKnife.bind(this);
        setCancelable(false);
        setCanceledOnTouchOutside(false);
        dlAlarmTxtReason.setText(reason);
        dlImgAlarm.setBackgroundResource(R.drawable.alarm_anim);
        AnimationDrawable animation = (AnimationDrawable) dlImgAlarm.getBackground();
        animation.setOneShot(false);
        animation.start();

        try {
            //保存报警日志
            alarmLogBean = new AlarmLogBean();
            alarmLogBean.setWarningTime(Utils.longTime2String(System.currentTimeMillis()));
            alarmLogBean.setWarningType(String.valueOf(alarmType));
            alarmLogBean.setWarningState(String.valueOf(2));
            alarmLogBean.setWarningContent(reason);
            alarmLogBean.setGunCabinetId(SharedUtils.getGunCabId());
            alarmLogBean.setIsSync(false);

            userBeanDao = DBManager.getInstance().getUserBeanDao();
            userBiosBeanDao = DBManager.getInstance().getUserBiosBeanDao();
//            roleBeanDao = DBManager.getInstance().getRoleBeanDao();
            alarmLogBeanDao = DBManager.getInstance().getAlarmLogBeanDao();
            insert = alarmLogBeanDao.insert(alarmLogBean);

            //上传平台
            if (Constants.isUploadMessage) {
                UploadAlarmMsg uploadAlarmMsg = new UploadAlarmMsg();
                uploadAlarmMsg.setcID(SharedUtils.getGunCabId());
                uploadAlarmMsg.setAlarmContect(reason);
                uploadAlarmMsg.setTime(new Date(System.currentTimeMillis()));
                String jsonString = JSON.toJSONString(uploadAlarmMsg);
                Log.i(TAG, "initView jsonString: " + jsonString);
                uploadAlarmMsg(jsonString);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
//        Log.i(TAG, "initView  insert: " + insert);

        if (!Constants.isFingerConnect) {
            ToastUtil.showShort("指纹未连接");
        }

        if (!Constants.isFingerInit) {
//            showDialog("指纹未初始化");
            FingerManager.getInstance().init(mContext);
        }

        //获取人员数据
        if (Utils.isNetworkAvailable()) {
            getUserList();
            getCharList();
        } else {
            userList = userBeanDao.loadAll();
            userBiosBeanList = userBiosBeanDao.loadAll();
        }
    }

    private void uploadAlarmMsg(String json) {
        HttpClient.getInstance().uploadAlarmMessage(mContext, json, new HttpListener<String>() {
            @Override
            public void onSucceed(int what, Response<String> response) throws JSONException {

            }

            @Override
            public void onFailed(int what, Response<String> response) {

            }
        });
    }

    @SuppressLint("HandlerLeak")
    private Handler mHandle = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            String txtMsg = (String) msg.obj;
            if (dlAlarmTxtMsg != null && !TextUtils.isEmpty(txtMsg)) {
                Log.i(TAG, "handleMessage msg: " + txtMsg);
                if (dlAlarmTxtMsg != null) {
                    dlAlarmTxtMsg.setText(txtMsg);
                }
            }
        }
    };

    @Override
    public void dismiss() {
        super.dismiss();
        Log.i(TAG, "alarm dialog dismiss: ");
//        try {
//            bind.unbind();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        SerialPortUtil.getInstance().closeAlertor(Integer.parseInt(SharedUtils.getLeftCabNo()));
        SoundPlayUtil.getInstance().stop(streamId);
    }

    @Override
    public void didVerify(int id, boolean success) {
        Log.i(TAG, "didVerify  id: " + id + " success: " + success);
        try {
            if (success) {
                verifyRelieve(id); //验证解除报警
            } else {
                streamId = SoundPlayUtil.getInstance().play(R.raw.verifyfailed);
                sendMsg("验证失败，请重试！");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void timeout() {
        //读取指纹超时 重复读取数据
        FingerManager.getInstance().searchfp(dlAlarmTxtMsg, this);
    }

    private Dialog dialog;

    protected void showDialog(String msg) {
        if (!((Activity) mContext).isFinishing()) {
            if (dialog != null) {
                Log.i(TAG, "showDialog is not null and is not isShowing: ");
                DialogUtils.setTipText(msg);
                if (!dialog.isShowing()) {
                    dialog.show();
                }
            } else {
                dialog = DialogUtils.creatTipDialog(mContext, "提示", msg, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        SharedUtils.saveBackupOpenStatus(1);
                        dialog.dismiss();
                        if (alarmLogBean != null) {
                            alarmLogBeanDao.update(alarmLogBean);
                        }
                        AlarmDialog.this.dismiss();
                    }
                });
                if (!dialog.isShowing()) {
                    dialog.show();
                }
            }
        }
    }

    private void verifyRelieve(int id) {
        UserBean curPolicce = verifyIdentity(id);
        if (curPolicce != null) {
            //获取到人员数据 只有枪管员或者领导才有权限解除报警
            int userId = curPolicce.getUserId();
            String userName = curPolicce.getUserName();
            Log.i(TAG, "verifyRelieve  userId: " + userId + " userName: " + userName);
            sendMsg("验证成功， 当前警员：" + userName);
            if (Utils.isNetworkAvailable()) {
                String roleKeys = curPolicce.getRoleKeys();
                if (!TextUtils.isEmpty(roleKeys)) {
                    String[] split = roleKeys.split(",");
                    List<String> roleList = Arrays.asList(split);
                    if (roleList != null && !roleList.isEmpty()) { //当前用户有没有权限
                        if (roleList.contains(Constants.ROLE_ROOM_ADMIN) || //库室管理员
                                roleList.contains(Constants.ROLE_APPROVER) || //审批人员
                                roleList.contains(Constants.ROLE_MANAGER) ||
                                roleList.contains(Constants.ROLE_ADMIN)) {  //枪管人员
                            verifySuccessAndRelieve(curPolicce, userId, userName);
                        } else {
                            //没有相关权限 权限不足
                            sendMsg("当前警员权限不足！无法解除报警");
                            verifyRetryFinger();
                        }
                    } else {
                        //没有设置权限
                        sendMsg("当前警员未设置任何权限！");
                        verifyRetryFinger();
                    }
                }
            } else {
                verifySuccessAndRelieve(curPolicce, userId, userName);
            }
        } else {
            Log.i(TAG, "didVerify 身份识别失败: ");
//            streamId = SoundPlayUtil.getInstance().play(R.raw.usernotexist);
            sendMsg("用户不存在");
            verifyRetryFinger();
        }
    }

    private void verifySuccessAndRelieve(UserBean curPolicce, int userId, String userName) {
        //验证成功 ，报警解除
        streamId = SoundPlayUtil.getInstance().play(R.raw.alarm_relieved);
//                    Sensor.getInstance().alarmSwitch(0);
        SerialPortUtil.getInstance().closeAlertor(Integer.parseInt(SharedUtils.getLeftCabNo()));
        //保存报警日志
        List<AlarmLogBean> alarmLogBeanList = new ArrayList<>();
//                    AlarmLogBean alarmLogBean = alarmLogBeanDao.queryBuilder()
//                            .where(AlarmLogBeanDao.Properties.Id.eq(insert)).unique();
        if (alarmLogBean != null) {
            alarmLogBean.setRelieveId(String.valueOf(userId)); //解除人员id
            alarmLogBean.setRelieveName(userName); //解除人员姓名
            alarmLogBean.setRelieveWarningTime(Utils.longTime2String(System.currentTimeMillis()));
            alarmLogBeanList.add(alarmLogBean);
            alarmLogBeanDao.update(alarmLogBean);
            String jsonString = JSON.toJSONString(alarmLogBeanList);
            Log.i(TAG, "verifyRelieve  jsonBody: " + jsonString);
            posAlarmLog(jsonString);

            DBManager.getInstance().insertCommLog(mContext, curPolicce, userName + "解除报警");
        }
    }

    /**
     * 提交报警日志数据
     *
     * @param jsonBody
     */
    private void posAlarmLog(String jsonBody) {
        HttpClient.getInstance().postAlarmLog(mContext, jsonBody, new HttpListener<String>() {
            @Override
            public void onSucceed(int what, Response<String> response) throws JSONException {
                Log.i(TAG, "onSucceed postAlarmLog response: " + response.get());
                try {
                    if (!TextUtils.isEmpty(response.get())) {
                        if (response.get().equals("success")) {
                            if (alarmLogBean != null) {
                                alarmLogBean.setIsSync(true);
                                alarmLogBeanDao.update(alarmLogBean);
                            }
                            dismiss();
                        } else {
                            showDialog("提交报警失败");
                        }
                    } else {
                        showDialog("提交报警失败");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailed(int what, Response<String> response) {
                showDialog("网络错误，提交数据失败");
            }
        });
    }

    /**
     * 根据指纹id获取警员信息
     *
     * @param id
     * @return
     */
    private UserBean verifyIdentity(int id) {
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

    private void verifyRetryFinger() {
        streamId = SoundPlayUtil.getInstance().play(R.raw.manager_leader_relieve_alarm);
        FingerManager.getInstance().fpsearch = false;
        FingerManager.getInstance().searchfp(dlAlarmTxtMsg, this);
    }

    private void sendMsg(String msg) {
        Message message = mHandle.obtainMessage();
        message.obj = msg;
        mHandle.sendMessage(message);
    }

    private boolean flag = false;

    @OnClick(R.id.dl_btn_relieve_alarm)
    public void onViewClicked() {
        //解除报警
        if (!flag) {
            flag = true;
            if (!Constants.isFingerConnect || !Constants.isFingerInit) {
                ToastUtil.showShort("指纹未连接或初始化异常！");
                dismiss();
            } else {
                if (!userList.isEmpty() && !userBiosBeanList.isEmpty()) {
                    verifyRetryFinger();
                } else {
                    ToastUtil.showShort("没有人员和生物特征数据");
                    dismiss();
                }
            }
        } else {
            SoundPlayUtil.getInstance().stop(streamId);
        }
    }

    private void getCharList() {
        HttpClient.getInstance().getCharList(mContext, "", new HttpListener<String>() {
            @Override
            public void onSucceed(int what, Response<String> response) throws JSONException {
                LogUtil.i(TAG, "onSucceed getCharList response: " + response.get());
                try {
                    if (!TextUtils.isEmpty(response.get())) {
                        userBiosBeanList = JSON.parseArray(response.get(), UserBiosBean.class);
                        if (userBiosBeanList.isEmpty()) {
                            showDialog("获取生物特征数据为空");
                        } else {
                            downCHar(userBiosBeanList);
                        }
                    } else {
                        showDialog("获取生物特征数据失败");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    showDialog("获取生物特征出错");
                }
            }

            @Override
            public void onFailed(int what, Response<String> response) {
                showDialog("网络出错！获取生物特征失败");
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
            verifyRetryFinger();
        } else {
            showDialog("无生物特征数据");
            Log.i(TAG, "downCHar:无生物特征数据 ! ");
        }
    }

    /**
     * 获取人员列表
     */
    private void getUserList() {
        HttpClient.getInstance().getUserList(mContext, "", new HttpListener<String>() {
            @Override
            public void onSucceed(int what, Response<String> response) throws JSONException {
                Log.i(TAG, "onSucceed getUserList response: " + response.get());
                try {
                    if (!TextUtils.isEmpty(response.get())) {
                        userList = JSON.parseArray(response.get(), UserBean.class);
                        if (userList.isEmpty()) {
                            showDialog("获取人员数据为空");
                        }
                    } else {
                        showDialog("获取数据为空");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    showDialog("获取数据出错");
                }
            }

            @Override
            public void onFailed(int what, Response<String> response) {
                showDialog("网络出错！获取人员数据失败");
            }
        });
    }

}
