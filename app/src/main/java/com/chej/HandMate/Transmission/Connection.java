package com.chej.HandMate.Transmission;

import android.app.ActivityManager;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.WindowManager;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.charset.Charset;
import java.util.Date;

import com.chej.HandMate.ExerciseActivity;
import com.chej.HandMate.IMyAidlInterface;
import com.chej.HandMate.MasterSlaveActivity;
import com.chej.HandMate.Model.MyCustomDialog;
import com.chej.HandMate.Model.users.UserData;
import com.chej.HandMate.ShutDownActivity;

import static android.content.Context.ACTIVITY_SERVICE;

/**
 * 建立上下位机之间的连接
 */

//建立底层WIFI连接
public class Connection {
    private String ip ="192.168.4.1";//服务器ip
    private String port="6000";//服务器端口


    public boolean isConnected = false;
    public boolean heartBeat = false;
    public long beatTime = 0;//心跳间隔
    public boolean machineConnection = false;
    public boolean machineException = false;
    public boolean socketReadingStatus = false;

    Socket mSocket = null;
    PrintWriter printWriter = null;
    InputStream in;
    OutputStream out=null;

    private ReceiveHandler receiveHandler = new ReceiveHandler();
    private BackHandler backHandler=new BackHandler();


    Thread receiverThread;
    Thread sendThread;
    Thread ExerciseThread;
    public static float angleFromDownStream = 0;
    public int fingerNumber =0;
    private int score = 0;
    private int powerInfo=0;//下位机电量
    public byte[] messageToDevice;//向下位机发送的角度报文
    public String[] fingerArray = {"0","0","0","0","0"};
    public String[] componentArray = {"0","0","0","0","0"};
    String strResult=null;//接收报文十进制
    String hexResult=null;//接收报文十六进制

    /**
     * 获取配置信息
     */
    SharedPreferences userSettings= UserData.getContext().getSharedPreferences("setting", 0);
    public String[] configArray={
            userSettings.getString("thumbFlat","10"),
            userSettings.getString("foreFlat","10"),
            userSettings.getString("middleFlat","10"),
            userSettings.getString("ringFlat","10"),
            userSettings.getString("littleFlat","10"),
            userSettings.getString("thumbMiddle","110"),
            userSettings.getString("foreMiddle","110"),
            userSettings.getString("middleMiddle","110"),
            userSettings.getString("ringMiddle","110"),
            userSettings.getString("littleMiddle","110"),
            userSettings.getString("thumbFist","120"),
            userSettings.getString("foreFist","140"),
            userSettings.getString("middleFist","140"),
            userSettings.getString("ringFist","140"),
            userSettings.getString("littleFist","120")};

    private IMyAidlInterface iMyAidlInterface;

    /**
     * 启动连接线程.
     */

    public Connection(){

    }


    //启动连接WIFI线程
    public void connectThread() {

        //判断WIFI是否已经连接,如果没有连接，则启动连接线程
        if (!isConnected) {
            new Thread(new Runnable() {

                @Override
                public void run() {

                    //启动线程，并启动Looper对事件进行监控和处理
                    Looper.prepare();

                    //在新建立的线程中连接无线WIFI
                    connectServer(ip, port);

                }
            }).start();

        }
    }

