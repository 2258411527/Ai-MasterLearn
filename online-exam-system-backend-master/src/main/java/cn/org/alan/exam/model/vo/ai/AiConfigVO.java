package cn.org.alan.exam.model.vo.ai;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * AI配置视图对象
 *
 * @author AI Assistant
 * @since 2024
 */
@Data
public class AiConfigVO {
    
    /**
     * 主键ID
     */
    private Integer id;
    
    /**
     * 配置名称
     */
    private String configName;
    
    /**
     * API密钥（显示部分内容）
     */
    private String apiKey;
    
    /**
     * 基础URL
     */
    private String baseUrl;
    
    /**
     * 模型名称
     */
    private String model;
    
    /**
     * 是否激活(0-否,1-是)
     */
    private Boolean isActive;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
    
    /**
     * 创建人姓名
     */
    private String createUserName;
    
    /**
     * 更新人姓名
     */
    private String updateUserName;
    
    /**
     * 获取API密钥的显示格式（隐藏中间部分）
     */
    public String getApiKeyDisplay() {
        if (apiKey == null || apiKey.length() <= 8) {
            return apiKey;
        }
        return apiKey.substring(0, 4) + "****" + apiKey.substring(apiKey.length() - 4);
    }
}