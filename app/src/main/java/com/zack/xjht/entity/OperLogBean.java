package com.zack.xjht.entity;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;

@Entity
public class OperLogBean {

    @Id(autoincrement = true)
    private Long id;
    private String gunManagementId; //管理员id
    private String gunManagementName; //管理员姓名
    private String approveId; //领导id
    private String approveName; //领导姓名
    private String policeId;    //民警id
    private String policeName;  //民警姓名
    private String loseCount;  //损耗数量
    private String inTime;    //入库时间
    private String inCount;   //入库数量
    private String outTime;   //出库时间
    private String outCount;  //出库数量
    private String objectId;  //枪弹id
    private String gunCabinetLocationId;//位置id
    private String gunCabinetId; //枪柜id
    private String status;  //1.已领取 2.已归还
    private int uploadStatus;//0.未上传 1.领取上传完成 2.归还上传完成
    private long taskId;
    private long taskItemId;
    private String type;         //类型
    private String typeName;    //枪弹类型
    private String gunNo;       //枪支编号


    @Generated(hash = 1249953147)
    public OperLogBean(Long id, String gunManagementId, String gunManagementName,
            String approveId, String approveName, String policeId,
            String policeName, String loseCount, String inTime, String inCount,
            String outTime, String outCount, String objectId,
            String gunCabinetLocationId, String gunCabinetId, String status,
            int uploadStatus, long taskId, long taskItemId, String type,
            String typeName, String gunNo) {
        this.id = id;
        this.gunManagementId = gunManagementId;
        this.gunManagementName = gunManagementName;
        this.approveId = approveId;
        this.approveName = approveName;
        this.policeId = policeId;
        this.policeName = policeName;
        this.loseCount = loseCount;
        this.inTime = inTime;
        this.inCount = inCount;
        this.outTime = outTime;
        this.outCount = outCount;
        this.objectId = objectId;
        this.gunCabinetLocationId = gunCabinetLocationId;
        this.gunCabinetId = gunCabinetId;
        this.status = status;
        this.uploadStatus = uploadStatus;
        this.taskId = taskId;
        this.taskItemId = taskItemId;
        this.type = type;
        this.typeName = typeName;
        this.gunNo = gunNo;
    }

    @Generated(hash = 636995490)
    public OperLogBean() {
    }

    public Long getId() {
        return this.id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getGunManagementId() {
        return this.gunManagementId;
    }
    public void setGunManagementId(String gunManagementId) {
        this.gunManagementId = gunManagementId;
    }
    public String getApproveId() {
        return this.approveId;
    }
    public void setApproveId(String approveId) {
        this.approveId = approveId;
    }
    public String getPoliceId() {
        return this.policeId;
    }
    public void setPoliceId(String policeId) {
        this.policeId = policeId;
    }
    public String getLoseCount() {
        return this.loseCount;
    }
    public void setLoseCount(String loseCount) {
        this.loseCount = loseCount;
    }
    public String getInTime() {
        return this.inTime;
    }
    public void setInTime(String inTime) {
        this.inTime = inTime;
    }
    public String getInCount() {
        return this.inCount;
    }
    public void setInCount(String inCount) {
        this.inCount = inCount;
    }
    public String getOutTime() {
        return this.outTime;
    }
    public void setOutTime(String outTime) {
        this.outTime = outTime;
    }
    public String getOutCount() {
        return this.outCount;
    }
    public void setOutCount(String outCount) {
        this.outCount = outCount;
    }
    public String getObjectId() {
        return this.objectId;
    }
    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }
    public String getGunCabinetLocationId() {
        return this.gunCabinetLocationId;
    }
    public void setGunCabinetLocationId(String gunCabinetLocationId) {
        this.gunCabinetLocationId = gunCabinetLocationId;
    }
    public String getGunCabinetId() {
        return this.gunCabinetId;
    }
    public void setGunCabinetId(String gunCabinetId) {
        this.gunCabinetId = gunCabinetId;
    }
    public String getStatus() {
        return this.status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
    public int getUploadStatus() {
        return this.uploadStatus;
    }
    public void setUploadStatus(int uploadStatus) {
        this.uploadStatus = uploadStatus;
    }
    public long getTaskId() {
        return this.taskId;
    }
    public void setTaskId(long taskId) {
        this.taskId = taskId;
    }
    public long getTaskItemId() {
        return this.taskItemId;
    }
    public void setTaskItemId(long taskItemId) {
        this.taskItemId = taskItemId;
    }
    public String getGunManagementName() {
        return this.gunManagementName;
    }
    public void setGunManagementName(String gunManagementName) {
        this.gunManagementName = gunManagementName;
    }
    public String getApproveName() {
        return this.approveName;
    }
    public void setApproveName(String approveName) {
        this.approveName = approveName;
    }
    public String getPoliceName() {
        return this.policeName;
    }
    public void setPoliceName(String policeName) {
        this.policeName = policeName;
    }
    public String getType() {
        return this.type;
    }
    public void setType(String type) {
        this.type = type;
    }
    public String getGunNo() {
        return this.gunNo;
    }
    public void setGunNo(String gunNo) {
        this.gunNo = gunNo;
    }

    public String getTypeName() {
        return this.typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }


}
