package com.zack.xjht.entity;

public class UrgentTaskInfoBean implements Comparable<UrgentTaskInfoBean> {

    /**
     * gunCabinetLocationId : 40d896eb6afb4648bdd37a825a37a5c0
     * locationNo : 21
     * typeName : 64式手枪子弹
     * locationType : ammunition
     * id : 8ee6b3ac2ea641c7bf72cbe5d9a81e72
     * outObjectNumber : 10
     * objectId : 64_pistol_bullet
     */

    private String gunCabinetLocationId;  //枪柜编号
    private int locationNo;         //位置编号
    private String typeName;        //枪弹类型
    private String locationType;    //位置类型
    private String id;
    private int outObjectNumber;    //归还数量
    private String objectId;        //枪弹id

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

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public String getLocationType() {
        return locationType;
    }

    public void setLocationType(String locationType) {
        this.locationType = locationType;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getOutObjectNumber() {
        return outObjectNumber;
    }

    public void setOutObjectNumber(int outObjectNumber) {
        this.outObjectNumber = outObjectNumber;
    }

    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    @Override
    public int compareTo(UrgentTaskInfoBean o) {
        return locationNo -o.locationNo;
    }
}
