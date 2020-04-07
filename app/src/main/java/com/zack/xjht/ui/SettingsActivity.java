package com.zack.xjht.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.zack.xjht.R;
import com.zack.xjht.ui.fragment.AboutFragment;
import com.zack.xjht.ui.fragment.BasicFragment;
import com.zack.xjht.ui.fragment.DebugFragment;
import com.zack.xjht.ui.fragment.LockFragment;
import com.zack.xjht.ui.fragment.OtherFragment;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SettingsActivity extends BaseActivity {

    @BindView(R.id.ac_top_setting)
    TextView acTopSetting;
    @BindView(R.id.setting_left_tabs)
    ConstraintLayout settingLeftTabs;
    @BindView(R.id.fragment_content)
    FrameLayout fragmentContent;
    @BindView(R.id.settings_iv_basic)
    ImageView settingsIvBasic;
    @BindView(R.id.settings_iv_debug)
    ImageView settingsIvDebug;
    @BindView(R.id.settings_iv_lock)
    ImageView settingsIvLock;
    @BindView(R.id.settings_iv_other)
    ImageView settingsIvOther;
    @BindView(R.id.settings_iv_about)
    ImageView settingsIvAbout;
    private List<View> viewList = new ArrayList<>();
    private FragmentManager fm;
    private FragmentTransaction ft;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_settings);
        ButterKnife.bind(this);

        viewList.add(settingsIvBasic);
        viewList.add(settingsIvDebug);
        viewList.add(settingsIvLock);
        viewList.add(settingsIvAbout);
        viewList.add(settingsIvOther);

        fm = getSupportFragmentManager();
        ft = fm.beginTransaction();
        setBackgroundColorById(R.id.settings_iv_basic);
        ft.replace(R.id.fragment_content, new BasicFragment());
        ft.commit();
    }

    private void setBackgroundColorById(int btnId) {
        for (View view : viewList) {
            if (view.getId() == btnId) {
                view.setSelected(true);
            } else {
                view.setSelected(false);
            }
        }
    }

    @OnClick({R.id.settings_iv_basic, R.id.settings_iv_debug, R.id.settings_iv_about,
            R.id.settings_iv_other, R.id.settings_iv_lock})
    public void onViewClicked(View view) {
        super.onViewClicked(view);
        fm = getSupportFragmentManager();
        ft = fm.beginTransaction();
        setBackgroundColorById(view.getId());
        switch (view.getId()) {
            case R.id.settings_iv_basic: //基本设置
                ft.replace(R.id.fragment_content, new BasicFragment());
                break;
            case R.id.settings_iv_debug://调试
                ft.replace(R.id.fragment_content, new DebugFragment());
                break;
            case R.id.settings_iv_lock://枪锁设置
                ft.replace(R.id.fragment_content, new LockFragment());
                break;
            case R.id.settings_iv_other://其它设置
                ft.replace(R.id.fragment_content, new OtherFragment());
                break;
            case R.id.settings_iv_about://系统信息
                ft.replace(R.id.fragment_content, new AboutFragment());
                break;
        }
        ft.commit();
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        //建议在此执行commit操作
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        //建议在此执行commit操作
    }
}
