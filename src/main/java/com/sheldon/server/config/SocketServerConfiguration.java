package com.sheldon.server.config;

import com.sheldon.server.encode.StringCrEncoder;
import com.sheldon.server.initializer.SocketInitializer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.Scope;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Configuration
@PropertySource("classpath:/server.properties")
@Slf4j
public class SocketServerConfiguration {
    @Value("${boss.count}")
    private int bossCount;

    @Value("${worker.count}")
    private int workerCount;

    @Value("${tcp.port}")
    private int tcpPort;

    @Value("${keep.alive}")
    private boolean keepAlive;

    @Value("${backlog}")
    private int backLog;

    @Value("${lineMaxLength}")
    private int lineMaxLength;

    @Autowired
    private SocketInitializer initializer;

    @Bean(name = "serverBootstrap")
    public ServerBootstrap bootstrap() {
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup(), workerGroup())
                .channel(NioServerSocketChannel.class)
                .handler(new LoggingHandler(LogLevel.DEBUG))
                .childHandler(initializer);

        // 添加option
        Map<ChannelOption<?>, Object> tcpChannelOptions = tcpChannelOptions();
        Set<ChannelOption<?>> keySet = tcpChannelOptions.keySet();
        for (@SuppressWarnings("rawtypes") ChannelOption option : keySet) {
            bootstrap.option(option, tcpChannelOptions.get(option));
        }

        // 添加childOption
        Map<ChannelOption<?>, Object> tcpChildChannelOptions = tcpChildChannelOptions();
        keySet = tcpChannelOptions.keySet();
        for (@SuppressWarnings("rawtypes") ChannelOption option : keySet) {
            bootstrap.childOption(option, tcpChildChannelOptions.get(option));
        }

        return bootstrap;
    }
    @Bean(name = "bossGroup", destroyMethod = "shutdownGracefully")
    public NioEventLoopGroup bossGroup() {
        return new NioEventLoopGroup(bossCount);
    }

    @Bean(name = "workerGroup", destroyMethod = "shutdownGracefully")
    public NioEventLoopGroup workerGroup() {
        return new NioEventLoopGroup(workerCount);
    }

    @Bean(name = "tcpSocketAddress")
    public InetSocketAddress tcpcPort() {
        return new InetSocketAddress(tcpPort);
    }

    @Bean(name = "tcpChannelOptions")
    public Map<ChannelOption<?>, Object> tcpChannelOptions() {
        Map<ChannelOption<?>, Object> options = new HashMap<>(2);
        //options.put(ChannelOption.SO_KEEPALIVE, keepAlive);
        options.put(ChannelOption.SO_BACKLOG, backLog);
        return options;
    }

    @Bean(name = "tcpChildChannelOptions")
    public Map<ChannelOption<?>, Object> tcpChildChannelOptions() {

        Map<ChannelOption<?>, Object> options = new HashMap<>(1);
        options.put(ChannelOption.SO_KEEPALIVE, true);
        return options;
    }

    @Bean(name = "stringDecoder")
    public StringDecoder stringDecoder() {
        return new StringDecoder(StandardCharsets.UTF_8);
    }

    @Bean(name = "lineBasedFrameDecoder")
    @Scope("prototype")
    public LineBasedFrameDecoder lineBasedFrameDecoder(){
        return new LineBasedFrameDecoder(lineMaxLength);
    }

    @Bean(name = "stringCrEncoder")
    public StringCrEncoder stringCrEncoder(){return new StringCrEncoder(StandardCharsets.UTF_8);}
}