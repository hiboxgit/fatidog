package com.dudu.serialcom.utils;

import static java.lang.Character.getNumericValue;

/**
 * Author: Robert
 * Date:  2017-06-01
 * Copyright (c) 2016,dudu Co.,Ltd. All rights reserved.
 * Desc:
 */
public class ConvertUtils {
    /**
     * @功能: BCD码转为10进制串(阿拉伯数据)
     * @参数: BCD码
     * @结果: 10进制串
     */
    public static String bcd2Str(byte[] bytes) {
        StringBuffer dst = new StringBuffer(bytes.length * 2);
        int tmp = 0x00;
        for (int i = 0; i < bytes.length; i++) {
            tmp = ((bytes[i] & 0xf0) >>> 4)&0x000000FF;
            dst.append(Integer.toHexString(tmp));
            tmp = (bytes[i] & 0x0f)&0x000000FF;
            dst.append((Integer.toHexString(tmp)) );
        }
        return dst.toString();
    }

    /**
     * @功能: 10进制串转为BCD码
     * @参数: 10进制串
     * @结果: BCD码
     */
    public static byte[] str2Bcd(String asc) {
        int len = asc.length();
        int mod = len % 2;
        if (mod != 0) {
            asc = "0" + asc;
            len = asc.length();
        }
        byte abt[] = new byte[len];
        if (len >= 2) {
            len = len / 2;
        }
        byte bbt[] = new byte[len];
        abt = asc.getBytes();
        int j, k;
        for (int p = 0; p < asc.length() / 2; p++) {
            if ((abt[2 * p] >= '0') && (abt[2 * p] <= '9')) {
                j = abt[2 * p] - '0';
            } else if ((abt[2 * p] >= 'a') && (abt[2 * p] <= 'z')) {
                j = abt[2 * p] - 'a' + 0x0a;
            } else {
                j = abt[2 * p] - 'A' + 0x0a;
            }
            if ((abt[2 * p + 1] >= '0') && (abt[2 * p + 1] <= '9')) {
                k = abt[2 * p + 1] - '0';
            } else if ((abt[2 * p + 1] >= 'a') && (abt[2 * p + 1] <= 'z')) {
                k = abt[2 * p + 1] - 'a' + 0x0a;
            } else {
                k = abt[2 * p + 1] - 'A' + 0x0a;
            }
            int a = (j << 4) + k;
            byte b = (byte) a;
            bbt[p] = b;
        }
        return bbt;
    }

    /**
     * 将byte转换为一个长度为8的byte数组，数组每个值代表bit
     */
    public static byte[] getBooleanArray(byte b) {
        byte[] array = new byte[8];
        for (int i = 7; i >= 0; i--) {
            array[i] = (byte)(b & 1);
            b = (byte) (b >> 1);
        }
        return array;
    }

    public   static   char  ascii2Char(byte  ASCII) {
        return  ( char ) ASCII;
    }

    public   static  String ascii2String(byte [] ASCIIs) {
        StringBuffer sb =  new  StringBuffer();
        for  ( int  i =  0 ; i < ASCIIs.length; i++) {
            sb.append(ascii2Char(ASCIIs[i]));
        }
        return  sb.toString();
    }

    public static int big2LittleDian(int value){
        return (value & 0x000000FF)<<24 | (value & 0x0000FF00) << 8 |
                (value & 0x00FF0000) >> 8 | (value & 0xFF000000) >> 24;
    }

    public static int bigBytes2LittleDian(byte[] src){
        int result = src[0]<<24 +src[1]<<16 +src[2]<<8+src[3];
        return result;
    }

    public static int littleBytes2LittleDian(byte[] src){
        int result = src[0] +src[1]<<8 +src[2]<<16+src[3]<<24;
        return result;
    }

    public static byte[] getBigEndianBytes(int src){
        byte[] dst = new byte[4];
        dst[0] = (byte)((src>>24)&0x00FF);
        dst[1] = (byte)((src>>16)&0x00FF);
        dst[2] = (byte)((src>>8)&0x00FF);
        dst[3] = (byte)(src&0x00FF);
        return dst;
    }

    public static byte[] getLittleEndianBytes(int src){
        byte[] dst = new byte[4];
        dst[0] = (byte)(src&0x00FF);
        dst[1] = (byte)((src>>8)&0x00FF);
        dst[2] = (byte)((src>>16)&0x00FF);
        dst[3] = (byte)((src>>24)&0x00FF);
        return dst;
    }

    //字节数组转换成字符串， 类似于 2 -> "2"
    public static String bytesArray2Str(byte[] src){
        String dst = "";
        for(int i=0;i<src.length;i++){
            String tmp = String.valueOf(src[i]);
            if(tmp.length()<2){
                tmp = "0"+tmp;
            }
            dst = dst+tmp;
        }
        return dst;
    }

    public static byte[] str2BytesArray(String src){
        byte[] dst = new byte[src.length()];
        for(int i=0;i<src.length();i++){
            int tmp = getNumericValue((int)src.charAt(i));
            dst[i]=(byte)tmp;
        }
        return dst;
    }

    //8bit转换成无符号整型
    public static int byte2Uint(byte src){
        return (src&0x000000FF);
    }

    //16bit转换成无符号整型
    public static int short2Uint(int src){
        return (src&0x0000FFFF);
    }

    //保留低8位
    public static int int2Uint(int src){
        return (src&0x000000FF);
    }

    //2bytes to int, 低索引放高位，大端结构
    public static int byteBig2Int(byte[] src){
        int dst = 0;
        if(src.length == 2){
            int dstH = (src[0]<<8)&0x0000FF00;
            int dstL = src[1]&0x000000FF;

            dst = dstH|dstL;
        }else {
            int dstHHH = (src[0]<<24)&0xFF000000;
            int dstHH = (src[1]<<16)&0x00FF0000;
            int dstH = (src[2]<<8)&0x0000FF00;
            int dstL = src[3]&0x000000FF;

            dst = dstHHH|dstHH|dstH|dstL;
        }

        return dst;
    }

    public static int byteLittle2Int(byte[] src){
        int dst = 0;
        if(src.length == 2){
            int dstH = (src[1]<<8)&0x0000FF00;
            int dstL = src[0]&0x000000FF;

            dst = dstH|dstL;
        }else {
            int dstHHH = (src[3]<<24)&0xFF000000;
            int dstHH = (src[2]<<16)&0x00FF0000;
            int dstH = (src[1]<<8)&0x0000FF00;
            int dstL = src[0]&0x000000FF;

            dst = dstHHH|dstHH|dstH|dstL;
        }

        return dst;
    }
}
