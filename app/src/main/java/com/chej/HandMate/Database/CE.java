package com.chej.HandMate.Database;

/**
 * Created by samael on 2017/10/11.
 */
import org.joda.time.DateTime;
import java.util.ArrayList;

/**
 *
 * @author chenxi
 */
public class CE {
    /// <summary>
    /// 主键ID
    /// </summary>
    private int IID;
    /// <summary>
    /// 事务id
    /// </summary>
    private String UID;

    /**
     * @return the Rid
     */
    public int getIID() {
        return IID;
    }


    public void setIID(int IID) {
        this.IID = IID;
    }

    /**
     * @return the Uid
     */
    public String getUID() {
        return UID;
    }

    /**
     * @param UID the Uid to set
     */
    public void setUID(String UID) {
        this.UID = UID;

    }

    public static String getIds(ArrayList<? extends CE> list) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            CE ce = list.get(i);
            if (i > 0) {
                builder.append(",");
            }
            builder.append("'").append(ce.getUID()).append("'");
        }
        return builder.toString();
    }

    public static void error(String msg){
        System.out.println(msg);
    }
}


