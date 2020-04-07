package com.zack.xjht.ui;

import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.yanzhenjie.nohttp.rest.Response;
import com.zack.xjht.R;
import com.zack.xjht.Utils.Utils;
import com.zack.xjht.adapter.ScrapTaskAdapter;
import com.zack.xjht.entity.ScrapTaskBean;
import com.zack.xjht.entity.UserBean;
import com.zack.xjht.http.HttpClient;
import com.zack.xjht.http.HttpListener;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 报废任务
 */
public class ScrapActivity extends BaseActivity {
    private static final String TAG = ScrapActivity.class.getSimpleName();
    @BindView(R.id.scrap_tv_tittle)
    TextView scrapTvTittle;
    @BindView(R.id.scrap_recycler_view)
    RecyclerView scrapRecyclerView;
    @BindView(R.id.scrap_ll_title)
    LinearLayout scrapLlTitle;
    @BindView(R.id.scrap_btn_pre_page)
    Button scrapBtnPrePage;
    @BindView(R.id.scrap_tv_cur_page)
    TextView scrapTvCurPage;
    @BindView(R.id.scrap_btn_next_page)
    Button scrapBtnNextPage;
    @BindView(R.id.scrap_ll_bottom)
    LinearLayout scrapLlBottom;

    private UserBean leader;
    private UserBean manage;
    private List<ScrapTaskBean> scrapTaskBeanList = new ArrayList<>();
    private int index = 0;
    private int pageCount = 9;
    private ScrapTaskAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_scrap);
        ButterKnife.bind(this);

        scrapBtnPrePage.setVisibility(View.INVISIBLE);
        scrapBtnNextPage.setVisibility(View.INVISIBLE);


//        String firstPoliceInfo = getIntent().getStringExtra("firstPoliceInfo");//管理员id
//        String secondPoliceInfo = getIntent().getStringExtra("secondPoliceInfo");//领导id
//        if (!TextUtils.isEmpty(firstPoliceInfo)) {
//            manage = JSON.parseObject(firstPoliceInfo, UserBean.class);
//        }
//        if (!TextUtils.isEmpty(secondPoliceInfo)) {
//            leader = JSON.parseObject(secondPoliceInfo, UserBean.class);
//        }

        GridLayoutManager glm = new GridLayoutManager(this, 3);
        scrapRecyclerView.setLayoutManager(glm);
        adapter = new ScrapTaskAdapter(this, scrapTaskBeanList, index, pageCount);
        scrapRecyclerView.setAdapter(adapter);

        if(Utils.isNetworkAvailable()){
            getScarpTask();
        }

    }

    /**
     * 获取枪支报废任务
     */
    private void getScarpTask() {
        HttpClient.getInstance().getScrapTaskList(this, new HttpListener<String>() {
            @Override
            public void onSucceed(int what, Response<String> response) throws JSONException {
                Log.i(TAG, "getScrapTask onSucceed response: " + response.get());
                try {
                    if (!TextUtils.isEmpty(response.get())) {
                        List<ScrapTaskBean> scrapTaskBeans = JSON.parseArray(response.get(), ScrapTaskBean.class);
                        if (!scrapTaskBeans.isEmpty()) {
                            scrapTaskBeanList.clear();
                            scrapTaskBeanList.addAll(scrapTaskBeans);
                            adapter.notifyDataSetChanged();
                            initPreNextBtn();
                        } else {
                            showDialogAndFinish("获取任务为空");
                        }
                    } else {
                        showDialogAndFinish("获取报废任务失败");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailed(int what, Response<String> response) {

            }
        });
    }

    private void initPreNextBtn() {
        if (scrapTaskBeanList.isEmpty()) {
            scrapTvCurPage.setText(index + 1 + "/1");
        } else {
            if (scrapTaskBeanList.size() <= pageCount) {
                scrapBtnNextPage.setVisibility(View.INVISIBLE);
            } else {
                scrapBtnNextPage.setVisibility(View.VISIBLE);
            }
            scrapTvCurPage.setText(index + 1 + "/" + (int) Math.ceil((double) scrapTaskBeanList.size() / pageCount));
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @OnClick({R.id.scrap_btn_pre_page, R.id.scrap_btn_next_page})
    public void onViewClicked(View view) {
        super.onViewClicked(view);
        switch (view.getId()) {
            case R.id.scrap_btn_pre_page://上一页
                prePager();
                break;
            case R.id.scrap_btn_next_page://下一页
                nexPager();
                break;
        }
    }

    private void prePager() {
        index--;
//        adapter.setIndex(index);
        scrapTvCurPage.setText(index + 1 + "/" + (int) Math.ceil((double) scrapTaskBeanList.size() / pageCount));
        //隐藏上一个或下一个按钮
        checkButton();
        Log.i(TAG, "prePager index: " + index);
    }

    private void nexPager() {
        index++;
//        adapter.setIndex(index);
        scrapTvCurPage.setText(index + 1 + "/" + (int) Math.ceil((double) scrapTaskBeanList.size() / pageCount));
        //隐藏上一个或下一个按钮
        checkButton();
        Log.i(TAG, "nexPager index: " + index);
    }

    private void checkButton() {
        if (index <= 0) {
            scrapBtnPrePage.setVisibility(View.INVISIBLE);
            scrapBtnNextPage.setVisibility(View.VISIBLE);
        } else if (scrapTaskBeanList.size() - index * pageCount <= pageCount) {    //数据总数减每页数当小于每页可显示的数字时既是最后一页
            scrapBtnPrePage.setVisibility(View.VISIBLE);
            scrapBtnNextPage.setVisibility(View.INVISIBLE);
        } else {
            scrapBtnNextPage.setVisibility(View.VISIBLE);
            scrapBtnPrePage.setVisibility(View.VISIBLE);
        }
    }

//    private Dialog dialog;
//    private void showDialog(String msg) {
//        if (dialog != null) {
//            if (!dialog.isShowing()) {
//                dialog.show();
//            }
//            DialogUtils.setTipText(msg);
//            Log.i(TAG, "dialog is not null ");
//        } else { //dialog为null
//            dialog = DialogUtils.creatTipDialog(this, "提示", msg,
//                    new View.OnClickListener() {
//                        @Override
//                        public void onClick(View v) {
//                            //获取最新的数据 并刷新适配器
//                            dialog.dismiss();
//                            finish();
//                        }
//                    });
//            if (!dialog.isShowing()) {
//                dialog.show();
//            }
//            Log.i(TAG, "dialog is null");
//        }
//    }

}
