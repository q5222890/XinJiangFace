package com.zack.xjht.entity;

/**
 * 枪弹入库任务清单实体类
 */
public class InStoreBean {


    /**
     * id :
     * gunCabinetLocationId :
     * locationType :
     * objectType : 54-shortGun
     * objectNumber : 1
     * gunNo : 1
     */

    private String id;
    private String gunCabinetLocationId;
    private String locationType;
    private String objectType;
    private int objectNumber;
    private String gunNo;
    private String gunMac;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getGunCabinetLocationId() {
        return gunCabinetLocationId;
    }

    public void setGunCabinetLocationId(String gunCabinetLocationId) {
        this.gunCabinetLocationId = gunCabinetLocationId;
    }

    public String getLocationType() {
        return locationType;
    }

    public void setLocationType(String locationType) {
        this.locationType = locationType;
    }

    public String getObjectType() {
        return objectType;
    }

    public void setObjectType(String objectType) {
        this.objectType = objectType;
    }

    public int getObjectNumber() {
        return objectNumber;
    }

    public void setObjectNumber(int objectNumber) {
        this.objectNumber = objectNumber;
    }

    public String getGunNo() {
        return gunNo;
    }

    public void setGunNo(String gunNo) {
        this.gunNo = gunNo;
    }

    public String getGunMac() {
        return gunMac;
    }

    public void setGunMac(String gunMac) {
        this.gunMac = gunMac;
    }
}
