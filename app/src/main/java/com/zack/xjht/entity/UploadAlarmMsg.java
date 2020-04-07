package com.zack.xjht.entity;

import java.util.Date;

/**
 * 上传报警数据
 */
public class UploadAlarmMsg {

    private String cID; //专用柜唯一编号
    private String alarmContect; //报警内容
    private Date time; //报警时间

    public String getcID() {
        return cID;
    }

    public void setcID(String cID) {
        this.cID = cID;
    }

    public String getAlarmContect() {
        return alarmContect;
    }

    public void setAlarmContect(String alarmContect) {
        this.alarmContect = alarmContect;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }
}
