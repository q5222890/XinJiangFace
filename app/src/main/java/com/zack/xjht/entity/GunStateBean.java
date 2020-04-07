package com.zack.xjht.entity;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;

@Entity
public class GunStateBean {

    @Id
    private Long id;
    private int position; //所在位置
    private int gunState; //0.枪在位 1.枪不在位
    private int lockState; //0.枪锁打开 1.枪锁关闭 2.枪锁开关异常（开关时卡住）
    @Generated(hash = 705377549)
    public GunStateBean(Long id, int position, int gunState, int lockState) {
        this.id = id;
        this.position = position;
        this.gunState = gunState;
        this.lockState = lockState;
    }
    @Generated(hash = 664740026)
    public GunStateBean() {
    }
    public Long getId() {
        return this.id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public int getPosition() {
        return this.position;
    }
    public void setPosition(int position) {
        this.position = position;
    }
    public int getGunState() {
        return this.gunState;
    }
    public void setGunState(int gunState) {
        this.gunState = gunState;
    }
    public int getLockState() {
        return this.lockState;
    }
    public void setLockState(int lockState) {
        this.lockState = lockState;
    }

}
