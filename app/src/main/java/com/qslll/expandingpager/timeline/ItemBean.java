package com.qslll.expandingpager.timeline;

/**
 * Created by samael on 2017/3/28.
 */

public class ItemBean {
    public int getStatu() {
        return statu;
    }

    public void setStatu(int statu) {
        this.statu = statu;
    }

    private int statu;
    String time, title,subtitle,ID;

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubTitle() {
        return subtitle;
    }

    public void setSubTitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

}
