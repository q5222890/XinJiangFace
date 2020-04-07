package com.zack.xjht.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;


import com.zack.xjht.Constants;
import com.zack.xjht.R;
import com.zack.xjht.Utils.BitmapUtils;
import com.zack.xjht.entity.SubCabBean;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


public class TempInfoAdapter extends RecyclerView.Adapter<TempInfoAdapter.TempInfoViewHolder> {
    private static final String TAG = "TempInfoAdapter";
    private List<SubCabBean> list;
    private SparseBooleanArray sba;
    private Context context;
    private Bitmap bitmap;
    private int index;
    private int pageCount;

    public TempInfoAdapter(List<SubCabBean> list, int index, int pageCount) {
        this.list = list;
        this.index =index;
        this.pageCount =pageCount;
    }

    public void setList(List<SubCabBean> list) {
        this.list = list;
        sba = new SparseBooleanArray(list.size());
        if (!list.isEmpty()) {
            for (int i = 0; i < list.size(); i++) {
                sba.put(i, true);
            }
        }
        notifyDataSetChanged();
    }

    @Override
    public TempInfoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(
                R.layout.recyclerview_gun_item, parent, false);
        return new TempInfoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final TempInfoAdapter.TempInfoViewHolder holder, final int position) {
        final int pos = position + index * pageCount;
        final SubCabBean subCabsBean = list.get(pos);
        String locationType = subCabsBean.getLocationType();
        int locationNo = subCabsBean.getLocationNo();
        holder.tvItemCabNo.setText(String.valueOf(locationNo));
        holder.tvItemGunStatus.setText("未存放");
        if(!TextUtils.isEmpty(locationType)){
            switch (locationType){
                case Constants.TYPE_AMMO://弹药
                    holder.tvItemGunType.setText("弹药类型");
                    holder.ivItemGunIcon.setImageResource(R.drawable.bullet_null);
                    break;
                case Constants.TYPE_SHORT_GUN://手枪
                    holder.tvItemGunType.setText("手枪类型");
                    holder.ivItemGunIcon.setImageResource(R.drawable.short_gun_null);
                    break;
                case Constants.TYPE_LONG_GUN://长枪
                    holder.tvItemGunType.setText("长枪类型");
                    holder.ivItemGunIcon.setImageResource(R.drawable.long_gun_null);
                    break;
            }
        }

        holder.recyclerLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "onClick  position: " + pos);
                if (onItemClickListener != null) {
                    onItemClickListener.onItemClick(v, pos);
                } else {
                    Log.i(TAG, "onClick  onItemClickListener  is null: ");
                }
            }
        });
    }

    public void setBitmap(TempInfoViewHolder holder, int resId) {
        try {
            bitmap = BitmapUtils.readBitMap(context, resId);
        } catch (Exception e) {
            Log.e(TAG, "GunInfoAdapter setBitmap: " + e.getMessage());
//            e.printStackTrace();
        }
        holder.ivItemGunIcon.setImageBitmap(bitmap);
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

    static class TempInfoViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.tv_item_gun_status)
        TextView tvItemGunStatus;
        @BindView(R.id.tv_item_cab_no)
        TextView tvItemCabNo;
        @BindView(R.id.iv_item_gun_icon)
        ImageView ivItemGunIcon;
        @BindView(R.id.tv_item_gun_type)
        TextView tvItemGunType;
        @BindView(R.id.tv_item_gun_count)
        TextView tvItemGunCount;
        @BindView(R.id.recycler_layout)
        RelativeLayout recyclerLayout;
        @BindView(R.id.iv_item_gun_check)
        ImageView ivItemGunCheck;

        TempInfoViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(View v, int position);
    }

    public TempInfoAdapter.OnItemClickListener onItemClickListener;

    public void setOnItemClickListener(TempInfoAdapter.OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    public void setSparseBooleanArray(int key, boolean val) {
        if (sba != null) {
            sba.put(key, val);
        }
        notifyDataSetChanged();
    }
}