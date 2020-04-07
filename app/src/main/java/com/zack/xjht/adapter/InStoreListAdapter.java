package com.zack.xjht.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zack.xjht.Constants;
import com.zack.xjht.R;
import com.zack.xjht.entity.InStoreListBean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

public class InStoreListAdapter extends RecyclerView.Adapter<InStoreListAdapter.TaskListViewHolder> {

    private List<InStoreListBean> list;
    private int index;
    private int pageCount;
    private List<InStoreListBean> selectedList;
    private Map<Integer, Boolean> checkStatus;
    private boolean isDisableAll =false;

    public InStoreListAdapter(List<InStoreListBean> list, int index, int pageCount) {
        this.list = list;
        this.index = index;
        this.pageCount = pageCount;
        selectedList =new ArrayList<>();
        checkStatus =new HashMap<>();
    }

    public List<InStoreListBean> getSelectedList() {
        return selectedList;
    }

    public void setList(List<InStoreListBean> list) {
        this.list = list;
        for (int i = 0; i < list.size(); i++) {
            checkStatus.put(i,false);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public InStoreListAdapter.TaskListViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(
                R.layout.store_item_list, viewGroup, false);
        return new TaskListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull InStoreListAdapter.TaskListViewHolder holder, int position) {
        final int pos = position + index * pageCount;
        holder.taskItemCbSelect.setOnCheckedChangeListener(null);
        if(checkStatus.containsKey(pos)){
            holder.taskItemCbSelect.setChecked(checkStatus.get(pos));
        }else{
            Log.i("InStoreListAdapter", "onBindViewHolder checkStatus is not contains key position: "+pos);
        }

        if(isDisableAll){
           holder.taskItemCbSelect.setEnabled(false);
           holder.taskItemCbSelect.setClickable(false);
        }else{
            holder.taskItemCbSelect.setEnabled(true);
            holder.taskItemCbSelect.setClickable(true);
        }
        final InStoreListBean inStoreListBean = list.get(pos);
        int locationNo = inStoreListBean.getLocationNo();
        String locationType = inStoreListBean.getLocationType();
        String objectName = inStoreListBean.getObjectName();
        int objectNumber = inStoreListBean.getObjectNumber();
        String gunNo = inStoreListBean.getGunNo();
        String objectType = inStoreListBean.getObjectType();
        holder.taskItemTvSubcabNo.setText(String.valueOf(locationNo));
        holder.taskItemTvGunType.setText(objectName);
        holder.taskItemTvGunNo.setText(gunNo);
        holder.taskItemTvAmmoNum.setText(String.valueOf(objectNumber));
        holder.taskItemTvSubcabType.setText(objectType);

        if(locationType.equals(Constants.TYPE_AMMO)){
            holder.taskItemTvGunNo.setVisibility(View.GONE);
        }
        holder.taskItemCbSelect.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                checkStatus.put(pos, isChecked);
                if(isChecked){ //被选中
                    selectedList.add(inStoreListBean);
                }else{//未被选中
                    selectedList.remove(inStoreListBean);
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
        isDisableAll =true;
        notifyDataSetChanged();
    }

    static class TaskListViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.task_item_tv_subcab_no)
        TextView taskItemTvSubcabNo;
        @BindView(R.id.task_item_tv_subcab_type)
        TextView taskItemTvSubcabType;
        @BindView(R.id.task_item_tv_gun_type)
        TextView taskItemTvGunType;
        @BindView(R.id.task_item_tv_gun_no)
        TextView taskItemTvGunNo;
        @BindView(R.id.task_item_tv_ammo_num)
        TextView taskItemTvAmmoNum;
        @BindView(R.id.task_item_cb_select)
        CheckBox taskItemCbSelect;
        @BindView(R.id.task_item_btn_get_back)
        Button taskItemBtnGetBack;
        @BindView(R.id.task_item_root)
        LinearLayout taskItemRoot;

        TaskListViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }
}
