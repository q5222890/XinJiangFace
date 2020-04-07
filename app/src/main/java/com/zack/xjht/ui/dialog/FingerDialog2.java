package com.zack.xjht.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.zack.xjht.Utils.SoundPlayUtil;
import com.zack.xjht.Utils.ToastUtil;
import com.zack.xjht.Utils.TransformUtil;
import com.zack.xjht.entity.UserBiosBean;
import com.zack.xjht.finger.FingerManager;
import com.zack.xjht.http.HttpClient;
import com.zack.xjht.http.HttpListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * 指纹管理窗体  注册和删除
 */
public class FingerDialog2 extends DialogFragment implements FingerManager.IEnrollStatus {
    private static final String TAG = "FingerDialog2";
    @BindView(R.id.dl_iv_close)
    ImageView dlIvClose;
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
    Unbinder unbinder;

    private Map<String, String> bioList = new HashMap<>();
    private List<Integer> fingerIdList = new ArrayList<>();
    private Bitmap bitmap;
    private List<UserBiosBean> userBiosList = new ArrayList<>();
    private String bioType;
    private Unbinder bind;
    private Context mContext;
    private Dialog choiceDialog;
    private int streamId;
    private Dialog tipDialog;
    private int userId;
    private String fingerPrintId;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.hands, container, false);
        setStyle(R.style.dialog, R.style.AppTheme);

        unbinder = ButterKnife.bind(this, view);
        fingerTvMsg.setTextColor(0XFFFFFF);
        Bundle arguments = getArguments();
        int userId = getActivity().getIntent().getIntExtra("userId", 0);
        Log.i(TAG, "onCreateView  userId===== : " + userId);
        if (arguments != null) {
            this.userId = arguments.getInt("userId");
            Log.i(TAG, "onCreateView  userId: " + this.userId);
        }
        setCancelable(false);
        getUserChar();
        return view;
    }

    private void getUserChar() {
        HttpClient.getInstance().getCharList(mContext, String.valueOf(userId),
                new HttpListener<String>() {
                    @Override
                    public void onSucceed(int what, Response<String> response) throws JSONException {
                        Log.i(TAG, "onSucceed getUserChar response: " + response.get());
                        try {
                            if (!TextUtils.isEmpty(response.get())) {
                                List<UserBiosBean> userBiosBeans = JSON.parseArray(response.get(), UserBiosBean.class);
                                if (!userBiosBeans.isEmpty()) {
                                    userBiosList.clear();
                                    userBiosList.addAll(userBiosBeans);
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

    public void initData() {
        if (userBiosList != null && !userBiosList.isEmpty()) {
            clearImgFinger(); //重置
            bioList.clear();
            for (UserBiosBean userBiosBean : userBiosList) {
                String deviceType = userBiosBean.getBiometricsType();
                if (deviceType.equals(Constants.DEVICE_FINGER)) {
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
                        mContext, R.drawable.left_thumb_yellow);
                leftThumb.setImageBitmap(bitmap);
                break;
            case "2":
                bitmap = BitmapUtils.readBitMap(
                        mContext, R.drawable.left_index_finger_yellow);
                leftIndexFinger.setImageBitmap(bitmap);
                break;
            case "3":
                bitmap = BitmapUtils.readBitMap(
                        mContext, R.drawable.left_middle_finger_yellow);
                leftMiddleFinger.setImageBitmap(bitmap);
                break;
            case "4":
                bitmap = BitmapUtils.readBitMap(
                        mContext, R.drawable.left_ring_finger_yellow);
                leftRingFinger.setImageBitmap(bitmap);
                break;
            case "5":
                bitmap = BitmapUtils.readBitMap(
                        mContext, R.drawable.left_little_finger_yellow);
                leftLittleFinger.setImageBitmap(bitmap);
                break;
            case "6":
                bitmap = BitmapUtils.readBitMap(
                        mContext, R.drawable.right_thumb_yellow);
                rightThumb.setImageBitmap(bitmap);
                break;
            case "7":
                bitmap = BitmapUtils.readBitMap(
                        mContext, R.drawable.right_index_finger_yellow);
                rightIndexFinger.setImageBitmap(bitmap);
                break;
            case "8":
                bitmap = BitmapUtils.readBitMap(
                        mContext, R.drawable.right_middle_finger_yellow);
                rightMiddleFinger.setImageBitmap(bitmap);
                break;
            case "9":
                bitmap = BitmapUtils.readBitMap(
                        mContext, R.drawable.right_ring_finger_yellow);
                rightRingFinger.setImageBitmap(bitmap);
                break;
            case "10":
                bitmap = BitmapUtils.readBitMap(
                        mContext, R.drawable.right_little_finger_yellow);
                rightLittleFinger.setImageBitmap(bitmap);
                break;
            default:
                break;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @OnClick({R.id.dl_iv_close, R.id.left_little_finger, R.id.left_ring_finger, R.id.left_middle_finger, R.id.left_index_finger, R.id.left_thumb, R.id.right_thumb, R.id.right_index_finger, R.id.right_middle_finger, R.id.right_ring_finger, R.id.right_little_finger})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.dl_iv_close:
                FingerDialog2.this.dismiss();
                break;
            case R.id.left_little_finger:  //左手小指
                if (bioList.containsKey("5")) {
                    deleteFinger(bioList.get("5"));
//                    deleteFinger("5");
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
                Log.i(TAG, "onViewClicked delete left_thumb: ");
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
                } else {
                    addFingerPrint("10");
                }
                break;
        }
    }

    private void addFingerPrint(String bioType) {
        this.bioType = bioType;
        getFingerPrintId();
    }

    private void getFingerPrintId() {
//        HttpClient.getInstance().getCharId(mContext, new HttpListener<String>() {
//            @Override
//            public void onSucceed(int what, Response<String> response) throws JSONException {
//                Log.i(TAG, "onSucceed getFingerPrintId response : " + response.get());
//                try {
//                    if (!TextUtils.isEmpty(response.get())) {
//                        String id = response.get();
//                        enroll(id);
//                    } else {
//                        showDialogAndFinish("获取特征id失败！");
//                    }
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//
//            }
//
//
//            @Override
//            public void onFailed(int what, Response<String> response) {
//
//            }
//        });
    }

    private void enroll(String id) {
        this.fingerPrintId =id;
        FingerManager.getInstance().fperoll = false;
        FingerManager.getInstance().erollfp(fingerTvMsg, Integer.parseInt(id), this);
    }

    /**
     * 删除指纹
     */
    private void deleteFinger(final String bioType) {
        Log.i(TAG, "deleteFinger: " + bioType);
        choiceDialog = DialogUtils.createChoiceDialog(mContext,
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
     *
     */
    private void deleteFingerBios(String bioId) {
        HttpClient.getInstance().deleteUserChar(mContext, bioId, new HttpListener<String>() {
            @Override
            public void onSucceed(int what, Response<String> response) throws JSONException {
                Log.i(TAG, "deleteFingerBios onSucceed response: " + response.get());
                try {
                    if (!TextUtils.isEmpty(response.get())) {
                        if (response.get().equals("success")) {
                            Log.i(TAG, "onSucceed 指纹删除成功！: ");
                            showDialogAndFinish("delete_success！");
                        } else {
                            Log.i(TAG, "onSucceed 指纹删除失败！: ");
                            showDialogAndFinish("delete_failure！");
                        }
                    } else {
                        Log.i(TAG, "onSucceed 指纹删除失败！: ");
                        showDialogAndFinish("delete_failure！");
                    }
                    choiceDialog.dismiss();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailed(int what, Response<String> response) {
                Log.i(TAG, "deleteFingerBios onFailed error: " +
                        response.getException().getMessage());
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
            postPoliceBio.setBiometricsNumber(Integer.parseInt(fingerPrintId));
            postPoliceBio.setBiometricsPart(bioType);
            postPoliceBio.setUserId(userId); //警员id
            postPoliceBio.setBiometricsType(Constants.DEVICE_FINGER);  //指纹类型
            postPoliceBio.setId(String.valueOf(id));
            postPoliceBio.setBiometricsKey(key);
            String jsonBody = JSON.toJSONString(postPoliceBio, SerializerFeature.WriteMapNullValue);
            Log.i(TAG, "onEnrollStatus jsonBody: " + jsonBody);

            HttpClient.getInstance().postUserChar(mContext, jsonBody, new HttpListener<String>() {
                @Override
                public void onSucceed(int what, Response<String> response) throws JSONException {
                    Log.i(TAG, "onSucceed postUserChar response: " + response.get());
                    try {
                        if (!TextUtils.isEmpty(response.get())) {
                            if (response.get().equals("success")) {
                                showDialogAndFinish("regist_success");
                            } else {
                                showDialogAndFinish("register_failure");
                            }
                        } else {
                            showDialogAndFinish("register_failure");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailed(int what, Response<String> response) {
                    Log.i(TAG, "onFailed error: " + response.getException().getMessage());
                }
            });
        }
    }

    @Override
    public void dismiss() {
        super.dismiss();
        if (bind != null) {
            bind.unbind();
        }
        FingerManager.getInstance().fperoll = true;
        SoundPlayUtil.getInstance().stop(streamId);
    }

    private void showDialogAndFinish(String msg) {
        tipDialog = DialogUtils.creatTipDialog(mContext, "提示", msg, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tipDialog.dismiss();
                FingerDialog2.this.dismiss();
            }
        });
        tipDialog.show();
    }
}
