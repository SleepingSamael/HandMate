package com.chej.HandMate.Transmission.USB;

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

import com.chej.HandMate.AdminActivity;
import com.chej.HandMate.ExerciseActivity;
import com.chej.HandMate.MasterSlaveActivity;
import com.chej.HandMate.Model.MyCustomDialog;
import com.chej.HandMate.Model.SetConstant;
import com.chej.HandMate.Model.users.UserData;
import com.chej.HandMate.ShutDownActivity;

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

import static android.content.Context.ACTIVITY_SERVICE;
import static com.chej.HandMate.AdminActivity.voltageToMessage;

/**
 * Created by samael on 2017/9/21.
 */

public class USBHelper {

    public String message=null;
    public boolean heartBeat = false;
    public long beatTime = 0;//心跳间隔
    public long tmp_exciseTime =0;//排除错误数据
    public long exciseTime = 0;//排除错误数据
    public boolean exciseFlag = false;//第一次获取用
    public int angleLimit = 50;//错误数据界限，可更改
    public int timelimit = 100;//错误时间界限，可更改

    public static float angleFromDownStream = 0;
    public int fingerNumber = 0;
    private int score = 0;
    public int powerInfo = 0;//下位机电量
    public String[] fingerArray = {"0", "0", "0", "0", "0"};
    public String[] componentArray = {"0", "0", "0", "0", "0"};//舵机位置
    public String[] componentTemperature = {"0", "0", "0", "0", "0"};//舵机温度
    public String[] componentError = {null, null, null, null, null};//舵机错误信息
    String strResult = null;//接收报文十进制
    String hexResult = null;//接收报文十六进制

    /**
     * 获取配置信息
     */
    SharedPreferences userSettings = UserData.getContext().getSharedPreferences("setting", 0);
    public String[] configArray = {
            userSettings.getString("thumbFlat", "10"),
            userSettings.getString("foreFlat", "10"),
            userSettings.getString("middleFlat", "10"),
            userSettings.getString("ringFlat", "10"),
            userSettings.getString("littleFlat", "10"),
            userSettings.getString("thumbMiddle", "110"),
            userSettings.getString("foreMiddle", "110"),
            userSettings.getString("middleMiddle", "110"),
            userSettings.getString("ringMiddle", "110"),
            userSettings.getString("littleMiddle", "110"),
            userSettings.getString("thumbFist", "120"),
            userSettings.getString("foreFist", "140"),
            userSettings.getString("middleFist", "140"),
            userSettings.getString("ringFist", "140"),
            userSettings.getString("littleFist", "120")};

