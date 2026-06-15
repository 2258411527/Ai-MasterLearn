package cn.org.alan.exam.model.form.ai;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * AI配置表单类
 *
 * @author AI Assistant
 * @since 2024
 */
@Data
public class AiConfigForm {
    
    /**
     * 配置名称
     */
    @NotBlank(message = "配置名称不能为空")
    private String configName;
    
    /**
     * API密钥
     */
    @NotBlank(message = "API密钥不能为空")
    private String apiKey;
    
    /**
     * 基础URL
     */
    @NotBlank(message = "基础URL不能为空")
    private String baseUrl;
    
    /**
     * 模型名称
     */
    @NotBlank(message = "模型名称不能为空")
    private String model;
    
    /**
     * 是否激活
     */
    private Boolean isActive = false;
}