package com.pethub.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * JWT 配置项。
 * 这几个字段会从 application.yaml 的 pethub.jwt 下自动读取。
 */
@Data
@Component
@ConfigurationProperties(prefix = "pethub.jwt")
public class JwtProperties {

    /**
     * JWT 密钥。
     */
    private String secretKey;

    /**
     * JWT 有效期，单位：毫秒。
     */
    private Long ttl;

    /**
     * 前端传 token 的请求头名称。
     * 当前项目里使用的是 Authorization。
     */
    private String tokenName;

    /**
     * 签名算法名称，当前支持 HS256 / HS384 / HS512。
     */
    private String algorithm;
}
