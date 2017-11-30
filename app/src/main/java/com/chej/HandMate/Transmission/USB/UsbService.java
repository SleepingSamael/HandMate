package com.chej.HandMate.Transmission.USB;

/**
 * Created by samael on 2017/9/21.
 */

import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.chej.HandMate.AdminActivity;
import com.chej.HandMate.Entity;
import com.chej.HandMate.ExerciseActivity;
import com.chej.HandMate.ICallBack;
import com.chej.HandMate.IMyAidlInterface;
import com.chej.HandMate.MasterSlaveActivity;
import com.chej.HandMate.Database.users.UserData;
import com.chej.HandMate.U3D.u3dPlayer;
import com.chej.HandMate.utils.Debuger;
import com.felhr.usbserial.CDCSerialDevice;
import com.felhr.usbserial.UsbSerialDevice;
import com.felhr.usbserial.UsbSerialInterface;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;

import static com.chej.HandMate.AdminActivity.voltageToMessage;
import static com.chej.HandMate.Transmission.USB.USBHelper.bytesToHexString;
import static com.chej.HandMate.Transmission.USB.USBHelper.bytesToString;

public class UsbService extends Service {
    private int updateNUM = 0;
    private int connectionCounter = 0;
    private int heartBeatCounter = 0;
    private Context mContext;
    public u3dPlayer u3d;
    public USBHelper usbHelper;
    private IntentFilter intentFilter;

    private LocalBroadcastManager localBroadcastManager;

    Timer heartBeatTimer;
    Timer timer2;

    private ReceiveHandler receiveHandler = new ReceiveHandler();
    Thread receiverThread;
    Thread sendThread;
    Thread ExerciseThread;

    //初始化connection对象，连接状态
    public UsbService() {
        usbHelper = new USBHelper();
        heartBeatTimer = new Timer();
        timer2 = new Timer();
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

        }

        @Override
        public boolean getConnectionStatus() {
            return false;
        }

        //向u3d发送手指角度
        @Override
        public String[] getFingerArray() {
            return usbHelper.fingerArray;
        }

        //向u3d发送舵机状态
        @Override
        public String[] getComponentStatus() {
            return usbHelper.componentArray;
        }
        @Override
        public void sendGetV(){
            sendData(usbHelper.getV());
        }
        //获取舵机温度
        @Override
        public String[] getComponentTemperature() {
            return usbHelper.componentTemperature;
        }
        //获取舵机错误
        @Override
        public String[] getComponentError() {
            return usbHelper.componentError;
        }

        //向u3d发送配置数据
        @Override
        public String[] getConfigArray(){
            return usbHelper.configArray;
        }
        //向下位机发送角度数据报文 dAWSAngle
        @Override
        public void setCurrentAngle(String strAngles) {
            String[] str = strAngles.split("\\ ");
            int[] angles = {Integer.parseInt(str[0]), Integer.parseInt(str[1]), Integer.parseInt(str[2])
                    , Integer.parseInt(str[3]), Integer.parseInt(str[4])};
            byte[] bAngles = {(byte) 0xff, (byte) 0xff, (byte) 0x12, (byte) 0x10,
                    (byte) 0xb4, (byte) 0xb4, (byte) 0xb4, (byte) 0xb4, (byte) 0xb4,
                    (byte) ~(0x10 + 0x10 + 0xb4 + 0xb4 + 0xb4 + 0xb4 + 0xb4)};
            bAngles[0] = (byte) 0xff;
            bAngles[1] = (byte) 0xff;
            bAngles[2] = (byte) 0x12;
            bAngles[3] = (byte) 0x10;
            bAngles[4] = (byte) angles[0];
            bAngles[5] = (byte) angles[1];
            bAngles[6] = (byte) angles[2];
            bAngles[7] = (byte) angles[3];
            bAngles[8] = (byte) angles[4];
            bAngles[9] = (byte) ~(bAngles[2] + bAngles[3] + bAngles[4] + bAngles[5] +
                    bAngles[6] + bAngles[7] + bAngles[8]);
            sendExercise(bAngles);
            Log.e("手套操", bytesToString(bAngles));
        }
        @Override
        public void sendrNetStatus()//请求网络状态
        {
            sendData(usbHelper.rNetStatus());
        }
        @Override
        public void senddGloveSelect(int gloveNum)//通知下位机开始发手套数据  0无手套数据 1 左手套数据 2 右手套数据
        {
            sendData((usbHelper.dGloveSelect(gloveNum)));
        }
        @Override
        public void sendTrainMode(int mode) {//向下位机发送训练模式
            //TrainMode报文
            byte[] bytes = new byte[6];
            bytes[0] = (byte) 0xff;
            bytes[1] = (byte) 0xff;
            bytes[2] = (byte) 0x0a;//ID
            bytes[3] = (byte) 0x06;//长度
            bytes[4] = (byte) mode;//模式
            /*
            0：游戏模式1：主动模式2：被动模式3：评估模式
             */
            bytes[5] = (byte) ~(bytes[2] + bytes[3] + bytes[4]);
            sendData(bytes);
            Log.e("切换模式", mode + "");
        }

