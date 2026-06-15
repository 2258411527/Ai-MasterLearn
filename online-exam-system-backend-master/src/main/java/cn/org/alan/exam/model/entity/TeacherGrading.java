package cn.org.alan.exam.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 教师阅卷记录实体类
 *
 * @author AI Assistant
 * @since 2025-05-04
 */
@Data
@EqualsAndHashCode(callSuper = false)
@ApiModel("教师阅卷记录实体类")
@TableName("t_teacher_grading")
public class TeacherGrading implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("教师阅卷记录ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty("考试ID")
    private Integer examId;

    @ApiModelProperty("用户ID")
    private Integer userId;

    @ApiModelProperty("题目ID")
    private Integer questionId;

    @ApiModelProperty("教师ID")
    private Integer teacherId;

    @ApiModelProperty("教师评分")
    private Integer teacherScore;

    @ApiModelProperty("教师评语")
    private String teacherComment;

    @ApiModelProperty("阅卷时间")
    private LocalDateTime gradingTime;

    @ApiModelProperty("创建时间")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @ApiModelProperty("更新时间")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}