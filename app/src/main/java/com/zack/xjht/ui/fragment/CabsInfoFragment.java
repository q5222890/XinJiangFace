package com.zack.xjht.ui.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.yanzhenjie.nohttp.rest.Response;
import com.zack.xjht.Constants;
import com.zack.xjht.R;
import com.zack.xjht.Utils.LogUtil;
import com.zack.xjht.Utils.SharedUtils;
import com.zack.xjht.Utils.ToastUtil;
import com.zack.xjht.Utils.Utils;
import com.zack.xjht.adapter.GunInfoAdapter;
import com.zack.xjht.db.gen.GunStateBeanDao;
import com.zack.xjht.entity.CabInfoBean;
import com.zack.xjht.entity.SubCabBean;
import com.zack.xjht.event.BulletNumEvent;
import com.zack.xjht.event.StatusEvent;
import com.zack.xjht.http.HttpClient;
import com.zack.xjht.http.HttpListener;
import com.zack.xjht.serial.SerialPortUtil;
import com.zack.xjht.ui.VerifyActivity;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * 当前枪柜信息
 */

public class CabsInfoFragment extends Fragment {

    private static final String TAG = "CabsInfoFragment";
    @BindView(R.id.cab_info_recycler_view)
    RecyclerView cabInfoRecyclerView;
    Unbinder unbinder;
    @BindView(R.id.cab_info_tv_type)
    TextView cabInfoTvType;
    @BindView(R.id.cab_info_tv_no)
    TextView cabInfoTvNo;
    @BindView(R.id.cab_btn_pre_page)
    Button cabBtnPrePage;
    @BindView(R.id.cab_tv_cur_page)
    TextView cabTvCurPage;
    @BindView(R.id.cab_btn_next_page)
    Button cabBtnNextPage;
    @BindView(R.id.ll_page)
    LinearLayout llPage;
    @BindView(R.id.gun_info_tv_gun_type)
    TextView gunInfoTvGunType;
    @BindView(R.id.gun_info_tv_ammo_type)
    TextView gunInfoTvAmmoType;
    @BindView(R.id.cab_info_open_cab)
    Button cabInfoOpenCab;
    @BindView(R.id.cab_info_tv_msg)
    TextView cabInfoTvMsg;
    @BindView(R.id.gun_info_tv_gun_type_title)
    TextView gunInfoTvGunTypeTitle;
    @BindView(R.id.gun_info_tv_ammo_type_title)
    TextView gunInfoTvAmmoTypeTitle;
    private GunInfoAdapter gunInfoAdapter;
    private List<SubCabBean> subCabsList = new ArrayList<>();
    private Map<String, Integer> gunTypeList = new HashMap<>();
    private Map<String, Integer> ammoTypeList = new HashMap<>();
    private int index = 0;
    private int pageCount = 12;
    private Context mContext;
    private FragmentActivity mActivity;
    private GunStateBeanDao gunStateBeanDao;
    private boolean isStop = false;
    private boolean isQuery = true;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
        mActivity = getActivity();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cabs_info, container, false);
        unbinder = ButterKnife.bind(this, view);

        cabBtnNextPage.setVisibility(View.INVISIBLE);
        cabBtnPrePage.setVisibility(View.INVISIBLE);

        if (SharedUtils.getCabType().equals(Constants.TYPE_AMMO_CAB)) {
            cabInfoOpenCab.setText("打开弹柜");
            gunInfoTvGunType.setVisibility(View.GONE);
            gunInfoTvGunTypeTitle.setVisibility(View.GONE);
        } else if (!SharedUtils.getCabType().equals(Constants.TYPE_MIX_CAB)) {
            cabInfoOpenCab.setText("打开枪柜");
            gunInfoTvAmmoType.setVisibility(View.GONE);
            gunInfoTvAmmoTypeTitle.setVisibility(View.GONE);
        }

        gunInfoAdapter = new GunInfoAdapter(subCabsList, index, pageCount);
        cabInfoRecyclerView.setLayoutManager(new GridLayoutManager(mContext, 3));
        cabInfoRecyclerView.setAdapter(gunInfoAdapter);

        if (SharedUtils.getIsServerOnline()) {
            getCabInfo();
        }

