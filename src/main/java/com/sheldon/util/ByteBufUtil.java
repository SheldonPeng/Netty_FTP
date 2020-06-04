package com.sheldon.util;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.nio.charset.StandardCharsets;

/**
 * @Description: 转换工具
 * @Author: SheldonPeng
 * @Date: 2020-06-04 11:30
 */
public class ByteBufUtil {


    public static ByteBuf parse(String content){

        byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
        ByteBuf ret = Unpooled.buffer(bytes.length);
        ret.writeBytes(bytes);
        return ret;
    }

}