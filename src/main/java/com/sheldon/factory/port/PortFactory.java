package com.sheldon.factory.port;

import com.sheldon.model.FtpCommand;
import com.sheldon.model.FtpSession;
import com.sheldon.model.FtpState;
import com.sheldon.model.ResponseEnum;
import com.sheldon.server.supervise.ClientSupervise;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.EventLoop;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Description: Port模式连接工厂
 * @Author: SheldonPeng
 * @Date: 2020-06-03 22:12
 */
@Component
@Log4j2
public class PortFactory {


    @Autowired
    private Bootstrap portBootstrap;


    public void connect(ChannelHandlerContext ctx, String param) throws InterruptedException {

        // 解析IP地址和端口
        String[] params = param.split(",");
        String ip = params[0] + "." + params[1] + "." + params[2] + "." + params[3];
        int port = Integer.parseInt(params[4]) * 256 + Integer.parseInt(params[5]);

        // 获取用户FTP状态
        final FtpSession session = ClientSupervise.getSession(ctx.channel().id());
        portBootstrap.handler(new ChannelInboundHandlerAdapter() {
            @Override
            public void channelActive(ChannelHandlerContext channelHandlerContext) throws Exception {
                log.info("PORT模式：成功连接至【{}:{}】",ip,port);
                session.setCtx(channelHandlerContext);
                session.getFtpState().setState(FtpState.READY_TRANSFORM);
                ctx.writeAndFlush(ResponseEnum.PORT_OK.toString());
            }
        });
        // 连接至客户端
        portBootstrap.connect(ip, port);
    }


}