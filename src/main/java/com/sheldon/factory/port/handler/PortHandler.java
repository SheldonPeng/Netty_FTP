package com.sheldon.factory.port.handler;

import com.sheldon.model.FtpSession;
import com.sheldon.model.FtpState;
import com.sheldon.model.ResponseEnum;
import com.sheldon.server.supervise.ClientSupervise;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.RandomAccessFile;

/**
 * @Description: port模式下逻辑处理器
 * @Author: SheldonPeng
 * @Date: 2020-06-05 09:55
 */
@ChannelHandler.Sharable
@Log4j2
public class PortHandler extends ChannelInboundHandlerAdapter {

    private ChannelHandlerContext ctx;

    public PortHandler(ChannelHandlerContext ctx){
        this.ctx = ctx;
    }

    @Override
    public void channelActive(ChannelHandlerContext childCtx) throws Exception {

        final FtpSession session = ClientSupervise.getSession(ctx.channel().id());
        log.info("PORT模式：成功连接至【{}】",childCtx.channel().remoteAddress());
        session.setCtx(childCtx);
        session.getFtpState().setState(FtpState.READY_TRANSFORM);
        ctx.writeAndFlush(ResponseEnum.PORT_OK.toString());
    }

    @Override
    public void channelRead(ChannelHandlerContext childCtx, Object msg) throws Exception {

        final FtpSession session = ClientSupervise.getSession(ctx.channel().id());
        log.info("接收到【{}】的上传指令",session.getId());
        ByteBuf byteBuf = (ByteBuf) msg;
        File file = (File) session.getFtpState().getData();
        RandomAccessFile accessFile = new RandomAccessFile(file,"rw");
        accessFile.getChannel().write(byteBuf.nioBuffer());
        byteBuf.clear();
        log.info("完成文件上传任务，文件名称为:{}",file.getName());
        ctx.writeAndFlush(ResponseEnum.TRANSFER_COMPLETE.toString());
        childCtx.close();
    }
}