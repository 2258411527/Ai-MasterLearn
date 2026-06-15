package cn.org.alan.exam.model.form.ai;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * AI聊天表单
 *
 * @author AI Assistant
 * @since 2024
 */
@Data
@ApiModel("AI聊天表单")
public class AiChatForm {

    @NotBlank(message = "问题不能为空")
    @ApiModelProperty("用户问题")
    private String question;

    @ApiModelProperty("题目内容（可选，用于上下文增强）")
    private String questionContent;

    @ApiModelProperty("标准答案（可选，用于上下文增强）")
    private String standardAnswer;

    @ApiModelProperty("学生答案（可选，用于上下文增强）")
    private String userAnswer;

    @ApiModelProperty("AI配置ID（可选，不传则使用激活的默认配置）")
    private Integer configId;
}