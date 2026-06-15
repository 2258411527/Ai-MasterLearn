package cn.org.alan.exam.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户备考目标实体类
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("t_user_study_goal")
@ApiModel(value = "UserStudyGoal对象", description = "用户备考目标")
public class UserStudyGoal implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty("用户ID")
    private Integer userId;

    @ApiModelProperty("考试类型：考研/考公/双线备考")
    private String examType;

    @ApiModelProperty("目标年份：2026/2027等")
    private Integer targetYear;

    @ApiModelProperty("备考身份：应届本科/往届脱产/在职上班族")
    private String studyIdentity;

    @ApiModelProperty("每日学习时长：2h内/3-4h/5-7h/8h以上")
    private String dailyStudyHours;

    @ApiModelProperty("当前备考进度：未开始/刚开始/备考中已有基础")
    private String currentProgress;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    @ApiModelProperty("更新时间")
    private LocalDateTime updateTime;
}