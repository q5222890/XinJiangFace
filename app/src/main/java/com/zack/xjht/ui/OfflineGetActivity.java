package com.zack.xjht.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.zack.xjht.Constants;
import com.zack.xjht.R;
import com.zack.xjht.Utils.LogUtil;
import com.zack.xjht.Utils.SharedUtils;
import com.zack.xjht.Utils.ToastUtil;
import com.zack.xjht.adapter.OfflineDataAdapter;
import com.zack.xjht.adapter.UserItemAdapter;
import com.zack.xjht.db.DBManager;
import com.zack.xjht.db.gen.SubCabBeanDao;
import com.zack.xjht.db.gen.UserBeanDao;
import com.zack.xjht.entity.SubCabBean;
import com.zack.xjht.entity.UserBean;
import com.zack.xjht.event.EventConsts;
import com.zack.xjht.event.MessageEvent;
import com.zack.xjht.serial.SerialPortUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class OfflineGetActivity extends BaseActivity {
    private static final String TAG = "OfflineGetActivity";

    @BindView(R.id.offline_get_ll_tittle)
    LinearLayout offlineGetLlTittle;
    @BindView(R.id.offline_get_recycler_view)
    RecyclerView offlineGetRecyclerView;
    @BindView(R.id.offline_get_btn_pre_page)
    Button offlineGetBtnPrePage;
    @BindView(R.id.offline_get_tv_cur_page)
    TextView offlineGetTvCurPage;
    @BindView(R.id.offline_get_btn_next_page)
    Button offlineGetBtnNextPage;
    @BindView(R.id.offline_get_btn_open_door)
    Button offlineGetBtnOpenDoor;
    @BindView(R.id.offline_get_btn_unlock)
    Button offlineGetBtnUnlock;
    @BindView(R.id.offline_get_btn_finish)
    Button offlineGetBtnFinish;
    @BindView(R.id.offline_get_ll_fun)
    LinearLayout offlineGetLlFun;
    @BindView(R.id.offline_get_btn_confirm)
    Button offlineGetBtnConfirm;
    @BindView(R.id.offline_get_tv_gun_no)
    TextView offlineGetTvGunNo;
    @BindView(R.id.offline_get_tv_count)
    TextView offlineGetTvCount;
    @BindView(R.id.offline_get_tv_get_num)
    TextView offlineGetTvGetNum;
    @BindView(R.id.offline_get_rv_police)
    RecyclerView offlineGetRvPolice;
    @BindView(R.id.offline_get_ll_police)
    LinearLayout offlineGetLlPolice;
    private SubCabBeanDao subCabBeanDao;
    private List<SubCabBean> subCabBeanList = new ArrayList<>();
    private int pageCount = 8;
    private int index = 0;
    private OfflineDataAdapter offlineDataAdapter;
    private UserBean apply;
    private UserBean approve;
    private boolean isQueryStatus;
    private List<UserBean> userList = new ArrayList<>();
    private UserBeanDao userBeanDao;
    private UserItemAdapter userItemAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offline_get);
        ButterKnife.bind(this);
        Constants.isCheckGunStatus = false;
        Constants.isIllegalGetGunAlarm = true;
        Constants.isIllegalOpenCabAlarm = true;
        //置为正在任务操作
        Constants.isExecuteTask = true;

        offlineGetBtnPrePage.setVisibility(View.INVISIBLE);
        offlineGetBtnNextPage.setVisibility(View.INVISIBLE);
        offlineGetBtnFinish.setVisibility(View.GONE);

        if (SharedUtils.getCabType().equals(Constants.TYPE_AMMO_CAB)) {
            //弹柜
            offlineGetTvGunNo.setVisibility(View.GONE);
            offlineGetTvGetNum.setVisibility(View.VISIBLE);
            offlineGetTvCount.setVisibility(View.VISIBLE);
        } else {
            //枪柜
            offlineGetTvGunNo.setVisibility(View.VISIBLE);
            offlineGetTvGetNum.setVisibility(View.GONE);
            offlineGetTvCount.setVisibility(View.GONE);
        }

        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }

        initData();
    }

    private void initData() {
        userBeanDao = DBManager.getInstance().getUserBeanDao();
        subCabBeanDao = DBManager.getInstance().getSubCabBeanDao();
        List<SubCabBean> subCabBeans = subCabBeanDao.loadAll();
        if (!subCabBeans.isEmpty()) {
            for (SubCabBean subCabBean : subCabBeans) {
                String gunState = subCabBean.getGunState();
                String locationType = subCabBean.getLocationType();
                String isUse = subCabBean.getIsUse();
                int objectNumber = subCabBean.getObjectNumber();
                if (!TextUtils.isEmpty(isUse) && isUse.equals("yes")) {
                    //子弹数大于0
                    if(!TextUtils.isEmpty(locationType)){
                        if (locationType.equals(Constants.TYPE_AMMO)) {//子弹或弹夹
                            if (objectNumber > 0) {
                                subCabBeanList.add(subCabBean);
                            }
                        } else {
                            //枪支在库添加到列表
                            if (!TextUtils.isEmpty(gunState) && gunState.equals("in")) {
                                subCabBeanList.add(subCabBean);
                            }
                        }
                    }
                }
            }
            LogUtil.i(TAG, "initData subCabBeanList: "+JSON.toJSONString(subCabBeanList));
        }
        Collections.sort(subCabBeanList);
        initPreNextBtn();

        userList = userBeanDao.loadAll();
//        LogUtil.i(TAG, "initData userList: " + JSON.toJSONString(userList));
        userItemAdapter = new UserItemAdapter(userList);
        offlineGetRvPolice.setLayoutManager(new LinearLayoutManager(
                this, LinearLayout.VERTICAL, false));
        offlineGetRvPolice.setAdapter(userItemAdapter);

        offlineGetRecyclerView.setLayoutManager(new LinearLayoutManager(this,
                LinearLayout.VERTICAL, false));
        offlineDataAdapter = new OfflineDataAdapter(subCabBeanList, index, pageCount, userItemAdapter);
        offlineGetRecyclerView.setAdapter(offlineDataAdapter);
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void onPostSuccessEvent(MessageEvent event) {
        EventBus.getDefault().removeStickyEvent(event);
        offlineDataAdapter.setDisableAll();
        String message = event.getMessage();
        apply = event.getApply();
        approve = event.getApprove();
        Log.i(TAG, "onPostSuccessEvent message: " + message + "  申请人:" + apply.getUserName()
                + " 审批人:" + approve.getUserName());
        if (message.equals(EventConsts.EVENT_POST_SUCCESS)) {
            //提交成功
            Log.i(TAG, "onPostSuccessEvent 提交成功: ");
            offlineGetBtnOpenDoor.setVisibility(View.VISIBLE);
            offlineGetBtnUnlock.setVisibility(View.VISIBLE);
            offlineGetBtnFinish.setVisibility(View.VISIBLE);
            offlineGetBtnConfirm.setVisibility(View.GONE);
            isQueryStatus = true;
            String cabType = SharedUtils.getCabType();
            if (cabType.equals(Constants.TYPE_AMMO_CAB)) {
                //领取弹药和归还弹药
                offlineGetBtnOpenDoor.setText("打开弹柜");
                offlineGetBtnUnlock.setText("打开弹仓");
            }
            //查询枪支状态
            if (!cabType.equals(Constants.TYPE_AMMO_CAB)) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Log.i(TAG, "run isQueryStatus : " + isQueryStatus);
                        while (isQueryStatus) {
                            List<SubCabBean> selectedList = offlineDataAdapter.getSelectList();
                            if (!selectedList.isEmpty()) {
                                for (SubCabBean subCabBean : selectedList) {
                                    int locationNo = subCabBean.getLocationNo();
                                    SerialPortUtil.getInstance().checkStatus(locationNo);
                                    Log.i(TAG, "run 查询: " + locationNo + "号枪锁状态");
                                    try {
                                        Thread.sleep(200);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                    if (!isQueryStatus) {
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }).start();
            }
        }
    }

    private void initPreNextBtn() {
        if (subCabBeanList.isEmpty()) {
            offlineGetTvCurPage.setText(index + 1 + "/1");
        } else {
            if (subCabBeanList.size() <= pageCount) {
                offlineGetBtnNextPage.setVisibility(View.INVISIBLE);
            } else {
                offlineGetBtnNextPage.setVisibility(View.VISIBLE);
            }
            offlineGetTvCurPage.setText(index + 1 + "/" + (int) Math.ceil((double) subCabBeanList.size() / pageCount));
        }
    }

    private void prePager() {
        index--;
        offlineDataAdapter.setIndex(index);
        offlineGetTvCurPage.setText(index + 1 + "/" + (int) Math.ceil((double) subCabBeanList.size() / pageCount));
        //隐藏上一个或下一个按钮
        checkButton();
//        Log.i(TAG, "prePager index: " + index);
    }

    private void nexPager() {
        index++;
        offlineDataAdapter.setIndex(index);
        offlineGetTvCurPage.setText(index + 1 + "/" + (int) Math.ceil((double) subCabBeanList.size() / pageCount));
        //隐藏上一个或下一个按钮
        checkButton();
//        Log.i(TAG, "nexPager index: " + index);
    }

    private void checkButton() {
        if (index <= 0) {
            offlineGetBtnPrePage.setVisibility(View.INVISIBLE);
            offlineGetBtnNextPage.setVisibility(View.VISIBLE);
        } else if (subCabBeanList.size() - index * pageCount <= pageCount) {    //数据总数减每页数当小于每页可显示的数字时既是最后一页
            offlineGetBtnPrePage.setVisibility(View.VISIBLE);
            offlineGetBtnNextPage.setVisibility(View.INVISIBLE);
        } else {
            offlineGetBtnPrePage.setVisibility(View.VISIBLE);
            offlineGetBtnNextPage.setVisibility(View.VISIBLE);
        }
    }

    @OnClick({R.id.offline_get_btn_pre_page, R.id.offline_get_btn_next_page,
            R.id.offline_get_btn_open_door, R.id.offline_get_btn_unlock,
            R.id.offline_get_btn_finish, R.id.offline_get_btn_confirm})
    public void onViewClicked(View view) {
        List<SubCabBean> selectList;
        switch (view.getId()) {
            case R.id.offline_get_btn_pre_page://上一页
                prePager();
                break;
            case R.id.offline_get_btn_next_page://下一页
                nexPager();
                break;
            case R.id.offline_get_btn_open_door://开柜门
                selectList = offlineDataAdapter.getSelectList();
                if (selectList.isEmpty()) {
                    ToastUtil.showShort("请选择枪弹");
                    return;
                }
                SerialPortUtil.getInstance().openLock(SharedUtils.getLeftCabNo());
                break;
            case R.id.offline_get_btn_unlock://开枪锁
                selectList = offlineDataAdapter.getSelectList();
                if (selectList.isEmpty()) {
                    ToastUtil.showShort("请选择枪弹");
                    return;
                }
                for (SubCabBean subCabBean : selectList) {
                    int locationNo = subCabBean.getLocationNo();

                    SerialPortUtil.getInstance().openLock(locationNo);
                    try {
                        Thread.sleep(300);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case R.id.offline_get_btn_confirm://确认领取
                selectList = offlineDataAdapter.getSelectList();
                if (selectList.isEmpty()) {
                    ToastUtil.showShort("请选择枪弹");
                    return;
                }
                String jsonBody = JSON.toJSONString(selectList);
                Intent intent = new Intent(this, VerifyActivity.class);
                intent.putExtra("activity", Constants.ACTIVITY_OFFLINE_GET);
                intent.putExtra("data", jsonBody);
                startActivity(intent);
                break;
            case R.id.ac_top_back:
            case R.id.offline_get_btn_finish:
                finish();
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        isQueryStatus = false;
        Constants.isCheckGunStatus = true;
        Constants.isIllegalGetGunAlarm = false;
        Constants.isIllegalOpenCabAlarm = false;
        //置为正在任务操作
        Constants.isExecuteTask = false;
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }
}
