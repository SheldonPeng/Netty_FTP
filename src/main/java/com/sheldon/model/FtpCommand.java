package com.sheldon.model;

import com.sheldon.excepetion.CommandException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @Description: FTP命令实体类
 * @Author: SheldonPeng
 * @Date: 2020-06-02 15:39
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FtpCommand implements Serializable {

    // FTP命令
    private RequestEnum requestEnum;

    private List<String> params;

    /**
     * @Description: 对FTP消息进行解码
     * @Param: [message]
     * @Return: void
     * @Author: SheldonPeng
     * @Date: 2020/6/2  17:42
     */
    public FtpCommand(Object message) {

        String[] splitMsg = message.toString().split(" ");
        if(splitMsg.length <= 0 ){
            throw new CommandException(message.toString());
        }
        // 判断当前命令的参数是否规范，如果参数数量小于允许最小参数值，则命令错误
        if ( RequestEnum.getMinLength(splitMsg[0]) > splitMsg.length){
            throw new CommandException(message.toString());
        }

        this.requestEnum = RequestEnum.getCommand(splitMsg[0]);
        // 读取参数,并将其注入命令中
        params = new ArrayList<>();
        this.params.addAll(Arrays.asList(splitMsg)
                   .subList(1, splitMsg.length));

    }
}