package cn.org.alan.exam.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 好友关系实体
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("friend_relation")
public class FriendRelation implements Serializable {

    private static final long serialVersionUID = 1L;

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
     * 好友ID
     */
    @TableField("friend_id")
    private Integer friendId;

    /**
     * 好友备注
     */
    @TableField("remark")
    private String remark;

    /**
     * 好友名称（来自关联查询，非数据库字段）
     */
    @TableField(exist = false)
    private String friendName;

    /**
     * 好友头像（来自关联查询，非数据库字段）
     */
    @TableField(exist = false)
    private String friendAvatar;

    /**
     * 好友角色ID（来自关联查询，非数据库字段）
     */
    @TableField(exist = false)
    private Integer friendRole;

    /**
     * 显示名称（优先使用备注，其次使用好友名称）
     */
    @TableField(exist = false)
    private String displayName;

    /**
     * 关系状态：0-待审核，1-已同意，2-已拒绝，3-已删除
     */
    @TableField("status")
    private Integer status;

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