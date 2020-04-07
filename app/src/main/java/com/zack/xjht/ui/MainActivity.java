package com.zack.xjht.ui;

import android.annotation.TargetApi;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.arcsoft.face.ActiveFileInfo;
import com.arcsoft.face.ErrorInfo;
import com.arcsoft.face.FaceEngine;
import com.arcsoft.face.enums.RuntimeABI;
import com.yanzhenjie.nohttp.rest.Response;
import com.zack.xjht.Constants;
import com.zack.xjht.R;
import com.zack.xjht.Utils.DialogUtils;
import com.zack.xjht.Utils.LogUtil;
import com.zack.xjht.Utils.SharedUtils;
import com.zack.xjht.Utils.ToastUtil;
import com.zack.xjht.Utils.TransformUtil;
import com.zack.xjht.Utils.Utils;
import com.zack.xjht.db.DBManager;
import com.zack.xjht.db.gen.CabInfoBeanDao;
import com.zack.xjht.db.gen.OfflineTaskDao;
import com.zack.xjht.db.gen.UserBeanDao;
import com.zack.xjht.db.gen.UserBiosBeanDao;
import com.zack.xjht.entity.CabInfoBean;
import com.zack.xjht.entity.OfflineTask;
import com.zack.xjht.entity.SubCabBean;
import com.zack.xjht.entity.UserBean;
import com.zack.xjht.entity.UserBiosBean;
import com.zack.xjht.face.faceserver.FaceServer;
import com.zack.xjht.finger.FingerManager;
import com.zack.xjht.http.HttpClient;
import com.zack.xjht.http.HttpListener;
import com.zack.xjht.iris.IrisManager;
import com.zack.xjht.serial.SerialPortUtil;
import com.zack.xjht.service.DataCacheService;
import com.zack.xjht.service.QueryService;


