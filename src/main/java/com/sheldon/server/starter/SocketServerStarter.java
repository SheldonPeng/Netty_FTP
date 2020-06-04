package com.sheldon.server.starter;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.net.InetSocketAddress;

/**
*@Description: server启动器
*@Author: SheldonPeng
*@Date: 2020/6/1  15:47
*/
@Component
@Log4j2
@DependsOn("fileFactory")
public class SocketServerStarter {
    private final ServerBootstrap serverBootstrap;

    private final InetSocketAddress tcpPort;

    @Autowired
    public SocketServerStarter(@Qualifier("serverBootstrap") ServerBootstrap serverBootstrap, @Qualifier("tcpSocketAddress") InetSocketAddress tcpPort) {
        this.serverBootstrap = serverBootstrap;
        this.tcpPort = tcpPort;
    }

    private ChannelFuture channelFuture;

    @PostConstruct
    public void start() throws InterruptedException {
        log.info("启动 socket server ,监听端口为：" + tcpPort);
        channelFuture = serverBootstrap.bind(tcpPort).sync();
    }

    @PreDestroy
    public void stop() throws InterruptedException {
        log.info("关闭 socket server ");
        channelFuture.channel().close().sync();
    }
}
