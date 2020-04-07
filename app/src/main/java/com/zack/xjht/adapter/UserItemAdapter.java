package com.zack.xjht.adapter;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.zack.xjht.R;
import com.zack.xjht.entity.UserBean;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class UserItemAdapter extends RecyclerView.Adapter<UserItemAdapter.UserItemViewHolder> {
    private static final String TAG = "UserItemAdapter";
    private List<UserBean> list;
    private int selectPosition =-1;
    private UserBean selectUser;

    public UserItemAdapter(List<UserBean> list) {
        this.list = list;
    }

    public void setList(List<UserBean> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    public int getSelectPosition() {
        return selectPosition;
    }

    public UserBean getSelectUser() {
        return selectUser;
    }

    public void setSelectPosition(int selectPosition) {
        this.selectPosition = selectPosition;
        notifyDataSetChanged();
    }

    public void setSelectUser(UserBean selectUser) {
        this.selectUser = selectUser;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public UserItemViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(
                R.layout.user_item, viewGroup, false);
        return new UserItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserItemViewHolder holder, final int position) {
        final UserBean userBean = list.get(position);
        Log.i(TAG, "onBindViewHolder user: "+ JSON.toJSONString(userBean));
        final String userName = userBean.getUserName();
        holder.userItemTvName.setText(userName);
        holder.userItemTvName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectPosition =position;
                selectUser =userBean;
                notifyDataSetChanged();
                Log.i(TAG, "onClick user name: "+userName);
            }
        });

        if(selectPosition ==position){
            holder.userItemTvName.setBackgroundColor(Color.parseColor("#4CAF50"));
        }else{
            holder.userItemTvName.setBackground(null);
        }

        //设置背景色
//        if ((position & 1) == 1) {
//            holder.userItemTvName.setBackgroundColor(Color.parseColor("#FF0D47A1"));
//        } else {
//            holder.userItemTvName.setBackgroundColor(Color.parseColor("#FF01579B"));
//        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class UserItemViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.user_item_tv_name)
        TextView userItemTvName;
        public UserItemViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
