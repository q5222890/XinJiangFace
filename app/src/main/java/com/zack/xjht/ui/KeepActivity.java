package com.zack.xjht.ui;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zack.xjht.R;
import com.zack.xjht.entity.UserBean;
import com.zack.xjht.ui.fragment.KeepBackFragment;
import com.zack.xjht.ui.fragment.KeepGetFragment;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 枪支保养
 */

public class KeepActivity extends BaseActivity {

    private static final String TAG = KeepActivity.class.getSimpleName();
    @BindView(R.id.keep_btn_get_gun)
    TextView keepBtnGetGun;
    @BindView(R.id.keep_btn_back_gun)
    TextView keepBtnBackGun;
    @BindView(R.id.keep_ll_tittle)
    LinearLayout keepLlTittle;
    @BindView(R.id.keep_content)
    FrameLayout keepContent;
    private UserBean firstManage;
    private UserBean secondManage;

    private List<View> viewList = new ArrayList<>();
    private FragmentManager fm;
    private FragmentTransaction ft;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_keep);
        ButterKnife.bind(this);

//        String firstPoliceInfo = getIntent().getStringExtra("firstPoliceInfo");
//        String secondPoliceInfo = getIntent().getStringExtra("secondPoliceInfo");

        viewList.add(keepBtnGetGun);
        viewList.add(keepBtnBackGun);

        fm = getSupportFragmentManager();
        ft = fm.beginTransaction();
        setBackgroundColorById(R.id.keep_btn_get_gun);
        ft.replace(R.id.keep_content, new KeepGetFragment());
        ft.commit();
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
    }

    private void setBackgroundColorById(int btnId) {
        for (View view : viewList) {
            if (view.getId() == btnId) {
                view.setBackgroundResource(R.color.light_blue);
            } else {
                view.setBackgroundResource(R.color.dark_blue);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @OnClick({R.id.keep_btn_get_gun, R.id.keep_btn_back_gun})
    public void onViewClicked(View view) {
        super.onViewClicked(view);
        fm = getSupportFragmentManager();
        ft = fm.beginTransaction();
        setBackgroundColorById(view.getId());
        switch (view.getId()) {
            case R.id.keep_btn_get_gun: //领取枪支
                ft.replace(R.id.keep_content, new KeepGetFragment());
                break;
            case R.id.keep_btn_back_gun://归还枪支
                ft.replace(R.id.keep_content, new KeepBackFragment());
                break;
        }
        ft.commit();
    }


}
