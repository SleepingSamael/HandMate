package com.chej.HandMate.Database;
import org.joda.time.DateTime;
/**
 * Created by samael on 2017/11/30.
 */
public class ECDO extends CE {
    private int isRemove;
    public int getIsRemove() {
        return isRemove;
    }
    public void setIsRemove(int isRemove) {
        this.isRemove = isRemove;
    }
    public DateTime updateDate;
    public String updateDateStr;
    public String getUpdateDateStr() {
        return updateDateStr;
    }
    public void setUpdateDateStr(String updateDateStr) {
        this.updateDateStr = updateDateStr;
    }
    public String getCreateDateStr() {
        return createDateStr;
    }
    public void setCreateDateStr(String createDateStr) {
        this.createDateStr = createDateStr;
    }
    public DateTime getUpdateDate() {
        return updateDate;
    }
    public void setUpdateDate(DateTime updateDate) {
        this.updateDate = updateDate;
        this.updateDateStr = updateDate==null?"":updateDate.toString("yyyy-MM-dd HH:mm:ss");
    }
    public DateTime getCreateDate() {
        return createDate;
    }
    public void setCreateDate(DateTime createDate) {
        this.createDate = createDate;
        this.createDateStr = createDate==null?"":createDate.toString("yyyy-MM-dd HH:mm:ss");
    }

    public DateTime createDate;
    public String createDateStr;

}