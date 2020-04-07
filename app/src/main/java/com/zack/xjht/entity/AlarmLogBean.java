package com.zack.xjht.entity;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;

@Entity
public class AlarmLogBean {


    @Id(autoincrement = true)
    private Long id;
    private String gunCabinetId; //枪柜id
    /**
     * 1.震动报警
     * 2.断电报警
     * 3.备用钥匙开启报警
     * 4.开门超时报警
     * 5.网络断开
     * 6.温湿度异常报警
     * 7.酒精检测异常报警
     * */
    private String warningType; //报警类型
    private String warningTime; //报警时间
    private String relieveWarningTime;  //解除时间
    private String relieveId;   //解除人id
    private String relieveName;   //解除人姓名
    private String warningContent; //报警内容
    private String warningState;    //报警状态  1.未解除  2.已解除
    private boolean isSync; //是否同步到后台

    @Generated(hash = 1380510217)
    public AlarmLogBean(Long id, String gunCabinetId, String warningType,
            String warningTime, String relieveWarningTime, String relieveId,
            String relieveName, String warningContent, String warningState,
            boolean isSync) {
        this.id = id;
        this.gunCabinetId = gunCabinetId;
        this.warningType = warningType;
        this.warningTime = warningTime;
        this.relieveWarningTime = relieveWarningTime;
        this.relieveId = relieveId;
        this.relieveName = relieveName;
        this.warningContent = warningContent;
        this.warningState = warningState;
        this.isSync = isSync;
    }

    @Generated(hash = 1667494094)
    public AlarmLogBean() {
    }

    public String getGunCabinetId() {
        return gunCabinetId;
    }

    public void setGunCabinetId(String gunCabinetId) {
        this.gunCabinetId = gunCabinetId;
    }

    public String getWarningType() {
        return warningType;
    }

    public void setWarningType(String warningType) {
        this.warningType = warningType;
    }

    public String getWarningTime() {
        return warningTime;
    }

    public void setWarningTime(String warningTime) {
        this.warningTime = warningTime;
    }

    public String getRelieveWarningTime() {
        return relieveWarningTime;
    }

    public void setRelieveWarningTime(String relieveWarningTime) {
        this.relieveWarningTime = relieveWarningTime;
    }

    public String getRelieveId() {
        return relieveId;
    }

    public void setRelieveId(String relieveId) {
        this.relieveId = relieveId;
    }

    public String getWarningContent() {
        return warningContent;
    }

    public void setWarningContent(String warningContent) {
        this.warningContent = warningContent;
    }

    public String getWarningState() {
        return warningState;
    }

    public void setWarningState(String warningState) {
        this.warningState = warningState;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRelieveName() {
        return this.relieveName;
    }

    public void setRelieveName(String relieveName) {
        this.relieveName = relieveName;
    }

    public boolean getIsSync() {
        return this.isSync;
    }

    public void setIsSync(boolean isSync) {
        this.isSync = isSync;
    }

}
