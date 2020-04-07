package com.zack.xjht.adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.AppCompatSpinner;
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
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.zack.xjht.Constants;
import com.zack.xjht.R;
import com.zack.xjht.Utils.SharedUtils;
import com.zack.xjht.Utils.ToastUtil;
import com.zack.xjht.db.DBManager;
import com.zack.xjht.db.gen.UserBeanDao;
import com.zack.xjht.entity.SubCabBean;
import com.zack.xjht.entity.UserBean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

public class OfflineDataAdapter extends RecyclerView.Adapter<OfflineDataAdapter.OfflineDataViewHolder> {
    private static final String TAG = "OfflineDataAdapter";
    private final List<UserBean> userBeans;

    private List<SubCabBean> list;
    private List<SubCabBean> checkedList;
    private int index;
    private int pageCount;
    private Map<Integer, Boolean> checkStatus;
    private Map<Integer, UserBean> selectUserMap;
    private boolean isDisableAll;
    private int getNumPos = -1;
    private InputMethodManager inputMethodManager;
    private SparseArray<String> stockNumAry = new SparseArray<>();
    private SparseArray<String> getNumAry = new SparseArray<>();
    private UserBeanDao userBeanDao;
    private UserItemAdapter userItemAdapter;

    public OfflineDataAdapter(List<SubCabBean> list, int index, int pageCount,
                              UserItemAdapter userItemAdapter) {
        this.list = list;
        this.index = index;
        this.pageCount = pageCount;
        checkedList = new ArrayList<>();
        checkStatus = new HashMap<>();
        for (int i = 0; i < list.size(); i++) {
            checkStatus.put(i, false);
        }
        isDisableAll = false;

        userBeanDao = DBManager.getInstance().getUserBeanDao();
        userBeans = userBeanDao.loadAll();
        this.userItemAdapter = userItemAdapter;

        selectUserMap =new HashMap<>();
    }

