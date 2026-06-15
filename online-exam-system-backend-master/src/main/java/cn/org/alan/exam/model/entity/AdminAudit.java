package cn.org.alan.exam.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("t_admin_audit")
@ApiModel(value = "AdminAudit对象", description = "管理员审核记录")
public class AdminAudit implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("主键ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty("申请用户ID")
    private Integer userId;

    @ApiModelProperty("审核人ID")
    private Integer auditorId;

    @ApiModelProperty("申请的管理员等级：2-一级管理员，3-二级管理员")
    private Integer adminLevel;

    @ApiModelProperty("审核状态：0-待审核，1-审核通过，2-审核拒绝")
    private Integer auditStatus;

    @ApiModelProperty("审核备注")
    private String auditRemark;

    @ApiModelProperty("审核时间")
    private LocalDateTime auditTime;

    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty("逻辑删除：0-未删除，1-已删除")
    private Integer isDeleted;
}