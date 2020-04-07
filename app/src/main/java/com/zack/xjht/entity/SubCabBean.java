package com.zack.xjht.entity;


import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;

import java.io.Serializable;

/**
 * 枪支弹药位置实体类
 */
@Entity
public class SubCabBean implements Comparable<SubCabBean> {

    @Id(autoincrement = true)
    private Long sId;
    private int locationNo;        //位置编号
    private String isUse;         //是否存放  yes or no
    private String gunNo;         //枪支编号
    private int objectNumber;     //子弹数量
    private String locationType;  //枪弹类型  shortGun：短枪  longGun:长枪 ammunition:弹药
    private String objectName;      //枪弹名称
    private String id;              //位置id
    private String objectId;        //枪弹id
    private String gunState;        //枪支状态  in：在库  out：出库 scrap:报废  lol hahahaha
    private String isTemporary;     //是否临时存放枪支   yes or no
    private Long cabId;  //对应的枪柜id
    private int getNum; //领取数量
    private String userId; //警员id
    private String userName; //警员姓名

    @Generated(hash = 1151586115)
    public SubCabBean(Long sId, int locationNo, String isUse, String gunNo,
            int objectNumber, String locationType, String objectName, String id,
            String objectId, String gunState, String isTemporary, Long cabId,
            int getNum, String userId, String userName) {
        this.sId = sId;
        this.locationNo = locationNo;
        this.isUse = isUse;
        this.gunNo = gunNo;
        this.objectNumber = objectNumber;
        this.locationType = locationType;
        this.objectName = objectName;
        this.id = id;
        this.objectId = objectId;
        this.gunState = gunState;
        this.isTemporary = isTemporary;
        this.cabId = cabId;
        this.getNum = getNum;
        this.userId = userId;
        this.userName = userName;
    }

    @Generated(hash = 175108957)
    public SubCabBean() {
    }

    public int getLocationNo() {
        return locationNo;
    }

    public void setLocationNo(int locationNo) {
        this.locationNo = locationNo;
    }

    public String getIsUse() {
        return isUse;
    }

    public void setIsUse(String isUse) {
        this.isUse = isUse;
    }

    public String getGunNo() {
        return gunNo;
    }

    public void setGunNo(String gunNo) {
        this.gunNo = gunNo;
    }

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

    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    public String getGunState() {
        return gunState;
    }

    public void setGunState(String gunState) {
        this.gunState = gunState;
    }

    public String getIsTemporary() {
        return isTemporary;
    }

    public void setIsTemporary(String isTemporary) {
        this.isTemporary = isTemporary;
    }

    public Long getCabId() {
        return cabId;
    }

    public void setCabId(Long cabId) {
        this.cabId = cabId;
    }

    @Override
    public int compareTo(SubCabBean o) {
        return locationNo -o.locationNo;
    }

    public Long getSId() {
        return this.sId;
    }

    public void setSId(Long sId) {
        this.sId = sId;
    }

    public int getGetNum() {
        return this.getNum;
    }

    public void setGetNum(int getNum) {
        this.getNum = getNum;
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
