package com.zack.xjht.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zack.xjht.R;
import com.zack.xjht.entity.UserBean;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class UserListAdapter extends RecyclerView.Adapter<UserListAdapter.UserListViewHolder> {
    private int index;
    private int pageCount;
    private List<UserBean> list;
    private UserBean selectUser;
    private int selectPos = 0;

    public UserListAdapter(int index, int pageCount, List<UserBean> list) {
        this.index = index;
        this.pageCount = pageCount;
        this.list = list;
    }

    public UserBean getSelectUser() {
        return selectUser;
    }

    @NonNull
    @Override
    public UserListAdapter.UserListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.user_list_item, parent, false);
        return new UserListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final UserListAdapter.UserListViewHolder holder, int position) {
        final int pos = position + index * pageCount;
        final UserBean userBean = list.get(pos);
        String userName = userBean.getUserName();
        String policeNo = userBean.getPoliceNo();
        String deptName = userBean.getDeptName();
        holder.userListItemUserName.setText(userName);
        holder.userListItemUserNo.setText(policeNo);
        holder.userListItemUserSex.setText(deptName);

        holder.userListClRootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                selectUser =userBean;
                selectPos = pos;
                notifyDataSetChanged();
            }
        });

        if (selectPos == pos) {
            selectUser = userBean;
            holder.userListItemIvSelect.setVisibility(View.VISIBLE);
            holder.userListClRootView.setBackgroundResource(R.color.colorPrimary);
        } else {
            holder.userListItemIvSelect.setVisibility(View.INVISIBLE);
            holder.userListClRootView.setBackground(null);
        }
    }

    @Override
    public int getItemCount() {
        int current = index * pageCount;
        return list.size() - current < pageCount ? list.size() - current : pageCount;
    }

    public void setIndex(int index) {
        this.index = index;
        notifyDataSetChanged();
    }

    static class UserListViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.user_list_item_user_name)
        TextView userListItemUserName;
        @BindView(R.id.user_list_item_user_no)
        TextView userListItemUserNo;
        @BindView(R.id.user_list_item_user_sex)
        TextView userListItemUserSex;
        @BindView(R.id.user_list_cl_root_view)
        LinearLayout userListClRootView;
        @BindView(R.id.user_list_item_iv_select)
        ImageView userListItemIvSelect;

        UserListViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

    }
}
