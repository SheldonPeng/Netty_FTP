package com.sheldon.factory.port.config;


import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

/**
 * @Description: port模式客户端连接配置
 * @Author: SheldonPeng
 * @Date: 2020-06-03 22:13
 */
@Configuration
public class PortConfiguration {


    @Bean(name = "portBootstrap")
    public Bootstrap bootstrap(){

        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(workerGroup())
                .channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.SO_KEEPALIVE, true);
        return bootstrap;
    }

    @Bean(name = "portWorkerGroup", destroyMethod = "shutdownGracefully")
    public NioEventLoopGroup workerGroup() {
        return new NioEventLoopGroup(1);
    }
}