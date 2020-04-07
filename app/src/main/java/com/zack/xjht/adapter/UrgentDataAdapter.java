package com.zack.xjht.adapter;

import android.content.Context;
import android.graphics.Color;
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
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.zack.xjht.Constants;
import com.zack.xjht.R;
import com.zack.xjht.Utils.ToastUtil;
import com.zack.xjht.entity.SubCabBean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 *
 */
public class UrgentDataAdapter extends RecyclerView.Adapter<UrgentDataAdapter.UrgentDataViewHolder> {
    private static final String TAG = "UrgentDataAdapter";

    private List<SubCabBean> subCabsBeanList;
    private List<SubCabBean> checkedList = new ArrayList<>();
    private int index;
    private int pageCount;
    private Map<Integer, Boolean> checkStatus;//用来记录所有checkbox的状态
    private String flag;
    private boolean isDisable = false;

    private SparseArray<String> stockNumAry = new SparseArray<>();
    private SparseArray<String> getNumAry = new SparseArray<>();
    private int stockNumPos = -1;
    private int getNumPos = -1;
    private InputMethodManager inputMethodManager;
    private Context context;

    public void setIndex(int index) {
        this.index = index;
        notifyDataSetChanged();
    }

    public UrgentDataAdapter(List<SubCabBean> subCabsBeanList, int index, int pageCount, String flag) {
        this.subCabsBeanList = subCabsBeanList;
        this.index = index;
        this.pageCount = pageCount;
        checkStatus = new HashMap<>();//用来记录所有checkbox的状态
        this.flag = flag;
    }

