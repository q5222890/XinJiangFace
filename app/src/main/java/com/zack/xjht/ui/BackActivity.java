package com.zack.xjht.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.yanzhenjie.nohttp.rest.Response;
import com.zack.xjht.R;
import com.zack.xjht.Utils.ToastUtil;
import com.zack.xjht.Utils.Utils;
import com.zack.xjht.adapter.GoTaskAdapter;
import com.zack.xjht.entity.GoTaskBean;
import com.zack.xjht.http.HttpClient;
import com.zack.xjht.http.HttpListener;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 还枪操作
 */
public class BackActivity extends BaseActivity {
    private static final String TAG = "BackActivity";
    @BindView(R.id.back_rv_task_list)
    RecyclerView backRvTaskList;
    private List<GoTaskBean> taskItems = new ArrayList<>();
    private int index = 0;
    private int pageCount = 10;
    private GoTaskAdapter goTaskAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_back);
        ButterKnife.bind(this);

        backRvTaskList.setLayoutManager(new GridLayoutManager(this, 3));
        goTaskAdapter = new GoTaskAdapter(taskItems, index, pageCount);
        backRvTaskList.setAdapter(goTaskAdapter);
        goTaskAdapter.setOnTaskIdListener(new GoTaskAdapter.OnTaskIdListener() {
            @Override
            public void onTaskId(GoTaskBean goTaskBean) {
                Log.i(TAG, "onTaskId: " + goTaskBean.getId());
//                if (Utils.isNetworkAvailable()) {
//                    getTaskInfo(goTaskBean.getId());
//                }
                Intent intent = new Intent(BackActivity.this, BackListActivity.class);
                intent.putExtra("backTaskId", goTaskBean.getId());
                intent.putExtra("policeId", goTaskBean.getApplyId());
                BackActivity.this.startActivity(intent);
            }
        });
        if (Utils.isNetworkAvailable()) {
            getPoliceTask();
        }
    }

    /**
     * 获取出警任务数据
     */
    private void getPoliceTask() {
        HttpClient.getInstance().getPoliceTaskList(this, "in", new HttpListener<String>() {
            @Override
            public void onSucceed(int what, Response<String> response) throws JSONException {
                Log.i(TAG, "onSucceed getPoliceTask response: " + response.get());
                try {
                    if (!TextUtils.isEmpty(response.get())) {
                        taskItems = JSON.parseArray(response.get(), GoTaskBean.class);
                        if (!taskItems.isEmpty()) {
                            goTaskAdapter.setList(taskItems);
                            goTaskAdapter.notifyDataSetChanged();
                        } else {
                            showDialogAndFinish("获取任务数据为空");
                        }
                    } else {
                        showDialogAndFinish("获取领枪任务失败！");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    showDialogAndFinish("获取领枪任务出现错误！");
                }
            }

            @Override
            public void onFailed(int what, Response<String> response) {
                showDialogAndFinish("获取领枪任务失败！");
            }
        });
    }

    private void postBackData(String jsonBody) {
        HttpClient.getInstance().postPoliceTaskData(this, jsonBody, new HttpListener<String>() {
            @Override
            public void onSucceed(int what, Response<String> response) throws JSONException {
                Log.i(TAG, "onSucceed postBackData response: " + response.get());
                try {
                    if (!TextUtils.isEmpty(response.get())) {
                        if (response.get().equals("success")) {
                            finish();
                        } else {
                            ToastUtil.showShort("提交数据失败！");
                        }
                    } else {
                        ToastUtil.showShort("提交数据失败");
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

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //重新获取数据
        goTaskAdapter.setSelectPos(-1);
        getPoliceTask();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
