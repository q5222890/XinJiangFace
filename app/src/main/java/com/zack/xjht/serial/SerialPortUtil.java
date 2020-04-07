package com.zack.xjht.serial;

import android.util.Log;


import com.zack.xjht.Constants;
import com.zack.xjht.Utils.SharedUtils;
import com.zack.xjht.Utils.TransformUtil;
import com.zack.xjht.Utils.Utils;
import com.zack.xjht.db.DBManager;
import com.zack.xjht.db.gen.GunStateBeanDao;
import com.zack.xjht.entity.GunStateBean;
import com.zack.xjht.event.BulletNumEvent;
import com.zack.xjht.event.EventConsts;
import com.zack.xjht.event.PowerStatusEvent;
import com.zack.xjht.event.StatusEvent;
import com.zack.xjht.humiture.OnHumitureListener;

import org.greenrobot.eventbus.EventBus;
import org.winplus.serial.utils.SerialPort;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


/**
 * 串口工具类
 */

public class SerialPortUtil {

    private String TAG = SerialPortUtil.class.getSimpleName();
    private SerialPort mSerialPort;
    private OutputStream mOutputStream;
    private InputStream mInputStream;
    private ReadThread mReadThread;
//    private String path = "/dev/ttySAC3"; //友坚
//    private String path = "/dev/ttyS4";  //新板RS485
        private String path = "/dev/ttyS0";  //TTL0
//        private String path = "/dev/ttyS1";  //TTL1
//    private String path = "/dev/ttyS4";  //瑞芯微
//    private String path = "/dev/ttyAMA3";  //九鼎三星4418
//    private String path = "/dev/ttyS3";  //旧板RS485
    private int baudrate = 115200;
    private static SerialPortUtil portUtil;
    private OnDataReceiveListener onDataReceiveListener = null;
    private boolean isStop = false;
    private GunStateBeanDao gunStateBeanDao;
    private OnHumitureListener onHumitureListener;
    private boolean isReceiveData = true;

    public void setOnHumitureValueListener(OnHumitureListener onHumitureListener) {
        this.onHumitureListener = onHumitureListener;
    }

    public interface OnDataReceiveListener {
        void onDataReceive(byte[] buffer, int size);
    }

    public void setOnDataReceiveListener(OnDataReceiveListener dataReceiveListener) {
        onDataReceiveListener = dataReceiveListener;
    }

    private SerialPortUtil() {
        onCreate();
    }

    public static SerialPortUtil getInstance() {
        if (portUtil == null) {
            synchronized (SerialPortUtil.class) {
                if (portUtil == null) {
                    portUtil = new SerialPortUtil();
                }
            }
        }
        return portUtil;
    }

