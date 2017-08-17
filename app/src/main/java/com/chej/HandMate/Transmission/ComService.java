package com.chej.HandMate.Transmission;

import com.chej.HandMate.Entity;
import com.chej.HandMate.ICallBack;
import com.chej.HandMate.IMyAidlInterface;
import com.chej.HandMate.Model.MyCustomDialog;
import com.chej.HandMate.U3D.u3dPlayer;

import android.app.Dialog;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.WindowManager;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;


//建立Android服务层，为下位机和UNITY提供通讯服务
public  class ComService extends Service {

    private String WifiName ="chej_glove";
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

        //向u3d发送手指角度
        @Override
        public String[] getFingerArray() {
            return connection.fingerArray;
        }

        //向u3d发送舵机状态
        @Override
        public String[] getComponentStatus() {
            return connection.componentArray;
        }

        //向u3d发送配置数据
        @Override
        public String[] getConfigArray(){
            return connection.configArray;
        }
        //向下位机发送角度数据报文
        @Override
        public void setCurrentAngle(String strAngles) {
            String[] str = strAngles.split("\\ ");
            int[] angles = {Integer.parseInt(str[0]), Integer.parseInt(str[1]), Integer.parseInt(str[2])
                    , Integer.parseInt(str[3]), Integer.parseInt(str[4])};
            byte[] bAngles = {(byte) 0xff, (byte) 0xff, (byte) 0x24, (byte) 0x10,
                    (byte) 0xb4, (byte) 0xb4, (byte) 0xb4, (byte) 0xb4, (byte) 0xb4,
                    (byte) ~(0x10 + 0x10 + 0xb4 + 0xb4 + 0xb4 + 0xb4 + 0xb4)};
            bAngles[0] = (byte) 0xff;
            bAngles[1] = (byte) 0xff;
            bAngles[2] = (byte) 0x24;
            bAngles[3] = (byte) 0x10;
            bAngles[4] = (byte) angles[0];
            bAngles[5] = (byte) angles[1];
            bAngles[6] = (byte) angles[2];
            bAngles[7] = (byte) angles[3];
            bAngles[8] = (byte) angles[4];
            bAngles[9] = (byte) ~(bAngles[2] + bAngles[3] + bAngles[4] + bAngles[5] +
                    bAngles[6] + bAngles[7] + bAngles[8]);
            connection.sendExercise(bAngles);
            Log.e("手套操", connection.bytesToString(bAngles));

        }
        @Override
        public void sendrNetStatus()//请求网络状态
        {
            connection.sendData(connection.rNetStatus());
        }
        @Override
        public void senddGloveSelect(int gloveNum)//通知下位机开始发手套数据  0无手套数据 1 左手套数据 2 右手套数据
        {
            connection.sendData((connection.dGloveSelect(gloveNum)));
        }
        @Override
        public void sendrConfigData(){//请求配置信息
            connection.sendData(connection.rConfigData());
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
            connection.sendData(bytes);
            Log.e("切换模式", mode + "");
        }

