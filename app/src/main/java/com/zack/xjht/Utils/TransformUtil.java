package com.zack.xjht.Utils;


/**
 * 转换的工具类
 */
public class TransformUtil {

    //hex String转byte[]
    /**
     * 16进制表示的字符串转换为字节数组
     *
     * @param s 16进制表示的字符串
     * @return byte[] 字节数组
     */
    public static byte[] hexStrToBytes(String s) {
        int len = s.length();
        byte[] b = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            // 两位一组，表示一个字节,把这样表示的16进制字符串，还原成一个字节
            b[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character
                    .digit(s.charAt(i + 1), 16));
        }
        return b;
    }

    /**
     * 以十六进制发送指令
     *
     * @param hex
     * @return oh shit!
     */
    public static byte[] hex2bytes(String hex) {
//        String digital = "0123456789ABCDEF";
        String digital = "0123456789abcdef";
        String hex1 = hex.replace(" ", "");
        char[] hex2char = hex1.toCharArray();
        byte[] bytes = new byte[hex1.length() / 2];
        byte temp;
        for (int p = 0; p < bytes.length; p++) {
            temp = (byte) (digital.indexOf(hex2char[2 * p]) * 16);
            temp += digital.indexOf(hex2char[2 * p + 1]);
            bytes[p] = (byte) (temp & 0xff);
        }
        return bytes;
    }

//    /**
//     * 16进制的字符串表示转成字节数组
//     *
//     * @param hexString 16进制格式的字符串
//     * @return 转换后的字节数组
//     **/
//    public static byte[] toByteArray(String hexString) {
//        if (StringUtils.isEmpty(hexString))
//            throw new IllegalArgumentException("this hexString must not be empty");
//
//        hexString = hexString.toLowerCase();
//        final byte[] byteArray = new byte[hexString.length() / 2];
//        int k = 0;
//        for (int i = 0; i < byteArray.length; i++) {
//            //因为是16进制，最多只会占用4位，转换成字节需要两个16进制的字符，高位在先
//            byte high = (byte) (Character.digit(hexString.charAt(k), 16) & 0xff);
//            byte low = (byte) (Character.digit(hexString.charAt(k + 1), 16) & 0xff);
//            byteArray[i] = (byte) (high << 4 | low);
//            k += 2;
//        }
//        return byteArray;
//    }

    //byte[] 转 hex String
    /**
     * 将字节数组转换为16进制字符串 (大写带空格)
     * @param bytes
     * @return
     */
    public static String BinaryToHexString(byte[] bytes) {
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

//    /**
//     * 转十六进制字符串(大写不带空格)
//     * @param val
//     * @return
//     */
//    public static String bytesToHexStr(byte[] val) {
//        String temp = "";
//        for (int i = 0; i < val.length; i++) {
//            String hex = Integer.toHexString(0xff & val[i]);
//            if (hex.length() == 1) { //在个位数补0
//                hex = '0' + hex;
//            }
//            temp += hex.toUpperCase(); //转大写
//        }
//        return temp;
//    }

//    /**
//     * byte[]数组转换为hex string （大写）
//     *
//     * @param data 要转换的字节数组
//     * @return 转换后的结果
//     */
//    public static String byteArrayToHexString(byte[] data) {
//        StringBuilder sb = new StringBuilder(data.length * 2);
//        for (byte b : data) {
//            int v = b & 0xff;
//            if (v < 16) {
//                sb.append('0');
//            }
//            sb.append(Integer.toHexString(v));
//        }
//        return sb.toString().toUpperCase(Locale.getDefault());
//    }

    /**
     * 字节数组转成16进制表示格式的字符串 (小写不带空格)
     *
     * @param byteArray 需要转换的字节数组
     * @return 16进制表示格式的字符串
     **/
    public static String toHexString(byte[] byteArray) {
        if (byteArray == null || byteArray.length < 1)
            throw new IllegalArgumentException("this byteArray must not be null or empty");

        final StringBuilder hexString = new StringBuilder();
        for (int i = 0; i < byteArray.length; i++) {
            if ((byteArray[i] & 0xff) < 0x10)//0~F前面补零
                hexString.append("0");
            hexString.append(Integer.toHexString(0xFF & byteArray[i]));
        }
        return hexString.toString().toLowerCase();
    }

//    /**
//     * byte[]数组转换为16进制的字符串 （小写）
//     *
//     * @param bytes 要转换的字节数组
//     * @return 转换后的结果
//     */
//    public static String bytesToHexString(byte[] bytes) {
//        StringBuilder sb = new StringBuilder();
//        for (int i = 0; i < bytes.length; i++) {
//            String hex = Integer.toHexString(0xFF & bytes[i]);
//            if (hex.length() == 1) {
//                sb.append('0');
//            }
//            sb.append(hex);
//        }
//        return sb.toString();
//    }

}
