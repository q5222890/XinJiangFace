package com.zack.xjht.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.yanzhenjie.nohttp.rest.Response;
import com.zack.xjht.Constants;
import com.zack.xjht.R;
import com.zack.xjht.Utils.BitmapUtils;
import com.zack.xjht.Utils.DialogUtils;
import com.zack.xjht.Utils.LogUtil;
import com.zack.xjht.Utils.SoundPlayUtil;
import com.zack.xjht.Utils.ToastUtil;
import com.zack.xjht.Utils.TransformUtil;
import com.zack.xjht.Utils.Utils;
import com.zack.xjht.db.DBManager;
import com.zack.xjht.db.gen.CommonLogBeanDao;
import com.zack.xjht.entity.UserBean;
import com.zack.xjht.entity.UserBiosBean;
import com.zack.xjht.event.EventConsts;
import com.zack.xjht.event.MessageEvent;
import com.zack.xjht.http.HttpClient;
import com.zack.xjht.http.HttpListener;
import com.zack.xjht.iris.IrisManager;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;


public class IrisDialog extends Dialog {
    private static final String TAG = "IrisDialog";

    @BindView(R.id.dl_iris_iv_status)
    ImageView dlIrisIvStatus;
    @BindView(R.id.dl_iris_tv_msg)
    TextView dlIrisTvMsg;
    @BindView(R.id.dl_iris_iv_close)
    ImageView dlIrisIvClose;
    private UserBean curPolice;
    private List<UserBiosBean> userBiosList;
    private Unbinder bind;
    private String bioId;
    private Context mContext;
    private boolean isRegistered;
    private int streamId;
    private CommonLogBeanDao commonLogBeanDao;
    private int biometricsNumber;
    private List<Integer> irisIdList = new ArrayList<>();

    public IrisDialog(@NonNull Context context, UserBean userBean) {
        super(context, R.style.dialog);
        this.mContext = context;
        this.curPolice = userBean;
        userBiosList = new ArrayList<>();
        initView();
    }

    private void initView() {
        setContentView(R.layout.dialog_iris);
        bind = ButterKnife.bind(this);
        setCancelable(false);
        setCanceledOnTouchOutside(false);

        EventBus.getDefault().register(this);

        commonLogBeanDao = DBManager.getInstance().getCommonLogBeanDao();
        //初始化虹膜特征
//        initData();
        getUserChar();
    }

    private void initData() {
        if (curPolice == null) {
            ToastUtil.showShort("人员信息为空！");
            return;
        }
        setImageByBioType(false);
        if (!userBiosList.isEmpty()) {
            for (int i = 0; i < userBiosList.size(); i++) {
                UserBiosBean policeBiosBean = userBiosList.get(i);
                String deviceType = policeBiosBean.getBiometricsType();
                int userId = policeBiosBean.getUserId();
                if (curPolice.getUserId() == userId && deviceType.equals(Constants.DEVICE_IRIS)) { //虹膜
                    bioId = policeBiosBean.getId();
                    biometricsNumber = policeBiosBean.getBiometricsNumber();
                    setImageByBioType(true);
                }
            }
        } else {
            setImageByBioType(false);
        }
    }

    private void setImageByBioType(boolean isRegister) {
        if (isRegister) {
            Log.i(TAG, "setImageByBioType : ");
            Bitmap bitmap = BitmapUtils.readBitMap(
                    mContext, R.drawable.ic_iris_manage);
            dlIrisIvStatus.setImageBitmap(bitmap);
            dlIrisTvMsg.setText("已注册");
            isRegistered = true;
        } else {
            Log.i(TAG, "clearImgFinger: ");
            Bitmap bitmap = BitmapUtils.readBitMap(
                    mContext, R.drawable.ic_iris_manage);
            dlIrisIvStatus.setImageBitmap(bitmap);
            dlIrisTvMsg.setText("未注册");
            isRegistered = false;
        }
    }

