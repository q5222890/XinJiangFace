package com.zack.xjht.entity;


import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;

import java.io.Serializable;
import org.greenrobot.greendao.annotation.Generated;

/**
 * 用户实体类
 */
@Entity
public class UserBean implements Serializable {
    public static final long serialVersionUID =1L;

    @Id(autoincrement = true)
    private Long uId;            //id
    private String deptName;    //部门名称
    private String phoneNumber; //电话号码
    private String sex;         //性别
    private String cardId;      //身份证号
    private String policeNo;    //警号
    private String userName;    //姓名
    private int userId;         //用户id
    private String email;       //邮箱
    private String roleKeys;    //权限值

    @Generated(hash = 727248200)
    public UserBean(Long uId, String deptName, String phoneNumber, String sex,
            String cardId, String policeNo, String userName, int userId,
            String email, String roleKeys) {
        this.uId = uId;
        this.deptName = deptName;
        this.phoneNumber = phoneNumber;
        this.sex = sex;
        this.cardId = cardId;
        this.policeNo = policeNo;
        this.userName = userName;
        this.userId = userId;
        this.email = email;
        this.roleKeys = roleKeys;
    }

    @Generated(hash = 1203313951)
    public UserBean() {
    }

    public String getDeptName() {
        return deptName;
    }

    public void setDeptName(String deptName) {
        this.deptName = deptName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getCardId() {
        return cardId;
    }

    public void setCardId(String cardId) {
        this.cardId = cardId;
    }

    public String getPoliceNo() {
        return policeNo;
    }

    public void setPoliceNo(String policeNo) {
        this.policeNo = policeNo;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Long getUId() {
        return this.uId;
    }

    public void setUId(Long uId) {
        this.uId = uId;
    }

    public String getRoleKeys() {
        return this.roleKeys;
    }

    public void setRoleKeys(String roleKeys) {
        this.roleKeys = roleKeys;
    }

}
