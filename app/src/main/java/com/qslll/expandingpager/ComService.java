package com.qslll.expandingpager;

import com.qslll.expandingpager.U3D.u3dPlayer;
import com.qslll.expandingpager.model.users.UserData;

import android.app.Activity;
import android.app.Service;
import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.service.carrier.CarrierService;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;


//建立Android服务层，为下位机和UNITY提供通讯服务
public class ComService extends Service {

    private int updateNUM = 0;
    private int connectionCounter = 0;
    private int heartBeatCounter = 0;
    private Context mContext;
    public u3dPlayer u3d;
    public Connection connection;
    private IntentFilter intentFilter;
    private LocalReceiver localReceiver;
    private LocalBroadcastManager localBroadcastManager;

    public enum States {SOCKETUNCONNECTED, SOCKETCONNECTED, MACHINECONNECTED}

    ;
    States state;
    Timer heartBeatTimer;
    TimerTask heartBeatTimerTask;
    Timer timer2;
    TimerTask timer2task;

    //初始化connection对象，WIFI连接状态
    public ComService() {
        connection = new Connection();
        state = States.SOCKETUNCONNECTED;
        heartBeatTimer = new Timer();
        timer2 = new Timer();
    }


    public int addNUM() {
        return updateNUM++;
    }

    //实例化回调函数列表
    private RemoteCallbackList<ICallBack> mCallbacks = new RemoteCallbackList<>();


    //注册服务程序，注册回调函数列表
    private IMyAidlInterface.Stub binder = new IMyAidlInterface.Stub() {
        @Override
        public void registerCallback(ICallBack cb) throws RemoteException {
            mCallbacks.register(cb);
        }

        @Override
        public void unregisterCallback(ICallBack cb) throws RemoteException {
            mCallbacks.unregister(cb);
        }

        @Override
        public void send2Service(Entity entity) throws RemoteException {
            String msg = "收到来自客户端是消息：" + entity.getName();
            //sendMsgToMain(msg);
        }

        @Override
        public float getCurrentAngle() {
            return connection.angleFromDownStream;
        }

        @Override
        public void runServiceState() {
            ServiceState();
        }

        @Override
        public boolean getConnectionStatus() {
            if (state == States.MACHINECONNECTED) {
                return true;
            } else {
                return false;
            }
        }

        @Override
        public int getCurrentFingerNumber() {
            return connection.fingerNumber;
        }

        @Override
        public int getScore() {
            return u3d.getScore();
        }

        @Override
        public String[] getFingerArray() {
            return connection.fingerArray;
        }

        //向下位机发送角度数据报文
        @Override
        public void setCurrentAngle(String strAngles) {
            String[] str = strAngles.split("\\ ");
            int[] angles = {Integer.parseInt(str[0]), Integer.parseInt(str[1]), Integer.parseInt(str[2])
                    , Integer.parseInt(str[3]), Integer.parseInt(str[4])};
            byte[] bAngles = {(byte) 0xff, (byte) 0xff, (byte) 0x20, (byte) 0x10,
                    (byte) 0xb4, (byte) 0xb4, (byte) 0xb4, (byte) 0xb4, (byte) 0xb4,
                    (byte) ~(0x10 + 0x10 + 0xb4 + 0xb4 + 0xb4 + 0xb4 + 0xb4)};
            bAngles[0] = (byte) 0xff;
            bAngles[1] = (byte) 0xff;
            bAngles[2] = (byte) 0x20;
            bAngles[3] = (byte) 0x10;
            bAngles[4] = (byte) angles[0];
            bAngles[5] = (byte) angles[1];
            bAngles[6] = (byte) angles[2];
            bAngles[7] = (byte) angles[3];
            bAngles[8] = (byte) angles[4];
            bAngles[9] = (byte) ~(bAngles[2] + bAngles[3] + bAngles[4] + bAngles[5] +
                    bAngles[6] + bAngles[7] + bAngles[8]);
            connection.SendByte(bAngles);
            Log.e("发送下位机", strAngles);

        }

        @Override
        public void sendTrainMode(int mode) {//向下位机发送训练模式
            //TrainMode报文
            byte[] bytes = new byte[6];
            bytes[0] = (byte) 0xff;
            bytes[1] = (byte) 0xff;
            bytes[2] = (byte) 0x07;//ID
            bytes[3] = (byte) 0x06;//长度
            bytes[4] = (byte) mode;//模式
            /*
            0：游戏模式1：主动模式2：被动模式3：评估模式
             */
            bytes[5] = (byte) ~(bytes[2] + bytes[3] + bytes[4]);
            connection.SendByte(bytes);
            Log.e("切换模式", mode + "");
        }

    };

