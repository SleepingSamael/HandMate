package com.qslll.expandingpager.model.users;

import android.app.Application;

/**
 * 用户信息类
 */

public class UserData extends Application{
    private String userId="id";                     //ID
    private String userName="name";                 //姓名
    private String userSex="sex";                   //性别
    private int userAge=1;                          //年龄
    private String userDate="date";                 //入院时间
    private String userAddress="address";           //联系地址
    private String userLinkman="linkman";           //联系人
    private String userTel="tel";                   //联系方式
    private String userDiag="diag";                 //诊断


    //获取用户名
    public String getUserName() {
        return userName;
    }
    //设置用户名
    public void setUserName(String userName) {
        this.userName = userName;
    }
    //获取用户性别
    public String getUserSex() {
        return userSex;
    }
    //设置用户性别
    public void setUserSex(String userSex) {
        this.userSex = userSex;
    }
    //获取用户年龄
    public int getUserAge() {
        return userAge;
    }
    //设置用户年龄
    public void setUserAge(int userAge) {
        this.userAge = userAge;
    }
    //获取用户id
    public String getUserId() {
        return userId;
    }
    //设置用户id
    public void setUserId(String userId) {
        this.userId = userId;
    }
    //获取入院时间
    public String getUserDate() {
        return userDate;
    }
    //设置入院时间
    public void setUserDate(String userDate) {
        this.userDate = userDate;
    }
    //获取地址
    public String getUserAddress() {
        return userAddress;
    }
    //设置地址
    public void setUserAddress(String userAddress) {
        this.userAddress = userAddress;
    }
    //获取联系人
    public String getUserLinkman() {
        return userLinkman;
    }
    //设置联系人
    public void setUserLinkman(String userLinkman) {
        this.userLinkman = userLinkman;
    }
    //获取联系方式
    public String getUserTel() {
        return userTel;
    }
    //设置联系方式
    public void setUserTel(String userTel) {
        this.userTel = userTel;
    }
    //获取诊断信息
    public String getUserDiag() {
        return userDiag;
    }
    //设置诊断信息
    public void setUserDiag(String userDiag) {
        this.userDiag = userDiag;
    }

    @Override
    public void onCreate()
    {
        userId="id";                     //ID
        userName="name";                 //姓名
        userSex="sex";                  //性别
        userAge=0;                     //年龄
        userDate="date";                   //入院时间
        userAddress="address";              //联系地址
        userLinkman="linkman";              //联系人
        userTel="tel";                    //联系方式
        userDiag="diag";
        super.onCreate();
    }

}
