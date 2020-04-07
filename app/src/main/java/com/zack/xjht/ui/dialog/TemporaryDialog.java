package com.zack.xjht.ui.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.yanzhenjie.nohttp.rest.Response;
import com.zack.xjht.Constants;
import com.zack.xjht.R;
import com.zack.xjht.Utils.DialogUtils;
import com.zack.xjht.Utils.SharedUtils;
import com.zack.xjht.Utils.SoundPlayUtil;
import com.zack.xjht.Utils.ToastUtil;
import com.zack.xjht.entity.SubCabBean;
import com.zack.xjht.entity.TempDataBean;
import com.zack.xjht.entity.UserBean;
import com.zack.xjht.event.EventConsts;
import com.zack.xjht.event.MessageEvent;
import com.zack.xjht.event.StatusEvent;
import com.zack.xjht.http.HttpClient;
import com.zack.xjht.http.HttpListener;
import com.zack.xjht.serial.SerialPortUtil;
import com.zack.xjht.ui.VerifyActivity;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * 临时存放枪支
 */

public class TemporaryDialog extends Dialog {
    private static final String TAG = "TemporaryDialog";
    @BindView(R.id.dl_temp_btn_confirm)
    Button dlTempBtnConfirm;
    @BindView(R.id.dl_temp_btn_cancel)
    Button dlTempBtnCancel;
    @BindView(R.id.dl_edt_gun_no)
    EditText dlEdtGunNo;
    @BindView(R.id.dl_edt_gun_eno)
    EditText dlEdtGunEno;
    @BindView(R.id.dl_edt_gun_type)
    EditText dlEdtGunType;
    @BindView(R.id.dl_temp_ll_gun_no)
    LinearLayout dlTempLlGunNo;
    @BindView(R.id.dl_temp_ll_gun_eno)
    LinearLayout dlTempLlGunEno;
    @BindView(R.id.dl_temp_ll_gun_type)
    LinearLayout dlTempLlGunType;
    @BindView(R.id.dl_edt_police_no)
    EditText dlEdtPoliceNo;
    @BindView(R.id.dl_temp_ll_police_no)
    LinearLayout dlTempLlPoliceNo;
    @BindView(R.id.dl_edt_id_number)
    EditText dlEdtIdNumber;
    @BindView(R.id.dl_temp_ll_id_number)
    LinearLayout dlTempLlIdNumber;
    @BindView(R.id.dl_temp_btn_open_cab)
    Button dlTempBtnOpenCab;
    @BindView(R.id.dl_temp_btn_open_lock)
    Button dlTempBtnOpenLock;

    private Context context;
    private SubCabBean subCabsBean;
    private Unbinder bind;
    private UserBean manager1;
    private UserBean manager2;
    private String gunId;
    private boolean isQueryStatus = true;

    public TemporaryDialog(@NonNull Context context, SubCabBean subCabsBean, UserBean manager1,
                           UserBean manager2) {
        super(context, R.style.dialog);
        this.context = context;
        this.subCabsBean = subCabsBean;
        this.manager1 = manager1;
        this.manager2 = manager2;
        initView();
    }

    private void initView() {
        setContentView(R.layout.dl_temporary);
        bind = ButterKnife.bind(this);
        setCanceledOnTouchOutside(false);
        setCancelable(true);

        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void onMessageEvent(MessageEvent event) {
        EventBus.getDefault().removeStickyEvent(event);
        String message = event.getMessage();
        Log.i(TAG, "onMessageEvent  message: " + message);
        if (message.equals(EventConsts.EVENT_POST_SUCCESS)) {
            Log.i(TAG, "onMessageEvent 提交成功！: ");
            dlTempBtnConfirm.setVisibility(View.GONE);
            dlTempBtnCancel.setVisibility(View.VISIBLE);
            dlTempBtnCancel.setText("完成");
            dlTempBtnOpenCab.setVisibility(View.VISIBLE);
            dlTempBtnOpenLock.setVisibility(View.VISIBLE);

            //查询枪支状态
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Log.i(TAG, "run isQueryStatus : " + isQueryStatus);
                    while (isQueryStatus) {
                        if (subCabsBean != null) {
                            int locationNo = subCabsBean.getLocationNo();
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
            }).start();
        } else {
            Log.i(TAG, "onMessageEvent: 提交失败！");
        }
    }

    private Map<Integer, Boolean> gunStatusMap = new HashMap<>();

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
                Log.i(TAG, "onSubscriber : " + address + "枪锁开启");
            } else if (status == 1) {//关闭
                Log.i(TAG, "onSubscriber : " + address + "枪锁关闭");
            } else if (status == 2) {//异常
                Log.i(TAG, "onSubscriber : " + address + "枪锁异常");
                ToastUtil.showShort(address + "号枪锁异常！");
            }
        } else if (category == 2) { //枪支在位状态
            if (status == 0) {//离位
                Log.i(TAG, "onSubscriber : " + address + "枪离位");
                gunStatusMap.put(address, false);
            } else if (status == 1) {//在位
                Log.i(TAG, "onSubscriber : " + address + "枪在位");
                gunStatusMap.put(address, true);
            }
        }
