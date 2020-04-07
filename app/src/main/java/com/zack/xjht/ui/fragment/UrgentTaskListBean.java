package com.zack.xjht.ui.fragment;

public class UrgentTaskListBean {

    private String gunCabinetLocationId;  //枪弹位置id
    private String objectId;                //枪弹id
    private String outObjectNumber;         //领取数量
    private String locationType;            //领取类型

    public String getGunCabinetLocationId() {
        return gunCabinetLocationId;
    }

    public void setGunCabinetLocationId(String gunCabinetLocationId) {
        this.gunCabinetLocationId = gunCabinetLocationId;
    }

    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    public String getOutObjectNumber() {
        return outObjectNumber;
    }

    public void setOutObjectNumber(String outObjectNumber) {
        this.outObjectNumber = outObjectNumber;
    }

    public String getLocationType() {
        return locationType;
    }

    public void setLocationType(String locationType) {
        this.locationType = locationType;
    }

}
