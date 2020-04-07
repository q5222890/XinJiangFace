package com.zack.xjht.event;


/**
 * eventBus串口数据消息
 */

public class BulletNumEvent {

    private int address; //地址
    private int count; //数量
    private String message; //状态消息

    public BulletNumEvent(int address, int count, String message) {
        this.address = address;
        this.count =count;
        this.message = message;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getAddress() {
        return address;
    }

    public void setAddress(int address) {
        this.address = address;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}