//        edtLockReceiveMsg.setText(message);
//        setStatusTxt(message);
    }

    @OnClick({R.id.dl_temp_btn_confirm, R.id.dl_temp_btn_cancel, R.id.dl_temp_btn_open_cab,
            R.id.dl_temp_btn_open_lock})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.dl_temp_btn_confirm: //确认存放
                if (subCabsBean == null) {
                    ToastUtil.showShort("枪弹位置对象为空");
                    return;
                }
                try {
                    postStoreGun();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case R.id.dl_temp_btn_cancel:
                if (Constants.isDebug || Constants.isOldBoard) {
                    dismiss();
                    isQueryStatus = false;
                } else {
                    int locationNo = subCabsBean.getLocationNo();
                    if (gunStatusMap.containsKey(locationNo)) {
                        //枪支状态  true 在位 false 不在位
                        boolean aBoolean = gunStatusMap.get(locationNo);
                        if (!aBoolean) {
                            Log.i(TAG, "onViewClicked  : " + locationNo + "号枪支未放置在位");
                            ToastUtil.showShort(locationNo + "号枪支未放置在位");
                            SoundPlayUtil.getInstance().play(R.raw.gun_not_in_position);
                            return;
                        }
                    }
                    //停止查询枪支状态
                    isQueryStatus =false;

                    if (SharedUtils.getCabOpenStatus() == 1) {
                        ToastUtil.showShort("柜门未关闭");
                        SoundPlayUtil.getInstance().play(R.raw.cab_not_close);
                    } else {
                        dismiss();
                    }
                }
                break;
            case R.id.dl_temp_btn_open_cab: //打开枪柜
                SharedUtils.setIsCheckStatus(false);//查询状态
                SerialPortUtil.getInstance().openLock(SharedUtils.getLeftCabNo());
                SoundPlayUtil.getInstance().play(R.raw.gun_cab_open);
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                SerialPortUtil.getInstance().openLED();//打开枪锁数码管led
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                SerialPortUtil.getInstance().openLED(SharedUtils.getPowerAddress());//打开枪锁数码管led
                SharedUtils.setIsCheckStatus(true);//查询状态
                break;
            case R.id.dl_temp_btn_open_lock:
                SharedUtils.setIsCheckStatus(false);//查询状态
                if (subCabsBean != null) {
                    SerialPortUtil.getInstance().openLock(subCabsBean.getLocationNo());
                    SoundPlayUtil.getInstance().play(R.raw.gun_lock_open);
                }
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                SharedUtils.setIsCheckStatus(true);//查询状态
                break;
        }
    }

    /**
     * 提交存放枪支数据
     */
    private void postStoreGun() {
        String gunNo = dlEdtGunNo.getText().toString();
        Log.i(TAG, "postStoreGun formatDate: " + gunNo);
        String policeName = dlEdtGunEno.getText().toString();
        String gunType = dlEdtGunType.getText().toString();
        String policeNo = dlEdtPoliceNo.getText().toString().trim();
        String idNumber = dlEdtIdNumber.getText().toString().trim();

        if (TextUtils.isEmpty(gunNo)) {
//            SoundPlayUtil.getInstance().play(R.raw.enter_gun_no);
            ToastUtil.showShort("请输入枪支编号！");
            return;
        }

        if (TextUtils.isEmpty(gunType)) {
//            SoundPlayUtil.getInstance().play(R.raw.enter_gun_no);
            ToastUtil.showShort("请输入枪支类型！");
            return;
        }

        if (TextUtils.isEmpty(policeName)) {
            ToastUtil.showShort("请输入警员姓名！");
            return;
        }

        if (TextUtils.isEmpty(policeNo)) {
            ToastUtil.showShort("请输入警员编号！");
            return;
        }

        if (TextUtils.isEmpty(idNumber)) {
            ToastUtil.showShort("请输入身份证号！");
            return;
        }

        TempDataBean tempDataBean = new TempDataBean();

        tempDataBean.setDepositor(policeName);//
        tempDataBean.setGunCabinetLocationId(subCabsBean.getId());//
        tempDataBean.setGunNo(gunNo); //
        tempDataBean.setGunType(gunType);//
        tempDataBean.setPoliceNo(policeNo);//
        tempDataBean.setCardNo(idNumber);//
        tempDataBean.setMac(SharedUtils.getMacAddress());
        String jsonString = JSON.toJSONString(tempDataBean);
//        LogUtil.i(TAG, "postStoreGun jsonString: " + jsonString);

        Intent intent = new Intent(context, VerifyActivity.class);
        intent.putExtra("activity", Constants.ACTIVITY_TEMP_IN);
        intent.putExtra("data", jsonString);
        context.startActivity(intent);
//        if(Utils.isNetworkAvailable()){
//            postTempStore(jsonString);
//        }
    }

    private void postTempStore(String jsonString) {
        HttpClient.getInstance().postTempStoreGun(context, jsonString, new HttpListener<String>() {
            @Override
            public void onSucceed(int what, Response<String> response) throws JSONException {
                Log.i(TAG, "postTempStore onSucceed  response: " + response.get());
                try {
                    if (!TextUtils.isEmpty(response.get())) {
                        if (response.get().equals("success")) {
                            showDialog("提交成功");
                            SerialPortUtil.getInstance().openLock(SharedUtils.getLeftCabNo());
                            if (subCabsBean != null) {
                                try {
                                    Thread.sleep(100);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                SerialPortUtil.getInstance().openLock(subCabsBean.getLocationNo());
                            }
                        } else {
                            showDialog("提交失败");
                        }
                    } else {
                        showDialog("提交失败");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailed(int what, Response<String> response) {
                Log.i(TAG, "onFailed error: " + response.getException().getMessage());
                showDialog("提交失败");
            }
        });
    }

    Dialog dialog;

    protected void showDialog(String msg) {
        if (!((Activity) context).isFinishing()) {
            if (dialog != null) {
                DialogUtils.setTipText(msg);
                if (!dialog.isShowing()) {
                    dialog.show();
                }
            } else {
                dialog = DialogUtils.creatTipDialog(context, "提示", msg, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                        TemporaryDialog.this.dismiss();
                    }
                });
                if (!dialog.isShowing()) {
                    dialog.show();
                }
            }
        }
    }

    @Override
    public void dismiss() {
        super.dismiss();
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
        try {
            isQueryStatus = false;
            bind.unbind();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
