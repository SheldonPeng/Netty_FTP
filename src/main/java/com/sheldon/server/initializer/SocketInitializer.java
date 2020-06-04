package com.sheldon.server.initializer;


import com.sheldon.server.encode.StringCrEncoder;
import com.sheldon.server.handler.*;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;


/**
*@Description: 服务端配置构造器
*@Author: SheldonPeng
*@Date: 2020/6/1  15:32
*/
@Component
@Log4j2
public class SocketInitializer extends ChannelInitializer<SocketChannel> {

    @Autowired
    private StringCrEncoder crEncoder;
    @Autowired
    private StringDecoder decoder;
    @Autowired
    private OnlineHandler onlineHandler;
    @Autowired
    private FtpCommandHandler ftpCommandHandler;
    @Autowired
    private CommandProcessHandler commandProcessHandler;
    @Autowired
    private LoginHandler loginHandler;
    @Autowired
    private ExceptionHandler exceptionHandler;
    @Autowired
    private ApplicationContext applicationContext;

    //从容器中获取多例handler，防止出现Sharable错误
    private LineBasedFrameDecoder getLineBasedFrameDecoder(){
        return applicationContext.getBean("lineBasedFrameDecoder",LineBasedFrameDecoder.class);
    }

    @Override
    protected void initChannel(SocketChannel channel) {

        channel.pipeline()
                .addLast(crEncoder)
                .addLast(getLineBasedFrameDecoder())
                .addLast(decoder)
                .addLast(onlineHandler)
                .addLast(ftpCommandHandler)
                .addLast(loginHandler)
                .addLast(commandProcessHandler)
                .addLast(exceptionHandler);
    }
}
