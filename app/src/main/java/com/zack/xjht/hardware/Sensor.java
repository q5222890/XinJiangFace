package com.zack.xjht.hardware;

import android.util.Log;


import com.zack.xjht.Utils.TransformUtil;
import com.zack.xjht.humiture.OnHumitureListener;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * 传感器
 */

public class Sensor {

    private static Sensor instance;

    private Sensor() {
    }

    public static Sensor getInstance() {
        if (instance == null) {
            instance = new Sensor();
        }
        return instance;
    }

    /**
     * gpio开关
     *
     * @param direction
     */
    public void switchGpio(int direction) {
        String cmd = "echo " + direction + " > /sys/bus/platform/devices/x4418-rs485/state\n";
        OutputStream os = null;
        try {
            Process process = Runtime.getRuntime().exec("sh");
            os = process.getOutputStream();
            os.write(cmd.getBytes());
            Log.i("switchGpio", "write: " + cmd);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                os.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 报警器开关
     *
     * @param code 1.打开警笛 2.关闭警笛
     */
    public void alarmSwitch(int code) {
        String cmd = "echo " + code + " > /proc/alarm_lamp\n";
        OutputStream os = null;
        try {
            Process process = Runtime.getRuntime().exec("sh");
            os = process.getOutputStream();
            os.write(cmd.getBytes());
            Log.e("alarmSwitch", "write: " + cmd);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                os.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 打开/关闭酒精传感器 /proc/mq3
     * 写入 1 打开传感器
     * 写入 0 关闭传感器
     *
     * @param code
     */
    public void switchEthanol(int code) {
        String cmd = "echo " + code + " > /proc/mq3\n";
        OutputStream os = null;
        try {
            Process process = Runtime.getRuntime().exec("sh");
            os = process.getOutputStream();
            os.write(cmd.getBytes());
            Log.e("switchEthanol", "write: " + cmd);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                os.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 打开/关闭门锁 /proc/door_lock
     * 写入 1 打开
     * 写入 0 关闭
     *
     * @param code
     */
    public void switchDoorLock(int code) {
        String cmd = "echo " + code + " > /proc/door_lock\n";
        OutputStream os = null;
        try {
            Process process = Runtime.getRuntime().exec("sh");
            os = process.getOutputStream();
            os.write(cmd.getBytes());
            Log.e("switchEthanol", "write: " + cmd);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 读取酒精传感器的值
     *
     * @return
     */
    public byte[] readEthanolValue() {
        String path = "/proc/mq3";//得到文件路径
        FileInputStream fis = null;
        ByteArrayOutputStream baos = null;
        byte[] data = new byte[0];
        BufferedReader reader;
        String prop;
        try {
            File file = new File(path);
            fis = new FileInputStream(file);
            byte[] buffer = new byte[2];
            int len = 0;
            baos = new ByteArrayOutputStream();
            if ((len = fis.read(buffer)) != -1) {
                baos.write(buffer, 0, len);
            }
            data = baos.toByteArray();
//            Log.i(TAG, "readEthanolValue data[0]: "+String.valueOf(data[0])
//                    +" data[1]:"+String.valueOf(data[1]));

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (baos != null && fis != null) {
                    baos.close();
                    fis.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return data;
    }

    public byte[] read(String path){
        Process sh = null;
        try {
            sh = Runtime.getRuntime().exec(new String[]{"chmod", "666", "/data/misc/eth_rand_mac"});
            OutputStream os = sh.getOutputStream();
            os.write(("chmod 666 /data/misc/eth_rand_mac").getBytes("ASCII"));
            sh.waitFor();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        FileInputStream fis = null;
        ByteArrayOutputStream baos = null;
        byte[] data = new byte[0];
        try {
            File file = new File(path);
            fis = new FileInputStream(file);
            byte[] buffer = new byte[2];
            int len = 0;
            baos = new ByteArrayOutputStream();
            while ((len = fis.read(buffer)) != -1) {
                baos.write(buffer, 0, len);
            }
            data = baos.toByteArray();
//            Log.i(TAG, "readEthanolValue data[0]: "+String.valueOf(data[0])
//                    +" data[1]:"+String.valueOf(data[1]));

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (baos != null && fis != null) {
                    baos.close();
                    fis.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return data;
    }

    public void write(String path, String data) {
        Process sh = null;
        try {
            sh = Runtime.getRuntime().exec(new String[]{"chmod", "666", "/data/misc/eth_rand_mac"});
            OutputStream os = sh.getOutputStream();

            os.write(("chmod 666 /data/misc/eth_rand_mac").getBytes("ASCII"));
            sh.waitFor();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        FileOutputStream fos = null;

        File file =new File(path);
        if(!file.exists()){
            return;
        }
        try {
            fos = new FileOutputStream(file);
            byte[] bytes = TransformUtil.hex2bytes(data);
            fos.write(bytes);
            fos.flush();
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            if(fos !=null){
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 读取温湿度值
     *
     * @return
     */
    public String readHumitureValue() {
        String path = "/proc/dht11";
        FileInputStream fis = null;
        ByteArrayOutputStream baos = null;
        byte[] data = new byte[8];
        StringBuilder sb = new StringBuilder();
        try {
            File file = new File(path);
            fis = new FileInputStream(file);
            fis.read(data);

            for (int i = 0; i < data.length; i++) {
                if (i % 2 == 0) {
                    sb.append((char) data[i]);
                } else {
                    sb.append((char) data[i] + " ");
                }
            }
            return sb.toString();
        } catch (Exception e) {
//            Log.w("readHumitureValue", " ***ERROR*** " + e.getMessage());
        } finally {
            try {
                if (baos != null && fis != null) {
                    baos.close();
                    fis.close();
                }
            } catch (IOException e) {
                Log.w("readHumitureValue", " error: " + e.getMessage());
            }
        }
//        Log.e("readFile", "readFile cmd from :" + path + " data:" + Arrays.toString(data));
        return sb.toString();
    }


}
