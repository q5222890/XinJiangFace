package com.zack.xjht.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.zack.xjht.Constants;
import com.zack.xjht.R;
import com.zack.xjht.Utils.SharedUtils;
import com.zack.xjht.Utils.ToastUtil;
import com.zack.xjht.entity.GunStateBean;
import com.zack.xjht.entity.SubCabBean;

import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;


/**
 * 枪支在库列表信息
 */

public class GunInfoAdapter extends RecyclerView.Adapter<GunInfoAdapter.GunInfoViewHolder> {
    private static final String TAG = "GunInfoAdapter";
    private List<SubCabBean> list;
    private Context context;
    private int index;
    private int pageCount;
    private int address;
    private int status;
    private List<GunStateBean> gunStateList;
    private Map<Integer, Integer> gunStatus;

    public GunInfoAdapter(List<SubCabBean> list, int index, int pageCount) {
        this.list = list;
        this.index = index;
        this.pageCount = pageCount;
    }

    public void setIndex(int index) {
        this.index = index;
        notifyDataSetChanged();
    }

    @Override
    public GunInfoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(
                R.layout.recycler_view_gun_item, parent, false);
        return new GunInfoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final GunInfoViewHolder holder, final int position) {
        final int pos = position + index * pageCount;
        SubCabBean subCabBean = list.get(pos);
        String locationType = subCabBean.getLocationType();
        String objectName = subCabBean.getObjectName();
        String gunNo = subCabBean.getGunNo();
        String isUse = subCabBean.getIsUse();
        int locationNo = subCabBean.getLocationNo();
        int objectNumber = subCabBean.getObjectNumber();
        String gunState = subCabBean.getGunState();
        holder.tvItemCabNo.setText(String.valueOf(locationNo));
        if (!TextUtils.isEmpty(objectName)) {
            holder.tvItemGunType.setText(objectName);
        }
        if (!TextUtils.isEmpty(isUse)) {
            if (isUse.equals("no")) { //未使用
                holder.tvItemGunState.setText("未使用");
            } else {
                if (!TextUtils.isEmpty(gunState)) {
                    if (gunState.equals("in")) {
                        if(!TextUtils.isEmpty(locationType)){
                            switch (locationType) {
                                case Constants.TYPE_SHORT_GUN: //手枪
                                case Constants.TYPE_LONG_GUN: //长枪
                                    holder.tvItemGunCount.setText("编号:" + gunNo);
                                    break;
                                case Constants.TYPE_AMMO://弹药
                                    holder.tvItemGunCount.setText("数量:" + objectNumber);
                                    holder.ivItemGunIcon.setImageResource(R.drawable.bullet_normal);
                                    break;
                                default: //其它
                                    holder.tvItemGunCount.setText("数据异常");
                                    break;
                            }
                        }
                    } else { //不在库
                        switch (locationType) {
                            case Constants.TYPE_SHORT_GUN: //手枪
                            case Constants.TYPE_LONG_GUN: //长枪
                                holder.tvItemGunCount.setText("编号:" + gunNo);
                                break;
                            case Constants.TYPE_AMMO://弹药
                                holder.tvItemGunCount.setText("数量:" + objectNumber);
                                holder.ivItemGunIcon.setImageResource(R.drawable.bullet_normal);
                                break;
                            default: //其它
                                holder.tvItemGunCount.setText("数据异常");
                                break;
                        }
                    }
                } else {
                    //弹药
                    //其它
                    if (Constants.TYPE_AMMO.equals(locationType)) {
                        holder.tvItemGunCount.setText("数量:" + objectNumber);
                        holder.ivItemGunIcon.setImageResource(R.drawable.bullet_normal);
                    } else {
                        holder.tvItemGunCount.setText("状态为空");
                    }
                }
            }
        }

        if (!SharedUtils.getCabType().equals(Constants.TYPE_AMMO_CAB)) {
            if (isUse.equals("no")) { //未使用
                holder.tvItemGunState.setText("未使用");
            } else { //已使用
                //枪柜 获取枪支状态
                if(!gunStatus.isEmpty() && gunStatus.containsKey(locationNo)){
                    int status = gunStatus.get(locationNo); //枪支状态
                    if (status == 0) { //不在位
                        switch (locationType) {
                            case Constants.TYPE_SHORT_GUN: //手枪
                                holder.ivItemGunIcon.setImageResource(R.drawable.short_gun_null);
                                break;
                            case Constants.TYPE_LONG_GUN: //长枪
                                holder.ivItemGunIcon.setImageResource(R.drawable.long_gun_null);
                                break;
                        }
                        holder.ivItemGunStatus.setImageResource(R.drawable.wrong);
                        holder.tvItemGunState.setTextColor(context.getResources().getColor(R.color.red));
                        holder.tvItemGunState.setText("不在位");
                    } else if (status == 1) { //在位
                        switch (locationType) {
                            case Constants.TYPE_SHORT_GUN: //手枪
                                holder.ivItemGunIcon.setImageResource(R.drawable.short_gun);
                                break;
                            case Constants.TYPE_LONG_GUN: //长枪
                                holder.ivItemGunIcon.setImageResource(R.drawable.long_gun);
                                break;
                        }
                        holder.ivItemGunStatus.setImageResource(R.drawable.right);
                        holder.tvItemGunState.setTextColor(context.getResources().getColor(R.color.green));
                        holder.tvItemGunState.setText("在位");
                    }
                }
            }
        } else {
            //查询弹药数量
            holder.tvItemGunCount.setText("数量:" + objectNumber);
        }
    }

    @Override
    public int getItemCount() {
        int current = index * pageCount;
        return list.size() - current < pageCount ? list.size() - current : pageCount;
    }

    @Override
    public int getItemViewType(int position) {
        return position + index * pageCount;
    }

    @Override
    public long getItemId(int position) {
        return position + index * pageCount;
    }

    public void setGunStateList(List<GunStateBean> gunStateBeans) {
        this.gunStateList = gunStateBeans;
        notifyDataSetChanged();
    }


    public void setGunStatus(Map<Integer, Integer> gunStatus) {
        this.gunStatus = gunStatus;
        notifyDataSetChanged();
    }

    public void setList(List<SubCabBean> listLocation) {
        this.list = listLocation;
        notifyDataSetChanged();
    }

    public void setGunState(int address, int status) {
        this.address = address;
        this.status = status;
        notifyDataSetChanged();
    }

    static class GunInfoViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.tv_item_cab_no)
        TextView tvItemCabNo;
        @BindView(R.id.iv_item_gun_icon)
        ImageView ivItemGunIcon;
        @BindView(R.id.tv_item_gun_type)
        TextView tvItemGunType;
        @BindView(R.id.tv_item_gun_count)
        TextView tvItemGunCount;
        @BindView(R.id.item_tv_gun_state)
        TextView tvItemGunState;
        @BindView(R.id.tv_item_lock_state)
        TextView tvItemLockState;
        @BindView(R.id.recycler_layout)
        RelativeLayout recyclerLayout;
        @BindView(R.id.iv_item_gun_status)
        ImageView ivItemGunStatus;

        GunInfoViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

}
