package com.zack.xjht.ui.fragment;


import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.zack.xjht.R;
import com.zack.xjht.Utils.DialogUtils;
import com.zack.xjht.Utils.SharedUtils;
import com.zack.xjht.Utils.ToastUtil;
import com.zack.xjht.event.BulletNumEvent;
import com.zack.xjht.event.StatusEvent;
import com.zack.xjht.serial.SerialPortUtil;
import com.zack.xjht.ui.dialog.OpenLockDialog;
import com.zack.xjht.ui.dialog.SetAddressDialog;
import com.zack.xjht.ui.dialog.SetBulletWeightDialog;

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
 * 枪锁配置
 */
public class LockFragment extends Fragment {

    private static final String TAG = "LockFragment";
    @BindView(R.id.btn_query_lock_status)
    Button btnQueryLockStatus;
    @BindView(R.id.btn_open_lock)
    Button btnOpenLock;
    @BindView(R.id.btn_open_all_lock)
    Button btnOpenAllLock;
    Unbinder unbinder;
    @BindView(R.id.btn_open_cab_lock)
    Button btnOpenCabLock;
    @BindView(R.id.btn_set_lock_address)
    Button btnSetLockAddress;
    @BindView(R.id.edt_lock_receive_msg)
    EditText edtLockReceiveMsg;
    @BindView(R.id.btn_set_bullet_weight)
    Button btnSetBulletWeight;
    @BindView(R.id.btn_read_bullet_weight)
    Button btnReadBulletWeight;
    @BindView(R.id.btn_read_bullet_count)
    Button btnReadBulletCount;
    @BindView(R.id.btn_set_tare)
    Button btnSetTare;
    @BindView(R.id.lock_btn_clear_recv)
    Button lockBtnClearRecv;
    @BindView(R.id.et_left_cab_no)
    EditText etLeftCabNo;
    @BindView(R.id.et_right_cab_no)
    EditText etRightCabNo;
    @BindView(R.id.btn_set_cab_no)
    Button btnSetCabNo;
    @BindView(R.id.lock_edt_lock_no)
    EditText lockEdtLockNo;
    @BindView(R.id.lock_ll_left)
    LinearLayout lockLlLeft;
    @BindView(R.id.lock_edt_power_address)
    EditText lockEdtPowerAddress;
    @BindView(R.id.btn_set_power_address)
    Button btnSetPowerAddress;
    @BindView(R.id.lock_ll_cab_lock)
    LinearLayout lockLlCabLock;
    @BindView(R.id.btn_close_lock)
    Button btnCloseLock;
    @BindView(R.id.btn_open_led)
    Button btnOpenLed;
    @BindView(R.id.btn_close_led)
    Button btnCloseLed;
    @BindView(R.id.lock_ll_middle)
    LinearLayout lockLlMiddle;
    @BindView(R.id.btn_auto_set_address)
    Button btnAutoSetAddress;
    @BindView(R.id.btn_invisible_button)
    Button btnInvisibleButton;
    @BindView(R.id.btn_open_lock2)
    Button btnOpenLock2;
    @BindView(R.id.btn_change_ad)
    Button btnChangeAd;
    @BindView(R.id.btn_adjustment_direction_plus)
    Button btnAdjustmentDirectionPlus;
    @BindView(R.id.btn_adjustment_direction_minus)
    Button btnAdjustmentDirectionMinus;
    @BindView(R.id.btn_adjustment_tare_plus)
    Button btnAdjustmentTarePlus;
    @BindView(R.id.btn_adjustment_tare_minus)
    Button btnAdjustmentTareMinus;
    @BindView(R.id.btn_adjustment_ad_plus)
    Button btnAdjustmentAdPlus;
    @BindView(R.id.btn_adjustment_ad_minus)
    Button btnAdjustmentAdMinus;
    @BindView(R.id.lock_edt_bullet_weight)
    EditText lockEdtBulletWeight;
    @BindView(R.id.btn_open_door)
    Button btnOpenDoor;
    @BindView(R.id.btn_close_door)
    Button btnCloseDoor;
    //    private List<SubCabsBean> subCabsList;
    private Context mContext;
    private FragmentActivity mActivity;

