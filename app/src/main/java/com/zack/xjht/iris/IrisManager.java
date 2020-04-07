package com.zack.xjht.iris;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.widget.TextView;


import com.sykean.A210Iris.Iris;
import com.zack.xjht.Constants;
import com.zack.xjht.Utils.ToastUtil;
import com.zack.xjht.Utils.TransformUtil;
import com.zack.xjht.Utils.Utils;
import com.zack.xjht.event.EventConsts;
import com.zack.xjht.event.MessageEvent;

import org.greenrobot.eventbus.EventBus;

/**
 * 虹膜设备管理
 */

public class IrisManager {
    private static final String TAG = "IrisManager";
    private static IrisManager instance;
    private static String readstr;
    private Iris iris;
//    private String port = "/dev/ttysWK0";
    private String port = "/dev/ttyS1";
    private int baudrate = 115200;
//    private int baudrate = 19200;
    private byte[] none;
    private String result;
    private static TextView tvMsg;
    private int ret;

    private IrisManager() {
    }

    public static IrisManager getInstance() {
        if (instance == null) {
            instance = new IrisManager();
        }
        return instance;
    }

    @SuppressLint("HandlerLeak")
    public static Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            Log.i(TAG, "handleMessage msg what: " + msg.what);
            switch (msg.what) {
                case 1:
                    if(tvMsg !=null){
                        tvMsg.setText(readstr);
                    }
                    break;
            }
        }
    };

    /**
     * 初始化虹膜
     */
    public boolean initIris() {
        Log.i(TAG, "initIris 初始化虹膜: ");
        iris = new Iris();
        iris.setJNIEnv(this, "recvCallback");
        Log.i(TAG, "initIris   打开串口: ");
        int ret = Iris.openPort(port, baudrate);
        Log.e("initIris ret==", ret + "");
        if (ret == 0) {
            result = "虹膜初始化成功";
            Log.i(TAG, "虹膜初始化成功: ");
        } else {
            result = "虹膜初始化失败";
            Log.i(TAG, "虹膜初始化失败！: ");
        }

        none = new byte[1024];
        Log.i(TAG, "initIris 发送 启动虹膜命令: ");
        ret = Iris.sendData((byte) (0X41), none, 0);
        Log.i(TAG, "MenuOnclick ret: " + ret);
        if (ret == 0) {
            Log.i(TAG, " 虹膜启动成功: ");
            result = "0x41 命令发送成功==" + ret;
            Log.i(TAG, "initIris: " + result);
            Constants.isIrisInit =true;
            return true;
        } else {
            Log.i(TAG, " 虹膜启动失败！: ");
            result = "0x41 发送错误返回值==" + ret;
            Log.i(TAG, "initIris: " + result);
            Constants.isIrisInit =false;
            return false;
        }
    }

    /**
     * 注册虹膜
     *
     * @param irisId
     * @param txtView
     */
    public void registerIris(String irisId, TextView txtView, OnRegisteredReceiv onRegisteredReceiv) {
        Log.i(TAG, "registerIris 注册虹膜: ");
        this.tvMsg = txtView;
        this.onRegisteredReceiv = onRegisteredReceiv;
        if (TextUtils.isEmpty(irisId)) {
            ToastUtil.showShort("ID为空");
            return;
        }
        none = Utils.StringToAsciiByte(irisId);
        Log.i(TAG, "MenuOnclick none: " + bytesToHexString(none));
        Log.i(TAG, "registerIris  发送注册命令: ");
        ret = Iris.sendData((byte) Iris.CMD_START_REGISTER, none, 0x0018);
        Log.i(TAG, "MenuOnclick 开始注册 ret: " + ret);
        if (ret == 0) {
            tvMsg.setText("发送注册命令成功");
            result = "0x43 命令发送成功 ===" + ret;
            Log.i(TAG, "发送注册命令成功 : " + result);
        } else {
            tvMsg.setText("发送注册命令失败");
            result = "0x43 发送错误返回值==" + ret;
            Log.i(TAG, "发送注册命令失败: " + result);
        }
    }

    /**
     * 删除指定模版
     */
    public void deleteTempByID(String irisId, TextView txtView) {
        tvMsg = txtView;
        Log.i(TAG, "deleteTempByID 删除指定模板: ");
        if (!TextUtils.isEmpty(irisId)) {//删除指定用户id
            none = Utils.StringToAsciiByte(irisId);
            Log.i(TAG, "deleteTempByID 发送删除模版命令: ");
            ret = Iris.sendData((byte) (0X44), none, 0x0018);
            if (ret == 0) { //delete_success
                result = "0x44 命令发送成功 ===" + ret;
                Log.i(TAG, "删除命令发送成功: " + result);
            } else { //delete_failure
                result = "0x44 发送错误返回值==" + ret;
                Log.i(TAG, "删除命令发送失败: " + result);
            }
        }
    }

    /**
     * 删除全部模版
     */
    public void deleteAllTemp() {
        Log.i(TAG, "deleteAllTemp 删除全部模版: ");
        none = new byte[24];
        ret = Iris.sendData((byte) (0X44), none, 0x0018);
        if (ret == 0) { //delete_success
            result = "0x44 命令发送成功 ===" + ret;
            Log.i(TAG, "deleteAllTemp: " + result);
            mHandler.sendEmptyMessage(1);
        } else { //delete_failure
            result = "0x44 发送错误返回值==" + ret;
            Log.i(TAG, "deleteAllTemp: " + result);
            mHandler.sendEmptyMessage(1);
        }
    }

    /**
     * 获取指定ID的模板
     */
    public void getTemp(String irisID, TextView txtView, OnTempReceiv onTempReceiv) {
        Log.i(TAG, "getTemp irisID: " + irisID);
        this.onTempReceiv = onTempReceiv;
        tvMsg = txtView;
        if (!TextUtils.isEmpty(irisID)) {
            none = Utils.StringToAsciiByte(irisID);
            ret = Iris.sendData((byte) Iris.CMD_GET_IRIS_TEMPLATE, none, 0x0018);
            if (ret == 0) {
                result = "0x48 发送命令成功==" + ret;
                Log.i(TAG, "getTemp: " + result);
            } else {
                result = "0x48 发送错误返回值==" + ret;
                Log.i(TAG, "getTemp: " + result);
            }
            mHandler.sendEmptyMessage(1);
        }
    }

    /**
     * 虹膜识别
     */
    public void recognition(OnReceiveCallback receiveCallback, TextView txtView) {
        Log.i(TAG, "recognition 1:N识别: ");
        onReceiveCallback = receiveCallback;
        tvMsg = txtView;
        none = new byte[24]; //1：N识别|
        for (int i = 0; i < none.length; i++) {
            none[i] = (byte) 0xff;
        }
        ret = Iris.sendData((byte) Iris.CMD_START_RECOGNITION, none, 0x0018);
        Log.i(TAG, "MenuOnclick开始识别  ret: " + ret);
        if (ret == 0) {
            result = "0x42 命令发送成功 ===" + ret;
            Log.i(TAG, "启动识别成功 : " + result);
        } else {
            result = "0x42 发送错误返回值==" + ret;
            Log.i(TAG, "启动识别失败 : " + result);
        }
    }

    //取消当前动作
    public void cancelAction() {
        none = new byte[24];
        ret = Iris.sendData((byte) (0X46), none, 0);
        if (ret == 0) {
            result = "0x46 命令发送成功 ===" + ret;
            Log.i(TAG, "发送取消成功 : " + result);
        } else {
            result = "0x46 发送错误返回值==" + ret;
            Log.i(TAG, "发送取消失败: " + result);
        }
    }

    /**
     * 下发模板
     */
    public void downTemplate(String id, String hexTemp) {
//        onReceiveCallback =receiveCallback;
//        Log.i(TAG, "downTemplate userId: "+Utils.StringToAsciiByte(id));
        byte[] userId = Utils.StringToAsciiByte(id);
//        Log.i(TAG, "downTemplate userId:"+Utils.byteArrayToHexString(userId));
//        String hexTemp2 = "0f0f0f0f0e0f0f1f1f1f1f1f1f3f3f3f3f3f7ffffffffeffeffffbffeffbfdbfcfcfcdc747c7c3c3c3c3c3c3c2c3cecff77f7f335f7f3f37393f3f3f3f3f3d3e3f3e3f3f3f3f3f3f3e3f3f3f7f7f7f7b7ffffffffdff7effffffeffffffffffffefefffb7f6f5f7f3f3f2f3f1f1f1f1f1f1f1d1f0f0f0f0f0f0f0f071f1f1f1f0f0f0e0f0f0f0f1f0f1f1f1b0f3f3f3f3f3f7bf7fffffffffffffffbfffeffffcfcfcb87c5c703c3c3c3c3c381c34fcdff3f7f7f7f7f3f2f2f3f3f3f353d3d3d3f1f3e3b3f3d3f3e3f3f3f1f5f7f7f7e7ffffffbfffffffff7fffffffffffdffffffffff7f7f6f7f1f3d3f3f1f1f1e1f1f1f1f1f0d0f0f0f0f0f0f1f1e1f1f1f0d1d1d3f2627a3abf9594c444682b1b1184ce6e6f339391cdccfc7232370fcbf8f076564f0b1b39fcc5c7c7f66e4ce9f3b30909dc7e76160682e0f1d9cfaf262078488b8bbbfff4441417b7bbebefcc000023bfff3e1810a0e04fcf8f9ffe727191883e3e2f0d0d8ce8e2f3f316020080b9b919292e66c4d4d0929a2b23323275c192222230359585c440682b2b1395c4ee6f73919188ccec7e323347cfdcf8f456474f09b9b8f4c4c7e63e3c49b9b3330c0c5e76760681e1f1f99f8f26005070d98797bff4640407bfbbebe94c44000037ffff580080e0ef4fdf97b4707061890e0e6e450581c0e2f3f313000088a9f97d4d4e66e6d4d1933b2b2b2b7c7cd5c1f1f1f1f1e1f1f0f0e0f0f070d0f0f0f0d0f0f0f3f3b3f3b3f3f3f3f3f3e7f7f3dffffffffff7fff7ffffeffffffffffffeffb7f6f7c7d1f3f3e2f3f1f3f373f3f3f3d3d373f3f3f3e3f3f3c2f7f7f7f7dffffffffffffff7ffded7fffffffffffffffffffedffdfffffffffff7f7d7f3e3f3f3f1f1e1f1f1f1d1d1d1d1d1d1f1f1f1f1b1f1f1f1f0f0f0f0f0e0f0d0f0f0f0f0f3f3f3f3f3f3f3f2f3f3f6f5f7ffff9fffe7ffff77efd77ffffffffcbffffff7e7f6f7f3e3b3f3f3f3f3f3f3f3f37371f3f3f3f3d1f3f3f3f3f7f7e5f7fffffffffffff7ffdfdfdfdffffffffffcfffffffff9fffefffffd7ff7f7f7f3d3f3f3f1f1f1f1f1f1d1d1d1d1d1d1f06273131a1a8eeee5f7f2f8684c0d1010d8f7f7b7814040403737bfefee9a1217c7efac385049cfbfbf3e78c1c1820e3e3c3d8dcecaced5b535256ce8cb8b0b0b3270707c7fff8b8352363495cdc9e868389997a76f6f71d8dc8e81293b3e060489f87a62652d8d9d99f3f3e7e4f030130b8bfff4f47504082832321383c1c0e7331314c4e4e5e5f17018080d1313d2f3f7e787080040bfbfbfefceca1a1155e7eca85850cfafbfbe7840c1810e3e3c3dcdcfcaf2f2b535316a4aca8b8b073470f0ec6defab83027634b48dc9c9683e1f9787e76f79d0908c8f01013e5e4481d9f87266058d9d9bf07266640c10101b0feffcf46400080a3a7a3f8fcdc4e4667";
        byte[] temp = TransformUtil.hexStrToBytes(hexTemp);
//        none =Utils.mergeBytes(userId, temp);
        none = new byte[userId.length + temp.length];
        System.arraycopy(userId, 0, none, 0, userId.length);
        System.arraycopy(temp, 0, none, userId.length, temp.length);
        ret = Iris.sendData((byte) Iris.CMD_DOWN_TEMPLATE, none, 0x0200);
        if (ret == 0) {
            Log.i(TAG, "MenuOnclick 下发模板命令成功: ");
        } else {
            Log.i(TAG, "MenuOnclick 下发模板命令失败: ");
        }
    }

    /**
     * 读取用户id
     * @param callback
     */
    public void readUserId(OnReadIDResult callback) {
        onReadIDResult = callback;
        none = new byte[24];
        ret = Iris.sendData((byte) Iris.CMD_READ_USER_ID, none, 0x0018);
        if (ret == 0) {//读取成功
            result = "0x45 命令发送成功 ===" + ret;
            Log.i(TAG,"启动读取成功:"+result);
        } else {
            result = "0x45 发送错误返回值==" + ret;
            Log.i(TAG,"启动读取失败:"+result);
        }
    }

    /**
     * 获取当前设备状态
     */
    public void getCurrentStatus(){
        none = new byte[24];
        ret = Iris.sendData((byte) Iris.CMD_CURRENT_DEVICE_STATE, none, 0x0000);
        if (ret == 0) {//读取成功
            result = "0x47 命令发送成功 ===" + ret;
            Log.i(TAG,"获取当前设备状态成功:"+result);
        } else {
            result = "0x45 发送错误返回值==" + ret;
            Log.i(TAG,"获取当前设备状态失败:"+result);
        }
    }

    public interface OnReceiveCallback {
        void onReceiveInfo(byte[] info);
    }

    private static OnReceiveCallback onReceiveCallback;

    private static OnRegisteredReceiv onRegisteredReceiv;

    public interface OnRegisteredReceiv {
        void onResult(int result);
    }

    private static OnTempReceiv onTempReceiv;

    public interface OnTempReceiv {
        void onTempReceiv(byte[] temp);
    }

    private static long lastTime;
    public static void recvCallback(byte cmd, byte[] info, int len) {
        try {
            Log.i(TAG, "recvCallback  cmd: " + Integer.toString(cmd,16) + " len: " + len);
            Log.i(TAG, "recvCallback info: " + TransformUtil.BinaryToHexString(info));
            if (cmd == 65) { //启动虹膜
                Log.i(TAG, "recvCallback info: " + bytesToHexString(info));
            }

            if (cmd == 67 && len > 0) { //注册
                switch (info[0]) {
                    case 0x00: //regist_success
                        readstr = "regist_success";
                        Log.i(TAG, "recvCallback: " + readstr);
                        mHandler.sendEmptyMessage(1);
                        onRegisteredReceiv.onResult(0);
                        break;
                    case 0x01: //register_failure
                        readstr = "register_failure";
                        mHandler.sendEmptyMessage(1);
                        Log.i(TAG, "recvCallback: " + readstr);
                        onRegisteredReceiv.onResult(1);
                        break;
                    case 0x02://不同ID 重复注册
                        readstr = "不同ID 重复注册";
                        mHandler.sendEmptyMessage(1);
                        Log.i(TAG, "recvCallback: " + readstr);
                        onRegisteredReceiv.onResult(2);
                        break;
                    case 0x03: //相同ID 注册
                        readstr = "相同ID 重复注册";
                        mHandler.sendEmptyMessage(1);
                        Log.i(TAG, "recvCallback: " + readstr);
                        onRegisteredReceiv.onResult(3);
                        break;
                    default:
                        break;
                }
            }
            if (cmd == 86 && len > 0) { //反馈信息

                long l = System.currentTimeMillis();
    //            readstr = bytesToHexString(info);
    //            mHandler.sendEmptyMessageDelayed(4, 0);

    //            while(receivCount > 0){
    //                receivCount--;
    //                try {
                if((l -lastTime) >1000L){
                    sendRemindMsg(info[0]);//发送提示信息
                }
    //                    Thread.sleep(200);
    //                } catch (InterruptedException e) {
    //                    e.printStackTrace();
    //                }
    //            }
    //            sendRemindMsg(info[1]);

            }

            if (cmd == 68 && len > 0) { //删除结果
                if (info[0] == 0x00) { //delete_success
                    readstr = "删除模板成功";
                    Log.i(TAG, "recvCallback: " + readstr);
                    mHandler.sendEmptyMessage(1);
                } else if (info[0] == 0x01) { //delete_failure
                    readstr = "删除模板失败";
                    Log.i(TAG, "recvCallback: " + readstr);
                    mHandler.sendEmptyMessage(1);
                }
            }

            if (cmd == 72 && len > 0) { //获取模板
                Log.i(TAG, "recvCallback 获取到的模板: " + bytesToHexString(info));
                readstr = bytesToHexString(info);
    //            mHandler.sendEmptyMessage(2);
                Log.i(TAG, "recvCallback len: " + len);
                Log.i(TAG, "recvCallback info length: " + info.length);
                if (len == 1024) {
                    Log.i(TAG, "recvCallback 获取模板成功: ");
                    onTempReceiv.onTempReceiv(info);
                }
            }

            if (cmd == 66 && len > 0) { //识别结果
    //            Log.i(TAG, "recvCallback info: " + Arrays.toString(info));
                String res = Utils.getIrisID(info, len);
                Log.i(TAG, "recvCallback res: " + res);
                if (!TextUtils.isEmpty(res)) {
                    Log.i(TAG, "recvCallback 识别成功: ");
                    readstr = "识别成功 ID:" + res;
                    mHandler.sendEmptyMessage(1);
                } else {
                    Log.i(TAG, "recvCallback 识别失败: ");
                    readstr = "识别失败！";
                    mHandler.sendEmptyMessage(1);
                }
                onReceiveCallback.onReceiveInfo(info);
            }

            if (cmd == 73 && len > 0) { //下发模板
                switch (info[0]) {
                    case 0: //下发成功
                        Log.i(TAG, "recvCallback 下发模板成功:  ");
                        break;
                    case 1: //下发失败
                        Log.i(TAG, "recvCallback 下发模板失败: ");
                        break;
                }
            }

            if (cmd == 69 && len > 0) { //读取用户数据
                // 返回：6+ Usercount*24，usercount 为模块存储的用户总数。
                // 请求返回一个用户时只返回该用户的，如果id 不存在则返回24 个0。
                String res = Utils.getIrisID(info, len);
                onReadIDResult.onResult(res);
                Log.i(TAG, "recvCallback res: " + res);
                if (!TextUtils.isEmpty(res)) {
                    readstr = "读取用户成功 ID：" + res;
                    Log.i(TAG, "recvCallback: "+readstr);
                } else {
                    readstr = "读取用户失败";
                    Log.i(TAG, "recvCallback: "+readstr);
                }
            }

            if(cmd == 71 && len >0){
                if(info[0] ==0x00){
                    Log.i(TAG, "recvCallback 模块工作正常: ");
                }else if(info[0] == 0x01){
                    Log.i(TAG, "recvCallback 硬件版本不匹配: ");
                }
            }

            if(cmd ==70 ){

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void sendRemindMsg(byte left) {
        switch (left) {
            case 0x00:
                readstr = "请保持当前状态";
                Log.i(TAG, "sendRemindMsg: " + readstr);
                mHandler.sendEmptyMessage(1);
                EventBus.getDefault().post(new MessageEvent(EventConsts.KEEP_CURRENT_STATUS));
                lastTime =System.currentTimeMillis();
                break;
            case 0x01:
                readstr = "调整距离";
                mHandler.sendEmptyMessage(1);
                Log.i(TAG, "sendRemindMsg: " + readstr);
                EventBus.getDefault().post(new MessageEvent(EventConsts.ADJUST_DISTANCE));
                lastTime =System.currentTimeMillis();
                break;
            case 0x02:
                readstr = "请看镜子里面";
                mHandler.sendEmptyMessage(1);
                Log.i(TAG, "sendRemindMsg: " + readstr);
                EventBus.getDefault().post(new MessageEvent(EventConsts.WATCH_MIRROR));
                lastTime =System.currentTimeMillis();
                break;
            case 0x03:
                readstr = "请靠近";
                mHandler.sendEmptyMessage(1);
                Log.i(TAG, "sendRemindMsg: " + readstr);
                EventBus.getDefault().post(new MessageEvent(EventConsts.CLOSE_TO));
                lastTime =System.currentTimeMillis();
                break;
            case 0x04:
                readstr = "请张大眼睛";
                mHandler.sendEmptyMessage(1);
                Log.i(TAG, "sendRemindMsg: " + readstr);
                EventBus.getDefault().post(new MessageEvent(EventConsts.OPEN_EYES));
                lastTime =System.currentTimeMillis();
                break;
            case 0x05:
                readstr = "请不要斜视";
                mHandler.sendEmptyMessage(1);
                Log.i(TAG, "sendRemindMsg: " + readstr);
                EventBus.getDefault().post(new MessageEvent(EventConsts.DONT_LOOK_AWRY));
                lastTime =System.currentTimeMillis();
                break;
            default:
                break;
        }
    }

    public interface OnReadIDResult{
        void onResult(String result);
    }
    public static OnReadIDResult onReadIDResult;

    public void setOnReadIDResult(OnReadIDResult onReadIDResult) {
        this.onReadIDResult = onReadIDResult;
    }

    public static String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder("");
        if (src == null || src.length <= 0) {
            return null;
        }
        for (byte aSrc : src) {
            int v = aSrc & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }
}