    //连接WIFI线程
    private void connectServer(String ip, String port) {

        try {

            Log.e("MainActivity", "--->>start connect  server !" + ip + "," + port);

            try {
                //声明Socket连接地址
                SocketAddress sockaddr = new InetSocketAddress(ip, Integer.parseInt(port));
                //声明socket连接事例
                mSocket = new Socket();
                //尝试连接socket, socket连接超时设置为1000ms
                mSocket.connect(sockaddr, 1000);
                mSocket.setKeepAlive(true);
                isConnected=true;

            }catch (IOException e1) {
                //如果连接出现异常，重置连接判断flag为false。
                isConnected = false;
                //关闭连接socket
                mSocket.close();
                //将连接socket赋值为空
                mSocket=null;
                Log.e("MainActivity", "The socket is closed!");
                Log.e("MainActivity", String.valueOf(e1));
            }

            Log.e("MainActivity", "--->>end connect  server!");
            Log.e("MainActivity", "--->>end connect  server!");
            Log.e("MainActivity", "--->>end connect  server!");

            //获取stream向下位机输出的实例
            out = mSocket.getOutputStream();
            Log.e("MainActivity", "I got output stream");
            //获取socket输入的
            in = mSocket.getInputStream();
            Log.e("MainActivity", "I got input stream");
            //获取stream向下位机输出的writer(字符流用)
            printWriter = new PrintWriter(new BufferedWriter(
                    new OutputStreamWriter(out,
                            Charset.forName("gb2312"))));
            /**
             * 系统开机检测连接
             */
            //读取数据流中的数据
            byte[] result =readFromInputStream(in);
            hexResult = bytesToHexString(result);
            String [] str = hexResult.split("\\ ");

            //发送第一条空信息以启动信息接收机制,在receiveData中启动MyReceiverRunnable。
            receiveHandler.sendEmptyMessage(2);
            Log.e("RECEIVE", "START");


        } catch (Exception e) {
            isConnected = false;
            Log.e("Connection", "连接失败");
            Log.e("Connection", String.valueOf(e));
        }

    }



    //向下位机发送数据(字符流)
    public void SendString( String context) {

        // sendThread.start();
        try {

            if (printWriter == null || context == null) {

                if (printWriter == null) {
                    Log.e("Connection", "连接失败");
                    return;
                }
                if (context == null) {
                    Log.e("Connection", "连接失败");
                    return;
                }
            }

            printWriter.print(context);
            printWriter.flush();
            Log.e("Connection", "--->> client send data!");
        } catch (Exception e) {
            Log.e("Connection", "--->> send failure!" + e.toString());

        }
    }

    //向下位机发送byte（字节流）
    public void SendByte( byte[] context) {
        try {
            if (mSocket != null) {
                out.write(context);//按字节发送
                out.flush();
                Log.e("SEND", "发送成功");
                Log.e("SEND", bytesToHexString(context));
            } else {
                Log.e("SEND", "连接不存在重新连接");
                connectServer(ip,port);
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
            }

        }
    }


    //接受下位机发送数据线程
    private class MyReceiverRunnable implements Runnable {

        public void run() {

            while (true) {

                // Log.i(tag, "---->>client receive....");
                if (isConnected) {
                    if (mSocket != null && mSocket.isConnected()) {
                        //读取数据流中的数据
                        byte[] result =readFromInputStream(in);
                        strResult = bytesToString(result);
                        hexResult = bytesToHexString(result);
                        try {

                            if (!result.equals("")) {
                                Message msg = new Message();
                                msg.what = 1;
                                Bundle data = new Bundle();
                                data.putString("msg", strResult);//十进制结果
                                data.putString("hex",hexResult);//十六进制结果
                                msg.setData(data);
                                receiveHandler.sendMessage(msg);
                            }

                        } catch (Exception e) {
                            // Log.e(tag, "--->>read failure!" + e.toString());
                        }
                    }
                }
                try {
                    Thread.sleep(50L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        }
    }

    /**
     * Convert byte[] to  string.这里我们可以将byte转换成int，然后利用Integer.toString(int)
     * *来转换成10进制字符串。
     * * @param src byte[] data
     * * @return hex string
     */
    public static String bytesToString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder("");
        if (src == null || src.length <= 0) {
            return null;
        }
        for (int i = 0; i < src.length; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv+" ");
        }
        return stringBuilder.toString();
    }

