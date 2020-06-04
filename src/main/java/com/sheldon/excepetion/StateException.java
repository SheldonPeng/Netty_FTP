package com.sheldon.excepetion;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @Description: 客户端登录信息异常
 * @Author: SheldonPeng
 * @Date: 2020-06-02 21:57
 */
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
public class StateException extends RuntimeException{

}