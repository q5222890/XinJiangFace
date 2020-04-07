package com.zack.xjht.event;

/**
 * Created by Administrator on 2017-08-04.
 */

public class PostEvent {

    String message;

    public PostEvent(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
