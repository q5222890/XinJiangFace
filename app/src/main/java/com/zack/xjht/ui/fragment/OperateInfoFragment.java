package com.zack.xjht.ui.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zack.xjht.Constants;
import com.zack.xjht.R;
import com.zack.xjht.Utils.SharedUtils;
import com.zack.xjht.Utils.Utils;
import com.zack.xjht.db.DBManager;
import com.zack.xjht.db.gen.OperLogBeanDao;
import com.zack.xjht.entity.OperLogBean;
import com.zack.xjht.entity.OperateInfoAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * 领还枪日志
 */

public class OperateInfoFragment extends Fragment {

    private static final String TAG = "OperateInfoFragment";
    @BindView(R.id.operate_info_rv)
    RecyclerView operateInfoRv;
    Unbinder unbinder;
    @BindView(R.id.oper_gun_info_line)
    LinearLayout operGunInfoLine;
    @BindView(R.id.get_gun_btn_pre_page)
    Button getGunBtnPrePage;
    @BindView(R.id.get_gun_tv_cur_page)
    TextView getGunTvCurPage;
    @BindView(R.id.get_gun_btn_next_page)
    Button getGunBtnNextPage;
    @BindView(R.id.ll_page)
    LinearLayout llPage;
    @BindView(R.id.operate_log_tv_gun_no)
    TextView operateLogTvGunNo;
    @BindView(R.id.operate_log_tv_num)
    TextView operateLogTvNum;
    private View view;
    private List<OperLogBean> operGunsLogList = new ArrayList<>();
    private OperLogBeanDao operLogBeanDao;
    private OperateInfoAdapter operateInfoAdapter;
    private int index = 0;
    private int pageCount = 10;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_operate_log, container, false);
        unbinder = ButterKnife.bind(this, view);
        initView();
        return view;
    }

    private void initView() {
        operLogBeanDao = DBManager.getInstance().getOperLogBeanDao();
        operGunsLogList = operLogBeanDao.loadAll();
        Log.i(TAG, "initView list size: " + operGunsLogList.size());
        Collections.reverse(operGunsLogList);
        LinearLayoutManager llm = new LinearLayoutManager(getContext(),
                LinearLayoutManager.VERTICAL, false);
        operateInfoRv.setLayoutManager(llm);
        operateInfoAdapter = new OperateInfoAdapter(operGunsLogList, index, pageCount);
        operateInfoRv.setAdapter(operateInfoAdapter);
        getGunBtnNextPage.setVisibility(View.INVISIBLE);
        getGunBtnPrePage.setVisibility(View.INVISIBLE);
        if (operGunsLogList.isEmpty()) {
            getGunTvCurPage.setText(index + 1 + "/1");
        } else {
            if (operGunsLogList.size() <= pageCount) {
                getGunBtnNextPage.setVisibility(View.INVISIBLE);
            } else {
                getGunBtnNextPage.setVisibility(View.VISIBLE);
            }
            getGunTvCurPage.setText(index + 1 + "/" + (int) Math.ceil((double) operGunsLogList.size() / pageCount));
        }
        if (SharedUtils.getCabType().equals(Constants.TYPE_AMMO_CAB)) {
            operateLogTvGunNo.setVisibility(View.GONE);
            operateLogTvNum.setVisibility(View.VISIBLE);
        } else {
            operateLogTvGunNo.setVisibility(View.VISIBLE);
            operateLogTvNum.setVisibility(View.GONE);
        }
    }

    @OnClick({R.id.get_gun_btn_pre_page, R.id.get_gun_btn_next_page})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.get_gun_btn_pre_page: //上一页
                prePager();
                break;
            case R.id.get_gun_btn_next_page://下一页
                nexPager();
                break;
        }
    }

    private void prePager() {
        index--;
        operateInfoAdapter.notifyDataSetChanged();
        getGunTvCurPage.setText(index + 1 + "/" + (int) Math.ceil((double) operGunsLogList.size() / pageCount));
        //隐藏上一个或下一个按钮
        checkButton();
        Log.i(TAG, "prePager index: " + index);
    }

    private void nexPager() {
        index++;
        getGunTvCurPage.setText(index + 1 + "/" + (int) Math.ceil((double) operGunsLogList.size() / pageCount));
        operateInfoAdapter.notifyDataSetChanged();
        //隐藏上一个或下一个按钮
        checkButton();
        Log.i(TAG, "nexPager index: " + index);
    }

    private void checkButton() {
        if (index <= 0) {
            getGunBtnPrePage.setVisibility(View.INVISIBLE);
            getGunBtnNextPage.setVisibility(View.VISIBLE);
        } else if (operGunsLogList.size() - index * pageCount <= pageCount) {
            //数据总数减每页数当小于每页可显示的数字时既是最后一页
            getGunBtnPrePage.setVisibility(View.VISIBLE);
            getGunBtnNextPage.setVisibility(View.INVISIBLE);
        } else {
            getGunBtnNextPage.setVisibility(View.VISIBLE);
            getGunBtnPrePage.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }


}
