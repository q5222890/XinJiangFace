package com.zack.xjht.ui.fragment;


import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.yanzhenjie.nohttp.rest.Response;
import com.zack.xjht.R;
import com.zack.xjht.Utils.DialogUtils;
import com.zack.xjht.Utils.Utils;
import com.zack.xjht.adapter.KeepGetAdapter;
import com.zack.xjht.entity.KeepTaskBean;
import com.zack.xjht.http.HttpClient;
import com.zack.xjht.http.HttpListener;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * 保养归还枪支
 */
public class KeepBackFragment extends Fragment {
    private static final String TAG = "KeepBackFragment";
    @BindView(R.id.keep_back_ll_title)
    LinearLayout keepBackLlTitle;
    @BindView(R.id.keep_back_recycler_view)
    RecyclerView keepBackRecyclerView;
    @BindView(R.id.keep_back_btn_unlock)
    Button keepBackBtnUnlock;
    @BindView(R.id.keep_back_btn_confirm)
    Button keepBackBtnConfirm;
    @BindView(R.id.keep_back_ll_bottom)
    LinearLayout keepBackLlBottom;
    Unbinder unbinder;
    @BindView(R.id.keep_back_btn_pre_page)
    Button keepBackBtnPrePage;
    @BindView(R.id.keep_back_tv_cur_page)
    TextView keepBackTvCurPage;
    @BindView(R.id.keep_back_btn_next_page)
    Button keepBackBtnNextPage;
    @BindView(R.id.keep_back_tv_msg)
    TextView keepBackTvMsg;
    @BindView(R.id.keep_back_btn_open_cab)
    Button keepBackBtnOpenCab;

    private List<KeepTaskBean> keepTaskList = new ArrayList<>();
    private KeepGetAdapter adapter;
    private int index = 0;
    private int pageCount = 7;

    public KeepBackFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_keep_back, container, false);
        unbinder = ButterKnife.bind(this, view);
        initView();
        return view;
    }

    private void initView() {
        keepBackBtnPrePage.setVisibility(View.INVISIBLE);
        keepBackBtnNextPage.setVisibility(View.INVISIBLE);

        keepBackRecyclerView.setVisibility(View.INVISIBLE);
        keepBackTvMsg.setVisibility(View.VISIBLE);

        GridLayoutManager glm = new GridLayoutManager(getContext(), 3);
        keepBackRecyclerView.setLayoutManager(glm);
        adapter = new KeepGetAdapter(keepTaskList, index, pageCount, "preservationGun");
        keepBackRecyclerView.setAdapter(adapter);


    }

    private void getKeepTask() {
        HttpClient.getInstance().getKeepTaskList(getContext(), "in", new HttpListener<String>() {
            @Override
            public void onSucceed(int what, Response<String> response) throws JSONException {
                Log.i(TAG, "onSucceed getKeepTaskList response: " + response.get());
                try {
                    if (!TextUtils.isEmpty(response.get())) {
                        keepTaskList = JSON.parseArray(response.get(), KeepTaskBean.class);
                        if (!keepTaskList.isEmpty()) {
                            keepBackTvMsg.setVisibility(View.INVISIBLE);
                            keepBackRecyclerView.setVisibility(View.VISIBLE);
                            adapter.setData(keepTaskList);
                            initPreNextBtn();
                        } else {
                            if (keepBackTvMsg != null) {
                                keepBackTvMsg.setText("获取保养任务为空！");
                            }
                        }
                    } else {
                        if (keepBackTvMsg != null) {
                            keepBackTvMsg.setText("获取数据为空！");
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    if (keepBackTvMsg != null) {
                        keepBackTvMsg.setText("获取保养任务出错！");
                    }
                }
            }

            @Override
            public void onFailed(int what, Response<String> response) {
                if (keepBackTvMsg != null) {
                    keepBackTvMsg.setText("网络错误！获取保养任务失败！");
                }
            }
        });
    }

    private void initPreNextBtn() {
        if (keepTaskList.isEmpty()) {
            keepBackTvCurPage.setText(index + 1 + "/1");
        } else {
            if (keepTaskList.size() <= pageCount) {
                keepBackBtnNextPage.setVisibility(View.INVISIBLE);
            } else {
                keepBackBtnNextPage.setVisibility(View.VISIBLE);
            }
            keepBackTvCurPage.setText(index + 1 + "/" + (int) Math.ceil((double) keepTaskList.size() / pageCount));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (Utils.isNetworkAvailable()) {
            getKeepTask();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (unbinder != null) {
            unbinder.unbind();
        }
    }

    @OnClick({R.id.keep_back_btn_unlock, R.id.keep_back_btn_confirm,
            R.id.keep_back_btn_pre_page, R.id.keep_back_btn_next_page})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.keep_back_btn_unlock: //开锁
                break;
            case R.id.keep_back_btn_confirm: //确认
//                Objects.requireNonNull(getActivity()).finish();
                break;
            case R.id.keep_back_btn_pre_page: //上一页
                prePager();
                break;
            case R.id.keep_back_btn_next_page: //下一页
                nexPager();
                break;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    private void prePager() {
        index--;
        adapter.setIndex(index);
        keepBackTvCurPage.setText(index + 1 + "/" + (int) Math.ceil((double) keepTaskList.size() / pageCount));
        //隐藏上一个或下一个按钮
        checkButton();
//        Log.i(TAG, "prePager index: " + index);
    }

    private void nexPager() {
        index++;
        adapter.setIndex(index);
        keepBackTvCurPage.setText(index + 1 + "/" + (int) Math.ceil((double) keepTaskList.size() / pageCount));
        //隐藏上一个或下一个按钮
        checkButton();
//        Log.i(TAG, "nexPager index: " + index);
    }

    private void checkButton() {
        if (index <= 0) {
            keepBackBtnPrePage.setVisibility(View.INVISIBLE);
            keepBackBtnNextPage.setVisibility(View.VISIBLE);
        } else if (keepTaskList.size() - index * pageCount <= pageCount) {    //数据总数减每页数当小于每页可显示的数字时既是最后一页
            keepBackBtnPrePage.setVisibility(View.VISIBLE);
            keepBackBtnNextPage.setVisibility(View.INVISIBLE);
        } else {
            keepBackBtnNextPage.setVisibility(View.VISIBLE);
            keepBackBtnPrePage.setVisibility(View.VISIBLE);
        }
    }


    private Dialog dialog;

    private void showDialog(String msg) {
        if (dialog != null) {
            if (!dialog.isShowing()) {
                dialog.show();
            }
            DialogUtils.setTipText(msg);
            Log.i(TAG, "dialog is not null ");
        } else { //dialog为null
            dialog = DialogUtils.creatTipDialog(getContext(), "提示", msg,
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //获取最新的数据 并刷新适配器
                            dialog.dismiss();
//                            Objects.requireNonNull(getActivity()).finish();
                        }
                    });
            if (!dialog.isShowing()) {
                dialog.show();
            }
            Log.i(TAG, "dialog is null");
        }
    }
}