        //游戏正式界面后，用户点击确实开始后，AWS发送此报文通知GCU运动
        @Override
        public void sendTrainAck(int mode,int status){
            //TrainAck报文
            byte[] bytes = new byte[7];
            bytes[0] = (byte) 0xff;
            bytes[1] = (byte) 0xff;
            bytes[2] = (byte) 0x23;//ID
            bytes[3] = (byte) 0x07;//长度
            bytes[4] = (byte) mode;//模式
            bytes[5] = (byte) status;//状态，1：开始 0：停止
            /*
            0：游戏模式1：主动模式2：被动模式3：评估模式
             */
            bytes[6] = (byte) ~(bytes[2] + bytes[3] + bytes[4] + bytes[5]);
            connection.sendData(bytes);
            Log.e("sendTrainAck", "start");
        }
        @Override
        public void sendShutdown(){
            //dAWSEndStatus报文
            byte[] bytes = new byte[6];
            bytes[0] = (byte) 0xff;
            bytes[1] = (byte) 0xff;
            bytes[2] = (byte) 0x06;//ID
            bytes[3] = (byte) 0x06;//长度
            bytes[4] = (byte) 0x00;//0：关机１：开机
            bytes[5] = (byte) ~(bytes[2] + bytes[3] + bytes[4] );
            connection.sendData(bytes);
            Log.e("sendShutdown", "send");
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

        ServiceState();
        //服务被创造时启动WIFI连接线程
        //connection.connectThread();

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
                if (WIFISSID.equals("\""+WifiName+"\"")) {

                    connection.connectThread();

                    //阻断线程3秒钟，以用来判断WIFI是否成功连接
                    new Thread(new Runnable() {
                        public void run() {

                            try {
                                Thread.currentThread().sleep(3000);//阻断3秒

                                if (connection.isConnected == false) {

                                    Log.e("ComService", "Connection is not working");

                                    //sendMsgToMain("CHECK_DEVICE_CONNECTION");
                                    dialogOne();

                                    Log.e("ComService", "Deroute !!!!!!!!!!!!!");
                                } else {
                                    Log.e("ComService", "I'm in connetion is done");
                                    state = States.MACHINECONNECTED;

                                    Intent intent = new Intent("com.example.broadcasttest.LOCAL_BROADCAST");
                                    intent.putExtra("Connection","reconnect");
                                    localBroadcastManager.sendBroadcast(intent); // 发送本地广播

                                }

                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                        }
                    }).start();

                } else {

                    dialogOne();
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
                connection.sendData(connection.rConnectGCU());
                //进入连接成功状态
                connection.sendData(connection.rConfigData());//请求配置信息
                boolean flag=false;
                for(int i=0;i<connection.configArray.length;i++)
                {
                    if (!connection.configArray[i].equals("0"))
                    {
                        flag=true;
                        break;
                    }
                    else {
                        flag=false;
                    }
                }
                if (!flag)
                {
                    dialogTwo();
                }
                Log.e("ComService", "I'm Connected in MACHINECONNECTED");
                Intent intent = new Intent("com.example.broadcasttest.LOCAL_BROADCAST");
                intent.putExtra("Connection","check_heart_beat");
                localBroadcastManager.sendBroadcast(intent); // 发送本地广播

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
                 ServiceState();
            } else if (intent.getStringExtra("Connection").equals("check_heart_beat")) {
                checkHeartBeat();
            }
        }
    }


    //心跳检测
    private void checkHeartBeat() {

        Log.e("ComService", "I'm checking my hearbeat");
        long time = connection.refFormatNowDate()-connection.beatTime;
        Log.e("ComService", time+"");
        if (time>10000)
        {
            connection.heartBeat=false;
        }
        if (heartBeatCounter == 0) {

            sentHeartBeatRequest();

            heartBeatCounter = 1;
            connection.heartBeat = true;
        } else if (heartBeatCounter == 1) {

            if (connection.heartBeat == true) {

                //connection.heartBeat = false;
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

        heartBeatTimer.schedule(heartBeatTimerTask, 1000);

         connection.sendData(connection.rHeartBeat());
    }

    //重新初始化WIFI连接
    private void reinitializeConnection() {

        Log.e("ComService", "I'm reinitializing Connnection");

        state = States.SOCKETUNCONNECTED;
        try {
            if (connection.mSocket != null) {
                connection.mSocket.close();
                connection.mSocket = null;
                Log.e("ComServise", "--->>取消server.");
               //connection.receiverThread.interrupt();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        connection.isConnected = false;
        connection.machineConnection = false;
        heartBeatCounter = 0;

        ServiceState();
        //服务被创造时启动WIFI连接线程
       // connection.connectThread();
        //connection.sendData(connection.rConnectGCU());
        //connection.sendData(connection.rConfigData());
        mContext = this;
    }

    public void dialogOne(){//弹出第一个对话框
        MyCustomDialog.Builder builder = new MyCustomDialog.Builder(getApplicationContext());
        builder.setMessage("请检查本机网络连接");
        builder.setTitle("提示");
        builder.setPositiveButton("重新连接", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ServiceState();
                dialog.dismiss();
            }
        });
        builder.setNegativeButton("暂不连接", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

            }
        });
        Dialog dialog=builder.create();
        dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        dialog.show();
    }

    public void dialogTwo(){//弹出第一个对话框
        MyCustomDialog.Builder builder = new MyCustomDialog.Builder(getApplicationContext());
        builder.setMessage("配置未获取");
        builder.setTitle("提示");
        builder.setPositiveButton("重新获取", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                connection.sendData(connection.rConfigData());//请求配置信息
                boolean flag=false;
                for(int i=0;i<connection.configArray.length;i++)
                {
                    if (!connection.configArray[i].equals("0"))
                    {
                        flag=true;
                        break;
                    }
                    else {
                        flag=false;
                    }
                }
                if (!flag)
                {
                    dialogTwo();
                }
                dialog.dismiss();
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

            }
        });
        Dialog dialog=builder.create();
        dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        dialog.show();
    }

}
