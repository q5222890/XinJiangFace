package com.zack.xjht.finger;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.za.android060;
import com.zack.xjht.Constants;
import com.zack.xjht.R;
import com.zack.xjht.Utils.SharedUtils;
import com.zack.xjht.Utils.SoundPlayUtil;
import com.zack.xjht.Utils.Utils;
import com.zafinger.ZA_finger;

import java.io.DataOutputStream;
import java.io.File;
import java.util.HashMap;
import java.util.Iterator;


/**
 * 指纹管理
 */
public class FingerManager {

    private static final String TAG = "FingerManager";
    private static volatile FingerManager  mInstance =null;
    private android060 a6 = new android060();
    int DEV_ADDR = 0xffffffff;
    private boolean fpflag = false;
    private boolean fpcharflag = false;
    private boolean fpmatchflag = false; //比对是否正确
    public boolean fperoll = false;  //注册标志
    public boolean fpsearch = false;  //搜索标志
    public boolean isfpon = false; //是否按手指

    private int testcount = 0;
    private int fpcharbuf = 1; //特征码 iBufferID： 0x01、 0x02(电容/光学) 0x01、 0x02、 0x03、 0x04(刮擦)
    private TextView mtvMessage;
    long ssart = System.currentTimeMillis();
    long ssend = System.currentTimeMillis();
    private Handler objHandler_fp;
    private HandlerThread handlerThread;

    private ImageView mFingerprintIv;
    private int IMG_SIZE = 0;//同参数：（0:256x288 1:256x360）
    private int defiCom = 3; //默认串口号
    private int defiBaud = 6; //默认波特率
    private int usborcomtype = 0; ///0 noroot  1root
    private int defDeviceType = 12;  //设备类型 9900
//        private int defDeviceType = 2;  //设备类型  zaz060
    private Context mContext;
    private IFingerStatus iFingerStatus;
    private IEnrollStatus iEnrollStatus;
    private int fd;
    private Handler mHandler = new Handler();

    private FpDownCharTasks fpdowncharTasks;
    private ClearAllFingerTask clearAllFingerTask;

    public static FingerManager getInstance() {
        if (mInstance == null) {
            synchronized (FingerManager.class){
                if(mInstance ==null){
                    mInstance = new FingerManager();
                }
            }
        }
        return mInstance;
    }

    private FingerManager() {
    }

    public interface IFingerStatus {
        void didVerify(int id, boolean success);
        void timeout();
    }

    public interface IEnrollStatus {
        void onEnrollStatus(int id, byte[] fpChar, boolean success);
    }

    public void init(Context context) {
        this.mContext = context;
        handlerThread = new HandlerThread("FingerHandlerThread");
        handlerThread.start();
        objHandler_fp = new Handler(handlerThread.getLooper());

        if(SharedUtils.getFingerprintType() ==0){
            defDeviceType =12;
        }else{
            defDeviceType =2;
        }
        objHandler_fp.post(new Runnable() {
            @Override
            public void run() {
                openDevice();
            }
        });
    }

    public boolean openDevice() {
        Log.i(TAG, "openFingerDev");
        byte[] pPassword = new byte[4];
        ZA_finger fppower = new ZA_finger();
        //开启电源
        fppower.finger_power_on();
        fppower.card_power_on();

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        int status = 0;
        if (1 == usborcomtype) {  //root
            LongDunD8800_CheckEuq();
            Log.i(TAG, "usborcomtype ==1");
            /**
             * -1 fd ,defDeviceType 设备类型，defiCom 串口号 ，defiBaud 波特率 ，nPackageSize 通讯包大小（默认:2）iDevNum 通讯端口号(默认 0)
             */
            status = a6.ZAZOpenDeviceEx(-1, defDeviceType, defiCom, defiBaud, 0, 0);

            if (status == 1 && a6.ZAZVfyPwd(DEV_ADDR, pPassword) == 0) {
                status = 1;
            } else {
                status = 0;
            }

            a6.ZAZSetImageSize(IMG_SIZE);
        } else { //noroot
            Log.i(TAG, "usborcomtype ==0");
            fd = getrwusbdevices();  //fileDescriptor
            Log.e(TAG, "zhw === open fd: " + fd);
            status = a6.ZAZOpenDeviceEx(fd, defDeviceType, defiCom, defiBaud, 0, 0);
        }
        Log.e(TAG, " open status: " + status);
        if (status == 1) {
            fpsearch = false;
            String temp = "指纹初始化成功";
            Log.i(TAG, temp);
            Constants.isFingerInit =true;
            return true;
        } else {
            String temp = "指纹初始化失败";
            Log.i(TAG, temp);
            Constants.isFingerInit =false;
            return false;
        }
    }

