package com.zack.xjht.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.zack.xjht.R;
import com.zack.xjht.entity.UrgentOutBean;
import com.zack.xjht.entity.UrgentTaskBean;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 紧急领枪任务
 */
public class UrgentTaskAdapter extends RecyclerView.Adapter<UrgentTaskAdapter.StoreItemViewHolder> {
    private static final String TAG = "UrgentTaskAdapter";

    private List<UrgentOutBean> list;
    private int index;
    private int pageCount;
    private Context context;
    private int selectPos =0;

    public UrgentTaskAdapter(List<UrgentOutBean> list, int index, int pageCount) {
        this.list = list;
        this.index = index;
        this.pageCount = pageCount;
    }

    public void setList(List<UrgentOutBean> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    @Override
    public StoreItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(
                R.layout.urgent_back_item_list, parent, false);
        return new StoreItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final StoreItemViewHolder holder, int position) {
        final int pos = position + index * pageCount;
        Log.i(TAG, "onBindViewHolder pos: " + pos + " position: " + position + "  index: " + index);
        UrgentOutBean urgentTaskBean = list.get(pos);
//        Log.i(TAG, "onBindViewHolder taskItemsBean: " + JSON.toJSONString(urgentTaskBean));
        String applyName = urgentTaskBean.getApplyName();//申请人
        String approvalName = urgentTaskBean.getApprovalName();//审批人
        String outTime = urgentTaskBean.getOutTime();//出库时间
        final long urgentTaskId = urgentTaskBean.getTId(); //任务ID
        holder.taskItemApplyName.setText("申请人：" + applyName);
        holder.taskItemStartTime.setText("审批人：" + approvalName);
        holder.taskItemEndTime.setText("领出时间：\n" + outTime);

        holder.taskItemRoot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //获取任务详情数据然后更新适配器
                Log.i(TAG, "onClick: ");
//                onTaskIdListener.onTaskId(urgentTaskId);
                selectPos = pos;
                notifyDataSetChanged();
            }
        });

        if(selectPos ==pos){
            onTaskIdListener.onTaskId(urgentTaskId);
            holder.taskItemRoot.setBackgroundColor(context.getResources().getColor(R.color.colorPrimaryDark));
        }else{
            holder.taskItemRoot.setBackgroundResource(R.drawable.translucent_bg);
        }
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
        @BindView(R.id.task_item_root)
        LinearLayout taskItemRoot;

        StoreItemViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

   private OnTaskIdListener onTaskIdListener;

    public void setOnTaskIdListener(OnTaskIdListener onTaskIdListener) {
        this.onTaskIdListener = onTaskIdListener;
    }

    public interface OnTaskIdListener {
        void onTaskId(Long taskId);
    }

}
