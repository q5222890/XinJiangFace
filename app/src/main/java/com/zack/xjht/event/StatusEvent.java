package com.zack.xjht.event;


/**
 * eventBus串口数据消息
 */

public class StatusEvent {

    private int address; //地址
    private int category; // 1.开关状态 2.在位状态
    private int status;  //0.打开 1.关闭 2 异常  0.离位 1.在位
    private String message; //状态消息

    public StatusEvent(int address, int cat, int status, String message) {
        this.address = address;
        this.category =cat;
        this.status = status;
        this.message = message;
    }

    public int getCategory() {
        return category;
    }

    public void setCategory(int category) {
        this.category = category;
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

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
