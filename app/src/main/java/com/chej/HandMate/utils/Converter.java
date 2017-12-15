package com.chej.HandMate.utils;

/**
 * Created by samael on 2017/12/15.
 */

public class Converter {
    public static int[] i64_2int(String[] source,int[] indexs){

        int[] target =new int[5];
        for(int i =0;i<5;i++){
            target[i] = Integer.parseInt(source[indexs[i]], 16);
        }
        return target;
    }

    public static void i64_2finger(String[] source,String[] target,int[] indexs){
        Converter.int2finger(Converter.i64_2int(source,indexs),target);
    }
    public static void int2finger(int[] source,String[] target){

        for(int i =0;i<5;i++){
            target[i] = ""+source[i];
        }
    }
}
