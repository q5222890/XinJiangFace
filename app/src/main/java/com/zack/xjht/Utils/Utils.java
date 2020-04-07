package com.zack.xjht.Utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.text.format.DateFormat;
import android.util.Base64;
import android.util.Log;

import com.zack.xjht.App;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

/**
 * 工具类
 */

public class Utils {

    public static String genUUID(){
        return UUID.randomUUID().toString().replaceAll("-","");
    }

    /**
     * 将Unicode格式转换成可
     *
     * @param s 需要格式转换的字符串
     * @return
     */
    public static String unicode2String(String s) {
        String re = "", sub = null;
        char c1, c2;
        for (int i = 0; i < s.length() - 1; i++) {
            c1 = s.charAt(i);
            c2 = s.charAt(i + 1);
            if (c1 == '\\' && c2 == 'u') {
                sub = s.substring(i + 2, i + 6);
                re = re + (char) Integer.parseInt(sub, 16);
                i += 5;
            } else {
                re = re + c1;
            }
        }
        return re;
    }

    /**
     * 将文件读取为16进制String
     * Read original File and transfer it into Hex String
     *
     * @return
     * @throws IOException
     */
    public static String readFile2Hex(FileInputStream fis)
            throws IOException {
        StringWriter sw = new StringWriter();

        int len = 1;
        byte[] temp = new byte[len];

        /*16进制转化模块*/
        for (; (fis.read(temp, 0, len)) != -1; ) {
            if (temp[0] > 0xf && temp[0] <= 0xff) {
                sw.write(Integer.toHexString(temp[0]));
            } else if (temp[0] >= 0x0 && temp[0] <= 0xf) {//对于只有1位的16进制数前边补“0”
                sw.write("0" + Integer.toHexString(temp[0]));
            } else { //对于int<0的位转化为16进制的特殊处理，因为Java没有Unsigned int，所以这个int可能为负数
                sw.write(Integer.toHexString(temp[0]).substring(6));
            }
        }
        return sw.toString();
    }

    /**
     * byte转short
     */

    //将字节数组转换为short类型，即统计字符串长度
    public static short bytes2Short(byte[] b) {
        short i = (short) (((b[1] & 0xff) << 8) | b[0] & 0xff);
        return i;
    }

    /**
     * 获取ip地址
     *
     * @return
     */
    public static String getHostIP() {
        String hostIp = null;
        try {
            Enumeration nis = NetworkInterface.getNetworkInterfaces();
            InetAddress ia = null;
            while (nis.hasMoreElements()) {
                NetworkInterface ni = (NetworkInterface) nis.nextElement();
                Enumeration<InetAddress> ias = ni.getInetAddresses();
                while (ias.hasMoreElements()) {
                    ia = ias.nextElement();
                    if (ia instanceof Inet6Address) {
                        continue;// skip ipv6
                    }
                    String ip = ia.getHostAddress();
                    if (!"127.0.0.1".equals(ip)) {
                        hostIp = ia.getHostAddress();
                        break;
                    }
                }
            }
        } catch (SocketException e) {
            Log.i("Utils", "SocketException");
            e.printStackTrace();
        }
        return hostIp;
    }

