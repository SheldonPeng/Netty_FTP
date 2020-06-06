package com.sheldon.util;

import com.sheldon.model.FtpSession;
import com.sheldon.model.ResponseEnum;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.Unpooled;
import io.netty.channel.DefaultFileRegion;
import io.netty.channel.FileRegion;
import lombok.extern.log4j.Log4j2;

import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;

/**
 * @Description: 转换工具
 * @Author: SheldonPeng
 * @Date: 2020-06-04 11:30
 */
@Log4j2
public class ByteBufUtil {


    public static ByteBuf parse(String content){

        byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
        ByteBuf ret = Unpooled.buffer(bytes.length);
        ret.writeBytes(bytes);
        return ret;
    }

    public static void receiveFile(ByteBuf byteBuf , FtpSession session) throws IOException {

        Object data = session.getFtpState().getData();
        if ( data instanceof File){

            File file = (File) data;
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            FileChannel channel = fileOutputStream.getChannel();
            //RandomAccessFile accessFile = new RandomAccessFile(file,"rw");

            //FileChannel channel = accessFile.getChannel();
            channel.write(byteBuf.nioBuffer());

            session.getFtpState().setData(channel);
        } else if(data instanceof FileChannel){

            FileChannel fileChannel = (FileChannel)data;
            fileChannel.write(byteBuf.nioBuffer());
            System.out.println(fileChannel.size());
        }


    }

}