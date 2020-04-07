package com.zack.xjht.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.zack.xjht.Constants;
import com.zack.xjht.R;
import com.zack.xjht.Utils.LogUtil;
import com.zack.xjht.Utils.SharedUtils;
import com.zack.xjht.Utils.ToastUtil;
import com.zack.xjht.adapter.OfflineTaskItemAdapter;
import com.zack.xjht.adapter.OfflineTaskListAdapter;
import com.zack.xjht.db.DBManager;
import com.zack.xjht.db.gen.OfflineTaskDao;
import com.zack.xjht.db.gen.OfflineTaskItemDao;
import com.zack.xjht.entity.OfflineTask;
import com.zack.xjht.entity.OfflineTaskItem;
import com.zack.xjht.entity.SubCabBean;
import com.zack.xjht.entity.UserBean;
import com.zack.xjht.event.EventConsts;
import com.zack.xjht.event.MessageEvent;
import com.zack.xjht.serial.SerialPortUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class OfflineBackActivity extends BaseActivity {
    private static final String TAG = "OfflineBackActivity";

    @BindView(R.id.ac_top_date_img)
    ImageView acTopDateImg;
    @BindView(R.id.ac_top_date_txt)
    TextView acTopDateTxt;
    @BindView(R.id.ac_top_net_txt)
    TextView acTopNetTxt;
    @BindView(R.id.ac_top_power_txt)
    TextView acTopPowerTxt;
    @BindView(R.id.ac_top_temper_txt)
    TextView acTopTemperTxt;
    @BindView(R.id.ac_top_humidity_txt)
    TextView acTopHumidityTxt;
    @BindView(R.id.ac_top_back)
    ImageView acTopBack;
    @BindView(R.id.offline_back_rv_task_list)
    RecyclerView offlineBackRvTaskList;
    @BindView(R.id.offline_back_ll_title)
    LinearLayout offlineBackLlTitle;
    @BindView(R.id.offline_back_rv_task_item)
    RecyclerView offlineBackRvTaskItem;
    @BindView(R.id.offline_back_btn_pre_page)
    Button offlineBackBtnPrePage;
    @BindView(R.id.offline_back_tv_cur_page)
    TextView offlineBackTvCurPage;
    @BindView(R.id.offline_back_btn_next_page)
    Button offlineBackBtnNextPage;
    @BindView(R.id.offline_back_btn_open_door)
    Button offlineBackBtnOpenDoor;
    @BindView(R.id.offline_back_btn_unlock)
    Button offlineBackBtnUnlock;
    @BindView(R.id.offline_back_btn_confirm)
    Button offlineBackBtnConfirm;
    @BindView(R.id.offline_back_btn_finish)
    Button offlineBackBtnFinish;
    @BindView(R.id.offline_back_ll_fun)
    LinearLayout offlineBackLlFun;
    @BindView(R.id.offline_back_tv_title_gun_no)
    TextView offlineBackTvTitleGunNo;
    @BindView(R.id.offline_back_tv_title_get_num)
    TextView offlineBackTvTitleGetNum;
    @BindView(R.id.offline_back_tv_title_back_num)
    TextView offlineBackTvTitleBackNum;

    private List<OfflineTask> offlineTaskList = new ArrayList<>();
    private List<OfflineTaskItem> offlineTaskItems = new ArrayList<>();
    private OfflineTaskDao offlineTaskDao;
    private OfflineTaskItemDao offlineTaskItemDao;
    private OfflineTaskListAdapter offlineTaskListAdapter;
    private OfflineTaskItemAdapter offlineTaskItemAdapter;
    private OfflineTask selectOfflineTask;
    private int pageCount = 8;
    private int index = 0;
    private UserBean apply,approve;
    private boolean isQueryStatus =false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offline_back);
        ButterKnife.bind(this);

        Constants.isCheckGunStatus = false;
        Constants.isIllegalGetGunAlarm = true;
        Constants.isIllegalOpenCabAlarm =true;
        //置为正在任务操作
        Constants.isExecuteTask =true;

        offlineBackBtnPrePage.setVisibility(View.INVISIBLE);
        offlineBackBtnNextPage.setVisibility(View.INVISIBLE);
        offlineBackBtnUnlock.setVisibility(View.GONE);
        offlineBackBtnOpenDoor.setVisibility(View.GONE);
        offlineBackBtnFinish.setVisibility(View.GONE);
        if (SharedUtils.getCabType().equals(Constants.TYPE_AMMO_CAB)) {
            offlineBackTvTitleGunNo.setVisibility(View.GONE);
            offlineBackTvTitleGetNum.setVisibility(View.VISIBLE);
            offlineBackTvTitleBackNum.setVisibility(View.VISIBLE);
        }else{
            offlineBackTvTitleGunNo.setVisibility(View.VISIBLE);
            offlineBackTvTitleGetNum.setVisibility(View.GONE);
            offlineBackTvTitleBackNum.setVisibility(View.GONE);
        }

        offlineTaskDao = DBManager.getInstance().getOfflineTaskDao();
        offlineTaskItemDao = DBManager.getInstance().getOfflineTaskItemDao();
        initView();
    }

    private void initView() {
        offlineTaskList = offlineTaskDao.queryBuilder()
                .where(OfflineTaskDao.Properties.TaskStatus.eq(1)).list();
        offlineBackRvTaskList.setLayoutManager(new LinearLayoutManager(this));
        offlineBackRvTaskItem.setLayoutManager(new LinearLayoutManager(this));

        offlineTaskItemAdapter =new OfflineTaskItemAdapter(offlineTaskItems, index, pageCount);
        offlineBackRvTaskItem.setAdapter(offlineTaskItemAdapter);
        offlineTaskListAdapter = new OfflineTaskListAdapter(offlineTaskList,
                new OfflineTaskListAdapter.OnTaskSelectListener() {
            @Override
            public void onTaskSelect(OfflineTask offlineTask) {
                Log.i(TAG, "onTaskSelect: "+JSON.toJSONString(offlineTask));
                selectOfflineTask = offlineTask;
//                offlineTask.setTaskStatus(2);
//                offlineTaskDao.update(offlineTask);
                //获取领取枪弹数据明细
                Long id = offlineTask.getId();
                List<OfflineTaskItem> offlineTaskItemList= offlineTaskItemDao.queryBuilder().where(
                        OfflineTaskItemDao.Properties.TaskId.eq(id)).list();
                offlineTaskItems.clear();
                LogUtil.i(TAG, "onTaskSelect offlineTaskItemList: "+JSON.toJSONString(offlineTaskItemList));
                if(!offlineTaskItemList.isEmpty()){
                    int finishSize =0;
                    for (OfflineTaskItem offlineTaskItem:offlineTaskItemList){
                        int status = offlineTaskItem.getStatus();
                        if(status ==1){//已领取
                            offlineTaskItems.add(offlineTaskItem);
                        }else if(status ==2){
                            finishSize++;
                        }
                    }
                    if(finishSize == offlineTaskItemList.size()){
                        offlineTask.setTaskStatus(2);
                        offlineTask.setFinishTime(System.currentTimeMillis());
                        offlineTask.setUpdateTime(System.currentTimeMillis());
                        offlineTaskDao.update(offlineTask);
                    }
                }

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        offlineTaskItemAdapter.setList(offlineTaskItems);
                    }
                },500);

                initPreNextBtn();
            }
        });
        offlineBackRvTaskList.setAdapter(offlineTaskListAdapter);
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void onPostSuccessEvent(MessageEvent event) {
        EventBus.getDefault().removeStickyEvent(event);
        offlineTaskItemAdapter.setDisableAll();
        String message = event.getMessage();
        apply = event.getApply();
        approve = event.getApprove();
        Log.i(TAG, "onPostSuccessEvent message: " + message + "  申请人:" + apply.getUserName()
                + " 审批人:" + approve.getUserName());
        if (message.equals(EventConsts.EVENT_POST_SUCCESS)) {
            //提交成功
            Log.i(TAG, "onPostSuccessEvent 提交成功: ");
            offlineBackBtnOpenDoor.setVisibility(View.VISIBLE);
            offlineBackBtnUnlock.setVisibility(View.VISIBLE);
            offlineBackBtnFinish.setVisibility(View.VISIBLE);
            offlineBackBtnConfirm.setVisibility(View.GONE);
            isQueryStatus = true;
            String cabType = SharedUtils.getCabType();
            if (cabType.equals(Constants.TYPE_AMMO_CAB)) {
                //领取弹药和归还弹药
                offlineBackBtnOpenDoor.setText("打开弹柜");
                offlineBackBtnUnlock.setText("打开弹仓");
            }
            //查询枪支状态
            if (!cabType.equals(Constants.TYPE_AMMO_CAB)) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Log.i(TAG, "run isQueryStatus : " + isQueryStatus);
                        while (isQueryStatus) {
                            List<OfflineTaskItem> taskItemList = offlineTaskItemAdapter.getCheckedList();
                            if (!taskItemList.isEmpty()) {
                                for (OfflineTaskItem offlineTaskItem: taskItemList) {
                                    int locationNo = offlineTaskItem.getLocationNo();
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
        if (offlineTaskItems.isEmpty()) {
            offlineBackTvCurPage.setText(index + 1 + "/1");
        } else {
            if (offlineTaskItems.size() <= pageCount) {
                offlineBackBtnNextPage.setVisibility(View.INVISIBLE);
            } else {
                offlineBackBtnNextPage.setVisibility(View.VISIBLE);
            }
            offlineBackTvCurPage.setText(index + 1 + "/" + (int) Math.ceil((double) offlineTaskItems.size() / pageCount));
        }
    }

    private void prePager() {
        index--;
        offlineTaskItemAdapter.setIndex(index);
        offlineBackTvCurPage.setText(index + 1 + "/" + (int) Math.ceil((double) offlineTaskItems.size() / pageCount));
        //隐藏上一个或下一个按钮
        checkButton();
//        Log.i(TAG, "prePager index: " + index);
    }

    private void nexPager() {
        index++;
        offlineTaskItemAdapter.setIndex(index);
        offlineBackTvCurPage.setText(index + 1 + "/" + (int) Math.ceil((double) offlineTaskItems.size() / pageCount));
        //隐藏上一个或下一个按钮
        checkButton();
//        Log.i(TAG, "nexPager index: " + index);
    }

    private void checkButton() {
        if (index <= 0) {
            offlineBackBtnPrePage.setVisibility(View.INVISIBLE);
            offlineBackBtnNextPage.setVisibility(View.VISIBLE);
        } else if (offlineTaskItems.size() - index * pageCount <= pageCount) {    //数据总数减每页数当小于每页可显示的数字时既是最后一页
            offlineBackBtnPrePage.setVisibility(View.VISIBLE);
            offlineBackBtnNextPage.setVisibility(View.INVISIBLE);
        } else {
            offlineBackBtnPrePage.setVisibility(View.VISIBLE);
            offlineBackBtnNextPage.setVisibility(View.VISIBLE);
        }
    }

    @OnClick({R.id.offline_back_btn_pre_page, R.id.offline_back_btn_next_page,
            R.id.offline_back_btn_open_door, R.id.offline_back_btn_unlock,
            R.id.offline_back_btn_confirm, R.id.offline_back_btn_finish})
    public void onViewClicked(View view) {
        List<OfflineTaskItem> checkedList;
        switch (view.getId()) {
            case R.id.offline_back_btn_pre_page:
                prePager();
                break;
            case R.id.offline_back_btn_next_page:
                nexPager();
                break;
            case R.id.offline_back_btn_open_door:
                 checkedList = offlineTaskItemAdapter.getCheckedList();
                if(checkedList.isEmpty()){
                    ToastUtil.showShort("请选择枪弹数据");
                    return;
                }
                SerialPortUtil.getInstance().openLock(SharedUtils.getLeftCabNo());
                break;
            case R.id.offline_back_btn_unlock:
                checkedList = offlineTaskItemAdapter.getCheckedList();
                if(!checkedList.isEmpty()){
                    for (OfflineTaskItem offlineTaskItem:checkedList) {
                        int locationNo = offlineTaskItem.getLocationNo();
                        SerialPortUtil.getInstance().openLock(locationNo);
                        try {
                            Thread.sleep(300);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }else{
                    ToastUtil.showShort("请选择枪弹数据");
                }
                break;
            case R.id.offline_back_btn_confirm:
                //确认归还枪弹
                checkedList =offlineTaskItemAdapter.getCheckedList();
                if(checkedList.isEmpty()){
                    ToastUtil.showShort("请选择枪弹数据");
                    return;
                }

                String jsonBody = JSON.toJSONString(checkedList);
                Intent intent = new Intent(this, VerifyActivity.class);
                intent.putExtra("activity", Constants.ACTIVITY_OFFLINE_BACK);
                intent.putExtra("data", jsonBody);
                startActivity(intent);
                break;
            case R.id.offline_back_btn_finish:
            case R.id.ac_top_back:
                finish();
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isQueryStatus =false;
        Constants.isCheckGunStatus = true;
        Constants.isIllegalGetGunAlarm = false;
        Constants.isIllegalOpenCabAlarm =false;
        //置为正在任务操作
        Constants.isExecuteTask =false;
    }
}
