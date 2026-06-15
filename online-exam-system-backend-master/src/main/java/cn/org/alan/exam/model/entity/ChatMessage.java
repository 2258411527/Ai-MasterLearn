package cn.org.alan.exam.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 聊天消息实体
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("chat_message")
public class ChatMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 发送者ID
     */
    @TableField("from_user_id")
    private Integer fromUserId;

    /**
     * 接收者ID
     */
    @TableField("to_user_id")
    private Integer toUserId;

    /**
     * 消息内容
     */
    @TableField("content")
    private String content;

    /**
     * 消息类型：1-文本，2-图片，3-文件
     */
    @TableField("message_type")
    private Integer messageType;

    /**
     * 是否已读：0-未读，1-已读
     */
    @TableField("is_read")
    private Integer isRead;

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
     * 是否删除：0-未删除，1-已删除
     */
    @TableField("is_deleted")
    @TableLogic
    private Integer isDeleted;

}