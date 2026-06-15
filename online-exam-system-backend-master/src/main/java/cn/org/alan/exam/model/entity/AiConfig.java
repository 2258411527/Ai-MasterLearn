package cn.org.alan.exam.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * AI配置实体类
 *
 * @author AI Assistant
 * @since 2024
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("t_ai_config")
public class AiConfig {
    
    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    
    /**
     * 配置名称
     */
    @TableField("config_name")
    private String configName;
    
    /**
     * API密钥
     */
    @TableField("api_key")
    private String apiKey;

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey != null ? apiKey.trim() : null;
    }

    /**
     * 基础URL
     */
    @TableField("base_url")
    private String baseUrl;

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl != null ? baseUrl.trim() : null;
    }

    public String getBaseUrl() {
        return this.baseUrl != null ? this.baseUrl.trim() : null;
    }
    
    /**
     * 模型名称
     */
    @TableField("model")
    private String model;

    public void setModel(String model) {
        this.model = model != null ? model.trim() : null;
    }
    
    /**
     * 是否激活(0-否,1-是)
     */
    @TableField("is_active")
    private Boolean isActive;
    
    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    /**
     * 创建人ID
     */
    @TableField("create_user_id")
    private Integer createUserId;
    
    /**
     * 更新人ID
     */
    @TableField("update_user_id")
    private Integer updateUserId;
    
    /**
     * 是否删除(0-否,1-是)
     */
    @TableField("is_deleted")
    @TableLogic
    private Boolean isDeleted;
}