    /**
     * 网络是否可用
     *
     * @return
     */
    public static boolean isNetworkAvailable() {
        ConnectivityManager connectivity = (ConnectivityManager) App.getInstance()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo info = connectivity.getActiveNetworkInfo();
            if (info != null && info.isConnected()) {
                // 当前网络是连接的
                if (info.getState() == NetworkInfo.State.CONNECTED) {
                    // 当前所连接的网络可用
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * ping IP地址
     *
     * @param str
     * @return
     */
    public static void ping(final Activity activity, final String str) {
//        String result = "";
        if (!isNetworkAvailable()) {
            ToastUtil.showShort("网络连接断开");
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                Process p;
                try {
                    //ping -c 3 -w 100  中  ，-c 是指ping的次数 3是指ping 3次 ，-w 100  以秒为单位指定超时间隔，是指超时时间为100秒
                    p = Runtime.getRuntime().exec("ping -c 3 -w 100 " + str);
                    int status = p.waitFor();

                    Log.i("-------------", "ping status: " + status);
                    InputStream input = p.getInputStream();
                    BufferedReader in = new BufferedReader(new InputStreamReader(input));
                    StringBuffer buffer = new StringBuffer();
                    String line = "";
                    while ((line = in.readLine()) != null) {
                        buffer.append(line);
                    }
                    System.out.println("Return ============" + buffer.toString());

                    if (status == 0) {
                        Log.i("-------------", "ping result: success");
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ToastUtil.showShort("ping " + str + "成功");
                            }
                        });
                    } else {
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ToastUtil.showShort("ping " + str + "失败");
                            }
                        });
                        Log.i("--------------", "ping result: failed");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    /**
     * 将整型格式化为占4位的16进制字符串 再将空格替换为0
     *
     * @param in
     * @return
     */
    public static String formatHex(int in) {
        String st = String.format("%4s", in).toUpperCase();
        st = st.replaceAll(" ", "0");
        return st;
    }

    //sys_path 为节点映射到的实际路径
    public static String readPath(String sys_path) {
        try {
            Runtime runtime = Runtime.getRuntime();
            Process process = runtime.exec("cat " + sys_path); // 此处进行读操作
            InputStream is = process.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String line;
            while (null != (line = br.readLine())) {
                Log.i("read", "read: " + line);
                return line;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    //sys_path 为节点映射到的实际路径
    public static String readFile(String sys_path) {
        String prop = "waiting";// 默认值
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(sys_path));
            prop = reader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return prop;
    }

    public static void writeSysFileSend(String sys_path) {
        Log.i("writeSysFileSend", "writeSysFileSend: ");
        Process p = null;
        DataOutputStream os = null;
        try {
            p = Runtime.getRuntime().exec("sh");
            os = new DataOutputStream(p.getOutputStream());
            os.writeBytes("echo 1 > " + sys_path + "\n");
            os.writeBytes("exit\n");
            os.flush();
            Log.i("writeSysFileSend", "writeSysFileSend: ");
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("Utils", " can't write " + sys_path + e.getMessage());
        } finally {
            if (p != null) {
                p.destroy();
            }
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void writeSysFileReceive(String sys_path) {
        Log.i("writeSysFileReceive", "writeSysFileReceive: ");
        Process p = null;
        DataOutputStream os = null;
        try {
            p = Runtime.getRuntime().exec("sh");
            os = new DataOutputStream(p.getOutputStream());
            os.writeBytes("echo 0 > " + sys_path + "\n");
            os.writeBytes("exit\n");
            os.flush();
            Log.i("writeSysFileReceive", "writeSysFileReceive: ");
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("Utils", " can't write " + sys_path + e.getMessage());
        } finally {
            if (p != null) {
                p.destroy();
            }
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static CharSequence formatTime(long timeMill) {
        return DateFormat.format("yyyy-MM-dd HH:mm", timeMill);
    }

    public static boolean isRootSystem() {
        String kSuSearchPaths[] = {"/system/bin/su", "/system/xbin/su",
                "/system/sbin/su", "/sbin/su", "/vendor/bin/su"};
        Process process = null;
        try {
            process = Runtime.getRuntime().exec("sh");
            process.getOutputStream().write("exit\n".getBytes());
            process.getOutputStream().flush();
            int i = process.waitFor();
            if (0 == i) {
                process = Runtime.getRuntime().exec("su");
                return true;
            }

        } catch (Exception e) {
            return false;
        } finally {
            try {
                process.getOutputStream().close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public static String getFileName() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        String date = format.format(new Date(System.currentTimeMillis()));
        return date;// 2012年10月03日 23:41:31
    }

    public static String getDateEN() {
        SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String date1 = format1.format(new Date(System.currentTimeMillis()));
        return date1;// 2012-10-03 23:41:31
    }

    /**
     * 将long型时间格式化成String型
     * @param time
     * @return
     */
    public static String longTime2String(long time) {
        CharSequence formatTime = DateFormat.format("yyyy-MM-dd HH:mm:ss", time);
        return (String) formatTime;
    }

    /**
     * 将String类型时间转换成long型
     * @param time
     * @return
     */
    public static long stringTime2Long(String time) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date();
        try {
            date = dateFormat.parse(time);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date.getTime();
    }

    /**
     * 将String转ascii表示的byte数组
     * @param id
     * @return
     */
    public static byte[] StringToAsciiByte(String id) {
        int max = id.length();
        byte[] result = new byte[max];
        for (int i = 0; i < max; i++) {
            char c = id.charAt(i);
//            String b = Integer.toHexString(c);
            byte i1 = (byte) c;
            result[i] = i1;
        }

        byte[] bytes = new byte[24];
        System.arraycopy(result, 0, bytes, bytes.length - result.length, result.length);
        return bytes;
    }

    public static byte[] mergeBytes(byte[] a, byte[] b) {
        byte[] newBytes = new byte[a.length + b.length];
        String[] dd = new String[a.length + b.length];
        System.arraycopy(a, 0, dd, 0, a.length);
        System.arraycopy(a, 0, dd, a.length, b.length);
        return newBytes;
    }

    /**
     * 获取irisID
     * @param info
     * @param len
     * @return
     */
    @NonNull
    public static String getIrisID(byte[] info, int len) {
        StringBuilder res = new StringBuilder();
        for (int i = 0; i < len; i++) {
            byte b = info[i];
            if (b != 0) {
                char c = (char) b;
                String s1 = String.valueOf(c);
//                Log.i(TAG, "recvCallback s1: "+s1);
                res.append(s1);
            }
        }
        return res.toString();
    }

    /**
     * 将多余的0去掉取大于0的byte数组
     * @param byteArray
     * @return
     */
    public static byte[] byte2byte(byte[] byteArray) {
        if (byteArray == null || byteArray.length < 1)
            throw new IllegalArgumentException("this byteArray must not be null or empty");

        List<Byte> byteList = new ArrayList<>();
        for (int i = 0; i < byteArray.length; i++) {
            if ((byteArray[i] & 0xff) == 0x00)// 为0直接去掉
                continue;
            byteList.add(byteArray[i]);
        }
        return listTobyte(byteList);
    }

    /**
     * byte list转byte数组
     * @param list
     * @return
     */
    private static byte[] listTobyte(List<Byte> list) {
        if (list == null || list.size() < 0)
            return null;
        byte[] bytes = new byte[list.size()];
        int i = 0;
        Iterator<Byte> iterator = list.iterator();
        while (iterator.hasNext()) {
            bytes[i] = iterator.next();
            i++;
        }
        return bytes;
    }

    /**
     * 获取数组中最小的值
     * @param t1
     * @param max
     * @return
     */
    public static List<Integer> compare(List<Integer> t1, int max) {
        List<Integer> maxList = new ArrayList<>();
        for (int i = 0; i < max; i++) {
            maxList.add(i + 1);
        }
        List<Integer> list2 = new ArrayList<>();
        for (int t : maxList) {
            if (!t1.contains(t)) {
                list2.add(t);
            }
        }
        return list2;
    }

    /**
     * 检查扩展名，得到图片格式的文件
     *
     * @param fName 文件名
     * @return
     */
    @SuppressLint("DefaultLocale")
    private static boolean checkIsImageFile(String fName) {
        boolean isImageFile = false;
        // 获取扩展名
        String FileEnd = fName.substring(fName.lastIndexOf(".") + 1,
                fName.length()).toLowerCase();
        if (FileEnd.equals("jpg") || FileEnd.equals("png") || FileEnd.equals("gif")
                || FileEnd.equals("jpeg") || FileEnd.equals("bmp")) {
            isImageFile = true;
        } else {
            isImageFile = false;
        }
        return isImageFile;
    }

    /**
     * Bitmap转Base64字符串
     * @param bitmap
     * @return
     */
    public static String bitmapToBase64Str(Bitmap bitmap) {
        // 要返回的字符串
        String result = null;
        ByteArrayOutputStream baos = null;
        try {
            if (bitmap != null) {
                baos = new ByteArrayOutputStream();
               //压缩只对保存有效果bitmap还是原来的大小
                bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);
                baos.flush();
                // 转换为字节数组
                byte[] byteArray = baos.toByteArray();
                // 转换为字符串
                result = Base64.encodeToString(byteArray, Base64.DEFAULT);
            } else {
                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (baos != null) {
                    baos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    /**
     * @param  base64String base64字符集
     * @return Bitmap  返回@Bitmap
     */
    public static Bitmap base64ToBitmap(String base64String) {

        byte[] decode = Base64.decode(base64String, Base64.DEFAULT);

        return BitmapFactory.decodeByteArray(decode, 0, decode.length);
    }

    /**
     * @Title: bytes2HexString
     * @Description: 字节数组转16进制字符串
     * @param b
     *            字节数组
     * @return 16进制字符串
     * @throws
     */
    public static String bytes2HexString(byte[] b) {
        StringBuffer result = new StringBuffer();
        String hex;
        for (int i = 0; i < b.length; i++) {
            hex = Integer.toHexString(b[i] & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            result.append(hex.toUpperCase());
        }
        return result.toString();
    }

    /**
     * @Title: hexString2Bytes
     * @Description: 16进制字符串转字节数组
     * @param src
     *            16进制字符串
     * @return 字节数组
     * @throws
     */
    public static byte[] hexString2Bytes(String src) {
        int l = src.length() / 2;
        byte[] ret = new byte[l];
        for (int i = 0; i < l; i++) {
            try {
                ret[i] = (byte) Integer
                        .valueOf(src.substring(i * 2, i * 2 + 2), 16).byteValue();
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        return ret;
    }

    /**
     * @Title  string2HexString
     * @Description: 字符串转16进制字符串
     * @param strPart
     *            字符串
     * @return 16进制字符串
     * @throws
     */
    public static String string2HexString(String strPart) {
        StringBuffer hexString = new StringBuffer();
        for (int i = 0; i < strPart.length(); i++) {
            int ch = (int) strPart.charAt(i);
            String strHex = Integer.toHexString(ch);
            hexString.append(strHex);
        }
        return hexString.toString();
    }

    /**
     * @Title: hexString2String
     * @Description: 16进制字符串转字符串
     * @param src
     *            16进制字符串
     * @return 字节数组
     * @throws
     */
    public static String hexString2String(String src) {
        String temp = "";
        for (int i = 0; i < src.length() / 2; i++) {
            temp = temp
                    + (char) Integer.valueOf(src.substring(i * 2, i * 2 + 2),
                    16).byteValue();
        }
        return temp;
    }

    /**
     * @Title: char2Byte
     * @Description: 字符转成字节数据char-->integer-->byte
     * @param  src
     * @return
     * @throws
     */
    public static Byte char2Byte(Character src) {
        return Integer.valueOf((int)src).byteValue();
    }

    /**
     * @Title:intToHexString
     * @Description:10进制数字转成16进制
     * @param a 转化数据
     * @param len 占用字节数
     * @return
     * @throws
     */
    private static String intToHexString(int a, int len){
        len<<=1;
        String hexString = Integer.toHexString(a);
        int b = len -hexString.length();
        if(b>0){
            for(int i=0;i<b;i++)  {
                hexString = "0" + hexString;
            }
        }
        return hexString;
    }


    /**
     *
     * 将字节数组转换为16进制字符串
     */
    public static String binaryToHexString(byte[] bytes) {
        String hexStr = "0123456789ABCDEF";
        String result = "";
        String hex = "";
        for (byte b : bytes) {
            hex = String.valueOf(hexStr.charAt((b & 0xF0) >> 4));
            hex += String.valueOf(hexStr.charAt(b & 0x0F));
            result += hex + " ";
        }
        return result;
    }

    /**
     * 判断异或校验值是否正确
     * @param buffer
     * @return
     */
    public static boolean isXorValue(byte[] buffer) {
        byte[] buf = new byte[buffer.length - 1];
        for (int i = 0; i < buffer.length - 1; i++) {
            buf[i] = buffer[i];
        }
        if (buf.length != 0) {
            byte xor = Utils.getXor(buf);
            if (xor == buffer[buffer.length - 1]) {
                return true;
            } else {
                return false;
            }
        }
        return false;
    }

    /**
     * 获取异或值
     *
     * @param datas
     * @return
     */
    public static byte getXor(byte[] datas) {

        byte temp = datas[0];

        for (int i = 1; i < datas.length; i++) {
            temp ^= datas[i];
        }
        return temp;
    }
}
