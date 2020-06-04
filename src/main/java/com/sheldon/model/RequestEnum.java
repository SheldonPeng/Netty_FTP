package com.sheldon.model;

import com.sheldon.excepetion.CommandException;

public enum RequestEnum {

    // OPTS
    OPTS("OPTS",3),
    // 密码
    PASS("PASS",2),
    // 用户名
    USER("USER",2),
    // 返回当前路径
    XPWD("XPWD",1),
    PWD("PWD",1),
    // 进入指定路径  对应客户端 cd命令
    CWD("CWD",2),

    // 创建文件夹
    XMKD("XMKD",1),
    MKD("MKD",1),

    // 删除目录
    XRMD("XRMD",1),
    RMD("RMD",1),
    // 删除文件
    DELE("DELE",1),

    // 当前目录下的所有文件详细信息 对应客户端 dir命令
    LIST("LIST",1),
    // 当前目录下的所有文件的列表信息，对应客户端 ls命令
    NLST("NLST",1),
    // 主动模式
    PORT("PORT",2),
    // 被动模式
    PASV("PASV",2);
    
    
    private final String command;
    private final int minLength;
    RequestEnum(String command , int minLength){
        this.command = command;
        this.minLength = minLength;
    }

    public static int getMinLength(String command){

        for(RequestEnum requestEnum : values()){
            if ( requestEnum.command.equals(command)){
                return requestEnum.minLength;
            }
        }
        throw new CommandException(command);
    }

    public static RequestEnum getCommand(String command){

        for(RequestEnum requestEnum : values()){
            if ( requestEnum.command.equals(command)){
                return requestEnum;
            }
        }
        throw new CommandException(command);
    }
}
