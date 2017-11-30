package com.chej.HandMate.Database.DeviceLog;
import com.chej.HandMate.Database.ECDO;
/**
 * Created by samael on 2017/10/11.
 */
public class DeviceLogData extends ECDO {
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
    public DeviceLogData setDeviceId(String _DeviceId){
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
    public DeviceLogData setDeviceStatus(int _DeviceStatus){
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
    public DeviceLogData setDeviceVersion(String _DeviceVersion){
        this.DeviceVersion=_DeviceVersion;
        return this;
    }
    private String ErrorTitle;
    /**
     * 日志标题
     * return 日志标题
     */
    public String getErrorTitle(){
        return ErrorTitle;
    }
    /**
     * 日志标题
     *
     */
    public DeviceLogData setErrorTitle(String _ErrorTitle){
        this.ErrorTitle=_ErrorTitle;
        return this;
    }
    private String ErrorMessage;
    /**
     * 日志信息
     * return 日志信息
     */
    public String getErrorMessage(){
        return ErrorMessage;
    }
    /**
     * 日志信息
     *
     */
    public DeviceLogData setErrorMessage(String _ErrorMessage){
        this.ErrorMessage=_ErrorMessage;
        return this;
    }

}// ~
