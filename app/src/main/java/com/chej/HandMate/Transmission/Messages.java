package com.chej.HandMate.Transmission;

/**
 * Created by samael on 2017/9/21.
 */

public class Messages {
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
    public byte[] dConfigData(String data)
    {
        String[] str = data.split("\\ ");
        byte[] bytes = new byte[30];
        bytes[0] = (byte) 0xff;
        bytes[1] = (byte) 0xff;
        bytes[2] = (byte) 0x26;//ID
        bytes[3] = (byte) 0x30;//长度
        bytes[4] = (byte)Integer.parseInt(str[0]);
        bytes[5] = (byte)Integer.parseInt(str[1]);
        bytes[6] = (byte)Integer.parseInt(str[2]);
        bytes[7] = (byte)Integer.parseInt(str[3]);
        bytes[8] = (byte)Integer.parseInt(str[4]);
        bytes[9] = (byte)Integer.parseInt(str[5]);
        bytes[10] = (byte)Integer.parseInt(str[6]);
        bytes[11] = (byte)Integer.parseInt(str[7]);
        bytes[12] = (byte)Integer.parseInt(str[8]);
        bytes[13] = (byte)Integer.parseInt(str[9]);
        bytes[14] = (byte)Integer.parseInt(str[10]);
        bytes[15] = (byte)Integer.parseInt(str[11]);
        bytes[16] = (byte)Integer.parseInt(str[12]);
        bytes[17] = (byte)Integer.parseInt(str[13]);
        bytes[18] = (byte)Integer.parseInt(str[14]);
        bytes[19] = (byte)Integer.parseInt(str[15]);
        bytes[20] = (byte)Integer.parseInt(str[16]);
        bytes[21] = (byte)Integer.parseInt(str[17]);
        bytes[22] = (byte)Integer.parseInt(str[18]);
        bytes[23] = (byte)Integer.parseInt(str[19]);
        bytes[24] = (byte)Integer.parseInt(str[20]);
        bytes[25] = (byte)Integer.parseInt(str[21]);
        bytes[26] = (byte)Integer.parseInt(str[22]);
        bytes[27] = (byte)Integer.parseInt(str[23]);
        bytes[28] = (byte)Integer.parseInt(str[24]);
        bytes[29] = (byte) ~(bytes[2] + bytes[3] + bytes[4] + bytes[5] + bytes[6] + bytes[7]
                + bytes[8] + bytes[9] + bytes[10] + bytes[11] + bytes[12] + bytes[13] + bytes[14]
                + bytes[15] + bytes[16] + bytes[17] + bytes[18] + bytes[19] + bytes[20] + bytes[21]
                + bytes[22] + bytes[23] + bytes[24] + bytes[25] + bytes[26] + bytes[27] + bytes[28]);
        return bytes;
    }
    /**
     * 当进入服务模式时，通知选择的服务模式和状态，此时下位机进入服务模式后停止运动。
     当退出服务模式后恢复运动，并使能最后发送的配置项。
     * @param SVCMode 0：NULL1：版本升级2：网络状态3：部件状态4：运动配置
     * @param ModeStatus 0：退出 1：进入
     * @return
     */
    public byte[] cSVCMode(int SVCMode,int ModeStatus){
        byte[]b=new byte[7];
        b[0]=(byte)0xff;
        b[1]=(byte)0xff;
        b[2]=(byte)0x14;//ID
        b[3]=(byte)0x07;//长度
        b[4]=(byte)SVCMode;
        b[5]=(byte)ModeStatus;
        b[6]=(byte) ~(b[2]+b[3]+b[4]+b[5]);
        return b;
    }
}
