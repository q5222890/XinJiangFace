package com.zack.xjht.entity;

import java.util.Date;

public class UploadOpenMsg {

    private String cID; //专用柜唯一编号
    private String identityNumber1; //第一位开柜人
    private String identityNumber2; //第二位开柜人
    private Date time; //开柜时间
    private int qzsl; //领取/还枪数量
    private String type; //开柜类型 1.授权开柜， 2 异常开柜
    private int zdlx; //领取/归还子弹类型
    private int zdsl; //领取/归还子弹数量


    public String getcID() {
        return cID;
    }

    public void setcID(String cID) {
        this.cID = cID;
    }

    public String getIdentityNumber1() {
        return identityNumber1;
    }

    public void setIdentityNumber1(String identityNumber1) {
        this.identityNumber1 = identityNumber1;
    }

    public String getIdentityNumber2() {
        return identityNumber2;
    }

    public void setIdentityNumber2(String identityNumber2) {
        this.identityNumber2 = identityNumber2;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public int getQzsl() {
        return qzsl;
    }

    public void setQzsl(int qzsl) {
        this.qzsl = qzsl;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getZdlx() {
        return zdlx;
    }

    public void setZdlx(int zdlx) {
        this.zdlx = zdlx;
    }

    public int getZdsl() {
        return zdsl;
    }

    public void setZdsl(int zdsl) {
        this.zdsl = zdsl;
    }
}
