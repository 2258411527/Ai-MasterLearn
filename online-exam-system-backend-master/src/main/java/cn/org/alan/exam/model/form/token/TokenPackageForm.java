package cn.org.alan.exam.model.form.token;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
@ApiModel("Token套餐表单")
public class TokenPackageForm {

    @NotBlank(message = "套餐名称不能为空")
    @ApiModelProperty("套餐名称")
    private String name;

    @NotNull(message = "Token数量不能为空")
    @ApiModelProperty("Token数量")
    private Integer tokens;

    @NotNull(message = "价格不能为空")
    @ApiModelProperty("价格(元)")
    private BigDecimal price;

    @ApiModelProperty("折扣比例，如0.80表示8折")
    private BigDecimal discount;

    @ApiModelProperty("原价")
    private BigDecimal originalPrice;

    @ApiModelProperty("套餐描述")
    private String description;

    @ApiModelProperty("是否启用: 0-否 1-是")
    private Boolean isActive;

    @ApiModelProperty("排序")
    private Integer sortOrder;
}
