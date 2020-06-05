package com.sheldon.factory.pasv;

import com.sheldon.factory.pasv.handler.PasvHandler;
import com.sheldon.factory.port.handler.PortHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.local.LocalAddress;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.BindException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutionException;

/**
 * @Description: pasv模式工厂
 * @Author: SheldonPeng
 * @Date: 2020-06-05 18:20
 */
@Component
@Log4j2
public class PasvFactory {

    @Autowired
    private ServerBootstrap pasvBootstrap;


    public void bind(ChannelHandlerContext ctx , int port) throws InterruptedException, ExecutionException {

        // 添加port模式逻辑处理器
        pasvBootstrap.childHandler(new PasvHandler(ctx,port));
        // 监听端口
        pasvBootstrap.bind(port).sync().get();
        log.info("绑定成功" + port);
    }
}