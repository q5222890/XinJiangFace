package com.zack.xjht.ui;

import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.zack.xjht.R;
import com.zack.xjht.ui.fragment.AlarmLogFragment;
import com.zack.xjht.ui.fragment.CabsInfoFragment;
import com.zack.xjht.ui.fragment.CaptureFragment;
import com.zack.xjht.ui.fragment.NormalLogFragment;
import com.zack.xjht.ui.fragment.OperateInfoFragment;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * 信息查询
 */
public class QueryActivity extends BaseActivity {
    private static final String TAG = "QueryActivity";

    @BindView(R.id.img_query_gun_info)
    ImageView imgQueryGunInfo;
    @BindView(R.id.query_tabs)
    ConstraintLayout queryTabs;
    @BindView(R.id.fragment_content)
    FrameLayout fragmentContent;
    @BindView(R.id.img_query_alarm_log)
    ImageView imgQueryAlarmLog;
    @BindView(R.id.img_query_oper_gun_log)
    ImageView imgQueryGetGunLog;
    @BindView(R.id.img_query_normal_log)
    ImageView imgQueryNormalLog;
    @BindView(R.id.img_query_capture_image)
    ImageView imgQueryCaptureImage;
    private List<View> viewList = new ArrayList<>();
    private FragmentManager fm;
    private FragmentTransaction ft;
    private Unbinder bind;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_query);
        bind = ButterKnife.bind(this);

        viewList.add(imgQueryGunInfo);
        viewList.add(imgQueryAlarmLog);
        viewList.add(imgQueryGetGunLog);
        viewList.add(imgQueryNormalLog);
        viewList.add(imgQueryCaptureImage);

        setBackgroundColorById(R.id.img_query_gun_info); //添加背景色

        fm = getSupportFragmentManager();
        ft = fm.beginTransaction();
        ft.replace(R.id.fragment_content, new CabsInfoFragment());
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

    @Override
    protected void onRestart() {
        super.onRestart();
//        Log.e(TAG, "onRestart: " );
    }

    @Override
    protected void onStart() {
        super.onStart();
//        Log.e(TAG, "onStart: " );
    }

    @Override
    protected void onResume() {
        super.onResume();
//        Log.e(TAG, "onResume: ");
    }

    @Override
    protected void onPause() {
        super.onPause();
//        Log.e(TAG, "onPause: ");
    }

    @Override
    protected void onStop() {
        super.onStop();
//        Log.e(TAG, "onStop: ");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        Log.e(TAG, "onDestroy: ");
        bind.unbind();
    }

    @OnClick({R.id.img_query_alarm_log, R.id.img_query_oper_gun_log, R.id.img_query_normal_log,
            R.id.img_query_capture_image, R.id.img_query_gun_info})
    public void onViewClicked(View view) {
        super.onViewClicked(view);
        fm = getSupportFragmentManager();
        ft = fm.beginTransaction();
        setBackgroundColorById(view.getId());
        switch (view.getId()) {
            case R.id.img_query_alarm_log:
                ft.replace(R.id.fragment_content, new AlarmLogFragment());
                break;
            case R.id.img_query_oper_gun_log:
                ft.replace(R.id.fragment_content, new OperateInfoFragment());
                break;
            case R.id.img_query_normal_log:
                ft.replace(R.id.fragment_content, new NormalLogFragment());
                break;
            case R.id.img_query_capture_image:
                ft.replace(R.id.fragment_content, new CaptureFragment());
                break;
            case R.id.img_query_gun_info:
                ft.replace(R.id.fragment_content, new CabsInfoFragment());
                break;
        }
        ft.commit();
    }
}
