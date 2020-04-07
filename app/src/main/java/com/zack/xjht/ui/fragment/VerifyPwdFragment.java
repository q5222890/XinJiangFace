package com.zack.xjht.ui.fragment;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.yanzhenjie.nohttp.rest.Response;
import com.zack.xjht.Constants;
import com.zack.xjht.R;
import com.zack.xjht.Utils.SoundPlayUtil;
import com.zack.xjht.Utils.ToastUtil;
import com.zack.xjht.entity.LoginBean;
import com.zack.xjht.entity.UserBean;
import com.zack.xjht.http.HttpClient;
import com.zack.xjht.http.HttpListener;
import com.zack.xjht.ui.LoginActivity;
import com.zack.xjht.ui.UserActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * 验证指纹
 */
public class VerifyPwdFragment extends Fragment {
    private static final String TAG = "VerifyPwdFragment";

    @BindView(R.id.edt_user_name)
    EditText edtUserName;
    @BindView(R.id.ll_user_name)
    LinearLayout llUserName;
    @BindView(R.id.edt_user_pwd)
    EditText edtUserPwd;
    @BindView(R.id.open_line_pwd)
    LinearLayout openLinePwd;
    @BindView(R.id.open_btn_login)
    Button openBtnLogin;
    @BindView(R.id.open_btn_cancel)
    Button openBtnCancel;
    @BindView(R.id.open_ll_confirm)
    LinearLayout openLlConfirm;
    Unbinder unbinder;
    @BindView(R.id.verify_pwd_tv_user)
    TextView verifyPwdTvUser;
    private int streamId;
    private String target;
    private Class<?> toClass;
    private List<UserBean> membersBeanList;
    private LoginActivity login;
    private FragmentActivity activity;

    public VerifyPwdFragment() {
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        login = (LoginActivity) context;
        activity = getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_verify_pwd, container, false);
        unbinder = ButterKnife.bind(this, view);
        target = getActivity().getIntent().getStringExtra("activity");
        if (Constants.isFirstVerify) {
            //第一次验证
            //验证值班管理员指纹
//            streamId = SoundPlayUtil.getInstance().play(R.raw.user_pwd_login);
            verifyPwdTvUser.setText("请输入警号和密码验证");
        } else {
            //第二次验证
            if (target.equals(Constants.ACTIVITY_URGENT)) { //紧急领枪
                //验证值班领导指纹
//                streamId = SoundPlayUtil.getInstance().play(R.raw.user_pwd_login);
                verifyPwdTvUser.setText("请输入警号和密码验证");
            } else { //非紧急领枪
                //验证值班管理员指纹
//                streamId = SoundPlayUtil.getInstance().play(R.raw.user_pwd_login);
                verifyPwdTvUser.setText("请第二位值班人员输入警号和密码验证");
            }
        }
//        streamId = SoundPlayUtil.getInstance().play(R.raw.user_pwd_login);
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
        SoundPlayUtil.getInstance().stop(streamId);
    }

    @OnClick({R.id.open_btn_login, R.id.open_btn_cancel})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.open_btn_login:
                loginVerify();
                break;
            case R.id.open_btn_cancel:
                activity.finish();//返回
                break;
        }
    }

    /**
     * 登入确认
     */
    private void loginVerify() {
        final String policeNo = edtUserName.getText().toString();
        final String password = edtUserPwd.getText().toString();
        if (TextUtils.isEmpty(policeNo)) {
            Log.i(TAG, "onClick 警号为空: ");
            streamId = SoundPlayUtil.getInstance().play(R.raw.user_name_null);
            return;
        }

        if (TextUtils.isEmpty(password)) {
            Log.i(TAG, "onClick 密码为空: ");
            streamId = SoundPlayUtil.getInstance().play(R.raw.password_null);
            return;
        }

        userLogin(policeNo, password);
    }

    /**
     * 验证警号和密码是否正确
     *
     * @param policeNo
     * @param password
     */
    private void userLogin(String policeNo, String password) {
        HttpClient.getInstance().userLogin(getContext(), policeNo, password,
                new HttpListener<String>() {
                    @Override
                    public void onSucceed(int what, Response<String> response) throws JSONException {
                        Log.i(TAG, "onSucceed userLogin response: " + response.get());
                        try {
                            if (!TextUtils.isEmpty(response.get())) {
                                LoginBean loginBean = JSON.parseObject(response.get(), LoginBean.class);
                                String result = loginBean.getResult();
                                if (result.equals("success")) {
    //                                ToastUtil.showShort("verify_success！");
                                    Log.i(TAG, "onSucceed  获取用户信息成功！: ");
                                    UserBean user = loginBean.getUser();
                                    if (user != null) {
                                        String roleKeys = user.getRoleKeys();
                                        if (!TextUtils.isEmpty(roleKeys)) {
                                            String[] split = roleKeys.split(",");
                                            List<String> roleList = Arrays.asList(split);
                                            if (roleList != null && !roleList.isEmpty()) { //判断权限
                                                //判断是否值班管理员或领导
                                                if (roleList.contains(Constants.ROLE_ADMIN)
                                                        || roleList.contains(Constants.ROLE_ROOM_ADMIN)
                                                        ||roleList.contains(Constants.ROLE_APPROVER)
                                                        ||roleList.contains(Constants.ROLE_MANAGER)) {
                                                    //是管理员 进人员特征管理界面
                                                    ToastUtil.showShort("验证成功");
                                                    Intent intent = new Intent(login, UserActivity.class);
                                                    intent.putExtra("firstPoliceInfo", user);
                                                    login.startActivity(intent);
                                                    login.finish();
                                                } else {
                                                    //不是管理员 退出
                                                    ToastUtil.showShort("权限不足！");
                                                }
                                            } else {
                                                Log.i(TAG, "onSucceed 权限获取失败: ");
                                                ToastUtil.showShort("没有权限!");
                                            }
                                        }else {
                                            Log.i(TAG, "onSucceed 权限获取失败: ");
                                            ToastUtil.showShort("没有权限!");
                                        }
                                    } else {
                                        Log.i(TAG, "onSucceed  user is empty: ");
                                        ToastUtil.showShort("获取用户数据为空！");
                                    }
                                } else {
                                    ToastUtil.showShort("验证失败！");
                                }
                            } else {
                                ToastUtil.showShort("获取警员信息为空！");
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            ToastUtil.showShort("获取警员信息为空！");
                        }
                    }

                    @Override
                    public void onFailed(int what, Response<String> response) {
                        ToastUtil.showShort("网络错误，获取警员信息失败！");
                    }
                });
    }

}
