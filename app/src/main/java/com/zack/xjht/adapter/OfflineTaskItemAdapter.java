package com.zack.xjht.adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.zack.xjht.Constants;
import com.zack.xjht.R;
import com.zack.xjht.Utils.SharedUtils;
import com.zack.xjht.Utils.ToastUtil;
import com.zack.xjht.entity.OfflineTaskItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class OfflineTaskItemAdapter extends RecyclerView.Adapter<OfflineTaskItemAdapter.OfflineTaskItemViewHolder> {
    private static final String TAG = "OfflineTaskItemAdapter";

    private HashMap<Integer, Boolean> checkStatus;
    private List<OfflineTaskItem> list;
    private List<OfflineTaskItem> checkedList;
    private int index;
    private int pageCount;
    private boolean isDisableAll;
    private int backNumPos = -1;
    private SparseArray<String> getNumAry = new SparseArray<>();
    private SparseArray<String> backNumAry = new SparseArray<>();

    public OfflineTaskItemAdapter(List<OfflineTaskItem> list, int index, int pageCount) {
        this.list = list;
        this.index = index;
        this.pageCount = pageCount;
        checkedList = new ArrayList<>();
        checkStatus = new HashMap<>();
        for (int i = 0; i < list.size(); i++) {
            checkStatus.put(i, false);
        }
        isDisableAll = false;
    }

    public void setList(List<OfflineTaskItem> list) {
        this.list = list;
        for (int i = 0; i < list.size(); i++) {
            checkStatus.put(i, false);
        }
        notifyDataSetChanged();
    }

    public void setIndex(int index) {
        this.index = index;
        notifyDataSetChanged();
    }

    public List<OfflineTaskItem> getCheckedList() {
        return checkedList;
    }

    public void setDisableAll() {
        isDisableAll = true;
    }

    @NonNull
    @Override
    public OfflineTaskItemViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.offline_task_item,
                viewGroup, false);
        return new OfflineTaskItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final OfflineTaskItemViewHolder holder, int position) {
        final int pos = position + index * pageCount;
        holder.offlineDataCbGet.setOnCheckedChangeListener(null);
        if (checkStatus.containsKey(pos)) {
            holder.offlineDataCbGet.setChecked(checkStatus.get(pos));
        }
        //设置背景色
        if ((pos & 1) == 1) {
            holder.offlineTaskItemRoot.setBackgroundColor(Color.parseColor("#FF0D47A1"));
        } else {
            holder.offlineTaskItemRoot.setBackgroundColor(Color.parseColor("#FF01579B"));
        }

        if (isDisableAll) {
            holder.offlineDataCbGet.setEnabled(false);
            holder.offlineDataCbGet.setClickable(false);
            holder.offlineTaskItemEdtBackNum.setEnabled(false);
            holder.offlineTaskItemEdtBackNum.setFocusable(false);
            String value = backNumAry.get(pos, null);
            if (TextUtils.isEmpty(value)) {
                holder.offlineTaskItemEdtBackNum.setText("");
            }
        } else {
            holder.offlineDataCbGet.setClickable(true);
            holder.offlineDataCbGet.setEnabled(true);
            holder.offlineTaskItemEdtBackNum.setEnabled(true);
            holder.offlineTaskItemEdtBackNum.setFocusable(true);
        }

        if(SharedUtils.getCabType().equals(Constants.TYPE_AMMO_CAB)){
            holder.offlineTaskItemTvGunNo.setVisibility(View.GONE);
            holder.offlineTaskItemTvGetNum.setVisibility(View.VISIBLE);
            holder.offlineTaskItemEdtBackNum.setVisibility(View.VISIBLE);
        }else{
            holder.offlineTaskItemTvGunNo.setVisibility(View.VISIBLE);
            holder.offlineTaskItemTvGetNum.setVisibility(View.GONE);
            holder.offlineTaskItemEdtBackNum.setVisibility(View.GONE);
        }
        final OfflineTaskItem offlineTaskItem = list.get(pos);
        Long id = offlineTaskItem.getId();
        String objectType = offlineTaskItem.getObjectType();
        int locationNo = offlineTaskItem.getLocationNo();
        final String locationType = offlineTaskItem.getLocationType();
        int objectNum = offlineTaskItem.getObjectNum();
        String gunNo = offlineTaskItem.getGunNo();
        String userName = offlineTaskItem.getUserName();

        holder.offlineTaskItemTvId.setText(String.valueOf(id));
        holder.offlineTaskItemTvType.setText(objectType);
        holder.offlineTaskItemTvPosNo.setText(String.valueOf(locationNo));
        holder.offlineTaskItemTvGunNo.setText(gunNo);
        holder.offlineTaskItemTvUserName.setText(userName);
        if (locationType.equals(Constants.TYPE_AMMO)) {
//            if (getNumAry.size() > 0 && !TextUtils.isEmpty(getNumAry.get(pos))) {
//                holder.offlineTaskItemTvGetNum.setText(getNumAry.get(pos));
//            } else {
                getNumAry.put(pos, String.valueOf(objectNum));
                holder.offlineTaskItemTvGetNum.setText(String.valueOf(objectNum));
//            }
            String value = backNumAry.get(pos, null);
            if (!TextUtils.isEmpty(value)) {
                holder.offlineTaskItemEdtBackNum.setText(value);
            } else {
                holder.offlineTaskItemEdtBackNum.setText("");
            }
            //监听数量输入
            holder.offlineTaskItemEdtBackNum.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    Log.i(TAG, "onTextChanged s: " + s);
                    if (!TextUtils.isEmpty(s)) {
                        int num = Integer.parseInt(s.toString());
                        if (num > Integer.parseInt(getNumAry.get(backNumPos))) {//输入数量大于库存数量
                            ToastUtil.showShort("归还数量无效");
                            holder.offlineTaskItemEdtBackNum.setText("");
                        }
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });

            holder.offlineTaskItemEdtBackNum.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View view, boolean b) {
                    if (b) {
                        backNumPos = pos;
                        Log.e(TAG, "backNumPos 焦点选中" + backNumPos);
                    }
                }
            });
        }

        holder.offlineDataCbGet.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                checkStatus.put(pos, isChecked);
                if (isChecked) {
                    if (locationType.equals(Constants.TYPE_AMMO)) {//弹药
                        String num = holder.offlineTaskItemEdtBackNum.getText().toString();
                        if (TextUtils.isEmpty(num)) {
                            ToastUtil.showShort("请输入子弹数量");
                            holder.offlineDataCbGet.setChecked(false);
                            return;
                        } else {
                            int backNum = Integer.parseInt(num);
                            if (backNum >= 0) {
                                offlineTaskItem.setBackNum(backNum); //保存归还数量
                                checkedList.add(offlineTaskItem);
                                holder.offlineTaskItemEdtBackNum.setEnabled(false);
                                Log.i(TAG, "onCheckedChanged offlineTaskItem: " + JSON.toJSONString(offlineTaskItem));
                            } else {
                                ToastUtil.showShort("输入的数量有误");
                                holder.offlineDataCbGet.setChecked(false);
                                return;
                            }
                        }
                    } else {
                        checkedList.add(offlineTaskItem);
                    }
                } else {
                    checkedList.remove(offlineTaskItem);
                    holder.offlineTaskItemEdtBackNum.setEnabled(true);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        int current = index * pageCount;
        return list.size() - current < pageCount ? list.size() - current : pageCount;
    }

    public class OfflineTaskItemViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.offline_task_item_tv_id)
        TextView offlineTaskItemTvId;
        @BindView(R.id.offline_task_item_tv_type)
        TextView offlineTaskItemTvType;
        @BindView(R.id.offline_task_item_tv_gun_no)
        TextView offlineTaskItemTvGunNo;
        @BindView(R.id.offline_task_item_tv_pos_no)
        TextView offlineTaskItemTvPosNo;
        @BindView(R.id.offline_task_item_tv_get_num)
        TextView offlineTaskItemTvGetNum;
        @BindView(R.id.offline_task_item_tv_user_name)
        TextView offlineTaskItemTvUserName;
        @BindView(R.id.offline_task_item_edt_back_num)
        EditText offlineTaskItemEdtBackNum;
        @BindView(R.id.offline_data_cb_get)
        CheckBox offlineDataCbGet;
        @BindView(R.id.offline_task_item_root)
        LinearLayout offlineTaskItemRoot;

        public OfflineTaskItemViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
