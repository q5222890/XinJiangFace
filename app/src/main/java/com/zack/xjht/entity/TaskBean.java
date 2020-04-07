package com.zack.xjht.entity;

import java.io.Serializable;

/**
 *
 * 枪弹入库任务实体类
 */
public class TaskBean implements Serializable {


    private String apply;  //申请人
    private String startTime;  //开始时间
    private String id;  //id
    private String endTime; //结束时间
    private String remark;


    public String getApply() {
        return apply;
    }

    public void setApply(String apply) {
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

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
