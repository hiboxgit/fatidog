package com.dudu.serialcom.reader;

import com.dudu.commonlib.utils.string.ByteTools;
import com.dudu.serialcom.utils.ConvertUtils;

import org.apache.commons.codec.binary.Hex;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * Author: Robert
 * Date:  2016-12-14
 * Copyright (c) 2016,dudu Co.,Ltd. All rights reserved.
 * Desc:
 */
public class FatiDogDataReader implements IComDataReader {
    private static org.slf4j.Logger logger = LoggerFactory.getLogger("FatiDog.serilacom.FatiDogDataReader");
    private static final byte[] FRAME_HEADER = {(byte) 0xFB}; //帧头
    private static final int FRAME_HEADER_MIN_SIZE = 3; //帧头最小长度 （帧头+长度字节）
    private static final int DEFUALT_BUFF_SIZE = 640;//256; //默认的接收缓冲区大小,如果实际超过了这个限制也没关系，因为我们的机制会保证下次的继续接，连接到上一次的数据尾部.只要至少大于一帧数据的长度即可。
    private static final int MAX_FRAME_SIZE = 512;
    private static final int MAX_IMAGE_SLICE_SIZE = 512;
    private static final int FRAME_FIRST_BYTE_UNFOUND = -1; //首字节没有找到
    private static final int MIN_FRAME_SIZE = 15; //最小帧长度

    private InputStream inputStream = null;
    private byte[] recvBuf = null; //串口数据接收缓冲区
    private int pos = 0; //接收缓冲区读指针，没有找到帧头的时候指向接收缓冲区头部，找到接收缓冲区中的帧头之后，指向帧头位置.
    private int end = 0; //接收缓冲区写指针，指向接收缓冲区已经接收到的数据的尾部第一个没有写过的字节.

    private ByteBuffer frameBuf = null; //帧数据接收缓冲区
    private int frameSize = 0; //数据帧长度

    private int readErrorCount = 0; //读数据读错的次数
    private static final int MAX_ERROR = 10; //读数据最多读错次数限制

    public FatiDogDataReader(InputStream inputStream) {

        this.inputStream = inputStream;
        recvBuf = new byte[DEFUALT_BUFF_SIZE];

        clearRecvBuf();
    }

    @Override
    public ByteBuffer readLine() {
//        logger.trace("开始获取一帧数据...");
//        logger.trace("初始状态: pos = {}, end = {},frameSize = {}, recvBuf.length = {},readErrorCount = {}",pos,end,frameSize,recvBuf.length,readErrorCount);
        return findFrameHeader();
//        return pickHead();
    }

    //从串口读一次数据，读到数据的话返回true，没有读到的话返回false;
    private ByteBuffer readComData() {

        if(readErrorCount > MAX_ERROR){
            logger.error("串口连续{}次读不到数据，串口异常，停止读取！！！",MAX_ERROR);
            return null;
        }

        int size = -1;
        try{
            size = inputStream.read(recvBuf, end, (recvBuf.length - end));
        }catch (Exception e) {

            size = -1;
            logger.error("串口读取数据失败: {}",e.toString());
        }

        if(size>0) {
            end = end +size; //将缓冲区指针指向缓冲区尾部

            logger.trace("接收缓冲区读到的原始数据是： {}",String.copyValueOf(Hex.encodeHex(recvBuf)).toUpperCase());
            logger.trace("读到串口原始数据：size = {}, pos = {}, end = {}, readErrorCount = {}",size,pos,end,readErrorCount);
            readErrorCount = 0;
            return findFrameHeader();
        }else{

            readErrorCount++;
            return readComData(); //没有读到数据继续读
        }
    }