    private void getUserChar() {
        HttpClient.getInstance().getCharList(getContext(), "",
                new HttpListener<String>() {
                    @Override
                    public void onSucceed(int what, Response<String> response) throws JSONException {
                        LogUtil.i(TAG, "onSucceed getUserChar response: " + response.get());
                        try {
                            if (!TextUtils.isEmpty(response.get())) {
                                userBiosList = JSON.parseArray(response.get(), UserBiosBean.class);
                                if (!userBiosList.isEmpty()) {
                                    initData();
                                } else {
                                    ToastUtil.showShort("获取生物特征为空");
                                }
                            } else {
                                ToastUtil.showShort("获取生物特征失败");
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventSubscriber(MessageEvent messageEvent) {
        String message = messageEvent.getMessage();
//        Log.i(TAG, "onEventSubscriber message: " + message);
        switch (message) {
            case EventConsts.KEEP_CURRENT_STATUS:
                streamId = SoundPlayUtil.getInstance().play(R.raw.iris_enroll_keep_current_status);
                break;
            case EventConsts.ADJUST_DISTANCE:
                streamId = SoundPlayUtil.getInstance().play(R.raw.iris_enroll_adjust_distance);
                break;
            case EventConsts.WATCH_MIRROR:
                streamId = SoundPlayUtil.getInstance().play(R.raw.iris_enroll_watch_mirror);
                break;
            case EventConsts.CLOSE_TO:
                streamId = SoundPlayUtil.getInstance().play(R.raw.iris_enroll_please_close);
                break;
            case EventConsts.OPEN_EYES:
                streamId = SoundPlayUtil.getInstance().play(R.raw.iris_enroll_open_eyes);
                break;
            case EventConsts.DONT_LOOK_AWRY:
                streamId = SoundPlayUtil.getInstance().play(R.raw.iris_enroll_dont_look_awry);
                break;
        }
    }

    @Override
    public void dismiss() {
        super.dismiss();
        bind.unbind();

        IrisManager.getInstance().cancelAction();
        EventBus.getDefault().unregister(this);
        SoundPlayUtil.getInstance().stop(streamId);
    }

    public int getIrisId() {
        Log.i(TAG, "getFingerPrintId: ");
        if (!userBiosList.isEmpty()) {
            for (UserBiosBean userBiosBean : userBiosList) {
//                Log.i(TAG, "getFingerPrintId policeBiosBean: " + JSON.toJSONString(userBiosBean));
                String biometricsType = userBiosBean.getBiometricsType(); //设备类型
                if (!TextUtils.isEmpty(biometricsType) && biometricsType.equals(Constants.DEVICE_IRIS)) {
                    int fingerprintId = userBiosBean.getBiometricsNumber();
                    irisIdList.add(fingerprintId);
                }
//                Log.i(TAG, "getFingerId: " + fingerprintId);
            }
        }

        if (!irisIdList.isEmpty()) {
            int emptyId = Collections.min(Utils.compare(irisIdList, 1000)); //获取最大值
            Log.i(TAG, "initView emptyId: " + emptyId);
            return emptyId;
        }
        return 1;
    }

    @OnClick({R.id.dl_iris_iv_close, R.id.dl_iris_iv_status})
    public void onViewClicked(View v) {
        switch (v.getId()) {
            case R.id.dl_iris_iv_close:
                dismiss();
                break;
            case R.id.dl_iris_iv_status:
                //注册删除虹膜
                enrollOrDeleteIris();
                break;
        }
    }

    private void enrollOrDeleteIris() {
        dlIrisTvMsg.setText("");
        Log.i(TAG, "onViewClicked isRegistered: " + isRegistered);
        if (isRegistered) { //已注册
            Log.i(TAG, "onViewClicked 已注册 删除模板: " + bioId);
            if (!TextUtils.isEmpty(bioId)) {
                deleteIris(bioId);
            }
        } else { //未注册过
            Log.i(TAG, "onViewClicked 未注册 注册模板: ");
            int irisId = getIrisId();
            enrollIris(irisId);
        }
    }

    private void enrollIris(final int irisId) {
        IrisManager.getInstance().registerIris(String.valueOf(irisId), dlIrisTvMsg, new IrisManager.OnRegisteredReceiv() {
            @Override
            public void onResult(int result) {
                Log.i(TAG, "onResult : " + result);
                switch (result) {
                    case 0: //regist_success 上传服务器
                        streamId = SoundPlayUtil.getInstance().play(R.raw.enroll_ok);
                        IrisManager.getInstance().getTemp(String.valueOf(irisId), dlIrisTvMsg,
                                new IrisManager.OnTempReceiv() {
                                    @Override
                                    public void onTempReceiv(byte[] temp) {
                                        Log.i(TAG, "onTempReceiv: " + TransformUtil.toHexString(temp));
                                        uploadChar(temp, irisId);
                                    }
                                });
                        break;
                    case 1: //register_failure
                        streamId = SoundPlayUtil.getInstance().play(R.raw.enroll_failed);
                        break;
                    case 2: //不同id重复注册
                        streamId = SoundPlayUtil.getInstance().play(R.raw.iris_enroll_diff_duplicated);
                        break;
                    case 3://相同id重复注册
                        streamId = SoundPlayUtil.getInstance().play(R.raw.iris_enroll_same_duplicated);
                        break;
                }
            }
        });
    }

    private void uploadChar(byte[] enrollBuf, final int irisId) {
        Log.i(TAG, "uploadChar userId: " + irisId);
        String key = TransformUtil.toHexString(enrollBuf);
        Log.i(TAG, "onEnrollStatus key: " + key);
        UserBiosBean postPoliceBio = new UserBiosBean();
//            String encode = Base64.encodeToString(fpChar, Base64.DEFAULT); //特征转为base64编码格式
//            String key = new String(encode, 0, encode.length); //将字节流转为String字符串
//            Log.i(TAG, "onEnrollStatus encode: " + encode);

        postPoliceBio.setBiometricsNumber(irisId);
        postPoliceBio.setBiometricsPart("1");
        postPoliceBio.setUserId(curPolice.getUserId()); //警员id
        postPoliceBio.setUserName(curPolice.getUserName());
        postPoliceBio.setBiometricsType(Constants.DEVICE_IRIS);  //指纹类型
//        postPoliceBio.setId(faceID);
        postPoliceBio.setBiometricsKey(key);
        String jsonBody = JSON.toJSONString(postPoliceBio, SerializerFeature.WriteMapNullValue);
        LogUtil.i(TAG, "onEnrollStatus jsonBody: " + jsonBody);

        HttpClient.getInstance().postUserChar(mContext, jsonBody, new HttpListener<String>() {
            @Override
            public void onSucceed(int what, Response<String> response) throws JSONException {
                Log.i(TAG, "onSucceed postUserChar response: " + response.get());
                try {
                    if (!TextUtils.isEmpty(response.get())) {
                        if (response.get().equals("success")) {
//                            CommonLogBean commonLogBean = new CommonLogBean();
//                            commonLogBean.setAddTime(System.currentTimeMillis());
//                            commonLogBean.setUserId(String.valueOf(curPolice.getUserId()));
//                            commonLogBean.setUserName(curPolice.getUserName());
//                            commonLogBean.setContent(curPolice.getUserName() + "注册虹膜特征");
//                            long insert = commonLogBeanDao.insert(commonLogBean);
                            DBManager.getInstance().insertCommLog(mContext, curPolice,
                                    curPolice.getUserName() + "注册虹膜特征");
                            showDialogAndDismiss("注册虹膜成功");
                        } else {
                            showDialogAndDismiss("注册虹膜失败");
                        }
                    } else {
                        showDialogAndDismiss("注册虹膜失败");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    showDialogAndDismiss("出现错误！注册虹膜失败！");
                }
            }

            @Override
            public void onFailed(int what, Response<String> response) {
                Log.i(TAG, "onFailed error: " + response.getException().getMessage());
                showDialogAndDismiss("糟糕！网络错误，注册虹膜失败。");
            }
        });
    }

    private Dialog choiceDialog;

    /**
     * 删除虹膜
     *
     * @param bioId 生物特征id
     */
    private void deleteIris(final String bioId) {
        choiceDialog = DialogUtils.createChoiceDialog(mContext,
                "确定要删除这条虹膜数据吗？", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        deleteBioById(bioId);
                    }
                });
        choiceDialog.show();
    }

    private void deleteBioById(final String bioId) {
        HttpClient.getInstance().deleteUserChar(getContext(), bioId, new HttpListener<String>() {
            @Override
            public void onSucceed(int what, Response<String> response) throws JSONException {
                try {
                    Log.i(TAG, "deleteFingerBios onSucceed response: " + response.get());
                    if (!TextUtils.isEmpty(response.get())) {
                        if (response.get().equals("success")) {
                            Log.i(TAG, "onSucceed 删除虹膜成功！: ");
                            //删除数据库数据 把模块内相应模版数据删除
                            IrisManager.getInstance().deleteTempByID(String.valueOf(biometricsNumber), dlIrisTvMsg);
//                            CommonLogBean commonLogBean = new CommonLogBean();
//                            commonLogBean.setAddTime(System.currentTimeMillis());
//                            commonLogBean.setUserId(String.valueOf(curPolice.getUserId()));
//                            commonLogBean.setUserName(curPolice.getUserName());
//                            commonLogBean.setContent(curPolice.getUserName() + "删除虹膜特征");
//                            long insert = commonLogBeanDao.insert(commonLogBean);
                            DBManager.getInstance().insertCommLog(mContext, curPolice,
                                    curPolice.getUserName() + "删除虹膜特征");
                            showDialogAndDismiss("删除特征成功！");
                        } else {
                            Log.i(TAG, "onSucceed 虹膜删除失败！: ");
                            showDialogAndDismiss("删除特征失败！");
                        }
                    } else {
                        Log.i(TAG, "onSucceed 特征删除失败！: ");
                        showDialogAndDismiss("删除特征失败！");
                    }
                    choiceDialog.dismiss();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailed(int what, Response<String> response) {
                Log.i(TAG, "deleteFingerBios onFailed error: " + response.getException().getMessage());
                choiceDialog.dismiss();
                showDialogAndDismiss("删除虹膜失败！");
            }
        });
    }

    private Dialog tipDialog;

    private void showDialogAndDismiss(String msg) {
        if (tipDialog == null) {
            tipDialog = DialogUtils.creatTipDialog(mContext, "提示", msg, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    tipDialog.dismiss();
                    IrisDialog.this.dismiss();
                }
            });
            if (!tipDialog.isShowing()) {
                tipDialog.show();
            }
        } else {
            if (!tipDialog.isShowing()) {
                tipDialog.show();
            }
        }
    }

}
