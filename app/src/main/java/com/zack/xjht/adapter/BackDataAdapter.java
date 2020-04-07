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
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.zack.xjht.Constants;
import com.zack.xjht.R;
import com.zack.xjht.Utils.ToastUtil;
import com.zack.xjht.entity.UrgentGetListBean;
import com.zack.xjht.entity.UrgentTaskInfoBean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

public class BackDataAdapter extends RecyclerView.Adapter<BackDataAdapter.BackDataViewHolder> {
    private static final String TAG = "BackDataAdapter";
    private List<UrgentGetListBean> operList;
    private List<UrgentGetListBean> checkedList;
    private Context mContext;
    private int index;
    private int pageCount;
    private Map<Integer, Boolean> checkStatus;
    private boolean isDisableAll = false;
    private boolean isConfirm = false;
    private int getNumPos = -1; //领取位置
    private int backNumPos = -1; //归还位置
    private SparseArray<String> getNumAry = new SparseArray<>();
    private SparseArray<String> backNumAry = new SparseArray<>();
    private InputMethodManager inputMethodManager;

    public BackDataAdapter(List<UrgentGetListBean> operList, int index, int pageCount) {
        this.operList = operList;
        this.index = index;
        this.pageCount = pageCount;
        checkedList = new ArrayList<>();
        checkStatus = new HashMap<>();
    }

    public List<UrgentGetListBean> getCheckedList() {
        return checkedList;
    }

    public void setConfirm(boolean confirm) {
        isConfirm = confirm;
    }

    public void setIndex(int index) {
        this.index = index;
        notifyDataSetChanged();
    }

    public void setOperList(List<UrgentGetListBean> operList) {
        checkedList.clear();
        this.operList = operList;
        for (int i = 0; i < operList.size(); i++) {
            checkStatus.put(i, false);
        }
        notifyDataSetChanged();
    }

    public void selectAll() {
        checkedList.addAll(operList);
        for (int i = 0; i < operList.size(); i++) {
            checkStatus.put(i, true);
        }
        notifyDataSetChanged();
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull BackDataAdapter.BackDataViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        Log.e("tag", "隐藏item=" + holder.getAdapterPosition());
        holder.taskItemEdtBackNum.removeTextChangedListener(textWatcher);
        holder.taskItemEdtBackNum.clearFocus();

        if (backNumPos == holder.getAdapterPosition()) {
            inputMethodManager.hideSoftInputFromWindow(holder.taskItemEdtBackNum.getWindowToken(), 0);
        }
    }

    @Override
    public void onViewAttachedToWindow(@NonNull BackDataAdapter.BackDataViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        Log.e("tag", "显示item=" + holder.getAdapterPosition());
        holder.taskItemEdtBackNum.addTextChangedListener(textWatcher);
        if (backNumPos == holder.getAdapterPosition()) {
            holder.taskItemEdtBackNum.requestFocus();
            holder.taskItemEdtBackNum.setSelection(holder.taskItemEdtBackNum.getText().length());
        }
    }

