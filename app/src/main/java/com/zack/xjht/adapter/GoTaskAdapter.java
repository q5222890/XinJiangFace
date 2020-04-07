package com.zack.xjht.adapter;

import android.content.Context;
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
import com.zack.xjht.entity.GoTaskBean;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 出警任务
 */
public class GoTaskAdapter extends RecyclerView.Adapter<GoTaskAdapter.StoreItemViewHolder> {
    private static final String TAG = "GoTaskAdapter";

    private List<GoTaskBean> list;
    private int index;
    private int pageCount;
    private Context context;
    private int selectPos = -1;
    private GoTaskBean selectGoTask;

    public GoTaskAdapter(List<GoTaskBean> list, int index, int pageCount) {
        this.list = list;
        this.index = index;
        this.pageCount = pageCount;
    }

    public void setList(List<GoTaskBean> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    public GoTaskBean getSelectGoTask() {
        return selectGoTask;
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
        GoTaskBean goTaskBean = list.get(pos);
        Log.i(TAG, "onBindViewHolder taskItemsBean: " + JSON.toJSONString(goTaskBean));
        String applyName = goTaskBean.getApply();  //申请人姓名
        String startTime = goTaskBean.getStartTime();  //开始时间
        String endTime = goTaskBean.getEndTime();  //结束时间
        final String goTaskId = goTaskBean.getId();  //任务id
        String remark = goTaskBean.getRemark(); //备注

        holder.taskItemApplyName.setText("申请人：" + applyName);
        holder.taskItemStartTime.setText("开始时间：\n" + startTime);
        holder.taskItemEndTime.setText("结束时间：\n" + endTime);
        if(!TextUtils.isEmpty(remark)){
            holder.taskItemEndRemark.setText("备注："+remark);
        }

        holder.taskItemRoot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //获取任务详情数据然后更新适配器
                selectPos =pos;
                notifyDataSetChanged();
            }
        });

        if(selectPos ==pos){
            selectGoTask =goTaskBean;
            onTaskIdListener.onTaskId(goTaskBean);
            holder.taskItemRoot.setBackgroundColor(context.getResources().getColor(R.color.colorPrimary));
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

    public void setSelectPos(int selectPos) {
        this.selectPos = selectPos;
    }

    static class StoreItemViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.task_item_apply_name)
        TextView taskItemApplyName;
        @BindView(R.id.task_item_start_time)
        TextView taskItemStartTime;
        @BindView(R.id.task_item_end_time)
        TextView taskItemEndTime;
        @BindView(R.id.task_item_remark)
        TextView taskItemEndRemark;
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
        void onTaskId(GoTaskBean goTaskBean);
    }

}
