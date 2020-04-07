package com.zack.xjht.entity;

public class KeepTaskBean {


    /**
     * apply : 1
     * startTime : 2019-06-21 00:00:00
     * id : 2c438247f2b444ab8af7e5dbdfce90ef
     * endTime : 2019-06-22 00:00:00
     * userName : 若依
     */

    private int apply;  //申请人id
    private String startTime; //开始时间
    private String id;  //任务id
    private String endTime; //结束时间
    private String userName;  //申请人
    private String remark;      //备注

    public int getApply() {
        return apply;
    }

    public void setApply(int apply) {
        this.apply = apply;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

}
