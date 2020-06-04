package com.sheldon.model.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @Description:  用户列表配置实体类
 * @Author: SheldonPeng
 * @Date: 2020-06-02 23:20
 */
@Data
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "ftp")
@Component
public class UserConfig {

   Map<String,String>  users;

}