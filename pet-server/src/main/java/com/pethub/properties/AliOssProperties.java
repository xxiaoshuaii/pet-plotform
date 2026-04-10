package com.pethub.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 阿里云 OSS 配置。
 * 从 application-dev.yaml 的 alioss 节点读取。
 */
@Data
@Component
@ConfigurationProperties(prefix = "alioss")
public class AliOssProperties {

    /**
     * 访问域名，例如 https://oss-cn-beijing.aliyuncs.com
     */
    private String endpoint;

    /**
     * 存储桶名称。
     */
    private String bucketName;

    /**
     * 区域，例如 cn-beijing。
     */
    private String region;

    /**
     * 访问密钥 ID。
     */
    private String accessKeyId;

    /**
     * 访问密钥 Secret。
     */
    private String accessKeySecret;
}
