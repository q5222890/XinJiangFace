package com.zack.xjht.ui;

import android.graphics.Color;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zack.xjht.Constants;
import com.zack.xjht.R;
import com.zack.xjht.Utils.SharedUtils;
import com.zack.xjht.entity.UserBean;
import com.zack.xjht.ui.fragment.UrgentBackAmmosFragment;
import com.zack.xjht.ui.fragment.UrgentBackFragment;
import com.zack.xjht.ui.fragment.UrgentBackGunsFragment;
import com.zack.xjht.ui.fragment.UrgentGetAmmosFragment;
import com.zack.xjht.ui.fragment.UrgentGetFragment;
import com.zack.xjht.ui.fragment.UrgentGetGunsFragment;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 紧急领枪
 */

public class UrgentGoActivity extends BaseActivity {

    private static final String TAG = UrgentGoActivity.class.getSimpleName();
    @BindView(R.id.urgent_btn_get)
    TextView urgentBtnGet;
    @BindView(R.id.urgent_btn_back)
    TextView urgentBtnBack;
    @BindView(R.id.urgent_content)
    FrameLayout urgentContent;
    @BindView(R.id.urgent_rl_title)
    LinearLayout urgentRlTitle;
    @BindView(R.id.root_view)
    ConstraintLayout rootView;
    private List<View> viewList = new ArrayList<>();
    private FragmentManager fm;
    private FragmentTransaction ft;
    private UserBean manage, leader;
    private UserBean firstPolice, secondPolice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_urgent);
        ButterKnife.bind(this);
//        ac_top_back.setVisibility(View.GONE);
//        timerunnable.run();
        firstPolice = (UserBean) getIntent().getSerializableExtra("firstPoliceInfo");
        secondPolice = (UserBean) getIntent().getSerializableExtra("secondPoliceInfo");

        if (firstPolice != null) {
            Log.i(TAG, "onCreate firstPolice: " + firstPolice.getUserName());
        }

        if (secondPolice != null) {
            Log.i(TAG, "onCreate  secondPolice: " + secondPolice.getUserName());
        }

        String cabType = SharedUtils.getCabType();
        switch (cabType) {
            case Constants.TYPE_AMMO_CAB://弹药柜
                //领取弹药和归还弹药
                urgentBtnGet.setText("领取弹药");
                urgentBtnBack.setText("归还弹药");
                viewList.add(urgentBtnGet);
                viewList.add(urgentBtnBack);
                fm = getSupportFragmentManager();
                ft = fm.beginTransaction();
                setBackgroundColorById(R.id.urgent_btn_get);
                ft.replace(R.id.urgent_content, new UrgentGetAmmosFragment());
                ft.commit();
                break;
            case Constants.TYPE_MIX_CAB://枪弹综合柜
                //领取枪弹和归还枪弹
                urgentBtnGet.setText("领取枪弹");
                urgentBtnBack.setText("归还枪弹");
                viewList.add(urgentBtnGet);
                viewList.add(urgentBtnBack);
                fm = getSupportFragmentManager();
                ft = fm.beginTransaction();
                setBackgroundColorById(R.id.urgent_btn_get);
                ft.replace(R.id.urgent_content, new UrgentGetFragment());
                ft.commit();
                break;
            case Constants.TYPE_LONG_GUN_CAB: //长枪柜
            case Constants.TYPE_SHORT_GUN_CAB://短枪柜
            case Constants.TYPE_SHORT_LONG_GUN_CAB: //长短枪混合柜
                //领取枪支和归还枪支
                urgentBtnGet.setText("领取枪支");
                urgentBtnBack.setText("归还枪支");
                viewList.add(urgentBtnGet);
                viewList.add(urgentBtnBack);
                fm = getSupportFragmentManager();
                ft = fm.beginTransaction();
                setBackgroundColorById(R.id.urgent_btn_get);
                ft.replace(R.id.urgent_content, new UrgentGetGunsFragment());
                ft.commit();
                break;
        }

    }

    private void setBackgroundColorById(int btnId) {
        for (View view : viewList) {
            if (view.getId() == btnId) {
                view.setBackgroundResource(R.color.simple_blue);
            } else {
                view.setBackgroundColor(Color.TRANSPARENT);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @OnClick({R.id.urgent_btn_get, R.id.urgent_btn_back})
    public void onViewClicked(View view) {
        super.onViewClicked(view);
        fm = getSupportFragmentManager();
        ft = fm.beginTransaction();
        setBackgroundColorById(view.getId());
        String cabType = SharedUtils.getCabType();
        switch (view.getId()) {
            case R.id.urgent_btn_get: //紧急领枪弹
                switch (cabType) {
                    case Constants.TYPE_AMMO_CAB://弹药柜
                        //领取弹药和归还弹药
                        ft.replace(R.id.urgent_content, new UrgentGetAmmosFragment());
                        break;
                    case Constants.TYPE_MIX_CAB://枪弹综合柜
                        //领取枪弹和归还枪弹
                        ft.replace(R.id.urgent_content, new UrgentGetFragment());
                        break;
                    case Constants.TYPE_LONG_GUN_CAB: //长枪柜
                    case Constants.TYPE_SHORT_GUN_CAB://短枪柜
                    case Constants.TYPE_SHORT_LONG_GUN_CAB: //长短枪混合柜
                        //领取枪支和归还枪支
                        ft.replace(R.id.urgent_content, new UrgentGetGunsFragment());
                        break;
                }
                break;
            case R.id.urgent_btn_back://归还枪弹
                switch (cabType) {
                    case Constants.TYPE_AMMO_CAB://弹药柜
                        //领取弹药和归还弹药
                        ft.replace(R.id.urgent_content, new UrgentBackAmmosFragment());
                        break;
                    case Constants.TYPE_MIX_CAB://枪弹综合柜
                        //领取枪弹和归还枪弹
                        ft.replace(R.id.urgent_content, new UrgentBackFragment());
                        break;
                    case Constants.TYPE_LONG_GUN_CAB: //长枪柜
                    case Constants.TYPE_SHORT_GUN_CAB://短枪柜
                    case Constants.TYPE_SHORT_LONG_GUN_CAB: //长短枪混合柜
                        //领取枪支和归还枪支
                        ft.replace(R.id.urgent_content, new UrgentBackGunsFragment());
                        break;
                }
                break;
        }
        ft.commit();
    }

}
