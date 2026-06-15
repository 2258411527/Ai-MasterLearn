package cn.org.alan.exam.model.form.token;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
@ApiModel("Token购买表单")
public class TokenPurchaseForm {

    @NotNull(message = "套餐ID不能为空")
    @ApiModelProperty("套餐ID")
    private Integer packageId;
}
