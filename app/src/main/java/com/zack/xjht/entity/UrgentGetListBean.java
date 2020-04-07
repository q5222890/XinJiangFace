package com.zack.xjht.entity;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;

/**
 * 紧急出警任务领出枪弹数据
 */
@Entity
public class UrgentGetListBean implements Comparable<UrgentGetListBean> {

    @Id(autoincrement = true)
    private Long uId;
    private String gunCabinetLocationId;  //枪弹位置id
    private String objectId;                //枪弹id
    private int outObjectNumber;         //领取数量
    private String locationType;            //领取类型 短枪 长枪 子弹 弹匣
    private String gunNo;
    private int locationNo;                 //位置编号
    private Long taskGetId;           //任务id
    private String objectType;          //类型名称
    private String urgentTaskListId;          //手动生成UUID主键

    @Generated(hash = 1119665781)
    public UrgentGetListBean(Long uId, String gunCabinetLocationId, String objectId,
            int outObjectNumber, String locationType, String gunNo, int locationNo,
            Long taskGetId, String objectType, String urgentTaskListId) {
        this.uId = uId;
        this.gunCabinetLocationId = gunCabinetLocationId;
        this.objectId = objectId;
        this.outObjectNumber = outObjectNumber;
        this.locationType = locationType;
        this.gunNo = gunNo;
        this.locationNo = locationNo;
        this.taskGetId = taskGetId;
        this.objectType = objectType;
        this.urgentTaskListId = urgentTaskListId;
    }

    @Generated(hash = 1726503482)
    public UrgentGetListBean() {
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
    public int compareTo(UrgentGetListBean o) {
        return locationNo - o.locationNo;
    }

    public Long getUId() {
        return this.uId;
    }

    public void setUId(Long uId) {
        this.uId = uId;
    }

    public int getOutObjectNumber() {
        return this.outObjectNumber;
    }

    public void setOutObjectNumber(int outObjectNumber) {
        this.outObjectNumber = outObjectNumber;
    }


    public String getUrgentTaskListId() {
        return this.urgentTaskListId;
    }

    public void setUrgentTaskListId(String urgentTaskListId) {
        this.urgentTaskListId = urgentTaskListId;
    }

    public Long getTaskGetId() {
        return this.taskGetId;
    }

    public void setTaskGetId(Long taskGetId) {
        this.taskGetId = taskGetId;
    }

    public String getGunNo() {
        return this.gunNo;
    }

    public void setGunNo(String gunNo) {
        this.gunNo = gunNo;
    }


}
