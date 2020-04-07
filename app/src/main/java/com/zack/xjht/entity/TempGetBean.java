package com.zack.xjht.entity;

public class TempGetBean {


    private int locationNo;  //位置编号
    private String gunCabinetLocationId; //位置ID
    private String gunNo; //枪支编号
    private String gunType;  //枪支类型
    private String createTime;  //创建时间
    private String depositor;  //存放人姓名
    private String id;       //任务id

    public int getLocationNo() {
        return locationNo;
    }

    public void setLocationNo(int locationNo) {
        this.locationNo = locationNo;
    }

    public String getGunCabinetLocationId() {
        return gunCabinetLocationId;
    }

    public void setGunCabinetLocationId(String gunCabinetLocationId) {
        this.gunCabinetLocationId = gunCabinetLocationId;
    }

    public String getGunNo() {
        return gunNo;
    }

    public void setGunNo(String gunNo) {
        this.gunNo = gunNo;
    }

    public String getGunType() {
        return gunType;
    }

    public void setGunType(String gunType) {
        this.gunType = gunType;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getDepositor() {
        return depositor;
    }

    public void setDepositor(String depositor) {
        this.depositor = depositor;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
