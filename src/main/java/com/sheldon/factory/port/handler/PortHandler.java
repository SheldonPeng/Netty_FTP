package com.sheldon.factory.port.handler;

import com.sheldon.model.FtpSession;
import com.sheldon.model.FtpState;
import com.sheldon.model.ResponseEnum;
import com.sheldon.server.supervise.ClientSupervise;
import com.sheldon.util.ByteBufUtil;
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

    private final ChannelHandlerContext ctx;

    public PortHandler(ChannelHandlerContext ctx){
        this.ctx = ctx;
    }

    @Override
    public void channelActive(ChannelHandlerContext childCtx) throws Exception {

        final FtpSession session = ClientSupervise.getSession(ctx.channel().id());
        log.info("PORT模式：成功连接至【{}】",childCtx.channel().remoteAddress());
        session.setCtx(childCtx);
        // 调整用户当前的状态
        session.getFtpState().setState(FtpState.READY_TRANSFORM);
        // 告知客户端port模式连接成功
        ctx.writeAndFlush(ResponseEnum.PORT_OK.toString());
    }

    @Override
    public void channelRead(ChannelHandlerContext childCtx, Object msg) throws Exception {

        // 获取用户信息
        final FtpSession session = ClientSupervise.getSession(ctx.channel().id());
        log.info("接收到【{}】的上传指令",session.getId());
        // 接收文件，并写入服务端文件夹内
        ByteBuf byteBuf = (ByteBuf) msg;
        File file = (File) session.getFtpState().getData();
        if( file != null ){

            ByteBufUtil.receiveFile(byteBuf,file);
            // 告知客户端接收完毕
            ctx.writeAndFlush(ResponseEnum.TRANSFER_COMPLETE.toString());
            childCtx.close();
            return;
        }
        ctx.writeAndFlush(ResponseEnum.UPLOAD_FAIL.toString());

    }
}