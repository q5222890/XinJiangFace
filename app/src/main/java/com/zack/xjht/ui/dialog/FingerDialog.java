package com.zack.xjht.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.zack.xjht.finger.FingerManager;
import com.zack.xjht.http.HttpClient;
import com.zack.xjht.http.HttpListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class FingerDialog extends Dialog implements FingerManager.IEnrollStatus {
    private static final String TAG = "FingerDialog";

    @BindView(R.id.left_little_finger)
    ImageView leftLittleFinger;
    @BindView(R.id.left_ring_finger)
    ImageView leftRingFinger;
    @BindView(R.id.left_middle_finger)
    ImageView leftMiddleFinger;
    @BindView(R.id.left_index_finger)
    ImageView leftIndexFinger;
    @BindView(R.id.left_thumb)
    ImageView leftThumb;
    @BindView(R.id.right_thumb)
    ImageView rightThumb;
    @BindView(R.id.right_index_finger)
    ImageView rightIndexFinger;
    @BindView(R.id.right_middle_finger)
    ImageView rightMiddleFinger;
    @BindView(R.id.right_ring_finger)
    ImageView rightRingFinger;
    @BindView(R.id.right_little_finger)
    ImageView rightLittleFinger;
    @BindView(R.id.ll_finger)
    LinearLayout llFinger;
    @BindView(R.id.finger_tv_msg)
    TextView fingerTvMsg;
    @BindView(R.id.dl_iv_close)
    ImageView dlIvClose;
    private UserBean curPolice;
    private Map<String, String> bioList = new HashMap<>();
    private List<Integer> fingerIdList = new ArrayList<>();
    private Bitmap bitmap;
    private List<UserBiosBean> userBiosList = new ArrayList<>();
    private String bioType;
    private Unbinder bind;
    private Context context;
    private Dialog choiceDialog;
    private int streamId;
    private int fingerPrintId;
    private CommonLogBeanDao commonLogBeanDao;

    public FingerDialog(@NonNull Context context, UserBean curPolice) {
        super(context, R.style.dialog);
        this.curPolice = curPolice;
        this.context = context;
        initView();
    }

    private void initView() {
        setContentView(R.layout.hands);
        bind = ButterKnife.bind(this);
        setCancelable(false);
        setCanceledOnTouchOutside(false);
        getUserChar();
        commonLogBeanDao = DBManager.getInstance().getCommonLogBeanDao();
    }

    public void initData() {
        if(curPolice ==null){
            ToastUtil.showShort("用户信息获取失败！");
            return;
        }
        if (userBiosList != null && !userBiosList.isEmpty()) {
            clearImgFinger(); //重置
            bioList.clear();
            for (UserBiosBean userBiosBean : userBiosList) {
                String deviceType = userBiosBean.getBiometricsType();
                int userId = userBiosBean.getUserId();
                if (curPolice.getUserId() ==userId && deviceType.equals(Constants.DEVICE_FINGER)) {
                    String bioType = userBiosBean.getBiometricsPart();
                    String bioId = userBiosBean.getId();
                    bioList.put(bioType, bioId);
                    setImageByBioType(bioType);
                }
            }
        } else { //没有指纹数据则在切换时重置
            Log.i(TAG, "initData police bios is null: ");
            bioList.clear();
            clearImgFinger();
        }
    }

    private void getUserChar() {
        HttpClient.getInstance().getCharList(context, "",
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

    /**
     * 重置
     */
    private void clearImgFinger() {
        leftThumb.setImageResource(R.drawable.left_thumb_green);
        leftIndexFinger.setImageResource(R.drawable.left_index_finger_green);
        leftMiddleFinger.setImageResource(R.drawable.left_middle_finger_green);
        leftRingFinger.setImageResource(R.drawable.left_ring_finger_green);
        leftLittleFinger.setImageResource(R.drawable.left_little_finger_green);
        rightThumb.setImageResource(R.drawable.right_thumb_green);
        rightIndexFinger.setImageResource(R.drawable.right_index_finger_green);
        rightMiddleFinger.setImageResource(R.drawable.right_middle_finger_green);
        rightRingFinger.setImageResource(R.drawable.right_ring_finger_green);
        rightLittleFinger.setImageResource(R.drawable.right_little_finger_green);
    }

    /**
     * 根据bioType设置图片
     */
    public void setImageByBioType(String bioType) {
        switch (bioType) {
            case "1":
                bitmap = BitmapUtils.readBitMap(
                        context, R.drawable.left_thumb_yellow);
                leftThumb.setImageBitmap(bitmap);
                break;
            case "2":
                bitmap = BitmapUtils.readBitMap(
                        context, R.drawable.left_index_finger_yellow);
                leftIndexFinger.setImageBitmap(bitmap);
                break;
            case "3":
                bitmap = BitmapUtils.readBitMap(
                        context, R.drawable.left_middle_finger_yellow);
                leftMiddleFinger.setImageBitmap(bitmap);
                break;
            case "4":
                bitmap = BitmapUtils.readBitMap(
                        context, R.drawable.left_ring_finger_yellow);
                leftRingFinger.setImageBitmap(bitmap);
                break;
            case "5":
                bitmap = BitmapUtils.readBitMap(
                        context, R.drawable.left_little_finger_yellow);
                leftLittleFinger.setImageBitmap(bitmap);
                break;
            case "6":
                bitmap = BitmapUtils.readBitMap(
                        context, R.drawable.right_thumb_yellow);
                rightThumb.setImageBitmap(bitmap);
                break;
            case "7":
                bitmap = BitmapUtils.readBitMap(
                        context, R.drawable.right_index_finger_yellow);
                rightIndexFinger.setImageBitmap(bitmap);
                break;
            case "8":
                bitmap = BitmapUtils.readBitMap(
                        context, R.drawable.right_middle_finger_yellow);
                rightMiddleFinger.setImageBitmap(bitmap);
                break;
            case "9":
                bitmap = BitmapUtils.readBitMap(
                        context, R.drawable.right_ring_finger_yellow);
                rightRingFinger.setImageBitmap(bitmap);
                break;
            case "10":
                bitmap = BitmapUtils.readBitMap(
                        context, R.drawable.right_little_finger_yellow);
                rightLittleFinger.setImageBitmap(bitmap);
                break;
            default:
                break;
        }
    }

    @OnClick({R.id.left_little_finger, R.id.left_ring_finger, R.id.left_middle_finger,
            R.id.left_index_finger, R.id.left_thumb, R.id.right_thumb, R.id.right_index_finger,
            R.id.right_middle_finger, R.id.right_ring_finger, R.id.right_little_finger})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.left_little_finger:  //左手小指
                if (bioList.containsKey("5")) {
                    deleteFinger(bioList.get("5"));
                } else {
                    addFingerPrint("5");
                }
                break;
            case R.id.left_ring_finger:
                if (bioList.containsKey("4")) {
                    deleteFinger(bioList.get("4"));
//                    deleteFinger("4");
                } else {
                    addFingerPrint("4");
                }
                break;
            case R.id.left_middle_finger:
                if (bioList.containsKey("3")) {
                    deleteFinger(bioList.get("3"));
//                    deleteFinger("3");
                } else {
                    addFingerPrint("3");
                }
                break;
            case R.id.left_index_finger:
                if (bioList.containsKey("2")) {
                    deleteFinger(bioList.get("2"));
//                    deleteFinger("2");
                } else {
                    addFingerPrint("2");
                }
                break;
            case R.id.left_thumb:
                if (bioList.containsKey("1")) {
//                    deleteFinger("1");
                    deleteFinger(bioList.get("1"));
                } else {
                    addFingerPrint("1");
                }
                break;
            case R.id.right_thumb:
                if (bioList.containsKey("6")) {
                    deleteFinger(bioList.get("6"));
//                    deleteFinger("6");
                } else {
                    addFingerPrint("6");
                }
                break;
            case R.id.right_index_finger:
                if (bioList.containsKey("7")) {
                    deleteFinger(bioList.get("7"));
//                    deleteFinger("7");
                } else {
                    addFingerPrint("7");
                }
                break;
            case R.id.right_middle_finger:
                if (bioList.containsKey("8")) {
                    deleteFinger(bioList.get("8"));
//                    deleteFinger("8");
                } else {
                    addFingerPrint("8");
                }
                break;
            case R.id.right_ring_finger:
                if (bioList.containsKey("9")) {
                    deleteFinger(bioList.get("9"));
//                    deleteFinger("9");
                } else {
                    addFingerPrint("9");
                }
                break;
            case R.id.right_little_finger:
                if (bioList.containsKey("10")) {
                    deleteFinger(bioList.get("10"));
//                    deleteFinger("10");
                } else {
                    addFingerPrint("10");
                }
                break;
        }
    }

    private void addFingerPrint(String bioType) {
        this.bioType = bioType;
        enroll(getFingerPrintId());
    }

    public int getFingerPrintId() {
        Log.i(TAG, "getFingerPrintId: ");
        if ( !userBiosList.isEmpty()) {
//            Log.i(TAG, "getFingerPrintId policeBiosBean: " + JSON.toJSONString(userBiosList));
            for (UserBiosBean userBiosBean :userBiosList) {
                String biometricsType = userBiosBean.getBiometricsType(); //设备类型
                if(!TextUtils.isEmpty(biometricsType) && biometricsType.equals(Constants.DEVICE_FINGER)){
                    int fingerprintId = userBiosBean.getBiometricsNumber();
                    Log.i(TAG, "getFingerPrintId fingerprintId: "+fingerprintId);
                    fingerIdList.add(fingerprintId);
                }
//                Log.i(TAG, "getFingerId: " + fingerprintId);
            }
            StringBuilder sb =new StringBuilder();
            for (int i = 0; i < fingerIdList.size(); i++) {
                sb.append(fingerIdList.get(i) +",");
            }
            Log.i(TAG, "getFingerPrintId list id: "+sb.toString());
        }

//        Log.i(TAG, "getFingerPrintId  fingerList: "+ fingerIdList.toString());
        if (!fingerIdList.isEmpty()) {
            int emptyId = Collections.min(Utils.compare(fingerIdList, 1000)); //获取最大值
            Log.i(TAG, "getFingerPrintId min: " + emptyId);
            return emptyId;
        }
        return 1;
    }

    private void enroll(int id) {
        this.fingerPrintId =id;
        FingerManager.getInstance().fperoll = false;
        FingerManager.getInstance().erollfp(fingerTvMsg, id, this);
    }

    /**
     * 删除指纹
     */
    private void deleteFinger(final String bioType) {
        Log.i(TAG, "deleteFinger: " + bioType);
        choiceDialog = DialogUtils.createChoiceDialog(context,
                "确定要删除这条指纹数据吗？", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //删除指纹
                        Log.i(TAG, "onClick 删除指纹。。。 ");
                        deleteFingerBios(bioType);
                    }
                });
        choiceDialog.show();
    }

    /**
     * 删除指纹
     */
    private void deleteFingerBios(String bioId) {
        HttpClient.getInstance().deleteUserChar(context, bioId, new HttpListener<String>() {
            @Override
            public void onSucceed(int what, Response<String> response) throws JSONException {
                Log.i(TAG, "deleteFingerBios onSucceed response: " + response.get());
                try {
                    if (!TextUtils.isEmpty(response.get())) {
                        if (response.get().equals("success")) {
                            Log.i(TAG, "onSucceed 指纹删除成功！: ");

                            DBManager.getInstance().insertCommLog(context, curPolice,
                                    curPolice.getUserName() + "删除指纹");
                            showDialogAndFinish("指纹删除成功！");
                        } else {
                            Log.i(TAG, "onSucceed 指纹删除失败！: ");
                            showDialogAndFinish("指纹删除失败！");
                        }
                    } else {
                        Log.i(TAG, "onSucceed 指纹删除失败！: ");
                        showDialogAndFinish("指纹删除失败！");
                    }
                    choiceDialog.dismiss();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailed(int what, Response<String> response) {
                Log.i(TAG, "deleteFingerBios onFailed error: " + response.getException().getMessage());
                showDialogAndFinish("指纹删除失败！");
                choiceDialog.dismiss();
            }
        });
    }

    @Override
    public void onEnrollStatus(int id, byte[] fpChar, boolean success) {
        if (success) {//注册指纹成功 上传指纹特征数据
            Log.i(TAG, "didVerify id: " + id);
            String key = TransformUtil.toHexString(fpChar);
            Log.i(TAG, "onEnrollStatus key1: " + key);
            UserBiosBean postPoliceBio = new UserBiosBean();
//            String encode = Base64.encodeToString(fpChar, Base64.DEFAULT); //特征转为base64编码格式
//            String key = new String(encode, 0, encode.length); //将字节流转为String字符串
//            Log.i(TAG, "onEnrollStatus encode: " + encode);

            postPoliceBio.setBiometricsNumber(fingerPrintId);
            postPoliceBio.setBiometricsPart(bioType);
            postPoliceBio.setUserId(curPolice.getUserId()); //警员id
            postPoliceBio.setUserName(curPolice.getUserName());
            postPoliceBio.setBiometricsType(Constants.DEVICE_FINGER);  //指纹类型
//            postPoliceBio.setId(String.valueOf(id));
            postPoliceBio.setBiometricsKey(key);
            String jsonBody = JSON.toJSONString(postPoliceBio, SerializerFeature.WriteMapNullValue);
            Log.i(TAG, "onEnrollStatus jsonBody: " + jsonBody);

            HttpClient.getInstance().postUserChar(context, jsonBody, new HttpListener<String>() {
                @Override
                public void onSucceed(int what, Response<String> response) throws JSONException {
                    Log.i(TAG, "onSucceed postUserChar response: " + response.get());
                    try {
                        if (!TextUtils.isEmpty(response.get())) {
                            if (response.get().equals("success")) {
                                DBManager.getInstance().insertCommLog(context, curPolice,
                                        curPolice.getUserName() + "注册指纹");
                                showDialogAndFinish("指纹注册成功");
                            } else {
                                showDialogAndFinish("指纹注册失败");
                            }
                        } else {
                            showDialogAndFinish("指纹注册失败");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailed(int what, Response<String> response) {
                    Log.i(TAG, "onFailed error: " + response.getException().getMessage());
                    showDialogAndFinish("指纹注册失败");
                }
            });
        }
    }

    @Override
    public void dismiss() {
        super.dismiss();
        bind.unbind();
        FingerManager.getInstance().fperoll = true;
        SoundPlayUtil.getInstance().stop(streamId);
    }

    Dialog tipDialog;
    private void showDialog(String msg) {
        tipDialog = DialogUtils.creatTipDialog(context, "提示", msg, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tipDialog.dismiss();
            }
        });
        tipDialog.show();
    }

    private void showDialogAndFinish(String msg) {
        tipDialog = DialogUtils.creatTipDialog(context, "提示", msg, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tipDialog.dismiss();
                FingerDialog.this.dismiss();
            }
        });
        tipDialog.show();
    }

    @OnClick(R.id.dl_iv_close)
    public void onViewClicked() {
        dismiss();
    }
}
