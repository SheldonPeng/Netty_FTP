package com.sheldon.server.handler;

import com.sheldon.excepetion.CommandException;
import com.sheldon.model.FtpCommand;
import com.sheldon.server.supervise.ClientSupervise;
import com.sun.tools.internal.ws.wsdl.document.soap.SOAPUse;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.log4j.Log4j2;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.RandomAccessFile;

/**
 * @Description: FTP命令控制器
 * @Author: SheldonPeng
 * @Date: 2020-06-02 15:51
 */
@Component
@ChannelHandler.Sharable
@Log4j2
public class FtpCommandHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        log.info("收到来自客户端【{}】的消息:【{}】", ctx.channel().id(), msg);
        FtpCommand ftpCommand = new FtpCommand(msg);
        ctx.fireChannelRead(ftpCommand);
    }
}