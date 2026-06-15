package cn.org.alan.exam.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * AI阅卷结果详情实体类
 *
 * @author AI Assistant
 * @since 2025-05-04
 */
@Data
@EqualsAndHashCode(callSuper = false)
@ApiModel("AI阅卷结果详情实体类")
@TableName("t_ai_grading_detail")
public class AiGradingDetail implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("AI阅卷详情ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty("考试ID")
    private Integer examId;

    @ApiModelProperty("用户ID")
    private Integer userId;

    @ApiModelProperty("题目ID")
    private Integer questionId;

    @ApiModelProperty("AI评分")
    private Integer aiScore;

    @ApiModelProperty("详细分析")
    private String detailedAnalysis;

    @ApiModelProperty("改进建议")
    private String improvementSuggestions;

    @ApiModelProperty("知识点分析")
    private String knowledgePoints;

    @ApiModelProperty("选项分析（针对客观题）")
    private String optionAnalysis;

    @ApiModelProperty("相似题目推荐")
    private String similarQuestions;

    @ApiModelProperty("AI阅卷时间")
    private LocalDateTime gradingTime;

    @ApiModelProperty("创建时间")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @ApiModelProperty("更新时间")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}