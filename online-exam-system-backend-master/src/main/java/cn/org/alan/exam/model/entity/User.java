package cn.org.alan.exam.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户实体类
 *
 * @author WeiJin
 * @since 2024-03-21
 */
@Data
@ApiModel("用户实体类")
@TableName("t_user")
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("用户ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty("用户名")
    private String userName;

    @ApiModelProperty("真实姓名")
    private String realName;

    @ApiModelProperty("密码")
    @TableField(fill = FieldFill.INSERT)
    private String password;

    @ApiModelProperty("头像地址")
    private String avatar;

    @ApiModelProperty("角色ID")
    @TableField(fill = FieldFill.INSERT)
    private Integer roleId;

    @ApiModelProperty("加入到班级ID")
    private Integer gradeId;

    /**
     * YYYY-MM-DD hh:mm:ss
     */
    @ApiModelProperty("创建时间")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 1正常0禁用
     */
    @ApiModelProperty("状态")
    private Integer status;

    @ApiModelProperty("审核状态：0-待审核，1-审核通过，2-审核拒绝")
    private Integer auditStatus;

    @ApiModelProperty("审核备注")
    private String auditRemark;

    @ApiModelProperty("审核时间")
    private LocalDateTime auditTime;

    @ApiModelProperty("审核人ID")
    private Integer auditorId;

    @ApiModelProperty("管理员等级：0-非管理员，1-系统管理员，2-一级管理员，3-二级管理员")
    private Integer adminLevel;

    @ApiModelProperty("最大可加入班级数量：0-无限制，1-一个班级，99-多个班级")
    private Integer maxGradeCount;

    @ApiModelProperty("是否可以添加班级：0-否，1-是")
    private Integer canAddGrade;

    @ApiModelProperty("是否可删除：0-否，1-是")
    private Integer canDelete;

    @TableLogic
    @ApiModelProperty("逻辑删除字段")
    private Integer isDeleted;

}