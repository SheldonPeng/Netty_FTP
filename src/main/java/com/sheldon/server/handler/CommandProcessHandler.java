package com.sheldon.server.handler;


import com.sheldon.factory.file.FileFactory;
import com.sheldon.factory.port.PortFactory;
import com.sheldon.model.FtpCommand;
import com.sheldon.model.FtpSession;
import com.sheldon.model.FtpState;
import com.sheldon.model.ResponseEnum;
import com.sheldon.server.supervise.ClientSupervise;
import com.sheldon.util.ByteBufUtil;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.string.LineSeparator;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutionException;


/**
 * @Description: FTP命令处理器
 * @Param:
 * @Return:
 * @Author: SheldonPeng
 * @Date: 2020/6/2  21:21
 */
@Component
@Log4j2
@ChannelHandler.Sharable
public class CommandProcessHandler extends ChannelInboundHandlerAdapter {


    @Autowired
    private FileFactory fileFactory;
    @Autowired
    private PortFactory portFactory;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        log.info(msg.toString());
        FtpCommand command = (FtpCommand) msg;
        // 处理控制命令
        switch (command.getRequestEnum()) {

            // 处理OPTS命令
            case OPTS: {
                ctx.writeAndFlush(ResponseEnum.OPTS_SUCCESS_OK.toString());
                break;
            }
            // 改变当前目录
            case CWD: {
                cwd(ctx, command);
                break;
            }
            // 读取当前路径
            case PWD:
            case XPWD:{
                xpwd(ctx);
                break;
            }
            // 主动模式
            case PORT: {
                portFactory.connect(ctx, command.getParams().get(0));
                break;
            }
            // 当前目录文件概述信息
            case NLST: {
                nlst(ctx);
                break;
            }
            // 当前目录文件详细信息
            case LIST: {
                list(ctx);
                break;
            }
            // 创建目录
            case MKD:
            case XMKD:{
                xmkd(ctx,command);
                break;
            }
            // 删除目录
            case RMD:
            case XRMD:{
                rmd(ctx,command);
                break;
            }
            // 删除文件
            case DELE:{
                dele(ctx,command);
                break;
            }

        }

    }

    private void dele(ChannelHandlerContext ctx, FtpCommand ftpCommand) {

        FtpSession session = ClientSupervise.getSession(ctx.channel().id());
        String param = ftpCommand.getParams().get(0);
        String delePath;

        if ( param.startsWith("/")){
            delePath = fileFactory.getRootPath() + param;
        } else {
            delePath = session.getPresentFile() + param;
        }
        File file = new File(delePath);
        if( file.isDirectory() || ! file.delete()){
            ctx.writeAndFlush(ResponseEnum.DELETE_FILE_FAIL.toString());
            return;
        }
        ctx.writeAndFlush(ResponseEnum.DELETE_FILE_SUCCESS.toString());

    }

    private void rmd(ChannelHandlerContext ctx, FtpCommand ftpCommand) {

        FtpSession session = ClientSupervise.getSession(ctx.channel().id());
        final String param = ftpCommand.getParams().get(0);
        String rmdPath;

        if ( param.startsWith("/")){
            rmdPath = fileFactory.getRootPath() + param;
        } else {
            rmdPath = session.getPresentFile() + param;
        }
        File file = new File(rmdPath);
        if (! file.isDirectory() || ! file.delete()){
            ctx.writeAndFlush(ResponseEnum.DELETE_DIRECTORY_FAIL.toString());
            return;
        }
        ctx.writeAndFlush(ResponseEnum.DELETE_DIRECTORY_SUCCESS.toString());

    }


    private void xmkd(ChannelHandlerContext ctx, FtpCommand ftpCommand) {

        final String param = ftpCommand.getParams().get(0);
        FtpSession session = ClientSupervise.getSession(ctx.channel().id());
        String mkdPath;

        if ( param.startsWith("/")){
            mkdPath = fileFactory.getRootPath() + param;
        } else {
            mkdPath = session.getPresentFile() + param;
        }
        File file = new File(mkdPath);
        if ( file.exists()){
            ctx.writeAndFlush(ResponseEnum.FILE_ALREADY_EXISTS.toString());
            return;
        }
        if(! file.mkdirs()){
            ctx.writeAndFlush(ResponseEnum.FILE_NOT_INVALID.toString());
            return;
        }

        ctx.writeAndFlush(ResponseEnum.FILE_CREATE_SUCCESS.toString());
    }

    private void list(ChannelHandlerContext ctx) throws IOException, InterruptedException, ExecutionException {

        FtpSession session = ClientSupervise.getSession(ctx.channel().id());
        // 判断用户是否准备好port模式的连接
        if (session.getFtpState().getState() != FtpState.READY_TRANSFORM) {
            ctx.writeAndFlush(ResponseEnum.UNIDENTIFY_CONNECTION.toString());
        }

        // 获取当前目录下的所有文件详细信息
        String fileInfo = fileFactory.getDetailInfo(session.getPresentFile());
        // 将文件信息写回至客户端
        ctx.writeAndFlush(ResponseEnum.DATA_CONNECTION_OK.toString());
        session.getCtx().channel().writeAndFlush(ByteBufUtil.parse(fileInfo))
                                  .sync()
                                  .get();
        // 关闭连接通道，并通知客户端关闭
        session.getCtx().close().sync().get();
        session.getFtpState().setState(FtpState.USER_LOGGED);
        ctx.writeAndFlush(ResponseEnum.TRANSFER_COMPLETE.toString());
    }

    private void nlst(ChannelHandlerContext ctx) throws ExecutionException, InterruptedException {

        FtpSession session = ClientSupervise.getSession(ctx.channel().id());
        // 判断用户是否准备好port模式的连接
        if (session.getFtpState().getState() != FtpState.READY_TRANSFORM) {
            ctx.writeAndFlush(ResponseEnum.UNIDENTIFY_CONNECTION.toString());
        }
        // 获取当前目录下的所有文件的简单信息
        String fileInfo = fileFactory.getSimpleInfo(session.getPresentFile());

        // 将文件信息写回至客户端
        ctx.writeAndFlush(ResponseEnum.DATA_CONNECTION_OK.toString());
        session.getCtx().channel().writeAndFlush(ByteBufUtil.parse(fileInfo))
                                  .sync()
                                  .get();
        // 关闭连接通道，并通知客户端关闭
        session.getCtx().close().sync().get();
        session.getFtpState().setState(FtpState.USER_LOGGED);
        ctx.writeAndFlush(ResponseEnum.TRANSFER_COMPLETE.toString());
    }


    private void xpwd(ChannelHandlerContext ctx) {

        FtpSession session = ClientSupervise.getSession(ctx.channel().id());
        String presentFile = session.getPresentFile();

        String path;

        if (presentFile.equals(fileFactory.getRootPath() + "/")) {
            path = "/";
        } else {
            // 去除本地真实root路径
            path = presentFile.replace(fileFactory.getRootPath(), "");
            // 去除尾部的"/"
            path = path.substring(0, path.length() - 1);
        }
        ctx.writeAndFlush(ResponseEnum.CURRENT_FILE.toString() + path);
    }

    private void cwd(ChannelHandlerContext ctx, FtpCommand ftpCommand) throws IOException {

        String rootPath = fileFactory.getRootPath();
        FtpSession session = ClientSupervise.getSession(ctx.channel().id());
        String param = ftpCommand.getParams().get(0);
        String presentFile;

        //  判断cd命令是从根目录开始还是从当前目录开始
        if (param.startsWith("/")) {
            presentFile = rootPath + param;
        } else {
            presentFile = session.getPresentFile() + param;
        }


        File file = new File(presentFile);
        if (!(file.exists() && file.isDirectory())) {
            ctx.writeAndFlush(ResponseEnum.FILE_NOT_INVALID.toString());
            log.info("客户端【{}】输入的目录不存在！", ctx.channel().id());
            return;
        }
        presentFile = file.getCanonicalPath();
        log.info("当前目录为：" + presentFile);

        if (presentFile.length() < rootPath.length()) {
            session.setPresentFile(rootPath);

        } else {
            session.setPresentFile(file.getCanonicalPath());
        }
        log.info("客户端【{}】执行CWD命令成功！", ctx.channel().id());
        ctx.writeAndFlush(ResponseEnum.CWD_SUCCESS.toString());
    }

}