    /**
     * 初始化串口信息
     */
    public void onCreate() {
        Log.i(TAG, "onCreate: ");
        synchronized (this){
            if (Constants.isOldBoard) {
                path = "/dev/ttyS3";
                baudrate =9600;
            } else {
                path = "/dev/ttyS0";
                baudrate =115200;
            }
            try {
                if (mSerialPort == null) {
                    mSerialPort = new SerialPort(new File(path), baudrate, 0);
                    mOutputStream = mSerialPort.getOutputStream();
                    mInputStream = mSerialPort.getInputStream();

                    isStop = false;
                    mReadThread = new ReadThread();
                    mReadThread.start();
                    gunStateBeanDao = DBManager.getInstance().getGunStateBeanDao();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 开枪柜/开枪锁
     *
     * @param no
     */
    public boolean openLock(String no) {
        synchronized (this) {
            Log.v(TAG, "openLock: " + no);
            byte[] data = new byte[5];
            int i = Integer.parseInt(no);
            data[0] = (byte) (i & 0xff);
            data[1] = 0x55;
            data[2] = 0x01;
            data[3] = 0x23;
            data[4] = (byte) (data[0] ^ data[1] ^ data[2] ^ data[3] ^ data[4]);
            return sendBuffer(data);
        }
    }

    /**
     * 关闭枪锁
     *
     * @param lock
     */
    public void closeLock(int lock) {
        synchronized (this) {
            Log.v(TAG, "closeLock: " + lock);
            byte[] data = new byte[5];
            data[0] = (byte) (lock & 0xff);
            data[1] = 0x55;
            data[2] = 0x01;
            data[3] = 0x25;
            data[4] = (byte) (data[0] ^ data[1] ^ data[2] ^ data[3] ^ data[4]);
            sendBuffer(data);
        }
    }

    /**
     * 开枪柜/开枪锁
     *
     * @param no
     */
    public boolean openLock(int no) {
        synchronized (this) {
            Log.v(TAG, "openLock: " + no);
            byte[] data = new byte[5];
            data[0] = (byte) (no & 0xff);
            data[1] = 0x55;
            data[2] = 0x01;
            data[3] = 0x23;
            data[4] = (byte) (data[0] ^ data[1] ^ data[2] ^ data[3] ^ data[4]);
            return sendBuffer(data);
        }
    }

    /**
     * 开数码管
     */
    public boolean openLED() {
        synchronized (this) {
            Log.v(TAG, "openLED: ");
            byte[] data = new byte[6];
            data[0] = 0x01;
            data[1] = 0x55;
            data[2] = 0x01;
            data[3] = 0x26;
            data[4] = 0x01;
            data[5] = (byte) (data[0] ^ data[1] ^ data[2] ^ data[3] ^ data[4]);
            return sendBuffer(data);
        }
    }

    /**
     * 关数码管
     */
    public boolean closeLED() {
        synchronized (this) {
            Log.v(TAG, "closeLED: ");
            byte[] data = new byte[6];
            data[0] = 0x01;
            data[1] = 0x55;
            data[2] = 0x01;
            data[3] = 0x27;
            data[4] = 0x01;
            data[5] = (byte) (data[0] ^ data[1] ^ data[2] ^ data[3] ^ data[4]);
            return sendBuffer(data);
        }
    }

    /**
     * 开数码管
     */
    public boolean openLED(int addr) {
        synchronized (this) {
            Log.v(TAG, "openLED: ");
            byte[] data = new byte[6];
            data[0] = (byte) (addr & 0xff);
            data[1] = 0x55;
            data[2] = 0x01;
            data[3] = 0x26;
            data[4] = 0x01;
            data[5] = (byte) (data[0] ^ data[1] ^ data[2] ^ data[3] ^ data[4]);
            return sendBuffer(data);
        }
    }

    /**
     * 关数码管
     */
    public boolean closeLED(int addr) {
        synchronized (this) {
            Log.v(TAG, "closeLED: ");
            byte[] data = new byte[6];
            data[0] = (byte) (addr & 0xff);
            data[1] = 0x55;
            data[2] = 0x01;
            data[3] = 0x27;
            data[4] = 0x01;
            data[5] = (byte) (data[0] ^ data[1] ^ data[2] ^ data[3] ^ data[4]);
            return sendBuffer(data);
        }
    }

    //超级指令打开枪锁
    public boolean superCmdOpenLock() {
        synchronized (this) {
            Log.v(TAG, "superComandOpenLock: ");
            byte[] superCmd = {0x01, (byte) 0x88, 0x00, 0x23, (byte) 0xaa};
            return sendBuffer(superCmd);
        }
    }

    /**
     * 设置新的锁地址
     *
     * @param address
     */
    public boolean setAddress(String address) {
        synchronized (this) {
            Log.v(TAG, "setAddress: " + address);
            byte[] data = new byte[6];
            data[0] = 0x01;
            data[1] = (byte) 0x55;
            data[2] = 0x01;
            data[3] = 0x24;
            int i = Integer.parseInt(address);
            data[4] = (byte) (i & 0xff); //锁编号
            data[5] = (byte) (data[0] ^ data[1] ^ data[2] ^ data[3] ^ data[4]);
            return sendBuffer(data);
        }
    }

    /**
     * 查询枪锁状态
     *
     * @param no
     */
    public void checkStatus(int no) {
        synchronized (this) {
            if (SharedUtils.getIsCheckStatus()) {
                byte[] data = new byte[5];
                data[0] = (byte) (no & 0xff);
                data[1] = 0x55;
                data[2] = 0x01;
                data[3] = 0x22;
                data[4] = (byte) (data[0] ^ data[1] ^ data[2] ^ data[3] ^ data[4]);

                Log.v(TAG, "checkStatus: " + TransformUtil.BinaryToHexString(data));
                boolean result = sendBuffer(data);
                Log.i(TAG, "checkStatus result: " + result);
            }
        }
    }

    /**
     * 打开继电器
     */
    public void powerOn() {
        synchronized (this) {
            Log.v(TAG, "powerOn: ");
            if (SharedUtils.getIsCheckStatus()) {
                byte[] data = new byte[5];
                data[0] = (byte) 0xfe;
                data[1] = 0x55;
                data[2] = 0x00;
                data[3] = 0x29;
                data[4] = (byte) (data[0] ^ data[1] ^ data[2] ^ data[3] ^ data[4]);
                sendBuffer(data);
            }
        }
    }

    /**
     * 关闭继电器
     */
    public void powerOff() {
        synchronized (this) {
            Log.v(TAG, "powerOff: ");
            if (SharedUtils.getIsCheckStatus()) {
                byte[] data = new byte[5];
                data[0] = (byte) 0xfe;
                data[1] = 0x55;
                data[2] = 0x00;
                data[3] = 0x28;
                data[4] = (byte) (data[0] ^ data[1] ^ data[2] ^ data[3] ^ data[4]);
                sendBuffer(data);
            }
        }
    }

    /**
     * 查询锁在不在
     *
     * @param no
     */
    public void isLockExist(String no) {
        synchronized (this) {
            Log.v(TAG, "isLockExist: " + no);
            byte[] data = new byte[5];
            data[0] = (byte) (Integer.parseInt(no) & 0xff);
            data[1] = 0x55;
            data[2] = 0x01;
            data[3] = 0x21;
            data[4] = (byte) (data[0] ^ data[1] ^ data[2] ^ data[3] ^ data[4]);
            sendBuffer(data);
        }
    }

    /**
     * 查询温湿度
     */
    public void checkHumiture() {
        synchronized (this) {
//        Log.i(TAG, "查询温湿度: ");
            byte[] data = {0x01, 0x04, 0x00, 0x01, 0x00, 0x02, 0x20, 0x0b};
            sendBuffer(data);
        }
    }

    /**
     * 打开声光报警器
     */
    public void openAlertor(int no) {
        synchronized (this) {
            Log.i(TAG, "打开声光报警器: ");
            final byte[] data = {0x00, 0x55, 0x01, 0x30, 0x00};
            data[0] = (byte) (no & 0xff);
            data[4] = (byte) (data[0] ^ data[1] ^ data[2] ^ data[3]);
            for (int i = 0; i < 3; i++) {
                sendBuffer(data);
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 关闭声光报警器
     */
    public void closeAlertor(int no) {
        synchronized (this) {
            Log.i(TAG, "关闭声光报警器: ");
            byte[] data = {0x00, 0x55, 0x01, 0x31, 0x00};
            data[0] = (byte) (no & 0xff);
            data[4] = (byte) (data[0] ^ data[1] ^ data[2] ^ data[3]);
            for (int i = 0; i < 3; i++) {
                sendBuffer(data);
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 发送指令到串口
     */
    public boolean sendCmds(String cmd) {
        synchronized (this) {
            Log.v(TAG, "发送命令" + cmd);
            boolean result = true;
            byte[] mBuffer = TransformUtil.hex2bytes(cmd);
            try {
                if (mOutputStream != null) {
                    mOutputStream.write(mBuffer);
                } else {
                    result = false;
                }
            } catch (IOException e) {
                e.printStackTrace();
                result = false;
            }
            return result;
        }
    }

    private int interval = 100;
    private long time;

    public boolean sendBuffer(byte[] mBuffer) {
        synchronized (this) {
//        Log.i(TAG, "sendBuffer hex: " + TransformUtil.BinaryToHexString(mBuffer));
            if (mSerialPort == null) {
                Log.e(TAG, "mSerialPort is null");
                onCreate();
            }
            boolean result = true;
            try {
                if (mOutputStream != null) {
                    if (isReceiveData) {
                        isReceiveData = false;
                        mOutputStream.write(mBuffer);
                    } else {
                        if ((System.currentTimeMillis() - time) >= interval) {
                            time = System.currentTimeMillis();
                            isReceiveData = false;
                            mOutputStream.write(mBuffer);
                        }
                    }
//                if (isReceiveData) {
//                    isReceiveData = false;
//                }
                } else {
                    result = false;
                }
            } catch (IOException e) {
                e.printStackTrace();
                result = false;
            }
            return result;
        }
    }

    /**
     * 合并byte数组
     */
    public static byte[] unitByteArray(byte[] byte1, byte[] byte2) {
        byte[] unitByte = new byte[byte1.length + byte2.length];
        System.arraycopy(byte1, 0, unitByte, 0, byte1.length);
        System.arraycopy(byte2, 0, unitByte, byte1.length, byte2.length);
        return unitByte;
    }

    //设置子弹重量
    public boolean setBulletWeight(String no) {
        synchronized (this) {
            Log.v(TAG, "setBulletWeight: " + no);
            byte[] setWeight = {0x00, 0x55, 0x01, 0x2b, 0x00};
            setWeight[0] = (byte) (Integer.parseInt(no) & 0xff);
            setWeight[4] = (byte) (setWeight[0] ^ setWeight[1] ^ setWeight[2] ^ setWeight[3]);
            return sendBuffer(setWeight);
        }
    }

    //读取子弹重量
    public boolean readBulletWeight(String no) {
        synchronized (this) {
            Log.v(TAG, "readBulletWeight: " + no);
            byte[] readWeight = {0x00, 0x55, 0x01, 0x2d, 0x00};
            readWeight[0] = (byte) (Integer.parseInt(no) & 0xff);
            readWeight[4] = (byte) (readWeight[0] ^ readWeight[1] ^ readWeight[2] ^ readWeight[3]);
            return sendBuffer(readWeight);
        }
    }

    //读取子弹个数
    public boolean readBulletCount(String no) {
        synchronized (this) {
            Log.v(TAG, "readBulletCount: " + no);
            byte[] readCount = {0x00, 0x55, 0x01, 0x2e, 0x00};
            readCount[0] = (byte) (Integer.parseInt(no) & 0xff);
            readCount[4] = (byte) (readCount[0] ^ readCount[1] ^ readCount[2] ^ readCount[3]);
            String s = TransformUtil.BinaryToHexString(readCount);
            Log.v(TAG, "readBulletCount 获取子弹数量命令: " + s);
            return sendBuffer(readCount);
        }
    }

    //设置皮重
    public boolean setBulletTare(String no) {
        synchronized (this) {
            Log.v(TAG, "setBulletTare: " + no);
            byte[] setTare = {0x00, 0x55, 0x01, 0x27, 0x00};
            setTare[0] = (byte) (Integer.parseInt(no) & 0xff);
            setTare[4] = (byte) (setTare[0] ^ setTare[1] ^ setTare[2] ^ setTare[3]);
            return sendBuffer(setTare);
        }
    }

    //设置皮重(开门时重新计算)
    public boolean setTare(int no) {
        synchronized (this) {
            Log.v(TAG, "setBulletTare: " + no);
            byte[] setTare = {0x00, 0x55, 0x01, 0x28, 0x00};
            setTare[0] = (byte) (no & 0xff);
            setTare[4] = (byte) (setTare[0] ^ setTare[1] ^ setTare[2] ^ setTare[3]);
            return sendBuffer(setTare);
        }
    }

    //保存皮重(闭门时保存)
    public boolean saveTare(int no) {
        synchronized (this) {
            Log.v(TAG, "setBulletTare: " + no);
            byte[] setTare = {0x00, 0x55, 0x01, 0x29, 0x00};
            setTare[0] = (byte) (no & 0xff);
            setTare[4] = (byte) (setTare[0] ^ setTare[1] ^ setTare[2] ^ setTare[3]);
            return sendBuffer(setTare);
        }
    }

    //设置子弹重量
    public boolean setBulletWeight(int no, int num) {
        synchronized (this) {
            Log.v(TAG, "setBulletWeight: " + no);
            byte[] setWeight = {0x00, 0x55, 0x01, 0x2b, 0x00, 0x00};
            setWeight[0] = (byte) (no & 0xff);
            setWeight[4] = (byte) (num & 0xff);
            setWeight[5] = (byte) (setWeight[0] ^ setWeight[1] ^ setWeight[2] ^ setWeight[3] ^ setWeight[4]);
            return sendBuffer(setWeight);
        }
    }

    //校准AD读数与g之间换算系数
    public boolean changeAD(int no, int weight) {
        synchronized (this) {
            Log.v(TAG, "setBulletTare: " + no);
            byte[] setTare = {0x00, 0x55, 0x02, 0x2F, (byte) (weight & 0xff), 0x00};
            setTare[0] = (byte) (no & 0xff);
            setTare[5] = (byte) (setTare[0] ^ setTare[1] ^ setTare[2] ^ setTare[3] ^ setTare[4]);
            return sendBuffer(setTare);
        }
    }

    //微调单个子弹重量(电子秤AD读数基数，每次调整0.2%左右)
    //1/0/FF 微调方向，1=+，0=只读取，FF=- (下面微调指令相同）
    public boolean adjustWeight(int no, byte adjust) {
        synchronized (this) {
            Log.v(TAG, "setBulletTare: " + no);
            byte[] setTare = {0x00, 0x55, 0x02, 0x31, adjust, 0x00};
            setTare[0] = (byte) (no & 0xff);
            setTare[5] = (byte) (setTare[0] ^ setTare[1] ^ setTare[2] ^ setTare[3] ^ setTare[4]);
            return sendBuffer(setTare);
        }
    }

    //微调皮重(电子秤AD读数技术,每次调整0.2%左右)
    public boolean adjustTare(int no, byte adjust) {
        synchronized (this) {
            Log.v(TAG, "setBulletTare: " + no);
            byte[] setTare = {0x00, 0x55, 0x02, 0x32, adjust, 0x00};
            setTare[0] = (byte) (no & 0xff);
            setTare[5] = (byte) (setTare[0] ^ setTare[1] ^ setTare[2] ^ setTare[3] ^ setTare[4]);
            return sendBuffer(setTare);
        }
    }

    //微调AD读数与g之间换算系数，每次调整1个读数
    public boolean adjustAD(int no, byte adjust) {
        synchronized (this) {
            Log.v(TAG, "setBulletTare: " + no);
            byte[] setTare = {0x00, 0x55, 0x02, 0x33, adjust, 0x00};
            setTare[0] = (byte) (no & 0xff);
            setTare[5] = (byte) (setTare[0] ^ setTare[1] ^ setTare[2] ^ setTare[3] ^ setTare[4]);
            return sendBuffer(setTare);
        }
    }

    private class ReadThread extends Thread {
        @Override
        public void run() {
            super.run();
            Log.v(TAG, "run ReadThread: ");
            while (!isStop && !isInterrupted()) {
                int size;
                try {
                    if (mInputStream == null) {
                        return;
                    }
                    Thread.sleep(100);
                    int nCount = mInputStream.available();
                    if (nCount == 0) {
//                        Log.i(TAG, "run nCount == 0: ");
                        continue;
                    }
//                    Log.i(TAG, "run  nCount: " + nCount);
                    byte[] buffer = new byte[nCount];
                    size = mInputStream.read(buffer);
//                    Log.i(TAG, "run size: " + size);
                    if (size > 0) {
                        isReceiveData = true;
//                        if (null != onDataReceiveListener) {
////                            Log.i(TAG, "run onDataReceiveListener is not null: ");
//                            onDataReceiveListener.onDataReceive(buffer, size);
//                        }
                        //格式化温湿度值
                        if (buffer.length == 9 && buffer[0] == 0x01 && buffer[1] == 0x04 && buffer[2] == 0x04) {
                            byte[] b1 = {buffer[3], buffer[4]};
                            byte[] b2 = {buffer[5], buffer[6]};
                            String s1 = TransformUtil.toHexString(b1);
                            String s2 = TransformUtil.toHexString(b2);
                            int temp = Integer.parseInt(s1, 16);
                            int humid = Integer.parseInt(s2, 16);
//                            Log.i(TAG, "run temp: "+temp);
//                            Log.i(TAG, "run humid: "+humid);

                            Log.i(TAG, "run 温度: " + (float) temp / 10 + "℃  湿度: " + (float) humid / 10 + "%");
                            if (onHumitureListener != null) {
                                onHumitureListener.onHumitureValue((float) temp / 10, (float) humid / 10);
                            }
                        }
                        String hexString = TransformUtil.BinaryToHexString(buffer);
                        Log.v(TAG, "run  buffer:  " + hexString);
                        if (Utils.isXorValue(buffer)) {
                            Log.v(TAG, "run 校验值正确: ");
                            String[] split = hexString.split(" ");
                            int position = Integer.parseInt(split[1], 16);
                            Log.v(TAG, "run  position: " + position);
                            if (split.length == 9) { //查询子弹数量
                                int thousands = Integer.parseInt(split[4], 16);//千位
                                int hundreds = Integer.parseInt(split[5], 16);//白位
                                int tens = Integer.parseInt(split[6], 16);//十位
                                int units = Integer.parseInt(split[7], 16);//个位
                                String result = thousands + "" + hundreds + "" + tens + "" + units;
                                if (split[0].equals("55") && split[2].equals("05") && split[3].equals("2E")) {
                                    Log.v(TAG, "onDataReceive : " + position + " 号弹仓 数量:" + result);
                                    EventBus.getDefault().post(new BulletNumEvent(position,
                                            Integer.parseInt(result), position + " 号弹仓 数量:" + result + "颗"));
                                } else if (split[0].equals("55") && split[2].equals("05") && split[3].equals("2D")) {
                                    Log.v(TAG, "onDataReceive : " + position + " 号弹仓 重量:" + result + "克");
                                    EventBus.getDefault().post(new BulletNumEvent(position,
                                            Integer.parseInt(result), +position + " 号弹仓 重量:" + result + "克"));
                                } else if (split[0].equals("55") && split[2].equals("05") && split[3].equals("31")) {
                                    EventBus.getDefault().post(new BulletNumEvent(position,
                                            Integer.parseInt(result), +position + " 号弹仓 调整后单个子弹重量:" + result));
                                } else if (split[0].equals("55") && split[2].equals("05") && split[3].equals("32")) {
                                    EventBus.getDefault().post(new BulletNumEvent(position,
                                            Integer.parseInt(result), +position + " 号弹仓 调整后皮重:" + result));
                                } else if (split[0].equals("55") && split[2].equals("05") && split[3].equals("33")) {
                                    EventBus.getDefault().post(new BulletNumEvent(position,
                                            Integer.parseInt(result), +position + " 号弹仓 调整后换算系数:" + result));
                                }
                            } else if (split.length == 10) { //查询电源和枪柜状态
                                //获取当前状态
                                if (split[0].equals("55") && split[2].equals("05") && split[3].equals("22")) {
                                    //根据电源地址查询电源状态
                                    int powerAddress = SharedUtils.getPowerAddress();
//                                    Log.v(TAG, "run  powerAddress: "+powerAddress);
                                    if (position == powerAddress) {
                                        //电源状态
                                        if (split[8].equals("00")) {
                                            Log.v(TAG, "run: " + position + "号市电正常");
                                            EventBus.getDefault().post(new PowerStatusEvent(EventConsts.EVENT_POWER_NORMAL));
                                        } else if (split[8].equals("01")) {
                                            Log.v(TAG, "run: " + position + "号备用电池");
                                            EventBus.getDefault().post(new PowerStatusEvent(EventConsts.EVENT_BACKUP_POWER));
                                        } else {
                                            Log.v(TAG, "run 获取数据错误: ");
                                        }
                                    }
                                    //根据枪柜门板地址查询枪柜状态
                                    if (position == Integer.parseInt(SharedUtils.getLeftCabNo())) {
                                        //钥匙开启状态
                                        if (split[4].equals("00")) {
                                            Log.v(TAG, "run: " + position + "号钥匙开启");
                                            EventBus.getDefault().post(new PowerStatusEvent(EventConsts.EVENT_BACKUP_OPEN));
                                        } else if (split[4].equals("01")) {
                                            Log.v(TAG, "run: " + position + "号钥匙未开启");
                                            EventBus.getDefault().post(new PowerStatusEvent(EventConsts.EVENT_BACKUP_CLOSE));
                                        } else {
                                            Log.v(TAG, "run 获取数据错误: ");
                                        }
                                        //开关门状态
                                        if (split[5].equals("01")) {
                                            Log.v(TAG, "run: " + position + "号门开启");
                                            EventBus.getDefault().post(new PowerStatusEvent(EventConsts.EVENT_CAB_OPEN));
                                        } else if (split[5].equals("00")) {
                                            Log.v(TAG, "run: " + position + "号门关闭");
                                            EventBus.getDefault().post(new PowerStatusEvent(EventConsts.EVENT_CAB_CLOSE));
                                        } else {
                                            Log.v(TAG, "run 获取数据错误: ");
                                        }
                                        //震动传感器状态
                                        if (split[7].equals("01")) {
                                            Log.v(TAG, "run: " + position + "号震动传感器正常");
                                            EventBus.getDefault().post(new PowerStatusEvent(EventConsts.EVENT_VIBRATION_NORMAL));
                                        } else if (split[7].equals("00")) {
                                            Log.v(TAG, "run: " + position + "号震动传感器异常");
                                            EventBus.getDefault().post(new PowerStatusEvent(EventConsts.EVENT_VIBRATION_ABNORMAL));
                                        } else {
                                            Log.v(TAG, "run 获取数据错误: ");
                                        }
                                    }
                                }
                            } else if (split.length == 8) {  //枪锁状态
                                if (split[0].equals("55") && split[2].equals("03") && split[3].equals("22")) {
                                    //获取枪锁开关和在位状态
                                    if (split[4].equals("00")) {
                                        //枪锁打开
                                        Log.v(TAG, "run() called  " + position + "号枪锁打开");
                                        setLockState(split, position, 0);
                                        EventBus.getDefault().post(new StatusEvent(position,
                                                1, 0, position + "号枪锁打开"));
                                    } else if (split[5].equals("00")) {
                                        //枪锁关闭
                                        Log.v(TAG, "run() called  " + position + "号枪锁关闭");
                                        setLockState(split, position, 1);
                                        EventBus.getDefault().post(new StatusEvent(position,
                                                1, 1, position + "号枪锁关闭"));
                                    } else if (split[4].equals("01") && split[5].equals("01")) {
                                        //枪锁异常
                                        Log.v(TAG, "run() called  " + position + "号枪锁异常");
                                        setLockState(split, position, 2);
                                        EventBus.getDefault().post(new StatusEvent(position,
                                                1, 2, position + "号枪锁异常"));
                                    }

                                    if (split[6].equals("00")) {
                                        //枪支在位
                                        Log.v(TAG, "run() called  " + position + "号枪支离位");
                                        int gunState = 0;
                                        setGunState(split, position, gunState);
                                        EventBus.getDefault().post(new StatusEvent(position,
                                                2, 0, position + "号枪支离位"));
                                    } else if (split[6].equals("01")) {
                                        //枪支离位
                                        Log.v(TAG, "run() called  " + position + "号枪支在位");
                                        int gunState = 1;
                                        setGunState(split, position, gunState);
                                        EventBus.getDefault().post(new StatusEvent(position,
                                                2, 1, position + "号枪支在位"));
                                    }
                                }
                            } else if (split.length == 7) {  //子弹抽屉状态
                                if (split[0].equals("55") && split[2].equals("03") && split[3].equals("22")) {
                                    //获取枪锁开关和在位状态
                                    if (split[4].equals("41")) {
                                        //枪锁打开
                                        Log.v(TAG, "run() called  " + position + "号弹仓打开");
                                        setLockState(split, position, 0);
                                        EventBus.getDefault().post(new StatusEvent(position,
                                                1, 41, position + "号弹仓打开"));
                                    } else if (split[4].equals("61")) {
                                        //枪锁关闭
                                        Log.v(TAG, "run() called  " + position + "号弹仓关闭");
                                        setLockState(split, position, 1);
                                        EventBus.getDefault().post(new StatusEvent(position,
                                                1, 61, position + "号弹仓关闭"));
                                    }
                                }
                            }
                        } else {
                            Log.v(TAG, "run 校验值错误: ");
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }
            }
        }
    }

    private void setGunState(String[] split, int position, int gunState) {
        GunStateBean gunStateBean = gunStateBeanDao.queryBuilder().where(
                GunStateBeanDao.Properties.Position.eq(position)).unique();
        if (gunStateBean != null) {
            gunStateBean.setGunState(gunState);
        } else {
            gunStateBean = new GunStateBean();
            gunStateBean.setId(Long.parseLong(split[1], 16));
            gunStateBean.setPosition(position);
            gunStateBean.setGunState(gunState);
        }
        gunStateBeanDao.update(gunStateBean);
    }

    private void setLockState(String[] split, int position, int lockState) {
        GunStateBean gunStateBean = gunStateBeanDao.queryBuilder().where(
                GunStateBeanDao.Properties.Position.eq(position)).unique();
        if (gunStateBean != null) {
            gunStateBean.setLockState(0);
        } else {
            gunStateBean = new GunStateBean();
            gunStateBean.setId(Long.parseLong(split[1], 16));
            gunStateBean.setPosition(position);
            gunStateBean.setLockState(lockState);
        }
        gunStateBeanDao.update(gunStateBean);
    }


    /**
     * 关闭串口
     */
    public void close() {
        try {
            isStop = true;
            if (this.mReadThread != null && !this.mReadThread.isInterrupted()) {
                this.mReadThread.interrupt();
            }
            if(mInputStream !=null){
                mInputStream.close();
                mInputStream =null;
            }
            if(mOutputStream !=null){
                mOutputStream.close();
                mOutputStream =null;
            }
            if (mSerialPort != null) {
                mSerialPort.close();
                mSerialPort = null;
            }
            if (onDataReceiveListener != null) {
                onDataReceiveListener = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
