package com.zack.xjht;


public class Constants {
    public static final String APP_ID = "841wYrFb7jzwVGnPz2Ug1mJsKU17eVPNuavwJmLGptVB";
    public static final String SDK_KEY = "3AJ3UwdVnZtRU2pTsJJEzpKFTKK6Sr5KQbXqb2zGAoM7";

    public static final String IP = "139.9.194.86"; //服务器IP地址
    public static final String PORT = "8080";  //服务器端口号
    //老主板
    public static boolean isOldBoard =false;
    //调试
    public static boolean isDebug =true;
    /**
     * 1.震动报警
     * 2.断电报警
     * 3.备用钥匙开启报警
     * 4.开门超时报警
     * 5.网络断开
     * 6.温湿度异常报警
     * 7.酒精检测异常报警
     * */
    public static final int ALARM_VIBRATION= 1;//震动报警
    public static final int ALARM_POWER_ABNORMAL =2;//市电断开
    public static final int ALARM_BACKUP_OPEN_GUN_LOCK =3;//备用钥匙开启柜门
    public static final int ALARM_OPEN_CAB_OVERTIME =4;//柜门开启超时报警
    public static final int ALARM_NETWORK_DISCONNECT =5;//网络断开报警
    public static final int ALARM_HUMITURE_ABNORMAL =6;//温湿度异常报警
    public static final int ALARM_ALCOHOL_ABNORMAL =7;//酒精检测异常报警
    public static final int ALARM_OPEN_CAB_ABNORMAL =8;//非正常开启枪柜
    public static final int ALARM_GET_GUN_ABNORMAL =9;//非正常领取枪支或弹药

    public static final int CHECK_SLEEP_TIME =7;//酒精检测异常报警

    //在执行任务中
    public static boolean isExecuteTask =false;
    /**
     * 是否检查枪支状态
     */
    public static boolean isCheckGunStatus =false;
    //指纹连接
    public static boolean isFingerConnect =false;
    //指纹初始化
    public static boolean isFingerInit =false;
    //人脸初始化
    public static boolean isFaceInit =false;
    //虹膜初始化
    public static boolean isIrisInit =false;

    //上传开柜数据
    public static boolean isUploadMessage =true;

    public static final String DEVICE_FINGER ="1";     //指纹
    public static final String DEVICE_VEIN ="2";     //指静脉
    public static final String DEVICE_IRIS ="3";     //虹膜
    public static final String DEVICE_FACE ="4";     //人脸

    public static final String finger = "finger";
    public static final String face = "face";
    public static final String iris = "iris";
    public static final String alcohol = "alcohol";

    public static final String TYPE_SHORT_GUN = "shortGun";
    public static final String TYPE_LONG_GUN = "longGun";
    public static final String TYPE_AMMO = "ammunition";

    public static final String ACTIVITY_URGENT = "URGENT";
    public static final String ACTIVITY_URGENT_GET_GUN = "URGENT_GET_GUN";
    public static final String ACTIVITY_URGENT_GET_AMMO = "URGENT_GET_AMMO";
    public static final String ACTIVITY_URGENT_BACK_GUN = "URGENT_BACK_GUN";
    public static final String ACTIVITY_URGENT_BACK_AMMO = "URGENT_BACK_AMMO";
    public static final String ACTIVITY_GET = "GET";
    public static final String ACTIVITY_BACK = "BACK";
    public static final String ACTIVITY_KEEP = "KEEP";
    public static final String ACTIVITY_SCRAP = "SCRAP";
    public static final String ACTIVITY_TEMP_IN = "TEMP_STORE_IN";
    public static final String ACTIVITY_TEMP_GET = "TEMP_STORE_GET";
    public static final String ACTIVITY_USER = "USER";
    public static final String ACTIVITY_IN_STORE = "INSTORE";
    public static final String ACTIVITY_SETTING = "SETTING";
    public static final String ACTIVITY_OPEN_CAB = "OPEN_CAB";
    public static final String ACTIVITY_OFFLINE_GET = "OFFLINE_GET";
    public static final String ACTIVITY_OFFLINE_BACK = "OFFLINE_BACK";

    public static final String ROLE_APPROVER = "Approver";
    public static final String ROLE_MANAGER = "GunManagement";
    public static final String ROLE_ROOM_ADMIN = "guncabinetAdmin";
    public static final String ROLE_ADMIN = "admin";
    public static final String ROLE_POLICE = "police";

    //枪柜类型
    public static final String TYPE_SHORT_LONG_GUN_CAB = "1"; //长短枪一体柜
    public static final String TYPE_AMMO_CAB = "2"; //弹柜
    public static final String TYPE_MIX_CAB = "3"; //枪弹柜
    public static final String TYPE_LONG_GUN_CAB = "4"; //长枪柜
    public static final String TYPE_SHORT_GUN_CAB = "5"; //短枪柜

    //是否第一个验证
    public static boolean isFirstVerify =true;

    public static boolean isVibrationAlarm = false;  //是否产生震动报警
    public static boolean isKeyOpenAlarm = false;  //钥匙开启报警
    public static boolean isOverTimeAlarm = false;  //开门超时报警
    public static boolean isPowerOffAlarm = false;  //市电断开报警
    public static boolean isNetDisconnectAlarm = false;  //网络断开报警
    public static boolean isHumitureAalrm = false;  //温湿度异常报警
    public static boolean isAlcoholAlarm = false;  //酒精浓度异常报警
    public static boolean isIllegalOpenCabAlarm =false; //非正常开启枪柜报警
    public static boolean isIllegalGetGunAlarm =false; //非正常领取枪弹报警

