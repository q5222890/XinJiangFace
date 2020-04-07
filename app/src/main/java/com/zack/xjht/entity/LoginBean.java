package com.zack.xjht.entity;

public class LoginBean {


    /**
     * result : success
     * user : {"deptName":"研发部门","phoneNumber":"18277570684","sex":"1","cardId":"362201199405041122","policeNo":"1","userName":"若依","userId":1,"email":"ry@163.com"}
     */

    private String result;
    private UserBean user;

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public UserBean getUser() {
        return user;
    }

    public void setUser(UserBean user) {
        this.user = user;
    }

}
