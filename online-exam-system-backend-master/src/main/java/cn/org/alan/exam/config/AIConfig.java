package cn.org.alan.exam.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * AI 核心配置类
 * 系统统一使用阿里百炼 DashScope 兼容 OpenAI 接口
 * 模型: qwen3.5-122b-a10b
 *
 * @author AI Assistant
 * @since 2025
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "ai-custom")
public class AIConfig {

    private String apiKey = "";

    private String baseUrl = "https://dashscope.aliyuncs.com/compatible-mode/v1";

    private String model = "qwen3.5-122b-a10b";

    private Integer maxTokens = 2000;

    private Double temperature = 0.7;

    private Boolean enabled = true;

    private Integer timeout = 30000;

    private Integer retryCount = 1;
}