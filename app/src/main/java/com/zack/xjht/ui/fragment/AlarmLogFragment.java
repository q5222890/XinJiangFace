package com.zack.xjht.ui.fragment;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zack.xjht.R;
import com.zack.xjht.db.DBManager;
import com.zack.xjht.db.gen.AlarmLogBeanDao;
import com.zack.xjht.entity.AlarmLogBean;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * 报警日志
 */

public class AlarmLogFragment extends Fragment {

    private static final String TAG = "AlarmLogFragment";
    Unbinder unbinder;
    @BindView(R.id.alarm_log_list)
    RecyclerView alarmLogRecyclerview;
    @BindView(R.id.alarm_log_line)
    LinearLayout alarmLogLine;
    @BindView(R.id.alarm_log_btn_pre_page)
    Button alarmLogBtnPrePage;
    @BindView(R.id.alarm_log_tv_cur_page)
    TextView alarmLogTvCurPage;
    @BindView(R.id.alarm_log_btn_next_page)
    Button alarmLogBtnNextPage;
    @BindView(R.id.ll_page)
    LinearLayout llPage;
    private View view;
    private List<AlarmLogBean> alarmLogList;
    private AlarmInfoAdapter alarminfoAdapter;
    private int index = 0;
    private int pageCount = 10;
    private AlarmLogBeanDao alarmLogBeanDao;
    private FragmentActivity activity;
    private Context mContext;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = getActivity();
        mContext = context;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_alarm_log, container, false);
        unbinder = ButterKnife.bind(this, view);
        initView();
        return view;
    }

    private void initView() {
        alarmLogBtnNextPage.setVisibility(View.INVISIBLE);
        alarmLogBtnPrePage.setVisibility(View.INVISIBLE);
        alarmLogList = new ArrayList<>();
        LinearLayoutManager llm = new LinearLayoutManager(getContext(),
                LinearLayoutManager.VERTICAL, false);
        alarmLogRecyclerview.setLayoutManager(llm);
        alarminfoAdapter = new AlarmInfoAdapter();
        alarmLogRecyclerview.setAdapter(alarminfoAdapter);

        alarmLogBeanDao = DBManager.getInstance().getAlarmLogBeanDao();

        new Thread(new Runnable() {
            @Override
            public void run() {
                alarmLogList = alarmLogBeanDao.loadAll();
                Collections.reverse(alarmLogList);
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        alarminfoAdapter.notifyDataSetChanged();
                        initPreNextBtn();
                    }
                });
            }
        }).start();
    }

    private void initPreNextBtn() {
        if (alarmLogList.isEmpty()) {
            alarmLogTvCurPage.setText(index + 1 + "/1");
        } else {
            if (alarmLogList.size() <= pageCount) {
                alarmLogBtnNextPage.setVisibility(View.INVISIBLE);
            } else {
                alarmLogBtnNextPage.setVisibility(View.VISIBLE);
            }
            alarmLogTvCurPage.setText(index + 1 + "/" + (int) Math.ceil((double) alarmLogList.size() / pageCount));
        }
    }

    @OnClick({R.id.alarm_log_btn_pre_page, R.id.alarm_log_btn_next_page})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.alarm_log_btn_pre_page:
                prePager();
                break;
            case R.id.alarm_log_btn_next_page:
                nexPager();
                break;
        }
    }

    private void prePager() {
        index--;
        alarminfoAdapter.notifyDataSetChanged();
        alarmLogTvCurPage.setText(index + 1 + "/" + (int) Math.ceil((double) alarmLogList.size() / pageCount));
        //隐藏上一个或下一个按钮
        checkButton();
        Log.i(TAG, "prePager index: " + index);
    }

    private void nexPager() {
        index++;
        alarminfoAdapter.notifyDataSetChanged();
        alarmLogTvCurPage.setText(index + 1 + "/" + (int) Math.ceil((double) alarmLogList.size() / pageCount));
        //隐藏上一个或下一个按钮
        checkButton();
        Log.i(TAG, "nexPager index: " + index);
    }

    private void checkButton() {
        if (index <= 0) {
            alarmLogBtnPrePage.setVisibility(View.INVISIBLE);
            alarmLogBtnNextPage.setVisibility(View.VISIBLE);
        } else if (alarmLogList.size() - index * pageCount <= pageCount) {    //数据总数减每页数当小于每页可显示的数字时既是最后一页
            alarmLogBtnPrePage.setVisibility(View.VISIBLE);
            alarmLogBtnNextPage.setVisibility(View.INVISIBLE);
        } else {
            alarmLogBtnNextPage.setVisibility(View.VISIBLE);
            alarmLogBtnPrePage.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 报警数据适配器
     */
    class AlarmInfoAdapter extends RecyclerView.Adapter<AlarmInfoAdapter.AlarmViewHolder> {

        @Override
        public AlarmViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(
                    R.layout.item_alarm_info, parent, false);
            return new AlarmViewHolder(view);
        }

        @Override
        public void onBindViewHolder(AlarmViewHolder holder, int position) {
            int pos = position + index * pageCount;
            AlarmLogBean alarmLog = alarmLogList.get(pos);
            Long id = alarmLog.getId();
            String relieveId = alarmLog.getRelieveId();
            String relieveName = alarmLog.getRelieveName();
            String warningContent = alarmLog.getWarningContent();
            String warningTime = alarmLog.getWarningTime();
            String warningType = alarmLog.getWarningType();

            holder.alarmInfoItemTxtNo.setText(String.valueOf(id)); //日志Id
            holder.alarmInfoItemTxtTime.setText(warningTime); //日志时间
            holder.alarmInfoItemTxtContent.setText(warningContent); //日志内容
            holder.alarmInfoItemRelievePolice.setText("" + relieveName); //解除警员姓名

            //设置背景色
            if ((pos & 1) == 1) {
                holder.alarmInfoItemRoot.setBackgroundColor(Color.parseColor("#FF3F51B5"));
            } else {
                holder.alarmInfoItemRoot.setBackgroundColor(Color.parseColor("#FF01579B"));
            }
        }

        @Override
        public int getItemCount() {
            int current = index * pageCount;
            return alarmLogList.size() - current < pageCount ? alarmLogList.size() - current : pageCount;
        }

        class AlarmViewHolder extends RecyclerView.ViewHolder {
            @BindView(R.id.alarm_info_item_txt_no)
            TextView alarmInfoItemTxtNo;
            @BindView(R.id.alarm_info_item_txt_time)
            TextView alarmInfoItemTxtTime;
            @BindView(R.id.alarm_info_item_txt_content)
            TextView alarmInfoItemTxtContent;
            @BindView(R.id.alarm_info_item_relieve_police)
            TextView alarmInfoItemRelievePolice;
            @BindView(R.id.alarm_info_item_root)
            LinearLayout alarmInfoItemRoot;

            AlarmViewHolder(View view) {
                super(view);
                ButterKnife.bind(this, view);
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }
}
