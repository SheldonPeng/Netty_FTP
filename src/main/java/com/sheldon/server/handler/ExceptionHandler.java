package com.sheldon.server.handler;

import com.sheldon.excepetion.CommandException;
import com.sheldon.model.ResponseEnum;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * @Description: 统一异常处理器
 * @Author: SheldonPeng
 * @Date: 2020-06-02 19:03
 */
@Component
@ChannelHandler.Sharable
@Log4j2
public class ExceptionHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {

        log.warn(cause.getMessage());
        if ( cause instanceof CommandException){
            log.info("来自客户端【{}】发送的命令：【{}】解析失败!",ctx.channel().id(),cause.getMessage());
            ctx.writeAndFlush(ResponseEnum.INVALID_COMMAND.toString());

        } else if ( cause instanceof IOException){
            ctx.writeAndFlush(ResponseEnum.FILE_NOT_INVALID.toString());

        } else if ( cause.getMessage().equals("Connection reset by peer")){

        } else if( cause.getMessage().equals("java.net.BindException: Address already in use")){
            ctx.writeAndFlush(ResponseEnum.UNIDENTIFY_CONNECTION.toString());
        }

        //ctx.writeAndFlush(ResponseEnum.INVALID_COMMAND.toString());
    }
}