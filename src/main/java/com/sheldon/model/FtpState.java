package com.sheldon.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @Description: FTP连接客户端当前状态
 * @Author: SheldonPeng
 * @Date: 2020-06-02 11:36
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FtpState implements Serializable {

    // 客户端连接
    public static final int CONNECTED = 0;

    // 客户端输入完用户名，等待输入密码
    public static final int USER_NAME_READY = 1;

    // 客户端登录成功
    public static final int USER_LOGGED = 2;

    // 客户端等待传输
    public static final int READY_TRANSFORM = 3;

    // 当前状态代码
    private int state;

    // 当前数据
    private Object data;

    // 加锁，防止状态更新冲突
    public synchronized void setState(int futureState) {
        this.state = futureState;
    }
}