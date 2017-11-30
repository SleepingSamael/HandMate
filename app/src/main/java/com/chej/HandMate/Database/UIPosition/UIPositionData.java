package com.chej.HandMate.Database.UIPosition;

/**
 * Created by samael on 2017/11/30.
 */
import com.chej.HandMate.Database.ECDO;

import org.joda.time.DateTime;
public class UIPositionData extends ECDO {
    private String UserId;
    /**
     * 患者ID
     * return 患者ID
     */
    public String getUserId(){
        return UserId;
    }
    /**
     * 患者ID
     *
     */
    public UIPositionData setUserId(String _UserId){
        this.UserId=_UserId;
        return this;
    }
    private int UIName;
    /**
     * 界面名称
     * return 界面名称
     */
    public int getUIName(){
        return UIName;
    }
    /**
     * 界面名称
     *
     */
    public UIPositionData setUIName(int _UIName){
        this.UIName=_UIName;
        return this;
    }
    private String OpInfo;
    /**
     * 操作信息
     * return 操作信息
     */
    public String getOpInfo(){
        return OpInfo;
    }
    /**
     * 操作信息
     *
     */
    public UIPositionData setOpInfo(String _OpInfo){
        this.OpInfo=_OpInfo;
        return this;
    }
    private String Bak;
    /**
     * 附加信息
     * return 附加信息
     */
    public String getBak(){
        return Bak;
    }
    /**
     * 附加信息
     *
     */
    public UIPositionData setBak(String _Bak){
        this.Bak=_Bak;
        return this;
    }

}// ~ 