    //转换16进制字符串使用Integer.toHexString(int)
    public static String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder("");
        if (src == null || src.length <= 0) {
            return null;
        }
        for (int i = 0; i < src.length; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv+" ");
        }
        return stringBuilder.toString();
    }

    //消息处理器，对接收到的消息进行处理（上位机接收下位机数据）
    private class ReceiveHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            //对第一条消息进行处理
            receiverData(msg.what);

            //对启动后的消息进行处理
            if (msg.what == 1) {
                String result = msg.getData().get("hex").toString();

                Log.e("Receiver", "result= " + result);
                //连包处理
                String[] strFF = result.split("ff ff ");
                for (int ff = 0; ff < strFF.length; ff++)
                {
                    strFF[ff].trim();
                    Log.e("Receiver", "I am receiving " + strFF[ff]);
                    String[] str = strFF[ff].split("\\ ");
                    /**
                     * 判断收到报文类型
                     */
                    if (str[0].equals("03"))//心跳检测
                    {
                        beatTime=refFormatNowDate();
                        heartBeat=true;
                        sendData(rHeartBeat());
                    }
                    if (str[0].equals("05"))//关机
                    {
                        if(getRunningActivityName().equals(".U3D.u3dPlayer")) {
                            try {
                                //暂停广播
                                Intent i = new Intent("com.example.U3D_BROADCAST");
                                i.putExtra("U3D", "pauseU3D");
                                UserData.getContext().sendBroadcast(i);
                            }catch(Exception e){
                                Log.e("u3d",e.toString());
                            }
                        }
                        Log.e("Receiver", "ID=    " + str[0]);
                        dialogShutDown();
                    }
                    if (str[0].equals("09"))//dButtonInfo改变模式
                    {
                        Log.e("Receiver", "ID=    " + str[0]);
                        try {
                            ButtonMode(Integer.valueOf(str[2]).intValue());
                        } catch (Exception e){
                            Log.e("Connection", String.valueOf(e));
                        }
                    }
                    if (str[0].equals("19"))//dNetStatus网络状态
                    {
                        String NetType = null;//网络类型
                        switch (str[2]){
                            case "00":NetType="WIFI";
                                break;
                            case "01":NetType="Zigbee";
                                break;
                            case "02":NetType="AX-12Bus";
                                break;
                        }
                        String rightStatus = "未连接";
                        String leftStatus ="未连接";
                        if(str[3].equals("01"))
                        {
                            rightStatus="连接正常";
                        }
                        if(str[4].equals("00"))
                        {
                            leftStatus="连接正常";
                        }
                        dialogError("手套"+NetType+"连接状态","右手："+rightStatus+"\n"+"左手："+leftStatus);
                    }
                    if (str[0].equals("20"))//dPowerinfo下位机电量
                    {
                        Log.e("Receiver", "ID=    " + str[0]);
                        try {
                            int a =Integer.parseInt(str[2], 16);
                            powerInfo = Integer.valueOf(a).intValue();
                            Log.e("PowerInfo", "下位机电量为   " + powerInfo);
                        }catch (Exception e){
                            Log.e("PowerInfo", String.valueOf(e));
                        }
                    }
                    if(str[0].equals("25"))//GCU向AWS发送配置报文请求。
                    {
                        sendData(dConfigData());
                    }
                    if (str[0].equals("10"))//下位机向上位机发送角度信息
                    {
                        if(str.length==8) {
                            try {
                                Log.e("Receiver", "ID=    " + str[0]);
                                String[] str2 = msg.getData().get("msg").toString().split("\\ ");
                                //手指序号
                                fingerNumber = Integer.parseInt(str2[2]);
                                //手指运动角度
                                angleFromDownStream = Float.parseFloat(str2[5]);
                                //对手指信息进行整理
                                fingerArray[0] = Integer.parseInt(str[2], 16) + "";
                                fingerArray[1] = Integer.parseInt(str[3], 16) + "";
                                fingerArray[2] = Integer.parseInt(str[4], 16) + "";
                                fingerArray[3] = Integer.parseInt(str[5], 16) + "";
                                fingerArray[4] = Integer.parseInt(str[6], 16) + "";
                                Log.e("fingerArray", "-----------------------------------");
                                for (int i = 0; i < 5; i++) {
                                    Log.e("fingerArray", "The Array Contains " + fingerArray[i]);
                                }
                                Log.e("fingerArray", "-----------------------------------");

                            } catch (Exception e) {
                                Log.e("Connection", String.valueOf(e));
                            }
                        }
                    }
                    if(str[0].equals("21"))//部件信息
                    {
                        if (str.length == 15)
                        {
                            if (str[2].equals("00"))//舵机
                            {

                                switch (str[3]) {
                                    case "00"://当前位置
                                        try {
                                            //对舵机位置信息进行整理
                                            componentArray[0] = Integer.parseInt(str[5], 16) + "";
                                            componentArray[1] = Integer.parseInt(str[7], 16) + "";
                                            componentArray[2] = Integer.parseInt(str[9], 16) + "";
                                            componentArray[3] = Integer.parseInt(str[11], 16) + "";
                                            componentArray[4] = Integer.parseInt(str[13], 16) + "";
                                            Log.e("componentArray", "-----------------------------------");
                                            for (int i = 0; i < 5; i++) {
                                                Log.e("componentArray", "The Array Contains " + componentArray[i]);
                                            }
                                            Log.e("componentArray", "-----------------------------------");
                                        } catch (Exception e) {
                                            Log.e("componentArray", String.valueOf(e));
                                        }
                                        break;
                                    case "01"://当前速度
                                        break;
                                    case "02"://当前负载
                                        break;
                                    case "03"://舵机错误状态
                                        motorErrorHandler("1", Integer.parseInt(str[5], 16));
                                        motorErrorHandler("2", Integer.parseInt(str[7], 16));
                                        motorErrorHandler("3", Integer.parseInt(str[9], 16));
                                        motorErrorHandler("4", Integer.parseInt(str[11], 16));
                                        motorErrorHandler("5", Integer.parseInt(str[13], 16));
                                        break;
                                    case "04"://当前温度
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
        }
    }

    //舵机错误处理
    private void motorErrorHandler(String motorNum,int code)
    {
        if((code&1)==1)
        {
            dialogError("舵机错误","舵机"+motorNum+"号发生错误\n"+"错误原因："+"输入电压错误");
        }
        else if((code&2)==2)
        {
            dialogError("舵机错误","舵机"+motorNum+"号发生错误\n"+"错误原因："+"目标角度超出范围");
        }
        else if((code&4)==4)
        {
            dialogError("舵机错误","舵机"+motorNum+"号发生错误\n"+"错误原因："+"舵机温度过高");
        }
        else if((code&8)==8)
        {
            dialogError("舵机错误","舵机"+motorNum+"号发生错误\n"+"错误原因："+"角度范围设置错误");
        }
        else if((code&16)==16)
        {
            dialogError("舵机错误","舵机"+motorNum+"号发生错误\n"+"错误原因："+"校验和错误");
        }
        else if((code&32)==32)
        {
            dialogError("舵机错误","舵机"+motorNum+"号发生错误\n"+"错误原因："+"舵机超负荷");
        }
        else if((code&64)==64)
        {
            dialogError("舵机错误","舵机"+motorNum+"号发生错误\n"+"错误原因："+"舵机超负荷");
        }
        else if((code&128)==128)
        {
            dialogError("舵机错误","舵机"+motorNum+"号发生错误\n"+"错误原因："+"发生指令错误");
        }
    }
    //反向处理 U3D→下位机
    private class BackHandler extends Handler {
        @Override
        public void handleMessage(Message message) {
            super.handleMessage(message);
            sendData(messageToDevice);

        }
    }

    /**
     * 当连接到服务器时,可以触发接收事件.
     */
    private void receiverData(int flag) {

        if (flag == 2) {
            // mTask = new ReceiverTask();
            receiverThread = new Thread(new MyReceiverRunnable());
            receiverThread.start();

            Log.e("MainActivity", "--->>socket 连接成功!");
            Log.e("MainActivity", "--->>socket 连接成功!");
            Log.e("MainActivity", "--->>socket 连接成功!");
            Log.e("MainActivity", "--->>socket 连接成功!");

            isConnected = true;

        }

    }


    //开启新线程发送数据
    public void sendData(byte[] data) {
        MySendRunnable mySendRunnable1=new MySendRunnable(data);
        sendThread=new Thread(mySendRunnable1);
        //sendThread = new Thread(new MySendRunnable(data));
        sendThread.start();
        isConnected = true;
    }

    //开启新线程发送手套操（上→下）
    public void sendExercise(byte[] data) {
        MySendRunnable mySendRunnable=new MySendRunnable(data);
        ExerciseThread=new Thread(mySendRunnable);
        //ExerciseThread = new Thread(new MySendRunnable(data));
        ExerciseThread.start();
    }

    //底层消息处理，如果消息在等待，则进行相应处理
    public byte[] readFromInputStream(InputStream in) {
        int count = 0;
        byte[] inDatas = null;
        try {
            while (count == 0) {
                count = in.available();
            }
            inDatas = new byte[count];
            in.read(inDatas);
            return inDatas;//new String(inDatas, "gb2312");
        } catch (Exception e) {
            Log.e("Connection","Error from Connection!!!");
            e.printStackTrace();
        }
        return null;
    }


    //显示用时
    public long refFormatNowDate() {
        Date date = new Date(System.currentTimeMillis() - 3600000 * 24 * 140);
        long time = System.currentTimeMillis();
        //Log.e("TIME",time+"");
        return time;
    }

    /**根据下位机按键报文操作
     * mode=1：主动模式
     * mode=2：被动模式
     * mode=3：开始
     * mode=4：停止
     * @param buttonType
     */
    public void ButtonMode(int buttonType) {
        if (buttonType == 1) //主从模式
        {
            if(getRunningActivityName().equals(".U3D.u3dPlayer")) {
                try {
                    //发送退出u3d广播
                    Intent i = new Intent("com.example.U3D_BROADCAST");
                    i.putExtra("U3D", "stopU3D");
                    i.putExtra("buttonType","1");
                    UserData.getContext().sendBroadcast(i);
                }catch(Exception e){
                    Log.e("u3d",e.toString());
                }
            }else {
                MasterSlaveActivity.MSActionStart(UserData.getContext());
                byte[] bytes = new byte[6];
                bytes[0] = (byte) 0xff;
                bytes[1] = (byte) 0xff;
                bytes[2] = (byte) 0x07;//ID
                bytes[3] = (byte) 0x06;//长度
                bytes[4] = (byte) 0x01;//模式
                sendData(bytes);
                Log.e("ChangeMode", "主从模式");
            }
        } else if (buttonType == 2) //手套操
        {
            if(getRunningActivityName().equals(".U3D.u3dPlayer")) {
                try {
                    //发送退出u3d广播
                    Intent i = new Intent("com.example.U3D_BROADCAST");
                    i.putExtra("U3D", "stopU3D");
                    i.putExtra("buttonType","2");
                    UserData.getContext().sendBroadcast(i);
                }catch(Exception e){
                    Log.e("u3d",e.toString());
                }
            }else {
                ExerciseActivity.ExerciseActionStart(UserData.getContext());//切换手套操主从模式
                byte[] bytes = new byte[6];
                bytes[0] = (byte) 0xff;
                bytes[1] = (byte) 0xff;
                bytes[2] = (byte) 0x07;//ID
                bytes[3] = (byte) 0x06;//长度
                bytes[4] = (byte) 0x02;//模式
                sendData(bytes);
                Log.e("ChangeMode", "手套操");
            }
        }
        else if (buttonType == 3) //开始
        {
            if(getRunningActivityName().equals(".U3D.u3dPlayer")) {
                try {
                    //暂停广播
                    Intent i = new Intent("com.example.U3D_BROADCAST");
                    i.putExtra("U3D", "pauseU3D");
                    UserData.getContext().sendBroadcast(i);
                }catch(Exception e){
                    Log.e("u3d",e.toString());
                }
            }
            Log.e("u3d", "开始");

        }
        else if (buttonType == 4) //停止
        {
            if(getRunningActivityName().equals(".U3D.u3dPlayer")) {
                try {
                    //暂停广播
                    Intent i = new Intent("com.example.U3D_BROADCAST");
                    i.putExtra("U3D", "pauseU3D");
                    UserData.getContext().sendBroadcast(i);
                }catch(Exception e){
                    Log.e("u3d",e.toString());
                }
            }
            Log.e("u3d", "暂停");
        }

    }

    //获取顶层Activity名称
    public String getRunningActivityName(){
        ActivityManager manager = (ActivityManager) UserData.getContext().getSystemService(ACTIVITY_SERVICE);
        String runningActivity=manager.getRunningTasks(1).get(0).topActivity.getShortClassName();
        return runningActivity;
    }

    protected void dialogShutDown() {
        MyCustomDialog.Builder builder = new MyCustomDialog.Builder(UserData.getContext());
        builder.setMessage("确定要关机吗？");
        builder.setTitle("提示");
        builder.setPositiveButton("是", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(getRunningActivityName().equals(".U3D.u3dPlayer")) {
                    try {
                        //关机广播
                        Intent i = new Intent("com.example.U3D_BROADCAST");
                        i.putExtra("U3D", "shutDown");
                        UserData.getContext().sendBroadcast(i);
                    }catch(Exception e){
                        Log.e("u3d",e.toString());
                    }
                }
                ShutDownActivity.shutDownStart(UserData.getContext());
                dialog.dismiss();
            }
        });
        builder.setNegativeButton("否", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

            }
        });
        Dialog dialog=builder.create();
        dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        //builder.create().show();
        dialog.show();
    }
    //报错信息
    protected void dialogError(String errorTitle,String errorMessage) {
        MyCustomDialog.Builder builder = new MyCustomDialog.Builder(UserData.getContext());
        builder.setTitle(errorTitle);
        builder.setMessage(errorMessage);
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setNegativeButton("忽略", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

            }
        });
        Dialog dialog=builder.create();
        dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        //builder.create().show();
        dialog.show();
    }
    /**
     * rConnectGCU
     * 请求连接报文
     * AWS->GCU
     * 报文格式：
     Index	Length	Parameter	Value range   Comment
     0	       1	              0xFF	       起始1
     1	       1	              0xFF	       起始2
     2	       1	   ID	      0x01	       报文ID
     3	       1	  Size	      0x05	       报文长度
     4	       1	Check Code 	               校验码
     */
    public byte[] rConnectGCU(){
        byte[]b=new byte[5];
        b[0]=(byte)0xff;
        b[1]=(byte)0xff;
        b[2]=(byte)0x01;//ID
        b[3]=(byte)0x05;//长度
        b[4]=(byte) ~(b[2]+b[3]);
        return b;
    }

    //心跳检测回复报文
    public byte[] rHeartBeat(){
        byte[]b=new byte[5];
        b[0]=(byte)0xff;
        b[1]=(byte)0xff;
        b[2]=(byte)0x04;//ID
        b[3]=(byte)0x05;//长度
        b[4]=(byte) ~(b[2]+b[3]);
        return b;
    }
    //向GCU请求当前网络状态
    public byte[] rNetStatus()
    {
        byte[]b=new byte[6];
        b[0]=(byte)0xff;
        b[1]=(byte)0xff;
        b[2]=(byte)0x18;//ID
        b[3]=(byte)0x06;//长度
        b[4]=(byte)0x01;//网络类型:  0：WIFI 1：Zigbee 2：AX-12Bus
        b[5]=(byte) ~(b[2]+b[3]+b[4]);
        return b;
    }

    /**
     * 用户选择手套类型时，上位机通知下位机传送选择的手套数据
     * @param gloveNum 手套编号，0无手套数据 1 左手套数据 2 右手套数据
     * @return 报文
     */
    public  byte[] dGloveSelect(int gloveNum)
    {
        byte[]b=new byte[6];
        b[0]=(byte)0xff;
        b[1]=(byte)0xff;
        b[2]=(byte)0x27;//ID
        b[3]=(byte)0x06;//长度
        b[4]=(byte)gloveNum;//当前手套选择: 0x00 无手套选择 0x01 左手套选择 0x02 右手套选择
        b[5]=(byte) ~(b[2]+b[3]+b[4]);
        return b;
    }
    /**
     * 向下位机发送配置信息
     * @return 报文
     */
    public byte[] dConfigData()
    {
        byte[] bytes = new byte[30];
        bytes[0] = (byte) 0xff;
        bytes[1] = (byte) 0xff;
        bytes[2] = (byte) 0x26;//ID
        bytes[3] = (byte) 0x30;//长度
        bytes[4] = (byte)Integer.parseInt(userSettings.getString("thumbFlat","10"));
        bytes[5] = (byte)Integer.parseInt(userSettings.getString("foreFlat","10"));
        bytes[6] = (byte)Integer.parseInt(userSettings.getString("middleFlat","10"));
        bytes[7] = (byte)Integer.parseInt(userSettings.getString("ringFlat","10"));
        bytes[8] = (byte)Integer.parseInt(userSettings.getString("littleFlat","10"));
        bytes[9] = (byte)Integer.parseInt(userSettings.getString("thumbMiddle","110"));
        bytes[10] = (byte)Integer.parseInt(userSettings.getString("foreMiddle","110"));
        bytes[11] = (byte)Integer.parseInt(userSettings.getString("middleMiddle","110"));
        bytes[12] = (byte)Integer.parseInt(userSettings.getString("ringMiddle","110"));
        bytes[13] = (byte)Integer.parseInt(userSettings.getString("littleMiddle","110"));
        bytes[14] = (byte)Integer.parseInt(userSettings.getString("thumbFist","120"));
        bytes[15] = (byte)Integer.parseInt(userSettings.getString("foreFist","140"));
        bytes[16] = (byte)Integer.parseInt(userSettings.getString("middleFist","140"));
        bytes[17] = (byte)Integer.parseInt(userSettings.getString("ringFist","140"));
        bytes[18] = (byte)Integer.parseInt(userSettings.getString("littleFist","120"));
        bytes[19] = (byte)Integer.parseInt(userSettings.getString("thumbStretch","50"));
        bytes[20] = (byte)Integer.parseInt(userSettings.getString("foreStretch","50"));
        bytes[21] = (byte)Integer.parseInt(userSettings.getString("middleStretch","50"));
        bytes[22] = (byte)Integer.parseInt(userSettings.getString("ringStretch","50"));
        bytes[23] = (byte)Integer.parseInt(userSettings.getString("littleStretch","50"));
        bytes[24] = (byte)Integer.parseInt(userSettings.getString("thumbMove","114"));
        bytes[25] = (byte)Integer.parseInt(userSettings.getString("foreMove","114"));
        bytes[26] = (byte)Integer.parseInt(userSettings.getString("middleMove","114"));
        bytes[27] = (byte)Integer.parseInt(userSettings.getString("ringMove","114"));
        bytes[28] = (byte)Integer.parseInt(userSettings.getString("littleMove","114"));
        bytes[29] = (byte) ~(bytes[2] + bytes[3] + bytes[4] + bytes[5] + bytes[6] + bytes[7]
                + bytes[8] + bytes[9] + bytes[10] + bytes[11] + bytes[12] + bytes[13] + bytes[14]
                + bytes[15] + bytes[16] + bytes[17] + bytes[18] + bytes[19] + bytes[20] + bytes[21]
                + bytes[22] + bytes[23] + bytes[24] + bytes[25] + bytes[26] + bytes[27] + bytes[28]);
        return bytes;
    }

}