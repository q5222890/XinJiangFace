package com.zack.xjht.entity;

public class TempDataBean {


    /**
     * gunNo : 1005
     * gunType : 95式自动步枪
     * gunCabinetLocationId : 002af7c6ab5e4e06abe63ed39d148590
     * depositor : 张三
     */

    private String gunNo;  //枪支编号
    private String gunType;  //枪支类型
    private String gunCabinetLocationId;  //位置id
    private String depositor;   //存放人
    private String policeNo; //警号
    private String cardNo;   //身份证号
    private String mac;

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

    public String getGunCabinetLocationId() {
        return gunCabinetLocationId;
    }

    public void setGunCabinetLocationId(String gunCabinetLocationId) {
        this.gunCabinetLocationId = gunCabinetLocationId;
    }

    public String getDepositor() {
        return depositor;
    }

    public void setDepositor(String depositor) {
        this.depositor = depositor;
    }

    public String getPoliceNo() {
        return policeNo;
    }

    public void setPoliceNo(String policeNo) {
        this.policeNo = policeNo;
    }

    public String getCardNo() {
        return cardNo;
    }

    public void setCardNo(String cardNo) {
        this.cardNo = cardNo;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }
}
