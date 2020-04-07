package com.zack.xjht.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zack.xjht.R;
import com.zack.xjht.entity.KeepTaskBean;
import com.zack.xjht.ui.KeepActivity;
import com.zack.xjht.ui.KeepGetListActivity;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 保养任务
 */
public class KeepGetAdapter extends RecyclerView.Adapter<KeepGetAdapter.KeepGetViewHolder> {
    private static final String TAG = "KeepGetAdapter";
    private List<KeepTaskBean> keepTaskList;
    private List<KeepTaskBean> checkedList;
    private int index;
    private int pageCount;
    private Context context;
    private String flag;
    private int selectPos = 0;

    public KeepGetAdapter(List<KeepTaskBean> keepList, int index, int pageCount, String flag) {
        this.keepTaskList = keepList;
        this.index = index;
        this.pageCount = pageCount;
        checkedList = new ArrayList<>();
        this.flag = flag;
    }

    public List<KeepTaskBean> getCheckedList() {
        return checkedList;
    }

    public void setIndex(int index) {
        this.index = index;
        notifyDataSetChanged();
    }

    @Override
    public KeepGetViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(
                R.layout.instore_item_list, parent, false);
        return new KeepGetViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final KeepGetViewHolder holder, int position) {
        final int pos = position + index * pageCount;
        final KeepTaskBean keepTaskBean = keepTaskList.get(pos);
//        Log.i(TAG, "onBindViewHolder taskItemsBean: " + JSON.toJSONString(keepTaskBean));
        String applyName = keepTaskBean.getUserName();
        String startTime = keepTaskBean.getStartTime();
        String endTime = keepTaskBean.getEndTime();
        final String keepTaskId = keepTaskBean.getId();
        holder.taskItemApplyName.setText("申请人：" + applyName);
        holder.taskItemStartTime.setText("开始时间：" + startTime);
        holder.taskItemEndTime.setText("结束时间：" + endTime);
        String remark = keepTaskBean.getRemark();
        if (!TextUtils.isEmpty(remark)) {
            holder.taskItemEndRemark.setText("备注：" + remark);
        }

        holder.taskItemRoot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, KeepGetListActivity.class);
                intent.putExtra("keepTaskId", keepTaskId);
                intent.putExtra("flag", flag);
                context.startActivity(intent);

                //杀掉页面
//                if (context instanceof KeepActivity) {
//                    KeepActivity activity = (KeepActivity) context;
//                    activity.finish();
//                }
            }
        });
    }

    @Override
    public int getItemCount() {
        int current = index * pageCount;
        return keepTaskList.size() - current < pageCount ? keepTaskList.size() - current : pageCount;
    }

    public void setData(List<KeepTaskBean> keepTaskList) {
        this.keepTaskList =keepTaskList;
        notifyDataSetChanged();
    }

    static class KeepGetViewHolder extends RecyclerView.ViewHolder {
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

        KeepGetViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }
}
