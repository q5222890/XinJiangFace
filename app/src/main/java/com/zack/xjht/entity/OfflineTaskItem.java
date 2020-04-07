package com.zack.xjht.entity;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;

@Entity
public class OfflineTaskItem {

    @Id(autoincrement = true)
    private Long id;
    private String locationId; //位置id
    private String objectId;  //枪弹id
    private String locationType; //位置类型
    private String objectType; //枪弹类型
    private int objectNum;  //领取数量
    private int backNum;   //归还数量
    private String gunNo;  //枪支编号
    private int status; //状态 1.已领取 2.已归还
    private int locationNo; //位置编号
    private long taskId;   //所属任务id
    private long subCabId; //枪弹数据id
    private String userId; //警员id
    private String userName; //警员姓名
    @Generated(hash = 1022630338)
    public OfflineTaskItem(Long id, String locationId, String objectId,
            String locationType, String objectType, int objectNum, int backNum,
            String gunNo, int status, int locationNo, long taskId, long subCabId,
            String userId, String userName) {
        this.id = id;
        this.locationId = locationId;
        this.objectId = objectId;
        this.locationType = locationType;
        this.objectType = objectType;
        this.objectNum = objectNum;
        this.backNum = backNum;
        this.gunNo = gunNo;
        this.status = status;
        this.locationNo = locationNo;
        this.taskId = taskId;
        this.subCabId = subCabId;
        this.userId = userId;
        this.userName = userName;
    }
    @Generated(hash = 651179584)
    public OfflineTaskItem() {
    }
    public Long getId() {
        return this.id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getLocationId() {
        return this.locationId;
    }
    public void setLocationId(String locationId) {
        this.locationId = locationId;
    }
    public String getObjectId() {
        return this.objectId;
    }
    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }
    public String getLocationType() {
        return this.locationType;
    }
    public void setLocationType(String locationType) {
        this.locationType = locationType;
    }
    public String getObjectType() {
        return this.objectType;
    }
    public void setObjectType(String objectType) {
        this.objectType = objectType;
    }
    public int getObjectNum() {
        return this.objectNum;
    }
    public void setObjectNum(int objectNum) {
        this.objectNum = objectNum;
    }
    public String getGunNo() {
        return this.gunNo;
    }
    public void setGunNo(String gunNo) {
        this.gunNo = gunNo;
    }
    public int getStatus() {
        return this.status;
    }
    public void setStatus(int status) {
        this.status = status;
    }
    public int getLocationNo() {
        return this.locationNo;
    }
    public void setLocationNo(int locationNo) {
        this.locationNo = locationNo;
    }
    public long getTaskId() {
        return this.taskId;
    }
    public void setTaskId(long taskId) {
        this.taskId = taskId;
    }
    public int getBackNum() {
        return this.backNum;
    }
    public void setBackNum(int backNum) {
        this.backNum = backNum;
    }
    public long getSubCabId() {
        return this.subCabId;
    }
    public void setSubCabId(long subCabId) {
        this.subCabId = subCabId;
    }
    public String getUserId() {
        return this.userId;
    }
    public void setUserId(String userId) {
        this.userId = userId;
    }
    public String getUserName() {
        return this.userName;
    }
    public void setUserName(String userName) {
        this.userName = userName;
    }

}
