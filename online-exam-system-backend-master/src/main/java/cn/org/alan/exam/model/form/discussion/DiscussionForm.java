package cn.org.alan.exam.model.form.discussion;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 讨论入参
 *
 * @author WeiJin
 * @version 1.0
 * @since 2025/4/3 9:32
 */
@Data
public class DiscussionForm {
    private Integer gradeId;

    @NotBlank(message = "标题不能为空")
    private String title;

    @NotBlank(message = "内容不能为空")
    private String content;

    private Integer visibility = 0; // 默认为指定班级可见

}
