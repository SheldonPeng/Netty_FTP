package com.sheldon.model;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.File;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @Description:
 * @Author: SheldonPeng
 * @Date: 2020-06-02 11:02
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FtpSession implements Serializable {
    // 开始连接时间
    private LocalDateTime startTime;

    // Channel id
    private ChannelId id;

    // 数据通道
    private ChannelHandlerContext ctx;

    // 当前文件位置
    private String presentFile;

    // 模式 PASV --> 0  PORT --->1
    private int mode;
    // 当前状态
    private FtpState ftpState;

    public void setPresentFile(String path){

        if ( path.endsWith("/")){
            this.presentFile = path;
        }else {
            this.presentFile = path + "/";
        }

    }

    public static final int PASV_MODE = 0;
    public static final int PORT_MODE = 1;
}