package com.zack.xjht.ui.fragment;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.zack.xjht.Constants;
import com.zack.xjht.R;
import com.zack.xjht.Utils.SoundPlayUtil;
import com.zack.xjht.entity.UserBean;
import com.zack.xjht.ui.BackActivity;
import com.zack.xjht.ui.GetActivity;
import com.zack.xjht.ui.InStoreActivity;
import com.zack.xjht.ui.KeepActivity;
import com.zack.xjht.ui.LoginActivity;
import com.zack.xjht.ui.ScrapActivity;
import com.zack.xjht.ui.TempStoreActivity;
import com.zack.xjht.ui.UrgentGoActivity;
import com.zack.xjht.ui.UserActivity;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * 人脸识别验证
 */
public class VerifyFaceFragment extends Fragment  {
    private static final String TAG = "VerifyFaceFragment";

    @BindView(R.id.verify_face_surface_view)
    SurfaceView verifyFaceSurfaceView;
    @BindView(R.id.verify_face_tv_msg)
    TextView verifyFaceTvMsg;
    Unbinder unbinder;

    private List<UserBean> userBeanList;
    private String target;
    private Class<?> toClass;
    private boolean isDutyManager = false;
    private boolean isDutyLeader = false;
    private int streamId;

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            String txtMsg = (String) msg.obj;
            if (verifyFaceTvMsg != null && !TextUtils.isEmpty(txtMsg)) {
                Log.i(TAG, "handleMessage msg: " + txtMsg);
                verifyFaceTvMsg.setText(txtMsg);
            }
        }
    };
    private LoginActivity login;

    public VerifyFaceFragment() {
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        login = (LoginActivity) context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_verify_face, container, false);
        unbinder = ButterKnife.bind(this, view);
        initView();
        return view;
    }

    private void initView() {
        target = getActivity().getIntent().getStringExtra("activity");
        Log.i(TAG, "onCreateView activity: " + target);
        if (!TextUtils.isEmpty(target)) {
            if (Constants.isFirstVerify) {
                //第一次验证
                if (target.equals(Constants.ACTIVITY_USER) ||
                        target.equals(Constants.ACTIVITY_SETTING)) {
//                    streamId = SoundPlayUtil.getInstance().play(R.raw.admin_verify_face);
                    verifyFaceTvMsg.setText("请系统管理员验证人脸");
                } else {
//                    streamId = SoundPlayUtil.getInstance().play(R.raw.duty_manager_face);
                    verifyFaceTvMsg.setText("请值班管理员验证人脸");
                }
            } else {
                //第二次验证
                if (target.equals(Constants.ACTIVITY_URGENT)) { //紧急领枪
                    //验证值班领导指纹
//                    streamId = SoundPlayUtil.getInstance().play(R.raw.duty_leader_face);
                    verifyFaceTvMsg.setText("请值班领导验证人脸");
                } else {
                    //验证值班管理员指纹
//                    streamId = SoundPlayUtil.getInstance().play(R.raw.duty_manager_face);
                    verifyFaceTvMsg.setText("请下一位值班管理员验证人脸");
                }
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
                case Constants.ACTIVITY_KEEP://保养枪弹
                    toClass = KeepActivity.class;
                    break;
                case Constants.ACTIVITY_SCRAP://报废枪弹
                    toClass = ScrapActivity.class;
                    break;
                case Constants.ACTIVITY_TEMP_IN://临时存放
                    toClass = TempStoreActivity.class;
                    break;
                case Constants.ACTIVITY_USER: //紧急出警
                    toClass = UserActivity.class;
                    break;
                case Constants.ACTIVITY_IN_STORE://值班管理
                    toClass = InStoreActivity.class;
                    break;

            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
        SoundPlayUtil.getInstance().stop(streamId);
    }


    /**
     * 第一次验证警员身份
     *
     * @param id
     */
    private void firstVerifyPolice(int id) {
        //根据id获取当前警员身份
        login.firstPolice = verifyIdentity(id);
        if (login.firstPolice != null) {
            int policeId = login.firstPolice.getUserId();
            String name = login.firstPolice.getUserName();
            String no = login.firstPolice.getPoliceNo();
            Log.i(TAG, "run policeId: " + policeId + " name：" + name);
            sendMsg(0, "verify_success，当前警员：" + name);
            if (target.equals(Constants.ACTIVITY_USER)) {
                if (name.equals("admin")) {
                    sendMsg(0, "verify_success 当前用户:" + name);
                    Intent intent = new Intent(getContext(), toClass);
                    intent.putExtra("firstPoliceInfo", JSON.toJSONString(login.firstPolice));
//                        intent.putExtra("secondPoliceInfo", JSON.toJSONString(login.secondPolice));
                    getContext().startActivity(intent);
                    getActivity().finish();
                } else {
//                    streamId = SoundPlayUtil.getInstance().play(R.raw.no_permission);
                    sendMsg(0, "您没有权限 当前用户：" + name);
                }
            } else {
                if (isDutyManager) {
                    if (target.equals(Constants.ACTIVITY_URGENT)) {
                        //值班领导验证指纹
                        sendMsg(0, "请值班领导验证指纹");
//                        streamId = SoundPlayUtil.getInstance().play(R.raw.duty_leader_face);
                    } else {
                        //值班管理员验证指纹
                        sendMsg(0, "请第二位值班管理员验证指纹");
//                        streamId = SoundPlayUtil.getInstance().play(R.raw.duty_manager_face);
                    }
                    Constants.isFirstVerify = false;
                } else {
//                    streamId = SoundPlayUtil.getInstance().play(R.raw.no_permission);
                    sendMsg(0, "您没有权限 当前用户：" + name);
                }
            }

        } else {
            Log.i(TAG, "获取用户信息失败: ");
            sendMsg(0, "获取用户信息失败！");
//            streamId = SoundPlayUtil.getInstance().play(R.raw.usernotexist);
        }
    }

    /**
     * 验证第二人指纹
     *
     * @param id
     */
    private void secondVerifyPolice(int id) {
        login.secondPolice = verifyIdentity(id);
        if (login.secondPolice != null) {
            int secondPoliceId = login.secondPolice.getUserId();
            String name = login.secondPolice.getUserName();
            Log.i(TAG, "run policeId: " + secondPoliceId + " 姓名：" + name);
            if (target.equals(Constants.ACTIVITY_URGENT)) {
                //判断是否值班领导
                if (isDutyLeader) {
                    sendMsg(0, "verify_success 当前用户：" + name);
                    Intent intent = new Intent(getContext(), toClass);
                    intent.putExtra("firstPoliceInfo", JSON.toJSONString(login.firstPolice));
                    intent.putExtra("secondPoliceInfo", JSON.toJSONString(login.secondPolice));
                    getActivity().startActivity(intent);
                    getActivity().finish();
                } else {
                    sendMsg(0, "您没有权限！当前用户：" + name);
//                    streamId = SoundPlayUtil.getInstance().play(R.raw.no_permission);
                }
            } else {
                //判断是否值班管理员
                if (isDutyManager) { //值班管理员
                    sendMsg(0, "verify_success 当前用户:" + name);
                    Intent intent = new Intent(getContext(), toClass);
                    intent.putExtra("firstPoliceInfo", JSON.toJSONString(login.firstPolice));
                    intent.putExtra("secondPoliceInfo", JSON.toJSONString(login.secondPolice));
                    getContext().startActivity(intent);
                    getActivity().finish();
                } else {
//                    streamId = SoundPlayUtil.getInstance().play(R.raw.no_permission);
                    sendMsg(0, "您没有权限 当前用户：" + name);
                }
            }
        } else {
            Log.i(TAG, "用户不存在: ");
            sendMsg(0, "获取用户信息失败！");
//            streamId = SoundPlayUtil.getInstance().play(R.raw.usernotexist);
        }
    }

    private UserBean verifyIdentity(int id) {
        isDutyLeader = false;
        isDutyManager = false;
        //根据指纹id获取警员信息
        if (userBeanList != null && !userBeanList.isEmpty()) {
            Log.i(TAG, "verifyIdentity polices from memory: " + id);
            for (int i = 0; i < userBeanList.size(); i++) {
                UserBean membersBean = userBeanList.get(i);
//                List<UserBiosBean> policeBios = membersBean.getPoliceBios();
//                if (policeBios != null && !policeBios.isEmpty()) {
////                Log.i(TAG, "getPoliceInfo policeBios size: " + policeBios.size());
//                    for (int j = 0; j < policeBios.size(); j++) {
//                        PoliceBiosBean policeBiosBean = policeBios.get(j);
//                        int deviceType = policeBiosBean.getDeviceType();
//                        if (deviceType == Constants.DEVICE_FACE) { //设备类型为人脸
//                            int faceprintId = policeBiosBean.getFingerprintId();
//                            Log.i(TAG, "getPoliceInfo faceprintId: " + faceprintId);
//                            if (faceprintId == id) {
//                                String policeId = policeBiosBean.getPoliceId();
//                                String name = membersBean.getName();
//                                int policeType = membersBean.getPoliceType();
//                                //验证是否值班管理员
//                                verifyIsCurrentManager(policeId);
//                                if (policeType == 3) { //领导
//                                    if (currentLeader != null) {
//                                        if (policeId.equals(currentLeader.getId())) {//值班领导
//                                            isDutyLeader = true;
//                                        }
//                                    }
//                                }
//                                LogUtil.i(TAG, "getIdentity  policeId: " + policeId + " ===警员姓名: " + name
//                                        + " ===policeType:" + RTool.convertPoliceType(policeType));
//                                return membersBean;
//                            }
//                        }
//                    }
//                }
            }
        }
        return null;
    }

    public void sendMsg(int what, Object obj) {
        Message message = mHandler.obtainMessage(what, obj);
        message.sendToTarget();
    }
}