    /**
     * 搜索指纹
     *
     * @param txtMessage
     * @param iFingerStatus
     */
    public void searchfp(TextView txtMessage, ImageView imageView, IFingerStatus iFingerStatus) {
        ssart = System.currentTimeMillis();
        ssend = System.currentTimeMillis();
        fpcharbuf = 1;
        testcount = 0;
//        isfpon =false;
        this.mtvMessage = txtMessage;
        this.mFingerprintIv =imageView;
        this.iFingerStatus = iFingerStatus;
        if(objHandler_fp !=null){
            objHandler_fp.postDelayed(fpsearchTasks, 0);
        }
    }

    /**
     * 搜索指纹
     *
     * @param txtMessage
     * @param iFingerStatus
     */
    public void searchfp(TextView txtMessage, IFingerStatus iFingerStatus) {
        ssart = System.currentTimeMillis();
        ssend = System.currentTimeMillis();
        fpcharbuf = 1;
        testcount = 0;
//        isfpon =false;
        this.mtvMessage = txtMessage;
        this.iFingerStatus = iFingerStatus;
        if(objHandler_fp !=null){
            objHandler_fp.postDelayed(fpsearchTasks, 0);
        }
    }

    private Runnable fpsearchTasks = new Runnable() {
        public void run() {
            String temp = "";
            long timecount = 0;
            int[] id_iscore = new int[1];
            ssend = System.currentTimeMillis();
            timecount = (ssend - ssart);

            if (timecount > 30000) {
                temp = "读指纹等待超时" + "\r\n";
                sendMsg(temp);
                iFingerStatus.timeout();
                return;
            }
            if (fpsearch) {
                temp = "搜索主动停止" + "\r\n";
                sendMsg(temp);
                return;
            }
            int nRet = 0;
            nRet = a6.ZAZGetImage(DEV_ADDR);
            if (nRet == 0) {
                testcount = 0;
                if(mFingerprintIv !=null){
                    int[] len = {0, 0};
                    byte[] Image =new byte[256*360];
                    a6.ZAZUpImage(DEV_ADDR, Image, len);

                    String strPath ="sdcard/fingerprint.png";
                    a6.ZAZImgData2BMP(Image, strPath);
//                temp = "获取图像成功";
//                mtvMessage.setText(temp);
                    Bitmap bmpDefaultPic;
                    bmpDefaultPic = BitmapFactory.decodeFile(strPath, null);
                    setFingerPrintImage(bmpDefaultPic);
                }
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                nRet = a6.ZAZGetImage(DEV_ADDR);
            }

            if (nRet == 0) {
                if (isfpon) {
                    temp = "请拿起手指";
                    Log.i(TAG, "run: " + temp);
                    sendMsg(temp);
//                    streamId = SoundPlayUtil.getInstance().play(R.raw.enroll_finger_up_finger);
                    ssart = System.currentTimeMillis();
                    objHandler_fp.postDelayed(fpsearchTasks, 100);
                    return;
                }

                nRet = a6.ZAZGenChar(DEV_ADDR, fpcharbuf);
                if (nRet == a6.PS_OK) {
                    isfpon = true;
                    nRet = a6.ZAZHighSpeedSearch(DEV_ADDR, 1, 0, 1000, id_iscore);
                    if (nRet == a6.PS_OK) {
                        int fingerId = id_iscore[0];
                        temp = "搜索指纹成功   ID:" + fingerId;
                        sendMsg(temp);
                        iFingerStatus.didVerify(fingerId, true);
//                        ssart = System.currentTimeMillis();
//                        objHandler_fp.postDelayed(fpsearchTasks, 100);
                    } else {
                        temp = "搜索指纹失败，请重试！";
                        sendMsg(temp);
                        iFingerStatus.didVerify(-1, false);
                        ssart = System.currentTimeMillis();
                        objHandler_fp.postDelayed(fpsearchTasks, 100);
                    }
                } else {
                    temp = "特征太差，请重新录入";
                    sendMsg(temp);
                    ssart = System.currentTimeMillis();
                    objHandler_fp.postDelayed(fpsearchTasks, 1000);
                }

            } else if (nRet == a6.PS_NO_FINGER) {
                temp = "正在读取指纹中   剩余时间:" + ((30000 - (ssend - ssart))) / 1000 + "s";
                isfpon = false;
                sendMsg(temp);
                objHandler_fp.postDelayed(fpsearchTasks, 10);
            } else if (nRet == a6.PS_GET_IMG_ERR) {
                temp = "图像获取中";
                Log.d(TAG, temp + ": " + nRet);
                objHandler_fp.postDelayed(fpsearchTasks, 10);
                sendMsg(temp);
                return;
            } else if (nRet == -2) {
                testcount++;
                if (testcount < 3) {
                    temp = "正在读取指纹中   剩余时间:" + ((30000 - (ssend - ssart))) / 1000 + "s";
                    isfpon = false;
                    sendMsg(temp);
                    objHandler_fp.postDelayed(fpsearchTasks, 10);
                } else {
                    if(!Constants.isFingerConnect){
                        temp="指纹设备未连接";
                        Log.d(TAG, temp + ": " + nRet);
                        sendMsg(temp);
                        Constants.isFingerConnect =false;
                        return;
                    }
                    if(!Constants.isFingerInit){
                        temp="指纹初始化失败";
                        Log.d(TAG, temp + ": " + nRet);
                        sendMsg(temp);
                        Constants.isFingerInit =false;
                        return;
                    }
                    temp="指纹通讯异常";
                    Log.d(TAG, temp + ": " + nRet);
                    sendMsg(temp);
                    Constants.isFingerInit =false;
                    return;
                }
            } else {
                if(!Constants.isFingerConnect){
                    temp = "指纹设备未连接";
                    Log.d(TAG, temp + ": " + nRet);
                    sendMsg(temp);
                    Constants.isFingerConnect =false;
                    return;
                }
                if(!Constants.isFingerInit){
                    temp = "指纹初始化失败";
                    Log.d(TAG, temp + ": " + nRet);
                    sendMsg(temp);
                    Constants.isFingerInit =false;
                    return;
                }
                temp = "指纹通讯异常";
                Log.d(TAG, temp + ": " + nRet);
                sendMsg(temp);
                Constants.isFingerInit =false;
                return;
            }
        }
    };

