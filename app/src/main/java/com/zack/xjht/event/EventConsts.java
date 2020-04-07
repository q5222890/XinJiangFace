package com.zack.xjht.event;

/**
 * Created by Administrator on 2017-09-05.
 */

public class EventConsts {

    public static final String GET_CABS_SUCCESS ="1001"; //获取枪柜数据成功
    public static final String GET_MEMBERS_SUCCESS ="1002"; //获取警员数据成功
    public static final String SYNC_TIME_SUCCESS ="1003"; //同步时间成功
    public static final String SYNC_TIME_FAILED ="1004"; //同步时间失败
    public static final String UPDATE_CABS_DATA ="1005"; //更新枪柜数据

    public static final String EVENT_POWER_NORMAL ="2001"; //市电电源正常
    public static final String EVENT_BACKUP_POWER ="2002"; //备用电源供电
    public static final String EVENT_BACKUP_OPEN ="2003"; //备用电源供电
    public static final String EVENT_BACKUP_CLOSE ="2004"; //备用电源供电
    public static final String EVENT_CAB_OPEN ="2005"; //备用电源供电
    public static final String EVENT_CAB_CLOSE ="2006"; //备用电源供电
    public static final String EVENT_VIBRATION_NORMAL ="2007"; //备用电源供电
    public static final String EVENT_VIBRATION_ABNORMAL ="2008"; //备用电源供电

    public static final String EVENT_POST_SUCCESS ="3001"; //提交成功
    public static final String EVENT_POST_FAILURE ="3002"; //提交失败

    public static final String EVENT_FINGER_TIME_OUT ="10000";


    public static final String KEEP_CURRENT_STATUS ="0x00";
    public static final String ADJUST_DISTANCE ="0x01";
    public static final String WATCH_MIRROR ="0x02";
    public static final String CLOSE_TO ="0x03";
    public static final String OPEN_EYES ="0x04";
    public static final String DONT_LOOK_AWRY ="0x05";



}
