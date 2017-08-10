// IMyAidlInterface.aidl
package com.chej.HandMate;
//客户端调用服务端
// Declare any non-default types here with import statements
// 函数需在ComService里override
import com.chej.HandMate.ICallBack;
import com.chej.HandMate.Entity;

interface IMyAidlInterface {
    void registerCallback(ICallBack cb);
    void unregisterCallback(ICallBack cb);
    void send2Service(inout Entity entity);

    void runServiceState();
    boolean getConnectionStatus();

    String [] getFingerArray();
    String[] getComponentStatus();
    String[] getConfigArray();

    //向下位机发送角度数据报文
    void setCurrentAngle(String strAngles);
   /*
   训练模式报文收发
   */
   void sendTrainMode(int mode);//向下位机发送训练模式
   void sendTrainAck(int mode,int status);//游戏正式界面后，用户点击确实开始后，AWS发送此报文通知GCU运动
   void sendShutdown();//通知下位机关机
   void sendrConfigData();//请求配置信息
}