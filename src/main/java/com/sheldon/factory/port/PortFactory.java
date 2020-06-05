package com.sheldon.factory.port;

import com.sheldon.factory.port.handler.PortHandler;
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

        // 添加port模式逻辑处理器
        portBootstrap.handler(new PortHandler(ctx));
        // 连接至客户端
        portBootstrap.connect(ip, port);
    }


}