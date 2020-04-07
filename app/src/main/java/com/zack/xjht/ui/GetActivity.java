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
 * 领枪操作
 */
public class GetActivity extends BaseActivity {
    private static final String TAG = "GetActivity";
    @BindView(R.id.get_rv_task_list)
    RecyclerView getRvTaskList;
    private List<GoTaskBean> taskItems = new ArrayList<>();
    private int index = 0;
    private int pageCount = 10;
    private GoTaskAdapter goTaskAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get);
        ButterKnife.bind(this);

        getRvTaskList.setLayoutManager(new GridLayoutManager(this, 3));
        goTaskAdapter = new GoTaskAdapter(taskItems, index, pageCount);
        getRvTaskList.setAdapter(goTaskAdapter);
        goTaskAdapter.setOnTaskIdListener(new GoTaskAdapter.OnTaskIdListener() {
            @Override
            public void onTaskId(GoTaskBean goTaskBean) {
                Log.i(TAG, "onTaskId taskId: " + goTaskBean.getId());
                Intent intent = new Intent(GetActivity.this, GetListActivity.class);
                intent.putExtra("getTaskId", goTaskBean.getId());
                intent.putExtra("policeId", goTaskBean.getApplyId());
                GetActivity.this.startActivity(intent);
            }
        });
        if (Utils.isNetworkAvailable()) {
            getPoliceTask();
        }

    }

    private void getPoliceTask() {
        HttpClient.getInstance().getPoliceTaskList(this, "out", new HttpListener<String>() {
            @Override
            public void onSucceed(int what, Response<String> response) throws JSONException {
                Log.i(TAG, "onSucceed getPoliceTask response: " + response.get());
                try {
                    if (!TextUtils.isEmpty(response.get())) {
                        taskItems = JSON.parseArray(response.get(), GoTaskBean.class);
                        if (!taskItems.isEmpty()) {
                            goTaskAdapter.setList(taskItems);
                        } else {
                            showDialogAndFinish("获取任务数据为空");
                        }
                    } else {
                        showDialogAndFinish("获取领枪任务失败！");
                    }
                } catch (Exception e) {
                    showDialogAndFinish("获取领枪任务出现错误！");
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailed(int what, Response<String> response) {
                showDialogAndFinish("网络错误，获取领枪任务失败！");
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        //重新获取数据
        goTaskAdapter.setSelectPos(-1);
        getPoliceTask();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

}