    //找到数据帧帧头, 没有找到的话返回-1，找到的话返回相应的索引
    private ByteBuffer findFrameHeader() {

        for (int i = pos; i < end; i++) {
            byte ch = recvBuf[i];
            if (ch == FRAME_HEADER[0]) {
                //如果匹配到开头
                pos = i; //读指针指向帧首字节标识
                logger.trace("找到了数据帧首字节...pos = {}",pos);
                return getFullFrame();
            }
        }
        logger.trace("接收缓冲区没有找到数据帧首字节，需要继续从串口读取...");
        clearRecvBuf();
        return readComData();
    }
//*****************************************************************************
    private ByteBuffer pickHead(){
        if(!hasEnoughBytesForMinFrame()){
            logger.trace("不足一个最小帧长度，需要继续读取...");
            return readComData();
        }

        for (int i = pos; i < end; i++) {
            if (recvBuf[i] == FRAME_HEADER[0]) {
                //如果匹配到开头
                pos = i; //读指针指向帧首字节标识
                logger.trace("找到了数据帧首字节...pos = {}",pos);

                if(!hasEnoughBytesForMinFrame()){
                    logger.trace("不足一个最小帧长度，需要继续读取...");
                    return readComData();
                }

                return afterFrameHeadLoop();
            }
        }
        logger.trace("接收缓冲区没有找到数据帧首字节，需要继续从串口读取...");
        clearRecvBuf();
        return readComData();

    }

    private ByteBuffer afterFrameHeadLoop(){
        if(getFrameSize()){

            if(hasEnoughFrameBytes()){

                if(verifyFrmeBeforeCopy()){

                    if(copyOneFrame()){

                        return frameBuf;
                    }else{
                        logger.error("拷贝数据帧异常！");
                        pos = pos+FRAME_HEADER.length;
                        return pickHead();
                    }
                }else{

                    pos = pos+FRAME_HEADER.length;
                    return pickHead();
                }
            }else{
                return readComData();
            }
        }else{
            pos = pos+FRAME_HEADER.length;
            return pickHead();
        }
    }

    private boolean verifyFrmeBeforeCopy(){
        byte sum = 0;
        for(int i=0;i<frameSize;i++) {

            sum = (byte)(sum+recvBuf[pos+i]);
        }

        if(sum == 0x00){
            return true;
        }
        logger.error("数据帧校验字节校验未通过 -> sum : 0x{}, lastByte : 0x{}",byte2HexString(sum).toUpperCase(),byte2HexString(frameBuf.get(frameSize-1)).toUpperCase());
        logger.error("帧长度异常，当前帧缓冲为：{}",String.copyValueOf(Hex.encodeHex(recvBuf)).toUpperCase());

        return false;
    }

    private boolean getFrameSize(){
        //得到帧长度
        frameSize = ConvertUtils.byteLittle2Int(new byte[]{recvBuf[pos + 1],recvBuf[pos + 2]});

        if(frameSize > 0 && frameSize<MAX_FRAME_SIZE){
            return true;
        }else{
            logger.error("帧长度异常，frameSize{},当前帧缓冲为：{}",frameSize,String.copyValueOf(Hex.encodeHex(recvBuf)).toUpperCase());
            return false;
        }
    }
    private boolean hasEnoughBytesForMinFrame(){
        if((end-pos)>= MIN_FRAME_SIZE){
            return true;
        }else{
            return false;
        }
    }

//*****************************************************************************

    private ByteBuffer getFullFrame() {

        if(hasEnoughFrameHeaderBytes()){ //缓冲区是否足够数据帧头的3个字节？

            if(checkFrameLength()){ //进行帧长度的检查

                if(hasEnoughFrameBytes()){ //是否有足够的完整帧数据 ?

                    if(copyOneFrame()){ //将完整的数据帧从接收缓冲区拷贝到帧缓冲区中

                        if(verifyFrame()){ //校验数据帧尾部的校验字节
                            logger.trace("获取数据帧 OK!!! - frameSize : {}",frameSize);
                            logger.trace("获取数据帧 OK -> 当前进程ID：{},当前线程ID：{}",android.os.Process.myPid(),Thread.currentThread().getId());
                            return frameBuf;
                        }else{

                            return findFrameHeader();
                        }
                    }else{ //如果拷贝失败

                        logger.error("申请帧缓冲区异常，理论上不会跑到这里...");
                        return null;
                    }
                }else { //如果没有的话，还需要再去读
                    logger.trace("当前接收缓冲区剩余数据不足一个完整帧，需要继续从串口读取...");
                    return readComData();
                }
            }else{ //如果枕头不不符合数据帧标准，那么读指针增加，继续寻找下一个AA，也就是帧首字节

                pos = pos+1;
                return findFrameHeader();
            }
        }else{ //不够的话继续读

            return readComData();
        }
    }

