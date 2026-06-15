package cn.org.alan.exam.model.form.ai;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Min;

/**
 * AI阅卷请求表单
 */
@Data
public class AiGradingForm {
    
    @NotNull(message = "考试ID不能为空")
    private Integer examId;
    
    private Integer userId;
    
    @NotNull(message = "题目ID不能为空")
    @Min(value = 1, message = "题目ID必须大于0")
    private Integer questionId;
    
    @NotNull(message = "用户答案不能为空")
    private String userAnswer;
    
    private String questionType;
    
    @ApiModelProperty("题目内容")
    private String questionContent;
    
    @ApiModelProperty("标准答案（由系统自动填充）")
    private String standardAnswer;

    @ApiModelProperty("阅卷模式: true=教师评分模式(保存分数), false=学生解析模式(仅分析)")
    private Boolean gradingMode;

    @ApiModelProperty("AI配置ID（可选，不传则使用激活的默认配置）")
    private Integer configId;
}