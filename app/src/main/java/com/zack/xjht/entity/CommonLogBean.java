package com.zack.xjht.entity;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;

/**
 * 普通操作日志
 */
@Entity
public class CommonLogBean {

    @Id(autoincrement = true)
    private Long id;
    private long addTime;
    private long updateTime;
    private String content;
    private String userId;
    private String userName;
    private String mac;
    private boolean isSync;

    @Generated(hash = 1039938692)
    public CommonLogBean(Long id, long addTime, long updateTime, String content,
            String userId, String userName, String mac, boolean isSync) {
        this.id = id;
        this.addTime = addTime;
        this.updateTime = updateTime;
        this.content = content;
        this.userId = userId;
        this.userName = userName;
        this.mac = mac;
        this.isSync = isSync;
    }
    @Generated(hash = 1834258906)
    public CommonLogBean() {
    }
    public Long getId() {
        return this.id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public long getAddTime() {
        return this.addTime;
    }
    public void setAddTime(long addTime) {
        this.addTime = addTime;
    }
    public long getUpdateTime() {
        return this.updateTime;
    }
    public void setUpdateTime(long updateTime) {
        this.updateTime = updateTime;
    }
    public String getContent() {
        return this.content;
    }
    public void setContent(String content) {
        this.content = content;
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

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }
    public boolean getIsSync() {
        return this.isSync;
    }
    public void setIsSync(boolean isSync) {
        this.isSync = isSync;
    }
}
