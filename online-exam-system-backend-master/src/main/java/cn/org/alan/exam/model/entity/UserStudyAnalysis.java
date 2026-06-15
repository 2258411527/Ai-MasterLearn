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
 * 用户学习分析实体类
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("t_user_study_analysis")
@ApiModel(value = "UserStudyAnalysis对象", description = "用户学习分析")
public class UserStudyAnalysis implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty("用户ID")
    private Integer userId;

    @ApiModelProperty("总学习天数")
    private Integer totalStudyDays;

    @ApiModelProperty("总学习时长（小时）")
    private Double totalStudyHours;

    @ApiModelProperty("日均学习时长")
    private Double avgDailyStudyHours;

    @ApiModelProperty("参加考试总数")
    private Integer totalExamsTaken;

    @ApiModelProperty("答题总数")
    private Integer totalQuestionsAnswered;

    @ApiModelProperty("正确率")
    private Double correctRate;

    @ApiModelProperty("薄弱知识点（JSON格式）")
    private String weakKnowledgePoints;

    @ApiModelProperty("优势知识点（JSON格式）")
    private String strongKnowledgePoints;

    @ApiModelProperty("学习趋势分析（JSON格式）")
    private String learningTrend;

    @ApiModelProperty("AI推荐内容（JSON格式）")
    private String aiRecommendations;

    @ApiModelProperty("最后分析时间")
    private LocalDateTime lastAnalysisTime;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    @ApiModelProperty("更新时间")
    private LocalDateTime updateTime;
}