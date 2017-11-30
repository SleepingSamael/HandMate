package com.chej.HandMate.Database.HandPosition;

/**
 * Created by samael on 2017/11/30.
 */
import com.chej.HandMate.Database.ECDO;

public class HandPositionData extends ECDO {
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
    public HandPositionData setUserId(String _UserId){
        this.UserId=_UserId;
        return this;
    }
    private int HandType;
    /**
     * 手指类型
     * return 手指类型
     */
    public int getHandType(){
        return HandType;
    }
    /**
     * 手指类型
     *
     */
    public HandPositionData setHandType(int _HandType){
        this.HandType=_HandType;
        return this;
    }
    private int Train;
    /**
     * 训练类型
     * return 训练类型
     */
    public int getTrain(){
        return Train;
    }
    /**
     * 训练类型
     *
     */
    public HandPositionData setTrain(int _Train){
        this.Train=_Train;
        return this;
    }
    private String TrainTag;
    /**
     * 训练标签
     * return 训练标签
     */
    public String getTrainTag(){
        return TrainTag;
    }
    /**
     * 训练标签
     *
     */
    public HandPositionData setTrainTag(String _TrainTag){
        this.TrainTag=_TrainTag;
        return this;
    }
    private double F1;
    /**
     * 手指1
     * return 手指1
     */
    public double getF1(){
        return F1;
    }
    /**
     * 手指1
     *
     */
    public HandPositionData setF1(double _F1){
        this.F1=_F1;
        return this;
    }
    private double F2;
    /**
     * 手指2
     * return 手指2
     */
    public double getF2(){
        return F2;
    }
    /**
     * 手指2
     *
     */
    public HandPositionData setF2(double _F2){
        this.F2=_F2;
        return this;
    }
    private double F3;
    /**
     * 手指3
     * return 手指3
     */
    public double getF3(){
        return F3;
    }
    /**
     * 手指3
     *
     */
    public HandPositionData setF3(double _F3){
        this.F3=_F3;
        return this;
    }
    private double F4;
    /**
     * 手指4
     * return 手指4
     */
    public double getF4(){
        return F4;
    }
    /**
     * 手指4
     *
     */
    public HandPositionData setF4(double _F4){
        this.F4=_F4;
        return this;
    }
    private double F5;
    /**
     * 手指5
     * return 手指5
     */
    public double getF5(){
        return F5;
    }
    /**
     * 手指5
     *
     */
    public HandPositionData setF5(double _F5){
        this.F5=_F5;
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
    public HandPositionData setBak(String _Bak){
        this.Bak=_Bak;
        return this;
    }

}// ~ 
