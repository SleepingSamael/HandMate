package com.chej.HandMate.utils;

import java.util.Queue;

/**
 * Created by samael on 2017/12/21.
 */

public class CheckData {
    //
    private static boolean  checkComponentData(Queue<Integer> queue, int limit) {
        if(queue.size()>=3) {
            Integer[] items = queue.toArray(new Integer[3]);
            int avg = (items[0]+items[1]+items[2])/3;
            if(Math.abs(items[0]-avg)<limit) {
                return true;
            }
            return false;
        }else {
            return false;
        }
    }
}
