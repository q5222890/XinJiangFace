package com.zack.xjht.entity;

/**
 * 枪弹入库任务清单实体类
 */
public class InStoreListBean implements Comparable<InStoreListBean>{


    private int locationNo;  //位置编号
    private String id;          //id
    private String gunCabinetLocationId; //位置id
    private String gunNo;       //枪支编号
    private String inFinishState;  //是否入库  yes or no
    private int objectNumber;   //弹药数量
    private String objectName;  //枪弹名称
    private String gunCabinetNo;   //枪柜编号
    private String locationType;   //位置类型
    private String objectType;     //枪弹类型
    private String outFinishState; //是否取出  yes or no
    private String objectId;
    private String state;  //in在库 out出库 scrap报废
    private String gunId;
    private String gunMac;

    public int getLocationNo() {
        return locationNo;
    }

    public void setLocationNo(int locationNo) {
        this.locationNo = locationNo;
    }

    public String getGunNo() {
        return gunNo;
    }

    public void setGunNo(String gunNo) {
        this.gunNo = gunNo;
    }

    public String getInFinishState() {
        return inFinishState;
    }

    public void setInFinishState(String inFinishState) {
        this.inFinishState = inFinishState;
    }

    public int getObjectNumber() {
        return objectNumber;
    }

    public void setObjectNumber(int objectNumber) {
        this.objectNumber = objectNumber;
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

    public String getGunCabinetNo() {
        return gunCabinetNo;
    }

    public void setGunCabinetNo(String gunCabinetNo) {
        this.gunCabinetNo = gunCabinetNo;
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

    public String getOutFinishState() {
        return outFinishState;
    }

    public void setOutFinishState(String outFinishState) {
        this.outFinishState = outFinishState;
    }

    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getGunId() {
        return gunId;
    }

    public void setGunId(String gunId) {
        this.gunId = gunId;
    }

    public String getGunMac() {
        return gunMac;
    }

    public void setGunMac(String gunMac) {
        this.gunMac = gunMac;
    }

    @Override
    public int compareTo(InStoreListBean o) {
        return locationNo -o.getLocationNo();
    }
}