    //绑定服务
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    //解绑服务
    @Override
    public boolean onUnbind(Intent intent) {

        return super.onUnbind(intent);
    }

    //执行服务发送到对应活动的回调函数列表（向系统发送信息）
    private void sendMsgToMain(String msg) {
        int N = mCallbacks.beginBroadcast();

        Log.e("ComService", "N is number " + Integer.toString(N));
        Log.e("ComService", "N is number " + Integer.toString(N));
        Log.e("ComService", "N is number " + Integer.toString(N));
        Log.e("ComService", "N is number " + Integer.toString(N));

        if (N < 1)
            return;
        for (int i = 0; i < N; i++) {
            try {
                mCallbacks.getBroadcastItem(i).callBack(new Entity(msg, 1));
            } catch (RemoteException e) {
            }
        }
        mCallbacks.finishBroadcast();
    }


    //服务被创造时执行该函数
    @Override
    public void onCreate() {
        super.onCreate();

        Log.e("ComService", "onCreate executed");

        localBroadcastManager = LocalBroadcastManager.getInstance(this); // 获取实例
        intentFilter = new IntentFilter();
        intentFilter.addAction("com.example.broadcasttest.LOCAL_BROADCAST");
        localReceiver = new LocalReceiver();
        localBroadcastManager.registerReceiver(localReceiver, intentFilter); // 注册本地广播监听器

        //ServiceState();
        //服务被创造时启动WIFI连接线程
        connection.connectThread();

        mContext = this;
    }

    //服务启动时执行
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("ComService", "onStartCommand executed");

