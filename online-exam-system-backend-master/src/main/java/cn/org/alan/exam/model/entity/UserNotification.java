package cn.org.alan.exam.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@ApiModel("用户通知实体类")
@TableName("t_user_notification")
public class UserNotification implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty("用户ID")
    private Integer userId;

    @ApiModelProperty("通知类型: notice-公告, system-系统通知, friend_request-好友请求, friend_message-好友消息, grading_complete-阅卷完成, token_low-Token不足, system_upgrade-系统升级")
    private String type;

    @ApiModelProperty("关联ID（公告ID等）")
    private Integer relatedId;

    @ApiModelProperty("发送者ID")
    private Integer senderId;

    @ApiModelProperty("发送者名称")
    private String senderName;

    @ApiModelProperty("发送者头像")
    private String senderAvatar;

    @ApiModelProperty("通知标题")
    private String title;

    @ApiModelProperty("通知内容")
    private String content;

    @ApiModelProperty("是否已读 0-未读 1-已读")
    private Integer isRead;

    @ApiModelProperty("是否必读 0-否 1-是")
    private Integer isMustRead;

    @ApiModelProperty("创建时间")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