    @Override
    public UrgentDataViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        context = parent.getContext();
        inputMethodManager = (InputMethodManager)
                context.getSystemService(Context.INPUT_METHOD_SERVICE);
        View view = LayoutInflater.from(context).inflate(R.layout.item_list_urgent_get,
                parent, false);
        return new UrgentDataViewHolder(view);
    }

    public List<SubCabBean> getCheckedList() {
        return checkedList;
    }

    public void setSubCabsBeanList(List<SubCabBean> subCabsBeanList) {
        checkedList.clear();
        this.subCabsBeanList = subCabsBeanList;
        for (int i = 0; i < subCabsBeanList.size(); i++) {
            checkStatus.put(i, false);// 默认所有的checkbox都是没选中
        }
        notifyDataSetChanged();
    }

    public void selectAll() {
        checkedList.clear();
        checkedList.addAll(subCabsBeanList);
        for (int i = 0; i < subCabsBeanList.size(); i++) {
            checkStatus.put(i, true);// 默认所有的checkbox都是没选中
        }
        notifyDataSetChanged();
    }

    public void setDisableAll() {
        isDisable = true;
        notifyDataSetChanged();
    }

    @Override
    public void onViewDetachedFromWindow(UrgentDataAdapter.UrgentDataViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
//        Log.e(TAG, "隐藏item=" + holder.getAdapterPosition());
        holder.listItemEdtGetNum.removeTextChangedListener(textWatcher);
        holder.listItemEdtGetNum.clearFocus();
        if (getNumPos == holder.getAdapterPosition()) {
            inputMethodManager.hideSoftInputFromWindow(holder.listItemEdtGetNum.getWindowToken(), 0);
        }
    }

    @Override
    public void onViewAttachedToWindow(UrgentDataAdapter.UrgentDataViewHolder holder) {
        super.onViewAttachedToWindow(holder);
//        Log.e(TAG, "显示item=" + holder.getAdapterPosition());
        holder.listItemEdtGetNum.addTextChangedListener(textWatcher);
        if (getNumPos == holder.getAdapterPosition()) {
            holder.listItemEdtGetNum.requestFocus();
            holder.listItemEdtGetNum.setSelection(holder.listItemEdtGetNum.getText().length());
        }
    }

    private TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            Log.i(TAG, "afterTextChanged s: " + s);
            getNumAry.put(getNumPos, s.toString());
        }
    };

    @Override
    public void onBindViewHolder(final UrgentDataViewHolder holder, int position) {
        final int pos = position + index * pageCount;
        final SubCabBean subCabsBean = subCabsBeanList.get(pos);
//        Log.i(TAG, "onBindViewHolder  subCabBean: "+ JSON.toJSONString(subCabsBean));

        if((pos&1)==1){
            holder.listItemLlRoot.setBackgroundColor(Color.parseColor("#FF0D47A1"));
        }else{
            holder.listItemLlRoot.setBackgroundColor(Color.parseColor("#FF01579B"));
        }
        //显示被选中的条目
        holder.listItemCbGet.setOnCheckedChangeListener(null);
        if (checkStatus.containsKey(pos)) {
            holder.listItemCbGet.setChecked(checkStatus.get(pos));
        }

        //禁用checkbox点击
        if (isDisable) {
            holder.listItemCbGet.setEnabled(false);
            holder.listItemCbGet.setClickable(false);
            holder.listItemEdtGetNum.setEnabled(false);
            holder.listItemEdtGetNum.setFocusable(false);
            String value = getNumAry.get(pos, null);
            if (TextUtils.isEmpty(value)) {
                holder.listItemEdtGetNum.setText("");
            }
        } else {
            holder.listItemCbGet.setEnabled(true);
            holder.listItemCbGet.setClickable(true);
            holder.listItemEdtGetNum.setEnabled(true);
            holder.listItemEdtGetNum.setFocusable(true);
        }

        if (flag.equals("gun")) {
            holder.listItemTvNo.setVisibility(View.VISIBLE);
            holder.listItemRlGetNum.setVisibility(View.GONE);
        } else {
            holder.listItemTvNo.setVisibility(View.GONE);
            holder.listItemRlGetNum.setVisibility(View.VISIBLE);
        }

        if (subCabsBean != null) {
            final String locationType = subCabsBean.getLocationType();
            String objectName = subCabsBean.getObjectName();
            String gunNo = subCabsBean.getGunNo();
            String isUse = subCabsBean.getIsUse();  //是否存放
            int locationNo = subCabsBean.getLocationNo();
            final int objectNumber1 = subCabsBean.getObjectNumber();
            if (!TextUtils.isEmpty(isUse) && isUse.equals("yes")) { //已存放
                holder.listItemTvType.setText(objectName);
                holder.listItemTvPosition.setText(String.valueOf(locationNo));
                if(!TextUtils.isEmpty(locationType)){
                    switch (locationType) {
                        case Constants.TYPE_SHORT_GUN:
                        case Constants.TYPE_LONG_GUN:
                            holder.listItemTvNo.setText(gunNo);
                            break;
                        case Constants.TYPE_AMMO:
                            if (stockNumAry.size() > 0 && !TextUtils.isEmpty(stockNumAry.get(pos))) {
                                holder.listItemEdtCount.setText(stockNumAry.get(pos));
                            } else {
                                stockNumAry.put(pos, String.valueOf(objectNumber1));
                                holder.listItemEdtCount.setText(String.valueOf(objectNumber1));
                            }
                            String value = getNumAry.get(pos, null);
                            if (!TextUtils.isEmpty(value)) {
                                holder.listItemEdtGetNum.setText(value);
                            } else {
                                holder.listItemEdtGetNum.setText("");
                            }
                            //监听数量输入
                            holder.listItemEdtGetNum.addTextChangedListener(new TextWatcher() {
                                @Override
                                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                                }

                                @Override
                                public void onTextChanged(CharSequence s, int start, int before, int count) {
                                    Log.i(TAG, "onTextChanged s: " + s);
                                    if (!TextUtils.isEmpty(s)) {
                                        int num = Integer.parseInt(s.toString());
                                        if (num > Integer.parseInt(stockNumAry.get(getNumPos))) {//输入数量大于库存数量
                                            ToastUtil.showShort("超出可领取子弹数量");
                                            holder.listItemEdtGetNum.setText("");
                                        }
                                    }
                                }

                                @Override
                                public void afterTextChanged(Editable s) {

                                }
                            });
                            holder.listItemEdtGetNum.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                                @Override
                                public void onFocusChange(View view, boolean b) {
                                    if (b) {
                                        getNumPos = pos;
                                        Log.e(TAG, "getNumPos 焦点选中" + getNumPos);
                                    }
                                }
                            });
                            break;

                    }
                }
            }

            holder.listItemCbGet.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    checkStatus.put(pos, isChecked);//check状态一旦改变，保存的check值也要发生相应的变化
                    if (isChecked) {
                        if (locationType.equals("ammunition")) {//弹药
                            String num = holder.listItemEdtGetNum.getText().toString();
                            if (TextUtils.isEmpty(num)) {
                                ToastUtil.showShort("请输入领取数量");
                                holder.listItemCbGet.setChecked(false);
                                return;
                            } else {
                                int getNum = Integer.parseInt(num);
                                if (getNum > 0) {
                                    subCabsBean.setObjectNumber(getNum); //领取数量
                                    checkedList.add(subCabsBean);
                                    holder.listItemEdtGetNum.setEnabled(false);
                                    Log.i(TAG, "onCheckedChanged subCabsBean: " + JSON.toJSONString(subCabsBean));
                                } else {
                                    ToastUtil.showShort("输入的领取数量有误");
                                    holder.listItemCbGet.setChecked(false);
                                    return;
                                }
                            }
                        } else {
                            checkedList.add(subCabsBean);
                        }
                    } else {
                        holder.listItemEdtGetNum.setEnabled(true);
                        checkedList.remove(subCabsBean);
                    }
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        int current = index * pageCount;
        return subCabsBeanList.size() - current < pageCount ? subCabsBeanList.size() - current : pageCount;
    }


    static class UrgentDataViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.list_item_tv_type)
        TextView listItemTvType;
        @BindView(R.id.list_item_tv_no)
        TextView listItemTvNo;
        @BindView(R.id.list_item_tv_position)
        TextView listItemTvPosition;
        @BindView(R.id.list_item_edt_count)
        EditText listItemEdtCount;
        @BindView(R.id.list_item_edt_get_num)
        EditText listItemEdtGetNum;
        @BindView(R.id.list_item_cb_get)
        CheckBox listItemCbGet;
        @BindView(R.id.list_item_ll_root)
        LinearLayout listItemLlRoot;
        @BindView(R.id.list_item_rl_get_num)
        RelativeLayout listItemRlGetNum;

        UrgentDataViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }
}