    public void setList(List<SubCabBean> list) {
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

    public List<SubCabBean> getSelectList() {
        return checkedList;
    }

    @NonNull
    @Override
    public OfflineDataViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        inputMethodManager = (InputMethodManager)
                viewGroup.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.offline_data_list,
                viewGroup, false);
        return new OfflineDataViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final OfflineDataViewHolder holder, int i) {
        final int pos = i + index * pageCount;
        holder.offlineDataCbGet.setOnCheckedChangeListener(null);
        if (checkStatus.containsKey(pos)) {
            holder.offlineDataCbGet.setChecked(checkStatus.get(pos));
        }

        if(selectUserMap.containsKey(pos)){
            holder.offlineDataTvPolice.setText(selectUserMap.get(pos).getUserName());
        }
        //设置背景色
        if ((pos & 1) == 1) {
            holder.offlineDataRootView.setBackgroundColor(Color.parseColor("#FF0D47A1"));
        } else {
            holder.offlineDataRootView.setBackgroundColor(Color.parseColor("#FF01579B"));
        }

        if (isDisableAll) {
            holder.offlineDataCbGet.setEnabled(false);
            holder.offlineDataCbGet.setClickable(false);
            holder.offlineDataEdtNum.setEnabled(false);
            holder.offlineDataEdtNum.setFocusable(false);
            String value = getNumAry.get(pos, null);
            if (TextUtils.isEmpty(value)) {
                holder.offlineDataEdtNum.setText("");
            }
        } else {
            holder.offlineDataCbGet.setClickable(true);
            holder.offlineDataCbGet.setEnabled(true);
            holder.offlineDataEdtNum.setEnabled(true);
            holder.offlineDataEdtNum.setFocusable(true);
        }

        final SubCabBean subCabBean = list.get(pos);
        String isUse = subCabBean.getIsUse();
        if (!TextUtils.isEmpty(isUse) && isUse.equals("yes")) {
            Long sId = subCabBean.getSId();
            int locationNo = subCabBean.getLocationNo();
            final String locationType = subCabBean.getLocationType();
            String objectName = subCabBean.getObjectName();
            int objectNumber = subCabBean.getObjectNumber();
            String gunNo = subCabBean.getGunNo();

            holder.offlineDataTvNo.setText(String.valueOf(sId));
            holder.offlineDataTvPosition.setText(String.valueOf(locationNo));
            holder.offlineDataTvType.setText(objectName);
            holder.offlineDataTvCount.setText(String.valueOf(objectNumber));
            holder.offlineDataTvGunNo.setText(gunNo);
            if(!TextUtils.isEmpty(locationType)){
                if (locationType.equals(Constants.TYPE_AMMO)) {
                    if (stockNumAry.size() > 0 && !TextUtils.isEmpty(stockNumAry.get(pos))) {
                        holder.offlineDataTvCount.setText(stockNumAry.get(pos));
                    } else {
                        stockNumAry.put(pos, String.valueOf(objectNumber));
                        holder.offlineDataTvCount.setText(String.valueOf(objectNumber));
                    }
                    String value = getNumAry.get(pos, null);
                    if (!TextUtils.isEmpty(value)) {
                        holder.offlineDataEdtNum.setText(value);
                    } else {
                        holder.offlineDataEdtNum.setText("");
                    }
                    //监听数量输入
                    holder.offlineDataEdtNum.addTextChangedListener(new TextWatcher() {
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
                                    holder.offlineDataEdtNum.setText("");
                                }
                            }
                        }

                        @Override
                        public void afterTextChanged(Editable s) {

                        }
                    });
                    holder.offlineDataEdtNum.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                        @Override
                        public void onFocusChange(View view, boolean b) {
                            if (b) {
                                getNumPos = pos;
                                Log.e(TAG, "getNumPos 焦点选中" + getNumPos);
                            }
                        }
                    });
                }
            }
            holder.offlineDataCbGet.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    checkStatus.put(pos, isChecked);
                    if (isChecked) { //已选中
                        if(userItemAdapter.getSelectUser() !=null){
                            UserBean selectUser = userItemAdapter.getSelectUser();
                            selectUserMap.put(pos, selectUser);
                            userItemAdapter.setSelectUser(null);
                            userItemAdapter.setSelectPosition(-1);
                            String userName = selectUser.getUserName();
                            holder.offlineDataTvPolice.setText(userName);
                            int userId = selectUser.getUserId();
                            subCabBean.setUserId(String.valueOf(userId));
                            subCabBean.setUserName(userName);
                            if (locationType.equals("ammunition")) {//弹药
                                String num = holder.offlineDataEdtNum.getText().toString();
                                if (TextUtils.isEmpty(num)) {
                                    ToastUtil.showShort("请输入领取数量");
                                    holder.offlineDataCbGet.setChecked(false);
                                    return;
                                } else {
                                    int getNum = Integer.parseInt(num);
                                    if (getNum > 0) {
                                        subCabBean.setGetNum(getNum); //领取数量
                                        checkedList.add(subCabBean);
                                        holder.offlineDataEdtNum.setEnabled(false);
                                        Log.i(TAG, "onCheckedChanged subCabsBean: " + JSON.toJSONString(subCabBean));
                                    } else {
                                        ToastUtil.showShort("输入的领取数量有误");
                                        holder.offlineDataCbGet.setChecked(false);
                                        return;
                                    }
                                }
                            } else {
                                checkedList.add(subCabBean);
                            }
                        }else{
                            ToastUtil.showShort("请先选择人员数据");
                            holder.offlineDataCbGet.setChecked(false);
                        }
                    } else { //取消选中
                        checkedList.remove(subCabBean);
                        holder.offlineDataEdtNum.setEnabled(true);
                        if(userItemAdapter.getSelectUser() !=null){
                            userItemAdapter.setSelectPosition(-1);
                            userItemAdapter.setSelectUser(null);
                        }
                        holder.offlineDataTvPolice.setText("");
                        selectUserMap.remove(pos);//取消记忆
                        subCabBean.setUserId("");
                        subCabBean.setUserName("");
                    }
                }
            });
        }
        if (SharedUtils.getCabType().equals(Constants.TYPE_AMMO_CAB)) {
            holder.offlineDataTvGunNo.setVisibility(View.GONE);
            holder.offlineDataTvCount.setVisibility(View.VISIBLE);
            holder.offlineDataEdtNum.setVisibility(View.VISIBLE);
        } else {
            holder.offlineDataTvGunNo.setVisibility(View.VISIBLE);
            holder.offlineDataTvCount.setVisibility(View.GONE);
            holder.offlineDataEdtNum.setVisibility(View.GONE);
        }

        holder.offlineDataTvPolice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //弹出popupwindow

            }
        });
    }

    @Override
    public void onViewDetachedFromWindow(OfflineDataAdapter.OfflineDataViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
//        Log.e(TAG, "隐藏item=" + holder.getAdapterPosition());
        holder.offlineDataEdtNum.removeTextChangedListener(textWatcher);
        holder.offlineDataEdtNum.clearFocus();
        if (getNumPos == holder.getAdapterPosition()) {
            inputMethodManager.hideSoftInputFromWindow(holder.offlineDataEdtNum.getWindowToken(), 0);
        }
    }

    @Override
    public void onViewAttachedToWindow(OfflineDataAdapter.OfflineDataViewHolder holder) {
        super.onViewAttachedToWindow(holder);
//        Log.e(TAG, "显示item=" + holder.getAdapterPosition());
        holder.offlineDataEdtNum.addTextChangedListener(textWatcher);
        if (getNumPos == holder.getAdapterPosition()) {
            holder.offlineDataEdtNum.requestFocus();
            holder.offlineDataEdtNum.setSelection(holder.offlineDataEdtNum.getText().length());
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
    public int getItemCount() {
        int current = index * pageCount;
        return list.size() - current < pageCount ? list.size() - current : pageCount;
    }

    public void setDisableAll() {
        isDisableAll = true;
        notifyDataSetChanged();
    }

    public class OfflineDataViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.offline_data_tv_no)
        TextView offlineDataTvNo;
        @BindView(R.id.offline_data_tv_position)
        TextView offlineDataTvPosition;
        @BindView(R.id.offline_data_tv_count)
        TextView offlineDataTvCount;
        @BindView(R.id.offline_data_tv_type)
        TextView offlineDataTvType;
        @BindView(R.id.offline_data_tv_gun_no)
        TextView offlineDataTvGunNo;
        @BindView(R.id.offline_data_edt_num)
        EditText offlineDataEdtNum;
        @BindView(R.id.offline_data_cb_get)
        CheckBox offlineDataCbGet;
        @BindView(R.id.offline_data_root_view)
        LinearLayout offlineDataRootView;
        @BindView(R.id.offline_data_tv_police)
        TextView offlineDataTvPolice;

        public OfflineDataViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

}
