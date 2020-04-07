package com.zack.xjht.entity;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;

/**
 * 用户生物特征类
 */
@Entity
public class UserBiosBean {

    /**
     * biometricsKey : 图片base64转码
     * biometricsPart : 4
     * id : f34caf67fde04c82bef55443fca7f06c
     * userName : 若依
     * biometricsType : 1
     * userId : 1
     */
    @Id(autoincrement = true)
    private Long bId;
    private String biometricsKey;  //特征base64字符串
    /**
     1、  左手小指
     2、  左手无名指
     3、  左手中指
     4、  左手食指
     5、  左手大拇指
     6、  右手大拇指
     7、  右手食指
     8、  右手中指
     9、  右手无名指
     10、 右手小指
     13、 其它
     **/
    private String biometricsPart;  //注册位置
    private String id;              //特征id
    private String userName;        //用户名称
    private String biometricsType;  //设备类型  1.指纹 2.指静脉 3.虹膜 4.人脸 5.掌静脉
    private int userId;             //用户id
    private int biometricsNumber;   //生物特征id

    @Generated(hash = 893997595)
    public UserBiosBean(Long bId, String biometricsKey, String biometricsPart,
            String id, String userName, String biometricsType, int userId,
            int biometricsNumber) {
        this.bId = bId;
        this.biometricsKey = biometricsKey;
        this.biometricsPart = biometricsPart;
        this.id = id;
        this.userName = userName;
        this.biometricsType = biometricsType;
        this.userId = userId;
        this.biometricsNumber = biometricsNumber;
    }

    @Generated(hash = 1723016267)
    public UserBiosBean() {
    }


    public String getBiometricsKey() {
        return biometricsKey;
    }

    public void setBiometricsKey(String biometricsKey) {
        this.biometricsKey = biometricsKey;
    }

    public String getBiometricsPart() {
        return biometricsPart;
    }

    public void setBiometricsPart(String biometricsPart) {
        this.biometricsPart = biometricsPart;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getBiometricsType() {
        return biometricsType;
    }

    public void setBiometricsType(String biometricsType) {
        this.biometricsType = biometricsType;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {

        this.userId = userId;
    }

    public int getBiometricsNumber() {
        return biometricsNumber;
    }

    public void setBiometricsNumber(int biometricsNumber) {
        this.biometricsNumber = biometricsNumber;
    }

    public Long getBId() {
        return this.bId;
    }

    public void setBId(Long bId) {
        this.bId = bId;
    }


}
