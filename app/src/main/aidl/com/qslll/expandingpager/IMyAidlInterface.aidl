// IMyAidlInterface.aidl
package com.qslll.expandingpager;
//客户端调用服务端
// Declare any non-default types here with import statements
// 函数需在ComService里override
import com.qslll.expandingpager.ICallBack;
import com.qslll.expandingpager.Entity;

interface IMyAidlInterface {
    void registerCallback(ICallBack cb);
    void unregisterCallback(ICallBack cb);
    void send2Service(inout Entity entity);

    void runServiceState();
    boolean getConnectionStatus();

    int getCurrentFingerNumber();//手指序号
    float getCurrentAngle();//当前角度
    String [] getFingerArray();
    int getScore();

    //向下位机发送角度数据报文
    void setCurrentAngle(String strAngles);
   /*
   训练模式报文收发
   */
   void sendTrainMode(int mode);//向下位机发送训练模式
   boolean aChangeMode(int mode);//接收下位机aChangeMode报文
}