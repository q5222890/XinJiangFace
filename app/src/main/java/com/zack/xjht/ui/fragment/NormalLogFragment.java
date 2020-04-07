package com.zack.xjht.ui.fragment;


import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
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

import com.alibaba.fastjson.JSON;
import com.zack.xjht.R;
import com.zack.xjht.Utils.LogUtil;
import com.zack.xjht.Utils.Utils;
import com.zack.xjht.db.DBManager;
import com.zack.xjht.db.gen.CommonLogBeanDao;
import com.zack.xjht.entity.CommonLogBean;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * 操作日志.
 */
public class NormalLogFragment extends Fragment {

    private static final String TAG = "NormalLogFragment";
    @BindView(R.id.normal_operate_info_rv)
    RecyclerView normalOperateInfoRv;
    Unbinder unbinder;
    @BindView(R.id.normal_oper_log_line)
    LinearLayout normalOperLogLine;
    @BindView(R.id.operate_btn_pre_page)
    Button operateBtnPrePage;
    @BindView(R.id.operate_tv_cur_page)
    TextView operateTvCurPage;
    @BindView(R.id.operate_btn_next_page)
    Button operateBtnNextPage;
    @BindView(R.id.ll_page)
    LinearLayout llPage;
    private List<CommonLogBean> normalOperLogList = new ArrayList<>();
    private NormalLogAdapter normalLogAdapter;
    private int index = 0;
    private int pageCount = 14;
    private FragmentActivity activity;
    private Context mContext;
    private CommonLogBeanDao commonLogBeanDao;

    public NormalLogFragment() {
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = getActivity();
        mContext = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_normal_log, container, false);
        unbinder = ButterKnife.bind(this, view);
        initView();
        return view;
    }

    private void initView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(
                getContext(), LinearLayoutManager.VERTICAL, false);
        normalOperateInfoRv.setLayoutManager(layoutManager);
        normalLogAdapter = new NormalLogAdapter();
        normalOperateInfoRv.setAdapter(normalLogAdapter);

        operateBtnNextPage.setVisibility(View.INVISIBLE);
        operateBtnPrePage.setVisibility(View.INVISIBLE);
        commonLogBeanDao = DBManager.getInstance().getCommonLogBeanDao();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    normalOperLogList = commonLogBeanDao.loadAll();
                    LogUtil.i(TAG, "run normalOperLogList : " + JSON.toJSONString(normalOperLogList));
                    Collections.reverse(normalOperLogList);
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            normalLogAdapter.notifyDataSetChanged();
                            if (!activity.isFinishing()) {
                                initPreNextBtn();
                            }
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void initPreNextBtn() {
        if (normalOperLogList.isEmpty()) {
            operateTvCurPage.setText(index + 1 + "/1");
        } else {
            if (normalOperLogList.size() <= pageCount) {
                operateBtnNextPage.setVisibility(View.INVISIBLE);
            } else {
                if (operateBtnNextPage != null) {
                    operateBtnNextPage.setVisibility(View.VISIBLE);
                }
            }
            operateTvCurPage.setText(index + 1 + "/" + (int) Math.ceil((double) normalOperLogList.size() / pageCount));
        }
    }

    @OnClick({R.id.operate_btn_pre_page, R.id.operate_btn_next_page})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.operate_btn_pre_page:
                prePager(); //上一页
                break;
            case R.id.operate_btn_next_page:
                nexPager();//下一页
                break;
        }
    }

    private void prePager() {
        index--;
        normalLogAdapter.notifyDataSetChanged();
        operateTvCurPage.setText(index + 1 + "/" + (int) Math.ceil((double) normalOperLogList.size() / pageCount));
        //隐藏上一个或下一个按钮
        checkButton();
        Log.i(TAG, "prePager index: " + index);
    }

    private void nexPager() {
        index++;
        normalLogAdapter.notifyDataSetChanged();
        operateTvCurPage.setText(index + 1 + "/" + (int) Math.ceil((double) normalOperLogList.size() / pageCount));
        //隐藏上一个或下一个按钮
        checkButton();
        Log.i(TAG, "nexPager index: " + index);
    }

    private void checkButton() {
        if (index <= 0) {
            operateBtnPrePage.setVisibility(View.INVISIBLE);
            operateBtnNextPage.setVisibility(View.VISIBLE);
        } else if (normalOperLogList.size() - index * pageCount <= pageCount) {    //数据总数减每页数当小于每页可显示的数字时既是最后一页
            operateBtnPrePage.setVisibility(View.VISIBLE);
            operateBtnNextPage.setVisibility(View.INVISIBLE);
        } else {
            operateBtnNextPage.setVisibility(View.VISIBLE);
            operateBtnPrePage.setVisibility(View.VISIBLE);
        }
    }

    class NormalLogAdapter extends RecyclerView.Adapter<NormalLogAdapter.ViewHolder> {

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_common_log,
                    parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            int pos = position + index * pageCount;
            CommonLogBean commonLogBean = normalOperLogList.get(pos);
            Long id = commonLogBean.getId();
            long addTime = commonLogBean.getAddTime();
            String userName = commonLogBean.getUserName();
            String content = commonLogBean.getContent();
            holder.commonItemTvId.setText(String.valueOf(id));
            holder.commonItemTvTime.setText(Utils.longTime2String(addTime));
            holder.commonItemTvName.setText(userName);
            holder.commonItemTvContent.setText(content);

            //设置背景色
            if ((pos & 1) == 1) {
                holder.common_item_root.setBackgroundColor(Color.parseColor("#FF3F51B5"));
            } else {
                holder.common_item_root.setBackgroundColor(Color.parseColor("#FF01579B"));
            }
        }

        @Override
        public int getItemCount() {
            int current = index * pageCount;
            return normalOperLogList.size() - current < pageCount ? normalOperLogList.size() - current : pageCount;
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            @BindView(R.id.common_item_tv_id)
            TextView commonItemTvId;
            @BindView(R.id.common_item_tv_name)
            TextView commonItemTvName;
            @BindView(R.id.common_item_tv_time)
            TextView commonItemTvTime;
            @BindView(R.id.common_item_tv_content)
            TextView commonItemTvContent;
            @BindView(R.id.common_item_root)
            LinearLayout common_item_root;

            ViewHolder(View view) {
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
