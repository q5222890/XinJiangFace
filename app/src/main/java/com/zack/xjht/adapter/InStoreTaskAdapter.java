package com.zack.xjht.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.zack.xjht.R;
import com.zack.xjht.entity.TaskBean;
import com.zack.xjht.ui.InStoreActivity;
import com.zack.xjht.ui.InStoreListActivity;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class InStoreTaskAdapter extends RecyclerView.Adapter<InStoreTaskAdapter.StoreItemViewHolder> {
    private static final String TAG = "InStoreTaskAdapter";

    private List<TaskBean> list;
    private int index;
    private int pageCount;
    private Context context;
    private InStoreActivity activity;

    public InStoreTaskAdapter(InStoreActivity activity, List<TaskBean> list, int index, int pageCount) {
        this.activity =activity;
        this.list = list;
        this.index = index;
        this.pageCount = pageCount;
    }

    public void setList(List<TaskBean> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    @Override
    public StoreItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(
                R.layout.instore_item_list, parent, false);
        return new StoreItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final StoreItemViewHolder holder, int position) {
        final int pos = position + index * pageCount;
        Log.i(TAG, "onBindViewHolder pos: " + pos + " position: " + position + "  index: " + index);
        TaskBean storeBean = list.get(pos);
        Log.i(TAG, "onBindViewHolder taskItemsBean: " + JSON.toJSONString(storeBean));
        String applyName = storeBean.getApply();
        String startTime = storeBean.getStartTime();
        String endTime = storeBean.getEndTime();
        final String instoreTaskId = storeBean.getId();
        String remark = storeBean.getRemark();

        holder.taskItemApplyName.setText("申请人：" + applyName);
        holder.taskItemStartTime.setText("开始时间：" + startTime);
        holder.taskItemEndTime.setText("结束时间：" + endTime);
        if(!TextUtils.isEmpty(remark)){
            holder.taskItemRemark.setText("备注："+remark);
        }

        holder.taskItemRoot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, InStoreListActivity.class);
                intent.putExtra("instoreTaskId", instoreTaskId);
                context.startActivity(intent);
                activity.finish();
            }
        });
    }

    @Override
    public int getItemCount() {
        int current = index * pageCount;
        return list.size() - current < pageCount ? list.size() - current : pageCount;
    }

    public void setIndex(int index) {
        this.index = index;
        notifyDataSetChanged();
    }

    static class StoreItemViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.task_item_apply_name)
        TextView taskItemApplyName;
        @BindView(R.id.task_item_start_time)
        TextView taskItemStartTime;
        @BindView(R.id.task_item_end_time)
        TextView taskItemEndTime;
        @BindView(R.id.task_item_remark)
        TextView taskItemRemark;
        @BindView(R.id.task_item_root)
        LinearLayout taskItemRoot;

        StoreItemViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

}