import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends BaseActivity {
    private static final String TAG = "MainActivity";
    @BindView(R.id.main_top_tittle)
    ImageView mainTopTittle;
    @BindView(R.id.standby_img_bg)
    ImageView standbyImgBg;
    @BindView(R.id.main_calendar_date)
    TextView mainCalendarDate;
    @BindView(R.id.main_btn_get_gun)
    ImageView mainBtnGetGun;
    @BindView(R.id.main_btn_back_gun)
    ImageView mainBtnBackGun;
    @BindView(R.id.main_btn_urgency_open)
    ImageView mainBtnUrgencyOpen;
    @BindView(R.id.main_btn_other)
    ImageView mainBtnOther;
    @BindView(R.id.main_btn_sync)
    ImageView mainBtnSync;
    @BindView(R.id.main_ll_functional)
    LinearLayout mainLlFunctional;
    private boolean isUpdateTime = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        SerialPortUtil.getInstance().onCreate();
        startService(new Intent(MainActivity.this, DataCacheService.class));
        startService(new Intent(MainActivity.this, QueryService.class));

        InitUSBDevice();

        if (Utils.isNetworkAvailable()) {
            syncTime();//同步时间
        }

        IrisManager.getInstance().initIris();

        FaceServer.getInstance().init(this);
        ac_top_back.setVisibility(View.GONE);

        new Thread(updateTimeThread).start();

//        new Thread(checkGunStatus).start();

        switch (SharedUtils.getCabType()) {
            case Constants.TYPE_AMMO_CAB://弹药柜
                mainBtnGetGun.setImageResource(R.drawable.get_ammo);
                mainBtnBackGun.setImageResource(R.drawable.back_ammo);
                //                                mainTopTittle.setText("新疆航天智能弹柜系统");
                break;
            case Constants.TYPE_MIX_CAB://枪弹综合柜
                mainBtnGetGun.setImageResource(R.drawable.get_gun_ammo);
                mainBtnBackGun.setImageResource(R.drawable.back_gun_ammo);
                //                                mainTopTittle.setText("新疆航天智能枪弹柜系统");
                break;
            case Constants.TYPE_LONG_GUN_CAB: //长枪柜
            case Constants.TYPE_SHORT_GUN_CAB://短枪柜
            case Constants.TYPE_SHORT_LONG_GUN_CAB: //长短枪混合柜
                mainBtnGetGun.setImageResource(R.drawable.get_gun);
                mainBtnBackGun.setImageResource(R.drawable.back_gun);
                //                                mainTopTittle.setText("新疆航天智能枪柜系统");
                break;
        }
    }

    private Runnable updateTimeThread = new Runnable() {
        @Override
        public void run() {
            while (isUpdateTime) {
                setDate();
                try {
                    Thread.sleep(3 * 60 * 60 * 1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    private boolean isCheckGun = true;
    private Runnable checkGunStatus = new Runnable() {
        @Override
        public void run() {
            while (isCheckGun) {
                if (Constants.isCheckGunStatus) {
                    for (int i = 1; i <= 8; i++) {
                        SerialPortUtil.getInstance().checkStatus(i);
                        try {
                            Thread.sleep(200);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    };

    //同步时间
    private void syncTime() {
        HttpClient.getInstance().getDateAndTime(this, new HttpListener<String>() {
            @Override
            public void onSucceed(int what, Response<String> response) throws JSONException {
//                Log.i(TAG, "onSucceed response: " + response.get());
                try {
                    if (!TextUtils.isEmpty(response.get())) {
                        long longTime = Utils.stringTime2Long(response.get());
                        boolean isSetTime = SystemClock.setCurrentTimeMillis(longTime);
                        if (isSetTime) {
                            //                        ToastUtil.showShort("同步时间成功！");
                            Log.i(TAG, "onSucceed 同步时间成功: ");
                        } else {
                            ToastUtil.showShort("同步时间失败！");
                            Log.i(TAG, "onSucceed 同步时间失败: ");
                        }
                    } else {
                        ToastUtil.showShort("获取时间为空");
                        Log.i(TAG, "onSucceed 获取到的时间为空: ");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    ToastUtil.showShort("获取时间出错");
                    Log.i(TAG, "onSucceed 请求时间错误: ");
                }
            }

            @Override
            public void onFailed(int what, Response<String> response) {
                Log.i(TAG, "onFailed 网络错误，同步时间失败: ");
                ToastUtil.showShort("网络错误，同步时间失败");
            }
        });
    }

    /**
     * 初始化USB设备
     */
    private void InitUSBDevice() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        intentFilter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);

        registerReceiver(mUsbStateChangeReceiver, intentFilter);
        getUsbDevices();

        String ethernetMacAddress = "";
        try {
            FileInputStream fis = new FileInputStream("/sys/class/net/eth0/address");
            BufferedReader input = new BufferedReader(new InputStreamReader(fis));
            ethernetMacAddress = input.readLine();
            SharedUtils.setMacAddress(ethernetMacAddress);
            Log.d(TAG, "Ethernet MAC Address: " + ethernetMacAddress);
        } catch (IOException ex) {
            Log.e(TAG, "ex: " + ex);
        }
    }

    private BroadcastReceiver mUsbStateChangeReceiver = new BroadcastReceiver() {
        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.i(TAG, "onReceive action: " + action);
            switch (action) {
                case UsbManager.ACTION_USB_DEVICE_ATTACHED: //USB设备插入
                    ToastUtil.showShort("USB设备插入");
                    UsbDevice inputDevice = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    int vendorId1 = inputDevice.getVendorId();
                    int productId1 = inputDevice.getProductId();
                    String deviceName = inputDevice.getDeviceName();
                    String manufacturerName1 = inputDevice.getManufacturerName();
                    String productName1 = inputDevice.getProductName();

                    Log.i(TAG, "onReceive input device name: " + deviceName
                            + "  PID VID:" + String.format("%04X, %04X", productId1, vendorId1));
                    if (vendorId1 == 0x2109 && productId1 == 0x7638) {
                        if (manufacturerName1.equals("USBKey Chip") && productName1.equals("USBKey Module")) {
                            ToastUtil.showShort("指纹设备插入");
                            Constants.isFingerConnect = true;
                            FingerManager.getInstance().init(MainActivity.this);
                            List<UserBiosBean> userBiosBeans = DBManager.getInstance().getUserBiosBeanDao().loadAll();
                            saveBiosDataToLocal(userBiosBeans);
                            downCHar(userBiosBeans);
                        }
                    }
                    break;
                case UsbManager.ACTION_USB_DEVICE_DETACHED://USB设备拔出
                    ToastUtil.showShort("USB设备拔出");
                    UsbDevice outputDevice = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    String manufacturerName = outputDevice.getManufacturerName();
                    String productName = outputDevice.getProductName();
                    String serialNumber = outputDevice.getSerialNumber();
//                    String version = outputDevice.getVersion();
                    Log.i(TAG, "onReceive manufacturerName: " + manufacturerName
                            + "  productName: " + productName
                            + "  serialNumber: " + serialNumber);

                    Log.i(TAG, "onReceive output device name: " + outputDevice.getDeviceName()
                            + "  PID VID:" + String.format("%04X, %04X",
                            outputDevice.getProductId(), outputDevice.getVendorId()));
                    int vendorId = outputDevice.getVendorId();
                    int productId = outputDevice.getProductId();
                    if (vendorId == 0x2109 && productId == 0x7638) {
                        if (manufacturerName.equals("USBKey Chip") &&
                                productName.equals("USBKey Module")) {
                            ToastUtil.showShort("指纹设备拔出");
                            Constants.isFingerConnect = false;
                        }
                    }
                    break;
            }
        }
    };

    /**
     * 当前插入的usb设备
     *
     * @return
     */
    private int getUsbDevices() {
        UsbManager mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        HashMap<String, UsbDevice> deviceList = mUsbManager.getDeviceList();
        Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
        Iterator<String> iterator = deviceList.keySet().iterator();
        while (iterator.hasNext()) {
            String next = iterator.next();
            Log.i(TAG, "getUsbDevices next: " + next);
        }
        while (deviceIterator.hasNext()) {
            UsbDevice device = deviceIterator.next();
            Log.i(TAG, "getUsbDevices deviceName:" + device.getDeviceName()
                    + " VID PID:" + String.format("%04x", device.getVendorId(), device.getProductId()));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                if ((device.getVendorId() == 0x2109) && (0x7638 == device.getProductId())) {
                    String manufacturerName = device.getManufacturerName();
                    String productName = device.getProductName();
                    String serialNumber = device.getSerialNumber();
                    Log.i(TAG, "getUsbDevices manufacturerName: " + manufacturerName
                            + "  productName:" + productName
                            + "  serialNumber:" + serialNumber);
                    if (manufacturerName.equals("USBKey Chip") && productName.equals("USBKey Module")) {
                        ToastUtil.showShort("指纹设备已插入");
                        Log.i(TAG, "getUsbDevices 指纹设备已插入: ");
                        Constants.isFingerConnect = true;
                        FingerManager.getInstance().init(this);
                        List<UserBiosBean> userBiosBeans = DBManager.getInstance().getUserBiosBeanDao().loadAll();
                        saveBiosDataToLocal(userBiosBeans);
                        downCHar(userBiosBeans);
                    }
                }
            }
        }
        return 0;
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "onResume: ");
        //获取枪柜数据
        getCabData();
    }

    /**
     * 获取当前枪柜数据
     */
    private void getCabData() {
        CabInfoBeanDao cabInfoBeanDao = DBManager.getInstance().getCabInfoBeanDao();
        List<CabInfoBean> cabInfoBeans = cabInfoBeanDao.loadAll();
        if (cabInfoBeans != null && !cabInfoBeans.isEmpty()) {
            CabInfoBean cabInfoBean = cabInfoBeans.get(0);
            String gunCabinetType = cabInfoBean.getGunCabinetType();//枪柜类型
            String id = cabInfoBean.getId();
            SharedUtils.setCabType(gunCabinetType);
            SharedUtils.saveGunCabId(id);
            switch (gunCabinetType) {
                case Constants.TYPE_AMMO_CAB://弹药柜
                    mainBtnGetGun.setImageResource(R.drawable.get_ammo);
                    mainBtnBackGun.setImageResource(R.drawable.back_ammo);
                    break;
                case Constants.TYPE_MIX_CAB://枪弹综合柜
                    mainBtnGetGun.setImageResource(R.drawable.get_gun_ammo);
                    mainBtnBackGun.setImageResource(R.drawable.back_gun_ammo);
                    break;
                case Constants.TYPE_LONG_GUN_CAB: //长枪柜
                case Constants.TYPE_SHORT_GUN_CAB://短枪柜
                case Constants.TYPE_SHORT_LONG_GUN_CAB: //长短枪混合柜
                    mainBtnGetGun.setImageResource(R.drawable.get_gun);
                    mainBtnBackGun.setImageResource(R.drawable.back_gun);
                    break;
            }
        } else {
            Log.i(TAG, "getCabData: ");
            Log.i(TAG, "getCabData cabInfoBeans is null: ");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    /**
     * 使用闹钟每日唤醒日期
     */
    private void setDate() {
        Calendar calendar = Calendar.getInstance();
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        String xingqi = "";
        switch (dayOfWeek) {
            case Calendar.SUNDAY:
                xingqi = "星期日";
                break;
            case Calendar.MONDAY:
                xingqi = "星期一";
                break;
            case Calendar.TUESDAY:
                xingqi = "星期二";
                break;
            case Calendar.WEDNESDAY:
                xingqi = "星期三";
                break;
            case Calendar.THURSDAY:
                xingqi = "星期四";
                break;
            case Calendar.FRIDAY:
                xingqi = "星期五";
                break;
            case Calendar.SATURDAY:
                xingqi = "星期六";
                break;
        }
        final String date = DateFormat.format(
                "yyyy年MM月dd日", System.currentTimeMillis()).toString();

        String s = ac_top_date_txt.getText().toString();
        Log.i(TAG, "setData top date: " + s);

        final String week = xingqi;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mainCalendarDate.setText(date + "  " + week);
            }
        });
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy: ");
        isCheckGun = false;
        stopService(new Intent(MainActivity.this, QueryService.class));
        stopService(new Intent(MainActivity.this, DataCacheService.class));
        if (mUsbStateChangeReceiver != null) {
            //取消注册指纹监听广播
            unregisterReceiver(mUsbStateChangeReceiver);
        }
        SerialPortUtil.getInstance().close(); //关闭串口
        FaceServer.getInstance().unInit(); //取消人脸识别初始化
        isUpdateTime = false; //更新时间
    }

    Dialog choiceDialog = null;

    @OnClick({R.id.main_btn_get_gun, R.id.main_btn_back_gun, R.id.main_btn_urgency_open,
            R.id.main_btn_sync, R.id.main_btn_other})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.main_btn_get_gun: //取枪
//                if(SharedUtils.getIsServerOnline()){
//                    startActivity(new Intent(MainActivity.this, GetActivity.class));
//                }else{
                //无法连接服务器，使用本地数据
                startActivity(new Intent(MainActivity.this, OfflineGetActivity.class));
//                }
                break;
            case R.id.main_btn_back_gun://还枪
//                OfflineTaskDao offlineTaskDao = DBManager.getInstance().getOfflineTaskDao();
//                List<OfflineTask> list = offlineTaskDao.queryBuilder().where(
//                        OfflineTaskDao.Properties.TaskStatus.eq(1)).list();
//                if(SharedUtils.getIsServerOnline() && list.isEmpty()){
//                    startActivity(new Intent(MainActivity.this, BackActivity.class));
//                }else{
                //无法连接服务器，使用本地数据
                startActivity(new Intent(MainActivity.this, OfflineBackActivity.class));
//                }
                break;
            case R.id.main_btn_urgency_open://应急处突
                if (!SharedUtils.getCabType().equals(Constants.TYPE_AMMO_CAB)) {
                    startActivity(new Intent(MainActivity.this, UrgentGoActivity.class));
                } else {
                    //弹柜 点击弹出选择界面
                    choiceDialog = DialogUtils.createChoiceDialog(this,
                            "请选择领取或归还弹药",
                            new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    //领取子弹
                                    Intent intent = new Intent(MainActivity.this, VerifyActivity.class);
                                    intent.putExtra("activity", Constants.ACTIVITY_URGENT_GET_AMMO);
                                    startActivity(intent);
                                    choiceDialog.dismiss();
                                }
                            }, new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    //归还弹药
                                    Intent intent = new Intent(MainActivity.this, VerifyActivity.class);
                                    intent.putExtra("activity", Constants.ACTIVITY_URGENT_BACK_AMMO);
                                    startActivity(intent);
                                    choiceDialog.dismiss();
                                }
                            });
                    if (!isFinishing() && !choiceDialog.isShowing()) {
                        choiceDialog.show();
                    }
                }
                break;
            case R.id.main_btn_sync://系统设置
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                break;
            case R.id.main_btn_other:
                startActivity(new Intent(MainActivity.this, IntegratedActivity.class));
                break;
        }
    }

    //下发模版
    private void downCHar(List<UserBiosBean> userBiosBeans) {
//        Log.i(TAG, "downCHar: ");
        if (!userBiosBeans.isEmpty()) {
//            Log.i(TAG, "downCHar size: " + userBiosBeans.size());
//              updateDlMsgInfo.append("清除所有指纹\n");
            if (Constants.isFingerConnect && Constants.isFingerInit) {
                //清除设备中所有指纹
                FingerManager.getInstance().clearAllFinger();
            }

            if (Constants.isFaceInit) {
                //删除所有人脸生物特征
                deleteFaceLib();
            }
            //清除虹膜模版
            if (Constants.isIrisInit) {
                IrisManager.getInstance().deleteAllTemp();
            }

            for (UserBiosBean policeBiosBean : userBiosBeans) {
                String deviceType = policeBiosBean.getBiometricsType();
                String key = policeBiosBean.getBiometricsKey();
                int id = policeBiosBean.getBiometricsNumber();
                Log.i(TAG, "downCHar id: " + id);
//                byte[] decodeKey = Base64.decode(key, Base64.DEFAULT);
                byte[] decodeKey = TransformUtil.hexStrToBytes(key);
                Log.i(TAG, "downCHar key " + key + " \n decodeKey: " + decodeKey.length);
                switch (deviceType) {
                    case Constants.DEVICE_FINGER:
                        if (Constants.isFingerConnect && Constants.isFingerInit) {
                            Log.i(TAG, "downCHar fingerID: " + id);
                            FingerManager.getInstance().fpDownChar(id, decodeKey);
                        }
                        break;
                    case Constants.DEVICE_FACE:
                        if (Constants.isFaceInit) {
                            FaceServer.getInstance().saveFaceFeature(this, decodeKey, String.valueOf(id));
                        }
                        break;
                    case Constants.DEVICE_IRIS:
                        if (Constants.isIrisInit) {
                            IrisManager.getInstance().downTemplate(String.valueOf(id), key);
                        }
                        break;
                }
            }
//            showDialog("生物特征更新成功");
            Log.i(TAG, "downCHar: 生物特征更新成功!");
        } else {
//            showDialog("无生物特征数据");
            Log.i(TAG, "downCHar:无生物特征数据 ! ");
        }
    }

    public void deleteFaceLib() {
        //获取人脸注册数量
        int faceNum = FaceServer.getInstance().getFaceNumber(this);
        Log.i(TAG, "deleteFaceLib faceNum: " + faceNum);
        if (faceNum == 0) {
//            ToastUtil.showShort(getString(R.string.batch_process_no_face_need_to_delete));
            Log.i(TAG, "deleteFaceLib 人脸数据为空: ");
        } else {
            /**
             * 清空所有人脸注册数据
             */
            int deleteCount = FaceServer.getInstance().clearAllFaces(this);
//            ToastUtil.showShort(deleteCount + " 清空人脸数据完成!");
            Log.i(TAG, "deleteFaceLib 清空人脸数据完成: " + deleteCount);
        }
    }

    private void saveBiosDataToLocal(List<UserBiosBean> userBiosBeans) {
        if (!userBiosBeans.isEmpty()) {
            UserBiosBeanDao userBiosBeanDao = DBManager.getInstance().getUserBiosBeanDao();
            userBiosBeanDao.deleteAll(); //删除所有人员特征数据
            userBiosBeanDao.insertInTx(userBiosBeans);//插入所有特征数据
        }
    }
}