    public static boolean isCapturing;    //是否在抓拍

    public static String SERVER_NAME = "/GunManagementSystem/business/androidInterface/";

    /**
     *  根据MAC地址获取枪柜信息
     */
    public static final String GET_CAB_INFO = SERVER_NAME + "getGunCabinetInfo";

    /**
     *  根据MAC地址获取日期和时间
     */
    public static final String GET_DATE_AND_TIME = SERVER_NAME + "getSystemTime";

    /**
     * 获取枪弹入库任务列表
     */
    public static final String GET_INSTORE_TASK_LIST = SERVER_NAME + "selectGunStorageList";

    /**
     * 获取枪弹入库任务清单列表
     */
    public static final String GET_INSTORE_TASK_INFO = SERVER_NAME + "selectGunStorageListList";

    /**
     * 获取生物特征
     */
    public static final String GET_CHAR_LIST = SERVER_NAME + "selectBiometricsList";

    /**
     * 获取用户数据
     */
    public static final String GET_USER_LIST = SERVER_NAME + "selectUserList";

    /**
     * 获取用户角色
     */
    public static final String GET_USER_ROLE = SERVER_NAME + "selectUserRoleList";

    /**
     * 上传用户生物特征
     */
    public static final String POST_USER_CHAR = SERVER_NAME + "registerBiometrics";
    /**
     * 删除用户生物特征
     */
    public static final String DELETE_USER_CHAR = SERVER_NAME + "deleteBiometrics";

    /**
     * 提交入库数据
     */
    public static final String POST_INSTORE_DATA = SERVER_NAME + "submitGunStorageList";

    /**
     * 获取报废任务
     */
    public static final String GET_SCRAP_TASK_LIST = SERVER_NAME + "selectGunScrapList";

    /**
     * 获取报废任务清单
     */
    public static final String GET_SCRAP_TASK_INFO = SERVER_NAME + "selectGunScrapListList";

    /**
     * 提交报废任务数据
     */
    public static final String POST_SCRAP_DATA = SERVER_NAME + "submitGunScrapList";

    /**
     * 获取保养任务
     */
    public static final String GET_KEEP_TASK_LIST = SERVER_NAME + "selectGunMaintainList";

    /**
     * 获取保养任务清单
     */
    public static final String GET_KEEP_TASK_INFO = SERVER_NAME + "selectGunMaintainListList";

    /**
     * 提交保养任务数据
     */
    public static final String POST_KEEP_DATA = SERVER_NAME + "submitGetGunForMaintainTask";

    /**
     * 临时存放枪支
     */
    public static final String POST_TEMP_STORE_GUN = SERVER_NAME + "submitDepositGun";
    /**
     * 获取临时存放枪支
     * 参数：mac
     */
    public static final String GET_TEMP_STORE_GUN = SERVER_NAME + "selectDepositGunList";
    /**
     * 提交取出临时存放枪支
     */
    public static final String POST_OUT_TEMP_STORE_GUN = SERVER_NAME + "obtainDepositGun";
    /**
     * 上传报警日志
     */
    public static final String POST_ALARM_LOG = SERVER_NAME + "addWarning";
    /**
     * 上传抓拍图片
     */
    public static final String POST_CAPTURE_PHOTO = SERVER_NAME + "insertPicture";

    /**
     * 提交紧急领枪任务
     */
    public static final String POST_URGENT_GET = SERVER_NAME + "submitUrgentTask";

    /**
     * 获取紧急领枪任务
     */
    public static final String GET_URGENT_TASK = SERVER_NAME + "selectUrgentTaskList";
    /**
     * 获取紧急领枪任务清单
     */
    public static final String GET_URGENT_TASK_INFO = SERVER_NAME + "selectUrgentTaskDetail";
    /**
     * 紧急领枪归还枪弹
     */
    public static final String POST_URGENT_TASK_BACK_DATA = SERVER_NAME + "revertGunByUrgentTask";
    /**
     * 警号密码登录
     */
    public static final String USER_NO_LOGIN = SERVER_NAME + "login";
    /**
     * 获取领枪任务
     */
    public static final String GET_POLICE_TASK_LIST = SERVER_NAME + "selectPoliceTaskList";
    /**
     * 获取领枪任务详情
     */
    public static final String GET_POLICE_TASK_INFO = SERVER_NAME + "selectPoliceTaskListList";
    /**
     * 提交领枪任务数据
     */
    public static final String POST_POLICE_TASK_DATA = SERVER_NAME + "submitPoliceTaskList";
    /**
     * 提交日常操作日志
     */
    public static final String POST_COMMON_LOG = SERVER_NAME + "insertOperationalLogBatch";
    /**
     * 提交离线领枪日志
     */
    public static final String POST_OFFLINE_TASK_LOG = SERVER_NAME + "insertNoNetworkGunRecord";

    /**
     * 上传开柜数据
     */
    public static final String UPLOAD_OPEN_MESSAGE = "uploadOpenMessage";

    /**
     * 上传报警数据
     */
    public static final String UPLOAD_ALARM_MESSAGE = "uploadAlarmMessage";

    /**
     * 提交紧急领枪数据
     */
    public static final String POST_URGENT_DATA = SERVER_NAME + "reciveUrgentTask";

}
