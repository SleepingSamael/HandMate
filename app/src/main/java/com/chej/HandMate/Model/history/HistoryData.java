package com.chej.HandMate.Model.history;

/**
 * 历史记录类
 */

public class HistoryData {
    private int hid =0;
    private String pid ="pid";
    private String date="date";
    private String time="time";
    private String item="item";
    private int score =0;

    public String getPid(){
        return pid;
    }
    public void setPid(String pid){
        this.pid=pid;
    }
    public int getHid(){
        return hid;
    }
    public void setHid(int hid){
        this.hid=hid;
    }
    public String getDate(){
        return date;
    }
    public void setDate(String date){
        this.date=date;
    }
    public String getTime(){
        return time;
    }
    public void setTime(String time){
        this.time=time;
    }
    public String getItem(){
        return item;
    }
    public void setItem(String item){
        this.item=item;
    }
    public int getScore(){
        return score;
    }
    public void setScore(int score){
        this.score=score;
    }

}
