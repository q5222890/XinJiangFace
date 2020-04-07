package com.zack.xjht.entity;

/**
 * 领枪任务实体类
 */
public class GoTaskBean {


    /**
     * applyId : 1
     * apply : 若依
     * startTime : 2019-06-25 17:30:00
     * id : 782d1ca8f09e45f9b83f08b0d0519f1a
     * endTime : 2019-06-27 18:35:00
     * operation : out
     */

    /**
     * {"apply":"若依","applyId":1,"endTime":"2019-06-29 18:50:00",
     * "id":"51712db118834f9a9fab3d39c123edd3","operation":"out",
     * "startTime":"2019-06-28 18:55:00"}
     */
    private int applyId;  //申请人id
    private String apply;  //申请人
    private String startTime;  //开始时间
    private String id;      //任务id
    private String endTime;  //结束时间
    private String remark; //备注

    public int getApplyId() {
        return applyId;
    }

    public void setApplyId(int applyId) {
        this.applyId = applyId;
    }

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
