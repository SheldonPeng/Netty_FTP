package com.sheldon.server.supervise;

import com.sheldon.excepetion.StateException;
import com.sheldon.model.FtpSession;
import com.sheldon.model.FtpState;
import io.netty.channel.ChannelId;
import lombok.extern.log4j.Log4j2;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Description: 客户端连接池
 * @Author: SheldonPeng
 * @Date: 2020-06-02 12:25
 */
@Slf4j
public class ClientSupervise {

    private static final Map<ChannelId, FtpSession> clients = new ConcurrentHashMap<>();


    public ClientSupervise(){};

    public static void addClient(ChannelId channelId){

        FtpState ftpState = new FtpState();
        ftpState.setState(FtpState.CONNECTED);
        FtpSession ftpSession = FtpSession.builder()
                                          .id(channelId)
                                          .ftpState(ftpState)
                                          .startTime(LocalDateTime.now())
                                          .build();
        log.info("客户端添加成功，channelID为【{}】",channelId);
        clients.put(channelId,ftpSession);
    }

    public static FtpSession getSession(ChannelId channelId) {
        FtpSession session = clients.get(channelId);
        if ( session == null ){
            throw new StateException();
        }
        return session;
    }

    public static void removeClient(ChannelId id) {
        clients.remove(id);
    }
}