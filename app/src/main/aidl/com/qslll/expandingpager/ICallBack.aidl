// ICallBack.aidl
package com.qslll.expandingpager;

// Declare any non-default types here with import statements
//回调注册表(服务端调用客户端)
import com.qslll.expandingpager.Entity;

interface ICallBack {

    void callBack(inout Entity entity);

}
