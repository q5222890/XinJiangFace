package com.zack.xjht.entity;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;

/**
 * 紧急出警任务归还枪弹数据
 */
@Entity
public class UrgentBackListBean implements Comparable<UrgentBackListBean> {

    @Id(autoincrement = true)
    private Long tId;
    private String gunCabinetLocationId;  //枪弹位置id
    private String objectId;                //枪弹id
    private int inObjectNumber;                   //归还数量
    private String locationType;            //领取类型 短枪 长枪 子弹 弹匣
    private int locationNo;                 //位置编号
    private Long taskBackId;           //任务id
    private String objectType;          //类型名称
    private String urgentTaskListId;          //生成UUID主键


    @Generated(hash = 1249554988)
    public UrgentBackListBean(Long tId, String gunCabinetLocationId,
            String objectId, int inObjectNumber, String locationType,
            int locationNo, Long taskBackId, String objectType,
            String urgentTaskListId) {
        this.tId = tId;
        this.gunCabinetLocationId = gunCabinetLocationId;
        this.objectId = objectId;
        this.inObjectNumber = inObjectNumber;
        this.locationType = locationType;
        this.locationNo = locationNo;
        this.taskBackId = taskBackId;
        this.objectType = objectType;
        this.urgentTaskListId = urgentTaskListId;
    }

    @Generated(hash = 1305376115)
    public UrgentBackListBean() {
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

    public String getLocationType() {
        return locationType;
    }

    public void setLocationType(String locationType) {
        this.locationType = locationType;
    }

    public int getLocationNo() {
        return this.locationNo;
    }

    public void setLocationNo(int locationNo) {
        this.locationNo = locationNo;
    }

    public String getObjectType() {
        return this.objectType;
    }

    public void setObjectType(String objectType) {
        this.objectType = objectType;
    }

    @Override
    public int compareTo(UrgentBackListBean o) {
        return locationNo - o.locationNo;
    }

    public int getInObjectNumber() {
        return this.inObjectNumber;
    }

    public void setInObjectNumber(int inObjectNumber) {
        this.inObjectNumber = inObjectNumber;
    }


    public Long getTId() {
        return this.tId;
    }

    public void setTId(Long tId) {
        this.tId = tId;
    }

    public String getUrgentTaskListId() {
        return this.urgentTaskListId;
    }

    public void setUrgentTaskListId(String urgentTaskListId) {
        this.urgentTaskListId = urgentTaskListId;
    }

    public Long getTaskBackId() {
        return this.taskBackId;
    }

    public void setTaskBackId(Long taskBackId) {
        this.taskBackId = taskBackId;
    }


}