        //游戏正式界面后，用户点击确实开始后，AWS发送此报文通知GCU运动
        @Override
        public void sendTrainAck(int mode,int status){
            //TrainAck报文
            byte[] bytes = new byte[7];
            bytes[0] = (byte) 0xff;
            bytes[1] = (byte) 0xff;
            bytes[2] = (byte) 0x11;//ID
            bytes[3] = (byte) 0x07;//长度
            bytes[4] = (byte) mode;//模式
            bytes[5] = (byte) status;//状态，1：开始 0：停止
            /*
            0：游戏模式1：主动模式2：被动模式3：评估模式
             */
            bytes[6] = (byte) ~(bytes[2] + bytes[3] + bytes[4] + bytes[5]);
            sendData(bytes);
            Log.e("sendTrainAck", "start");
        }
        @Override
        public void sendShutdown(){
            //dAWSEndStatus报文
            byte[] bytes = new byte[6];
            bytes[0] = (byte) 0xff;
            bytes[1] = (byte) 0xff;
            bytes[2] = (byte) 0x09;//ID
            bytes[3] = (byte) 0x06;//长度
            bytes[4] = (byte) 0x00;//0：关机１：开机
            bytes[5] = (byte) ~(bytes[2] + bytes[3] + bytes[4] );
            sendData(bytes);
            Log.e("sendShutdown", "send");
        }
        @Override
        public void sendConfigData(String Data){
            //收到GCU发送的rConfigData报文时，需要把配置数据发送给GCU。
            sendData(usbHelper.dConfigData(Data));
            Log.e("sendConfigData", "send");
        }
        @Override
        public void sendcSVCMode(int SVCMode,int ModeStatus){
            //SVCMode 0：NULL1：版本升级2：网络状态3：部件状态4：运动配置
            // ModeStatus 0：退出 1：进入
            sendData(usbHelper.cSVCMode(SVCMode,ModeStatus));
            Log.e("sendcSVCMode", "send");
        }
        @Override
        public void sendrComponentStatus()//请求舵机状态
        {
            sendData(usbHelper.rComponentStatus());
        }

    };

    public static final String ACTION_USB_READY = "com.felhr.connectivityservices.USB_READY";
    public static final String ACTION_USB_ATTACHED = "android.hardware.usb.action.USB_DEVICE_ATTACHED";
    public static final String ACTION_USB_DETACHED = "android.hardware.usb.action.USB_DEVICE_DETACHED";
    public static final String ACTION_USB_NOT_SUPPORTED = "com.felhr.usbservice.USB_NOT_SUPPORTED";
    public static final String ACTION_NO_USB = "com.felhr.usbservice.NO_USB";
    public static final String ACTION_USB_PERMISSION_GRANTED = "com.felhr.usbservice.USB_PERMISSION_GRANTED";
    public static final String ACTION_USB_PERMISSION_NOT_GRANTED = "com.felhr.usbservice.USB_PERMISSION_NOT_GRANTED";
    public static final String ACTION_USB_DISCONNECTED = "com.felhr.usbservice.USB_DISCONNECTED";
    public static final String ACTION_CDC_DRIVER_NOT_WORKING = "com.felhr.connectivityservices.ACTION_CDC_DRIVER_NOT_WORKING";
    public static final String ACTION_USB_DEVICE_NOT_WORKING = "com.felhr.connectivityservices.ACTION_USB_DEVICE_NOT_WORKING";
    public static final int MESSAGE_FROM_SERIAL_PORT = 0;
    public static final int CTS_CHANGE = 1;
    public static final int DSR_CHANGE = 2;
    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
    private static final int BAUD_RATE = 115200; // 波特率
    public static boolean SERVICE_CONNECTED = false;

    private Context context;
    private Handler mHandler;
    private UsbManager usbManager;
    private UsbDevice device;
    private UsbDeviceConnection connection;
    private UsbSerialDevice serialPort;

    private boolean serialPortConnected;
    /*
     *  Data received from serial port will be received here. Just populate onReceivedData with your code
     *  In this particular example. byte stream is converted to String and send to UI thread to
     *  be treated there.
     */
    private UsbSerialInterface.UsbReadCallback uCallback = new UsbSerialInterface.UsbReadCallback() {
        @Override
        public void onReceivedData(byte[] arg0) {

            //读取数据流中的数据
            byte[] result = arg0;

            try {
                usbHelper.strResult = bytesToString(result);
            //    Log.e("USB",usbHelper.strResult);
                usbHelper.hexResult = bytesToHexString(result);
                if (!result.equals("")) {
                    Message msg = new Message();
                    msg.what = 1;
                    Bundle data = new Bundle();
                    data.putString("msg", usbHelper.strResult);//十进制结果
                    data.putString("hex", usbHelper.hexResult);//十六进制结果
                    msg.setData(data);
                    receiveHandler.sendMessage(msg);
                }
            } catch (Exception e) {
                Log.e("ReadThread", "--->>read failure!" + e.toString());
            }
            try {
                Thread.sleep(50L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    };

    /*
     * State changes in the CTS line will be received here
     */
    private UsbSerialInterface.UsbCTSCallback ctsCallback = new UsbSerialInterface.UsbCTSCallback() {
        @Override
        public void onCTSChanged(boolean state) {

            if(mHandler != null)
                mHandler.obtainMessage(CTS_CHANGE).sendToTarget();
        }
    };

    /*
     * State changes in the DSR line will be received here
     */
    private UsbSerialInterface.UsbDSRCallback dsrCallback = new UsbSerialInterface.UsbDSRCallback() {
        @Override
        public void onDSRChanged(boolean state) {

            if(mHandler != null)
                mHandler.obtainMessage(DSR_CHANGE).sendToTarget();
        }
    };
    /*
     * 错误排查
     */
    private final BroadcastReceiver usbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context arg0, Intent arg1) {

            if (arg1.getAction().equals(ACTION_USB_PERMISSION)) {
                boolean granted = arg1.getExtras().getBoolean(UsbManager.EXTRA_PERMISSION_GRANTED);
                if (granted) // User accepted our USB connection. Try to open the device as a serial port
                {
                    Intent intent = new Intent(ACTION_USB_PERMISSION_GRANTED);
                    arg0.sendBroadcast(intent);
                    connection = usbManager.openDevice(device);
                    new ConnectionThread().start();
                    sendData(usbHelper.rConnectGCU());
                    //进入连接成功状态
                } else // User not accepted our USB connection. Send an Intent to the Main Activity
                {
                    Intent intent = new Intent(ACTION_USB_PERMISSION_NOT_GRANTED);
                    arg0.sendBroadcast(intent);
                    Debuger.dialogError("UsbService.usbReceiver","ACTION_USB_PERMISSION_NOT_GRANTED");
                }
            } else if (arg1.getAction().equals(ACTION_USB_ATTACHED)) {
                Debuger.dialogError("UsbService.usbReceiver","ACTION_USB_ATTACHED");
                if (!serialPortConnected)
                    findSerialPortDevice(); // A USB device has been attached. Try to open it as a Serial port
            } else if (arg1.getAction().equals(ACTION_USB_DETACHED)) {
                // Usb device was disconnected. send an intent to the Main Activity
                //限定pid编号
                boolean flag = false;
                HashMap<String, UsbDevice> usbDevices = usbManager.getDeviceList();
                for (Map.Entry<String, UsbDevice> entry : usbDevices.entrySet()) {
                    UsbDevice device0= entry.getValue();
                    int devicePID = device0.getProductId();
                    if(devicePID==29987) {
                        flag=true;
                        break;
                    }
                }
                if(!flag) {
                    Debuger.dialogError("UsbService.usbReceiver", "ACTION_USB_DISCONNECTED");
                    Intent intent = new Intent(ACTION_USB_DISCONNECTED);
                    arg0.sendBroadcast(intent);
                    if (serialPortConnected) {
                        serialPort.close();
                    }
                    serialPortConnected = false;
                }

            }
        }
    };

    /*
     * onCreate will be executed when service is started. It configures an IntentFilter to listen for
     * incoming Intents (USB ATTACHED, USB DETACHED...) and it tries to open a serial port.
     */
    @Override
    public void onCreate() {
        try{
            this.context = this;
            serialPortConnected = false;
            UsbService.SERVICE_CONNECTED = true;

            setFilter();

            usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
            findSerialPortDevice();
        }catch (Exception ex){
            Debuger.dialogError("UsbService.onCreate","UsbService.onCreat.err"+ex.getMessage());
        }

    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return Service.START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mCallbacks.kill();
        UsbService.SERVICE_CONNECTED = false;
    }

    /*
     * This function will be called from MainActivity to change baud rate
     */

    public void changeBaudRate(int baudRate){
        if(serialPort != null)
            serialPort.setBaudRate(baudRate);
    }

    public void setHandler(Handler mHandler) {
        this.mHandler = mHandler;
    }

    private void findSerialPortDevice() {
        try{
            // This snippet will try to open the first encountered usb device connected, excluding usb root hubs
            HashMap<String, UsbDevice> usbDevices = usbManager.getDeviceList();
            StringBuilder stringBuilder=new StringBuilder();
           for (Map.Entry<String, UsbDevice> entry : usbDevices.entrySet()) {
                UsbDevice device0= entry.getValue();
               stringBuilder.append("PID:"+device0.getProductId()+"  "
                       +"VID:"+device0.getVendorId()+"\n"
                       +"DeviceName:"+device0.getDeviceName() + "\n"
                       +"ProductName:"+device0.getProductName()+ "\n"
                       +"DeviceId:"+device0.getDeviceId()+ "\n"
                       +"SerialNumber:"+device0.getSerialNumber()+ "\n"
                       +"ManufacturerName"+device0.getManufacturerName()+ "\n\n");
            }
            /**
             * 存储USB连接设备信息
             */
            SharedPreferences userSettings = UserData.getContext().getSharedPreferences("setting", 0);
            //让setting处于编辑状态
            SharedPreferences.Editor editor = userSettings.edit();
            //存放数据
            editor.putString("deviceInfo",stringBuilder.toString());
            //d、完成提交
            editor.commit();
            if (!usbDevices.isEmpty()) {
                boolean keep = true;
                for (Map.Entry<String, UsbDevice> entry : usbDevices.entrySet()) {
                     UsbDevice tmp_device = entry.getValue();
                    int deviceVID = tmp_device.getVendorId();
                    int devicePID = tmp_device.getProductId();

                    if (deviceVID != 0x1d6b && (devicePID != 0x0001 && devicePID != 0x0002 && devicePID != 0x0003)) {
                        // There is a device connected to our Android device. Try to open it as a Serial Port.
                        if(devicePID==29987) {
                            device = tmp_device;
                            requestUserPermission();
                            keep = false;
                        }
                    } else {
                        connection = null;
                        device = null;
                    }

                    if (!keep)
                        break;
                }
                if (!keep) {
                    // There is no USB devices connected (but usb host were listed). Send an intent to MainActivity.
                    Intent intent = new Intent(ACTION_NO_USB);
                    sendBroadcast(intent);
                }
            } else {
                // There is no USB devices connected. Send an intent to MainActivity
                Intent intent = new Intent(ACTION_NO_USB);
                sendBroadcast(intent);
            }
        }catch (Exception ex){
            Debuger.dialogError("UsbService.findSerialPortDevice","findSerialPortDevice.err"+ex.getMessage());
        }


    }

    private void setFilter() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_USB_PERMISSION);
        filter.addAction(ACTION_USB_DETACHED);
        filter.addAction(ACTION_USB_ATTACHED);
        registerReceiver(usbReceiver, filter);
    }

    /*
     * Request user permission. The response will be received in the BroadcastReceiver
     */
    private void requestUserPermission() {
        PendingIntent mPendingIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
        usbManager.requestPermission(device, mPendingIntent);
    }

    public class UsbBinder extends Binder {
        public UsbService getService() {
            return UsbService.this;
        }
    }

    /*
     * A simple thread to open a serial port.
     * Although it should be a fast operation. moving usb operations away from UI thread is a good thing.
     */
    private class ConnectionThread extends Thread {
        @Override
        public void run() {
            try{
                serialPort = UsbSerialDevice.createUsbSerialDevice(device, connection);
                if (serialPort != null) {
                    if (serialPort.open()) {
                        serialPortConnected = true;
                        serialPort.setBaudRate(BAUD_RATE);
                        serialPort.setDataBits(UsbSerialInterface.DATA_BITS_8);
                        serialPort.setStopBits(UsbSerialInterface.STOP_BITS_1);
                        serialPort.setParity(UsbSerialInterface.PARITY_NONE);
                        /**
                         * Current flow control Options:
                         * UsbSerialInterface.FLOW_CONTROL_OFF
                         * UsbSerialInterface.FLOW_CONTROL_RTS_CTS only for CP2102 and FT232
                         * UsbSerialInterface.FLOW_CONTROL_DSR_DTR only for CP2102 and FT232
                         */
                        serialPort.setFlowControl(UsbSerialInterface.FLOW_CONTROL_OFF);
                        serialPort.read(uCallback);
                        serialPort.getCTS(ctsCallback);
                        serialPort.getDSR(dsrCallback);
                        //sendData(usbHelper.rConnectGCU());

                        //           new ReadThread().start();

                        //
                        // Some Arduinos would need some sleep because firmware wait some time to know whether a new sketch is going
                        // to be uploaded or not
                    /*try {
                        Thread.sleep(2000); // sleep some. YMMV with different chips.
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }*/

                        // Everything went as expected. Send an intent to MainActivity
                        Intent intent = new Intent(ACTION_USB_READY);
                        context.sendBroadcast(intent);
                        Debuger.dialogError("UsbService.ConnectionThread.Run","ACTION_USB_READY");
                    } else {
                        // Serial port could not be opened, maybe an I/O error or if CDC driver was chosen, it does not really fit
                        // Send an Intent to Main Activity
                        if (serialPort instanceof CDCSerialDevice) {
                            Intent intent = new Intent(ACTION_CDC_DRIVER_NOT_WORKING);
                            context.sendBroadcast(intent);
                            Debuger.dialogError("UsbService.ConnectionThread.Run","ACTION_CDC_DRIVER_NOT_WORKING");
                        } else {
                            Intent intent = new Intent(ACTION_USB_DEVICE_NOT_WORKING);
                            context.sendBroadcast(intent);
                            Debuger.dialogError("UsbService.ConnectionThread.Run","ACTION_USB_DEVICE_NOT_WORKING");
                        }
                    }
                } else {
                    // No driver for given device, even generic CDC driver could not be loaded
                    Intent intent = new Intent(ACTION_USB_NOT_SUPPORTED);
                    context.sendBroadcast(intent);
                }
            }catch (Exception ex){
                Debuger.dialogError("UsbService.ConnectionThread","ConnectionThread.err"+ex.getMessage());
            }

        }//~ run
    }


    /*
     * 发送byte数据
     */
    public void SendByte(byte[] data) {
        try {
            if (serialPort != null) {
                serialPort.write(data);
                Log.e("SEND", "发送成功");
            } else {
                Log.e("SEND", "连接不存在重新连接");
            }
        } catch (Exception e) {
            Log.e("SEND", "发送失败");
            Log.e("SEND", String.valueOf(e));
            e.printStackTrace();
        }
    }
    //向下位机发送数据进程,手套操调用
    private class MySendRunnable implements Runnable{
        private byte[] sendData;
        public MySendRunnable(byte[] sendData)
        {
            this.sendData = sendData;
        }
        public void run() {

            try {
                // if (!sendData.equals("")) {
                SendByte(sendData);

                Log.e("SendRunnable", "---->>已发送至下位机....");
                //  }
            } catch (Exception e) {
                Log.e("SendRunnable", "--->>read failure!" + e.toString());
                Debuger.dialogError("UsbService","MySendRunnable.run.err"+e.getMessage());
            }

        }
    }
    //开启新线程发送数据
    public void sendData(byte[] data) {
        MySendRunnable mySendRunnable1 = new MySendRunnable(data);
        sendThread = new Thread(mySendRunnable1);
        //sendThread = new Thread(new MySendRunnable(data));
        sendThread.start();
    }
    //开启新线程发送手套操（上→下）
    public void sendExercise(byte[] data) {
        MySendRunnable mySendRunnable = new MySendRunnable(data);
        ExerciseThread = new Thread(mySendRunnable);
        //ExerciseThread = new Thread(new MySendRunnable(data));
        ExerciseThread.start();
    }

    //消息处理器，对接收到的消息进行处理（上位机接收下位机数据）
    private class ReceiveHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            try {
                super.handleMessage(msg);
                //对启动后的消息进行处理
                if (msg.what == 1) {
                    String result = msg.getData().get("hex").toString();
              //      Log.e("Receiver", "result= " + result);
                    //连包处理
                    String[] strFF = result.split("ff ff ");
                    for (int ff = 0; ff < strFF.length; ff++) {
                        strFF[ff].trim();
               //         Log.e("Receiver", "I am receiving " + strFF[ff]);
                        String[] str = strFF[ff].split("\\ ");
                        /**
                         * 判断收到报文类型
                         */
                        if (str[0].equals("03"))//心跳检测
                        {
                            usbHelper.beatTime = usbHelper.refFormatNowDate();
                            usbHelper.heartBeat = true;
                            sendData(usbHelper.rHeartBeat());
                        }
                        if (str[0].equals("08"))//关机
                        {
                            if (usbHelper.getRunningActivityName().equals(".U3D.u3dPlayer")) {
                                try {
                                    //暂停广播
                                    Intent i = new Intent("com.example.U3D_BROADCAST");
                                    i.putExtra("U3D", "pauseU3D");
                                    UserData.getContext().sendBroadcast(i);
                                } catch (Exception e) {
                                    Log.e("u3d", e.toString());
                                }
                            }
                            Log.e("Receiver", "ID=    " + str[0]);
                            usbHelper.dialogShutDown();
                        }
                        if (str[0].equals("0c"))//dButtonInfo改变模式
                        {
                            Log.e("Receiver", "ID=    " + str[0]);
                            try {
                                ButtonMode(Integer.valueOf(str[2]).intValue());
                            } catch (Exception e) {
                                Log.e("Connection", String.valueOf(e));
                            }
                        }
                        if (str[0].equals("20"))//dNetStatus网络状态
                        {
                            Log.e("Receiver", "ID=    " + str[0]);
                            String NetType = null;//网络类型
                            switch (str[2]) {
                                case "00":
                                    NetType = "WIFI";
                                    break;
                                case "01":
                                    NetType = "Zigbee";
                                    break;
                                case "02":
                                    NetType = "AX-12Bus";
                                    break;
                                default:
                                    NetType = str[2];
                            }
                            String rightStatus = "未连接";
                            String leftStatus = "未连接";
                            if (str[3].equals("01")) {
                                rightStatus = "连接正常";
                            }
                            if (str[4].equals("00")) {
                                leftStatus = "连接正常";
                            }
                            usbHelper.dialogError("手套" + NetType + "连接状态", "右手：" + rightStatus + "\n" + "左手：" + leftStatus);
                        }
                        if (str[0].equals("10"))//dPowerinfo下位机电量
                        {
                            Log.e("Receiver", "ID=    " + str[0]);
                            try {
                                int a = Integer.parseInt(str[2], 16);
                                usbHelper.powerInfo = Integer.valueOf(a).intValue();
                                Log.e("PowerInfo", "下位机电量为   " + usbHelper.powerInfo);
                            } catch (Exception e) {
                                Log.e("PowerInfo", String.valueOf(e));
                            }
                        }
                        if (str[0].equals("07"))//dFignerVInit电压初始值
                        {
                            Log.e("Receiver", "ID=    " + str[0]);
                            try {
                                //让setting处于编辑状态
                                SharedPreferences.Editor editor = usbHelper.userSettings.edit();
                                //存放数据
                                editor.putString("thumb180V",(float)Integer.parseInt(str[2]+str[3],16)/1000+"");
                                editor.putString("fore180V",(float)Integer.parseInt(str[4]+str[5],16)/1000+"");
                                editor.putString("middle180V",(float)Integer.parseInt(str[6]+str[7],16)/1000+"");
                                editor.putString("ring180V",(float)Integer.parseInt(str[8]+str[9],16)/1000+"");
                                editor.putString("little180V",(float)Integer.parseInt(str[10]+str[11],16)/1000+"");
                                editor.putString("thumb0V",(float)Integer.parseInt(str[12]+str[13],16)/1000+"");
                                editor.putString("fore0V",(float)Integer.parseInt(str[14]+str[15],16)/1000+"");
                                editor.putString("middle0V",(float)Integer.parseInt(str[16]+str[17],16)/1000+"");
                                editor.putString("ring0V",(float)Integer.parseInt(str[18]+str[19],16)/1000+"");
                                editor.putString("little0V",(float)Integer.parseInt(str[20]+str[21],16)/1000+"");
                                //d、完成提交
                                editor.commit();
                                Log.e("dFingerVInit", usbHelper.userSettings.getString("thumb180V","0"));
                                Log.e("dFingerVInit", usbHelper.userSettings.getString("fore180V","0"));
                                Log.e("dFingerVInit", usbHelper.userSettings.getString("middle180V","0"));
                                Log.e("dFingerVInit", usbHelper.userSettings.getString("ring180V","0"));
                                Log.e("dFingerVInit", usbHelper.userSettings.getString("little180V","0"));
                            }catch (Exception e){
                                Log.e("dFingerVInit", String.valueOf(e));
                            }
                        }
                        if(str[0].equals("05"))//GCU向AWS发送配置报文请求。
                        {
                            Log.e("Receiver", "ID=    " + str[0]);
                            String data = usbHelper.userSettings.getString("thumbFlat","10")+" "+usbHelper.userSettings.getString("foreFlat","10")+" "
                                    +usbHelper.userSettings.getString("middleFlat","10")+" "+usbHelper.userSettings.getString("ringFlat","10")+
                                    " "+usbHelper.userSettings.getString("littleFlat","10")+" "+usbHelper.userSettings.getString("thumbMiddle","110")
                                    +" "+usbHelper.userSettings.getString("foreMiddle","110")+" "+usbHelper.userSettings.getString("middleMiddle","110")
                                    +" "+usbHelper.userSettings.getString("ringMiddle","110")+" "+usbHelper.userSettings.getString("littleMiddle","110")
                                    +" "+usbHelper.userSettings.getString("thumbFist","120")+" "+usbHelper.userSettings.getString("foreFist","140")
                                    +" "+usbHelper.userSettings.getString("middleFist","140")+" "+usbHelper.userSettings.getString("ringFist","140")
                                    +" "+usbHelper.userSettings.getString("littleFist","120")+" "+usbHelper.userSettings.getString("thumbStretch","50")
                                    +" "+usbHelper.userSettings.getString("foreStretch","50")+" "+usbHelper.userSettings.getString("middleStretch","50")
                                    +" "+usbHelper.userSettings.getString("ringStretch","50")+" "+usbHelper.userSettings.getString("littleStretch","50")
                                    +" "+usbHelper.userSettings.getString("thumbMove","113")+" "+usbHelper.userSettings.getString("foreMove","113")
                                    +" "+usbHelper.userSettings.getString("middleMove","113")+" "+usbHelper.userSettings.getString("ringMove","113")
                                    +" "+usbHelper.userSettings.getString("littleMove","113")
                                    +" "+voltageToMessage(usbHelper.userSettings.getString("thumbAdjust180V", "1"), AdminActivity.DigitPosition.HIGH)
                                    +" "+voltageToMessage(usbHelper.userSettings.getString("foreAdjust180V","1"), AdminActivity.DigitPosition.HIGH)
                                    +" "+voltageToMessage(usbHelper.userSettings.getString("middleAdjust180V","1"), AdminActivity.DigitPosition.HIGH)
                                    +" "+voltageToMessage(usbHelper.userSettings.getString("ringAdjust180V","1"), AdminActivity.DigitPosition.HIGH)
                                    +" "+voltageToMessage(usbHelper.userSettings.getString("littleAdjust180V","1"), AdminActivity.DigitPosition.HIGH)
                                    +" "+voltageToMessage(usbHelper.userSettings.getString("thumbAdjust180V","1"), AdminActivity.DigitPosition.LOW)
                                    +" "+voltageToMessage(usbHelper.userSettings.getString("foreAdjust180V","1"), AdminActivity.DigitPosition.LOW)
                                    +" "+voltageToMessage(usbHelper.userSettings.getString("middleAdjust180V","1"), AdminActivity.DigitPosition.LOW)
                                    +" "+voltageToMessage(usbHelper.userSettings.getString("ringAdjust180V","1"), AdminActivity.DigitPosition.LOW)
                                    +" "+voltageToMessage(usbHelper.userSettings.getString("littleAdjust180V","1"), AdminActivity.DigitPosition.LOW)
                                    +" "+voltageToMessage(usbHelper.userSettings.getString("thumbAdjust0V","1"), AdminActivity.DigitPosition.HIGH)
                                    +" "+voltageToMessage(usbHelper.userSettings.getString("foreAdjust0V","1"), AdminActivity.DigitPosition.HIGH)
                                    +" "+voltageToMessage(usbHelper.userSettings.getString("middleAdjust0V","1"), AdminActivity.DigitPosition.HIGH)
                                    +" "+voltageToMessage(usbHelper.userSettings.getString("ringAdjust0V","1"), AdminActivity.DigitPosition.HIGH)
                                    +" "+voltageToMessage(usbHelper.userSettings.getString("littleAdjust0V","1"), AdminActivity.DigitPosition.HIGH)
                                    +" "+voltageToMessage(usbHelper.userSettings.getString("thumbAdjust0V","1"), AdminActivity.DigitPosition.LOW)
                                    +" "+voltageToMessage(usbHelper.userSettings.getString("foreAdjust0V","1"), AdminActivity.DigitPosition.LOW)
                                    +" "+voltageToMessage(usbHelper.userSettings.getString("middleAdjust0V","1"), AdminActivity.DigitPosition.LOW)
                                    +" "+voltageToMessage(usbHelper.userSettings.getString("ringAdjust0V","1"), AdminActivity.DigitPosition.LOW)
                                    +" "+voltageToMessage(usbHelper.userSettings.getString("littleAdjust0V","1"), AdminActivity.DigitPosition.LOW);
                            sendData(usbHelper.dConfigData(data));
                            Log.e("AAAAAAA",data);
                        }
                        if (str[0].equals("0d"))//下位机向上位机发送角度信息 dAngleInfo
                        {
                            if (str.length == 8) {
                                try {
                                    Log.e("Receiver", "ID=    " + str[0]);
                                    String[] str2 = msg.getData().get("msg").toString().split("\\ ");
                                    //手指序号
                                    usbHelper.fingerNumber = Integer.parseInt(str2[2]);
                                    //手指运动角度
                                    usbHelper.angleFromDownStream = Float.parseFloat(str2[5]);
                                    //对手指信息进行整理
                                    usbHelper.fingerArray[0] = Integer.parseInt(str[2], 16) + "";
                                    usbHelper.fingerArray[1] = Integer.parseInt(str[3], 16) + "";
                                    usbHelper.fingerArray[2] = Integer.parseInt(str[4], 16) + "";
                                    usbHelper.fingerArray[3] = Integer.parseInt(str[5], 16) + "";
                                    usbHelper.fingerArray[4] = Integer.parseInt(str[6], 16) + "";
                                    Log.e("fingerArray", "-----------------------------------");
                                    for (int i = 0; i < 5; i++) {
                                        Log.e("fingerArray", "The Array Contains " + usbHelper.fingerArray[i]);
                                    }
                                    Log.e("fingerArray", "-----------------------------------");

                                } catch (Exception e) {
                                    Log.e("Connection", String.valueOf(e));
                                }
                            }
                        }
                        if (str[0].equals("21"))//部件信息 dComponentStatus
                        {
                            Log.e("Receiver", "ID=    " + str[0]);
                            if (str.length == 15) {
                                if (str[2].equals("00"))//舵机
                                {

                                    switch (str[3]) {
                                        case "00"://当前位置
                                            try {
                                                if(!usbHelper.exciseFlag) {//初次获取数据
                                                    usbHelper.exciseTime = usbHelper.refFormatNowDate();
                                                    usbHelper.tmp_exciseTime = usbHelper.refFormatNowDate();
                                                    //对舵机位置信息进行整理
                                                    usbHelper.componentArray[0] = Integer.parseInt(str[5], 16) + "";
                                                    usbHelper.componentArray[1] = Integer.parseInt(str[7], 16) + "";
                                                    usbHelper.componentArray[2] = Integer.parseInt(str[9], 16) + "";
                                                    usbHelper.componentArray[3] = Integer.parseInt(str[11], 16) + "";
                                                    usbHelper.componentArray[4] = Integer.parseInt(str[13], 16) + "";
                                                    /*
                                                    Log.e("componentArray", "-----------------------------------");
                                                    for (int i = 0; i < 5; i++) {
                                                        Log.e("componentArray", "The Array Contains " + usbHelper.componentArray[i]);
                                                    }
                                                    Log.e("componentArray", "-----------------------------------");
                                                    */
                                                    usbHelper.exciseFlag=true;
                                                }
                                                else {
                                                    usbHelper.exciseTime=usbHelper.refFormatNowDate();
                                                    if (!(usbHelper.exciseTime-usbHelper.tmp_exciseTime<usbHelper.timelimit &&
                                                            ((Math.abs(Integer.parseInt(usbHelper.componentArray[0])-Integer.parseInt(str[5], 16))>usbHelper.angleLimit)||
                                                                    (Math.abs(Integer.parseInt(usbHelper.componentArray[1])-Integer.parseInt(str[7], 16))>usbHelper.angleLimit)||
                                                                    (Math.abs(Integer.parseInt(usbHelper.componentArray[2])-Integer.parseInt(str[9], 16))>usbHelper.angleLimit)||
                                                                    (Math.abs(Integer.parseInt(usbHelper.componentArray[3])-Integer.parseInt(str[11], 16))>usbHelper.angleLimit)||
                                                                    (Math.abs(Integer.parseInt(usbHelper.componentArray[4])-Integer.parseInt(str[13], 16))>usbHelper.angleLimit))))
                                                    {
                                                        //对舵机位置信息进行整理
                                                        usbHelper.componentArray[0] = Integer.parseInt(str[5], 16) + "";
                                                        usbHelper.componentArray[1] = Integer.parseInt(str[7], 16) + "";
                                                        usbHelper.componentArray[2] = Integer.parseInt(str[9], 16) + "";
                                                        usbHelper.componentArray[3] = Integer.parseInt(str[11], 16) + "";
                                                        usbHelper.componentArray[4] = Integer.parseInt(str[13], 16) + "";
                                                        Log.e("componentArray", "-----------------------------------");
                                                        for (int i = 0; i < 5; i++) {
                                                            Log.e("componentArray", "The Array Contains " + usbHelper.componentArray[i]);
                                                        }
                                                        Log.e("componentArray", "-----------------------------------");
                                                        usbHelper.tmp_exciseTime=usbHelper.exciseTime;
                                                    }
                                                }
                                            } catch (Exception e) {
                                                Log.e("componentArray", String.valueOf(e));
                                            }
                                            break;
                                        case "01"://当前速度
                                            break;
                                        case "02"://当前负载
                                            break;
                                        case "03"://舵机错误状态
                                            usbHelper.componentError[0] = usbHelper.motorErrorHandler("1", Integer.parseInt(str[5], 16));
                                            usbHelper.componentError[1] = usbHelper.motorErrorHandler("2", Integer.parseInt(str[7], 16));
                                            usbHelper.componentError[2] = usbHelper.motorErrorHandler("3", Integer.parseInt(str[9], 16));
                                            usbHelper.componentError[3] = usbHelper.motorErrorHandler("4", Integer.parseInt(str[11], 16));
                                            usbHelper.componentError[4] = usbHelper.motorErrorHandler("5", Integer.parseInt(str[13], 16));
                                            break;
                                        case "04"://当前温度
                                            try {
                                                //对舵机位置信息进行整理
                                                usbHelper.componentTemperature[0] = Integer.parseInt(str[5], 16) + "";
                                                usbHelper.componentTemperature[1] = Integer.parseInt(str[7], 16) + "";
                                                usbHelper.componentTemperature[2] = Integer.parseInt(str[9], 16) + "";
                                                usbHelper.componentTemperature[3] = Integer.parseInt(str[11], 16) + "";
                                                usbHelper.componentTemperature[4] = Integer.parseInt(str[13], 16) + "";
                                                Log.e("componentTemperature", "-----------------------------------");
                                                for (int i = 0; i < 5; i++) {
                                                    Log.e("componentTemperature", "The Array Contains " + usbHelper.componentArray[i]);
                                                }
                                                Log.e("componentTemperature", "-----------------------------------");
                                            } catch (Exception e) {
                                                Log.e("componentTemperature", String.valueOf(e));
                                            }
                                            break;
                                        case "05"://当前电压
                                            break;
                                        default:
                                            break;
                                    }
                                }
                            }
                        }
                    }
                }


            }catch(Exception ex)
            {
                Debuger.dialogError("UsbService","ReceiveHandler.handlermessage.err  "+ex.getMessage());
            }
        }// ~
    }



    /**
     * 根据下位机按键报文操作
     * mode=1：主动模式
     * mode=2：被动模式
     * mode=3：开始
     * mode=4：停止
     *
     * @param buttonType
     */
    public void ButtonMode(int buttonType) {
        if (buttonType == 1) //主从模式
        {
            if (usbHelper.getRunningActivityName().equals(".U3D.u3dPlayer")) {
                try {
                    //发送退出u3d广播
                    Intent i = new Intent("com.example.U3D_BROADCAST");
                    i.putExtra("U3D", "stopU3D");
                    i.putExtra("buttonType", "1");
                    UserData.getContext().sendBroadcast(i);
                } catch (Exception e) {
                    Log.e("u3d", e.toString());
                }
            } else {
                MasterSlaveActivity.MSActionStart(UserData.getContext());
                byte[] bytes = new byte[6];
                bytes[0] = (byte) 0xff;
                bytes[1] = (byte) 0xff;
                bytes[2] = (byte) 0x0b;//ID
                bytes[3] = (byte) 0x06;//长度
                bytes[4] = (byte) 0x01;//模式
                sendData(bytes);//aChangeMode
                Log.e("ChangeMode", "主从模式");
            }
        } else if (buttonType == 2) //手套操
        {
            if (usbHelper.getRunningActivityName().equals(".U3D.u3dPlayer")) {
                try {
                    //发送退出u3d广播
                    Intent i = new Intent("com.example.U3D_BROADCAST");
                    i.putExtra("U3D", "stopU3D");
                    i.putExtra("buttonType", "2");
                    UserData.getContext().sendBroadcast(i);
                } catch (Exception e) {
                    Log.e("u3d", e.toString());
                }
            } else {
                ExerciseActivity.ExerciseActionStart(UserData.getContext());//切换手套操主从模式
                byte[] bytes = new byte[6];
                bytes[0] = (byte) 0xff;
                bytes[1] = (byte) 0xff;
                bytes[2] = (byte) 0x0b;//ID
                bytes[3] = (byte) 0x06;//长度
                bytes[4] = (byte) 0x02;//模式
                sendData(bytes);//aChangeMode
                Log.e("ChangeMode", "手套操");
            }
        } else if (buttonType == 3) //开始
        {
            if (usbHelper.getRunningActivityName().equals(".U3D.u3dPlayer")) {
                try {
                    //暂停广播
                    Intent i = new Intent("com.example.U3D_BROADCAST");
                    i.putExtra("U3D", "pauseU3D");
                    UserData.getContext().sendBroadcast(i);
                } catch (Exception e) {
                    Log.e("u3d", e.toString());
                }
            }
            Log.e("u3d", "开始");

        } else if (buttonType == 4) //停止
        {
            if (usbHelper.getRunningActivityName().equals(".U3D.u3dPlayer")) {
                try {
                    //暂停广播
                    Intent i = new Intent("com.example.U3D_BROADCAST");
                    i.putExtra("U3D", "pauseU3D");
                    UserData.getContext().sendBroadcast(i);
                } catch (Exception e) {
                    Log.e("u3d", e.toString());
                }
            }
            Log.e("u3d", "暂停");
        }

    }





}

