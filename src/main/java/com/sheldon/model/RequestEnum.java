package com.sheldon.model;

import com.sheldon.excepetion.CommandException;

public enum RequestEnum {

    // OPTS
    OPTS("OPTS",3),
    SYST("SYST",1),

    // 当前文件大小
    SIZE("SIZE",2),
    // 密码
    PASS("PASS",2),
    // 用户名
    USER("USER",2),
    // 返回当前路径
    XPWD("XPWD",1),
    PWD("PWD",1),
    // 进入指定路径  对应客户端 cd命令
    CWD("CWD",2),

    // 将文件重命名指定的旧文件
    RNFR("RNFR",2),
    // 重命名指定的新文件
    RNTO("RNTO",2),
    // 两种模式 Binary和Ascii  type i和type a
    TYPE("TYPE",2),
    // 创建文件夹
    XMKD("XMKD",1),
    MKD("MKD",1),

    // 空指令
    NOOP("NOOP",1),
    // 删除目录
    XRMD("XRMD",1),
    RMD("RMD",1),
    // 删除文件
    DELE("DELE",1),

    // 当前目录下的所有文件详细信息 对应客户端 dir命令
    LIST("LIST",1),
    // 当前目录下的所有文件的列表信息，对应客户端 ls命令
    NLST("NLST",1),
    // 下载文件
    RETR("RETR",2),
    // 上传文件
    STOR("STOR",2),
    // 主动模式
    PORT("PORT",2),
    // 被动模式
    PASV("PASV",1),
    // 退出
    QUIT("QUIT",1);

    
    
    private final String command;
    private final int minLength;
    RequestEnum(String command , int minLength){
        this.command = command;
        this.minLength = minLength;
    }

    public static int getMinLength(String command){

        command = command.toUpperCase();
        for(RequestEnum requestEnum : values()){
            if ( requestEnum.command.equals(command)){
                return requestEnum.minLength;
            }
        }
        throw new CommandException(command);
    }

    public static RequestEnum getCommand(String command){

        command = command.toUpperCase();
        for(RequestEnum requestEnum : values()){
            if ( requestEnum.command.equals(command)){
                return requestEnum;
            }
        }
        throw new CommandException(command);
    }
}
