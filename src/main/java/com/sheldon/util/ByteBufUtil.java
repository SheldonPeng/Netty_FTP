package com.sheldon.util;

import com.sheldon.model.ResponseEnum;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.extern.log4j.Log4j2;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
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

    public static void receiveFile(ByteBuf byteBuf , File file) throws IOException {

        RandomAccessFile accessFile = new RandomAccessFile(file,"rw");
        accessFile.getChannel().write(byteBuf.nioBuffer());
        // 清除缓存
        byteBuf.clear();
        log.info("完成文件上传任务，文件名称为:{}",file.getName());
    }

}