package com.sheldon.factory.pasv.handler;

import com.sheldon.model.FtpSession;
import com.sheldon.model.FtpState;
import com.sheldon.server.supervise.ClientSupervise;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.log4j.Log4j2;

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

}