package com.qslll.expandingpager.Transmission;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;

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
import java.util.List;

import com.qslll.expandingpager.ExerciseActivity;
import com.qslll.expandingpager.IMyAidlInterface;
import com.qslll.expandingpager.MasterSlaveActivity;
import com.qslll.expandingpager.Model.users.UserData;
import com.qslll.expandingpager.ShutDownActivity;

import static android.content.Context.ACTIVITY_SERVICE;

/**
 * 建立上下位机之间的连接
 */

//建立底层WIFI连接
public class Connection {
    private String ip ="192.168.137.1";//服务器ip
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
    OutputStream out;

    private ReceiveHandler receiveHandler = new ReceiveHandler();
    private BackHandler backHandler=new BackHandler();


    Thread receiverThread;
    Thread sendThread;
    public static float angleFromDownStream = 0;
    public int fingerNumber =0;
    private int score = 0;
    private int powerInfo=0;//下位机电量
    public byte[] messageToDevice;//向下位机发送的角度报文
    public String[] fingerArray = {"0","0","0","0","0"};

    String strResult=null;//接收报文十进制
    String hexResult=null;//接收报文十六进制


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
                    //Log.i(tag, "---->> connect/close server!");

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
            e.printStackTrace();
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
            } else {
                Log.e("SEND", "连接不存在重新连接");
                connectServer(ip,port);
            }
        } catch (Exception e) {
            Log.e("SEND", "send error");
            Log.e("SEND", String.valueOf(e));
            e.printStackTrace();
        } finally {
            Log.e("SEND", "发送完毕");

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
                if (!sendData.equals("")) {
                    SendByte(sendData);

                    Log.e("SEND", "---->>已发送至下位机....");
                }
            } catch (Exception e) {
                Log.e("SendRunnable", "--->>read failure!" + e.toString());
            }

            }
        }



    //从u3d获取score
    public int getScore() {

        if (iMyAidlInterface!=null){
            try {
                score = iMyAidlInterface.getScore();
            } catch (RemoteException e) {
                e.printStackTrace();
            }        }

        return score;
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
                    Log.e("Receiver", "ID=    " + str[0]);
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
                    ShutDownActivity.shutDownStart(UserData.getContext());
                }
                if (str[0].equals("09"))//dButtonInfo改变模式
                {
                    try {
                        ButtonMode(Integer.valueOf(str[2]).intValue());
                    } catch (Exception e){
                    Log.e("Connection", String.valueOf(e));
                }
                }
                if (str[0].equals("20"))//dPowerinfo下位机电量
                {
                    try {
                        int a =Integer.parseInt(str[2], 16);
                        powerInfo = Integer.valueOf(a).intValue();
                        Log.e("PowerInfo", "下位机电量为   " + powerInfo);
                    }catch (Exception e){
                        Log.e("Connection", String.valueOf(e));
                    }
                }
                if (str[0].equals("10"))//下位机向上位机发送角度信息
                {
                    try {
                        String[] str2 = msg.getData().get("msg").toString().split("\\ ");

                        //手指序号
                        fingerNumber = Integer.parseInt(str2[2]);

                        //手指运动角度
                        angleFromDownStream = Float.parseFloat(str2[5]);

                        //对手指信息进行整理
                        System.arraycopy(str2, 4, fingerArray, 0, 5);

                        Log.e("Connection", "-----------------------------------");

                        for (int i = 0; i < 5; i++) {
                            Log.e("Connection", "The Array Contains " + fingerArray[i]);
                        }
                        Log.e("Connection", "-----------------------------------");
                    }catch (Exception e){
                        Log.e("Connection", String.valueOf(e));
                    }
                }
            }
            }

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
            //btnConn.setText("Disconnect");

            isConnected = true;
            // mTask.execute(null);
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
        Thread ExerciseThread=new Thread(mySendRunnable);
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
     * @param buttontype
     */
    public void ButtonMode(int buttontype) {
        if(getRunningActivityName()!="com.qslll.expandingpager.U3D.u3dPlayer") {
            if (buttontype == 1) //主从模式
            {
                MasterSlaveActivity.MSActionStart(UserData.getContext());
                Log.e("ChangeMode", "主从模式");
            } else if (buttontype == 2) //手套操
            {
                ExerciseActivity.ExerciseActionStart(UserData.getContext());//切换手套操主从模式
                Log.e("ChangeMode", "手套操");
            }
        }
    }

    //获取顶层Activity名称
    public String getRunningActivityName(){
        ActivityManager manager = (ActivityManager) UserData.getContext().getSystemService(ACTIVITY_SERVICE);
        String runningActivity=manager.getRunningTasks(1).get(0).topActivity.getClassName();
        return runningActivity;
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
     接口：WIFI
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
}