    public USBHelper() {

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
            stringBuilder.append(hv + " ");
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
            stringBuilder.append(hv + " ");
        }
        return stringBuilder.toString();
    }

    //舵机错误处理
    public String motorErrorHandler(String motorNum, int code) {
        StringBuilder errorMsg = new StringBuilder();
        if ((code & 1) == 1) {
            errorMsg.append("舵机" + motorNum + "号发生错误  " + "错误原因：" + "输入电压错误\n");
        } else if ((code & 2) == 2) {
            errorMsg.append("舵机" + motorNum + "号发生错误  " + "错误原因：" + "目标角度超出范围\n");
        } else if ((code & 4) == 4) {
            errorMsg.append("舵机" + motorNum + "号发生错误  " + "错误原因：" + "舵机温度过高\n");
        } else if ((code & 8) == 8) {
            errorMsg.append("舵机" + motorNum + "号发生错误 " + "错误原因：" + "角度范围设置错误\n");
        } else if ((code & 16) == 16) {
            errorMsg.append("舵机" + motorNum + "号发生错误 " + "错误原因：" + "校验和错误\n");
        } else if ((code & 32) == 32) {
            errorMsg.append("舵机" + motorNum + "号发生错误 " + "错误原因：" + "舵机超负荷\n");
        } else if ((code & 64) == 64) {
            errorMsg.append("舵机" + motorNum + "号发生错误 " + "错误原因：" + "舵机超负荷\n");
        } else if ((code & 128) == 128) {
            errorMsg.append("舵机" + motorNum + "号发生错误 " + "错误原因：" + "发生指令错误\n");
        }
        return errorMsg.toString();
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
            Log.e("Connection", "Error from Connection!!!");
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

    //获取顶层Activity名称
    public String getRunningActivityName() {
        ActivityManager manager = (ActivityManager) UserData.getContext().getSystemService(ACTIVITY_SERVICE);
        String runningActivity = manager.getRunningTasks(1).get(0).topActivity.getShortClassName();
        return runningActivity;
    }

    protected void dialogShutDown() {
        MyCustomDialog.Builder builder = new MyCustomDialog.Builder(UserData.getContext());
        builder.setMessage("确定要关机吗？");
        builder.setTitle("提示");
        builder.setPositiveButton("是", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (getRunningActivityName().equals(".U3D.u3dPlayer")) {
                    try {
                        //关机广播
                        Intent i = new Intent("com.example.U3D_BROADCAST");
                        i.putExtra("U3D", "shutDown");
                        UserData.getContext().sendBroadcast(i);
                    } catch (Exception e) {
                        Log.e("u3d", e.toString());
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
        Dialog dialog = builder.create();
        dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        //builder.create().show();
        dialog.show();
    }

    //报错信息
    protected void dialogError(String errorTitle, String errorMessage) {
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
        Dialog dialog = builder.create();
        dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        //builder.create().show();
        dialog.show();
    }

    /**
     * rConnectGCU
     * 请求连接报文
     * AWS->GCU
     * 报文格式：
     * Index	Length	Parameter	Value range   Comment
     * 0	       1	              0xFF	       起始1
     * 1	       1	              0xFF	       起始2
     * 2	       1	   ID	      0x01	       报文ID
     * 3	       1	  Size	      0x05	       报文长度
     * 4	       1	Check Code 	               校验码
     */
    public byte[] rConnectGCU() {
        byte[] b = new byte[5];
        b[0] = (byte) 0xff;
        b[1] = (byte) 0xff;
        b[2] = (byte) 0x01;//ID
        b[3] = (byte) 0x05;//长度
        b[4] = (byte) ~(b[2] + b[3]);
        return b;
    }

    //心跳检测回复报文
    public byte[] rHeartBeat() {
        byte[] b = new byte[5];
        b[0] = (byte) 0xff;
        b[1] = (byte) 0xff;
        b[2] = (byte) 0x04;//ID
        b[3] = (byte) 0x05;//长度
        b[4] = (byte) ~(b[2] + b[3]);
        return b;
    }

    //向GCU请求当前网络状态
    public byte[] rNetStatus() {
        byte[] b = new byte[6];
        b[0] = (byte) 0xff;
        b[1] = (byte) 0xff;
        b[2] = (byte) 0x19;//ID
        b[3] = (byte) 0x06;//长度
        b[4] = (byte) 0x01;//网络类型:  0：WIFI 1：Zigbee 2：AX-12Bus
        b[5] = (byte) ~(b[2] + b[3] + b[4]);
        return b;
    }

    /**
     * 用户选择手套类型时，上位机通知下位机传送选择的手套数据
     *
     * @param gloveNum 手套编号，0无手套数据 1 左手套数据 2 右手套数据
     * @return 报文
     */
    public byte[] dGloveSelect(int gloveNum) {
        byte[] b = new byte[6];
        b[0] = (byte) 0xff;
        b[1] = (byte) 0xff;
        b[2] = (byte) 0x13;//ID
        b[3] = (byte) 0x06;//长度
        b[4] = (byte) gloveNum;//当前手套选择: 0x00 无手套选择 0x01 左手套选择 0x02 右手套选择
        b[5] = (byte) ~(b[2] + b[3] + b[4]);
        return b;
    }

    /**
     * 向下位机发送配置信息
     *
     * @return 报文
     */
    public byte[] dConfigData(String data) {
        String[] str = data.split("\\ ");
        byte[] bytes = new byte[50];
        bytes[0] = (byte) 0xff;
        bytes[1] = (byte) 0xff;
        bytes[2] = (byte) 0x06;//ID
        bytes[3] = (byte) 0x50;//长度
        bytes[4] = (byte) Integer.parseInt(str[0]);
        bytes[5] = (byte) Integer.parseInt(str[1]);
        bytes[6] = (byte) Integer.parseInt(str[2]);
        bytes[7] = (byte) Integer.parseInt(str[3]);
        bytes[8] = (byte) Integer.parseInt(str[4]);
        bytes[9] = (byte) Integer.parseInt(str[5]);
        bytes[10] = (byte) Integer.parseInt(str[6]);
        bytes[11] = (byte) Integer.parseInt(str[7]);
        bytes[12] = (byte) Integer.parseInt(str[8]);
        bytes[13] = (byte) Integer.parseInt(str[9]);
        bytes[14] = (byte) Integer.parseInt(str[10]);
        bytes[15] = (byte) Integer.parseInt(str[11]);
        bytes[16] = (byte) Integer.parseInt(str[12]);
        bytes[17] = (byte) Integer.parseInt(str[13]);
        bytes[18] = (byte) Integer.parseInt(str[14]);
        bytes[19] = (byte) Integer.parseInt(str[15]);
        bytes[20] = (byte) Integer.parseInt(str[16]);
        bytes[21] = (byte) Integer.parseInt(str[17]);
        bytes[22] = (byte) Integer.parseInt(str[18]);
        bytes[23] = (byte) Integer.parseInt(str[19]);
        bytes[24] = (byte) Integer.parseInt(str[20]);
        bytes[25] = (byte) Integer.parseInt(str[21]);
        bytes[26] = (byte) Integer.parseInt(str[22]);
        bytes[27] = (byte) Integer.parseInt(str[23]);
        bytes[28] = (byte) Integer.parseInt(str[24]);
        bytes[29] = (byte) Integer.parseInt(str[25]);
        bytes[30] = (byte) Integer.parseInt(str[26]);
        bytes[31] = (byte) Integer.parseInt(str[27]);
        bytes[32] = (byte) Integer.parseInt(str[28]);
        bytes[33] = (byte) Integer.parseInt(str[29]);
        bytes[34] = (byte) Integer.parseInt(str[30]);
        bytes[35] = (byte) Integer.parseInt(str[31]);
        bytes[36] = (byte) Integer.parseInt(str[32]);
        bytes[37] = (byte) Integer.parseInt(str[33]);
        bytes[38] = (byte) Integer.parseInt(str[34]);
        bytes[39] = (byte) Integer.parseInt(str[35]);
        bytes[40] = (byte) Integer.parseInt(str[36]);
        bytes[41] = (byte) Integer.parseInt(str[37]);
        bytes[42] = (byte) Integer.parseInt(str[38]);
        bytes[43] = (byte) Integer.parseInt(str[39]);
        bytes[44] = (byte) Integer.parseInt(str[40]);
        bytes[45] = (byte) Integer.parseInt(str[41]);
        bytes[46] = (byte) Integer.parseInt(str[42]);
        bytes[47] = (byte) Integer.parseInt(str[43]);
        bytes[48] = (byte) Integer.parseInt(str[44]);
        bytes[49] = (byte) ~(bytes[2] + bytes[3] + bytes[4] + bytes[5] + bytes[6] + bytes[7]
                + bytes[8] + bytes[9] + bytes[10] + bytes[11] + bytes[12] + bytes[13] + bytes[14]
                + bytes[15] + bytes[16] + bytes[17] + bytes[18] + bytes[19] + bytes[20] + bytes[21]
                + bytes[22] + bytes[23] + bytes[24] + bytes[25] + bytes[26] + bytes[27] + bytes[28]
                + bytes[29] + bytes[30] + bytes[31] + bytes[32] + bytes[33] + bytes[34] + bytes[35]
                + bytes[36] + bytes[37] + bytes[38] + bytes[39] + bytes[40] + bytes[41] + bytes[42]
                + bytes[43] + bytes[44] + bytes[45] + bytes[46] + bytes[47] + bytes[48]);
        return bytes;
    }

    /**
     * 当进入服务模式时，通知选择的服务模式和状态，此时下位机进入服务模式后停止运动。
     * 当退出服务模式后恢复运动，并使能最后发送的配置项。
     *
     * @param SVCMode    0：NULL1：版本升级2：网络状态3：部件状态4：运动配置
     * @param ModeStatus 0：退出 1：进入
     * @return
     */
    public byte[] cSVCMode(int SVCMode, int ModeStatus) {
        byte[] b = new byte[7];
        b[0] = (byte) 0xff;
        b[1] = (byte) 0xff;
        b[2] = (byte) 0x15;//ID
        b[3] = (byte) 0x07;//长度
        b[4] = (byte) SVCMode;
        b[5] = (byte) ModeStatus;
        b[6] = (byte) ~(b[2] + b[3] + b[4] + b[5]);
        return b;
    }

    //向GCU请求舵机状态
    public byte[] rComponentStatus() {
        byte[] b = new byte[5];
        b[0] = (byte) 0xff;
        b[1] = (byte) 0xff;
        b[2] = (byte) 0x22;//ID
        b[3] = (byte) 0x05;//长度
        b[4] = (byte) ~(b[2] + b[3]);
        return b;
    }
    //获取电压值 rGloveInitV
    public byte[] getV(){
        byte[]b=new byte[5];
        b[0]=(byte)0xff;
        b[1]=(byte)0xff;
        b[2]=(byte)0x24;//ID
        b[3]=(byte)0x05;//长度
        b[4]=(byte) ~(b[2]+b[3]);
        return b;
    }
}
