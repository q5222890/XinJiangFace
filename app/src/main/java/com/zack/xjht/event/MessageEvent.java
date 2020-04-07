package com.zack.xjht.event;

import com.zack.xjht.entity.UserBean;

import java.util.Map;

/**
 * eventBus消息
 */

public class MessageEvent {

    private  UserBean apply;
    private String message;
    private Map<String, Object> param;
    private UserBean approve;

    public MessageEvent(String message) {
        this.message = message;
    }

    public MessageEvent(String message, UserBean applyPolice, UserBean approve) {
        this.message = message;
        this.apply =applyPolice;
        this.approve =approve;
    }

    public MessageEvent(Map<String, Object> param) {
        this.param = param;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Map<String, Object> getParam() {
        return param;
    }

    public void setParam(Map<String, Object> param) {
        this.param = param;
    }

    public UserBean getApply() {
        return apply;
    }

    public void setApply(UserBean apply) {
        this.apply = apply;
    }

    public UserBean getApprove() {
        return approve;
    }

    public void setApprove(UserBean approve) {
        this.approve = approve;
    }
}
