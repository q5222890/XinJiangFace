package com.zack.xjht.event;

/**
 * Created by Administrator on 2017-08-04.
 */

public class PowerStatusEvent {

    String message;

    public PowerStatusEvent(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
