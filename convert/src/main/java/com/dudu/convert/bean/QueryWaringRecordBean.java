package com.dudu.convert.bean;

/**
 * Author: Robert
 * Date:  2016-12-16
 * Copyright (c) 2016,dudu Co.,Ltd. All rights reserved.
 * Desc:
 */
public class QueryWaringRecordBean {

    int videoId; //要获取的录像的 id
    int offset; //要获取的分片的数据偏移地址，第一个分片偏移为0， 0<=offset<totalSize
    int size; //期望返回的视频分片的大小
}
