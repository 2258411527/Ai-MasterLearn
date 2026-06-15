package cn.org.alan.exam.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * AI聊天记录实体类
 *
 * @author AI Assistant
 * @since 2024
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("t_ai_chat_history")
public class AiChatHistory {
    
    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    
    /**
     * 用户ID
     */
    @TableField("user_id")
    private Integer userId;
    
    /**
     * 角色(user/assistant)
     */
    @TableField("role")
    private String role;
    
    /**
     * 聊天内容
     */
    @TableField("content")
    private String content;
    
    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    /**
     * 是否删除(0-否,1-是)
     */
    @TableField("is_deleted")
    @TableLogic
    private Boolean isDeleted;
}