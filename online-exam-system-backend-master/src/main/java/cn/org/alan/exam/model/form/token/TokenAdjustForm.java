package cn.org.alan.exam.model.form.token;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
@ApiModel("管理员调整Token表单")
public class TokenAdjustForm {

    @NotNull(message = "用户ID不能为空")
    @ApiModelProperty("用户ID")
    private Integer userId;

    @NotNull(message = "调整数量不能为空")
    @ApiModelProperty("调整数量(正数为增加,负数为减少)")
    private Integer amount;

    @ApiModelProperty("调整原因")
    private String reason;
}
