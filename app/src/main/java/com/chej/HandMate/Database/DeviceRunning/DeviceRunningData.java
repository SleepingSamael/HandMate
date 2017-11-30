package com.chej.HandMate.Database.DeviceRunning;

/**
 * Created by samael on 2017/11/30.
 */
import com.chej.HandMate.Database.ECDO;

public class DeviceRunningData extends ECDO {
    private String DeviceId;
    /**
     * 设备ID
     * return 设备ID
     */
    public String getDeviceId(){
        return DeviceId;
    }
    /**
     * 设备ID
     *
     */
    public DeviceRunningData setDeviceId(String _DeviceId){
        this.DeviceId=_DeviceId;
        return this;
    }
    private int DeviceStatus;
    /**
     * 设备状态
     * return 设备状态
     */
    public int getDeviceStatus(){
        return DeviceStatus;
    }
    /**
     * 设备状态
     *
     */
    public DeviceRunningData setDeviceStatus(int _DeviceStatus){
        this.DeviceStatus=_DeviceStatus;
        return this;
    }
    private String DeviceVersion;
    /**
     * 设备版本号
     * return 设备版本号
     */
    public String getDeviceVersion(){
        return DeviceVersion;
    }
    /**
     * 设备版本号
     *
     */
    public DeviceRunningData setDeviceVersion(String _DeviceVersion){
        this.DeviceVersion=_DeviceVersion;
        return this;
    }

}// ~ 
