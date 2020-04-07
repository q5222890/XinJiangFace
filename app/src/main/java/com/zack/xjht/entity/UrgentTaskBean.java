package com.zack.xjht.entity;

public class UrgentTaskBean {


    /**
     * approvalName : 李四
     * id : 2271709b530d40138644112422dd9d69
     * applyName : 张三
     * outTime : 2011-01-01 21:23:55
     * gunCabinetNo : 1003
     */

    private String approvalName;  //审批人姓名
    private String id;          //任务ID
    private String applyName;     //申请人姓名
    private String outTime;     //领出时间
    private String gunCabinetNo;    //枪柜编号

    public String getApprovalName() {
        return approvalName;
    }

    public void setApprovalName(String approvalName) {
        this.approvalName = approvalName;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getApplyName() {
        return applyName;
    }

    public void setApplyName(String applyName) {
        this.applyName = applyName;
    }

    public String getOutTime() {
        return outTime;
    }

    public void setOutTime(String outTime) {
        this.outTime = outTime;
    }

    public String getGunCabinetNo() {
        return gunCabinetNo;
    }

    public void setGunCabinetNo(String gunCabinetNo) {
        this.gunCabinetNo = gunCabinetNo;
    }
}
