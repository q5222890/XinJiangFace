package com.zack.xjht.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.yanzhenjie.nohttp.rest.Response;
import com.zack.xjht.R;
import com.zack.xjht.Utils.LogUtil;
import com.zack.xjht.Utils.SharedUtils;
import com.zack.xjht.Utils.ToastUtil;
import com.zack.xjht.adapter.UserListAdapter;
import com.zack.xjht.db.DBManager;
import com.zack.xjht.entity.UserBean;
import com.zack.xjht.entity.UserBiosBean;
import com.zack.xjht.http.HttpClient;
import com.zack.xjht.http.HttpListener;
import com.zack.xjht.ui.dialog.FingerDialog;
import com.zack.xjht.ui.dialog.IrisDialog;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class UserActivity extends BaseActivity {
    private static final String TAG = "UserActivity";

    @BindView(R.id.user_btn_pre_page)
    Button userBtnPrePage;
    @BindView(R.id.user_tv_cur_page)
    TextView userTvCurPage;
    @BindView(R.id.user_btn_next_page)
    Button userBtnNextPage;
    @BindView(R.id.user_list_tv_tittle)
    TextView userListTvTittle;
    @BindView(R.id.user_ll_tittle)
    LinearLayout userLlTittle;
    @BindView(R.id.user_rv_user_list)
    RecyclerView userRvUserList;
    @BindView(R.id.user_rl_list)
    RelativeLayout userRlList;
    @BindView(R.id.user_rl_char)
    RelativeLayout userRlChar;
    @BindView(R.id.user_ll_bottom)
    LinearLayout userLlBottom;
    @BindView(R.id.user_btn_enroll_finger)
    ImageView userBtnEnrollFinger;
    @BindView(R.id.user_btn_enroll_vein)
    Button userBtnEnrollVein;
    @BindView(R.id.user_btn_enroll_iris)
    Button userBtnEnrollIris;
    @BindView(R.id.user_btn_enroll_face)
    ImageView userBtnEnrollFace;
    //    private List<PoliceBiosBean> policeBiosBeanList = new ArrayList<>();
    private int index = 0;
    private int pageCount = 8;
    public List<UserBean> userList = new ArrayList<>();
    //    public MembersBean curPolice;
    public int selectedPosition;
    private UserListAdapter userListAdapter;
    private UserBean userBean;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);
        ButterKnife.bind(this);

        userBtnPrePage.setVisibility(View.INVISIBLE);
        userBtnNextPage.setVisibility(View.INVISIBLE);

        userBean = (UserBean) getIntent().getSerializableExtra("firstPoliceInfo");
        if (userBean != null) {
            Log.i(TAG, "onCreate  username: " + userBean.getUserName());
        }

        LinearLayoutManager llm = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        userRvUserList.setLayoutManager(llm);

        userListAdapter = new UserListAdapter(index, pageCount, userList);
        userRvUserList.setAdapter(userListAdapter);

        if(SharedUtils.getIsServerOnline()){
            getUserList();
            getCharList();
        }

        DBManager.getInstance().insertCommLog(this, userBean,
                userBean.getUserName() + "进入特征管理");

    }

    private void getCharList() {
        HttpClient.getInstance().getCharList(this, "", new HttpListener<String>() {
            @Override
            public void onSucceed(int what, Response<String> response) throws JSONException {
                LogUtil.i(TAG, "onSucceed getCharList response: " + response.get());
                try {
                    if (!TextUtils.isEmpty(response.get())) {
                        List<UserBiosBean> userBiosBeans = JSON.parseArray(response.get(), UserBiosBean.class);
                        if (!userBiosBeans.isEmpty()) {

                        } else {
                            ToastUtil.showShort("获取生物特征数据为空");
                        }
                    } else {
                        ToastUtil.showShort("获取生物特征数据为空");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    ToastUtil.showShort("请求数据错误，获取生物特征失败！");
                }
            }

            @Override
            public void onFailed(int what, Response<String> response) {
                ToastUtil.showShort("网络错误，获取生物特征失败！");
            }
        });
    }

    private void getUserList() {
        HttpClient.getInstance().getUserList(this, "", new HttpListener<String>() {
            @Override
            public void onSucceed(int what, Response<String> response) throws JSONException {
                Log.i(TAG, "onSucceed getUserList response: " + response.get());
                try {
                    if (!TextUtils.isEmpty(response.get())) {
                        List<UserBean> userBeans = JSON.parseArray(response.get(), UserBean.class);
                        if (!userBeans.isEmpty()) {
                            userList.clear();
                            userList.addAll(userBeans);
                            userListAdapter.notifyDataSetChanged();
                            initPreNextBtn();
                            for (UserBean user : userBeans) {
                                int userId = user.getUserId();
//                                getUserRole(userId);
                            }
                        } else {
                            ToastUtil.showShort("获取人员数据为0");
                        }
                    } else {
                        ToastUtil.showShort("获取人员数据为空");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    ToastUtil.showShort("发生错误，获取人员数据失败！");
                }
            }

            @Override
            public void onFailed(int what, Response<String> response) {
                ToastUtil.showShort("网络错误，获取数据失败！");
            }
        });
    }

    private void getUserRole(int userId) {
        HttpClient.getInstance().getUserRole(this, userId, new HttpListener<String>() {
            @Override
            public void onSucceed(int what, Response<String> response) throws JSONException {
                Log.i(TAG, "onSucceed getRoleByUserId response: " + response.get());
            }

            @Override
            public void onFailed(int what, Response<String> response) {

            }
        });
    }

    private void initPreNextBtn() {
        if (userList.isEmpty()) {
            userTvCurPage.setText(index + 1 + "/1");
        } else {
            if (userList.size() <= pageCount) {
                userBtnNextPage.setVisibility(View.INVISIBLE);
            } else {
                userBtnNextPage.setVisibility(View.VISIBLE);
            }
            userTvCurPage.setText(index + 1 + "/" + (int) Math.ceil((double) userList.size() / pageCount));
        }
    }

    @OnClick({R.id.user_btn_enroll_vein, R.id.user_btn_enroll_iris,
            R.id.user_btn_pre_page, R.id.user_btn_next_page,
            R.id.user_btn_enroll_finger, R.id.user_btn_enroll_face})
    public void onViewClicked(View view) {
        super.onViewClicked(view);
        UserBean selectUser = userListAdapter.getSelectUser();
        switch (view.getId()) {
            case R.id.user_btn_pre_page://上一页
                prePager();
                break;
            case R.id.user_btn_next_page://下一页
                nexPager();
                break;
            case R.id.user_btn_enroll_finger: //注册指纹
                if (userListAdapter != null) {
                    if (selectUser != null) {
                        //注册指纹
                        FingerDialog fingerDialog = new FingerDialog(UserActivity.this, selectUser);
                        fingerDialog.show();
                    }
                }
                break;
            case R.id.user_btn_enroll_face://注册人脸
                ///注册人脸
                if(selectUser !=null){
                    Intent intent = new Intent(UserActivity.this, RegisterFaceActivity.class);
                    intent.putExtra("user", selectUser);
                    startActivity(intent);
                }else{
                    ToastUtil.showShort("请先选择人员！");
                }
                break;
            case R.id.user_btn_enroll_iris://注册虹膜
                try {
                    if (userListAdapter != null) {
                        if (selectUser != null) {
                            IrisDialog irisDialog = new IrisDialog(UserActivity.this, selectUser);
                            irisDialog.show();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
        }
    }

    /**
     * 上一页
     */
    private void prePager() {
        index--;
        userListAdapter.setIndex(index);
        userTvCurPage.setText(index + 1 + "/" + (int) Math.ceil((double) userList.size() / pageCount));
        //隐藏上一个或下一个按钮
        checkButton();
    }

    /**
     * 下一页
     */
    private void nexPager() {
        index++;
        userListAdapter.setIndex(index);
        userTvCurPage.setText(index + 1 + "/" + (int) Math.ceil((double) userList.size() / pageCount));
        //隐藏上一个或下一个按钮
        checkButton();
    }

    /**
     * button显示和隐藏
     */
    private void checkButton() {
        if (index <= 0) {
            userBtnPrePage.setVisibility(View.INVISIBLE);
            userBtnNextPage.setVisibility(View.VISIBLE);
        } else if (userList.size() - index * pageCount <= pageCount) {    //数据总数减每页数当小于每页可显示的数字时既是最后一页
            userBtnPrePage.setVisibility(View.VISIBLE);
            userBtnNextPage.setVisibility(View.INVISIBLE);
        } else {
            userBtnNextPage.setVisibility(View.VISIBLE);
            userBtnPrePage.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy : ");
        if (userBean != null) {
//            CommonLogBean commonLogBean = new CommonLogBean();
//            commonLogBean.setAddTime(System.currentTimeMillis());
//            commonLogBean.setUserId(String.valueOf(userBean.getUserId()));
//            commonLogBean.setUserName(userBean.getUserName());
//            commonLogBean.setContent(userBean.getUserName() + "进入特征管理");
//            long insert = commonLogBeanDao.insert(commonLogBean);
            DBManager.getInstance().insertCommLog(this, userBean,
                    userBean.getUserName() + "退出特征管理");
        }

    }
}
