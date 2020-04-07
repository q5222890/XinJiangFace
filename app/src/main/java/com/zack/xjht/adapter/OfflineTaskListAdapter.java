package com.zack.xjht.adapter;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.zack.xjht.R;
import com.zack.xjht.Utils.Utils;
import com.zack.xjht.db.DBManager;
import com.zack.xjht.db.gen.UserBeanDao;
import com.zack.xjht.entity.OfflineTask;
import com.zack.xjht.entity.UserBean;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class OfflineTaskListAdapter extends RecyclerView.Adapter<OfflineTaskListAdapter.OfflineTaskListViewHolder> {

    private List<OfflineTask> list;
    private OnTaskSelectListener onTaskSelectListener;
    private int selectPos =0;


    public interface OnTaskSelectListener {
        void onTaskSelect(OfflineTask offlineTask);
    }

    public OfflineTaskListAdapter(List<OfflineTask> list, OnTaskSelectListener onTaskSelectListener) {
        this.list = list;
        this.onTaskSelectListener = onTaskSelectListener;
    }

    @NonNull
    @Override
    public OfflineTaskListViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(
                R.layout.offline_task_list_item, viewGroup,false);
        return new OfflineTaskListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OfflineTaskListViewHolder holder, final int position) {
        final OfflineTask offlineTask = list.get(position);
        Long id = offlineTask.getId();
        long startTime = offlineTask.getStartTime();
        Long applyId = offlineTask.getApplyId();
        Long approveId = offlineTask.getApproveId();

        holder.offlineTaskListTvNo.setText("id："+ id);
        holder.offlineTaskListTvStartTime.setText("开始时间：\n"+Utils.longTime2String(startTime));
        UserBeanDao userBeanDao = DBManager.getInstance().getUserBeanDao();
        UserBean apply = userBeanDao.queryBuilder().where(UserBeanDao.Properties.UId.eq(applyId)).unique();
        UserBean approve = userBeanDao.queryBuilder().where(UserBeanDao.Properties.UId.eq(approveId)).unique();
        if(apply !=null){
            holder.offlineTaskListTvApply.setText("申请人："+apply.getUserName());
        }
        if(approve !=null){
            holder.offlineTaskListTvApply.setText("审批人："+approve.getUserName());
        }

        holder.offlineTaskListRoot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectPos = position;
                notifyDataSetChanged();
            }
        });

        if(selectPos ==position){
            onTaskSelectListener.onTaskSelect(offlineTask);
            holder.offlineTaskListRoot.setBackgroundColor(Color.parseColor("#FF00897B"));
        }else{
            holder.offlineTaskListRoot.setBackgroundColor(Color.parseColor("#FF388E3C"));
        }

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class OfflineTaskListViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.offline_task_list_tv_no)
        TextView offlineTaskListTvNo;
        @BindView(R.id.offline_task_list_tv_startTime)
        TextView offlineTaskListTvStartTime;
        @BindView(R.id.offline_task_list_tv_apply)
        TextView offlineTaskListTvApply;
        @BindView(R.id.offline_task_list_tv_aprove)
        TextView offlineTaskListTvAprove;
        @BindView(R.id.offline_task_list_root)
        ConstraintLayout offlineTaskListRoot;

        public OfflineTaskListViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
