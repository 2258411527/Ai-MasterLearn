package cn.org.alan.exam.model.vo.ai;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * AI聊天记录视图对象
 *
 * @author AI Assistant
 * @since 2024
 */
@Data
public class AiChatHistoryVO {
    
    /**
     * 主键ID
     */
    private Integer id;
    
    /**
     * 用户ID
     */
    private Integer userId;
    
    /**
     * 角色(user/assistant)
     */
    private String role;
    
    /**
     * 聊天内容
     */
    private String content;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 用户姓名
     */
    private String userName;
}