// ICallBack.aidl
package com.chej.HandMate;

// Declare any non-default types here with import statements
//回调注册表(服务端调用客户端)
import  com.chej.HandMate.Entity;

interface ICallBack {

    void callBack(inout Entity entity);

}
