package com.sheldon.excepetion;

import com.sheldon.model.FtpCommand;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @Description: FTP命令错误异常
 * @Author: SheldonPeng
 * @Date: 2020-06-02 17:48
 */
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
public class CommandException extends RuntimeException{

    private String message;

}