        return super.onStartCommand(intent, flags, startId);
    }

    //服务销毁时同时销毁回调函数
    @Override
    public void onDestroy() {
        super.onDestroy();
        mCallbacks.kill();
        Log.d("ComService", "onDestroy executed");
    }


    //连接WIFI使用的状态机
    public void ServiceState() {

        switch (state) {

            case SOCKETUNCONNECTED:

                //获取WIFI信息
                WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                String WIFISSID = wifiInfo.getSSID();

                //判定WIFI是否为指定的WIFI
                if (WIFISSID.equals("\"ChejRobot_Glove\"")) {

                    connection.connectThread();

                    //阻断线程3秒钟，以用来判断WIFI是否成功连接
                    new Thread(new Runnable() {
                        public void run() {

                            try {
                                Thread.currentThread().sleep(3000);//阻断3秒

                                if (connection.isConnected == false) {

                                    Log.e("ComService", "Connection is not working");
                                    Toast.makeText(getBaseContext(), "Connection is not working", Toast.LENGTH_SHORT).show();

                                    sendMsgToMain("CHECK_DEVICE_CONNECTION");

                                    Log.e("ComService", "Deroute !!!!!!!!!!!!!1");
                                } else {
                                    Log.e("ComService", "I'm in connetion is done");
                                    state = States.MACHINECONNECTED;
/*
                                    Intent intent = new Intent("com.example.broadcasttest.LOCAL_BROADCAST");
                                    intent.putExtra("Connection","reconnect");
                                    localBroadcastManager.sendBroadcast(intent); // 发送本地广播
*/
                                }

                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                        }
                    }).start();

                } else {

                    sendMsgToMain("CHECK_WIFI_STATUS");

                }


                break;

            //WIFI硬件连接成功后，等待下位机发送接收确认回执
            case SOCKETCONNECTED:
                Log.e("ComService", "I'm Connected in SOCKETCONNECTED");

                if (connection.machineConnection == true) {

                    state = States.MACHINECONNECTED;

                } else if (connectionCounter == 3 && connection.machineConnection == false) {

                    connectionCounter = 0;

                    //在等待3个3秒钟后，如果没有收到下位机确认回执，则重新发起连接邀请
                    reinitializeConnection();

                    sendMsgToMain("CHECK_DEVICE_CONNECTION");

                    break;
                }

                // connection.requestMachineConnection();

                if (timer2 != null) {
                    if (timer2task != null) {
                        timer2task.cancel();  //将原任务从队列中移除
                    }
                }

                timer2task = new TimerTask() {
                    @Override
                    public void run() {
                        Intent intent = new Intent("com.example.broadcasttest.LOCAL_BROADCAST");
                        intent.putExtra("Connection", "reconnect");
                        localBroadcastManager.sendBroadcast(intent); // 发送本地广播
                    }
                };

                timer2.schedule(timer2task, 3000);

                connectionCounter++;

                break;

            case MACHINECONNECTED:

                //进入连接成功状态
                Log.e("ComService", "I'm Connected in MACHINECONNECTED");
                if (connection.machineException == true) {
                    Log.e("ComService", "SoftwareException");
                    connection.machineException = false;
                }
/*
                Intent intent = new Intent("com.example.broadcasttest.LOCAL_BROADCAST");
                intent.putExtra("Connection","check_heart_beat");
                localBroadcastManager.sendBroadcast(intent); // 发送本地广播
*/
                break;

            default:
                Log.e("ComService", "I'm execute no");
                break;
        }
    }


    //本地服务广播
    class LocalReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            //Toast.makeText(context, "received local broadcast", Toast.LENGTH_SHORT).show();
            Log.e("ComService", "I received local message!");
            if (intent.getStringExtra("Connection").equals("reconnect")) {
                // ServiceState();
            } else if (intent.getStringExtra("Connection").equals("check_heart_beat")) {
                //checkHeartBeat();

                //Please put heart beat check in here
                //Please put heart beat check in here
                //Please put heart beat check in here
                //Please put heart beat check in here
                //Please put heart beat check in here
            }
        }
    }


    //心跳检测
    private void checkHeartBeat() {

        Log.e("ComService", "I'm checking my hearbeat");

        if (heartBeatCounter == 0) {

            sentHeartBeatRequest();

            heartBeatCounter = 1;
        } else if (heartBeatCounter == 1) {

            if (connection.heartBeat == true) {

                connection.heartBeat = false;
                sentHeartBeatRequest();

            } else {

                reinitializeConnection();

            }

        }
    }


    //发送心跳请求
    private void sentHeartBeatRequest() {
        if (heartBeatTimer != null) {
            if (heartBeatTimerTask != null) {
                heartBeatTimerTask.cancel();  //将原任务从队列中移除
            }
        }

        heartBeatTimerTask = new TimerTask() {
            @Override
            public void run() {
                Intent intent = new Intent("com.example.broadcasttest.LOCAL_BROADCAST");
                intent.putExtra("Connection", "check_heart_beat");
                localBroadcastManager.sendBroadcast(intent); // 发送本地广播
            }
        };

        heartBeatTimer.schedule(heartBeatTimerTask, 5000);

        // connection.requestMachineConnection();
    }

    //重新初始化WIFI连接
    private void reinitializeConnection() {

        Log.e("ComService", "I'm reinitializing Connnection");

        state = States.SOCKETUNCONNECTED;
        try {
            if (connection.mSocket != null) {
                connection.mSocket.close();
                connection.mSocket = null;
                //Log.i(tag, "--->>取消server.");
                // receiverThread.interrupt();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        connection.isConnected = false;
        connection.machineConnection = false;
        heartBeatCounter = 0;

    }


    //接收下位机aChangeMode报文
    public void ChangeMode(int mode) {
        ExerciseActivity.ExerciseActionStart(UserData.getContext(),mode);//切换手套餐
    }
}