    //清空接收缓冲区，和读写指针
    private void clearRecvBuf() {
        pos = 0;
        end = 0;
        frameSize = 0;
        Arrays.fill(recvBuf,(byte)0); //清空接收缓冲区
    }
    //检查帧首字节后面是否足够4个字节？如果足够返回true，否则返回false;
    private boolean hasEnoughFrameHeaderBytes() {

        if(end >= (pos+FRAME_HEADER_MIN_SIZE)) {
            return true;
        }
        return false;
    }

    //帧头校验,帧头匹配的话返回true，不匹配返回false;
    private boolean checkFrameLength() {

        //得到帧长度
        frameSize = ByteTools.bytes2Int(new byte[]{recvBuf[pos + 1],recvBuf[pos + 2]});

        if(frameSize > 0){
            logger.trace("获取数据帧长度成功...frameSize : {}",frameSize);
            return true;
        }else{
            return false;
        }
    }

    //判断当前接收缓冲区中是否有足够字节，达到帧头中长度字节的要求，有的话返回true，没有的话返回false;
    private boolean hasEnoughFrameBytes(){

        if(end >= (pos+frameSize)) {
            return true;
        }
        logger.trace("当前接收缓冲区不足完整一帧");
        return false;
    }

    //将完整的一帧从接收缓冲区拷贝到帧缓冲区中,然后接收缓冲区平移,去除已经拷贝过的完整帧
    private boolean copyOneFrame(){
        logger.trace("数据帧拷贝之前数据状态： pos = {}, end = {}, frameSize = {}",pos,end,frameSize);
        frameBuf = ByteBuffer.allocate(frameSize);
        if(frameBuf == null){
            return false;
        }

        frameBuf.clear(); //这个bytebuff里面有个写指针，拷贝东西进去就会自动移动，如果每次不清一下，下次拷贝的时候会接着上一次的，不会从头开始.
        frameBuf.put(recvBuf, pos, frameSize);

        //执行平移操作，也就是去掉已经拷贝过的数据帧
        pos = pos + frameSize;
        System.arraycopy(recvBuf, pos, recvBuf, 0, (end-pos)); //平移，去掉已经转移到帧缓冲中的数据
        Arrays.fill(recvBuf, (end-pos), recvBuf.length, (byte)0); //接收缓冲区后面的清0

        //更新读写指针位置
        end = end - pos;
        pos = 0;

        logger.trace("数据帧拷贝之后数据状态： pos = {}, end = {}, frameSize = {}",pos,end,frameSize);
        logger.trace("平移之后的接收缓冲区状态：{}",String.copyValueOf(Hex.encodeHex(recvBuf)).toUpperCase());
        logger.trace("平移之后的帧缓冲区状态：{}",String.copyValueOf(Hex.encodeHex(frameBuf.array())).toUpperCase());

        return true;
    }

    //数据帧校验，最后一个字节校验和检查,校验通过返回true，校验不通过返回false;
    private boolean verifyFrame(){

        byte sum = 0;
        for(int i=0;i<frameSize;i++) {

            sum = (byte)(sum+frameBuf.get(i));
        }

        if(sum == 0){
            logger.trace("数据帧校验字节校验通过，得到一个完整正确的数据帧");
            return true;
        }
        logger.error("数据帧校验字节校验未通过 -> sum : 0x{}, lastByte : 0x{}",byte2HexString(sum).toUpperCase(),byte2HexString(frameBuf.get(frameSize-1)).toUpperCase());
        logger.error("frameSize:{},当前接收缓冲区状态：{}",frameSize,String.copyValueOf(Hex.encodeHex(recvBuf)).toUpperCase());
        return false;
    }

    //单字节数据转换成16进制字符串
    private String byte2HexString(byte src) {

        int v = src & 0xFF;
        return Integer.toHexString(v);
    }
}
