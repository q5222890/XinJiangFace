package com.zack.xjht.entity;

import java.util.List;

public class PostGetDataBean {


    /**
     * policeTaskId : 782d1ca8f09e45f9b83f08b0d0519f1a
     * operation : in
     * listPoliceTaskList : [{"objectNumber":1,"locationType":"shortGun","returnNumber":1,"gunCabinetLocationId":"9750a130adbc40ba8faf83f99bfa9ec7","id":"b4990053814a45f7ab2710b0e496a003","objectId":"890a0652-5d79-46b2-b5f2-8be9c19feae6"},{"objectNumber":1,"locationType":"shortGun","returnNumber":1,"gunCabinetLocationId":"dfd0a6b1d2054c68accfcf9963a9b634","id":"f0cdc9b3fd994d9987c8d4e6706e90ce","objectId":"60757e59-3ae8-4023-815c-6e6a7131e309"}]
     */

    private String policeTaskId;  //任务id
    private String operation;       //in:归还 out:取出
    private List<ListPoliceTaskListBean> listPoliceTaskList; //

    public String getPoliceTaskId() {
        return policeTaskId;
    }

    public void setPoliceTaskId(String policeTaskId) {
        this.policeTaskId = policeTaskId;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public List<ListPoliceTaskListBean> getListPoliceTaskList() {
        return listPoliceTaskList;
    }

    public void setListPoliceTaskList(List<ListPoliceTaskListBean> listPoliceTaskList) {
        this.listPoliceTaskList = listPoliceTaskList;
    }

    public static class ListPoliceTaskListBean {

        private int objectNumber;  //领出数量
        private String locationType;  //位置
        private int returnNumber;   //归还数量
        private String gunCabinetLocationId;  //枪柜id
        private String id;              //清单id
        private String objectId;        //枪弹id
        private String ammunitionType;

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

        public String getGunCabinetLocationId() {
            return gunCabinetLocationId;
        }

        public void setGunCabinetLocationId(String gunCabinetLocationId) {
            this.gunCabinetLocationId = gunCabinetLocationId;
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

        public String getAmmunitionType() {
            return ammunitionType;
        }

        public void setAmmunitionType(String ammunitionType) {
            this.ammunitionType = ammunitionType;
        }
    }
}