    private void setFingerPrintImage(final Bitmap bmpDefaultPic) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if(mFingerprintIv !=null){
                    mFingerprintIv.setImageBitmap(bmpDefaultPic);
                }
            }
        });
    }

    private void sendMsg(final String message) {
//        Log.i(TAG, "sendMsg: "+message);
       mHandler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    if (mtvMessage != null && !TextUtils.isEmpty(message)) {
                        mtvMessage.setText(message);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * 下载模版
     */
    public void downChar(String charBuf1, String charBuf2, int fingerId) {
        Log.i(TAG, "downChar");
        fpcharbuf = 1;
        objHandler_fp.removeCallbacks(fpdowncharTasks);
        fpdowncharTasks = new FpDownCharTasks(charBuf1, charBuf2, fingerId);
        objHandler_fp.post(fpdowncharTasks);  //执行线程
    }

    private class FpDownCharTasks implements Runnable {

        private final String charBuf1;
        private final String charBuf2;
        private final int iPageID;

        public FpDownCharTasks(String charBuf1, String charBuf2, int fingerId) {
            this.charBuf1 = charBuf1;
            this.charBuf2 = charBuf2;
            this.iPageID = fingerId;
        }

        @Override
        public void run() {
            Log.i(TAG, "fpdowncharTasks");
            int nRet = 0;
            String temp = "";

            Log.i(TAG, "iPageID :" + iPageID);
            Log.i(TAG, "charBuf1 :" + charBuf1);
            Log.i(TAG, "charBuf2 :" + charBuf2);

            int iTempletLenth = 512;
            byte[] templet = Utils.hexString2Bytes(charBuf1);
            nRet = a6.ZAZDownChar(DEV_ADDR, fpcharbuf, templet, iTempletLenth); //下载特征

            Log.i(TAG, "返回值1：" + nRet);
            if (nRet == a6.PS_OK) {
                temp = "写入特征成功";
                Log.i(TAG, temp);
                sendMsg(temp);
                try {
                    handlerThread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                templet = Utils.hexString2Bytes(charBuf2);

                nRet = a6.ZAZDownChar(DEV_ADDR, fpcharbuf, templet, iTempletLenth);  //下载特征
                Log.i(TAG, "返回值2：" + nRet);
            }

            if (nRet == a6.PS_OK) {
                nRet = a6.ZAZStoreChar(DEV_ADDR, 1, iPageID); //存储模板
                temp = "存储指纹摸板";
                Log.i(TAG, temp);
                sendMsg(temp);
                if (nRet == a6.PS_OK) {
                    temp = "存储模板成功   存储ID:" + iPageID;
                    sendMsg(temp);
                    Log.i(TAG, temp);
                } else {
                    temp = "存储模版失败";
                    sendMsg(temp);
                    Log.i(TAG, temp);
                }
            } else {
                temp = "下载特征失败";
                sendMsg(temp);
                Log.i(TAG, temp);
            }
        }
    }

    /**
     * 模版写入指纹
     */
    public void fpDownChar(int fingerId, byte[] template) {
        objHandler_fp.removeCallbacks(fpsearchTasks);
        objHandler_fp.post(new FingerDownCharTask(fingerId, template));
    }

    private class FingerDownCharTask implements Runnable {
        private int fingerId;
        private byte[] template;

        FingerDownCharTask(int fingerId, byte[] template) {
            this.fingerId = fingerId;
            this.template = template;
        }

        @Override
        public void run() {
            int nRet;
            int iTempletLenth = 512;
            String temp;
            nRet = a6.ZAZDownChar(DEV_ADDR, fpcharbuf, template, iTempletLenth);
            if (nRet == a6.PS_OK) {
                nRet = a6.ZAZStoreChar(DEV_ADDR, fpcharbuf, fingerId);
//                temp = "存储指纹模板";
//                Log.i(TAG, temp);
                if (nRet == a6.PS_OK) {
                    temp = "存储指纹成功   存储ID:" + fingerId;
                    Log.i(TAG, temp);
                } else {
                    temp = "存储指纹失败";
                    Log.i(TAG, temp);
                }

            } else {
                Log.i(TAG, "fingerDownCharTask: 写入特征失败");
            }
        }
    }

    public void clearAllFinger() {
        clearAllFingerTask = new ClearAllFingerTask();
        objHandler_fp.post(new ClearAllFingerTask());
    }

    class ClearAllFingerTask implements Runnable {

        @Override
        public void run() {
            String temp;
            int Rnet = a6.ZAZEmpty(DEV_ADDR);
            if (Rnet == a6.PS_OK) {
                temp = "清空指纹成功" + "\r\n";
                Log.i(TAG, temp);
            } else {
                temp = "清除指纹失败";
                Log.i(TAG, temp);
            }
        }
    }

    int iPageID;
    int fperollTime;

    //注册指纹
    public void erollfp(TextView txt, int fingerId, IEnrollStatus enrollStatus) {
        ssart = System.currentTimeMillis();
        ssend = System.currentTimeMillis();
        fpcharbuf = 1;
        fperollTime = 1;
        isfpon = false;
        testcount = 0; //注册读取次数
        this.mtvMessage = txt;
        this.iPageID = fingerId;
        this.iEnrollStatus = enrollStatus;
        try {
            objHandler_fp.postDelayed(fperollTasks, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int streamId;
    boolean isFirstPutFinger = false;
    boolean isSecondPutFinger = false;
    private Runnable fperollTasks = new Runnable() {
        public void run()// 运行该服务执行此函数
        {
            String temp = "";
            long timecount = 0;
            ssend = System.currentTimeMillis();
            timecount = (ssend - ssart);
            Log.i(TAG, "run fperollTasks: " + timecount);

            if (timecount > 30000) {
                temp = "读指纹等待超时" + "\r\n";
                Log.i(TAG, "run : " + temp);
                sendMsg(temp);
                return;
            }
            if (fperoll) {
                temp = "注册主动停止" + "\r\n";
                Log.i(TAG, "run : " + temp);
                sendMsg(temp);
                return;
            }
            int nRet = 0;
            nRet = a6.ZAZGetImage(DEV_ADDR);
            Log.i(TAG, "run 第一次获取图像 nRet: " + nRet);
            if (nRet == 0) {
                testcount = 0;
                try {
                    handlerThread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                nRet = a6.ZAZGetImage(DEV_ADDR);
                Log.i(TAG, "run 第二次获取图像 nRet: " + nRet);
            }

            Log.i(TAG, "run 获取图像nRet: " + nRet);
            if (nRet == 0) {
                if (isfpon) {
                    temp = "请拿起手指";
                    Log.i(TAG, "run: " + temp);
//                    streamId = SoundPlayUtil.getInstance().play(R.raw.enroll_finger_up_finger);
                    sendMsg(temp);
                    ssart = System.currentTimeMillis();
                    objHandler_fp.postDelayed(fperollTasks, 100);
                    return;
                }
                nRet = a6.ZAZGenChar(DEV_ADDR, fpcharbuf);// != PS_OK) { //生成特征
                Log.i(TAG, "run 生成特征: " + nRet);
                if (nRet == a6.PS_OK) {
//                    Log.i(TAG, "run 获取指纹 fpcharbuf:: " + fpcharbuf); //生成模版次数
                    fperollTime++;
                    fpcharbuf++;
                    isfpon = true;
                    if (fpcharbuf > 2) {
                        nRet = a6.ZAZRegModule(DEV_ADDR);  //合成模板
                        Log.i(TAG, "run 合成模版: " + nRet);
                        if (nRet != a6.PS_OK) {
                            streamId = SoundPlayUtil.getInstance().play(R.raw.enroll_finger_char_mismatch);
                            temp = "指纹特征不匹配，合成模板失败";
                            Log.i(TAG, "run : " + temp);
                            sendMsg(temp);
                        } else {
                            int[] iTempletLength = {0, 0};
                            byte[] pTemplet = new byte[512];
                            //上传特征函数
                            nRet = a6.ZAZUpChar(DEV_ADDR, a6.CHAR_BUFFER_A, pTemplet, iTempletLength); //上传特征
                            if (nRet == a6.PS_OK) {
//                        temp = "指i纹特征:\r\n";
//                        temp += charToHexStrng(pTemplet);
//                                Log.i(TAG, "run: " + charToHexString(pTemplet));
                            }
                            Log.i(TAG, "run ipageId: " + iPageID);
                            nRet = a6.ZAZStoreChar(DEV_ADDR, 1, iPageID); //存储模版
                            if (nRet == a6.PS_OK) {
                                streamId = SoundPlayUtil.getInstance().play(R.raw.enroll_finger_success);
                                temp = "注册指纹成功   存储ID:" + iPageID;
                                sendMsg(temp);
//                                iFingerStatus.didVerify(iPageID, true); //注册成功回调
                                iEnrollStatus.onEnrollStatus(iPageID, pTemplet, true);
                                Base64.encode(pTemplet, Base64.DEFAULT);
                            } else {
                                streamId = SoundPlayUtil.getInstance().play(R.raw.enroll_finger_failed);
                                temp = "注册指纹失败";
                                sendMsg(temp);
//                                iFingerStatus.didVerify(-1, false); //注册失败回调
                                iEnrollStatus.onEnrollStatus(-1, null, false);
                            }
                        }
                    } else {
                        temp = "获取指纹成功";
                        sendMsg(temp);
                        ssart = System.currentTimeMillis();
                        objHandler_fp.postDelayed(fperollTasks, 500);
                    }
                } else {
                    temp = "特征太差，请重新录入";
                    sendMsg(temp);
                    ssart = System.currentTimeMillis();
                    objHandler_fp.postDelayed(fperollTasks, 1000);
                }

            } else if (nRet == a6.PS_NO_FINGER) {
                temp = "第" + fperollTime + "次录入指纹  剩余时间:" + ((30000 - (ssend - ssart))) / 1000 + "s";
//                Log.i(TAG, "======= testcount: " + temp);
                if(fperollTime ==1){
                    if(!isFirstPutFinger){
                        isFirstPutFinger =true;
                        isSecondPutFinger =false;
                        streamId = SoundPlayUtil.getInstance().play(R.raw.enroll_finger_first);
                    }
                }else if(fperollTime ==2){
                    if(!isSecondPutFinger){
                        isFirstPutFinger =false;
                        isSecondPutFinger =true;
                        streamId = SoundPlayUtil.getInstance().play(R.raw.enroll_finger_second);
                    }
                }
                isfpon = false;
                sendMsg(temp);
                objHandler_fp.postDelayed(fperollTasks, 10);
            } else if (nRet == a6.PS_GET_IMG_ERR) {
                temp = "图像获取中";
                Log.d(TAG, temp + ": " + nRet);
                objHandler_fp.postDelayed(fperollTasks, 10);
                sendMsg(temp);
                return;
            } else if (nRet == -2) {
                testcount++;
                if (testcount < 3) {
                    temp = "正在读取指纹中   剩余时间:" + ((30000 - (ssend - ssart))) / 1000 + "s";
//                    Log.i(TAG, "------" + temp);
                    isfpon = false;
                    sendMsg(temp);
//                    objHandler_fp.postDelayed(fperollTasks, 10);
                } else {
                    temp = "通讯异常";
                    Log.d(TAG, temp + ": " + nRet);
                    sendMsg(temp);
                    Constants.isFingerInit =false;
                    return;
                }
            } else {
                temp = "通讯异常";
                Log.d(TAG, temp + ": " + nRet);
                sendMsg(temp);
                Constants.isFingerInit =false;
                return;
            }
        }
    };

    /**
     * 删除指定指纹数据
     *
     * @param startPageID：    需删除指纹区域的起始 ID 号；
     * @param   //pageNum：需删除的从起始 ID 开始的模板个数
     */
    public boolean deleteChar(int startPageID) {
        int ret = a6.ZAZDelChar(DEV_ADDR, startPageID, 1);
        if (ret == a6.PS_OK) {
            Log.i(TAG, "deleteChar delete_success : ");
            return true;
        } else {
            Log.i(TAG, "deleteChar delete_failure : ");
            return false;
        }
    }

    private int getrwusbdevices() {
        // get FileDescriptor by Android USB Host API
        UsbManager mUsbManager = (UsbManager) mContext.getSystemService(Context.USB_SERVICE);

        final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
        HashMap<String, UsbDevice> deviceList = mUsbManager.getDeviceList();
        Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
        Iterator<String> iterator = deviceList.keySet().iterator();
        while (iterator.hasNext()) {
            String next = iterator.next();
            Log.i(TAG, "getrwusbdevices next: " + next);
        }
        PendingIntent mPermissionIntent = PendingIntent.getBroadcast(mContext, 0,
                new Intent(ACTION_USB_PERMISSION), 0);
        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        BroadcastReceiver mUsbReceiver = null;
        mContext.registerReceiver(mUsbReceiver, filter);
        Log.i(TAG, "zhw 060");
        int fd = -1;
        while (deviceIterator.hasNext()) {
            UsbDevice device = deviceIterator.next();
            Log.i(TAG, device.getDeviceName() + " "
                    + Integer.toHexString(device.getVendorId()) + " "
                    + Integer.toHexString(device.getProductId()));
            if ((device.getVendorId() == 0x2109)  //VendorId ==0X2109 && ProductId ==0x7638
                    && (0x7638 == device.getProductId())) {
                Log.d(TAG, " get FileDescriptor ");
                mUsbManager.requestPermission(device, mPermissionIntent);
                if (!mUsbManager.hasPermission(device)) {
                    Log.i(TAG, "USB Permission Denied");
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                if (mUsbManager.hasPermission(device)) { //有当前USB设备权限
                    if (mUsbManager.openDevice(device) != null) {  //USB设备已开启
                        fd = mUsbManager.openDevice(device).getFileDescriptor();  //获取设备的文件描述符
                        Log.d(TAG, " get FileDescriptor fd " + fd);
                        return fd;
                    } else {
                        Log.e(TAG, "UsbManager openDevice failed");
                        mUsbManager.openDevice(device).close();
                    }
                }
                break;
            }
        }
        return 0;
    }

    private int LongDunD8800_CheckEuq() {
        Process process = null;
        DataOutputStream os = null;

        String path = "/dev/bus/usb/00*/*";
        String path1 = "/dev/bus/usb/00*/*";
        File fpath = new File(path);
        Log.d("*** LongDun D8800 ***", " check path:" + path);
        String command = "chmod 777 " + path;
        String command1 = "chmod 777 " + path1;
        Log.d("*** LongDun D8800 ***", " exec command:" + command);
        try {
            process = Runtime.getRuntime().exec("su");
            os = new DataOutputStream(process.getOutputStream());
            os.writeBytes(command + "\n");
            os.writeBytes("exit\n");
            os.flush();
            process.waitFor();
            return 1;
        } catch (Exception e) {
            Log.d("*** DEBUG ***", "Unexpected error - Here is what I know: " + e.getMessage());
        }
        return 0;
    }

}
