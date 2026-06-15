package cn.org.alan.exam.model.form.discussion;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 评论入参
 *
 * @author WeiJin
 * @version 1.0
 * @since 2026/05/01
 */
@Data
public class ReplyForm {
    @NotNull(message = "讨论id不能为空")
    private Integer discussionId;
    
    @NotBlank(message = "评论内容不能为空")
    private String content;
    
    @NotNull(message = "可见范围不能为空")
    private Integer visibility;
    
    private Integer parentId;
}