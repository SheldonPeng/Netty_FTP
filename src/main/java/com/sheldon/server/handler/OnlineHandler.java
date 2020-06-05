package com.sheldon.server.handler;

import com.sheldon.excepetion.CommandException;
import com.sheldon.model.ResponseEnum;
import com.sheldon.server.supervise.ClientSupervise;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelId;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.log4j.Log4j2;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @Description: 客户端上线与下线处理器
 * @Author: SheldonPeng
 * @Date: 2020-06-02 15:06
 */
@Component
@Log4j2
@ChannelHandler.Sharable
public class OnlineHandler extends ChannelInboundHandlerAdapter {

    /**
    *@Description: 客户端上线处理方法
    *@Param: [ctx]
    *@Return: void
    *@Author: SheldonPeng
    *@Date: 2020/6/2  15:07
    */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {

        log.info("接收到来自客户端的连接,客户端信息为【{}】",ctx.channel().remoteAddress());
        ChannelId channelId = ctx.channel().id();
        // 添加至客户端连接池
        ClientSupervise.addClient(channelId);
        // 回应客户端连接信息
        ctx.writeAndFlush(ResponseEnum.CONN_SUCCESS_WC.toString());
        ctx.fireChannelActive();
    }

    /**
    *@Description: 客户端下线处理方法
    *@Param: [ctx]
    *@Return: void
    *@Author: SheldonPeng
    *@Date: 2020/6/2  15:07
    */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {

        log.info("检测到客户端下线,客户端ip为【{}】,channelId为【{}】",ctx.channel().remoteAddress(),ctx.channel().id());
        // 移除客户端连接池
        ClientSupervise.removeClient(ctx.channel().id());
        ctx.writeAndFlush(ResponseEnum.CONN_SUCCESS_WC.toString());
        ctx.close().sync().get();
        log.info("客户端下线成功!");

    }
}