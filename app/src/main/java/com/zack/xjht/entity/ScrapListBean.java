package com.zack.xjht.entity;

public class ScrapListBean {


    /**
     * id : 65b5764fb1d847f78bdbed1c5cff2523
     * gunCabinetLocationId : 65b5764fb1d847123bdbed1c5cff2523
     * objectId : 65b5764fb1d847123bdbed452cff2523
     * gunNo : 1
     * outFinishState : yes
     * inFinishState :
     * objectNumber : 1
     * state : scrap
     */

    private String id;
    private String gunCabinetLocationId;
    private String objectId;
    private String gunNo;
    private String outFinishState;
    private String inFinishState;
    private int objectNumber;
    private String state;

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

    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    public String getGunNo() {
        return gunNo;
    }

    public void setGunNo(String gunNo) {
        this.gunNo = gunNo;
    }

    public String getOutFinishState() {
        return outFinishState;
    }

    public void setOutFinishState(String outFinishState) {
        this.outFinishState = outFinishState;
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

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }
}
