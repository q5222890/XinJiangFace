package com.zack.xjht.entity;

/**
 * 紧急任务归还提交数据实体类
 */
public class UrgentBackBean {


    /**
     * id : 1afffe8f6c1246cd9ffaed8045a0b134
     * gunCabinetLocationId : 1063986822414248802568966
     * inObjectNumber : 1
     * locationType : shortGun
     * objectId : 3
     */

    private String id;  //清单id
    private String gunCabinetLocationId;  //位置id
    private String inObjectNumber;   //存入数量
    private String locationType;        //位置类型
    private String objectId;            //枪弹id

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

    public String getInObjectNumber() {
        return inObjectNumber;
    }

    public void setInObjectNumber(String inObjectNumber) {
        this.inObjectNumber = inObjectNumber;
    }

    public String getLocationType() {
        return locationType;
    }

    public void setLocationType(String locationType) {
        this.locationType = locationType;
    }

    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }
}
