package com.sykean.A210Iris;

public class Iris{

    public static final int CMD_START_DEVICE =0x41; //启动命令
    public static final int CMD_START_RECOGNITION =0x42; //开始识别
    public static final int CMD_START_REGISTER =0x43; //开始注册
    public static final int CMD_DELETE_TEMPLATE =0x44; //删除模版
    public static final int CMD_READ_USER_ID =0x45; //读取用户id号
    public static final int CMD_CANCEL_CURRENT_ACTION =0x46; //取消当前动作
    public static final int CMD_CURRENT_DEVICE_STATE =0x47; //获取当前设备状态
    public static final int CMD_GET_IRIS_TEMPLATE =0x48; //获取ID对应虹膜模板
    public static final int CMD_DOWN_TEMPLATE =0x49; //下发对应ID的模版
    public static final int CMD_DOWN_APPLICATION =0x4A; //下发应用程序
    public static final int CMD_SWITCH_CARD_READ_MODE =0x4B; //切换成卡识别模式
    public static final int CMD_READ_M1_CARD_NO =0x4C; //读M1卡卡号
    public static final int CMD_START_PRODUCE_TEMPLATE =0x4D; //开始产生模板
    public static final int CMD_REAL_TIME_GET_TEMPLATE =0x4E; //实时获取模版cmd发送
    public static final int CMD_CATCH_PICTURE =0x4F; //捕捉图像
    public static final int CMD_RESPONSE_HANDSHAKE_PACKET =0x53; //应答握手包
    public static final int CMD_VERIFY_WRONG_HANDSHAKE_PACKET =0x54; //校验错误握手包
    public static final int CMD_DOWN_REGISTER_VERIFY_CONFIG =0x55; //下发对应注册识别配置
    public static final int CMD_BACK_IRIS_PROCESS_FEEDBACK =0x56; //返回虹膜过程反馈信息

    static {
    	System.loadLibrary("A210Iris");
    }
    
    public native void setJNIEnv(Object obj,String func);
    public static native int openPort(String path,int baudrate);
    public static native int closePort();
    public static native int sendData(byte cmd,byte[] send_data,int nDatalen);
//    public static native int recvData(byte cmd, byte[] recv_data, int recv_len);
}
