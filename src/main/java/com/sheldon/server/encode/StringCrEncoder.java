package com.sheldon.server.encode;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.handler.codec.string.LineSeparator;
import io.netty.util.internal.ObjectUtil;
import lombok.extern.log4j.Log4j2;

import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.List;

/**
*@Description: 自定义CRencoder，用于pipeline尾部自动添加CR标记符
*@Author: SheldonPeng
*@Date: 2020/6/1  15:31
*/
@ChannelHandler.Sharable
@Log4j2
public class StringCrEncoder extends MessageToMessageEncoder<CharSequence> {
    private final Charset charset;

    public StringCrEncoder() {
        this(Charset.defaultCharset());
    }

    public StringCrEncoder(Charset charset) {
        this.charset = (Charset) ObjectUtil.checkNotNull(charset, "charset");
    }

    protected void encode(ChannelHandlerContext ctx, CharSequence msg, List<Object> out) throws Exception {

        if (msg.length() != 0) {
            ByteBuf byteBuf = ByteBufUtil.encodeString(ctx.alloc(), CharBuffer.wrap(msg), this.charset);
            byteBuf.writeBytes(LineSeparator.WINDOWS.value().getBytes());
            out.add(byteBuf);
        }
    }
}