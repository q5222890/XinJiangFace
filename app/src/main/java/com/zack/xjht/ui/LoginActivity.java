package com.zack.xjht.ui;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.zack.xjht.Constants;
import com.zack.xjht.R;
import com.zack.xjht.entity.UserBean;
import com.zack.xjht.ui.fragment.VerifyFaceFragment;
import com.zack.xjht.ui.fragment.VerifyFingerFragment;
import com.zack.xjht.ui.fragment.VerifyPwdFragment;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 选择多方式登录界面
 */
public class LoginActivity extends BaseActivity {
    private static final String TAG = "LoginActivity";

    @BindView(R.id.login_tv_tittle)
    TextView loginTvTittle;
    @BindView(R.id.login_content)
    FrameLayout loginContent;
    @BindView(R.id.login_btn_finger_verify)
    Button loginBtnFingerVerify;
    @BindView(R.id.login_btn_vein_verify)
    Button loginBtnVeinVerify;
    @BindView(R.id.login_btn_iris_verify)
    Button loginBtnIrisVerify;
    @BindView(R.id.login_btn_face_verify)
    Button loginBtnFaceVerify;
    @BindView(R.id.login_btn_password_verify)
    Button loginBtnPasswordVerify;
    private FragmentManager fm;
    private FragmentTransaction transaction;
    private List<View> viewList;
    private VerifyFingerFragment verifyFingerFragment;
    private VerifyFaceFragment verifyFaceFragment;
    private VerifyPwdFragment verifyPwdFragment;
    public UserBean firstPolice, secondPolice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        viewList = new ArrayList<>();

        loginBtnVeinVerify.setVisibility(View.GONE);
        loginBtnIrisVerify.setVisibility(View.GONE);
        loginBtnFaceVerify.setVisibility(View.GONE);

        viewList.add(loginBtnFingerVerify);
        viewList.add(loginBtnFaceVerify);
        viewList.add(loginBtnPasswordVerify);

        String activity = getIntent().getStringExtra("activity");
        if(activity.equals(Constants.ACTIVITY_USER)){
            loginBtnPasswordVerify.setVisibility(View.VISIBLE);
        }else{
            loginBtnPasswordVerify.setVisibility(View.GONE);
        }
        verifyFingerFragment = new VerifyFingerFragment();
        verifyFaceFragment = new VerifyFaceFragment();
        verifyPwdFragment = new VerifyPwdFragment();

        setBackgroundColorById(R.id.login_btn_finger_verify);
        fm = getSupportFragmentManager();
        transaction = fm.beginTransaction();
        transaction.replace(R.id.login_content, verifyFingerFragment);
        transaction.commit();
    }

    /**
     * 设置item背景颜色
     * * @param btnId
     */
    private void setBackgroundColorById(int btnId) {
        for (View view : viewList) {
            if (view.getId() == btnId) {
                view.setBackgroundResource(R.drawable.btn_bg_04);
                ((Button)view).setTextColor(getResources().getColor(R.color.white));
            } else {
                view.setBackgroundResource(R.drawable.bg_button);
                ((Button)view).setTextColor(getResources().getColor(R.color.white));
            }
        }
    }

    @OnClick({R.id.login_btn_finger_verify, R.id.login_btn_vein_verify, R.id.login_btn_iris_verify,
            R.id.login_btn_face_verify, R.id.login_btn_password_verify, R.id.ac_top_back})
    public void onViewClicked(View view) {
        setBackgroundColorById(view.getId());
        fm = getSupportFragmentManager();
        transaction = fm.beginTransaction();
        switch (view.getId()) {
            case R.id.login_btn_finger_verify://指纹验证
                transaction.replace(R.id.login_content, verifyFingerFragment);
                break;
            case R.id.login_btn_face_verify: //人脸验证
                transaction.replace(R.id.login_content, verifyFaceFragment);
                break;
            case R.id.login_btn_password_verify: //用户名密码验证
                transaction.replace(R.id.login_content, verifyPwdFragment);
                break;
            case R.id.ac_top_back: //返回
                finish();
                break;
        }
        transaction.commit();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Constants.isFirstVerify = true;
    }

}
