package com.sheldon.server.handler;

import com.sheldon.factory.file.FileFactory;
import com.sheldon.model.FtpCommand;
import com.sheldon.model.FtpSession;
import com.sheldon.model.FtpState;
import com.sheldon.model.ResponseEnum;
import com.sheldon.model.config.UserConfig;
import com.sheldon.server.supervise.ClientSupervise;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


/**
 * @Description: 客户端登录处理器
 * @Author: SheldonPeng
 * @Date: 2020-06-02 21:27
 */
@ChannelHandler.Sharable
@Component
@Log4j2
public class LoginHandler extends ChannelInboundHandlerAdapter {

    @Autowired
    private UserConfig userConfig;
    @Autowired
    private FileFactory fileFactory;


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        FtpCommand ftpCommand = (FtpCommand) msg;
        FtpSession session = ClientSupervise.getSession(ctx.channel().id());

        switch (ftpCommand.getRequestEnum()) {

            case USER: {

                String param = ftpCommand.getParams().get(0);
                if (param.equals("anonymous")) {

                    session.getFtpState().setState(FtpState.USER_LOGGED);
                    session.setPresentFile(fileFactory.getRootPath());
                    ctx.writeAndFlush(ResponseEnum.USER_LOGGED.toString());
                } else {
                    session.getFtpState().setData(param);
                    session.getFtpState().setState(FtpState.USER_NAME_READY);
                    ctx.writeAndFlush(ResponseEnum.USER_NEED_PASSWORD.toString());
                }
                break;
            }
            case PASS: {
                if (session.getFtpState().getData() == null || session.getFtpState().getState() != FtpState.USER_NAME_READY) {
                    ctx.writeAndFlush(ResponseEnum.USER_NOT_IDENTIFY.toString());

                } else {

                    String username = (String) session.getFtpState().getData();
                    String param = ftpCommand.getParams().get(0);
                    log.info("客户端尝试使用username:{},password:{} 进行登录", username, userConfig.getUsers().get(username));
                    if (userConfig.getUsers().containsKey(username)
                            && userConfig.getUsers().get(username).equals(param)) {
                        session.getFtpState().setState(FtpState.USER_LOGGED);
                        session.setPresentFile(fileFactory.getRootPath());
                        ctx.writeAndFlush(ResponseEnum.USER_LOGGED.toString());
                    } else {

                        session.getFtpState().setState(FtpState.CONNECTED);
                        ctx.writeAndFlush(ResponseEnum.PASS_INCORRECT.toString());
                    }
                    break;
                }
            }
            default: {
                if (session.getFtpState().getState() >= FtpState.USER_LOGGED) {
                    ctx.fireChannelRead(msg);
                } else {
                    ctx.writeAndFlush(ResponseEnum.NOT_LOGIN.toString());
                }
                break;
            }
        }

    }
}