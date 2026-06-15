package cn.org.alan.exam.model.form.reply;

import lombok.Data;
import org.apache.ibatis.type.Alias;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @author WeiJin
 * @version 1.0
 * @since 2025/4/4 14:09
 */
@Data
@Alias("SimpleReplyForm")
public class ReplyForm {
    @NotNull(message = "讨论id都不能为空")
    private Integer discussionId;

    private Integer parentId;

    @NotBlank(message = "回复内容不能为空")
    private String content;
}
