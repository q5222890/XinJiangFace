package com.zack.xjht.entity;


/**
 * 报废任务实体类
 */
public class ScrapTaskBean {


    /**
     * apply : 1
     * startTime : 2019-06-21 00:00:00
     * id : 36e87430c9514a67b36891f42b91d608
     * endTime : 2019-06-22 00:00:00
     * applyName : 若依
     */

    private int apply;  //申请人id
    private String startTime; //开始时间
    private String id;  //任务id
    private String endTime;  //结束时间
    private String applyName; //申请人
    private String remark;

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

    public String getApplyName() {
        return applyName;
    }

    public void setApplyName(String applyName) {
        this.applyName = applyName;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