    private TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
//            Log.i(TAG, "onTextChanged s: " + s);
//            if (!TextUtils.isEmpty(s)) {
//                int num = Integer.parseInt(s.toString());
//                if (num > Integer.parseInt(getNumAry.get(backNumPos))) {//输入数量大于库存数量
//                    ToastUtil.showShort("超出领出子弹数量");
//                    backDataViewHolder.taskItemEdtBackNum.setText("");
//                    return;
//                }
//            }
        }

        @Override
        public void afterTextChanged(Editable s) {
            Log.e(TAG, "afterTextChanged s: " + s);
            backNumAry.put(backNumPos, s.toString());
        }
    };

    @Override
    public BackDataViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        inputMethodManager = (InputMethodManager)
                mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_back_gun,
                parent, false);
        return new BackDataViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final BackDataViewHolder holder, int position) {
//        backDataViewHolder = holder;
        final int pos = position + index * pageCount;
        Log.i(TAG, "onBindViewHolder pos: " + pos + " position: " + position + "  index: " + index);
        holder.taskItemCbBack.setOnCheckedChangeListener(null);
        if (checkStatus != null && !checkStatus.isEmpty() && checkStatus.containsKey(pos)) {
            holder.taskItemCbBack.setChecked(checkStatus.get(pos));
        } else {
            Log.i(TAG, "onBindViewHolder checkStatus is not containskey position: " + pos);
        }

        if((pos&1)==1){
            holder.taskItemRoot.setBackgroundColor(Color.parseColor("#FF0D47A1"));
        }else{
            holder.taskItemRoot.setBackgroundColor(Color.parseColor("#FF01579B"));
        }

        if (isDisableAll) {
            holder.taskItemCbBack.setClickable(false);
            holder.taskItemCbBack.setEnabled(false);
        } else {
            holder.taskItemCbBack.setClickable(true);
            holder.taskItemCbBack.setEnabled(true);
        }

        final UrgentGetListBean urgentTaskInfoBean = operList.get(pos);
        int subCabNo = urgentTaskInfoBean.getLocationNo(); //所在位置编号
        final int operNumber = urgentTaskInfoBean.getOutObjectNumber(); //物件数量
        String typeName = urgentTaskInfoBean.getObjectType();//类型
        String gunNo = urgentTaskInfoBean.getGunNo();
        final String positionType = urgentTaskInfoBean.getLocationType();
        if (positionType.equals(Constants.TYPE_AMMO)) { //枪支归还隐藏 填写归还数量
            holder.taskItemRlBackNum.setVisibility(View.VISIBLE);
        } else {
            holder.taskItemRlBackNum.setVisibility(View.GONE);
        }
        holder.taskItemTvNo.setText(gunNo);
//        holder.taskItemEdtBackNum.setText("");
        if (getNumAry.size() > 0 && !TextUtils.isEmpty(getNumAry.get(pos))) {
            holder.taskItemTvObjectNum.setText(getNumAry.get(pos));
        } else {
            getNumAry.put(pos, String.valueOf(operNumber));
            holder.taskItemTvObjectNum.setText(String.valueOf(operNumber));
        }
        //监听数量输入
        if (!TextUtils.isEmpty(backNumAry.get(pos))) {
            holder.taskItemEdtBackNum.setText(backNumAry.get(pos));
        }

        holder.taskItemTvObjectType.setText(typeName);
//        holder.taskItemTvObjectNum.setText(String.valueOf(operNumber));
        holder.taskItemTvPosition.setText(String.valueOf(subCabNo));

        holder.taskItemEdtBackNum.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (hasFocus) {
                    backNumPos = pos;
                    Log.e("tag", "backNumPos 焦点选中" + backNumPos);
                }
            }
        });
        holder.taskItemCbBack.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                checkStatus.put(pos, isChecked);
                if (isChecked) {
                    //选中添加到集合
                    if (!positionType.equals(Constants.TYPE_AMMO)) {
//                        Log.i(TAG, "onCheckedChanged operBean: " + JSON.toJSONString(urgentTaskInfoBean));
                        checkedList.add(urgentTaskInfoBean);
                    } else {
                        String backNum = holder.taskItemEdtBackNum.getText().toString();
                        Log.i("backdataadapter", "onCheckedChanged backNum: " + backNum);
                        if (TextUtils.isEmpty(backNum)) {
                            ToastUtil.showShort("请输入归还数量");
                            holder.taskItemCbBack.setChecked(false);
                            return;
                        }
                        int intBackNum = Integer.parseInt(backNum);
                        if (intBackNum > operNumber) {
                            ToastUtil.showShort("请输入正确的归还数量");
                            holder.taskItemCbBack.setChecked(false);
                            return;
                        }

                        urgentTaskInfoBean.setOutObjectNumber(intBackNum);
                        checkedList.add(urgentTaskInfoBean);
                        holder.taskItemEdtBackNum.setEnabled(false);
                        Log.i(TAG, "onCheckedChanged operBean: " + JSON.toJSONString(urgentTaskInfoBean));
                    }
                } else {
                    //取消选中从集合中移除
                    checkedList.remove(urgentTaskInfoBean);
                    holder.taskItemEdtBackNum.setEnabled(true);
                }
            }
        });
        holder.taskItemEdtBackNum.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.i("urgentBackData", "onTextChanged  s: " + s.toString());
                try {
                    if (!TextUtils.isEmpty(s)) {
                        int num = Integer.parseInt(s.toString());
                        if (num > Integer.parseInt(getNumAry.get(pos))) {//输入数量大于库存数量
                            if (!isConfirm) {
                                ToastUtil.showShort("超出领出子弹数量");
                                holder.taskItemEdtBackNum.setText("");
                            }
                            return;
                        }
                    }
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
//                backNumAry.put(pos, s.toString());
            }
        });
    }

    @Override
    public int getItemCount() {
        int current = index * pageCount;
        return operList.size() - current < pageCount ? operList.size() - current : pageCount;
    }

    /**
     * 禁用checkbox选中
     */
    public void setDisableAll() {
        isDisableAll = true;
        notifyDataSetChanged();
    }

    static class BackDataViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.task_item_tv_object_type)
        TextView taskItemTvObjectType;
        @BindView(R.id.task_item_tv_object_num)
        TextView taskItemTvObjectNum;
        @BindView(R.id.task_item_tv_position)
        TextView taskItemTvPosition;
        @BindView(R.id.task_item_tv_no)
        TextView taskItemTvNo;
        @BindView(R.id.task_item_tv_task_status)
        TextView taskItemTvTaskStatus;
        @BindView(R.id.task_item_cb_back)
        CheckBox taskItemCbBack;
        @BindView(R.id.task_item_root)
        LinearLayout taskItemRoot;
        @BindView(R.id.task_item_rl_back_num)
        RelativeLayout taskItemRlBackNum;
        @BindView(R.id.task_item_edt_back_num)
        EditText taskItemEdtBackNum;

        BackDataViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }
}
