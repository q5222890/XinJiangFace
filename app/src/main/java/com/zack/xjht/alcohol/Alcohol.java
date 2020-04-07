package com.zack.xjht.alcohol;

import android.util.Log;

import com.zack.xjht.db.DBManager;
import com.zack.xjht.serial.SerialPortUtil;

import org.winplus.serial.utils.SerialPort;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class Alcohol {
    private static final String TAG = "Alcohol";

    private SerialPort mSerialPort;
    private OutputStream mOutputStream;
    private InputStream mInputStream;
    private ReadThread mReadThread;
    private String path = "/dev/ttyS1";  //TTL
    private int baudrate = 9600;
    private boolean isStop = false;
    private OnAlcoholValueListener onAlcoholValueListener;
    private static Alcohol alcohol;

    public void setOnAlcoholValueListener(OnAlcoholValueListener onAlcoholValueListener) {
        this.onAlcoholValueListener = onAlcoholValueListener;
    }
    private Alcohol() {
        init();
    }

    public void init() {
        try {
            if (mSerialPort == null) {
                mSerialPort = new SerialPort(new File(path), baudrate, 0);
                mOutputStream = mSerialPort.getOutputStream();
                mInputStream = mSerialPort.getInputStream();

                isStop = false;
                mReadThread = new ReadThread();
                mReadThread.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 查询酒精
     * @return
     */
    public void checkAlcohol() {
        Log.i(TAG, "checkAlcohol: ");
        if (mSerialPort == null) {
            Log.e(TAG, "mSerialPort is null");
            init();
        }
        try {
            if (mOutputStream != null) {
                DataOutputStream dos =new DataOutputStream(mOutputStream);
                dos.write("AT+V".getBytes());
                dos.write('\r');
                dos.write('\n');
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Alcohol getInstance() {
        if (null == alcohol) {
            alcohol = new Alcohol();
        }
        return alcohol;
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
                        continue;
                    }
                    byte[] buffer = new byte[nCount];
                    size = mInputStream.read(buffer);
                    if (size > 0) {
                        String alcoholResult = new String(buffer, 0 , size);
                        if(alcoholResult.contains("=")){
                            String substring = alcoholResult.substring(alcoholResult.indexOf("=")+1,
                                    alcoholResult.indexOf("\r"));
                            onAlcoholValueListener.onAlcoholValue(substring);
                            Log.v(TAG, "run  alcoholResult 电压值:  " + substring+"V");
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }
            }
        }
    }

    public void close(){
        try {
            isStop = true;
            if (this.mReadThread != null && !this.mReadThread.isInterrupted()) {
                this.mReadThread.interrupt();
            }
            if (mSerialPort != null) {
                mSerialPort.close();
                mSerialPort = null;
            }
            if (onAlcoholValueListener != null) {
                onAlcoholValueListener = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
