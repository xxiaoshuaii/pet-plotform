package com.pethub.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "pethub.ai.qwen")
public class QwenProperties {

    /**
     * 是否启用千问大模型。
     */
    private boolean enabled = true;

    /**
     * DashScope OpenAI 兼容模式 base url，例如：
     * https://dashscope.aliyuncs.com/compatible-mode/v1
     */
    private String baseUrl = "https://dashscope.aliyuncs.com/compatible-mode/v1";

    /**
     * 阿里云百炼 / DashScope API Key。
     */
    private String apiKey;

    /**
     * 默认模型名。
     */
    private String model = "qwen3.6-plus";

    /**
     * 视觉模型名，处理图片理解时使用。
     */
    private String visionModel = "qwen3.6-plus";

    /**
     * 生成温度。
     */
    private Double temperature = 0.4D;


}
