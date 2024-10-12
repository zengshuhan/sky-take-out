package com.sky.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "sky.jwt")//表示当前类是配置属性类 去application.yml中找
@Data
public class JwtProperties {

    /**
     * 管理端员工生成jwt令牌相关配置
     * 读取application.yml封装成对象之后注入controller中
     */
    private String adminSecretKey;
    private long adminTtl;
    private String adminTokenName;

    /**
     * 用户端微信用户生成jwt令牌相关配置
     */
    private String userSecretKey;
    private long userTtl;
    private String userTokenName;

}
