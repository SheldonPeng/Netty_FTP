package com.sheldon.server.handler;


import ch.qos.logback.core.net.server.Client;
import com.sheldon.factory.file.FileFactory;
import com.sheldon.factory.pasv.PasvFactory;
import com.sheldon.factory.port.PortFactory;
import com.sheldon.model.FtpCommand;
import com.sheldon.model.FtpSession;
import com.sheldon.model.FtpState;
import com.sheldon.model.ResponseEnum;
import com.sheldon.server.supervise.ClientSupervise;
import com.sheldon.util.ByteBufUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.handler.codec.string.LineSeparator;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


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
    @Autowired
    private PasvFactory pasvFactory;

    private static final int CORE_COUNT = Runtime.getRuntime().availableProcessors();
    private static ThreadPoolExecutor bizThreadPool = new ThreadPoolExecutor(CORE_COUNT, CORE_COUNT, 0L, TimeUnit.SECONDS, new ArrayBlockingQueue<>(1024));

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
            // 当前系统信息
            case SYST: {
                ctx.writeAndFlush(ResponseEnum.CONN_SUCCESS_WC.toString());
                break;
            }
            case TYPE: {
                type(ctx, command);
                break;
            }
            // 改变当前目录
            case CWD: {
                cwd(ctx, command);
                break;
            }
            // 读取当前路径
            case PWD:
            case XPWD: {
                xpwd(ctx);
                break;
            }
            // 主动模式
            case PORT: {
                portFactory.connect(ctx, command.getParams().get(0));
                // 设置当前模式为被动模式
                FtpSession session = ClientSupervise.getSession(ctx.channel().id());
                session.setMode(FtpSession.PASV_MODE);
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
            case XMKD: {
                xmkd(ctx, command);
                break;
            }
            // 删除目录
            case RMD:
            case XRMD: {
                rmd(ctx, command);
                break;
            }
            // 删除文件
            case DELE: {
                dele(ctx, command);
                break;
            }
            // 返回文件大小
            case SIZE: {

                size(ctx, command);
                break;
            }
            // 下载文件
            case RETR: {
                retr(ctx, command);
                break;
            }
            case STOR: {
                stor(ctx, command);
                break;
            }
            case PASV: {
                pasv(ctx);
                break;
            }
            // 退出
            case QUIT: {
                ctx.close();
                break;
            }
        }

    }

    private void pasv(ChannelHandlerContext ctx) throws UnknownHostException, InterruptedException, ExecutionException {


        FtpSession session = ClientSupervise.getSession(ctx.channel().id());

        // 计算随机端口号 50000 - 60000   256*185 - 256*234
        int param1 = (int) (Math.random() * 49 + 185);
        int param2 = (int) (Math.random() * 100);
        // 组合成符合格式的IP、端口组合
        String hostAddress = InetAddress.getLocalHost().getHostAddress();
        hostAddress = hostAddress.replace(".", ",");
        hostAddress = hostAddress + "," + param1 + "," + param2;

        // 设置当前模式为被动模式
        session.setMode(FtpSession.PASV_MODE);
        // 绑定对应端口
        pasvFactory.bind(ctx, param1 * 256 + param2);
        // 通知客户端
        ctx.writeAndFlush(ResponseEnum.PASV_SUCCESS.toString().replace("{}", hostAddress));
    }

    private void stor(ChannelHandlerContext ctx, FtpCommand ftpCommand) {

        // 获取用户的信息，并且判断用户是否已打开连接
        FtpSession session = ClientSupervise.getSession(ctx.channel().id());
        if (session.getFtpState().getState() < FtpState.READY_TRANSFORM) {
            ctx.writeAndFlush(ResponseEnum.UNIDENTIFY_CONNECTION.toString());
            return;
        }
        String param = ftpCommand.getParams().get(0);
        String filePath;

        // 判断文件是相对路径还是绝对路径
        if (param.startsWith("/")) {
            filePath = fileFactory.getRootPath() + param;
        } else {
            filePath = session.getPresentFile() + param;
        }

        // 判断文件是否存在或者对应路径下为目录
        File file = new File(filePath);
        if (file.exists() || file.isDirectory()) {
            ctx.writeAndFlush(ResponseEnum.UPLOAD_FAIL.toString());
            return;
        }
        // 将用户的文件信息存入data中，并告知客户端连接已成功打开
        session.getFtpState().setData(file);
        ctx.writeAndFlush(ResponseEnum.DATA_CONNECTION_OK.toString());
    }

    private void retr(ChannelHandlerContext ctx, FtpCommand ftpCommand) throws IOException, InterruptedException, ExecutionException {

        // 获取参数
        String param = ftpCommand.getParams().get(0);
        FtpSession session = ClientSupervise.getSession(ctx.channel().id());
        String filePath;
        // 判断是否打开PORT模式
        if ( session.getMode() == FtpSession.PORT_MODE){
            // 判断用户是否已经准备好传输
            if (session.getFtpState().getState() < FtpState.READY_TRANSFORM) {
                ctx.writeAndFlush(ResponseEnum.UNIDENTIFY_CONNECTION.toString());
                return;
            }

        // 当用户的传输模式为PASV
        } else {

            // 用于计数，防止死循环卡死
            int count = 0;
            // 判断用户是否准备好PASV模式的连接
            while (session.getFtpState().getState() != FtpState.READY_TRANSFORM) {
                if( count > 10 ){
                    ctx.writeAndFlush(ResponseEnum.UNIDENTIFY_CONNECTION.toString());
                    return;
                }
                Thread.sleep(100);
                count++;
            }

        }

        // 获取文件路径，判断文件路径是绝对路径还是相对路径
        if (param.startsWith("/")) {
            filePath = fileFactory.getRootPath() + param;
        } else {
            filePath = session.getPresentFile() + param;
        }
        // 判断文件是否存在以及判断其是否为目录
        File file = new File(filePath);
        if (!file.exists() || file.isDirectory()) {
            ctx.writeAndFlush(ResponseEnum.FILE_NOT_INVALID.toString());
            return;
        }
        // 告知客户端准备传输
        ctx.writeAndFlush(ResponseEnum.DATA_CONNECTION_OK.toString());

        // 采用零拷贝的方式写回客户端
        RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");
        FileRegion fileRegion = new DefaultFileRegion(randomAccessFile.getChannel(), 0, randomAccessFile.length());
        session.getCtx().writeAndFlush(fileRegion);

        //  关闭数据客户端，并且告知客户端数据端口已被关闭
        session.getCtx().close().sync().get();
        session.getFtpState().setState(FtpState.USER_LOGGED);
        ctx.writeAndFlush(ResponseEnum.TRANSFER_COMPLETE.toString());
    }

    private void size(ChannelHandlerContext ctx, FtpCommand ftpCommand) {

        String parm = ftpCommand.getParams().get(0);
        FtpSession session = ClientSupervise.getSession(ctx.channel().id());
        String sizePath;

        if (parm.startsWith("/")) {
            sizePath = fileFactory.getRootPath() + parm;
        } else {
            sizePath = session.getPresentFile() + parm;
        }
        File file = new File(sizePath);

        if (!file.exists() || file.isDirectory()) {
            ctx.writeAndFlush(ResponseEnum.SIZE_DENY.toString());
            return;

        }
        String size = String.valueOf(file.length());
        ctx.writeAndFlush(ResponseEnum.SIZE_ACCEPT.toString().replace("{}", size));
    }

    private void type(ChannelHandlerContext ctx, FtpCommand ftpCommand) {

        if (ftpCommand.getParams().get(0).equals("I")) {
            ctx.writeAndFlush(ResponseEnum.TYPE_SUCCESS.toString() + "I");
        } else if (ftpCommand.getParams().get(0).equals("A")) {
            ctx.writeAndFlush(ResponseEnum.TYPE_SUCCESS.toString() + "A");
        } else {
            ctx.writeAndFlush(ResponseEnum.INVALID_COMMAND.toString());
        }
    }

    private void dele(ChannelHandlerContext ctx, FtpCommand ftpCommand) {

        FtpSession session = ClientSupervise.getSession(ctx.channel().id());
        String param = ftpCommand.getParams().get(0);
        String delePath;

        if (param.startsWith("/")) {
            delePath = fileFactory.getRootPath() + param;
        } else {
            delePath = session.getPresentFile() + param;
        }
        File file = new File(delePath);
        if (file.isDirectory() || !file.delete()) {
            ctx.writeAndFlush(ResponseEnum.DELETE_FILE_FAIL.toString());
            return;
        }
        ctx.writeAndFlush(ResponseEnum.DELETE_FILE_SUCCESS.toString());

    }

    private void rmd(ChannelHandlerContext ctx, FtpCommand ftpCommand) {

        FtpSession session = ClientSupervise.getSession(ctx.channel().id());
        final String param = ftpCommand.getParams().get(0);
        String rmdPath;

        // 防止根目录被删除
        if (param.equals("/")) {
            ctx.writeAndFlush(ResponseEnum.INVALID_COMMAND.toString());
            return;
        }
        if (param.startsWith("/")) {
            rmdPath = fileFactory.getRootPath() + param;
        } else {
            rmdPath = session.getPresentFile() + param;
        }
        File file = new File(rmdPath);
        if (!file.isDirectory() || !file.delete()) {
            ctx.writeAndFlush(ResponseEnum.DELETE_DIRECTORY_FAIL.toString());
            return;
        }
        ctx.writeAndFlush(ResponseEnum.DELETE_DIRECTORY_SUCCESS.toString());

    }

    private void xmkd(ChannelHandlerContext ctx, FtpCommand ftpCommand) {

        final String param = ftpCommand.getParams().get(0);
        FtpSession session = ClientSupervise.getSession(ctx.channel().id());
        String mkdPath;

        if (param.startsWith("/")) {
            mkdPath = fileFactory.getRootPath() + param;
        } else {
            mkdPath = session.getPresentFile() + param;
        }
        File file = new File(mkdPath);
        if (file.exists()) {
            ctx.writeAndFlush(ResponseEnum.FILE_ALREADY_EXISTS.toString());
            return;
        }
        if (!file.mkdirs()) {
            ctx.writeAndFlush(ResponseEnum.FILE_NOT_INVALID.toString());
            return;
        }

        ctx.writeAndFlush(ResponseEnum.FILE_CREATE_SUCCESS.toString());
    }

    private void list(ChannelHandlerContext ctx) throws IOException, InterruptedException, ExecutionException {

        FtpSession session = ClientSupervise.getSession(ctx.channel().id());

        // 当用户属于主动模式时
        if (session.getMode() == FtpSession.PORT_MODE) {
            // 判断用户是否准备好port模式的连接
            if (session.getFtpState().getState() != FtpState.READY_TRANSFORM) {
                ctx.writeAndFlush(ResponseEnum.UNIDENTIFY_CONNECTION.toString());
                return;
            }
            // 获取当前目录下的所有文件详细信息
            String fileInfo = fileFactory.getDetailInfo(session.getPresentFile());

            // 将文件信息写回至客户端
            ctx.writeAndFlush(ResponseEnum.DATA_CONNECTION_OK.toString());
            session.getCtx().channel().writeAndFlush(ByteBufUtil.parse(fileInfo))
                                      .sync()
                                      .get();
            ctx.writeAndFlush(ResponseEnum.TRANSFER_COMPLETE.toString());

        // 当用户属于被动模式
        } else {

            // 用于计数，防止死循环卡死
            int count = 0;
            // 判断用户是否准备好PASV模式的连接
            while (session.getFtpState().getState() != FtpState.READY_TRANSFORM) {
                if( count > 8 ){
                    ctx.writeAndFlush(ResponseEnum.UNIDENTIFY_CONNECTION.toString());
                    return;
                }
                Thread.sleep(100);
                count++;
            }
            // 获取当前目录下的所有文件详细信息
            String fileInfo = fileFactory.getDetailInfo(session.getPresentFile());
            // 将文件信息写回至客户端
            ctx.writeAndFlush(ResponseEnum.PASV_DATA_CONN_SUCC.toString() + session.getFtpState().getData());
            session.getCtx().channel().writeAndFlush(ByteBufUtil.parse(fileInfo))
                                      .sync()
                                      .get();
            //通知客户端关闭
            ctx.writeAndFlush(ResponseEnum.PASV_TRANSFER_COMPLETE.toString());
        }
        // 关闭连接通道
        session.getFtpState().setState(FtpState.USER_LOGGED);
        session.getCtx().close().sync().get();
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
        ctx.writeAndFlush(ResponseEnum.CURRENT_FILE.toString().replace("{}", path));
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
            log.info("客户端【{}】输入的目录{}不存在！", ctx.channel().id(),presentFile);
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
