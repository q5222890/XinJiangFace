package com.zack.xjht.entity;

public class GoTaskInfoBean {


    /**
     * gunNo : 11
     * objectNumber : 1
     * locationType : shortGun
     * returnNumber : 1
     * gunCabinetNo : W1009
     * objectType : 92_pistol
     * gunCabinetLocationId : 9750a130adbc40ba8faf83f99bfa9ec7
     * locationNo : 1
     * inFinishState : no
     * outFinishState : no
     * objectName : 92式手枪
     * id : b4990053814a45f7ab2710b0e496a003
     * objectId : 890a0652-5d79-46b2-b5f2-8be9c19feae6
     *
     * ammunitionType 弹药类型
     */

    private String gunNo;    //枪支编号
    private int objectNumber;   //  领出子弹数量
    private String locationType;  //位置类型  shortGun：短枪 longGun:长枪
    private int returnNumber;   //归还子弹数量
    private String gunCabinetNo;  //枪柜编号
    private String objectType;  //枪弹类型
    private String gunCabinetLocationId;  //枪柜位置id
    private int locationNo;         //位置编号
    private String inFinishState;   //归还状态 yes or  no
    private String outFinishState;  //领出状态  yes or no
    private String objectName;      //枪弹类型
    private String id;          //任务详情id
    private String objectId;    //枪弹id
    private String policeName;
    private String policeNo;
    private String ammunitionType; //弹药类型

    public String getGunNo() {
        return gunNo;
    }

    public void setGunNo(String gunNo) {
        this.gunNo = gunNo;
    }

    public int getObjectNumber() {
        return objectNumber;
    }

    public void setObjectNumber(int objectNumber) {
        this.objectNumber = objectNumber;
    }

    public String getLocationType() {
        return locationType;
    }

    public void setLocationType(String locationType) {
        this.locationType = locationType;
    }

    public int getReturnNumber() {
        return returnNumber;
    }

    public void setReturnNumber(int returnNumber) {
        this.returnNumber = returnNumber;
    }

    public String getGunCabinetNo() {
        return gunCabinetNo;
    }

    public void setGunCabinetNo(String gunCabinetNo) {
        this.gunCabinetNo = gunCabinetNo;
    }

    public String getObjectType() {
        return objectType;
    }

    public void setObjectType(String objectType) {
        this.objectType = objectType;
    }

    public String getGunCabinetLocationId() {
        return gunCabinetLocationId;
    }

    public void setGunCabinetLocationId(String gunCabinetLocationId) {
        this.gunCabinetLocationId = gunCabinetLocationId;
    }

    public int getLocationNo() {
        return locationNo;
    }

    public void setLocationNo(int locationNo) {
        this.locationNo = locationNo;
    }

    public String getInFinishState() {
        return inFinishState;
    }

    public void setInFinishState(String inFinishState) {
        this.inFinishState = inFinishState;
    }

    public String getOutFinishState() {
        return outFinishState;
    }

    public void setOutFinishState(String outFinishState) {
        this.outFinishState = outFinishState;
    }

    public String getObjectName() {
        return objectName;
    }

    public void setObjectName(String objectName) {
        this.objectName = objectName;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    public String getPoliceName() {
        return policeName;
    }

    public void setPoliceName(String policeName) {
        this.policeName = policeName;
    }

    public String getPoliceNo() {
        return policeNo;
    }

    public void setPoliceNo(String policeNo) {
        this.policeNo = policeNo;
    }

    public String getAmmunitionType() {
        return ammunitionType;
    }

    public void setAmmunitionType(String ammunitionType) {
        this.ammunitionType = ammunitionType;
    }

}