    public LockFragment() {
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.mContext = context;
        this.mActivity = getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_lock, container, false);
        unbinder = ButterKnife.bind(this, view);
//        subCabsList = new ArrayList<>();
        SerialPortUtil.getInstance().onCreate();
        etLeftCabNo.setText(SharedUtils.getLeftCabNo());
        etRightCabNo.setText(SharedUtils.getRightCabNo());
        lockEdtPowerAddress.setText(String.valueOf(SharedUtils.getPowerAddress()));
        btnOpenLock.setVisibility(View.GONE);
        btnOpenCabLock.setVisibility(View.GONE);
        btnOpenAllLock.setVisibility(View.GONE);
        btnAutoSetAddress.setVisibility(View.GONE);
        btnSetLockAddress.setVisibility(View.GONE);
        btnOpenLock2.setVisibility(View.GONE);

        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        return view;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSubscriber(StatusEvent event) {
        int address = event.getAddress();
        String message = event.getMessage();
        Log.i(TAG, "onSubscriber  address : " + address + " message:" + message);
//        edtLockReceiveMsg.setText(message);
        setStatusTxt(message);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onBulletSubscriber(BulletNumEvent event) {
        int address = event.getAddress();
        String message = event.getMessage();
        Log.i(TAG, "onBulletSubscriber  address : " + address + " message:" + message);
//        edtLockReceiveMsg.setText(message);
        setStatusTxt(message);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
//        SerialPortUtil.getInstance().close();
    }

    private int type;
    private Map<String, Boolean> openStatus = new HashMap<>();
    private Dialog choiceDialog;

    @OnClick({R.id.btn_query_lock_status, R.id.btn_open_lock, R.id.btn_invisible_button,
            R.id.btn_open_all_lock, R.id.btn_open_cab_lock, R.id.btn_auto_set_address,
            R.id.btn_set_lock_address, R.id.btn_set_bullet_weight,
            R.id.btn_read_bullet_weight, R.id.btn_read_bullet_count, R.id.btn_set_tare,
            R.id.lock_btn_clear_recv, R.id.btn_set_cab_no, R.id.btn_set_power_address,
            R.id.btn_close_lock, R.id.btn_open_led, R.id.btn_close_led,
            R.id.btn_open_lock2, R.id.btn_change_ad, R.id.btn_adjustment_direction_plus,
            R.id.btn_adjustment_direction_minus, R.id.btn_adjustment_tare_plus,
            R.id.btn_adjustment_tare_minus, R.id.btn_adjustment_ad_plus, R.id.btn_adjustment_ad_minus,
            R.id.btn_open_door, R.id.btn_close_door})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_query_lock_status://查询锁状态
                type = 2;
                String status = lockEdtLockNo.getText().toString();
                if (!TextUtils.isEmpty(status)) {
                    SerialPortUtil.getInstance().checkStatus(Integer.parseInt(status));
                }
                break;
            case R.id.btn_open_lock://开锁
                type = 3;
                String no = lockEdtLockNo.getText().toString();
                if (!TextUtils.isEmpty(no)) {
                    SerialPortUtil.getInstance().openLock(no);
                }
                break;
            case R.id.btn_open_all_lock: //打开所有枪锁
                type = 5;
                break;
            case R.id.btn_open_cab_lock: //打开枪柜
                type = 6;
                SerialPortUtil.getInstance().openLock(SharedUtils.getLeftCabNo());
//                try {
//                    Thread.sleep(300);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//                SerialPortUtil.getInstance().openLock(SharedUtils.getRightCabNo());
                setStatusTxt("打开枪柜门");
                break;
            case R.id.btn_set_lock_address: //设置锁地址
                type = 7;
                choiceDialog = DialogUtils.createChoiceDialog(getContext(), "确定设置地址吗？",
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                String address = lockEdtLockNo.getText().toString();
                                if (!TextUtils.isEmpty(address)) {
                                    SerialPortUtil.getInstance().setAddress(address);
                                }
                                choiceDialog.dismiss();
                            }
                        });
                choiceDialog.show();
                break;
            case R.id.btn_set_bullet_weight: //设置子弹重量
                type = 9;
                String boxNo = lockEdtLockNo.getText().toString().trim();
                if (TextUtils.isEmpty(boxNo)) {
//                    SerialPortUtil.getInstance().setBulletWeight(boxNo);
                    ToastUtil.showShort("请输入弹仓编号");
                    return;
                }
                SetBulletWeightDialog setBulletWeightDialog = new SetBulletWeightDialog();
                Bundle dataBundle = new Bundle();
                dataBundle.putInt("num", Integer.parseInt(boxNo));
                setBulletWeightDialog.setArguments(dataBundle);
                setBulletWeightDialog.show(getChildFragmentManager(), "setBulletWeightDialog");
                break;
            case R.id.btn_read_bullet_weight: //读取子弹重量
                type = 10;
                String bNo = lockEdtLockNo.getText().toString().trim();
                if (TextUtils.isEmpty(bNo)) {
                    return;
                }
                SerialPortUtil.getInstance().readBulletWeight(bNo);
                break;
            case R.id.btn_read_bullet_count: //读取子弹个数
                type = 11;
                String trim = lockEdtLockNo.getText().toString().trim();
                if (!TextUtils.isEmpty(trim)) {
                    SerialPortUtil.getInstance().readBulletCount(trim);
                }
                break;
            case R.id.btn_set_tare://设置皮重
                type = 12;
                String trim1 = lockEdtLockNo.getText().toString().trim();
                if (!TextUtils.isEmpty(trim1)) {
                    SerialPortUtil.getInstance().setBulletTare(trim1);
                }
                break;
            case R.id.lock_btn_clear_recv:
                edtLockReceiveMsg.setText("");
                break;
            case R.id.btn_set_cab_no:
                //保存枪柜门地址
                String leftNo = etLeftCabNo.getText().toString();
                String rightNo = etRightCabNo.getText().toString();
                if (TextUtils.isEmpty(leftNo)) {
                    return;
                }
                if (TextUtils.isEmpty(rightNo)) {
                    return;
                }
                SharedUtils.saveLeftCabNo(leftNo);
                SharedUtils.saveRightCabNo(rightNo);
                ToastUtil.showShort("保存枪柜门地址成功！");
                setStatusTxt("保存枪柜门地址成功！");
                break;
            case R.id.btn_set_power_address:  //设置电源地址
                String powerAddr = lockEdtPowerAddress.getText().toString().trim();
                if (TextUtils.isEmpty(powerAddr)) {
                    return;
                }
                SharedUtils.setPowerAddress(Integer.parseInt(powerAddr));
                ToastUtil.showShort("保存电源地址成功！");
                setStatusTxt("保存电源地址成功！");
                break;
            case R.id.btn_close_lock: //关枪锁
                String lockNo = lockEdtLockNo.getText().toString();
                if (!TextUtils.isEmpty(lockNo)) {
                    SerialPortUtil.getInstance().closeLock(Integer.parseInt(lockNo));//关闭枪锁数码管led
                }
                break;
            case R.id.btn_open_led://打开led
                SerialPortUtil.getInstance().openLED();//关闭枪锁数码管led
                break;
            case R.id.btn_close_led://关闭led
                SerialPortUtil.getInstance().closeLED();//关闭枪锁数码管led
                break;
            case R.id.btn_auto_set_address://自动设置地址
                //1.点击弹出设置地址对话框
                //2.点击设置开始从1开始设置 当1设置成功
                SetAddressDialog setAddressDialog = new SetAddressDialog();
                setAddressDialog.show(getChildFragmentManager(), "SetAddressDialog");
                break;
            case R.id.btn_invisible_button://显示设置和开锁按钮
                String pwd = lockEdtLockNo.getText().toString();
                if (!TextUtils.isEmpty(pwd)) {
                    String formatTime = DateFormat.format("yyyyMMdd",
                            System.currentTimeMillis()).toString();
                    if (pwd.equals(formatTime)) {
                        btnOpenLock.setVisibility(View.VISIBLE);
                        btnOpenCabLock.setVisibility(View.VISIBLE);
                        btnOpenAllLock.setVisibility(View.VISIBLE);
                        btnAutoSetAddress.setVisibility(View.VISIBLE);
                        btnSetLockAddress.setVisibility(View.VISIBLE);
                        btnOpenLock2.setVisibility(View.VISIBLE);
                        lockEdtLockNo.setText("");
                    }
                }
                break;
            case R.id.btn_open_lock2:
                String num = lockEdtLockNo.getText().toString();
                if (TextUtils.isEmpty(num)) {
                    setStatusTxt("请输入抽屉号");
                    return;
                }
                OpenLockDialog openLockDialog = new OpenLockDialog();
                Bundle bundle = new Bundle();
                bundle.putInt("num", Integer.parseInt(num));
                openLockDialog.setArguments(bundle);
                openLockDialog.show(getChildFragmentManager(), "OpenLockDialog");
                break;
            case R.id.btn_change_ad: //AD转换
                String lockNumber = lockEdtLockNo.getText().toString();
                String weight = lockEdtBulletWeight.getText().toString();
                if (TextUtils.isEmpty(lockNumber)) {
                    setStatusTxt("请输入抽屉号");
                    return;
                }
                if (TextUtils.isEmpty(weight)) {
                    setStatusTxt("请输入重量");
                    return;
                }
                SerialPortUtil.getInstance().changeAD(Integer.parseInt(lockNumber), Integer.parseInt(weight));
                break;
            case R.id.btn_adjustment_direction_plus: //调整方向加
                num = lockEdtLockNo.getText().toString();
                if (TextUtils.isEmpty(num)) {
                    setStatusTxt("请输入抽屉号");
                    return;
                }
                SerialPortUtil.getInstance().adjustWeight(Integer.parseInt(num), (byte) 0x01);
                break;
            case R.id.btn_adjustment_direction_minus://调整方向减
                num = lockEdtLockNo.getText().toString();
                if (TextUtils.isEmpty(num)) {
                    setStatusTxt("请输入抽屉号");
                    return;
                }
                SerialPortUtil.getInstance().adjustWeight(Integer.parseInt(num), (byte) 0xff);
                break;
            case R.id.btn_adjustment_tare_plus://微调皮重加
                num = lockEdtLockNo.getText().toString();
                if (TextUtils.isEmpty(num)) {
                    setStatusTxt("请输入抽屉号");
                    return;
                }
                SerialPortUtil.getInstance().adjustTare(Integer.parseInt(num), (byte) 0x01);
                break;
            case R.id.btn_adjustment_tare_minus://微调皮重减
                num = lockEdtLockNo.getText().toString();
                if (TextUtils.isEmpty(num)) {
                    setStatusTxt("请输入抽屉号");
                    return;
                }
                SerialPortUtil.getInstance().adjustTare(Integer.parseInt(num), (byte) 0xff);
                break;
            case R.id.btn_adjustment_ad_plus://微调AD值加
                num = lockEdtLockNo.getText().toString();
                if (TextUtils.isEmpty(num)) {
                    setStatusTxt("请输入抽屉号");
                    return;
                }
                SerialPortUtil.getInstance().adjustAD(Integer.parseInt(num), (byte) 0x01);
                break;
            case R.id.btn_adjustment_ad_minus://微调AD值减
                num = lockEdtLockNo.getText().toString();
                if (TextUtils.isEmpty(num)) {
                    setStatusTxt("请输入抽屉号");
                    return;
                }
                SerialPortUtil.getInstance().adjustAD(Integer.parseInt(num), (byte) 0xff);
                break;
            case R.id.btn_open_door:
                num = lockEdtLockNo.getText().toString();
                if (TextUtils.isEmpty(num)) {
                    setStatusTxt("请输入抽屉号");
                    return;
                }
                SerialPortUtil.getInstance().setTare(Integer.parseInt(num));
                break;
            case R.id.btn_close_door:
                num = lockEdtLockNo.getText().toString();
                if (TextUtils.isEmpty(num)) {
                    setStatusTxt("请输入抽屉号");
                    return;
                }
                SerialPortUtil.getInstance().saveTare(Integer.parseInt(num));
                break;
        }
    }

    private void setStatusTxt(final String info) {
        try {
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (edtLockReceiveMsg != null && !TextUtils.isEmpty(info)) {
                        edtLockReceiveMsg.append(info + "\n");
                    }
                }
            });
        } catch (Exception e) {
            ToastUtil.showShort(e.getMessage());
        }
    }



}
