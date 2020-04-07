package com.zack.xjht.entity;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zack.xjht.Constants;
import com.zack.xjht.R;
import com.zack.xjht.Utils.Utils;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class OperateInfoAdapter extends RecyclerView.Adapter<OperateInfoAdapter.OperateInfoViewHolder> {
    private List<OperLogBean> list;
    private int index;
    private int pageCount;

    public OperateInfoAdapter(List<OperLogBean> list, int index, int pageCount) {
        this.list = list;
        this.index = index;
        this.pageCount = pageCount;
    }

    public void setIndex(int index) {
        this.index = index;
        notifyDataSetChanged();
    }

    public void setList(List<OperLogBean> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    @Override
    public OperateInfoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.operate_info_item, parent, false);
        OperateInfoViewHolder holder = new OperateInfoViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(OperateInfoViewHolder holder, int position) {
        int pos = position + index * pageCount;
        OperLogBean log = list.get(pos);
        holder.operateInfoItemTvId.setText(String.valueOf(log.getId()));
        holder.operateInfoItemTvFirstName.setText(log.getApproveName());
        holder.operateInfoItemTvSecondName.setText(log.getGunManagementName());
        holder.operateInfoItemTvPoliceName.setText(log.getPoliceName());
        String status = log.getStatus();
        if(status.equals("1")){
            holder.operateInfoItemTvStatus.setText("领取");
            holder.operateInfoItemTvDatetime.setText(log.getOutTime());
        }else{
            holder.operateInfoItemTvStatus.setText("归还");
            holder.operateInfoItemTvDatetime.setText(log.getInTime());
        }
        holder.operateInfoItemTvTypeName.setText(log.getTypeName());
        if(log.getType().equals(Constants.TYPE_AMMO)){
            holder.operateInfoItemTvGunNo.setVisibility(View.GONE);
            holder.operateInfoItemTvBulletNum.setVisibility(View.VISIBLE);
            if(status.equals("1")){
                holder.operateInfoItemTvBulletNum.setText(log.getOutCount());
            }else{
                holder.operateInfoItemTvBulletNum.setText(log.getInCount());
            }
        }else{
            holder.operateInfoItemTvGunNo.setVisibility(View.VISIBLE);
            holder.operateInfoItemTvBulletNum.setVisibility(View.GONE);
            holder.operateInfoItemTvGunNo.setText(log.getGunNo());
        }

        //设置背景色
        if ((pos & 1) == 1) {
            holder.operate_info_item_root.setBackgroundColor(Color.parseColor("#FF3F51B5"));
        } else {
            holder.operate_info_item_root.setBackgroundColor(Color.parseColor("#FF01579B"));
        }
    }

    @Override
    public int getItemCount() {
        int current = index * pageCount;
        return list.size() - current < pageCount ? list.size() - current : pageCount;
    }

    class OperateInfoViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.operate_info_item_tv_id)
        TextView operateInfoItemTvId;
        @BindView(R.id.operate_info_item_tv_first_name)
        TextView operateInfoItemTvFirstName;
        @BindView(R.id.operate_info_item_tv_second_name)
        TextView operateInfoItemTvSecondName;
        @BindView(R.id.operate_info_item_tv_police_name)
        TextView operateInfoItemTvPoliceName;
        @BindView(R.id.operate_info_item_tv_status)
        TextView operateInfoItemTvStatus;
        @BindView(R.id.operate_info_item_tv_type_name)
        TextView operateInfoItemTvTypeName;
        @BindView(R.id.operate_info_item_tv_gun_no)
        TextView operateInfoItemTvGunNo;
        @BindView(R.id.operate_info_item_tv_bullet_num)
        TextView operateInfoItemTvBulletNum;
        @BindView(R.id.operate_info_item_tv_datetime)
        TextView operateInfoItemTvDatetime;
        @BindView(R.id.operate_info_item_root)
        LinearLayout operate_info_item_root;

        OperateInfoViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }
}