//        gunStateBeanDao = DBManager.getInstance().getGunStateBeanDao();
//        List<GunStateBean> gunStateBeans = gunStateBeanDao.loadAll();
//        gunInfoAdapter.setGunStateList(gunStateBeans);

        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }

        isStop = false;

        Constants.isIllegalOpenCabAlarm = true;

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.i(TAG, "run Thread: ");
                while (!isStop) {
                    mActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.i(TAG, "runOnUiThread: ");
                            gunInfoAdapter.setGunStatus(gunStatus);
                        }
                    });

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    private Map<Integer, Integer> gunStatus = new HashMap<>();

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onGunStatusSubscriber(StatusEvent event) {
        int address = event.getAddress();
        int category = event.getCategory();
        int status = event.getStatus();
        String message = event.getMessage();
        Log.i(TAG, "onSubscriber message: " + message);
        Log.i(TAG, "onSubscriber  address : " + address + " category:" + category + " status:" + status);
        if (category == 1) { //锁开关状态
            if (status == 0) { //开启
                Log.i(TAG, "onSubscriber : " + address + "枪锁打开");
            } else if (status == 1) {//关闭
                Log.i(TAG, "onSubscriber : " + address + "枪锁锁闭");
            } else if (status == 2) {//异常
                Log.i(TAG, "onSubscriber : " + address + "枪锁异常");
            }
        } else if (category == 2) { //枪支在位状态
            if (status == 0) {//离位
                Log.i(TAG, "onSubscriber : " + address + "枪不在位");
            } else if (status == 1) {//在位
                Log.i(TAG, "onSubscriber : " + address + "枪在位");
            }
//            gunInfoAdapter.setGunState(address, status);
            gunStatus.put(address, status);
        }
//        edtLockReceiveMsg.setText(message);
//        setStatusTxt(message);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onBulletNumSubscriber(BulletNumEvent event) {
        int address = event.getAddress();
        int count = event.getCount();
        Log.i(TAG, "onBulletNumSubscriber : " + address + "号弹仓的子弹数量：" + count);
//        gunInfoAdapter.setGunState(address, count);
    }

    private void getCabInfo() {
        HttpClient.getInstance().getCabByMac(mContext, new HttpListener<String>() {
            @Override
            public void onSucceed(int what, Response<String> response) throws JSONException {
                try {
                    LogUtil.i(TAG, "onSucceed cabInfo response: " + response.get());
                    if (!TextUtils.isEmpty(response.get())) {
                        CabInfoBean cabInfoBean = JSON.parseObject(response.get(), CabInfoBean.class);
                        if (cabInfoBean != null) {
                            String gunCabinetNo = cabInfoBean.getGunCabinetNo();
                            String gunCabinetType = cabInfoBean.getGunCabinetType();

                            if (cabInfoTvNo != null) {
                                cabInfoTvNo.setText(gunCabinetNo);
                            }
                            switch (gunCabinetType) {
                                case Constants.TYPE_LONG_GUN_CAB:
                                    if (cabInfoTvType != null) {
                                        cabInfoTvType.setText("长枪柜");
                                    }
                                    break;
                                case Constants.TYPE_SHORT_GUN_CAB:
                                    if (cabInfoTvType != null) {
                                        cabInfoTvType.setText("短枪柜");
                                    }
                                    break;
                                case Constants.TYPE_SHORT_LONG_GUN_CAB:
                                    if (cabInfoTvType != null) {
                                        cabInfoTvType.setText("长短枪一体柜");
                                    }
                                    break;
                                case Constants.TYPE_AMMO_CAB:
                                    if (cabInfoTvType != null) {
                                        cabInfoTvType.setText("弹柜");
                                    }
                                    break;
                                case Constants.TYPE_MIX_CAB:
                                    if (cabInfoTvType != null) {
                                        cabInfoTvType.setText("枪弹综合柜");
                                    }
                                    break;
                                default:
                                    if (cabInfoTvType != null) {
                                        cabInfoTvType.setText("枪弹柜");
                                    }
                                    break;
                            }
                            subCabsList = cabInfoBean.getListLocation();
                            if (!subCabsList.isEmpty()) {
                                cabInfoTvMsg.setVisibility(View.INVISIBLE);
                                gunInfoAdapter.setList(subCabsList);
                                Collections.sort(subCabsList);
                                initPreNextBtn();

                                for (SubCabBean subCabBean : subCabsList) {
                                    String isUse = subCabBean.getIsUse();
                                    String locationType = subCabBean.getLocationType();
                                    if (!TextUtils.isEmpty(isUse) && isUse.equals("yes")) { //已存放
                                        if (!TextUtils.isEmpty(locationType)) {
                                            switch (locationType) {
                                                case Constants.TYPE_SHORT_GUN://短枪
                                                case Constants.TYPE_LONG_GUN://长枪
                                                    String gunState = subCabBean.getGunState();
                                                    if (!TextUtils.isEmpty(gunState)) {
                                                        if (gunState.equals("in")) {//在库
                                                            String objectName = subCabBean.getObjectName();
                                                            if (!TextUtils.isEmpty(objectName)) {
                                                                if (!gunTypeList.containsKey(objectName)) {
                                                                    gunTypeList.put(objectName, 1);
                                                                } else {
                                                                    gunTypeList.put(objectName, gunTypeList.get(objectName) + 1);
                                                                }
                                                            }
                                                        }
                                                    } else {
                                                        ToastUtil.showShort("状态数据为空");
                                                    }
                                                    break;
                                                case Constants.TYPE_AMMO: //弹药
                                                    String objectName = subCabBean.getObjectName();
                                                    int objectNumber = subCabBean.getObjectNumber();
                                                    if (!TextUtils.isEmpty(objectName)) {
                                                        if (!ammoTypeList.containsKey(objectName)) {
                                                            ammoTypeList.put(objectName, objectNumber);
                                                        } else {
                                                            ammoTypeList.put(objectName,
                                                                    ammoTypeList.get(objectName) + objectNumber);
                                                        }
                                                    }
                                                    break;
                                            }
                                        }
                                    }
                                }
                                if (SharedUtils.getCabType().equals(Constants.TYPE_SHORT_LONG_GUN_CAB)) {
                                    new Thread(new Runnable() {
                                        @Override
                                        public void run() {
                                            while (isQuery) {
                                                if (subCabsList.isEmpty()) {
                                                    break;
                                                }
                                                for (int i = 0; i < subCabsList.size(); i++) {
                                                    SubCabBean subCabBean = subCabsList.get(i);
                                                    if (subCabBean != null) {
                                                        int locationNo = subCabBean.getLocationNo();
                                                        Log.v(TAG, "onHandleIntent query gun state: " + locationNo);
                                                        SerialPortUtil.getInstance().checkStatus(locationNo);
                                                        try {
                                                            Thread.sleep(200);
                                                        } catch (InterruptedException e) {
                                                            e.printStackTrace();
                                                        }
                                                        if (!isQuery) {
                                                            break;
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }).start();
                                }

                                Set<String> strings = gunTypeList.keySet();
                                for (String key : strings) {
                                    Integer integer = gunTypeList.get(key);
                                    gunInfoTvGunType.append(key + ":" + integer + "支\n");
                                }
                                Set<String> ammoType = ammoTypeList.keySet();
                                for (String key : ammoType) {
                                    Integer integer = ammoTypeList.get(key);
                                    gunInfoTvAmmoType.append(key + ":" + integer + "发\n");
                                }
                            } else {
                                if (cabInfoTvMsg != null) {
                                    cabInfoTvMsg.setText("枪柜位置数据为空");
                                }
                            }
                        } else {
                            if (cabInfoTvMsg != null) {
                                cabInfoTvMsg.setText("获取数据为空");
                            }
                        }
                    } else {
                        if (cabInfoTvMsg != null) {
                            cabInfoTvMsg.setText("获取枪柜数据失败");
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    if (cabInfoTvMsg != null) {
                        cabInfoTvMsg.setText("获取枪柜数据出错");
                    }
                }
            }

            @Override
            public void onFailed(int what, Response<String> response) {
                if (cabInfoTvMsg != null) {
                    cabInfoTvMsg.setText("网络请求出错，获取枪柜数据失败");
                }
            }
        });
    }

    private void initPreNextBtn() {
        if (subCabsList.isEmpty()) {
            cabTvCurPage.setText(index + 1 + "/1");
        } else {
            if (subCabsList.size() <= pageCount) {
                cabBtnNextPage.setVisibility(View.INVISIBLE);
            } else {
                cabBtnNextPage.setVisibility(View.VISIBLE);
            }
            cabTvCurPage.setText(index + 1 + "/" + (int) Math.ceil((double) subCabsList.size() / pageCount));
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        isQuery = false;
    }

    @Override
    public void onStop() {
        super.onStop();
        isQuery = false;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
        isStop = true;
        isQuery = false;
        Constants.isIllegalOpenCabAlarm = false;
    }

    @OnClick({R.id.cab_btn_pre_page, R.id.cab_btn_next_page, R.id.cab_info_open_cab})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.cab_btn_pre_page://上一页
                prePager();
                break;
            case R.id.cab_btn_next_page://下一页
                nexPager();
                break;
            case R.id.cab_info_open_cab: //打开柜门
                Intent intent = new Intent(mContext, VerifyActivity.class);
                intent.putExtra("activity", Constants.ACTIVITY_OPEN_CAB);
                mContext.startActivity(intent);
                break;
        }
    }

    private void prePager() {
        index--;
        gunInfoAdapter.setIndex(index);
        cabTvCurPage.setText(index + 1 + "/" + (int) Math.ceil((double) subCabsList.size() / pageCount));
        //隐藏上一个或下一个按钮
        checkButton();
        Log.i(TAG, "prePager index: " + index);
    }

    private void nexPager() {
        index++;
        gunInfoAdapter.setIndex(index);
        cabTvCurPage.setText(index + 1 + "/" + (int) Math.ceil((double) subCabsList.size() / pageCount));
        //隐藏上一个或下一个按钮
        checkButton();
        Log.i(TAG, "nexPager index: " + index);
    }

    private void checkButton() {
        if (index <= 0) {
            cabBtnPrePage.setVisibility(View.INVISIBLE);
            cabBtnNextPage.setVisibility(View.VISIBLE);
        } else if (subCabsList.size() - index * pageCount <= pageCount) {    //数据总数减每页数当小于每页可显示的数字时既是最后一页
            cabBtnPrePage.setVisibility(View.VISIBLE);
            cabBtnNextPage.setVisibility(View.INVISIBLE);
        } else {
            cabBtnNextPage.setVisibility(View.VISIBLE);
            cabBtnPrePage.setVisibility(View.VISIBLE);
        }
    }


}
