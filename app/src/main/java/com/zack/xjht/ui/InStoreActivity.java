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
import com.zack.xjht.adapter.InStoreTaskAdapter;
import com.zack.xjht.entity.SubCabBean;
import com.zack.xjht.entity.TaskBean;
import com.zack.xjht.http.HttpClient;
import com.zack.xjht.http.HttpListener;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 枪弹入库
 */
public class InStoreActivity extends BaseActivity {
    private static final String TAG = "InStoreActivity";
    @BindView(R.id.store_in_recycler_view)
    RecyclerView storeInRecyclerView;
    @BindView(R.id.store_in_ll)
    LinearLayout storeInLl;
    @BindView(R.id.in_store_btn_pre_page)
    Button inStoreBtnPrePage;
    @BindView(R.id.in_store_tv_cur_page)
    TextView inStoreTvCurPage;
    @BindView(R.id.in_store_btn_next_page)
    Button inStoreBtnNextPage;
    private List<SubCabBean> storeBeanList;
    private InStoreTaskAdapter inStoreAdapter;
    private int index = 0;
    private int pageCount = 6;
    private  List<TaskBean> inStoreTaskList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_in_store);
        ButterKnife.bind(this);
        initView();
    }

    private void initView() {
        inStoreTaskList =new ArrayList<>();
        inStoreBtnPrePage.setVisibility(View.INVISIBLE);
        inStoreBtnNextPage.setVisibility(View.INVISIBLE);
        storeBeanList = new ArrayList<>();
        GridLayoutManager glm = new GridLayoutManager(this, 3);
        storeInRecyclerView.setLayoutManager(glm);
        inStoreAdapter =new InStoreTaskAdapter(this, inStoreTaskList, index, pageCount);
        storeInRecyclerView.setAdapter(inStoreAdapter);
        if(Utils.isNetworkAvailable()){
            getInStoreList();
        }
    }

    private void getInStoreList() {
        HttpClient.getInstance().getInStoreTaskList(this, new HttpListener<String>() {
            @Override
            public void onSucceed(int what, Response<String> response) throws JSONException {
                try {
                    Log.i(TAG, "onSucceed getInStoreList response: "+response.get());
                    if(!TextUtils.isEmpty(response.get())){
                        List<TaskBean> inStoreTaskBeans = JSON.parseArray(response.get(), TaskBean.class);
                        if(!inStoreTaskBeans.isEmpty()){
                            inStoreTaskList.clear();
                            inStoreTaskList.addAll(inStoreTaskBeans);
                            inStoreAdapter.notifyDataSetChanged();
                            initPreNextBtn();
                        }else{
                            showDialogAndFinish("获取入库任务为空！");
                        }
                    }else{
                        showDialogAndFinish("获取数据为空!");
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
        if (storeBeanList.isEmpty()) {
            inStoreTvCurPage.setText(index + 1 + "/1");
        } else {
            if (storeBeanList.size() <= pageCount) {
                inStoreBtnNextPage.setVisibility(View.INVISIBLE);
            } else {
                inStoreBtnNextPage.setVisibility(View.VISIBLE);
            }
            inStoreTvCurPage.setText(index + 1 + "/" + (int) Math.ceil((double) storeBeanList.size() / pageCount));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @OnClick({ R.id.in_store_btn_pre_page, R.id.in_store_btn_next_page})
    public void onViewClicked(View view) {
        super.onViewClicked(view);
        switch (view.getId()) {
            case R.id.in_store_btn_pre_page: //上一页
                prePager();
                break;
            case R.id.in_store_btn_next_page: //下一页
                nexPager();
                break;
        }
    }

    private void prePager() {
        index--;
        inStoreAdapter.setIndex(index);
        inStoreTvCurPage.setText(index + 1 + "/" + (int) Math.ceil((double) storeBeanList.size() / pageCount));
        //隐藏上一个或下一个按钮
        checkButton();
//        Log.i(TAG, "prePager index: " + index);
    }

    private void nexPager() {
        index++;
        inStoreAdapter.setIndex(index);
        inStoreTvCurPage.setText(index + 1 + "/" + (int) Math.ceil((double) storeBeanList.size() / pageCount));
        //隐藏上一个或下一个按钮
        checkButton();
//        Log.i(TAG, "nexPager index: " + index);
    }

    private void checkButton() {
        if (index <= 0) {
            inStoreBtnPrePage.setVisibility(View.INVISIBLE);
            inStoreBtnNextPage.setVisibility(View.VISIBLE);
        } else if (storeBeanList.size() - index * pageCount <= pageCount) {    //数据总数减每页数当小于每页可显示的数字时既是最后一页
            inStoreBtnPrePage.setVisibility(View.VISIBLE);
            inStoreBtnNextPage.setVisibility(View.INVISIBLE);
        } else {
            inStoreBtnNextPage.setVisibility(View.VISIBLE);
            inStoreBtnPrePage.setVisibility(View.VISIBLE);
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
