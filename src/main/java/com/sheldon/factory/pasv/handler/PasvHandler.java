package com.sheldon.factory.pasv.handler;

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

import java.io.File;
import java.nio.channels.FileChannel;

/**
 * @Description: pasv模式下的连接控制器
 * @Author: SheldonPeng
 * @Date: 2020-06-05 18:19
 */
@Log4j2
@ChannelHandler.Sharable
public class PasvHandler extends ChannelInboundHandlerAdapter {

    private final ChannelHandlerContext ctx;
    private final int port;

    public PasvHandler(ChannelHandlerContext ctx , int port){
        this.ctx = ctx;
        this.port = port;
    }

    @Override
    public void channelActive(ChannelHandlerContext childCtx) throws Exception {

        log.info("【{}】PASV端口连接成功,端口为【{}】",ctx.channel().id(),port);
        FtpSession session = ClientSupervise.getSession(ctx.channel().id());
        session.setCtx(childCtx);
        session.getFtpState().setData(port);
        session.getFtpState().setState(FtpState.READY_TRANSFORM);
    }


    @Override
    public void channelRead(ChannelHandlerContext childCtx, Object msg) throws Exception {

        // 获取用户信息
        final FtpSession session = ClientSupervise.getSession(ctx.channel().id());
        log.info("接收到来自【{}】的上传文件流数据",session.getId());
        // 接收文件，并写入服务端文件夹内
        ByteBuf byteBuf = (ByteBuf) msg;
        if( session.getFtpState().getData() != null ){
            // 告知客户端接收完毕
            ByteBufUtil.receiveFile(byteBuf,session);
            return;
        }
        ctx.writeAndFlush(ResponseEnum.UPLOAD_FAIL.toString());
    }

    @Override
    public void channelInactive(ChannelHandlerContext childCtx) throws Exception {
        log.info(childCtx.channel().remoteAddress() + "完成数据传输操作，已关闭连接通道！");
        ctx.writeAndFlush(ResponseEnum.PASV_TRANSFER_COMPLETE.toString());
    }
}