package com.zack.xjht.adapter;

import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.zack.xjht.Constants;
import com.zack.xjht.R;
import com.zack.xjht.entity.GoTaskInfoBean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

public class TaskItemAdapter extends RecyclerView.Adapter<TaskItemAdapter.TaskItemViewHolder> {
    private static final String TAG = "TaskItemAdapter";

    private List<GoTaskInfoBean> list;
    private List<GoTaskInfoBean> checkedList;
    private int index;
    private int pageCount;
    private Map<Integer, Boolean> checkStatus;
    private String flag;
    private boolean isDisableAll =false;

    public TaskItemAdapter(List<GoTaskInfoBean> list, int index, int pageCount, String flag) {
        this.list = list;
        this.index =index;
        this.pageCount =pageCount;
        checkedList = new ArrayList<>();
        checkStatus =new HashMap<>();
        this.flag =flag;
    }

    public List<GoTaskInfoBean> getCheckedList() {
        return checkedList;
    }

    public void setList(List<GoTaskInfoBean> list) {
        this.list = list;
        for (int i = 0; i < list.size(); i++) {
            checkStatus.put(i,false);
        }
        notifyDataSetChanged();
    }

    @Override
    public TaskItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.task_item_list, parent, false);
        return new TaskItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final TaskItemViewHolder holder, int position) {
        final int pos = position + index * pageCount;
        Log.i(TAG, "onBindViewHolder pos: "+pos +" position: "+position+"  index: "+index);
        holder.taskItemCbGetBack.setOnCheckedChangeListener(null);
        if(checkStatus.containsKey(pos)){
            holder.taskItemCbGetBack.setChecked(checkStatus.get(pos));
        }else{
            Log.i(TAG, "onBindViewHolder checkStatus is not containskey position: "+pos);
        }

        if(isDisableAll){
           holder.taskItemCbGetBack.setEnabled(false);
           holder.taskItemCbGetBack.setClickable(false);
        }else{
            holder.taskItemCbGetBack.setClickable(true);
            holder.taskItemCbGetBack.setEnabled(true);
        }
        final GoTaskInfoBean goTaskInfoBean = list.get(pos);
        Log.i(TAG, "onBindViewHolder taskItemsBean: " + JSON.toJSONString(goTaskInfoBean));
        String objectName = goTaskInfoBean.getObjectName();
        int posNo = goTaskInfoBean.getLocationNo();
        int objectNumber = goTaskInfoBean.getObjectNumber();
        String gunNo = goTaskInfoBean.getGunNo();
        int returnNumber = goTaskInfoBean.getReturnNumber();
        String locationType = goTaskInfoBean.getLocationType();
        String policeName = goTaskInfoBean.getPoliceName();
        String policeNo = goTaskInfoBean.getPoliceNo();
        if(flag.equals("get")){
            holder.taskItemTvBackNum.setVisibility(View.GONE);
            if(locationType.equals(Constants.TYPE_AMMO)){
               holder.taskItemTvGunNo.setVisibility(View.GONE);
            }
        }else{
            if(locationType.equals(Constants.TYPE_AMMO)){
                holder.taskItemTvGunNo.setVisibility(View.GONE);
                holder.taskItemTvBackNum.setVisibility(View.VISIBLE);
                holder.taskItemTvBackNum.setText(String.valueOf(returnNumber));
            }else{
                holder.taskItemTvBackNum.setVisibility(View.GONE);
            }
        }
        holder.taskItemTvObjectNum.setText(String.valueOf(objectNumber));
        if(!TextUtils.isEmpty(objectName)){
            holder.taskItemTvTypeName.setText(objectName);
        }
        holder.taskItemTvPosNo.setText(String.valueOf(posNo));
        if(!TextUtils.isEmpty(gunNo)){
            holder.taskItemTvGunNo.setText(gunNo);
        }

        holder.taskItemTvPoliceName.setText(policeName);
        holder.taskItemTvPoliceNo.setText(policeNo);

        holder.taskItemCbGetBack.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                checkStatus.put(pos, isChecked);
                if (isChecked) {
                    Log.i(TAG, "onCheckedChanged isChecked: ");
                    checkedList.add(goTaskInfoBean);
                } else {
                    Log.i(TAG, "onCheckedChanged 移除: ");
                    checkedList.remove(goTaskInfoBean);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        int current = index * pageCount;
        return list.size() - current < pageCount ? list.size() - current : pageCount;
    }

    public void setIndex(int index) {
        this.index =index;
        notifyDataSetChanged();
    }

    public void setDisableAll() {
        isDisableAll = true;
        notifyDataSetChanged();
    }

    static class TaskItemViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.task_item_root)
        LinearLayout taskItemTvRoot;
        @BindView(R.id.task_item_tv_type_name)
        TextView taskItemTvTypeName;
        @BindView(R.id.task_item_tv_gun_no)
        TextView taskItemTvGunNo;
        @BindView(R.id.task_item_tv_pos_no)
        TextView taskItemTvPosNo;
        @BindView(R.id.task_item_tv_object_num)
        TextView taskItemTvObjectNum;
        @BindView(R.id.task_item_tv_back_num)
        TextView taskItemTvBackNum;
        @BindView(R.id.task_item_tv_police_name)
        TextView taskItemTvPoliceName;
        @BindView(R.id.task_item_tv_police_no)
        TextView taskItemTvPoliceNo;
        @BindView(R.id.task_item_cb_get_back)
        CheckBox taskItemCbGetBack;

        TaskItemViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }
}
