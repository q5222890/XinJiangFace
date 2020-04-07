package com.zack.xjht.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.zack.xjht.R;
import com.zack.xjht.entity.SubCabBean;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 枪弹数据
 */

public class GunAmmoDataAdapter extends RecyclerView.Adapter<GunAmmoDataAdapter.ViewHolder> {

    private List<SubCabBean> subCabs;
    //    private List<SubCabsBean> selectList = new ArrayList<>();
    private int index;
    private int pageCount;
    private Context mContext;

    public GunAmmoDataAdapter(List<SubCabBean> list, int pageCount, Context context) {
        this.subCabs = list;
        this.pageCount = pageCount;
        this.mContext = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.gun_ammo_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        int pos = position + index * pageCount;
        SubCabBean subCabsBean = subCabs.get(pos);
        int locNo = subCabsBean.getLocationNo();
        String locType = subCabsBean.getLocationType(); //存放枪弹类型
        String isUse = subCabsBean.getIsUse(); //是否存放
        String gunNo = subCabsBean.getGunNo(); //枪支编号
        String objectName1 = subCabsBean.getObjectName(); //枪弹名称
        int objectNumber1 = subCabsBean.getObjectNumber(); //子弹数量
        String gunState = subCabsBean.getGunState();
        holder.itemObjectNo.setText(String.valueOf(locNo));
        if(!TextUtils.isEmpty(isUse)){
            if (isUse.equals("yes")) { //有放枪弹
                if (!TextUtils.isEmpty(locType)) {
                    switch (locType) {
                        case "shortGun": //手枪
                            holder.itemObjectNumber.setText(gunNo);
                            holder.itemObjectName.setText(objectName1);
                            if (!TextUtils.isEmpty(gunState)) {
                                if (gunState.equals("in")) {
                                    holder.itemIvObjectImg.setImageResource(R.drawable.short_gun);
                                } else {
                                    holder.itemIvObjectImg.setImageResource(R.drawable.short_gun_null);
                                }
                            }
                            break;
                        case "longGun": //长枪
                            holder.itemObjectNumber.setText(gunNo);
                            holder.itemObjectName.setText(objectName1);
                            if (!TextUtils.isEmpty(gunState)) {
                                if (gunState.equals("in")) {
                                    holder.itemIvObjectImg.setImageResource(R.drawable.long_gun);
                                } else {
                                    holder.itemIvObjectImg.setImageResource(R.drawable.long_gun_null);
                                }
                            }
                            break;
                        case "ammunition": //子弹
                            holder.itemObjectName.setText(objectName1);
                            holder.itemObjectNumber.setText("数量：" + objectNumber1);
                            holder.itemIvObjectImg.setImageResource(R.drawable.bullet_normal);
                            break;
                        default:  //其它类型
                            holder.itemIvObjectImg.setImageResource(R.drawable.short_gun);
                            break;
                    }
                } else {
                    holder.itemObjectName.setText("sorry!没有get到枪弹类型");
                }
            } else if (isUse.equals("no")) {//空着的
                holder.itemObjectName.setText("未存放");
                holder.itemObjectNumber.setText("");
                switch (locType) {
                    case "shortGun": //手枪
                        holder.itemIvObjectImg.setImageResource(R.drawable.short_gun_null);
                        break;
                    case "longGun": //长枪
                        holder.itemIvObjectImg.setImageResource(R.drawable.long_gun_null);
                        break;
                    case "ammunition": //子弹
                        holder.itemIvObjectImg.setImageResource(R.drawable.bullet_null);
                        break;
                    default:  //其它类型
                        holder.itemIvObjectImg.setImageResource(R.drawable.short_gun_null);
                        break;
                }
            } else { //可能存在的其他情况
                holder.itemObjectName.setText("异常");
                holder.itemObjectName.setTextColor(mContext.getResources().getColor(R.color.red));
                switch (locType) {
                    case "shortGun": //手枪
                        holder.itemIvObjectImg.setImageResource(R.drawable.short_gun);
                        break;
                    case "longGun": //长枪
                        holder.itemIvObjectImg.setImageResource(R.drawable.long_gun);
                        break;
                    case "ammunition": //子弹
                        holder.itemIvObjectImg.setImageResource(R.drawable.bullet_normal);
                        break;
                    default:  //其它类型
                        holder.itemIvObjectImg.setImageResource(R.drawable.short_gun_null);
                        break;
                }
            }
        }
    }

    @Override
    public int getItemCount() {
        int current = index * pageCount;
        return subCabs.size() - current < pageCount ? subCabs.size() - current : pageCount;
    }

    public void setIndex(int index) {
        this.index = index;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.item_iv_object_img)
        ImageView itemIvObjectImg;
        @BindView(R.id.item_object_name)
        TextView itemObjectName;
        @BindView(R.id.item_rl_list_item)
        RelativeLayout itemRlListItem;
        @BindView(R.id.item_object_number)
        TextView itemObjectNumber;
        @BindView(R.id.item_object_no)
        TextView itemObjectNo;

        ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }
}
